import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173, //前端项目启动端口
    proxy: {
      '/api': {
        target: 'http://localhost:8080', //后端的地址
        changeOrigin: true, // 开启跨域
        // 重写路径：把前端请求的 '/api/auth/login' 转换成后端的 '/auth/login'
        rewrite: (path) => path.replace(/^\/api/, '') 
      }
    }
  }
})