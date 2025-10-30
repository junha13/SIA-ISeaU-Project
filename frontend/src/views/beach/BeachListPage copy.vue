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
            {{ sortOptions.find(opt => opt.value === currentSort).label ?? '이름순'}}
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
            :key="beach.beachNumber"  
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

          <!-- 🔻 무한 스크롤 컴포넌트 -->
          <InfiniteLoading
            :identifier="infiniteId"
            @infinite="infiniteHandler">
            <template #spinner>{{ blank }} </template>
            <template #complete>{{ blank }}</template>
            <template #error="{ retry }">
              <div class="text-center py-3">
                로딩 실패 <button class="btn btn-sm btn-outline-secondary ms-2" @click="retry()">다시시도</button>
              </div>
            </template>
          </InfiniteLoading>

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
        <bottom-sheet>
          <div>바텀시트 추가</div>
        </bottom-sheet>
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
//import InfiniteLoading from 'infinite-loading-vue3-ts'


import bottomSheet from '@/components/BottomSheet.vue'


const blank = ""

const store = useStore();
const { header, beach, tabOptions, sortOptions, regionOptions } = storeToRefs(store)

const router = useRouter();
const beaches = ref([]);
const selectedBeachId = ref(null);
const favoriteBeachIds = ref([]);
const isLoading = ref(false);
const apiError = ref(null);

// ▶ 무한스크롤 상태
const page = ref(1);                   // 다음에 요청할 페이지
const size = ref(10);                  // 페이지 크기(백엔드와 동일)
const hasMore = ref(true);             // 더 가져올 수 있는지
const infiniteId = ref(0);             // 변경되면 InfiniteLoading이 초기화됨

const FAVORITES_API_URL = 'http://localhost:8080/api/beach/favorites';
const BEACH_LIST_API_URL = 'http://localhost:8080/api/beach/beaches';

const mainColor = '#0092BA';
const darkColor = '#0B1956';

const activeTab = ref('all');
const viewMode = ref('list');
const currentSort = ref('name');

const tabCondition = tabOptions
const sortCondition = sortOptions
const regionCondition = regionOptions

const primaryBtnStyle = { backgroundColor: mainColor, borderColor: mainColor, color: 'white' };
const dropdownBtnStyle = { backgroundColor: '#f8f9fa', borderColor: '#ced4da', color: darkColor };

const searchParams = ref({
  region: '',
  keyword: '',
  sort: currentSort.value,
});

const sortMap = {
  name: 'name_asc',
  distance: 'distance_asc',
  review: 'review_desc',
  rating: 'rating_desc',
};

onMounted(() => {
  loadData();
  fetchFavoriteIds();
  header.value = "해수욕장 리스트"
  getLocation();
});

async function loadData() {
  isLoading.value = true;
  apiError.value = null;
  try {
    const backendSort = sortMap[currentSort.value] ?? 'name_asc';

    // 백엔드 DTO(BeachListRequest)에 맞춰 최소 필드만 전송
    const payload = {
      region: searchParams.value.region || '',
      sort: backendSort,
      // keyword는 백에서 아직 안 받는 듯 → 받게 되면 여기에 추가
    };

    const response = await axios.post(BEACH_LIST_API_URL, payload);
    beaches.value = response.data.result
  } catch (error) {
    apiError.value = error;
  } finally {
    isLoading.value = false;
  }
}


const fetchFavoriteIds = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/beach/favorites/my');
    const resData = res.data?.data?.result;
    favoriteBeachIds.value = Array.isArray(resData) ? resData : resData ? [resData] : [];
    console.log("⭐ 즐겨찾기 API 응답:", favoriteBeachIds.value);
  } catch (error) {
    console.error("즐겨찾기 초기 로딩 실패:", error);
    favoriteBeachIds.value = [];
  }
};

function selectRegion(region) {
  searchParams.value.region = region;
  loadData();
}

function selectSort(sortValue) {
  currentSort.value = sortValue;
  searchParams.value.sort = sortValue;
  loadData();
}

// 📥 무한스크롤 핸들러
async function infiniteHandler($state) {
  if (!hasMore.value) {
    $state.complete();
    return;
  }
  try {
    const backendSort = sortMap[currentSort.value] ?? 'name_asc';
    const payload = {
      region: searchParams.value.region || '',
      sort: backendSort,
      page: page.value,
      size: size.value,
      // keyword: searchParams.value.keyword, // 백에서 받으면 주석 해제
    };

    const res = await axios.post(BEACH_LIST_API_URL, payload);
    const list = res.data?.result ?? [];

    // 중복 방지 후 추가
    const existing = new Set(beaches.value.map(b => b.beachNumber));
    const toAdd = list.filter(b => !existing.has(b.beachNumber));

    await new Promise(r => setTimeout(r, 600)); // 👈 지연 딜레이 0.6초

    if (toAdd.length) beaches.value.push(...toAdd);

    // 다음 페이지 계산
    hasMore.value = !!res.data?.hasMore && list.length > 0;
    page.value = res.data?.nextPage ?? (page.value + 1);

    // v3-infinite-loading 상태 업데이트
    if (hasMore.value) $state.loaded();
    else $state.complete();

    // 결과가 하나도 없고 처음 요청이라면 완료 처리
    if (!hasMore.value && beaches.value.length === 0) $state.complete();
  } catch (err) {
    apiError.value = err;
    $state.error();
  }
}

// [수정] 즐겨찾기 토글 (console.log 추가)
async function toggleFavorite(beachNumber) {
  const isCurrentlyFavorite = favoriteBeachIds.value.includes(beachNumber);

  // 1. UI 상태 먼저 변경!
  if (isCurrentlyFavorite) {
    favoriteBeachIds.value = favoriteBeachIds.value.filter(id => id !== beachNumber);
    console.log('💔 즐겨찾기 제거 (UI):', JSON.stringify(favoriteBeachIds.value)); // <-- 로그 추가 (배열 내용 확인)
  } else {
    favoriteBeachIds.value.push(beachNumber);
    console.log('💖 즐겨찾기 추가 (UI):', JSON.stringify(favoriteBeachIds.value)); // <-- 로그 추가 (배열 내용 확인)
  }

  // 2. API 요청 보내기
  try {
    if (isCurrentlyFavorite) {
      await axios.delete(`${FAVORITES_API_URL}/${beachNumber}`);
      console.log(`⭐ ${beachNumber} 즐겨찾기 삭제 성공`);
    } else {
      await axios.post(FAVORITES_API_URL, { beachNumber });
      console.log(`⭐ ${beachNumber} 즐겨찾기 추가 성공`);
    }
  } catch (error) {
    console.error("😥 즐겨찾기 토글 API 실패:", error);

    // 3. API 실패 시, UI 상태 원래대로 되돌리기!
    if (isCurrentlyFavorite) {
      favoriteBeachIds.value.push(beachNumber);
      console.log('롤백: 즐겨찾기 다시 추가 (UI)', JSON.stringify(favoriteBeachIds.value)); // <-- 롤백 로그
    } else {
      favoriteBeachIds.value = favoriteBeachIds.value.filter(id => id !== beachNumber);
      console.log('롤백: 즐겨찾기 다시 제거 (UI)', JSON.stringify(favoriteBeachIds.value)); // <-- 롤백 로그
    }
    alert("즐겨찾기 변경 중 오류가 발생했습니다. 다시 시도해 주세요.");
  }
}

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

// [수정] isFavorite 함수 (console.log 추가)
const isFavorite = id => {
  const result = favoriteBeachIds.value.includes(id);
  // console.log(`isFavorite(${id}) 호출됨, 결과: ${result}`); // <-- 로그 추가 (너무 많이 찍힐 수 있음)
  return result;
};

const goToDetail = id => router.push(`/beach/${id}`);
const tagClass = tag => ({
  '안전': 'bg-secondary', '수영': 'bg-info', '서핑': 'bg-info',
  '산책': 'bg-warning', '가족': 'bg-success',
}[tag] || 'bg-light text-dark');

// 지도 부분 (변경 없음)
const beachMap = ref(null)
let map
let markers = []
const latitude = ref('')
const longitude = ref('')

watch(viewMode, (mode) => {
  if (mode !== 'map') { map = null; markers = []; }
})

watchEffect(() => {
  const lat = latitude.value
  const lng = longitude.value
  if (viewMode.value !== 'map' || !lat || !lng || !beachMap.value || !window.naver?.maps) return
  const list = beaches.value
  if (!map) {
    const center = new window.naver.maps.LatLng(lat, lng)
    map = new window.naver.maps.Map(beachMap.value, { center, zoom: 15 })
  }
  markers.forEach(m => m.setMap(null))
  markers = []
  list.forEach(b => {
    if (!b.latitude || !b.longitude) return
    const pos = new window.naver.maps.LatLng(b.latitude, b.longitude)
    const m = new window.naver.maps.Marker({ position: pos, map, title: b.beachName })
    markers.push(m)
  })
})

function getLocation() {
  if (!navigator.geolocation) return;
  navigator.geolocation.getCurrentPosition(
    (pos) => { latitude.value = pos.coords.latitude; longitude.value = pos.coords.longitude; },
    (err) => { console.error('위치 실패: ' + err.message); }, // err.value 대신 console.error 사용
    { enableHighAccuracy: true }
  )
}
</script>

<style scoped>
.beach-card .fa-heart.text-danger {
    /* 빨간색을 강제로 적용 */
    color: var(--bs-danger, #dc3545) !important; 
    /* Bootstrap 변수 사용 또는 #dc3545 같은 hex 값 사용 */
}
.beach-card .fa-heart.text-muted {
    /* 회색을 강제로 적용 */
    color: var(--bs-gray-600, #6c757d) !important;
}
.beach-list-page { padding-top: 10px; }
.beach-card { transition: transform 0.2s; cursor: pointer; box-shadow: 0 4px 10px rgba(0,0,0,.05) !important; }
.beach-card:hover { transform: translateY(-5px); box-shadow: 0 .5rem 1rem rgba(0,0,0,.15) !important; }
.beach-image-placeholder { width: 100px; height: 100px; background-color: #f8f9fa; position: relative; display: flex; align-items: center; justify-content: center; border-radius: .25rem; }
.beach-image-placeholder > p { line-height: 1.2; padding: .2rem; font-size: .65rem !important; }
.beach-image-placeholder > img { width: 100%; height: 100%; object-fit: cover; position: absolute; top: 0; left: 0; border-radius: .25rem; }
.rating-badge { position: absolute; bottom: 5px; right: 5px; font-size: .75rem; z-index: 10; }
.badge { font-size: .65rem; padding: .3em .6em; }
.tab-btn-primary { background-color: v-bind(mainColor) !important; border-color: v-bind(mainColor) !important; color: white !important; }
.tab-btn-light-secondary { background-color: #ffffff !important; border-color: #ced4da !important; color: #6c757d !important; }
.dropdown-toggle { box-shadow: none !important; }
.overflow-auto { -ms-overflow-style: none; scrollbar-width: none; }
.overflow-auto::-webkit-scrollbar { display: none; }

.scroll-box {
  height: 400px;
  overflow-y: auto;
  border: 1px solid #ccc;
}
.item {
  padding: 8px;
  border-bottom: 1px solid #eee;
}
.loading {
  text-align: center;
  padding: 12px;
}
</style>