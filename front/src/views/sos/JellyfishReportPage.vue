<template>
  <div class="jellyfish-report-page container-fluid p-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between pb-3 border-bottom mb-4">
      <div class="d-flex align-items-center">
        <i class="fas fa-chevron-left me-2 fs-5" @click="$router.back()" style="cursor: pointer;" :style="{ color: darkColor }"></i>
        <h5 class="fw-bolder mb-0" :style="{ color: darkColor }">해파리 제보</h5>
      </div>
      <div>
        <i class="fas fa-bell me-3 fs-5" :style="{ color: dangerColor }"></i>
        <i class="fas fa-bars fs-5" :style="{ color: darkColor }"></i>
      </div>
    </div>

    <!-- 사진 등록 -->
    <div class="text-center mb-4 p-5 rounded-3 border" style="background-color: #f8f9fa; cursor: pointer;" @click="triggerFileUpload">
      <i class="fas fa-camera fs-1 mb-2 text-muted"></i>
      <p class="mb-0 fw-bold text-muted">사진 등록</p>
      <input type="file" ref="fileInput" @change="handleFileUpload" accept="image/*" style="display: none;">
    </div>

    <!-- 제보 양식 -->
    <div class="mb-3">
      <label class="fw-bold mb-2" :style="{ color: darkColor }">위치 (필수)</label>
      <input type="text" class="form-control rounded-3" placeholder="발견 위치를 입력하세요">
    </div>

    <div class="mb-3">
      <label class="fw-bold mb-2" :style="{ color: darkColor }">전화번호</label>
      <input type="tel" class="form-control rounded-3" placeholder="전화번호를 입력하세요">
    </div>

    <div class="mb-5">
      <label class="fw-bold mb-2" :style="{ color: darkColor }">상세 설명</label>
      <textarea class="form-control rounded-3" rows="4" placeholder="상세 설명을 입력하세요"></textarea>
    </div>

    <!-- 제보 완료 버튼 (고정 하단) -->
    <div class="fixed-action-bottom p-3">
      <button class="btn w-100 fw-bolder py-3 fs-5 text-white rounded-3 shadow"
              :style="{ backgroundColor: dangerColor }"
              @click="submitReport">
        제보 완료
      </button>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useConfirmModal } from '@/utils/modalUtils.js'; // 모달 유틸리티 import

const { showConfirmModal } = useConfirmModal();

const darkColor = '#0B1956';
const dangerColor = '#EB725B';

const fileInput = ref(null);

const triggerFileUpload = () => {
  fileInput.value.click();
};

const handleFileUpload = (event) => {
  const file = event.target.files[0];
  if (file) {
    showConfirmModal({
      title: '사진 등록 완료',
      message: `선택된 파일: ${file.name}`,
      type: 'success',
      autoHide: true,
      duration: 1500
    });
  }
};

const submitReport = () => {
  // API 호출을 통해 제보 데이터를 전송해야 합니다.
  showConfirmModal({
    title: '제보 완료',
    message: '해파리 제보가 성공적으로 접수되었습니다. 감사합니다.',
    type: 'success',
    autoHide: true,
    duration: 2000
  });
};
</script>

<style scoped>
.jellyfish-report-page {
  /* 하단 버튼 공간 확보 (버튼 높이 + 패딩 고려) */
  min-height: calc(100vh - 55px - 60px);
  padding-bottom: 100px !important;
}
.form-control {
  border-radius: 0.75rem !important;
}
/* SOSSimpleReportPage와 동일한 고정 버튼 스타일 */
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
