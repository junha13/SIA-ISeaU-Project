<template>
    <Transition name="fade">
      <Splash v-if="showSplash"></Splash>
    </Transition>

    <GroupInviteConfirmModal
      :is-visible="groupStore.receivedInvitation != null"
      :invitationData="groupStore.receivedInvitation"
    />
    <router-view />
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { RouterView } from 'vue-router';
import { useGroupStore } from '@/stores/groupStore';
import GroupInviteConfirmModal from '@/components/GroupInviteConfirmModal.vue';
import Splash from '@/components/Splash.vue'

const groupStore = useGroupStore();

const showSplash = ref(true)

onMounted(() => {
  setTimeout(() => (showSplash.value = false), 1500)
  console.log("App.vue 마운트됨. 대기 중인 초대 확인 시작...");
  groupStore.checkPendingInvitations();
});
</script>
<style scoped>
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: scale(.995);
}
.fade-enter-active,
.fade-leave-active {
  transition: all .2s ease;
}

</style>