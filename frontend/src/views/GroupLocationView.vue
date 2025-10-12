<template>
  <div class="container py-3">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h3 class="fw-bold text-primary mb-0">👥 그룹 위치 공유</h3>
      <button class="btn btn-outline-primary btn-sm" @click="reload" :disabled="loading">
        <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
        새로고침
      </button>
    </div>

    <div v-if="error" class="alert alert-danger">{{ error }}</div>

    <div class="row g-3">
      <!-- 구성원 리스트 -->
      <div class="col-12 col-lg-6">
        <MemberLocation :members="members" />
      </div>

      <!-- 간단 미니맵(선택: Naver Map SDK를 또 써도 됨, 여기선 placeholder) -->
      <div class="col-12 col-lg-6">
        <div class="card shadow-sm border-0 h-100">
          <div class="card-body d-flex justify-content-center align-items-center text-muted">
            <div class="text-center">
              <i class="bi bi-map fs-1 d-block mb-2"></i>
              <div>지도 미리보기 (필요 시 별도 컴포넌트로)</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 시작 안내 -->
    <div class="alert alert-info mt-3" v-if="!members.length && !loading">
      아직 구성원 데이터가 없습니다. 그룹에 참여하거나 위치 공유를 활성화하세요.
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useGroupStore } from '@/stores/groupStore'
import MemberLocation from '@/components/user/MemberLocation.vue'

const group = useGroupStore()
const members = ref([])
const loading = ref(false)
const error = ref('')

const load = async () => {
  try {
    loading.value = true
    error.value = ''
    members.value = await group.fetchMembers()
  } catch (e) {
    error.value = e?.message || '구성원 불러오기 실패'
  } finally {
    loading.value = false
  }
}
const reload = () => load()

onMounted(load)
</script>
