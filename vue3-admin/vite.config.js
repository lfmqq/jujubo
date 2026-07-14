import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, __dirname, '')
  const proxyTarget = env.VITE_PROXY_TARGET || 'http://localhost:8080'

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: proxyTarget,
          changeOrigin: true
        },
        '/uploads': {
          target: proxyTarget,
          changeOrigin: true,
          rewrite: (path) => '/api' + path
        }
      }
    },
    build: {
      outDir: 'dist',
      assetsDir: 'assets',
      // 调高单包体积警告阈值（echarts / element-plus 等 vendor 本身较大）
      chunkSizeWarningLimit: 2000,
      // Vite 8 使用 rolldown，分包配置需写在 rolldownOptions.output 下
      rolldownOptions: {
        output: {
          // 按依赖拆包，避免单文件过大、提升缓存命中率
          manualChunks(id) {
            if (!id.includes('node_modules')) return
            if (id.includes('echarts')) return 'echarts'
            if (id.includes('element-plus') || id.includes('@element-plus')) return 'element-plus'
            if (id.includes('@vueuse')) return 'vueuse'
            if (id.includes('vue') || id.includes('pinia') || id.includes('vue-router') || id.includes('axios')) {
              return 'vue-vendor'
            }
            return 'vendor'
          }
        },
        // 抑制第三方库 @vueuse/core 的 PURE 注解位置警告（无害）
        onwarn(warning, warn) {
          if (warning.code === 'INVALID_ANNOTATION') return
          warn(warning)
        }
      }
    }
  }
})