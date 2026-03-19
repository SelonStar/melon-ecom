<!-- src/views/cart/index.vue -->
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

import  http  from '@/utils/http'
import type { Result } from '@/types/api'
import type { CartVO, CartItemVO } from '@/types/cart'

const router = useRouter()

const loading = ref(false)
const cart = ref<CartVO | null>(null)

const cartItems = computed<CartItemVO[]>(() => cart.value?.items ?? [])

const toNumber = (v?: string) => {
  if (!v) return 0
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}
const money = (v?: string) => toNumber(v).toFixed(2)

// 本地“勾选”统计（你后端若未提供更新 checked 的接口，这里只做前端展示统计）
const selectedItems = computed(() => cartItems.value.filter((i) => i.checked === 1))
const selectedAmount = computed(() => {
  const sum = selectedItems.value.reduce((acc, it) => acc + toNumber(it.lineAmount), 0)
  return sum.toFixed(2)
})

const fetchCart = async () => {
  loading.value = true
  try {
    // 约定：GET /cart -> Result<CartVO>
    const res = await http<Result<CartVO>>('get', '/cart')
    if (res.code !== 0 || !res.data) {
      ElMessage.error(res.message || '获取购物车失败')
      cart.value = { items: [], totalAmount: '0.00' }
      return
    }
    cart.value = res.data
  } catch (e) {
    console.error(e)
    ElMessage.error('获取购物车失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(fetchCart)

/** 修改数量（PUT /cart/items/{skuId}）成功后刷新购物车，确保小计/总价一致 */
const setQty = async (item: CartItemVO, qty: number) => {
  if (qty < 1) return
  try {
    const res = await http<Result<null>>('put', `/cart/items/${item.skuId}`, { data: { quantity: qty } })
    if (res.code !== 0) {
      ElMessage.error(res.message || '修改数量失败')
      return
    }
    await fetchCart()
  } catch (e) {
    console.error(e)
    ElMessage.error('修改数量失败，请稍后重试')
  }
}
const inc = (item: CartItemVO) => setQty(item, item.quantity + 1)
const dec = (item: CartItemVO) => setQty(item, item.quantity - 1)

/** 删除（DELETE /cart/items/{skuId}） */
const remove = async (item: CartItemVO) => {
  try {
    const res = await http<Result<null>>('delete', `/cart/items/${item.skuId}`)
    if (res.code !== 0) {
      ElMessage.error(res.message || '删除失败')
      return
    }
    await fetchCart()
  } catch (e) {
    console.error(e)
    ElMessage.error('删除失败，请稍后重试')
  }
}

/**
 * checked：你目前接口没提供“更新 checked”端点，所以这里只做本地切换展示。
 * 真要只结算已选，后端建议提供：PATCH /cart/items/{skuId}/checked
 */
const toggleCheckedLocal = (item: CartItemVO) => {
  item.checked = item.checked === 1 ? 0 : 1
}
const toggleAllLocal = (checked: boolean) => {
  cartItems.value.forEach((it) => (it.checked = checked ? 1 : 0))
}
const allChecked = computed(() => cartItems.value.length > 0 && cartItems.value.every((i) => i.checked === 1))
const anyChecked = computed(() => cartItems.value.some((i) => i.checked === 1))

/** 结算（POST /cart/checkout） */
const goCheckout = async () => {
  try {
    const res = await http<Result<{ orderId: number; orderNo: string; totalAmount: string }>>('post', '/cart/checkout')
    if (res.code !== 0) {
      ElMessage.error(res.message || '结算失败')
      return
    }
    router.push(`/order/confirm?orderId=${res.data.orderId}`)
  } catch (e) {
    console.error(e)
    ElMessage.error('结算失败，请稍后重试')
  }
}

const goShopping = () => router.push('/')
</script>

<template>
  <!-- 全局 Header + 分类导航在 index；Cart 页面只写主体 -->
  <div class="mx-auto max-w-[1200px] px-6 py-8">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-semibold text-foreground">购物车</h1>
      <button
        class="rounded-md border border-input px-3 py-2 text-sm hover:bg-accent hover:text-accent-foreground"
        @click="goShopping"
      >
        继续购物
      </button>
    </div>

    <div v-if="loading" class="mt-6 text-sm text-muted-foreground">加载中...</div>

    <div v-else class="mt-6 grid grid-cols-12 gap-6">
      <!-- 左：商品列表 -->
      <section class="col-span-12 lg:col-span-8">
        <div v-if="cartItems.length === 0" class="rounded-lg border border-input p-10 text-center">
          <div class="text-lg font-medium">你的购物车是空的</div>
          <div class="mt-2 text-sm text-muted-foreground">去首页看看热销商品吧</div>
          <button
            class="mt-6 rounded-md bg-primary px-4 py-2 text-sm text-primary-foreground hover:opacity-90"
            @click="goShopping"
          >
            去逛逛
          </button>
        </div>

        <div v-else class="rounded-lg border border-input">
          <!-- 列表头 -->
          <div class="flex items-center gap-3 border-b border-input px-4 py-3">
            <input
              type="checkbox"
              class="h-4 w-4"
              :checked="allChecked"
              :indeterminate="!allChecked && anyChecked"
              @change="toggleAllLocal(($event.target as HTMLInputElement).checked)"
            />
            <div class="text-sm text-muted-foreground">全选</div>
          </div>

          <!-- 列表 -->
          <div
            v-for="item in cartItems"
            :key="item.skuId"
            class="flex gap-4 border-b border-input px-4 py-4 last:border-b-0"
          >
            <!-- 勾选 -->
            <div class="pt-1">
              <input type="checkbox" class="h-4 w-4" :checked="item.checked === 1" @change="toggleCheckedLocal(item)" />
            </div>

            <!-- SKU 图片 -->
            <div class="h-24 w-24 flex-shrink-0 rounded-md overflow-hidden bg-muted">
              <img
                v-if="item.imageUrl"
                :src="item.imageUrl"
                :alt="item.skuName"
                class="h-full w-full object-cover"
              />
            </div>

            <!-- 信息 -->
            <div class="flex-1">
              <div class="line-clamp-2 text-sm font-medium text-foreground">
                {{ item.skuName }}
              </div>

              <div class="mt-2 flex flex-wrap items-center gap-4">
                <div class="text-sm text-muted-foreground">
                  单价：<span class="font-medium text-foreground">￥{{ money(item.price) }}</span>
                </div>
                <div class="text-sm text-muted-foreground">
                  小计：<span class="font-medium text-foreground">￥{{ money(item.lineAmount) }}</span>
                </div>
              </div>

              <!-- 数量操作 -->
              <div class="mt-3 flex items-center justify-between">
                <div class="inline-flex items-center rounded-md border border-input">
                  <button class="h-9 w-9 text-lg hover:bg-accent" @click="dec(item)">-</button>
                  <div class="w-12 text-center text-sm">{{ item.quantity }}</div>
                  <button class="h-9 w-9 text-lg hover:bg-accent" @click="inc(item)">+</button>
                </div>

                <button class="text-sm text-destructive hover:underline" @click="remove(item)">删除</button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 右：结算卡片 -->
      <aside class="col-span-12 lg:col-span-4">
        <div class="sticky top-6 rounded-lg border border-input p-4">
          <div class="text-lg font-semibold">订单摘要</div>

          <div class="mt-4 space-y-3 text-sm">
            <div class="flex items-center justify-between">
              <span class="text-muted-foreground">已选商品数</span>
              <span class="font-medium">{{ selectedItems.length }}</span>
            </div>

            <div class="flex items-center justify-between">
              <span class="text-muted-foreground">已选合计（前端计算）</span>
              <span class="font-medium">￥{{ selectedAmount }}</span>
            </div>

            <div class="flex items-center justify-between">
              <span class="text-muted-foreground">订单总计（后端返回）</span>
              <span class="text-base font-semibold">￥{{ money(cart?.totalAmount) }}</span>
            </div>

            <div class="pt-2">
              <button
                class="w-full rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:opacity-90 disabled:opacity-50"
                :disabled="cartItems.length === 0"
                @click="goCheckout"
              >
                去结算
              </button>
              <div class="mt-2 text-xs text-muted-foreground">
                注：当前页面“勾选”仅本地展示；若要“只结算已选”，后端需提供更新 checked 的接口。
              </div>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>