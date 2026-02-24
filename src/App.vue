<script setup>
import { ref, computed } from "vue"; // 引入 computed
import { RouterLink, RouterView, useRouter, useRoute } from "vue-router";

const router = useRouter();
const route = useRoute(); //以此判断当前页面

// 判断是否是登录页（如果是登录页，就不显示导航栏）
const isLoginPage = computed(() => route.path === "/login");

// 退出登录方法
const handleLogout = () => {
  userStore.clearUser();
  router.push("/login");
};
</script>

<template>
  <div class="app-shell">
    <nav v-if="!isLoginPage" class="navbar">
      <div class="logo">学校竞赛报名系统</div>
      <div class="links"></div>
    </nav>

    <main class="main-content">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
/* 样式保持不变，新增一个退出按钮样式 */
.navbar {
  background-color: #2c3e50;
  color: white;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.logo {
  font-size: 1.2rem;
  font-weight: bold;
}
.logout-btn {
  background: none;
  border: 1px solid white;
  color: white;
  padding: 5px 15px;
  border-radius: 4px;
  cursor: pointer;
}
.logout-btn:hover {
  background: white;
  color: #2c3e50;
}
.main-content {
  min-height: 100vh;
}

*,
*::before,
*::after {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
}
</style>
