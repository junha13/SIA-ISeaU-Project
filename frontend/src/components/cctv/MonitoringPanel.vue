<template>
  <div class="card shadow-lg border-0">
    <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
      <span>📹 실시간 CCTV 감시</span>
      <span v-if="detectionActive" class="badge bg-success">AI 감지 중</span>
      <span v-else class="badge bg-secondary">대기</span>
    </div>

    <div class="card-body text-center position-relative">
      <!-- 영상 -->
      <video
          ref="videoRef"
          width="100%"
          height="auto"
          autoplay
          muted
          loop
          playsinline
          class="rounded"
      >
        <source :src="videoSrc" type="video/mp4" />
      </video>

      <!-- AI 감지 결과 Canvas -->
      <canvas ref="canvasRef" class="overlay-canvas"></canvas>

      <!-- 감지 경보 -->
      <div v-if="alertActive" class="alert alert-danger fw-bold mt-3">
        🚨 익수자 의심 감지됨 ({{ Math.round(confidence) }}%)
      </div>

      <!-- 버튼 -->
      <button class="btn btn-outline-primary w-100 mt-3" @click="toggleDetection">
        {{ detectionActive ? '감지 중지' : 'AI 감지 시작' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted, nextTick } from 'vue'
import { useSafetyStore } from '@/stores/safetyStore'
import { useApi } from '@/composables/useApi'
import sampleVideo from '@/assets/video/sample_cctv.mp4'

const safetyStore = useSafetyStore()
const { get } = useApi()

const videoSrc = sampleVideo
const videoRef = ref(null)
const canvasRef = ref(null)

const detectionActive = ref(false)
const alertActive = ref(false)
const confidence = ref(0)
let interval = null

/**
 * 감지 결과 시각화 함수
 * YOLO: bounding box
 * OpenPose: keypoints + skeleton line
 */
const drawDetections = (detections = []) => {
  const canvas = canvasRef.value
  const ctx = canvas?.getContext('2d')
  const video = videoRef.value
  if (!video || !canvas || !ctx) return

  canvas.width = video.videoWidth
  canvas.height = video.videoHeight
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  detections.forEach((det) => {
    // YOLO bounding box
    if (det.x && det.y) {
      ctx.strokeStyle = det.label === 'person' ? 'red' : 'lime'
      ctx.lineWidth = 3
      ctx.strokeRect(det.x, det.y, det.width, det.height)
      ctx.font = 'bold 14px Inter'
      ctx.fillStyle = 'rgba(255,0,0,0.7)'
      ctx.fillText(`${det.label} (${Math.round(det.confidence * 100)}%)`, det.x + 5, det.y + 20)
    }

    // OpenPose keypoints (스켈레톤)
    if (det.keypoints && Array.isArray(det.keypoints)) {
      ctx.strokeStyle = 'cyan'
      ctx.lineWidth = 2
      ctx.beginPath()
      det.keypoints.forEach((p, i) => {
        if (i === 0) ctx.moveTo(p.x, p.y)
        else ctx.lineTo(p.x, p.y)
        ctx.fillStyle = 'yellow'
        ctx.beginPath()
        ctx.arc(p.x, p.y, 4, 0, 2 * Math.PI)
        ctx.fill()
      })
      ctx.stroke()
    }
  })
}

/**
 * AI 감지 시작
 */
const startDetection = async () => {
  detectionActive.value = true

  interval = setInterval(async () => {
    try {
      const res = await get('/api/safety/drowning-alert', { cctvId: 1 })
      if (res.status === 'alert') {
        alertActive.value = true
        confidence.value = res.confidence
        safetyStore.setAlert(res)
        drawDetections(res.detections || [])
      } else {
        alertActive.value = false
      }
    } catch (err) {
      console.error('AI 감지 요청 실패:', err)
    }
  }, 3000)
}

/**
 * 감지 중지
 */
const stopDetection = () => {
  detectionActive.value = false
  clearInterval(interval)
  interval = null
  const canvas = canvasRef.value
  if (canvas) {
    const ctx = canvas.getContext('2d')
    ctx.clearRect(0, 0, canvas.width, canvas.height)
  }
}

/**
 * 버튼 토글
 */
const toggleDetection = () => {
  detectionActive.value ? stopDetection() : startDetection()
}

onUnmounted(stopDetection)
</script>

<style scoped>
.card {
  max-width: 680px;
  margin: auto;
}

.overlay-canvas {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
}

.alert {
  animation: blink 1s step-end infinite alternate;
}

@keyframes blink {
  from { opacity: 1; }
  to { opacity: 0.4; }
}

@media (max-width: 768px) {
  .card {
    max-width: 100%;
  }
}
</style>
