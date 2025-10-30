import { ref, computed } from 'vue';
import { defineStore } from 'pinia';
import axios from 'axios';
import { beachApi } from '@/api/beach';
import { useConfirmModal } from '@/utils/modalUtils';

const FAVORITES_API_URL = 'http://localhost:8080/api/beach/favorites';

export const useBeachStore = defineStore('beach', () => {
    const beaches = ref([]);
    // ✅ 선택 상태를 새로고침 후에도 유지: localStorage에서 초기값 복원
    const selectedBeachId = ref(Number(localStorage.getItem('selectedBeachId')) || null);
    const favoriteBeachIds = ref([]);
    const isLoading = ref(false);
    const apiError = ref(null);
    const currentBeachDetail = ref(null);
    const isDetailLoading = ref(false);
    const { showConfirmModal } = useConfirmModal();

    // === 댓글 상태 ===
    const comments = ref([]);
    const isCommentsLoading = ref(false);   

    // 현재 id가 선택 상태인지?
    const isSelected = (id) => selectedBeachId.value === id

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
    /**
     * ✅ 선택/해제 토글 (낙관적 업데이트 → 서버 호출 → 실패 시 롤백)
     *  - 선택: POST /user/select-beach { beachNumber }
     *  - 해제: POST /user/unselect-beach
     */
    const toggleSelectBeach = async (beachId, beachName) => {
      const prevId = selectedBeachId.value
      const selecting = prevId !== beachId // true=선택, false=해제

      // 1) UI/스토리지 먼저 반영
      selectedBeachId.value = selecting ? beachId : null
      if (selecting) localStorage.setItem('selectedBeachId', String(beachId))
      else localStorage.removeItem('selectedBeachId')

      try {
        if (selecting) {
          await beachApi.selectBeach({ beachNumber: beachId })
          showConfirmModal({
            title: '활동 해수욕장',
            message: `${beachName}가 선택되었습니다.`,
            type: 'success',
            autoHide: true,
            duration: 1200,
          })
        } else {
          await beachApi.unselectBeach()
          showConfirmModal({
            title: '선택 해제',
            message: `${beachName} 선택이 해제되었습니다.`,
            type: 'info',
            autoHide: true,
            duration: 1000,
          })
        }
      } catch (e) {
        // 2) 실패 → 롤백
        selectedBeachId.value = prevId
        if (prevId) localStorage.setItem('selectedBeachId', String(prevId))
        else localStorage.removeItem('selectedBeachId')

        showConfirmModal({
          title: '처리 실패',
          message: '선택 상태 저장 중 오류가 발생했습니다. 다시 시도해 주세요.',
          type: 'error',
          autoHide: false,
        })
        console.error('toggleSelectBeach error:', e)
      }
    }

  const toggleFavoriteBeach = async (beachNumber) => {
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

    // -------- 댓글 액션 --------
    const loadComments = async (beachNumber) => {
      isCommentsLoading.value = true;
      try {
        const call = beachApi.fetchComments(beachNumber);
        const res = await call();
        comments.value = res?.result ?? res?.data?.result ?? [];
      } catch (e) {
        console.error('리뷰 목록 불러오기 실패', e);
        comments.value = [];
      } finally {
        isCommentsLoading.value = false;
      }
    };

    const addComment = async (beachNumber, { commentContent, rating }) => {
      const call = beachApi.addComment(beachNumber);
      await call({ commentContent, rating });
      await loadComments(beachNumber);
    };

    const editComment = async (beachNumber, beachCommentNumber, { commentContent, rating }) => {
      const call = beachApi.editComment(beachNumber, beachCommentNumber);
      await call({ commentContent, rating });
      await loadComments(beachNumber);
    };

    const deleteComment = async (beachCommentNumber, beachNumber) => {
      const call = beachApi.deleteComment(beachCommentNumber);
      await call();
      await loadComments(beachNumber);
    };
    


    // --- Getters ---
    const getCurrentSelectedBeachId = computed(() => selectedBeachId.value);
    const getFavoriteBeachIds = computed(() => favoriteBeachIds.value);
    const getCurrentBeachDetail = computed(() => currentBeachDetail.value);
    const getComments = computed(() => comments.value);

    return {
        // state
        beaches,
        selectedBeachId,
        favoriteBeachIds,
        isLoading,
        apiError,
        currentBeachDetail,
        isDetailLoading,
        comments, 
        isCommentsLoading,

        // actions
        fetchBeaches,
        fetchBeachDetail,
        fetchFavoriteIds,
        toggleSelectBeach,
        toggleFavoriteBeach,
        addComment,
        editComment,
        deleteComment,
        loadComments,
        isSelected,

        // getters
        getCurrentSelectedBeachId,
        getFavoriteBeachIds,
        getCurrentBeachDetail,
        getComments
    };
});
