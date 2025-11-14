import { ref, computed } from 'vue';
import { useAuthStore } from '@/stores/authStore';

// JWT 디코드(간단한 base64url 디코딩)
function decodeJwt(token) {
  try {
    const payload = token.split('.')[1];
    let b64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    while (b64.length % 4) b64 += '=';
    const json = decodeURIComponent(atob(b64).split('').map(c => {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(json);
  } catch (e) {
    return null;
  }
}

export function useAuthToken() {
  const store = useAuthStore();
  const token = ref(localStorage.getItem('token'));
  const claims = ref(token.value ? decodeJwt(token.value) : null);

  const isAuthenticated = computed(() => !!token.value && !!claims.value);
  const userName = computed(() => claims.value?.user_name || claims.value?.name || store.userInfo.userName || null);
  const userNumber = computed(() => claims.value?.user_number || claims.value?.userNumber || store.userInfo.userNumber || null);
  const userMobile = computed(() => claims.value?.mobile || store.userInfo.mobile || null);

  const setToken = (t) => {
    try {
      if (t) {
        localStorage.setItem('token', t);
        token.value = t;
        claims.value = decodeJwt(t);
      } else {
        localStorage.removeItem('token');
        token.value = null;
        claims.value = null;
      }

      // authStore 동기화: 토큰의 클레임을 우선으로 store 채움
      if (claims.value) {
        store.isAuthenticated = true;
        store.userInfo.userNumber = claims.value.user_number || claims.value.userNumber || store.userInfo.userNumber;
        store.userInfo.id = claims.value.id || claims.value.sub || store.userInfo.id;
        store.userInfo.userName = claims.value.user_name || claims.value.name || store.userInfo.userName;
        store.userInfo.mobile = claims.value.mobile || store.userInfo.mobile;
      } else if (!t) {
        // 토큰이 지워지면 스토어 초기화(로그아웃과 유사)
        store.isAuthenticated = false;
        store.userInfo.userNumber = null;
        store.userInfo.id = null;
        store.userInfo.userName = null;
        store.userInfo.mobile = null;
      }

      // localStorage의 'auth' 페이로드도 갱신하여 스토어 복원에 사용되게 합니다.
      try {
        const authPayload = {
          isAuthenticated: !!claims.value,
          userInfo: store.userInfo
        };
        localStorage.setItem('auth', JSON.stringify(authPayload));
      } catch (e) {
        /* ignore */
      }

      // 다른 탭으로 변경 사항 전파는 localStorage.setItem로 이미 이루어집니다.
    } catch (e) {
      console.error('Failed to set token', e);
    }
  };

  const clear = () => setToken(null);

  // 탭 간 동기화: storage 이벤트로 token 변경 감지
  window.addEventListener('storage', (e) => {
    if (e.key === 'token') {
      token.value = e.newValue;
      claims.value = e.newValue ? decodeJwt(e.newValue) : null;
      // 스토어 업데이트
      if (claims.value) {
        store.isAuthenticated = true;
        store.userInfo.userNumber = claims.value.user_number || claims.value.userNumber || store.userInfo.userNumber;
        store.userInfo.id = claims.value.id || claims.value.sub || store.userInfo.id;
        store.userInfo.userName = claims.value.user_name || claims.value.name || store.userInfo.userName;
        store.userInfo.mobile = claims.value.mobile || store.userInfo.mobile;
      } else {
        store.isAuthenticated = false;
        store.userInfo.userNumber = null;
        store.userInfo.id = null;
        store.userInfo.userName = null;
        store.userInfo.mobile = null;
      }

      if (typeof store.saveToSession === 'function') {
        try { store.saveToSession(); } catch (e) { /* ignore */ }
      }
    }
  });

  const isTokenExpired = () => {
    if (!claims.value) return true;
    const exp = claims.value.exp; // exp: seconds since epoch
    if (!exp) return false; // 만약 exp가 없으면 만료 체크하지 않음
    return Date.now() / 1000 >= exp;
  };

  return {
    token,
    claims,
    isAuthenticated,
    userName,
    userNumber,
    userMobile,
    setToken,
    clear,
    isTokenExpired,
  };
}
