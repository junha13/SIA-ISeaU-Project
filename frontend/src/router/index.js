import { createRouter, createWebHistory } from 'vue-router'

// ✅ Layout
import AppLayout from '@/components/AppLayout.vue'

// ✅ Views (Root)
import MainPage from '@/views/MainPage.vue'

// ✅ Views (beach)
import BeachListPage from '@/views/beach/BeachListPage.vue'
import BeachDetailPage from '@/views/beach/BeachDetailPage.vue'

// ✅ Views (safeguide)
import SafetyGuidePage from '@/views/safeguide/SafetyGuidePage.vue'

// ✅ Views (Group)
import GroupListPage from '@/views/group/GroupListPage.vue'
import GroupMainPage from '@/views/group/GroupMainPage.vue'

// ✅ Views (SOS)
import SOSMainPage from '@/views/sos/SOSMainPage.vue'
import SOSSimpleReportPage from '@/views/sos/SOSSimpleReportPage.vue'
import JellyfishReportPage from '@/views/sos/JellyfishReportPage.vue'

// ✅ Views (MyInfo)
import MyPage from '@/views/myinfo/MyPage.vue' // 마이 페이지
import MyCommentPage from '@/views/myinfo/MyCommentPage.vue' // 내 댓글 목록

// ✅ Views (Auth) - 레이아웃 미적용
import LoginPage from '@/views/auth/LoginPage.vue'
import RegisterPage from '@/views/auth/RegisterPage.vue'
import FindIdPasswordPage from '@/views/auth/FindIdPasswordPage.vue'
import ResetPasswordPage from '@/views/auth/ResetPasswordPage.vue'


const routes = [
    // ----------------------------------------------------
    // 1. 레이아웃 제외 (인증 페이지)
    // ----------------------------------------------------
    {
        path: '/login',
        name: 'Login',
        component: LoginPage,
    },
    {
        path: '/register',
        name: 'Register',
        component: RegisterPage,
    },
    {
        path: '/find-account',
        name: 'FindAccount',
        component: FindIdPasswordPage,
    },
    {
        path: '/reset-password',
        name: 'ResetPassword',
        component: ResetPasswordPage,
    },

    // ----------------------------------------------------
    // 2. 레이아웃 적용 (메인 페이지)
    // ----------------------------------------------------
    {
        path: '/',
        component: AppLayout, // 레이아웃 적용
        children: [
            // 메인 및 기본 기능
            { path: '', name: 'Main', component: MainPage },
            { path: 'safety-guide', name: 'SafetyGuide', component: SafetyGuidePage },

            // 해수욕장
            { path: 'beach-list', name: 'BeachList', component: BeachListPage },
            { path: 'beach/:beachNumber', name: 'beachDetail', component: BeachDetailPage},
            // 그룹
            { path: 'group', name: 'GroupList', component: GroupListPage },
            { path: 'group/:id', name: 'GroupMain', component: GroupMainPage },

            // SOS
            { path: 'sos', name: 'SOSMain', component: SOSMainPage },
            { path: 'sos/simple-report', name: 'SOSSimpleReport', component: SOSSimpleReportPage },
            { path: 'sos/jellyfish-report', name: 'JellyfishReport', component: JellyfishReportPage },

            // 마이페이지 (MyPage와 MyCommentPage가 MyInfo 도메인에 포함됨)
            { path: 'my-info', name: 'MyInfo', component: MyPage },
            { path: 'my-info/comments', name: 'MyComment', component: MyCommentPage },
        ],
    },
]

export default createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes,
})