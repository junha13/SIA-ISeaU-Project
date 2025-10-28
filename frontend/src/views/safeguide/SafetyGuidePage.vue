<template>
  <div class="safety-guide-page container-fluid p-0">
    <div class="d-flex align-items-center justify-content-between p-3 border-bottom shadow-sm">
      <div class="d-flex align-items-center">
        <i class="fas fa-chevron-left me-2 fs-5" @click="$router.back()" style="cursor: pointer;" :style="{ color: darkColor }"></i>
        <h5 class="fw-bolder mb-0" :style="{ color: darkColor }">안전 수칙</h5>
      </div>
      <div>
        <i class="fas fa-bell me-3 fs-5" :style="{ color: dangerColor }"></i>
        <i class="fas fa-bars fs-5" :style="{ color: darkColor }"></i>
      </div>
    </div>

    <div class="p-3">
      <div id="safetyGuideCarousel" class="carousel slide mb-4" data-bs-ride="carousel">
        <div class="carousel-inner rounded-3 overflow-hidden bg-light" style="height: 200px;">
          <div class="carousel-item active h-100 d-flex justify-content-center align-items-center">
            <h6 class="text-muted">해수욕장 안전 가이드</h6>
          </div>
        </div>
        <div class="carousel-indicators-custom d-flex justify-content-center mt-3">
          <button type="button" data-bs-target="#safetyGuideCarousel" data-bs-slide-to="0" class="active me-2" aria-current="true" aria-label="Slide 1" :style="{ backgroundColor: mainColor, width: '25px', height: '4px', borderRadius: '2px' }"></button>
          <button type="button" data-bs-target="#safetyGuideCarousel" data-bs-slide-to="1" class="me-2" aria-label="Slide 2" :style="{ backgroundColor: '#e9ecef', width: '25px', height: '4px', borderRadius: '2px' }"></button>
        </div>
      </div>

      <h5 class="fw-bold mb-3" :style="{ color: darkColor }">바다에서 안전하게 즐기기</h5>
      <p class="text-muted small mb-4">
        해수욕장에서 발생할 수 있는 위험 상황과 대처법을 영상으로 학습하세요.
      </p>

      <div v-for="(item, index) in safetyItems" :key="index" class="safety-item-card card shadow-sm border-0 rounded-3 mb-3 p-3">
        <div class="d-flex align-items-center mb-3">
          <i :class="[item.icon, 'fs-3 me-3']" :style="{ color: item.color }"></i>
          <h6 class="fw-bolder mb-0" :style="{ color: darkColor }">{{ item.title }}</h6>
        </div>

        <div class="d-flex justify-content-between gap-3">
          
          <div v-for="(videoId, vIndex) in item.videos" :key="videoId" 
               
               class="video-placeholder flex-fill rounded d-flex justify-content-center align-items-center ratio ratio-16x9"
               
               style="cursor: pointer; background-size: cover; background-position: center; position: relative;"
               
               :style="{ backgroundImage: `url(https://img.youtube.com/vi/${videoId}/mqdefault.jpg)` }"
               @click="showVideoModal(item.title, videoId, vIndex + 1)">
            
            <i class="fas fa-play fs-5 text-white" style="text-shadow: 0 0 6px rgba(0,0,0,0.6);"></i>
          </div>
          
        </div>
      </div>
    </div>

    <VideoModal v-model:isVisible="showModal" :title="videoModalTitle" :videoUrl="videoModalUrl" />

  </div>
</template>

<script setup>
import { ref } from 'vue';
import VideoModal from '@/components/VideoModal.vue'; 

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
  { 
    title: '준비 자세의 필요성', 
    icon: 'fas fa-exchange-alt', 
    color: mainColor, 
    videos: ['9XOvMD2HQE4', '-01E_ccUJbo', 'xAjvHjIldjA'] 
  },
  { 
    title: '안전 장비의 필요성', 
    icon: 'fas fa-toolbox', 
    color: safetyColor, 
    videos: ['tFnYqTQbOUE', 'kl_JPt_6beY', 'ZZGg8MtVB7E'] 
  },
  { 
    title: '바다 속 위험 생물', 
    icon: 'fas fa-fish', 
    color: dangerColor, 
    videos: ['66Gxc9XlnSA', 'Y5ltvhGIwko', 'ZLh5t1sCLBI'] 
  },
  { 
    title: '사고 다발 지역', 
    icon: 'fas fa-exclamation-triangle', 
    color: cautionColor, 
    // [수정] 3개의 영상 ID 추가
    videos: ['dS16DpiyTKU', 'ZLSV1KcywKk', 'YkyHZfd4FuY'] 
  },
];

// --- Methods ---
const showVideoModal = (title, videoId, videoIndex) => {
  videoModalTitle.value = `${title} (영상 ${videoIndex})`;
  videoModalUrl.value = `https://www.youtube.com/embed/${videoId}?autoplay=1`;
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
  overflow: hidden; /* ratio가 rounded를 잘 따르도록 */
}
.video-placeholder:hover {
  filter: brightness(0.95);
}

/* ratio 클래스 (부트스트랩 5 미사용 시 대비) */
.ratio {
  position: relative;
  width: 100%;
}
.ratio-16x9::before {
  display: block;
  padding-top: 56.25%; /* 16:9 비율 */
  content: '';
}
/* ratio 내부의 컨텐츠(아이콘)를 절대 중앙 정렬 */
.ratio > * {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}
</style>