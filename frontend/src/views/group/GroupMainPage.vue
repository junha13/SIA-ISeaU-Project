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
              @click="confirmDeleteGroup">
              <i class="fas fa-trash me-1"></i> 삭제
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
// 🐬 작동 로직 (JavaScript)
// ---------------------------------
import { ref, onMounted, computed, watch, watchEffect } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirmModal } from '@/utils/modalUtils';
import axios from 'axios'; 
import GroupInviteModal from '@/components/GroupInviteModal.vue';
import GroupCreateModal from '@/components/GroupCreateModal.vue';

import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'
const store = useStore();
const { header, beach } = storeToRefs(store)

const mapEl = ref(null);
let map;

const router = useRouter();
const { showConfirmModal } = useConfirmModal(); 

const mainColor = '#0092BA';
const darkColor = '#0B1956';

// --- State (기억 상자) ---
const myGroupList = ref([]);
const activeGroupLocations = ref([]);
const showInviteModal = ref(false);
const showCreateGroupModal = ref(false);

// --- Computed (자동 계산기) ---
const hasGroup = computed(() => myGroupList.value.length > 0);

// 그룹이 있으면 첫 번째 그룹의 ID를 활성 ID로 사용
const activeGroupId = computed(() => {
  return hasGroup.value ? myGroupList.value[0].id : null;
});

// 멤버 목록에서 중복 제거
const groupLocations = computed(() => {
    const locations = activeGroupLocations.value;
    const uniqueMembers = {};
    
    // id (user_number)를 키로 사용하여 중복 제거
    locations.forEach(member => {
        // 중복될 경우, 나중에 들어온 값(일반적으로 더 정확한 리더 정보)으로 덮어씁니다.
        // (PostgreSQL 쿼리에서 ORDER BY id, order_key를 사용했으므로 안정적입니다)
        uniqueMembers[member.id] = member;
    });

    return Object.values(uniqueMembers);
});


// --- Actions (하는 일) ---
const handleNotificationSettings = () => {
    console.log("알림 설정 버튼 클릭됨");
};

/**
 * 그룹 목록을 API로부터 가져와 업데이트합니다.
 * [API] 내 그룹 목록을 서버에서 가져와 'myGroupList'를 채우는 일
 * 🐬 이 함수는 서버에게 "나(로그인한 사용자)의 유일한 그룹이 있는지 찾아 줘!"라고 요청합니다.
 */
const fetchGroups = async () => {
    try {
        const url = `${import.meta.env.VITE_API_BASE_URL}/groups?timestamp=${new Date().getTime()}`;
        const response = await axios.get(url, { withCredentials: true });
        myGroupList.value = response.data.data.result; // 결과를 'myGroupList' 상자에 저장

        console.log("[FetchGroups] 그룹 목록:", myGroupList.value);
        // 🐬 그룹이 존재하면 ID를 확정하여 활성 상태로 전환합니다.
        if (hasGroup.value) {
            console.log("[FetchGroups] 활성 그룹 ID:", activeGroupId.value);
        }

    } catch (error) {
        console.error('그룹 목록 조회 실패:', error, error.response);
        myGroupList.value = []; // 실패하면 비워버림
    }
};

/**
 * 활성화된 그룹의 멤버 위치 정보를 가져옵니다.
 */
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

// [이벤트] 그룹 생성 성공 후 처리 (목록 갱신)
const handleGroupCreated = (newGroupId) => {
    showCreateGroupModal.value = false; // 생성 팝업창 스위치를 끔
    console.log(`[GroupCreate] 새 그룹 생성됨: ${newGroupId}. 그룹 목록 갱신...`);
    fetchGroups(); // [일 1]을 다시 실행 (UI를 '그룹 있음' 상태로 바꾸기 위해)
};


const confirmDeleteGroup = () => {
  if (!activeGroupId.value) return;

  // 🚨 deleteGroup을 호출합니다.
  console.log(`[ConfirmDelete] 그룹 ID ${activeGroupId.value} 삭제 확인 건너뛰고 즉시 실행.`);
  deleteGroup();
};

// [API] 그룹 삭제 (서버 통신)
const deleteGroup = async () => {
    if (!activeGroupId.value) return;

    console.log(`[DeleteGroup] 그룹 ID ${activeGroupId.value} 삭제 API 호출 시작...`);

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
    // 위치 정보 로드
    fetchLocations(); 
  }
}

// [자동] 'onMounted': 이 페이지(컴포넌트)가 화면에 처음 나타났을 때 *단 한 번* 실행
onMounted(() => {
  fetchGroups(); // [일 1] 실행 (그룹 있는지 확인)
  getLocation(); // 내 핸드폰 위치 켜기
  requestGeoLocation("test"); // (500 오류 방지를 위해 'test' 대신 null 전달)
});

// [자동] 'watch': 'activeGroupId' 상자를 *계속 지켜봅니다.*
watch(activeGroupId, (newId, oldId) => {
    // 'activeGroupId' 상자의 값이 바뀌면 (예: 그룹 생성 직후)
    if (newId) {
        console.log(`[Watcher] activeGroupId 변경됨: ${oldId} -> ${newId}. 위치 로드 시작...`);
        loadGroupData(); // 'loadGroupData' 일을 실행
    }
}, { immediate: true }); // immediate: true (페이지 로드 시에도 일단 한 번 실행)

// 'markerStyle': 더미 마커의 위치와 스타일을 정해주는 함수
const markerStyle = (color) => ({
  backgroundColor: color || 'blue',
  width: '12px',
  height: '12px',
  top: `${Math.random() * 80 + 10}%`,
  left: `${Math.random() * 80 + 10}%`,
  zIndex: 10,
  border: '2px solid white',
});


/*
========================================================
                        지도 부분
========================================================
*/

const latitude = ref('')
const longitude = ref('')
let marker=null

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
    marker = new window.naver.maps.Marker({
      position: pos,
      map
    })

  //window.naver.maps.Event.once(map, 'init', loadBoundary)
  window.naver.maps.Event.once(map, 'init', testLoadBoundary)
  loadBoundary()
    // 이미 map이 만들어져 있으면 새로 안 만들고 중심 좌표와 마커 위치만 업데이트
  } else {
    map.setCenter(pos)
    marker.setPosition(pos)
  }
})

/**
 * ============================================
 *          지오로케이션 내 위치 보기
 * ============================================
 */
function getLocation() {
  if (!navigator.geolocation) return;
  navigator.geolocation.getCurrentPosition(
    (pos) => { latitude.value = pos.coords.latitude; longitude.value = pos.coords.longitude; },
    (err) => { console.error('위치 실패: ' + err.message); },
    { enableHighAccuracy: true }
  )
}


/**
 * ============================================
 *  내 위치 해안선 or 테스트 폴리곤 비교하고 거리 받기
 * ============================================
 */
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
      }
      console.log('sending to server:', payload)

      let axiosUrl;
      if ( value === "test") {
        axiosUrl = `${import.meta.env.VITE_API_BASE_URL}/location/testBoundaryCheck`;
      }
      if ( value === "boundary") {
        axiosUrl = `${import.meta.env.VITE_API_BASE_URL}/location/boundaryCheck`;
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

/**
 * ================================================
 *                  폴리곤 만들기
 * ================================================
 */
const url = `http://127.0.0.1:8090/geoserver/iseau/ows` +
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
const test_url = `http://127.0.0.1:8090/geoserver/iseau/ows` +
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

</script>

<style scoped>
/* --------------------------------- */
/* 🐬 디자인 (CSS) */
/* --------------------------------- */

.group-main-page {
  /* AppLayout의 Header와 Footer 사이의 공간 */
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

/* 💡 [디자인 수정] 버튼들을 지도 아래로 내림 */
.group-actions {
  position: relative;
  padding-top: 1rem;
}

/* 💡 [추가] 3개 버튼 공통 스타일 (크기 고정) */
.action-button {
  font-size: 0.9rem;
  padding: 8px 12px;
  height: 42px;       /* 높이 통일 */
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