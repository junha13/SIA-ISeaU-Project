<template>
  <div class="auth-page d-flex flex-column align-items-center justify-content-center min-vh-100 p-4"
       :style="{ backgroundColor: 'white' }">
    <!-- Logo -->
    <h1 class="logo fw-bolder mb-4" :style="{ color: darkColor, fontSize: '2.5rem' }">I Sea U</h1>

    <!-- Register Form Container -->
    <div class="auth-card p-4 rounded-3 shadow-lg w-100" style="max-width: 400px; background-color: white; border: 1px solid #eee;">

      <h5 class="fw-bold mb-4" :style="{ color: mainColor }">회원가입</h5>

      <div class="form-group mb-3">
        <input type="text" class="form-control" placeholder="본인 실명" v-model="name">
      </div>
      <div class="form-group mb-3">
        <input type="email" class="form-control" placeholder="이메일" v-model="email">
      </div>
      <div class="form-group mb-3">
        <input type="tel" class="form-control" placeholder="전화번호" v-model="phone">
      </div>
      <div class="form-group mb-3">
        <input type="date" class="form-control" placeholder="생년월일" v-model="birthDate">
      </div>

      <!-- 아이디 중복 확인 -->
      <div class="form-group mb-3 d-flex gap-2">
        <input type="text" class="form-control" placeholder="아이디 (영어 or 숫자, 4자 이상)" v-model="username">
        <button class="btn fw-bold text-white" :style="{ backgroundColor: mainColor, minWidth: '100px' }" @click="checkUsername">
          중복확인
        </button>
      </div>

      <div class="form-group mb-3 d-flex gap-2">
        <input type="password" class="form-control" placeholder="비밀번호 (영어 + 숫자, 8자 이상)" v-model="password">
        <button class="btn fw-bold text-white" :style="{ backgroundColor: mainColor, minWidth: '100px' }" @click="handleCheckPassword">
          확인
        </button>
      </div>

      <div class="form-group mb-4">
        <input type="password" class="form-control" placeholder="비밀번호 확인" v-model="passwordConfirm">
      </div>

      <!-- Register Button -->
      <button class="btn w-100 fw-bold text-white py-2" :style="{ backgroundColor: dangerColor }" @click="handleRegister">
        회원가입 완료
      </button>
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
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirmModal } from '@/utils/modalUtils';
import { useAuthStore } from '@/stores/authStore'; // AuthStore 임포트

const router = useRouter();
const { showConfirmModal } = useConfirmModal();
const authStore = useAuthStore();

const mainColor = '#0092BA';
const darkColor = '#0B1956';
const dangerColor = '#EB725B';

const name = ref('');
const email = ref('');
const phone = ref('');
const birthDate = ref('');
const username = ref('');
const password = ref('');
const passwordConfirm = ref('');
const isUsernameChecked = ref(false);
const isPasswordValid = ref(false);

const checkUsername = async () => {
  if (username.value.length < 4) {
    showConfirmModal({ title: '오류', message: '아이디는 4자 이상이어야 합니다.', type: 'error', autoHide: true });
    isUsernameChecked.value = false;
    return;
  }
  // API 호출: 중복 확인 (더미 로직)
  // try { await authApi.checkUsername(username.value); } catch { ... }

  isUsernameChecked.value = true;
  showConfirmModal({ title: '확인', message: '사용 가능한 아이디입니다.', type: 'success', autoHide: true });
};

const handleCheckPassword = () => {
  if (password.value.length < 8 || !/[a-zA-Z]/.test(password.value) || !/[0-9]/.test(password.value)) {
    showConfirmModal({ title: '오류', message: '비밀번호는 영어, 숫자를 포함하여 8자 이상이어야 합니다.', type: 'error', autoHide: true });
    isPasswordValid.value = false;
    return;
  }
  isPasswordValid.value = true;
  showConfirmModal({ title: '확인', message: '비밀번호가 안전합니다.', type: 'success', autoHide: true });
};


const handleRegister = async () => {
  if (!isUsernameChecked.value) {
    showConfirmModal({ title: '필수', message: '아이디 중복 확인이 필요합니다.', type: 'error', autoHide: false });
    return;
  }
  if (!isPasswordValid.value) {
    showConfirmModal({ title: '필수', message: '비밀번호 확인 버튼을 눌러 유효성을 검사해야 합니다.', type: 'error', autoHide: false });
    return;
  }
  if (password.value !== passwordConfirm.value) {
    showConfirmModal({ title: '오류', message: '비밀번호 확인이 일치하지 않습니다.', type: 'error', autoHide: false });
    return;
  }

  const userData = {
    name: name.value,
    email: email.value,
    phone: phone.value,
    birthDate: birthDate.value,
    username: username.value,
    password: password.value,
  };

  try {
    // Store Action 호출 (API 통신)
    await authStore.register(userData);

    // 성공 시
    showConfirmModal({
      title: '회원가입 성공',
      message: '회원가입이 완료되었습니다. 로그인해주세요.',
      type: 'success',
      autoHide: false,
    });

    router.push({ name: 'Login' });

  } catch (e) {
    // 실패 시
    showConfirmModal({
      title: '회원가입 실패',
      message: e.message,
      type: 'error',
      autoHide: false
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
</style>