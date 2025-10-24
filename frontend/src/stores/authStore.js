import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirmModal } from '@/utils/modalUtils';
import { authApi } from '@/api/auth'; // authApi 임포트

export const useAuthStore = defineStore('auth', () => {
    const { showConfirmModal } = useConfirmModal();
    const router = useRouter();

    // --- State ---
    
    const isAuthenticated = ref(false); // 인증 상태(로그인 성공 여부)
    const userInfo = ref({
        id: null,
        name: '김바다',
        phone: '010-1234-5678',
        settings: {
            alertLevel1: true,
            alertLevel2: true,
            alertLevel3: false,
            missingAlert: true,
            tideAlert: false,
        }
    });

    // --- Actions ---

    /**
     * 로그인 액션: API 호출 및 상태 업데이트
     * @param {string} id - 사용자 아이디
     * @param {string} password - 사용자 비밀번호
     * @return {boolean} 로그인 성공 여부
     * @throws {Error} 로그인 실패 시 에러 발생
     */
    const login = async (id, password) => {
        try {
            // API 호출
            const res = await authApi.login({ id, password });

            // API 호출 결과
            const user = res?.data;
            if (!user) throw new Error('로그인 API 실행 결과가 비어있습니다.');
            
            // 상태 및 정보 업데이트
            isAuthenticated.value = true;
            userInfo.value.userNumber = user.userNumber;  // 카멜케이스로 변환됨
            userInfo.value.id = user.id;
            userInfo.value.name = user.userName;          // 백엔드는 userName으로 반환

            return true;
        } catch (e) {
            console.error('로그인 실패:', e);
            isAuthenticated.value = false;
            throw new Error(e.response?.data?.message || '아이디 또는 비밀번호가 일치하지 않습니다.');
        }
    };
    

    /**
     * 회원가입 액션: API 호출
     */
    const register = async (userData) => {
        try {
            await authApi.register(userData);
            return true;
        } catch (e) {
            throw new Error(e.response?.data?.message || '회원가입에 실패했습니다.');
        }
    };

    /**
     * 로그아웃 액션: API 호출 및 상태 초기화
     */
    const logout = async () => {
        try {
            // await authApi.logout();
        } catch (e) {
            console.error('로그아웃 API 호출 실패:', e);
        }

        isAuthenticated.value = false;

        await showConfirmModal({
            title: '완료',
            message: '성공적으로 로그아웃되었습니다.',
            type: 'success',
            autoHide: true,
            duration: 1000,
        });

        // 로그인 페이지로 이동
        router.push({ name: 'Login' });
    };

    /**
     * 사용자 설정 업데이트 액션: API 호출
     */
    const updateSettings = async (newSettings) => {
        try {
            // await authApi.updateSettings(newSettings);

            // 성공 시 로컬 상태 업데이트
            Object.assign(userInfo.value.settings, newSettings);
        } catch (e) {
            console.error('설정 업데이트 실패:', e);
            throw new Error('설정 업데이트에 실패했습니다.');
        }
    };

    // --- Getters ---
    const getUserInfo = computed(() => userInfo.value);

    return {
        isAuthenticated,
        userInfo,
        login,
        register,
        logout,
        updateSettings,
        getUserInfo,
    };
});