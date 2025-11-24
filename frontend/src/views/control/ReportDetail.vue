<template>
  <div class="report-detail container-fluid p-3" style="background-color: #F8F9FA;">
    <div class="row">

      <div class="col-lg-4 mb-4 mb-lg-0">
        <h4 class="mb-3 text-secondary">신고 리스트</h4>
        <div class="list-group" style="height: 700px; overflow-y: auto;">
          <template v-if="!isLoading && !loadError">
            <template v-if="activeReports.length">
              <a
                href="#"
                v-for="report in activeReports"
                :key="report.id"
                class="list-group-item list-group-item-action py-3 border-start border-5 report-list-item"
                :class="[
                  selectedReport && selectedReport.id === report.id ? 'bg-primary-light-active border-primary-light' : 'bg-light-card border-light-card-border',
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
                  <strong>{{ report.ageLabel }} · {{ report.genderLabel }}</strong>
                  <small class="text-muted">{{ report.date }} {{ report.time }}</small>
                </div>
                <div class="report-type-badge">{{ report.type }}</div>
              </a>
            </template>
            <p v-else class="text-center text-muted mt-5">현재 활성 신고가 없습니다.</p>
          </template>
          <p v-else-if="isLoading" class="text-center text-muted mt-5">데이터를 불러오는 중입니다...</p>
          <p v-else class="text-center text-danger mt-5">관제 데이터를 가져오는 데 실패했습니다.</p>
        </div>
      </div>

      <div class="col-lg-8" v-if="selectedReport">
        <h4 class="mb-3 text-secondary">신고 상세정보</h4>
        <div class="row g-3">

          <div class="col-md-8 pe-3 map-col">
            <div class="mb-3 text-secondary ps-2">신고자 위치</div>
            <div class="map-placeholder bg-light rounded d-flex align-items-center justify-content-center border" style="height: 400px; border-color: #EAECEF !important;">
              <span class="text-muted">지도에 신고자 위치 (API 연동 필요)</span>
            </div>
          </div>

          <div class="col-md-4 d-flex justify-content-end align-items-start detail-col pt-1">
            <div class="detail-inner w-100">
              <div class="mb-3 text-secondary ps-2">신고자 상태정보</div>
              <div :class="['detail-card card h-100 border-0', { 'flash-highlight': highlight }]" style="background-color: #FFFFFF;">
                <div class="card-header border-0 pb-0" style="background-color: #FFFFFF; border-color: #EAECEF !important;">
                  <div class="d-flex w-100 justify-content-between align-items-start">
                    <div class="pt-5">
                      <h class="card-title fw-bold mb-1 fs-1" :class="getAlertColor(selectedReport.level)">
                        {{ mapReportType(selectedReport.type) }}
                      </h>
                      <div class="report-datetime-box mt-2">
                        <div class="d-flex align-items-center">
                          <i class="bi bi-clock-fill me-2 text-info-custom fs-5"></i>
                          <div class="fw-semibold mono-time">{{ selectedReport.time }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                
                <div class="card-body text-dark">
                  <div class="info-grid row gy-3">
                    <div class="col-12 d-flex align-items-center">
                    <i class="fs-1 bi bi-person-fill info-icon text-muted me-2" title="신고인"></i>
                    <div class="info-value fw-bold fs-2">{{ selectedReport.ageLabel }} · {{ selectedReport.genderLabel }}</div>
                    </div>

                    <div class="col-12 d-flex align-items-center">
                      <i class="fs-1 bi bi-geo-alt info-icon text-muted me-2" title="위치"></i>
                      <div class="fs-2 info-value fw-bold text-truncate">
                        <template v-if="selectedReport.mapLat">
                             <div class="info-value fw-bold fs-2" style="line-height: 1.3; font-size: 1.2rem !important;"> 
                                <div class="text-truncate">
                                    위도: {{ selectedReport.mapLat.toFixed(4) }}
                                </div>
                                <div class="text-truncate" style="margin-top: 4px;"> 
                                    경도: {{ selectedReport.mapLon.toFixed(4) }}
                                </div>
                            </div>
                        </template>
                        <template v-else>
                            {{ selectedReport.location }}
                        </template>
                      </div>
                    </div>

                    <div class="col-12 d-flex align-items-center">
                      <i class="fs-1 bi bi-heart-pulse info-icon text-muted me-2" title="심박수"></i>
                      <div class="fs-2 info-value fw-bold d-flex align-items-center">
                        <span :class="{'text-danger': selectedReport.hr !== null && selectedReport.hr > 150, 'me-2': true}">{{ prettyHr(selectedReport.hr) }}</span>
                        <small class="fs-3 ms-2 badge bg-light border text-muted">실시간</small>
                      </div>
                    </div>
                  </div>
                </div>

                <button
                  class="btn btn-sm"
                  :class="selectedReport.processed === 1 ? 'btn-processed-disabled' : 'btn-processed'"
                  :disabled="selectedReport.processed === 1"
                  @click="markProcessed(selectedReport)"
                >
                  {{ selectedReport.processed === 1 ? '구조 요청 처리됨' : '구조 요청' }}
                </button>

              </div>
            </div>
          </div>

          <div class="col-12 mt-4">
            <div class="card p-3 border-0" style="background-color: #FFFFFF;">
              <h6 class="fw-bold text-secondary-default mb-3"><i class="bi bi-journal-text me-2"></i>상황 기록 로그</h6>
              <div class="log-area small" style="height: 150px; overflow-y: auto; background-color: #F8F9FA; padding: 10px; border-radius: 6px; border: 1px solid #EAECEF;">
                <div class="log-item mb-1"><span class="log-time text-muted">[{{ selectedReport.time }}]</span> <span class="fw-semibold text-dark">신고 접수 완료 (레벨: {{ selectedReport.level.toUpperCase() }})</span></div>
                <div class="log-item mb-1"><span class="log-time text-muted">[{{ selectedReport.time }}]</span> <span class="text-muted">심박수 {{ prettyHr(selectedReport.hr) }}, 산소포화도 {{ prettySpo2(selectedReport.spo2) }} 기록</span></div>
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
import { ref, computed, onMounted, watch, onBeforeUnmount } from 'vue';
import { useRoute } from 'vue-router';
import { useApi } from '@/utils/useApi.js';

const activeReports = ref([]);
const selectedReport = ref(null);
const highlight = ref(false);
const isLoading = ref(false);
const loadError = ref(null);

const route = useRoute();
const DEFAULT_CONTROL_TOWER_NUMBER = 1;

const controlTowerNumber = computed(() => {
  const raw = Number(route.query.controlTowerNumber);
  return Number.isFinite(raw) && raw > 0 ? raw : DEFAULT_CONTROL_TOWER_NUMBER;
});

const { execute: fetchTaskList } = useApi('get', '/api/controltower/task/list/controltower');

let highlightTimer = null;

const clearHighlightTimer = () => {
  if (!highlightTimer) return;
  clearTimeout(highlightTimer);
  highlightTimer = null;
};

const triggerHighlight = () => {
  highlight.value = false;
  requestAnimationFrame(() => {
    highlight.value = true;
    clearHighlightTimer();
    highlightTimer = setTimeout(() => {
      highlight.value = false;
      highlightTimer = null;
    }, 700);
  });
};

const setSelectedReport = (report, shouldHighlight = false) => {
  selectedReport.value = report ?? null;
  if (report && shouldHighlight) {
    triggerHighlight();
  }
  if (!report) {
    highlight.value = false;
    clearHighlightTimer();
  }
};

const selectReport = (report) => {
  if (!report) return;
  setSelectedReport(report, true);
};

const toFiniteNumber = (value) => {
  if (value === null || value === undefined) return null;
  const num = Number(value);
  return Number.isFinite(num) ? num : null;
};

const isValidCoordinatePair = (lat, lon) => {
  if (lat === null || lon === null) return false;
  return !(lat === 0 && lon === 0);
};

const parseDateTime = (value) => {
  if (!value) return { date: '-', time: '--:--:--' };
  const normalized = String(value).includes('T') ? String(value) : String(value).replace(' ', 'T');
  const parsed = new Date(normalized);
  if (!Number.isNaN(parsed.getTime())) {
    return {
      date: parsed.toISOString().slice(0, 10),
      time: parsed.toLocaleTimeString('ko-KR', { hour12: false })
    };
  }
  const [datePart, timePart] = String(value).split(' ');
  return { date: datePart ?? '-', time: timePart ?? '--:--:--' };
};

const computeInternationalAge = (birthDate) => {
  if (!birthDate) return null;
  const normalized = String(birthDate).split(' ')[0].replace(/\./g, '-');
  const date = new Date(normalized);
  if (Number.isNaN(date.getTime())) return null;
  const today = new Date();
  let age = today.getFullYear() - date.getFullYear();
  const monthDiff = today.getMonth() - date.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < date.getDate())) {
    age -= 1;
  }
  return age;
};

const mapGender = (gender) => {
  if (!gender) return '성별 정보 없음';
  const normalized = String(gender).trim().toLowerCase();
  if (['f', 'female', '여', '여자', '여성'].includes(normalized)) return '여성';
  if (['m', 'male', '남', '남자', '남성'].includes(normalized)) return '남성';
  return String(gender);
};

const determineLevel = (count) => {
  const numeric = Number(count);
  if (!Number.isFinite(numeric)) return 'warning';
  if (numeric >= 10) return 'emergency';
  if (numeric >= 5) return 'danger';
  return 'warning';
};

const mapReportType = (typeCode) => {
    // 🚨 수동 신고 Type Code를 한글 이름으로 매핑
    const codeMap = {
        'DROWNING': '물에 빠짐',
        'INJURY': '부상',
        'COLLAPSE': '쓰러짐',
        'MISSING': '일행 이탈/실종',
        'OTHERS': '수동 호출 (기타)',
        'WATCH': '심박수 이상',
        '라이프가드 호출': '라이프가드 호출', // 기존 기본값
    };
    // DTO에서 받은 typeCode가 map에 있으면 반환, 없으면 기본값
    return codeMap[String(typeCode).toUpperCase()] || String(typeCode) || '라이프가드 호출';
}


const determineTypeAndLocation = (task) => {
  // 🚨 [필드 확인] Task DTO에 taskLat/taskLon이 추가되었다고 가정하고 가져옴
  const taskLat = toFiniteNumber(task?.taskLat); 
  const taskLon = toFiniteNumber(task?.taskLon);
  
  const watchLat = toFiniteNumber(task?.watchLat);
  const watchLon = toFiniteNumber(task?.watchLon);
  const userLat = toFiniteNumber(task?.userLat); // User의 기본 위치
  const userLon = toFiniteNumber(task?.userLon);
  
  let type = task?.type ?? '라이프가드 호출'; // DTO의 type을 우선 사용

  // 1. Task Location (수동 신고 위치)이 있는지 확인 (최우선)
  if (isValidCoordinatePair(taskLat, taskLon)) {
      return { type, mapLat: taskLat, mapLon: taskLon };
  }
  
  // 2. Watch Location (자동 신고 위치)이 있는지 확인
  if (isValidCoordinatePair(watchLat, watchLon)) {
    // 자동 신고일 경우 type 조정 (task.type이 WATCH일 수 있음)
    return { type: type === '라이프가드 호출' ? '심박수 이상' : type, mapLat: watchLat, mapLon: watchLon };
  }

  // 3. User Location (기본 위치)이 있는지 확인
  if (isValidCoordinatePair(userLat, userLon)) {
    return { type, mapLat: userLat, mapLon: userLon };
  }

  return { type, mapLat: null, mapLon: null };
};

const toReportViewModel = (task) => {
  const { date, time } = parseDateTime(task?.dateAndTime);
  const age = computeInternationalAge(task?.birthDateForAge);
  const genderLabel = mapGender(task?.gender);
  const hr = toFiniteNumber(task?.hr);
  const count = toFiniteNumber(task?.count);
  const { type, mapLat, mapLon } = determineTypeAndLocation(task);

  // 🚨 [수정 1] Type Code를 한글 Label로 변환
  const typeLabel = mapReportType(type);

  return {
    id: task?.id ?? task?.taskNumber ?? null,
    type: typeLabel, // 🚨 Type Code 대신 한글 Label 사용
    level: determineLevel(count),
    date,
    time,
    age,
    ageLabel: age !== null ? `(만)${age}세` : '연령 정보 없음',
    genderLabel,
    hr,
    spo2: toFiniteNumber(task?.spo2),
    // 🚨 [수정 2] location 필드 재정의: GPS 좌표가 있을 때만 좌표 문자열 표시
    location: task?.beachName ?? (mapLat ? `위치 (${mapLat.toFixed(4)}, ${mapLon.toFixed(4)})` : '위치 정보 없음'),
    mapLat,
    mapLon,
    processed: task?.taskProcessed === 1 ? 1 : 0,
    count,
    raw: task
  };
};

const fetchReports = async () => {
  isLoading.value = true;
  loadError.value = null;
  try {
    const response = await fetchTaskList({ controlTowerNumber: controlTowerNumber.value });
    const list = Array.isArray(response?.result) ? response.result : [];
    
    // 🚨 [디버깅] 서버에서 받아온 Raw 데이터를 확인합니다.
    console.log('Server Reports (Raw):', list);

    const mapped = list.map(toReportViewModel);

    activeReports.value = mapped;

    if (!mapped.length) {
      setSelectedReport(null);
      return;
    }

    const previousId = selectedReport.value?.id;
    const nextSelected = mapped.find((report) => report.id === previousId) ?? mapped[0];
    const shouldFlash = previousId !== nextSelected?.id;
    setSelectedReport(nextSelected, shouldFlash);
  } catch (error) {
    console.error('관제 신고 목록 조회 실패:', error);
    loadError.value = error;
    activeReports.value = [];
    setSelectedReport(null);
  } finally {
    isLoading.value = false;
  }
};

watch(controlTowerNumber, fetchReports);
onMounted(fetchReports);
onBeforeUnmount(clearHighlightTimer);

const prettyHr = (hr) => {
  const numeric = toFiniteNumber(hr);
  if (numeric === null) return '정보 없음';
  return `${numeric} BPM`;
};

const prettySpo2 = (spo2) => {
  const numeric = toFiniteNumber(spo2);
  if (numeric === null) return '정보 없음';
  return `${numeric}%`;
};

const markProcessed = (report) => {
  if (!report || report.processed === 1) return;
  report.processed = 1;
  if (selectedReport.value?.id === report.id) {
    selectedReport.value.processed = 1;
  }
  // TODO: 필요 시 백엔드 API 호출로 persisted 처리
};

const getBadgeClass = (level) => {
  switch (level) {
    case 'warning': return 'bg-warning-custom text-dark';
    case 'danger': return 'bg-danger-custom';
    case 'emergency': return 'bg-emergency-custom';
    default: return 'bg-safety-custom';
  }
};

const getListBorderClass = (level) => {
  switch (level) {
    case 'warning': return 'border-warning-custom';
    case 'danger': return 'border-danger-custom';
    case 'emergency': return 'border-emergency-custom';
    default: return 'border-safety-custom';
  }
};

const getAlertColor = (level) => {
  switch (level) {
    case 'warning': return 'text-warning-custom';
    case 'danger': return 'text-danger-custom';
    case 'emergency': return 'text-emergency-custom';
    default: return 'text-safety-custom';
  }
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

/* Secondary Blue Accent (모두 검은색으로 통일) */
.text-info-custom { color: #212529 !important; }
.text-secondary-default { color: #212529 !important; }

/* Primary Accent */
.bg-primary-light-active {
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

/* 리스트 항목 내부 */
.report-list-item { position: relative; }
.report-type-badge {
  position: absolute;
  right: 12px;
  bottom: 10px;
  background: #F1F3F5;
  color: #212529;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 600;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
  border: 1px solid #E6E9EE;
}
.report-type-badge:hover {
  background: #E9ECEF;
  transform: translateY(-1px);
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

.detail-card .small.text-muted {
  color: #6c757d !important;
}
.detail-card .fw-bold {
  font-size: 0.98rem;
}

/* Flash highlight animation */
.flash-highlight {
  animation: flash-highlight 0.7s ease-in-out;
}
@keyframes flash-highlight {
  0% { box-shadow: 0 0 0 rgba(0,0,0,0); background-color: #ffffff; }
  10% { background-color: rgba(0,146,186,0.12); }
  40% { background-color: rgba(0,146,186,0.06); }
  100% { background-color: #ffffff; }
}

/* Workflow Buttons */
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
.btn-dispatch-disabled:hover {
  transform: none;
}

.btn-disabled {
  opacity: 0.65;
  pointer-events: none;
}

.btn-processed {
  background-color: #0092BA;
  color: #fff;
  border-color: #0092BA;
}
.btn-processed-disabled {
  background-color: #F1F3F5;
  color: #6c757d;
  border-color: #E6E9EE;
  opacity: 0.85;
  pointer-events: none;
}

/* Info grid */
.info-grid .info-label { color: #6c757d; }
.info-grid .info-value { color: #212529; }
.log-item { font-size: 0.9rem; }
.log-time {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, 'Roboto Mono', 'Noto Sans Mono', monospace;
  margin-right: 6px;
}

/* Icon labels */
.info-icon {
  font-size: 1.4rem;
  width: 40px;
  text-align: center;
  color: #6c757d;
}
.info-grid .d-flex > .info-value { min-width: 0; }
.info-grid .info-value.text-truncate {
  max-width: calc(100% - 44px);
}

/* Responsive */
@media (max-width: 768px) {
  .info-icon {
    font-size: 1.2rem;
    width: 34px;
  }
  .info-grid .info-value.text-truncate {
    max-width: calc(100% - 38px);
  }
}

.card-header { position: relative; }

/* Datetime Box - 공백 완전 정리본 */
.report-datetime-box {
  position: absolute;
  right: 16px;
  top: 16px;
  text-align: right;
  display: inline-flex;
  align-items: center;
  background-color: #F8FAFC;
  border: 1px solid #E6EEF5;
  padding: 6px 10px;
  border-radius: 8px;
  width: auto;
  min-width: 0;
}
.mono-time { white-space: nowrap; }

/* Detail Width */
.detail-inner {
  max-width: 380px;
  padding-left: 8px;
  display: block;
}
.detail-inner .detail-card {
  width: 100%;
}

@media (max-width: 992px) {
  .detail-inner {
    max-width: 100%;
    padding-left: 0;
  }
}
</style>
