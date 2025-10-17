<template>
  <div id="app" class="d-flex flex-column min-vh-100"
       :style="{ '--main-color': mainColor, '--dark-color': darkColor }">

    <!-- ✅ Confirm Modal -->
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

        <div class="nav-item-custom" @click="goTo('/group')">
          <i class="fas fa-users fs-4 mb-1" :style="navIconStyle('/group')"></i>
          <span class="fs-7 fw-bold" :style="navTextStyle('/group')">그룹</span>
        </div>

        <div class="nav-item-custom" @click="goTo('/beach/1')">
          <i class="fas fa-swimmer fs-4 mb-1" :style="navIconStyle('/beach-list')"></i>
          <span class="fs-7 fw-bold" :style="navTextStyle('/beach-list')">해수욕장</span>
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
import ConfirmModal from '@/components/ConfirmModal.vue'
import { useBeachStore } from '@/stores/beachStore'
import { useConfirmModal } from '@/utils/modalUtils'

const router = useRouter()
const route = useRoute()

// 🎨 색상
const mainColor = '#0092BA'
const darkColor = '#0B1956'
const dangerColor = '#EB725B'
const safetyColor = '#8482FF'
const cautionColor = '#FFB354'

const store = useBeachStore()
const { showConfirmModal } = useConfirmModal()

// 🔔 모달
const { modalState } = useConfirmModal()
const handleModalConfirm = () => { modalState.isVisible = false }
const handleModalCancel = () => { modalState.isVisible = false }


const goTo = (path) => {
  // '해수욕장' 푸터 항목 (path: '/beach/1')을 클릭했을 때의 로직
  if (path.startsWith('/beach/') && !store.selectedBeachId) {
    // 1. 선택된 해수욕장 ID가 없을 경우 모달 표시
    showConfirmModal({
      title: '알림',
      // beach-list 경로로 이동하도록 안내 메시지 수정
      message: '현재 선택된 해수욕장이 없습니다.\n해수욕장 목록 페이지로 이동합니다.',
      type: 'info',
      autoHide: true, // 1.5초 후 자동 닫힘
      duration: 1500
    })

    // 2. BeachList 페이지로 이동
    // '/beach-list' 경로로 push
    router.push({ name: 'BeachList' })
    return
  }

  // 3. 그 외의 경로 또는 selectedBeachId가 있는 경우 정상 이동
  // 단, 푸터의 '/beach/1' 경로 대신 'BeachList' 경로를 사용하도록 수정합니다.
  if (path === '/beach-list') {
    router.push({ name: 'BeachList' })
  } else {
    router.push(path)
  }
}



/// 하단 메뉴 색상 처리
// /beach/1 경로는 상세 페이지이므로, '/beach-list'로 간주하여 스타일을 적용하는 것이 논리적입니다.
// 실제 라우트 경로가 '/beach/'로 시작하는지 확인하는 로직으로 변경
const isBeachActive = computed(() => route.path.startsWith('/beach'))

const navIconStyle = (path) => ({ color: (path === '/beach/1' ? isBeachActive.value : route.path === path) ? mainColor : 'white' })
const navTextStyle = (path) => ({ color: (path === '/beach/1' ? isBeachActive.value : route.path === path) ? mainColor : 'white' })

import { computed } from 'vue' // computed import 추가
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
