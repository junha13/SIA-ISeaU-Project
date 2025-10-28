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

  /**
   * 로그인 처리
   * POST /api/auth/login
   * @param {string} id - 로그인 아이디
   * @param {string} password - 비밀번호
   * @returns {Object} userData - { userNumber, id, userName, mobile }
   * @throws {Error} 로그인 실패 시
   */
  try {
    // 공통 API 컴포저블 사용 (VITE_API_BASE_URL 적용)
    const result = await authApi.login({
      id: id.value,
      password: password.value
    });

    // 응답 데이터 가져오기 (백엔드 응답 형식: { data: {...} })
    const userData = result?.data; // {userNumber, id, userName, mobile}

    if (!userData) {
      throw new Error('로그인 API 응답이 비어있습니다.');
    }

    // authStore에 로그인한 사용자 정보 저장
    authStore.isAuthenticated = true;
    authStore.userInfo.userNumber = userData.user_number;
    authStore.userInfo.id = userData.id;
    authStore.userInfo.userName = userData.user_name;
    authStore.userInfo.mobile = userData.mobile || null;

    console.log('로그인 후 저장된 정보:', authStore.userInfo);


    console.log('FCM에 전달할 userNumber:', userData.user_number);

    // 3. 🚨 FCM 토큰 저장 로직
    // 🚨 수정: userData.userNumber -> userData.user_number
    getTokenAndSave(userData.user_number).catch(fcmError => {
      // FCM 실패 시에도 로그인 자체는 성공하도록 처리
      console.error('FCM 토큰 저장 중 오류 발생:', fcmError);
    });


    // 성공 시 알림 표시 후 페이지 이동
    alert(`${userData.user_name}님 환영합니다!`);
    router.replace({ name: 'Main' });

  } catch (e) {
    // 에러 처리
    let errorMessage = '알 수 없는 오류가 발생했습니다.';

    // 백엔드에서 보낸 에러 메시지 (401 등)
    if (e.response?.data?.message) {
      errorMessage = e.response.data.message;
    }
    // 네트워크 오류 등
    else if (e.message) {
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