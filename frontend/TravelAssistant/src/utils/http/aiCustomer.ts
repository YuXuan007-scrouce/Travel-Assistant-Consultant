import axios from "axios";

export const chatService = axios.create({
  baseURL: import.meta.env.PROD
    ? import.meta.env.VITE_CHAT_BASE_URL
    : "/api",
  timeout: 30000,
  responseType: "text"
});

chatService.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 Token，注意这里的 Key 要和老项目一致（比如 'token' 或 'authorization'）
    const token = localStorage.getItem("authorization"); 
    
    if (token) {
      // 这里的 Header Key 也必须和后端 LoginInterceptor 检查的 Key 一致
      config.headers["authorization"] = token; 
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);
const chatHttp = {
  get(url: string, params?: any, config?: any) {
    return chatService.get(url, { params, ...config });
  },

  post(url: string, data?: any, config?: any) {
    return chatService.post(url, data, config);
  }
};

export default chatHttp;
