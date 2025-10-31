<template>
  <div class="p-10 max-w-lg mx-auto bg-white shadow-2xl rounded-xl mt-16 border border-gray-100">
    <h1 class="text-3xl font-extrabold text-gray-900 mb-2">FCM PWA 알림 테스트 🔔</h1>
    <p class="text-md text-gray-500 mb-8">
      클라이언트 환경 설정 및 백엔드 토큰 저장 테스트를 수행합니다.
    </p>

    <div class="space-y-6">
      <button
          @click="requestPermissionAndGetToken"
          class="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-3 px-4 rounded-xl transition duration-300 transform hover:scale-[1.01] shadow-lg shadow-indigo-200"
      >
        ✨ 알림 권한 요청 및 토큰 발급/저장
      </button>

      <div v-if="notificationStatus" :class="statusClass" class="p-4 rounded-xl text-sm font-medium border">
        <p class="font-bold">{{ isError ? '❌ 오류 발생' : '✅ 현재 상태' }}</p>
        <p class="mt-1">{{ notificationStatus }}</p>
      </div>

      <div v-if="backendError" class="bg-red-50 p-4 rounded-xl border border-red-300 shadow-inner">
        <p class="font-semibold text-red-800 mb-1">백엔드 응답 오류 상세:</p>
        <code class="text-red-700 text-xs whitespace-pre-wrap">{{ backendError }}</code>
        <p class="text-xs mt-2 text-red-600">
          🚨 **DB 저장 문제**일 가능성이 높습니다. Spring Boot 콘솔의 **`DataAccessException`** 로그를 확인하세요.
        </p>
      </div>

      <div v-if="currentToken" class="bg-gray-50 p-4 rounded-xl break-all text-xs border border-gray-200">
        <p class="font-semibold text-gray-700 mb-2 flex justify-between items-center">
          FCM 등록 토큰:
          <span class="text-indigo-600 text-xs">({{ currentToken.length }}자)</span>
        </p>
        <code class="text-gray-800 select-all font-mono leading-relaxed block">{{ currentToken }}</code>
        <p class="mt-3 text-gray-500 text-sm italic">
          💡 이 토큰으로 DB 레코드가 **성공적으로 업데이트**되었는지 확인하세요.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
//import { getToken, onMessage } from 'firebase/messaging';
import { Workbox } from 'workbox-window';
// 🚨 다음 두 파일은 프로젝트에 맞게 경로를 수정해야 합니다.
import { messaging } from '@/firebase.js'; // Firebase 인스턴스 (초기화)
import axios from 'axios'; // npm install axios

// --- 상태 관리 ---
const currentToken = ref(null);
const notificationStatus = ref('알림 권한 요청 대기 중');
const statusClass = ref('bg-gray-100 text-gray-700 border border-gray-300');
const backendError = ref(null);
const isError = ref(false);

/**
 * UI 상태 메시지 및 스타일 업데이트
 * @param {string} message - 표시할 메시지
 * @param {any} errorData - 백엔드에서 받은 상세 오류 데이터 (null이면 성공)
 */
const setStatus = (message, errorData = null) => {
  notificationStatus.value = message;
  isError.value = !!errorData;

  if (errorData) {
    statusClass.value = 'bg-red-100 text-red-700 border border-red-300 shadow-md';
    if (typeof errorData === 'object' && errorData !== null) {
      backendError.value = JSON.stringify(errorData, null, 2);
    } else {
      backendError.value = String(errorData);
    }
  } else {
    // 성공 시 에러 상태 및 데이터 초기화
    statusClass.value = 'bg-green-100 text-green-700 border border-green-300 shadow-md';
    backendError.value = null;
  }
};


// --- 1. 토큰 서버 전송 함수 ---
const saveTokenToServer = async (token) => {
  // 🚨 Spring Boot의 엔드포인트와 일치해야 합니다. (프록시 설정 필요)
  const SERVER_URL = '/api/fcm/save-token';
  const CURRENT_USER_ID = 'GUEST_USER'; // FcmController.java에서 사용하는 테스트 ID와 일치

  try {
    setStatus('서버로 FCM 토큰을 저장하는 중...');

    await axios.post(SERVER_URL, {
      token: token,
      userId: CURRENT_USER_ID
    });

    setStatus('FCM 토큰 발급 및 서버 저장 성공!', null);
    // 백엔드 콘솔에 '✅ FCM Token saved/updated...' 로그가 출력되었는지 확인하세요.

  } catch (error) {
    let errorDetail = '알 수 없는 네트워크 오류.';
    if (error.response) {
      errorDetail = error.response.data || `HTTP Status ${error.response.status} 응답`;
    } else if (error.message) {
      errorDetail = error.message;
    }

    // 상태 업데이트 함수에 상세 오류 데이터 전달
    setStatus('FCM 토큰 서버 저장 실패! 백엔드 DB 오류일 가능성 높음.', errorDetail);
    console.error('Full Axios Error Object:', error);
  }
};


// --- 2. 권한 요청 및 토큰 발급 함수 ---
const requestPermissionAndGetToken = async () => {
  try {
    const permission = await Notification.requestPermission();

    if (permission === 'granted') {
      setStatus('알림 권한이 허용되었습니다. 토큰을 발급합니다.');

      let registration = await navigator.serviceWorker.getRegistration('/');

      // 📌 등록되지 않았다면 강제로 Workbox를 사용하여 등록을 시도합니다.
      if (!registration) {
        setStatus('Service Worker 미등록. 등록을 시작합니다...');

        // Workbox-window 인스턴스 생성 (filename: 'firebase-messaging-sw.js' 사용)
        const wb = new Workbox('/firebase-messaging-sw.js');

        // Service Worker 등록
        registration = await wb.register();
        setStatus('Service Worker 등록 완료. 토큰 발급을 계속합니다.');
      }
      // 🚨 [수정된 부분 끝] 🚨

      if (!registration) {
        setStatus('Service Worker 등록 실패. 브라우저/PWA 설정을 확인하세요.', 'Service Worker Not Found');
        return;
      }

      // 🚨 환경 변수 설정 필요 (vite.config.js 또는 .env 파일)
      const VAPID_KEY = import.meta.env.VITE_FIREBASE_VAPID_KEY;

      // currentToken.value = await getToken(messaging, {
      //   vapidKey: VAPID_KEY,
      //   serviceWorkerRegistration: registration
      // });

      if (currentToken.value) {
        await saveTokenToServer(currentToken.value);
      } else {
        setStatus('토큰 발급 실패. VAPID Key 또는 Firebase 설정을 확인하세요.', 'Token is null');
      }
    } else {
      setStatus('알림 권한이 거부되었습니다. 메시지를 받을 수 없습니다.', 'Permission Denied');
    }
  } catch (err) {
    setStatus(`토큰 발급 중 치명적인 오류 발생: ${err.message}`, err.message);
    console.error('An error occurred while retrieving token. ', err);
  }
};


// --- 3. 포그라운드 메시지 처리 ---
const setupForegroundMessaging = () => {
  onMessage(messaging, (payload) => {
    // 포그라운드 시 웹 알림 API를 사용하여 수동으로 알림 표시
    new Notification(payload.notification?.title || '새 알림', {
      body: payload.notification?.body || '내용이 도착했습니다.',
      icon: '/pwa-512x512.png'
    });
    setStatus(`새 알림 수신: ${payload.notification?.title || '메시지'}.`, null);
  });
};


// --- 4. 초기화 ---
onMounted(() => {
  setupForegroundMessaging();
  requestPermissionAndGetToken();
});
</script>