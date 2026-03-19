export default {
  path: "/product",
  redirect: "/product/index",
  meta: {
    icon: "ep:goods",
    title: "商品管理",
    rank: 3
  },
  children: [
    {
      path: "/product/index",
      name: "ProductManagement",
      component: () => import("@/views/product/index.vue"),
      meta: {
        title: "商品管理"
      }
    },
    {
      path: "/product/edit/:id?",
      name: "ProductEdit",
      component: () => import("@/views/product/edit.vue"),
      meta: {
        title: "编辑商品",
        showLink: false
      }
    }
  ]
} satisfies RouteConfigsTable;
