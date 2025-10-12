<template>
  <div class="d-flex align-items-center justify-content-center" style="min-height:65vh;">
    <div class="card shadow-sm p-4" style="width:360px;">
      <h5 class="fw-bold text-center text-primary mb-4">로그인</h5>

      <form @submit.prevent="login">
        <div class="mb-3">
          <label class="form-label fw-bold">이메일</label>
          <input v-model.trim="email" type="email" class="form-control" placeholder="user@example.com" required />
        </div>
        <div class="mb-3">
          <label class="form-label fw-bold">비밀번호</label>
          <input v-model="password" type="password" class="form-control" placeholder="********" required />
        </div>

        <button class="btn btn-primary w-100 fw-bold py-2" :disabled="loading">
          <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
          로그인
        </button>
      </form>

      <div v-if="error" class="alert alert-danger mt-3">{{ error }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
// 필요 시 실제 로그인 API로 교체 가능
// import { useApi } from '@/composables/useApi'
// const api = useApi()

const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

const login = async () => {
  try {
    loading.value = true
    error.value = ''
    // const res = await api.post('/api/auth/login', { email: email.value, password: password.value })
    // 토큰 처리/스토어 저장 등…
    await new Promise(r => setTimeout(r, 800)) // 데모 딜레이
    window.location.href = '/'
  } catch (e) {
    error.value = e?.message || '로그인 실패'
  } finally {
    loading.value = false
  }
}
</script>
