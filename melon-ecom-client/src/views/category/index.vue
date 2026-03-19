<!-- src/views/category/index.vue -->
<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import  http  from '@/utils/http'
import type { ResultTable } from '@/types/api'

/**
 * 分类商品列表页（也承载搜索结果）：
 * - 从 Header 搜索进入：/category/0?q=关键词
 * - 从分类导航进入：/category/:id
 *
 * 后端接口我这里做了一个约定（你可按你后端实际路径调整）：
 * GET /product/page
 * params: { categoryId?, keyword?, pageNum, pageSize }
 * resp: ResultTable<ProductListItemVO>
 */
type ProductListItemVO = {
  productId: number
  name: string
  mainImageUrl?: string
  minPrice?: string // BigDecimal -> string
  ratingAvg?: string
  commentCount?: number
  salesCount?: number
  categoryId?: number
}

const route = useRoute()
const router = useRouter()

const list = ref<ProductListItemVO[]>([])
const loading = ref(false)

const categoryId = computed(() => {
  const raw = route.params.id
  const n = Number(raw)
  return Number.isFinite(n) ? n : 0
})
const keyword = computed(() => (route.query.q ? String(route.query.q) : ''))
// 分页
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

const pagerState = reactive({
  layout: 'total, sizes, prev, pager, next, jumper',
  pageSizes: [12, 24, 36, 48],
})

const money = (v?: string) => {
  if (!v) return '0.00'
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const params: Record<string, any> = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
    }
    // categoryId=0 视为“全分类搜索/全量”
    if (categoryId.value && categoryId.value !== 0) params.categoryId = categoryId.value
    if (keyword.value) params.keyword = keyword.value

    const res = await http<ResultTable<ProductListItemVO>>('get', '/product/page', { params })
    if (res.code !== 0 || !res.data) {
      ElMessage.error(res.message || '获取商品列表失败')
      list.value = []
      total.value = 0
      return
    }

    list.value = res.data.items ?? res.data.records ?? []
    total.value = res.data.total ?? 0
  } catch (e) {
    console.error(e)
    ElMessage.error('获取商品列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  fetchProducts()
}
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchProducts()
}

const goProduct = (productId: number) => router.push(`/product/${productId}`)

const clearSearch = () => {
  // 清掉 q，保留分类
  router.push({ path: `/category/${categoryId.value || 0}`, query: {} })
}

watch(
  () => [route.params.id, route.query.q],
  () => {
    currentPage.value = 1
    fetchProducts()
  },
  { immediate: true }
)

onMounted(() => {
  // watch immediate 已经触发，这里留空也可
})
</script>

<template>
  <div class="mx-auto max-w-[1200px] px-6 py-6">
    <!-- 顶部信息栏：显示当前模式（分类 or 搜索） -->
    <div class="mb-4 flex items-center justify-between">
      <div class="flex items-center gap-3">
        <h1 class="text-xl font-semibold text-foreground">
          <span v-if="categoryId !== 0">分类商品</span>
          <span v-else>全部商品</span>
        </h1>

        <button
          v-if="keyword"
          class="rounded-md border border-input px-3 py-1.5 text-sm hover:bg-accent hover:text-accent-foreground"
          @click="clearSearch"
        >
          清除搜索
        </button>
      </div>

      <div class="text-sm text-muted-foreground" v-if="!loading">
        共 {{ total }} 件
      </div>
    </div>

    <!-- 商品网格 -->
    <div v-if="loading" class="rounded-lg border border-input p-10 text-sm text-muted-foreground">
      商品加载中...
    </div>

    <div v-else-if="list.length === 0" class="rounded-lg border border-input p-10 text-sm text-muted-foreground">
      暂无商品
    </div>

    <div v-else class="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
      <div
        v-for="p in list"
        :key="p.productId"
        class="group rounded-xl border border-input bg-background p-3 shadow-sm transition hover:shadow-md"
      >
        <button class="w-full text-left" @click="goProduct(p.productId)">
          <div class="aspect-square w-full overflow-hidden rounded-lg bg-muted">
            <img
              v-if="p.mainImageUrl"
              class="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
              :src="p.mainImageUrl"
              :alt="p.name"
            />
          </div>

          <div class="mt-3 line-clamp-2 text-sm font-medium text-foreground">
            {{ p.name }}
          </div>

          <div class="mt-2 flex items-center justify-between">
            <div class="text-base font-semibold text-foreground">￥{{ money(p.minPrice) }}</div>
          </div>

          <div class="mt-2 text-xs text-muted-foreground">
            <span v-if="p.ratingAvg != null">评分：{{ p.ratingAvg }}</span>
            <span v-if="p.commentCount != null" class="ml-3">评论：{{ p.commentCount }}</span>
            <span v-if="p.salesCount != null" class="ml-3">销量：{{ p.salesCount }}</span>
          </div>
        </button>
      </div>
    </div>

    <!-- 分页 -->
    <nav v-if="total > 0" class="mt-8 flex w-full justify-center">
      <el-pagination
        v-model:page-size="pageSize"
        v-model:current-page="currentPage"
        :total="total"
        :page-sizes="pagerState.pageSizes"
        :layout="pagerState.layout"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </nav>
  </div>
</template>
