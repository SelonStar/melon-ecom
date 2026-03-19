<script setup lang="ts">
import { computed } from 'vue'

import type { TryOnCandidateSkuVO, TryOnPhotoType, TryOnSlotType } from '@/types/tryon'

const props = defineProps<{
  title: string
  slotType: TryOnSlotType
  photoType: TryOnPhotoType
  list: TryOnCandidateSkuVO[]
  loading?: boolean
  selectedSkuId?: number | null
}>()

const emit = defineEmits<{
  (e: 'select', value: TryOnCandidateSkuVO): void
}>()

const disabledTip = computed(() => {
  if (props.photoType !== 'HALF_BODY') return ''
  if (props.slotType === 'BOTTOM') return '半身照暂不开放下装试穿'
  if (props.slotType === 'SHOES') return '半身照暂不开放鞋子试穿'
  return ''
})

const disabled = computed(() => !!disabledTip.value)

const money = (value?: string) => {
  const n = Number(value || 0)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}
</script>

<template>
  <section class="tryon-card selector-card">
    <div class="selector-card__head">
      <div>
        <h3>{{ title }}</h3>
        <p v-if="disabledTip">{{ disabledTip }}</p>
        <p v-else>仅展示已配置 AI 试穿素材的白名单商品。</p>
      </div>
    </div>

    <div v-if="disabled" class="selector-card__empty">
      {{ disabledTip }}
    </div>
    <div v-else-if="loading" class="selector-card__empty">
      试穿商品加载中...
    </div>
    <div v-else-if="list.length === 0" class="selector-card__empty">
      当前没有可试穿商品
    </div>
    <div v-else class="selector-grid">
      <button
        v-for="item in list"
        :key="item.skuId"
        class="selector-tile"
        :class="{ 'selector-tile--active': selectedSkuId === item.skuId }"
        @click="emit('select', item)"
      >
        <div class="selector-tile__image-wrap">
          <img v-if="item.imageUrl" :src="item.imageUrl" :alt="item.skuName" class="selector-tile__image" />
          <div v-else class="selector-tile__placeholder">暂无图片</div>
        </div>
        <div class="selector-tile__name">{{ item.productName }}</div>
        <div class="selector-tile__sku">{{ item.skuName }}</div>
        <div class="selector-tile__meta">
          <span>￥{{ money(item.price) }}</span>
          <span v-if="item.stock != null">库存 {{ item.stock }}</span>
        </div>
      </button>
    </div>
  </section>
</template>

<style scoped>
.tryon-card {
  border: 1px solid #d5d9d9;
  border-radius: 10px;
  background: #fff;
  padding: 16px;
}

.selector-card__head h3 {
  margin: 0;
  color: #0f1111;
  font-size: 18px;
  font-weight: 700;
}

.selector-card__head p {
  margin: 8px 0 0;
  color: #565959;
  font-size: 12px;
}

.selector-card__empty {
  margin-top: 16px;
  border-radius: 10px;
  background: #f7fafa;
  color: #565959;
  font-size: 13px;
  padding: 20px;
}

.selector-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.selector-tile {
  border: 1px solid #d5d9d9;
  border-radius: 10px;
  background: #fff;
  cursor: pointer;
  padding: 12px;
  text-align: left;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.selector-tile:hover {
  border-color: #e77600;
  transform: translateY(-1px);
}

.selector-tile--active {
  border-color: #e77600;
  box-shadow: 0 0 0 2px rgba(231, 118, 0, 0.18);
  background: #fffaf2;
}

.selector-tile__image-wrap {
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: 8px;
  background: #f7f8fa;
}

.selector-tile__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.selector-tile__placeholder {
  display: flex;
  height: 100%;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 12px;
}

.selector-tile__name {
  margin-top: 10px;
  color: #0f1111;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.4;
}

.selector-tile__sku {
  margin-top: 4px;
  color: #565959;
  font-size: 12px;
}

.selector-tile__meta {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  color: #b12704;
  font-size: 12px;
  font-weight: 700;
}

@media (max-width: 767px) {
  .selector-grid {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }
}
</style>
