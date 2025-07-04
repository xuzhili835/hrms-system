import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      // 代理所有以 /api 开头的请求到后端服务器
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        ws: true,
        // 可选：重写路径，如果后端不需要/api前缀，可以去掉
        // rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  define: {
    // 设置环境变量默认值
    __VUE_PROD_DEVTOOLS__: false,
    __VUE_OPTIONS_API__: true
  }
})
