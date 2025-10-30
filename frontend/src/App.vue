<template>
  <GroupInviteConfirmModal 
    :is-visible="!!invitationToShow"  
    :invitation-data="invitationToShow"
    @confirm="handleInvitationAccept"
    @cancel="handleInvitationReject"
  />
  
  <router-view />
</template>
<script setup>
import { computed, onMounted } from 'vue'; // 👈 onMounted 임포트
import { RouterView } from 'vue-router';
import GroupInviteConfirmModal from '@/components/GroupInviteConfirmModal.vue';
import { useGroupStore } from '@/stores/groupStore';
import { storeToRefs } from 'pinia';

const groupStore = useGroupStore();

// 1. 스토어의 receivedInvitation 상태를 가져옵니다.
const { receivedInvitation: invitationToShow } = storeToRefs(groupStore);

// 🚨 [필수 추가] App.vue가 마운트될 때 (앱 시작 시), 
//    Pinia 스토어의 checkPendingInvitations 액션을 호출합니다.
onMounted(() => {
    // (실제로는 로그인 상태가 확인된 후에 호출하는 것이 좋습니다)
    console.log("App.vue 마운트됨. 대기 중인 초대 확인 시작...");
    groupStore.checkPendingInvitations();
});

// 2. 모달에서 @confirm 이벤트가 오면 실행될 함수
const handleInvitationAccept = () => {
	console.log("App.vue: @confirm 이벤트 수신. 초대 수락 처리...");
	if (invitationToShow.value) {
		groupStore.acceptInvitation(invitationToShow.value);
	}
};

// 3. 모달에서 @cancel 이벤트가 오면 실행될 함수
const handleInvitationReject = () => {
	console.log("App.vue: @cancel 이벤트 수신. 초대 거절 처리...");
	if (invitationToShow.value) {
		groupStore.rejectInvitation(invitationToShow.value);
	}
};
</script>