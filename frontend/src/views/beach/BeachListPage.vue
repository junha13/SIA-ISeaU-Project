<template>
  <div class="beach-list-page p-3">

    <div class="sticky-top bg-white pt-1 pb-3" style="z-index: 900;">
      <div class="d-flex align-items-center mb-3">
        <div class="dropdown me-2">
          <button class="btn btn-secondary dropdown-toggle d-flex align-items-center" type="button"
                  data-bs-toggle="dropdown" aria-expanded="false"
                  :style="dropdownBtnStyle">
            {{ searchParams.region || '-지역-' }}
            <i class="fas fa-chevron-down ms-2"></i>
          </button>
          <ul class="dropdown-menu">
            <li><a class="dropdown-item" href="#" @click.prevent="selectRegion('')">전체</a></li>
            <li><a class="dropdown-item" href="#" @click.prevent="selectRegion('부산')">부산</a></li>
            <li><a class="dropdown-item" href="#" @click.prevent="selectRegion('강원')">강원</a></li>
          </ul>
        </div>

        <div class="input-group flex-grow-1">
          <input type="text" class="form-control" placeholder="해수욕장 검색..."
                 v-model="searchParams.keyword" @keyup.enter="loadData"
                 aria-label="해수욕장 검색" style="border-radius: 0.475rem 0 0 0.475rem;">
          <button class="btn" type="button" @click="loadData" :style="{ backgroundColor: mainColor, color: 'white', border: 'none', borderTopLeftRadius: 0, borderBottomLeftRadius: 0 }">
            <i class="fas fa-search"></i>
          </button>
        </div>
      </div>

      <div class="d-flex overflow-auto gap-2 mb-3 pb-2" style="white-space: nowrap;">
        <button :class="['btn', 'btn-sm', 'rounded-pill', activeTab === 'all' ? 'btn-primary' : 'btn-outline-secondary']"
                @click="activeTab = 'all'">전체</button>
        <button :class="['btn', 'btn-sm', 'rounded-pill', activeTab === 'favorite' ? 'btn-primary' : 'btn-outline-secondary']"
                @click="activeTab = 'favorite'">저장됨</button>
        <button :class="['btn', 'btn-sm', 'rounded-pill', activeTab === 'surfing' ? 'btn-primary' : 'btn-outline-secondary']"
                @click="activeTab = 'surfing'">서핑</button>
        <button :class="['btn', 'btn-sm', 'rounded-pill', activeTab === 'walking' ? 'btn-primary' : 'btn-outline-secondary']"
                @click="activeTab = 'walking'">산책</button>
        <button :class="['btn', 'btn-sm', 'rounded-pill', activeTab === 'family' ? 'btn-primary' : 'btn-outline-secondary']"
                @click="activeTab = 'family'">가족</button>
      </div>

      <div class="d-flex align-items-center justify-content-between">
        <div class="dropdown me-3">
          <button class="btn btn-light-secondary dropdown-toggle" type="button"
                  data-bs-toggle="dropdown" aria-expanded="false">
            {{ sortOptions.find(opt => opt.value === currentSort).label }}
          </button>
          <ul class="dropdown-menu">
            <li v-for="opt in sortOptions" :key="opt.value">
              <a class="dropdown-item" href="#" @click.prevent="selectSort(opt.value)">{{ opt.label }}</a>
            </li>
          </ul>
        </div>

        <div class="btn-group" role="group">
          <button type="button" :class="['btn', viewMode === 'list' ? 'btn-primary' : 'btn-light-secondary']"
                  @click="viewMode = 'list'" :style="viewMode === 'list' ? primaryBtnStyle : {}">
            리스트
          </button>
          <button type="button" :class="['btn', viewMode === 'map' ? 'btn-primary' : 'btn-light-secondary']"
                  @click="viewMode = 'map'" :style="viewMode === 'map' ? primaryBtnStyle : {}">
            지도
          </button>
        </div>
      </div>
    </div>

    <div class="mt-3">
      <div v-if="viewMode === 'list'">
        <!-- [로직 수정] 로딩 및 에러 상태를 store와 연동 -->
        <div v-if="beachStore.isLoading" class="text-center p-5">
            <i class="fas fa-spinner fa-spin me-2"></i> 목록을 불러오는 중...
        </div>
        <div v-else-if="beachStore.apiError" class="text-center text-danger p-5">
            목록을 불러오는 중 오류가 발생했습니다.
        </div>
        <div v-else-if="!filteredBeachList.length" class="text-center text-muted p-5">
            표시할 해수욕장이 없습니다.
        </div>
        <div v-else>
            <div v-for="beach in filteredBeachList" :key="beach.beachNumber" class="beach-card card shadow-sm mb-4 rounded-3 border-0" @click="goToDetail(beach.beachNumber)">
              <!-- [디자인 복구] v-for 내부의 카드 디자인은 보내주신 원본과 동일합니다 -->
              <div class="card-body p-3 d-flex">
                <div class="beach-image-placeholder me-3 rounded-2"
                     :style="{ border: '1px solid #eee' }">
                  <img v-if="beach.beachImage"
                       :src="beach.beachImage"
                       :alt="beach.beachName + ' 이미지'"
                       style="width: 100%; height: 100%; object-fit: cover; border-radius: 0.25rem;">

                  <p v-else class="text-center text-muted fw-bold mb-0 pt-2 fs-7">
                    {{ beach.beachImage === null ? '이미지 준비 중' : '이미지 없음' }}
                  </p>
                  <div class="rating-badge badge text-white px-2 py-1 rounded-pill" :style="{ backgroundColor: mainColor }">
                    <i class="fas fa-star fs-7"></i> {{ beach.rating.toFixed(1) }}
                  </div>
                </div>

                <div class="beach-info flex-grow-1">
                  <div class="d-flex justify-content-between align-items-start">
                    <h5 class="fw-bolder fs-6 mb-1" :style="{ color: darkColor }">{{ beach.beachName }}</h5>
                    <i :class="['fas fa-heart fs-5', { 'text-danger': isFavorite(beach.beachNumber), 'text-muted': !isFavorite(beach.beachNumber) }]"
                       @click.stop="toggleFavorite(beach.beachNumber)"
                       style="cursor: pointer;"></i>
                  </div>

                  <p class="text-muted fs-7 mb-2">{{ beach.address }}</p>

                  <div class="d-flex gap-2 mb-3">
                    <span v-for="(tag, index) in beach.tags" :key="index"
                          :class="['badge', 'px-2', 'py-1', 'fw-bold', tagClass(tag)]">
                      {{ tag }}
                    </span>
                  </div>

                  <div class="d-flex justify-content-between align-items-center">
                    <p class="fs-7 mb-0 text-muted">{{ beach.distance }} 거리</p>

                    <button v-if="isSelected(beach.beachNumber)"
                            class="btn btn-sm fw-bold"
                            :style="{ backgroundColor: mainColor, color: 'white' }"
                            @click.stop="toggleSelect(beach.beachNumber, beach.beachName)">
                      선택됨
                    </button>
                    <button v-else
                            class="btn btn-sm btn-outline-secondary fw-bold"
                            @click.stop="toggleSelect(beach.beachNumber, beach.beachName)">
                      선택하기
                    </button>
                  </div>
                </div>
              </div>
            </div>
        </div>
      </div>

      <div v-else>
        <div class="map-view d-flex align-items-center justify-content-center bg-light rounded-3 shadow-sm"
             style="height: 50vh; border: 1px solid #ccc;">
            <!-- <NMarker
           :map="map"
           :lat="beach.latitude"   
           :lng="beach.longitude" /> -->
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useBeachStore } from '@/stores/beachStore.js';
import NMarker from '@/components/NaverMapMarker.vue'
// [로직 수정] useApi와 beachApi 직접 import 제거
// import { useApi } from '@/utils/useApi.js';
// import { beachApi } from '@/api/beach.js';

const router = useRouter();
const beachStore = useBeachStore();

// --- 디자인 관련 상태 (원본과 동일) ---
const mainColor = '#0092BA';
const darkColor = '#0B1956';
const activeTab = ref('all');
const viewMode = ref('list');
const currentSort = ref('distance');
const sortOptions = [
  { label: '거리순', value: 'distance' },
  { label: '리뷰순', value: 'review' },
  { label: '평점순', value: 'rating' },
];
const primaryBtnStyle = { backgroundColor: mainColor, borderColor: mainColor, color: 'white' };
const dropdownBtnStyle = { backgroundColor: '#f8f9fa', borderColor: '#ced4da', color: darkColor };

// --- [로직 수정] 데이터 로딩을 위한 상태 ---
const searchParams = ref({
  region: '',
  keyword: '',
  sort: currentSort.value,
});

// [로직 수정] 데이터를 불러오는 함수를 스토어 호출로 통일
async function loadData() {
  await beachStore.fetchBeaches(searchParams.value);
}

// [로직 수정] 사용자 인터랙션 함수들
function selectRegion(region) {
    searchParams.value.region = region;
    loadData();
}
function selectSort(sortValue) {
    currentSort.value = sortValue;
    // watch가 감지하도록 currentSort만 변경
}

// [로직 수정] 컴포넌트가 처음 로드될 때 및 정렬 기준 변경 시 데이터 로드
onMounted(loadData);
watch(currentSort, (newSortValue) => {
    searchParams.value.sort = newSortValue;
    loadData();
});

// --- [로직 수정] Computed 속성 ---
const filteredBeachList = computed(() => {
  // 이제 API 호출 결과인 beachList 대신, 스토어의 상태(beachStore.beaches)를 사용
  let list = (beachStore.beaches || []).slice();

  if (activeTab.value === 'favorite') {
    return list.filter(beach => isFavorite(beach.beachNumber));
  }
  // (필요 시 다른 탭 필터링 로직 추가)

  return list;
});

// --- 스토어 연동 함수 (원본과 동일) ---
const isSelected = (beachNumber) => beachStore.selectedBeachId === beachNumber;
const toggleSelect = (beachNumber, beachName) => beachStore.toggleSelectBeach(beachNumber, beachName);
const isFavorite = (beachNumber) => beachStore.favoriteBeachIds.includes(beachNumber);
const toggleFavorite = (beachNumber) => beachStore.toggleFavoriteBeach(beachNumber);
const goToDetail = (beachNumber) => {
  // [수정] router.push by name 대신 by path를 사용하여 라우터 설정 파일의 의존성을 제거합니다.
  router.push(`/beach/${beachNumber}`);
};

// 태그 스타일링 함수 (원본과 동일)
const tagClass = (tag) => {
  switch (tag) {
    case '안전': return 'bg-secondary';
    case '수영': case '서핑': return 'bg-info';
    case '산책': return 'bg-warning';
    case '가족': return 'bg-success';
    default: return 'bg-light text-dark';
  }
};

//=============================================
// const mapEl = ref(null)
// let map // 한 번만 생성

// // init: 네이버 지도 SDK가 로드된 뒤 1회 실행.
// // Pinia의 beach 좌표로 지도 생성하고 마커 1개 표시합니다.
// function initMap() {
//   if (!mapEl.value || !window.naver?.maps) return
//   if (map) {
//     // 탭 전환 등으로 숨겼다가 보일 때 크기만 보정
//     map.setSize(new window.naver.maps.Size(mapEl.value.clientWidth, mapEl.value.clientHeight))
//     return
//   }
//   const center = new window.naver.maps.LatLng(37.5665, 126.9780) // 필요하면 원하는 좌표로
//   map = new window.naver.maps.Map(mapEl.value, { center, zoom: 12 })
// }

// // onMounted: 컴포넌트가 DOM에 붙은 직후 1번 실행.
// onMounted(() => {
//   // SDK 없으면 로드, 있으면 바로 시도
//   if (!window.naver?.maps) {
//     const s = document.createElement('script')
//     s.src = `https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${import.meta.env.VITE_NAVER_CLIENT_ID}`
//     s.onload = () => setTimeout(() =>{ if (viewMode.value === 'map') initMap() }, 300)
//     document.head.appendChild(s)
//   } else {
//     if (viewMode.value === 'map') initMap()
//   }
// })

// // '지도' 탭으로 바뀌는 순간에만 생성/보정
// watch(viewMode, v => { if (v === 'map') setTimeout(initMap,150) })


</script>

<style scoped>
/* 제공해주신 모든 스타일을 그대로 유지합니다 */
.beach-list-page {
  padding-top: 10px;
}
.beach-card {
  transition: transform 0.2s;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05) !important;
}
.beach-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15) !important;
}
.beach-image-placeholder {
  width: 100px;
  height: 100px;
  background-color: #f8f9fa;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 0.25rem; /* rounded-2 와 유사 */
}
.beach-image-placeholder > p {
    line-height: 1.2;
    padding: 0.2rem;
    font-size: 0.65rem !important;
}
.beach-image-placeholder > img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    position: absolute; 
    top: 0;
    left: 0;
    border-radius: 0.25rem;
}
.rating-badge {
  position: absolute;
  bottom: 5px;
  right: 5px;
  font-size: 0.75rem;
  z-index: 10;
}
.badge {
  font-size: 0.65rem;
  padding: 0.3em 0.6em;
}
.btn-primary {
  background-color: v-bind(mainColor) !important;
  border-color: v-bind(mainColor) !important;
}
.btn-outline-secondary {
  border-color: #ced4da !important;
  color: #6c757d !important;
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
