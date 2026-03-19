// src/router/index.ts
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import index from '@/layouts/index.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: index,
    children: [
      // 首页：轮播图 + 热销商品
      { path: '', name: 'home', component: () => import('@/views/home/index.vue') },

      // 分类商品罗列页（也可承载搜索结果）
      { path: 'category/:id', name: 'category', component: () => import('@/views/category/index.vue') },

      // 商品详情页
      { path: 'product/:id', name: 'product', component: () => import('@/views/product/index.vue') },

      // AI 试穿页
      { path: 'tryon', name: 'tryon', component: () => import('@/views/tryon/index.vue') },

      // 购物车页
      { path: 'cart', name: 'cart', component: () => import('@/views/cart/index.vue') },

      // 账户信息页
      { path: 'account', name: 'account', component: () => import('@/views/account/index.vue') },

      // （可选）订单确认页占位：你 cart 页面里跳了 /order/confirm
      { path: 'order/confirm', name: 'orderConfirm', component: () => import('@/views/order/confirm.vue') },
    ],
  },

  // 兜底 404（也可以重定向到首页）
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

export default router
