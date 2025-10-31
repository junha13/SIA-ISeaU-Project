<template>
  <GroupInviteConfirmModal
    :is-visible="groupStore.receivedInvitation != null"
    :invitationData="groupStore.receivedInvitation"
  />
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue';
import { RouterView } from 'vue-router';
import { useGroupStore } from '@/stores/groupStore';
import GroupInviteConfirmModal from '@/components/GroupInviteConfirmModal.vue';

const groupStore = useGroupStore();

// 🚨 [삭제]
// handleInvitationAccept, handleInvitationReject 함수들은
// 이제 모달이 직접 처리하므로 App.vue에서 필요 없습니다.
/*
const handleInvitationAccept = () => { ... };
const handleInvitationReject = () => { ... };
*/

onMounted(() => {
  console.log("App.vue 마운트됨. 대기 중인 초대 확인 시작...");
  groupStore.checkPendingInvitations();
});
</script>