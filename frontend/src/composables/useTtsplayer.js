// src/composables/useTtsPlayer.js
import { ref } from 'vue'
import axios from 'axios'

export function useTtsPlayer() {
  const isPlaying = ref(false)

  const speak = async (text) => {
    const msg = text?.trim()
    if (!msg) return

    try {
      isPlaying.value = true

      // ★ TTS API 호출 – responseType 꼭 arraybuffer!
      const res = await axios.post(
        '/api/tts',            // 실제 TTS 엔드포인트로 바꿔줘
        { text: msg },
        { responseType: 'arraybuffer' }
      )

      // ★ audio/mpeg 으로 Blob 만들기
      const blob = new Blob([res.data], { type: 'audio/mpeg' })
      const url = URL.createObjectURL(blob)

      const audio = new Audio(url)

      // 재생
      await audio.play()

      audio.onended = () => {
        URL.revokeObjectURL(url)
        isPlaying.value = false
      }
      audio.onerror = (e) => {
        console.error('오디오 재생 오류:', e)
        URL.revokeObjectURL(url)
        isPlaying.value = false
      }
    } catch (err) {
      console.error('TTS 요청/재생 오류:', err)
      isPlaying.value = false
    }
  }

  return {
    isPlaying,
    speak,
  }
}
