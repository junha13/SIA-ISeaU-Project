<template>
  <div class="card shadow-sm border-0">
    <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
      <span>📹 실시간 CCTV 감시</span>
      <span v-if="detectionActive" class="badge bg-success">AI 감지 중</span>
      <span v-else class="badge bg-secondary">대기</span>
    </div>

    <div class="card-body text-center">
      <video
          ref="videoRef"
          width="100%"
          height="auto"
          controls
          autoplay
          muted
          loop
          class="rounded mb-3"
      >
        <source src="/assets/video/sample_cctv.mp4" type="video/mp4" />
      </video>

      <div v-if="alertActive" class="alert alert-danger fw-bold">
        🚨 익수자 의심 감지됨 ({{ confidence }}%)
      </div>

      <button class="btn btn-outline-primary w-100 mt-2" @click="toggleDetection">
        {{ detectionActive ? '감지 중지' : 'AI 감지 시작' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useSafetyStore } from '@/stores/safetyStore'
import { useApi } from '@/composables/useApi'

const safetyStore = useSafetyStore()
const { get } = useApi()
const detectionActive = ref(false)
const alertActive = ref(false)
const confidence = ref(0)
let interval = null

const startDetection = async () => {
  detectionActive.value = true
  interval = setInterval(async () => {
    const res = await get('/api/safety/drowning-alert', { cctvId: 1 })
    if (res.status === 'alert') {
      alertActive.value = true
      confidence.value = res.confidence
      safetyStore.setAlert(res)
    } else {
      alertActive.value = false
    }
  }, 4000)
}

const stopDetection = () => {
  detectionActive.value = false
  clearInterval(interval)
  interval = null
}

const toggleDetection = () => {
  if (detectionActive.value) stopDetection()
  else startDetection()
}

onUnmounted(stopDetection)
</script>

<style scoped>
video {
  border-radius: 10px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
}
</style>
