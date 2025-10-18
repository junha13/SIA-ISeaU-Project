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
    <!-- Pinia Store의 receivedInvitation 상태에 따라 전역적으로 노출 -->
    <GroupInviteConfirmModal
        v-if="groupStore.receivedInvitation"
        :isVisible="true"
        :invitationData="{
            inviterName: groupStore.receivedInvitation.inviterName,
            inviterPhone: groupStore.receivedInvitation.inviterPhone,
            // GroupStore에서 필요한 모든 정보를 모달에 전달
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

        <!-- 경로를 '/group'으로 변경하고 isGroupActive로 스타일 처리 -->
        <div class="nav-item-custom" @click="goTo('/group')">
          <i class="fas fa-users fs-4 mb-1" :style="navIconStyle('/group')"></i>
          <span class="fs-7 fw-bold" :style="navTextStyle('/group')">그룹</span>
        </div>

        <div class="nav-item-custom" @click="goTo('/beach/1')">
          <i class="fas fa-swimmer fs-4 mb-1" :style="navIconStyle('/beach/1')"></i>
          <span class="fs-7 fw-bold" :style="navTextStyle('/beach/1')">해수욕장</span>
        </div>

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
// 일반 ConfirmModal의 결과 처리 (type: info, success, error, confirm)
const handleModalConfirm = () => {
  // 일반 모달 (confirm 타입)일 경우 resolve 호출 후 모달 숨김
  if (modalState.resolvePromise) {
    modalState.resolvePromise(true);
  }
  modalState.isVisible = false
}
const handleModalCancel = () => {
  // 일반 모달 (confirm 타입)일 경우 resolve(false) 처리
  if (modalState.resolvePromise) {
    modalState.resolvePromise(false);
  }
  modalState.isVisible = false
}

/**
 * 🔔 그룹 초대 확인 모달 결과 처리 (GroupInviteConfirmModal)
 * GroupStore에서 받은 초대에 대한 수락/거절을 처리합니다.
 * @param {boolean} isAccepted - true면 수락, false면 거절
 */
const handleGroupInviteConfirm = (isAccepted) => {
  // GroupStore의 receiveInvitation 액션에서 Promise가 처리되도록 유도
  // GroupInviteConfirmModal은 GroupStore의 receivedInvitation이 null이 될 때 자동으로 사라짐

  // GroupStore의 receiveInvitation 액션 내부에 Promise 로직이 있으므로,
  // 여기서는 단순히 해당 로직을 실행하기 위해 GroupStore에 정의된 Promise를 resolve합니다.
  if (groupStore.receivedInvitation) {
    // GroupStore의 receiveInvitation 액션이 GroupInviteConfirmModal을 띄우기 위해
    // 내부적으로 Promise를 생성하고 resolvePromise를 modalState에 저장하는 경우를 가정합니다.
    // (현재 GroupStore 설계에 따라 resolvePromise를 사용하지 않고,
    // GroupStore의 receiveInvitation 내부에서 모달을 띄우고 Promise를 처리하는 방식이 더 적절)

    // GroupStore.js를 확인해보면, GroupStore의 receiveInvitation 액션 내에서 Promise를 처리하고,
    // 그 결과에 따라 모달을 숨깁니다. 따라서 여기서는 GroupStore의 액션을 직접 호출할 필요 없이,
    // GroupInviteConfirmModal이 emit한 이벤트를 통해 GroupStore의 Promise를 resolve해야 합니다.

    // 이전에 GroupStore의 receiveInvitation 로직이 모달을 띄우기 위해 modalUtils의 showConfirmModal을 사용하고
    // 그 반환된 Promise를 처리하려고 시도했습니다.
    // GroupInviteConfirmModal은 직접적인 modalState.isVisible 제어보다는 groupStore.receivedInvitation 상태에 의존하므로,
    // GroupStore의 receiveInvitation 로직이 GroupInviteConfirmModal 대신 일반 ConfirmModal을 사용하도록 설계된 것 같습니다.

    // GroupInviteConfirmModal이 GroupStore의 Promise를 처리할 수 있도록, GroupStore의 Action을 직접 호출합니다.

    // GroupStore.js에 acceptInvitation/rejectInvitation 액션이 없으므로, GroupStore의 receiveInvitation이
    // Promise를 반환한다고 가정하고, 그 Promise를 이 컴포넌트에서 제어하는 것이 아니라,
    // GroupStore 내에서 Promise를 resolve해야 합니다.

    // GroupInviteConfirmModal이 emit하는 'confirm'/'cancel'을 GroupStore에서 처리하도록 수정합니다.
    if (isAccepted) {
      groupStore.acceptInvitation(groupStore.receivedInvitation);
    } else {
      groupStore.rejectInvitation(groupStore.receivedInvitation);
    }

  }
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

  // 그룹 페이지: GroupMain로 이동
  if (path === '/group/:1') {
    router.push({ name: 'GroupMain' }) // Group List 페이지로 이동
  } else {
    router.push(path)
  }
}


// 하단 메뉴 색상 처리
const isBeachActive = computed(() => route.path.startsWith('/beach'))
const isGroupActive = computed(() => route.path.startsWith('/group'))

const navIconStyle = (path) => {
  let isActive = false
  if (path === '/beach/1') {
    isActive = isBeachActive.value
  } else if (path === '/group') {
    isActive = isGroupActive.value
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
  } else {
    isActive = route.path === path
  }
  return { color: isActive ? mainColor : 'white' }
}
</script>

<style scoped>
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
