// useApi.js (개선된 구조 - Post, Put, Get 등 범용성 확보)
import { ref } from 'vue';
import axios from 'axios';

// API 인스턴스 (BaseURL 설정)
const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL
});
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
            let response;
            if (method.toLowerCase() === 'get') {
                // GET 요청은 payload를 params로 사용
                response = await api.get(url, { params: payload });
            } else {
                // POST, PUT 등은 payload를 요청 본문(body)으로 사용
                response = await api[method.toLowerCase()](url, payload);
            }
            data.value = response.data;
            return response.data; // 데이터를 리턴하여 액션에서 사용할 수 있게 함
        } catch (err) {
            error.value = err;
            throw err; // 에러를 다시 던져서 스토어에서 처리할 수 있게 함
        } finally {
            loading.value = false;
        }
    };

    return { data, error, loading, execute };
}