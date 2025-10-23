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
  optimizeDeps: {
    include: ['quill', 'vue3-naver-maps'],
    // ⬅️ 핵심: quill을 사전 번들
    // UMD → ESM 변환을 사전에 시켜서 “default export 없음” 문제를 Vite가 자동으로 보정해준 거예요.
  },
  
})
