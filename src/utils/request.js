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

    // 2. 统一处理 Result<T> 包装
    // 后端统一返回 { code, msg, data }
    if (res.code !== undefined) {
      if (res.code === 200) {
        return res.data;
      } else {
        alert(res.msg || "业务处理失败");
        return Promise.reject(new Error(res.msg || "Error"));
      }
    }

    // 3. 兼容没有包装的情况 (如果有的话)
    return res;
  },
  (error) => {
    console.error("请求发生错误:", error);
    if (error.response) {
      const status = error.response.status;
      const res = error.response.data;
      
      // 如果后端在异常处理器中返回了 Result 包装的错误
      if (res && res.msg) {
        alert(res.msg);
      } else {
        if (status === 500) {
          alert("服务器内部错误 (500)");
        } else if (status === 403) {
          alert("权限不足，拒绝访问");
        } else if (status === 401) {
          alert("登录已过期，请重新登录");
        }
      }
    } else {
      alert("网络连接异常或服务器无法访问");
    }
    return Promise.reject(error);
  },
);

export default service;
