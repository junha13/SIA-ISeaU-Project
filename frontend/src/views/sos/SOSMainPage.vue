<template>
  <div class="sos-main-page container-fluid p-0">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between p-3 border-bottom shadow-sm">
      <div class="d-flex align-items-center">
        <!-- 뒤로 가기 버튼 -->
        <i class="fas fa-chevron-left me-2 fs-5" @click="$router.back()" style="cursor: pointer;" :style="{ color: darkColor }"></i>
        <h5 class="fw-bolder mb-0" :style="{ color: darkColor }">SOS</h5>
      </div>
      <div>
        <i class="fas fa-bell me-3 fs-5" :style="{ color: dangerColor }"></i>
        <i class="fas fa-bars fs-5" :style="{ color: darkColor }"></i>
      </div>
    </div>

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

    <!-- 응급 처치 모달 (FirstAidModal은 컴포넌트로 분리됨) -->
    <FirstAidModal v-model:isVisible="showFirstAidModal" :type="firstAidType" />

  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useSOSStore } from '@/stores/sosStore'; // SOS Store 사용
import { useConfirmModal } from '@/utils/modalUtils';
import FirstAidModal from '@/components/FirstAidModal.vue';

const sosStore = useSOSStore();
const { showConfirmModal } = useConfirmModal();

// --- Color Definitions ---
const mainColor = '#0092BA';
const darkColor = '#0B1956';
const safetyColor = '#8482FF';
const cautionColor = '#FFB354';
const dangerColor = '#EB725B';

// --- State ---
const showFirstAidModal = ref(false);
const firstAidType = ref('');

// --- Methods ---
// 신고 타깃: '119' | '122' | 'lifeguard'
const handleEmergencyCall = async (target) => {
  
  if (target === 'lifeguard') {
    // 의료소 찾기는 별도 로직 (여기서는 응급 처치 모달로 대체)
    showFirstAidModal.value = true;
    firstAidType.value = 'jellyfish';
    return;
  }
  

  // 1) 타깃별 UI 텍스트/타이틀 설정 (한 곳에서 관리)
  const CONFIG = {
    '119':        { title: '119 긴급 신고' },
    '122': { title: '해양경찰 신고' },
    'lifeguard':  { title: '라이프가드 신고' },
  };

  const cfg = CONFIG[target];

  const result = await showConfirmModal({
    title: cfg.title,
    message: `${cfg.title}에 바로 연결하여 신고하시겠습니까?`,
    type: 'confirm',
    confirmText: '신고 연결',
    cancelText: '취소',
  });

  if (result) {
    sosStore.setLoading(true);

    try {
      // Store Action 호출 (API 로깅)
      await sosStore.logEmergencyCall(cfg);

      // 실제 연결 지연 시간 (UX용)
      setTimeout(() => {
        sosStore.setLoading(false);
        showConfirmModal({
          title: '연결 완료',
          message: `${cfg.title} 연결 성공했습니다. (실제 연결: ${target})`,
          type: 'success',
          autoHide: true,
          duration: 2000,
        });
      }, 1500);

    } catch (e) {
      sosStore.setLoading(false);
      showConfirmModal({ title: '오류', message: '신고 로깅 중 오류가 발생했습니다.', type: 'error' });
    }
  }
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