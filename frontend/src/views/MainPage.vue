<template>
  <div class="main-page p-3">

    <!-- 1. 알림/공지 섹션 (해파리 쏘임) -->
    <div class="alert-section d-flex align-items-center mb-4 p-3 rounded-3" :style="{ color: darkColor, border: '1px solid #E0E0FF' }">
      <i class="fas fa-bell me-3 fs-5" :style="{ color: safetyColor }"></i>
      <p class="mb-0 fw-bold fs-6">부산 20대 남성 해파리 쏘임</p>
    </div>

    <!-- 2. 이미지 캐러셀 (Carousel) -->
    <div id="mainCarousel" class="carousel slide mb-5" data-bs-ride="carousel">
      <div class="carousel-inner rounded-3 overflow-hidden">
        <div class="carousel-item active">
          <!-- 더미 이미지 (main.png 참고) -->
          <img src="/images/mainBeach/1.png"
               class="d-block w-100" alt="해변 풍경 1" style="height: 200px; object-fit: cover;">
        </div>
        <div class="carousel-item">
          <img src="/images/mainBeach/2.png"
               class="d-block w-100" alt="해변 풍경 2" style="height: 200px; object-fit: cover;">
        </div>
        <div class="carousel-item">
          <!-- 더미 이미지 (main.png 참고) -->
          <img src="/images/mainBeach/3.png"
               class="d-block w-100" alt="해변 풍경 3" style="height: 200px; object-fit: cover;">
        </div>
        <div class="carousel-item">
          <!-- 더미 이미지 (main.png 참고) -->
          <img src="/images/mainBeach/4.png"
               class="d-block w-100" alt="해변 풍경 4" style="height: 200px; object-fit: cover;">
        </div>
        <!-- ... 추가 슬라이드 항목 ... -->
      </div>

      <!-- Indicator (페이지네이션: 4 dots) -->
      <div class="carousel-indicators-custom d-flex justify-content-center mt-3">
        <button type="button" class="dot" :class="{ active: currentSlide === 0 }" @click="setSlide(0)" aria-label="Slide 1"></button>
        <button type="button" class="dot" :class="{ active: currentSlide === 1 }" @click="setSlide(1)" aria-label="Slide 2"></button>
        <button type="button" class="dot" :class="{ active: currentSlide === 2 }" @click="setSlide(2)" aria-label="Slide 3"></button>
        <button type="button" class="dot" :class="{ active: currentSlide === 3 }" @click="setSlide(3)" aria-label="Slide 4"></button>
      </div>
    </div>

    <div class="p-3 mt-5 border rounded">

    <!-- 3. 주요 기능 카드 4개 (Grid Layout) -->
    <div class="row g-3">

      <!-- 카드 1: 해수욕장 리스트 -->
      <div class="col-6">
        <div class="feature-card" :style="{ borderColor: mainColor, color: darkColor, backgroundImage: `url(${cardImages.BeachList})`, backgroundSize: 'cover', backgroundPosition: 'center' }" @click="goToPage('BeachList')">
          <p class="card-top-right fw-bolder fs-1 mt-2 me-2">해수욕장 리스트</p>
        </div>
      </div>

      <!-- 카드 2: 안전 수칙 (SafetyGuide 라우트 연결) -->
      <div class="col-6">
        <div class="feature-card" :style="{ backgroundImage: `url(${cardImages.SafetyGuide})`, backgroundSize: 'cover', backgroundPosition: 'center' }" @click="goToPage('SafetyGuide')">
          <p class="card-top-right fw-bolder fs-1 mt-2 me-2">안전 수칙</p>
        </div>
      </div>

      <!-- 카드 3: 그룹 -->
     <div class="col-6">
      <div class="feature-card" :style="{ borderColor: cautionColor, backgroundImage: `url(${cardImages.GroupList})`, backgroundSize: 'cover', backgroundPosition: 'center' }" @click="goToPage('GroupList')">
        <p class="card-top-right fw-bolder fs-1 mt-2 me-2">그룹</p>
      </div>
    </div>

      <!-- 카드 4: 신고 (SOSMain 라우트 연결) -->
      <div class="col-6">
        <div class="feature-card" :style="{ borderColor: dangerColor, backgroundImage: `url(${cardImages.SOSMain})`, backgroundSize: 'cover', backgroundPosition: 'center' }" @click="goToPage('SOSMain')">
          <p class="card-top-right fw-bolder fs-1 mt-2 me-2">신고</p>
        </div>
      </div>
            <!-- 카드 5: 게시글 리스트 -->
      <div class="col-6">
        <div class="feature-card" :style="{ borderColor: mainColor, color: darkColor, backgroundImage: `url(${cardImages.PostList})`, backgroundSize: 'cover', backgroundPosition: 'center' }" @click="goToPage('PostList')">
          <p class="card-top-right fw-bolder fs-1 mt-2 me-2">게시글 리스트</p>
        </div>
      </div>
    </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue';
import { useRouter } from 'vue-router'; // useRouter 추가
import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'
const store = useStore();
const { header, beach } = storeToRefs(store)

onMounted(() => {
  header.value = "I Sea U"
});

// --- Color Definitions ---
const mainColor = '#0092BA';   // 주 컬러
const darkColor = '#0B1956';   // 헤더 텍스트 및 기본 텍스트
const safetyColor = '#8482FF'; // 안전
const cautionColor = '#FFB354'; // 주의
const dangerColor = '#EB725B';  // 경고
const lightGrayColor = '#E9ECEF'; // 인디케이터 비활성 색상

// 카드별 배경 이미지 매핑 (경로는 public 디렉터리 기준)
const cardImages = {
  BeachList: '/images/mainButton/beachList3.png',
  SafetyGuide: '/images/mainButton/firstAid3.png',
  GroupList: '/images/mainButton/group3.png',
  SOSMain: '/images/mainButton/report3.png',
  PostList: '/images/mainButton/postingList3.png'
};

// pagination state for static carousel
const currentSlide = ref(0);
let carouselItems = [];
let carouselObserver = null;

const setSlide = (i) => {
  currentSlide.value = i;
  if (!carouselItems.length) carouselItems = Array.from(document.querySelectorAll('#mainCarousel .carousel-item'));
  carouselItems.forEach((el, idx) => el.classList.toggle('active', idx === i));
};

onMounted(() => {
  // initialize carouselItems and currentSlide from DOM
  carouselItems = Array.from(document.querySelectorAll('#mainCarousel .carousel-item'));
  const activeIndex = carouselItems.findIndex((el) => el.classList.contains('active'));
  if (activeIndex >= 0) currentSlide.value = activeIndex;

  // Observe class changes on carousel items so pagination stays in sync
  if (carouselItems.length) {
    carouselObserver = new MutationObserver(() => {
      const idx = carouselItems.findIndex((el) => el.classList.contains('active'));
      if (idx >= 0 && idx !== currentSlide.value) {
        currentSlide.value = idx;
      }
    });
    carouselItems.forEach((el) => {
      carouselObserver.observe(el, { attributes: true, attributeFilter: ['class'] });
    });
  }
});

onUnmounted(() => {
  if (carouselObserver) {
    carouselObserver.disconnect();
    carouselObserver = null;
  }
});

const router = useRouter(); // 라우터 인스턴스 생성

// 실제 페이지 이동 함수
const goToPage = (pageName) => {
  // Vue Router를 사용하여 실제 경로로 이동
  router.push({ name: pageName });
};

</script>

<style scoped>
html, body {
  overflow: hidden;   /* 바깥 스크롤 죽이기 */
}
/* Main Page Custom Styles */
.main-page {
  /* App.vue의 padding-bottom만큼 고려하여 높이 설정 */
  height: calc(100vh - 55px - 60px); /* 화면 전체 - 헤더 - 푸터 (App.vue의 크기에 따라 조정 필요) */
  overflow-y: auto;
}

/* 알림 섹션 */
.alert-section {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

/* 캐러셀 인디케이터 커스텀 (Metronic/Bootstrap 기본 스타일 오버라이드) */
/* .carousel-indicators-custom 스타일은 그대로 유지 */

/* 주요 기능 카드 스타일 */
.feature-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 180px; /* 카드 높이 고정 */
  padding: 20px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative; /* 오버레이용 */
  overflow: hidden;
}

.feature-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
}

.card-content {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* 카드 우측 상단에 텍스트를 배치 (m-1 유사 마진) */
.card-top-right {
  position: absolute;
  top: 0.25rem;
  right: 0.25rem;
  margin: 0;
  z-index: 3;
  color: #ffffff;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
}

/* pagination dots (4) */
.carousel-indicators-custom .dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #d3d3d3; /* light gray */
  border: none;
  margin-right: 8px;
  cursor: pointer;
  padding: 0;
}
.carousel-indicators-custom .dot.active {
  background: #6c757d; /* darker gray for active */
}
</style>
