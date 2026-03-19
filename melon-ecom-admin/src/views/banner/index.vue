<script setup lang="ts">
import { ref, reactive } from "vue";
import { useBanner } from "./utils/hook";
import { PureTableBar } from "@/components/RePureTableBar";
import { useRenderIcon } from "@/components/ReIcon/src/hooks";
import { deviceDetection } from "@pureadmin/utils";
import type { FormInstance } from "element-plus";
import AddFill from "@iconify-icons/ri/add-circle-line";
import EditPen from "@iconify-icons/ep/edit-pen";
import Delete from "@iconify-icons/ep/delete";
import Refresh from "@iconify-icons/ep/refresh";
import Link from "@iconify-icons/ep/link";

defineOptions({
  name: "BannerManagement"
});

const formRef = ref<FormInstance>();
const form = reactive({ bannerStatus: null });
const tableRef = ref();

const {
  loading,
  columns,
  dataList,
  pagination,
  selectedNum,
  onSearch,
  handleSizeChange,
  handleCurrentChange,
  handleSelectionChange,
  handleDelete,
  onBatchDelete,
  handleUpload,
  jumpDialogVisible,
  jumpForm,
  productOptions,
  productLoading,
  categoryTreeData,
  searchProducts,
  openJumpDialog,
  submitJumpConfig
} = useBanner(form, tableRef);

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  formEl.resetFields();
  onSearch();
};
</script>

<template>
  <div :class="['flex', 'justify-between', deviceDetection() && 'flex-wrap']">
    <div :class="[deviceDetection() ? ['w-full', 'mt-2'] : 'w-[calc(100%-0px)]']">
      <el-form
        ref="formRef"
        :inline="true"
        :model="form"
        class="search-form bg-bg_color w-[99/100] pl-8 pt-[12px] overflow-auto"
      >
        <el-form-item label="状态：" prop="bannerStatus">
          <el-select v-model="form.bannerStatus" placeholder="请选择" clearable class="!w-[180px]">
            <el-option label="启用" :value="0" />
            <el-option label="禁用" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="useRenderIcon('ri:search-line')" :loading="loading" @click="onSearch">搜索</el-button>
          <el-button :icon="useRenderIcon(Refresh)" @click="resetForm(formRef)">重置</el-button>
        </el-form-item>
      </el-form>

      <PureTableBar title="轮播图管理" :columns="columns" @refresh="onSearch">
        <template #buttons>
          <el-button type="primary" :icon="useRenderIcon(AddFill)" @click="handleUpload()">新增轮播图</el-button>
        </template>
        <template v-slot="{ size, dynamicColumns }">
          <div v-if="selectedNum > 0" v-motion-fade class="bg-[var(--el-fill-color-light)] w-full h-[46px] mb-2 pl-4 flex items-center">
            <div class="flex-auto">
              <span style="font-size: var(--el-font-size-base)" class="text-[rgba(42,46,54,0.5)] dark:text-[rgba(220,220,242,0.5)]">已选 {{ selectedNum }} 项</span>
            </div>
            <el-popconfirm title="是否确认删除?" @confirm="onBatchDelete">
              <template #reference>
                <el-button type="danger" text class="mr-1">批量删除</el-button>
              </template>
            </el-popconfirm>
          </div>

          <pure-table
            ref="tableRef"
            row-key="bannerId"
            adaptive
            :adaptiveConfig="{ offsetBottom: 108 }"
            align-whole="center"
            table-layout="auto"
            :loading="loading"
            :size="size"
            :data="dataList"
            :columns="dynamicColumns"
            :pagination="{ ...pagination, size }"
            :header-cell-style="{ background: 'var(--el-fill-color-light)', color: 'var(--el-text-color-primary)' }"
            @page-size-change="handleSizeChange"
            @page-current-change="handleCurrentChange"
            @selection-change="handleSelectionChange"
          >
            <template #operation="{ row }">
              <el-button class="reset-margin" link type="primary" :size="size" :icon="useRenderIcon(EditPen)" @click="handleUpload(row)">
                编辑图片
              </el-button>
              <el-button class="reset-margin" link type="warning" :size="size" :icon="useRenderIcon(Link)" @click="openJumpDialog(row)">
                跳转配置
              </el-button>
              <el-popconfirm :title="`是否确认删除编号为 ${row.bannerId} 的轮播图?`" @confirm="handleDelete(row)">
                <template #reference>
                  <el-button class="reset-margin" link type="primary" :size="size" :icon="useRenderIcon(Delete)">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </pure-table>
        </template>
      </PureTableBar>
    </div>
  </div>

  <!-- 跳转配置弹窗（标准 el-dialog，Vue 响应式完全可用） -->
  <el-dialog v-model="jumpDialogVisible" title="配置跳转" width="500px" :destroy-on-close="true" :close-on-click-modal="false">
    <div class="space-y-5 py-2">
      <div class="flex items-center gap-3">
        <span class="w-20 text-sm shrink-0 text-gray-700">跳转类型：</span>
        <el-radio-group
          v-model="jumpForm.jumpType"
          @change="() => { jumpForm.jumpTargetId = null; }"
        >
          <el-radio value="NONE">无跳转</el-radio>
          <el-radio value="PRODUCT">商品</el-radio>
          <el-radio value="CATEGORY">分类</el-radio>
        </el-radio-group>
      </div>

      <div v-if="jumpForm.jumpType === 'PRODUCT'" class="flex items-center gap-3">
        <span class="w-20 text-sm shrink-0 text-gray-700">选择商品：</span>
        <el-select
          v-model="jumpForm.jumpTargetId"
          filterable
          remote
          :remote-method="searchProducts"
          :loading="productLoading"
          placeholder="输入商品名称搜索"
          clearable
          class="flex-1"
        >
          <el-option
            v-for="opt in productOptions"
            :key="opt.value"
            :value="opt.value"
            :label="opt.label"
          />
        </el-select>
      </div>

      <div v-if="jumpForm.jumpType === 'CATEGORY'" class="flex items-center gap-3">
        <span class="w-20 text-sm shrink-0 text-gray-700">选择分类：</span>
        <el-tree-select
          v-model="jumpForm.jumpTargetId"
          :data="categoryTreeData"
          :props="{ label: 'name', value: 'categoryId', children: 'children' }"
          placeholder="选择分类"
          filterable
          clearable
          check-strictly
          node-key="categoryId"
          :render-after-expand="false"
          class="flex-1"
        />
      </div>
    </div>

    <template #footer>
      <el-button @click="jumpDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submitJumpConfig">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
:deep(.el-dropdown-menu__item i) { margin: 0; }
.main-content { margin: 24px 24px 0 !important; }
.search-form { :deep(.el-form-item) { margin-bottom: 12px; } }
</style>
