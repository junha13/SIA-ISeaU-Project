<template>
  <div class="app-content flex-column-fluid mb-17">
    <div class="app-container-fluid">

      <!-- 게시글 본문 컨테이너 -->
      <div class="card card-flush shadow-sm mx-3 mb-5 p-5">

        <!-- 제목 영역 및 메타 데이터 재배치 -->
        <div class="mb-5 border-bottom pb-4">
          <h2 class="fw-bolder text-gray-900 mb-3 fs-1">{{ post.postTitle }}</h2>
          <div class="d-flex justify-content-between flex-wrap text-muted small fw-semibold">
            <!-- 작성자, 분류, 날짜 정보 (좌측) -->
            <div class="d-flex align-items-center text-gray-600 mb-2 fs-6">
              <span class="badge badge-light-secondary fw-bold rounded-pill me-3">{{ post.boardName }}</span>
              <span class="me-3">작성자: <span class="text-dark fw-semibold">{{ post.id }}</span></span>
              <span class="text-gray-500">{{ post.createdAt }}</span>
            </div>
            <!-- 조회수, 추천수 정보 (우측 - 아이콘 축소) -->
            <div class="d-flex align-items-center mb-2 fs-6">
              <span class="text-gray-600 me-3">
                <i class="ki-duotone ki-eye fs-6 me-1 text-info"></i> 조회 {{ post.viewCount }}
              </span>
              <span class="text-gray-600">
                <i class="ki-duotone ki-heart fs-6 me-1 text-danger"></i> 추천 {{ post.recommendCount }}
              </span>
            </div>
          </div>
        </div>

        <!-- 본문 내용: 폰트 크기 및 줄 간격 조정 -->
        <div class="mb-5">
          <p class="text-gray-800 fs-4 lh-lg" v-html="post.postContent"></p>
        </div>

        <!--  
        <div v-if="post.tags.length > 0" class="mb-5 border-top pt-4">
          <span v-for="tag in post.tags" :key="tag" class="badge badge-light-secondary fw-bold me-2 py-2 px-4 rounded-pill">
            #{{ tag }}
          </span>
        </div>-->

        <!-- 버튼: 크기 축소, 정렬 우측 유지, 순서 변경 -->
        <div class="d-flex justify-content-end gap-2 pt-3 border-top">
          <!-- 1. 추천 버튼 (크기: btn-sm, 텍스트 크기: fs-6) -->
          <button v-if="!recommendedByMe" class="btn btn-dark btn-sm fw-bold shadow-sm d-flex align-items-center justify-content-start ps-3 pe-4" @click="requestRecommendPost(post.postNumber)">
            <i class="ki-duotone ki-heart fs-6 me-1 text-danger"></i> <span class="text-white fs-6">추천 ({{ recommendCount }})</span>
          </button>
          <button v-if="recommendedByMe" class="btn btn-dark btn-sm fw-bold shadow-sm d-flex align-items-center justify-content-start ps-3 pe-4" @click="requestRecommendPost(post.postNumber)">
            <i class="ki-duotone ki-heart fs-6 me-1 text-danger"></i> <span class="text-white fs-6">추천취소 ({{ recommendCount }})</span>
          </button>
          <!-- 2. 수정 버튼 (크기: btn-sm, 텍스트 크기: fs-6) -->
          <button v-if="loginId === post.id" class="btn btn-dark btn-sm fw-bold d-flex align-items-center justify-content-start ps-3 pe-4" @click="editPost">
            <i class="ki-duotone ki-pencil fs-6 me-1"></i> <span class="text-white fs-6">수정</span>
          </button>
          <!-- 3. 삭제 버튼 (크기: btn-sm, 텍스트 크기: fs-6) -->
          <button v-if="loginId === post.id" class="btn btn-dark btn-sm fw-bold d-flex align-items-center justify-content-start ps-3 pe-4" @click="confirmDelete">
            <i class="ki-duotone ki-trash fs-6 me-1 text-danger"></i> <span class="text-white fs-6">삭제</span>
          </button>
        </div>
      </div>

      <!-- 댓글 영역: 쉐도우 비율 낮추기 -->
      <div class="card card-flush shadow-xs mx-3">
        <div class="card-body p-5">
          <!-- 댓글 헤더: text-gray-900로 변경하여 다크 톤 유지 -->
          <h5 class="fw-bolder text-gray-900 mb-4">댓글 (<span class="text-gray-900">{{ comments.length }}</span>)</h5>

          <!-- 댓글 입력 -->
          <div class="mb-6 d-flex align-items-center">
            <input
                v-model="newComment"
                type="text"
                class="form-control me-3 form-control-solid rounded-2 border border-gray-300"
                placeholder="댓글을 입력하세요"
                @keyup.enter="requestAddComment(post.postNumber)"
            />
            <button class="btn btn-dark fw-bold text-nowrap" @click="requestAddComment(post.postNumber)">등록</button>
          </div>

          <!-- 댓글 리스트 -->
          <ul class="list-group list-group-flush">
            <li v-for="(comment, index) in comments" :key="index"
                class="list-group-item d-flex justify-content-between align-items-start border-bottom py-4 px-">
              <div class="d-flex flex-column">
                <span class="text-dark fw-bolder mb-1">{{ comment.id }}</span>
                <span class="text-gray-800">{{ comment.commentContent }}</span>
              </div>
              <div class="d-flex flex-column align-items-end">
                  <small class="text-muted fs-7 mb-2">{{ comment.createdAt }}</small>
                  <!-- 댓글 삭제 버튼도 Dark 스타일로 변경 -->
                  <button v-if="loginId === comment.id" class="btn btn-sm btn-dark fw-semibold" @click="removeComment(index)">
                    <span class="text-white">삭제</span>
                  </button>
              </div>
            </li>
          </ul>
        </div>
      </div>

      <!-- Custom Modal (삭제 확인용) -->
      <!-- <ConfirmModal
          v-model:isVisible="isModalVisible"
          :title="modalTitle"
          :message="modalMessage"
          :type="modalType"
          :autoHide="modalAutoHide"
          confirmText="삭제하기"
          @confirm="handleDelete"
      /> -->
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue"
import { useRoute, useRouter } from "vue-router"
import { useAuthStore } from '@/stores/authStore';

const route = useRoute()
const router = useRouter()

const authStore = useAuthStore();
const loginId = ref(authStore.userInfo.id)

// 임시 데이터
const post = ref({})

const comments = ref([])
const newComment = ref("")


// const addComment = () => {
//   if (newComment.value.trim() !== "") {
//     comments.value.push({
//       author: "나",
//       text: newComment.value.trim(),
//       date: "방금"
//     })
//     newComment.value = ""
//     showModal('댓글 등록', '댓글이 성공적으로 등록되었습니다.', 'success', true)
//   } else {
//     showModal('댓글 오류', '내용을 입력해주세요.', 'error', true)
//   }
// }

const removeComment = (index) => {
  comments.value.splice(index, 1)
}

// const likePost = () => {
//   post.value.likes++
//   showModal('추천 완료', '이 게시글을 추천하셨습니다.', 'success', true)
// }

const editPost = () => {
  router.push(`/board/edit?id=${post.value.id}`)
}

const handleDelete = () => {
  router.push("/board")
}



//====================================================================================
import axios from 'axios'

const recommendedByMe = ref()
const recommendCount = ref()

onMounted(() => {
    post.value.views++

    requestPostDetail(route.params.postNumber)

    loginId.value = authStore.userInfo.id
})

async function requestPostDetail(postNumber) {

    try {
      const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/post/getPostDetail/${postNumber}`, 
      null,
        {
          headers: { 'Content-Type': 'application/json' },
          withCredentials: true,
          timeout: 5000,
        })
        console.log('OK', response.data)
        post.value = response.data.result.result
        recommendedByMe.value = response.data.result.result.recommendedByMe
        recommendCount.value = response.data.result.result.recommendCount
        comments.value = response.data.result.commentList

    } catch (e) {
      console.error('[Detail] load error:', e)
    } 
  }


  // 아직 안함
async function requestAddComment(postNumber) {
  const comment = {
    postNumber:postNumber,
    commentContent:newComment.value
  }

    try {
      const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/post/addComment`,
      comment,
        {
          headers: { 'Content-Type': 'application/json' },
          withCredentials: true,
          timeout: 5000,
        })
        console.log('OK', response.data)
        comments.value = response.data.result.commentList
        newComment.value = ""

    } catch (e) {
      console.error('[Detail] load error:', e)
    } 
}

async function requestRecommendPost(postNumber) {

  const recommend = {
    postNumber:postNumber,
    recommendedByMe:post.recommendedByMe
  }

  try {
      const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/post/recommend`,
      recommend,
        {
          headers: { 'Content-Type': 'application/json' },
          withCredentials: true,
          timeout: 5000,
        })
        console.log('OK', response.data)
        recommendedByMe.value = response.data.result.recommendedByMe
        recommendCount.value = response.data.result.recommendCount

    } catch (e) {
      console.error('[Detail] load error:', e)
    } 
}


  // 동일 컴포넌트 내에서 :id만 바뀌는 경우 대응
  // watch(
  //   () => route.params.id,
  //   (newId) => requestPostDetail(newId)
  // )

</script>

<style scoped>

/* 댓글 입력 필드 배경색 설정 */
.form-control-solid {
    background-color: var(--bs-gray-100) !important;
}

/* 추천 버튼의 배경색 (Dark 스타일 통일) */
.btn-primary {
    background-color: var(--bs-dark) !important;
    border-color: var(--bs-dark) !important;
    color: #fff !important;
}

/* 수정 및 등록 버튼의 배경색 (Dark 스타일 통일) */
.btn-dark {
    background-color: var(--bs-dark) !important;
    border-color: var(--bs-dark) !important;
    color: #fff !important;
}

/* Light Danger 버튼에 대한 별도 처리 (삭제 버튼에 사용됨) */
.btn-light-danger {
    color: var(--bs-danger) !important;
    background-color: transparent !important; /* 배경 투명하게 */
    border-color: transparent !important;
}
</style>