import { createRouter, createWebHistory } from 'vue-router'

// ✅ Layout
import AppLayout from '@/components/AppLayout.vue'

// ✅ Views
import MainPage from '@/views/MainPage.vue'
import BeachListPage from '@/views/BeachListPage.vue'
import BeachDetailPage from '@/views/beach/BeachDetailPage.vue'


// import Login from '@/views/auth/LoginPage.vue'
// import Register from '@/views/auth/RegisterPage.vue'

const routes = [
    {
        path: '/',
        component: AppLayout, // 레이아웃 적용
        children: [
            { path: '', name: 'Main', component: MainPage },
            { path: 'beach-list', name: 'BeachList', component: BeachListPage },
            { path: 'beach/:id', name: 'BeachDetail', component: BeachDetailPage },
        ],
    },
    // {
    //     path: '/login',
    //     name: 'Login',
    //     component: Login,
    //     meta: { layout: false }, // 레이아웃 제외
    // },
    // {
    //     path: '/register',
    //     name: 'Register',
    //     component: Register,
    //     meta: { layout: false }, // 레이아웃 제외
    // },
]

export default createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes,
})
