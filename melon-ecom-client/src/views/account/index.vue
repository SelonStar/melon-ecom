<!-- src/views/account/index.vue -->
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import  http  from '@/utils/http'
import type { Result } from '@/types/api'

/**
 * 约定接口（你可按实际后端路径改）：
 * GET /user/getUserInfo -> Result<AccountVO>
 *
 * 如果你现在还没实现后端账号体系，也可以先返回 mock。
 */
type AccountVO = {
  userId: number
  nickname?: string
  email?: string
  avatar?: string
  createTime?: string
}

const router = useRouter()
const loading = ref(false)
const user = ref<AccountVO | null>(null)

const fetchUserInfo = async () => {
  loading.value = true
  try {
    const res = await http<Result<AccountVO>>('get', '/user/getUserInfo')
    if (res.code !== 0 || !res.data) {
      ElMessage.error(res.message || '获取账户信息失败')
      user.value = null
      return
    }
    user.value = res.data
  } catch (e) {
    console.error(e)
    ElMessage.error('获取账户信息失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(fetchUserInfo)

const goCart = () => router.push('/cart')
const goHome = () => router.push('/')
</script>

<template>
  <!-- Header + 分类导航在 index；Account 页面只写主体 -->
  <div class="mx-auto max-w-[1200px] px-6 py-8">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-semibold text-foreground">账户信息</h1>

      <div class="flex items-center gap-3">
        <button
          class="rounded-md border border-input px-3 py-2 text-sm hover:bg-accent hover:text-accent-foreground"
          @click="goHome"
        >
          回首页
        </button>
        <button
          class="rounded-md border border-input px-3 py-2 text-sm hover:bg-accent hover:text-accent-foreground"
          @click="goCart"
        >
          去购物车
        </button>
      </div>
    </div>

    <div v-if="loading" class="mt-6 rounded-lg border border-input p-10 text-sm text-muted-foreground">
      账户信息加载中...
    </div>

    <div v-else-if="!user" class="mt-6 rounded-lg border border-input p-10 text-sm text-muted-foreground">
      暂无账户信息（请先登录或确认后端 /user/getUserInfo 接口）
    </div>

    <div v-else class="mt-6 grid grid-cols-12 gap-6">
      <!-- 左：头像卡片 -->
      <section class="col-span-12 lg:col-span-4">
        <div class="rounded-xl border border-input bg-background p-6">
          <div class="flex items-center gap-4">
            <div class="h-16 w-16 overflow-hidden rounded-full bg-muted">
              <img v-if="user.avatar" :src="user.avatar" class="h-full w-full object-cover" />
            </div>
            <div>
              <div class="text-lg font-semibold">{{ user.nickname || '用户' }}</div>
              <div class="text-sm text-muted-foreground">ID：{{ user.userId }}</div>
            </div>
          </div>

          <div class="mt-6 text-sm">
            <div class="flex items-center justify-between py-2">
              <span class="text-muted-foreground">邮箱</span>
              <span class="font-medium text-foreground">{{ user.email || '-' }}</span>
            </div>
            <div class="flex items-center justify-between py-2">
              <span class="text-muted-foreground">注册时间</span>
              <span class="font-medium text-foreground">{{ user.createTime || '-' }}</span>
            </div>
          </div>

          <!-- 以后可扩展：修改资料、退出登录 -->
          <div class="mt-6">
            <button class="w-full rounded-md border border-input px-4 py-2 text-sm hover:bg-accent">
              编辑资料（后续再做）
            </button>
          </div>
        </div>
      </section>

      <!-- 右：功能入口 -->
      <section class="col-span-12 lg:col-span-8">
        <div class="rounded-xl border border-input bg-background p-6">
          <div class="text-lg font-semibold">快捷入口</div>
          <div class="mt-4 grid grid-cols-1 gap-4 md:grid-cols-2">
            <button
              class="rounded-xl border border-input p-4 text-left hover:bg-accent"
              @click="goCart"
            >
              <div class="text-sm font-medium">购物车</div>
              <div class="mt-1 text-xs text-muted-foreground">查看已加入购物车的商品</div>
            </button>

            <button
              class="rounded-xl border border-input p-4 text-left hover:bg-accent"
              @click="router.push('/order/list')"
            >
              <div class="text-sm font-medium">我的订单（占位）</div>
              <div class="mt-1 text-xs text-muted-foreground">后续接入订单列表页</div>
            </button>
          </div>

          <div class="mt-6 text-xs text-muted-foreground">
            注：订单相关页面你还没实现，我先留了入口占位，后续我们按同一交互逻辑补齐。
          </div>
        </div>
      </section>
    </div>
  </div>
</template>