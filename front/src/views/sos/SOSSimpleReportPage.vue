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
    <!-- AppLayout의 footer 위에 확실히 보이도록 fixed-bottom 적용 -->
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
import { useConfirmModal } from '@/utils/modalUtils.js'; // 모달 유틸리티 import

const { showConfirmModal } = useConfirmModal();

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
    type: 'confirm', // Confirm 모달 사용
  }).then(confirmed => {
    if (confirmed) {
      // 실제 신고 로직 (예: 'tel:119' 또는 API 호출)
      showConfirmModal({
        title: '신고 요청 완료',
        message: '119 긴급 신고를 요청했습니다.\n잠시 후 연결됩니다.',
        type: 'error', // 긴급 상황을 위해 dangerColor 기반의 error 타입을 사용
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
  /* 디자인을 위해 d1ecf1 대신 주 컬러의 연한 버전 사용 */
  background-color: #e6f6fa;
  color: v-bind(darkColor);
  border: 1px solid v-bind(mainColor);
}

.fixed-action-bottom {
  position: fixed; /* Fixed로 변경 */
  bottom: 60px; /* AppLayout의 footer (60px) 위에 배치 */
  left: 0;
  right: 0;
  background-color: white;
  border-top: 1px solid #eee;
  z-index: 100; /* Footer보다 높은 z-index */
}
</style>
