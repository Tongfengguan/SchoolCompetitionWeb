<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { loginApi } from "@/api/user";
import { User, Lock, ArrowRight, Warning } from "@element-plus/icons-vue";

const router = useRouter();
const userStore = useUserStore();

const form = reactive({ username: "", password: "" });
const errorMsg = ref("");
const loading = ref(false);

const handleLogin = async () => {
  if (!form.username || !form.password) return;
  loading.value = true;
  errorMsg.value = "";

  try {
    const res = await loginApi(form);
    const userData = res.data || res;
    userStore.setUser(userData);
    const targetPath = userData.role === "admin" ? "/admin" : "/student";
    router.push(targetPath);
  } catch (e) {
    errorMsg.value = e.message || "账号或密码错误";
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="login-page">
    <!-- 左侧：视觉区域 -->
    <div class="brand-section">
      <div class="brand-content">
        <div class="logo-wrapper">
          <div class="logo-icon">SC</div>
          <h1 class="brand-title">School<span>Competition</span></h1>
        </div>
        <p class="brand-desc">
          基于现代技术的校园竞赛全生命周期管理平台。<br />
          让每一场竞赛更公平、更高效、更具影响力。
        </p>
        <div class="abstract-shape"></div>
      </div>
      <div class="brand-footer">
        © 2026 SchoolCompetition System. All rights reserved.
      </div>
    </div>

    <!-- 右侧：登录区域 -->
    <div class="form-section">
      <div class="login-card">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>请登录您的账号以继续</p>
        </div>

        <div class="login-form">
          <div class="input-group" :class="{ error: errorMsg }">
            <label>用户名 / 手机号</label>
            <div class="input-wrapper">
              <el-icon class="icon"><User /></el-icon>
              <input
                v-model="form.username"
                placeholder="请输入您的账号"
                @keyup.enter="handleLogin"
              />
            </div>
          </div>

          <div class="input-group" :class="{ error: errorMsg }">
            <label>登录密码</label>
            <div class="input-wrapper">
              <el-icon class="icon"><Lock /></el-icon>
              <input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                @keyup.enter="handleLogin"
              />
            </div>
          </div>

          <div v-if="errorMsg" class="error-banner">
            <el-icon><Warning /></el-icon>
            <span>{{ errorMsg }}</span>
          </div>

          <button class="submit-btn" :disabled="loading" @click="handleLogin">
            <span v-if="!loading">登录系统</span>
            <span v-else>验证中...</span>
            <el-icon v-if="!loading" class="arrow"><ArrowRight /></el-icon>
          </button>

          <div class="form-footer">
            <span>忘记密码？</span>
            <a href="#">联系管理员</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 核心布局 */
.login-page {
  height: 100vh;
  display: flex;
  background-color: #ffffff;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

/* 左侧品牌区 */
.brand-section {
  flex: 1.2;
  background-color: #0a0a0a;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 80px;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 2;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 40px;
}

.logo-icon {
  width: 45px;
  height: 45px;
  background: #3b82f6;
  color: white;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 18px;
  letter-spacing: -1px;
}

.brand-title {
  color: #ffffff;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.brand-title span {
  color: #3b82f6;
}

.brand-desc {
  color: #888888;
  font-size: 18px;
  line-height: 1.6;
  max-width: 480px;
}

/* 装饰性几何图形 */
.abstract-shape {
  position: absolute;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.15) 0%, transparent 70%);
  top: -200px;
  left: -200px;
  filter: blur(60px);
}

.brand-footer {
  position: absolute;
  bottom: 40px;
  left: 80px;
  color: #444444;
  font-size: 13px;
}

/* 右侧表单区 */
.form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background-color: #f9fafb;
}

.login-card {
  width: 100%;
  max-width: 420px;
}

.form-header {
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 32px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 10px;
}

.form-header p {
  color: #6b7280;
  font-size: 16px;
}

/* 表单细节 */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-group label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-wrapper .icon {
  position: absolute;
  left: 16px;
  color: #9ca3af;
  font-size: 18px;
}

.input-wrapper input {
  width: 100%;
  padding: 14px 16px 14px 48px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  font-size: 15px;
  background-color: white;
  transition: all 0.2s ease;
  outline: none;
}

.input-wrapper input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

/* 错误状态 */
.error-banner {
  background-color: #fef2f2;
  border: 1px solid #fee2e2;
  padding: 12px 16px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #dc2626;
  font-size: 14px;
}

/* 按钮 */
.submit-btn {
  margin-top: 10px;
  width: 100%;
  padding: 16px;
  background-color: #111827;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.2s ease;
}

.submit-btn:hover:not(:disabled) {
  background-color: #1f2937;
  transform: translateY(-1px);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.submit-btn .arrow {
  transition: transform 0.2s ease;
}

.submit-btn:hover .arrow {
  transform: translateX(4px);
}

.form-footer {
  text-align: center;
  font-size: 14px;
  color: #6b7280;
  margin-top: 10px;
}

.form-footer a {
  color: #3b82f6;
  text-decoration: none;
  font-weight: 600;
  margin-left: 5px;
}

/* 响应式适配 */
@media (max-width: 1024px) {
  .brand-section {
    display: none;
  }
  .form-section {
    background-color: white;
  }
}
</style>
