<template>
  <div class="simple-report-page container-fluid p-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between pb-3 border-bottom mb-4">
      <div class="d-flex align-items-center">
        <i class="fas fa-chevron-left me-2 fs-5" @click="$router.back()" style="cursor: pointer;" :style="{ color: darkColor }"></i>
        <h5 class="fw-bolder mb-0" :style="{ color: darkColor }">간편 신고</h5>
      </div>
      <div>
        <i class="fas fa-bell me-3 fs-5" :style="{ color: dangerColor }"></i>
        <i class="fas fa-bars fs-5" :style="{ color: darkColor }"></i>
      </div>
    </div>

    <h6 class="fw-bold mb-3" :style="{ color: darkColor }">상황 설명</h6>

    <!-- 상황 선택 버튼 리스트 -->
    <div class="d-grid gap-3 mb-5">
      <button v-for="(situation, index) in situations" :key="index"
              class="btn btn-block fw-bold py-3 rounded-3 shadow-sm"
              :class="{ 'btn-secondary-light': selectedSituation !== situation, 'btn-info-highlight': selectedSituation === situation }"
              @click="selectedSituation = situation">
        {{ situation }}
      </button>
    </div>

    <!-- 119 신고하기 버튼 (고정 하단) -->
    <div class="fixed-action-bottom p-3">
      <button class="btn btn-block w-100 fw-bolder py-3 fs-5 text-white rounded-3 shadow"
              :style="{ backgroundColor: dangerColor }"
              @click="handle119Report">
        119 신고하기
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useConfirmModal } from '@/utils/modalUtils.js';
import { useSOSStore } from '@/stores/sosStore'; // SOS Store 임포트

const { showConfirmModal } = useConfirmModal();
const sosStore = useSOSStore(); // Store 인스턴스

const mainColor = '#0092BA';
const darkColor = '#0B1956';
const dangerColor = '#EB725B';

const situations = ref([
  '머리가 어지럽고 아파요',
  '몸이 너무 차가워요',
  '해파리에 쏘였어요',
  '햇빛에 화상을 입었어요',
  '바다벌레에 물렸어요',
  '조개껍데기에 발을 베었어요'
]);

const selectedSituation = ref(null);

const handle119Report = () => {
  let message = '선택된 상황 없이 119에 신고를 요청하시겠습니까?';
  let title = '긴급 신고 확인';

  if (selectedSituation.value) {
    message = `선택된 상황: "${selectedSituation.value}"\n정말 119에 신고를 요청하시겠습니까?`;
  }

  showConfirmModal({
    title: title,
    message: message,
    type: 'confirm',
  }).then(async (confirmed) => {
    if (confirmed) {
      // 신고 로깅 및 로직 처리
      const situation = selectedSituation.value || '상황 미선택';

      // Store Action 호출 (API 로깅)
      await sosStore.logEmergencyCall('119_simple_report', situation);

      // 신고 완료 알림
      showConfirmModal({
        title: '신고 요청 완료',
        message: '119 긴급 신고를 요청했습니다.\n잠시 후 연결됩니다.',
        type: 'error',
        autoHide: true,
        duration: 3000
      });
      // window.location.href = 'tel:119'; // 실제 웹앱 환경에서는 주석 처리
    }
  });
};
</script>

<style scoped>
.simple-report-page {
  /* 하단 버튼 공간 확보 (버튼 높이 + 패딩 고려) */
  min-height: calc(100vh - 55px - 60px);
  padding-bottom: 100px !important;
}
.btn-secondary-light {
  background-color: #f8f9fa;
  color: v-bind(darkColor);
  border: 1px solid #ced4da;
}
.btn-info-highlight {
  /* 디자인을 위해 주 컬러의 연한 버전 사용 */
  background-color: #e6f6fa;
  color: v-bind(darkColor);
  border: 1px solid v-bind(mainColor);
}

.fixed-action-bottom {
  position: fixed;
  bottom: 60px;
  left: 0;
  right: 0;
  background-color: white;
  border-top: 1px solid #eee;
  z-index: 100;
}
</style>