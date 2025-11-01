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
                  :disabled="!searchResult?.found || !searchResult?.username || isInviting"
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
//import { useRoute } from 'vue-router'; // useRoute import

const mainColor = '#0092BA';
const darkColor = '#0B1956';
const { showConfirmModal } = useConfirmModal();
//const route = useRoute(); // 1. useRoute 인스턴스 생성

const props = defineProps({
    isVisible: { type: Boolean, default: false },
    // 부모(GroupMainPage)로부터 현재 그룹 ID를 받습니다.
    groupId: { type: Number, required: true }
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

const searchUser = async () => {
    searchResult.value = null;
    searchError.value = null;
    isSearching.value = true;
    console.log('[GroupInviteModal] 사용자 검색 시작');

    // 입력값 유효성 검사
    let isValid = false;
    let payload = {
        id: null,
        name: null,
        mobile: null
    };

    if (searchMethod.value === 'id' && searchQuery.value) {
        isValid = true;
        payload.id = searchQuery.value;
    } else if (searchMethod.value === 'phone' && searchQueryName.value && searchQueryPhone.value) {
        isValid = true;
        payload.name = searchQueryName.value;
        payload.mobile = searchQueryPhone.value;
    }

    if (!isValid) {
        showConfirmModal({ title: '알림', message: '검색 정보를 올바르게 입력하세요.', type: 'warning' });
        isSearching.value = false; return;
    }

    try {
        const apiUrl = `${import.meta.env.VITE_API_BASE_URL}/api/user/search`;

        const response = await axios.post(apiUrl, payload, {
            headers: { 'Content-Type': 'application/json' },
            withCredentials: true
        });

        const responsePayload = response.data.data;

        if (responsePayload && responsePayload.found && responsePayload.user) {
            const userData = responsePayload.user;

            searchResult.value = {
                found: true,
                id: userData.id,
                name: userData.userName || userData.name,
                username: userData.id, // targetUserId로 사용할 로그인 ID
            };
        } else {
            searchResult.value = { found: false, id: null, name: null, username: null };
        }
        console.log('[GroupInviteModal] 사용자 검색 완료:', searchResult.value);

    } catch (error) {
        console.error('[GroupInviteModal] 사용자 검색 오류:', error);
        searchError.value = error.response ? error.response.data : error;
        searchResult.value = { found: false, id: null, name: null, username: null };
    } finally {
        isSearching.value = false;
    }
};

/**
 * 그룹 초대 API 호출
 */
const handleInvite = async () => {
	// Target ID가 undefined인지 확인
	console.log('Target ID:', searchResult.value?.username);

    if (!searchResult.value?.found || !searchResult.value?.username) {
        showConfirmModal({ title: '알림', message: '초대할 사용자를 먼저 조회하세요.', type: 'warning' });
        return;
    }

    isInviting.value = true;
    console.log('[GroupInviteModal] 그룹 초대 API 호출 시작...');

    // 💡 [수정] prop으로 받은 groupId 사용
    const groupId = props.groupId;

    if (!groupId) {
        console.error('[GroupInviteModal] 그룹 ID를 찾을 수 없습니다. (Prop에서 null 받음)');
        showConfirmModal({ title: '오류', message: '초대할 그룹 정보를 찾을 수 없습니다 (ID 누락).', type: 'error' });
        isInviting.value = false;
        return;
    }

	// 3. API 요청 본문(Payload) 생성
	const payload = {
		groupId: groupId,
		targetUserId: searchResult.value.username, // 유효한 ID가 들어올 것으로 기대
		markerColor: selectedMarkerColor.value
	};

    try {
        const apiUrl = `${import.meta.env.VITE_API_BASE_URL}/api/groups/invite`;
        const response = await axios.post(apiUrl, payload, {
            headers: { 'Content-Type': 'application/json' },
            withCredentials: true,
            timeout: 5000,
        });

		// 5. 🚨 응답 구조 (response.data.data) 확인
		const responsePayload = response.data.data;

		// 🚨  응답 페이로드의 success 필드를 확인
		if (responsePayload && responsePayload.success) {
			showConfirmModal({
				title: '초대 성공',
				message: `${searchResult.value.name}님에게 그룹 초대 요청을 보냈습니다.`,
				type: 'success',
				autoHide: true,
				duration: 1500
			});
			emit('update:isVisible', false);
			resetState();
			console.log('[GroupInviteModal] 그룹 초대 성공');
		} else {
			// API는 성공(200 OK)했지만, 백엔드가 success: false 반환 시
			console.warn('[GroupInviteModal] 초대 API 응답 실패:', responsePayload);
			showConfirmModal({
				title: '초대 실패',
				message: responsePayload.message || '초대 요청에 실패했습니다.',
				type: 'error'
			});
		}

	} catch (error) {
		// 6. 에러 처리 (4xx, 5xx 에러)
		console.error('[GroupInviteModal] 그룹 초대 오류:', error);
		let errorMessage = '초대 요청 중 오류가 발생했습니다.';
		if (error.response && error.response.data && error.response.data.message) {
			errorMessage = error.response.data.message; // 백엔드가 보낸 에러 메시지 사용
		}
		showConfirmModal({ title: '초대 실패', message: errorMessage, type: 'error' });
	} finally {
		isInviting.value = false;
	}
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
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.6);
    transition: opacity 0.3s ease;
}
.modal-content {
    border-radius: 12px;
    animation: modal-in 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}
.marker-color-option {
    width: 30px;
    height: 30px;
    border-radius: 50%;
    cursor: pointer;
    transition: border-color 0.2s;
    box-sizing: border-box;
}
.marker-color-option.active-color {
    box-shadow: 0 0 0 3px v-bind(mainColor);
    border: 3px solid white !important;
}
.form-check-label {
    cursor: pointer;
}
.form-check-input {
    display: none;
}
.form-check-label.border-primary {
    border-color: v-bind(mainColor) !important;
    background-color: #f1f9ff;
}
@keyframes modal-in {
    0% {
        transform: scale(0.9);
        opacity: 0;
    }
    100% {
        transform: scale(1);
        opacity: 1;
    }
}
</style>