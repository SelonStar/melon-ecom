<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useProduct } from "./utils/hook";
import { PureTableBar } from "@/components/RePureTableBar";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { deviceDetection } from "@pureadmin/utils";
import type { FormInstance } from "element-plus";
import AddFill from "@iconify-icons/ri/add-circle-line";
import EditPen from "@iconify-icons/ep/edit-pen";
import Delete from "@iconify-icons/ep/delete";
import Refresh from "@iconify-icons/ep/refresh";
import View from "@iconify-icons/ep/view";

defineOptions({
  name: "ProductManagement"
});

const router = useRouter();
const formRef = ref<FormInstance>();
const tableRef = ref();

const {
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
} = useProduct(tableRef);

/** 重置表单 */
const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  formEl.resetFields();
  onSearch();
};

/** 跳转到新增页面 */
function goAdd() {
  router.push("/product/edit");
}

/** 跳转到编辑页面 */
function goEdit(row: any) {
  router.push(`/product/edit/${row.productId}`);
}

/** el-tree 配置 */
const treeProps = {
  children: "children",
  label: "name"
};
</script>

<template>
  <div class="flex gap-3">
    <!-- 左侧分类树 -->
    <div
      class="w-[220px] min-w-[220px] bg-bg_color rounded-lg p-4"
      :class="{ hidden: deviceDetection() }"
    >
      <div class="flex justify-between items-center mb-3">
        <span class="font-bold text-base">商品分类</span>
        <el-button
          v-if="categoryId"
          link
          type="primary"
          size="small"
          @click="onClearCategory"
        >
          全部
        </el-button>
      </div>
      <el-tree
        :data="categoryTree"
        :props="treeProps"
        node-key="categoryId"
        highlight-current
        default-expand-all
        :expand-on-click-node="false"
        @node-click="onCategoryClick"
      />
    </div>

    <!-- 右侧商品列表 -->
    <div class="flex-1 overflow-hidden">
      <el-form
        ref="formRef"
        :inline="true"
        :model="searchForm"
        class="search-form bg-bg_color w-full pl-8 pt-[12px] overflow-auto"
      >
        <el-form-item label="关键词：" prop="keyword">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入商品名称"
            clearable
            class="!w-[200px]"
          />
        </el-form-item>
        <el-form-item label="状态：" prop="status">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择"
            clearable
            class="!w-[120px]"
          >
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :icon="useRenderIcon('ri:search-line')"
            :loading="loading"
            @click="onSearch"
          >
            搜索
          </el-button>
          <el-button
            :icon="useRenderIcon(Refresh)"
            @click="resetForm(formRef)"
          >
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <PureTableBar title="商品管理" :columns="columns" @refresh="onSearch">
        <template #buttons>
          <el-button
            type="primary"
            :icon="useRenderIcon(AddFill)"
            @click="goAdd"
          >
            新增商品
          </el-button>
        </template>
        <template v-slot="{ size, dynamicColumns }">
          <div
            v-if="selectedNum > 0"
            v-motion-fade
            class="bg-[var(--el-fill-color-light)] w-full h-[46px] mb-2 pl-4 flex items-center"
          >
            <div class="flex-auto">
              <span
                style="font-size: var(--el-font-size-base)"
                class="text-[rgba(42,46,54,0.5)] dark:text-[rgba(220,220,242,0.5)]"
              >
                已选 {{ selectedNum }} 项
              </span>
              <el-button type="primary" text @click="onSelectionCancel">
                取消选择
              </el-button>
            </div>
          </div>

          <pure-table
            ref="tableRef"
            row-key="productId"
            adaptive
            :adaptiveConfig="{ offsetBottom: 108 }"
            align-whole="center"
            table-layout="auto"
            :loading="loading"
            :size="size"
            :data="dataList"
            :columns="dynamicColumns"
            :pagination="{ ...pagination, size }"
            :header-cell-style="{
              background: 'var(--el-fill-color-light)',
              color: 'var(--el-text-color-primary)'
            }"
            @page-size-change="handleSizeChange"
            @page-current-change="handleCurrentChange"
            @selection-change="handleSelectionChange"
          >
            <template #operation="{ row }">
              <el-button
                class="reset-margin"
                link
                type="primary"
                :size="size"
                :icon="useRenderIcon(EditPen)"
                @click="goEdit(row)"
              >
                编辑
              </el-button>
              <el-popconfirm
                :title="`是否确认删除商品「${row.name}」?`"
                @confirm="handleDelete(row)"
              >
                <template #reference>
                  <el-button
                    class="reset-margin"
                    link
                    type="primary"
                    :size="size"
                    :icon="useRenderIcon(Delete)"
                  >
                    删除
                  </el-button>
                </template>
              </el-popconfirm>
            </template>
          </pure-table>
        </template>
      </PureTableBar>
    </div>
  </div>
</template>

<style scoped lang="scss">
:deep(.el-dropdown-menu__item i) {
  margin: 0;
}

.search-form {
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
}
</style>
