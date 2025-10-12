<template>
  <div class="d-flex flex-column align-items-center justify-content-center vh-100">
    <div class="card p-4 shadow" style="width: 380px;">
      <h3 class="text-center fw-bold text-primary mb-4">🔐 로그인</h3>

      <form @submit.prevent="login">
        <div class="mb-3">
          <label class="form-label">이메일</label>
          <input v-model="email" type="email" class="form-control" required />
        </div>
        <div class="mb-4">
          <label class="form-label">비밀번호</label>
          <input v-model="password" type="password" class="form-control" required />
        </div>
        <button class="btn btn-primary w-100 fw-bold">로그인</button>
      </form>
    </div>

    <ConfirmModal
        v-if="showConfirm"
        title="로그인 성공"
        message="홈으로 이동하시겠습니까?"
        @confirm="goHome"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import ConfirmModal from '@/components/common/ConfirmModal.vue'
import * as bootstrap from 'bootstrap'

const router = useRouter()
const email = ref('')
const password = ref('')
const showConfirm = ref(false)

const login = () => {
  if (email.value && password.value) {
    showConfirm.value = true
    const modal = new bootstrap.Modal(document.getElementById('confirmModal'))
    modal.show()
  } else {
    alert('이메일과 비밀번호를 입력하세요.')
  }
}

const goHome = () => router.push('/')
</script>
