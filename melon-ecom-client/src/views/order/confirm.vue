<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/utils/http'
import type { Result } from '@/types/api'

const route = useRoute()
const router = useRouter()

const orderId = computed(() => Number(route.query.orderId))

// ===== 状态 =====
const loading   = ref(false)
const paying    = ref(false)
const cancelling = ref(false)

interface OrderItemVO {
  skuId: number
  skuName: string
  quantity: number
  unitPrice: string
  amount: string
}
interface OrderDetailVO {
  orderId: number
  orderNo: string
  status: number   // 0=未支付 1=已支付 2=已取消 3=支付中
  totalAmount: string
  createTime: string
  payTime: string | null
  items: OrderItemVO[]
}

const order   = ref<OrderDetailVO | null>(null)
const balance = ref(0)

// ===== 计算 =====
const isPaid      = computed(() => order.value?.status === 1)
const isCancelled = computed(() => order.value?.status === 2)
const canPay      = computed(() => order.value?.status === 0)
const sufficient  = computed(() => balance.value >= Number(order.value?.totalAmount ?? 0))

const STATUS_TEXT: Record<number, string> = {
  0: '待支付', 1: '已支付', 2: '已取消', 3: '支付中'
}
const STATUS_COLOR: Record<number, string> = {
  0: 'text-orange-500', 1: 'text-green-600', 2: 'text-gray-400', 3: 'text-blue-500'
}

// ===== 接口 =====
async function fetchOrder() {
  if (!orderId.value) return
  loading.value = true
  try {
    const res = await http<Result<OrderDetailVO>>('get', `/orders/${orderId.value}`)
    if (res.code === 0) order.value = res.data
    else ElMessage.error(res.message || '加载订单失败')
  } catch {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

async function fetchWallet() {
  try {
    const res = await http<Result<number>>('get', '/payments/wallet')
    if (res.code === 0) balance.value = Number(res.data) || 0
  } catch { /* 静默 */ }
}

async function handlePay() {
  if (!canPay.value) return
  if (!sufficient.value) { ElMessage.warning('余额不足，无法支付'); return }

  try {
    await ElMessageBox.confirm(
      `确认使用余额支付 ￥${Number(order.value!.totalAmount).toFixed(2)} 吗？`,
      '确认支付',
      { confirmButtonText: '确认支付', cancelButtonText: '取消', type: 'warning' }
    )
  } catch { return }

  paying.value = true
  try {
    const res = await http<Result<string>>('post', '/payments/balance/pay', {
      data: { orderId: orderId.value }
    })
    if (res.code === 0) {
      ElMessage.success('支付成功！')
      await fetchOrder()
      await fetchWallet()
    } else {
      ElMessage.error(res.message || '支付失败')
    }
  } catch {
    ElMessage.error('支付失败，请稍后重试')
  } finally {
    paying.value = false
  }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('确认取消该订单吗？', '取消订单', {
      confirmButtonText: '确认取消', cancelButtonText: '再想想', type: 'warning'
    })
  } catch { return }

  cancelling.value = true
  try {
    const res = await http<Result<string>>('post', `/orders/${orderId.value}/cancel`)
    if (res.code === 0) {
      ElMessage.success('订单已取消')
      router.push('/')
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch {
    ElMessage.error('取消失败，请稍后重试')
  } finally {
    cancelling.value = false
  }
}

onMounted(() => {
  if (!orderId.value) { router.replace('/cart'); return }
  fetchOrder()
  fetchWallet()
})
</script>

<template>
  <div class="mx-auto max-w-[1100px] px-6 py-8">
    <!-- 标题 -->
    <div class="mb-6 flex items-center gap-3">
      <button class="text-sm text-primary hover:underline" @click="router.push('/cart')">
        ← 返回购物车
      </button>
      <h1 class="text-xl font-semibold">确认订单 &amp; 支付</h1>
    </div>

    <div v-if="loading" class="py-20 text-center text-muted-foreground">加载中…</div>

    <template v-else-if="order">
      <div class="grid grid-cols-12 gap-6">

        <!-- 左：订单明细 -->
        <section class="col-span-12 lg:col-span-8 space-y-4">
          <!-- 订单信息条 -->
          <div class="flex items-center justify-between rounded-lg border border-input bg-card p-4 text-sm">
            <div class="space-y-1">
              <div>
                <span class="text-muted-foreground">订单编号：</span>
                <span class="font-mono text-foreground">{{ order.orderNo }}</span>
              </div>
              <div>
                <span class="text-muted-foreground">下单时间：</span>
                <span>{{ order.createTime?.replace('T', ' ').slice(0, 19) }}</span>
              </div>
            </div>
            <span :class="['text-base font-semibold', STATUS_COLOR[order.status]]">
              {{ STATUS_TEXT[order.status] ?? '未知' }}
            </span>
          </div>

          <!-- 商品列表 -->
          <div class="rounded-lg border border-input bg-card">
            <div class="border-b border-input px-4 py-3 text-sm font-medium">
              商品清单（{{ order.items.length }} 件）
            </div>
            <div
              v-for="item in order.items"
              :key="item.skuId"
              class="flex items-center justify-between border-b border-input px-4 py-3 last:border-b-0 text-sm"
            >
              <div class="flex-1">
                <div class="font-medium text-foreground">{{ item.skuName }}</div>
                <div class="mt-1 text-muted-foreground">
                  单价 ￥{{ Number(item.unitPrice).toFixed(2) }} × {{ item.quantity }}
                </div>
              </div>
              <div class="ml-4 font-semibold whitespace-nowrap">
                ￥{{ Number(item.amount).toFixed(2) }}
              </div>
            </div>
          </div>

          <!-- 支付成功提示 -->
          <div
            v-if="isPaid"
            class="rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-700"
          >
            ✅ 已于 {{ order.payTime?.replace('T', ' ').slice(0, 19) }} 支付成功
          </div>
          <div
            v-if="isCancelled"
            class="rounded-lg border border-gray-200 bg-gray-50 px-4 py-3 text-sm text-gray-500"
          >
            此订单已取消
          </div>
        </section>

        <!-- 右：支付卡片 -->
        <aside class="col-span-12 lg:col-span-4">
          <div class="sticky top-6 rounded-lg border border-input bg-card p-5 space-y-4">
            <h2 class="text-base font-semibold">支付方式</h2>

            <!-- 金额汇总 -->
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-muted-foreground">订单总额</span>
                <span class="text-lg font-bold">￥{{ Number(order.totalAmount).toFixed(2) }}</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-muted-foreground">账户余额</span>
                <span
                  :class="[
                    'font-medium',
                    sufficient ? 'text-green-600' : 'text-red-500'
                  ]"
                >
                  ￥{{ balance.toFixed(2) }}
                </span>
              </div>
              <div v-if="canPay && !sufficient" class="text-xs text-red-500">
                余额不足，还差 ￥{{ (Number(order.totalAmount) - balance).toFixed(2) }}
              </div>
            </div>

            <hr class="border-input" />

            <!-- 操作按钮 -->
            <template v-if="canPay">
              <button
                class="w-full rounded-md bg-primary px-4 py-2.5 text-sm font-semibold
                       text-primary-foreground hover:opacity-90
                       disabled:opacity-40 disabled:cursor-not-allowed"
                :disabled="paying || !sufficient"
                @click="handlePay"
              >
                {{ paying ? '支付中…' : '余额支付' }}
              </button>
              <button
                class="w-full rounded-md border border-input px-4 py-2 text-sm
                       text-muted-foreground hover:bg-accent disabled:opacity-40"
                :disabled="cancelling"
                @click="handleCancel"
              >
                {{ cancelling ? '取消中…' : '取消订单' }}
              </button>
            </template>

            <template v-else-if="isPaid">
              <button
                class="w-full rounded-md bg-green-600 px-4 py-2.5 text-sm font-semibold text-white hover:opacity-90"
                @click="router.push('/')"
              >
                继续购物
              </button>
            </template>

            <template v-else-if="isCancelled">
              <button
                class="w-full rounded-md border border-input px-4 py-2 text-sm hover:bg-accent"
                @click="router.push('/')"
              >
                返回首页
              </button>
            </template>

            <p class="text-xs text-center text-muted-foreground">
              Demo 环境，余额支付仅扣除账户虚拟余额
            </p>
          </div>
        </aside>
      </div>
    </template>

    <div v-else class="py-20 text-center text-muted-foreground">订单不存在或无权访问</div>
  </div>
</template>
