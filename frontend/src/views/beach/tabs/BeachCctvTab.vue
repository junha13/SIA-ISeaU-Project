<template>
  <section class="p-3">
    <!-- 해수욕장 바뀔 때만 리마운트 -->
    <YouTubeLive
      v-if="cctvUrl"
      :url="cctvUrl"
      :key="beachKey"
      :ratio="'16/9'"
      :autoplay="true"
      :muted="true"
      :controlsMinimal="true"
      :nocookie="true"
    />
    <div v-else class="text-muted small">
      CCTV 정보가 없습니다.
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import YouTubeLive from '@/components/YouTubeLive.vue'
import { CCTV_BY_BEACH_NUMBER, CCTV_BY_BEACH_NAME } from '@/constants/cctvMap.js'
const props = defineProps({
  beach: { type: Object, required: true }
})

/** 백엔드 응답 키 유연 대응 */
const cctvUrl = computed(() => {
  const b = props.beach || {}
  const numberKey = b?.beachNumber != null ? String(b.beachNumber) : ''
  const nameKey = (b?.beachName || b?.name || '').trim()
  return (
    b.cctvUrl ||
    b?.cctv?.youtubeUrl ||
    b.cctv_link ||
    b.youtubeUrl ||
    (numberKey && CCTV_BY_BEACH_NUMBER[numberKey]) ||
     (nameKey && CCTV_BY_BEACH_NAME[nameKey]) ||

    ''
  )
})

/** key는 해수욕장 단위로만(탭 이동 시 리마운트 방지) */
const beachKey = computed(() => props.beach?.beachNumber ?? props.beach?.id ?? '')
</script>

<style scoped>
/* 필요시 스타일 보강 */
</style>
