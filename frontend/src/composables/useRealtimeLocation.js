// src/composables/useRealtimeLocation.js
import { onUnmounted } from 'vue';
import { useGeolocation } from './useGeolocation';
import { putUserLocation } from '@/api/user'; // user.js에서 임포트

export function useRealtimeLocation(interval = 10000) { // 10초 간격
    const { position, getLocation } = useGeolocation();
    let intervalId = null;

    const startTracking = () => {
        // 이미 인터벌이 설정되어 있으면 중복 실행 방지
        if (intervalId) return;

        intervalId = setInterval(async () => {
            try {
                // 위치 정보 획득
                const pos = await getLocation();

                // 획득한 위치를 FastAPI 백엔드로 전송
                // 이 호출에는 useApi.js에 의해 Firebase 토큰이 자동으로 포함됨
                await putUserLocation(pos.lat, pos.lng);
                console.log('Location updated to backend:', pos);
            } catch (e) {
                // API 통신 실패 또는 위치 정보 획득 실패 처리
                console.error('Failed to update location:', e);
                // 401 에러 발생 시 로그아웃 처리 등을 추가할 수 있음
            }
        }, interval);
    };

    const stopTracking = () => {
        clearInterval(intervalId);
        intervalId = null;
    };

    onUnmounted(stopTracking); // 컴포넌트 언마운트 시 추적 중지

    return { startTracking, stopTracking, position };
}