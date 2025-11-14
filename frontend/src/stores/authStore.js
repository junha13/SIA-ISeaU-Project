import { defineStore } from 'pinia';
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirmModal } from '@/utils/modalUtils';

export const useAuthStore = defineStore('auth', () => {
    const { showConfirmModal } = useConfirmModal();
    const router = useRouter();

    // --- State ---
    const isAuthenticated = ref(false);
    // userInfo를 reactive 객체로 선언하여 템플릿/컴포넌트에서 일관되게 사용합니다.
    const userInfo = reactive({
        userNumber: null,
        id: null,
        userName: null,
        mobile: null,
        settings: {
            alertLevel1: true,
            alertLevel2: true,
            alertLevel3: false,
            missingAlert: true,
            tideAlert: false,
        }
    });

    // --- 세션(브라우저 저장소) 유지용 헬퍼들 ---
    // SESSION_KEY: sessionStorage/localStorage에 저장할 때 사용하는 키
    // const SESSION_KEY = 'auth';

    // 토큰 기반 동작만 사용합니다. 세션(localStorage.auth) 관련 로직은 만들지 않습니다.
    // 기존 호출부와의 호환을 위해 no-op 함수들을 제공합니다.
    const SESSION_KEY = 'auth';

    // saveAuthLocal: 이전 'saveToSession' 대신 사용하는 이름입니다 (no-op).
    const saveAuthLocal = () => {
        // intentionally no-op to avoid creating session-like storage
    };

    /**
     * saveToSession
     * - 현재 Pinia 상태를 직렬화하여 sessionStorage에 저장합니다.
     * - 개발/디버깅 목적이며, 민감한 토큰은 보안 정책에 따라 따로 관리하세요.
     */
    // const saveToSession = () => {
    //     try {
    //         const payload = {
    //             isAuthenticated: isAuthenticated.value,
    //             // reactive 객체이므로 그대로 직렬화 가능
    //             userInfo: userInfo,
    //         };
    //         sessionStorage.setItem(SESSION_KEY, JSON.stringify(payload));
    //     } catch (e) {
    //         console.error('Failed to save auth to sessionStorage', e);
    //     }
    // };

    /**
     * loadFromSession
     * - 스토어가 생성될 때 sessionStorage에서 저장된 auth 정보를 읽어와
     *   Pinia 상태를 복원합니다. (새로고침/탭 유지 목적)
     */
    // const loadFromSession = () => {
    //     try {
    //         // 우선 sessionStorage를 확인하고, 없으면 localStorage에 저장된 'remember me' 정보를 확인합니다.
    //         let raw = sessionStorage.getItem(SESSION_KEY);
    //         if (!raw) {
    //             raw = localStorage.getItem(SESSION_KEY);
    //         }
    //         if (!raw) return;
    //         const parsed = JSON.parse(raw);
    //         if (parsed?.isAuthenticated) {
    //             isAuthenticated.value = true;
    //             // reactive 객체에 저장된 값들을 병합하여 반응성 유지
    //             Object.assign(userInfo, parsed.userInfo || {});
    //         }
    //     } catch (e) {
    //         console.error('Failed to load auth from sessionStorage', e);
    //     }
    // };

    // 실제 구현: localStorage의 auth 또는 token으로부터 상태 복원
    const decodeJwt = (token) => {
        try {
            const payload = token.split('.')[1];
            let b64 = payload.replace(/-/g, '+').replace(/_/g, '/');
            while (b64.length % 4) b64 += '=';
            const json = decodeURIComponent(atob(b64).split('').map(c => {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));
            return JSON.parse(json);
        } catch (e) {
            return null;
        }
    };

    // restoreFromToken: 오직 token 기반으로만 복원합니다 (localStorage.token).
    const restoreFromToken = () => {
        try {
            const token = localStorage.getItem('token');
            if (!token) return;
            const claims = decodeJwt(token);
            if (!claims) return;
            isAuthenticated.value = true;
            userInfo.userNumber = claims.user_number || claims.userNumber || userInfo.userNumber;
            userInfo.id = claims.id || claims.sub || userInfo.id;
            userInfo.userName = claims.user_name || claims.name || userInfo.userName;
            userInfo.mobile = claims.mobile || userInfo.mobile;
        } catch (e) {
            console.error('Failed to load auth from token', e);
        }
    };

    /**
     * clearSession
     * - 로그아웃 시 세션 저장소에서 auth 정보를 제거합니다.
     */
    // const clearSession = () => {
    //     try {
    //         // sessionStorage와 localStorage 둘 다 정리
    //         sessionStorage.removeItem(SESSION_KEY);
    //         localStorage.removeItem(SESSION_KEY);
    //     } catch (e) {
    //         console.error('Failed to clear auth session', e);
    //     }
    // };

    // clearAuth: 토큰만 제거합니다.
    const clearAuth = () => {
        try {
            try { localStorage.removeItem('token'); } catch (_) { }
        } catch (e) {
            // ignore
        }
    };

    // 스토어 생성 시 세션에서 가능한 경우 상태 복원
    // loadFromSession();

    // 스토어 생성 시 토큰 기반으로 상태 복원
    try { restoreFromToken(); } catch (e) { /* ignore */ }

    // --- Actions ---

    /**
     * 로그아웃: 상태 초기화 및 로그인 페이지 이동
     */
    const logout = async () => {
        isAuthenticated.value = false;
        // 기본값으로 초기화하되 reactive 객체를 재할당하지 않고 값만 덮어씌움
        Object.assign(userInfo, {
            userNumber: null,
            id: null,
            userName: null,
            mobile: null,
            settings: {
                alertLevel1: ref(true),
                alertLevel2: ref(true),
                alertLevel3: ref(false),
                missingAlert: ref(true),
                tideAlert: ref(false),
            }
        });

        // 토큰 기반 로그아웃: 로컬 토큰 정리 후 라우팅
        try { localStorage.removeItem('token'); } catch (_) {}

        router.push({ name: 'Login' });
    };

    // 로그아웃 시 상태 초기화와 세션 삭제를 함께 수행하는 함수
    // - 기존 logout() 함수로 상태를 재설정하고
    // - 그 이후 세션 저장소에서 auth 정보를 제거합니다.
    const logoutAndClear = async () => {
        await logout();
        try { clearAuth(); } catch (_) {}
    };

    /**
     * 설정 업데이트
     */
    // 사용자 설정 변경 시 Pinia 상태에 병합하고 세션에 저장
    // updateSettings: 토큰/서버와 관련된 저장은 하지 않습니다. 로컬 상태만 갱신.
    const updateSettings = (newSettings) => {
        Object.assign(userInfo.settings, newSettings);
    };

    return {
        isAuthenticated,
        userInfo,
        logout: logoutAndClear,
        updateSettings,
        // expose token/auth helpers for explicit control if needed
        saveAuthLocal,
        restoreFromToken,
        clearAuth,
    };
});