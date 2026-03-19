import "./reset.css";
import { message } from "@/utils/message";
import { usePublicHooks } from "../hooks";
import type { PaginationProps } from "@pureadmin/table";
import { ElMessageBox, ElSwitch, ElImage, ElTag } from "element-plus";
import { h, ref, reactive, onMounted, type Ref } from "vue";
import {
  getAllProducts,
  updateProductStatus,
  deleteProduct
} from "@/api/product";
import { getCategoryTree } from "@/api/category";
import type { CategoryVO } from "@/api/category";

export function useProduct(tableRef: Ref) {
  const dataList = ref([]);
  const loading = ref(true);
  const switchLoadMap = ref({});
  const selectedNum = ref(0);
  const categoryId = ref<number | null>(null);
  const categoryTree = ref<CategoryVO[]>([
    {
      categoryId: 1,
      name: "手机数码",
      children: []
    },
    {
      categoryId: 2,
      name: "电脑办公",
      children: []
    },
    {
      categoryId: 3,
      name: "家用电器",
      children: []
    },
    {
      categoryId: 4,
      name: "服装鞋帽",
      children: []
    },
    {
      categoryId: 5,
      name: "时尚潮玩",
      children: []
    }
  ]);
  const { switchStyle } = usePublicHooks();

  const searchForm = reactive({
    keyword: "",
    status: null as number | null
  });

  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });

  const columns: TableColumnList = [
    {
      label: "勾选列",
      type: "selection",
      fixed: "left",
      reserveSelection: true
    },
    {
      label: "商品ID",
      prop: "productId",
      width: 80
    },
    {
      label: "主图",
      prop: "mainImageUrl",
      width: 120,
      cellRenderer: ({ row }) => (
        <ElImage
          fit="cover"
          preview-teleported={true}
          src={row.mainImageUrl}
          preview-src-list={row.mainImageUrl ? [row.mainImageUrl] : []}
          class="w-[80px] h-[80px] rounded align-middle"
        />
      )
    },
    {
      label: "商品名称",
      prop: "name",
      minWidth: 180
    },
    {
      label: "副标题",
      prop: "subTitle",
      minWidth: 160
    },
    {
      label: "状态",
      prop: "status",
      width: 100,
      cellRenderer: scope => (
        <ElSwitch
          v-model={scope.row.status}
          activeValue={1}
          inactiveValue={0}
          style={switchStyle.value}
          onChange={val => onStatusChange(scope.row, scope.$index, val)}
        />
      )
    },
    {
      label: "操作",
      fixed: "right",
      width: 180,
      slot: "operation"
    }
  ];

  /** 加载分类树 */
  async function loadCategoryTree() {
    try {
      const res = await getCategoryTree();
      console.log("分类树接口返回", JSON.stringify(res));
      if (res.code === 0 && res.data && res.data.length > 0) {
        categoryTree.value = res.data;
      }
    } catch (e) {
      console.error("加载分类树失败", e);
    }
  }

  /** 切换分类 */
  function onCategoryClick(data: CategoryVO) {
    categoryId.value = data.categoryId;
    pagination.currentPage = 1;
    onSearch();
  }

  /** 清除分类筛选 */
  function onClearCategory() {
    categoryId.value = null;
    pagination.currentPage = 1;
    onSearch();
  }

  /** 搜索商品 */
  async function onSearch() {
    loading.value = true;

    const params = {
      page: pagination.currentPage,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status,
      categoryId: categoryId.value
    };

    try {
      const result = await getAllProducts(params);

      if (result.code === 0 && result.data && result.data.records) {
        pagination.total = result.data.total;
        dataList.value = result.data.records;
      } else {
        dataList.value = [];
        pagination.total = 0;
      }
    } catch (error) {
      console.error("请求失败", error);
      message("请求失败", { type: "error" });
    }

    setTimeout(() => {
      loading.value = false;
    }, 300);
  }

  /** 切换商品状态 */
  function onStatusChange(row, index, newValue) {
    ElMessageBox.confirm(
      `确认要<strong>${newValue === 0 ? "下架" : "上架"
      }</strong><strong style='color:var(--el-color-primary)'> ${row.name} </strong>吗？`,
      "系统提示",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
        dangerouslyUseHTMLString: true,
        draggable: true
      }
    )
      .then(() => {
        switchLoadMap.value[index] = { loading: true };

        updateProductStatus(row.productId, newValue)
          .then(res => {
            if (res.code === 0) {
              row.status = newValue;
              message("已成功修改商品状态", { type: "success" });
              onSearch();
            } else {
              message("修改商品状态失败: " + res.message, { type: "error" });
              row.status = row.status === 1 ? 0 : 1;
            }
          })
          .catch(() => {
            message("修改商品状态失败", { type: "error" });
            row.status = row.status === 1 ? 0 : 1;
          })
          .finally(() => {
            switchLoadMap.value[index] = { loading: false };
          });
      })
      .catch(() => {
        row.status = row.status === 1 ? 0 : 1;
      });
  }

  /** 删除商品 */
  function handleDelete(row) {
    deleteProduct(row.productId)
      .then(res => {
        if (res.code === 0) {
          message(`删除成功 ${row.name}`, { type: "success" });
          onSearch();
        } else {
          message(`删除失败: ${res.message}`, { type: "error" });
        }
      })
      .catch(err => {
        message(`删除失败: ${err}`, { type: "error" });
      });
  }

  function handleSizeChange(val: number) {
    pagination.pageSize = val;
    onSearch();
  }

  function handleCurrentChange(val: number) {
    pagination.currentPage = val;
    onSearch();
  }

  function handleSelectionChange(val) {
    selectedNum.value = val.length;
    tableRef.value?.setAdaptive?.();
  }

  function onSelectionCancel() {
    selectedNum.value = 0;
    tableRef.value?.getTableRef?.()?.clearSelection();
  }

  onMounted(() => {
    loadCategoryTree();
    onSearch();
  });

  return {
    loading,
    columns,
    dataList,
    pagination,
    selectedNum,
    searchForm,
    categoryId,
    categoryTree,
    onSearch,
    onCategoryClick,
    onClearCategory,
    handleDelete,
    handleSizeChange,
    handleCurrentChange,
    handleSelectionChange,
    onSelectionCancel
  };
}