<template>
  <div class="event-detail-view container-fluid p-4" style="background-color: #F0F2F5; min-height: 100vh;">

    <div class="top-info-bar d-flex justify-content-between align-items-center mb-4 pb-3 border-bottom" style="border-color: #E0E2E6;">
      <div class="d-flex align-items-center">
        <h3 class="m-0 fw-bold" style="color: #0B1956;">
          <i class="bi bi-exclamation-triangle-fill me-2" style="color: #EB725B;"></i>
          접근 금지 위반 상세 (처리 필요)
        </h3>
        <div class="small fw-semibold d-flex gap-4 ms-5" style="color: #333333;">
            <div class="text-center">
                <span class="text-muted d-block small">감지 이벤트</span>
                <span class="fw-bold" style="color: #0092BA;">접근 금지 위반</span>
            </div>
            <div class="text-center">
                <span class="text-muted d-block small">정확도</span>
                <span class="fw-bold" style="color: #8482FF;">96%</span>
            </div>
            <div class="text-center">
                <span class="text-muted d-block small">발생 시간</span>
                <span class="fw-bold">2023-11-29 06:26:27</span>
            </div>
            <div class="text-center">
                <span class="text-muted d-block small">발생 구역</span>
                <span class="fw-bold">해양 안전 구역 A</span>
            </div>
        </div>
      </div>
      <button class="btn btn-link text-muted p-0" style="font-size: 1.5rem;">
        <i class="bi bi-x-lg"></i>
      </button>
    </div>

    <div class="row g-4">

      <div class="col-lg-8">

        <div class="row g-3">
            
            <div v-for="cam in cams.slice(0, 4)" :key="cam.id" class="col-6">
                 <div 
                    class="video-container-grid rounded overflow-hidden shadow-sm border" 
                    :class="{'is-danger-light': cam.isDanger, 'selected-frame-light': selectedCam === cam.id}"
                    style="border-color: #EAECEF;"
                    @click="selectedCam = cam.id"
                 >
                    <div class="video-header p-2 small fw-bold text-white d-flex justify-content-between" style="background-color: #0B1956;">
                        <span :style="{'color': cam.isDanger ? '#EB725B' : '#8482FF'}"><i class="bi bi-camera me-1"></i> CAM {{ cam.id }}</span>
                        <span class="text-white-50">{{ cam.isDanger ? '긴급 이벤트' : '실시간' }}</span>
                    </div>
                    <div class="video-placeholder-grid d-flex align-items-center justify-content-center bg-dark text-white-50">
                        <div>[Stream / Playback]</div>
                    </div>
                 </div>
            </div>

            <div class="col-6">
                <div class="card p-3 h-100 border-0 shadow-sm" style="background-color: #FFFFFF;">
                    <h6 class="fw-bold mb-3 small" style="color: #333;">현장 지도 오버뷰</h6>
                    <div class="map-placeholder-base border rounded h-100 d-flex align-items-center justify-content-center text-muted" style="background-color: #F0F2F5;">
                        [지도/레이아웃 Placeholder]
                    </div>
                </div>
            </div>
            <div class="col-6">
                <div class="card p-3 h-100 border-0 shadow-sm" style="background-color: #FFFFFF;">
                    <h6 class="fw-bold mb-3 small" style="color: #333;">감지 정보 통계</h6>
                    <div class="chart-placeholder-base border rounded h-100 d-flex align-items-center justify-content-center text-muted" style="background-color: #F0F2F5;">
                        [통계 그래프 Placeholder]
                    </div>
                </div>
            </div>

        </div>
      </div>

      <div class="col-lg-4">

        <div class="card p-3 border-0 shadow-sm" style="background-color: #FFFFFF;">
            <h6 class="text-muted fw-bold mb-3 small">주변 카메라 ({{ cams.length }})</h6>
            <div class="text-center text-muted p-4 border rounded" style="background-color: #F8F9FA; border-color: #EAECEF !important;">
                <i class="bi bi-list-nested d-block mb-2 fs-3"></i>
                <small>카메라 목록 상세 뷰 Placeholder</small>
            </div>
            
            <h6 class="text-muted fw-bold mt-4 mb-3 small">이벤트 로그</h6>
            <div class="log-entry p-2 small rounded mb-1 fw-semibold" style="background-color: #F8F9FA; border-left: 4px solid #EB725B; color: #333;">
                <span class="text-muted">[06:26:31]</span> <span style="color: #EB725B;">위반 행위 최종 확정</span>
            </div>
            <div class="log-entry p-2 small rounded mb-1 fw-semibold" style="background-color: #F8F9FA; border-left: 4px solid #FFB354; color: #333;">
                <span class="text-muted">[06:26:27]</span> <span style="color: #FFB354;">접근 금지 감지 시작</span>
            </div>
             <div class="log-entry p-2 small rounded fw-semibold" style="background-color: #F8F9FA; border-left: 4px solid #8482FF; color: #333;">
                <span class="text-muted">[06:26:26]</span> <span style="color: #8482FF;">새로운 객체 영역 진입</span>
            </div>
        </div>

      </div>
    </div>

    <div class="action-bar fixed-bottom d-flex justify-content-end p-3 border-top" style="background-color: #FFFFFF; border-color: #EAECEF !important;">
        <div class="me-auto text-dark fw-semibold small d-flex align-items-center">
            <span class="me-3">처리 상태:</span>
            <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" name="actionStatus" id="actionPending" checked>
                <label class="form-check-label text-dark" for="actionPending">미처리</label>
            </div>
            <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" name="actionStatus" id="actionProcessing">
                <label class="form-check-label text-dark" for="actionProcessing">처리 중</label>
            </div>
            <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" name="actionStatus" id="actionComplete">
                <label class="form-check-label text-dark" for="actionComplete">완료</label>
            </div>
        </div>
        <button class="btn btn-outline-secondary me-2">영상 다운로드</button>
        <button class="btn btn-warning me-2 fw-bold" style="background-color: #FFB354; border-color: #FFB354;">경고 알림 발송</button>
        <button class="btn btn-danger fw-bold" style="background-color: #EB725B; border-color: #EB725B;">이벤트 처리</button>
    </div>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const cams = ref([
  { id: 1, name: 'CAM 1', status: 'ok', detections: [1, 2], isDanger: true }, // 이벤트 발생 영상
  { id: 2, name: 'CAM 2', status: 'ok', detections: [1], isDanger: false }, // 주변 1
  { id: 3, name: 'CAM 3', status: 'ok', detections: [1, 2, 3], isDanger: false }, // 주변 2
  { id: 4, name: 'CAM 4', status: 'error', detections: [], isDanger: false }, // 주변 3
  { id: 5, name: 'CAM 5', status: 'ok', detections: [0], isDanger: false },
  { id: 6, name: 'CAM 6', status: 'ok', detections: [1], isDanger: false },
]);

const safeCamCount = computed(() => cams.value.filter(c => c.status === 'ok' && !c.isDanger).length);
const dangerCamCount = computed(() => cams.value.filter(c => c.isDanger).length);
const errorCamCount = computed(() => cams.value.filter(c => c.status === 'error').length);
const selectedCam = ref(1); 
</script>

<style scoped>
/* Color Variables */
:root {
  --color-main: #0092BA; /* 주 컬러 */
  --color-dark-bg: #0B1956; /* 캠 헤더/고대비 배경 */
  --color-safe: #8482FF; /* 안전 */
  --color-warning: #FFB354; /* 주의 */
  --color-danger: #EB725B; /* 경고 */
  --color-light-bg: #F0F2F5; 
  --color-panel-bg: #FFFFFF; 
  --color-separator: #EAECEF;
  --color-text-dark: #333333;
}

/* 1. 위험 프레임 지속적인 글로우 애니메이션 (경각심 유지) */
@keyframes dangerGlow {
    0% { box-shadow: 0 0 10px 0 rgba(235, 114, 91, 0.7); }
    50% { box-shadow: 0 0 20px 0 rgba(235, 114, 91, 0.9); }
    100% { box-shadow: 0 0 10px 0 rgba(235, 114, 91, 0.7); }
}

.event-detail-view {
    font-family: 'Noto Sans KR', sans-serif; /* 폰트 변경: Noto Sans KR */
    color: var(--color-text-dark);
}

.card {
    border-radius: 8px !important;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05) !important;
    background-color: var(--color-panel-bg) !important;
}

/* --- 영상 컨테이너 (2x2) --- */
.video-container-grid {
    background-color: var(--color-dark-bg);
    border: 1px solid var(--color-separator);
    min-height: 150px; 
}
.video-placeholder-grid {
    /* 16:9 비율 유지 */
    aspect-ratio: 16 / 9; 
    width: 100%;
    height: 100%;
    background-color: #000000 !important;
}

.video-header {
    background-color: var(--color-dark-bg);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    color: white !important;
}
/* --- --- */

/* 선택 프레임 강조 */
.selected-frame-light {
    border: 3px solid var(--color-main) !important;
    box-shadow: 0 0 0 2px rgba(0, 146, 186, 0.5);
}

/* 경각심 적용 */
.is-danger-light {
    border: 5px solid var(--color-danger) !important; 
    animation: dangerGlow 2s infinite ease-in-out;
}
.is-error-light {
    border: 3px solid var(--color-warning) !important;
    box-shadow: 0 0 8px rgba(255, 179, 84, 0.5);
}

.danger-overlay-ref {
    /* 오버레이 CSS는 템플릿에서 제거되었으나, 정의는 유지 */
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background-color: rgba(235, 114, 91, 0.95); 
    z-index: 10;
    font-size: 1rem;
    text-shadow: 0 0 5px rgba(0, 0, 0, 0.8);
}

/* ETC UI ELEMENTS */
.map-thumbnail-overlay {
    font-size: 0.75rem;
    cursor: pointer;
    color: var(--color-text-muted);
}
.chart-placeholder-base {
    height: 150px;
    background-color: #F0F2F5 !important;
}

/* Form element styling for bright mode */
.form-check-input {
    background-color: #fff;
    border-color: #ccc;
}

.form-check-input:checked {
    background-color: var(--color-main);
    border-color: var(--color-main);
}

.btn-warning {
    color: var(--color-text-dark) !important; 
}
.btn-danger {
    color: white !important;
}
</style>