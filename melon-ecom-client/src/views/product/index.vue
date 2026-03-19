<!-- src/views/product/index.vue -->
<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import http from '@/utils/http'
import type { Result } from '@/types/api'
import { useCartStore } from '@/stores/modules/cart'

const cartStore = useCartStore()

type SkuVO = {
  skuId: number
  skuCode: string
  name: string
  specJson?: string // '{"color":"red","size":"XL"}'
  price?: string | number
  status?: number | null
  stock?: number
  imageUrl?: string
}

type ProductDetailVO = {
  productId: number
  name: string
  subTitle?: string
  brandId?: number
  categoryId?: number
  mainImageUrl?: string
  detailHtml?: string
  status: number
  ratingAvg?: string
  commentCount?: number
  salesCount?: number
  imageUrls?: string[]
  skus?: SkuVO[]
}

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const product = ref<ProductDetailVO | null>(null)

const productId = computed(() => {
  const n = Number(route.params.id)
  return Number.isFinite(n) ? n : 0
})

// ---------- 图片 ----------
const gallery = computed(() => {
  const urls = product.value?.imageUrls ?? []
  if (urls.length > 0) return urls
  const main = product.value?.mainImageUrl
  return main ? [main] : []
})

const activeImage = ref('')
const savedImage = ref('') // 用于 hover 恢复

watch(
  () => gallery.value,
  (g) => {
    const first = g?.[0] ?? ''
    activeImage.value = first
    savedImage.value = first
  },
  { immediate: true }
)

// ---------- SKU ----------
function resolveDisplaySkus(source?: SkuVO[]) {
  const all = source ?? []
  const active = all.filter((s) => s.status === 1)
  if (active.length > 0) return active
  return all.filter((s) => s.status !== 0)
}

const skus = computed(() => resolveDisplaySkus(product.value?.skus))
const selectedSkuId = ref<number | null>(null)
const hoveredSkuId = ref<number | null>(null)
const quantity = ref(1)

const selectedSku = computed(() => skus.value.find((s) => s.skuId === selectedSkuId.value) ?? null)
const hoveredSku = computed(() => skus.value.find((s) => s.skuId === hoveredSkuId.value) ?? null)

// 当前展示的 SKU（hover 优先，否则 selected）
const displaySku = computed(() => hoveredSku.value ?? selectedSku.value)

// ---------- 规格解析 ----------
function parseSpec(specJson?: string): Record<string, string> {
  if (!specJson) return {}
  try {
    return JSON.parse(specJson)
  } catch {
    return {}
  }
}

const displaySpecLabel = computed(() => {
  if (!displaySku.value) return ''
  const spec = parseSpec(displaySku.value.specJson)
  const entries = Object.entries(spec)
  if (entries.length === 0) return displaySku.value.name
  return entries.map(([k, v]) => `${k}: ${v}`).join('，')
})

// ---------- SKU Hover 联动 ----------
function onSkuMouseEnter(sku: SkuVO) {
  hoveredSkuId.value = sku.skuId
  if (sku.imageUrl) {
    activeImage.value = sku.imageUrl
  }
}

function onSkuMouseLeave() {
  hoveredSkuId.value = null
  // 恢复为选中 SKU 的图片，或默认主图
  if (selectedSku.value?.imageUrl) {
    activeImage.value = selectedSku.value.imageUrl
  } else {
    activeImage.value = savedImage.value
  }
}

function onSkuClick(sku: SkuVO) {
  selectedSkuId.value = sku.skuId
  // 确认选择后更新 saved
  if (sku.imageUrl) {
    activeImage.value = sku.imageUrl
    savedImage.value = sku.imageUrl
  }
}

// ---------- 价格 ----------
const toNumber = (v?: string | number | null) => {
  if (v == null || v === '') return 0
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}
const money = (v?: string | number | null) => toNumber(v).toFixed(2)

const displayPrice = computed(() => {
  if (displaySku.value) return money(displaySku.value.price)
  if (skus.value.length === 0) return '0.00'
  const min = Math.min(...skus.value.map((s) => toNumber(s.price)))
  return min.toFixed(2)
})

const priceLabel = computed(() => {
  if (displaySku.value) return ''
  if (skus.value.length > 1) return '起'
  return ''
})

// ---------- 评分星星 ----------
const ratingNum = computed(() => {
  const n = toNumber(product.value?.ratingAvg)
  return Math.min(5, Math.max(0, n))
})
const fullStars = computed(() => Math.floor(ratingNum.value))
const hasHalfStar = computed(() => ratingNum.value - fullStars.value >= 0.3)
const emptyStars = computed(() => 5 - fullStars.value - (hasHalfStar.value ? 1 : 0))

// ---------- 加入购物车 ----------
const canAddToCart = computed(() => {
  if (!product.value) return false
  if (product.value.status !== 1) return false
  if (skus.value.length > 0 && !selectedSku.value) return false
  if (quantity.value < 1) return false
  if (selectedSku.value?.stock != null && quantity.value > selectedSku.value.stock) return false
  return true
})

const addToCart = async () => {
  if (!canAddToCart.value) {
    ElMessage.warning('请选择规格并确认数量')
    return
  }
  const skuId = selectedSku.value ? selectedSku.value.skuId : product.value!.productId
  await cartStore.addToCart(skuId, quantity.value)
}

const inc = () => (quantity.value += 1)
const dec = () => {
  if (quantity.value > 1) quantity.value -= 1
}

// ---------- 数据获取 ----------
const fetchDetail = async () => {
  if (!productId.value) return
  loading.value = true
  try {
    const res = await http<Result<ProductDetailVO>>('get', `/product/detail/${productId.value}`)
    if (res.code !== 0 || !res.data) {
      ElMessage.error(res.message || '获取商品详情失败')
      product.value = null
      return
    }
    product.value = res.data

    const first = resolveDisplaySkus(res.data.skus ?? [])[0]
    selectedSkuId.value = first ? first.skuId : null
    quantity.value = 1

    // 如果第一个 SKU 有图片，用它做初始主图
    if (first?.imageUrl) {
      activeImage.value = first.imageUrl
      savedImage.value = first.imageUrl
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('获取商品详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

watch(
  () => route.params.id,
  () => fetchDetail(),
  { immediate: true }
)

const backToCategory = () => {
  const cid = product.value?.categoryId
  if (cid) router.push(`/category/${cid}`)
  else router.push('/category/0')
}
</script>

<template>
  <div class="bg-white min-h-screen">
    <div class="mx-auto max-w-[1200px] px-6 py-6">
      <!-- Loading -->
      <div v-if="loading" class="py-20 text-center text-gray-400">商品详情加载中...</div>

      <!-- Not found -->
      <div v-else-if="!product" class="py-20 text-center text-gray-400">商品不存在或已下架</div>

      <div v-else>
        <!-- 面包屑 -->
        <nav class="mb-4 text-sm text-gray-500">
          <button class="hover:text-[#c45500] hover:underline" @click="router.push('/')">首页</button>
          <span class="mx-1">›</span>
          <button class="hover:text-[#c45500] hover:underline" @click="backToCategory">分类商品</button>
          <span class="mx-1">›</span>
          <span class="text-gray-700">{{ product.name }}</span>
        </nav>

        <div class="grid grid-cols-12 gap-8">
          <!-- ========== 左侧：图片区域 ========== -->
          <section class="col-span-12 lg:col-span-5">
            <!-- 主图 -->
            <div class="border border-gray-200 rounded bg-white flex items-center justify-center overflow-hidden"
                 style="aspect-ratio: 1/1;">
              <img
                v-if="activeImage"
                :src="activeImage"
                :alt="product.name"
                class="max-h-full max-w-full object-contain transition-all duration-200"
              />
              <div v-else class="text-gray-300 text-sm">暂无图片</div>
            </div>

            <!-- 缩略图行 -->
            <div v-if="gallery.length > 1" class="mt-3 flex gap-2 overflow-x-auto">
              <button
                v-for="(img, idx) in gallery"
                :key="idx"
                class="h-16 w-16 flex-shrink-0 overflow-hidden rounded border-2 transition-colors"
                :class="activeImage === img ? 'border-[#e77600]' : 'border-gray-200 hover:border-[#e77600]'"
                @mouseenter="activeImage = img"
                @click="activeImage = img; savedImage = img"
              >
                <img :src="img" class="h-full w-full object-contain" />
              </button>
            </div>
          </section>

          <!-- ========== 右侧：商品信息 ========== -->
          <section class="col-span-12 lg:col-span-7">
            <!-- 标题 -->
            <h1 class="text-xl font-normal leading-8 text-gray-900">
              {{ product.name }}
            </h1>

            <div v-if="product.subTitle" class="mt-1 text-sm text-gray-500">
              {{ product.subTitle }}
            </div>

            <!-- 评分 -->
            <div class="mt-2 flex items-center gap-2">
              <span class="text-sm text-[#007185]">{{ product.ratingAvg ?? '0' }}</span>
              <div class="flex items-center">
                <!-- 满星 -->
                <svg v-for="i in fullStars" :key="'f'+i" class="h-4 w-4 text-[#FFA41C]" viewBox="0 0 20 20" fill="currentColor">
                  <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                </svg>
                <!-- 半星 -->
                <svg v-if="hasHalfStar" class="h-4 w-4 text-[#FFA41C]" viewBox="0 0 20 20">
                  <defs><clipPath id="halfClip"><rect x="0" y="0" width="10" height="20"/></clipPath></defs>
                  <path fill="#E0E0E0" d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  <path clip-path="url(#halfClip)" fill="currentColor" d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                </svg>
                <!-- 空星 -->
                <svg v-for="i in emptyStars" :key="'e'+i" class="h-4 w-4 text-[#E0E0E0]" viewBox="0 0 20 20" fill="currentColor">
                  <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                </svg>
              </div>
              <button v-if="product.commentCount != null" class="text-sm text-[#007185] hover:text-[#c45500] hover:underline">
                {{ product.commentCount }} 条评价
              </button>
              <span v-if="product.salesCount != null" class="text-sm text-gray-500">
                | 已售 {{ product.salesCount }}+
              </span>
            </div>

            <!-- 分隔线 -->
            <hr class="my-3 border-gray-200" />

            <!-- 价格区域 -->
            <div class="flex items-baseline gap-1">
              <span class="text-sm text-gray-500">￥</span>
              <span class="text-[28px] font-normal text-[#B12704]">{{ displayPrice }}</span>
              <span v-if="priceLabel" class="text-sm text-gray-500">{{ priceLabel }}</span>
            </div>

            <!-- 商品状态 -->
            <div class="mt-2">
              <span v-if="product.status === 1" class="text-lg text-[#007600]">有货</span>
              <span v-else class="text-lg text-[#B12704]">已下架</span>
            </div>

            <hr class="my-3 border-gray-200" />

            <!-- ===== 规格选择 ===== -->
            <div v-if="skus.length > 0" class="mb-4">
              <!-- 规格标签：随 hover/选中变化 -->
              <div class="mb-2 text-sm">
                <span class="font-bold text-gray-800">规格：</span>
                <span class="text-gray-700">{{ displaySpecLabel || '请选择' }}</span>
              </div>

              <div class="flex flex-wrap gap-2">
                <button
                  v-for="s in skus"
                  :key="s.skuId"
                  class="relative rounded border-2 px-3 py-2 text-sm transition-all duration-150"
                  :class="[
                    selectedSkuId === s.skuId
                      ? 'border-[#e77600] bg-[#FEF8F2] shadow-[0_0_3px_2px_rgba(228,121,17,0.5)]'
                      : hoveredSkuId === s.skuId
                        ? 'border-[#e77600] bg-white'
                        : 'border-gray-300 bg-white hover:border-gray-400'
                  ]"
                  @mouseenter="onSkuMouseEnter(s)"
                  @mouseleave="onSkuMouseLeave()"
                  @click="onSkuClick(s)"
                >
                  <!-- SKU 缩略图 -->
                  <div class="flex items-center gap-2">
                    <img
                      v-if="s.imageUrl"
                      :src="s.imageUrl"
                      class="h-8 w-8 rounded object-contain"
                    />
                    <div>
                      <div class="font-medium text-gray-900">{{ s.name }}</div>
                      <div class="text-xs text-gray-500">￥{{ money(s.price) }}</div>
                    </div>
                  </div>
                  <!-- 库存提示 -->
                  <div v-if="s.stock != null && s.stock <= 5 && s.stock > 0" class="mt-1 text-xs text-[#B12704]">
                    仅剩 {{ s.stock }} 件
                  </div>
                  <div v-if="s.stock === 0" class="mt-1 text-xs text-gray-400">
                    暂时缺货
                  </div>
                </button>
              </div>
            </div>

            <hr v-if="skus.length > 0" class="my-3 border-gray-200" />

            <!-- 数量 -->
            <div class="flex items-center gap-3 mb-4">
              <span class="text-sm font-bold text-gray-800">数量：</span>
              <div class="inline-flex items-center rounded border border-gray-300 bg-[#F0F2F2]">
                <button
                  class="h-8 w-8 text-lg hover:bg-[#D5D9D9] rounded-l transition-colors"
                  @click="dec"
                >−</button>
                <div class="h-8 w-12 flex items-center justify-center border-x border-gray-300 bg-white text-sm">
                  {{ quantity }}
                </div>
                <button
                  class="h-8 w-8 text-lg hover:bg-[#D5D9D9] rounded-r transition-colors"
                  @click="inc"
                >+</button>
              </div>
              <span v-if="selectedSku?.stock != null" class="text-xs text-gray-500">
                库存 {{ selectedSku.stock }} 件
              </span>
            </div>

            <!-- 操作按钮 -->
            <div class="space-y-2 max-w-[300px]">
              <button
                class="w-full rounded-full py-2 text-sm font-medium transition-colors"
                :class="canAddToCart
                  ? 'bg-[#FFD814] hover:bg-[#F7CA00] border border-[#FCD200] text-gray-900'
                  : 'bg-gray-200 text-gray-400 cursor-not-allowed'"
                :disabled="!canAddToCart"
                @click="addToCart"
              >
                加入购物车
              </button>
              <button
                class="w-full rounded-full py-2 text-sm font-medium transition-colors"
                :class="canAddToCart
                  ? 'bg-[#FFA41C] hover:bg-[#FA8900] border border-[#FF8F00] text-gray-900'
                  : 'bg-gray-200 text-gray-400 cursor-not-allowed'"
                :disabled="!canAddToCart"
                @click="addToCart"
              >
                立即购买
              </button>
            </div>

            <div v-if="product.status !== 1" class="mt-2 text-xs text-[#B12704]">
              该商品已下架
            </div>
            <div v-else-if="skus.length > 0 && !selectedSku" class="mt-2 text-xs text-[#B12704]">
              请先选择规格
            </div>

            <hr class="my-6 border-gray-200" />

            <!-- 商品详情 -->
            <div>
              <h2 class="text-lg font-bold text-gray-900 mb-3">商品详情</h2>
              <div class="text-sm text-gray-700 leading-relaxed">
                <div v-if="product.detailHtml" class="product-detail-html" v-html="product.detailHtml"></div>
                <div v-else class="text-gray-400">暂无详情描述</div>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-detail-html :deep(img) {
  max-width: 100%;
  height: auto;
}

.product-detail-html :deep(p) {
  margin-bottom: 0.5rem;
}

.product-detail-html :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 1rem 0;
}

.product-detail-html :deep(td),
.product-detail-html :deep(th) {
  border: 1px solid #e5e7eb;
  padding: 8px 12px;
  text-align: left;
}
</style>
