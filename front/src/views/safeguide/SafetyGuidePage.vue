<template>
  <div class="safety-guide-page container-fluid p-0">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between p-3 border-bottom shadow-sm">
      <div class="d-flex align-items-center">
        <!-- 뒤로 가기 버튼 -->
        <i class="fas fa-chevron-left me-2 fs-5" @click="$router.back()" style="cursor: pointer;" :style="{ color: darkColor }"></i>
        <h5 class="fw-bolder mb-0" :style="{ color: darkColor }">안전 수칙</h5>
      </div>
      <div>
        <i class="fas fa-bell me-3 fs-5" :style="{ color: dangerColor }"></i>
        <i class="fas fa-bars fs-5" :style="{ color: darkColor }"></i>
      </div>
    </div>

    <!-- Main Content -->
    <div class="p-3">
      <!-- 1. 안전 가이드 캐러셀 -->
      <div id="safetyGuideCarousel" class="carousel slide mb-4" data-bs-ride="carousel">
        <div class="carousel-inner rounded-3 overflow-hidden bg-light" style="height: 200px;">
          <div class="carousel-item active h-100 d-flex justify-content-center align-items-center">
            <!-- Placeholder for Content 1 -->
            <h6 class="text-muted">해수욕장 안전 가이드</h6>
          </div>
        </div>
        <!-- Indicator -->
        <div class="carousel-indicators-custom d-flex justify-content-center mt-3">
          <button type="button" data-bs-target="#safetyGuideCarousel" data-bs-slide-to="0" class="active me-2" aria-current="true" aria-label="Slide 1" :style="{ backgroundColor: mainColor, width: '25px', height: '4px', borderRadius: '2px' }"></button>
          <button type="button" data-bs-target="#safetyGuideCarousel" data-bs-slide-to="1" class="me-2" aria-label="Slide 2" :style="{ backgroundColor: '#e9ecef', width: '25px', height: '4px', borderRadius: '2px' }"></button>
        </div>
      </div>

      <!-- 2. 안전 영상 학습 섹션 -->
      <h5 class="fw-bold mb-3" :style="{ color: darkColor }">바다에서 안전하게 즐기기</h5>
      <p class="text-muted small mb-4">
        해수욕장에서 발생할 수 있는 위험 상황과 대처법을 영상으로 학습하세요.
      </p>

      <!-- 학습 항목 리스트 -->
      <div v-for="(item, index) in safetyItems" :key="index" class="safety-item-card card shadow-sm border-0 rounded-3 mb-3 p-3">
        <div class="d-flex align-items-center mb-3">
          <!-- Icon -->
          <i :class="[item.icon, 'fs-3 me-3']" :style="{ color: item.color }"></i>
          <!-- Title -->
          <h6 class="fw-bolder mb-0" :style="{ color: darkColor }">{{ item.title }}</h6>
        </div>

        <!-- Video Play Button Placeholder -->
        <div class="d-flex justify-content-between gap-3">
          <div v-for="n in 3" :key="n" class="video-placeholder flex-fill bg-light rounded d-flex justify-content-center align-items-center"
               style="height: 60px; cursor: pointer;"
               @click="showVideoModal(item.title, n)">
            <i class="fas fa-play fs-5 text-muted"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- ✅ Youtube Video Modal (영상 재생 모달) -->
    <VideoModal v-model:isVisible="showModal" :title="videoModalTitle" :videoUrl="videoModalUrl" />

  </div>
</template>

<script setup>
import { ref } from 'vue';
import VideoModal from '@/components/VideoModal.vue'; // 영상 재생 모달 컴포넌트

const mainColor = '#0092BA';
const darkColor = '#0B1956';
const dangerColor = '#EB725B';
const safetyColor = '#8482FF';
const cautionColor = '#FFB354';

// --- State for Video Modal ---
const showModal = ref(false);
const videoModalTitle = ref('');
const videoModalUrl = ref('');

// --- Safety Learning Items ---
const safetyItems = [
  { title: '준비 자세의 필요성', icon: 'fas fa-exchange-alt', color: mainColor, videos: ['url1', 'url2', 'url3'] },
  { title: '안전 장비의 필요성', icon: 'fas fa-toolbox', color: safetyColor, videos: ['url4', 'url5', 'url6'] },
  { title: '바다 속 위험 생물', icon: 'fas fa-fish', color: dangerColor, videos: ['url7', 'url8', 'url9'] },
  { title: '사고 다발 지역', icon: 'fas fa-exclamation-triangle', color: cautionColor, videos: ['url10', 'url11', 'url12'] },
];

// --- Methods ---

const showVideoModal = (title, videoIndex) => {
  videoModalTitle.value = `${title} (영상 ${videoIndex})`;
  // Dummy URL. 실제로는 YouTube embed URL이 들어가야 합니다.
  videoModalUrl.value = `https://www.youtube.com/embed/dummy_video_id_${videoIndex}`;
  showModal.value = true;
};
</script>

<style scoped>
.safety-guide-page {
  min-height: calc(100vh - 55px - 60px); /* 헤더와 푸터 제외 높이 */
}
.safety-item-card {
  transition: transform 0.2s;
}
.safety-item-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.08) !important;
}
.video-placeholder {
  transition: background-color 0.2s;
  border: 1px solid #dee2e6;
}
.video-placeholder:hover {
  background-color: #e9ecef !important;
}
</style>