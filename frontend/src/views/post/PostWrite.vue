<template>
  <div class="app-content flex-column-fluid">
    <div class="app-container-fluid">

      <!-- 메인 컨텐츠 영역 -->
      <div class="card card-flush shadow-sm mx-3 mb-5">
        <div class="card-body p-5">

          <!-- 지역 선택 -->
          <div class="mb-8">
            <label class="form-label fw-bold text-gray-800">게시글 분류 선택</label>
            <div class="d-flex flex-wrap gap-2">
              <button v-for="cat in postCategories" :key="cat"
                      class="btn btn-sm fw-semibold rounded-pill"
                      :class="boardData.postCategoryName === cat ? 'btn-dark text-white' : 'btn-outline-secondary text-gray-700'"
                      @click="boardData.postCategoryName = cat">
                {{ cat }}
              </button>
            </div>
          </div>

          <!-- 제목 -->
          <div class="mb-8">
            <label class="form-label fw-bold text-gray-800">제목</label>
            <input
                type="text"
                class="form-control bg-white text-dark rounded-2 border border-gray-400"
                v-model="postData.title"
                placeholder="제목을 입력하세요" 
            />
          </div>

          <!-- 내용 (TextArea) -->
          <div class="mb-8">
            <label class="form-label fw-bold text-gray-800">내용</label>
            <QuillForm ref="editorRef"/>
          </div>
          <!-- 
          태그
          <div class="mb-10">
            <label class="form-label fw-bold text-gray-800">태그</label>
            <div class="d-flex flex-wrap gap-2 mb-3">
              <span
                  v-for="(tag, index) in postData.tags"
                  :key="index"
                  class="badge bg-secondary text-white p-2 rounded-pill fs-7 fw-semibold"
              >
                #{{ tag }}
                태그 삭제 아이콘 색상을 text-danger로 변경하여 시인성 확보
                <i class="ki-duotone ki-cross-circle fs-5 ms-1 text-danger" style="cursor: pointer;" @click="removeTag(index)"></i>
              </span>
            </div>
            <input
                type="text"
                v-model="newTag"
                @keyup.enter.prevent="addTag"
                class="form-control bg-white text-dark rounded-2 border border-gray-400"
                placeholder="태그를 입력하고 Enter를 누르세요"
            />
          </div> -->

          <!-- 버튼: 텍스트 동적 변경 및 등록/수정 로직 호출 -->
          <div class="d-flex justify-content-end gap-3">
            <button class="btn btn-light-secondary fw-bold" @click="saveDraft">임시저장</button>
            <button class="btn btn-dark fw-bold" @click="requestAddPost()">
              {{ isEditMode ? '수정 완료' : '등록' }}
            </button>
          </div>
        </div>
      </div>
  </div>
</div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue" 
import { useRoute, useRouter } from "vue-router" 
import axios from 'axios'
import { useAuthStore } from '@/stores/authStore';

const authStore = useAuthStore();
const userNumber = ref(authStore.userInfo.userNumber)


const router = useRouter()
const route = useRoute() 
const DRAFT_STORAGE_KEY = 'board_post_draft'; // localStorage 키 정의

const postCategories = ['공지','정보','친목']

const editorRef = ref(null)

const boardData = ref({
    //regionName: "서울특별시",
    //boardCategoryName: "미술",
    postCategoryName:"잡담",
})

// 하나의 통합된 폼 데이터 상태
const postData = ref({
    id: null,
    postTitle: ""
})

// isEditMode computed 속성: URL에 ID가 있으면 수정 모드
const isEditMode = computed(() => !!route.query.id)

// ⭐ onMounted: 수정 모드일 때 데이터 로드, 아니면 임시 저장 데이터 로드 여부 질문
onMounted(() => {
  if (isEditMode.value) {
    const postId = parseInt(route.query.id) || 2;

    // 수정 모드: 기존 게시글 데이터 로드 (API 호출 시뮬레이션)
    postData.value = {
      id: postId,
      category: mockPost.category,
      title: mockPost.title,
      content: mockPost.content,
    }
  } else {
    // ⭐ 작성 모드: 임시 저장 데이터 로드 여부 질문
    const savedDraft = localStorage.getItem(DRAFT_STORAGE_KEY);
    if (savedDraft) {
    }
  }
})
// const addTag = () => {
//   const tagText = newTag.value.trim()
//   if (tagText && !postData.value.tags.includes(tagText) && postData.value.tags.length < 5) {
//     postData.value.tags.push(tagText)
//     newTag.value = ''
//   } else if (postData.value.tags.length >= 5) {
//     showModal('태그 제한', '태그는 최대 5개까지만 추가할 수 있습니다.', 'error')
//   }
// }

// const removeTag = (index) => {
//   postData.value.tags.splice(index, 1)
// }

// saveDraft 함수: 현재 글의 내용과 카테고리, 태그를 localStorage에 저장
const saveDraft = () => {
  try {
    const draftContent = {
      category: postData.value.category,
      title: postData.value.title,
      content: postData.value.content,
      tags: postData.value.tags,
      userNumber: userNumber.value
    };
    localStorage.setItem(DRAFT_STORAGE_KEY, JSON.stringify(draftContent));
    // 임시 저장 후에는 페이지를 이동하지 않고 저장 완료 모달만 표시
    showModal('임시저장 완료', '작성 중인 내용이 저장되었습니다. 페이지 이동 시에도 유지됩니다.', 'success', 'none', '확인', true);
  } catch (e) {
    console.error("Error saving draft to localStorage", e);
    showModal('저장 실패', '브라우저 저장 공간 문제로 임시 저장에 실패했습니다.', 'error');
  }
}

// const confirmSubmit = () => {
//   if (!postData.value.title || !postData.value.content) {
//     showModal('등록 오류', '제목과 내용을 모두 입력해주세요.', 'error')
//     return
//   }
  
//   if (isEditMode.value) {
//     submitPost();
//   } else {
//     const actionText = '등록'
//     const confirmMessage = '게시글을 등록하시겠습니까?'
//     showModal(actionText, confirmMessage, 'confirm', 'submit', actionText, false)
//   }
// }

// const submitPost = () => {
//   const finalData = {
//       ...postData.value,
//       newFiles: uploadedFiles.value.map(file => file.name) 
//   }

//   if (isEditMode.value) {
//     console.log('게시글 수정 완료 (ID: ' + finalData.id + '):', finalData)
//     showModal('수정 완료', "게시글이 성공적으로 수정되었습니다!", 'success', 'submitSuccess')
//   } else {
//     // ⭐ 등록 로직: 게시글 목록 데이터에 새 글 추가 (시연용)
//     if (window.appData && window.appData.addPost) {
//         window.appData.addPost(finalData);
//     }
//     console.log('게시글 등록:', finalData)
//     showModal('등록 완료', "게시글이 성공적으로 등록되었습니다!", 'success', 'submitSuccess')
//   }
// }

//===========================================================================================

import QuillForm from '@/components/Editor.vue'

  async function requestAddPost() {
    const html = editorRef.value?.getContent() || ''

    const params = {
        postTitle: postData.value.title,
        postContent:html,
        // regionName:boardData.value.regionName,
        // boardCategoryName:boardData.value.boardCategoryName,
        boardName:boardData.value.postCategoryName
    }
    try {
      const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/post/post`,
        params,
        {
          headers: { 'Content-Type': 'application/json' },
          timeout: 5000,
          withCredentials: true
        })
        console.log('OK', response.data)
        const postNumber = response.data.result.PostNumber
        alert(`게시글 등록이 완료되었습니다.`)

        router.replace(`/post/${postNumber}`)

    } catch (e) {
      console.error('[Detail] load error:', e)
    }
  }


</script>

<style scoped>
/* ⭐ [수정]: 배경색을 아주 약간만 더 어둡게 조정 */
.write-card-bg {
  background-color: #fcfcfc !important;
}

.btn-outline-secondary {
  border-color: #d1d1d1 !important;
  color: var(--bs-gray-700) !important;
}

.btn-dark {
  background-color: var(--bs-dark) !important;
  border-color: var(--bs-dark) !important;
  color: #fff !important;
}

.d-flex.align-items-center.justify-content-between {
  position: relative;
}
.page-heading.position-absolute {
  z-index: 10;
  max-width: 70%;
  text-align: center;
}

.form-control.bg-white {
  background-color: #fff !important;
  color: var(--bs-dark) !important;
  border-color: var(--bs-gray-400) !important;
}
</style>