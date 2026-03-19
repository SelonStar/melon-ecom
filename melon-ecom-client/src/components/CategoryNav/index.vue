<!-- src/components/CategoryNav.vue -->
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import http from '@/utils/http'
import type { Result } from '@/types/api'

type Category = {
  categoryId: number
  name: string
  children?: Category[]
}

const router = useRouter()
const categories = ref<Category[]>([])

const TRYON_NAV_KEY = -999999
const TRYON_NAV_NAME = 'AI 试穿'

async function loadCategories() {
  try {
    const res = await http<Result<Category[]>>('get', '/category/tree')
    if (res.code !== 0 || !Array.isArray(res.data)) {
      ElMessage.error(res.message || '获取分类失败')
      categories.value = []
      return
    }
    categories.value = res.data
  } catch (error) {
    console.error(error)
    ElMessage.error('获取分类失败，请稍后重试')
    categories.value = []
  }
}

onMounted(loadCategories)

const goCategory = (id: number) => {
  router.push(`/category/${id}`)
}

const navItems = computed(() => {
  const items = [...categories.value]
  const tryOnItem: Category = { categoryId: TRYON_NAV_KEY, name: TRYON_NAV_NAME }
  const fashionIndex = items.findIndex((item) => item.name === '时尚潮玩')

  if (fashionIndex >= 0) {
    items.splice(fashionIndex + 1, 0, tryOnItem)
  } else {
    items.push(tryOnItem)
  }

  return items
})

const goNav = (item: Category) => {
  if (item.categoryId === TRYON_NAV_KEY) {
    router.push('/tryon')
    return
  }
  goCategory(item.categoryId)
}
</script>

<template>
  <nav class="category-nav w-full">
    <div class="mx-auto flex h-10 max-w-[1500px] items-center gap-1 px-4">
      <button
        v-for="c in navItems"
        :key="`${c.categoryId}-${c.name}`"
        class="inline-flex items-center gap-1 rounded px-3 py-1 text-sm text-white hover:outline hover:outline-1 hover:outline-white transition-colors"
        :class="{ 'bg-white/10 font-bold': c.categoryId === TRYON_NAV_KEY }"
        @click="goNav(c)"
      >
        <svg
          v-if="c.categoryId === TRYON_NAV_KEY"
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 24 24"
          fill="currentColor"
          class="h-4 w-4 text-[#ffd814]"
          aria-hidden="true"
        >
          <path d="M12 2l1.76 4.24L18 8l-4.24 1.76L12 14l-1.76-4.24L6 8l4.24-1.76L12 2zm6.5 9l.88 2.12L21.5 14l-2.12.88L18.5 17l-.88-2.12L15.5 14l2.12-.88L18.5 11zM5.5 13l1.17 2.83L9.5 17l-2.83 1.17L5.5 21l-1.17-2.83L1.5 17l2.83-1.17L5.5 13z"/>
        </svg>
        {{ c.name }}
      </button>
    </div>
  </nav>
</template>

<style scoped>
.category-nav {
  background-color: #232f3e;
}
</style>
