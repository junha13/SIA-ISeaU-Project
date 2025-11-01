<template>
  <div class="app-content flex-column-fluid">
    <div class="app-container-fluid">
      <div class=" px-3">

        <!-- 검색 입력창 및 드롭다운 -->
        <div class="d-flex justify-content-between mb-5">
                      <!-- ⭐ 검색 필터 드롭다운 -->
          <button class="btn btn-secondary dropdown-toggle text-dark fw-bold"
                  type="button"
                  data-bs-toggle="dropdown"
                  aria-expanded="false"
                  style="border-top-left-radius: .475rem; border-bottom-left-radius: .475rem;">
            {{ postCategory }}
          </button>
            <ul class="dropdown-menu">
              <li><a v-for="categorie in postCategories" :key="categorie" class="dropdown-item"  @click.prevent="postCategory = categorie">{{ categorie}}</a></li>
            </ul>

          <div class="input-group w-100 mw-500px border border-gray-500">

            <!-- 검색 입력 필드 -->
            <input type="text"
                   class="form-control border-0 bg-white"
                   placeholder="검색어를 입력하세요"
                   v-model="searchQuery"
                   @keyup.enter="searchPosts"
                   :style="{height: 40,
                   borderTopLeftRadius: 0, borderBottomLeftRadius: 0}"/>
          </div>
          <button
            class="btn"
            type="button"
            @click="searchPosts"
            :style="{ backgroundColor: 'black', color: 'white', border: 'none', borderTopLeftRadius: 0, borderBottomLeftRadius: 0 }"
          >
            <i class="fas fa-search"></i>
          </button>
        </div>

        <!-- 카테고리 필터 및 글쓰기 버튼 -->
        <div class="d-flex align-items-center justify-content-between mb-5">
          <!-- 카테고리 버튼 -->
          <div class="d-flex overflow-auto flex-nowrap me-3">
          </div>
          <button class="btn btn-dark btn-sm fw-bold text-nowrap mt-n2" @click="goWrite">글쓰기</button>
        </div>

        <!-- 게시글 테이블 -->
        <div class="table-responsive">
          <table class="table table-row-dashed table-row-gray-300 align-middle gs-0 gy-4">
            <thead>
            <tr class="fw-normal fs-8 text-gray-500 border-bottom border-gray-200">
              <th class="text-start " style="width: 40%;"><div class="ms-2">제목</div></th>
              <th class="text-start cursor-pointer" style="width: 15%;" @click="sortBy('author')">회원</th>
              <th class="text-start cursor-pointer" style="width: 13%;" @click="sortBy('likes')">
                추천
                <i v-if="sortColumn === 'likes'" :class="sortDirection === 'asc' ? 'ki-duotone ki-up fs-7' : 'ki-duotone ki-down fs-7'"></i>
              </th>
              <th class=" text-start cursor-pointer" style="width: 13%;" @click="sortBy('views')">
                조회
                <i v-if="sortColumn === 'views'" :class="sortDirection === 'asc' ? 'ki-duotone ki-up fs-7' : 'ki-duotone ki-down fs-7'"></i>
              </th>
              <th class=" text-start cursor-pointer" style="width: 20%;" @click="sortBy('date')">
                시간
                <i v-if="sortColumn === 'date'" :class="sortDirection === 'asc' ? 'ki-duotone ki-up fs-7' : 'ki-duotone ki-down fs-7'"></i>
              </th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="post in posts" :key="post.postNumber" @click="goDetail(post.postNumber)" style="cursor:pointer"
                :class="{ 'bg-hover-light-primary': post.category === '공지' }">
              <td class="text-start">
                 <span class="text-gray-800 fw-bold text-hover-primary fs-7">
                  <div class="ms-2">
                  [ {{ post.boardName }} ] {{ post.postTitle }}
                  </div>
                </span>
              </td>
              <td class="text-start"><span class="text-gray-600 fw-semibold d-block fs-7">{{ post.id }}</span></td>
              <td class="text-start">
                <span class="text-gray-600 fw-semibold d-block fs-7">
                  <i class="ki-duotone ki-heart fs-7 me-1 text-danger"></i>
                  {{ post.recommendCount }}
                </span>
              </td>
              <td class="text-start">
                <span class="text-gray-600 fw-semibold d-block fs-7">
                  <i class="ki-duotone ki-eye fs-7 me-1 text-info"></i>
                  {{ post.viewCount }}
                </span>
              </td>
              <td class="text-start"><span class="text-gray-600 fw-semibold d-block fs-7">{{ post.createdAt.slice(2, 10) }}</span></td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue"
import { useRouter } from "vue-router"


import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'

const store = useStore();

const { header } = storeToRefs(store)

onMounted(() => {
  header.value = '게시판'
})


const router = useRouter()

const searchQuery = ref("")
const selectedCategory = ref("전체")
const searchField = ref("title") // ⭐ 'title'로 초기값 변경됨

// 글 분야 표시 부분
const postCategories = ['공지','정보','친목']
const postCategory = ref('공지')


const sortColumn = ref("id")
const sortDirection = ref("desc")

const isModalVisible = ref(false)
const modalTitle = ref('')
const modalMessage = ref('')
const modalType = ref('info')

const posts = ref([])

// ⭐ 정렬 함수
const sortBy = (column) => {
    if (sortColumn.value === column) {
        sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
    } else {
        sortColumn.value = column
        sortDirection.value = 'desc'
    }
}

// ⭐ 검색 필터 이름 매핑 함수
const getSearchFieldName = (field) => {
    const names = {
        title: '제목',
        content: '내용',
        author: '글쓴이',
        title_content: '제목+내용',
        comment: '댓글'
    }
    return names[field] || '제목' // '제목'이 기본값이 되도록 수정
}

const searchPosts = () => {
  console.log("검색 필드:", getSearchFieldName(searchField.value), "검색어:", searchQuery.value)
}

const goWrite = () => {
  router.push("/post/write")
}

const goDetail = (postNumber) => {
  router.push(`/post/${postNumber}`)
}

const goBack = () => {
  router.back()
}
//===========================================================================

import axios from 'axios'

onMounted(() => {
  requestPostList()
})

async function requestPostList() {

    try {
      const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/api/post/getPostList`, 
        {
          headers: { 'Content-Type': 'application/json' },
          timeout: 5000,
        })
        console.log('OK', response.data.result)
        posts.value = response.data.result

    } catch (e) {
      console.error('[Detail] load error:', e)
    } 
  }
async function requestPostListByCondition() {
  const condition = {
    
  }

  try {
    const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}api/post/getPostListByCondition`,
    condition,
      {
        headers: { 'Content-Type': 'application/json' },
        timeout: 5000,
      })
      console.log('OK', response.data.result)

  } catch (e) {
      console.error('[Detail] load error:', e)
  }
}
</script>

<style scoped>
/* 상단 헤더의 제목 중앙 정렬을 위한 설정 */

.page-heading.position-absolute {
    z-index: 10; /* 버튼 위에 표시되도록 z-index 설정 */
    max-width: 70%;
    text-align: center;
}

.input-group .btn.dropdown-toggle.text-dark {
    color: var(--bs-dark) !important;
    background-color: var(--bs-gray-200);
    border-color: var(--bs-gray-500) !important;
}

.btn-dark, .pagination .page-item.active .page-link {
    background-color: var(--bs-dark) !important;
    border-color: var(--bs-dark) !important;
    color: #fff !important;
}

.pagination .page-item .page-link {
    border-radius: 50% !important;
    display: flex;
    justify-content: center;
    align-items: center;
    margin: 0 4px;
    color: var(--bs-gray-700);
}

.table tbody tr[style*="cursor:pointer"]:hover {
    background-color: var(--bs-light) !important;
}

.table tbody tr.bg-hover-light-primary:hover {
  background-color: var(--bs-light-primary) !important;
}
</style>