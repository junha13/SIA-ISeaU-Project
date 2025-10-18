<template>
  <div class="my-page container-fluid p-3">
    <!-- Header (뒤로 가기 및 타이틀) -->
    <div class="d-flex align-items-center justify-content-start pb-3 border-bottom shadow-sm mb-4">
      <i class="fas fa-chevron-left me-2 fs-5" @click="$router.back()" style="cursor: pointer;" :style="{ color: darkColor }"></i>
      <h5 class="fw-bolder mb-0" :style="{ color: darkColor }">마이페이지</h5>
    </div>

    <!-- 1. 정보 섹션 -->
    <div class="card shadow-sm border-0 rounded-3 mb-4 p-4">
      <h6 class="fw-bold mb-3" :style="{ color: mainColor }">정보</h6>
      <div class="d-flex justify-content-between py-2 border-bottom">
        <span class="text-muted">이름</span>
        <span class="fw-bold">{{ userInfo.name }}</span>
      </div>
      <div class="d-flex justify-content-between py-2">
        <span class="text-muted">전화번호</span>
        <span class="fw-bold">{{ userInfo.phone }}</span>
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
          <input class="form-check-input" type="checkbox" id="level1" v-model="settings.alertLevel1" :style="switchStyle(settings.alertLevel1)">
        </div>
      </div>
      <div class="d-flex justify-content-between align-items-center py-2">
        <span class="text-muted small">2단계 (현재 200M)</span>
        <div class="form-check form-switch">
          <input class="form-check-input" type="checkbox" id="level2" v-model="settings.alertLevel2" :style="switchStyle(settings.alertLevel2)">
        </div>
      </div>
      <div class="d-flex justify-content-between align-items-center py-2 border-bottom pb-4">
        <span class="text-muted small">3단계 (현재 200M)</span>
        <div class="form-check form-switch">
          <input class="form-check-input" type="checkbox" id="level3" v-model="settings.alertLevel3" :style="switchStyle(settings.alertLevel3)">
        </div>
      </div>

      <!-- 기타 알림 설정 -->
      <h6 class="fw-bold text-muted mt-4 mb-3">그룹 위치 미갱신 알림</h6>
      <div class="d-flex justify-content-between align-items-center py-2">
        <span class="text-muted small">그룹 위치 미갱신 알림</span>
        <div class="form-check form-switch">
          <input class="form-check-input" type="checkbox" id="missing" v-model="settings.missingAlert" :style="switchStyle(settings.missingAlert)">
        </div>
      </div>
      <div class="d-flex justify-content-between align-items-center py-2">
        <span class="text-muted small">조위/물때 알림</span>
        <div class="form-check form-switch">
          <input class="form-check-input" type="checkbox" id="tide" v-model="settings.tideAlert" :style="switchStyle(settings.tideAlert)">
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
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirmModal } from '@/utils/modalUtils';

const router = useRouter();
const { showConfirmModal } = useConfirmModal();

// --- Color Definitions ---
const mainColor = '#0092BA';
const darkColor = '#0B1956';
const dangerColor = '#EB725B';

// --- Dummy Data ---
const userInfo = ref({
  name: '김바다',
  phone: '010-1234-5678',
});

const settings = ref({
  alertLevel1: true,
  alertLevel2: true,
  alertLevel3: false,
  missingAlert: true,
  tideAlert: false,
});

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
 * 로그아웃 처리 함수 (Promise 체인 오류 수정 및 강제 리로드 로직 유지)
 */
const handleLogout = async () => {
  // 1. 확인 모달 (Promise를 반환하며, await로 결과를 받음)
  const result = await showConfirmModal({
    title: '로그아웃',
    message: '정말로 로그아웃 하시겠습니까?',
    type: 'confirm',
    confirmText: '로그아웃',
    cancelText: '취소',
  });

  if (result) {
    // 2. API 호출: 로그아웃 처리 (생략)
    // await authApi.logout();

    // 3. 성공 알림 모달 (Promise를 반환하지만, 결과를 await로 받고 .then()을 사용하지 않음)
    // 이전 에러의 원인이었던 .then() 호출을 제거합니다.
    await showConfirmModal({
      title: '완료',
      message: '성공적으로 로그아웃되었습니다.',
      type: 'success',
      autoHide: true,
      duration: 1000,
    });

    // 4. 로그인 페이지로 이동 시도
    try {
      // Option A: Vue Router를 사용한 일반적인 이동
      router.push({ name: 'Login' });
    } catch (e) {
      // Option B: Vue Router 이동이 실패할 경우, 강제로 페이지를 리로드하며 경로 변경
      console.error("Vue Router push failed, using window.location.href fallback.", e);
      window.location.href = '/login';
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