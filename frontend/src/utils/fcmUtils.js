import { getToken } from 'firebase/messaging';
import { Workbox } from 'workbox-window';
import { messaging } from '@/firebase.js'; 
import axios from 'axios';

// --- 헬퍼 함수: 서버에 토큰 저장 ---
const saveTokenToServer = async (token, userNumber) => {
    const SERVER_URL = '/api/fcm/save-token'; // 토큰 저장 엔드포인트
    
    try {
        console.log(`[FCM UTIL] 서버 저장 시도: userNumber=${userNumber}, Token=${token.substring(0, 10)}...`);
        await axios.post(SERVER_URL, {
            token: token,
            userNumber: userNumber
        });
        console.log(`[FCM UTIL] 토큰 서버 저장 성공. (User: ${userNumber})`);
    } catch (error) {
        // 토큰 저장이 실패해도 로그인 흐름은 막지 않습니다.
        console.error(`[FCM UTIL] 토큰 서버 저장 실패 (User: ${userNumber}). DB 또는 네트워크 문제 확인 필요.`, error);
        throw new Error('FCM 토큰 서버 저장 실패'); 
    }
};

// --- 메인 함수: 권한 요청, 토큰 발급 및 저장 ---
/**
 * 알림 권한을 요청하고, 토큰을 발급받아 서버에 저장합니다.
 * @param {string} loginId - 로그인한 사용자의 고유 loginId (로그인 loginId)
 */
export const getTokenAndSave = async (loginId) => {
    if (!loginId) {
        console.error('[FCM UTIL] 오류: FCM 토큰 저장을 위해 유효한 loginId가 필요합니다.');
        return;
    }

    try {
        // 1. 알림 권한 요청
        const permission = await Notification.requestPermission();

        if (permission !== 'granted') {
            console.log('[FCM UTIL] 알림 권한이 거부되었습니다.');
            return;
        }

        // 2. Service Worker 등록 확인 및 등록
        let registration = await navigator.serviceWorker.getRegistration('/');
        if (!registration) {
            const wb = new Workbox('/firebase-messaging-sw.js'); // Service Worker 파일 경로
            registration = await wb.register();
            console.log('[FCM UTIL] Service Worker 등록 완료.');
        }

        if (!registration) {
            console.error('[FCM UTIL] Service Worker 등록 실패. 토큰 발급 불가.');
            return;
        }

        // 3. 토큰 발급
        const VAPID_KEY = import.meta.env.VITE_FIREBASE_VAPID_KEY;
        const currentToken = await getToken(messaging, {
            vapidKey: VAPID_KEY,
            serviceWorkerRegistration: registration
        });

        if (currentToken) {
            // 4. 서버에 토큰 저장
            await saveTokenToServer(currentToken, loginId);
        } else {
            console.error('[FCM UTIL] 토큰 발급 실패. VAPID Key 또는 Firebase 설정을 확인하세요.');
        }

    } catch (err) {
        console.error('[FCM UTIL] 토큰 발급/저장 중 치명적인 오류 발생:', err);
    }
};