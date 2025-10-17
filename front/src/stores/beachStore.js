// src/store/beachStore.js

import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { beachApi } from '../api/beach'; // beach.js에서 API 로직을 가져옵니다.
import { useConfirmModal } from '../utils/modalUtils'; // 모달 유틸리티를 사용합니다.

export const useBeachStore = defineStore('beach', () => {
    // 현재 선택된 해수욕장 ID (토글식 선택)
    const selectedBeachId = ref(null);
    // 즐겨찾기 해수욕장 ID 목록
    const favoriteBeachIds = ref([]);
    // 로딩 상태 및 API 에러
    const isLoading = ref(false);
    const apiError = ref(null);

    // --- Actions ---

    /**
     * 해수욕장 선택/해제 토글 및 DB 저장 (선택은 하나만 가능)
     * @param {number} beachId - 토글할 해수욕장 ID
     * @param {string} beachName - 선택된 해수욕장 이름 (UX용)
     */
    const toggleSelectBeach = async (beachId, beachName) => {
        const { showConfirmModal } = useConfirmModal();

        try {
            isLoading.value = true;

            let newSelectedId = null;
            let message = '';

            if (selectedBeachId.value === beachId) {
                // 선택 해제
                newSelectedId = null;
                // API 호출: 선택 해제 (null 또는 특정 해제 API)
                // await beachApi.unselectBeach();
                message = `${beachName} 해수욕장의 선택이 해제되었습니다.`;
            } else {
                // 새로운 해수욕장 선택 (이전 선택 자동 해제)
                newSelectedId = beachId;
                // API 호출: 해수욕장 선택 저장
                // const result = await beachApi.selectBeach(beachId);
                message = `${beachName} 해수욕장이 현재 활동 해수욕장으로 선택되었습니다.`;
            }

            // 실제 상태 업데이트
            selectedBeachId.value = newSelectedId;

            // 모달 표시
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
            console.error('해수욕장 선택 중 에러 발생:', error);
        } finally {
            isLoading.value = false;
        }
    };

    /**
     * 즐겨찾기 토글 및 DB 저장
     * @param {number} beachId - 즐겨찾기 토글할 해수욕장 ID
     */
    const toggleFavoriteBeach = async (beachId) => {
        const { showConfirmModal } = useConfirmModal();

        try {
            isLoading.value = true;
            const isFavorite = favoriteBeachIds.value.includes(beachId);

            if (isFavorite) {
                // 즐겨찾기 해제
                // API 호출: 즐겨찾기 해제
                // await beachApi.removeFavorite(beachId);
                favoriteBeachIds.value = favoriteBeachIds.value.filter(id => id !== beachId);
                showConfirmModal({
                    title: '즐겨찾기 해제',
                    message: '즐겨찾기에서 해제되었습니다.',
                    type: 'info',
                    autoHide: true,
                    duration: 1000
                });
            } else {
                // 즐겨찾기 등록
                // API 호출: 즐겨찾기 등록
                // await beachApi.addFavorite(beachId);
                favoriteBeachIds.value.push(beachId);
                showConfirmModal({
                    title: '즐겨찾기 등록',
                    message: '즐겨찾기에 등록되었습니다.',
                    type: 'success',
                    autoHide: true,
                    duration: 1000
                });
            }
        } catch (error) {
            apiError.value = error;
            showConfirmModal({
                title: '처리 실패',
                message: '즐겨찾기 처리 중 오류가 발생했습니다.',
                type: 'error',
            });
            console.error('즐겨찾기 처리 중 에러 발생:', error);
        } finally {
            isLoading.value = false;
        }
    };

    // --- Getters ---

    // 현재 선택된 해수욕장 ID를 반환
    const getCurrentSelectedBeachId = computed(() => selectedBeachId.value);

    // 즐겨찾기 ID 목록을 반환
    const getFavoriteBeachIds = computed(() => favoriteBeachIds.value);

    return {
        selectedBeachId,
        favoriteBeachIds,
        isLoading,
        apiError,
        toggleSelectBeach,
        toggleFavoriteBeach,
        getCurrentSelectedBeachId,
        getFavoriteBeachIds,
    };
});
