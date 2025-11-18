<template>
  <div
    class="event-detail-view container-fluid p-4"
    style="background-color: #F0F2F5; min-height: 100vh;"
  >
    <div
      class="top-info-bar d-flex justify-content-between align-items-center pb-3 border-bottom"
      style="border-color: #E0E2E6;"
    >
      <!-- 🛎 관제센터 속보 바 -->
      <div class="alert-bar d-flex align-items-center fs-1">
        <span class="badge  bg-danger fs-3 fw-bold me-4 p-2">속보</span>

        <transition name="alert-fade" mode="out-in">
          <span :key="currentAlert" class="flex-grow-1 text-truncate fw-semibold mb-1">
            {{ currentAlert }}
          </span>
        </transition>
      </div>
    </div>

    <div class="row g-4">
      <!-- =================== 좌측 영역 =================== -->
      <div class="col-lg-8 d-flex flex-column" style="gap: 1.5rem;">
        <!-- CCTV 2x2 스트림 -->
          <UseStreams
            :ws-url="'ws://localhost:8000/ws/stream'"
            :cam-ids="controlView === '해수욕장' ? [1, 2, 3, 4] : [5, 6, 7, 8]"
            :key="controlView"  
            @danger-update="handleDangerUpdate"
          />
      </div>

      <!-- =================== 우측 패널 =================== -->
      <div class="col-lg-4 d-flex flex-column" style="gap: 1.5rem;">
        <div class="d-flex justify-content-between mb-n3">
          <div>
            <span class="ms-1 fs-2 fw-bold"> 현재 선택된 장소 : </span>
            <span class="fw-bold fs-2">{{ cctvName }} </span>
          </div>
          <div>
            <button type="button" class="alert-send-btn me-2"
            @click="cctvAlert = true">
              안내방송
            </button>
            <button type="button" class="safe-send-btn"
            @click="rescueModal = true">
              구조요청
            </button>
          </div>
        </div>

        <!-- 🔹 탭 + 내용이 하나의 카드 안에 붙어있도록 -->
        <div class="card p-0 border-0 shadow-sm flex-grow-1 h-300px" style="flex-grow: 2;">
          <!-- 탭 바 (카드 헤더처럼) -->
          <div
            class="tab-segment-group w-100 rounded-1 h-30px"
          >
          <button
            v-for="tab in rightTabs"
            :key="tab.key"
            type="button"
            class="tab-segment flex-fill"
            :class="{ active: rightPanelTab === tab.key }"
            @click="rightPanelTab = tab.key"
          >
              {{ tab.label }}
            </button>
          </div>

          <!-- 카드 본문 영역 -->
          <div class="p-3 h-300px " style="overflow-y: auto;">
            <!-- 진입 알림 탭 내용 -->
            <div
              v-if="rightPanelTab === 'overview'"
              class="map-placeholder-base border rounded d-flex flex-column h-100"
              style="background-color: #F0F2F5;"
            >
              <!-- 상단: 제목 + 모두 읽음 버튼 -->
              <div class="d-flex justify-content-between align-items-center" style="height: 10%;">
                    <span
                  class="badge bg-light text-muted small"
                >
                  전체 {{ filteredAlerts.length }}건
                </span>
                
                <button
                  v-show="filteredAlerts.length"
                  type="button"
                  class="btn btn-link p-0 small text-secondary text-decoration-none"
                  @click="markAllAsRead"
                >
                  모두 읽음
                </button>
              </div>

              <!-- 알림 리스트 -->
              <div class="flex-grow-1 overflow-auto px-2 " style="height: 90%;">
                <div
                  v-for="item in filteredAlerts"
                  :key="item.id"
                  class="alert-item d-flex justify-content-between align-items-center py-2 px-2 rounded-3 mb-1"
                  :class="item.read ? 'bg-read' : 'bg-unread'"
                  @click="markAsRead(item.id)"
                >
                  <div class="small">
                    <div class="fw-semibold">
                      {{ item.label }}에서 위험 구역 진입
                      <span class="badge bg-danger ms-1">{{ item.danger }}명</span>
                    </div>
                    <div class="text-muted" style="font-size: 0.75rem;">
                      {{ item.timeText }}
                    </div>
                  </div>

                  <span
                    class="badge rounded-pill"
                    :class="item.read ? 'bg-secondary-subtle text-secondary' : 'bg-primary text-white'"
                  >
                    {{ item.read ? '읽음' : '신규' }}
                  </span>
                </div>
              </div>
            </div>

            <!-- 알림 상세 탭 -->
            <div
              v-if="rightPanelTab === 'detail'"
              class="map-placeholder-base border rounded d-flex flex-column h-100"
              style="background-color: #F0F2F5;"
            >
            <div class="h-300px">

            </div>
            </div>

            <!-- CCTV 정보 탭 -->
            <div
              v-if="rightPanelTab === 'cctv'"
              class="map-placeholder-base border rounded d-flex flex-column h-100"
              style="background-color: #F0F2F5;"
            >

              <!-- 🔹 네이버맵 컨테이너 -->
              <div class="flex-grow-1 h-100">
                <div
                  ref="beachMap"
                  class="naver-map-box"
                ></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 감지 정보 통계 카드 (아래 그대로 유지) -->
        <div class="card p-3 border-0 shadow-sm flex-grow-1" style="flex-grow: 1;">
          <!-- 제목 + 탭 버튼 줄 -->
          <div class="d-flex justify-content-between align-items-center mb-2">
            <h6 class="fw-bold mb-0 small" style="color: #333;">
              {{ statsTab === '10min' ? '10분 위험구역 통계' : '금일 누적 통계' }}
            </h6>

            <div class="tab-segment-group">
              <button
                type="button"
                class="tab-segment"
                :class="{ active: statsTab === '10min' }"
                @click="statsTab = '10min'"
              >
                10분 통계
              </button>

              <button
                type="button"
                class="tab-segment"
                :class="{ active: statsTab === 'today' }"
                @click="statsTab = 'today'"
              >
                금일 누적
              </button>
            </div>
          </div>

  <div
  v-if="statsTab === '10min'"
  class="chart-placeholder-base border rounded p-2 h-100"
  style="background-color: #FFFFFF;"
>
  <div class="table-responsive small">
    <table class="table table-sm table-bordered mb-0 align-middle">
      <thead class="table-light">
        <tr>
          <th scope="col" style="width: 40%;">CCTV</th>
          <th scope="col" class="text-end">최근 10분 위험 진입 횟수</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="id in camList"
          :key="`10-${id}`"
          :class="cctvName === `CAM ${id}` ? 'cam-row-active' : ''"
        >
          <td class="fw-semibold"> {{ getCamLabel(id) }} (CAM {{ id }}) </td>

          <td class="text-end">
            <span
              class="badge"
              :class="(danger10min[id] ?? 0) > 0 ? 'bg-danger text-white' : 'bg-light text-muted'"
            >
              {{ danger10min[id] ?? 0 }} 회
            </span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</div>

<!-- 금일 누적 통계 -->
<div
  v-else
  class="chart-placeholder-base border rounded p-2 h-100"
  style="background-color: #FFFFFF;"
>
  <div class="table-responsive small">
    <table class="table table-sm table-bordered mb-0 align-middle">
      <thead class="table-light">
        <tr>
          <th scope="col" style="width: 40%;">CCTV</th>
          <th scope="col" class="text-end">금일 누적 위험 진입 횟수</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="id in camList"
          :key="`today-${id}`"
          :class="cctvName === `CAM ${id}` ? 'cam-row-active' : ''"
        >
         <td class="fw-semibold">{{ getCamLabel(id) }} (CAM {{ id }}) </td>
          <td class="text-end">
            <span
              class="badge"
              :class="(dangerToday[id] ?? 0) > 0 ? 'bg-danger text-white' : 'bg-light text-muted'"
            >
              {{ dangerToday[id] ?? 0 }} 회
            </span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</div>

        </div>
      </div>
</div>
<div
  v-if="cctvAlert"
  @click.self="cctvAlert= false"
  style="position: fixed; inset: 0; background: rgba(0,0,0,0.35); display: flex; align-items: center; justify-content: center; z-index: 2000;"
>
  <div
    style="background: #ffffff; width: 420px; max-width: 90%; border-radius: 12px; box-shadow: 0 8px 30px rgba(0,0,0,0.15); padding: 16px 18px;"
  >
    <!-- 헤더 -->
    <div
      style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;"
    >
      <h6
        style="margin:0; font-weight:700; font-size:14px;"
      >
       안내 방송 _ {{ cctvName }} 
      </h6>
    </div>

    <!-- 바디 -->
    <div
      style="margin-bottom:12px;"
    >
      <label
        style="display:block; font-size:12px; color:#6c757d; margin-bottom:4px;"
      >
        변환할 텍스트
      </label>
      <textarea
        v-model="alertMessage"
        rows="4"
        class="form-control"
        placeholder="관제센터에서 송출할 방송 문구를 입력하세요. "
        style="height: 120px; resize: none;"
      ></textarea>
    </div>

    <!-- 푸터 -->
    <div
      style="display:flex; justify-content:flex-end; gap:8px;"
    >
      <button
        type="button"
        class="btn btn-danger btn-sm fw-bold"
        @click="sendAlertMessage"
      >
        발송
      </button>
      <button
        type="button"
        class="btn btn-secondary btn-sm"
        @click="cctvAlert = false"
      >
        취소
      </button>
    </div>
  </div>
</div>
<div
  v-if="rescueModal"
  @click.self="rescueModal = false"
  style="position: fixed; inset: 0; background: rgba(0,0,0,0.35); display: flex; align-items: center; justify-content: center; z-index: 2100;"
>
  <div
    style="background: #ffffff; width: 520px; max-width: 95%; border-radius: 12px; box-shadow: 0 8px 30px rgba(0,0,0,0.18); padding: 16px 18px;"
  >
    <div
      style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;"
    >
    <div>
      <h6 style="margin:0; font-weight:700; font-size:14px;">
        구조 요청 위치 확인 _ {{ cctvName }}
      </h6>
      
      <h6 style="margin-top:5px; margin-bottom: 0px; font-weight:600; font-size:11px; color:#e53935;">
        - 위험구역 진입 예상위치
      </h6>
      </div>
    </div>

    <div style="margin-bottom:12px;">
      <div
        ref="rescueMap"
        class="naver-map-box"
        style="height: 260px; margin-bottom: 6px;"
      ></div>
    </div>

    <div style="display:flex; justify-content:flex-end; gap:8px;">
      <button
        type="button"
        class="btn btn-danger btn-sm fw-bold py-2"
        @click="sendRescueRequest"
      >
        구조 요청
      </button>
      <button
        type="button"
        class="btn btn-secondary btn-sm py-2"
        @click="rescueModal = false"
      >
        취소
      </button>
    </div>
  </div>
</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import UseStreams from '@/components/useStreams.vue'
import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'
const store = useStore();
const { controlView, cctvName } = storeToRefs(store)

const rightPanelTab = ref('overview')

const rightTabs = [
  { key: 'overview', label: '진입 알림' },
  { key: 'detail', label: '기상 정보' },
  { key: 'cctv', label: 'CCTV 정보' },
]

// 📊 위험구역 통계 탭 상태 (10분 / 금일 누적)
const statsTab = ref('10min')

// 🛎 속보 텍스트 목록
const alerts = ref([
  '이호테우 해수욕장 신원 미상 남성 해파리 쏘임',
  '함덕 해수욕장 완충구역 내 인원 밀집, 관제 요원 주의 필요',
  '중문 해수욕장 파도 높이 상승, 안전요원 순찰 강화 요망',
  '협재 해수욕장 해파리 다수 관측, 입수객 주의 안내 방송 필요',
  '이호테우 해수욕장 20대 여성 구조 요청'
])

const alertIndex = ref(0)
const currentAlert = computed(() => alerts.value[alertIndex.value])

// 🔔 속보 롤링 타이머
let alertTimer = null

// 📊 감시 대상 CAM 목록 (CAM1 ~ CAM4)
const camList = computed(() =>
  controlView.value === '해수욕장' ? [1, 2, 3, 4] : [5, 6, 7, 8]
)

const camLabelMap = {
  1: '이호테우',
  2: '중문',
  3: '함덕',
  4: '월정리',
  5: '애월 하귀 가문동 포구',
  6: 'CAM 6',
  7: 'CAM 7',
  8: 'CAM 8',
}

const getCamLabel = (id) => camLabelMap[id] ?? `CAM ${id}`

// 📊 10분 단위 위험 진입 카운트 (CAM별)
const danger10min = ref({
  1: 0, 2: 0, 3: 0, 4: 0,
  5: 0, 6: 0, 7: 0, 8: 0,
})

// 📊 금일 누적 위험 진입 카운트 (CAM별)
const dangerToday = ref({
  1: 0, 2: 0, 3: 0, 4: 0,
  5: 0, 6: 0, 7: 0, 8: 0,
})

// 🔔 진입 알림 리스트 (뷰 공통 저장)
// view: 'beach' | 'harbor' 로 해수욕장/항구 구분
const alertEntries = ref([])


// ➕ CCTV별 마지막 알림 시각(분 단위) 기억
// key 예시: 'beach-1' , 'harbor-3'
const lastAlertByCam = ref({})

// ➕ CCTV별 통계(10분/금일)도 1분 단위로만 반영
const lastStatsByCam = ref({})

// 현재 탭이 해수욕장/항구인지에 따라 키 만들기
const currentViewKey = computed(() =>
  controlView.value === '해수욕장' ? 'beach' : 'harbor'
)

// 화면에 보여줄 알림 = 현재 뷰에 해당하는 알림만 필터링
const filteredAlerts = computed(() =>
  alertEntries.value.filter(a => a.view === currentViewKey.value)
)

// 개별 알림 읽음 처리
const markAsRead = (id) => {
  const target = alertEntries.value.find(a => a.id === id)
  if (target) target.read = true
}

// 전체 알림 읽음 처리
const markAllAsRead = () => {
  alertEntries.value.forEach(a => {
    if (a.view === currentViewKey.value) {
      a.read = true
    }
  })
}

// 🔴 UseStreams 에서 올라오는 “위험구역 진입” 이벤트 처리
// payload: { camId, streamId: 'CAM1', label: '이호테우', danger, timestamp }
// payload: { camId, streamId: 'CAM1', label, danger, timestamp }
const handleDangerUpdate = ({ camId, streamId, label, danger, timestamp }) => {
  if (!danger || danger <= 0) return
  if (!camList.value.includes(camId)) return

  const viewKey = currentViewKey.value // 'beach' or 'harbor'
  const now = new Date(timestamp || Date.now())

  // ✅ 시간 텍스트 (HH:MM:SS)
  const timeText = now.toLocaleTimeString('ko-KR', {
    hour12: false,
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })

  // ✅ 같은 "분"인지 비교하기 위한 키 (날짜+시+분)
  const minuteKey = [
    now.getFullYear(),
    now.getMonth(),
    now.getDate(),
    now.getHours(),
    now.getMinutes(),
  ].join('-')

  const camKey = `${viewKey}-${camId}`

  // ================================
  // 1) 통계(10분 / 금일)도 1분 단위로만 반영
  //    → 같은 분에 온 값은 누적 X, 마지막 danger 값으로 교체
  // ================================
  const lastStats = lastStatsByCam.value[camKey]

  if (!lastStats || lastStats.minuteKey !== minuteKey) {
    // 🔹 새로운 분: 그냥 더하기
    danger10min.value[camId] += danger
    dangerToday.value[camId] += danger

    lastStatsByCam.value[camKey] = {
      minuteKey,
      lastDanger: danger,
    }
  } else {
    // 🔹 같은 분: 이전 값 빼고, 이번 값으로 교체
    const diff = danger - lastStats.lastDanger
    danger10min.value[camId] += diff
    dangerToday.value[camId] += diff

    lastStatsByCam.value[camKey].lastDanger = danger
  }

  // ================================
  // 2) 알림 리스트도 1분에 한 줄만, 명수는 마지막 값으로
  // ================================
  const lastInfo = lastAlertByCam.value[camKey]

  if (lastInfo && lastInfo.minuteKey === minuteKey) {
    // 이미 같은 분 알림 있음 → 그 줄만 업데이트
    const target = alertEntries.value.find(a => a.id === lastInfo.alertId)
    if (target) {
      target.danger = danger      // 누적 X, 마지막 값으로
      target.timeText = timeText  // 시간도 최신으로
    }
    return
  }

  // 🆕 새 알림 한 건 추가 (위로 쌓이게 unshift)
  const newId = `${now.getTime()}-${camId}-${Math.random()}`

  alertEntries.value.unshift({
    id: newId,
    view: viewKey,
    camId,
    streamId,
    label: label || `CAM ${camId}`,
    danger,
    timeText,
    read: false,
  })

  // 이 CAM의 "마지막 알림" 갱신
  lastAlertByCam.value[camKey] = {
    minuteKey,
    alertId: newId,
  }

  // 너무 길어지지 않게 최대 30개만 유지
  if (alertEntries.value.length > 30) {
    alertEntries.value.pop()
  }
}



// ⏱ 10분마다 10분 통계만 리셋 (금일 누적은 유지)
let tenMinTimer = null

onMounted(() => {
  // 🔔 속보 문구 롤링
  alertTimer = window.setInterval(() => {
    alertIndex.value = (alertIndex.value + 1) % alerts.value.length
  }, 5000)

  // ⏱ 10분(600,000ms)마다 danger10min 초기화
 tenMinTimer = window.setInterval(() => {
  danger10min.value = {
    1: 0, 2: 0, 3: 0, 4: 0,
    5: 0, 6: 0, 7: 0, 8: 0,
  }
}, 10 * 60 * 1000)

})

onUnmounted(() => {
  if (alertTimer !== null) {
    clearInterval(alertTimer)
  }
  if (tenMinTimer !== null) {
    clearInterval(tenMinTimer)
  }
})


/**
 *  naver map 
 */
const cctvLocation = [
  {
    type: "해수욕장",
    label: "이호테우",
    latitude: 33.497940,
    longitude: 126.453614,
    direction: 300,
    fov: 45,
    range: 300
  },
  {
    type: "해수욕장",
    label: "중문",
    latitude: 33.243882,
    longitude: 126.414540,
    direction: 285,
    fov: 45,
    range: 300
  },
  {
    type: "해수욕장",
    label: "함덕",
    latitude: 33.544320,
    longitude: 126.674138,
    direction: 200,
    fov: 60,
    range: 300
  },
  {
    type: "해수욕장",
    label: "월정리",
    latitude: 33.556556,
    longitude: 126.795072,
    direction: 70,
    fov: 45,
    range: 200
  },
  {
    type: "항구",
    label: "애월 하귀 가문동 포구",
    latitude: 33.486824,
    longitude: 126.392415,
    direction: 60,
    fov: 60,
    range: 300
  },
]
const beachMap = ref(null)
let map
let markers = []
let fovPolygon = null
const latitude = ref(33.396585)
const longitude = ref(126.574286)

watch(
  () => rightPanelTab.value,
  (tab) => {
    if (tab !== 'cctv') return   // ❌ map = null 안 해도 됨

    nextTick(() => {
      if (!beachMap.value || !window.naver?.maps) return

      const center = new window.naver.maps.LatLng(
        latitude.value,
        longitude.value
      )

      // ✅ 탭 들어올 때마다 지도 새로 생성
      map = new window.naver.maps.Map(beachMap.value, {center, zoom: 9})

      // ✅ 기존 마커 제거
      markers.forEach(m => m.setMap(null))
      markers = []

      // ✅ 현재 화면 타입(해수욕장 / 항구)에 맞는 CCTV만 마커로 찍기
      const currentType = controlView.value === '해수욕장' ? '해수욕장' : '항구'

      cctvLocation
        .filter(loc => loc.type === currentType)
        .forEach(loc => {
          const marker = new window.naver.maps.Marker({
            map,
            position: new window.naver.maps.LatLng(loc.latitude, loc.longitude),
            title: loc.label,
          })
          markers.push(marker)
        })
    })
  }
)

watch(
  [() => cctvName.value, () => controlView.value, () => rightPanelTab.value],
  ([name, view, tab]) => {
    // CCTV 정보 탭 아닐 때는 무시
    if (tab !== 'cctv') return

    // 지도 아직 안 만들어졌으면 패스
    if (!map || !window.naver?.maps || !name) return

    // 현재 화면 타입 (해수욕장 / 항구)
    const currentType = view === '해수욕장' ? '해수욕장' : '항구'

    // 선택된 CCTV 찾기
    const target = cctvLocation.find(
      (loc) => loc.type === currentType && loc.label === name
    )

    if (!target) return

    const {
      latitude: lat,
      longitude: lng,
      direction,
      fov,
      range
    } = target

    const center = new window.naver.maps.LatLng(lat, lng)

    // 🔍 선택된 CCTV 위치로 이동 + 줌
    map.setCenter(center)
    map.setZoom(18)

    // ===== 여기부터 시야각 삼각형 간단 버전 =====
    // 기존 폴리곤 지우기
    if (fovPolygon) {
      fovPolygon.setMap(null)
      fovPolygon = null
    }

    const toRad = (deg) => (deg * Math.PI) / 180

    const dist = range / 111000

    const makePoint = (baseLat, baseLng, angleDeg) => {
      const rad = toRad(angleDeg)
      const dLat = Math.cos(rad) * dist
      const dLng = Math.sin(rad) * dist
      return new window.naver.maps.LatLng(baseLat + dLat, baseLng + dLng)
    }

    const startAngle = direction - fov / 2
    const endAngle = direction + fov / 2

    const p1 = makePoint(lat, lng, startAngle)
    const p2 = makePoint(lat, lng, endAngle)

    const path = [center, p1, p2, center] // 삼각형

    fovPolygon = new window.naver.maps.Polygon({
      map,
      paths: path,
      fillColor: 'rgba(51, 51, 51, 1)',
      fillOpacity: 0.18,
      strokeColor: '#4f4f4f',
      strokeOpacity: 0.9,
      strokeWeight: 1,
    })
  }
)

/**
 *  alert
 */

const cctvAlert = ref(false)
const alertMessage = ref('')

// 실제 알림 발송 (백엔드 붙일 자리)
const sendAlertMessage = () => {
  if (!alertMessage.value.trim()) {
    // 비어 있으면 그냥 리턴 (원하면 alert 넣어도 됨)
    return
  }

  // TODO: 여기서 백엔드로 API 호출해서 알림 발송하면 됨
  console.log('🔔 알림 발송:', {
    cctv: cctvName.value,
    message: alertMessage.value,
  })

  // 일단 모달 닫고 내용 비우기
  alertMessage.value = ''
  cctvAlert.value = false
}

/**
 *  rescue
 */

// 모달 열림 상태
const rescueModal = ref(false)

// 네이버맵 DOM ref
const rescueMap = ref(null)

// 지도 / 시야각 폴리곤 인스턴스
let rescueMapInstance = null
let rescueFovPolygon = null

const sendRescueRequest = () => {
  // TODO: 백엔드 연동 시 여기서 API 호출
  // 예시 payload
  const currentType = controlView.value === '해수욕장' ? '해수욕장' : '항구'

  const target = cctvLocation.find(
    (loc) => loc.type === currentType && loc.label === cctvName.value
  )

  const payload = {
    cctv: cctvName.value,
    type: currentType,
    latitude: target?.latitude ?? null,
    longitude: target?.longitude ?? null,
    direction: target?.direction ?? null,
    fov: target?.fov ?? null,
    range: target?.range ?? null,
    requestedAt: new Date().toISOString(),
  }

  console.log('🆘 구조 요청 payload:', payload)

  // 모달 닫기
  rescueModal.value = false
}

watch(
  () => rescueModal.value,
  (visible) => {
    if (!visible) return

    nextTick(() => {
      if (!rescueMap.value || !window.naver?.maps) return

      const currentType = controlView.value === '해수욕장' ? '해수욕장' : '항구'

      // 현재 선택된 CCTV 찾기
      const target = cctvLocation.find(
        (loc) => loc.type === currentType && loc.label === cctvName.value
      )

      if (!target) {
        console.warn('구조요청 모달: CCTV 정보를 찾을 수 없습니다.', cctvName.value)
        return
      }

      const {
        latitude: lat,
        longitude: lng,
        direction,
        fov,
        range,
      } = target

      const center = new window.naver.maps.LatLng(lat, lng)

      // 기존 지도 있으면 제거 (필요시)
      if (rescueMapInstance) {
        rescueMapInstance.destroy?.()
        rescueMapInstance = null
      }

      // 지도 생성
      rescueMapInstance = new window.naver.maps.Map(rescueMap.value, {
        center,
        zoom: 17,
      })

      // 카메라 위치 마커
      new window.naver.maps.Marker({
        map: rescueMapInstance,
        position: center,
        title: cctvName.value,
      })

      // ======== 시야각 폴리곤 (삼각형) ========
      if (rescueFovPolygon) {
        rescueFovPolygon.setMap(null)
        rescueFovPolygon = null
      }

      const toRad = (deg) => (deg * Math.PI) / 180
      const dist = range / 111000 // 단순 위도 기준 (1도 ≒ 111km)

      const makePoint = (baseLat, baseLng, angleDeg) => {
        const rad = toRad(angleDeg)
        const dLat = Math.cos(rad) * dist
        const dLng = Math.sin(rad) * dist
        return new window.naver.maps.LatLng(baseLat + dLat, baseLng + dLng)
      }

      const startAngle = direction - fov / 2
      const endAngle   = direction + fov / 2

      const p1 = makePoint(lat, lng, startAngle)
      const p2 = makePoint(lat, lng, endAngle)

      const path = [center, p1, p2, center]

      rescueFovPolygon = new window.naver.maps.Polygon({
        map: rescueMapInstance,
        paths: path,
        fillColor: 'rgba(51, 51, 51, 1)',
        fillOpacity: 0.2,
        strokeColor: '#4f4f4f',
        strokeOpacity: 0.9,
        strokeWeight: 1,
      })
    })
  }
)

</script>



<style scoped>
:root {
  --iseu-primary: #0092ba;
  --iseu-secondary: #0b1956;
  --iseu-warning: #ffb354;
  --iseu-danger: #eb725b;

  --color-safe: #8482ff;
  --color-light-bg: #f0f2f5;
  --color-panel-bg: #ffffff;
  --color-separator: #eaecef;
  --color-text-dark: #333333;
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

/* 지도/통계 placeholder 기본 스타일 */
.map-placeholder-base,
.chart-placeholder-base {
  font-size: 1rem;
}

/* 로그 스크롤 영역 */
.log-scroll-area {
  max-height: 150px;
  overflow-y: auto;
}

/* 라디오 버튼 스타일 */
.form-check-input {
  background-color: #fff;
  border-color: #ccc;
}
.form-check-input:checked {
  background-color: var(--iseu-primary);
  border-color: var(--iseu-primary);
}

/* 버튼 텍스트 색상 */

/* 속보 바: 한 줄 + ... 처리 */
.alert-bar {
  white-space: nowrap;
  overflow: hidden;
}

/* 속보 전환 애니메이션 */
.alert-fade-enter-active,
.alert-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.alert-fade-enter-from,
.alert-fade-leave-to {
  opacity: 0;
  transform: translateY(6px);
}

.right-tab-bar {
  width: 100%;
  margin-top: 0.25rem;
  border-radius: 0.5rem 0.5rem 0 0;
  overflow: hidden;
}

/* 탭 버튼 공통 스타일 */
.right-tab-btn {
  border: none;
  background: transparent;
  padding: 0.45rem 0.25rem;
  font-size: 0.8rem;
  font-weight: 700;
  color: #6c757d;
  text-align: center;
  cursor: pointer;
}

/* 버튼 사이 구분선 */
.right-tab-btn + .right-tab-btn {
  border-left: 1px solid #dee2e6;
}

/* 활성 탭 */
.right-tab-btn.active {
  background-color: var(--iseu-primary);
  color: #ffffff;
}

/* 비활성 탭 hover 시 */
.right-tab-btn:not(.active):hover {
  background-color: #f1f3f5;
}

.tab-segment-group {
  display: inline-flex;
  border-radius: 999px;
  border: 1px solid #dee2e6;
  overflow: hidden;
  background-color: #ffffff;
  font-size: 0.8rem;
}

.tab-segment {
  padding: 0.3rem 1.1rem;
  border: none;
  background: transparent;
  color: #6c757d;
  font-weight: 700;
  min-width: 70px;
  text-align: center;
  cursor: pointer;
}

.tab-segment + .tab-segment {
  border-left: 1px solid #dee2e6;
}

.tab-segment.active {
  background-color: var(--iseu-primary);
  color:#40C4FF;
}

/* hover 시 살짝 강조 */
.tab-segment:not(.active):hover {
  background-color: #f1f3f5;
}

/* 진입 알림 카드 스타일 */
.alert-item {
  transition: background-color 0.15s ease, transform 0.1s ease;
  cursor: pointer;
}

.alert-item:hover {
  transform: translateY(-1px);
}

/* 안 읽은 알림 */
.bg-unread {
  background-color: #ffe8e5;
}

/* 읽은 알림 */
.bg-read {
  background-color: #ffffff;
}


.naver-map-box {
  width: 100%;
  height: 300px;      /* 🔴 여기 숫자만 조절해서 원하는 높이로 */
  border-radius: 8px;
  overflow: hidden;
  background-color: #e5e8ec; /* 로딩 중에 회색 배경 */
}

.alert-send-btn {
  background-color: #e53935;  /* 빨간색 */
  color: #ffffff;
  border: none;
  border-radius: 6px;
  padding: 0.4rem 0.9rem;
  font-weight: 700;
  font-size: 0.9rem;
  cursor: pointer;
}

.safe-send-btn {
  background-color: #ff9800;;  /* 빨간색 */
  color: #ffffff;
  border: none;
  border-radius: 6px;
  padding: 0.4rem 0.9rem;
  font-weight: 700;
  font-size: 0.9rem;
  cursor: pointer;
}

.cam-row-active {
  background-color: #fff3cd; /* 선택된 CCTV 강조 (연노랑) */
}

.table.table-sm > :not(caption) > * > * {
  padding-top: 0.3rem;
  padding-bottom: 0.3rem;
}
</style>