<template>
  <div class="group-main-page" style="position: relative;">

    <div ref="mapEl" style="width:100%;height:300px;"></div>

    <div class="map-overlay-buttons position-absolute top-0 end-0 p-3">
      <button class="btn btn-sm btn-white rounded-pill shadow-sm mb-2" style="background-color: white;" @click="getLocation">
        내 위치 새로고침 <i class="fas fa-sync-alt ms-1"></i>
      </button>
      <button class="btn btn-sm btn-primary rounded-circle shadow-sm" style="width: 40px; height: 40px; background-color: white; border: 1px solid #ccc;">
        <i class="fas fa-location-arrow" :style="{ color: darkColor }"></i>
      </button>
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

        <h6 class="fw-bold mb-3" :style="{ color: darkColor }">그룹 멤버 ({{ groupLocations.length }}명)</h6>
        <div class="member-list">
          <div v-for="member in groupLocations" :key="member.id" class="d-flex align-items-center py-2 border-bottom">
            <div class="me-3 rounded-pill" :style="{ backgroundColor: member.color, width: '4px', height: '50px' }"></div>
            <div class="flex-grow-1">
              <h6 class="fw-bolder mb-0 fs-6">{{ member.name }} <span class="small text-muted fw-normal ms-1">{{ member.username }}</span></h6>
              <p class="text-secondary small mb-0">{{ member.phone }}</p>
            </div>
            <div class="d-flex align-items-center">
              <span v-if="member.status === 'online'" class="text-success small fw-bold"> online </span>
              <span v-else-if="member.status === 'pending'" class="text-muted small fw-bold"> (초대 중) </span>
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

// ⏱️ 위치 추적 타이머
let locationPollTimer = null; 

// =================================================================
// ## 2. 핵심 상태 (State)
// 이 컴포넌트의 주요 반응형 상태(기억 상자)입니다.
// =================================================================
const myGroupList = ref([]); 
const activeGroupLocations = ref([]);
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

// 💡 중복 제거된 그룹 멤버 위치 목록 (Template에서 사용)
const groupLocations = computed(() => {
    const locations = activeGroupLocations.value;
    const uniqueMembers = {};
    
    locations.forEach(member => {
        uniqueMembers[member.id] = member;
    });

    return Object.values(uniqueMembers);
});

// =================================================================
// ## 4. 🤝 그룹 관리 로직 (Group Management)
// 그룹 생성, 조회, 삭제, 초대 등 그룹 자체에 대한 기능입니다.
// =================================================================

// [API] 내 그룹 목록 조회
const fetchGroups = async () => {
    try {
        const url = `${import.meta.env.VITE_API_BASE_URL}/api/groups?timestamp=${new Date().getTime()}`; 
        const response = await axios.get(url, { withCredentials: true });
        myGroupList.value = response.data.data.result; 
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
    console.log("알림 설정 버튼 클릭됨");
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
    
    console.log(`[FetchLocations] 그룹 ID ${activeGroupId.value}의 위치 조회...`);
    try {
        const url = `${import.meta.env.VITE_API_BASE_URL}/api/groups/locations?groupId=${activeGroupId.value}`;
        const response = await axios.get(url, { withCredentials: true });
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
    // 1. 기존 타이머가 있다면 제거
    if (locationPollTimer) {
        clearInterval(locationPollTimer);
        locationPollTimer = null;
    }

    // 2. 새 그룹 ID가 생기면
    if (newId) {
        console.log(`[Watcher] activeGroupId 변경: ${oldId} -> ${newId}. 위치 폴링 시작...`);
        loadGroupData(); // 즉시 1회 실행
        locationPollTimer = setInterval(loadGroupData, 10000); // 10초마다 반복
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
      zoom: 15
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
    // 1. 재료 확인 (지도 API)
    if (!map || !window.naver?.maps) return; 

    console.log("[MapMarker] 그룹 위치 변경 감지. 마커 업데이트...", newLocations);

    // 2. 기존 마커 모두 삭제
    memberMarkers.forEach(marker => marker.setMap(null));
    memberMarkers = [];

    // 3. 새 위치 데이터로 마커 생성
    newLocations.forEach(member => {
        if (member.latitude && member.longitude) {
            const marker = new window.naver.maps.Marker({
                position: new window.naver.maps.LatLng(member.latitude, member.longitude),
                map: map,
                title: member.name, 
            });
            memberMarkers.push(marker);
        }
    });
}, { deep: true }); 

// =================================================================
// ## 7.Geo-Services (내 위치 & GeoServer)
// =================================================================

// [Browser API] 내 GPS 위치 1회 가져오기 (지도 초기화용)
function getLocation() {
  if (!navigator.geolocation) return;
  navigator.geolocation.getCurrentPosition(
    (pos) => { 
        latitude.value = pos.coords.latitude; 
        longitude.value = pos.coords.longitude; 
    },
    (err) => { console.error('위치 실패:', err.message); },
    { enableHighAccuracy: true }
  )
}

// [Browser API + Server] 내 위치를 서버로 전송 (경계 확인용)
function requestGeoLocation(value) {
  if (!navigator.geolocation) return;

  navigator.geolocation.getCurrentPosition(
    async (pos) => {
      // 1. '내 위치' 상태 업데이트
      latitude.value = pos.coords.latitude
      longitude.value = pos.coords.longitude

      // 2. 서버로 전송할 데이터
      const payload = {
        latitude: pos.coords.latitude,
        longitude: pos.coords.longitude,
      }
      console.log('sending to server:', payload)

      let axiosUrl;
      if ( value === "test") {
        axiosUrl = `${import.meta.env.VITE_API_BASE_URL}/api/location/testBoundaryCheck`;
      }
      if ( value === "boundary") {
        axiosUrl = `${import.meta.env.VITE_API_BASE_URL}/api/location/boundaryCheck`;
      }

      if (!axiosUrl) return;

      // 3. 서버 API 호출
      try {
        const res = await axios.post( axiosUrl, payload,
          {
            headers: { 'Content-Type': 'application/json' },
            withCredentials: true,
            timeout: 5000,
          }
        )
        console.log('OK', res.data)
      } catch (e) {
        console.error('send error', e)
      }
    },
    (err) => { console.error('위치 실패:', err.message) },
    { enableHighAccuracy: true }
  )
}

// --- GeoServer 관련 로직 (수정 없이 원본 유지) ---
const url = `http://127.0.0.1:8090/geoserver/iseau/ows` +
  `?service=WFS` +
  `&version=1.0.0` +
  `&request=GetFeature` +
  `&typeName=iseau:tb_boundary` +
  `&outputFormat=application/json` +
  `&srsName=EPSG:4326`
let boundaryRings = [];

async function loadBoundary() {
  try {
    const res = await fetch(url);
    const data = await res.json();
    boundaryRings = []; 
    (data.features || []).forEach(f => {
      const geom = f.geometry;
      if (!geom) return;
      geom.coordinates.forEach(poly => {
        const outerRing = poly[0]; 
        boundaryRings.push(outerRing);
      });
    });
    console.log('[boundaryRings]', boundaryRings);
  } catch(e) {
    console.error("GeoServer 'tb_boundary' load failed:", e)
  }
}

const test_url = `http://127.0.0.1:8090/geoserver/iseau/ows` +
  `?service=WFS` +
  `&version=1.0.0` +
  `&request=GetFeature` +
  `&typeName=iseau:tb_test_layer` +
  `&outputFormat=application/json` +
  `&srsName=EPSG:4326`
let testBoundaryRings = []

async function testLoadBoundary() {
  try {
    const testRes = await fetch(test_url);
    const testData = await testRes.json();
    testBoundaryRings = []; 
    (data.features || []).forEach(f => {
      const geom = f.geometry;
      if (!geom) return;
      geom.coordinates.forEach(poly => {
        const outerRing = poly[0]; 
        testBoundaryRings.push(outerRing);
      });
    });
    console.log('[testBoundaryRings]', testBoundaryRings);
    testDrawBoundaryRings() 
  } catch(e) {
    console.error("GeoServer 'tb_test_layer' load failed:", e)
  }
}

function testDrawBoundaryRings() {
  if (!map) return;
  testBoundaryRings.forEach(ring => {
    const path = ring.map(([lon, lat]) => new window.naver.maps.LatLng(lat, lon));
    new window.naver.maps.Polyline({
      map,
      path,
      strokeColor: '#0092BA',
      strokeWeight: 3,
      strokeOpacity: 0.9,
    });
  });
  const bounds = new window.naver.maps.LatLngBounds();
  testBoundaryRings.forEach(ring => {
    ring.forEach(([lon, lat]) => bounds.extend(new window.naver.maps.LatLng(lat, lon)));
  });
  if (!bounds.isEmpty?.() && bounds.hasOwnProperty('extend')) {
    map.fitBounds(bounds);
  }
}


// =================================================================
// ## 8. 🔄 생명주기 훅 (Lifecycle Hooks)
// 컴포넌트가 생성/소멸될 때 실행되는 진입점입니다.
// =================================================================

onMounted(() => {
  fetchGroups(); // 1. 그룹 정보 가져오기 (-> 4번, 5번 섹션 로직 실행)
  getLocation(); // 2. 내 위치 1회 가져오기 (-> 6번 섹션 로직 실행)
  requestGeoLocation("test"); // 3. 내 위치 서버로 전송 (7번 섹션 로직 실행)
});

onUnmounted(() => {
  // 4. 페이지 이탈 시, 타이머를 반드시 정리 (메모리 누수 방지)
  if (locationPollTimer) {
      clearInterval(locationPollTimer);
      console.log("[Watcher] 페이지 이탈. 위치 폴링 타이머 제거.");
  }
});
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
</style>