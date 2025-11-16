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

    <!-- Welcome Modal (shown after successful login) -->
    <div v-if="showWelcomeModal" class="modal-backdrop d-flex align-items-center justify-content-center">
      <div class="modal-card p-4 shadow-lg rounded">
        <h5 class="fw-bold mb-2">{{ welcomeName }}님 환영합니다!</h5>
        <p class="small text-muted mb-3">정상적으로 로그인되었습니다.</p>
        <div class="d-flex justify-content-end">
          <button class="btn btn-primary btn-sm" :style="{ backgroundColor: mainColor, borderColor: mainColor }" @click="confirmWelcome">확인</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { useAuthToken } from '@/composables/useAuthToken';
import { authApi } from '@/api/auth';
import { getTokenAndSave} from "@/utils/fcmUtils";

const router = useRouter();
const authStore = useAuthStore();
const { token: authToken, userNumber, userName, setToken, isAuthenticated } = useAuthToken();

const mainColor = '#0092BA';
const darkColor = '#0B1956';

const id = ref('');
const password = ref('');
const rememberMe = ref(false);

// Welcome modal state
const showWelcomeModal = ref(false);
const welcomeName = ref('');
let modalAutoTimer = null;

const confirmWelcome = () => {
  if (modalAutoTimer) { clearTimeout(modalAutoTimer); modalAutoTimer = null; }
  showWelcomeModal.value = false;
  router.replace({ name: 'Main' });
};

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

    // 응답 데이터 가져오기 (백엔드 응답 형식 변경: { data: {...}, token: '...' })
    const userData = result?.data; // {user_number, id, user_name, mobile}
    const token = result?.token;

    // 디버그: 응답과 토큰 로그
    console.log('Login API result:', result);
    console.log('Extracted userData:', userData);
    console.log('Extracted token:', token);

    if (!userData) {
      throw new Error('로그인 API 응답이 비어있습니다.');
    }

    // 토큰을 composable에 등록하면 composable에서 store 동기화까지 처리합니다.
    try {
      if (token) {
        setToken(token);
        // composable 상태 확인 로그
        console.log('After setToken - isAuthenticated:', isAuthenticated.value, 'userName:', userName.value, 'userNumber:', userNumber.value);
      } else {
        console.warn('No token received from login API — generating client token');
        // 백엔드가 토큰을 반환하지 않는 경우, 간단한 클라이언트 토큰을 생성하여 사용합니다.
        try {
          const createClientToken = (user) => {
            const header = {alg: 'none', typ: 'JWT'};
            const payload = {
              user_number: user.user_number || user.userNumber,
              id: user.id,
              user_name: user.user_name || user.userName,
              mobile: user.mobile,
              // 만료 시간을 짧게 두거나 필요에 따라 조정
              exp: Math.floor(Date.now() / 1000) + (60 * 60 * 24 * 7)
            };
            const base64url = (obj) => {
              const str = JSON.stringify(obj);
              return btoa(unescape(encodeURIComponent(str))).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
            };
            const token = `${base64url(header)}.${base64url(payload)}`;
            return token;
          };

          const clientToken = createClientToken(userData);
          setToken(clientToken);
          console.log('Generated client token and set it');
        } catch (e) {
          console.error('Failed to generate client token:', e);
        }
      }

    console.log('FCM에 전달할 userId (로그인 ID):', userData.id);
      // 3. 🚨 FCM 토큰 저장 로직
      // 🚨 수정: userData.userNumber -> userData.user_number
      getTokenAndSave(userData.id).catch(fcmError => {
      // FCM 실패 시에도 로그인 자체는 성공하도록 처리
      console.error('FCM 토큰 저장 중 오류 발생:', fcmError);
    });
    } catch (e) {
      console.error('로그인 후 token 처리 중 오류:', e);
    }

    // 성공 시 모달로 환영 메시지 표시 (확인 시 페이지 이동)
    welcomeName.value = userData.user_name || userData.userName || '';
    showWelcomeModal.value = true;
    // 자동 이동 안전장치: 4초 후에도 사용자가 확인하지 않으면 자동으로 닫고 이동
    modalAutoTimer = setTimeout(() => {
      if (showWelcomeModal.value) {
        showWelcomeModal.value = false;
        router.replace({ name: 'Main' });
      }
    }, 4000);

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

/* Modal styles */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  z-index: 1050;
}
.modal-card {
  background: #fff;
  max-width: 380px;
  width: 92%;
  border-radius: 10px;
  box-shadow: 0 10px 30px rgba(11,22,38,0.12);
}
.modal-card .btn { min-width: 72px; }
</style>