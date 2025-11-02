<template>
  <div class="my-comment-page container-fluid p-0">
    <!-- Filter & Sort Bar (Sticky) -->
    <div class="sticky-top bg-white p-3 border-bottom d-flex justify-content-between align-items-center" style="z-index: 900;">
      <!-- 댓글 유형 탭 -->
      <div class="d-flex align-items-center">
        <span
          class="fw-bold me-3"
          :class="{'text-primary': commentType === 'visitor', 'text-muted': commentType !== 'visitor'}"
          @click="commentType = 'visitor'"
          style="cursor: pointer;"
        >방문자 댓글</span>
        <span
          class="fw-bold"
          :class="{'text-primary': commentType === 'post', 'text-muted': commentType !== 'post'}"
          @click="commentType = 'post'"
          style="cursor: pointer;"
        >게시글 댓글</span>
      </div>

      <!-- 정렬 드롭다운 -->
      <div class="dropdown">
        <button class="btn btn-light-secondary btn-sm dropdown-toggle" type="button"
                data-bs-toggle="dropdown" aria-expanded="false" style="background-color:#f8f9fa;">
          {{ currentSortLabel }}
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
      <div v-if="loading" class="text-center text-muted p-5">불러오는 중…</div>
      <div v-else-if="errorMsg" class="text-center text-danger p-5">{{ errorMsg }}</div>

      <div v-else-if="filteredComments.length > 0">
        <div v-for="comment in filteredComments" :key="comment.beachCommentNumber" class="comment-item border-bottom pb-4 mb-4">
          <!-- 대상 -->
          <h6 class="fw-bolder mb-2" :style="{ color: darkColor }">{{ comment.targetName }}</h6>

          <!-- 평점 + 액션 -->
          <div class="d-flex align-items-center mb-2">
            <div class="rating-stars me-2">
              <i v-for="i in 5" :key="i" :class="['fas fa-star', i <= Math.round(comment.rating) ? 'text-warning' : 'text-muted']"></i>
            </div>
            <span class="fw-bold small me-3" :style="{ color: darkColor }">{{ Number(comment.rating ?? 0).toFixed(1) }}</span>

            <!-- (옵션) 수정/삭제 -->
            <i class="fas fa-pencil-alt text-secondary me-3" style="cursor: pointer;" @click="onEdit(comment)"></i>
            <i class="fas fa-trash-alt text-danger" style="cursor: pointer;" @click="onDelete(comment)"></i>
          </div>

          <!-- 내용 -->
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
// ✅ 주석 포함: 중앙 API만 사용
import { ref, computed, onMounted, watch } from 'vue'
import { useStore } from '@/stores/store.js'
import { storeToRefs } from 'pinia'
import { beachApi } from '@/api/beach.js' // ← 경로는 프로젝트 구조에 맞게 조정

const store = useStore()
const { header } = storeToRefs(store)
onMounted(() => { header.value = '내 댓글 목록(삭제 안됨@@@@@)' })

// 색상 토큰
const mainColor = '#0092BA'
const darkColor = '#0B1956'

// 탭/정렬 상태
const commentType = ref('visitor')   // 'visitor' | 'post'
const currentSort = ref('latest')     // 'latest'  | 'oldest'

const sortOptions = [
  { label: '최신순',   value: 'latest' },
  { label: '오래된순', value: 'oldest' },
]

// 드롭다운 라벨
const currentSortLabel = computed(() =>
  (sortOptions.find(o => o.value === currentSort.value) || sortOptions[0]).label
)

// 서버 원본(현재: 방문자 댓글만)
const allComments = ref([])

// UI 상태
const loading  = ref(false)
const errorMsg = ref('')

// 날짜 포맷 (ISO → 'YYYY-MM-DD HH:mm')
function fmtDate(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  if (isNaN(d.getTime())) return String(iso)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

// ✅ 중앙 API 사용 버전
async function loadMyVisitorComments() {
  loading.value = true
  errorMsg.value = ''

  try {
    // beachApi.fetchMyComments(sort) → execute 함수 반환이므로 한 번 더 호출
    const res = await beachApi.fetchMyComments(currentSort.value)()

    // Axios 래퍼 응답 표준화 대비: data/result 안전 파싱
    const payload = res?.data ?? res
    const list = payload?.result ?? payload?.data?.result ?? []

    // DTO → 화면 모델 매핑
    allComments.value = list.map(c => ({
      beachCommentNumber: c.beachCommentNumber,
      content: c.commentContent,
      rating: Number(c.rating ?? 0),
      date: fmtDate(c.createdTime),

      // 추가 필드
      beachNumber: c.beachNumber,
      userNumber: c.userNumber,
      userId: c.userId,
      type: 'visitor',

      // 정책상 이름 없으면 번호로 표시
      targetName: c.beachName ? c.beachName : `해변 #${c.beachNumber}`,
    }))
  } catch (e) {
    console.error('[loadMyVisitorComments] error:', e)
    errorMsg.value = '댓글을 불러오지 못했어요.'
    allComments.value = []
  } finally {
    loading.value = false
  }
}

// ✅ 댓글 수정
async function onEdit(comment) {
  // (간단 버전) prompt로 새 내용/평점 입력 받기 — 추후 모달로 교체 가능
  const newContent = prompt('댓글 내용을 수정하세요', comment.content)
  if (newContent == null) return

  const newRatingStr = prompt('평점을 0~5 사이로 입력하세요', String(comment.rating ?? 0))
  if (newRatingStr == null) return

  const newRating = Number(newRatingStr)
  if (Number.isNaN(newRating) || newRating < 0 || newRating > 5) {
    alert('평점은 0~5 사이 숫자여야 해요.')
    return
  }

  try {
    // ⬇️ 이미 선언된 API 사용: beachNumber와 commentNumber를 함께 전달
    const payload = {
      commentContent: newContent,
      rating: newRating,
    }
    const res = await beachApi
      .editComment(comment.beachNumber, comment.beachCommentNumber)(payload)

    // 응답 스키마에 맞춰 성공 판별 (프로젝트 표준에 따라 조정)
    const ok = res?.data?.success ?? res?.success ?? true
    if (!ok) throw new Error('수정 실패')

    // ✅ 로컬 목록 즉시 반영(옵티미스틱 업데이트)
    Object.assign(comment, {
      content: newContent,
      rating: newRating,
    })
    // 또는 완전 정확도를 원하면 리스트 재조회:
    // await loadMyVisitorComments()
  } catch (e) {
    console.error('[onEdit] error:', e)
    alert('수정에 실패했어요.')
  }
}

// ✅ 댓글 삭제
async function onDelete(comment) {
  if (!confirm('정말 이 댓글을 삭제할까요?')) return

  try {
    // ⬇️ 이미 선언된 API 사용: delete는 commentNumber만 전달
    const res = await beachApi.deleteComment(comment.beachCommentNumber)()

    const ok = res?.data?.success ?? res?.success ?? true
    if (!ok) throw new Error('삭제 실패')

    // ✅ 로컬 목록에서 제거
    allComments.value = allComments.value.filter(
      c => c.beachCommentNumber !== comment.beachCommentNumber
    )
    // 또는 재조회:
    // await loadMyVisitorComments()
  } catch (e) {
    console.error('[onDelete] error:', e)
    alert('삭제에 실패했어요.')
  }
}

// 최초 로딩
onMounted(loadMyVisitorComments)

// 정렬/탭 변경 시 재조회 (백엔드 정렬을 그대로 사용)
watch(currentSort, () => {
  if (commentType.value === 'visitor') loadMyVisitorComments()
})
watch(commentType, (t) => {
  if (t === 'visitor') loadMyVisitorComments()
  // 'post'는 추후 API 연결 후 구현 예정
})

// 화면 표시용: 현재는 방문자만
const filteredComments = computed(() => {
  if (commentType.value !== 'visitor') return []
  return allComments.value
})

</script>

<style scoped>
.my-comment-page {
  /* AppLayout Header(55px) + Footer(60px) 고려하여 높이 조정 */
  min-height: calc(100vh - 55px - 60px);
}
.comment-list { padding-top: 5px; }
.btn-sm { font-size: 0.85rem; }
</style>