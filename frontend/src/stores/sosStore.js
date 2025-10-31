// src/stores/sosStore.js
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { sosApi } from '@/api/sos'; // sosApi 임포트

export const useSOSStore = defineStore('sos', () => {
    // --- State ---
    const isLoading = ref(false); // 119/122 신고 시 로딩 상태
    const reportData = ref({ // 해파리 제보 폼 데이터
        lat: '',
        lng: '',
        //location: '',
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
        // 1) 기본 검증
        if (!reportData.value.lat || !reportData.value.lng) {
        throw new Error('위도/경도는 필수입니다.');
        }

        // 숫자 안정화 (백엔드 Double 파싱 오차 최소화)
        const latNum = Number(reportData.value.lat);
        const lngNum = Number(reportData.value.lng);
        if (Number.isNaN(latNum) || Number.isNaN(lngNum)) {
        throw new Error('위도/경도가 숫자가 아닙니다.');
        }

        // 2) location 문자열 동기화(옵션: DB에 location 컬럼이 있을 때만 의미 있음)
        const locStr = `${latNum},${lngNum}`;
        reportData.value.location = locStr;

        // 3) FormData 구성 (백엔드 키에 맞춰 안전하게 동시 전송)
        const fd = new FormData();
        // 좌표(분리)
        fd.append('lat', String(latNum));
        fd.append('lon', String(lngNum));

        /* 좌표(문자열; DB에 location 컬럼이 있으면 유용)
        fd.append('location', locStr);
        */

        // 전화/설명 (네이밍 혼재 대비)
        if (reportData.value.phone) fd.append('mobile', reportData.value.phone); 
        if (reportData.value.description) fd.append('details', reportData.value.description); 
        
        // 파일
        if (reportData.value.imageFile) fd.append('imageFile', reportData.value.imageFile); 
        
        // 선택: 기본 승인 상태
        fd.append('adminApproval', 'N');

        try {
        setLoading(true);
        await sosApi.submitJellyfishReport(fd); // POST /api/sos/jellyfish/report (multipart/form-data)
        // 성공 시 초기화
        reportData.value = {
            lat: '',
            lng: '',
            //location: '',
            phone: '',
            description: '',
            imageFile: null,
        };
        return true;
        } catch (e) {
        throw new Error(e?.response?.data?.message || '제보 전송에 실패했습니다.');
        } finally {
        setLoading(false);
        }
    };

    /*
      긴급 신고 로깅 액션: API 호출
     
    const logEmergencyCall = async (type, situation = null) => {
        try {
            // await sosApi.logEmergencyCall({ type, situation });
            return true;
        } catch (e) {
            console.error('긴급 신고 로깅 실패:', e);
            return false;
        }
    };
    */

    // --- Getters ---
    const getReportData = computed(() => reportData.value);

    return {
        isLoading,
        reportData,
        setLoading,
        updateReportData,
        submitJellyfishReport,
        //logEmergencyCall,
        getReportData,
    };
});