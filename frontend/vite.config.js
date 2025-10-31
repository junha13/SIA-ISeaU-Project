import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import { VitePWA } from 'vite-plugin-pwa'
import path from 'path'
import { fileURLToPath, URL } from 'node:url'

// Spring Boot static 폴더 (frontend 기준 상대경로)
const springBootStatic = '../../backend/springboot/src/main/resources/static'

export default defineConfig({
    base: './',
    plugins: [
        vue(),
        vueDevTools(),
        VitePWA({
            strategies: 'injectManifest',
            srcDir: 'public',
            filename: 'firebase-messaging-sw.js',
            injectManifest: {
                maximumFileSizeToCacheInBytes: 5 * 1024 * 1024,
            },
            includeAssets: ['/iseau.ico', '/apple-touch-icon.png', '/masked-icon.svg'],
            manifest: {
                name: 'ISeaU PWA App',
                short_name: 'ISeaU PWA',
                theme_color: '#ffffff',
                icons: [
                    { src: 'pwa-192x192.png', sizes: '192x192', type: 'image/png', purpose: 'maskable' },
                    { src: 'pwa-512x512.png', sizes: '512x512', type: 'image/png' },
                ],
            },
            registerType: 'autoUpdate',
        }),
    ],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url)),
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
    },
    build: {
        outDir: springBootStatic, // 빌드 결과 Spring Boot static으로
        emptyOutDir: true,         // 기존 내용 삭제 후 빌드
    },
    define: {
        __API_BASE_URL__: JSON.stringify(process.env.VITE_API_BASE_URL)
    }
})
