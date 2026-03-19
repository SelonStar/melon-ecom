<script setup lang="ts">
import type { TryOnSelectionMap, TryOnSlotType } from '@/types/tryon'

defineProps<{
  selection: TryOnSelectionMap
}>()

const slots: Array<{ key: TryOnSlotType; label: string }> = [
  { key: 'TOP', label: '上装' },
  { key: 'BOTTOM', label: '下装' },
  { key: 'DRESS', label: '裙子' },
  { key: 'SHOES', label: '鞋子' },
]

const money = (value?: string) => {
  const n = Number(value || 0)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}
</script>

<template>
  <section class="preview-card">
    <div class="preview-card__head">
      <div>
        <h3>当前搭配</h3>
        <p>裙子会自动与上装/下装互斥，加入购物车时会按当前选择的 SKU 批量加入。</p>
      </div>
    </div>

    <div class="preview-list">
      <div v-for="slot in slots" :key="slot.key" class="preview-item">
        <div class="preview-item__label">{{ slot.label }}</div>
        <div v-if="selection[slot.key]" class="preview-item__content">
          <img v-if="selection[slot.key]?.imageUrl" :src="selection[slot.key]?.imageUrl" alt="" />
          <div>
            <div class="preview-item__name">{{ selection[slot.key]?.productName }}</div>
            <div class="preview-item__sku">{{ selection[slot.key]?.skuName }}</div>
            <div class="preview-item__price">￥{{ money(selection[slot.key]?.price) }}</div>
          </div>
        </div>
        <div v-else class="preview-item__empty">未选择</div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.preview-card {
  border: 1px solid #d5d9d9;
  border-radius: 10px;
  background: #fff;
  padding: 16px;
}

.preview-card__head h3 {
  margin: 0;
  color: #0f1111;
  font-size: 18px;
  font-weight: 700;
}

.preview-card__head p {
  margin: 8px 0 0;
  color: #565959;
  font-size: 12px;
  line-height: 1.5;
}

.preview-list {
  display: grid;
  gap: 12px;
  margin-top: 16px;
}

.preview-item {
  border: 1px solid #eaeded;
  border-radius: 10px;
  padding: 12px;
}

.preview-item__label {
  color: #565959;
  font-size: 12px;
  font-weight: 700;
}

.preview-item__content {
  display: flex;
  gap: 12px;
  margin-top: 10px;
}

.preview-item__content img {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  object-fit: cover;
  background: #f7f8fa;
}

.preview-item__name {
  color: #0f1111;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.4;
}

.preview-item__sku,
.preview-item__empty {
  margin-top: 4px;
  color: #565959;
  font-size: 12px;
}

.preview-item__price {
  margin-top: 8px;
  color: #b12704;
  font-size: 13px;
  font-weight: 700;
}
</style>
