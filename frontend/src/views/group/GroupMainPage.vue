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
                style="font-size: 18px; cursor: pointer;"
                @click="handleManualReport(member)"
              >
                🚨
              </span>
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
    
    <GroupReportModal
        v-if="showReportModal && reportTarget"
        v-model:isVisible="showReportModal"
        :report-data="reportTarget"
        @update:isVisible="handleReportModalClose"
    />


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
// 🚨 [추가] GroupReportModal 컴포넌트 임포트
import GroupReportModal from '@/components/GroupReportModal.vue' 
import { useStore } from '@/stores/store.js'
import { useGroupStore } from '@/stores/groupStore'
import { storeToRefs } from 'pinia' // 🚨 필수 추가

const store = useStore()
const { header } = storeToRefs(store)

const groupStore = useGroupStore()
const { receivedInvitation } = storeToRefs(groupStore)
// 🚨 [추가] Store의 그룹 리스트를 반응형으로 가져옵니다.
const { myGroupList: globalGroupList } = storeToRefs(groupStore)

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
const showReportModal = ref(false) // 🚨 [추가] GroupReportModal 표시 상태
const reportTarget = ref(null)      // 🚨 [추가] 신고 대상 멤버 데이터
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
    // member 객체에 age, gender 등 서버에서 내려오는 모든 필드가 포함됩니다.
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

/* ===== Store 변경 감지 -> 화면 갱신 ===== */
watch(globalGroupList, async (newList) => {
    console.log('🔄 [GroupMainPage] Store 그룹 목록 변경 감지 -> 화면 갱신');
    
    myGroupList.value = newList;

    if (myGroupList.value.length > 0) {
        const newId = myGroupList.value[0].id;
        alertSettingsArr.value = await fetchAlertSettings(newId);
        await fetchLocations();
        await pingBoundaryAndLocations();
        restartGeoLoop();
    } else {
        activeGroupLocations.value = [];
    }
}, { deep: true });


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
    { id: 1, label: '100m 이탈 알림', radius: 10, enabled: true, levelField: 'groupLeaveLevel1' },
    { id: 2, label: '200m 이탈 알림', radius: 200, enabled: false, levelField: 'groupLeaveLevel2' },
    { id: 3, label: '해안선 알림', radius: 0, enabled: true, levelField: 'tide' },
  ]
}

// Level 1, 2에 min/max/unit 정보를 추가하여 모달에 전달
const transformDbToLevels = (dbSettings) => {
  const maxRadius = Math.max(dbSettings.groupLeaveLevel1Distance || 10, dbSettings.groupLeaveLevel2Distance || 10)
  const isDistanceEnabled = dbSettings.groupLeaveLevel1Alert === 'Y' || dbSettings.groupLeaveLevel2Alert === 'Y';
  
  return [
    {
      id: 1,
      label: '안전 거리 이탈 알림',
      radius: maxRadius, 
      enabled: isDistanceEnabled, 
      levelField: 'groupLeaveLevel1',
      min: 10,
      max: 1000,
      unit: 'm',
    },
    {
      id: 2,
      label: '안전 거리 이탈 알림 (Placeholder)', 
      radius: maxRadius,
      enabled: isDistanceEnabled,
      levelField: 'groupLeaveLevel2',
      min: 10,
      max: 1000,
      unit: 'm',
      hidden: true 
    },
    {
      id: 3,
      label: '해안선 이탈 알림',
      radius: 0,
      enabled: dbSettings.tideAlert === 'Y',
      levelField: 'tide',
      min: 0,
      max: 0,
      unit: '',
    },
  ]
}

const alertSettingsArr = ref(transformDbToLevels({}))

const handleNotificationSettings = async () => {
  if (!activeGroupId.value) {
    alert('그룹을 선택/생성한 후 알림 설정을 할 수 있습니다.')
    return
  }
  alertSettingsArr.value = await fetchAlertSettings(activeGroupId.value)
  showAlertModal.value = true
}

const handleSettingsUpdated = async () => {
}

const handleSettingsSynced = (syncedLevels) => {
  prevMemberDistances.value = {};
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
    const path = ring.map(([lon, lat]) => new window.naver.maps.LatLng(lat, lon))
    new window.naver.maps.Polyline({
      map,
      path,
      strokeColor: color,
      strokeWeight: 3,
      strokeOpacity: 0.9,
    })
  })
}

const fitRings = (rings) => {
  const bounds = new window.naver.maps.LatLngBounds()
  rings.forEach((ring) =>
    ring.forEach(([lon, lat]) => bounds.extend(new window.naver.maps.LatLng(lat, lon))),
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

/* ===== 함수: 나이 계산 (YYYY-MM-DD 형식의 birthDate 필요) ===== */
const calculateAge = (birthDate) => {
    if (!birthDate) return '미상';
    
    // YYYY-MM-DD 형식이라고 가정
    const birth = new Date(birthDate);
    const today = new Date();
    
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    
    // 생일이 지나지 않았으면 -1
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
        age--;
    }
    
    return age; // 만 나이 반환
};


/* ===== 함수: 수동 신고 모달 열기/닫기 (🚨 클릭) ===== */
const handleManualReport = (member) => {
    // 🚨 member 객체에서 age, gender, id(userNumber) 등의 필드를 가져와야 합니다.
    
    const memberId = member.id; // userNumber로 사용
    
  // 🚨 [핵심] DB에서 가져온 원본 lat/lng 값
    const memberLat = Number(member.lat); 
    const memberLng = Number(member.lng);
    
    // 🚨 [BPM Fix] 수동 신고 시 BPM 정보는 없으므로 NULL로 설정
    const memberBpm = null;
    
    // 🚨 [수정] 나이 계산 로직 적용 (member.birthDate 필드가 서버에서 내려와야 함)
    const memberAge = calculateAge(member.birthDate); 
    const memberGender = member.gender || 'N/A';
    
    const manualLog = `관리자 수동 호출 시작 (${member.name} 위치 기준)`;

  // 필수 유효성 검사 (isFinite로 숫자이면서 NaN/Infinity가 아님을 확인)
    if (!memberId || !Number.isFinite(memberLat) || !Number.isFinite(memberLng)) {
        // 🚨 [디버깅] 유효성 검사 실패 시, 어떤 값이 문제인지 명시적으로 출력
        console.error("❌ 위치 값 오류:", { lat: member.lat, lng: member.lng, isNumLat: Number.isFinite(memberLat) });
        alert('필수 위치/사용자 정보가 유효하지 않습니다. (ID, 위도, 경도 확인 필요)');
        return;
    }

    // reportTarget 객체 구성
    reportTarget.value = {
        memberName: member.name,
        age: memberAge, // 🚨 계산된 나이 적용
        gender: memberGender,
        bpm: memberBpm, // 🚨 NULL 전달
        userNumber: memberId, // GroupReportModal에 전달할 userNumber
        latitude: memberLat, 
        longitude: memberLng, // 🚨 Number 타입으로 전달
        timestamp: Date.now(),
        log: manualLog,
    };
    
    // 🚨 [추가] 최종 전달 Props 확인 로그
   console.log('✅ [Report Props] Lat/Lng:', reportTarget.value.latitude, reportTarget.value.longitude);
    // ...

    showReportModal.value = true;
    
    // 선택된 멤버의 위치로 지도를 중앙 이동 (시각적 강조)
    if (map) {
        const reportPos = new window.naver.maps.LatLng(memberLat, memberLng);
        map.setCenter(reportPos);
        map.setZoom(18); 
    }
};

/* ===== 알림 ===== */
const prevMemberDistances = ref({})
const prevMemberSwim = ref({})
const alertDialog = ref({ visible: false, message: '' })

const pushAlert = async (_type, msg) => {
  // 1. 화면에 모달 띄우기 (사용자에게 보여줌)
  alertDialog.value.visible = true
  alertDialog.value.message = msg

  // 2. 서버로 FCM 알림 요청 전송
  try {
    const url = `${import.meta.env.VITE_API_BASE_URL}/api/groups/send-alert`;

    await axios.post(url, {
      type: _type,    // 'radius', 'radius_2', 'swim' 등
      message: msg
    }, { withCredentials: true });

    console.log('🚀 FCM 알림 요청 전송 완료:', msg);

  } catch (e) {
    console.error('❌ FCM 알림 요청 실패:', e);
  }
}

const closeAlert = () => {
  alertDialog.value.visible = false
}

watch(
  [groupLocations, alertSettingsArr],
  ([list, settings]) => {
    if (!settings || settings.length === 0) {
        return; 
    }
    
    if (list.length <= 1) {
        return; 
    }
    
    list.forEach((m) => {
      if (!m.id) return
      if (m.status !== 'online') return

      const isMe = Number(m.distance) <= 0.3
      if (isMe) return

      const now = Number(m.distance)
      if (!Number.isFinite(now)) return

      const prev = prevMemberDistances.value[m.id]
      
      const distanceSettings = settings.find((l) => l.levelField === 'groupLeaveLevel1');
      const distanceEnabled = distanceSettings?.enabled;
      const threshold = distanceSettings?.radius || 10; 
      
      const isTransition = (prev != null && prev < threshold && now >= threshold);
      const isInitialBreach = (prev == null && now >= threshold);
      
      if (distanceEnabled && (isTransition || isInitialBreach)) {
        pushAlert('radius', `🚨 ${m.name}님이 설정 거리 ${threshold}m을 이탈했습니다. (${now.toFixed(1)}m)`);
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

.empty-group-card {
  border-width: 1px !important;
  border-radius: 0.5rem;
  width: 100%;
  max-width: 400px;
}

.group-actions {
  position: relative;
  padding-top: 1rem;
}

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
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3000;
}

.ga-modal {
  width: 280px;
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid v-bind(darkColor);
}

.ga-header {
  background-color: v-bind(darkColor);
  color: white;
  padding: 12px 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ga-header h5 {
  margin: 0;
  font-weight: 800;
  font-size: 16px;
  color: white;
}

.ga-close {
  color: white;
  background: transparent;
  border: none;
  font-size: 18px;
  cursor: pointer;
  line-height: 1;
}

.ga-body {
  padding: 20px 18px;
  font-size: 16px;
  font-weight: 600;
  color: #333333;
  line-height: 1.4;
  text-align: center;
}

.ga-footer {
  padding: 10px 14px 14px;
  display: flex;
  justify-content: center;
}

.ga-btn {
  background: v-bind(darkColor);
  border: none;
  color: #fff;
  padding: 8px 24px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
}

.ga-btn:hover {
  background: v-bind(mainColor);
}
</style>
