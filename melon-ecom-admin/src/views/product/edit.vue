<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { message } from "@/utils/message";
import {
  getProductDetail,
  addProduct,
  updateProduct,
  uploadImage
} from "@/api/product";
import { getCategoryTree } from "@/api/category";
import type { CategoryVO } from "@/api/category";
import type { SpecAttribute, SkuFormItem } from "./utils/types";
import type { FormInstance, UploadFile, UploadRawFile } from "element-plus";
import { Plus, Delete, ArrowLeft, Edit } from "@element-plus/icons-vue";
import Sortable from "sortablejs";
import ReCropper from "@/components/ReCropper";

defineOptions({
  name: "ProductEdit"
});

const route = useRoute();
const router = useRouter();
const productId = computed(() =>
  route.params.id ? Number(route.params.id) : null
);
const isEdit = computed(() => !!productId.value);
const formRef = ref<FormInstance>();
const saving = ref(false);
const categoryTree = ref<CategoryVO[]>([]);
const fashionTryonPattern = /服装|鞋|帽/;

// === 基本信息 ===
const form = reactive({
  name: "",
  subTitle: "",
  categoryId: null as number | null,
  price: 0,
  mainImageUrl: "",
  detailHtml: "",
  status: 1
});

const mainImageList = ref<UploadFile[]>([]);

// === SKU 规格 ===
const specAttrs = ref<SpecAttribute[]>([]);
const skuList = ref<SkuFormItem[]>([]);
const newAttrName = ref("");

// === 商品详情图 ===
const detailImages = ref<string[]>([]);
const detailImageFiles = ref<UploadFile[]>([]);

// === 表单校验 ===
const rules = {
  name: [{ required: true, message: "Please enter product name", trigger: "blur" }],
  categoryId: [
    { required: true, message: "请选择商品分类", trigger: "change" }
  ]
};

// ===================== 裁剪弹窗 =====================
type CropTarget =
  | { type: "main" }
  | { type: "sku"; index: number }
  | { type: "skuTryon"; index: number };

const cropperVisible = ref(false);
const cropperSrc = ref("");
const cropperBlob = ref<Blob | null>(null);
const cropperUploading = ref(false);
const cropperKey = ref(0);
const cropTarget = ref<CropTarget>({ type: "main" });

function readFileAsDataUrl(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = e => resolve(e.target?.result as string);
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

function onCropperData({ blob }: { blob: Blob }) {
  cropperBlob.value = blob;
}

function openCropperWithSrc(src: string, target: CropTarget) {
  cropperSrc.value = src;
  cropperBlob.value = null;
  cropTarget.value = target;
  cropperKey.value++;
  cropperVisible.value = true;
}

async function openCropperForFile(file: File, target: CropTarget) {
  const base64 = await readFileAsDataUrl(file);
  openCropperWithSrc(base64, target);
}

async function confirmCrop() {
  if (!cropperBlob.value) {
    message("请先完成裁剪操作", { type: "warning" });
    return;
  }
  cropperUploading.value = true;
  try {
    const target = cropTarget.value;
    const imgFile = new File(
      [cropperBlob.value],
      `img_${Date.now()}.png`,
      { type: "image/png" }
    );

    if (target.type === "main") {
      const res = await uploadImage(imgFile, "products");
      const url = pickUploadedUrl(res);
      if (res.code === 0 && url) {
        form.mainImageUrl = url;
        mainImageList.value = [
          { name: "main.png", url, uid: Date.now() } as UploadFile
        ];
        message("主图上传成功", { type: "success" });
      } else {
        message("主图上传失败: " + (res?.message || ""), { type: "error" });
      }
    } else if (target.type === "sku") {
      const res = await uploadImage(imgFile, "products/sku");
      const url = pickUploadedUrl(res);
      if (res.code === 0 && url) {
        skuList.value[target.index].imageUrl = url;
        message("SKU图片上传成功", { type: "success" });
      } else {
        message("SKU图片上传失败: " + (res?.message || ""), { type: "error" });
      }
    } else {
      const res = await uploadImage(imgFile, "products/tryon");
      const url = pickUploadedUrl(res);
      if (res.code === 0 && url) {
        skuList.value[target.index].tryonImageUrl = url;
        message("试穿素材图上传成功", { type: "success" });
      } else {
        message("试穿素材图上传失败: " + (res?.message || ""), { type: "error" });
      }
    }
    cropperVisible.value = false;
  } catch (e: any) {
    message("上传失败: " + (e?.message || e), { type: "error" });
  } finally {
    cropperUploading.value = false;
  }
}

// 主图：文件选中后打开裁剪（on-change 方案，兼容 auto-upload=false）
const mainUploadRef = ref<any>(null);
function handleMainImageChange(uploadFile: UploadFile) {
  if (uploadFile.raw) {
    openCropperForFile(uploadFile.raw, { type: "main" });
    // 清除 el-upload 内部的临时文件记录，防止显示本地预览
    nextTick(() => mainUploadRef.value?.clearFiles());
  }
}

// 主图：点预览（编辑已有图）
function onMainImagePreview(file: UploadFile) {
  if (file.url) openCropperWithSrc(file.url, { type: "main" });
}

// SKU图：选择新文件前拦截
function makeBeforeSkuUpload(index: number) {
  return async (rawFile: UploadRawFile) => {
    await openCropperForFile(rawFile, { type: "sku", index });
    return false;
  };
}

// SKU图：编辑已有图
function editSkuImage(index: number) {
  const url = skuList.value[index].imageUrl;
  if (url) openCropperWithSrc(url, { type: "sku", index });
}

function makeBeforeSkuTryonUpload(index: number) {
  return async (rawFile: UploadRawFile) => {
    await openCropperForFile(rawFile, { type: "skuTryon", index });
    return false;
  };
}

function editSkuTryonImage(index: number) {
  const url = skuList.value[index].tryonImageUrl;
  if (url) openCropperWithSrc(url, { type: "skuTryon", index });
}

function handleTryonEnabledChange(row: SkuFormItem, enabled: boolean) {
  row.aiTryonEnabled = enabled ? 1 : 0;
  if (!enabled) {
    row.tryonCategory = "";
    row.tryonImageUrl = "";
    row.tryonMaskUrl = "";
    row.tryonSort = 0;
  } else if (!row.tryonSort) {
    row.tryonSort = 0;
  }
}

function findCategoryPath(
  list: CategoryVO[],
  targetId: number,
  parents: CategoryVO[] = []
): CategoryVO[] {
  for (const item of list) {
    const currentPath = [...parents, item];
    if (item.categoryId === targetId) return currentPath;
    if (item.children && item.children.length > 0) {
      const childPath = findCategoryPath(item.children, targetId, currentPath);
      if (childPath.length > 0) return childPath;
    }
  }
  return [];
}

const selectedCategoryPath = computed(() => {
  if (!form.categoryId) return [] as CategoryVO[];
  return findCategoryPath(categoryTree.value, form.categoryId);
});

const showTryonWhitelistConfig = computed(() => {
  if (!form.categoryId) return false;
  return selectedCategoryPath.value.some(item => fashionTryonPattern.test(item.name));
});

function resetTryonFields() {
  skuList.value.forEach(row => {
    row.aiTryonEnabled = 0;
    row.tryonCategory = "";
    row.tryonImageUrl = "";
    row.tryonMaskUrl = "";
    row.tryonSort = 0;
  });
}

watch(
  () => showTryonWhitelistConfig.value,
  enabled => {
    if (!enabled) resetTryonFields();
  }
);
// ===================================================

/** 加载分类树 */
async function loadCategoryTree() {
  try {
    const res = await getCategoryTree();
    if (res.code === 0 && res.data && res.data.length > 0) {
      categoryTree.value = res.data;
    }
  } catch (e) {
    console.error("Failed to load category tree", e);
  }
}

/** 编辑模式 - 加载商品详情 */
async function loadProductDetail() {
  if (!productId.value) return;
  try {
    const res = await getProductDetail(productId.value);
    if (res.code === 0 && res.data) {
      const d = res.data;
      form.name = d.name;
      form.subTitle = d.subTitle || "";
      form.categoryId = d.categoryId || null;
      form.price = d.skus?.[0]?.price ?? 0;
      form.mainImageUrl = d.mainImageUrl || "";
      form.detailHtml = d.detailHtml || "";
      form.status = d.status ?? 1;

      // 主图
      if (d.mainImageUrl) {
        mainImageList.value = [
          {
            name: "主图",
            url: d.mainImageUrl,
            uid: Date.now()
          } as UploadFile
        ];
      }

      // SKU
      if (d.skus && d.skus.length > 0) {
        skuList.value = d.skus.map(sku => ({
          skuId: sku.skuId,
          skuCode: sku.skuCode,
          name: sku.name,
          specJson: sku.specJson,
          price: sku.price,
          costPrice: sku.costPrice,
          stock: sku.stock,
          weight: sku.weight,
          imageUrl: sku.imageUrl,
          aiTryonEnabled: sku.aiTryonEnabled ?? 0,
          tryonCategory: sku.tryonCategory || "",
          tryonImageUrl: sku.tryonImageUrl || "",
          tryonMaskUrl: sku.tryonMaskUrl || "",
          tryonSort: sku.tryonSort ?? 0,
          status: sku.status
        }));

        // 从 SKU 的 specJson 反向推导规格属性
        rebuildSpecAttrs();
      }

      // 详情图
      if (d.imageUrls && d.imageUrls.length > 0) {
        detailImages.value = [...d.imageUrls];
        detailImageFiles.value = d.imageUrls.map((url, idx) => ({
          name: `详情图${idx + 1}`,
          url,
          uid: Date.now() + idx
        })) as UploadFile[];
      }
    }
  } catch (e) {
    message("加载商品详情失败", { type: "error" });
  }
}

/** 从 SKU 列表反向推导规格属性（兼容旧 JSON 格式和新纯字符串格式） */
function rebuildSpecAttrs() {
  const names: string[] = [];
  for (const sku of skuList.value) {
    if (!sku.specJson) continue;
    try {
      const spec = JSON.parse(sku.specJson);
      const vals = Object.values(spec) as string[];
      if (vals.length > 0) names.push(vals[0]);
    } catch {
      names.push(sku.specJson);
    }
  }
  specAttrs.value = names.map(name => ({ name, values: [] }));
}

function pickUploadedUrl(res: any): string {
  const dataUrl = typeof res?.data === "string" ? res.data.trim() : "";
  if (dataUrl) return dataUrl;

  const msg = typeof res?.message === "string" ? res.message.trim() : "";
  if (msg.startsWith("http://") || msg.startsWith("https://")) return msg;

  return "";
}

function handleMainImageRemove() {
  form.mainImageUrl = "";
  mainImageList.value = [];
}

// === 规格属性管理 ===
function addSpecAttr() {
  const name = newAttrName.value.trim();
  if (!name) {
    message("Please enter spec name", { type: "warning" });
    return;
  }
  if (specAttrs.value.some(a => a.name === name)) {
    message("Spec name already exists", { type: "warning" });
    return;
  }
  specAttrs.value.push({ name, values: [] });
  newAttrName.value = "";
}

function removeSpecAttr(index: number) {
  specAttrs.value.splice(index, 1);
  generateSkuList();
}

function addSpecValue(attrIndex: number, value: string) {
  const trimmed = value.trim();
  if (!trimmed) return;
  const attr = specAttrs.value[attrIndex];
  if (attr.values.includes(trimmed)) {
    message("规格值已存在", { type: "warning" });
    return;
  }
  attr.values.push(trimmed);
  generateSkuList();
}

function removeSpecValue(attrIndex: number, valIndex: number) {
  specAttrs.value[attrIndex].values.splice(valIndex, 1);
  generateSkuList();
}

function generateSkuList() {
  const attrsWithValues = specAttrs.value.filter(a => a.values.length > 0);

  if (attrsWithValues.length === 0) {
    skuList.value = [{
      skuCode: "",
      name: "默认",
      specJson: "",
      price: 0,
      costPrice: 0,
      stock: 0,
      weight: 0,
      imageUrl: "",
      aiTryonEnabled: 0,
      tryonCategory: "",
      tryonImageUrl: "",
      tryonMaskUrl: "",
      tryonSort: 0,
      status: 1
    }];
    return;
  }

  const combinations: string[][] = attrsWithValues.reduce<string[][]>(
    (acc, attr) =>
      acc.length === 0
        ? attr.values.map(v => [v])
        : acc.flatMap(combo => attr.values.map(v => [...combo, v])),
    []
  );

  const oldMap = new Map(skuList.value.map(s => [s.specJson, s]));

  skuList.value = combinations.map(combo => {
    const name = combo.join(" / ");
    const old = oldMap.get(name);
    return {
      skuCode: old?.skuCode ?? "",
      skuId: old?.skuId,
      name,
      specJson: name,
      price: old?.price ?? 0,
      costPrice: old?.costPrice ?? 0,
      stock: old?.stock ?? 0,
      weight: old?.weight ?? 0,
      imageUrl: old?.imageUrl ?? "",
      aiTryonEnabled: old?.aiTryonEnabled ?? 0,
      tryonCategory: old?.tryonCategory ?? "",
      tryonImageUrl: old?.tryonImageUrl ?? "",
      tryonMaskUrl: old?.tryonMaskUrl ?? "",
      tryonSort: old?.tryonSort ?? 0,
      status: old?.status ?? 1
    };
  });
}

function removeSkuRow(index: number) {
  skuList.value.splice(index, 1);
}

// === 详情图上传 ===
async function handleDetailImageUpload(options: any) {
  try {
    const res = await uploadImage(options.file, "products/detail");
    const url = pickUploadedUrl(res);
    if (res.code === 0 && url) {
      detailImages.value.push(url);
      detailImageFiles.value.push({
        name: options.file.name,
        url,
        uid: Date.now()
      } as UploadFile);
      options.onSuccess?.(res);
      message("Detail image upload success", { type: "success" });
    } else {
      options.onError?.(new Error(res?.message || "upload failed"));
      message("Detail image upload failed", { type: "error" });
    }
  } catch (e: any) {
    options.onError?.(e);
    message("Detail image upload failed", { type: "error" });
  }
}

function handleDetailImageRemove(file: UploadFile) {
  const idx = detailImageFiles.value.findIndex(f => f.uid === file.uid);
  if (idx !== -1) {
    detailImageFiles.value.splice(idx, 1);
    detailImages.value.splice(idx, 1);
  }
}

/** 初始化详情图拖拽排序 */
function initDetailImageSort() {
  nextTick(() => {
    const el = document.querySelector(
      ".detail-image-upload .el-upload-list"
    ) as HTMLElement;
    if (!el) return;
    Sortable.create(el, {
      animation: 150,
      onEnd({ oldIndex, newIndex }) {
        if (oldIndex == null || newIndex == null) return;
        const movedUrl = detailImages.value.splice(oldIndex, 1)[0];
        detailImages.value.splice(newIndex, 0, movedUrl);
        const movedFile = detailImageFiles.value.splice(oldIndex, 1)[0];
        detailImageFiles.value.splice(newIndex, 0, movedFile);
      }
    });
  });
}

// === 保存商品 ===
async function handleSave() {
  if (!formRef.value) return;
  await formRef.value.validate();

  saving.value = true;
  try {
    for (const sku of skuList.value) {
      if (showTryonWhitelistConfig.value && sku.aiTryonEnabled === 1) {
        if (!sku.tryonCategory) {
          message(`SKU「${sku.name || "未命名"}」已开启 AI 试穿，请选择试穿分类`, {
            type: "warning"
          });
          saving.value = false;
          return;
        }
        if (!sku.tryonImageUrl) {
          message(`SKU「${sku.name || "未命名"}」已开启 AI 试穿，请上传试穿素材图`, {
            type: "warning"
          });
          saving.value = false;
          return;
        }
      }
    }

    const skusPayload = skuList.value.length > 0
      ? skuList.value.map(s => ({
          productId: 0,
          skuCode: s.skuCode,
          name: s.name,
          specJson: s.specJson,
          price: s.price,
          costPrice: s.costPrice,
          stock: s.stock,
          imageUrl: s.imageUrl,
          aiTryonEnabled:
            showTryonWhitelistConfig.value && s.aiTryonEnabled === 1 ? 1 : 0,
          tryonCategory:
            showTryonWhitelistConfig.value && s.aiTryonEnabled === 1
              ? s.tryonCategory
              : "",
          tryonImageUrl:
            showTryonWhitelistConfig.value && s.aiTryonEnabled === 1
              ? s.tryonImageUrl
              : "",
          tryonMaskUrl:
            showTryonWhitelistConfig.value && s.aiTryonEnabled === 1
              ? s.tryonMaskUrl
              : "",
          tryonSort:
            showTryonWhitelistConfig.value && s.aiTryonEnabled === 1
              ? s.tryonSort ?? 0
              : 0,
          weight: s.weight,
          status: s.status
        }))
      : [{
          productId: 0,
          skuCode: "",
          name: "默认",
          price: form.price,
          costPrice: 0,
          stock: 0,
          imageUrl: "",
          aiTryonEnabled: 0,
          tryonCategory: "",
          tryonImageUrl: "",
          tryonMaskUrl: "",
          tryonSort: 0,
          weight: 0,
          status: 1
        }];

    if (isEdit.value) {
      const res = await updateProduct({
        productId: productId.value!,
        name: form.name,
        subTitle: form.subTitle,
        categoryId: form.categoryId!,
        mainImageUrl: form.mainImageUrl,
        detailHtml: form.detailHtml,
        status: form.status,
        imageUrls: detailImages.value,
        skus: skusPayload
      });
      if (res.code === 0) {
        message("更新商品成功", { type: "success" });
        router.back();
      } else {
        message("更新失败: " + res.message, { type: "error" });
      }
    } else {
      const res = await addProduct({
        name: form.name,
        subTitle: form.subTitle,
        categoryId: form.categoryId!,
        mainImageUrl: form.mainImageUrl,
        detailHtml: form.detailHtml,
        status: form.status,
        imageUrls: detailImages.value,
        skus: skusPayload
      });
      if (res.code === 0) {
        message("新增商品成功", { type: "success" });
        router.back();
      } else {
        message("新增失败: " + res.message, { type: "error" });
      }
    }
  } catch (e: any) {
    const detail = e?.response?.data?.message || e?.message || String(e);
    message("保存失败: " + detail, { type: "error" });
  } finally {
    saving.value = false;
  }
}

function handleCancel() {
  router.back();
}

// 规格值输入框
const specValueInputs = ref<Record<number, string>>({});

/** el-tree-select 配置 */
const treeSelectProps = {
  children: "children",
  label: "name",
  value: "categoryId"
};

onMounted(async () => {
  await loadCategoryTree();
  if (isEdit.value) {
    await loadProductDetail();
  } else {
    generateSkuList();
  }
  initDetailImageSort();
});
</script>

<template>
  <div class="p-4">
    <!-- 顶部返回 -->
    <div class="mb-4 flex items-center">
      <el-button :icon="ArrowLeft" @click="handleCancel">返回</el-button>
      <h2 class="ml-3 text-lg font-bold">
        {{ isEdit ? "编辑商品" : "新增商品" }}
      </h2>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <!-- 基本信息 -->
      <el-card shadow="never" class="mb-4">
        <template #header>
          <span class="font-bold">基本信息</span>
        </template>

        <el-form-item label="商品名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="Enter product name"
            maxlength="128"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="商品分类" prop="categoryId">
          <el-tree-select
            v-model="form.categoryId"
            :data="categoryTree"
            :props="treeSelectProps"
            placeholder="请选择分类"
            check-strictly
            filterable
            class="!w-[300px]"
            node-key="categoryId"
            :render-after-expand="false"
          />
        </el-form-item>

        <el-form-item label="商品副标题">
          <el-input
            v-model="form.subTitle"
            placeholder="请输入副标题"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="商品价格">
          <el-input-number
            v-model="form.price"
            :min="0"
            :precision="2"
            :step="1"
            controls-position="right"
            class="!w-[200px]"
          />
          <span class="ml-2 text-sm text-gray-400">（无规格时作为默认售价；有规格时请在下方 SKU 表格填写）</span>
        </el-form-item>

        <el-form-item label="商品主图">
          <el-upload
            ref="mainUploadRef"
            :file-list="mainImageList"
            list-type="picture-card"
            :auto-upload="false"
            :on-change="handleMainImageChange"
            :on-remove="handleMainImageRemove"
            :on-preview="onMainImagePreview"
            :limit="1"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">点击图片可裁剪编辑，最多上传一张主图</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-card>

      <!-- SKU 规格 -->
      <el-card shadow="never" class="mb-4">
        <template #header>
          <span class="font-bold">SKU 规格</span>
        </template>

        <!-- 添加规格属性 -->
        <div class="mb-4 flex items-center gap-2">
          <el-input
            v-model="newAttrName"
            placeholder="规格名称（如：颜色、尺寸）"
            class="!w-[200px]"
            @keyup.enter="addSpecAttr"
          />
          <el-button type="primary" @click="addSpecAttr">添加规格</el-button>
        </div>

        <!-- 规格属性列表 -->
        <div
          v-for="(attr, attrIdx) in specAttrs"
          :key="attrIdx"
          class="mb-4 p-3 bg-gray-50 rounded"
        >
          <div class="flex items-center justify-between mb-2">
            <span class="font-medium">{{ attr.name }}</span>
            <el-button
              type="danger"
              link
              size="small"
              @click="removeSpecAttr(attrIdx)"
            >
              删除规格
            </el-button>
          </div>
          <div class="flex flex-wrap gap-2 items-center">
            <el-tag
              v-for="(val, valIdx) in attr.values"
              :key="valIdx"
              closable
              @close="removeSpecValue(attrIdx, valIdx)"
            >
              {{ val }}
            </el-tag>
            <el-input
              v-model="specValueInputs[attrIdx]"
              placeholder="Add spec value"
              class="!w-[140px]"
              size="small"
              @keyup.enter="
                addSpecValue(attrIdx, specValueInputs[attrIdx] || '');
                specValueInputs[attrIdx] = '';
              "
            >
              <template #append>
                <el-button
                  size="small"
                  @click="
                    addSpecValue(attrIdx, specValueInputs[attrIdx] || '');
                    specValueInputs[attrIdx] = '';
                  "
                >
                  +
                </el-button>
              </template>
            </el-input>
          </div>
        </div>

        <!-- SKU 列表表格 -->
        <el-table
          v-if="skuList.length > 0"
          :data="skuList"
          border
          class="mt-4"
        >
          <el-table-column label="规格组合" min-width="150">
            <template #default="{ row }">
              <el-input v-model="row.name" size="small" placeholder="规格名称" />
            </template>
          </el-table-column>
          <el-table-column label="价格" width="140">
            <template #default="{ row }">
              <el-input-number
                v-model="row.price"
                :min="0"
                :precision="2"
                size="small"
                controls-position="right"
              />
            </template>
          </el-table-column>
          <el-table-column label="库存" width="130">
            <template #default="{ row }">
              <el-input-number
                v-model="row.stock"
                :min="0"
                :step="1"
                size="small"
                controls-position="right"
              />
            </template>
          </el-table-column>
          <el-table-column label="SKU图片" width="170">
            <template #default="{ row, $index }">
              <div v-if="row.imageUrl" class="flex items-center gap-1">
                <el-image
                  :src="row.imageUrl"
                  fit="cover"
                  class="w-[50px] h-[50px] rounded"
                  :preview-src-list="[row.imageUrl]"
                  preview-teleported
                />
                <div class="flex flex-col gap-1">
                  <el-button
                    type="primary"
                    link
                    size="small"
                    :icon="Edit"
                    @click="editSkuImage($index)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    type="danger"
                    link
                    size="small"
                    @click="row.imageUrl = ''"
                  >
                    删除
                  </el-button>
                </div>
              </div>
              <el-upload
                v-else
                :show-file-list="false"
                :before-upload="makeBeforeSkuUpload($index)"
                accept="image/*"
              >
                <el-button size="small" type="primary" link>上传</el-button>
              </el-upload>
            </template>
          </el-table-column>
          <el-table-column v-if="showTryonWhitelistConfig" label="AI试穿白名单" width="140">
            <template #default="{ row }">
              <el-switch
                :model-value="row.aiTryonEnabled === 1"
                inline-prompt
                active-text="是"
                inactive-text="否"
                @change="handleTryonEnabledChange(row, $event)"
              />
            </template>
          </el-table-column>
          <el-table-column v-if="showTryonWhitelistConfig" label="试穿分类" width="160">
            <template #default="{ row }">
              <el-select
                v-model="row.tryonCategory"
                :disabled="row.aiTryonEnabled !== 1"
                placeholder="请选择"
                clearable
                size="small"
              >
                <el-option label="上装" value="TOP" />
                <el-option label="下装" value="BOTTOM" />
                <el-option label="裙子" value="DRESS" />
                <el-option label="鞋子" value="SHOES" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column v-if="showTryonWhitelistConfig" label="试穿素材图" width="210">
            <template #default="{ row, $index }">
              <div v-if="row.aiTryonEnabled === 1">
                <div v-if="row.tryonImageUrl" class="flex items-center gap-1">
                  <el-image
                    :src="row.tryonImageUrl"
                    fit="cover"
                    class="w-[50px] h-[50px] rounded"
                    :preview-src-list="[row.tryonImageUrl]"
                    preview-teleported
                  />
                  <div class="flex flex-col gap-1">
                    <el-button
                      type="primary"
                      link
                      size="small"
                      :icon="Edit"
                      @click="editSkuTryonImage($index)"
                    >
                      编辑
                    </el-button>
                    <el-button
                      type="danger"
                      link
                      size="small"
                      @click="row.tryonImageUrl = ''"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
                <el-upload
                  v-else
                  :show-file-list="false"
                  :before-upload="makeBeforeSkuTryonUpload($index)"
                  accept="image/*"
                >
                  <el-button size="small" type="primary" link>上传素材</el-button>
                </el-upload>
              </div>
              <span v-else class="text-xs text-gray-400">未加入白名单</span>
            </template>
          </el-table-column>
          <el-table-column v-if="showTryonWhitelistConfig" label="试穿排序" width="120">
            <template #default="{ row }">
              <el-input-number
                v-model="row.tryonSort"
                :min="0"
                :disabled="row.aiTryonEnabled !== 1"
                size="small"
                controls-position="right"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ $index }">
              <el-button
                type="danger"
                link
                size="small"
                :icon="Delete"
                @click="removeSkuRow($index)"
              />
            </template>
          </el-table-column>
        </el-table>

        <el-empty
          v-if="specAttrs.length === 0 && skuList.length === 0"
          description="No specs yet, please add attributes first"
          :image-size="60"
        />
        <div v-if="showTryonWhitelistConfig" class="mt-3 text-xs text-gray-400">
          勾选“AI试穿白名单”后，该 SKU 会进入试穿商品池；建议只给服装/鞋帽类 SKU 配置专用试穿素材图。
        </div>
      </el-card>

      <!-- 商品详情图 -->
      <el-card shadow="never" class="mb-4">
        <template #header>
          <span class="font-bold">Product detail images</span>
        </template>

        <el-upload
          class="detail-image-upload"
          :file-list="detailImageFiles"
          list-type="picture-card"
          :auto-upload="true"
          :http-request="handleDetailImageUpload"
          :on-remove="handleDetailImageRemove"
          accept="image/*"
          multiple
        >
          <el-icon><Plus /></el-icon>
          <template #tip>
            <div class="el-upload__tip">
              支持多图上传，拖拽可排序，排序决定详情页展示顺序
            </div>
          </template>
        </el-upload>
      </el-card>
    </el-form>

    <!-- 底部操作栏 -->
    <div class="flex justify-center gap-4 mt-6 pb-8">
      <el-button type="primary" size="large" :loading="saving" @click="handleSave">
        保存商品
      </el-button>
      <el-button size="large" @click="handleCancel">取消</el-button>
    </div>

    <!-- 裁剪弹窗 -->
    <el-dialog
      v-model="cropperVisible"
      title="裁剪图片"
      width="740px"
      :destroy-on-close="true"
      :close-on-click-modal="false"
    >
      <ReCropper
        v-if="cropperVisible"
        :key="cropperKey"
        :src="cropperSrc"
        height="400px"
        :options="{ aspectRatio: NaN, viewMode: 1 }"
        :real-time-preview="true"
        @cropper="onCropperData"
      />
      <div class="mt-2 text-xs text-gray-400 text-center">
        右键裁剪区可使用旋转、翻转等更多工具
      </div>
      <template #footer>
        <el-button @click="cropperVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="cropperUploading"
          @click="confirmCrop"
        >
          确认并上传
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
:deep(.el-upload--picture-card) {
  width: 120px;
  height: 120px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 120px;
  height: 120px;
}
</style>
