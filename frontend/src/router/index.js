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
import GroupMainPage from '@/views/group/GroupMainPage.vue'
import FirebaseTest from '@/views/group/Firebase.vue'

// ✅ Views (SOS)
import SOSMainPage from '@/views/sos/SOSMainPage.vue'
import FirstAidPage from '@/views/sos/FirstAidPage.vue'
import JellyfishReportPage from '@/views/sos/JellyfishReportPage.vue'

// ✅ Views (MyInfo)
import MyPage from '@/views/myinfo/MyPage.vue' // 마이 페이지
import MyCommentPage from '@/views/myinfo/MyCommentPage.vue' // 내 댓글 목록

// ✅ Views (Auth) - 레이아웃 미적용
import LoginPage from '@/views/auth/LoginPage.vue'
import RegisterPage from '@/views/auth/RegisterPage.vue'
import FindIdPasswordPage from '@/views/auth/FindIdPasswordPage.vue'
import ResetPasswordPage from '@/views/auth/ResetPasswordPage.vue'

// ✅ Views (Post)
import PostDetail from '@/views/post/PostDetail.vue'
import PostList from '@/views/post/PostList.vue'
import PostWrite from '@/views/post/PostWrite.vue'

const routes = [
    // ----------------------------------------------------
    // 1. 레이아웃 제외 (인증 페이지)
    // ----------------------------------------------------
    {
        path: '/login',
        name: 'Login',
        component: LoginPage,
        meta: {
            enter: 'animate__animated animate__fadeInUp',
            leave: 'animate__animated animate__fadeOutDown',
        },
    },
    {
        path: '/register',
        name: 'Register',
        component: RegisterPage,
        meta: {
            enter: 'animate__animated animate__fadeInUp',
            leave: 'animate__animated animate__fadeOutDown',
        },
    },
    {
        path: '/find-account',
        name: 'FindAccount',
        component: FindIdPasswordPage,
        meta: {
            enter: 'animate__animated animate__fadeInUp',
            leave: 'animate__animated animate__fadeOutDown',
        },
    },
    {
        path: '/reset-password',
        name: 'ResetPassword',
        component: ResetPasswordPage,
        meta: {
            enter: 'animate__animated animate__fadeInUp',
            leave: 'animate__animated animate__fadeOutDown',
        },
    },

    // ----------------------------------------------------
    // 2. 레이아웃 적용 (메인 페이지)
    // ----------------------------------------------------
    {
        path: '/',
        component: AppLayout, // 레이아웃 적용
        children: [
            // 메인 및 기본 기능
            { path: '', name: 'Main', component: MainPage,
                meta: {
                    enter: 'animate__animated animate__fadeIn',
                    leave: 'animate__animated animate__fadeOut',
                }
            },
            { path: 'safety-guide', name: 'SafetyGuide', component: SafetyGuidePage,
                meta: {
                    enter: 'animate__animated animate__fadeIn',
                    leave: 'animate__animated animate__fadeOut',
                }
             },

            // 해수욕장
            { path: 'beach-list', name: 'BeachList', component: BeachListPage,
                meta: {
                    enter: 'animate__animated animate__fadeIn',
                    leave: 'animate__animated animate__fadeOut',
                } },
            { path: 'beach/:beachNumber', name: 'beachDetail', component: BeachDetailPage,
                meta: {
                    enter: 'animate__animated animate__fadeIn',
                    leave: 'animate__animated animate__fadeOut',
                }},
            // 그룹
            { path: 'group', name: 'GroupMain', component: GroupMainPage },
            { path: 'firebase', name: 'Firebase', component: FirebaseTest },

            // SOS
            { path: 'sos', name: 'SOSMain', component: SOSMainPage },
            { path: 'sos/first-aid/cases', name: 'FirstAid', component: FirstAidPage },
            { path: 'sos/jellyfish-report', name: 'JellyfishReport', component: JellyfishReportPage },

            // 마이페이지 (MyPage와 MyCommentPage가 MyInfo 도메인에 포함됨)
            { path: 'my-info', name: 'MyInfo', component: MyPage },
            { path: 'my-info/comments', name: 'MyComment', component: MyCommentPage },

            // post 
            { path: 'post/:postNumber', name: 'PostDetail', component: PostDetail },
            { path: 'post/list', name: 'PostList', component: PostList },
            { path: 'post/write', name: 'PostWrite', component: PostWrite },
        ],
    },
]

export default createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes,
})