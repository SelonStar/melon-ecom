<!-- src/views/home/index.vue -->
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

import { getHomeNavCards, type HomeNavCardVO } from '@/api/home'
import HomeCard from '@/components/HomeCard.vue'
import type { Result } from '@/types/api'
import http from '@/utils/http'

import {
  DEFAULT_PRODUCT_IMAGE,
  defaultHomeGuideCards,
} from './mock'
import type { HomeCard as HomeCardData, HomeProductPreview } from './mock'

type BannerVO = {
  bannerId: number
  bannerUrl: string
  linkUrl?: string
  jumpType?: string
  jumpTargetId?: number
}

type HomeProductVO = {
  productId: number
  name: string
  mainImageUrl?: string
  minPrice?: string
  categoryId?: number
  ratingAvg?: string
  commentCount?: number
  salesCount?: number
}

const router = useRouter()

const loadingBanner = ref(false)
const loadingProducts = ref(false)

const banners = ref<BannerVO[]>([])
const products = ref<HomeProductVO[]>([])
const homeNavCards = ref<HomeNavCardVO[]>([])

const hasBanner = computed(() => banners.value.length > 0)
const hasProducts = computed(() => products.value.length > 0)

const money = (v?: string) => {
  if (!v) return '0.00'
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

function unwrapResult<T>(maybe: any): Result<T> {
  if (maybe && typeof maybe === 'object' && 'data' in maybe && maybe.data && typeof maybe.data === 'object' && 'code' in maybe.data) {
    return maybe.data as Result<T>
  }
  return maybe as Result<T>
}

const fetchBanners = async () => {
  loadingBanner.value = true
  try {
    const raw = await http<Result<BannerVO[]>>('get', '/banner/getBannerList')
    const res = unwrapResult<BannerVO[]>(raw)

    if (res.code !== 0) {
      ElMessage.error(res.message || '获取轮播图失败')
      banners.value = []
      return
    }

    banners.value = Array.isArray(res.data) ? res.data : []
  } catch (error) {
    console.error(error)
    ElMessage.error('获取轮播图失败，请稍后重试')
    banners.value = []
  } finally {
    loadingBanner.value = false
  }
}

const fetchProducts = async () => {
  loadingProducts.value = true
  try {
    const raw = await http<any>('get', '/product/page', {
      params: { pageNum: 1, pageSize: 32 },
    })
    const res = unwrapResult<any>(raw)

    if (res.code !== 0) {
      ElMessage.error(res.message || '获取首页商品失败')
      products.value = []
      return
    }

    const data = res.data
    const list: HomeProductVO[] = (data?.records ?? data?.items ?? []).map((product: any) => ({
      productId: product.productId,
      name: product.name,
      mainImageUrl: product.mainImageUrl,
      minPrice: product.minPrice ?? product.price,
      categoryId: product.categoryId,
      ratingAvg: product.ratingAvg,
      commentCount: product.commentCount,
      salesCount: product.salesCount,
    }))

    products.value = list
  } catch (error) {
    console.error(error)
    ElMessage.error('获取首页商品失败，请稍后重试')
    products.value = []
  } finally {
    loadingProducts.value = false
  }
}

const fetchHomeCardConfig = async () => {
  try {
    const raw = await getHomeNavCards()
    const res = unwrapResult<HomeNavCardVO[]>(raw)

    if (res.code !== 0) {
      return
    }

    homeNavCards.value = Array.isArray(res.data) ? res.data : []
  } catch (error) {
    console.error(error)
    homeNavCards.value = []
  }
}

onMounted(async () => {
  await Promise.all([fetchBanners(), fetchProducts(), fetchHomeCardConfig()])
})

const goProduct = (productId: number) => {
  if (!productId) return
  router.push(`/product/${productId}`)
}

const goCategory = (categoryId?: number) => {
  router.push(`/category/${categoryId ?? 0}`)
}

const goLink = (link: string) => {
  router.push(link)
}

const onBannerClick = (banner: BannerVO) => {
  if (banner.linkUrl) {
    router.push(banner.linkUrl)
    return
  }

  if (banner.jumpType === 'PRODUCT' && banner.jumpTargetId) {
    router.push(`/product/${banner.jumpTargetId}`)
  } else if (banner.jumpType === 'CATEGORY' && banner.jumpTargetId) {
    router.push(`/category/${banner.jumpTargetId}`)
  }
}

const normalizeProduct = (product: HomeProductVO): HomeProductPreview => ({
  productId: product.productId,
  name: product.name,
  image: product.mainImageUrl || DEFAULT_PRODUCT_IMAGE,
  price: product.minPrice,
  categoryId: product.categoryId,
  link: product.productId ? `/product/${product.productId}` : '/category/0',
  salesCount: product.salesCount,
  ratingAvg: product.ratingAvg,
  commentCount: product.commentCount,
})

const salesRankedProducts = computed<HomeProductPreview[]>(() =>
  [...products.value]
    .sort((a, b) => (b.salesCount ?? 0) - (a.salesCount ?? 0))
    .map(normalizeProduct)
)

const recommendationRankedProducts = computed<HomeProductPreview[]>(() =>
  [...products.value]
    .sort((a, b) => {
      const scoreA =
        Number(a.ratingAvg ?? 0) * 40 + (a.commentCount ?? 0) * 0.4 + (a.salesCount ?? 0) * 0.2
      const scoreB =
        Number(b.ratingAvg ?? 0) * 40 + (b.commentCount ?? 0) * 0.4 + (b.salesCount ?? 0) * 0.2
      return scoreB - scoreA
    })
    .map(normalizeProduct)
)

const buildSearchLink = (keyword?: string) => {
  if (!keyword) return '/category/0'
  return `/category/0?q=${encodeURIComponent(keyword)}`
}

const guideCards = computed<HomeCardData[]>(() =>
  defaultHomeGuideCards.map((fallbackCard, cardIndex) => {
    const configuredCard = homeNavCards.value[cardIndex]

    return {
      title: configuredCard?.title || fallbackCard.title,
      items: fallbackCard.items.map((fallbackItem, itemIndex) => {
        const configuredItem = configuredCard?.items?.[itemIndex]

        return {
          image: configuredItem?.imageUrl || fallbackItem.image,
          label: configuredItem?.title || fallbackItem.label,
          link: configuredItem?.keyword
            ? buildSearchLink(configuredItem.keyword)
            : configuredItem?.title
              ? buildSearchLink(configuredItem.title)
              : fallbackItem.link,
        }
      }),
    }
  })
)

const hotGridProducts = computed<HomeProductPreview[]>(() => salesRankedProducts.value.slice(0, 8))

const featuredGridProducts = computed<HomeProductPreview[]>(() => {
  const selected: HomeProductPreview[] = []
  const categoryCache = new Set<number>()

  for (const product of recommendationRankedProducts.value) {
    const key = product.categoryId ?? product.productId
    if (categoryCache.has(key)) continue

    categoryCache.add(key)
    selected.push(product)
    if (selected.length === 8) break
  }

  if (selected.length < 8) {
    for (const product of recommendationRankedProducts.value) {
      if (selected.some((item) => item.productId === product.productId)) continue
      selected.push(product)
      if (selected.length === 8) break
    }
  }

  return selected
})
</script>

<template>
  <div class="home-page">
    <section class="hero-banner">
      <div class="hero-banner__inner">
        <div v-if="loadingBanner" class="p-10 text-sm text-white/90">轮播图加载中...</div>

        <div v-else-if="!hasBanner" class="p-10 text-sm text-white/90">
          暂无轮播图数据（你可以先插入 tb_banner 测试数据）
        </div>

        <el-carousel
          v-else
          class="hero-carousel"
          height="350px"
          trigger="click"
          arrow="always"
          :interval="4500"
          indicator-position="none"
        >
          <el-carousel-item v-for="banner in banners" :key="banner.bannerId">
            <button class="relative h-full w-full cursor-pointer" @click="onBannerClick(banner)">
              <img class="h-full w-full object-cover" :src="banner.bannerUrl" :alt="`banner-${banner.bannerId}`" />
              <div class="absolute inset-0 hero-banner__overlay"></div>
              <div class="hero-banner__bottom-fade"></div>
            </button>
          </el-carousel-item>
        </el-carousel>
      </div>
    </section>

    <section class="home-card-zone">
      <div class="mx-auto max-w-[1500px]">
        <div class="home-guide-grid">
          <HomeCard
            v-for="card in guideCards"
            :key="card.title"
            :title="card.title"
            :card="card"
            variant="grid"
            @navigate="goLink"
            @product-click="goProduct"
          />
        </div>

        <div class="home-content">
          <section class="home-section mt-8">
            <div class="home-section__head">
              <div>
                <h2 class="home-section__title">精选推荐</h2>
                <!-- <p class="home-section__desc">基于评分、评论量和销量做简单推荐，并尽量覆盖不同分类。</p> -->
              </div>
              <button class="home-section__link" @click="goCategory(0)">查看更多</button>
            </div>

            <div v-if="loadingProducts" class="mt-4 text-sm text-[#565959]">精选推荐加载中...</div>

            <div v-else-if="featuredGridProducts.length === 0" class="home-empty mt-4">
              暂无精选推荐数据
            </div>

            <div v-else class="product-grid mt-4">
              <article
                v-for="product in featuredGridProducts"
                :key="`featured-${product.productId}`"
                class="product-tile"
              >
                <div class="product-tile__panel">
                  <button class="product-tile__main" @click="goProduct(product.productId)">
                    <div class="product-tile__image-wrap">
                      <img class="product-tile__image" :src="product.image" :alt="product.name" />
                    </div>

                    <div class="product-tile__name">{{ product.name }}</div>
                  </button>

                  <div class="product-tile__meta">
                    <div class="product-tile__price">￥{{ money(product.price) }}</div>

                    <button
                      v-if="product.categoryId"
                      class="product-tile__category"
                      @click="goCategory(product.categoryId)"
                    >
                      查看同类
                    </button>
                  </div>

                  <div class="product-tile__stats">
                    <span v-if="product.ratingAvg != null">评分：{{ product.ratingAvg }}</span>
                    <span v-if="product.commentCount != null">评论：{{ product.commentCount }}</span>
                    <span v-if="product.salesCount != null">销量：{{ product.salesCount }}</span>
                  </div>
                </div>
              </article>
            </div>
          </section>

          <section class="home-section mt-8">
            <div class="home-section__head">
              <div>
                <h2 class="home-section__title">热销商品</h2>
                <!-- <p class="home-section__desc">按销量排序展示，保留首页核心购买入口。</p> -->
              </div>
              <button class="home-section__link" @click="goCategory(0)">查看全部</button>
            </div>

            <div v-if="loadingProducts" class="mt-4 text-sm text-[#565959]">热销商品加载中...</div>

            <div v-else-if="!hasProducts" class="home-empty mt-4">
              暂无热销商品数据（当前依赖 `/product/page` 返回商品）
            </div>

            <div v-else class="product-grid mt-4">
              <article v-for="product in hotGridProducts" :key="`hot-${product.productId}`" class="product-tile">
                <div class="product-tile__panel">
                  <button class="product-tile__main" @click="goProduct(product.productId)">
                    <div class="product-tile__image-wrap">
                      <img class="product-tile__image" :src="product.image" :alt="product.name" />
                    </div>

                    <div class="product-tile__name">{{ product.name }}</div>
                  </button>

                  <div class="product-tile__meta">
                    <div class="product-tile__price">￥{{ money(product.price) }}</div>

                    <button
                      v-if="product.categoryId"
                      class="product-tile__category"
                      @click="goCategory(product.categoryId)"
                    >
                      查看同类
                    </button>
                  </div>

                  <div class="product-tile__stats">
                    <span v-if="product.ratingAvg != null">评分：{{ product.ratingAvg }}</span>
                    <span v-if="product.commentCount != null">评论：{{ product.commentCount }}</span>
                    <span v-if="product.salesCount != null">销量：{{ product.salesCount }}</span>
                  </div>
                </div>
              </article>
            </div>
          </section>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
:deep(.el-carousel__container) {
  height: 350px;
  min-height: 350px;
}

:deep(.el-carousel__item) {
  padding: 0;
}

:deep(.el-carousel__indicators) {
  display: none;
}

:deep(.el-carousel__arrow) {
  height: 48px;
  width: 48px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.7);
  color: #111111;
  font-size: 22px;
  transition: background-color 0.2s ease;
  z-index: 4;
}

:deep(.el-carousel__arrow:hover) {
  background: rgba(255, 255, 255, 0.92);
}

:deep(.el-carousel__arrow--left) {
  left: 20px;
}

:deep(.el-carousel__arrow--right) {
  right: 20px;
}

.home-page {
  min-height: 100%;
  background: #f3f3f3;
}

.hero-banner {
  position: relative;
  overflow: hidden;
  background: #131921;
}

.hero-banner::before,
.hero-banner::after {
  position: absolute;
  top: 0;
  z-index: 3;
  height: 350px;
  width: 80px;
  content: '';
  pointer-events: none;
}

.hero-banner::before {
  left: 0;
  background: linear-gradient(to right, rgba(0, 0, 0, 0.35), transparent);
}

.hero-banner::after {
  right: 0;
  background: linear-gradient(to left, rgba(0, 0, 0, 0.35), transparent);
}

.hero-banner__inner {
  width: 100%;
}

.hero-carousel {
  width: 100%;
}

.hero-banner__overlay {
  background: linear-gradient(180deg, rgba(19, 25, 33, 0.04) 0%, rgba(19, 25, 33, 0.2) 100%);
}

.home-card-zone {
  background: #f3f3f3;
}

.home-guide-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 24px;
  padding: 12px;
}

.home-content {
  padding: 0 12px 24px;
}

.home-section {
  border: 0;
  border-radius: 4px;
  background: #ffffff;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.home-section__head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
}

.home-section__title {
  margin: 0;
  color: #0f1111;
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
}

.home-section__desc {
  margin: 6px 0 0;
  color: #565959;
  font-size: 14px;
}

.home-section__link {
  border: 0;
  background: transparent;
  color: #007185;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  padding: 0;
}

.home-section__link:hover {
  color: #c7511f;
  text-decoration: underline;
}

.home-empty {
  border: 1px dashed #d5d9d9;
  border-radius: 14px;
  background: #fafafa;
  color: #565959;
  padding: 24px;
}

.product-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.product-tile {
  min-width: 0;
}

.product-tile__panel {
  display: flex;
  height: 100%;
  width: 100%;
  flex-direction: column;
  border: 0;
  border-radius: 4px;
  background: #ffffff;
  cursor: pointer;
  padding: 14px;
  text-align: left;
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease;
}

.product-tile__panel:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);
}

.product-tile__main {
  display: flex;
  flex: 1;
  flex-direction: column;
  border: 0;
  background: transparent;
  cursor: pointer;
  padding: 0;
  text-align: left;
}

.product-tile__image-wrap {
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: 12px;
  background: #f7f8fa;
}

.product-tile__image {
  height: 100%;
  width: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-tile__panel:hover .product-tile__image {
  transform: scale(1.05);
}

.product-tile__name {
  display: -webkit-box;
  margin-top: 14px;
  overflow: hidden;
  color: #0f1111;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.45;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.product-tile__meta {
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.product-tile__price {
  color: #b12704;
  font-size: 20px;
  font-weight: 700;
}

.product-tile__category {
  border: 1px solid #d5d9d9;
  border-radius: 999px;
  background: #f7fafa;
  color: #0f1111;
  cursor: pointer;
  font-size: 12px;
  padding: 6px 10px;
}

.product-tile__category:hover {
  background: #eef6f7;
}

.product-tile__stats {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: #565959;
  font-size: 12px;
}

@media (max-width: 1279px) {
  .home-guide-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .product-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1023px) {
  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  :deep(.el-carousel__container) {
    height: 260px;
    min-height: 260px;
  }

  .hero-banner::before,
  .hero-banner::after {
    height: 260px;
    width: 56px;
  }

  :deep(.el-carousel__arrow) {
    height: 40px;
    width: 40px;
  }

  .home-guide-grid {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }

  .home-section {
    padding: 16px;
  }

  .home-section__head {
    flex-direction: column;
    align-items: flex-start;
  }

  .product-grid {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }
}
</style>
