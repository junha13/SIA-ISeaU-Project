<template>
  <div class="my-comment-page container-fluid p-0">

    <!-- Filter & Sort Bar (Sticky) -->
    <div class="sticky-top bg-white p-3 border-bottom d-flex justify-content-between align-items-center" style=" z-index: 900;">
      <!-- 댓글 유형 탭 -->
      <div class="d-flex align-items-center">
        <span
            class="fw-bold me-3"
            :class="{'text-primary': commentType === 'visitor', 'text-muted': commentType !== 'visitor'}"
            @click="commentType = 'visitor'"
            style="cursor: pointer;"
        >
          방문자 댓글
        </span>
        <span
            class="fw-bold"
            :class="{'text-primary': commentType === 'post', 'text-muted': commentType !== 'post'}"
            @click="commentType = 'post'"
            style="cursor: pointer;"
        >
          게시글 댓글
        </span>
      </div>

      <!-- 정렬 드롭다운 -->
      <div class="dropdown">
        <button class="btn btn-light-secondary btn-sm dropdown-toggle" type="button"
                data-bs-toggle="dropdown" aria-expanded="false"
                style="background-color: #f8f9fa;">
          {{ sortOptions.find(opt => opt.value === currentSort).label }}
        </button>
        <ul class="dropdown-menu dropdown-menu-end">
          <li v-for="opt in sortOptions" :key="opt.value">
            <a class="dropdown-item" href="#" @click.prevent="currentSort = opt.value">{{ opt.label }}</a>
          </li>
        </ul>
      </div>
    </div>

    <!-- 댓글 목록 -->
    <div class="comment-list p-3">
      <div v-if="filteredComments.length > 0">
        <div v-for="comment in filteredComments" :key="comment.id" class="comment-item border-bottom pb-4 mb-4">
          <!-- 댓글 대상 (해수욕장/게시글) -->
          <h6 class="fw-bolder mb-2" :style="{ color: darkColor }">{{ comment.targetName }}</h6>

          <!-- 평점 -->
          <div class="d-flex align-items-center mb-2">
            <div class="rating-stars me-2">
              <i v-for="i in 5" :key="i"
                 :class="['fas fa-star', i <= comment.rating ? 'text-warning' : 'text-muted']"></i>
            </div>
            <span class="fw-bold small me-3" :style="{ color: darkColor }">{{ comment.rating.toFixed(1) }}</span>

            <!-- 수정/삭제 버튼 -->
            <i class="fas fa-pencil-alt text-secondary me-3" style="cursor: pointer;"></i>
            <i class="fas fa-trash-alt text-danger" style="cursor: pointer;"></i>
          </div>

          <!-- 댓글 내용 -->
          <p class="small text-muted mb-2">{{ comment.content }}</p>

          <!-- 작성일 -->
          <small class="text-secondary">{{ comment.date }}</small>
        </div>
      </div>
      <div v-else class="text-center p-5 text-muted">
        <p class="mb-0">작성된 댓글이 없습니다.</p>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'

const store = useStore();

const { header } = storeToRefs(store)

onMounted(() => {
  header.value = '내 댓글 목록'
})


const mainColor = '#0092BA';
const darkColor = '#0B1956';

// --- State ---
const commentType = ref('visitor'); // 'visitor' or 'post'
const currentSort = ref('latest'); // 'latest' or 'oldest'

const sortOptions = [
  { label: '최신순', value: 'latest' },
  { label: '오래된순', value: 'oldest' },
];

// --- Dummy Data (해수욕장 댓글 기준) ---
const allComments = ref([
  { id: 1, type: 'visitor', targetName: '해운대 해수욕장', content: '정말 깨끗하고 좋은 해수욕장이에요! 모래도 부드럽고 물도 맑아서 가족과 함께 즐거운 시간을 보냈습니다. 주변 편의시설도 잘 되어있어요.', rating: 5.0, date: '2025년 1월 15일' },
  { id: 2, type: 'visitor', targetName: '광안리 해수욕장', content: '야경이 정말 아름다운 곳이에요. 광안대교 뷰가 최고입니다. 다만 사람이 많아서 조금 복잡했어요.', rating: 4.0, date: '2025년 1월 12일' },
  { id: 3, type: 'visitor', targetName: '송정 해수욕장', content: '한적하고 조용한 분위기가 좋아요. 서핑하기에도 좋은 조건이었습니다. 카페들도 많아서 여유롭게 즐길 수 있어요.', rating: 5.0, date: '2025년 1월 10일' },
  { id: 4, type: 'visitor', targetName: '다대포 해수욕장', content: '일몰이 예쁘긴 한데 시설이 조금 아쉬웠어요. 그래도 조용해서 산책하기엔 좋았습니다.', rating: 3.0, date: '2025년 1월 8일' },
  { id: 5, type: 'visitor', targetName: '일광 해수욕장', content: '넓고 깨끗한 해변이에요. 주차장도 넓어서 편리했습니다. 가족 단위로 오기 좋은 곳 같아요.', rating: 4.0, date: '2025년 1월 5일' },
  { id: 6, type: 'post', targetName: '해파리 쏘임 후기', content: '해파리 쏘임 후기 게시글에 달린 댓글입니다.', rating: 4.5, date: '2025년 2월 1일' },
  { id: 7, type: 'post', targetName: '서핑 보드 추천', content: '서핑 보드 추천 게시글에 달린 댓글입니다.', rating: 3.5, date: '2025년 2월 5일' },
]);

// --- Computed ---

/**
 * 선택된 댓글 유형에 따라 필터링
 */
const filteredComments = computed(() => {
  let list = allComments.value.filter(c => c.type === commentType.value);

  // 정렬 로직 (날짜 기준)
  list.sort((a, b) => {
    // 날짜 문자열을 Date 객체로 변환하는 복잡한 로직은 생략하고, 임시로 ID 기준으로 정렬합니다.
    if (currentSort.value === 'latest') {
      return b.id - a.id; // ID가 높을수록 최신이라 가정
    } else {
      return a.id - b.id; // ID가 낮을수록 오래됨이라 가정
    }
  });

  return list;
});
</script>

<style scoped>
.my-comment-page {
  /* AppLayout Header(55px) + Sticky Filter(약 50px) + Footer(60px) 고려하여 높이 조정 필요 */
  min-height: calc(100vh - 55px - 60px);
}
.comment-list {
  padding-top: 5px; /* 필터 바 아래 여백 */
}
/* 정렬 드롭다운 버튼의 폰트 사이즈 조정 */
.btn-sm {
  font-size: 0.85rem;
}

</style>
