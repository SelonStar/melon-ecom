export default {
  path: "/home-nav-card",
  redirect: "/home-nav-card/index",
  meta: {
    icon: "ri:layout-grid-fill",
    title: "导航卡管理",
    rank: 7
  },
  children: [
    {
      path: "/home-nav-card/index",
      name: "HomeNavCardManagement",
      component: () => import("@/views/home-nav-card/index.vue"),
      meta: {
        title: "导航卡管理"
      }
    }
  ]
} satisfies RouteConfigsTable;
