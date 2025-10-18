<template>
  <div class="auth-page d-flex flex-column align-items-center justify-content-center min-vh-100 p-4"
       :style="{ backgroundColor: 'white' }">
    <h1 class="logo fw-bolder mb-5" :style="{ color: darkColor, fontSize: '2.5rem' }">I Sea U</h1>

    <div class="auth-card p-4 rounded-3 shadow-lg w-100" style="max-width: 400px; background-color: white; border: 1px solid #eee;">

      <h5 class="fw-bold mb-4" :style="{ color: mainColor }">로그인</h5>

      <div class="form-group mb-3">
        <input type="text" class="form-control" placeholder="아이디" v-model="username">
      </div>

      <div class="form-group mb-4">
        <input type="password" class="form-control" placeholder="비밀번호" v-model="password">
      </div>

      <div class="d-flex justify-content-between align-items-center mb-4">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" id="rememberMe" v-model="rememberMe">
          <label class="form-check-label small text-muted" for="rememberMe">
            로그인 정보 저장
          </label>
        </div>
        <button class="btn fw-bold text-white py-2" :style="{ backgroundColor: mainColor, padding: '0.375rem 1.5rem' }" @click="handleLogin">
          로그인
        </button>
      </div>

      <div class="d-flex justify-content-end gap-3 small">
        <a href="#" @click.prevent="$router.push({ name: 'Register' })" class="text-decoration-none" :style="{ color: darkColor }">
          회원가입 >
        </a>
        <a href="#" @click.prevent="$router.push({ name: 'FindAccount' })" class="text-decoration-none" :style="{ color: darkColor }">
          아이디/비밀번호 찾기 >
        </a>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirmModal } from '@/utils/modalUtils';

const router = useRouter();
const { showConfirmModal } = useConfirmModal();

const mainColor = '#0092BA';
const darkColor = '#0B1956';

const username = ref('');
const password = ref('');
const rememberMe = ref(false);

const handleLogin = async () => {
  if (!username.value || !password.value) {
    showConfirmModal({
      title: '로그인 오류',
      message: '아이디와 비밀번호를 모두 입력해주세요.',
      type: 'error',
      autoHide: true,
      duration: 1500,
    });
    return;
  }

  // 1. API 호출: 로그인 시도
  // try {
  //   // const response = await authApi.login(username.value, password.value);

  // 2. 성공 시 메인 페이지로 이동
  showConfirmModal({
    title: '로그인 성공',
    message: `${username.value}님 환영합니다!`,
    type: 'success',
    autoHide: true,
    duration: 1000,
  }).then(() => {
    router.push({ name: 'Main' });
  });

  // } catch (e) {
  //   showConfirmModal({ title: '로그인 실패', message: '아이디 또는 비밀번호가 올바르지 않습니다.', type: 'error' });
  // }
};
</script>

<style scoped>
/* 입력 필드 스타일 오버라이드 */
.form-control {
  border-radius: 0.475rem;
  border: 1px solid #ced4da;
  height: 48px;
}
.auth-page {
  /* 하단 푸터가 없으므로 min-vh-100을 사용하여 전체 화면을 차지 */
}
</style>