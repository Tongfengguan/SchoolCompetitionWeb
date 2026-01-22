import { createRouter, createWebHashHistory } from "vue-router";

import StudentView from "@/view/StudentView.vue";
import AdminView from "@/view/AdminView.vue";
import LoginView from "@/view/LoginView.vue";

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/login",
      name: "login",
      component: LoginView,
    },
    {
      path: "/",
      name: "student",
      component: StudentView,
    },
    {
      path: "/admin",
      name: "admin",
      component: AdminView,
    },
  ],
});

// 路由守卫：每次跳转页面前，都会执行这里
router.beforeEach((to, from, next) => {
  // 1. 从浏览器缓存里拿用户信息
  const userStr = localStorage.getItem("user");

  // 2. 如果要去的是登录页，直接放行
  if (to.path === "/login") {
    next();
    return;
  }

  // 3. 如果没登录（没拿到 user），强制跳回登录页
  if (!userStr) {
    next("/login");
    return;
  }

  // 4. 已登录，放行
  next();
});

export default router;
