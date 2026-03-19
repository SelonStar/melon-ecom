<script setup lang="ts">
import { computed, ref } from 'vue'

import type { TryOnPhotoType, UploadTryOnPhotoResp } from '@/types/tryon'

const props = defineProps<{
  modelValue?: UploadTryOnPhotoResp | null
  uploading?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: UploadTryOnPhotoResp | null): void
  (e: 'upload', payload: { file: File; photoType: TryOnPhotoType }): void
}>()

const photoType = ref<TryOnPhotoType>(props.modelValue?.photoType || 'FULL_BODY')

const previewUrl = computed(() => props.modelValue?.photoUrl || '')

const onFileChange = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  emit('upload', { file, photoType: photoType.value })
}
</script>

<template>
  <section class="tryon-card">
    <div class="tryon-card__head">
      <div>
        <h3>1. 上传试穿照片</h3>
        <p>建议使用正面、光线均匀、背景简洁的人像。半身照更适合上装与裙子。</p>
      </div>
    </div>

    <div class="photo-type-switch">
      <button
        class="photo-type-switch__item"
        :class="{ 'photo-type-switch__item--active': photoType === 'FULL_BODY' }"
        @click="photoType = 'FULL_BODY'"
      >
        全身照
      </button>
      <button
        class="photo-type-switch__item"
        :class="{ 'photo-type-switch__item--active': photoType === 'HALF_BODY' }"
        @click="photoType = 'HALF_BODY'"
      >
        半身照
      </button>
    </div>

    <label class="upload-panel">
      <input class="hidden" type="file" accept="image/*" @change="onFileChange" />
      <div v-if="previewUrl" class="upload-panel__preview">
        <img :src="previewUrl" alt="try-on photo" />
      </div>
      <div v-else class="upload-panel__empty">
        <div class="upload-panel__title">点击上传你的试穿照片</div>
        <div class="upload-panel__desc">
          当前模式：{{ photoType === 'FULL_BODY' ? '全身照，可尝试完整穿搭' : '半身照，优先上装/裙子' }}
        </div>
      </div>
      <div class="upload-panel__mask" v-if="uploading">上传中...</div>
    </label>
  </section>
</template>

<style scoped>
.tryon-card {
  border: 1px solid #d5d9d9;
  border-radius: 10px;
  background: #fff;
  padding: 20px;
}

.tryon-card__head h3 {
  margin: 0;
  color: #0f1111;
  font-size: 20px;
  font-weight: 700;
}

.tryon-card__head p {
  margin: 8px 0 0;
  color: #565959;
  font-size: 13px;
  line-height: 1.5;
}

.photo-type-switch {
  display: inline-flex;
  margin-top: 16px;
  border: 1px solid #d5d9d9;
  border-radius: 999px;
  overflow: hidden;
  background: #f7fafa;
}

.photo-type-switch__item {
  border: 0;
  background: transparent;
  color: #0f1111;
  cursor: pointer;
  font-size: 13px;
  padding: 10px 16px;
}

.photo-type-switch__item--active {
  background: #ffd814;
  font-weight: 700;
}

.upload-panel {
  position: relative;
  display: flex;
  min-height: 360px;
  margin-top: 16px;
  border: 1px dashed #b8c2cc;
  border-radius: 14px;
  background:
    linear-gradient(180deg, rgba(19, 25, 33, 0.02), rgba(19, 25, 33, 0.05)),
    #f7f8fa;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
}

.upload-panel__preview {
  height: 100%;
  width: 100%;
}

.upload-panel__preview img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #f7f8fa;
}

.upload-panel__empty {
  text-align: center;
  padding: 24px;
}

.upload-panel__title {
  color: #0f1111;
  font-size: 18px;
  font-weight: 700;
}

.upload-panel__desc {
  margin-top: 8px;
  color: #565959;
  font-size: 13px;
}

.upload-panel__mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.7);
  color: #0f1111;
  font-size: 14px;
  font-weight: 700;
}
</style>
