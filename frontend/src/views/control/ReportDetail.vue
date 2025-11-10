<template>
  <div class="report-detail container-fluid p-3">
    <div class="row">
      <div class="col-lg-4 mb-3 mb-lg-0">
        <h4 class="mb-3 text-secondary">활성 신고 리스트</h4>
        <div class="list-group" style="height: 700px; overflow-y: auto;">
          <a
              href="#"
              v-for="report in activeReports"
              :key="report.id"
              class="list-group-item list-group-item-action py-3 border-start border-5"
              :class="[selectedReport.id === report.id ? 'bg-primary bg-opacity-10 border-primary' : 'border-light', getListBorderClass(report.level)]"
              @click.prevent="selectReport(report)"
          >
            <div class="d-flex w-100 justify-content-between">
              <span class="badge rounded-pill me-2" :class="getBadgeClass(report.level)">
                {{ report.level.toUpperCase() }}
              </span>
              <small class="text-muted">{{ report.time }}</small>
            </div>
            <div class="mt-1 small">
              <strong>{{ report.age }}세 {{ report.gender }}</strong>
              <span class="d-block text-truncate">유형: {{ report.type }}</span>
            </div>
          </a>
          <p v-if="activeReports.length === 0" class="text-center text-muted mt-5">현재 활성 신고가 없습니다.</p>
        </div>
      </div>

      <div class="col-lg-8" v-if="selectedReport">
        <h4 class="mb-3 text-secondary">신고 상세</h4>
        <div class="row g-3">
          <div class="col-md-6">
            <div class="map-placeholder bg-light rounded shadow-sm d-flex align-items-center justify-content-center border" style="height: 400px;">
              <span class="text-muted">지도에 신고자 위치 (API 연동 필요)</span>
            </div>
          </div>

          <div class="col-md-6">
            <div class="detail-card card h-100 border-0 shadow-sm">
              <div class="card-header border-0 pb-0 bg-white">
                <h5 class="card-title" :class="getAlertColor(selectedReport.level)">{{ selectedReport.type }}</h5>
                <small class="text-muted">발생: {{ selectedReport.date }} {{ selectedReport.time }}</small>
              </div>
              <div class="card-body">
                <h6>생체 신호 및 위험도</h6>
                <p class="mb-2">
                  <strong class="me-2">심박수:</strong>
                  <span class="fw-bold" :class="{'text-danger': selectedReport.hr > 150}">{{ selectedReport.hr }} BPM</span>
                </p>
                <p class="mb-2">
                  <strong class="me-2">산소포화도:</strong>
                  <span class="fw-bold" :class="getOxygenClass(selectedReport.spo2)">{{ selectedReport.spo2 }}%</span>
                </p>
                <p class="mb-2">
                  <strong class="me-2">Z축 변화:</strong>
                  <span class="fw-bold" :class="{'text-emergency': selectedReport.zChange === '급격'}">{{ selectedReport.zChange }}</span>
                </p>
              </div>
              <div class="card-footer bg-light border-0 d-flex justify-content-end">
                <button class="btn btn-sm btn-outline-danger">출동 확인</button>
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
]);

const selectedReport = ref(activeReports.value[0]);

const selectReport = (report) => {
  selectedReport.value = report;
};

// 위험 단계에 따른 뱃지 색상 클래스 반환
const getBadgeClass = (level) => {
  switch (level) {
    case 'warning': return 'bg-warning text-dark';
    case 'danger': return 'bg-danger';
    case 'emergency': return 'bg-emergency'; // SCSS에 정의된 커스텀 클래스
    default: return 'bg-secondary';
  }
};

// 리스트 좌측 보더 색상 클래스 반환
const getListBorderClass = (level) => {
  switch (level) {
    case 'warning': return 'border-warning';
    case 'danger': return 'border-danger';
    case 'emergency': return 'border-emergency';
    default: return 'border-secondary';
  }
};

// 신고 제목 색상 클래스 반환
const getAlertColor = (level) => {
  switch (level) {
    case 'warning': return 'text-warning';
    case 'danger': return 'text-danger';
    case 'emergency': return 'text-emergency';
    default: return 'text-secondary';
  }
}

// 산소 포화도에 따른 텍스트 색상 클래스 반환
const getOxygenClass = (spo2) => {
  if (spo2 <= 92) return 'text-emergency'; // Deep Magenta
  if (spo2 <= 95) return 'text-danger';    // Coral Red
  return 'text-success';
};
</script>

<style scoped>
/* 커스텀 클래스 (SCSS 변수 기반) */
.text-emergency {
  color: #B93F67 !important;
}
.bg-emergency {
  background-color: #B93F67 !important;
}
.border-emergency {
  border-color: #B93F67 !important;
}

.map-placeholder {
  font-size: 0.9rem;
  color: #6c757d;
}

.list-group-item:hover {
  cursor: pointer;
}
</style>