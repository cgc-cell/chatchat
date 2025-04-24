import { resolve } from 'path'
import { defineConfig, externalizeDepsPlugin } from 'electron-vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  css: {
    preprocessorOptions: {
      sass: {
        api: 'modern-compiler',
      }
    }
  },
  logLevel: 'error',
  main: {
    plugins: [externalizeDepsPlugin()]
  },
  preload: {
    plugins: [externalizeDepsPlugin()]
  },
  renderer: {
    resolve: {
      alias: {
        '@': resolve('src/renderer/src')
      }
    },
    plugins: [vue()],
    server: {
      hmr: true,
      port: 5000,
      proxy: {
        '/api': {
          target: 'http://localhost:5050',
          changeOrigin: true, // needed for virtual hosted sites
          pathRewrite: { '^/api': '/api' },
          secure: false
        }
      }
    }
  }
})
