<template>
  <div class="sos-main-page container-fluid p-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between pb-3 border-bottom mb-4">
      <div class="d-flex align-items-center">
        <i class="fas fa-chevron-left me-2 fs-5" @click="$router.back()" style="cursor: pointer;" :style="{ color: darkColor }"></i>
        <h5 class="fw-bolder mb-0" :style="{ color: darkColor }">SOS</h5>
      </div>
      <div>
        <i class="fas fa-bell me-3 fs-5" :style="{ color: dangerColor }"></i>
        <i class="fas fa-bars fs-5" :style="{ color: darkColor }"></i>
      </div>
    </div>

    <!-- 로딩 오버레이 (loding.png 참고) -->
    <div v-if="isLoading" class="loading-overlay d-flex align-items-center justify-content-center">
      <div class="text-center p-4 rounded-3 shadow-lg" style="background-color: white;">
        <p class="fs-4 fw-bolder mb-0" :style="{ color: darkColor }">연결 중입니다....</p>
      </div>
    </div>

    <!-- 버튼 Grid -->
    <div class="row g-3">
      <!-- 119 긴급 신고 (가장 큰 버튼) -->
      <div class="col-12">
        <div class="sos-card main-call text-center p-4 rounded-3 shadow-sm"
             :style="{ backgroundColor: dangerColor, color: 'white' }"
             @click="handleCall(119)">
          <i class="fas fa-phone-volume fs-1 mb-2"></i>
          <p class="fs-4 fw-bolder mb-0">119 긴급 신고</p>
        </div>
      </div>

      <!-- 인근 응급 의료소 -->
      <div class="col-6">
        <div class="sos-card text-center p-4 rounded-3 shadow-sm"
             @click="handleAction('emergency-hospital')"
             :style="{ backgroundColor: '#F8F9FA', color: dangerColor }">
          <i class="fas fa-ambulance fs-2 mb-2"></i>
          <p class="fw-bold mb-0" :style="{ color: darkColor }">인근 응급 의료소</p>
        </div>
      </div>

      <!-- 간편 신고 -->
      <div class="col-6">
        <div class="sos-card text-center p-4 rounded-3 shadow-sm"
             @click="$router.push({ name: 'SOSSimpleReport' })"
             :style="{ backgroundColor: '#F8F9FA', color: dangerColor }">
          <i class="fas fa-exclamation-triangle fs-2 mb-2"></i>
          <p class="fw-bold mb-0" :style="{ color: darkColor }">간편 신고</p>
        </div>
      </div>

      <!-- 해양경찰 신고 -->
      <div class="col-6">
        <div class="sos-card text-center p-4 rounded-3 shadow-sm"
             @click="handleCall(122)"
             :style="{ backgroundColor: '#F8F9FA', color: mainColor }">
          <i class="fas fa-shield-alt fs-2 mb-2"></i>
          <p class="fw-bold mb-0" :style="{ color: darkColor }">해양경찰 신고</p>
        </div>
      </div>

      <!-- 해파리 제보 -->
      <div class="col-6">
        <div class="sos-card text-center p-4 rounded-3 shadow-sm"
             @click="$router.push({ name: 'JellyfishReport' })"
             :style="{ backgroundColor: '#F8F9FA', color: mainColor }">
          <i class="fas fa-umbrella-beach fs-2 mb-2"></i>
          <p class="fw-bold mb-0" :style="{ color: darkColor }">해파리 제보</p>
        </div>
      </div>
    </div>

    <div class="text-center mt-5">
      <p class="small text-muted mb-0">버튼 클릭시 연결됩니다</p>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue';

const mainColor = '#0092BA';
const darkColor = '#0B1956';
const dangerColor = '#EB725B';

const isLoading = ref(false);

const handleCall = (number) => {
  // 실제 전화 연결 로직 (모바일 환경에서만 작동)
  // 임시로 로딩 상태와 알림을 보여줍니다.
  isLoading.value = true;

  // 2초 후 로딩 해제 및 전화 요청 시뮬레이션
  setTimeout(() => {
    isLoading.value = false;
    alert(`${number} (으)로 신고 요청을 보냅니다.`); // 실제로는 window.location.href = `tel:${number}` 사용
  }, 2000);
};

const handleAction = (type) => {
  if (type === 'emergency-hospital') {
    // 인근 응급 의료소 페이지 이동 (라우터 필요)
    alert('인근 응급 의료소 찾기 페이지로 이동');
  }
  // 다른 액션 추가 가능
};

</script>

<style scoped>
.sos-main-page {
  position: relative;
  min-height: calc(100vh - 55px - 60px);
}

.sos-card {
  height: 150px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid #eee;
}

.sos-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1) !important;
}

.main-call {
  height: 150px;
  background-image: linear-gradient(135deg, v-bind(dangerColor), #ff9800);
  border: none !important;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(255, 255, 255, 0.7);
  z-index: 100;
}
.loading-overlay p {
  color: v-bind(darkColor);
}
</style>
