<template>
  <div class="group-main-page" style="position: relative;">
    <!-- 2. 지도 영역 -->
    <div ref="mapEl" style="width:100%;height:300px;"></div>

      <!-- 지도 오버레이 버튼 -->
      <div class="map-overlay-buttons position-absolute top-0 end-0 p-3">
        <button class="btn btn-sm btn-white rounded-pill shadow-sm mb-2" style="background-color: white;">
          내 위치 새로고침 <i class="fas fa-sync-alt ms-1"></i>
        </button>
        <button class="btn btn-sm btn-primary rounded-circle shadow-sm" style="width: 40px; height: 40px; background-color: white; border: 1px solid #ccc;">
          <i class="fas fa-location-arrow" :style="{ color: darkColor }"></i>
        </button>
      </div>

      <!-- 멤버 마커 (더미) -->
      <div v-for="member in groupLocations" :key="member.id"
           :style="markerStyle(member.color)"
           class="position-absolute rounded-circle shadow-sm">
      </div>
    

    <!-- 3. 그룹 액션 & 멤버 리스트 -->
    <div class="group-actions p-3">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <button class="btn btn-light-secondary fw-bold rounded-pill" style="background-color: #e9ecef;">
          <i class="fas fa-bell me-2"></i> 알림 설정
        </button>
        <button class="btn fw-bold text-white" :style="{ backgroundColor: mainColor }" @click="showInviteModal = true">
          <i class="fas fa-user-plus me-2"></i> 그룹 초대
        </button>
      </div>

      <!-- 그룹 멤버 리스트 -->
      <h6 class="fw-bold mb-3" :style="{ color: darkColor }">그룹 멤버 ({{ groupLocations.length }}명)</h6>
      <div class="member-list">
        <div v-for="member in groupLocations" :key="member.id" class="d-flex align-items-center py-2 border-bottom">
          <!-- 마커 색상 구분 선 -->
          <div class="me-3 rounded-pill" :style="{ backgroundColor: member.color, width: '4px', height: '50px' }"></div>

          <!-- 멤버 정보 -->
          <div class="flex-grow-1">
            <h6 class="fw-bolder mb-0 fs-6">{{ member.name }} <span class="small text-muted fw-normal ms-1">{{ member.username }}</span></h6>
            <p class="text-secondary small mb-0">{{ member.phone }}</p>
          </div>

          <!-- 상태 및 액션 -->
          <div class="d-flex align-items-center">
            <span :class="['small fw-bold', member.status === '활동 중' ? 'text-success' : 'text-danger']">{{ member.status }}</span>
            <i class="fas fa-comment-dots text-secondary ms-3 me-3" style="cursor: pointer;"></i>
            <i class="fas fa-ellipsis-v text-secondary" style="cursor: pointer;"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- 그룹원 추가 모달 -->
    <GroupInviteModal v-model:isVisible="showInviteModal" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, watchEffect } from 'vue';
import { useRoute } from 'vue-router';
import { useGroupStore } from '@/stores/groupStore.js';
import GroupInviteModal from '@/components/GroupInviteModal.vue';
import axios from 'axios';

import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'
const store = useStore();
const { header, beach } = storeToRefs(store)

const mapEl = ref(null)
let map, marker

const route = useRoute();
const mainColor = '#0092BA';
const darkColor = '#0B1956';
const dangerColor = '#EB725B';

const groupStore = useGroupStore();
const showInviteModal = ref(false);

// 라우트 파라미터에서 그룹 ID 가져오기
const activeGroupId = computed(() => parseInt(route.params.id) || null);
// 그룹 스토어의 그룹 목록에서 현재 그룹 이름 찾기
const activeGroupName = computed(() =>
    groupStore.getMyGroupList.find(g => g.id === activeGroupId.value)?.name || '그룹 위치 알림'
);


// Pinia에서 그룹 위치 정보 가져오기
const groupLocations = computed(() => groupStore.getActiveGroupLocations);

// --- Lifecycle & Watchers ---

const loadGroupData = () => {
  if (activeGroupId.value) {
    // Pinia Store의 활성 그룹 ID 업데이트
    groupStore.setActiveGroup(activeGroupId.value);
    // 위치 정보 로드
    groupStore.fetchLocations();
  }
}

onMounted(() => {
  // 마운트 시 데이터 로드
  groupStore.fetchGroups(); // 그룹 목록을 먼저 로드

  getLocation() // 내 위치 로드

  requestGeoLocation("test")


});

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

지도 부분 

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


// 지오로케이션 내 위치 보기
function getLocation() {
  if (!navigator.geolocation) return;
  navigator.geolocation.getCurrentPosition(
    (pos) => { latitude.value = pos.coords.latitude; longitude.value = pos.coords.longitude; },
    (err) => { console.error('위치 실패: ' + err.message); },
    { enableHighAccuracy: true }
  )
}

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
      if ( value = "test") axiosUrl = `${import.meta.env.VITE_API_BASE_URL}api/location/testBoundaryCheck`
      if ( value = "boundary") axiosUrl = `${import.meta.env.VITE_API_BASE_URL}api/location/boundaryCheck`

      try {
        const res = await axios.post(
          `${import.meta.env.VITE_API_BASE_URL}api/location/testBoundaryCheck`,
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