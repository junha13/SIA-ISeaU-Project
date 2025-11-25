<template>
  <div class="voice-page container py-4">
    <h3 class="mb-3 fw-bold">원격 안내 방송 수신 (모바일)</h3>

    <p class="small text-muted mb-2">
      이 페이지를 휴대폰 브라우저에서 열어두면,
      관제센터에서 안내방송을 발송할 때마다 자동으로 음성이 재생됩니다.
    </p>

    <div class="mb-2">
      <span class="badge" :class="statusBadgeClass">
        {{ connectionStatus }}
      </span>
    </div>

    <div class="card p-3">
      <h6 class="fw-semibold mb-2">마지막으로 받은 방송 내용</h6>
      <div v-if="lastMessageText" class="mb-2">
        <p class="mb-1">{{ lastMessageText }}</p>
        <small class="text-muted">({{ lastReceivedTime }})</small>
      </div>
      <div v-else class="text-muted small">
        아직 수신된 방송이 없습니다. 관제 화면에서 안내 방송을 발송하면 여기에 표시됩니다.
      </div>

      <!-- 🔊 실제 음성 재생 -->
      <div class="mt-3">
        <TtsPlayer :audio-content="ttsAudioBase64" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue"
import TtsPlayer from "@/components/TtsPlayer.vue"

// 🔊 서버에서 온 Base64 오디오
const ttsAudioBase64 = ref(null)

// 안내 문구 표시용
const lastMessageText = ref("")
const lastReceivedTime = ref("")

// 연결 상태 표시
const connectionStatus = ref("연결 중...")
let socket = null

const statusBadgeClass = computed(() => {
  switch (connectionStatus.value) {
    case "연결됨":
      return "bg-success"
    case "연결 끊김":
    case "연결 실패":
      return "bg-danger"
    default:
      return "bg-secondary"
  }
})

onMounted(() => {
  // 🌐 VITE_API_BASE_URL 기반으로 ws 주소 만들기
  // 예) http://localhost:8080  ->  ws://localhost:8080
  const httpBase = import.meta.env.VITE_API_BASE_URL
  const wsBase = httpBase.replace("http", "ws")
  const wsUrl = `${wsBase}/ws/voice` // 🔴 서버에서 이 주소로 WebSocket 열어줄 예정

  console.log("[Voice.vue] WebSocket 연결 시도:", wsUrl)

  socket = new WebSocket(wsUrl)

  socket.onopen = () => {
    console.log("[Voice.vue] WebSocket 연결 완료")
    connectionStatus.value = "연결됨"
  }

  socket.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      console.log("[Voice.vue] 수신 데이터:", data)

      // 서버에서 이렇게 쏜다고 가정:
      // { type: "TTS", message: "...", audioContent: "AAAA..." }
      if (data.type === "TTS" && data.audioContent) {
        ttsAudioBase64.value = data.audioContent
        lastMessageText.value = data.message || "(문구 없음)"
        lastReceivedTime.value = new Date().toLocaleTimeString("ko-KR", {
          hour12: false,
          hour: "2-digit",
          minute: "2-digit",
          second: "2-digit",
        })
      }
    } catch (e) {
      console.error("[Voice.vue] 메시지 파싱 실패:", e)
    }
  }

  socket.onerror = (err) => {
    console.error("[Voice.vue] WebSocket 에러:", err)
    connectionStatus.value = "연결 실패"
  }

  socket.onclose = () => {
    console.log("[Voice.vue] WebSocket 종료")
    connectionStatus.value = "연결 끊김"
  }
})

onMounted(() => {
  // 🌐 현재 페이지 기준으로 ws / wss 주소 만들기
  const protocol = window.location.protocol === "https:" ? "wss" : "ws"
  const host = window.location.host           // 예: localhost:5173, iseau.com
  const wsUrl = `${protocol}://${host}/ws/voice`

  console.log("[Voice.vue] WebSocket 연결 시도:", wsUrl)

  socket = new WebSocket(wsUrl)

  socket.onopen = () => {
    console.log("[Voice.vue] WebSocket 연결 완료")
    connectionStatus.value = "연결됨"
  }

  socket.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      console.log("[Voice.vue] 수신 데이터:", data)

      if (data.type === "TTS" && data.audioContent) {
        ttsAudioBase64.value = data.audioContent
        lastMessageText.value = data.message || "(문구 없음)"
        lastReceivedTime.value = new Date().toLocaleTimeString("ko-KR", {
          hour12: false,
          hour: "2-digit",
          minute: "2-digit",
          second: "2-digit",
        })
      }
    } catch (e) {
      console.error("[Voice.vue] 메시지 파싱 실패:", e)
    }
  }

  socket.onerror = (err) => {
    console.error("[Voice.vue] WebSocket 에러:", err)
    connectionStatus.value = "연결 실패"
  }

  socket.onclose = () => {
    console.log("[Voice.vue] WebSocket 종료")
    connectionStatus.value = "연결 끊김"
  }
})

</script>

<style scoped>
.voice-page {
  max-width: 600px;
}
</style>
