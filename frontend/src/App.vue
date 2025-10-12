<template>
  <div id="app" class="d-flex flex-column overflow-hidden" style="height:100vh;">
    <!-- Header 10% -->
    <header class="d-flex align-items-center justify-content-between px-4 border-bottom" style="height:10%;">
      <h1 class="m-0 fw-bold text-primary fs-3">I Sea U</h1>
      <nav class="d-none d-md-flex align-items-center gap-4">
        <RouterLink class="text-gray-800 fw-semibold" to="/">홈</RouterLink>
        <RouterLink class="text-gray-800" to="/group">위치공유</RouterLink>
        <RouterLink class="text-gray-800" to="/safety-info">안전수칙</RouterLink>
        <RouterLink class="text-danger fw-bold" to="/emergency">긴급신고</RouterLink>
        <RouterLink class="text-gray-800" to="/login">로그인</RouterLink>
      </nav>
      <button class="btn btn-sm btn-light-primary d-md-none" @click="toggleMenu">
        <i class="ki-duotone ki-element-11 fs-2"></i>  <!-- Metronic 아이콘 세트 사용 시 -->
      </button>
    </header>

    <!-- Main 80% -->
    <main class="flex-grow-1 overflow-auto p-3 p-md-5" style="height:80%;">
      <RouterView />
    </main>

    <!-- Footer 10% (모바일 하단 탭) -->
    <footer class="d-flex d-md-none justify-content-around align-items-center border-top" style="height:10%;">
      <RouterLink to="/" class="text-gray-700 text-decoration-none text-center">
        <i class="bi bi-house fs-3 d-block"></i><span class="fs-8">홈</span>
      </RouterLink>
      <RouterLink to="/group" class="text-gray-700 text-decoration-none text-center">
        <i class="bi bi-people fs-3 d-block"></i><span class="fs-8">공유</span>
      </RouterLink>
      <RouterLink to="/safety-info" class="text-gray-700 text-decoration-none text-center">
        <i class="bi bi-life-preserver fs-3 d-block"></i><span class="fs-8">안전</span>
      </RouterLink>
      <RouterLink to="/emergency" class="text-danger text-decoration-none text-center">
        <i class="bi bi-exclamation-triangle fs-3 d-block"></i><span class="fs-8">신고</span>
      </RouterLink>
    </footer>

    <!-- 공통 모달들 (그대로) -->
    <ConfirmModal
        v-model:isVisible="confirmVisible"
        :title="confirmTitle"
        :message="confirmMessage"
        type="confirm"
        @confirm="onConfirmAction"
    />
    <DangerAlertModal
        v-model:isVisible="alertVisible"
        :depth="alertDepth"
        :height="alertHeight"
        @confirm="alertVisible = false"
    />
  </div>
</template>

<script setup>
import { ref, provide } from 'vue'
import { RouterView, RouterLink } from 'vue-router'
import ConfirmModal from '@/components/common/ConfirmModal.vue'
import DangerAlertModal from '@/components/common/DangerAlertModal.vue'

const confirmVisible = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
let confirmCallback = null
const openConfirm = (title, message, cb) => {
  confirmTitle.value = title
  confirmMessage.value = message
  confirmVisible.value = true
  confirmCallback = cb
}
const onConfirmAction = () => {
  confirmVisible.value = false
  if (confirmCallback) confirmCallback()
}

const alertVisible = ref(false)
const alertDepth = ref(1.2)
const alertHeight = ref(1.3)
const showDepthAlert = (depth, height) => {
  alertDepth.value = depth
  alertHeight.value = height
  alertVisible.value = true
}

provide('openConfirm', openConfirm)
provide('showDepthAlert', showDepthAlert)

const toggleMenu = () => {
  // TODO: 모바일 사이드 메뉴 열기
}
</script>
