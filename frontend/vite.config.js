import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import { VitePWA } from 'vite-plugin-pwa'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    VitePWA({
          strategies: 'injectManifest',

          // 🚨 추가: Service Worker 소스 파일 지정
          srcDir: 'public',
          filename: 'firebase-messaging-sw.js',

          includeAssets: ['favicon.ico', 'apple-touch-icon.png', 'masked-icon.svg'],
          manifest: {
              name: 'ISeaU PWA App',
              short_name: 'ISeaU PWA',
              theme_color: '#ffffff',
              icons: [
                  {
                      src: 'pwa-192x192.png',
                      sizes: '192x192',
                      type: 'image/png',
                      // 📌 icons의 purpose 속성 설정: maskable 아이콘을 반드시 포함해야 합니다.
                      purpose: 'maskable'
                  },
                  {
                      src: 'pwa-512x512.png',
                      sizes: '512x512',
                      type: 'image/png'
                  }
              ]

          },
          workbox: {
              // 개발 환경에서 로그를 줄이는 데 도움을 줍니다.
              // 이 옵션만으로 알림창이 사라지지 않을 수 있습니다.
              // skipWaiting: true,
              // clientsClaim: true
          },

          // 🚨 [핵심] SW 등록 방식 설정 (개발 시 알림을 최소화)
          registerType: 'autoUpdate',
          // 💡 개발 중에는 SW 업데이트 시 사용자에게 팝업을 띄우지 않도록 설정하는 것이 편리합니다.
      })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  optimizeDeps: {
    include: ['quill', 'vue3-naver-maps'],
  },
    server: {
        port: 5173,
        host: true,        // 외부에서도 접근 가능하게
        allowedHosts: [
            'hellokiyo.ngrok.io'   // ngrok 도메인 허용
        ],
        proxy: {
            // Vue에서 '/api/'로 시작하는 모든 요청을 Spring Boot 서버 (8080)로 전달
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true
            }
        }
    }
  
})
