<template>
  <div class="group-main-page" style="position: relative;">

    <template v-if="hasGroup">
      
      <div ref="mapEl" style="width:100%;height:300px;"></div>

      <div class="map-overlay-buttons position-absolute top-0 end-0 p-3">
        <button class="btn btn-sm btn-white rounded-pill shadow-sm mb-2" style="background-color: white;" @click="fetchLocations">
          내 위치 새로고침 <i class="fas fa-sync-alt ms-1"></i>
        </button>
        <button class="btn btn-sm btn-primary rounded-circle shadow-sm" style="width: 40px; height: 40px; background-color: white; border: 1px solid #ccc;">
          <i class="fas fa-location-arrow" :style="{ color: darkColor }"></i>
        </button>
      </div>

      <div v-for="member in groupLocations" :key="member.id"
           :style="markerStyle(member.color)"
           class="position-absolute rounded-circle shadow-sm">
      </div>
      
      <div class="group-actions p-3">
        
        <div class="button-row position-absolute d-flex justify-content-between w-100 px-3">
          <button
            class="btn fw-bold rounded-pill shadow-sm notification-button" 
            style="background-color: #e9ecef;" 
            @click="handleNotificationSettings">
            <i class="fas fa-bell me-2"></i> 알림 설정
          </button>
          <button
            class="btn fw-bold text-white rounded-pill shadow-sm invite-button" 
            :style="{ backgroundColor: mainColor }" 
            @click="showInviteModal = true">
            <i class="fas fa-user-plus me-2"></i> 그룹 초대
          </button>
        </div>

        <button class="btn btn-outline-danger btn-sm w-100 mt-3 mb-4" @click="confirmDeleteGroup">
            <i class="fas fa-trash me-2"></i> 그룹 삭제
        </button>

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
      </div>
    </template>

    <template v-else>
      <div class="group-actions p-3 d-flex align-items-center justify-content-center" style="min-height: 50vh;">
        <div class="p-4 border rounded text-center empty-group-card" :style="{ borderColor: mainColor }">
          <h5 class="fw-bold mb-3">그룹이 없습니다</h5>
          <p class="text-muted mb-4">위치 공유를 위한 그룹을 생성하세요.</p>
          <button class="btn fw-bold text-white w-100 create-group-button" 
                  :style="{ backgroundColor: mainColor }" 
                  @click="showCreateGroupModal = true">
              <i class="fas fa-plus me-2"></i> 그룹 생성하기
          </button>
        </div>
      </div>
    </template>

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
// 이 컴포넌트의 작동 로직 (보이지 않는 부분)
// ---------------------------------

// 사용할 도구들(Vue 기능, Axios 등)을 가져옵니다.
import { ref, onMounted, computed, watch, watchEffect } from 'vue';
import { useRouter } from 'vue-router'; 
import { useConfirmModal } from '@/utils/modalUtils';
import axios from 'axios'; 

import GroupInviteModal from '@/components/GroupInviteModal.vue';
import GroupCreateModal from '@/components/GroupCreateModal.vue'; 

import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'
const store = useStore();
const { } = storeToRefs(store);

// 'mapEl' 이름표를 붙인 HTML 태그(div)를 담을 상자
const mapEl = ref(null);
// Naver Map 객체를 담을 상자
let map;

const router = useRouter(); // 페이지 이동(새로고침) 기능
const { showConfirmModal } = useConfirmModal(); // '정말요?' 확인 팝업창 기능

// 디자인에 사용할 색상 값
const mainColor = '#0092BA';
const darkColor = '#0B1956';

// --- State (컴포넌트의 핵심 기억 상자들) ---
const myGroupList = ref([]); // 내 그룹 정보 (1개 또는 0개)
const activeGroupLocations = ref([]); // 현재 그룹 멤버들의 위치 정보
const showInviteModal = ref(false); // 초대 팝업창 스위치 (true: 보임, false: 숨김)
const showCreateGroupModal = ref(false); // 생성 팝업창 스위치

// --- Computed (자동 계산기) ---

// 'hasGroup' 계산기: 'myGroupList' 상자에 그룹이 1개라도 들어있는지?(true/false)
const hasGroup = computed(() => myGroupList.value.length > 0);

// 'activeGroupId' 계산기: 'hasGroup'이 true면, 'myGroupList'의 첫 번째 그룹 ID를 사용
const activeGroupId = computed(() => {
  return hasGroup.value ? myGroupList.value[0].id : null;
});

// 'groupLocations' 계산기: 'activeGroupLocations' 목록에서 중복된 멤버를 제거한 최종 목록
const groupLocations = computed(() => {
    const locations = activeGroupLocations.value;
    const uniqueMembers = {};
    
    locations.forEach(member => {
        uniqueMembers[member.id] = member;
    });

    return Object.values(uniqueMembers);
});


// --- Actions (컴포넌트가 하는 일) ---

// '알림 설정' 버튼을 누르면 실행되는 일 (지금은 콘솔에 로그만 찍음)
const handleNotificationSettings = () => {
    console.log("알림 설정 버튼 클릭됨");
};

// [일 1] 'fetchGroups': 서버에게 "내 그룹 목록 줘!"라고 요청하는 일
const fetchGroups = async () => {
    try {
        const url = `${import.meta.env.VITE_API_BASE_URL}/groups?timestamp=${new Date().getTime()}`; 
        const response = await axios.get(url, { withCredentials: true });
        myGroupList.value = response.data.data.result; // 결과를 'myGroupList' 상자에 저장
        
        console.log("[FetchGroups] 그룹 목록:", myGroupList.value);
        if (hasGroup.value) {
            console.log("[FetchGroups] 활성 그룹 ID:", activeGroupId.value);
        }

    } catch (error) {
        console.error('그룹 목록 조회 실패:', error, error.response);
        myGroupList.value = []; // 실패하면 비워버림
    }
};

// [일 2] 'fetchLocations': 서버에게 "활성화된 그룹 멤버들 위치 줘!"라고 요청하는 일
const fetchLocations = async () => {
    // 'activeGroupId'가 없으면(null) 일을 시작하지 않음
    if (!activeGroupId.value) {
        console.warn("[FetchLocations] Aborted: activeGroupId is null.");
        return;
    }
    
    console.log(`[FetchLocations] 그룹 ID ${activeGroupId.value}의 위치 조회 시작...`);

    try {
        const url = `${import.meta.env.VITE_API_BASE_URL}/groups/locations?groupId=${activeGroupId.value}`;
        const response = await axios.get(url, { withCredentials: true });
        activeGroupLocations.value = response.data.data.result; // 결과를 'activeGroupLocations' 상자에 저장
    } catch (error) {
        console.error('그룹 위치 정보 조회 실패:', error);
        activeGroupLocations.value = []; // 실패하면 비워버림
    }
};

// [일 3] 'handleGroupCreated': 그룹 생성 팝업창이 "성공!" 신호를 보냈을 때 하는 일
const handleGroupCreated = (newGroupId) => {
    showCreateGroupModal.value = false; // 생성 팝업창 스위치를 끔
    console.log(`[GroupCreate] 새 그룹 생성됨: ${newGroupId}. 그룹 목록 갱신...`);
    fetchGroups(); // [일 1]을 다시 실행 (UI를 '그룹 있음' 상태로 바꾸기 위해)
};

// [일 4] 'confirmDeleteGroup': '그룹 삭제' 버튼을 눌렀을 때 하는 일
const confirmDeleteGroup = () => {
  if (!activeGroupId.value) return;
  
  // '정말 삭제할거야?'라고 물어보는 팝업창을 띄웁니다.
  showConfirmModal(
    '그룹 삭제',
    '정말로 이 그룹을 삭제하시겠습니까? 모든 멤버의 연결이 끊어집니다.',
    () => deleteGroup() // '확인'을 누르면 [일 5]를 실행
  );
};

// [일 5] 'deleteGroup': 'confirmDeleteGroup'에서 '확인'을 눌렀을 때 진짜로 삭제하는 일
const deleteGroup = async () => {
    if (!activeGroupId.value) return;
    
    console.log(`[DeleteGroup] 그룹 ID ${activeGroupId.value} 삭제 시도...`);
    
    try {
        const url = `${import.meta.env.VITE_API_BASE_URL}/groups/${activeGroupId.value}`; 
        await axios.delete(url, { withCredentials: true }); // 서버에 "이 그룹 삭제해줘!" 요청
        
        console.log("[DeleteGroup] 삭제 성공. 그룹 목록 갱신...");
        activeGroupLocations.value = []; // 멤버 위치 목록 비우기
        fetchGroups(); // [일 1]을 다시 실행 (UI를 '그룹 없음' 상태로 바꾸기 위해)
        
    } catch (error) {
        console.error('그룹 삭제 실패:', error);
        alert('그룹 삭제에 실패했습니다.');
    }
};

// --- Lifecycle & Watchers (자동으로 실행되는 코드) ---

// 'loadGroupData'라는 작은 일 (fetchLocations 실행)
const loadGroupData = () => {
  if (activeGroupId.value) {
    fetchLocations(); 
  }
}

// [자동] 'onMounted': 이 페이지(컴포넌트)가 화면에 처음 나타났을 때 *단 한 번* 실행
onMounted(() => {
  fetchGroups(); // [일 1] 실행 (그룹 있는지 확인)
  getLocation(); // 내 핸드폰 위치 켜기
  requestGeoLocation(null); // (500 오류 방지를 위해 'test' 대신 null 전달)
});

// [자동] 'watch': 'activeGroupId' 상자를 *계속 지켜봅니다.*
watch(activeGroupId, (newId, oldId) => {
    // 'activeGroupId' 상자의 값이 바뀌면 (예: 그룹 생성 직후)
    if (newId) {
        console.log(`[Watcher] activeGroupId 변경됨: ${oldId} -> ${newId}. 위치 로드 시작...`);
        loadGroupData(); // 'loadGroupData' 일을 실행
    }
}, { immediate: true }); // immediate: true (페이지 로드 시에도 일단 한 번 실행)

// 'markerStyle': 더미 마커의 스타일(색상, 위치)을 랜덤으로 정해주는 함수
const markerStyle = (color) => ({
  backgroundColor: color || 'blue', 
  width: '12px',
  height: '12px',
  top: `${Math.random() * 80 + 10}%`, 
  left: `${Math.random() * 80 + 10}%`,
  zIndex: 10,
  border: '2px solid white',
});


/* 지도 부분 */
const latitude = ref('') // 내 위치(위도) 기억 상자
const longitude = ref('') // 내 위치(경도) 기억 상자

// [자동] 'watchEffect': 지도 그리기에 필요한 재료들을(위도, 경도, 지도 영역, 그룹 상태) 계속 지켜봄
watchEffect(() => {
  const lat = latitude.value
  const lng = longitude.value

  // 재료가 하나라도 준비 안 됐거나, 그룹이 없으면(hasGroup: false) 지도를 그리지 않음
  if (!hasGroup.value || !lat || !lng || !mapEl.value || !window.naver?.maps) return

  // 재료가 다 준비되면 Naver 지도 API를 사용해 지도를 그림
  const pos = new window.naver.maps.LatLng(lat, lng)

  if (!map) {
    // (지도 그린 적 없으면) 새로 그림
    map = new window.naver.maps.Map(mapEl.value, {
      center: pos,
      zoom: 15
    })
    
    // 💡 GeoServer 요청 (주석 없음 - 사용자 요청)
     window.naver.maps.Event.once(map, 'init', testLoadBoundary)
    loadBoundary()
  } else {
    // (지도 그린 적 있으면) 중심 위치만 이동
    map.setCenter(pos)
  }
})

// --- GeoServer / Location (사용자 요청으로 주석 제외) ---

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
    (testData.features || []).forEach(f => {
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


// [일] 'getLocation': 핸드폰 GPS에게 "현재 위도/경도 줘!"라고 요청하는 일
function getLocation() {
  if (!navigator.geolocation) return;
  navigator.geolocation.getCurrentPosition(
    (pos) => { latitude.value = pos.coords.latitude; longitude.value = pos.coords.longitude; },
    (err) => { console.error('위치 실패: ' + err.message); },
    { enableHighAccuracy: true }
  )
}

// [일] 'requestGeoLocation': 서버에 내 위치를 전송하는 일 (현재는 500 오류 유발)
function requestGeoLocation(value) {
  if (!navigator.geolocation) return;

  navigator.geolocation.getCurrentPosition(
    async (pos) => {
      latitude.value = pos.coords.latitude
      longitude.value = pos.coords.longitude

      const payload = {
        latitude: pos.coords.latitude,
        longitude: pos.coords.longitude,
      }
      console.log('sending to server:', payload)

      let axiosUrl;
      
      if ( value === "test") {
        axiosUrl = `${import.meta.env.VITE_API_BASE_URL}/location/testBoundaryCheck`;
      }
      if ( value === "boundary") {
        axiosUrl = `${import.meta.env.VITE_API_BASE_URL}/location/boundaryCheck`;
      }

      if (!axiosUrl) {
        console.warn("requestGeoLocation: 'value'가 'test' 또는 'boundary'가 아니라서 API를 호출하지 않습니다.");
        return;
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
        console.log('OK', res.data)
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

</script>

<style scoped>
/* --------------------------------- */
/* 이 컴포넌트의 디자인 (CSS) */
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
.create-group-button {
  font-size: 1rem;
  padding: 10px 20px;
  height: 50px;
  border-radius: 25px;
}

/* 💡 [디자인 복구] 스크린샷의 버튼 배치를 위한 스타일 */
.group-actions {
  position: relative; 
  padding-top: 40px; 
}

.button-row {
  z-index: 100; /* 지도 위에 오도록 */
  top: -20px; /* 버튼을 지도와 겹치도록 위로 올림 */
  left: 0;
}
.notification-button {
  font-size: 0.95rem;
  padding: 10px 15px;
  height: 45px;
  min-width: 120px; 
  color: v-bind(darkColor); 
  border: 1px solid #dee2e6; 
  background-color: #e9ecef; 
}
.invite-button {
  font-size: 0.95rem;
  padding: 10px 15px;
  height: 45px;
  min-width: 120px; 
}

.btn-outline-danger {
  border-color: #dc3545;
  color: #dc3545;
}

.btn-outline-danger:hover {
  background-color: #dc3545;
  color: white;
}
</style>