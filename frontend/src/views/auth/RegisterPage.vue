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
        <input type="text" class="form-control" placeholder="아이디 (영어 or 숫자, 4자 이상)" v-model="id">
        <button 
          class="btn fw-bold text-white" 
          :style="{ backgroundColor: isIdChecked ? '#28a745' : mainColor, minWidth: '100px' }" 
          @click="checkId"
          :disabled="isIdChecked">
          {{ isIdChecked ? '완료됨' : '중복확인' }}
        </button>
      </div>

      <div class="form-group mb-3 d-flex gap-2">
        <input type="password" class="form-control" placeholder="비밀번호 (영어 + 숫자, 8자 이상)" v-model="password">
        <button 
          class="btn fw-bold text-white" 
          :style="{ backgroundColor: isPasswordValid ? '#28a745' : mainColor, minWidth: '100px' }" 
          @click="handleCheckPassword"
          :disabled="isPasswordValid">
          {{ isPasswordValid ? '완료됨' : '확인' }}
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
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirmModal } from '@/utils/modalUtils';
import { authApi } from '@/api/auth';

const router = useRouter();
const { showConfirmModal } = useConfirmModal();
// axios를 직접 사용하여 API 호출

const mainColor = '#0092BA';
const darkColor = '#0B1956';
const dangerColor = '#EB725B';

const name = ref('');
const email = ref('');
const phone = ref('');
const birthDate = ref('');
const id = ref('');
const password = ref('');
const passwordConfirm = ref('');
const isIdChecked = ref(false);
const isCheckingId = ref(false);
const isRegistering = ref(false);
const isPasswordValid = ref(false);

// id이 바뀌면 기존 중복확인 상태 초기화
watch(id, () => {
  isIdChecked.value = false;
});

// password가 바뀌면 기존 비밀번호 확인 상태 초기화
watch(password, () => {
  isPasswordValid.value = false;
});

const checkId = async () => {
  console.log('중복확인 버튼 클릭됨');
  console.log('입력된 아이디:', id.value);
  
  // 아이디 형식 검사
  if (id.value.length < 4) {
    console.log('아이디 길이 부족:', id.value.length);
    showConfirmModal({ title: '오류', message: '아이디는 4자 이상이어야 합니다.', type: 'error', autoHide: true });
    isIdChecked.value = false;
    return;
  }
  
  isCheckingId.value = true;
  console.log('API 호출 시작...');

  // 중복확인 API 호출 (공통 API 사용)
  try {
    /**
     * POST /api/auth/check-id
     * @param String id - 중복 확인할 아이디 (백엔드에서 String body 기대 → JSON.stringify로 전달)
     * @returns {number} 0: 사용 가능(중복 안됨), 1 이상: 이미 사용 중(중복됨)
     */
    const res = await authApi.checkId(JSON.stringify(id.value));
    console.log('API 응답:', res);
    
    const result = res?.data;
    console.log('중복확인 결과:', result);
    
    if (result === 0) {
      isIdChecked.value = true;
      showConfirmModal({ title: '확인', message: '사용 가능한 아이디입니다.', type: 'success', autoHide: true });
    } else {
      isIdChecked.value = false;
      showConfirmModal({ title: '중복된 아이디', message: '이미 사용 중인 아이디입니다. 다른 아이디를 입력해주세요.', type: 'error', autoHide: false });
    }
  } catch (err) {
    console.error('아이디 중복확인 오류:', err);
    console.error('오류 상세:', err.response);
    isIdChecked.value = false;
    showConfirmModal({ title: '오류', message: err.response?.data?.message || '아이디 중복확인에 실패했습니다.', type: 'error' });
  } finally {
    isCheckingId.value = false;
    console.log('API 호출 종료');
  }
};

// 비밀번호 유효성 검사
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
  // 필수 입력값 검증 추가
  if (!name.value || !email.value || !phone.value || !birthDate.value) {
    showConfirmModal({ 
      title: '필수', 
      message: '모든 정보를 입력해주세요.', 
      type: 'error' 
    });
    return;
  }
  
  // 이메일 형식 검증
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) {
    showConfirmModal({ 
      title: '오류', 
      message: '올바른 이메일 형식이 아닙니다.', 
      type: 'error' 
    });
    return;
  }
  
  // 전화번호 형식 검증 (010-XXXX-XXXX)
  if (!/^010-\d{4}-\d{4}$/.test(phone.value)) {
    showConfirmModal({ 
      title: '오류', 
      message: '전화번호는 010-XXXX-XXXX 형식이어야 합니다.', 
      type: 'error' 
    });
    return;
  }
  
  if (!isIdChecked.value) {
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

  // 백엔드 RegisterDTO 필드명에 맞게 매핑
  const userData = {
    userName: name.value,
    email: email.value,
    mobile: phone.value,
    birthDate: birthDate.value,
    id: id.value,
    password: password.value,
    checkPassword: passwordConfirm.value,
  };

  isRegistering.value = true;
  try {
    /**
     * POST /api/auth/register
     * @param {Object} userData - 회원가입 정보 (userName, email, mobile, birthDate, id, password, checkPassword)
     * @returns {number} 1: 회원가입 성공, 0 또는 그 외: 실패
     */
    const res = await authApi.register(userData);
    const result = res?.data;

    if (result === 1) {
      showConfirmModal({ title: '회원가입 성공', message: '회원가입이 완료되었습니다. 로그인해주세요.', type: 'success', autoHide: false });
      router.push({ name: 'Login' });
    } else {
      throw new Error('회원가입에 실패했습니다.');
    }
  } catch (e) {
    console.error('회원가입 오류', e);
    showConfirmModal({ title: '회원가입 실패', message: e.response?.data?.message || e.message || '회원가입 중 오류가 발생했습니다.', type: 'error', autoHide: false });
  } finally {
    isRegistering.value = false;
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