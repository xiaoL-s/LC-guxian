import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    plugins: [
      vue()
    ],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    server: {
      port: 5173,
      open: true,
      host: '0.0.0.0',
      // 代理配置：API统一走 /api 前缀，页面路由（/system/...）不会被代理，避免刷新时页面导航被转发到后端
      proxy: {
        // 客户模块（8082）必须放在 /api 之前，前缀更长先匹配
        '/api/customer': {
          target: 'http://127.0.0.1:8082',
          changeOrigin: true,
          // 去掉 /api/customer 前缀再转发，后端接口路径保持 /customer/xxx 不变
          rewrite: path => path.replace(/^\/api\/customer/, '/customer')
        },
        // 销售订单模块（8083）
        '/api/sales': {
          target: 'http://127.0.0.1:8083',
          changeOrigin: true,
          rewrite: path => path.replace(/^\/api\/sales/, '/sales')
        },
        // 库存物料模块（8084）
        '/api/stock': {
          target: 'http://127.0.0.1:8084',
          changeOrigin: true,
          rewrite: path => path.replace(/^\/api\/stock/, '/stock')
        },
        // 生产管理模块（8085）
        '/api/production': {
          target: 'http://127.0.0.1:8085',
          changeOrigin: true,
          rewrite: path => path.replace(/^\/api\/production/, '/production')
        },
        '/api': {
          target: 'http://127.0.0.1:8081',
          changeOrigin: true,
          // 去掉 /api 前缀再转发，后端接口路径保持 /system/xxx 不变
          rewrite: path => path.replace(/^\/api/, '')
        }
      }
    },
    build: {
      outDir: 'dist',
      chunkSizeWarningLimit: 1500
    }
  }
})
