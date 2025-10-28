<template>
  <div v-if="isVisible" class="modal-backdrop d-flex align-items-center justify-content-center" style="z-index: 1060;">
    <div class="modal-dialog modal-dialog-centered" role="document">
      <div class="modal-content shadow-xl rounded-lg border-0 bg-white rounded-xl">
        <div class="modal-header d-flex justify-content-between align-items-center border-0 p-4">
          <h5 class="modal-title fw-bolder" :style="{ color: darkColor }">그룹원 추가</h5>
          <button type="button" class="btn-close" @click="handleCancel"></button>
        </div>
        <div class="modal-body p-4">

          <div class="mb-4">
            <h6 class="fw-bold mb-3">검색 방법 선택</h6>
            <div class="form-check p-0 mb-2">
              <input class="form-check-input" type="radio" name="searchMethod" id="searchById" value="id" v-model="searchMethod" checked>
              <label class="form-check-label w-100 p-3 rounded border" for="searchById" :class="{'border-primary': searchMethod === 'id', 'border-opacity-10': searchMethod === 'id'}">
                <span class="fw-bold">아이디로 검색</span>
              </label>
            </div>
            <div class="form-check p-0">
              <input class="form-check-input" type="radio" name="searchMethod" id="searchByPhone" value="phone" v-model="searchMethod">
              <label class="form-check-label w-100 p-3 rounded border" for="searchByPhone" :class="{'border-primary': searchMethod === 'phone', 'border-opacity-10': searchMethod === 'phone'}">
                <span class="fw-bold">이름 + 전화번호로 검색</span>
              </label>
            </div>
          </div>

          <div class="mb-4">
            <h6 class="fw-bold mb-2">{{ searchMethod === 'id' ? '사용자 아이디' : '사용자 정보 (이름, 전화번호)' }}</h6>
            <input v-if="searchMethod === 'id'" type="text" class="form-control" placeholder="아이디를 입력하세요" v-model="searchQuery">
            <div v-else>
              <input type="text" class="form-control mb-2" placeholder="이름을 입력하세요" v-model="searchQueryName">
              <input type="text" class="form-control" placeholder="전화번호를 입력하세요" v-model="searchQueryPhone">
            </div>
            <button class="btn w-100 mt-3 fw-bold text-white" :style="{ backgroundColor: mainColor }" @click="searchUser" :disabled="isSearching">
              <span v-if="isSearching"><i class="fas fa-spinner fa-spin me-2"></i> 검색 중...</span>
              <span v-else>조회</span>
            </button>
          </div>

          <div v-if="searchError" class="alert alert-warning p-3 rounded-lg mb-4" role="alert">
            <i class="fas fa-exclamation-triangle me-2"></i> 사용자 검색 중 오류 발생: {{ searchError.message }}
          </div>
          <div v-if="searchResult" class="mb-4">
            <div class="alert p-3 rounded-lg" :class="searchResult.found ? 'alert-success' : 'alert-danger'" role="alert">
              <div class="d-flex align-items-center">
                <i :class="['fas me-2', searchResult.found ? 'fa-check-circle' : 'fa-times-circle']"></i>
                <span class="fw-bold">{{ searchResult.found ? '회원이 존재합니다' : '회원을 찾을 수 없습니다' }}</span>
              </div>
              <div v-if="searchResult.found" class="mt-2 ms-4">
                <div class="d-flex align-items-center">
                  <div class="me-3 rounded-circle" :style="{ backgroundColor: selectedMarkerColor, width: '20px', height: '20px' }"></div>
                  <div>
                    <h6 class="fw-bold mb-0">{{ searchResult.name }}</h6>
                    <small class="text-muted">{{ searchResult.username }}</small>
                  </div>
                </div>
              </div>
            </div>

            <h6 class="fw-bold mb-3 mt-4">마커 색상 선택</h6>
            <div class="d-flex gap-3 justify-content-center">
              <div v-for="color in markerColors" :key="color"
                   :class="['marker-color-option', { 'active-color': selectedMarkerColor === color }]"
                   :style="{ backgroundColor: color, border: selectedMarkerColor === color ? `3px solid ${mainColor}` : '3px solid transparent' }"
                   @click="selectedMarkerColor = color">
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer d-flex justify-content-center border-0 p-4 pt-0">
          <button type="button" class="btn fw-bold flex-fill text-white" :style="{ backgroundColor: mainColor }"
                  :disabled="!searchResult?.found || isInviting"
                  @click="handleInvite">
            <span v-if="isInviting"><i class="fas fa-spinner fa-spin me-2"></i> 추가 중...</span>
            <span v-else>추가</span>
          </button>
          <button type="button" class="btn btn-light-secondary fw-bold flex-fill" @click="handleCancel" :disabled="isInviting">취소</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useConfirmModal } from '@/utils/modalUtils';
import axios from 'axios';
import { useRoute } from 'vue-router';

const mainColor = '#0092BA';
const darkColor = '#0B1956';
const { showConfirmModal } = useConfirmModal();
const route = useRoute();

const props = defineProps({
  isVisible: { type: Boolean, default: false },
});

const emit = defineEmits(['update:isVisible']);

// --- State ---
const searchMethod = ref('id');
const searchQuery = ref('');
const searchQueryName = ref('');
const searchQueryPhone = ref('');
const searchResult = ref(null);
const markerColors = ['#0b1956', '#702568', '#B93F67', '#EB725B', '#FFB354', '#8482FF'];
const selectedMarkerColor = ref(markerColors[0]);

const isSearching = ref(false);
const searchError = ref(null);
const isInviting = ref(false);

// --- Methods ---

// [수정] Axios 호출 없이 더미 결과 반환
const searchUser = async () => {
  searchResult.value = null;
  searchError.value = null;
  isSearching.value = true;
  console.log('[GroupInviteModal] 사용자 검색 시작 (더미)');

  // 입력값 유효성 검사 (간단히)
  if (searchMethod.value === 'id' && !searchQuery.value) {
    showConfirmModal({ title: '알림', message: '아이디를 입력하세요.', type: 'warning' });
    isSearching.value = false; return;
  }
  if (searchMethod.value === 'phone' && (!searchQueryName.value || !searchQueryPhone.value)) {
    showConfirmModal({ title: '알림', message: '이름과 전화번호를 모두 입력하세요.', type: 'warning' });
    isSearching.value = false; return;
  }

  // --- API 호출 로직 주석 처리 ---
  // try {
  //   const apiUrl = '/api/users/search';
  //   let params = {};
  //   // ... params 설정 ...
  //   const response = await axios.get(apiUrl, { params });
  //   // ... 응답 처리 ...
  // } catch (error) {
  //   // ... 에러 처리 ...
  // } finally {
  //   isSearching.value = false;
  // }
  // --- API 호출 로직 끝 ---

  // 항상 더미 성공 결과 반환 (약간의 딜레이 추가)
  setTimeout(() => {
    if (searchMethod.value === 'id') {
      searchResult.value = {
        found: true,
        id: Math.floor(Math.random() * 1000), // 임의 ID
        name: `사용자_${searchQuery.value}`, // 입력 ID 기반 이름
        username: searchQuery.value, // 입력 ID
      };
    } else {
      searchResult.value = {
        found: true,
        id: Math.floor(Math.random() * 1000), // 임의 ID
        name: searchQueryName.value, // 입력 이름
        username: `user_${searchQueryName.value.toLowerCase()}`, // 이름 기반 임의 ID
      };
    }
    isSearching.value = false;
    console.log('[GroupInviteModal] 사용자 검색 완료 (더미 결과):', searchResult.value);
  }, 300); // 0.3초 딜레이 예시
};

// handleInvite - 더미 성공 로직만 실행 (변경 없음)
const handleInvite = async () => {
  if (!searchResult.value?.found) {
     showConfirmModal({ title: '알림', message: '초대할 사용자를 먼저 조회하세요.', type: 'warning' });
     return;
  }

  isInviting.value = true;
  console.log('[GroupInviteModal] 그룹 초대 시작 (더미)');

  setTimeout(() => {
      showConfirmModal({
        title: '초대 성공 (테스트)',
        message: `${searchResult.value.name}님에게 그룹 초대 요청을 보냈습니다. (테스트 메시지)`,
        type: 'success',
        autoHide: true,
        duration: 1500
      });
      emit('update:isVisible', false);
      resetState();
      isInviting.value = false;
      console.log('[GroupInviteModal] 그룹 초대 완료 (더미)');
  }, 500);
};

const handleCancel = () => {
  if (isInviting.value) return;
  emit('update:isVisible', false);
  resetState();
};

const resetState = () => {
  searchMethod.value = 'id';
  searchQuery.value = '';
  searchQueryName.value = '';
  searchQueryPhone.value = '';
  searchResult.value = null;
  searchError.value = null;
  selectedMarkerColor.value = markerColors[0];
  isSearching.value = false;
  isInviting.value = false;
};

watch(() => props.isVisible, (newValue) => {
    if (!newValue) {
        resetState();
    }
});

</script>

<style scoped>
/* 스타일 변경 없음 */
.modal-backdrop {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background-color: rgba(0, 0, 0, 0.6);
  transition: opacity 0.3s ease;
}
.modal-content {
  border-radius: 12px;
  animation: modal-in 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}
.marker-color-option {
  width: 30px; height: 30px; border-radius: 50%; cursor: pointer;
  transition: border-color 0.2s; box-sizing: border-box;
}
.marker-color-option.active-color {
  box-shadow: 0 0 0 3px v-bind(mainColor);
  border: 3px solid white !important;
}
.form-check-label { cursor: pointer; }
.form-check-input { display: none; }
.form-check-label.border-primary {
  border-color: v-bind(mainColor) !important;
  background-color: #f1f9ff;
}
@keyframes modal-in {
  0% { transform: scale(0.9); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}
</style>