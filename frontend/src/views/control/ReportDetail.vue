<template>
  <div class="report-detail container-fluid p-3" style="background-color: #F8F9FA;">
    <div class="row">

      <!-- Left: Active reports list -->
      <div class="col-lg-4 mb-4 mb-lg-0">
        <h4 class="mb-3 text-secondary">활성 신고 리스트</h4>
        <div class="list-group" style="height: 700px; overflow-y: auto;">
          <a
            href="#"
            v-for="report in activeReports"
            :key="report.id"
            class="list-group-item list-group-item-action py-3 border-start border-5 report-list-item"
            :class="[
              selectedReport.id === report.id ? 'bg-primary-light-active border-primary-light' : 'bg-light-card border-light-card-border',
              getListBorderClass(report.level)
            ]"
            @click.prevent="selectReport(report)"
          >
            <div class="d-flex w-100 justify-content-between">
              <span class="badge rounded-pill me-2 fw-bold" :class="getBadgeClass(report.level)">
                {{ report.level.toUpperCase() }}
              </span>
            </div>
            <div class="mt-1 small text-dark">
              <strong>{{ report.age }}세 {{ report.gender }}</strong>
              <small class="text-muted">{{ report.time }}</small>
            </div>
            <div class="report-type-badge">{{ report.type }}</div>
          </a>
          <p v-if="activeReports.length === 0" class="text-center text-muted mt-5">현재 활성 신고가 없습니다.</p>
        </div>
      </div>

      <!-- Right: Detail panel -->
      <div class="col-lg-8" v-if="selectedReport">
        <h4 class="mb-3 text-secondary">신고 상세</h4>
        <div class="row g-3">

          <div class="col-md-6">
            <div class="map-placeholder bg-light rounded d-flex align-items-center justify-content-center border" style="height: 400px; border-color: #EAECEF !important;">
              <span class="text-muted">지도에 신고자 위치 (API 연동 필요)</span>
            </div>
          </div>

          <div class="col-md-6">
            <div :class="['detail-card card h-100 border-0', { 'flash-highlight': highlight }]" style="background-color: #FFFFFF;">
              <div class="card-header border-0 pb-0" style="background-color: #FFFFFF; border-color: #EAECEF !important;">
                <div class="d-flex w-100 justify-content-between align-items-start">
                  <div class="pt-3">
                    <h5 class="card-title fw-bold mb-1" :class="getAlertColor(selectedReport.level)">{{ selectedReport.type }}</h5>
                    <div class="text-secondary-default small">신고자 정보 · 빠른 확인</div>
                  </div>

                  <div class="report-datetime-box text-end">
                    <div class="d-flex align-items-center justify-content-end">
                      <i class="bi bi-calendar-event me-2 text-info-custom"></i>
                      <div class="fw-semibold fs-6">{{ selectedReport.date }}</div>
                    </div>
                    <div class="text-muted small mono-time">{{ selectedReport.time }}</div>
                  </div>
                </div>
              </div>

              <!-- Updated 신고 정보 영역 -->
              <div class="card-body text-dark">

                <div class="info-grid row gy-3">
                  
                  <div class="col-6">
                    <div class="d-flex align-items-center">
                      <i class="bi bi-person-fill info-icon text-muted me-2" title="신고인"></i>
                      <div class="info-value fw-bold">{{ selectedReport.age }}세 · {{ selectedReport.gender }}</div>
                    </div>
                  </div>

                  <div class="col-6">
                    <div class="d-flex align-items-center">
                      <i class="bi bi-geo-alt info-icon text-muted me-2" title="위치"></i>
                      <div class="info-value fw-bold text-truncate">{{ selectedReport.location || '위치 정보 없음' }}</div>
                    </div>
                  </div>


                  <div class="col-6 pt-8">
                    <div class="d-flex align-items-center">
                      <i class="bi bi-heart-pulse info-icon text-muted me-2" title="심박수"></i>
                      <div class="info-value fw-bold d-flex align-items-center">
                        <span :class="{'text-danger': selectedReport.hr > 150, 'me-2': true}">{{ selectedReport.hr }} BPM</span>
                        <small class="ms-2 badge bg-light border text-muted">실시간</small>
                      </div>
                    </div>
                  </div>

                </div>
              </div>

              <div class="card-footer border-0 d-flex justify-content-end" style="background-color: #F8F9FA; border-color: #EAECEF !important;">
                <button
                  class="btn btn-sm me-2"
                  :class="['btn-receive', { 'btn-disabled': isReceiveDisabled(selectedReport) }]"
                  :disabled="isReceiveDisabled(selectedReport)"
                  @click="receiveReport(selectedReport)"
                >
                  신고 접수
                </button>

                <button
                  class="btn btn-sm ms-2"
                  :class="[ isDispatchDisabled(selectedReport) ? 'btn-dispatch-disabled' : getButtonClass(selectedReport.level) ]"
                  :disabled="isDispatchDisabled(selectedReport)"
                  @click="confirmDispatch(selectedReport)"
                >
                  {{ dispatchButtonText(selectedReport) }}
                </button>
              </div>
            </div>
          </div>

          <div class="col-md-6 mt-4">
            <div class="card p-3 border-0" style="background-color: #FFFFFF;">
              <h6 class="fw-bold text-secondary-default mb-3"><i class="bi bi-journal-text me-2"></i>상황 기록 로그</h6>
              <div class="log-area small" style="height: 150px; overflow-y: auto; background-color: #F8F9FA; padding: 10px; border-radius: 6px; border: 1px solid #EAECEF;">
                <div class="log-item mb-1"><span class="log-time text-muted">[{{ selectedReport.time }}]</span> <span class="fw-semibold text-dark">신고 접수 완료 (레벨: {{ selectedReport.level.toUpperCase() }})</span></div>
                <div class="log-item mb-1"><span class="log-time text-muted">[{{ selectedReport.time }}]</span> <span class="text-muted">심박수 {{ selectedReport.hr }} BPM, 산소포화도 {{ selectedReport.spo2 }}% 기록</span></div>
                <div class="log-item mb-1"><span class="log-time text-muted">[{{ selectedReport.time }}]</span> <span class="text-dark">최단거리 출동 경로 탐색 완료</span></div>
                <div class="log-item"><span class="log-time text-muted">[{{ selectedReport.time }}]</span> <span :class="getAlertColor(selectedReport.level)">**{{ selectedReport.type }}** 발생 확인. 위치: {{ selectedReport.location }}</span></div>
              </div>
            </div>
          </div>

        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

// activeReports에 상태 필드(status)와 processed 플래그를 추가합니다.
// status: 'initial' -> 'received' -> 'completed'
const activeReports = ref([
  { id: 1, type: '자동', level: 'danger', date: '2025-11-10', time: '11:24:32', age: 23, gender: '남성', hr: 160, spo2: 92, zChange: '정상', location: '제1구역 인근', status: 'initial', processed: false },
  { id: 2, type: '자동', level: 'emergency', date: '2025-11-10', time: '10:05:10', age: 10, gender: '여성', hr: 95, spo2: 98, zChange: '정상', location: '백사장 중앙', status: 'initial', processed: false },
  { id: 3, type: '수동(119)', level: 'warning', date: '2025-11-10', time: '09:40:00', age: 40, gender: '여성', hr: 70, spo2: 99, zChange: '정상', location: '부모님 신고', status: 'initial', processed: false },
  { id: 4, type: '수동(라이프가드)', level: 'emergency', date: '2025-11-10', time: '12:15:00', age: 72, gender: '남성', hr: 130, spo2: 91, zChange: '급격', location: '제2구역 벤치', status: 'initial', processed: false },
  { id: 5, type: '수동(112)', level: 'danger', date: '2025-11-10', time: '13:30:45', age: 35, gender: '여성', hr: 185, spo2: 98, zChange: '정상', location: '해변 산책로', status: 'initial', processed: false },
  { id: 6, type: '수동(해파리)', level: 'warning', date: '2025-11-10', time: '14:55:00', age: 50, gender: '남성', hr: 60, spo2: 99, zChange: '정상', location: '파라솔 아래', status: 'initial', processed: false },
  { id: 7, type: '수동(112)', level: 'danger', date: '2025-11-10', time: '15:10:20', age: 28, gender: '여성', hr: 80, spo2: 97, zChange: '정상', location: '제3구역 외곽', status: 'initial', processed: false },
  { id: 8, type: '수동(119)', level: 'warning', date: '2025-11-10', time: '16:02:55', age: 55, gender: '남성', hr: 80, spo2: 98, zChange: '정상', location: '시스템 알림', status: 'initial', processed: false },
]);

const selectedReport = ref(activeReports.value[0]);

// 상세영역 하이라이트 상태 및 타이머
const highlight = ref(false);
let highlightTimer = null;

const selectReport = (report) => {
  selectedReport.value = report;
  // 트리거 애니메이션
  highlight.value = false; // reflow for retrigger
  requestAnimationFrame(() => {
    highlight.value = true;
    if (highlightTimer) clearTimeout(highlightTimer);
    highlightTimer = setTimeout(() => {
      highlight.value = false;
      highlightTimer = null;
    }, 700);
  });
};

// 버튼 상태 제어 함수들
const receiveReport = (report) => {
  if (!report || report.status !== 'initial') return;
  report.status = 'received';
};

const confirmDispatch = (report) => {
  if (!report) return;
  // '출동 확인'은 'received' 상태에서만 동작
  if (report.status === 'received') {
    report.status = 'completed';
    report.processed = true;
  }
};

const isReceiveDisabled = (report) => !report || report.status !== 'initial';
const isDispatchDisabled = (report) => !report || report.status !== 'received';
const dispatchButtonText = (report) => (report && report.status === 'completed') ? '처리완료' : '출동 확인';

// --- Color Mapping (Using custom classes based on new palette) ---

// 위험 단계에 따른 뱃지 색상 클래스 반환
const getBadgeClass = (level) => {
  switch (level) {
    case 'warning': return 'bg-warning-custom text-dark';
    case 'danger': return 'bg-danger-custom';
    case 'emergency': return 'bg-emergency-custom';
    default: return 'bg-safety-custom'; // 안전/관심은 #7EEC85
  }
};

// 리스트 좌측 보더 색상 클래스 반환
const getListBorderClass = (level) => {
  switch (level) {
    case 'warning': return 'border-warning-custom';
    case 'danger': return 'border-danger-custom';
    case 'emergency': return 'border-emergency-custom';
    default: return 'border-safety-custom';
  }
};

// 신고 제목 및 텍스트 색상 반환
const getAlertColor = (level) => {
  switch (level) {
    case 'warning': return 'text-warning-custom';
    case 'danger': return 'text-danger-custom';
    case 'emergency': return 'text-emergency-custom';
    default: return 'text-safety-custom';
  }
}

// 출동 확인 버튼 색상 반환 (활성 시에만 적용)
const getButtonClass = (level) => {
    switch (level) {
        case 'emergency': return 'btn-emergency-custom';
        case 'danger': return 'btn-danger-custom';
        case 'warning': return 'btn-warning-custom text-dark';
        default: return 'btn-safety-custom';
    }
}

// 산소 포화도에 따른 텍스트 색상 클래스 반환
const getOxygenClass = (spo2) => {
  if (spo2 <= 92) return 'text-emergency-custom';
  if (spo2 <= 95) return 'text-danger-custom';
  return 'text-safety-custom';
};
</script>

<style scoped>
/* --- NEW COLOR PALETTE MAPPING --- */
/* Palette: #0092BA (Primary), #7EEC85 (Safety), #FFB354 (Warning/주의), #EB725B (Danger/경고), #B93F67 (Emergency/위험), #8482FF (Info/보조파랑) */

/* Safety/Default (안전) */
.text-safety-custom { color: #7EEC85 !important; }
.bg-safety-custom { background-color: #7EEC85 !important; color: #333 !important; }
.border-safety-custom { border-color: #7EEC85 !important; }
.btn-safety-custom { background-color: #7EEC85; border-color: #7EEC85; color: #333; }

/* Warning (주의) */
.text-warning-custom { color: #FFB354 !important; }
.bg-warning-custom { background-color: #FFB354 !important; }
.border-warning-custom { border-color: #FFB354 !important; }
.btn-warning-custom { background-color: #FFB354; border-color: #FFB354; color: #333; }

/* Danger (경고) */
.text-danger-custom { color: #EB725B !important; }
.bg-danger-custom { background-color: #EB725B !important; color: white !important; }
.border-danger-custom { border-color: #EB725B !important; }
.btn-danger-custom { background-color: #EB725B; border-color: #EB725B; color: white; }

/* Emergency (위험) */
.text-emergency-custom { color: #B93F67 !important; }
.bg-emergency-custom { background-color: #B93F67 !important; color: white !important; }
.border-emergency-custom { border-color: #B93F67 !important; }
.btn-emergency-custom { background-color: #B93F67; border-color: #B93F67; color: white; }

/* --- Secondary Blue Accent (모두 검은색으로 통일) --- */
.text-info-custom { color: #212529 !important; } /* 파랑 계열 -> 검정 */
.text-secondary-default { color: #212529 !important; } /* 주 컬러/Secondary -> 검정 */

/* Primary Accent */
.bg-primary-light-active {
    /* Selected item background: Primary color + 10% opacity */
    background-color: rgba(0, 146, 186, 0.1) !important;
}
.border-primary-light {
    border-color: #0092BA !important;
}
.bg-light-card {
    background-color: #FFFFFF !important;
    border-color: #EAECEF !important;
}
.list-group-item:hover {
  cursor: pointer;
  background-color: #F8F9FA !important;
}

/* 리스트 항목 내부: 유형을 우측 하단의 버튼 스타일로 배치 */
.report-list-item { position: relative; }
.report-type-badge {
  position: absolute;
  right: 12px;
  bottom: 10px;
  background: #F1F3F5; /* 연한 회색 배경 */
  color: #212529;      /* 짙은 텍스트 */
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 600;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
  border: 1px solid #E6E9EE;
}
.report-type-badge:hover { background: #E9ECEF; transform: translateY(-1px); }

/* LAYOUT AND UTILITY */
.report-detail {
    font-family: 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
.list-group-item {
    transition: all 0.2s;
}
.map-placeholder {
  font-size: 0.9rem;
  color: #6c757d;
}
.video-placeholder-small {
    height: 180px; 
    font-size: 0.85rem;
}
.text-dark {
    color: #212529 !important; 
}

.report-datetime-box {
  background-color: #F8FAFC;
  border: 1px solid #E6EEF5;
  padding: 8px 12px;
  border-radius: 8px;
  min-width: 140px;
}
.mono-time {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, 'Roboto Mono', 'Noto Sans Mono', monospace;
}

.detail-card .small.text-muted {
  color: #6c757d !important;
}

.detail-card .fw-bold { font-size: 0.98rem; }

/* 상세 영역 선택 시 잠깐 반짝이는 하이라이트 */
.flash-highlight {
  animation: flash-highlight 0.7s ease-in-out;
}
@keyframes flash-highlight {
  0% { box-shadow: 0 0 0 rgba(0,0,0,0); background-color: #ffffff; }
  10% { background-color: rgba(0,146,186,0.12); }
  40% { background-color: rgba(0,146,186,0.06); }
  100% { background-color: #ffffff; }
}

/* Button styles for workflow:
   1) 신고 접수: 활성(파란색), 비활성시 흐리게
   2) 출동 확인: 기본적으로 비활성, 활성 시 레벨 기반 클래스 사용
   3) 출동 확인이 완료되면 '처리완료' 텍스트로 비활성화
*/
.btn-receive {
  background-color: #0092BA;
  color: #fff;
  border-color: #0092BA;
}
.btn-receive.btn-disabled,
.btn-receive:disabled {
  opacity: 0.65;
  pointer-events: none;
}

.btn-dispatch-disabled {
  background-color: #F1F3F5;
  color: #6c757d;
  border-color: #E6E9EE;
}

.btn-disabled {
  opacity: 0.65;
  pointer-events: none;
}

.btn-dispatch-disabled:hover { transform: none; }

/* Info grid and status badge */
.info-grid .info-label { color: #6c757d; }
.info-grid .info-value { color: #212529; }

.log-item { font-size: 0.9rem; }
.log-time { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, 'Roboto Mono', 'Noto Sans Mono', monospace; margin-right: 6px; }

/* Iconized labels for compact readability */
.info-icon { font-size: 1.4rem; width: 40px; text-align: center; color: #6c757d; }
.info-grid .d-flex > .info-value { min-width: 0; }
.info-grid .info-value.text-truncate { max-width: calc(100% - 44px); }
@media (max-width: 768px) {
  .info-icon { font-size: 1.2rem; width: 34px; }
  .info-grid .info-value.text-truncate { max-width: calc(100% - 38px); }
}

.card-header { position: relative; }
.report-datetime-box {
  position: absolute;
  right: 16px;
  top: 16px;
  text-align: right;
  display: inline-flex;
  flex-direction: column;
  align-items: flex-end;
}
.mono-time { white-space: nowrap; }
</style>