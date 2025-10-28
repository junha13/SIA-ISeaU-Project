<template>
  <div class="main-page p-3">

    <!-- 1. 알림/공지 섹션 (해파리 쏘임) -->
    <div class="alert-section d-flex align-items-center mb-4 p-3 rounded-3"
         :style="{ backgroundColor: '#F0F0FF', color: darkColor, border: '1px solid #E0E0FF' }">
      <i class="fas fa-bell me-3 fs-5" :style="{ color: safetyColor }"></i>
      <p class="mb-0 fw-bold fs-6">부산 20대 남성 해파리 쏘임</p>
    </div>

    <!-- 2. 이미지 캐러셀 (Carousel) -->
    <div id="mainCarousel" class="carousel slide mb-5" data-bs-ride="carousel">
      <div class="carousel-inner rounded-3 overflow-hidden">
        <div class="carousel-item active">
          <!-- 더미 이미지 (main.png 참고) -->
          <img src="/assets/media/books/1.png"
               class="d-block w-100" alt="해변 풍경 1" style="height: 200px; object-fit: cover;">
        </div>
        <div class="carousel-item">
          <img src="/assets/media/books/2.png"
               class="d-block w-100" alt="해변 풍경 2" style="height: 200px; object-fit: cover;">
        </div>
        <div class="carousel-item">
          <!-- 더미 이미지 (main.png 참고) -->
          <img src="/assets/media/books/3.png"
               class="d-block w-100" alt="해변 풍경 3" style="height: 200px; object-fit: cover;">
        </div>
        <div class="carousel-item">
          <!-- 더미 이미지 (main.png 참고) -->
          <img src="/assets/media/books/4.png"
               class="d-block w-100" alt="해변 풍경 4" style="height: 200px; object-fit: cover;">
        </div>
        <!-- ... 추가 슬라이드 항목 ... -->
      </div>

      <!-- Indicator (페이지네이션) -->
      <div class="carousel-indicators-custom d-flex justify-content-center mt-3">
        <button type="button" data-bs-target="#mainCarousel" data-bs-slide-to="0" class="active me-2" aria-current="true" aria-label="Slide 1" :style="{ backgroundColor: mainColor }"></button>
        <button type="button" data-bs-target="#mainCarousel" data-bs-slide-to="1" class="me-2" aria-label="Slide 2" :style="{ backgroundColor: lightGrayColor }"></button>
        <button type="button" data-bs-target="#mainCarousel" data-bs-slide-to="2" class="me-2" aria-label="Slide 3" :style="{ backgroundColor: lightGrayColor }"></button>
        <button type="button" data-bs-target="#mainCarousel" data-bs-slide-to="3" class="me-2" aria-label="Slide 4" :style="{ backgroundColor: lightGrayColor }"></button>
      </div>
    </div>

    <!-- 3. 주요 기능 카드 4개 (Grid Layout) -->
    <div class="row g-3">

      <!-- 카드 1: 해수욕장 리스트 -->
      <div class="col-6">
        <div class="feature-card" :style="{ borderColor: mainColor, color: darkColor }" @click="goToPage('BeachList')">
          <i class="fas fa-list fs-2 mb-3" :style="{ color: mainColor }"></i>
          <p class="fw-bolder fs-5 mb-0" :style="{ color: darkColor }">해수욕장 리스트</p>
        </div>
      </div>

      <!-- 카드 2: 안전 수칙 (SafetyGuide 라우트 연결) -->
      <div class="col-6">
        <div class="feature-card" :style="{ borderColor: safetyColor, color: darkColor }" @click="goToPage('SafetyGuide')">
          <i class="fas fa-shield-alt fs-2 mb-3" :style="{ color: safetyColor }"></i>
          <p class="fw-bolder fs-5 mb-0" :style="{ color: darkColor }">안전 수칙</p>
        </div>
      </div>

      <!-- 카드 3: 그룹 -->
     <div class="col-6">
  <div class="feature-card" :style="{ borderColor: cautionColor, color: darkColor }" @click="goToPage('GroupList')">
    <i class="fas fa-users fs-2 mb-3" :style="{ color: cautionColor }"></i>
    <p class="fw-bolder fs-5 mb-0" :style="{ color: darkColor }">그룹</p>
  </div>
</div>

      <!-- 카드 4: 신고 (SOSMain 라우트 연결) -->
      <div class="col-6">
        <div class="feature-card" :style="{ borderColor: dangerColor, color: darkColor }" @click="goToPage('SOSMain')">
          <i class="fas fa-exclamation-triangle fs-2 mb-3" :style="{ color: dangerColor }"></i>
          <p class="fw-bolder fs-5 mb-0" :style="{ color: darkColor }">신고</p>
        </div>
      </div>
            <!-- 카드 5: 게시글 리스트 -->
      <div class="col-6">
        <div class="feature-card" :style="{ borderColor: mainColor, color: darkColor }" @click="goToPage('PostList')">
          <i class="fas fa-list fs-2 mb-3" :style="{ color: mainColor }"></i>
          <p class="fw-bolder fs-5 mb-0" :style="{ color: darkColor }">게시글 리스트</p>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
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

const router = useRouter(); // 라우터 인스턴스 생성

// 실제 페이지 이동 함수
const goToPage = (pageName) => {
  // Vue Router를 사용하여 실제 경로로 이동
  router.push({ name: pageName });
};

</script>

<style scoped>
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
  height: 150px; /* 카드 높이 고정 */
  padding: 20px;
  border-radius: 12px;
  border: 2px solid; /* 테두리 색상으로 컬러 구분 */
  text-align: center;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.feature-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
}
</style>
