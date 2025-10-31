<template>
  <div class="p-3">
    <h6 class="fw-bold mb-3">소개</h6>
    <p class="text-black small">
      {{ beach.beachInformation }}
    </p>

    <h6 class="fw-bold mb-3">상세 주소</h6>
    <p class="text-black small">
      {{ beach.address }}
    </p>

    <!-- 지도 -->
    <div class="rounded shadow-sm overflow-hidden my-3" style="height:200px;">
      <div class="bg-light h-100 w-100 d-flex justify-content-center align-items-center text-muted">
        <div ref="mapEl" style="width:100%;height:200px;"></div>
      </div>
    </div>

    <div class="mt-4">
      <!-- 카운트 연결 -->
      <h6 class="fw-bold">
        방문자 리뷰 ({{ isCommentsLoading ? '...' : comments.length }})
      </h6>

      <!-- 작성 폼: 레이아웃 유지 -->
      <div class="border rounded p-3 mt-2">
        <textarea
          class="form-control mb-2"
          placeholder="리뷰를 작성해주세요..."
          rows="2"
          v-model="newContent"
        ></textarea>

        <div class="d-flex justify-content-between align-items-center">
          <!-- 평점 선택 -->
          <select class="form-select form-select-sm" style="width:auto" v-model.number="newRating">
            <option :value="5">⭐ 5</option>
            <option :value="4">⭐ 4</option>
            <option :value="3">⭐ 3</option>
            <option :value="2">⭐ 2</option>
            <option :value="1">⭐ 1</option>
          </select>

          <button
            class="btn btn-sm text-white"
            style="background-color:#0092BA;"
            :disabled="submitting || !newContent.trim()"
            @click="submitComment"
          >
            {{ submitting ? '등록 중...' : '등록' }}
          </button>
        </div>
      </div>

      <!-- 목록: 기존 detailData?.reviews → store의 comments 로 교체 -->
      <div class="mt-3">
        <div v-if="isCommentsLoading" class="text-muted small">리뷰 불러오는 중...</div>
        <div v-else-if="!comments.length" class="text-muted small">리뷰가 아직 없습니다.</div>

        <div v-else>
          <div
            v-for="c in comments"
            :key="c.beachCommentNumber"
            class="border rounded p-3 mb-2"
          >
            <div class="fw-bold">
              {{ c.userName ?? ('사용자 ' + c.userNumber) }}
              <small class="text-muted ms-2">{{ (c.createdTime ?? '').toString().slice(0,16) }}</small>
              <span class="ms-2">⭐ {{ c.rating }}</span>
            </div>
            <p class="mb-1 small text-secondary">{{ c.commentContent }}</p>

            <!-- 본인 댓글 가정(userNumber === 1)일 때만 노출 -->
            <div v-if="c.userNumber === 1" class="mt-1">
              <button class="btn btn-outline-secondary btn-sm me-2" @click="startEdit(c)">
                수정
              </button>
              <button class="btn btn-outline-danger btn-sm" @click="remove(c)">
                삭제
              </button>
            </div>

            <!-- 인라인 수정 UI (선택사항) -->
            <div v-if="editingId === c.beachCommentNumber" class="mt-2">
              <textarea class="form-control mb-2" rows="2" v-model="editContent"></textarea>
              <div class="d-flex align-items-center justify-content-between">
                <select class="form-select form-select-sm" style="width:auto" v-model.number="editRating">
                  <option :value="5">⭐ 5</option>
                  <option :value="4">⭐ 4</option>
                  <option :value="3">⭐ 3</option>
                  <option :value="2">⭐ 2</option>
                  <option :value="1">⭐ 1</option>
                </select>
                <div>
                  <button class="btn btn-sm btn-primary me-2" @click="confirmEdit(c)">
                    저장
                  </button>
                  <button class="btn btn-sm btn-light" @click="cancelEdit">취소</button>
                </div>
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// ===== 기존 기능들 유지 =====
import { ref, computed, onMounted, watchEffect } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useStore } from '@/stores/store.js'
import { useBeachStore } from '@/stores/beachStore'

const ui = useStore()
const { header, beach } = storeToRefs(ui) // 지도/소개/주소는 기존대로 beach 사용

// ===== 댓글/상세는 beachStore 사용 =====
const route = useRoute()
const beachStore = useBeachStore()
const { getComments, isCommentsLoading } = storeToRefs(beachStore)
const comments = computed(() => getComments.value)

// 초기 로딩: 현재 페이지의 beachNumber로 댓글 불러오기
onMounted(async () => {
  const beachNumber = Number(route.params.beachNumber)

  await beachStore.loadComments(beachNumber)
})

// 작성 폼 상태
const newContent = ref('')
const newRating = ref(5)
const submitting = ref(false)
const submitComment = async () => {
  if (!beach.value?.beachNumber) return
  submitting.value = true
  try {
    await beachStore.addComment(beach.value.beachNumber, {
      commentContent: newContent.value,
      rating: newRating.value
    })
    newContent.value = ''
    newRating.value = 5
  } finally {
    submitting.value = false
  }
}

// 삭제
const remove = async (c) => {
  if (!confirm('이 댓글을 삭제할까요?')) return
  await beachStore.deleteComment(c.beachCommentNumber, beach.value.beachNumber)
}

// 수정 (인라인)
const editingId = ref(null)
const editContent = ref('')
const editRating = ref(5)

const startEdit = (c) => {
  editingId.value = c.beachCommentNumber
  editContent.value = c.commentContent
  editRating.value = c.rating
}
const cancelEdit = () => {
  editingId.value = null
  editContent.value = ''
  editRating.value = 5
}
const confirmEdit = async (c) => {
  await beachStore.editComment(
    beach.value.beachNumber,
    c.beachCommentNumber,
    { commentContent: editContent.value, rating: editRating.value }
  )
  cancelEdit()
}

// ===== 지도 =====
const mapEl = ref(null)
let map, marker

watchEffect(() => {
  const lat = beach.value?.latitude
  const lng = beach.value?.longitude
  if (!lat || !lng || !mapEl.value || !window.naver?.maps) return

  const pos = new window.naver.maps.LatLng(lat, lng)
  if (!map) {
    map = new window.naver.maps.Map(mapEl.value, { center: pos, zoom: 15 })
    marker = new window.naver.maps.Marker({ position: pos, map })
  } else {
    map.setCenter(pos)
    marker.setPosition(pos)
  }
})
</script>