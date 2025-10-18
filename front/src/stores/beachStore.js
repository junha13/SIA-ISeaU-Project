// src/stores/beachStore.js

import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { beachApi } from '@/api/beach';
import { useConfirmModal } from '@/utils/modalUtils';

export const useBeachStore = defineStore('beach', () => {
    // --- State ---
    const selectedBeachId = ref(null);
    const favoriteBeachIds = ref([]);
    const isLoading = ref(false);
    const apiError = ref(null);
    // 현재 상세 정보를 보는 해수욕장 데이터
    const currentBeachDetail = ref(null);
    const isDetailLoading = ref(false);

    // --- Actions ---

    /**
     * 특정 해수욕장의 상세 정보를 가져옵니다. (날씨, 위험 정보 포함)
     * @param {number} beachId - 해수욕장 ID
     */
    const fetchBeachDetail = async (beachId) => {
        isDetailLoading.value = true;
        try {
            // const result = await beachApi.fetchBeachDetail({ id: beachId });

            // Dummy Data (상세 정보)
            const result = {
                id: beachId,
                name: '속초 해수욕장',
                description: '맑고 깨끗한 바다와 부드러운 모래사장으로 유명합니다.',
                weather: { temp: 24, wind: 3.2, visibility: 10, humidity: 65, tide: 1.2 },
                danger: { rip: '관심', jelly: '주의', wave: 1.8 },
                reviews: [{ user: '익명사용자', date: '2025.01.15', content: '정말 깨끗하고 아름다운 해수욕장이에요.' }],
            };

            currentBeachDetail.value = result;
        } catch (error) {
            console.error('해수욕장 상세 정보 조회 실패:', error);
            apiError.value = error;
            currentBeachDetail.value = null;
        } finally {
            isDetailLoading.value = false;
        }
    };

    /**
     * 해수욕장 선택/해제 토글 및 DB 저장 (선택은 하나만 가능)
     */
    const toggleSelectBeach = async (beachId, beachName) => {
        const { showConfirmModal } = useConfirmModal();

        try {
            isLoading.value = true;

            let newSelectedId = null;
            let message = '';

            if (selectedBeachId.value === beachId) {
                newSelectedId = null;
                // await beachApi.unselectBeach();
                message = `${beachName} 해수욕장의 선택이 해제되었습니다.`;
            } else {
                newSelectedId = beachId;
                // const result = await beachApi.selectBeach(beachId);
                message = `${beachName} 해수욕장이 현재 활동 해수욕장으로 선택되었습니다.`;
            }

            selectedBeachId.value = newSelectedId;

            showConfirmModal({
                title: '해수욕장 선택 변경',
                message: message,
                type: 'info',
                autoHide: true,
                duration: 1500
            });

        } catch (error) {
            apiError.value = error;
            showConfirmModal({
                title: '선택 실패',
                message: '해수욕장 선택 중 오류가 발생했습니다.',
                type: 'error',
            });
        } finally {
            isLoading.value = false;
        }
    };

    /**
     * 즐겨찾기 토글 및 DB 저장
     */
    const toggleFavoriteBeach = async (beachId) => {
        const { showConfirmModal } = useConfirmModal();

        try {
            isLoading.value = true;
            const isFavorite = favoriteBeachIds.value.includes(beachId);

            if (isFavorite) {
                // await beachApi.removeFavorite(beachId);
                favoriteBeachIds.value = favoriteBeachIds.value.filter(id => id !== beachId);
                showConfirmModal({ title: '즐겨찾기 해제', message: '즐겨찾기에서 해제되었습니다.', type: 'info', autoHide: true, duration: 1000 });
            } else {
                // await beachApi.addFavorite(beachId);
                favoriteBeachIds.value.push(beachId);
                showConfirmModal({ title: '즐겨찾기 등록', message: '즐겨찾기에 등록되었습니다.', type: 'success', autoHide: true, duration: 1000 });
            }
        } catch (error) {
            apiError.value = error;
            showConfirmModal({ title: '처리 실패', message: '즐겨찾기 처리 중 오류가 발생했습니다.', type: 'error' });
        } finally {
            isLoading.value = false;
        }
    };

    // --- Getters ---

    const getCurrentSelectedBeachId = computed(() => selectedBeachId.value);
    const getFavoriteBeachIds = computed(() => favoriteBeachIds.value);
    const getCurrentBeachDetail = computed(() => currentBeachDetail.value);

    return {
        selectedBeachId,
        favoriteBeachIds,
        isLoading,
        apiError,
        currentBeachDetail,
        isDetailLoading,
        fetchBeachDetail,
        toggleSelectBeach,
        toggleFavoriteBeach,
        getCurrentSelectedBeachId,
        getFavoriteBeachIds,
        getCurrentBeachDetail,
    };
});