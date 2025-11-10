<template>
  <div id="control-layout" class="d-flex flex-column vh-100">
    <header class="main-header p-2 text-white shadow-sm d-flex justify-content-between align-items-center bg-secondary">
      <div class="d-flex align-items-center">
        <h3 class="mb-0 ms-2 text-primary fw-bold">□□ 해수욕장 관제 시스템</h3>
      </div>
      <div class="d-flex align-items-center">
        <select class="form-select form-select-sm me-3" style="width: 120px;">
          <option>지역 선택</option>
          <option>해수욕장 A</option>
        </select>
        <span class="badge bg-primary fs-6">{{ currentTime }}</span>
      </div>
    </header>

    <div class="d-flex flex-grow-1 overflow-hidden">
      <nav class="sidebar bg-light shadow-sm p-3 border-end">
        <ul class="nav flex-column">
          <li class="nav-item mb-2">
            <router-link to="/control/cctv" class="nav-link text-dark py-2 rounded" active-class="bg-primary text-white">
              <i class="bi bi-camera-video me-2"></i> CCTV
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/control/report" class="nav-link text-dark py-2 rounded" active-class="bg-primary text-white">
              <i class="bi bi-exclamation-triangle-fill me-2"></i> 신고
            </router-link>
          </li>
        </ul>
      </nav>

      <main class="content-area p-0 flex-grow-1 overflow-auto">
        <router-view></router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

const currentTime = ref('');
let timer = null;

const updateTime = () => {
  const now = new Date();
  const options = { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: true };
  currentTime.value = now.toLocaleTimeString('ko-KR', options);
};

onMounted(() => {
  updateTime();
  timer = setInterval(updateTime, 1000);
});

onUnmounted(() => {
  clearInterval(timer);
});
</script>

<style scoped>
/* SCSS 변수 정의에 따라 bg-secondary, text-primary가 #0B1956, #0092BA로 적용됩니다. */
.main-header {
  height: 50px;
}
.sidebar {
  width: 180px;
  flex-shrink: 0;
}
</style>