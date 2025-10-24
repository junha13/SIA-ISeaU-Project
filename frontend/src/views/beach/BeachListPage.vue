<template>
  <div class="beach-list-page p-3">
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
            {{ searchParams.region || '-지역-' }}
            <i class="fas fa-chevron-down ms-2"></i>
          </button>
          <ul class="dropdown-menu">
            <li><a class="dropdown-item" href="#" @click.prevent="selectRegion('')">전체</a></li>
            <li><a class="dropdown-item" href="#" @click.prevent="selectRegion('강원')">강원</a></li>
            <li><a class="dropdown-item" href="#" @click.prevent="selectRegion('경기')">경기</a></li>
            <li><a class="dropdown-item" href="#" @click.prevent="selectRegion('인천')">인천</a></li>
            <li><a class="dropdown-item" href="#" @click.prevent="selectRegion('충청')">충청</a></li>
            <li><a class="dropdown-item" href="#" @click.prevent="selectRegion('전라')">전라</a></li>
            <li><a class="dropdown-item" href="#" @click.prevent="selectRegion('경상')">경상</a></li>
            <li><a class="dropdown-item" href="#" @click.prevent="selectRegion('부산')">부산</a></li>
            <li><a class="dropdown-item" href="#" @click.prevent="selectRegion('제주')">제주</a></li>
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
        <button
          :class="['btn', 'btn-sm', 'rounded-pill', activeTab === 'all' ? 'btn-primary' : 'btn-outline-secondary']"
          @click="activeTab = 'all'"
        >전체</button>
        <button
          :class="['btn', 'btn-sm', 'rounded-pill', activeTab === 'favorite' ? 'btn-primary' : 'btn-outline-secondary']"
          @click="activeTab = 'favorite'"
        >저장됨</button>
        <button
          :class="['btn', 'btn-sm', 'rounded-pill', activeTab === 'surfing' ? 'btn-primary' : 'btn-outline-secondary']"
          @click="activeTab = 'surfing'"
        >서핑</button>
        <button
          :class="['btn', 'btn-sm', 'rounded-pill', activeTab === 'walking' ? 'btn-primary' : 'btn-outline-secondary']"
          @click="activeTab = 'walking'"
        >산책</button>
        <button
          :class="['btn', 'btn-sm', 'rounded-pill', activeTab === 'family' ? 'btn-primary' : 'btn-outline-secondary']"
          @click="activeTab = 'family'"
        >가족</button>
      </div>

      <div class="d-flex align-items-center justify-content-between">
        <div class="dropdown me-3">
          <button class="btn btn-light-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
            {{ sortOptions.find(opt => opt.value === currentSort).label }}
          </button>
          <ul class="dropdown-menu">
            <li v-for="opt in sortOptions" :key="opt.value">
              <a class="dropdown-item" href="#" @click.prevent="selectSort(opt.value)">{{ opt.label }}</a>
            </li>
          </ul>
        </div>

        <div class="btn-group" role="group">
          <button
            type="button"
            :class="['btn', viewMode === 'list' ? 'btn-primary' : 'btn-light-secondary']"
            @click="viewMode = 'list'"
            :style="viewMode === 'list' ? primaryBtnStyle : {}"
          >리스트</button>
          <button
            type="button"
            :class="['btn', viewMode === 'map' ? 'btn-primary' : 'btn-light-secondary']"
            @click="viewMode = 'map'"
            :style="viewMode === 'map' ? primaryBtnStyle : {}"
          >지도</button>
        </div>
      </div>
    </div>

    <div class="mt-3">
      <div v-if="viewMode === 'list'">
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
          <div
            v-for="beach in filteredBeachList"
            :key="`${beach.beachNumber}-${beach.updatedAt || ''}`"
            class="beach-card card shadow-sm mb-4 rounded-3 border-0"
            @click="goToDetail(beach.beachNumber)"
          >
            <div class="card-body p-3 d-flex">
              <div class="beach-image-placeholder me-3 rounded-2" :style="{ border: '1px solid #eee' }">
                <img
                  v-if="beach.beachImage"
                  :src="beach.beachImage"
                  :alt="beach.beachName + ' 이미지'"
                  style="width: 100%; height: 100%; object-fit: cover; border-radius: 0.25rem;"
                />
                <p v-else class="text-center text-muted fw-bold mb-0 pt-2 fs-7">
                  {{ beach.beachImage === null ? '이미지 준비 중' : '이미지 없음' }}
                </p>
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
                  <span
                    v-for="(tag, index) in beach.tags"
                    :key="index"
                    :class="['badge', 'px-2', 'py-1', 'fw-bold', tagClass(tag)]"
                  >
                    {{ tag }}
                  </span>
                </div>

                <div class="d-flex justify-content-between align-items-center">
                  <p class="fs-7 mb-0 text-muted">{{ beach.distance }} 거리</p>

                  <button
                    v-if="isSelected(beach.beachNumber)"
                    class="btn btn-sm fw-bold"
                    :style="{ backgroundColor: mainColor, color: 'white' }"
                    @click.stop="toggleSelect(beach.beachNumber, beach.beachName)"
                  >선택됨</button>
                  <button
                    v-else
                    class="btn btn-sm btn-outline-secondary fw-bold"
                    @click.stop="toggleSelect(beach.beachNumber, beach.beachName)"
                  >선택하기</button>
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

import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'
const store = useStore();
const { header, beach } = storeToRefs(store)


const router = useRouter();
const beachStore = useBeachStore();

// 디자인
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

// 검색 파라미터
const searchParams = ref({
  region: '',
  keyword: '',
  sort: currentSort.value,
});

// 로드
async function loadData() {
  await beachStore.fetchBeaches(searchParams.value);
}
function selectRegion(region) {
  searchParams.value.region = region;
  loadData();
}
function selectSort(sortValue) {
  currentSort.value = sortValue;
}

// 최초/정렬 변경
onMounted(() => {
  loadData()
  header.value = "해수욕장 리스트"
});

watch(currentSort, (newSortValue) => {
  searchParams.value.sort = newSortValue;
  loadData();
  
});

// === 리스트 정리 로직 ===
function normalizeName(s) {
  return String(s || '').replace(/\s+/g, '').toLowerCase();
}

// 최신우선 uniq(id)
function uniqueByIdNewest(list) {
  if (!Array.isArray(list)) return [];
  const sorted = [...list].sort((a, b) => {
    const at = a?.updatedAt ?? a?.updated_at ?? 0;
    const bt = b?.updatedAt ?? b?.updated_at ?? 0;
    return bt - at;
  });
  const map = new Map();
  for (const item of sorted) {
    const id = item?.beachNumber ?? item?.beach_number ?? null;
    if (id == null) continue;
    if (!map.has(id)) map.set(id, item); // 최신이 먼저 들어오므로 첫 값 유지
  }
  return Array.from(map.values());
}

// 표준화된 목록
const dbOnlyList = computed(() => {
  const raw = Array.isArray(beachStore.beaches) ? beachStore.beaches : [];
  const uniq = uniqueByIdNewest(raw);
  return uniq.map(b => ({
    beachNumber:  b.beachNumber ?? b.beach_number ?? b.id ?? null,
    beachName:    b.beachName   ?? b.beach_name ?? '',
    beachImage:   b.beachImage  ?? b.beach_image ?? null,
    address:      b.address     ?? b.addr ?? '',
    rating:       Number(b.rating ?? b.rating_score ?? 0),
    updatedAt:    b.updatedAt   ?? b.updated_at ?? null,
    tags:         Array.isArray(b.tags) ? b.tags : [],
    distance:     b.distance ?? ''
  }));
});

// UI 필터(즐겨찾기 등)
const filteredBeachList = computed(() => {
  let list = dbOnlyList.value.slice();

  // 키워드/지역 필터 사용하려면 주석 해제
  // const kw = (searchParams.value.keyword || '').trim().toLowerCase();
  // if (kw) list = list.filter(b =>
  //   [b.beachName, b.address].some(t => (t || '').toLowerCase().includes(kw))
  // );
  // const region = (searchParams.value.region || '').trim();
  // if (region) list = list.filter(b =>
  //   (b.address || '').includes(region) || (b.beachName || '').includes(region)
  // );

  if (activeTab.value === 'favorite') {
    list = list.filter(b => beachStore.favoriteBeachIds.includes(b.beachNumber));
  }
  return list;
});

// 스토어 연동
const isSelected = (beachNumber) => beachStore.selectedBeachId === beachNumber;
const toggleSelect = (beachNumber, beachName) => beachStore.toggleSelectBeach(beachNumber, beachName);
const isFavorite = (beachNumber) => beachStore.favoriteBeachIds.includes(beachNumber);
const toggleFavorite = (beachNumber) => beachStore.toggleFavoriteBeach(beachNumber);
const goToDetail = (beachNumber) => router.push(`/beach/${beachNumber}`);

// 태그 스타일
const tagClass = (tag) => {
  switch (tag) {
    case '안전': return 'bg-secondary';
    case '수영':
    case '서핑': return 'bg-info';
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
.beach-list-page { padding-top: 10px; }
.beach-card { transition: transform 0.2s; cursor: pointer; box-shadow: 0 4px 10px rgba(0,0,0,.05) !important; }
.beach-card:hover { transform: translateY(-5px); box-shadow: 0 .5rem 1rem rgba(0,0,0,.15) !important; }
.beach-image-placeholder { width: 100px; height: 100px; background-color: #f8f9fa; position: relative; display: flex; align-items: center; justify-content: center; border-radius: .25rem; }
.beach-image-placeholder > p { line-height: 1.2; padding: .2rem; font-size: .65rem !important; }
.beach-image-placeholder > img { width: 100%; height: 100%; object-fit: cover; position: absolute; top: 0; left: 0; border-radius: .25rem; }
.rating-badge { position: absolute; bottom: 5px; right: 5px; font-size: .75rem; z-index: 10; }
.badge { font-size: .65rem; padding: .3em .6em; }
.btn-primary { background-color: v-bind(mainColor) !important; border-color: v-bind(mainColor) !important; }
.btn-outline-secondary { border-color: #ced4da !important; color: #6c757d !important; }
.dropdown-toggle { box-shadow: none !important; }
.overflow-auto { -ms-overflow-style: none; scrollbar-width: none; }
.overflow-auto::-webkit-scrollbar { display: none; }
</style>
