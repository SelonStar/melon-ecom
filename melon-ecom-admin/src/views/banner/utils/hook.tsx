import "./reset.css";
import { message } from "@/utils/message";
import { usePublicHooks } from "../hooks";
import { addDialog } from "@/components/ReDialog";
import type { PaginationProps } from "@pureadmin/table";
import ReCropper from "@/components/ReCropper";
import { getKeyList, deviceDetection, formatBytes } from "@pureadmin/utils";
import {
  ElMessageBox,
  ElSwitch,
  ElImage,
  ElUpload,
  ElIcon
} from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import { h, ref, computed, reactive, onMounted, type Ref } from "vue";
import {
  getBannerList,
  addBanner,
  updateBanner,
  updateBannerJumpConfig,
  updateBannerStatus,
  deleteBanner,
  deleteBanners
} from "@/api/system";
import { getCategoryTree, type CategoryVO } from "@/api/category";
import { getAllProducts } from "@/api/product";

const JUMP_TYPE_LABEL: Record<string, string> = {
  NONE: "None",
  PRODUCT: "Product",
  CATEGORY: "Category"
};

export function useBanner(formData: any, tableRef: Ref) {
  const dataList = ref([]);
  const loading = ref(true);
  const switchLoadMap = ref({});
  const selectionList = ref([]);
  const latestBlob = ref<Blob | null>(null);
  const latestBase64 = ref<string | null>(null);
  const latestInfo = ref<any>(null);
  const cropperSrc = ref<string>("");
  const cropperKey = ref(0);

  const jumpDialogVisible = ref(false);
  const jumpEditingRow = ref<any>(null);
  const jumpForm = reactive({ jumpType: "NONE", jumpTargetId: null as number | null });
  const productOptions = ref<{ value: number; label: string }[]>([]);
  const productLoading = ref(false);
  const categoryTreeData = ref<CategoryVO[]>([]);

  const { switchStyle } = usePublicHooks();
  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 5,
    currentPage: 1,
    background: true
  });

  const columns: TableColumnList = [
    { label: "Select", type: "selection", fixed: "left", reserveSelection: true },
    { label: "Banner ID", prop: "bannerId", minWidth: 100 },
    {
      label: "Banner Image",
      prop: "bannerUrl",
      minWidth: 400,
      cellRenderer: ({ row }) => (
        <ElImage
          fit="cover"
          preview-teleported={true}
          src={row.bannerUrl}
          preview-src-list={[row.bannerUrl]}
          class="w-[500px] h-[195px] rounded-lg align-middle"
        />
      )
    },
    {
      label: "Jump Type",
      prop: "jumpType",
      minWidth: 100,
      cellRenderer: ({ row }) => (
        <span>{JUMP_TYPE_LABEL[row.jumpType] ?? row.jumpType ?? "None"}</span>
      )
    },
    {
      label: "Jump Target",
      prop: "jumpTargetName",
      minWidth: 150,
      cellRenderer: ({ row }) =>
        row.jumpTargetName ? (
          <span class="text-blue-500">{row.jumpTargetName}</span>
        ) : (
          <span class="text-gray-400">-</span>
        )
    },
    {
      label: "Status",
      prop: "bannerStatus",
      minWidth: 200,
      cellRenderer: scope => (
        <ElSwitch
          v-model={scope.row.bannerStatus}
          activeValue={1}
          inactiveValue={0}
          style={switchStyle.value}
          onChange={val => onChange(scope.row, scope.$index, val)}
        />
      )
    },
    { label: "Actions", fixed: "right", width: 220, slot: "operation" }
  ];

  function onChange(row, index, newValue) {
    ElMessageBox.confirm(
      `Confirm to <strong>${newValue === 0 ? "Disable" : "Enable"}</strong> banner <strong style='color:var(--el-color-primary)'>#${row.bannerId}</strong>?`,
      "System Prompt",
      { confirmButtonText: "Confirm", cancelButtonText: "Cancel", type: "warning", dangerouslyUseHTMLString: true, draggable: true }
    )
      .then(() => {
        switchLoadMap.value[index] = { ...switchLoadMap.value[index], loading: true };
        updateBannerStatus(row.bannerId, newValue)
          .then(res => {
            if (res.code === 0) {
              row.bannerStatus = newValue;
              message("Banner status updated successfully", { type: "success" });
              onSearch();
            } else {
              message("Failed to update banner status: " + res.message, { type: "error" });
              row.bannerStatus = row.bannerStatus === 1 ? 0 : 1;
            }
          })
          .catch(() => { row.bannerStatus = row.bannerStatus === 1 ? 0 : 1; })
          .finally(() => { switchLoadMap.value[index] = { ...switchLoadMap.value[index], loading: false }; });
      })
      .catch(() => { row.bannerStatus = row.bannerStatus === 1 ? 0 : 1; });
  }

  function handleDelete(row) {
    deleteBanner(row.bannerId)
      .then(res => {
        if (res.code === 0) {
          message(`Deleted banner #${row.bannerId}`, { type: "success" });
          onSearch();
        } else {
          message(`Delete failed: ${res.message}`, { type: "error" });
        }
      })
      .catch(err => { message(`Delete failed: ${err}`, { type: "error" }); });
  }

  function handleSizeChange(val: number) { pagination.pageSize = val; onSearch(); }
  function handleCurrentChange(val: number) { pagination.currentPage = val; onSearch(); }
  function handleSelectionChange(val) { selectionList.value = val; }
  function onSelectionCancel() { selectionList.value = []; tableRef.value?.getTableRef()?.clearSelection(); }

  function onBatchDelete() {
    if (selectionList.value.length === 0) { message("Please select records to delete", { type: "warning" }); return; }
    ElMessageBox.confirm(
      `Confirm delete selected <strong>${selectionList.value.length}</strong> banners?`,
      "System Prompt",
      { confirmButtonText: "Confirm", cancelButtonText: "Cancel", type: "warning", dangerouslyUseHTMLString: true, draggable: true }
    )
      .then(() => {
        const ids = getKeyList(selectionList.value, "bannerId");
        loading.value = true;
        deleteBanners(ids)
          .then(res => {
            if (res.code === 0) { message(`Deleted ${ids.length} records`, { type: "success" }); onSearch(); onSelectionCancel(); }
            else { message(`Batch delete failed: ${res.message}`, { type: "error" }); }
          })
          .catch(err => { message(`Batch delete failed: ${err}`, { type: "error" }); })
          .finally(() => { loading.value = false; });
      })
      .catch(() => { });
  }

  async function onSearch() {
    loading.value = true;
    const params = { ...formData, ...pagination };
    params.pageNum = params.currentPage;
    delete params.currentPage;
    delete params.total;
    try {
      const result = await getBannerList(params);
      if (result.code === 0 && result.data && result.data.records) {
        pagination.total = result.data.total;
        dataList.value = result.data.records.map(item => ({ ...item, bannerStatus: item.status }));
      } else {
        dataList.value = [];
        pagination.total = 0;
        message("No banners found", { type: "warning" });
      }
    } catch (error) {
      console.error("Request failed", error);
      message("Session expired, please login again", { type: "error" });
    }
    setTimeout(() => { loading.value = false; }, 500);
  }

  function handleBeforeUpload(file: File) {
    const validTypes = ["image/jpeg", "image/png", "image/webp", "image/bmp"];
    if (!validTypes.includes(file.type)) { message("Only JPG, PNG, WebP and BMP are supported", { type: "warning" }); return false; }
    const reader = new FileReader();
    reader.onload = e => {
      cropperSrc.value = e.target?.result as string;
      cropperKey.value++;
      latestBlob.value = null;
      latestBase64.value = null;
      latestInfo.value = null;
    };
    reader.readAsDataURL(file);
    return false;
  }

  async function searchProducts(keyword: string) {
    if (!keyword) { productOptions.value = []; return; }
    productLoading.value = true;
    try {
      const res = await getAllProducts({ page: 1, pageSize: 20, keyword, status: 1 });
      if (res.code === 0 && res.data?.records) {
        productOptions.value = res.data.records.map((p: any) => ({ value: p.productId, label: p.name }));
      }
    } catch (e) {
      console.error("Search products failed", e);
    } finally {
      productLoading.value = false;
    }
  }

  function openJumpDialog(row: any) {
    jumpEditingRow.value = row;
    jumpForm.jumpType = row.jumpType || "NONE";
    jumpForm.jumpTargetId = row.jumpTargetId || null;
    productOptions.value = [];
    if (row.jumpType === "PRODUCT" && row.jumpTargetId) {
      productOptions.value = [{ value: row.jumpTargetId, label: row.jumpTargetName || String(row.jumpTargetId) }];
    }
    jumpDialogVisible.value = true;
  }

  async function submitJumpConfig() {
    const row = jumpEditingRow.value;
    if (!row) return;
    const jumpType = (jumpForm.jumpType || "NONE") as
      | "NONE"
      | "PRODUCT"
      | "CATEGORY";
    const jumpTargetId =
      jumpForm.jumpTargetId == null || jumpForm.jumpTargetId === ""
        ? null
        : Number(jumpForm.jumpTargetId);

    if (
      (jumpType === "PRODUCT" || jumpType === "CATEGORY") &&
      (jumpTargetId == null || Number.isNaN(jumpTargetId))
    ) {
      message("Please select a valid jump target", { type: "warning" });
      return;
    }

    loading.value = true;
    try {
      const res = await updateBannerJumpConfig(
        row.bannerId,
        jumpType,
        jumpType === "NONE" ? null : jumpTargetId
      );
      if (res.code === 0) {
        message("Jump config saved", { type: "success" });
        jumpDialogVisible.value = false;
        onSearch();
      } else {
        message(`Save failed: ${res.message}`, { type: "error" });
      }
    } catch (e) {
      message(`Save failed: ${e}`, { type: "error" });
    } finally {
      loading.value = false;
    }
  }

  function handleUpload(row?: any) {
    const isEdit = !!row?.bannerId;
    cropperSrc.value = isEdit ? row.bannerUrl : "";
    cropperKey.value++;

    addDialog({
      title: isEdit ? "Edit Banner" : "Add Banner",
      width: "60%",
      draggable: true,
      fullscreen: deviceDetection(),
      closeOnClickModal: false,
      contentRenderer: () => {
        if (!cropperSrc.value) {
          return h("div", { class: "flex flex-col items-center py-8" }, [
            h(ElUpload, { accept: "image/*", showFileList: false, beforeUpload: handleBeforeUpload, drag: true, class: "w-full" }, {
              default: () => h("div", { class: "flex flex-col items-center justify-center border-2 border-dashed border-gray-300 rounded-lg py-16 px-8 cursor-pointer hover:border-blue-400 transition-colors" }, [
                h(ElIcon, { size: 48, color: "#909399" }, () => h(Plus)),
                h("p", { class: "mt-4 text-base text-gray-600" }, "Click or drag image here to upload"),
                h("p", { class: "mt-2 text-sm text-gray-400" }, "Supports JPG / PNG / WebP / BMP formats")
              ])
            })
          ]);
        }
        return h("div", {}, [
          h("div", { class: "flex justify-between items-start" }, [
            h("div", { class: "w-[60%] mr-4" }, [
              h(ReCropper, { key: cropperKey.value, src: cropperSrc.value, options: { aspectRatio: 1080 / 420, viewMode: 1, dragMode: "move" }, onCropper: handleCropperData }),
              h("div", { class: "mt-3 flex items-center justify-center gap-3" }, [
                h(ElUpload, { accept: "image/*", showFileList: false, beforeUpload: handleBeforeUpload }, {
                  default: () => h("button", { class: "inline-flex items-center gap-1 rounded bg-blue-500 px-3 py-1.5 text-sm text-white hover:bg-blue-600 transition-colors", type: "button" }, [h(ElIcon, { size: 14 }, () => h(Plus)), "Re-select image"])
                }),
                h("span", { class: "text-xs text-gray-400" }, "Right click crop area to use more tools")
              ])
            ]),
            h("div", { class: "w-[40%] flex flex-col justify-center items-center" }, [
              latestBase64.value
                ? h(ElImage, { src: latestBase64.value, "preview-src-list": [latestBase64.value], fit: "contain", class: "w-full mb-2 max-h-[200px]" })
                : h("div", { class: "text-gray-400 text-sm" }, "Crop preview will be displayed here..."),
              latestInfo.value
                ? h("div", { class: "mt-2 text-sm text-gray-600" }, [
                  h("p", `Image size: ${parseInt(latestInfo.value.width)} x ${parseInt(latestInfo.value.height)} px`),
                  h("p", `File size: ${formatBytes(latestInfo.value.size)} (${latestInfo.value.size} bytes)`)
                ])
                : null
            ])
          ])
        ]);
      },
      beforeSure: async done => {
        const blob = latestBlob.value;
        if (!blob && !isEdit) { message("Please upload and crop an image first", { type: "warning" }); return; }
        const fd = new FormData();
        if (blob) fd.append("banner", blob, `banner_${Date.now()}.png`);
        if (isEdit) {
          fd.append("jumpType", row.jumpType || "NONE");
          if (row.jumpTargetId != null) fd.append("jumpTargetId", String(row.jumpTargetId));
        }
        loading.value = true;
        try {
          const res = isEdit
            ? await updateBanner(row.bannerId, fd)
            : await addBanner(fd);

          if (res.code === 0) {
            message(isEdit ? "轮播图修改成功" : "轮播图新增成功", {
              type: "success"
            });
            onSearch();
            done();
          } else {
            message(
              `${isEdit ? "轮播图修改失败" : "轮播图新增失败"}: ${res.message}`,
              {
                type: "error"
              }
            );
          }
        } catch (e) {
          message(
            `${isEdit ? "轮播图修改失败" : "轮播图新增失败"}: ${String(e)}`,
            {
              type: "error"
            }
          );
        } finally {
          loading.value = false;
        }
      },
      closeCallBack: () => { latestBlob.value = null; latestBase64.value = null; latestInfo.value = null; cropperSrc.value = ""; }
    });
  }

  function handleCropperData({ blob, base64, info }: { blob: Blob; base64: string; info: any }) {
    latestBlob.value = blob;
    latestBase64.value = base64;
    latestInfo.value = info;
  }

  onMounted(async () => {
    onSearch();
    try {
      const res = await getCategoryTree();
      if (res.code === 0 && res.data) categoryTreeData.value = res.data;
    } catch (e) {
      console.error("Load category tree failed", e);
    }
  });

  return {
    loading,
    columns,
    dataList,
    pagination,
    selectionList,
    selectedNum: computed(() => selectionList.value.length),
     jumpDialogVisible,
    jumpForm,
    productOptions,
    productLoading,
    categoryTreeData,
    searchProducts,
    openJumpDialog,
    submitJumpConfig,
    onSearch,
    handleSizeChange,
    handleCurrentChange,
    handleSelectionChange,
    handleDelete,
    onBatchDelete,
    handleUpload
  };
}
