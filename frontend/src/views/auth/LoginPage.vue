<template>
  <div class="auth-page d-flex flex-column align-items-center justify-content-center min-vh-100 p-4"
       :style="{ backgroundColor: 'white' }">
    <!-- Logo -->
    <h1 class="logo fw-bolder mb-5" :style="{ color: darkColor, fontSize: '2.5rem' }">I Sea U</h1>

    <!-- Login Form Container -->
    <div class="auth-card p-4 rounded-3 shadow-lg w-100"
         style="max-width: 400px; background-color: white; border: 1px solid #eee;">

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
import { useAuthStore } from '@/stores/authStore';
import { authApi } from '@/api/auth';
import { getTokenAndSave} from "@/utils/fcmUtils";

const router = useRouter();
const authStore = useAuthStore();

const mainColor = '#0092BA';
const darkColor = '#0B1956';

const id = ref('');
const password = ref('');
const rememberMe = ref(false);

const handleLogin = async () => {
  if (!id.value || !password.value) {
    alert('아이디와 비밀번호를 모두 입력해주세요.');
    return;
  }

  try {
    // 1. 로그인 API 호출
    const result = await authApi.login({
      id: id.value,
      password: password.value
    });

    const userData = result?.data; // {user_number, id, user_name, mobile}

    console.log('Login API result:', result);
    console.log('Extracted userData:', userData);

    if (!userData || !userData.id) {
      throw new Error('로그인 API 응답이 비어있거나 사용자 ID가 없습니다.');
    }

    // 3. 🔑 FCM 토큰 저장 - userData.id
    const loginId = userData.id;
    console.log('FCM에 전달할 ID (로그인 ID):', loginId);

    getTokenAndSave(loginId).catch(fcmError => {
      console.error('FCM 토큰 저장 중 오류 발생:', fcmError);
    });

    // 4. ✅ Android WebView 환경에서 ID 동기화 (user_number 대신 'id' 사용)
    if (window.AndroidBridge && typeof window.AndroidBridge.setUserId === 'function') {
      try {
        if (loginId) { // 유효한 ID인지 확인
          // 백엔드에서 받은 ID를 네이티브 Java 함수로 전달
          window.AndroidBridge.setUserId(loginId);
          console.log('AndroidBridge.setUserId 호출 성공:', loginId);
        } else {
          console.error('⚠️ 사용자 ID 값이 유효하지 않아 setUserId 호출을 건너뜜니다.', userData);
        }
      } catch (bridgeError) {
        console.error('AndroidBridge.setUserId 호출 중 오류:', bridgeError);
      }
    }

    // 5. 성공 시 페이지 이동
    alert(`${userData.user_name}님 환영합니다!`);
    router.replace({name: 'Main'});

  } catch (e) {
    // 에러 처리
    let errorMessage = '알 수 없는 오류가 발생했습니다.';
    if (e.response?.data?.message) {
      errorMessage = e.response.data.message;
    } else if (e.message) {
      errorMessage = e.message;
    }
    alert(`로그인 실패: ${errorMessage}`);
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