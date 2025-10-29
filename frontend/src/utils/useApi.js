// useApi.js (개선된 구조 - Post, Put, Get 등 범용성 확보)
import { ref } from 'vue';
import axios from 'axios';
// Firebase Auth 객체를 가져오는 함수 (Firebase 초기화 후)
//import { getAuth } from 'firebase/auth';

// API 인스턴스 (BaseURL 설정)
// VITE_API_BASE_URL이 설정되지 않았다면 '/api'로 기본값 지정하여 Vite 프록시를 사용
const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api'
});

// 요청 인터셉터 추가: 모든 요청에 Firebase ID 토큰을 추가
//api.interceptors.request.use(async (config) => {
    // 1. Firebase Auth 객체 가져오기
  //  const auth = getAuth();
    // 2. 현재 사용자 확인
   // const user = auth.currentUser;

   // if (user) {
        // 3. ID 토큰 비동기적으로 가져오기
        //const token = await user.getIdToken();
        // 4. Authorization 헤더에 Bearer 토큰 형식으로 추가
        //config.headers.Authorization = `Bearer ${token}`;
   // }
   // return config;
//}, (error) => {
  //  return Promise.reject(error);
//});


/**
 * 범용적인 API 요청 컴포저블
 * @param {string} method - HTTP 메서드 ('get', 'post', 'put', 'delete')
 * @param {string} url - 요청 URL (baseURL 이후 경로)
 * @returns { function(payload: any): Promise<void> } - 데이터를 페치하는 함수
 */
export function useApi(method, url) { // 함수 이름도 useAxios 대신 useApi로 변경할 수 있습니다.
    const data = ref(null);
    const error = ref(null);
    const loading = ref(false); // 초기값은 false로 설정하여 명시적으로 호출될 때만 true로 변경

    // 실제 API 요청을 수행하는 함수를 반환
    const execute = async (payload = null) => { // 페이로드는 POST/PUT 시 데이터로 사용됨
        loading.value = true;
        error.value = null; // 요청 전 에러 초기화

        try {
            console.log('API 요청:', { method, url, payload }); // 디버깅용 로그
            
            let response;
            if (method.toLowerCase() === 'get') {
                // GET 요청은 payload를 params로 사용
                response = await api.get(url, { params: payload });
            } else {
                // POST, PUT 등은 payload를 요청 본문(body)으로 사용
                // String을 직접 보낼 경우 Content-Type을 명시
                const config = {};
                if (typeof payload === 'string') {
                    config.headers = { 'Content-Type': 'application/json' };
                }
                response = await api[method.toLowerCase()](url, payload, config);
            }
            
            console.log('API 응답:', response.data); // 디버깅용 로그
            data.value = response.data;
            return response.data; // 데이터를 리턴하여 액션에서 사용할 수 있게 함
        } catch (err) {
            console.error('API 에러:', err); // 디버깅용 로그
            error.value = err;
            throw err; // 에러를 다시 던져서 스토어에서 처리할 수 있게 함
        } finally {
            loading.value = false;
        }
    };

    return { data, error, loading, execute };
}