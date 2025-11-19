<template>
  <div class="group-main-page" style="position: relative;">
    <div ref="mapEl" style="width: 100%; height: 300px;"></div>

    <div class="map-overlay-buttons position-absolute top-0 end-0 p-3"></div>

    <div class="group-actions p-3">
      <template v-if="hasGroup">
        <div class="d-flex justify-content-between align-items-center mb-4 gap-2">
          <button
            class="btn fw-bold rounded-pill shadow-sm action-button notification-button"
            @click="handleNotificationSettings"
          >
            <i class="fas fa-bell me-1"></i> 알림
          </button>

          <div class="d-flex gap-2">
            <button
              class="btn fw-bold text-white rounded-pill shadow-sm action-button"
              :style="{ backgroundColor: mainColor }"
              @click="showInviteModal = true"
            >
              <i class="fas fa-user-plus me-1"></i> 초대
            </button>

            <button
              class="btn fw-bold rounded-pill shadow-sm action-button btn-outline-danger"
              @click="confirmDeleteGroup"
            >
              <i class="fas fa-trash me-1"></i> 삭제
            </button>
          </div>
        </div>

        <h3 class="fw-bolder mb-3" :style="{ color: darkColor }">{{ groupName }}</h3>

        <h6 class="fw-bold mb-3" :style="{ color: darkColor }">
          그룹 멤버 ({{ groupLocations.length }}명)
        </h6>

        <div class="member-list">
          <div
            v-for="member in groupLocations"
            :key="member.id"
            class="d-flex align-items-center py-2 border-bottom"
          >
            <div
              class="me-3 rounded-pill"
              :style="{ backgroundColor: member.color, width: '4px', height: '50px' }"
            />

            <div class="flex-grow-1">
              <h6 class="fw-bolder mb-0 fs-6">
                {{ member.name }}
                <span class="small text-muted fw-normal ms-1">{{ member.username }}</span>
              </h6>

              <p v-if="member.status === 'online'" class="text-secondary small mb-0">
                {{
                  bootLoading
                    ? '로딩중...'
                    : (Number(member.distance) <= 0.2
                        ? '본인'
                        : (Number.isFinite(Number(member.distance))
                            ? `나와의 거리 : ${Number(member.distance).toFixed(1)} m`
                            : '나와의 거리 : -'))
                }}
              </p>

              <p v-if="member.status === 'online'" class="text-secondary small mb-0">
                {{
                  !isStatusReady
                    ? '상태 확인 중...'
                    : (
                        (member.userStatus === false || member.userStatus === 'false')
                          ? '수영 중'
                          : (
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
              <span
                v-if="member.status === 'online'"
                class="text-success small fw-bold"
                style="font-size: 8px;"
              >
                (등록완료)
              </span>
              <span
                v-else-if="member.status === 'pending'"
                class="text-muted small fw-bold"
                style="font-size: 8px;"
              >
                (초대 중)
              </span>
              <i class="fas fa-comment-dots text-secondary ms-3 me-3" style="cursor: pointer;"></i>
              <i class="fas fa-ellipsis-v text-secondary" style="cursor: pointer;"></i>
            </div>
          </div>
        </div>
      </template>

      <template v-else>
        <div class="d-flex justify-content-end align-items-center mb-4 gap-2">
          <button
            class="btn fw-bold text-white action-button create-button-full"
            :style="{ backgroundColor: mainColor, minWidth: '150px' }"
            @click="showCreateGroupModal = true"
          >
            <i class="fas fa-plus me-2"></i> 그룹 생성하기
          </button>
        </div>

        <h6 class="fw-bold mb-3" :style="{ color: darkColor }">그룹 멤버 (0명)</h6>
        <div
          class="alert alert-info text-center"
          role="alert"
          style="color: #666; background-color: #f0f8ff; border-color: #cce5ff;"
        >
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
      :levels="alertSettingsArr" 
      :group-id="Number(activeGroupId)"
      @settings-updated="handleSettingsUpdated"
      @settings-synced="handleSettingsSynced" />

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
    : (v === true || v === 'true') ? 'land'
      : null

const isStatusReady = computed(() =>
  firstPingDone.value &&
  user_status.value !== null &&
  latitude.value !== null &&
  longitude.value !== null
)

/* ===== 계산 ===== */
const hasGroup = computed(() => myGroupList.value.length > 0)
const activeGroupId = computed(() => (hasGroup.value ? myGroupList.value[0].id : null))
const groupName = computed(() => (hasGroup.value ? myGroupList.value[0].name : ''))

/* 멤버 위치 중복 제거 + 정렬 */
const groupLocations = computed(() => {
  const mapObj = {}
  activeGroupLocations.value.forEach((m) => {
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

const handleGroupCreated = () => {
  showCreateGroupModal.value = false
  fetchGroups()
}

const confirmDeleteGroup = () => {
  if (activeGroupId.value) deleteGroup()
}

const deleteGroup = async () => {
  try {
    const url = `${import.meta.env.VITE_API_BASE_URL}/api/groups/${activeGroupId.value}`
    await axios.delete(url, { withCredentials: true })
    activeGroupLocations.value = []
    fetchGroups()
  } catch (e) {
    console.error('그룹 삭제 실패:', e)
    alert('그룹 삭제에 실패했습니다.')
  }
}

// 알림 설정 로드/변환
const fetchAlertSettings = async (groupId) => {
  if (!groupId) return []
  try {
    const url = `${import.meta.env.VITE_API_BASE_URL}/api/groups/settings/${groupId}`
    const res = await axios.get(url, { withCredentials: true })
    const dbSettings = res.data?.data?.settings
    if (dbSettings) {
      return transformDbToLevels(dbSettings)
    }
  } catch (e) {
    console.error('알림 설정 로드 실패 (Sync):', e)
  }
  return [
    { id: 1, label: '3m 이탈 알림', radius: 3, enabled: true, levelField: 'groupLeaveLevel1' },
    { id: 2, label: '200m 이탈 알림', radius: 200, enabled: false, levelField: 'groupLeaveLevel2' },
    { id: 3, label: '해안선 알림', radius: 0, enabled: true, levelField: 'tide' },
  ]
}

const transformDbToLevels = (dbSettings) => {
  return [
    {
      id: 1,
      label: '3m 이탈 알림',
      radius: dbSettings.groupLeaveLevel1Distance || 3,
      enabled: dbSettings.groupLeaveLevel1Alert === 'Y',
      levelField: 'groupLeaveLevel1',
    },
    {
      id: 2,
      label: '200m 이탈 알림',
      radius: dbSettings.groupLeaveLevel2Distance || 200,
      enabled: dbSettings.groupLeaveLevel2Alert === 'Y',
      levelField: 'groupLeaveLevel2',
    },
    {
      id: 3,
      label: '해안선 알림',
      radius: 0,
      enabled: dbSettings.tideAlert === 'Y',
      levelField: 'tide',
    },
  ]
}

// 💡 [수정] alertSettings를 groupLevels로 변경하고, 초기값은 transformDbToLevels({})로 설정
const alertSettingsArr = ref(transformDbToLevels({}))

const handleNotificationSettings = async () => {
  if (!activeGroupId.value) {
    alert('그룹을 선택/생성한 후 알림 설정을 할 수 있습니다.')
    return
  }
  // alertSettingsArr.value = await fetchAlertSettings(activeGroupId.value) // 💡 GET 실패로 돌아가는 문제를 방지하기 위해 주석 처리 또는 제거 필요
  showAlertModal.value = true
}

const handleSettingsUpdated = async () => {
  // 💡 GET 실패로 토글이 돌아가는 문제를 방지하기 위해 이 함수에서 props를 직접 갱신하지 않고, settings-synced를 사용합니다.
  // 이 함수는 주로 부모가 자식에게 알리는 용도로 사용됩니다.
  // alertSettingsArr.value = await fetchAlertSettings(activeGroupId.value) 
}

// 💡 [추가] 새로운 이벤트 핸들러: 자식 모달에서 저장 성공 후 최신 상태를 직접 받아 갱신
const handleSettingsSynced = (syncedLevels) => {
    // 💡 [핵심 추가] 알림 설정을 변경하면,
    // 이전 거리 상태를 초기화하여 다음 위치 갱신 시
    // 현재 거리가 새로운 '이탈 이벤트'로 재평가되도록 강제합니다.
  prevMemberDistances.value = {};
    // 모달에서 전달받은 최신 상태로 alertSettingsArr를 직접 갱신
    alertSettingsArr.value = JSON.parse(JSON.stringify(syncedLevels));
};

/* ===== API: 위치 ===== */
const fetchLocations = async () => {
  if (!activeGroupId.value) return
  const payload = {
    myLatitude: latitude.value,
    myLongitude: longitude.value,
    groupNumber: activeGroupId.value,
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

/* ===== 위치 스트림 ===== */
let geoWatchId = null
const startGeoWatch = () => {
  if (!navigator.geolocation) return
  if (geoWatchId != null) return
  geoWatchId = navigator.geolocation.watchPosition(
    (pos) => {
      latitude.value = pos.coords.latitude
      longitude.value = pos.coords.longitude
    },
    (err) => {
      console.error('지오로케이션 실패:', err.message)
    },
    { enableHighAccuracy: true, maximumAge: 2000, timeout: 5000 },
  )
}

const stopGeoWatch = () => {
  if (geoWatchId != null && navigator.geolocation) {
    navigator.geolocation.clearWatch(geoWatchId)
    geoWatchId = null
  }
}

/* ===== 서버 boundary 체크 루프 ===== */
const boundary_distance = ref(null)
const user_status = ref(null)
let geoTimer = null
let serverIntervalMs = 2000

const pingBoundaryAndLocations = async () => {
  if (!activeGroupId.value || latitude.value == null || longitude.value == null) {
    return
  }

  try {
    const updateUrl = `${import.meta.env.VITE_API_BASE_URL}/api/location/update`
    const updatePayload = {
      longitude: longitude.value,
      latitude: latitude.value,
      userStatus: user_status.value ?? 'online',
    }
    await axios.post(updateUrl, updatePayload, { withCredentials: true })
  } catch (e) {
    console.error('boundary check / 위치 갱신 중 오류:', e)
  }

  await fetchLocations()
}

const startGeoLoop = (ms) => {
  if (geoTimer) clearInterval(geoTimer)
  geoTimer = setInterval(pingBoundaryAndLocations, ms)
}

const restartGeoLoop = () => startGeoLoop(serverIntervalMs)

/* ===== 지도 초기화 ===== */
watchEffect(() => {
  const lat = latitude.value
  const lng = longitude.value
  if (!lat || !lng || !mapEl.value || !window.naver?.maps) return
  const pos = new window.naver.maps.LatLng(lat, lng)

  if (!map) {
    map = new window.naver.maps.Map(mapEl.value, { center: pos, zoom: 18 })
    setTimeout(() => {
      loadBoundariesDeferred()
    }, 0)
  } else {
    map.setCenter(pos)
  }
})

/* ===== 정리/리셋 ===== */
const clearLoops = () => {
  if (geoTimer) {
    clearInterval(geoTimer)
    geoTimer = null
  }
  stopGeoWatch()
}

const resetTransient = () => {
  boundary_distance.value = null
  user_status.value = null
  firstPingDone.value = false

  alertDialog.value = { visible: false, message: '' }
  bootLoading.value = true

  activeGroupLocations.value = []
  prevMemberDistances.value = {}
  prevMemberSwim.value = {}

  memberMarkers.forEach((m) => m.setMap?.(null))
  memberMarkers = []
}

/* ===== 그룹 변경 감시 ===== */
watch(
  activeGroupId,
  async (newId) => {
    if (!newId) {
      activeGroupLocations.value = []
      if (geoTimer) {
        clearInterval(geoTimer)
        geoTimer = null
      }
      return
    }

    // 💡 [추가] 그룹 변경 시 알림 설정 초기 로드를 시도합니다. (GET 요청 성공 시 동기화)
    alertSettingsArr.value = await fetchAlertSettings(newId) 

    await fetchLocations()
    await pingBoundaryAndLocations()
    restartGeoLoop()
  },
  { immediate: true },
)

/* ===== Boundary (지연 로드 + BBOX 최적화) ===== */
const loadBoundariesDeferred = async () => {
  try {
    const base = `${import.meta.env.VITE_GEO_BASE_URL}/geoserver/iseau/ows`

    const bounds = map.getBounds()
    const sw = bounds.getSW()
    const ne = bounds.getNE()
    const bbox = `${sw.lng()},${sw.lat()},${ne.lng()},${ne.lat()},EPSG:4326`

    const url = `${base}?service=WFS&version=1.0.0&request=GetFeature&typeName=iseau:tb_test_layer&outputFormat=application/json&srsName=EPSG:4326&BBOX=${encodeURIComponent(bbox)}`
    const res = await fetch(url)
    const data = await res.json()

    const rings = []
    ;(data.features || []).forEach((f) => {
      const g = f.geometry
      if (!g) return
      g.coordinates.forEach((poly) => rings.push(poly[0]))
    })

    drawRings(rings, '#0092BA')
    fitRings(rings)
  } catch (e) {
    console.error('Boundary 로드 실패:', e)
  }
}

const drawRings = (rings, color) => {
  rings.forEach((ring) => {
    const path = ring.map(([lon, lat]) => new naver.maps.LatLng(lat, lon))
    new naver.maps.Polyline({
      map,
      path,
      strokeColor: color,
      strokeWeight: 3,
      strokeOpacity: 0.9,
    })
  })
}

const fitRings = (rings) => {
  const bounds = new naver.maps.LatLngBounds()
  rings.forEach((ring) =>
    ring.forEach(([lon, lat]) => bounds.extend(new naver.maps.LatLng(lat, lon))),
  )
  if (!bounds.isEmpty?.() && Object.prototype.hasOwnProperty.call(bounds, 'extend')) {
    map.fitBounds(bounds)
  }
}

/* ===== 마커 갱신 ===== */
watch(
  groupLocations,
  (list) => {
    if (!list.length || !map || !window.naver?.maps) return
    memberMarkers.forEach((m) => m.setMap(null))
    memberMarkers = []

    list.forEach((member) => {
      if (member.status !== 'online') return
      if (!member.lat || !member.lng) return

      const marker = new window.naver.maps.Marker({
        position: new window.naver.maps.LatLng(member.lat, member.lng),
        map,
        title: member.name,
        icon: {
          content: `
            <div style="
              width: 15px; height: 15px; border-radius: 100%;
              background: ${member.color || '#0092BA'};
              border: 1px solid white; box-shadow: 0 2px 6px rgba(0,0,0,.25);
            "></div>`,
          anchor: new window.naver.maps.Point(10, 10),
        },
      })

      memberMarkers.push(marker)
    })
  },
  { deep: true },
)

/* ===== 알림 ===== */
// 💡 [수정] alertSettingsArr는 그대로 두고, 모달에 전달되는 props를 groupLevels로 통일
// const alertSettingsArr = ref(transformDbToLevels({})) // 이미 위에 정의됨

const prevMemberDistances = ref({})
const prevMemberSwim = ref({})
const alertDialog = ref({ visible: false, message: '' })

const pushAlert = (_type, msg) => {
  alertDialog.value.visible = true
  alertDialog.value.message = msg
}

const closeAlert = () => {
  alertDialog.value.visible = false
}

watch(
  [groupLocations, alertSettingsArr],
  ([list, settings]) => {
    
    // 💡 [추가] 설정 데이터 로드 대기 중일 경우, 실행을 건너뜁니다.
    if (!settings || settings.length === 0) {
        return; 
    }
    
    // 💡 [핵심 추가] 그룹 인원이 1명 이하(즉, 혼자 있거나 아무도 없을 때)이면 알림 로직 전체를 건너뜁니다.
    if (list.length <= 1) {
        return; 
    }
    
    list.forEach((m) => {
      
      if (!m.id) return
      if (m.status !== 'online') return

      const isMe = Number(m.distance) <= 0.3
      // 💡 [핵심] 자기 자신에 대한 거리 알림은 건너뜁니다. (위에 그룹 인원 체크가 있지만, 방어 로직으로 유지)
      if (isMe) return

      const now = Number(m.distance)
      if (!Number.isFinite(now)) return

      const prev = prevMemberDistances.value[m.id]
      
      // Level 1 알림 (3m 이탈)
      const level1Settings = settings.find((l) => l.id === 1);
      const level1Enabled = level1Settings?.enabled;
      const threshold1 = level1Settings?.radius || 3;
      
      // 조건 1: 상태 변화 (prev < threshold1 에서 now >= threshold1)
      const isTransition1 = (prev != null && prev < threshold1 && now >= threshold1);
      
      // 조건 2: 앱 시작/로드 시 이미 이탈 상태인 경우 (prev == null && now >= threshold1)
      const isInitialBreach1 = (prev == null && now >= threshold1);
      
      if (level1Enabled && (isTransition1 || isInitialBreach1)) {
        pushAlert('radius', `⚠ ${m.name}님이 ${threshold1}m 이상 떨어졌어요. (${now.toFixed(1)}m)`);
      }


      // Level 2 알림 (200m 이탈) - Level 1과 동일한 로직 적용
      const level2Settings = settings.find((l) => l.id === 2);
      const level2Enabled = level2Settings?.enabled;
      const threshold2 = level2Settings?.radius || 200;

      const isTransition2 = (prev != null && prev < threshold2 && now >= threshold2);
      const isInitialBreach2 = (prev == null && now >= threshold2);
      
      if (level2Enabled && (isTransition2 || isInitialBreach2)) { 
          pushAlert('radius_2', `🚨 ${m.name}님이 ${threshold2}m 이상 크게 이탈했습니다. (${now.toFixed(1)}m)`);
      }
      
      prevMemberDistances.value[m.id] = now

      const nowSwim = m.userStatus === 'false'
      const prevSwim = prevMemberSwim.value[m.id]
      if (prevSwim !== undefined && prevSwim === false && nowSwim === true) {
        pushAlert('swim', `🌊 ${m.name}님이 수영 중으로 바뀌었어요.`)
      }
      prevMemberSwim.value[m.id] = nowSwim
    })
  },
  { deep: true ,immediate: true },
)

/* ===== 라이프사이클 ===== */
onMounted(async () => {
  await Promise.allSettled([
    (async () => startGeoWatch())(),
    fetchGroups(),
  ])

  header.value = groupName.value || '그룹 화면'

  checkPendingInvitations()
})

onUnmounted(() => {
  stopGeoWatch()
  if (geoTimer) clearInterval(geoTimer)
})

watch(groupLocations, (members) => {
  members.forEach((m) => {
    if (!m?.id) return
    const s = normStatus(m.userStatus)
    if (s == null) return
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

/* 그룹이 없을 때 카드 스타일 */
.empty-group-card {
border-width: 1px !important;
border-radius: 0.5rem;
width: 100%;
max-width: 400px;
}

/* 버튼들을 지도 아래로 */
.group-actions {
position: relative;
padding-top: 1rem;
}

/* 공통 버튼 */
.action-button {
font-size: 0.9rem;
padding: 8px 12px;
height: 42px;
text-align: center;
border-width: 1px;
min-width: 90px;
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

/* 모달 */
.ga-backdrop {
position: fixed;
inset: 0;
background: rgba(0, 0, 0, 0.4);
display: flex;
align-items: center;
justify-content: center;
z-index: 3000;
}
/* 1. 배경 (Backdrop) - 더 어둡게 하여 모달 집중 */
.ga-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7); /* 기존 0.4에서 0.7로 진하게 */
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3000;
}

/* 2. 모달 본체 (Modal) - 경고색 테두리 및 그림자 강조 */
.ga-modal {
  width: 280px;
  background: #ffffff; /* 내부 배경은 흰색 유지 */
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #0b3356; 
}

/* 3. 헤더 (Header) - 긴급 경고 톤 */
.ga-header {
  background-color: #0B1956; 
  color: white;
  padding: 12px 14px 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.ga-header h5 {
  margin: 0;
  font-weight: 1200;
  font-size: 16px;
  color: white; /* 텍스트 흰색 */
}
.ga-close {
  color: white; /* 닫기 버튼 흰색 */
  background: transparent;
  border: none;
  font-size: 18px;
  cursor: pointer;
  line-height: 1; /* x 버튼 중앙 정렬 */
}


/* 4. 내용 (Body) - 메시지 텍스트 강조 */
.ga-body {
  padding: 20px 18px; /* 패딩을 늘려 메시지 공간 확보 */
  font-size: 16px;
  font-weight: 1600;
  color: #333333;
  line-height: 1.4;
  text-align: center; /* 메시지 중앙 정렬 */
}

/* 5. 푸터 (Footer) */
.ga-footer {
  padding: 10px 14px 14px;
  display: flex;
  justify-content: center; /* 버튼을 중앙에 배치 */
}

/* 6. 버튼 (Button) - 확인 버튼을 눈에 띄게 */
.ga-btn {
  background: #0B1956; /* 헤더와 동일한 경고색 */
  border: none;
  color: #fff;
  padding: 8px 24px;
  border-radius: 20px; /* 둥근 버튼 */
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
  box-shadow: 0 4px 6px rgba(255, 69, 0, 0.3); /* 버튼 그림자 */
}
.ga-btn:hover {
    background: #0092BA; /* 호버 시 색상 진하게 */
}
.ga-close {
 background: transparent;
 border: none;
 font-size: 18px;
 cursor: pointer;
}
</style>