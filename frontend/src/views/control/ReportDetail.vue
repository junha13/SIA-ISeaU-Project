<template>
  <div class="report-detail container-fluid p-3" style="background-color: #F8F9FA;">
    <div class="row">

      <!-- Left: Active reports list -->
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

      <!-- Right: Detail panel (narrower for better balance) -->
      <div class="col-lg-8" v-if="selectedReport">
        <h4 class="mb-3 text-secondary">신고 상세정보</h4>
        <div class="row g-3">

          <div class="col-md-8 pe-3 map-col">
            <div class="mb-3 text-secondary ps-2">신고자 위치</div>
              <div class="map-placeholder bg-light rounded d-flex align-items-center justify-content-center border" style="height: 400px; border-color: #EAECEF !important;">
              <div
                v-if="!hasValidMapPosition"
                class="text-muted small text-center px-3"
              >
                신고자의 위치 정보가 없거나<br />
                지도 API 준비 중입니다.
              </div>

              <!-- 좌표 있으면 지도 렌더링 -->
              <div
                v-else
                ref="mapEl"
                style="width: 100%; height: 100%;"
              ></div>
            </div>
          </div>

          <div class="col-md-4 d-flex justify-content-end align-items-start detail-col pt-1">
            <div class="detail-inner w-100">
              <div class="mb-3 text-secondary ps-2">신고자 상태정보</div>
              <div :class="['detail-card card h-100 border-0', { 'flash-highlight': highlight }]" style="background-color: #FFFFFF;">
                <div class="card-header border-0 pb-0" style="background-color: #FFFFFF; border-color: #EAECEF !important;">
                  <div class="d-flex w-100 justify-content-between align-items-start">
                    <div class="pt-5">
                      <h class="card-title fw-bold mb-1 fs-1" :class="getAlertColor(selectedReport.level)">{{ selectedReport.type }}</h>
                      <div class="report-datetime-box mt-2">
                        <div class="d-flex align-items-center">
                          <i class="bi bi-clock-fill me-2 text-info-custom fs-5"></i>
                          <div class="fw-semibold mono-time">{{ selectedReport.time }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                    
                  </div>
                  
                  <!-- Updated 신고 정보 영역 -->
                  <div class="card-body text-dark">
                    <div class="info-grid row gy-3">
                      <div class="col-12 d-flex align-items-center">
                      <i class="fs-1 bi bi-person-fill info-icon text-muted me-2" title="신고인"></i>
                      <div class="info-value fw-bold fs-2">{{ selectedReport.ageLabel }} · {{ selectedReport.genderLabel }}</div>
                    </div>

                    <div class="col-12 d-flex align-items-center">
                      <i class="fs-1 bi bi-geo-alt info-icon text-muted me-2" title="위치"></i>
                      <div class="fs-2 info-value fw-bold text-truncate">
                        {{ selectedReport.location }}
                        <small
                          v-if="selectedReport.coordinateLabel"
                          class="text-muted ms-2 fs-6 coordinate-tag"
                        >
                          {{ selectedReport.coordinateLabel }}
                        </small>
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
                    :class="isReportProcessed(selectedReport) ? 'btn-processed-disabled' : 'btn-processed'"
                    :disabled="isReportProcessed(selectedReport)"
                    @click="handleRescueRequest(selectedReport)"
                  >
                    {{ isReportProcessed(selectedReport) ? '구조 요청 처리됨' : '구조 요청' }}
                  </button>

              </div>
            </div>
          </div>

          <div class="col-12 mt-4">
            <div class="card p-3 border-0" style="background-color: #FFFFFF;">
              <h6 class="fw-bold text-secondary-default mb-3"><i class="bi bi-journal-text me-2"></i>상황 기록 로그</h6>
              <div class="log-area small" style="height: 150px; overflow-y: auto; background-color: #F8F9FA; padding: 10px; border-radius: 6px; border: 1px solid #EAECEF;">
                <div v-if="logsLoading" class="text-muted text-center py-2">상황 기록을 불러오는 중입니다...</div>
                <div v-else-if="logsError" class="text-danger text-center py-2">상황 기록을 가져오지 못했습니다.</div>
                <div v-else-if="!activityLogs.length" class="text-muted text-center py-2">기록이 없습니다.</div>
                <div v-else>
                  <div
                    class="log-item mb-1"
                    v-for="log in activityLogs"
                    :key="`${log.forTime ?? ''}-${log.hr ?? ''}`"
                  >
                    <span class="log-time text-muted">[{{ formatLogTime(log.forTime) }}]</span>
                    <span class="text-dark">심박수 : {{ formatLogHr(log.hr) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

        </div>
      </div>

    </div>
  </div>

  <div
    v-if="showRescueModal && selectedReport"
    class="rescue-modal-backdrop"
    @click.self="closeRescueModal"
  >
    <div class="rescue-modal-card">
      <div class="rescue-modal-header d-flex justify-content-between align-items-start">
        <div>
          <h5 class="mb-1">구조 요청 전송</h5>
          <p class="text-muted small mb-0">다음과 같은 내용을 구조요원에게 전달합니다</p>
        </div>
        <button
          type="button"
          class="btn btn-sm btn-link text-muted p-0"
          @click="closeRescueModal"
        >
          <i class="bi bi-x-lg fs-5"></i>
        </button>
      </div>

      <div class="rescue-modal-body">
        <div class="modal-map-wrapper mb-3">
          <div
            v-if="hasValidMapPosition"
            ref="modalMapEl"
            class="modal-map"
          ></div>
          <div v-else class="modal-map-placeholder text-muted small">
            위치 정보가 없어 지도를 표시할 수 없습니다.
          </div>
        </div>

        <div class="modal-info-grid">
          <div class="modal-info-row">
            <span class="label">신고 유형</span>
            <span class="value">{{ selectedReport.type }}</span>
          </div>
          <div class="modal-info-row">
            <span class="label">만나이</span>
            <span class="value">{{ selectedReport.ageLabel }}</span>
          </div>
          <div class="modal-info-row">
            <span class="label">성별</span>
            <span class="value">{{ selectedReport.genderLabel }}</span>
          </div>
          <div class="modal-info-row">
            <span class="label">심박수</span>
            <span class="value">{{ prettyHr(selectedReport.hr) }}</span>
          </div>
          <div class="modal-info-row">
            <span class="label">해수욕장</span>
            <span class="value">{{ selectedReport.location }}</span>
          </div>
        </div>
      </div>

      <div class="rescue-modal-footer d-flex justify-content-end">
        <button class="btn btn-secondary btn-sm" @click="closeRescueModal">닫기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onBeforeUnmount, watchEffect, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import { useApi } from '@/utils/useApi.js';

const activeReports = ref([]);
const selectedReport = ref(null);
const highlight = ref(false);
const isLoading = ref(false);
const loadError = ref(null);
const activityLogs = ref([]);
const logsLoading = ref(false);
const logsError = ref(null);
const mapEl = ref(null);
const processedReportIds = ref(new Set());
const showRescueModal = ref(false);
const modalMapEl = ref(null);

let map = null;
let watchMarker = null;
let watchCircle = null;
let modalMap = null;
let modalWatchMarker = null;
let modalWatchCircle = null;

const route = useRoute();
const DEFAULT_CONTROL_TOWER_NUMBER = 1;

const controlTowerNumber = computed(() => {
  const raw = Number(route.query.controlTowerNumber);
  return Number.isFinite(raw) && raw > 0 ? raw : DEFAULT_CONTROL_TOWER_NUMBER;
});

const { execute: fetchTaskList } = useApi('get', '/api/controltower/task/list/controltower');
const { execute: fetchTaskLog } = useApi('get', '/api/controltower/task/log');
const POLL_INTERVAL_MS = 1000;

let highlightTimer = null;
let pollTimer = null;
let isFetching = false;

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
    activityLogs.value = [];
    logsLoading.value = false;
    logsError.value = null;
  }
};

const selectReport = (report) => {
  if (!report) return;
  setSelectedReport(report, true);
  loadReportLogs(report).catch(() => {});
};

const loadReportLogs = async (report, { silent = false } = {}) => {
  const userNumber = report?.userNumber
    ?? report?.raw?.userNumber
    ?? report?.raw?.user_number
    ?? null;
  if (!userNumber) {
    if (!silent) {
      activityLogs.value = [];
      logsError.value = null;
      logsLoading.value = false;
    }
    return;
  }

  if (!silent) {
    logsLoading.value = true;
    logsError.value = null;
  }

  try {
    const response = await fetchTaskLog({ userNumber });
    const list = Array.isArray(response?.result) ? response.result : [];
    activityLogs.value = list;
    if (!silent) {
      logsError.value = null;
    }
  } catch (error) {
    if (!silent) {
      logsError.value = error;
      activityLogs.value = [];
    }
  } finally {
    if (!silent) {
      logsLoading.value = false;
    }
  }
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

const padTwoDigits = (value) => String(value).padStart(2, '0');

const formatKoreanTimeParts = (hour, minute, second) => `${hour}시${minute}분${second}초`;

const formatKoreanTimeDisplay = (value) => {
  if (value instanceof Date && !Number.isNaN(value.getTime())) {
    return formatKoreanTimeParts(
      padTwoDigits(value.getHours()),
      padTwoDigits(value.getMinutes()),
      padTwoDigits(value.getSeconds())
    );
  }

  const str = String(value ?? '').trim();
  if (!str) return '--시--분--초';

  const timeMatch = str.match(/(\d{1,2}):(\d{2}):(\d{2})/);
  if (timeMatch) {
    const [, hour, minute, second] = timeMatch;
    return formatKoreanTimeParts(
      padTwoDigits(hour),
      padTwoDigits(minute),
      padTwoDigits(second)
    );
  }

  return '--시--분--초';
};

const normalizeDateTimeString = (value) => {
  if (!value) return '';
  let normalized = String(value).trim();
  if (!normalized.includes('T') && normalized.includes(' ')) {
    normalized = normalized.replace(' ', 'T');
  }
  normalized = normalized.replace(/([+-]\d{2})(?!:)/, '$1:00');
  return normalized;
};

const parseDateTime = (value) => {
  if (!value) return { date: '-', time: '--시--분--초' };
  const str = String(value).trim();
  const normalized = normalizeDateTimeString(str);
  const parsed = new Date(normalized);
  const hasValidDate = !Number.isNaN(parsed.getTime());
  const datePart = hasValidDate
    ? parsed.toISOString().slice(0, 10)
    : (str.split(' ')[0] ?? '-');

  const timePart = hasValidDate
    ? formatKoreanTimeDisplay(parsed)
    : formatKoreanTimeDisplay(str);

  return { date: datePart, time: timePart };
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
  if (numeric >= 20) return 'emergency';
  if (numeric >= 10) return 'danger';
  return 'warning';
};

const determineTypeAndLocation = (task) => {
  const backendType = typeof task?.type === 'string' ? task.type.trim() : null;
  const resolvedType = backendType && backendType.length ? backendType : '심박수 이상';
  const watchLat = toFiniteNumber(task?.watchLat);
  const watchLon = toFiniteNumber(task?.watchLon);
  const userLat = toFiniteNumber(task?.userLat);
  const userLon = toFiniteNumber(task?.userLon);

  if (isValidCoordinatePair(watchLat, watchLon)) {
    return { type: resolvedType, mapLat: watchLat, mapLon: watchLon };
  }

  if (isValidCoordinatePair(userLat, userLon)) {
    return { type: resolvedType, mapLat: userLat, mapLon: userLon };
  }

  return { type: resolvedType, mapLat: null, mapLon: null };
};

const formatCoordinateLabel = (lat, lon) => {
  const latNum = toFiniteNumber(lat);
  const lonNum = toFiniteNumber(lon);
  if (latNum === null || lonNum === null) return null;
  return `${latNum.toFixed(5)}, ${lonNum.toFixed(5)}`;
};

const toReportViewModel = (task) => {
  const id = task?.id ?? task?.taskNumber ?? task?.task_number ?? null;
  const { date, time } = parseDateTime(task?.dateAndTime);
  const age = computeInternationalAge(task?.birthDateForAge);
  const genderLabel = mapGender(task?.gender);
  const hr = toFiniteNumber(task?.hr);
  const count = toFiniteNumber(task?.count);
  const { type, mapLat, mapLon } = determineTypeAndLocation(task);
  const backendProcessed = task?.taskProcessed === 1;
  const locallyProcessed = id !== null && processedReportIds.value.has(id);

  return {
    id,
    type,
    level: determineLevel(count),
    date,
    time,
    age,
    ageLabel: age !== null ? `(만)${age}세` : '연령 정보 없음',
    genderLabel,
    hr,
    spo2: toFiniteNumber(task?.spo2),
    location: task?.beachName ?? '위치 정보 없음',
    mapLat,
    mapLon,
    coordinateLabel: formatCoordinateLabel(mapLat, mapLon),
    processed: backendProcessed || locallyProcessed ? 1 : 0,
    count,
    userNumber: task?.userNumber ?? task?.user_number ?? null,
    raw: task
  };
};

const fetchReports = async ({ silent = false } = {}) => {
  if (isFetching) {
    return;
  }
  isFetching = true;
  if (!silent) {
    isLoading.value = true;
    loadError.value = null;
  }
  try {
    const response = await fetchTaskList({ controlTowerNumber: controlTowerNumber.value });
    loadError.value = null;
    const list = Array.isArray(response?.result) ? response.result : [];
    const nextProcessed = new Set(processedReportIds.value);
    list.forEach((task) => {
      const id = task?.id ?? task?.taskNumber ?? task?.task_number ?? null;
      if (task?.taskProcessed === 1 && id !== null) {
        nextProcessed.add(id);
      }
    });
    processedReportIds.value = nextProcessed;

    const mapped = list.map(toReportViewModel);

    activeReports.value = mapped;

    if (!mapped.length) {
      setSelectedReport(null);
      activityLogs.value = [];
      if (!silent) {
        logsError.value = null;
        logsLoading.value = false;
      }
      return;
    }

    const previousId = selectedReport.value?.id;
    const nextSelected = mapped.find((report) => report.id === previousId) ?? mapped[0];
    const shouldFlash = previousId !== nextSelected?.id;
    setSelectedReport(nextSelected, shouldFlash);
    loadReportLogs(nextSelected, { silent }).catch(() => {});
  } catch (error) {
    console.error('관제 신고 목록 조회 실패:', error);
    if (!silent) {
      loadError.value = error;
      activeReports.value = [];
      setSelectedReport(null);
      activityLogs.value = [];
    }
  } finally {
    if (!silent) {
      isLoading.value = false;
    }
    isFetching = false;
  }
};

const startPolling = () => {
  stopPolling();
  pollTimer = setInterval(() => {
    fetchReports({ silent: true }).catch(() => {});
  }, POLL_INTERVAL_MS);
};

const stopPolling = () => {
  if (!pollTimer) return;
  clearInterval(pollTimer);
  pollTimer = null;
};

watch(controlTowerNumber, () => {
  fetchReports().catch(() => {});
  startPolling();
});

onMounted(() => {
  fetchReports().catch(() => {});
  startPolling();
});

onBeforeUnmount(() => {
  clearHighlightTimer();
  stopPolling();
});

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

const formatLogTime = (value) => {
  return formatKoreanTimeDisplay(value);
};

const formatLogHr = (hr) => {
  const numeric = toFiniteNumber(hr);
  if (numeric === null) return '정보 없음';
  return `${numeric}bpm`;
};

const isReportProcessed = (report) => {
  if (!report) return false;
  if (report.processed === 1) return true;
  const id = report?.id ?? null;
  if (id === null) return false;
  return processedReportIds.value.has(id);
};

const markProcessed = (report) => {
  const id = report?.id ?? null;
  if (id === null) return;
  if (processedReportIds.value.has(id)) return;

  const next = new Set(processedReportIds.value);
  next.add(id);
  processedReportIds.value = next;

  if (selectedReport.value?.id === id) {
    selectedReport.value = { ...selectedReport.value, processed: 1 };
  }

  activeReports.value = activeReports.value.map((item) =>
    item.id === id ? { ...item, processed: 1 } : item
  );

  // TODO: 필요 시 백엔드 API 호출로 persisted 처리
};

const handleRescueRequest = async (report) => {
  if (!report) return;
  if (!isReportProcessed(report)) {
    markProcessed(report);
  }
  showRescueModal.value = true;
  await nextTick();
  if (modalMap && hasNaverMaps()) {
    window.naver.maps.Event.trigger(modalMap, 'resize');
  }
};

const closeRescueModal = () => {
  showRescueModal.value = false;
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

const getLevelBorderColor = (level) => {
  switch (level) {
    case 'warning':
      return '#FFB354';
    case 'danger':
      return '#EB725B';
    case 'emergency':
      return '#B93F67';
    default:
      return '#7EEC85';
  }
};

const buildMarkerHtml = (borderColor) => `
  <div style="
    width: 22px;
    height: 22px;
    border-radius: 50%;
    border: 3px solid ${borderColor};
    background: rgba(0,146,186,0.20);
    box-shadow: 0 0 0 4px rgba(0,146,186,0.15);
    box-sizing: border-box;
  "></div>
`;

const hasNaverMaps = () => typeof window !== 'undefined' && window.naver && window.naver.maps;

/** 현재 선택된 신고에 지도에 찍을 수 있는 좌표가 있는지 여부 */
const hasValidMapPosition = computed(() => {
  const r = selectedReport.value;
  if (!r) return false;
  return isValidCoordinatePair(r.mapLat, r.mapLon);
});

/**
 * 선택된 신고가 바뀌거나 좌표가 바뀔 때마다
 * 네이버 지도 초기화 또는 위치 업데이트
 */
watchEffect(() => {
  // 좌표 없으면 지도 안 띄움
  if (!hasValidMapPosition.value) return;
  // DOM 아직 안 잡혔으면 리턴
  if (!mapEl.value) return;
  // 네이버 지도 스크립트 안 올라와 있으면 리턴
  if (!hasNaverMaps()) return;

  const { mapLat, mapLon, level } = selectedReport.value;
  const pos = new window.naver.maps.LatLng(mapLat, mapLon);

  const borderColor = getLevelBorderColor(level);

  // 1) 지도 최초 생성
  if (!map) {
    map = new window.naver.maps.Map(mapEl.value, {
      center: pos,
      zoom: 17
    });

    // DOM에 처음 그려질 때 사이즈 재계산
    window.naver.maps.Event.trigger(map, 'resize');
  } else {
    // 2) 선택된 신고가 바뀌면 중심만 이동
    map.setCenter(pos);
  }

  // 3) 워치 위치 마커 생성 또는 위치 업데이트
  const markerHtml = buildMarkerHtml(borderColor);

  if (!watchMarker) {
    watchMarker = new window.naver.maps.Marker({
      position: pos,
      map,
      icon: {
        content: markerHtml,
        anchor: new window.naver.maps.Point(11, 11) // 동그라미 중심 기준
      }
    });
  } else {
    watchMarker.setPosition(pos);
    // 레벨이 바뀔 수도 있으니 아이콘도 같이 업데이트
    watchMarker.setIcon({
      content: markerHtml,
      anchor: new window.naver.maps.Point(11, 11)
    });
  }
  // 워치 위치 기준 20m 오차 원(반경 표시)
  if (!watchCircle) {
    watchCircle = new window.naver.maps.Circle({
      map,
      center: pos,
      radius: POSITION_ERROR_RADIUS_M, // 20m
      strokeColor: borderColor,
      strokeOpacity: 0.9,
      strokeWeight: 2,
      fillColor: borderColor,
      fillOpacity: 0.15
    });
  } else {
    watchCircle.setCenter(pos);
    watchCircle.setRadius(POSITION_ERROR_RADIUS_M);
    watchCircle.setOptions({
      strokeColor: borderColor,
      fillColor: borderColor
    });
  }
});

watch(showRescueModal, (visible) => {
  if (!visible) {
    if (modalMap && typeof modalMap.destroy === 'function') {
      modalMap.destroy();
    }
    modalMap = null;
    modalWatchMarker = null;
    // 반경 원도 제거
    if (modalWatchCircle) {
      modalWatchCircle.setMap(null);
      modalWatchCircle = null;
    }

    modalMapEl.value = null;
  }
});

watchEffect(() => {
  if (!showRescueModal.value) return;
  if (!hasValidMapPosition.value) return;
  if (!modalMapEl.value) return;
  if (!hasNaverMaps()) return;
  const report = selectedReport.value;
  if (!report) return;

  const { mapLat, mapLon, level } = report;
  const pos = new window.naver.maps.LatLng(mapLat, mapLon);
  const borderColor = getLevelBorderColor(level);
  const markerHtml = buildMarkerHtml(borderColor);

  if (!modalMap) {
    modalMap = new window.naver.maps.Map(modalMapEl.value, {
      center: pos,
      zoom: 17
    });
    window.naver.maps.Event.trigger(modalMap, 'resize');
  } else {
    modalMap.setCenter(pos);
    window.naver.maps.Event.trigger(modalMap, 'resize');
  }

  if (!modalWatchMarker) {
    modalWatchMarker = new window.naver.maps.Marker({
      position: pos,
      map: modalMap,
      icon: {
        content: markerHtml,
        anchor: new window.naver.maps.Point(11, 11)
      }
    });
  } else {
    modalWatchMarker.setPosition(pos);
    modalWatchMarker.setIcon({
      content: markerHtml,
      anchor: new window.naver.maps.Point(11, 11)
    });
  }
  // 모달 지도에서도 동일한 20m 오차 반경 표시
  if (!modalWatchCircle) {
    modalWatchCircle = new window.naver.maps.Circle({
      map: modalMap,
      center: pos,
      radius: POSITION_ERROR_RADIUS_M,
      strokeColor: borderColor,
      strokeOpacity: 0.9,
      strokeWeight: 2,
      fillColor: borderColor,
      fillOpacity: 0.15
    });
  } else {
    modalWatchCircle.setCenter(pos);
    modalWatchCircle.setRadius(POSITION_ERROR_RADIUS_M);
    modalWatchCircle.setOptions({
      strokeColor: borderColor,
      fillColor: borderColor
    });
  }

});
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

/* Processed button styles */
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

/* Info grid and status badge */
.info-grid .info-label { color: #6c757d; }
.info-grid .info-value { color: #212529; }

.coordinate-tag {
  font-weight: 500;
}

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
  align-items: center;
  background-color: #F8FAFC;
  border: 1px solid #E6EEF5;
  padding: 6px 10px;
  border-radius: 8px;
  width: auto; /* size to content */
  min-width: 0;
}
.mono-time { white-space: nowrap; }

/* Narrow wrapper for the detail section to improve readability */
.detail-inner { max-width: 380px; padding-left: 8px; }
.detail-inner .detail-card { width: 100%; }
.detail-inner { display: block; }

@media (max-width: 992px) {
  /* tablet and below: let detail full width under map */
  .detail-inner { max-width: 100%; padding-left: 0; }
}

.rescue-modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  z-index: 1050;
}

.rescue-modal-card {
  width: min(520px, 90vw);
  max-height: 90vh;
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.rescue-modal-header {
  padding: 20px 24px 12px;
  border-bottom: 1px solid #EAECEF;
}

.rescue-modal-header h5 {
  font-weight: 700;
}

.rescue-modal-body {
  padding: 16px 24px 4px;
  overflow-y: auto;
}

.rescue-modal-footer {
  padding: 12px 24px 20px;
  border-top: 1px solid #EAECEF;
}

.modal-map-wrapper {
  border: 1px solid #E6EEF5;
  border-radius: 12px;
  background-color: #F8FAFC;
  overflow: hidden;
}

.modal-map {
  width: 100%;
  height: 220px;
}

.modal-map-placeholder {
  width: 100%;
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background-color: #F8FAFC;
  border: 1px dashed #CED4DA;
}

.modal-info-grid {
  display: grid;
  row-gap: 10px;
}

.modal-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.95rem;
}

.modal-info-row .label {
  color: #6c757d;
  font-weight: 600;
}

.modal-info-row .value {
  color: #212529;
  font-weight: 500;
  text-align: right;
  margin-left: 16px;
}

.rescue-modal-card .btn-link {
  color: inherit;
}

.rescue-modal-card .btn-link:hover {
  color: #212529;
  text-decoration: none;
}
</style>