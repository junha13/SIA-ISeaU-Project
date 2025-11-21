<template>

  <Transition name="fade">
    <Splash v-if="showSplash" />
  </Transition>

  <GroupInviteConfirmModal
    v-if="groupStore.receivedInvitation"
    :key="groupStore.receivedInvitation.id || Date.now()" 
    :is-visible="true" 
    :invitationData="groupStore.receivedInvitation"
  />

  <router-view />
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { RouterView } from 'vue-router'
import { useGroupStore } from '@/stores/groupStore'
import { useAuthStore } from '@/stores/authStore' // 🚨 AuthStore 추가
import GroupInviteConfirmModal from '@/components/GroupInviteConfirmModal.vue'
import Splash from '@/components/Splash.vue'
import axios from 'axios'

const groupStore = useGroupStore()
const authStore = useAuthStore() // 🚨 AuthStore 사용
const showSplash = ref(true)
let fcmChannel = null;

const testFCM = async () => {
  // 🚨 현재 로그인한 내 ID를 동적으로 가져옴
  const myId = authStore.userInfo?.userNumber; // 또는 userStore.userNumber

  if (!myId) {
    alert('로그인이 필요합니다! (User ID 없음)');
    return;
  }

  console.log(`🧪 FCM 테스트 요청 보냄 (Target: ${myId})...`);
  
  try {
    const API_BASE = import.meta.env.VITE_API_BASE_URL;
    // 🚨 내 ID로 요청 전송
    await axios.post(`${API_BASE}/api/fcm/send-invite-test?userId=${myId}&inviterName=테스터&groupId=999`, {}, {
      withCredentials: true
    });
    
    alert(`User ${myId}에게 알림을 보냈습니다!`);
  } catch (e) {
    alert('테스트 실패: ' + e.message);
    console.error(e);
  }
};

const handleFcmMessage = (data) => {
  if (data && data.type === 'FCM_BACKGROUND_MESSAGE') {
    console.log('⚡ [App.vue] 메시지 수신:', data.payload);
    const payload = data.payload;
    if (payload.data && payload.data.type === 'GROUP_INVITE_PENDING') {
        console.log('🔔 모달 열기 시도');
        groupStore.setInvitation({
          inviterName: payload.data.inviter_name,
          groupId: payload.data.group_id,
          inviterPhone: payload.data.inviter_phone,
          id: payload.data.id
        });
    }
  }
}

onMounted(async () => {
  setTimeout(() => { showSplash.value = false }, 1500)
  console.log('🚀 [App.vue] 시작')

  await groupStore.initFCM();

  // BroadcastChannel
  try {
    fcmChannel = new BroadcastChannel('fcm_channel');
    fcmChannel.onmessage = (event) => {
      console.log('📡 [Broadcast] 수신:', event.data);
      handleFcmMessage(event.data);
    };
  } catch (e) { console.warn('Broadcast 미지원'); }

  // Service Worker
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.ready.then((registration) => {
      console.log('✅ SW Ready');
      navigator.serviceWorker.addEventListener('message', (event) => {
        console.log('💤 [PostMessage] 수신:', event.data);
        handleFcmMessage(event.data);
      });
    });
  }

  await groupStore.checkPendingInvitations();
})

onUnmounted(() => {
  if (fcmChannel) fcmChannel.close();
})
</script>

<style scoped>
.fade-enter-from, .fade-leave-to { opacity: 0; transform: scale(0.995); }
.fade-enter-active, .fade-leave-active { transition: all 0.2s ease; }
</style>