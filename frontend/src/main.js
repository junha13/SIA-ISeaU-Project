import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// 🎨 스타일
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

// Metronic (선택적 JS 추가 가능)
//import '@/assets/css/metronic-custom.css' // 나중에 커스터마이징용
// import '@/assets/js/metronic-init.js'  // 필요 시 메트로닉 기능 활성화

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.mount('#app')
