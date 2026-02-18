import { defineStore } from "pinia";
import { ref } from "vue";

export const useUserStore = defineStore("user", () => {
  // 从本地存储初始化，防止页面刷新后登录状态丢失
  const userInfo = ref(JSON.parse(localStorage.getItem("user")) || null);

  const setUser = (data) => {
    userInfo.value = data;
    // 持久化存储用户信息
    localStorage.setItem("user", JSON.stringify(data));

    // ✨ 健壮性处理：只有当 data.token 存在时才存储，避免存入 "undefined" 字符串
    if (data.token) {
      localStorage.setItem("token", data.token);
    }
  };

  const clearUser = () => {
    userInfo.value = null;
    localStorage.removeItem("user");
    localStorage.removeItem("token");
  };

  return {
    userInfo,
    setUser,
    clearUser,
  };
});
