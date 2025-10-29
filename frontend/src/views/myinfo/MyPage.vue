<template>
  <div class="my-page container-fluid p-3">
    <!-- Header (뒤로 가기 및 타이틀) -->
    <div class="d-flex align-items-center justify-content-start pb-3 border-bottom shadow-sm mb-4">
      <i class="fas fa-chevron-left me-2 fs-5" @click="$router.back()" style="cursor: pointer;" :style="{ color: darkColor }"></i>
      <h5 class="fw-bolder mb-0" :style="{ color: darkColor }">마이페이지</h5>
    </div>

    <!-- 로그인 되어있지 않을 경우 로그인 제안, 로그인 했을 경우 사용자 정보 표시 -->
    <div v-if="!authStore.isAuthenticated" class="col-12">
      <div class="card mb-3 border-info">
          <div class="card-body d-flex flex-column flex-md-row justify-content-between align-items-start">
            <div>
              <h5 class="card-title">잠깐, 로그인 하셨나요?</h5>
              <p class="card-text mb-0">로그인을 하시면 마이페이지의 보다 더 많은 기능을 만나보실 수 있습니다.</p>
              <small class="text-muted">로그인이 필요하시다면, 다음 버튼을 클릭해 주세요!</small>
            </div>
            <div class="mt-3 mt-md-0">
              <button class="btn" @click="goToLogin" :style="{ backgroundColor: mainColor }">로그인 하러 가기</button>
            </div>
          </div>
        </div>
      </div>
    
  <div v-else>
    <!-- 1. 정보 섹션 -->
        <div class="card shadow-sm border-0 rounded-3 mb-4 p-4">
          <h6 class="fw-bold mb-3" :style="{ color: mainColor }">정보</h6>
          <div class="d-flex justify-content-between py-2 border-bottom">
            <span class="text-muted">이름</span>
            <span 
              class="fw-bold" 
              :class="{ 'text-primary': !authStore.userInfo.userName, 'cursor-pointer': !authStore.userInfo.userName }"
              :style="!authStore.userInfo.userName ? { cursor: 'pointer', textDecoration: 'underline' } : {}"
              @click="!authStore.userInfo.userName && goToLogin()">
              {{ authStore.userInfo.userName || '알 수 없음' }}
            </span>
          </div>
          <div class="d-flex justify-content-between py-2">
            <span class="text-muted">전화번호</span>
            <span 
              class="fw-bold"
              :class="{ 'text-primary': !authStore.userInfo.mobile, 'cursor-pointer': !authStore.userInfo.mobile }"
              :style="!authStore.userInfo.mobile ? { cursor: 'pointer', textDecoration: 'underline' } : {}"
              @click="!authStore.userInfo.mobile && goToLogin()">
              {{ authStore.userInfo.mobile || '알 수 없음' }}
            </span>
          </div>
        </div>

    <!-- 2. 활동 섹션 -->
    <div class="card shadow-sm border-0 rounded-3 mb-4 p-4">
      <h6 class="fw-bold mb-3" :style="{ color: mainColor }">활동</h6>

      <!-- 게시글 관리 -->
      <div class="d-flex justify-content-between align-items-center py-2 border-bottom">
        <span class="text-muted">게시글 관리</span>
        <button class="btn btn-sm text-white fw-bold" :style="{ backgroundColor: mainColor }">
          확인하기
        </button>
      </div>

      <!-- 댓글 관리 -->
      <div class="d-flex justify-content-between align-items-center py-2" @click="$router.push({ name: 'MyComment' })" style="cursor: pointer;">
        <span class="text-muted">댓글 관리</span>
        <button class="btn btn-sm btn-outline-secondary fw-bold" style="border-color: #ced4da;">
          확인하기
        </button>
      </div>
    </div>

    <!-- 3. 설정 섹션 -->
    <div class="card shadow-sm border-0 rounded-3 mb-4 p-4">
      <h6 class="fw-bold mb-3" :style="{ color: mainColor }">설정</h6>

      <!-- 그룹 이탈 알림 설정 -->
      <h6 class="fw-bold text-muted mt-3 mb-3">그룹 이탈 알림</h6>
      <div class="d-flex justify-content-between align-items-center py-2">
        <span class="text-muted small">1단계 (현재 200M)</span>
        <div class="form-check form-switch">
          <input class="form-check-input" type="checkbox" id="level1" v-model="settings.alertLevel1" @change="updateSettings" :style="switchStyle(settings.alertLevel1)">
        </div>
      </div>
      <div class="d-flex justify-content-between align-items-center py-2">
        <span class="text-muted small">2단계 (현재 200M)</span>
        <div class="form-check form-switch">
          <input class="form-check-input" type="checkbox" id="level2" v-model="settings.alertLevel2" @change="updateSettings" :style="switchStyle(settings.alertLevel2)">
        </div>
      </div>
      <div class="d-flex justify-content-between align-items-center py-2 border-bottom pb-4">
        <span class="text-muted small">3단계 (현재 200M)</span>
        <div class="form-check form-switch">
          <input class="form-check-input" type="checkbox" id="level3" v-model="settings.alertLevel3" @change="updateSettings" :style="switchStyle(settings.alertLevel3)">
        </div>
      </div>

      <!-- 기타 알림 설정 -->
      <h6 class="fw-bold text-muted mt-4 mb-3">그룹 위치 미갱신 알림</h6>
      <div class="d-flex justify-content-between align-items-center py-2">
        <span class="text-muted small">그룹 위치 미갱신 알림</span>
        <div class="form-check form-switch">
          <input class="form-check-input" type="checkbox" id="missing" v-model="settings.missingAlert" @change="updateSettings" :style="switchStyle(settings.missingAlert)">
        </div>
      </div>
      <div class="d-flex justify-content-between align-items-center py-2">
        <span class="text-muted small">조위/물때 알림</span>
        <div class="form-check form-switch">
          <input class="form-check-input" type="checkbox" id="tide" v-model="settings.tideAlert" @change="updateSettings" :style="switchStyle(settings.tideAlert)">
        </div>
      </div>
    </div>

    <!-- 4. 이용 약관 및 로그아웃 -->
    <div class="card shadow-sm border-0 rounded-3 mb-5">
      <div class="d-flex justify-content-between align-items-center py-3 px-4" style="cursor: pointer;">
        <h6 class="fw-bold mb-0 text-muted">이용약관 확인</h6>
        <i class="fas fa-chevron-right text-muted"></i>
      </div>
      <div class="p-4 pt-0">
        <!-- 로그아웃 버튼 연결 -->
        <button class="btn w-100 fw-bolder py-3 fs-5 text-white rounded-3 shadow" :style="{ backgroundColor: dangerColor }" @click="handleLogout">
          로그아웃
        </button>
      </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore'; // Auth Store 사용
import { useConfirmModal } from '@/utils/modalUtils';

const router = useRouter();
const { showConfirmModal } = useConfirmModal();
const authStore = useAuthStore(); // Auth Store 인스턴스

// --- Color Definitions ---
const mainColor = '#0092BA';
const darkColor = '#0B1956';
const dangerColor = '#EB725B';

// --- State ---
// 설정 상태를 Store의 userInfo.settings에서 가져오기
const settings = authStore.userInfo.settings;


// --- Computed & Methods ---
const switchStyle = (isActive) => {
  if (isActive) {
    return {
      backgroundColor: mainColor,
      borderColor: mainColor,
      '--bs-form-switch-bg': `url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='-4 -4 8 8'%3e%3ccircle r='3' fill='%23fff'/%3e%3c/svg%3e")`,
    };
  }
  return {};
};

/**
 * 설정 변경 시 Store Action 호출
 */
const updateSettings = () => {
  // settings 객체가 Pinia Store의 reactive 객체이므로 변경 시 자동으로 Pinia state가 업데이트됨.
  // 추가적인 API 호출은 AuthStore의 updateSettings Action에서 처리됨.
  authStore.updateSettings(settings);
};

/**
 * 로그인 페이지로 이동
 */
const goToLogin = () => {
  router.push({ name: 'Login' });
};

/**
 * 로그아웃 처리 함수
 */
const handleLogout = async () => {
  const result = await showConfirmModal({
    title: '로그아웃',
    message: '정말로 로그아웃 하시겠습니까?',
    type: 'confirm',
    confirmText: '로그아웃',
    cancelText: '취소',
  });

  if (result) {
    try {
      // Auth Store의 logout Action 호출 (성공 시 Store 내에서 모달 알림 및 페이지 이동 처리)
      await authStore.logout();
    } catch (e) {
      // API 호출 실패 등의 에러 처리
      showConfirmModal({
        title: '오류',
        message: '로그아웃 중 에러가 발생했습니다.',
        type: 'error',
        autoHide: true,
      });
    }
  }
};
</script>

<style scoped>
.my-page {
  /* Footer (60px)만큼 공간 확보 */
  min-height: calc(100vh - 55px - 60px);
  padding-bottom: 30px !important;
}

/* Bootstrap form-switch input 스타일 오버라이드 (Vue style binding에 도움) */
.form-switch .form-check-input:checked:focus {
  box-shadow: 0 0 0 0.25rem rgba(0, 146, 186, 0.25); /* 주 컬러의 투명도 버전 */
}

/* 폼 스위치의 기본 배경색 오버라이드 */
.form-switch .form-check-input {
  /* 기본 비활성화 색상 */
  background-color: #e9ecef;
  border-color: #ced4da;
}
</style>