<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { loginApi } from "@/api/user";
// 导入专业图标
import { User, Lock, Trophy } from "@element-plus/icons-vue";

const router = useRouter();
const userStore = useUserStore();

const form = reactive({ username: "", password: "" });
const errorMsg = ref("");
const loading = ref(false);

const handleLogin = async () => {
  if (!form.username || !form.password) return alert("请输入账号密码");

  loading.value = true;
  // console.log("准备发送请求，参数为:", JSON.stringify(form));

  try {
    const res = await loginApi(form);
    // console.log("API 原始响应:", res); // 观察这里 res 的结构

    // 适配拦截器：如果拦截器已经剥离了 data，则直接使用 res
    const userData = res.data || res;

    if (!userData || !userData.role) {
      throw new Error("后端返回的数据结构不正确，缺少角色信息");
    }

    userStore.setUser(userData);

    // 跳转逻辑
    const targetPath = userData.role === "admin" ? "/admin" : "/";
    router.push(targetPath);
  } catch (e) {
    // 👈 找回消失的日志
    console.error("登录逻辑发生错误，详情如下：");
    console.dir(e);

    if (e.response) {
      // 后端有响应，但状态码不是 2xx
      errorMsg.value =
        e.response.status === 401
          ? "账号或密码错误"
          : `服务器错误(${e.response.status})`;
    } else if (e.request) {
      // 请求发出了，但没收到响应（后端没开或跨域）
      errorMsg.value = "无法连接到服务器，请检查后端是否启动";
    } else {
      // 在设置请求时触发了其他错误
      errorMsg.value = "请求配置错误: " + e.message;
    }
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="icon-wrapper">
          <el-icon :size="40" color="#fff"><Trophy /></el-icon>
        </div>
        <h2>竞赛管理系统</h2>
        <p>Competition Management System</p>
      </div>

      <div class="login-form">
        <div class="input-item">
          <el-icon class="prefix-icon"><User /></el-icon>
          <input
            v-model="form.username"
            type="text"
            placeholder="请输入账号"
            @focus="errorMsg = ''"
          />
        </div>

        <div class="input-item">
          <el-icon class="prefix-icon"><Lock /></el-icon>
          <input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            @focus="errorMsg = ''"
            @keyup.enter="handleLogin"
          />
        </div>

        <p v-if="errorMsg" class="error-text">{{ errorMsg }}</p>

        <button @click="handleLogin" :disabled="loading" class="login-btn">
          <span v-if="!loading">立即登录</span>
          <span v-else class="loading-dots">登录中...</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 核心美化样式 */
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  /* 使用更高级的渐变色 */
  background: radial-gradient(circle at top left, #6a11cb 0%, #2575fc 100%);
}

.login-box {
  background: rgba(255, 255, 255, 0.95);
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
  width: 100%;
  max-width: 400px;
  text-align: center;
}

.login-header {
  margin-bottom: 35px;
}

.icon-wrapper {
  background: #2575fc;
  width: 70px;
  height: 70px;
  border-radius: 18px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto 15px;
  box-shadow: 0 10px 20px rgba(37, 117, 252, 0.3);
}

.login-header h2 {
  font-size: 24px;
  color: #333;
  margin: 10px 0 5px;
}

.login-header p {
  color: #999;
  font-size: 14px;
}

/* 输入框样式重构 */
.input-item {
  position: relative;
  margin-bottom: 20px;
}

.prefix-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #aaa;
}

.input-item input {
  width: 100%;
  padding: 15px 15px 15px 45px;
  border: 2px solid #f0f2f5;
  border-radius: 12px;
  font-size: 16px;
  background: #f9fafb;
  box-sizing: border-box;

  transition: all 0.3s ease;
  outline: none;
}

.input-item input:focus {
  border-color: #2575fc;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(37, 117, 252, 0.1);
  outline: none;
}

.error-text {
  color: #ff4d4f;
  font-size: 13px;
  margin-bottom: 15px;
}

.login-btn {
  width: 100%;
  padding: 15px;
  background: #2575fc;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition:
    transform 0.2s,
    background 0.3s;
}

.login-btn:hover:not(:disabled) {
  background: #1a5edb;
  transform: translateY(-2px);
}

.login-btn:active {
  transform: translateY(0);
}

.login-btn:disabled {
  background: #a0cfff;
  cursor: not-allowed;
}
</style>
