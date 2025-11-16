<template>
  <div class="report-detail container-fluid p-3" style="background-color: #F8F9FA;">
    <div class="row">
      
      <div class="col-lg-4 mb-4 mb-lg-0">
        <h4 class="mb-3 text-secondary">활성 신고 리스트</h4> 
        <div class="list-group" style="height: 700px; overflow-y: auto;">
          <a
              href="#"
              v-for="report in activeReports"
              :key="report.id"
              class="list-group-item list-group-item-action py-3 border-start border-5"
              :class="[
                  // 선택된 리포트는 주 컬러로 강조
                  selectedReport.id === report.id ? 'bg-primary-light-active border-primary-light' : 'bg-light-card border-light-card-border', 
                  getListBorderClass(report.level)
              ]"
              @click.prevent="selectReport(report)"
          >
            <div class="d-flex w-100 justify-content-between">
              <span class="badge rounded-pill me-2 fw-bold" :class="getBadgeClass(report.level)">
                {{ report.level.toUpperCase() }}
              </span>
              <small class="text-muted">{{ report.time }}</small>
            </div>
            <div class="mt-1 small text-dark">
              <strong>{{ report.age }}세 {{ report.gender }}</strong>
              <span class="d-block text-truncate text-secondary-default">유형: {{ report.type }}</span>
            </div>
          </a>
          <p v-if="activeReports.length === 0" class="text-center text-muted mt-5">현재 활성 신고가 없습니다.</p>
        </div>
      </div>

      <div class="col-lg-8" v-if="selectedReport">
        <h4 class="mb-3 text-secondary">신고 상세</h4> 
        <div class="row g-3">
          
          <div class="col-md-6">
            <div class="map-placeholder bg-light rounded d-flex align-items-center justify-content-center border" style="height: 400px; border-color: #EAECEF !important;">
              <span class="text-muted">지도에 신고자 위치 (API 연동 필요)</span>
            </div>
          </div>

          <div class="col-md-6">
            <div class="detail-card card h-100 border-0" style="background-color: #FFFFFF;">
              <div class="card-header border-0 pb-0" style="background-color: #FFFFFF; border-color: #EAECEF !important;">
                <h5 class="card-title fw-bold" :class="getAlertColor(selectedReport.level)">
                    {{ selectedReport.type }}
                </h5>
                <small class="text-muted">발생: {{ selectedReport.date }} {{ selectedReport.time }}</small>
              </div>
              <div class="card-body text-dark">
                <h6 class="fw-bold text-secondary-default">생체 신호 및 위험도</h6>
                <p class="mb-2">
                  <strong class="me-2 text-muted">신고인:</strong>
                  <span>{{ selectedReport.age }}세 {{ selectedReport.gender }}</span>
                </p>
                <hr style="border-color: #EAECEF;">
                <p class="mb-2">
                  <strong class="me-2 text-muted">심박수:</strong>
                  <span class="fw-bold" :class="{'text-danger': selectedReport.hr > 150}">{{ selectedReport.hr }} BPM</span>
                </p>
                <p class="mb-2">
                  <strong class="me-2 text-muted">산소포화도:</strong>
                  <span class="fw-bold" :class="getOxygenClass(selectedReport.spo2)">{{ selectedReport.spo2 }}%</span>
                </p>
                <p class="mb-2">
                  <strong class="me-2 text-muted">Z축 변화 (낙상):</strong>
                  <span class="fw-bold" :class="{'text-emergency-custom': selectedReport.zChange === '급격'}">{{ selectedReport.zChange }}</span>
                </p>
              </div>
              <div class="card-footer border-0 d-flex justify-content-end" style="background-color: #F8F9FA; border-color: #EAECEF !important;">
                <button class="btn btn-sm btn-outline-secondary">신고 접수</button>
                <button class="btn btn-sm ms-2" :class="getButtonClass(selectedReport.level)">출동 확인</button>
              </div>
            </div>
          </div>

          <div class="col-md-6 mt-4">
              <div class="card p-3 border-0" style="background-color: #FFFFFF;">
                  <h6 class="fw-bold text-secondary-default mb-3"><i class="bi bi-image me-2"></i>신고 관련 자료</h6>
                  <div class="video-placeholder-small bg-light rounded d-flex align-items-center justify-content-center border" style="background-color: #F0F2F5 !important;">
                      <span class="text-muted">신고 시 첨부된 이미지/영상 (Placeholder)</span>
                  </div>
              </div>
          </div>
          
          <div class="col-md-6 mt-4">
              <div class="card p-3 border-0" style="background-color: #FFFFFF;">
                  <h6 class="fw-bold text-secondary-default mb-3"><i class="bi bi-journal-text me-2"></i>상황 기록 로그</h6>
                  <div class="log-area small" style="height: 150px; overflow-y: auto; background-color: #F8F9FA; padding: 10px; border-radius: 6px; border: 1px solid #EAECEF;">
                      <p class="mb-0 text-dark fw-semibold">[{{ selectedReport.time }}] 신고 접수 완료 (레벨: {{ selectedReport.level.toUpperCase() }})</p>
                      <p class="mb-0 text-muted">[{{ selectedReport.time }}] 심박수 {{ selectedReport.hr }} BPM, 산소포화도 {{ selectedReport.spo2 }}% 기록</p>
                      <p class="mb-0 text-dark">[{{ selectedReport.time }}] 최단거리 출동 경로 탐색 완료</p> 
                      <p class="mb-0" :class="getAlertColor(selectedReport.level)">[{{ selectedReport.time }}] **{{ selectedReport.type }}** 발생 확인. 위치: {{ selectedReport.location }}</p>
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

const activeReports = ref([
  { id: 1, type: '생체 신호 위험 (저산소)', level: 'danger', date: '2025-11-10', time: '11:24:32', age: 23, gender: '남성', hr: 160, spo2: 92, zChange: '정상', location: '제1구역 인근' },
  { id: 2, type: '위해 생물 물림', level: 'emergency', date: '2025-11-10', time: '10:05:10', age: 10, gender: '여성', hr: 95, spo2: 98, zChange: '정상', location: '백사장 중앙' },
  { id: 3, type: '아이 위치 미확인', level: 'warning', date: '2025-11-10', time: '09:40:00', age: 40, gender: '여성', hr: 70, spo2: 99, zChange: '정상', location: '부모님 신고' },
  // --- 추가된 더미 데이터 (5개) ---
  { id: 4, type: '낙상 사고 감지', level: 'emergency', date: '2025-11-10', time: '12:15:00', age: 72, gender: '남성', hr: 130, spo2: 91, zChange: '급격', location: '제2구역 벤치' },
  { id: 5, type: '심박 급상승 (공황)', level: 'danger', date: '2025-11-10', time: '13:30:45', age: 35, gender: '여성', hr: 185, spo2: 98, zChange: '정상', location: '해변 산책로' },
  { id: 6, type: '장시간 움직임 없음', level: 'warning', date: '2025-11-10', time: '14:55:00', age: 50, gender: '남성', hr: 60, spo2: 99, zChange: '정상', location: '파라솔 아래' },
  { id: 7, type: '위치 이탈 (통제선)', level: 'danger', date: '2025-11-10', time: '15:10:20', age: 28, gender: '여성', hr: 80, spo2: 97, zChange: '정상', location: '제3구역 외곽' },
  { id: 8, type: '배터리/신호 저하', level: 'warning', date: '2025-11-10', time: '16:02:55', age: 55, gender: '남성', hr: 80, spo2: 98, zChange: '정상', location: '시스템 알림' },
  // ------------------------------
]);

const selectedReport = ref(activeReports.value[0]);

const selectReport = (report) => {
  selectedReport.value = report;
};

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

// 출동 확인 버튼 색상 반환
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
  // Danger (<=95), Emergency (<=92)
  if (spo2 <= 92) return 'text-emergency-custom';
  if (spo2 <= 95) return 'text-danger-custom';
  return 'text-safety-custom'; // 안전 기준은 안전 컬러 (#7EEC85)
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
</style>