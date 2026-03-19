<script setup lang="ts">
import { computed } from 'vue'

import { DEFAULT_PRODUCT_IMAGE } from '@/views/home/mock'
import type { HomeCard, HomeCardVariant, HomeProductPreview } from '@/views/home/mock'

const props = withDefaults(
  defineProps<{
    title: string
    variant?: HomeCardVariant
    card?: HomeCard
    product?: HomeProductPreview | null
    products?: HomeProductPreview[]
    actionText?: string
  }>(),
  {
    variant: 'grid',
    card: undefined,
    product: null,
    products: () => [],
    actionText: '查看更多',
  }
)

const emit = defineEmits<{
  navigate: [link: string]
  productClick: [productId: number]
}>()

const money = (value?: string) => {
  if (!value) return '0.00'
  const amount = Number(value)
  return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
}

const featuredProduct = computed(() => {
  if (!props.product) return null
  return {
    ...props.product,
    image: props.product.image || DEFAULT_PRODUCT_IMAGE,
  }
})

const stripProducts = computed(() =>
  props.products.map((item) => ({
    ...item,
    image: item.image || DEFAULT_PRODUCT_IMAGE,
  }))
)

const footerLink = computed(() => {
  if (props.variant === 'grid') {
    return props.card?.items[0]?.link ?? '/category/0'
  }
  if (props.variant === 'featured') {
    return featuredProduct.value?.link ?? '/category/0'
  }
  return stripProducts.value[0]?.link ?? '/category/0'
})

const openLink = (link: string) => emit('navigate', link)

const openProduct = (product: HomeProductPreview) => {
  if (product.productId > 0) {
    emit('productClick', product.productId)
    return
  }
  emit('navigate', product.link)
}
</script>

<template>
  <article class="home-card" :class="`home-card--${variant}`">
    <div class="home-card__header">
      <h3 class="home-card__title">{{ title }}</h3>
    </div>

    <div v-if="variant === 'grid'" class="home-card__body">
      <div class="home-card__grid">
        <button
          v-for="item in card?.items ?? []"
          :key="`${title}-${item.label}`"
          type="button"
          class="grid-item"
          @click="openLink(item.link)"
        >
          <div class="grid-item__image-wrap">
            <img class="grid-item__image" :src="item.image || DEFAULT_PRODUCT_IMAGE" :alt="item.label" />
          </div>
          <span class="grid-item__label">{{ item.label }}</span>
        </button>
      </div>
    </div>

    <div v-else-if="variant === 'featured'" class="home-card__body">
      <button
        v-if="featuredProduct"
        type="button"
        class="feature-product"
        @click="openProduct(featuredProduct)"
      >
        <div class="feature-product__image-wrap">
          <img class="feature-product__image" :src="featuredProduct.image" :alt="featuredProduct.name" />
        </div>
        <div class="feature-product__meta">
          <span class="feature-product__badge">精选推荐</span>
          <h4 class="feature-product__name">{{ featuredProduct.name }}</h4>
          <div class="feature-product__price">￥{{ money(featuredProduct.price) }}</div>
        </div>
      </button>
    </div>

    <div v-else class="home-card__body">
      <div class="strip-list">
        <button
          v-for="product in stripProducts"
          :key="`${title}-${product.productId}-${product.name}`"
          type="button"
          class="strip-item"
          @click="openProduct(product)"
        >
          <div class="strip-item__image-wrap">
            <img class="strip-item__image" :src="product.image" :alt="product.name" />
          </div>
          <div class="strip-item__name">{{ product.name }}</div>
          <div class="strip-item__price">￥{{ money(product.price) }}</div>
        </button>
      </div>
    </div>

    <div class="home-card__footer">
      <button type="button" class="home-card__action" @click="openLink(footerLink)">
        {{ actionText }}
      </button>
    </div>
  </article>
</template>

<style scoped>
.home-card {
  display: flex;
  min-height: 100%;
  flex-direction: column;
  overflow: hidden;
  border: 0;
  border-radius: 4px;
  background: #ffffff;
  padding: 16px;
  box-shadow: none;
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease;
}

.home-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.home-card__header {
  margin-bottom: 12px;
}

.home-card__body {
  flex: 1;
}

.home-card__title {
  margin: 0;
  color: #0f1111;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.35;
}

.home-card__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.grid-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  border: 0;
  background: transparent;
  cursor: pointer;
  padding: 0;
  text-align: left;
}

.grid-item__image-wrap,
.feature-product__image-wrap,
.strip-item__image-wrap {
  overflow: hidden;
  border-radius: 0;
  background: #f7f8fa;
}

.grid-item__image-wrap {
  aspect-ratio: 1;
}

.grid-item__image,
.feature-product__image,
.strip-item__image {
  height: 100%;
  width: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.grid-item:hover .grid-item__image,
.feature-product:hover .feature-product__image,
.strip-item:hover .strip-item__image {
  transform: scale(1.06);
}

.grid-item__label {
  color: #0f1111;
  font-size: 12px;
  line-height: 1.35;
}

.feature-product,
.strip-item {
  border: 0;
  background: transparent;
  cursor: pointer;
  padding: 0;
  text-align: left;
}

.feature-product {
  display: flex;
  height: 100%;
  flex-direction: column;
  gap: 12px;
}

.feature-product__image-wrap {
  aspect-ratio: 1;
}

.feature-product__meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.feature-product__badge {
  display: inline-flex;
  width: fit-content;
  border-radius: 999px;
  background: #ffd814;
  color: #0f1111;
  font-size: 12px;
  font-weight: 700;
  padding: 4px 10px;
}

.feature-product__name {
  margin: 0;
  color: #0f1111;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.4;
}

.feature-product__price,
.strip-item__price {
  color: #b12704;
  font-size: 18px;
  font-weight: 700;
}

.strip-list {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.strip-item {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.strip-item__image-wrap {
  aspect-ratio: 1;
}

.strip-item__name {
  display: -webkit-box;
  overflow: hidden;
  color: #0f1111;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.4;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.home-card__footer {
  margin-top: 12px;
}

.home-card__action {
  border: 0;
  background: transparent;
  color: #007185;
  cursor: pointer;
  font-size: 13px;
  font-weight: 400;
  padding: 0;
}

.home-card__action:hover {
  color: #c7511f;
  text-decoration: underline;
}

@media (max-width: 1279px) {
  .strip-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .home-card {
    padding: 16px;
  }

  .home-card__title {
    font-size: 16px;
  }

  .feature-product__image-wrap {
    aspect-ratio: 1;
  }

  .strip-list {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }
}
</style>
