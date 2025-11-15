<template>
  <div id="control-layout" class="d-flex flex-column vh-100">
    <!-- 헤더 -->
    <header class="main-header p-2 text-white shadow-sm d-flex justify-content-between align-items-center bg-secondary">
      <div class="d-flex align-items-center">
        <!-- 햄버거 버튼 (모든 해상도에서 사용) -->
        <button
          class="btn btn-link text-white p-1 me-2"
          aria-label="메뉴 열기"
          aria-controls="sidebar"
          :aria-expanded="isSidebarOpen.toString()"
          @click="toggleSidebar"
        >
          <i class="bi bi-list fs-3"></i>
        </button>

        <h3 class="mb-0 ms-2 text-primary fw-bold">제주도 해수욕장 관제 시스템</h3>
      </div>

      <div class="d-flex align-items-center">
        <select
          class="form-select form-select-sm me-1"
          style="width: 120px;"
          v-model="controlView"
        >
          <option>해수욕장</option>
          <option>항구 </option>
        </select>
        <span class="badge bg-primary fs-6">{{ currentTime }}</span>
      </div>
    </header>

    <!-- 본문 -->
    <div class="flex-grow-1 position-relative overflow-hidden">
      <!-- 오프캔버스 사이드바 -->
      <nav
        id="sidebar"
        class="sidebar bg-light shadow-sm p-3 border-end"
        :class="{ open: isSidebarOpen }"
        :aria-hidden="(!isSidebarOpen).toString()"
        :tabindex="isSidebarOpen ? 0 : -1"
      >
        <ul class="nav flex-column">
          <li class="nav-item mb-2">
            <router-link
              to="/control/cctv"
              class="nav-link text-dark py-2 rounded"
              active-class="bg-primary text-white"
              @click="handleNavClick"
              ref="firstLinkRef"
            >
              <i class="bi bi-camera-video me-2"></i> CCTV
            </router-link>
          </li>
          <li class="nav-item">
            <router-link
              to="/control/report"
              class="nav-link text-dark py-2 rounded"
              active-class="bg-primary text-white"
              @click="handleNavClick"
            >
              <i class="bi bi-exclamation-triangle-fill me-2"></i> 신고
            </router-link>
          </li>
        </ul>
      </nav>

      <!-- 콘텐츠 -->
      <main class="content-area p-0 overflow-auto h-100">
        <router-view></router-view>
      </main>

      <!-- 백드롭 -->
      <div v-if="isSidebarOpen" class="backdrop" @click="closeSidebar"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'

import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'
const store = useStore();
const { controlView } = storeToRefs(store)


/* ==== 시계 ==== */
const currentTime = ref('')
let timer = null
const updateTime = () => {
  const now = new Date()
  const options = { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: true }
  currentTime.value = now.toLocaleTimeString('ko-KR', options)
}

/* ==== 오프캔버스 제어 ==== */
const isSidebarOpen = ref(false)
const firstLinkRef = ref(null)

const openSidebar = async () => {
  isSidebarOpen.value = true
  await nextTick()
  // 접근성: 열릴 때 첫 링크로 포커스
  firstLinkRef.value?.$el?.focus?.()
}
const closeSidebar = () => (isSidebarOpen.value = false)
const toggleSidebar = () => (isSidebarOpen.value ? closeSidebar() : openSidebar())

// 라우터 이동 시 닫기
const handleNavClick = () => closeSidebar()

// Esc 키 닫기
const onKeydown = (e) => {
  if (e.key === 'Escape') closeSidebar()
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  document.addEventListener('keydown', onKeydown)
})

onUnmounted(() => {
  clearInterval(timer)
  document.removeEventListener('keydown', onKeydown)
})
</script>

<style scoped>
/* 헤더를 항상 상단에 고정 */
.main-header {
  height: 50px;
  position: sticky;
  top: 0;
  z-index: 1100;
}

/* 오프캔버스 사이드바 (모든 해상도에서 슬라이드) */
.sidebar {
  width: 240px;              /* 필요시 조절 */
  position: fixed;
  top: 50px;                 /* 헤더 높이와 동일 */
  left: 0;
  bottom: 0;
  transform: translateX(-100%);
  transition: transform 0.25s ease;
  z-index: 1040;
  overflow-y: auto;
}

.sidebar.open {
  transform: translateX(0);
}

/* 백드롭 */
.backdrop {
  position: fixed;
  top: 50px;                 /* 헤더 아래부터 */
  left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.35);
  z-index: 1030;
}

/* 콘텐츠는 항상 전체 폭 사용 (사이드바는 fixed라 공간을 차지하지 않음) */
.content-area {
  width: 100%;
}
</style>
