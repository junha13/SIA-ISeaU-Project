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
            :ws-url="`${import.meta.env.VITE_PYTHON_API_BASE_URL}/ws/stream`"
            :cam-ids="controlView === '해수욕장' ? [1, 2, 3, 4] : [5, 6, 7, 8]"
            :key="controlView"  
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
<div class="p-3 h-300px" style="overflow-y: auto;">
  <!-- 진입 알림 탭 -->
  <div
    v-if="rightPanelTab === 'overview'"
    class="map-placeholder-base border rounded d-flex flex-column h-100"
    style="background-color: #F0F2F5;"
  >
    <!-- 상단: 제목 + 모두 읽음 버튼 -->
    <div class="d-flex justify-content-between align-items-center" style="height: 10%;">
      <span class="badge bg-light text-muted small">
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
    <div class="flex-grow-1 overflow-auto px-2" style="height: 90%;">
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

  <!-- 기상 정보 탭 -->
  <div
    v-else-if="rightPanelTab === 'detail'"
    class="map-placeholder-base border rounded d-flex flex-column h-100"
    style="background-color: #F0F2F5;"
  >
    <WeatherPanel :beach-number="beachNumber" />
  </div>

  <!-- CCTV 정보 탭 -->
  <div
    v-else-if="rightPanelTab === 'cctv'"
    class="map-placeholder-base border rounded d-flex flex-column h-100"
    style="background-color: #F0F2F5;"
  >
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
          :class="cctvName === getCamLabel(id) ? 'cam-row-active' : ''"
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
import WeatherPanel from '@/components/WeatherPanel.vue'
import { useStore } from '@/stores/store.js'
import { storeToRefs } from 'pinia'
import axios from 'axios'

const BEACH_LIST_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/beach/beaches`
const CCTV_LOG_LIST_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/cctv/logList` // ★추가: 위험 로그 조회 API

const store = useStore()
const { controlView, cctvName } = storeToRefs(store)
const { camLabelMap, dangerTemplate, cctvLocation } = store

// 🔹 통계 상태 (기본값은 dangerTemplate 기반)
const danger10min = ref({ ...dangerTemplate })
const dangerToday = ref({ ...dangerTemplate })

const rightPanelTab = ref('overview')
const beachNumberMap = ref({}) // { '이호테우': 6, '중문': 2, ... }

// 기상 패널용 beachNumber
const beachNumber = computed(() => {
  const key = cctvName.value?.trim()

  // ✅ 아무 것도 선택 안 되어 있으면 0 (전체)
  if (!key) {
    return 0
  }

  const num = beachNumberMap.value[key]

  console.log('▶ beachNumber 계산:', {
    key,
    num,
    view: controlView.value,
  })
  return num ?? 0
})
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
  '이호테우 해수욕장 20대 여성 구조 요청',
])

const alertIndex = ref(0)
const currentAlert = computed(() => alerts.value[alertIndex.value])

// 🔔 속보 롤링 타이머
let alertTimer = null

const getCamLabel = (id) => camLabelMap[id] ?? `CAM ${id}`

// 🔔 진입 알림 리스트 (view: 'beach' | 'harbor')
//  - DB 로그를 그대로 리스트로 보여줌
const alertEntries = ref([])

// 현재 탭이 해수욕장/항구인지에 따라 키 만들기
const currentViewKey = computed(() =>
  controlView.value === '해수욕장' ? 'beach' : 'harbor',
)

// 화면에 보여줄 알림 = 현재 뷰에 해당하는 알림만 필터링
const filteredAlerts = computed(() =>
  alertEntries.value.filter((a) => a.view === currentViewKey.value),
)

// 개별 알림 읽음 처리
const markAsRead = async (id) => {
  const target = alertEntries.value.find((a) => a.id === id)
  if (!target || target.read) return

  // 1) UI는 먼저 읽음으로 표시 (낙관적 업데이트)
  target.read = true

  const payload = {
    logNumber: id
  }
  try {
    await axios.post(`${import.meta.env.VITE_API_BASE_URL}/api/cctv/readLog`, null, {
      params: {
        logNumber: id,   // 👉 이게 @RequestParam("logNumber") 로 들어감
      },
    })
  } catch (e) {
    console.error('❌ 로그 읽음 처리 실패:', e)
    // 실패하면 다시 되돌리고 싶으면 이 줄 유지
    target.read = false
  }
}

// 전체 알림 읽음 처리
const markAllAsRead = () => {
  alertEntries.value.forEach((a) => {
    if (a.view === currentViewKey.value) {
      a.read = true
    }
  })
}

// ★추가: 위험 로그(DB)에서 알림 + 통계 재계산
const fetchDangerLogs = async () => {
  try {
    const viewType = controlView.value === '해수욕장' ? 'BEACH' : 'HARBOR'

    const payload = { 
      viewType,
      beachNumber: beachNumber.value ?? 0,  // ✅ 선택 안 되어 있으면 0 (전체)
    } // 실제 스프링 파라미터 이름에 맞게 수정

    const res = await axios.post(CCTV_LOG_LIST_API_URL, payload)
    console.log(res)

    const list = res.data.result?.result

    const now = new Date()

    // 새 통계 객체 초기화
    const new10 = { ...dangerTemplate }
    const newToday = { ...dangerTemplate }

    const rawAlerts = []

    list.forEach((log) => {
      const camId =
        log.camNumber ??
        log.cam_id ??
        log.cctvNumber // 실제 필드명에 맞게 하나 골라 쓰면 됨

      if (!camId || !camLabelMap[camId]) return

      const createdAtStr = log.createdAt
      const createdAt = new Date(createdAtStr)

      const diffMs = now - createdAt
      const diffMin = diffMs / 60000

      const isSameDay =
        now.getFullYear() === createdAt.getFullYear() &&
        now.getMonth() === createdAt.getMonth() &&
        now.getDate() === createdAt.getDate()

      const read = log.read

      // 10분 이내 로그만 카운트
      if (!Number.isNaN(diffMin) && diffMin <= 10) {
        // "횟수" 기준 → 로그 1개 = 1회
        new10[camId] += 1
      }

      // 금일 누적
      if (isSameDay) {
        newToday[camId] += 1
      }

      const viewKey = camId <= 4 ? 'beach' : 'harbor'

      const timeText = createdAt.toLocaleTimeString('ko-KR', {
        hour12: false,
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      })

      const label =
        log.label ?? log.beachName ?? getCamLabel(camId)

      const danger =
        log.dangerCount ?? log.danger ?? 1

      const logNumber = log.dangerLogNumber

      rawAlerts.push({
        id: logNumber,
        view: viewKey,
        camId,
        streamId: `CAM ${camId}`,
        label,
        danger,
        timeText,
        read,
        createdAt,
      })
    })

    // 최신 로그가 위로 오도록 정렬
    rawAlerts.sort((a, b) => b.createdAt - a.createdAt)

    // state 반영 (알림은 최대 30개만)
    alertEntries.value = rawAlerts.slice(0, 30)
    danger10min.value = new10
    dangerToday.value = newToday
  } catch (e) {
    console.error('❌ 위험 로그 불러오기 실패:', e)
  }
}

const camList = computed(() =>
  controlView.value === '해수욕장' ? [1, 2, 3, 4] : [5, 6, 7, 8],
)

const fetchBeachNumberMap = async () => {
  try {
    const backendSort = 'name_asc'

    const payload = {
      region: '',
      sort: backendSort,
      tagFilter: null,
      userLatitude: null,
      userLongitude: null,
    }

    const res = await axios.post(BEACH_LIST_API_URL, payload)

    const beachList = res.data.result || []

    const map = {}
    beachList.forEach((b) => {
      if (!b.beachName || b.beachNumber == null) return

      const shortName = b.beachName.replace(/해수욕장$/, '').trim()
      map[shortName] = b.beachNumber
    })

    beachNumberMap.value = map
    console.log('✅ beachNumberMap 로드됨:', map)
  } catch (e) {
    console.error('❌ beachNumberMap 불러오기 실패:', e)
  }
}

// 🔁 위험 로그 주기적 갱신 타이머
let logTimer = null

onMounted(() => {
  fetchBeachNumberMap()

  // 속보 롤링
  alertTimer = window.setInterval(() => {
    alertIndex.value = (alertIndex.value + 1) % alerts.value.length
  }, 5000)

  // 최초 1번 즉시 조회
  fetchDangerLogs()


  // 이후 5초마다 갱신 (필요하면 10초/30초로 늘려도 됨)
  logTimer = window.setInterval(fetchDangerLogs, 5000)
})

onUnmounted(() => {
  if (alertTimer !== null) {
    clearInterval(alertTimer)
  }
  if (logTimer !== null) {
    clearInterval(logTimer)
  }
})

// 뷰(해수욕장/항구) 변경 시 바로 로그 다시 조회
watch(
  () => controlView.value,
  () => {
    fetchDangerLogs()
  },
)

/**
 *  naver map
 */

const beachMap = ref(null)
let map
let markers = []
let fovPolygon = null
const latitude = ref(33.396585)
const longitude = ref(126.574286)

watch(
  () => rightPanelTab.value,
  (tab) => {
    if (tab !== 'cctv') return

    nextTick(() => {
      if (!beachMap.value || !window.naver?.maps) return

      const center = new window.naver.maps.LatLng(
        latitude.value,
        longitude.value,
      )

      map = new window.naver.maps.Map(beachMap.value, { center, zoom: 9 })

      markers.forEach((m) => m.setMap(null))
      markers = []

      const currentType =
        controlView.value === '해수욕장' ? '해수욕장' : '항구'

      cctvLocation
        .filter((loc) => loc.type === currentType)
        .forEach((loc) => {
          const marker = new window.naver.maps.Marker({
            map,
            position: new window.naver.maps.LatLng(loc.latitude, loc.longitude),
            title: loc.label,
          })
          markers.push(marker)
        })
    })
  },
)

watch(
  [() => cctvName.value, () => controlView.value, () => rightPanelTab.value],
  ([name, view, tab]) => {
    if (tab !== 'cctv') return
    if (!map || !window.naver?.maps || !name) return

    const currentType = view === '해수욕장' ? '해수욕장' : '항구'

    const target = cctvLocation.find(
      (loc) => loc.type === currentType && loc.label === name,
    )

    if (!target) return

    const { latitude: lat, longitude: lng, direction, fov, range } = target

    const center = new window.naver.maps.LatLng(lat, lng)

    map.setCenter(center)
    map.setZoom(18)

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

    const path = [center, p1, p2, center]

    fovPolygon = new window.naver.maps.Polygon({
      map,
      paths: path,
      fillColor: 'rgba(51, 51, 51, 1)',
      fillOpacity: 0.18,
      strokeColor: '#4f4f4f',
      strokeOpacity: 0.9,
      strokeWeight: 1,
    })
  },
)

/**
 *  안내 방송 모달
 */
const cctvAlert = ref(false)
const alertMessage = ref('')

const sendAlertMessage = () => {
  if (!alertMessage.value.trim()) return

  console.log('🔔 알림 발송:', {
    cctv: cctvName.value,
    message: alertMessage.value,
  })

  alertMessage.value = ''
  cctvAlert.value = false
}

/**
 *  구조 요청 모달
 */
const rescueModal = ref(false)
const rescueMap = ref(null)

let rescueMapInstance = null
let rescueFovPolygon = null

const sendRescueRequest = () => {
  const currentType =
    controlView.value === '해수욕장' ? '해수욕장' : '항구'

  const target = cctvLocation.find(
    (loc) => loc.type === currentType && loc.label === cctvName.value,
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

  rescueModal.value = false
}

watch(
  () => rescueModal.value,
  (visible) => {
    if (!visible) return

    nextTick(() => {
      if (!rescueMap.value || !window.naver?.maps) return

      const currentType =
        controlView.value === '해수욕장' ? '해수욕장' : '항구'

      const target = cctvLocation.find(
        (loc) => loc.type === currentType && loc.label === cctvName.value,
      )

      if (!target) {
        console.warn(
          '구조요청 모달: CCTV 정보를 찾을 수 없습니다.',
          cctvName.value,
        )
        return
      }

      const { latitude: lat, longitude: lng, direction, fov, range } = target

      const center = new window.naver.maps.LatLng(lat, lng)

      if (rescueMapInstance) {
        rescueMapInstance.destroy?.()
        rescueMapInstance = null
      }

      rescueMapInstance = new window.naver.maps.Map(rescueMap.value, {
        center,
        zoom: 17,
      })

      new window.naver.maps.Marker({
        map: rescueMapInstance,
        position: center,
        title: cctvName.value,
      })

      if (rescueFovPolygon) {
        rescueFovPolygon.setMap(null)
        rescueFovPolygon = null
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
  },
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

.map-placeholder-base,
.chart-placeholder-base {
  font-size: 1rem;
}

.log-scroll-area {
  max-height: 150px;
  overflow-y: auto;
}

.form-check-input {
  background-color: #fff;
  border-color: #ccc;
}
.form-check-input:checked {
  background-color: var(--iseu-primary);
  border-color: var(--iseu-primary);
}

.alert-bar {
  white-space: nowrap;
  overflow: hidden;
}

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

.right-tab-btn + .right-tab-btn {
  border-left: 1px solid #dee2e6;
}

.right-tab-btn.active {
  background-color: var(--iseu-primary);
  color: #ffffff;
}

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
  color: #40c4ff;
}

.tab-segment:not(.active):hover {
  background-color: #f1f3f5;
}

.alert-item {
  transition: background-color 0.15s ease, transform 0.1s ease;
  cursor: pointer;
}

.alert-item:hover {
  transform: translateY(-1px);
}

.bg-unread {
  background-color: #ffe8e5;
}

.bg-read {
  background-color: #ffffff;
}

.naver-map-box {
  width: 100%;
  height: 300px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #e5e8ec;
}

.alert-send-btn {
  background-color: #e53935;
  color: #ffffff;
  border: none;
  border-radius: 6px;
  padding: 0.4rem 0.9rem;
  font-weight: 700;
  font-size: 0.9rem;
  cursor: pointer;
}

.safe-send-btn {
  background-color: #ff9800;
  color: #ffffff;
  border: none;
  border-radius: 6px;
  padding: 0.4rem 0.9rem;
  font-weight: 700;
  font-size: 0.9rem;
  cursor: pointer;
}

.cam-row-active {
  background-color: #fff3cd;
}

.table.table-sm > :not(caption) > * > * {
  padding-top: 0.3rem;
  padding-bottom: 0.3rem;
}
</style>