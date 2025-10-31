<template>
  <div class="sos-main-page container-fluid p-0">
    <!-- Header 
    <div class="d-flex align-items-center justify-content-between p-3 border-bottom shadow-sm">
      <div class="d-flex align-items-center">
         //뒤로 가기 버튼 
        <i class="fas fa-chevron-left me-2 fs-5" @click="$router.back()" style="cursor: pointer;" :style="{ color: darkColor }"></i>
        <h5 class="fw-bolder mb-0" :style="{ color: darkColor }">SOS</h5>
      </div>
      <div>
        <i class="fas fa-bell me-3 fs-5" :style="{ color: dangerColor }"></i>
        <i class="fas fa-bars fs-5" :style="{ color: darkColor }"></i>
      </div>
    </div>
    -->

    <div class="p-3">

      <!-- 119 긴급 신고 (가장 큰 버튼) -->
      <div class="card shadow-lg mb-4 rounded-3 p-5 text-center"
           :style="{ backgroundColor: dangerColor, cursor: 'pointer' }"
           @click="handleEmergencyCall('119')">
        <i class="fas fa-phone-alt fa-3x mb-2 text-white"></i>
        <h4 class="fw-bolder mb-0 text-white">119 긴급 신고</h4>
      </div>

      <!-- 주요 기능 4개 (Grid Layout) -->
      <div class="row g-3 mb-4">

        <!-- 카드: 라이프가드 -->
        <div class="col-6">
          <div class="feature-card" :style="{ borderColor: safetyColor, color: darkColor }"
               @click="handleEmergencyCall('lifeguard')">
            <i class="fas fa-plus-square fa-2x mb-3" :style="{ color: safetyColor }"></i>
            <p class="fw-bolder fs-6 mb-0" :style="{ color: darkColor }">라이프가드</p>
          </div>
        </div>

        <!-- 카드: 응급 대처법 -->
        <div class="col-6">
          <div class="feature-card" :style="{ borderColor: cautionColor, color: darkColor }"
               @click="$router.push({ name: 'FirstAid' })">
            <i class="fas fa-exclamation-triangle fa-2x mb-3" :style="{ color: cautionColor }"></i>
            <p class="fw-bolder fs-6 mb-0" :style="{ color: darkColor }">응급 대처법</p>
          </div>
        </div>

        <!-- 카드: 해양경찰 신고 -->
        <div class="col-6">
          <div class="feature-card" :style="{ borderColor: mainColor, color: darkColor }"
               @click="handleEmergencyCall('122')">
            <i class="fas fa-shield-alt fa-2x mb-3" :style="{ color: mainColor }"></i>
            <p class="fw-bolder fs-6 mb-0" :style="{ color: darkColor }">해양경찰 신고</p>
          </div>
        </div>

        <!-- 카드: 해파리 제보 -->
        <div class="col-6">
          <div class="feature-card" :style="{ borderColor: mainColor, color: darkColor }"
               @click="$router.push({ name: 'JellyfishReport' })">
            <i class="fas fa-umbrella-beach fa-2x mb-3" :style="{ color: mainColor }"></i>
            <p class="fw-bolder fs-6 mb-0" :style="{ color: darkColor }">해파리 제보</p>
          </div>
        </div>

      </div>

      <!-- 안내 메시지 -->
      <div class="alert alert-light text-center p-3 rounded-3 fw-bold border-0"
           :style="{ backgroundColor: '#f1f9ff', color: darkColor }">
        버튼 클릭 시 연결됩니다
      </div>
    </div>

    <!-- 로딩 모달 (sosStore.isLoading 상태에 연결) -->
    <div v-if="sosStore.isLoading" class="loading-overlay d-flex align-items-center justify-content-center">
      <div class="loading-box p-5 rounded-3 shadow-lg text-center" :style="{ backgroundColor: 'white' }">
        <h4 class="fw-bolder mb-0" :style="{ color: darkColor }">연결 중입니다....</h4>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useSOSStore } from '@/stores/sosStore'; // SOS Store 사용
import { useConfirmModal } from '@/utils/modalUtils';

import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'

const store = useStore();

const { header } = storeToRefs(store)

onMounted(() => {
  header.value = '긴급신고'
})


const sosStore = useSOSStore();
const { showConfirmModal } = useConfirmModal();

// --- Color Definitions ---
const mainColor = '#0092BA';
const darkColor = '#0B1956';
const safetyColor = '#8482FF';
const cautionColor = '#FFB354';
const dangerColor = '#EB725B';

// ===== 플랫폼 판단 유틸 =====
const isMobileEnv = () => {
  const ua = navigator.userAgent || navigator.vendor || window.opera;
  return /android|iphone|ipad|ipod|iemobile|opera mini|mobile/i.test(ua);
};

// ===== 실제 다이얼 호출 =====
// - 모바일: 전화앱 호출 (tel:)
// - PC/미지원: 번호 복사 + 안내
const dial = async (displayTitle, number) => {
  try {
    sosStore.setLoading(true);

    // 스토어에 logEmergencyCall이 존재하면 로깅 (없으면 무시)
    if (typeof sosStore.logEmergencyCall === 'function') {
      await sosStore.logEmergencyCall({ title: displayTitle, target: number }).catch(() => {});
    }

    if (isMobileEnv()) {
      // 모바일: 전화 앱 호출
      window.location.href = `tel:${number}`;
    } else {
      // PC 등: 번호 복사 + 안내
      try {
        await navigator.clipboard.writeText(number);
        await showConfirmModal({
          title: '전화 연결 안내',
          message: `${displayTitle} 번호(${number})를 복사했어요.\n스마트폰에서 붙여넣어 전화를 걸어주세요.`,
          type: 'info',
        });
      } catch {
        await showConfirmModal({
          title: '전화 연결 안내',
          message: `${displayTitle} 번호: ${number}\n복사가 차단된 환경이에요. 직접 눌러 전화해 주세요.`,
          type: 'info',
        });
      }
    }
  } finally {
    sosStore.setLoading(false);
  }
};

// --- Methods ---
// 신고 타깃: '119' | '122' | 'lifeguard'
const handleEmergencyCall = async (target) => {
  
  // 1) 타깃별 텍스트/번호 맵
  const CONFIG = {
    '119':        { title: '119 긴급 신고', number: '119' },
    '122':        { title: '해양경찰 신고(122)', number: '122'},
    'lifeguard':  { title: '라이프가드 신고', number: null } // 고정번호가 없으니 별도 처리
  };

  const cfg = CONFIG[target];

  const confirmed = await showConfirmModal({
    title: cfg.title,
    message: `${cfg.title}에 바로 연결할까요?`,
    type: 'confirm',
    confirmText: '전화 연결',
    cancelText: '취소',
  });
  if (!confirmed) return;

  // 실제 다이얼 (로딩/로깅 포함)
  await dial(cfg.title, cfg.number);
};
</script>

<style scoped>
.sos-main-page {
  min-height: calc(100vh - 55px - 60px);
}

/* 주요 기능 카드 스타일 */
.feature-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 140px;
  padding: 20px;
  border-radius: 12px;
  border: 2px solid;
  text-align: center;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.feature-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1);
}

/* 로딩 오버레이 (loding.png) */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.4);
  z-index: 2000;
}

.loading-box {
  background-color: white;
  padding: 40px;
  border-radius: 12px;
}
</style>