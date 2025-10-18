<template>
  <div id="app" class="d-flex flex-column min-vh-100"
       :style="{ '--main-color': mainColor, '--dark-color': darkColor }">

    <!-- ✅ Confirm Modal (기존 알림/확인 모달) -->
    <ConfirmModal
        v-model:isVisible="modalState.isVisible"
        :title="modalState.title"
        :message="modalState.message"
        :type="modalState.type"
        :confirmText="modalState.confirmText"
        :cancelText="modalState.cancelText"
        :autoHide="modalState.autoHide"
        :duration="modalState.duration"
        @confirm="handleModalConfirm"
        @cancel="handleModalCancel"
    />

    <!-- ✅ Group Invite Confirm Modal (Group 3982) -->
    <GroupInviteConfirmModal
        v-if="groupStore.receivedInvitation"
        :isVisible="true"
        :invitationData="{
            inviterName: groupStore.receivedInvitation.inviterName,
            inviterPhone: groupStore.receivedInvitation.inviterPhone,
            ...groupStore.receivedInvitation
        }"
        @confirm="handleGroupInviteConfirm(true)"
        @cancel="handleGroupInviteConfirm(false)"
    />


    <!-- ✅ Header -->
    <header class="app-header shadow-sm sticky-top" :style="{ backgroundColor: 'white', color: darkColor }">
      <div class="container-fluid d-flex align-items-center justify-content-between p-3">
        <div class="d-flex align-items-center">
          <i class="fas fa-water fs-3 me-2" :style="{ color: mainColor }"></i>
          <h1 class="fs-4 fw-bolder mb-0" :style="{ color: darkColor }">I Sea U</h1>
        </div>
        <div class="d-flex align-items-center">
          <i class="fas fa-bell fs-5 me-3" :style="{ color: dangerColor }"></i>
          <i class="fas fa-bars fs-5"></i>
        </div>
      </div>
    </header>

    <!-- ✅ Main Content -->
    <main class="flex-grow-1 container-fluid p-0">
      <router-view />
    </main>

    <!-- ✅ Footer -->
    <footer class="app-footer fixed-bottom" :style="{ backgroundColor: darkColor }">
      <div class="container-fluid d-flex justify-content-around py-2">

        <div class="nav-item-custom" @click="goTo('/')">
          <i class="fas fa-home fs-4 mb-1" :style="navIconStyle('/')"></i>
          <span class="fs-7 fw-bold" :style="navTextStyle('/')">홈</span>
        </div>

        <!-- 경로를 '/group'으로 변경 (GroupList 페이지로 연결) -->
        <div class="nav-item-custom" @click="goTo('/group')">
          <i class="fas fa-users fs-4 mb-1" :style="navIconStyle('/group')"></i>
          <span class="fs-7 fw-bold" :style="navTextStyle('/group')">그룹</span>
        </div>

        <div class="nav-item-custom" @click="goTo('/beach/1')">
          <i class="fas fa-swimmer fs-4 mb-1" :style="navIconStyle('/beach/1')"></i>
          <span class="fs-7 fw-bold" :style="navTextStyle('/beach/1')">해수욕장</span>
        </div>

        <!-- 내정보 탭 (MyInfo 라우트 연결) -->
        <div class="nav-item-custom" @click="goTo('/my-info')">
          <i class="fas fa-user-circle fs-4 mb-1" :style="navIconStyle('/my-info')"></i>
          <span class="fs-7 fw-bold" :style="navTextStyle('/my-info')">내정보</span>
        </div>

      </div>
    </footer>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { computed } from 'vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import GroupInviteConfirmModal from '@/components/GroupInviteConfirmModal.vue'
import { useBeachStore } from '@/stores/beachStore'
import { useGroupStore } from '@/stores/groupStore'
import { useConfirmModal } from '@/utils/modalUtils'

const router = useRouter()
const route = useRoute()

// 🎨 색상
const mainColor = '#0092BA'
const darkColor = '#0B1956'
const dangerColor = '#EB725B'
const safetyColor = '#8482FF'
const cautionColor = '#FFB354'

const beachStore = useBeachStore()
const groupStore = useGroupStore()
const { showConfirmModal } = useConfirmModal()

// 🔔 일반 모달 상태
const { modalState } = useConfirmModal()
const handleModalConfirm = () => {
  if (modalState.resolvePromise) {
    modalState.resolvePromise(true);
  }
  modalState.isVisible = false
}
const handleModalCancel = () => {
  if (modalState.resolvePromise) {
    modalState.resolvePromise(false);
  }
  modalState.isVisible = false
}

// 🔔 그룹 초대 확인 모달 결과 처리
const handleGroupInviteConfirm = (isAccepted) => {
  // groupStore의 receivedInvitation 값을 사용하여 처리 로직 호출
  if (isAccepted) {
    // 수락 로직 (GroupStore에서 처리)
  } else {
    // 거절 로직 (GroupStore에서 처리)
  }
  // GroupStore에서 receivedInvitation 상태를 변경하여 모달을 닫도록 유도
  groupStore.receivedInvitation = null;
}


const goTo = (path) => {
  // 해수욕장 상세 페이지 (선택된 해수욕장 확인 로직)
  if (path.startsWith('/beach/') && !beachStore.selectedBeachId) {
    showConfirmModal({
      title: '알림',
      message: '현재 선택된 해수욕장이 없습니다.\n해수욕장 목록 페이지로 이동합니다.',
      type: 'info',
      autoHide: true,
      duration: 1500
    })
    router.push({ name: 'BeachList' })
    return
  }

  // 그룹 페이지: GroupList로 이동
  if (path === '/group') {
    router.push({ name: 'GroupList' })
  }
  // 내정보 페이지: MyInfo로 이동
  else if (path === '/my-info') {
    router.push({ name: 'MyInfo' })
  }
  // 그 외 페이지 (홈)
  else {
    router.push(path)
  }
}


// 하단 메뉴 색상 처리
const isBeachActive = computed(() => route.path.startsWith('/beach'))
const isGroupActive = computed(() => route.path.startsWith('/group'))
const isMyInfoActive = computed(() => route.path.startsWith('/my-info'))

const navIconStyle = (path) => {
  let isActive = false
  if (path === '/beach/1') {
    isActive = isBeachActive.value
  } else if (path === '/group') {
    isActive = isGroupActive.value
  } else if (path === '/my-info') {
    isActive = isMyInfoActive.value
  } else {
    isActive = route.path === path
  }
  return { color: isActive ? mainColor : 'white' }
}

const navTextStyle = (path) => {
  let isActive = false
  if (path === '/beach/1') {
    isActive = isBeachActive.value
  } else if (path === '/group') {
    isActive = isGroupActive.value
  } else if (path === '/my-info') {
    isActive = isMyInfoActive.value
  } else {
    isActive = route.path === path
  }
  return { color: isActive ? mainColor : 'white' }
}
</script>

<style scoped>
/* (이전 스타일 유지) */
#app {
  font-family: Arial, sans-serif;
  padding-bottom: 60px;
}

.app-header {
  height: 55px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05) !important;
  z-index: 1000;
}

.app-footer {
  height: 60px;
  z-index: 1000;
}

.nav-item-custom {
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 5px 0;
  transition: background-color 0.2s ease;
}

.nav-item-custom:hover {
  background-color: rgba(0, 146, 186, 0.2);
}
</style>