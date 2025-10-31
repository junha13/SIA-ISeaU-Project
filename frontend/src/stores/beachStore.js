import { ref, computed } from 'vue';
import { defineStore } from 'pinia';
import axios from 'axios';
import { beachApi } from '@/api/beach';
import { useConfirmModal } from '@/utils/modalUtils';

const FAVORITES_API_URL = 'http://172.168.10.15:8080/api/beach/favorites';

export const useBeachStore = defineStore('beach', () => {
    const beaches = ref([]);
    const selectedBeachId = ref(null);
    const favoriteBeachIds = ref([]);
    const isLoading = ref(false);
    const apiError = ref(null);
    const currentBeachDetail = ref(null);
    const isDetailLoading = ref(false);

    // --- Actions ---
    const fetchBeaches = async (params = {}) => {
        isLoading.value = true;
        apiError.value = null;
        try {
            const res = await beachApi.fetchBeachList(params);
            const list = res?.data?.result ?? res?.result ?? res ?? [];
            beaches.value = Array.isArray(list) ? list : [];
        } catch (error) {
            console.error('해수욕장 목록 조회 실패:', error);
            apiError.value = error;
            beaches.value = [];
        } finally {
            isLoading.value = false;
        }
    };

    const fetchBeachDetail = async (beachNumber) => {
        isDetailLoading.value = true;
        currentBeachDetail.value = null;
        try {
            const url = `/api/beach/detail/${beachNumber}/info`;
            const { execute: callDetailApi } = useApi('get', url);
            const result = await callDetailApi();
            currentBeachDetail.value = result.data.result;
        } catch (error) {
            console.error('해수욕장 상세 정보 조회 실패:', error);
            apiError.value = error;
        } finally {
            isDetailLoading.value = false;
        }
    };

    const fetchFavoriteIds = async () => {
        try {
            const MY_FAVORITES_API = FAVORITES_API_URL + '/my';
            const response = await axios.get(MY_FAVORITES_API);
            const favoritesList = response.data?.result ?? response.data?.data;
            if (Array.isArray(favoritesList)) {
                favoriteBeachIds.value = favoritesList.map(fav => fav.beachNumber).filter(id => id != null);
            }
        } catch (error) {
            console.error('즐겨찾기 초기 로딩 실패:', error);
        }
    };

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

    const toggleFavoriteBeach = async (beachNumber) => {
  const { showConfirmModal } = useConfirmModal();
  const isFav = favoriteBeachIds.value.includes(beachNumber);

  try {
    if (isFav) {
      // 삭제
      await axios.delete(`${FAVORITES_API_URL}/${beachNumber}`);
      favoriteBeachIds.value = favoriteBeachIds.value.filter(id => id !== beachNumber);
      showConfirmModal({
        title: '즐겨찾기 해제',
        message: '즐겨찾기에서 해제되었습니다.',
        type: 'info',
        autoHide: true,
        duration: 1000
      });
    } else {
      // 추가
      await axios.post(FAVORITES_API_URL, { beachNumber });
      // 중복 409는 catch에서 처리
      if (!favoriteBeachIds.value.includes(beachNumber)) {
        favoriteBeachIds.value.push(beachNumber);
      }
      showConfirmModal({
        title: '즐겨찾기 등록',
        message: '등록되었습니다.',
        type: 'success',
        autoHide: true,
        duration: 1000
      });
    }
  } catch (error) {
    if (error.response?.status === 409) {
      // 이미 등록됨 → UI만 반영
      if (!favoriteBeachIds.value.includes(beachNumber)) {
        favoriteBeachIds.value.push(beachNumber);
      }
      showConfirmModal({
        title: '이미 등록됨',
        message: '이미 즐겨찾기에 등록되어 있습니다.',
        type: 'info',
        autoHide: true,
        duration: 1000
      });
    } else {
      console.error('즐겨찾기 API 오류:', error);
      showConfirmModal({
        title: '처리 실패',
        message: '즐겨찾기 처리 중 오류가 발생했습니다.',
        type: 'error',
        autoHide: false,
        duration: 2000
      });
    }
  }
};

    // --- Getters ---
    const getCurrentSelectedBeachId = computed(() => selectedBeachId.value);
    const getFavoriteBeachIds = computed(() => favoriteBeachIds.value);
    const getCurrentBeachDetail = computed(() => currentBeachDetail.value);

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
        fetchFavoriteIds,
        toggleSelectBeach,
        toggleFavoriteBeach,
        getCurrentSelectedBeachId,
        getFavoriteBeachIds,
        getCurrentBeachDetail,
    };
});
