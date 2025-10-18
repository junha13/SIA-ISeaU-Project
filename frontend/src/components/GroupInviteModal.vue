<template>
  <!-- Group 3981 디자인 기반 모달 -->
  <div v-if="isVisible" class="modal-backdrop d-flex align-items-center justify-content-center" style="z-index: 1060;">
    <div class="modal-dialog modal-dialog-centered" role="document">
      <div class="modal-content shadow-xl rounded-lg border-0 bg-white rounded-xl">
        <div class="modal-header d-flex justify-content-between align-items-center border-0 p-4">
          <h5 class="modal-title fw-bolder" :style="{ color: darkColor }">그룹원 추가</h5>
          <button type="button" class="btn-close" @click="handleCancel"></button>
        </div>
        <div class="modal-body p-4">

          <!-- 1. 검색 방법 선택 -->
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

          <!-- 2. 검색 입력 -->
          <div class="mb-4">
            <h6 class="fw-bold mb-2">{{ searchMethod === 'id' ? '사용자 아이디' : '사용자 정보 (이름, 전화번호)' }}</h6>
            <input v-if="searchMethod === 'id'" type="text" class="form-control" placeholder="아이디를 입력하세요" v-model="searchQuery">
            <div v-else>
              <input type="text" class="form-control mb-2" placeholder="이름을 입력하세요" v-model="searchQueryName">
              <input type="text" class="form-control" placeholder="전화번호를 입력하세요" v-model="searchQueryPhone">
            </div>
            <button class="btn w-100 mt-3 fw-bold text-white" :style="{ backgroundColor: mainColor }" @click="searchUser">조회</button>
          </div>

          <!-- 3. 검색 결과 및 마커 색상 선택 -->
          <div v-if="searchResult" class="mb-4">
            <!-- 검색 결과 -->
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

            <!-- 마커 색상 선택 -->
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

        <!-- 4. 액션 버튼 -->
        <div class="modal-footer d-flex justify-content-center border-0 p-4 pt-0">
          <button type="button" class="btn fw-bold flex-fill text-white" :style="{ backgroundColor: mainColor }"
                  :disabled="!searchResult?.found"
                  @click="handleInvite">추가</button>
          <button type="button" class="btn btn-light-secondary fw-bold flex-fill" @click="handleCancel">취소</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useConfirmModal } from '@/utils/modalUtils';
import { groupApi } from '@/api/group';

const mainColor = '#0092BA';
const darkColor = '#0B1956';
const { showConfirmModal } = useConfirmModal();

const props = defineProps({
  isVisible: { type: Boolean, default: false },
});

const emit = defineEmits(['update:isVisible']);

// --- State ---
const searchMethod = ref('id');
const searchQuery = ref(''); // ID 검색
const searchQueryName = ref(''); // 이름 검색
const searchQueryPhone = ref(''); // 전화번호 검색
const searchResult = ref(null); // { found: boolean, name: string, username: string, id: number }
const markerColors = ['#0b1956', '#702568', '#B93F67', '#EB725B', '#FFB354', '#8482FF']; // #F9F871 대신 안전 컬러 사용
const selectedMarkerColor = ref(markerColors[0]);

// --- Methods ---

const searchUser = async () => {
  // if (!searchQuery.value && searchMethod.value === 'id') return;
  // if (!searchQueryName.value && !searchQueryPhone.value && searchMethod.value === 'phone') return;

  // 1. API 호출: 사용자 검색 (더미 로직)
  // const payload = searchMethod.value === 'id' ? { userId: searchQuery.value } : { name: searchQueryName.value, phone: searchQueryPhone.value };
  // const { data, error } = await groupApi.searchUser(payload);

  // 2. 더미 검색 결과
  if (searchMethod.value === 'id' && searchQuery.value === 'marine_kim') {
    searchResult.value = { found: true, id: 101, name: '김해양', username: 'marine_kim' };
  } else if (searchMethod.value === 'phone' && searchQueryName.value === '박선장' && searchQueryPhone.value === '010-2345-6789') {
    searchResult.value = { found: true, id: 102, name: '박선장', username: 'captain_park' };
  } else {
    searchResult.value = { found: false, name: '', username: '', id: null };
  }
};

const handleInvite = async () => {
  if (!searchResult.value?.found) return;

  // 1. API 호출: 그룹원 초대 (Dummy: 그룹 ID 1로 가정)
  // try {
  //   await groupApi.inviteGroupMember({
  //     groupId: 1,
  //     targetUserId: searchResult.value.id,
  //     markerColor: selectedMarkerColor.value
  //   });

  showConfirmModal({
    title: '초대 성공',
    message: `${searchResult.value.name}님에게 그룹 초대 요청을 보냈습니다.`,
    type: 'success',
    autoHide: true,
    duration: 1500
  });

  // 2. 모달 닫기 및 상태 초기화
  emit('update:isVisible', false);
  resetState();
  // } catch(error) {
  //    showConfirmModal({ title: '초대 실패', message: '초대 요청 중 오류가 발생했습니다.', type: 'error' });
  // }
};

const handleCancel = () => {
  emit('update:isVisible', false);
  resetState();
};

const resetState = () => {
  searchMethod.value = 'id';
  searchQuery.value = '';
  searchQueryName.value = '';
  searchQueryPhone.value = '';
  searchResult.value = null;
  selectedMarkerColor.value = markerColors[0];
};

</script>

<style scoped>
/* 모달 오버라이드 */
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6); /* 배경 불투명도 높임 */
  transition: opacity 0.3s ease;
}

.modal-content {
  border-radius: 12px;
  animation: modal-in 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

/* 마커 색상 옵션 스타일 */
.marker-color-option {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.marker-color-option.active-color {
  /* 선택된 색상일 때 메인 컬러로 테두리 강조 */
  box-shadow: 0 0 0 3px v-bind(mainColor);
  border: 3px solid white !important;
}

/* 폼 체크 라디오 버튼 커스텀 */
.form-check-label {
  cursor: pointer;
}
.form-check-input {
  display: none; /* 기본 라디오 버튼 숨기기 */
}

.form-check-label.border-primary {
  border-color: v-bind(mainColor) !important;
  background-color: #f1f9ff; /* 연한 배경색 */
}

@keyframes modal-in {
  0% { transform: scale(0.9); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}
</style>
