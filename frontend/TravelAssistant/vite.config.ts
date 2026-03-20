import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import path from "path";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],

  resolve: {
    alias: {
      "@": path.resolve(__dirname, "src")
    }
  },

  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8018",
        changeOrigin: true,
        rewrite: (pathStr) => pathStr.replace(/^\/api/, ""),
        // 必须加上这个，防止代理服务器缓存数据
  configure: (proxy) => {
    proxy.on('proxyRes', (proxyRes) => {
      // 确保后端返回的 Content-Type 是 text/event-stream
      // 并强制不缓存
      proxyRes.headers['content-type'] = 'text/event-stream;charset=UTF-8';
      proxyRes.headers['cache-control'] = 'no-cache';
      proxyRes.headers['connection'] = 'keep-alive';
    });
  }
      }
    }
  }
});