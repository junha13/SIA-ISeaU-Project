// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'

// === Views ===
import HomeView from '@/views/HomeView.vue'
import EmergencyView from '@/views/EmergencyView.vue'
import GroupLocationView from '@/views/GroupLocationView.vue'
import SafetyInfoView from '@/views/SafetyInfoView.vue'
import LoginView from '@/views/LoginView.vue'

const routes = [
    {
        path: '/',
        name: 'home',
        component: HomeView,
        meta: {
            title: 'I Sea U | 해양 안전 지도',
        },
    },
    {
        path: '/emergency',
        name: 'emergency',
        component: EmergencyView,
        meta: {
            title: '긴급 신고 - I Sea U',
        },
    },
    {
        path: '/group',
        name: 'group',
        component: GroupLocationView,
        meta: {
            title: '그룹 위치 공유 - I Sea U',
        },
    },
    {
        path: '/safety-info',
        name: 'safety-info',
        component: SafetyInfoView,
        meta: {
            title: '안전 수칙 안내 - I Sea U',
        },
    },
    {
        path: '/login',
        name: 'login',
        component: LoginView,
        meta: {
            title: '로그인 - I Sea U',
            guestOnly: true,
        },
    },
    {
        path: '/:catchAll(.*)',
        redirect: '/',
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior() {
        return { top: 0 }
    },
})

// === 페이지 타이틀 변경 ===
router.beforeEach((to, from, next) => {
    document.title = to.meta.title || 'I Sea U'
    next()
})

export default router
