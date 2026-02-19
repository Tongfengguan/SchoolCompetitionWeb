import axios from "axios";

const service = axios.create({
  baseURL: "http://localhost:8080/api",
  timeout: 5000,
});

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      // ✨ 修正：使用反引号以正确注入变量
      config.headers["Authorization"] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    // 1. 处理文件下载 (Blob)
    if (
      response.data instanceof Blob ||
      response.config.responseType === "blob"
    ) {
      return response.data;
    }

    const res = response.data;

    // 2. 兼容原生数组 (Spring JPA 默认返回)
    if (Array.isArray(res)) {
      return res;
    }

    // 3. 处理带状态码的包装对象
    if (res.code !== undefined && res.code !== 200) {
      alert(res.msg || "业务处理失败");
      return Promise.reject(new Error(res.msg || "Error"));
    }

    // 4. 返回核心数据
    if (res.code === 200 && res.data !== undefined) {
      return res.data;
    }

    return res;
  },
  (error) => {
    console.error("请求发生错误:", error);

    // ✨ 增强：区分服务器报错和网络/跨域报错
    if (error.response) {
      const status = error.response.status;
      if (status === 500) {
        alert("服务器内部错误 (500)，请检查后端日志");
      } else if (status === 403) {
        alert("权限不足，拒绝访问");
      } else if (status === 401) {
        alert("登录已过期，请重新登录");
      }
    } else {
      // 如果没有 response，通常是网络中断或 CORS 跨域拦截
      alert("网络连接异常或服务器无法访问 (CORS)");
    }

    return Promise.reject(error);
  },
);

export default service;
