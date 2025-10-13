import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

export default defineConfig({
    plugins: [vue(), vueDevTools()],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url)),
            'vue3-naver-maps': 'vue3-naver-maps/dist/vue3-naver-maps.es.js',
        },
    },
    server: {
        host: true,
        port: 5173,
        allowedHosts: ['hellokiyo.ngrok.io'],
        cors: true,
        proxy: {
            // ✅ 1. FastAPI (메인 API)
            '/api': {
                target: 'http://localhost:8000',
                changeOrigin: true,
                secure: false,
            },
            // ✅ 2. Flask 또는 Spring Boot (예시: /data-api로 시작)
            '/data-api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false,
                rewrite: (path) => path.replace(/^\/data-api/, '') // Spring/Flask에서 /data-api 제거
            }
            // Spring/Flask 백엔드의 실제 포트에 맞게 8080을 변경하세요.
        },
    },
})