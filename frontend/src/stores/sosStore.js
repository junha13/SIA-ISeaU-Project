// src/stores/sosStore.js
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { sosApi } from '@/api/sos'; // sosApi 임포트

export const useSOSStore = defineStore('sos', () => {
    // --- State ---
    const isLoading = ref(false); // 119/122 신고 시 로딩 상태
    const reportData = ref({ // 해파리 제보 폼 데이터
        location: '',
        phone: '',
        description: '',
        imageFile: null,
    });

    // --- Actions ---

    const setLoading = (status) => {
        isLoading.value = status;
    };

    const updateReportData = (key, value) => {
        reportData.value[key] = value;
    };

    /**
     * 해파리 제보 액션: API 호출 및 데이터 초기화
     */
    const submitJellyfishReport = async () => {
        try {
            // API 호출 (FormData로 변환 필요)
            // const formData = new FormData();
            // Object.keys(reportData.value).forEach(key => {
            //     formData.append(key, reportData.value[key]);
            // });
            // await sosApi.submitJellyfishReport(formData);

            // 초기화
            reportData.value = { location: '', phone: '', description: '', imageFile: null };
            return true;
        } catch (e) {
            throw new Error(e.response?.data?.message || '제보 전송에 실패했습니다.');
        }
    };

    /**
     * 긴급 신고 로깅 액션: API 호출
     */
    const logEmergencyCall = async (type, situation = null) => {
        try {
            // await sosApi.logEmergencyCall({ type, situation });
            return true;
        } catch (e) {
            console.error('긴급 신고 로깅 실패:', e);
            return false;
        }
    };


    // --- Getters ---
    const getReportData = computed(() => reportData.value);

    return {
        isLoading,
        reportData,
        setLoading,
        updateReportData,
        submitJellyfishReport,
        logEmergencyCall,
        getReportData,
    };
});