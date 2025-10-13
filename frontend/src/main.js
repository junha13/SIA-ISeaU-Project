// src/main.js
import { createApp } from 'vue'
import { createPinia } from 'pinia'
// ✅ Firebase 임포트 및 설정 임포트
import { initializeApp } from 'firebase/app'
import { getAuth } from 'firebase/auth'

import App from './App.vue'
import router from './router'

// 1. Firebase 환경 설정 (Vite 환경 변수 사용)
// .env 파일에 VITE_FIREBASE_API_KEY 등을 정의해야 합니다.
const firebaseConfig = {
    apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
    authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
    projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
    storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
    messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
    appId: import.meta.env.VITE_FIREBASE_APP_ID,
    measurementId: import.meta.env.VITE_FIREBASE_MEASUREMENT_ID,
};

// 2. Firebase 앱 초기화
const firebaseApp = initializeApp(firebaseConfig)
const firebaseAuth = getAuth(firebaseApp) // 인증 서비스 초기화

// 🎨 스타일
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// ✅ Firebase Auth 객체를 Vue의 전역 속성으로 추가 (필요시)
app.config.globalProperties.$firebaseAuth = firebaseAuth

app.mount('#app')