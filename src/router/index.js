import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from "@/stores/user";

// 1. 定义页面路由
const routes = [
  {
    path: "/",
    redirect: "/login", // 默认重定向到登录页
  },
  {
    path: "/login",
    name: "Login",
    component: () => import("@/view/LoginView.vue"),
    meta: { requiresAuth: false }, // 明确标记不需要登录
  },
  {
    path: "/admin",
    name: "Admin",
    component: () => import("@/view/AdminView.vue"),
    meta: {
      requiresAuth: true,
      role: "admin", // ✨ 核心：打上“仅限管理员”的标签
    },
  },
  {
    path: "/student",
    name: "Student",
    component: () => import("@/view/StudentView.vue"),
    meta: {
      requiresAuth: true,
      role: "student", // ✨ 核心：打上“仅限学生”的标签
    },
  },
  // 捕获所有未定义的路由，重定向到登录页（防止 404）
  {
    path: "/:pathMatch(.*)*",
    redirect: "/login",
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 2. ✨ 全局前置守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore();

  // 💡 核心修复：检查 userInfo 是否存在，而不是 token
  // 只要 userInfo 里有数据（比如有个 id 或者是个非空对象），就认为已经成功登录
  const isAuthenticated =
    !!userStore.userInfo && Object.keys(userStore.userInfo).length > 0;

  const userRole = userStore.userInfo?.role;

  // 规则 A：如果前往的页面需要登录，但用户没登录
  if (to.meta.requiresAuth && !isAuthenticated) {
    alert("请先登录系统！");
    return next("/login");
  }

  // 规则 B：如果页面对角色有要求，且当前用户角色不匹配
  if (to.meta.role && userRole !== to.meta.role && userRole === "student") {
    alert("❌ 越权访问拦截：您没有权限进入该页面！");
    if (userRole === "student") return next("/student");
    return next("/login");
  }

  // 规则 C：如果已登录用户还要往登录页跑，直接送回他的主页
  if (to.path === "/login" && isAuthenticated) {
    if (userRole === "admin") return next("/admin");
    if (userRole === "student") return next("/student");
  }

  // 所有检查通过，放行！
  next();
});

export default router;
