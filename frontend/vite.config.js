import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import { VitePWA } from 'vite-plugin-pwa'
import path from 'path'
import { fileURLToPath, URL } from 'node:url'

// Spring Boot static 폴더 (frontend 기준 상대경로)
const springBootStatic = path.resolve(__dirname, '../../backend/springboot/src/main/resources/static')

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        vue(),
        vueDevTools(),
        VitePWA({
            strategies: 'injectManifest',
            srcDir: 'public',
            filename: 'firebase-messaging-sw.js',
            injectManifest: {
                maximumFileSizeToCacheInBytes: 10 * 1024 * 1024,
            },
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
        host: true, // 외부에서 접속 가능 (개발용)
        proxy: {
            '/api': {
                target: 'http://172.168.10.15:8080', // 개발 시 로컬 Spring Boot
                changeOrigin: true,
                secure: false,
            },
        },
        // build: {
        //     outDir: springBootStatic, // 빌드 결과 Spring Boot static으로
        //     emptyOutDir: true,         // 기존 내용 삭제 후 빌드
        // },
        define: {
            __API_BASE_URL__: JSON.stringify(process.env.VITE_API_BASE_URL)
        }
    }
})