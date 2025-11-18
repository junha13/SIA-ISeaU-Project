<template>
  <div
      id="app"
      class="d-flex flex-column min-vh-100"
      :class="{ 'control-view-mode-app': route.meta.hideAppLayout }"
      :style="{ '--main-color': mainColor, '--dark-color': darkColor }"
  >

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


    <header
        v-if="!route.meta.hideAppLayout"
        class="app-header shadow-sm sticky-top"
        :style="{ backgroundColor: 'white', color: darkColor }"
    >
      <div class="container-fluid d-flex align-items-center justify-content-between p-3">
        <div class="d-flex align-items-center">

          <h1 v-if="header === mainHeaderName" class="fw-bolder mt-3" :style="{ color: darkColor, fontSize: '1.6rem'}">
            <img class="mt-n2" src="/iseau.png" style="max-width: 35px; max-width: 40px;">
            {{ header }}
          </h1>
          <h1 v-else="header !== mainHeaderName" class="fw-bolder mb-0" :style="{ color: darkColor, fontSize: '1.5rem' }">
            <i class="ki-duotone ki-arrow-left fs-2" @click="goBack()">
              <span class="path1"></span>
              <span class="pat2"></span>
            </i>
            {{ header }}
          </h1>
        </div>
        <div class="d-flex align-items-center">
          <i class="fas fa-triangle-exclamation fs-1 me-5" :style="{ color: dangerColor }" @click="goToSOS()"></i>
          <button
              class="bg-transparent border-0 p-2 burger-btn"
              type="button"
              data-bs-toggle="offcanvas"
              data-bs-target="#sideMenu"
              aria-controls="sideMenu"
              aria-label="menu"
          >
            <i class="fas fa-bars mt-3 fs-1 me-4 navbar-toggler-icon"></i>
          </button>
        </div>
      </div>
      <div class="offcanvas offcanvas-end" tabindex="-1" id="sideMenu" style="max-width: 38%;">
        <div class="offcanvas-header border-3 border-bottom shadow-sm">
          <span class="offcanvas-title fw-bold mt-2" style="font-size: 17px;">메뉴</span>
          <button type="button" class="btn-close" data-bs-dismiss="offcanvas"></button>
        </div>
        <div class="offcanvas-body">
          <a class="d-block mb-3 text-dark text-decoration-none" href="#">해수욕장 목록</a>
          <a class="d-block mb-3 text-dark text-decoration-none" href="#">위험도 / 예보</a>
          <a class="d-block mb-3 text-dark text-decoration-none" href="#">즐겨찾기</a>
          <a class="d-block mb-3 text-dark text-decoration-none" href="#">마이페이지</a>
        </div>
      </div>
    </header>

    <main
        class="flex-grow-1 container-fluid p-0 main-scroll"
        :class="{ 'control-view-mode': route.meta.hideAppLayout }"
    >
      <router-view v-slot="{ Component, route }" >
        <transition
            enter-active-class="animate__animated animate__fadeIn fast-route"
            leave-active-class="animate__animated animate__fadeOut fast-route"
            mode="out-in"
        >
          <div class="route-shell" :key="route.fullPath">
            <component :is="Component" />
          </div>
        </transition>
      </router-view>
    </main>

    <footer
        v-if="!route.meta.hideAppLayout"
        class="app-footer fixed-bottom"
        :style="{ backgroundColor: darkColor }"
    >
      <div class="container-fluid d-flex justify-content-around py-2">

        <div class="nav-item-custom" @click="goTo('/')">
          <i class="fas fa-home fs-4 mb-1" :style="navIconStyle('/')"></i>
          <span class="fs-7 fw-bold" :style="navTextStyle('/')">홈</span>
        </div>

        <div class="nav-item-custom" @click="goTo('/group')">
          <i class="fas fa-users fs-4 mb-1" :style="navIconStyle('/group')"></i>
          <span class="fs-7 fw-bold" :style="navTextStyle('/group')">위치공유</span>
        </div>

        <div class="nav-item-custom" @click="goToSelectedBeach">
          <i class="fas fa-swimmer fs-4 mb-1" :style="navIconStyle('/beach')"></i>
          <span class="fs-7 fw-bold" :style="navTextStyle('/beach')">해안가</span>
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
import { computed, ref, onMounted } from 'vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import GroupInviteConfirmModal from '@/components/GroupInviteConfirmModal.vue'
import { useBeachStore } from '@/stores/beachStore'
import { useGroupStore } from '@/stores/groupStore'
import { useConfirmModal } from '@/utils/modalUtils'

import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'
const store = useStore();
const { header, beach } = storeToRefs(store)
const mainHeaderName = "I Sea U"

function goBack() {
  router.back();
}

const router = useRouter()
const route = useRoute() // ✅ route 객체를 사용해 meta 정보에 접근합니다.

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

// 사용자가 선택한 해수욕장 번호
const { selectedBeachId } = storeToRefs(beachStore) // 숫자 또는 null

function goToSOS() {
  router.push(`/sos`)
}

function goToSelectedBeach() {
  const id = Number(selectedBeachId.value || 0); // 0/null ⇒ 미선택
  if (id > 0) {
    // 선택된 해수욕장 상세로 이동
    router.push(`/beach/${id}`)
  } else {
    // 미선택 ⇒ 안내 후 리스트로
    showConfirmModal({
      title: '알림',
      message: '현재 선택된 해수욕장이 없습니다.\n해수욕장 목록 페이지로 이동합니다.',
      type: 'info',
      autoHide: true,
      duration: 1500
    })
    router.push({ name: 'BeachList' })
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

  // 그룹 페이지: GroupList로 이동
  if (path === '/group') {
    router.push({ name: 'GroupMain' })
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
  if (path === '/beach') {
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
  if (path === '/beach') {
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

const isBack = ref(false)
const stack = ref([])

onMounted(() => {
  // 1) 현재 페이지 먼저 넣어둔다
  stack.value = [router.currentRoute.value.fullPath]

  // 2) 이후부터는 이동마다 판단
  router.beforeEach((to, from, next) => {
    // 같은 데 또 가는 거면 그냥 통과
    if (to.fullPath === from.fullPath) {
      isBack.value = false
      return next()
    }

    const i = stack.value.indexOf(to.fullPath)

    if (i !== -1) {
      // 이미 갔던 곳 → 뒤로가기
      isBack.value = true
      stack.value = stack.value.slice(0, i + 1)
    } else {
      // 처음 가는 곳 → 앞으로가기
      isBack.value = false
      stack.value.push(to.fullPath)
    }
    next()
  })
})
</script>

<style scoped>
/* AppLayout의 root div에서 padding-bottom을 조건부로 제어하기 위한 클래스 */
.control-view-mode-app {
  padding-bottom: 0px !important;
}

.main-scroll {
  /* 기본 높이: 헤더(55px)와 푸터(60px)가 있을 때 */
  height: calc(100vh - 55px - 60px);
  overflow-y: scroll;
  scrollbar-gutter: stable;           /* ✅ 크롬/엣지에서 덜 흔들리게 */
  -webkit-overflow-scrolling: touch;  /* 모바일 부드럽게 */
}

/* Control 화면일 경우 높이 재설정 (헤더/푸터가 없을 때) */
.main-scroll.control-view-mode {
  height: 100vh;
  overflow-y: hidden; /* ControlLayout이 100vh를 차지하므로 AppLayout에서는 스크롤을 막습니다. */
}

#app {
  font-family: Arial, sans-serif;
  padding-bottom: 60px; /* 기본값 */
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

.route-wrapper {
  position: relative;
  overflow: hidden;
}
.route-page {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.fast-route {
  animation-duration: .12s !important;
}
</style>