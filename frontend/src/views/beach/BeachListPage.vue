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
                 aria-label="해수욕장 검색" style="border-radius: 0.475rem 0 0 0.475rem;">
          <button class="btn" type="button" :style="{ backgroundColor: mainColor, color: 'white', border: 'none', borderTopLeftRadius: 0, borderBottomLeftRadius: 0 }">
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
              <a class="dropdown-item" href="#" @click.prevent="currentSort = opt.value">{{ opt.label }}</a>
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
        <div v-for="beach in filteredBeachList" :key="beach.beachNumber" class="beach-card card shadow-sm mb-4 rounded-3 border-0" @click="goToDetail(beach.beachNumber)">
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

      <div v-else>
        <div class="map-view d-flex align-items-center justify-content-center bg-light rounded-3 shadow-sm"
             style="height: 50vh; border: 1px solid #ccc;">
          <p class="text-muted fw-bold fs-5 mb-0">내 지역 주변 해수욕장 지도 표시</p>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useBeachStore } from '@/stores/beachStore.js';
import { useApi } from '@/utils/useApi.js';
import { beachApi } from '@/api/beach.js';


const router = useRouter();
const beachStore = useBeachStore();

// --- Color Definitions (수정 없음) ---
const mainColor = '#0092BA';
const darkColor = '#0B1956';
const safetyColor = '#8482FF';
const cautionColor = '#FFB354';
const dangerColor = '#EB725B';

// --- State (수정 없음) ---
const activeTab = ref('all');
const viewMode = ref('list');
const currentSort = ref('distance');

const sortOptions = [
  { label: '거리순', value: 'distance' },
  { label: '리뷰순', value: 'review' },
  { label: '평점순', value: 'rating' },
];

// --- Styles (수정 없음) ---
const primaryBtnStyle = {
  backgroundColor: mainColor,
  borderColor: mainColor,
  color: 'white'
};
const dropdownBtnStyle = {
  backgroundColor: '#f8f9fa',
  borderColor: '#ced4da',
  color: darkColor,
};

// --- API 데이터 로딩 ---
const { data: beachList, error, loading, execute: fetchBeaches } = useApi(
  beachApi.fetchBeachList.method, 
  beachApi.fetchBeachList.url
);
const searchParams = ref({
  region: '', // ⭐ 초기값: 빈 문자열로 설정하여 전체 지역 조회
  sort: currentSort.value,
});

// 3. 데이터를 불러오는 함수
async function loadData() {
  try {
    searchParams.value.sort = currentSort.value; // 정렬 기준 업데이트
    await fetchBeaches(searchParams.value); 
    if (error.value) {
        console.error("해수욕장 목록 로딩 중 API 오류:", error.value);
    }
  } catch (err) {
    console.error("해수욕장 목록 로딩 실패:", err);
  }
}

// ⭐⭐ 지역 선택 함수 추가 ⭐⭐
function selectRegion(region) {
    searchParams.value.region = region; // 선택된 지역으로 파라미터 업데이트
    loadData(); // 데이터 새로고침
}

// 4. 컴포넌트가 마운트될 때 데이터를 불러옵니다.
onMounted(loadData);

// ⭐⭐ 정렬 기준이 변경될 때마다 데이터를 다시 로드하는 watch 추가 ⭐⭐
watch(currentSort, () => {
    loadData();
});

// --- Computed & Methods ---
const filteredBeachList = computed(() => {
 // [수정] 
 // 1. beachList.value?.result : beachList가 null일 때 오류 방지 (Optional Chaining)
 // 2. || [] : .result가 없거나 null일 경우 빈 배열([])을 기본값으로 사용
 // 3. .slice() : 원본 배열을 복사
 let list = (beachList.value?.result || []).slice();

 // 1. 탭 필터링 (이하 로직 동일)
 if (activeTab.value === 'favorite') {
   list = list.filter(beach => beachStore.getFavoriteBeachIds.includes(beach.beachNumber));
 } else if (activeTab.value !== 'all') {
   list = list.filter(beach => beach.tags.includes(activeTab.value.charAt(0).toUpperCase() + activeTab.value.slice(1)));
 }

 // 2. 정렬 (이하 로직 동일)
 list.sort((a, b) => {
   if (currentSort.value === 'distance') {
     const distA = parseFloat(a.distance);
     const distB = parseFloat(b.distance);
     return distA - distB;
   } else if (currentSort.value === 'review') {
     return b.reviewCount - a.reviewCount;
   } else if (currentSort.value === 'rating') {
     return b.rating - a.rating;
   }
   return 0;
 });

 return list;
});

/**
 * 해수욕장 선택 상태 확인
 */
// [수정 9] beachId -> beachNumber
const isSelected = (beachNumber) => {
  return beachStore.getCurrentSelectedBeachId === beachNumber;
};

/**
 * 해수욕장 선택 토글 액션 호출
 */
// [수정 10] beachId -> beachNumber
const toggleSelect = (beachNumber, beachName) => {
  beachStore.toggleSelectBeach(beachNumber, beachName);
};

/**
 * 즐겨찾기 상태 확인
 */
// [수정 11] beachId -> beachNumber
const isFavorite = (beachNumber) => {
  return beachStore.getFavoriteBeachIds.includes(beachNumber);
};

/**
 * 즐겨찾기 토글 액션 호출
 */
// [수정 12] beachId -> beachNumber
const toggleFavorite = (beachNumber) => {
  beachStore.toggleFavoriteBeach(beachNumber);
};

/**
 * 상세 페이지로 이동
 */

const goToDetail = (beachNumber) => {
  // [수정 14] params: { id: beachId } -> params: { id: beachNumber }
  // (참고: 라우터 파라미터 이름이 'id'라면 { id: beachNumber }가 맞습니다)
  router.push({ name: 'BeachDetail', params: { id: beachNumber } });
}


const tagClass = (tag) => {
  switch (tag) {
    case '안전':
      return 'bg-secondary';
    case '수영':
    case '서핑':
      return 'bg-info';
    case '산책':
      return 'bg-warning';
    case '가족':
      return 'bg-success';
    default:
      return 'bg-light text-dark';
  }
};
</script>

<style scoped>
/* List Page Custom Styles */
.beach-list-page {
  padding-top: 10px;
}

/* 해수욕장 카드 스타일 */
.beach-card {
  transition: transform 0.2s;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05) !important;
}

/* 해변 이미지 Placeholder */
.beach-image-placeholder {
  width: 100px;
  height: 100px;
  background-color: #f8f9fa;
  position: relative;
  /* 이미지를 담을 수 있도록 기본 스타일 추가 */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* 플레이스홀더 내부의 p 태그 스타일 (이미지가 없을 때) */
.beach-image-placeholder > p {
    line-height: 1.2;
    padding: 0.2rem;
    font-size: 0.65rem !important; /* 글씨 크기 조정 */
}

/* 이미지 스타일 (추가) */
.beach-image-placeholder > img {
    /* 이미지가 div 크기에 맞도록 조정 */
    width: 100%;
    height: 100%;
    object-fit: cover;
    position: absolute; 
    top: 0;
    left: 0;
}

/* 평점 배지 오버레이 */
.rating-badge {
  position: absolute;
  bottom: 5px;
  right: 5px;
  font-size: 0.75rem;
  z-index: 10; /* 이미지 위에 오도록 z-index 추가 */
}

/* 태그 배지 공통 스타일 */
.badge {
  font-size: 0.65rem;
  padding: 0.3em 0.6em;
}

/* 탭 버튼 색상 커스텀 */
.btn-primary {
  background-color: v-bind(mainColor) !important;
  border-color: v-bind(mainColor) !important;
}
.btn-outline-secondary {
  border-color: #ced4da !important;
  color: #6c757d !important;
}

/* Metronic Dropdown 버튼 스타일 재정의 */
.dropdown-toggle {
  box-shadow: none !important;
}

/* 스크롤 가능한 요소의 스크롤바 숨기기 (모바일 친화적) */
.overflow-auto {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.overflow-auto::-webkit-scrollbar {
  display: none;
} 
</style>