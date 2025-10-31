<template>
  <div class="group-main-page" style="position: relative;">
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
      <div class="d-flex justify-content-between align-items-center mb-3">
        <button class="btn btn-light-secondary fw-bold rounded-pill" style="background-color: #e9ecef;">
          <i class="fas fa-bell me-2"></i> 알림 설정
        </button>
        <button class="btn fw-bold text-white" :style="{ backgroundColor: mainColor }" @click="showInviteModal = true">
          <i class="fas fa-user-plus me-2"></i> 그룹 초대
        </button>
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
            
            <span v-if="member.status === 'online'" class="text-success small fw-bold">
              online
            </span>
            <span v-else-if="member.status === 'pending'" class="text-muted small fw-bold">
              (초대 중)
            </span>
            <i class="fas fa-comment-dots text-secondary ms-3 me-3" style="cursor: pointer;"></i>
            <i class="fas fa-ellipsis-v text-secondary" style="cursor: pointer;"></i>
          </div>
        </div>
        </div>
    </div>

    <GroupInviteModal v-model:isVisible="showInviteModal" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, watchEffect } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useConfirmModal } from '@/utils/modalUtils';
import axios from 'axios'; 
import GroupInviteModal from '@/components/GroupInviteModal.vue';

import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'
const store = useStore();
const { header} = storeToRefs(store)

const mapEl = ref(null)
let map, marker

const route = useRoute();
const router = useRouter();

const mainColor = '#0092BA';
const darkColor = '#0B1956';
const dangerColor = '#EB725B';

// --- State ---
const myGroupList = ref([]);
const activeGroupLocations = ref([]);
const receivedInvitation = ref(null); 
const showInviteModal = ref(false);

// --- Getters & Computed ---
const activeGroupId = computed(() => parseInt(route.params.id) || null); 
const activeGroupName = computed(() =>
    myGroupList.value.find(g => g.id === activeGroupId.value)?.name || '그룹 위치 알림'
);

/**
 * [수정됨] 최종적으로 UI에 표시될 그룹 멤버 목록 (중복 제거 로직)
 */

 /**
 * =============================================================
 *         이게 언제 실행되는건지?
 * =============================================================
 */ 
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


// --- Actions ---

/**
 * 그룹 목록을 API로부터 가져와 업데이트합니다.
 */
const fetchGroups = async () => {
    try {
        const url = `${import.meta.env.VITE_API_BASE_URL}/groups?timestamp=${new Date().getTime()}`; 

        const response = await axios.get(url, { withCredentials: true });
        myGroupList.value = response.data.data.result; 

    } catch (error) {
        console.error('그룹 목록 조회 실패:', error, error.response);
        if (error.response && error.response.status === 401) {
            console.log('로그인이 필요합니다.');
            // router.push('/login'); 
        }
    }
};


/**
 * =============================================================
 *          활성화된 그룹의 멤버 위치 정보를 가져옵니다.
 * =============================================================
 */ 
const fetchLocations = async () => {
    if (!activeGroupId.value) return;

    try {
        const url = `${import.meta.env.VITE_API_BASE_URL}/groups/locations?groupId=${activeGroupId.value}`;
        
        const response = await axios.get(url, { withCredentials: true });
        
        // State 업데이트
        activeGroupLocations.value = response.data.data.result;
        console.log(response.data.data.result)

    } catch (error) {
        console.error('그룹 위치 정보 조회 실패:', error);
        activeGroupLocations.value = [];
    }
};

/**
 * 특정 그룹의 위치 공유 메인 페이지로 이동 (컴포넌트 자체에서는 사용하지 않음)
 */
const goToGroupMain = (groupId) => {
  router.push({ name: 'GroupMain', params: { id: groupId } });
};

// --- Lifecycle & Watchers ---

const loadGroupData = () => {
  if (activeGroupId.value) {
    // 위치 정보 로드
    fetchLocations(); 
  }
}

onMounted(() => {
  // 그룹 목록을 먼저 로드 (그룹 이름을 표시하기 위해 필요)
  //fetchGroups(); 

  getLocation() // 내 위치 로드

  requestGeoLocation("test")


});


/**
 * ============================================
 *          이게 온마운트 기능인가? 
 * ============================================
 */ 
// URL의 그룹 ID가 변경될 때마다 데이터 다시 로드
watch(activeGroupId, loadGroupData, { immediate: true });

// 지도 마커 스타일 (더미)
const markerStyle = (color) => ({
  backgroundColor: color,
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
    // marker = new window.naver.maps.Marker({
    //   position: pos,
    //   map
    // })

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
</style>