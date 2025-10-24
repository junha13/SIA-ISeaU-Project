<template>
  <div class="auth-page d-flex flex-column align-items-center justify-content-center min-vh-100 p-4"
       :style="{ backgroundColor: 'white' }">
    <!-- Logo -->
    <h1 class="logo fw-bolder mb-5" :style="{ color: darkColor, fontSize: '2.5rem' }">I Sea U</h1>

    <!-- Login Form Container -->
    <div class="auth-card p-4 rounded-3 shadow-lg w-100" style="max-width: 400px; background-color: white; border: 1px solid #eee;">

      <h5 class="fw-bold mb-4" :style="{ color: mainColor }">로그인</h5>

      <div class="form-group mb-3">
        <input type="text" class="form-control" placeholder="아이디" v-model="id">
      </div>

      <div class="form-group mb-4">
        <input type="password" class="form-control" placeholder="비밀번호" v-model="password">
      </div>

      <!-- Checkbox & Login Button -->
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

      <!-- Links -->
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
import { useAuthStore } from '@/stores/authStore';
import axios from 'axios';


const router = useRouter();
const { showConfirmModal } = useConfirmModal();
const authStore = useAuthStore();

const mainColor = '#0092BA';
const darkColor = '#0B1956';

const id = ref('');
const password = ref('');
const rememberMe = ref(false);

const handleLogin = async () => {
  if (!id.value || !password.value) {
    showConfirmModal({
      title: '로그인 오류',
      message: '아이디와 비밀번호를 모두 입력해주세요.',
      type: 'error',
      autoHide: true,
      duration: 1500,
    });
    return;
  }

  try {
    // Store Action 호출 (API 통신 및 상태 업데이트)
    const result = await axios.post('api/auth/login', {
      id: id.value,
      password: password.value
    });

    // 응답 완료 데이터 가져오기
    const userData = result.data.data // {userNumber, id, userName}

    // Store에 사용자 정보 저장
    authStore.setUser({
      userNumber: userData.userNumber,
      id: userData.id,
      userName: userData.userName
    });

    // 성공 시 모달 표시 후 페이지 이동
    showConfirmModal({
      title: '로그인 성공',
      message: `${userData.userName}님 환영합니다!`,
      type: 'success',
      autoHide: true,
      duration: 1000,
    });

    router.push({ name: 'Main' });

  } catch (e) {
    // Axios 오류 처리
    let errorMessage = '알 수 없는 오류가 발생했습니다.';

    // Spring에서 보낸 401 (비밀번호 틀림 등) 오류 메시지
    if (e.response && e.response.data && e.response.data.message) {
      errorMessage = e.response.data.message;
    }

    // 404 (경로 없음) 또는 기타 네트워크 오류
    else if (e.message) {
      errorMessage = e.message;
    }

    showConfirmModal({
      title: '로그인 실패',
      message: e.message,
      type: 'error'
    });
  }
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