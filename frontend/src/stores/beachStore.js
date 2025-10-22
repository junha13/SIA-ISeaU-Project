import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { beachApi } from '@/api/beach';
import { useConfirmModal } from '@/utils/modalUtils';
// [추가] useApi 유틸리티를 직접 import 합니다.
import { useApi } from '@/utils/useApi.js';

export const useBeachStore = defineStore('beach', () => {
    // --- State ---
    const beaches = ref([]); // [추가] 해수욕장 '목록' 상태
    const selectedBeachId = ref(null);
    const favoriteBeachIds = ref([]);
    const isLoading = ref(false); // 목록 로딩 상태
    const apiError = ref(null);
    const currentBeachDetail = ref(null);
    const isDetailLoading = ref(false); // 상세 정보 로딩 상태

    // --- Actions ---

    /**
     * [추가] 전체 해수욕장 목록을 가져오는 Action
     */
    const fetchBeaches = async (params = {}) => {
        isLoading.value = true;
        apiError.value = null;
        try {
            const result = await beachApi.fetchBeachList(params);
            // 백엔드 응답이 result.result 형태일 수 있으므로 안전하게 접근
            beaches.value = result?.result || [];
        } catch (error) {
            console.error('해수욕장 목록 조회 실패:', error);
            apiError.value = error;
            beaches.value = [];
        } finally {
            isLoading.value = false;
        }
    };

    /**
     * [수정] 특정 해수욕장의 상세 정보를 가져오는 Action
     */
    const fetchBeachDetail = async (beachNumber) => {
        isDetailLoading.value = true;
        currentBeachDetail.value = null;
        try {
            // 백엔드 컨트롤러와 정확히 일치하는 동적 URL 경로를 만듭니다.
            const url = `/api/beach/detail/${beachNumber}/info`;
            
            // useApi를 action 함수 안에서 직접 호출합니다.
            const { execute: callDetailApi } = useApi('get', url);

            // 생성된 함수를 호출하여 API 요청을 보냅니다.
            const result = await callDetailApi();
            
            // 백엔드 응답이 { data: { ... } } 형태이므로, result.data를 할당합니다.
            currentBeachDetail.value = result.data.result;

        } catch (error) {
            console.error('해수욕장 상세 정보 조회 실패:', error);
            apiError.value = error;
        } finally {
            isDetailLoading.value = false;
        }
    };

    /**
     * (기존 코드 유지) 해수욕장 선택/해제 토글
     */
    const toggleSelectBeach = (beachId, beachName) => {
        const { showConfirmModal } = useConfirmModal();
        let message = '';
        if (selectedBeachId.value === beachId) {
            selectedBeachId.value = null;
            message = `${beachName} 해수욕장의 선택이 해제되었습니다.`;
        } else {
            selectedBeachId.value = beachId;
            message = `${beachName} 해수욕장이 현재 활동 해수욕장으로 선택되었습니다.`;
        }
        showConfirmModal({
            title: '해수욕장 선택 변경',
            message: message,
            type: 'info',
            autoHide: true,
            duration: 1500
        });
    };

    /**
     * (기존 코드 유지) 즐겨찾기 토글
     */
    const toggleFavoriteBeach = (beachId) => {
        const { showConfirmModal } = useConfirmModal();
        const isFavorite = favoriteBeachIds.value.includes(beachId);
        if (isFavorite) {
            favoriteBeachIds.value = favoriteBeachIds.value.filter(id => id !== beachId);
            showConfirmModal({ title: '즐겨찾기 해제', message: '즐겨찾기에서 해제되었습니다.', type: 'info', autoHide: true, duration: 1000 });
        } else {
            favoriteBeachIds.value.push(beachId);
            showConfirmModal({ title: '즐겨찾기 등록', message: '즐겨찾기에 등록되었습니다.', type: 'success', autoHide: true, duration: 1000 });
        }
    };

    // --- Getters (기존 코드 유지) ---
    const getCurrentSelectedBeachId = computed(() => selectedBeachId.value);
    const getFavoriteBeachIds = computed(() => favoriteBeachIds.value);
    const getCurrentBeachDetail = computed(() => currentBeachDetail.value);

    // --- Return (수정한 내용 포함하여 반환) ---
    return {
        beaches,
        selectedBeachId,
        favoriteBeachIds,
        isLoading,
        apiError,
        currentBeachDetail,
        isDetailLoading,
        fetchBeaches,
        fetchBeachDetail,
        toggleSelectBeach,
        toggleFavoriteBeach,
        getCurrentSelectedBeachId,
        getFavoriteBeachIds,
        getCurrentBeachDetail,
    };
});

