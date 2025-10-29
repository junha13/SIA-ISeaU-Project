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
      <div class="form-group mb-3 d-flex gap-2">
        <!-- 전화번호를 3-4-4로 분리 입력 받음 -->
        <input type="tel" class="form-control" placeholder="010" v-model="phone1" maxlength="3">
        <input type="tel" class="form-control" placeholder="1234" v-model="phone2" maxlength="4">
        <input type="tel" class="form-control" placeholder="5678" v-model="phone3" maxlength="4">
      </div>
      <div class="form-group mb-3">
        <input type="date" class="form-control" placeholder="생년월일" v-model="birthDate">
      </div>

      <!-- 아이디 중복 확인 -->
      <div class="form-group mb-3 d-flex gap-2">
        <input type="text" class="form-control" placeholder="아이디 (영어 or 숫자, 4자 이상)" v-model="id">
        <button 
          class="btn fw-bold text-white" 
          :style="{ backgroundColor: (isIdChecked && isPasswordValid) ? '#dedede' : (isIdChecked ? '#28a745' : mainColor), minWidth: '100px', opacity: (isIdChecked && isPasswordValid) ? 0.65 : 1 }" 
          @click="checkId"
          :disabled="isIdChecked">
          {{ isIdChecked ? '완료됨' : '중복확인' }}
        </button>
      </div>

      <div class="form-group mb-3">
        <input type="password" class="form-control" placeholder="비밀번호 (영어 + 숫자, 8자 이상)" v-model="password">
      </div>

      <div class="form-group mb-4">
        <input type="password" class="form-control" placeholder="비밀번호 확인" v-model="passwordConfirm">
        <div class="mt-2">
          <small :class="passwordFormatOk ? 'text-success' : 'text-danger'">
            {{ passwordFormatOk ? '비밀번호 형식이 유효합니다.' : '비밀번호는 영어와 숫자를 포함하여 8자 이상이어야 합니다.' }}
          </small>
          <br />
          <small :class="passwordsMatch ? 'text-success' : (passwordConfirm ? 'text-danger' : 'text-muted')">
            {{ passwordsMatch ? '비밀번호가 일치합니다.' : (passwordConfirm ? '비밀번호가 일치하지 않습니다.' : '비밀번호 확인을 입력하세요.') }}
          </small>
        </div>
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
// computed: 종속된 갑이 바뀔 때마다 재계산됨(캐시)
import { ref, watch, computed } from 'vue';
import { useRouter } from 'vue-router';
import { authApi } from '@/api/auth';

const router = useRouter();

const mainColor = '#0092BA';
const darkColor = '#0B1956';
const dangerColor = '#EB725B';

const name = ref('');
const email = ref('');
const phone1 = ref('010');
const phone2 = ref('');
const phone3 = ref('');
const birthDate = ref('');
const id = ref('');
const password = ref('');
const passwordConfirm = ref('');
const isIdChecked = ref(false);
const isCheckingId = ref(false);
const isRegistering = ref(false);
const isPasswordValid = ref(false);

// 라이브 비밀번호 유효성 상태
const passwordFormatOk = computed(() => {
  return password.value.length >= 8 && /[a-zA-Z]/.test(password.value) && /[0-9]/.test(password.value);
});
const passwordsMatch = computed(() => {
  return passwordConfirm.value.length > 0 && password.value === passwordConfirm.value;
});

// password 또는 confirm 변경 시 isPasswordValid를 자동으로 갱신
watch([passwordFormatOk, passwordsMatch], () => {
  isPasswordValid.value = passwordFormatOk.value && passwordsMatch.value;
}, { immediate: true });

// id이 바뀌면 기존 중복확인 상태 초기화
watch(id, () => {
  isIdChecked.value = false;
});

// (기존에는 수동 확인 버튼으로 처리했으나) 실시간으로 유효성/일치 여부를 표시하므로 별도 초기화가 필요 없습니다.

const checkId = async () => {
  console.log('중복확인 버튼 클릭됨');
  console.log('입력된 아이디:', id.value);
  
  // 아이디 형식 검사
  if (id.value.length < 4) {
    console.log('아이디 길이 부족:', id.value.length);
    alert('아이디는 4자 이상이어야 합니다.');
    isIdChecked.value = false;
    return;
  }
  
  isCheckingId.value = true;
  console.log('API 호출 시작...');

  // 중복확인 API 호출 (공통 API 사용)
  try {
    /**
     * POST /api/auth/check-id
     * @param String id - 중복 확인할 아이디 (백엔드에서 @RequestBody String으로 받으므로 JSON 형식 문자열로 전송)
     * @returns {number} 0: 사용 가능(중복 안됨), 1 이상: 이미 사용 중(중복됨)
     */
    // JSON.stringify로 감싸서 "아이디" 형태의 JSON 문자열로 전송
    const res = await authApi.checkId(JSON.stringify(id.value));
    console.log('API 응답:', res);
    
    const result = res?.data;
    console.log('중복확인 결과:', result);
    
    if (result === 0) {
      isIdChecked.value = true;
      alert('사용 가능한 아이디입니다.');
    } else {
      isIdChecked.value = false;
      alert('이미 사용 중인 아이디입니다. 다른 아이디를 입력해주세요.');
    }
  } catch (err) {
    console.error('아이디 중복확인 오류:', err);
    console.error('오류 상세:', err.response);
    isIdChecked.value = false;
    alert(err.response?.data?.message || '아이디 중복확인에 실패했습니다.');
  } finally {
    isCheckingId.value = false;
    console.log('API 호출 종료');
  }
};

// 확인 버튼을 제거하고 실시간으로 유효성/일치 여부를 계산합니다.


const handleRegister = async () => {
  // 필수 입력값 검증 추가
  if (!name.value || !email.value || !phone1.value || !phone2.value || !phone3.value || !birthDate.value) {
    alert('모든 정보를 입력해주세요.');
    return;
  }
  
  // 이메일 형식 검증
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) {
    alert('올바른 이메일 형식이 아닙니다.');
    return;
  }
  
  // 전화번호 분리 입력 검증 (3-4-4)
  if (!/^\d{3}$/.test(phone1.value) || !/^\d{4}$/.test(phone2.value) || !/^\d{4}$/.test(phone3.value)) {
    alert('전화번호는 3-4-4 숫자 형식으로 입력해야 합니다. 예: 010 1234 5678');
    return;
  }
  
  if (!isIdChecked.value) {
    alert('아이디 중복 확인이 필요합니다.');
    return;
  }
  if (!isPasswordValid.value) {
    alert('비밀번호가 유효하지 않거나 일치하지 않습니다. 조건을 확인해주세요.');
    return;
  }
  if (password.value !== passwordConfirm.value) {
    alert('비밀번호 확인이 일치하지 않습니다.');
    return;
  }

  // 백엔드 RegisterDTO 필드명에 맞게 매핑
  // 입력된 분리 전화번호를 합쳐서 서버의 RegisterDTO 필드(mobile 또는 mobile1/2/3)로 전송
  const composedPhone = `${phone1.value}-${phone2.value}-${phone3.value}`;

  const userData = {
    userName: name.value,
    email: email.value,
    // 서버는 RegisterDTO.getMobile()에서 mobile1/2/3을 합쳐 처리하므로
    // mobile 필드에 합친 문자열을 전송하거나 mobile1/mobile2/mobile3 각각을 전송해도 됩니다.
    mobile: composedPhone,
    mobile1: phone1.value,
    mobile2: phone2.value,
    mobile3: phone3.value,
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
      alert('회원가입이 완료되었습니다. 로그인해주세요.');
      router.push({ name: 'Login' });
    } else {
      throw new Error('회원가입에 실패했습니다.');
    }
  } catch (e) {
    console.error('회원가입 오류', e);
    alert(e.response?.data?.message || e.message || '회원가입 중 오류가 발생했습니다.');
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