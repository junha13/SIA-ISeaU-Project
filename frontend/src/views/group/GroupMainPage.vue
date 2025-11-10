<template>
  <div class="group-main-page" style="position: relative;">

    <div ref="mapEl" style="width:100%;height:300px;"></div>

    <div class="map-overlay-buttons position-absolute top-0 end-0 p-3">


    </div>
    
    <div class="group-actions p-3">

      <template v-if="hasGroup">
        
        <div class="d-flex justify-content-between align-items-center mb-4 gap-2">
          
          <button
            class="btn fw-bold rounded-pill shadow-sm action-button notification-button" 
            @click="handleNotificationSettings">
            <i class="fas fa-bell me-1"></i> 알림
          </button>
          
          <div class="d-flex gap-2">
            <button
              class="btn fw-bold text-white rounded-pill shadow-sm action-button" 
              :style="{ backgroundColor: mainColor }" 
              @click="showInviteModal = true">
              <i class="fas fa-user-plus me-1"></i> 초대
            </button>
            
            <button 
              class="btn fw-bold rounded-pill shadow-sm action-button btn-outline-danger" 
              @click="confirmDeleteGroup"> <i class="fas fa-trash me-1"></i> 삭제
            </button>
          </div>
        </div>

        <h3 class="fw-bolder mb-3" :style="{ color: darkColor }">
            {{ groupName }}
        </h3>

        <h6 class="fw-bold mb-3" :style="{ color: darkColor }">그룹 멤버 ({{ groupLocations.length }}명)</h6>
        <div class="member-list">
          <div v-for="member in groupLocations" :key="member.id" class="d-flex align-items-center py-2 border-bottom">
            <div class="me-3 rounded-pill" :style="{ backgroundColor: member.color, width: '4px', height: '50px' }"></div>
            <div class="flex-grow-1">
              <h6 class="fw-bolder mb-0 fs-6">{{ member.name }} <span class="small text-muted fw-normal ms-1">{{ member.username }}</span></h6>
              <!-- 기존 거리 표시 줄 교체 -->
            <p v-if="member.status === 'online'" class="text-secondary small mb-0">
              {{ bootLoading
                ? '로딩중...'
                : (Number(member.distance) <= 0.2
                    ? '본인'
                    : (Number.isFinite(Number(member.distance))
                        ? `나와의 거리 : ${Number(member.distance).toFixed(1)} m`
                        : '나와의 거리 : -')) }}
            </p>

            <!-- 수영/육지 상태 표시 (⚠ 준비되기 전엔 절대 '육지' 안뜸) -->
<p v-if="member.status === 'online'" class="text-secondary small mb-0">
  {{
    !isStatusReady
      ? '상태 확인 중...'
      : (
          // 수영/육지 판단 우선
          (member.userStatus === false || member.userStatus === 'false')
            ? '수영 중'
            : (
                // 본인일 때만 해안선 거리 노출 (boundary_distance가 있을 때만)
                Number(member.distance) <= 0.2
                  ? (boundary_distance != null
                      ? `육지 (해안선까지의 거리 ${Number(boundary_distance).toFixed(1)} m)`
                      : '육지')
                  : '육지'
              )
        )
  }}
</p>
            </div>
            <div class="d-flex align-items-center">
              <span v-if="member.status === 'online'" class="text-success small fw-bold" style="font-size: 8px;"> (등록완료) </span>
              <span v-else-if="member.status === 'pending'" class="text-muted small fw-bold" style="font-size: 8px;"> (초대 중) </span>
              <i class="fas fa-comment-dots text-secondary ms-3 me-3" style="cursor: pointer;"></i>
              <i class="fas fa-ellipsis-v text-secondary" style="cursor: pointer;"></i>
            </div>
          </div>
        </div>
      </template>

      <template v-else>
        
        <div class="d-flex justify-content-end align-items-center mb-4 gap-2">
            <button class="btn fw-bold text-white action-button create-button-full" 
                    :style="{ backgroundColor: mainColor, minWidth: '150px' }" 
                    @click="showCreateGroupModal = true">
                <i class="fas fa-plus me-2"></i> 그룹 생성하기
            </button>
        </div>
        
        <h6 class="fw-bold mb-3" :style="{ color: darkColor }">그룹 멤버 (0명)</h6>
        <div class="alert alert-info text-center" role="alert" style="color: #666; background-color: #f0f8ff; border-color: #cce5ff;">
            그룹 생성 후 멤버가 표시됩니다.
        </div>
      </template>
    </div>

    <GroupInviteModal
      v-if="hasGroup && activeGroupId != null"
      v-model:isVisible="showInviteModal"
      :group-id="Number(activeGroupId)"
    />
    <GroupCreateModal 
      v-model:isVisible="showCreateGroupModal" 
      @group-created="handleGroupCreated" 
    />
    <GroupAlertSettingsModal
      v-model:isVisible="showAlertModal"
      :levels="alertSettings"
    />
    <GroupInviteConfirmModal
      v-if="receivedInvitation"
      :isVisible="true"
      :invitationData="receivedInvitation"
      @confirm="acceptInvitation"
      @cancel="rejectInvitation"
    />
  </div>
  <div v-if="alertDialog.visible" class="ga-backdrop">
  <div class="ga-modal">
    <div class="ga-header">
      <h5>알림</h5>
      <button class="ga-close" @click="closeAlert">×</button>
    </div>
    <div class="ga-body">
      {{ alertDialog.message }}
    </div>
    <div class="ga-footer">
      <button class="ga-btn" @click="closeAlert">확인</button>
    </div>
  </div>
</div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch, watchEffect } from 'vue'
import axios from 'axios'
import GroupInviteModal from '@/components/GroupInviteModal.vue'
import GroupCreateModal from '@/components/GroupCreateModal.vue'
import GroupAlertSettingsModal from '@/components/GroupAlertSettingsModal.vue'
import GroupInviteConfirmModal from '@/components/GroupInviteConfirmModal.vue'
import { useStore } from '@/stores/store.js'
import { useGroupStore } from '@/stores/groupStore'
import { storeToRefs } from 'pinia'
const store = useStore()
const { header } = storeToRefs(store)

const groupStore = useGroupStore()
const { receivedInvitation } = storeToRefs(groupStore)
const { acceptInvitation, rejectInvitation, checkPendingInvitations } = groupStore

/* ===== 색상 ===== */
const mainColor = '#0092BA'
const darkColor = '#0B1956'

/* ===== 지도/마커 ===== */
const mapEl = ref(null)
let map
let memberMarkers = []
const firstPingDone = ref(false)

/* ===== 상태 ===== */
const myGroupList = ref([])
const activeGroupLocations = ref([])
const showAlertModal = ref(false)
const showInviteModal = ref(false)
const showCreateGroupModal = ref(false)
const latitude = ref(null)
const longitude = ref(null)
const bootLoading = ref(true)

const lastStatus = ref({})
const stableStatus = ref({})

const normStatus = (v) =>
  (v === false || v === 'false') ? 'swim'
  : (v === true  || v === 'true')  ? 'land'
  : null


const isStatusReady = computed(() =>
  firstPingDone.value &&
  user_status.value !== null &&          // ✅ 수영/육지 확정이 먼저
  latitude.value !== null &&
  longitude.value !== null
)

/* ===== 계산 ===== */
const hasGroup = computed(() => myGroupList.value.length > 0)
const activeGroupId = computed(() => hasGroup.value ? myGroupList.value[0].id : null)
const groupName = computed(() => hasGroup.value ? myGroupList.value[0].name : '')

/* 멤버 위치 중복 제거 + 정렬 */
const groupLocations = computed(() => {
  const mapObj = {}
  activeGroupLocations.value.forEach(m => {
    if (!m.id) return
    const d = m.distance != null ? Number(m.distance) : null
    const lat = m.lat ?? m.latitude ?? null
    const lng = m.lng ?? m.longitude ?? null
    mapObj[m.id] = { ...(mapObj[m.id] || {}), ...m, distance: d, lat, lng }
  })
  return Object.values(mapObj).sort((a, b) => {
    const aMe = a.distance != null && a.distance <= 0.3
    const bMe = b.distance != null && b.distance <= 0.3
    if (aMe && !bMe) return -1
    if (!aMe && bMe) return 1
    return (a.distance ?? 999999) - (b.distance ?? 999999)
  })
})

/* ===== API: 그룹 ===== */
const fetchGroups = async () => {
  try {
    const url = `${import.meta.env.VITE_API_BASE_URL}/api/groups?timestamp=${Date.now()}`
    const res = await axios.get(url, { withCredentials: true })
    myGroupList.value = res.data?.data?.result ?? []
    header.value = groupName.value || '그룹 화면'
  } catch (e) {
    console.error('그룹 목록 조회 실패:', e)
    myGroupList.value = []
  }
}
const handleGroupCreated = () => { showCreateGroupModal.value = false; fetchGroups() }
const confirmDeleteGroup = () => { if (activeGroupId.value) deleteGroup() }
const deleteGroup = async () => {
  try {
    const url = `${import.meta.env.VITE_API_BASE_URL}/api/groups/${activeGroupId.value}`
    await axios.delete(url, { withCredentials: true })
    activeGroupLocations.value = []
    fetchGroups()
  } catch (e) {
    console.error('그룹 삭제 실패:', e); alert('그룹 삭제에 실패했습니다.')
  }
}
const handleNotificationSettings = () => { showAlertModal.value = true }

/* ===== API: 위치 ===== */
const fetchLocations = async () => {
  if (!activeGroupId.value) return
  const payload = {
    myLatitude: latitude.value,
    myLongitude: longitude.value,
    groupNumber: activeGroupId.value
  }
  try {
    const url = `${import.meta.env.VITE_API_BASE_URL}/api/groups/locations`
    const res = await axios.post(url, payload, { withCredentials: true })
    activeGroupLocations.value = res.data?.data?.result ?? []
  } catch (e) {
    console.error('그룹 위치 정보 조회 실패:', e)
    activeGroupLocations.value = []
  } finally {
    bootLoading.value = false
  }
}

/* ===== 위치 스트림: watchPosition (초기 응답을 빠르게) ===== */
let geoWatchId = null
const startGeoWatch = () => {
  if (!navigator.geolocation) return
  if (geoWatchId != null) return
  geoWatchId = navigator.geolocation.watchPosition(
    pos => {
      latitude.value = pos.coords.latitude
      longitude.value = pos.coords.longitude
      // 좌표가 처음 들어온 순간에 지도/마커가 바로 반응
    },
    err => {
      console.error('지오로케이션 실패:', err.message)
    },
    { enableHighAccuracy: true, maximumAge: 2000, timeout: 5000 }
  )
}
const stopGeoWatch = () => {
  if (geoWatchId != null && navigator.geolocation) {
    navigator.geolocation.clearWatch(geoWatchId)
    geoWatchId = null
  }
}

/* ===== 서버 boundary 체크 루프 (interval) ===== */
let boundary_distance = ref(null)
let user_status = ref(null)
let geoTimer = null
let serverIntervalMs = 2000

const pingBoundaryAndLocations = async () => {
  if (!activeGroupId.value || latitude.value == null || longitude.value == null) {
  return
}
  try {
    // test 모드로 서버 인터벌/상태 가져오기
    const axiosUrl = `${import.meta.env.VITE_API_BASE_URL}/api/location/testBoundaryCheck`
    const payload = {
      latitude: latitude.value,
      longitude: longitude.value,
      groupNumber: activeGroupId.value,
      userStatus: user_status.value
    }
    const res = await axios.post(axiosUrl, payload, {
      headers: { 'Content-Type': 'application/json' },
      withCredentials: true,
      timeout: 5000
    })
    const r = res.data?.data?.result
if (r) {
  serverIntervalMs = r.interval ?? serverIntervalMs
  boundary_distance.value = (r.distance ?? null)
  user_status.value = (r.inside ?? null)

  // ✅ user_status가 들어와야만 Ready로 간주 (boundary만 먼저 와도 렌더 안 함)
  firstPingDone.value = (user_status.value !== null)
}
  } catch (e) {
    console.error('boundary check error', e)
  }
  // 그룹 위치도 같이 당겨오기
  await fetchLocations()
}
const startGeoLoop = (ms) => {
  if (geoTimer) clearInterval(geoTimer)
  geoTimer = setInterval(pingBoundaryAndLocations, ms)
}
const restartGeoLoop = () => startGeoLoop(serverIntervalMs)

/* ===== 지도 1회 초기화 (중복 watchEffect 제거) ===== */
watchEffect(() => {
  const lat = latitude.value
  const lng = longitude.value
  if (!lat || !lng || !mapEl.value || !window.naver?.maps) return
  const pos = new window.naver.maps.LatLng(lat, lng)

  if (!map) {
    map = new window.naver.maps.Map(mapEl.value, { center: pos, zoom: 18 })
    // 지도만 먼저 띄우고, boundary는 지연 로드
    setTimeout(() => {
      // 필요시 BBOX로 WFS 제한해서 불러오세요 (아래 함수에서 설명)
      loadBoundariesDeferred()
    }, 0)
  } else {
    map.setCenter(pos)
  }
})
// ✅ 지오루프/워치 정리
const clearLoops = () => {
  if (geoTimer) { clearInterval(geoTimer); geoTimer = null }
  stopGeoWatch()
}

// ✅ 화면/상태 찌꺼기 초기화
const resetTransient = () => {
  // 서버 상태
  boundary_distance.value = null
  user_status.value = null
  firstPingDone.value = false

  // UI 상태
  alertDialog.value = { visible: false, message: '' }
  bootLoading.value = true

  // 목록/거리 기록
  activeGroupLocations.value = []
  prevMemberDistances.value = {}
  prevMemberSwim.value = {}

  // 지도 마커 제거
  memberMarkers.forEach(m => m.setMap?.(null))
  memberMarkers = []
}
/* ===== 그룹이 생기면 즉시 1회 로딩 + 이후 주기 ===== */
watch(activeGroupId, async (newId) => {
  if (!newId) {
    activeGroupLocations.value = []
    if (geoTimer) { clearInterval(geoTimer); geoTimer = null }
    return
  }
  // 즉시 1회: 초기 화면 빨리 채움
  await fetchLocations()
  // 서버 interval 기반 루프 시작
  await pingBoundaryAndLocations()
  restartGeoLoop()
}, { immediate: true })

/* ===== Boundary (지연 로드 + 선택적 BBOX 최적화) ===== */
const loadBoundariesDeferred = async () => {
  try {
    // 기본 URL
    const base = `${import.meta.env.VITE_GEO_BASE_URL}/geoserver/iseau/ows`
    // BBOX 최적화 (지도의 현재 bounds로 제한) — 전체 레이어가 크면 필수!
    const bounds = map.getBounds()
    const sw = bounds.getSW(); const ne = bounds.getNE()
    const bbox = `${sw.lng()},${sw.lat()},${ne.lng()},${ne.lat()},EPSG:4326`

    const url = `${base}?service=WFS&version=1.0.0&request=GetFeature&typeName=iseau:tb_test_layer&outputFormat=application/json&srsName=EPSG:4326&BBOX=${encodeURIComponent(bbox)}`
    const res = await fetch(url)
    const data = await res.json()
    const rings = []
    ;(data.features || []).forEach(f => {
      const g = f.geometry
      if (!g) return
      g.coordinates.forEach(poly => rings.push(poly[0])) // 외곽링만
    })
    drawRings(rings, '#0092BA')
    fitRings(rings)
  } catch (e) {
    console.error('Boundary 로드 실패:', e)
  }
}
const drawRings = (rings, color) => {
  rings.forEach(ring => {
    const path = ring.map(([lon, lat]) => new naver.maps.LatLng(lat, lon))
    new naver.maps.Polyline({ map, path, strokeColor: color, strokeWeight: 3, strokeOpacity: 0.9 })
  })
}
const fitRings = (rings) => {
  const bounds = new naver.maps.LatLngBounds()
  rings.forEach(ring => ring.forEach(([lon, lat]) => bounds.extend(new naver.maps.LatLng(lat, lon))))
  if (!bounds.isEmpty?.() && bounds.hasOwnProperty('extend')) map.fitBounds(bounds)
}

/* ===== 마커: 멤버 목록이 바뀌면 갱신 ===== */
watch(groupLocations, (list) => {
  if (!list.length || !map || !window.naver?.maps) return
  memberMarkers.forEach(m => m.setMap(null))
  memberMarkers = []
  list.forEach(member => {
    if (member.status !== 'online') return   // 온라인만 마커
    if (!member.lat || !member.lng) return
    const marker = new naver.maps.Marker({
      position: new naver.maps.LatLng(member.lat, member.lng),
      map,
      title: member.name,
      icon: {
        content: `
          <div style="
            width:15px;height:15px;border-radius:100%;
            background:${member.color || '#0092BA'};
            border:1px solid white;box-shadow:0 2px 6px rgba(0,0,0,.25);
          "></div>`,
        anchor: new naver.maps.Point(10, 10)
      }
    })
    memberMarkers.push(marker)
  })
}, { deep: true })

/* ===== 알림 ===== */
const alertSettings = ref([
  { id: 1, label: '3m 이탈 알림', radius: 3, enabled: true },
  { id: 2, label: '200m 이탈 알림', radius: 200, enabled: false },
  { id: 3, label: '해안선 알림', radius: 0, enabled: true }
])
const prevMemberDistances = ref({})
const prevMemberSwim = ref({})
const alertDialog = ref({ visible: false, message: '' })
const pushAlert = (_type, msg) => { alertDialog.value.visible = true; alertDialog.value.message = msg }
const closeAlert = () => { alertDialog.value.visible = false }

watch(groupLocations, (members) => {
  members.forEach(m => {
    if (!m.id) return

        // ✅ 초대중이면 알림 전부 무시
  if (m.status !== 'online') return

  const isMe = Number(m.distance) <= 0.3   // 필요하면 임계값 조정
    if (isMe) return


    const now = Number(m.distance)
    if (!Number.isFinite(now)) return
    const prev = prevMemberDistances.value[m.id]
    if ((prev == null && now >= 3) || (prev != null && prev < 3 && now >= 3)) {
      if (alertSettings.value.find(l => l.id === 1)?.enabled) {
        pushAlert('radius', `⚠ ${m.name}님이 3m 이상 떨어졌어요. (${now.toFixed(1)}m)`)
      }
    }
    prevMemberDistances.value[m.id] = now

    const nowSwim = m.userStatus === 'false'
    const prevSwim = prevMemberSwim.value[m.id]
    if (prevSwim !== undefined && prevSwim === false && nowSwim === true) {
      pushAlert('swim', `🌊 ${m.name}님이 수영 중으로 바뀌었어요.`)
    }
    prevMemberSwim.value[m.id] = nowSwim
  })
}, { deep: true })

/* ===== 라이프사이클 ===== */
onMounted(async () => {
  // 위치 스트림 & 그룹 병렬 시작 → 먼저 오는 걸로 바로 화면 채움
  await Promise.allSettled([ (async () => startGeoWatch())(), fetchGroups() ])
  // 그룹명 헤더
  header.value = groupName.value || '그룹 화면'

  checkPendingInvitations()
})
onUnmounted(() => {
  stopGeoWatch()
  if (geoTimer) clearInterval(geoTimer)
})

  watch(groupLocations, (members) => {
  members.forEach(m => {
    if (!m?.id) return
    const s = normStatus(m.userStatus) // null | 'swim' | 'land'
    if (s == null) return

    // 이전값과 같으면 확정(stable)로 승격
    if (lastStatus.value[m.id] === s) {
      stableStatus.value[m.id] = s
    }
    lastStatus.value[m.id] = s
  })
})
</script>

<style scoped>
/* --------------------------------- */
/* 🎨 디자인 (CSS) */
/* --------------------------------- */
.group-main-page {
  min-height: calc(100vh - 55px - 60px);
}
.map-overlay-buttons button:first-child {
  background-color: rgba(255, 255, 255, 0.8);
  color: v-bind(darkColor);
  font-size: 0.8rem;
  padding: 5px 10px;
}
.map-overlay-buttons button:last-child {
  background-color: v-bind(mainColor) !important;
  color: white !important;
}

/* 💡 그룹이 없을 때 카드 스타일 */
.empty-group-card {
  border-width: 1px !important;
  border-radius: 0.5rem;
  width: 100%;
  max-width: 400px;
}

/* 💡 [디자인 수정] 버튼들을 지도 아래로 내림 */
.group-actions {
  position: relative; 
  padding-top: 1rem; 
}

/* 💡 [추가] 3개 버튼 공통 스타일 (크기 고정) */
.action-button {
  font-size: 0.9rem;
  padding: 8px 12px; 
  height: 42px;      /* 높이 통일 */
  text-align: center;
  border-width: 1px;
  min-width: 90px; /* 최소 너비로 크기 고정 */
}

.notification-button {
  color: v-bind(darkColor); 
  border: 1px solid #dee2e6; 
  background-color: #e9ecef; 
}

.btn-outline-danger {
  border-color: #dc3545;
  color: #dc3545;
  background-color: white; 
}

.btn-outline-danger:hover {
  background-color: #dc3545;
  color: white;
}

/* 모달관련 지우기 */
.ga-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3000;
}
.ga-modal {
  width: 280px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 14px 35px rgba(0,0,0,.15);
}
.ga-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px 6px;
}
.ga-body {
  padding: 12px 14px 4px;
  font-size: 14px;
}
.ga-footer {
  padding: 10px 14px 14px;
  display: flex;
  justify-content: flex-end;
}
.ga-btn {
  background: #0092ba;
  border: none;
  color: #fff;
  padding: 5px 12px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
}
.ga-close {
  background: transparent;
  border: none;
  font-size: 18px;
  cursor: pointer;
}
</style>