<template>
  <div class="auth-page d-flex flex-column align-items-center justify-content-center min-vh-100 p-4"
       :style="{ backgroundColor: 'white' }">
    <!-- Logo -->
    <h1 class="logo fw-bolder mb-5" :style="{ color: darkColor, fontSize: '2.5rem' }">I Sea U</h1>

    <!-- Authentication Card Container -->
    <div class="auth-card p-4 rounded-3 shadow-lg w-100" style="max-width: 400px; background-color: white; border: 1px solid #eee;">

      <!-- Tab Navigation (Placeholder for consistency) -->
      <div class="nav nav-tabs d-flex mb-4" role="tablist">
        <button class="nav-link flex-fill fw-bold active" type="button" :style="activeTabStyle">비밀번호 재설정</button>
      </div>

      <!-- Content -->
      <div v-if="step === 1" class="tab-content">
        <!-- Step 1: 비밀번호 재설정 입력 -->
        <h6 class="fw-bold mb-3" :style="{ color: darkColor }">새 비밀번호 설정</h6>

        <div class="form-group mb-3">
          <input type="password" class="form-control" placeholder="비밀번호 (영어 + 숫자, 8자 이상)" v-model="password" :class="{'is-invalid': errors.password}">
          <div v-if="errors.password" class="invalid-feedback">{{ errors.password }}</div>
        </div>

        <div class="form-group mb-4">
          <input type="password" class="form-control" placeholder="비밀번호 확인" v-model="passwordConfirm" :class="{'is-invalid': errors.passwordConfirm}">
          <div v-if="errors.passwordConfirm" class="invalid-feedback">{{ errors.passwordConfirm }}</div>
        </div>

        <!-- Action Button -->
        <button class="btn w-100 fw-bold text-white py-2" :style="{ backgroundColor: mainColor }" @click="handleResetPassword">
          비밀번호 재설정
        </button>
      </div>

      <div v-else-if="step === 2" class="tab-content">
        <!-- Step 2: 성공 메시지 -->
        <div class="alert text-center p-3 rounded-3 border-0 fw-bold" :style="{ backgroundColor: mainColor + '10', color: darkColor }">
          <i class="fas fa-check-circle me-2" :style="{ color: mainColor }"></i>
          {{ successMessage }}
        </div>
      </div>
    </div>

    <!-- Links -->
    <div class="mt-4 text-center">
      <a href="#" @click.prevent="$router.push({ name: 'Login' })" class="small fw-bold text-decoration-none" :style="{ color: darkColor }">
        로그인으로 돌아가기 >
      </a>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirmModal } from '@/utils/modalUtils'; // 모달 유틸리티 사용

const router = useRouter();
const { showConfirmModal } = useConfirmModal();

// --- Color Definitions ---
const mainColor = '#0092BA';
const darkColor = '#0B1956';
const dangerColor = '#EB725B';

// --- State ---
const step = ref(1); // 1: 입력 폼, 2: 성공 메시지
const password = ref('');
const passwordConfirm = ref('');
const errors = ref({});
const successMessage = ref('');

// 임시: 인증 성공 시 URL 쿼리 파라미터로 받은 사용자 이름 (FindAccountPage에서 이동 시)
const userName = computed(() => '김준하');

// --- Styles ---
const activeTabStyle = computed(() => ({
  backgroundColor: mainColor,
  color: 'white',
  borderColor: mainColor,
  borderTopLeftRadius: '0.475rem',
  borderTopRightRadius: '0.475rem',
}));

// --- Methods ---
const validate = () => {
  errors.value = {};
  let isValid = true;

  if (password.value.length < 8 || !/[a-zA-Z]/.test(password.value) || !/[0-9]/.test(password.value)) {
    errors.value.password = '비밀번호는 영어와 숫자를 포함하여 8자 이상이어야 합니다.';
    isValid = false;
  }

  if (password.value !== passwordConfirm.value) {
    errors.value.passwordConfirm = '비밀번호 확인이 일치하지 않습니다.';
    isValid = false;
  }

  return isValid;
};

const handleResetPassword = async () => {
  if (!validate()) {
    showConfirmModal({
      title: '입력 오류',
      message: '비밀번호를 정확히 입력해주세요.',
      type: 'error',
      autoHide: true,
      duration: 1500,
    });
    return;
  }

  // 1. API 호출: 비밀번호 재설정
  // try {
  //   // const response = await authApi.resetPassword({ userId: 'tempId', newPassword: password.value });

  // 2. 성공 처리
  successMessage.value = `${userName.value}님의 비밀번호가 재설정되었습니다.`;
  step.value = 2;

  // } catch (e) {
  //   showConfirmModal({ title: '재설정 실패', message: '비밀번호 재설정 중 오류가 발생했습니다.', type: 'error' });
  // }
};
</script>

<style scoped>
.auth-page {
  /* 하단 푸터가 없으므로 min-vh-100을 사용하여 전체 화면을 차지 */
}

/* 입력 필드 스타일 오버라이드 */
.form-control {
  border-radius: 0.475rem;
  border: 1px solid #ced4da;
  height: 48px;
}

/* 탭 스타일 오버라이드 (body-3.png 디자인 기반) */
.nav-tabs {
  border-bottom: none;
}
.nav-link {
  border: 1px solid #ced4da;
  border-bottom-color: transparent !important; /* 아래쪽 테두리 제거 */
  border-radius: 0.475rem 0.475rem 0 0;
  color: v-bind(darkColor);
  background-color: #f8f9fa;
  margin-right: 5px;
}
.nav-link:last-child {
  margin-right: 0;
}
.nav-link.active {
  color: white !important;
  background-color: v-bind(mainColor) !important;
  border-color: v-bind(mainColor) !important;
}

/* 오류 메시지 스타일 */
.is-invalid {
  border-color: v-bind(dangerColor) !important;
}
.invalid-feedback {
  color: v-bind(dangerColor);
}
</style>