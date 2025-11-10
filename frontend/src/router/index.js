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

// ----------------------------------------------------
// ✅ Views (Control) - 새로운 관제 시스템 컴포넌트 추가
// ----------------------------------------------------
// 관제 시스템 컴포넌트는 src/views/control 폴더에 있다고 가정합니다.
import ControlLayout from '@/views/control/ControlLayout.vue' // 저희가 만든 MainLayout
import CCTVMonitoring from '@/views/control/CCTVMonitoring.vue'
import ReportDetail from '@/views/control/ReportDetail.vue'

const routes = [
    // ----------------------------------------------------
    // 1. 레이아웃 제외 (인증 페이지)
    // ----------------------------------------------------
    {
        path: '/login',
        name: 'Login',
        component: LoginPage
    },
    {
        path: '/register',
        name: 'Register',
        component: RegisterPage
    },
    {
        path: '/find-account',
        name: 'FindAccount',
        component: FindIdPasswordPage
    },
    {
        path: '/reset-password',
        name: 'ResetPassword',
        component: ResetPasswordPage
    },
    // ----------------------------------------------------
    // 1-1. ✅ 관제 시스템 레이아웃 적용 (신규 추가)
    // ----------------------------------------------------
    {
        path: '/control', // 관제 시스템의 기본 경로
        component: ControlLayout, // 관제 전용 레이아웃 적용
        children: [
            // 기본 경로 진입 시 CCTV 관제 페이지로 리다이렉트
            { path: '', redirect: 'cctv' },

            // CCTV 관제 페이지
            {
                path: 'cctv',
                name: 'CCTVControl', // 이름 충돌을 피하기 위해 'Control' 접미사 추가
                component: CCTVMonitoring
            },

            // 신고 상세 페이지
            {
                path: 'report',
                name: 'ReportControl', // 이름 충돌을 피하기 위해 'Control' 접미사 추가
                component: ReportDetail
            },
        ],
    },
    // ----------------------------------------------------
    // 2. 레이아웃 적용 (메인 페이지)
    // ----------------------------------------------------
    {
        path: '/',
        component: AppLayout, // 레이아웃 적용
        children: [
            // 메인 및 기본 기능
            { path: '', name: 'Main', component: MainPage},
            { path: 'safety-guide', name: 'SafetyGuide', component: SafetyGuidePage},

            // 해수욕장
            { path: 'beach-list', name: 'BeachList', component: BeachListPage },
            { path: 'beach/:beachNumber', name: 'beachDetail', component: BeachDetailPage},
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