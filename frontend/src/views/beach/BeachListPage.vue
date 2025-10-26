<template>
  <div v-if="viewMode === 'list'" class="beach-list-page p-3">
    <div class="sticky-top bg-white pt-1 pb-3" style="z-index: 900;">
      <div class="d-flex align-items-center mb-3">
        <div class="dropdown me-2">
          <button
            class="btn btn-secondary dropdown-toggle d-flex align-items-center"
            type="button"
            data-bs-toggle="dropdown"
            aria-expanded="false"
            :style="dropdownBtnStyle"
          >
            {{ searchParams.region || '지역 ' }}
          </button>
          <ul class="dropdown-menu">
            <li v-for="region in regionCondition" :key="region.value">
              <a class="dropdown-item" href="#" @click.prevent="selectRegion(region.value)">{{ region.label }}</a>
            </li>
          </ul>
        </div>

        <div class="input-group flex-grow-1">
          <input
            type="text"
            class="form-control"
            placeholder="해수욕장 검색..."
            v-model="searchParams.keyword"
            @keyup.enter="loadData"
            aria-label="해수욕장 검색"
            style="border-radius: 0.475rem 0 0 0.475rem;"
          />
          <button
            class="btn"
            type="button"
            @click="loadData"
            :style="{ backgroundColor: mainColor, color: 'white', border: 'none', borderTopLeftRadius: 0, borderBottomLeftRadius: 0 }"
          >
            <i class="fas fa-search"></i>
          </button>
        </div>
      </div>

      <div class="d-flex overflow-auto gap-2 mb-3 pb-2" style="white-space: nowrap;">
        <button v-for="tab in tabCondition" :key="tab.value" :class="['btn','btn-sm', 'rounded-pill', activeTab === tab.value ? 'tab-btn-primary' : 'tab-btn-light-secondary']"
          @click="activeTab = tab.value">
          {{ tab.label }}
        </button>
      </div>

      <div class="d-flex align-items-center justify-content-between">
        <div class="dropdown me-3">
          <button class="btn btn-light-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
            {{ sortOptions.find(opt => opt.value === currentSort).label }}
          </button>
          <ul class="dropdown-menu">
            <li v-for="opt in sortCondition" :key="opt.value">
              <a class="dropdown-item" href="#" @click.prevent="selectSort(opt.value)">{{ opt.label }}</a>
            </li>
          </ul>
        </div>

        <div class="btn-group" role="group">
          <button
            type="button"
            :class="['btn', 'border', viewMode === 'list' ? 'tab-btn-primary' : 'tab-btn-light-secondary']"
            @click="viewMode = 'list'"
            :style="viewMode === 'list' ? primaryBtnStyle : {}"
          >리스트</button>
          <button
            type="button"
            :class="['btn', 'border', viewMode === 'map' ? 'tab-btn-primary' : 'tab-btn-light-secondary']"
            @click="viewMode = 'map'"
            :style="viewMode === 'map' ? primaryBtnStyle : {}"
          >지도</button>
        </div>
      </div>
    </div>

    <div class="mt-3">
      <div v-if="viewMode === 'list'">
        <!-- 목록 -->
        <div v-if="isLoading" class="text-center p-5">
          <i class="fas fa-spinner fa-spin me-2"></i> 목록을 불러오는 중...
        </div>
        <div v-else-if="apiError" class="text-center text-danger p-5">
          목록을 불러오는 중 오류가 발생했습니다.
        </div>
        <div v-else-if="!filteredBeachList.length" class="text-center text-muted p-5">
          표시할 해수욕장이 없습니다.
        </div>
        <div v-else>
          <div
            v-for="beach in filteredBeachList"
            :key="`${beach.beachNumber}-${beach.updatedAt || ''}`"
            class="beach-card card shadow-sm mb-4 rounded-3 border-0"
            @click="goToDetail(beach.beachNumber)"
          >
            <div class="card-body p-3 d-flex">
              <div class="beach-image-placeholder me-3 rounded-2" :style="{ border: '1px solid #eee' }">
                <img v-if="beach.beachImage" :src="beach.beachImage" :alt="beach.beachName + ' 이미지'" style="width: 100%; height: 100%; object-fit: cover; border-radius: 0.25rem;" />
                <p v-else class="text-center text-muted fw-bold mb-0 pt-2 fs-7">{{ beach.beachImage === null ? '이미지 준비 중' : '이미지 없음' }}</p>
                <div class="rating-badge badge text-white px-2 py-1 rounded-pill" :style="{ backgroundColor: mainColor }">
                  <i class="fas fa-star fs-7"></i> {{ Number(beach.rating).toFixed(1) }}
                </div>
              </div>

              <div class="beach-info flex-grow-1">
                <div class="d-flex justify-content-between align-items-start">
                  <h5 class="fw-bolder fs-6 mb-1" :style="{ color: darkColor }">{{ beach.beachName }}</h5>
                  <i
                    :class="['fas fa-heart fs-5', { 'text-danger': isFavorite(beach.beachNumber), 'text-muted': !isFavorite(beach.beachNumber) }]"
                    @click.stop="toggleFavorite(beach.beachNumber)"
                    style="cursor: pointer;"
                  ></i>
                </div>

                <p class="text-muted fs-7 mb-2">{{ beach.address }}</p>

                <div class="d-flex gap-2 mb-3">
                  <span v-for="(tag, index) in beach.tags" :key="index" :class="['badge', 'px-2', 'py-1', 'fw-bold', tagClass(tag)]">
                    {{ tag }}
                  </span>
                </div>

                <div class="d-flex justify-content-between align-items-center">
                  <p class="fs-7 mb-0 text-muted">{{ beach.distance }} 거리</p>

                  <button v-if="isSelected(beach.beachNumber)" class="btn btn-sm fw-bold" :style="{ backgroundColor: mainColor, color: 'white' }" @click.stop="toggleSelect(beach.beachNumber, beach.beachName)">선택됨</button>
                  <button v-else class="btn btn-sm btn-outline-secondary fw-bold" @click.stop="toggleSelect(beach.beachNumber, beach.beachName)">선택하기</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div v-else>
    <div>
      <div class="map-view d-flex align-items-center justify-content-center bg-light rounded-3 shadow-sm"
            style="height: 100vh; border: 1px solid #ccc;">
        <div ref="beachMap" style="width:100%;height:100%; z-index: 1;">
          <div class="d-flex justify-content-end">
            <div class="btn-group p-1" role="group" style="z-index: 2;">
              <button
                type="button"
                :class="['btn', 'border', viewMode === 'list' ? 'tab-btn-primary' : 'tab-btn-light-secondary']"
                @click="viewMode = 'list'"
                :style="viewMode === 'list' ? primaryBtnStyle : {}"
              >리스트</button>
              <button
                type="button"
                :class="['btn', 'border', viewMode === 'map' ? 'tab-btn-primary' : 'tab-btn-light-secondary']"
                @click="viewMode = 'map'"
                :style="viewMode === 'map' ? primaryBtnStyle : {}"
              >지도</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, watchEffect } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'

const store = useStore();
const { header, beach, tabOptions, sortOptions, regionOptions } = storeToRefs(store)

const router = useRouter();
const beaches = ref([]);
const selectedBeachId = ref(null);
const favoriteBeachIds = ref([]);
const isLoading = ref(false);
const apiError = ref(null);

// 수정하기
const FAVORITES_API_URL = 'http://localhost:8080/api/beach/favorites';
const BEACH_LIST_API_URL = 'http://localhost:8080/api/beach/beaches';

const mainColor = '#0092BA';
const darkColor = '#0B1956';

// 보여주는 방식
const activeTab = ref('all');
const viewMode = ref('list');
const currentSort = ref('distance');

// 검색조건 - store에서 가져옴
const tabCondition = tabOptions
const sortCondition = sortOptions
const regionCondition = regionOptions

const primaryBtnStyle = { backgroundColor: mainColor, borderColor: mainColor, color: 'white' };
const dropdownBtnStyle = { backgroundColor: '#f8f9fa', borderColor: '#ced4da', color: darkColor };

// 검색 파라미터
const searchParams = ref({
  region: '',
  keyword: '',
  sort: currentSort.value,
});

onMounted(() => {
  loadData();
  fetchFavoriteIds();
  header.value = "해수욕장 리스트"
  getLocation()
});

// 검색조건 넣어서 받기
async function loadData() {
  isLoading.value = true;
  apiError.value = null;
  try {
    const response = await axios.post(BEACH_LIST_API_URL, searchParams.value);
    beaches.value = response.data.result
  } catch (error) {
    apiError.value = error;
  } finally {
    isLoading.value = false;
  }
}

// 즐겨찾기
const fetchFavoriteIds = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/beach/favorites/my');

    const resData = res.data?.data?.result;
    favoriteBeachIds.value = Array.isArray(resData) ? resData : resData ? [resData] : [];

    console.log("⭐ 즐겨찾기 API 응답:", favoriteBeachIds.value);

  } catch (error) {
    console.error("즐겨찾기 초기 로딩 실패:", error);
    favoriteBeachIds.value = []; // 실패 시 빈 배열
  }
};

// 지역 검색조건
function selectRegion(region) {
  searchParams.value.region = region;
  loadData();
}

// 거리, 평점 등 검색조건
function selectSort(sortValue) {
  currentSort.value = sortValue;
  searchParams.value.sort = sortValue;
  loadData();
}

// 즐겨찾기 토글
async function toggleFavorite(beachNumber) {
  try {
    if (favoriteBeachIds.value.includes(beachNumber)) {
      // 즐겨찾기 삭제
      await axios.delete(`${FAVORITES_API_URL}/${beachNumber}`);
      favoriteBeachIds.value = favoriteBeachIds.value.filter(id => id !== beachNumber);
    } else {
      // 즐겨찾기 추가
      await axios.post(FAVORITES_API_URL, { beachNumber });
      favoriteBeachIds.value.push(beachNumber);
    }
  } catch (error) {
    console.error("즐겨찾기 토글 실패:", error);
  }
}

// 필터된 해수욕장 리스트 (즐겨찾기 탭 포함)
const filteredBeachList = computed(() => {
  let list = dbOnlyList.value;

  if (activeTab.value === 'favorite') {
    list = list.filter(b => favoriteBeachIds.value.includes(b.beachNumber));
  }

  return list;
});

const dbOnlyList = computed(() => beaches.value);
const isSelected = id => selectedBeachId.value === id;
const toggleSelect = id => selectedBeachId.value = isSelected(id) ? null : id;
const isFavorite = id => favoriteBeachIds.value.includes(id);
const goToDetail = id => router.push(`/beach/${id}`);
const tagClass = tag => ({
  '안전': 'bg-secondary',
  '수영': 'bg-info',
  '서핑': 'bg-info',
  '산책': 'bg-warning',
  '가족': 'bg-success',
}[tag] || 'bg-light text-dark');

//===================== 지도 부분 ========================

const beachMap = ref(null)
let map
let markers = []

const latitude = ref('')
const longitude = ref('')

watch(viewMode, (mode) => {
  if (mode !== 'map') {
    map = null
    markers = []
  }
})

// 관련된(함수 내부) 반응형 값들이 바뀌면 이 콜백을 다시 실행해주는 함수
watchEffect(() => {

  const lat = latitude.value
  const lng = longitude.value

  // 지도 모드일 때만 돌리기 (리스트 모드일 땐 굳이 안 그림)
  if (viewMode.value !== 'map') return

  // 아직 준비 안 된 경우 바로 종료
  if (!lat || !lng || !beachMap.value || !window.naver?.maps) return

  const list = beaches.value

  // map이 한 번도 만들어진 적 없으면 (초기 렌더 시점)
  if (!map) {
    // 내위치로 센터 맞춤
    const center = new window.naver.maps.LatLng(lat, lng)
    map = new window.naver.maps.Map(beachMap.value, {
      center,
      zoom: 15
    })
  }

  // 기존 마커 있었으면 지도에서 지우고 배열 초기화
  markers.forEach(m => m.setMap(null)) // marker.setMap(null) 이 지도에서 마커 지우는거임
  markers = []

// 전체 해수욕장 목록 마커 다시 그림
  list.forEach(b => {
    if (!b.latitude || !b.longitude) return
    const pos = new window.naver.maps.LatLng(b.latitude, b.longitude)
    const m = new window.naver.maps.Marker({
      position: pos,
      map,
      title: b.beachName
    })
    markers.push(m)
  })
})

// ========== Geolocation API ==========
function getLocation() {
  if (!navigator.geolocation) {
    //error.value = '이 브라우저는 Geolocation을 지원하지 않아요.'
    return
  }
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      latitude.value = pos.coords.latitude
      longitude.value = pos.coords.longitude
    },
    (err) => { err.value = '위치 실패: ' + err.message },
    { enableHighAccuracy: true }
  )
}
</script>

<style scoped>
.beach-list-page {
  padding-top: 10px;
}
.beach-card {
  transition: transform 0.2s;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(0,0,0,.05) !important;
}
.beach-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 .5rem 1rem rgba(0,0,0,.15) !important;
}
.beach-image-placeholder {
  width: 100px;
  height: 100px;
  background-color: #f8f9fa;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: .25rem; }

.beach-image-placeholder > p {
  line-height: 1.2;
  padding: .2rem;
  font-size: .65rem !important;
}
.beach-image-placeholder > img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
  top: 0;
  left: 0;
  border-radius: .25rem;
}
.rating-badge {
  position: absolute;
  bottom: 5px;
  right: 5px;
  font-size: .75rem;
  z-index: 10;
}
.badge {
  font-size: .65rem;
  padding: .3em .6em;
}
.tab-btn-primary  {
  background-color: v-bind(mainColor) !important;
  border-color: v-bind(mainColor) !important;
}
.tab-btn-light-secondary {
  background-color: #ffffff !important;
  border-color: v-bind(mainColor) !important;
}
.dropdown-toggle {
  box-shadow: none !important;
}
.overflow-auto {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.overflow-auto::-webkit-scrollbar {
  display: none;
}
</style>