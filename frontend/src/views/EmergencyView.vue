<template>
  <div class="container py-4" style="max-width:720px;">
    <h3 class="fw-bold text-danger mb-4 text-center">🚨 긴급 신고</h3>

    <div class="card shadow-sm border-0">
      <div class="card-body">
        <div class="d-flex align-items-center gap-2 text-muted mb-3">
          <i class="bi bi-geo-alt"></i>
          <span>현재 위치 기반으로 구조 요청이 전송됩니다.</span>
        </div>

        <form @submit.prevent="send">
          <div class="row g-2">
            <div class="col-12 col-md-6">
              <label class="form-label fw-bold">위도</label>
              <input type="text" class="form-control" :value="latText" disabled />
            </div>
            <div class="col-12 col-md-6">
              <label class="form-label fw-bold">경도</label>
              <input type="text" class="form-control" :value="lngText" disabled />
            </div>
          </div>

          <div class="mt-3">
            <label class="form-label fw-bold">상황 설명</label>
            <textarea v-model="detail" class="form-control" rows="4" placeholder="상황을 간단히 입력하세요" required></textarea>
          </div>

          <button class="btn btn-danger w-100 fw-bold py-2 mt-3" :disabled="sending">
            <span v-if="sending" class="spinner-border spinner-border-sm me-2"></span>
            🚨 SOS 전송
          </button>
        </form>
      </div>
    </div>

    <ConfirmModal
        :isVisible="doneVisible"
        title="신고 완료"
        message="신고가 접수되었습니다. 홈으로 돌아가시겠습니까?"
        @update:isVisible="doneVisible = $event"
        @confirm="toHome"
    />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { postSOS } from '@/api/user'
import ConfirmModal from '@/components/common/ConfirmModal.vue'

const router = useRouter()
const user = useUserStore()

const detail = ref('')
const sending = ref(false)
const doneVisible = ref(false)

const latText = computed(() => user.currentLocation?.lat?.toFixed(6) ?? '-')
const lngText = computed(() => user.currentLocation?.lng?.toFixed(6) ?? '-')

const send = async () => {
  try {
    sending.value = true
    const { lat = 0, lng = 0 } = user.currentLocation || {}
    await postSOS(lat, lng, detail.value)
    doneVisible.value = true
  } catch (e) {
    alert(e?.message || '신고 전송 실패')
  } finally {
    sending.value = false
  }
}

const toHome = () => { doneVisible.value = false; router.push('/') }
</script>
