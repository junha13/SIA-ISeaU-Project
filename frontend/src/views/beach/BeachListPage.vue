<template>
  <div v-if="viewMode === 'list'" class="beach-list-page p-3">
    <div class="sticky-top bg-white mt-n1 p-1" style="z-index: 1000;">
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
            @keyup.enter="loadData" aria-label="해수욕장 검색"
            style="border-radius: 0.475rem 0 0 0.475rem;"
          />
          <button
            class="btn"
            type="button"
            @click="loadData" :style="{ backgroundColor: mainColor, color: 'white', border: 'none', borderTopLeftRadius: 0, borderBottomLeftRadius: 0 }"
          >
            <i class="fas fa-search"></i>
          </button>
        </div>
      </div>

      <div
      class="d-flex overflow-auto gap-2 mb-3 pb-2"
      style="white-space: nowrap;"
      >
      <button
      v-for="tab in tagFilterOptions"
      :key="tab.value"
      :class="[
      'btn',
      'btn-sm',
      'rounded-pill',
      activeTab === tab.value ? 'tab-btn-primary' : 'tab-btn-light-secondary'
      ]"
      @click="activeTab = tab.value"
      >
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
                  <span v-for="(tag, index) in beach.tags" :key="index" :class="['badge', 'px-3', 'py-2', 'fw-bold']" :style="tagClass(tag)" style="font-size:11px;">
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
import { storeToRefs } from 'pinia';
import { useBeachStore } from '@/stores/beachStore';

import bottomSheet from '@/components/BottomSheet.vue'


const store = useStore();
const { header, beach, tabOptions, sortOptions, regionOptions } = storeToRefs(store)
const beachStore = useBeachStore();

const router = useRouter();
const beaches = ref([]);
const { selectedBeachId } = storeToRefs(beachStore)
const favoriteBeachIds = ref([]);
const isLoading = ref(false);
const apiError = ref(null);

const FAVORITES_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/beach/favorites`;
const BEACH_LIST_API_URL = `${import.meta.env.VITE_API_BASE_URL}/api/beach/beaches`;

const mainColor = '#0092BA';
const darkColor = '#0B1956';

const activeTab = ref('all');
const viewMode = ref('list');
const currentSort = ref('name');

// ✅ 태그 필터링을 위한 고정 옵션
const tagFilterOptions = ref([
{ value: 'all', label: '전체' },
{ value: 'favorite', label: '즐겨찾기' },
{ value: '핫플', label: '핫플' },
{ value: '레저', label: '레저' },
{ value: '수영', label: '수영' },
{ value: '가족', label: '가족' },
{ value: '산책', label: '산책' },
{ value: '서핑', label: '서핑' },
{ value: '한적', label: '한적' },
{ value: '반려동물 동반', label: '반려동물 동반' },
]);

// ********** 데이터 로딩 및 필터링 로직 **********
// activeTab watch 로직: activeTab이 변경되면 loadData를 다시 호출하여 필터링합니다.
watch(activeTab, (newValue, oldValue) => {
if (newValue !== oldValue) {
// '즐겨찾기' 탭은 프론트엔드 필터링을 사용하므로 API 호출을 건너뜁니다.
if (newValue !== 'favorite') {
loadData();
}
}
});

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
  beaches.value = [];
  try {
    const backendSort = sortMap[currentSort.value] ?? 'name_asc';

      const payload = {
      region: searchParams.value.region || '',
      sort: backendSort,
      // 'all'이나 'favorite'이 아닐 때만 태그 필터를 백엔드에 전달
      tagFilter: (activeTab.value !== 'all' && activeTab.value !== 'favorite')
      ? activeTab.value
      : null,
      userLatitude: latitude.value,
      userLongitude: longitude.value,

      };

    const response = await axios.post(BEACH_LIST_API_URL, payload);
    
      beaches.value = (response.data.result || []).map(b => {
    // tagsString 필드가 DTO에 없거나 비어있는 경우를 대비하여 안전하게 배열로 변환
    const tagsList = b.tagsString
    ? b.tagsString.split(',').map(tag => tag.trim()).filter(tag => tag)
    : [];

    return {
    ...b,
    tags: tagsList,
    // distance 필드가 없으면 빈 문자열 할당 (템플릿 오류 방지)
    distance: b.distance || '',
    };
    }); 

  } catch (error) {
    apiError.value = error;
    console.error("😥 데이터 로딩 실패:", error);
  } finally {
    isLoading.value = false;
  }
}


const fetchFavoriteIds = async () => {
  try {
    const res = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/beach/favorites/my`);
    
    // ⭐ [수정 반영] 서버 응답 구조에 유연하게 대처합니다. (res.data.result 또는 res.data.data.result)

    let resData = res.data?.result || res.data?.data?.result;


    if (!resData) {
      resData = res.data?.data?.result;
    }
    
    // ID 목록 갱신: 리스트가 아닌 단일 값이 오더라도 배열로 만듭니다.
    favoriteBeachIds.value = Array.isArray(resData) ? resData : resData ? [resData] : [];
    
    // 👇👇👇 [로그 추가] 서버 응답 원본 확인 👇👇👇
    console.log('⭐ API 원본 응답:', res.data);
    console.log(`⭐ [성공] 즐겨찾기 ID ${favoriteBeachIds.value.length}개 로드 완료.`);
    if (favoriteBeachIds.value.length === 0) {
        console.log("⭐ (주의) 로드된 ID 목록이 비어 있습니다. (백엔드 확인 필요)");
    }
    // 👆👆👆 [로그 추가] 서버 응답 원본 확인 👆👆👆

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

async function toggleFavorite(beachNumber) {
  const isCurrentlyFavorite = favoriteBeachIds.value.includes(beachNumber);

  // 1. UI 상태 변경 (Optimistic Update)
  if (isCurrentlyFavorite) {
    favoriteBeachIds.value = favoriteBeachIds.value.filter(id => id !== beachNumber);
  } else {
    favoriteBeachIds.value.push(beachNumber);
  }

  // 2. API 요청
  try {
    if (isCurrentlyFavorite) {
      await axios.delete(`${import.meta.env.VITE_API_BASE_URL}/api/beach/favorites/${beachNumber}`);
    } else {
      await axios.post(FAVORITES_API_URL, { beachNumber });
    }
    
    // ⭐ [핵심] API 성공 후, ID 목록을 다시 가져와 UI를 강제 동기화합니다.
    await fetchFavoriteIds();
    
  } catch (error) {
    console.error("😥 즐겨찾기 토글 API 실패:", error);

    // 💡 409 Conflict에 대한 특정 메시지 처리 및 재동기화
    if (error.response && error.response.status === 409) {
      alert("이미 등록된 해수욕장입니다. 목록을 다시 불러옵니다.");
      await fetchFavoriteIds(); // DB 상태와 UI를 다시 맞춥니다.
      return; 
    }

    // 롤백 (일반 오류)
    if (isCurrentlyFavorite) {
      favoriteBeachIds.value.push(beachNumber);
    } else {
      favoriteBeachIds.value = favoriteBeachIds.value.filter(id => id !== beachNumber);
    }
    alert("즐겨찾기 변경 중 오류가 발생했습니다. 다시 시도해 주세요.");
  }
}
// ********** Computed 속성 및 기타 **********


const dbOnlyList = computed(() => beaches.value);
/**
 * 클라이언트 측 필터링: 서버에서 받은 전체 목록(beaches)에 대해 검색어와 탭 필터를 적용합니다.
 */
const filteredBeachList = computed(() => {
  const kw = (searchParams.value.keyword || '').trim().toLowerCase();
  let list = dbOnlyList.value;

  // 1. 검색어 필터링 (프론트엔드)
  if (kw) {
    list = list.filter(b => {
      const name = (b.beachName || '').toLowerCase();
      const addr = (b.address || '').toLowerCase();
      const tags = Array.isArray(b.tags) ? b.tags.join(' ').toLowerCase() : '';
      return name.includes(kw) || addr.includes(kw) || tags.includes(kw);
    });
  }

  // 2. 탭 필터링 (프론트엔드)
  if (activeTab.value === 'favorite') {
    list = list.filter(b => favoriteBeachIds.value.includes(b.beachNumber));
  }
  return list;
});

const
isSelected = id => beachStore.isSelected(id);
const
toggleSelect = (id, name) => beachStore.toggleSelectBeach(id, name);
const
isFavorite = id => favoriteBeachIds.value.includes(id);
const
goToDetail = id => router.push(`/beach/${id}`);


// 태그에 따라 색상 클래스 지정
const tagClass = (tag) => {
  const palette = {
    '핫플':           ['#FDE2E4', '#7A1D2F'], // 연한 레드
    '레저':           ['#D7F3F7', '#0B5E65'], // 연한 시안/민트
    '수영':           ['#D6E9FF', '#0B4F8A'], // 연한 블루
    '가족':           ['#DFF5E1', '#1B5E20'], // 연한 그린
    '산책':           ['#FFF6D1', '#7A5E00'], // 연한 옐로
    '서핑':           ['#F1F3F5', '#343A40'], // 라이트 그레이
    '한적':           ['#F8FAFC', '#111827'], // 아주 연한 슬레이트
    '반려동물 동반':   ['#EAEAEA', '#1F2937'], // 밝은 그레이
  };
  const [bg, fg] = palette[tag] || ['#F3F4F6', '#111827']; // 기본값
  return { backgroundColor: bg, color: fg };

};
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
    (err) => { console.error('위치 실패: ' + err.message); },
    { enableHighAccuracy: true }
  )
}
</script>

<style scoped>
.beach-card .fa-heart.text-danger {
    color: var(--bs-danger, #dc3545) !important; 
}
.beach-card .fa-heart.text-muted {
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

.beach-image-placeholder {
  width: 100px;
  height: 100px;
  min-width: 100px;
  min-height: 100px;
  flex: 0 0 100px; /* flex-basis 고정 + shrink 방지 */
}
</style>