<template>
  <div class="beach-list-page p-3">

    <div class="sticky-top bg-white pt-1 pb-3" style="z-index: 900;">
      <div class="d-flex align-items-center mb-3">
        <div class="dropdown me-2">
          <button class="btn btn-secondary dropdown-toggle d-flex align-items-center" type="button"
                  data-bs-toggle="dropdown" aria-expanded="false"
                  :style="dropdownBtnStyle">
            -지역-
            <i class="fas fa-chevron-down ms-2"></i>
          </button>
          <ul class="dropdown-menu">
            <li><a class="dropdown-item" href="#">부산</a></li>
            <li><a class="dropdown-item" href="#">강릉</a></li>
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
        <div v-for="beach in filteredBeachList" :key="beach.id" class="beach-card card shadow-sm mb-4 rounded-3 border-0" @click="goToDetail(beach.id)">
          <div class="card-body p-3 d-flex">
            <div class="beach-image-placeholder me-3 rounded-2"
                 :style="{ border: '1px solid #eee' }">
              <p class="text-center text-muted fw-bold mb-0 pt-2 fs-7">해변 이미지</p>
              <div class="rating-badge badge text-white px-2 py-1 rounded-pill" :style="{ backgroundColor: mainColor }">
                <i class="fas fa-star fs-7"></i> {{ beach.rating.toFixed(1) }}
              </div>
            </div>

            <div class="beach-info flex-grow-1">
              <div class="d-flex justify-content-between align-items-start">
                <h5 class="fw-bolder fs-6 mb-1" :style="{ color: darkColor }">{{ beach.name }}</h5>
                <i :class="['fas fa-heart fs-5', { 'text-danger': isFavorite(beach.id), 'text-muted': !isFavorite(beach.id) }]"
                   @click.stop="toggleFavorite(beach.id)"
                   style="cursor: pointer;"></i>
              </div>

              <p class="text-muted fs-7 mb-2">{{ beach.location }}</p>

              <div class="d-flex gap-2 mb-3">
                            <span v-for="(tag, index) in beach.tags" :key="index"
                                  :class="['badge', 'px-2', 'py-1', 'fw-bold', tagClass(tag)]">
                                {{ tag }}
                            </span>
              </div>

              <div class="d-flex justify-content-between align-items-center">
                <p class="fs-7 mb-0 text-muted">{{ beach.distance }} 거리</p>

                <button v-if="isSelected(beach.id)"
                        class="btn btn-sm fw-bold"
                        :style="{ backgroundColor: mainColor, color: 'white' }"
                        @click.stop="toggleSelect(beach.id, beach.name)">
                  선택됨
                </button>
                <button v-else
                        class="btn btn-sm btn-outline-secondary fw-bold"
                        @click.stop="toggleSelect(beach.id, beach.name)">
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
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useBeachStore } from '@/stores/beachStore.js';

const router = useRouter();
const beachStore = useBeachStore();

// --- Color Definitions ---
const mainColor = '#0092BA';
const darkColor = '#0B1956';
const safetyColor = '#8482FF';
const cautionColor = '#FFB354';
const dangerColor = '#EB725B';

// --- State ---
const activeTab = ref('all');
const viewMode = ref('list');
const currentSort = ref('distance');

const sortOptions = [
  { label: '거리순', value: 'distance' },
  { label: '리뷰순', value: 'review' },
  { label: '평점순', value: 'rating' },
];

// --- Styles (for dynamic binding) ---
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

// --- Dummy Data (실제로는 API로 가져옴) ---
const beachList = ref([
  { id: 1, name: '해운대 해수욕장', location: '부산광역시 해운대구', rating: 4.5, distance: '2.5km', tags: ['안전', '수영', '서핑'], reviewCount: 500 },
  { id: 2, name: '광안리 해수욕장', location: '부산광역시 수영구', rating: 3.7, distance: '5.1km', tags: ['안전', '수영', '산책'], reviewCount: 300 },
  { id: 3, name: '송정 해수욕장', location: '부산광역시 기장군', rating: 4.2, distance: '8.0km', tags: ['서핑', '가족'], reviewCount: 150 },
  { id: 4, name: '다대포 해수욕장', location: '부산광역시 사하구', rating: 4.8, distance: '12.3km', tags: ['산책', '가족', '안전'], reviewCount: 700 },
]);

// --- Computed & Methods ---

/**
 * 필터링 및 정렬된 해수욕장 리스트
 */
const filteredBeachList = computed(() => {
  let list = beachList.value.slice();

  // 1. 탭 필터링
  if (activeTab.value === 'favorite') {
    list = list.filter(beach => beachStore.getFavoriteBeachIds.includes(beach.id));
  } else if (activeTab.value !== 'all') {
    list = list.filter(beach => beach.tags.includes(activeTab.value.charAt(0).toUpperCase() + activeTab.value.slice(1)));
  }

  // 2. 정렬
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
const isSelected = (beachId) => {
  return beachStore.getCurrentSelectedBeachId === beachId;
};

/**
 * 해수욕장 선택 토글 액션 호출
 */
const toggleSelect = (beachId, beachName) => {
  beachStore.toggleSelectBeach(beachId, beachName);
};

/**
 * 즐겨찾기 상태 확인
 */
const isFavorite = (beachId) => {
  return beachStore.getFavoriteBeachIds.includes(beachId);
};

/**
 * 즐겨찾기 토글 액션 호출
 */
const toggleFavorite = (beachId) => {
  beachStore.toggleFavoriteBeach(beachId);
};

/**
 * 상세 페이지로 이동
 */
const goToDetail = (beachId) => {
  router.push({ name: 'BeachDetail', params: { id: beachId } });
}

/**
 * 태그에 따른 배지 클래스 반환
 */
const tagClass = (tag) => {
  switch (tag) {
    case '안전':
      return 'bg-secondary';
    case '수영':
      return 'bg-info';
    case '서핑':
      return 'bg-dark';
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
}

/* 평점 배지 오버레이 */
.rating-badge {
  position: absolute;
  bottom: 5px;
  right: 5px;
  font-size: 0.75rem;
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