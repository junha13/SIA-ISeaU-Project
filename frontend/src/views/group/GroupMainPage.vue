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
              <p class="text-secondary small mb-0">{{ member.distance <= 0.2 ? '본인' : `나와의 거리 : ${Number(member.distance).toFixed(1)} m` }}</p>
              <p class="text-secondary small mb-0">
                {{
                  member.userStatus === "false"
                    ? "수영 중"
                    : ( member.distance <= 0.2 && boundary_distance.value
                        ? `육지 (해안선까지의 거리 ${Number(boundary_distance.value).toFixed(1)} m)`
                        : "육지")
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
      v-model:isVisible="showInviteModal" 
      :group-id="activeGroupId" 
    />
    <GroupCreateModal 
      v-model:isVisible="showCreateGroupModal" 
      @group-created="handleGroupCreated" 
    />
    <GroupAlertSettingsModal
      v-model:isVisible="showAlertModal"
      :levels="alertSettings"
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
// ---------------------------------
// 🐬 작동 로직 (리팩토링)
// ---------------------------------
import { ref, onMounted, onUnmounted, computed, watch, watchEffect } from 'vue'; 
import axios from 'axios'; 
import GroupInviteModal from '@/components/GroupInviteModal.vue';
import GroupCreateModal from '@/components/GroupCreateModal.vue'; 
import GroupAlertSettingsModal from '@/components/GroupAlertSettingsModal.vue'

import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'
const store = useStore();
const { header, beach } = storeToRefs(store)


// =================================================================
// ## 1. 기본 설정 (Setup)
// 전역 변수, 스토어, 상수, 지도 객체 등을 정의합니다.
// =================================================================


const mainColor = '#0092BA';
const darkColor = '#0B1956';

// 🗺️ 지도 관련 변수
const mapEl = ref(null);
let map; // Naver Map 객체 (반응형 X)
let memberMarkers = []; // 지도 위에 표시된 마커 목록


// =================================================================
// ## 2. 핵심 상태 (State)
// 이 컴포넌트의 주요 반응형 상태(기억 상자)입니다.
// =================================================================
const myGroupList = ref([]); 
const activeGroupLocations = ref([]);
const showAlertModal = ref(false)
const showInviteModal = ref(false); 
const showCreateGroupModal = ref(false); 
const latitude = ref(''); // 내 위치(위도)
const longitude = ref(''); // 내 위치(경도)

// =================================================================
// ## 3. 계산된 상태 (Computed)
// 상태(State)를 기반으로 자동 계산되는 값들입니다.
// =================================================================

// 💡 그룹이 있는지?
const hasGroup = computed(() => myGroupList.value.length > 0);

// 💡 현재 활성화된 그룹 ID
const activeGroupId = computed(() => {
  return hasGroup.value ? myGroupList.value[0].id : null;
});

// 💡 [추가] 현재 활성화된 그룹의 이름
const groupName = computed(() => {
  return hasGroup.value ? myGroupList.value[0].name : '';
});

// 💡 중복 제거된 그룹 멤버 위치 목록 (Template에서 사용)
const groupLocations = computed(() => {
  const map = {}

  activeGroupLocations.value.forEach(m => {
    if (!m.id) return
    const d = m.distance != null ? Number(m.distance) : null
    const lat = m.lat ?? m.latitude ?? null
    const lng = m.lng ?? m.longitude ?? null
    map[m.id] = {
      ...(map[m.id] || {}),
      ...m,
      distance: d,
      lat,
      lng,
    }
  })

  return Object.values(map).sort((a, b) => {
    const aMe = a.distance != null && a.distance <= 0.3
    const bMe = b.distance != null && b.distance <= 0.3
    if (aMe && !bMe) return -1
    if (!aMe && bMe) return 1
    return (a.distance ?? 999999) - (b.distance ?? 999999)
  })
})

// =================================================================
// ## 4. 🤝 그룹 관리 로직 (Group Management)
// 그룹 생성, 조회, 삭제, 초대 등 그룹 자체에 대한 기능입니다.
// =================================================================

// [API] 내 그룹 목록 조회
const fetchGroups = async () => {
    try {
        const url = `${import.meta.env.VITE_API_BASE_URL}/api/groups?timestamp=${new Date().getTime()}`; 
        // 💡 [원본 유지] withCredentials: true (세션 방식)
        const response = await axios.get(url, { withCredentials: true });
        myGroupList.value = response.data.data.result; 
        header.value = groupName.value || "그룹 화면"
        console.log("[FetchGroups] 그룹 목록:", myGroupList.value);
    } catch (error) {
        console.error('그룹 목록 조회 실패:', error, error.response);
        myGroupList.value = [];
    }
};

// [Event] 그룹 생성 성공 시
const handleGroupCreated = (newGroupId) => {
    showCreateGroupModal.value = false;
    console.log(`[GroupCreate] 새 그룹 생성됨: ${newGroupId}. 그룹 목록 갱신...`);
    fetchGroups(); 
};

// [Event] 그룹 삭제 버튼 클릭 시 (즉시 삭제)
const confirmDeleteGroup = () => {
  if (!activeGroupId.value) return;
  console.log(`[ConfirmDelete] 그룹 ID ${activeGroupId.value} 즉시 삭제 실행.`);
  deleteGroup();
};

// [API] 그룹 삭제
const deleteGroup = async () => {
    if (!activeGroupId.value) return;
    
    console.log(`[DeleteGroup] 그룹 ID ${activeGroupId.value} 삭제 API 호출...`);
    try {
        const url = `${import.meta.env.VITE_API_BASE_URL}/api/groups/${activeGroupId.value}`; 
        // 💡 [원본 유지] withCredentials: true (세션 방식)
        await axios.delete(url, { withCredentials: true });
        
        console.log("[DeleteGroup] 삭제 성공. 그룹 목록 갱신...");
        activeGroupLocations.value = []; 
        fetchGroups(); 
    } catch (error) {
        console.error('그룹 삭제 실패:', error);
        alert('그룹 삭제에 실패했습니다.');
    }
};

// [Event] 알림 설정 (아직 미구현)
const handleNotificationSettings = () => {
    showAlertModal.value = true
};

// =================================================================
// ## 5. 📍 위치 추적 로직 (Location Tracking)
// 그룹 멤버의 위치를 '주기적'으로 가져오는 기능입니다.
// =================================================================

// [API] 그룹 멤버 위치 조회
const fetchLocations = async () => {
    if (!activeGroupId.value) {
        console.warn("[FetchLocations] Aborted: activeGroupId is null.");
        return;
    }
    const payload = {
        myLatitude: latitude.value,
        myLongitude: longitude.value,
        groupNumber: activeGroupId.value
    }
    console.log(`[FetchLocations] 그룹 ID ${activeGroupId.value}의 위치 조회...`);
    try {
        const url = `${import.meta.env.VITE_API_BASE_URL}/api/groups/locations`;
        // 💡 [원본 유지] withCredentials: true (세션 방식)
        const response = await axios.post(url, payload, { withCredentials: true });
        console.log(response)
        activeGroupLocations.value = response.data.data.result; 
    } catch (error) {
        console.error('그룹 위치 정보 조회 실패:', error);
        activeGroupLocations.value = [];
    }
};

// HELPER: fetchLocations를 호출하는 작은 일
const loadGroupData = () => {
  if (activeGroupId.value) {
    fetchLocations(); 
  }
}

// [WATCH] activeGroupId가 변경되면 '주기적' 위치 조회를 시작/중지합니다.
// (1. fetchGroups 성공 -> 2. activeGroupId 변경 -> 3. 이 watch 실행 -> 4. loadGroupData 호출)
watch(activeGroupId, (newId, oldId) => {
    // 2. 새 그룹 ID가 생기면
    if (newId) {
        console.log(`[Watcher] activeGroupId 변경: ${oldId} -> ${newId}. 위치 폴링 시작...`);
        loadGroupData(); // 즉시 1회 실행
    } else {
        // 3. 그룹이 없어지면
        activeGroupLocations.value = []; 
    }
}, { immediate: true }); 

// =================================================================
// ## 6. 🗺️ 지도 & 마커 로직 (Map & Markers)
// Naver Map을 초기화하고, 그룹 멤버 마커를 그리는 기능입니다.
// =================================================================

// [WATCH-EFFECT] '내 위치'(latitude)가 준비되면 Naver Map 객체를 '초기화'합니다.
// (1. getLocation 성공 -> 2. latitude 변경 -> 3. 이 watchEffect 실행 -> 4. map 객체 생성)
watchEffect(() => {
  const lat = latitude.value
  const lng = longitude.value

  // 1. 재료 확인 (위치, 지도 DOM, Naver API)
  if (!lat || !lng || !mapEl.value || !window.naver?.maps) return

  const pos = new window.naver.maps.LatLng(lat, lng)

  // 2. 지도 '최초' 생성
  if (!map) {
    map = new window.naver.maps.Map(mapEl.value, {
      center: pos,
      zoom: 18
    })
    // GeoServer 레이어 로드 (최초 1회)
    window.naver.maps.Event.once(map, 'init', testLoadBoundary)
    loadBoundary()
  } else {
  // 3. 이미 생성된 경우, 중심 좌표만 이동
    map.setCenter(pos)
  }
})


// [WATCH] '그룹 위치'(groupLocations)가 변경되면 '마커'를 새로 그립니다.
// (1. fetchLocations 성공 -> 2. activeGroupLocations 변경 -> 3. groupLocations 변경 -> 4. 이 watch 실행)
watch(groupLocations, (newLocations) => {

    // 위치가 아예 없으면 끝
  if (!newLocations.length) return;
    // 1. 재료 확인 (지도 API)
    if (!map || !window.naver?.maps) return; 

    console.log("[MapMarker] 그룹 위치 변경 감지. 마커 업데이트...", newLocations);

    // 2. 기존 마커 모두 삭제
    memberMarkers.forEach(marker => marker.setMap(null));
    memberMarkers = [];

    // 3. 새 위치 데이터로 마커 생성
  newLocations.forEach(member => {
    if (!member.lat || !member.lng) return

    const marker = new naver.maps.Marker({
      position: new naver.maps.LatLng(member.lat, member.lng),
      map,
      title: member.name,
      icon: {
        content: `
          <div style="
            width:15px;
            height:15px;
            border-radius:100%;
            background:${member.color || '#0092BA'};
            border:1px solid white;
            box-shadow:0 2px 6px rgba(0,0,0,.25);
          "></div>
        `,
        anchor: new naver.maps.Point(10, 10),
      },
    })

    memberMarkers.push(marker)
  })
}, { deep: true }); 

// =================================================================
// ## 7.Geo-Services (내 위치 & GeoServer)
// =================================================================
watchEffect(() => {
  // Pinia에서 가져온 beach 정보에서 위경도 꺼냄
  const lat = latitude.value
  const lng = longitude.value

  // 아직 준비 안 된 경우 바로 종료
  if (!lat || !lng || !mapEl.value || !window.naver?.maps) return

  // 네이버 지도에서 쓰는 좌표 객체 생성
  const pos = new window.naver.maps.LatLng(lat, lng)

  // map이 한 번도 만들어진 적 없으면 (초기 렌더 시점)
  if (!map) {
    map = new window.naver.maps.Map(mapEl.value, {
      center: pos,
      zoom: 15
    })

  //window.naver.maps.Event.once(map, 'init', loadBoundary)
  window.naver.maps.Event.once(map, 'init', testLoadBoundary)
  loadBoundary()
    // 이미 map이 만들어져 있으면 새로 안 만들고 중심 좌표와 마커 위치만 업데이트
  } else {
    map.setCenter(pos)
    //marker.setPosition(pos)
  }
})

/**
 * ============================================
 *          지오로케이션 내 위치 보기
 * ============================================
 */ 
function getLocation() {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      resolve();
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        latitude.value = pos.coords.latitude;
        longitude.value = pos.coords.longitude;
        resolve();
      },
      (err) => {
        console.error('위치 실패: ' + err.message);
        resolve(); // 실패해도 흐름은 계속
      },
      { enableHighAccuracy: true }
    );
  });
}

/**
 * ============================================
 *  내 위치 해안선 or 테스트 폴리곤 비교하고 거리 받기 
 * ============================================
 */ 

let boundary_distance= ref()
let user_status = ref()
function requestGeoLocation(value) {
  if (!navigator.geolocation) return;

  navigator.geolocation.getCurrentPosition(
    async (pos) => {
      // 1) 값 넣고
      latitude.value = pos.coords.latitude
      longitude.value = pos.coords.longitude

      // 2) 서버로 보냄
      const payload = {
        latitude: pos.coords.latitude,
        longitude: pos.coords.longitude,
        groupNumber: activeGroupId.value,
        userStatus: user_status.value
      }
      console.log('sending to server:', payload)

      let axiosUrl;
      if ( value === "test") {
        axiosUrl = `${import.meta.env.VITE_API_BASE_URL}/api/location/testBoundaryCheck`;
      }
      if ( value === "boundary") {
        axiosUrl = `${import.meta.env.VITE_API_BASE_URL}/api/location/boundaryCheck`;
      }
      try {
        const res = await axios.post(
          axiosUrl,
          payload,
          {
            headers: { 'Content-Type': 'application/json' },
            withCredentials: true,
            timeout: 5000,
          }
        )
        console.log('OK', res.data.data.result)
        interval = res.data.data.result.interval
        boundary_distance.value = res.data.data.result.distance
        user_status.value = res.data.data.result.inside
        startGeoLoop(interval)
      } catch (e) {
        console.error('send error', e)
      }
    },
    (err) => {
      console.error('위치 실패:', err.message)
    },
    { enableHighAccuracy: true }
  )
}

/**
 * ================================================
 *                  폴리곤 만들기
 * ================================================
 */
const url = `${import.meta.env.VITE_GEO_BASE_URL}/geoserver/iseau/ows` +
  `?service=WFS` +
  `&version=1.0.0` +
  `&request=GetFeature` +
  `&typeName=iseau:tb_boundary` +
  `&outputFormat=application/json` +
  `&srsName=EPSG:4326`

// 해안선 가져오기
let boundaryRings = [];

async function loadBoundary() {
  const res = await fetch(url);
  const data = await res.json();

  boundaryRings = []; // 초기화

  // 이 데이터는 항상 MultiPolygon이라고 가정
  (data.features || []).forEach(f => {
    const geom = f.geometry;
    if (!geom) return;

    // 👇 멀티폴리곤 한 개 = 여러 폴리곤
    // geom.coordinates = [ polygon1, polygon2, ... ]
    geom.coordinates.forEach(poly => {
      // poly[0] = 외곽링
      const outerRing = poly[0]; // [[lon,lat], [lon,lat], ...]
      boundaryRings.push(outerRing);
    });
  });

  console.log('[boundaryRings]', boundaryRings);
}

// =========== 테스트 데이터 (공카데미) ==========
const test_url = `${import.meta.env.VITE_GEO_BASE_URL}/geoserver/iseau/ows` +
  `?service=WFS` +
  `&version=1.0.0` +
  `&request=GetFeature` +
  `&typeName=iseau:tb_test_layer` +
  `&outputFormat=application/json` +
  `&srsName=EPSG:4326`

let testBoundaryRings = []

async function testLoadBoundary() {
  const testRes = await fetch(test_url);
  const testData = await testRes.json();

  testBoundaryRings = []; // 초기화

  // 이 데이터는 항상 MultiPolygon이라고 가정
  (testData.features || []).forEach(f => {
    const geom = f.geometry;
    if (!geom) return;

    // 👇 멀티폴리곤 한 개 = 여러 폴리곤
    // geom.coordinates = [ polygon1, polygon2, ... ]
    geom.coordinates.forEach(poly => {
      // poly[0] = 외곽링
      const outerRing = poly[0]; // [[lon,lat], [lon,lat], ...]
      testBoundaryRings.push(outerRing);
    });
  });

  console.log('[boundaryRings]', testBoundaryRings);

  testDrawBoundaryRings() 
}

function testDrawBoundaryRings() {
  if (!map) return;

  testBoundaryRings.forEach(ring => {
    // lon,lat → naver LatLng
    const path = ring.map(([lon, lat]) => new naver.maps.LatLng(lat, lon));

    new naver.maps.Polyline({
      map,
      path,
      strokeColor: '#0092BA',
      strokeWeight: 3,
      strokeOpacity: 0.9,
    });
  });

  // 보기 좋게 화면도 경계로 맞춰주자
  const bounds = new naver.maps.LatLngBounds();
  testBoundaryRings.forEach(ring => {
    ring.forEach(([lon, lat]) => bounds.extend(new naver.maps.LatLng(lat, lon)));
  });
  if (!bounds.isEmpty?.() && bounds.hasOwnProperty('extend')) {
    map.fitBounds(bounds);
  }
}

// =================================================================
// ## 8. 🔄 생명주기 훅 (Lifecycle Hooks)
// 컴포넌트가 생성/소멸될 때 실행되는 진입점입니다.
// =================================================================

onMounted(async() => {
  await getLocation();        // ✅ 위치 먼저
  await fetchGroups();        // ✅ 그다음 그룹
  await fetchLocations();     // ✅ 이제 거리 있어 → 정렬 바로 됨
  requestGeoLocation("test"); // 그다음에 서버에서 interval 받아서 주기 시작
  header.value = groupName.value || "그룹 화면"
});

onUnmounted(() => {
  if (geoTimer) clearInterval(geoTimer)
});

// =================================================================
// ## 9. requestGeoLocation("test")로 인터벌 가져오면 인터벌로 돌리기
// 
// =================================================================
let interval = 10000;
let geoTimer = null;

function startGeoLoop(intervalMs) {
  // 기존 타이머 있으면 정리
  if (geoTimer) {
    clearInterval(geoTimer)
    geoTimer = null
  }
  // 새 타이머 시작
  geoTimer = setInterval(() => {
    requestGeoLocation("test")
    fetchLocations()
  }, intervalMs)
}

// =================================================================
// ## 10. 그룹 알림 쏴주기
// 
// =================================================================

const alertSettings = ref([
  { id: 1, label: '3m 이탈 알림', radius: 3, enabled: true },
  { id: 3, label: '해안선 알림', radius: 0, enabled: true },
])

const prevMemberDistances = ref({})
const prevMemberSwim = ref({})   // 수영 여부 이전 값 저장

watch(groupLocations, (members) => {
    members.forEach((m) => {
        if (!m.id) return

        const now = Number(m.distance)
        if (Number.isNaN(now)) return

        const prev = prevMemberDistances.value[m.id]

        // 처음 3m 이상 들어오거나, 3m 밑에서 3m 이상으로 넘어갈 때만
        if ((prev == null && now >= 3) || (prev != null && prev < 3 && now >= 3)) {
          const isOn = alertSettings.value.find(l => l.id === 1)?.enabled
          if (isOn) {
            pushAlert('radius', `⚠ ${m.name}님이 3m 이상 떨어졌어요. (${now.toFixed(1)}m)`)
          }
        }

        prevMemberDistances.value[m.id] = now

    // 👉 수영 알림
    const nowSwim = m.userStatus === 'false'   // 서버가 "false" 주면 수영중
    const prevSwim = prevMemberSwim.value[m.id]

    // 🔴 처음 들어온 데이터면 알림 말고 기록만
    if (prevSwim === undefined) {
      prevMemberSwim.value[m.id] = nowSwim
      return
    }

    // 🟢 진짜로 (육지 → 수영중) 으로 바뀐 순간만 알림
    if (prevSwim === false && nowSwim === true) {
      pushAlert('swim', `🌊 ${m.name}님이 수영 중으로 바뀌었어요.`)
    }

    // 마지막에 현재값 저장
    prevMemberSwim.value[m.id] = nowSwim
  })
}, { deep: true })

const alertDialog = ref({ visible: false, message: '' })

const pushAlert = (_type, msg) => {
  alertDialog.value.visible = true
  alertDialog.value.message = msg
}

const closeAlert = () => {
  alertDialog.value.visible = false
}


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