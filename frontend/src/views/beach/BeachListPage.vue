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
        <button :class="['btn', 'btn-sm', 'rounded-pill', activeTab === 'all' ? 'btn-primary' : 'btn-outline-secondary']" @click="activeTab = 'all'">전체</button>
        <button :class="['btn', 'btn-sm', 'rounded-pill', activeTab === 'favorite' ? 'btn-primary' : 'btn-outline-secondary']" @click="activeTab = 'favorite'">저장됨</button>
        <button :class="['btn', 'btn-sm', 'rounded-pill', activeTab === 'surfing' ? 'btn-primary' : 'btn-outline-secondary']" @click="activeTab = 'surfing'">서핑</button>
        <button :class="['btn', 'btn-sm', 'rounded-pill', activeTab === 'walking' ? 'btn-primary' : 'btn-outline-secondary']" @click="activeTab = 'walking'">산책</button>
        <button :class="['btn', 'btn-sm', 'rounded-pill', activeTab === 'family' ? 'btn-primary' : 'btn-outline-secondary']" @click="activeTab = 'family'">가족</button>
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
          <button type="button" :class="['btn', viewMode === 'list' ? 'btn-primary' : 'btn-light-secondary']" @click="viewMode = 'list'" :style="viewMode === 'list' ? primaryBtnStyle : {}">리스트</button>
          <button type="button" :class="['btn', viewMode === 'map' ? 'btn-primary' : 'btn-light-secondary']" @click="viewMode = 'map'" :style="viewMode === 'map' ? primaryBtnStyle : {}">지도</button>
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

      <!-- ✅ 여기만 수정됨 -->
      <div v-else>
        <div class="map-view d-flex align-items-center justify-content-center bg-light rounded-3 shadow-sm"
             style="height: 50vh; border: 1px solid #ccc;">
          <!-- 지도 들어갈 자리 -->
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia';

const store = useStore();
const { header } = storeToRefs(store);

const router = useRouter();
const beaches = ref([]);
const selectedBeachId = ref(null);
const favoriteBeachIds = ref([]);
const isLoading = ref(false);
const apiError = ref(null);

const FAVORITES_API_URL = 'http://localhost:8080/api/beach/favorites';
const BEACH_LIST_API_URL = 'http://localhost:8080/api/beach/beaches';

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

const searchParams = ref({
  region: '',
  keyword: '',
  sort: currentSort.value,
});

async function loadData() {
  isLoading.value = true;
  apiError.value = null;
  try {
    const response = await axios.post(BEACH_LIST_API_URL, searchParams.value);
    beaches.value = response.data?.result ?? [];
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
favoriteBeachIds.value = Array.isArray(resData) ? resData : resData && [resData] || [];



    console.log("⭐ 즐겨찾기 API 응답:", favoriteBeachIds.value);

  } catch (error) {
    console.error("즐겨찾기 초기 로딩 실패:", error);
    favoriteBeachIds.value = []; // 실패 시 빈 배열
  }
};

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


function selectRegion(region) {
  searchParams.value.region = region;
  loadData();
}
function selectSort(v) {
  currentSort.value = v;
}
onMounted(() => {
  loadData();
  fetchFavoriteIds();
  header.value = "해수욕장 리스트";
});
watch(currentSort, () => {
  searchParams.value.sort = currentSort.value;
  loadData();
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
</script>

<style scoped>
.beach-list-page { padding-top: 10px; }
.beach-card { transition: transform 0.2s; cursor: pointer; box-shadow: 0 4px 10px rgba(0,0,0,.05) !important; }
.beach-card:hover { transform: translateY(-5px); box-shadow: 0 .5rem 1rem rgba(0,0,0,.15) !important; }
.beach-image-placeholder { width: 100px; height: 100px; background-color: #f8f9fa; position: relative; display: flex; align-items: center; justify-content: center; border-radius: .25rem; }
.rating-badge { position: absolute; bottom: 5px; right: 5px; font-size: .75rem; z-index: 10; }
</style>
