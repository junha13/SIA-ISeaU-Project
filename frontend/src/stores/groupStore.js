import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { groupApi } from '@/api/group';
import { useConfirmModal } from '@/utils/modalUtils';
import axios from 'axios';
import { getMessaging, onMessage, getToken } from 'firebase/messaging';
import { useAuthStore } from '@/stores/authStore'; 

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
const FCM_TOKEN_SAVE_URL = `${API_BASE_URL}/api/fcm/save-token`;
const PENDING_URL = `${API_BASE_URL}/api/groups/invitations/pending`;
const SERVICE_WORKER_URL = '/firebase-messaging-sw.js';

export const useGroupStore = defineStore('group', () => {
  const { showConfirmModal } = useConfirmModal();
  const authStore = useAuthStore();

  // --- State ---
  const myGroupList = ref([]);
  const activeGroupId = ref(null);
  const activeGroupLocations = ref([]);
  const receivedInvitation = ref(null);

  // --- Actions ---

  const fetchGroups = async () => {
    try {
      const response = await groupApi.fetchGroupList();
      myGroupList.value = response.data.result || [];

      if (!activeGroupId.value && myGroupList.value.length > 0) {
        activeGroupId.value = myGroupList.value[0].id;
      } else if (myGroupList.value.length === 0) {
        activeGroupId.value = null;
      }
    } catch (error) {
      console.error('그룹 목록 조회 실패:', error);
    }
  };

  const setActiveGroup = (groupId) => {
    activeGroupId.value = groupId;
  };

  const fetchLocations = async () => {
    if (!activeGroupId.value) return;
    try {
      const response = await groupApi.fetchGroupLocations({ groupId: activeGroupId.value });
      activeGroupLocations.value = response.data.result || [];
    } catch (error) {
      console.error('그룹 위치 정보 조회 실패:', error);
      activeGroupLocations.value = [];
    }
  };

  const closeModal = () => {
    receivedInvitation.value = null;
  };

  /** 초대장 데이터 설정 (매핑 강화) */
  const setInvitation = async (data) => { // async 추가
    console.group('📦 [Store] setInvitation 실행 (데이터 수신)');
    console.log('수신 데이터:', data);

    if (!data) {
        console.warn('❌ 데이터가 비어있습니다.');
        console.groupEnd();
        return;
    }

    // 1. ID 추출 (소문자/대문자/카멜케이스 모두 확인)
    const rawId = data.id || data.invitationId || data.ID || data.invitationid;

    // 🚨 [안전장치] ID가 없거나 '0'이면 API를 찔러서 진짜 데이터를 가져옵니다.
    // (백엔드에서 ID를 못 찾아서 0을 보냈거나, 데이터가 유실된 경우 대비)
    if (!rawId || String(rawId) === '0') {
        console.warn('⚠️ 초대장 ID가 0이거나 없습니다. 서버에서 최신 목록을 다시 가져옵니다...');
        console.groupEnd();
        await checkPendingInvitations(); 
        return;
    }

    // 2. 나머지 데이터 매핑 (소문자 키 포함)
    const name = data.inviterName || data.inviter_name || data.invitername || '알 수 없음';
    const phone = data.inviterPhone || data.inviter_phone || data.inviterphone || '';
    
    let gId = data.groupId || data.group_id || data.groupNumber || data.groupnumber;
    if (gId) gId = Number(gId);

    // 3. 상태 업데이트
    receivedInvitation.value = {
        id: rawId,
        inviterName: name,
        inviterPhone: phone,
        groupId: gId
    };
    
    console.log('✅ 최종 매핑 결과:', receivedInvitation.value);
    console.groupEnd();
  };

  const checkPendingInvitations = async () => {
    try {
      const response = await axios.get(PENDING_URL, { withCredentials: true });
      const responseData = response.data.data || response.data;
      const invitationList = responseData.invitations || responseData.result || [];

      if (invitationList.length > 0) {
        console.log("API 조회: 대기 중 초대 발견:", invitationList[0]);
        // API 데이터도 setInvitation을 통해 안전하게 매핑
        setInvitation(invitationList[0]);
      }
    } catch (error) {
      console.error("대기 중 초대 확인 실패:", error);
    }
  };

  const setupFCMListener = () => {
    console.log('[FCM] Listener setup started.');
    const messaging = getMessaging();

    return onMessage(messaging, (payload) => {
      console.log('🚨 [Foreground] FCM Message:', payload);
      if (payload.data && payload.data.type === 'GROUP_INVITE_PENDING') {
        setInvitation(payload.data);
      }
    });
  };

  const initFCM = async () => {
      const currentUserNumber = authStore.userInfo?.userNumber;

      if (!currentUserNumber) {
          console.warn('🚫 [FCM] 로그인 정보 없음(authStore). 토큰 발급 대기.');
          return;
      }

      try {
          const registration = await navigator.serviceWorker.register(SERVICE_WORKER_URL);
          console.log('[FCM INIT] Service Worker registered.');

          const messaging = getMessaging();
          
          // VAPID Key
          const currentToken = await getToken(messaging, { 
              vapidKey: "BOrTN03wDt5R3KnzYIclpfQrQJKKyqMM_OkQLLjaHwvLN8H0jEjjDooo4KFH-bj8HCCta_43a4xuOllOZ-aoTRw",
              serviceWorkerRegistration: registration 
          });

          if (currentToken) {
              console.log(`[FCM] Token obtained for User ${currentUserNumber}`);
             await axios.post(FCM_TOKEN_SAVE_URL, { 
    // 🚨 [수정] userId 대신 userNumber 사용 (DTO 필드명 일치)
    userNumber: currentUserNumber, // Integer 타입이므로 String() 제거
    token: currentToken
}, { withCredentials: true });

              console.log('[FCM] 토큰 서버 저장 완료');
          } else {
              console.warn('[FCM] No registration token available.');
          }
          
          setupFCMListener();
      } catch (error) {
          console.error('🚨 [FCM INIT FATAL] Failed:', error);
      }
  };

  const acceptInvitation = async (invitation) => {
    const target = invitation || receivedInvitation.value;
    const payload = target.id ? { invitationId: target.id } : { groupId: target.groupId };

    console.log(`[수락 시작]`, payload);

    try {
      // 백엔드 경로: /location/accept
      const response = await axios.post(`${API_BASE_URL}/api/groups/location/accept`, payload, { withCredentials: true });

      if (response.data?.success || response.data?.data?.success) {
        console.log("[수락 성공]");
        showConfirmModal({
          title: '초대 수락',
          message: `${target.inviterName} 님의 초대를 수락했습니다.`,
          type: 'success',
          autoHide: true,
        });
        closeModal();
        await fetchGroups(); 
      } else {
        throw new Error(response.data?.message || '실패');
      }
    } catch (error) {
      console.error("수락 실패:", error);
      showConfirmModal({ title: '오류', message: '초대 수락에 실패했습니다.', type: 'error' });
      closeModal();
    }
  };

  const rejectInvitation = async (invitation) => {
    const target = invitation || receivedInvitation.value;
    const payload = target.id ? { invitationId: target.id } : { groupId: target.groupId };

    console.log(`[거절 시작]`, payload);

    try {
      // 백엔드 경로: /location/reject
      const response = await axios.post(`${API_BASE_URL}/api/groups/location/reject`, payload, { withCredentials: true });

      if (response.data?.success || response.data?.data?.success) {
        console.log("[거절 성공]");
        showConfirmModal({
          title: '초대 거절',
          message: '초대를 거절했습니다.',
          type: 'info',
          autoHide: true,
        });
        closeModal();
      } else {
        throw new Error(response.data?.message || '실패');
      }
    } catch (error) {
      console.error("거절 실패:", error);
      showConfirmModal({ title: '오류', message: '초대 거절 처리에 실패했습니다.', type: 'error' });
      closeModal();
    }
  };

  return {
    myGroupList,
    activeGroupId,
    activeGroupLocations,
    receivedInvitation,

    fetchGroups,
    setActiveGroup,
    fetchLocations,
    acceptInvitation,
    rejectInvitation,
    checkPendingInvitations,
    setupFCMListener,
    initFCM,
    setInvitation,
    closeModal,

    getActiveGroupLocations: computed(() => activeGroupLocations.value),
    getActiveGroupId: computed(() => activeGroupId.value),
    getMyGroupList: computed(() => myGroupList.value),
  };
});