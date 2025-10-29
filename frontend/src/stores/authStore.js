import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirmModal } from '@/utils/modalUtils';

export const useAuthStore = defineStore('auth', () => {
    const { showConfirmModal } = useConfirmModal();
    const router = useRouter();

    // --- State ---
    const isAuthenticated = ref(false);
    const userInfo = ref({
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

    // --- Actions ---

    /**
     * 로그아웃: 상태 초기화 및 로그인 페이지 이동
     */
    const logout = async () => {
        isAuthenticated.value = false;
        userInfo.value = {
            userNumber: ref(),
            id: ref(''),
            userName: ref(''),
            mobile: ref(''),
            settings: {
                alertLevel1: ref(true),
                alertLevel2: ref(true),
                alertLevel3: ref(false),
                missingAlert: ref(true),
                tideAlert: ref(false),
            }
        };

        await showConfirmModal({
            title: '완료',
            message: '성공적으로 로그아웃되었습니다.',
            type: 'success',
            autoHide: true,
            duration: 1000,
        });

        router.push({ name: 'Login' });
    };

    /**
     * 설정 업데이트
     */
    const updateSettings = (newSettings) => {
        Object.assign(userInfo.value.settings, newSettings);
    };

    return {
        isAuthenticated,
        userInfo,
        logout,
        updateSettings,
    };
});