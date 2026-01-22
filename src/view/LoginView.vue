<script setup>
import { ref, reactive } from "vue";
import axios from "axios";
import { useRouter } from "vue-router";

const router = useRouter(); // 用于跳转页面
const form = reactive({ username: "", password: "" });
const errorMsg = ref("");

const handleLogin = async () => {
  if (!form.username || !form.password) return alert("请输入账号密码");

  try {
    // 1. 发送请求给后端
    const res = await axios.post("http://localhost:8080/api/auth/login", form);

    // 2. 登录成功！后端返回了用户信息
    const user = res.data;
    console.log("登录成功:", user);

    // 3. 把用户信息存到浏览器的 localStorage 里 (相当于记住了登录状态)
    // JSON.stringify 是把对象转成字符串
    localStorage.setItem("user", JSON.stringify(user));

    // 4. 根据角色跳转到不同页面
    if (user.role === "admin") {
      router.push("/admin"); // 老师去后台
    } else {
      router.push("/"); // 学生去前台
    }
  } catch (e) {
    console.error(e);
    if (e.response && e.response.status === 401) {
      errorMsg.value = "账号或密码错误！";
    } else {
      errorMsg.value = "登录失败，请检查网络";
    }
  }
};
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <h2>🔐 系统登录</h2>

      <div class="input-group">
        <label>账号</label>
        <input v-model="form.username" type="text" placeholder="请输入用户名" />
      </div>

      <div class="input-group">
        <label>密码</label>
        <input
          v-model="form.password"
          type="password"
          placeholder="请输入密码"
        />
      </div>

      <p v-if="errorMsg" class="error">{{ errorMsg }}</p>

      <button @click="handleLogin" class="login-btn">立即登录</button>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #74ebd5 0%, #9face6 100%);
}
.login-box {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  width: 350px;
}
h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}
.input-group {
  margin-bottom: 20px;
}
.input-group label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
}
.input-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
  box-sizing: border-box;
  outline: none;
  transition: border 0.3s;
}
.input-group input:focus {
  border-color: #9face6;
}
.login-btn {
  width: 100%;
  padding: 12px;
  background: #9face6;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
}
.login-btn:hover {
  background: #74ebd5;
}
.error {
  color: red;
  font-size: 12px;
  text-align: center;
  margin-bottom: 10px;
}
</style>
