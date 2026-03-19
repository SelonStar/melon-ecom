<!-- src/layouts/index.vue -->
<script setup lang="ts">
import Header from '@/components/header/index.vue'
import CategoryNav from '@/components/CategoryNav/index.vue'
import LoginDialog from '@/components/Auth/LoginDialog.vue'
</script>

<template>
  <div class="min-h-screen bg-background text-foreground flex flex-col">
    <!-- Amazon 风格：全局顶部栏（Logo / 搜索 / 账户 / 购物车） -->
    <Header />

    <!-- 顶部之后：分类导航栏 -->
    <CategoryNav />

    <!-- 页面主体 -->
    <main class="flex-1">
      <router-view />
    </main>

    <!-- 全局 footer -->
    <footer class="border-t border-input">
      <div class="mx-auto max-w-[1200px] px-6 py-6 text-sm text-muted-foreground">
        © Melon-Ecom
      </div>
    </footer>

    <!-- 全局登录弹窗 -->
    <LoginDialog />
  </div>
</template>
