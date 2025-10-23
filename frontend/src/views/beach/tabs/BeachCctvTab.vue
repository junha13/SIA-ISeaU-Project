<!-- src/views/beach/tabs/BeachCctvTab.vue -->
<template>
  <section class="p-3">
    <template v-if="cctvUrl">
      <!-- 1) 유튜브 링크면 기존 컴포넌트로 재생 -->
      <YouTubeLive
        v-if="isYouTube"
        :url="cctvUrl"
        :key="beachKey"
        :ratio="'16/9'"
        :autoplay="true"
        :muted="true"
        :controlsMinimal="true"
        :nocookie="true"
      />

      <!-- 2) 그 외(예: nowjeju/bangjae 등)엔 페이지를 iframe으로 임베드 -->
      <iframe
        v-else
        :src="cctvUrl"
        title="CCTV"
        allow="autoplay; encrypted-media; picture-in-picture; fullscreen"
        referrerpolicy="origin-when-cross-origin"
        allowfullscreen
        style="width:100%; height:56vw; max-height:70vh; border:0; border-radius:12px; box-shadow:0 4px 10px rgba(0,0,0,.2)"
      ></iframe>
    </template>

    <div v-else class="text-muted small">CCTV 정보가 없습니다.</div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import YouTubeLive from '@/components/YouTubeLive.vue'
import { CCTV_BY_BEACH_NUMBER, CCTV_BY_BEACH_NAME } from '@/constants/cctvMap.js'

const props = defineProps({
  beach: { type: Object, required: true }
})

/** 백엔드 응답 키 유연 대응 + 매핑 fallback */
const cctvUrl = computed(() => {
  const b = props.beach || {}
  const numberKey = b?.beachNumber != null ? String(b.beachNumber) : ''
  const nameKey   = (b?.beachName || b?.name || '').trim()
  return (
    b.cctvUrl ||
    b?.cctv?.youtubeUrl ||
    b.cctv_link ||
    b.youtubeUrl ||
    (numberKey && CCTV_BY_BEACH_NUMBER[numberKey]) ||
    (nameKey   && CCTV_BY_BEACH_NAME[nameKey]) ||
    ''
  )
})

/** 유튜브 여부 감지 */
const isYouTube = computed(() =>
  /youtu(\.be|be\.com)/i.test(cctvUrl.value || '')
)

/** key는 해수욕장 단위(탭 이동 시 리마운트 방지) */
const beachKey = computed(() => props.beach?.beachNumber ?? props.beach?.id ?? '')
</script>

<style scoped>
/* 필요시 스타일 보강 */
</style>
