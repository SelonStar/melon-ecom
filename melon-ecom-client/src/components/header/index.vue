<!-- src/components/Header.vue -->
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { UserStore } from '@/stores/modules/user'
import { useAuthModalStore } from '@/stores/modules/authModal'

const router = useRouter()
const userStore = UserStore()
const authModal = useAuthModalStore()
const keyword = ref('')

const goHome = () => router.push('/')
const goAccount = () => {
  if (userStore.isLoggedIn) {
    router.push('/account')
  } else {
    authModal.open('login')
  }
}
const goCart = () => router.push('/cart')

const onSearch = () => {
  if (!keyword.value) return
  router.push({
    path: '/category/0',
    query: { q: keyword.value },
  })
}
</script>

<template>
  <header class="header-nav w-full">
    <div class="mx-auto flex h-16 max-w-[1500px] items-center gap-4 px-6">
      <!-- Logo -->
      <button class="flex items-center gap-1 text-xl font-bold tracking-wide text-white shrink-0" @click="goHome">
        <span class="text-[#ff9900]">&#9679;</span> Melon
      </button>

      <!-- 搜索框 -->
      <div class="flex flex-1 items-center">
        <input
          v-model="keyword"
          @keyup.enter="onSearch"
          class="h-10 w-full rounded-l-md border-none px-3 text-sm text-gray-900 outline-none focus:ring-2 focus:ring-[#ff9900]"
          placeholder="搜索商品"
        />
        <button
          class="h-10 rounded-r-md bg-[#febd69] px-4 text-sm text-gray-900 hover:bg-[#f3a847] transition-colors"
          @click="onSearch"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd" />
          </svg>
        </button>
      </div>

      <!-- 账户 -->
      <button
        class="flex flex-col items-start text-white hover:outline hover:outline-1 hover:outline-white rounded px-2 py-1 shrink-0"
        @click="goAccount"
      >
        <span class="text-xs text-gray-300">
          {{ userStore.isLoggedIn ? `Hello, ${userStore.userInfo.username}` : 'Hello, Sign in' }}
        </span>
        <span class="text-sm font-bold">Account & Lists</span>
      </button>

      <!-- 购物车 -->
      <button
        class="flex items-center gap-1 text-white hover:outline hover:outline-1 hover:outline-white rounded px-2 py-1 shrink-0"
        @click="goCart"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 100 4 2 2 0 000-4z" />
        </svg>
        <span class="text-sm font-bold">Cart</span>
      </button>
    </div>
  </header>
</template>

<style scoped>
.header-nav {
  background-color: #131921;
}
</style>