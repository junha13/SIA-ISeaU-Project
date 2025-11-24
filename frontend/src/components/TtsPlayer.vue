<script setup>
import { ref, watch } from "vue"

// 부모에서 audioContent를 받아오기
const props = defineProps({
  audioContent: {
    type: String,
    default: null,
  },
})

// <audio>에 넣을 blob URL
const audioUrl = ref(null)

watch(
  () => props.audioContent,
  (val) => {
    if (!val) {
      audioUrl.value = null
      return
    }

    try {
      // 1) Base64 → 바이너리 문자열
      const byteString = atob(val)

      // 2) 바이너리 문자열 → 실제 바이트 배열
      const len = byteString.length
      const bytes = new Uint8Array(len)
      for (let i = 0; i < len; i++) {
        bytes[i] = byteString.charCodeAt(i)
      }

      // 3) 바이트 배열 → Blob (오디오 파일처럼)
      const blob = new Blob([bytes], { type: "audio/mpeg" })

      // 기존 URL 있으면 정리
      if (audioUrl.value) {
        URL.revokeObjectURL(audioUrl.value)
      }

      // 4) Blob → blob:... 형태의 URL 만들기
      audioUrl.value = URL.createObjectURL(blob)
    } catch (e) {
      console.error("오디오 변환 실패:", e)
      audioUrl.value = null
    }
  },
  { immediate: true }
)
</script>

<template>
  <div>
    <!-- 오디오가 준비되어 있으면 -->
    <audio
      v-if="audioUrl"
      :src="audioUrl"
      controls
      autoplay
    />
    <!-- 아직 변환된 음성이 없으면 -->
    <div v-else class="text-muted small">
      아직 변환된 음성이 없습니다.
    </div>
  </div>
</template>
