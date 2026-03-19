<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import type { UploadRequestOptions } from "element-plus";

import {
  getHomeNavCards,
  saveHomeNavCards,
  type HomeNavCardVO
} from "@/api/homeNavCard";
import { uploadImage } from "@/api/product";

defineOptions({
  name: "HomeNavCardManagement"
});

type NavCardItemForm = {
  itemId: number | null;
  keyword: string;
  title: string;
  imageUrl: string;
  sort: number;
};

type NavCardForm = {
  cardId: number | null;
  title: string;
  sort: number;
  items: NavCardItemForm[];
};

const CARD_COUNT = 4;
const ITEM_COUNT = 4;

const loading = ref(false);
const saving = ref(false);
const cards = ref<NavCardForm[]>(createEmptyCards());
const uploadLoadingMap = reactive<Record<string, boolean>>({});

function createEmptyItem(sort: number): NavCardItemForm {
  return {
    itemId: null,
    keyword: "",
    title: "",
    imageUrl: "",
    sort
  };
}

function createEmptyCard(sort: number): NavCardForm {
  return {
    cardId: null,
    title: "",
    sort,
    items: Array.from({ length: ITEM_COUNT }, (_, index) =>
      createEmptyItem(index + 1)
    )
  };
}

function createEmptyCards(): NavCardForm[] {
  return Array.from({ length: CARD_COUNT }, (_, index) =>
    createEmptyCard(index + 1)
  );
}

function normalizeCards(rawCards?: HomeNavCardVO[]): NavCardForm[] {
  return Array.from({ length: CARD_COUNT }, (_, cardIndex) => {
    const sourceCard = rawCards?.[cardIndex];
    const items = Array.from({ length: ITEM_COUNT }, (_, itemIndex) => {
      const sourceItem = sourceCard?.items?.[itemIndex];
      return {
        itemId: sourceItem?.itemId ?? null,
        keyword: sourceItem?.keyword ?? "",
        title: sourceItem?.title ?? "",
        imageUrl: sourceItem?.imageUrl ?? "",
        sort: itemIndex + 1
      };
    });

    return {
      cardId: sourceCard?.cardId ?? null,
      title: sourceCard?.title ?? "",
      sort: cardIndex + 1,
      items
    };
  });
}

async function loadCards() {
  loading.value = true;
  try {
    const res = await getHomeNavCards();
    if (res.code === 0) {
      cards.value = normalizeCards(res.data);
    } else {
      ElMessage.error(res.message || "加载导航卡配置失败");
    }
  } catch (error) {
    console.error(error);
    ElMessage.error("加载导航卡配置失败");
  } finally {
    loading.value = false;
  }
}

async function uploadItemImage(
  options: UploadRequestOptions,
  item: NavCardItemForm,
  key: string
) {
  uploadLoadingMap[key] = true;
  try {
    const res = await uploadImage(options.file as File, "home-nav-cards");
    if (res.code === 0 && res.data) {
      item.imageUrl = res.data;
      ElMessage.success("图片上传成功");
      options.onSuccess?.(res as any);
    } else {
      ElMessage.error(res.message || "图片上传失败");
      options.onError?.(new Error(res.message || "upload failed"));
    }
  } catch (error) {
    console.error(error);
    ElMessage.error("图片上传失败");
    options.onError?.(error as Error);
  } finally {
    uploadLoadingMap[key] = false;
  }
}

function validateCards() {
  for (let cardIndex = 0; cardIndex < cards.value.length; cardIndex += 1) {
    const card = cards.value[cardIndex];
    if (!card.title.trim()) {
      ElMessage.warning(`请填写第 ${cardIndex + 1} 张导航卡标题`);
      return false;
    }

    for (let itemIndex = 0; itemIndex < card.items.length; itemIndex += 1) {
      const item = card.items[itemIndex];
      if (!item.keyword.trim()) {
        ElMessage.warning(
          `请填写第 ${cardIndex + 1} 张导航卡第 ${itemIndex + 1} 个商品位关键词`
        );
        return false;
      }
      if (!item.title.trim()) {
        ElMessage.warning(
          `请填写第 ${cardIndex + 1} 张导航卡第 ${itemIndex + 1} 个商品位标题`
        );
        return false;
      }
      if (!item.imageUrl) {
        ElMessage.warning(
          `请上传第 ${cardIndex + 1} 张导航卡第 ${itemIndex + 1} 个商品位图片`
        );
        return false;
      }
    }
  }
  return true;
}

async function handleSave() {
  if (!validateCards()) return;

  saving.value = true;
  try {
    const res = await saveHomeNavCards({
      cards: cards.value.map((card, cardIndex) => ({
        cardId: card.cardId,
        title: card.title.trim(),
        sort: cardIndex + 1,
        items: card.items.map((item, itemIndex) => ({
          itemId: item.itemId,
          keyword: item.keyword.trim(),
          title: item.title.trim(),
          imageUrl: item.imageUrl,
          sort: itemIndex + 1
        }))
      }))
    });

    if (res.code === 0) {
      ElMessage.success(res.message || "导航卡保存成功");
      await loadCards();
    } else {
      ElMessage.error(res.message || "导航卡保存失败");
    }
  } catch (error) {
    console.error(error);
    ElMessage.error("导航卡保存失败");
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  loadCards();
});
</script>

<template>
  <div class="home-nav-card-page" v-loading="loading">
    <el-card shadow="never" class="page-toolbar">
      <div class="toolbar-row">
        <div>
          <h2 class="toolbar-title">首页导航卡管理</h2>
          <p class="toolbar-desc">
            固定管理首页 4 张导航卡，每张卡内 4 个小商品位；关键词用于前端点击后的搜索词，支持多个关键词并集搜索，小商品标题仅用于展示。
          </p>
        </div>
        <div class="toolbar-actions">
          <el-button @click="loadCards">刷新</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">
            保存配置
          </el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20">
      <el-col
        v-for="(card, cardIndex) in cards"
        :key="`card-${cardIndex}`"
        :xs="24"
        :sm="24"
        :lg="12"
      >
        <el-card class="card-editor" shadow="hover">
          <template #header>
            <div class="card-editor__header">
              <span>导航卡 {{ cardIndex + 1 }}</span>
            </div>
          </template>

          <el-form label-position="top">
            <el-form-item label="导航卡标题">
              <el-input
                v-model="card.title"
                maxlength="30"
                show-word-limit
                placeholder="请输入导航卡标题"
              />
            </el-form-item>

            <div class="item-list">
              <div
                v-for="(item, itemIndex) in card.items"
                :key="`card-${cardIndex}-item-${itemIndex}`"
                class="item-editor"
              >
                <div class="item-editor__title">商品位 {{ itemIndex + 1 }}</div>

                <el-form-item label="关键词">
                  <el-input
                    v-model="item.keyword"
                    maxlength="60"
                    show-word-limit
                    placeholder="请输入关键词，多个词可用逗号、分号或换行分隔"
                  />
                </el-form-item>

                <el-form-item label="小商品标题">
                  <el-input
                    v-model="item.title"
                    maxlength="40"
                    show-word-limit
                    placeholder="请输入仅用于展示的小商品标题"
                  />
                </el-form-item>

                <el-form-item label="小商品图片">
                  <div class="item-editor__image-row">
                    <div class="item-editor__preview">
                      <el-image
                        v-if="item.imageUrl"
                        :src="item.imageUrl"
                        fit="cover"
                        :preview-src-list="[item.imageUrl]"
                        preview-teleported
                        class="item-editor__image"
                      />
                      <div v-else class="item-editor__placeholder">暂无图片</div>
                    </div>

                    <div class="item-editor__image-actions">
                      <el-upload
                        :show-file-list="false"
                        accept="image/*"
                        :http-request="
                          options =>
                            uploadItemImage(
                              options,
                              item,
                              `card-${cardIndex}-item-${itemIndex}`
                            )
                        "
                      >
                        <el-button
                          type="primary"
                          plain
                          :loading="
                            uploadLoadingMap[
                              `card-${cardIndex}-item-${itemIndex}`
                            ]
                          "
                        >
                          上传图片
                        </el-button>
                      </el-upload>

                      <el-button
                        text
                        type="danger"
                        @click="item.imageUrl = ''"
                      >
                        清空图片
                      </el-button>
                    </div>
                  </div>
                </el-form-item>
              </div>
            </div>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped lang="scss">
.home-nav-card-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-toolbar {
  border: 0;
}

.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.toolbar-title {
  margin: 0;
  color: var(--el-text-color-primary);
  font-size: 20px;
  font-weight: 700;
}

.toolbar-desc {
  margin: 8px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
}

.card-editor {
  margin-bottom: 20px;
}

.card-editor__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 700;
}

.item-list {
  display: grid;
  gap: 16px;
}

.item-editor {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  background: var(--el-fill-color-blank);
  padding: 16px;
}

.item-editor__title {
  margin-bottom: 12px;
  color: var(--el-text-color-primary);
  font-size: 14px;
  font-weight: 700;
}

.item-editor__image-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.item-editor__preview {
  display: flex;
  height: 96px;
  width: 96px;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 10px;
  background: var(--el-fill-color-light);
  flex-shrink: 0;
}

.item-editor__image {
  height: 96px;
  width: 96px;
  border-radius: 10px;
}

.item-editor__placeholder {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.item-editor__image-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

@media (max-width: 768px) {
  .toolbar-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .toolbar-actions {
    width: 100%;
  }

  .item-editor__image-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
