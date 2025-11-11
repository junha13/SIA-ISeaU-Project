<template>
  <div class="event-detail-view container-fluid p-4" style="background-color: #F0F2F5; min-height: 100vh;">
    <div class="top-info-bar d-flex justify-content-between align-items-center mb-4 pb-3 border-bottom" style="border-color: #E0E2E6;">
      <div class="d-flex align-items-center">
        <h3 class="m-0 fw-bold text-secondary">
          <i class="bi bi-exclamation-triangle-fill me-2 text-danger"></i>
          접근 금지 위반 상세 (처리 필요)
        </h3>
        <div class="small fw-semibold d-flex gap-4 ms-5" style="color: #333333;">
          <div class="text-center">
            <span class="text-muted d-block small">감지 이벤트</span>
            <span class="fw-bold text-primary">접근 금지 위반</span>
          </div>
          <div class="text-center">
            <span class="text-muted d-block small">정확도</span>
            <span class="fw-bold" style="color: var(--color-safe);">96%</span>
          </div>
          <div class="text-center">
            <span class="text-muted d-block small">발생 시간</span>
            <span class="fw-bold">2023-11-29 06:26:27</span>
          </div>
          <div class="text-center">
            <span class="fw-bold">해양 안전 구역 A</span>
          </div>
        </div>
      </div>
      <button class="btn btn-link text-muted p-0" style="font-size: 1.5rem;">
        <i class="bi bi-x-lg"></i>
      </button>
    </div>

    <div class="row g-4">
      
      <div class="col-lg-8 d-flex flex-column" style="gap: 1.5rem;">
        
        <div class="row g-3 flex-shrink-0">
          <div
            v-for="cam in cams.slice(0, 4)"
            :key="cam.id"
            class="col-6"
          >
            <div
              class="video-container-grid overflow-hidden border" 
              :class="{
                'is-danger-light': cam.isDanger,
                'selected-frame-light': cam.id === selectedCam
              }"
              style="border-color: #EAECEF; cursor: pointer;"
              @click="selectedCam = cam.id"
            >
              <div class="video-header px-2 small fw-bold d-flex justify-content-between align-items-center"
                   style="background-color: var(--iseu-secondary); height: 30px;"
              >
                <span class="text-light">
                  <i class="bi bi-camera me-1"></i> CAM {{ cam.id }}
                </span>
                <span :class="{'text-white': cam.isDanger, 'text-light': !cam.isDanger}" class="small">
                    {{ cam.isDanger ? '긴급 이벤트' : '실시간' }}
                </span>
              </div>
              <div class="video-placeholder-grid d-flex align-items-center justify-content-center bg-dark text-white-50">
                <div>[Stream / Playback]</div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="card p-3 border-0 shadow-sm flex-shrink-0" style="background-color: #FFFFFF;">
          <h6 class="text-muted fw-bold mb-3 small">이벤트 로그 (Timeline)</h6>

          <div class="d-flex flex-column gap-2 log-scroll-area">
            <div
              class="log-entry p-2 small rounded fw-semibold"
              style="background-color: #F8F9FA; border-left: 4px solid var(--iseu-danger); color: #333;"
            >
              <span class="text-muted">[06:26:31]</span>
              <span class="text-danger">위반 행위 최종 확정</span>
            </div>
            <div
              class="log-entry p-2 small rounded fw-semibold"
              style="background-color: #F8F9FA; border-left: 4px solid var(--iseu-warning); color: #333;"
            >
              <span class="text-muted">[06:26:27]</span>
              <span class="text-warning">접근 금지 감지 시작</span>
            </div>
            <div
              class="log-entry p-2 small rounded fw-semibold"
              style="background-color: #F8F9FA; border-left: 4px solid var(--color-safe); color: #333;"
            >
              <span class="text-muted">[06:26:26]</span>
              <span style="color: var(--color-safe);">새로운 객체 영역 진입</span>
            </div>
            <div
              class="log-entry p-2 small rounded text-muted"
              style="background-color: #F8F9FA; border-left: 4px solid #EAECEF;"
            >
              <span class="text-muted">[06:26:20]</span> 시스템 활성화
            </div>
          </div>
        </div>
        
      </div>

      <div class="col-lg-4 d-flex flex-column" style="gap: 1.5rem;">
        
        <div class="d-flex justify-content-end align-items-end flex-shrink-0" style="height: 30px;">
            <button class="btn btn-sm btn-link p-0 fw-bold" style="color: #6c757d; margin-right: 0.5rem;">알림</button>
            <button class="btn btn-sm btn-link p-0 fw-bold" style="color: var(--iseu-primary);">상세</button>
        </div>
        
        <div class="card p-3 border-0 shadow-sm flex-grow-1" style="flex-grow: 2;">
          <h6 class="fw-bold mb-3 small" style="color: #333;">현장 지도 오버뷰</h6>
          <div
            class="map-placeholder-base border rounded d-flex align-items-center justify-content-center text-muted h-100"
            style="background-color: #F0F2F5;"
          >
            [지도/레이아웃 Placeholder]
          </div>
        </div>

        <div class="card p-3 border-0 shadow-sm flex-grow-1" style="flex-grow: 1;">
          <h6 class="fw-bold mb-3 small" style="color: #333;">감지 정보 통계</h6>
          <div
            class="chart-placeholder-base border rounded d-flex align-items-center justify-content-center text-muted h-100"
            style="background-color: #F0F2F5;"
          >
            [통계 그래프 Placeholder]
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
      <button class="btn btn-warning me-2 fw-bold text-dark">경고 알림 발송</button>
      <button class="btn btn-danger fw-bold text-white">이벤트 처리</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const cams = ref([
  // 2x2 그리드에 4개만 사용
  { id: 1, name: 'CAM 1', status: 'ok', detections: [1, 2], isDanger: true }, // 위반 발생
  { id: 2, name: 'CAM 2', status: 'ok', detections: [1], isDanger: false },
  { id: 3, name: 'CAM 3', status: 'ok', detections: [1, 2, 3], isDanger: false },
  { id: 4, name: 'CAM 4', status: 'error', detections: [], isDanger: false }, // 에러 상태
  { id: 5, name: 'CAM 5 (미사용)', status: 'ok', detections: [0], isDanger: false },
  { id: 6, name: 'CAM 6 (미사용)', status: 'ok', detections: [1], isDanger: false },
]);

const safeCamCount = computed(() => cams.value.filter(c => c.status === 'ok' && !c.isDanger).length);
const dangerCamCount = computed(() => cams.value.filter(c => c.isDanger).length);
const errorCamCount = computed(() => cams.value.filter(c => c.status === 'error').length);

// 기본 선택은 이벤트가 발생한 CAM 1
const selectedCam = ref(1); 
</script>

<style scoped>
:root {
  --iseu-primary: #0092BA;
  --iseu-secondary: #0B1956; /* 짙은 남색 */
  --iseu-warning: #FFB354;
  --iseu-danger: #EB725B;

  --color-safe: #8482FF;
  --color-light-bg: #F0F2F5;
  --color-panel-bg: #FFFFFF;
  --color-separator: #EAECEF;
  --color-text-dark: #333333;
}

@keyframes dangerGlow {
  0% {
    box-shadow: 0 0 8px 0 rgba(235, 114, 91, 0.7);
  }
  50% {
    box-shadow: 0 0 16px 0 rgba(235, 114, 91, 0.9);
  }
  100% {
    box-shadow: 0 0 8px 0 rgba(235, 114, 91, 0.7);
  }
}

.event-detail-view {
  font-family: 'Noto Sans KR', sans-serif;
  color: var(--color-text-dark);
}

.card {
  border-radius: 8px !important;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05) !important;
  background-color: var(--color-panel-bg) !important;
}

/* --- 2x2 그리드 비디오 컨테이너 --- */
.video-container-grid {
  /* 이미지와 같이 둥근 모서리와 그림자 제거 */
  border-radius: 0 !important;
  box-shadow: none !important;
  
  /* 16:9 비율 유지 */
  aspect-ratio: 16 / 9;
  width: 100%;
  height: auto;
  border: 1px solid var(--color-separator);
  transition: all 0.2s ease-in-out;
}

/* 이미지와 같이 짙은 남색 배경 */
.video-header {
  background-color: var(--iseu-secondary) !important; 
  /* 내부 padding을 통해 높이 30px을 유지하도록 조정 */
  padding: 0.5rem 0.75rem !important;
}

.video-placeholder-grid {
  /* 플레이스 홀더가 컨테이너 비율을 채우도록 설정 */
  width: 100%;
  height: calc(100% - 30px); /* 헤더 높이(30px) 제외 */
  background-color: #000000 !important;
  font-size: 0.9rem;
}
/* --- --- */

/* 지도 및 통계 컨테이너 높이 조정 (우측 패널) */

/* Flexbox를 사용하여 내부 요소를 균등하게 채우므로, 개별 height 속성은 제거함 */
.map-placeholder-base {
    /* height 속성 제거 */
    font-size: 1rem;
}
.chart-placeholder-base {
    /* height 속성 제거 */
    font-size: 1rem;
}

/* 로그 스크롤 영역 높이 제한 */
.log-scroll-area {
    /* 로그가 스크롤 되도록 높이 제한 (픽셀 조정) */
    max-height: 150px; 
    overflow-y: auto;
}

/* 선택 프레임 강조 */
.selected-frame-light {
  border: 3px solid var(--iseu-primary) !important;
  box-shadow: 0 0 0 2px rgba(0, 146, 186, 0.5);
}

/* 경각심 적용 */
.is-danger-light {
  border: 5px solid var(--iseu-danger) !important;
  animation: dangerGlow 2s infinite ease-in-out;
}

/* Form element styling */
.form-check-input {
  background-color: #fff;
  border-color: #ccc;
}
.form-check-input:checked {
  background-color: var(--iseu-primary);
  border-color: var(--iseu-primary);
}

/* 버튼 텍스트 색상 */
.btn-warning {
  color: var(--color-text-dark) !important;
}
.btn-danger {
  color: white !important;
}
</style>