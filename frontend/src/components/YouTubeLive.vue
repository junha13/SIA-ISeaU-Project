<!-- YouTubeLive.vue -->
<template>
  <!-- ─────────────────────────────────────────────────────────────
       [TEMPLATE] 반응형 비율 컨테이너 + YouTube iframe
       - containerStyle: 비율(16/9 등)을 유지하기 위한 인라인 스타일
       - :key="videoId": videoId가 바뀌면 iframe을 새로 마운트 → 상태 꼬임 방지
       - :src="embedUrl": 아래 script에서 계산한 최종 임베드 주소
       ───────────────────────────────────────────────────────────── -->
  <div class="yt-container" :style="containerStyle">
    <iframe
      :key="videoId"
      :src="embedUrl"
      title="Beach CCTV Live"
      allow="autoplay; encrypted-media; picture-in-picture"
      referrerpolicy="origin-when-cross-origin"
      allowfullscreen
    ></iframe>
  </div>
</template>

<script setup>
/* ────────────────────────────────────────────────────────────────
   [SCRIPT SETUP] Vue 3 <script setup> 문법
   - defineProps로 외부에서 받을 옵션 정의
   - computed로 파생 데이터 계산(임베드 URL, 비율 등)
   - URL → videoId 추출 유틸리티 함수 포함
   ──────────────────────────────────────────────────────────────── */
import { computed } from 'vue'

/* ────────────────────────────────────────────────────────────────
   [PROPS] 외부에서 조절 가능한 옵션
   - url / videoId: 둘 중 하나만 주면 됨(둘 다 주면 url이 우선)
   - ratio: 가로/세로 비율 문자열. 예: '16/9', '4/3', '1/1'
   - nocookie: youtube-nocookie 도메인 사용 → 쿠키 최소화(프라이버시 강화)
   - controlsMinimal: 재생 UI 최소화(발표/전시용)
   - autoplay + muted: 자동재생은 보통 muted가 켜져 있어야 안정적으로 동작
   ──────────────────────────────────────────────────────────────── */
const props = defineProps({
  url: { type: String, default: '' }, // 예: https://www.youtube.com/live/<id>
  videoId: { type: String, default: '' },    // 예: mUyJ30QxPMg
  ratio: { type: String, default: '16/9' },  // 예: '16/9', '4/3', '1/1'
  nocookie: { type: Boolean, default: false },
  controlsMinimal: { type: Boolean, default: true },
  autoplay: { type: Boolean, default: true },
  muted: { type: Boolean, default: true }
})

/* ────────────────────────────────────────────────────────────────
   [UTIL] URL에서 YouTube videoId 추출
   - 지원 형태:
     • https://youtu.be/<id>
     • https://www.youtube.com/watch?v=<id>
     • https://www.youtube.com/live/<id>
     • https://www.youtube.com/embed/<id>
   - URL 파싱 실패 시: 이미 id가 들어왔다고 가정하고 그대로 반환
   ──────────────────────────────────────────────────────────────── */
function extractVideoId(u) {
  if (!u) return ''
  try {
    const parsed = new URL(u)

    // 1) youtu.be/<id>
    if (parsed.hostname.includes('youtu.be')) {
      return parsed.pathname.replace('/', '')
    }

    // 2) watch?v=<id>
    const v = parsed.searchParams.get('v')
    if (v) return v

    // 3) /live/<id>, /embed/<id> 등: 마지막 경로 세그먼트 사용
    const seg = parsed.pathname.split('/').filter(Boolean).pop()
    return seg || ''
  } catch {
    // URL 생성 실패 → 문자열 자체를 id로 간주
    return u
  }
}

/* ────────────────────────────────────────────────────────────────
   [COMPUTED] videoId / 임베드 베이스 / 최종 임베드 URL
   - id: url이 있으면 url에서 추출, 없으면 videoId prop 사용
   - embedBase: nocookie 여부에 따라 도메인 분기
   - embedUrl: autoplay/mute/controls 등 URLSearchParams로 구성
   ──────────────────────────────────────────────────────────────── */
const id = computed(() => (props.url ? extractVideoId(props.url) : props.videoId))
const videoId = computed(() => (id.value || '').trim())

const embedBase = computed(() =>
  props.nocookie ? 'https://www.youtube-nocookie.com/embed' : 'https://www.youtube.com/embed'
)

const embedUrl = computed(() => {
  if(!videoId.value) return 'about:blank'
  // 유튜브 임베드 파라미터 구성
  const params = new URLSearchParams({
    autoplay: props.autoplay ? '1' : '0',       // 자동재생
    mute: props.muted ? '1' : '0',             // 브라우저 정책 때문에 자동재생 시 보통 필요
    playsinline: '1',                           // iOS에서 전체화면 대신 인라인 재생 허용
    ...(props.controlsMinimal
      ? { controls: '0', rel: '0', modestbranding: '1' } // 최소 UI(재생 컨트롤 숨김 등)
      : {})
  })

  // 최종 임베드 주소(예: https://www.youtube.com/embed/<id>?autoplay=1&...)
  return `${embedBase.value}/${encodeURIComponent(videoId.value)}?${params.toString()}`
})

/* ────────────────────────────────────────────────────────────────
   [ASPECT RATIO] 화면 비율 유지
   - parseRatio: '16/9' → { w:16, h:9 }
   - containerStyle: CSS 변수(--w, --h)와 padding-bottom(구형 브라우저용) 전달
   ──────────────────────────────────────────────────────────────── */
function parseRatio(r) {
  const [w, h] = (r || '16/9').split('/').map(n => Number(n) || 1)
  return { w, h }
}
const wh = computed(() => parseRatio(props.ratio))

const containerStyle = computed(() => ({
  '--w': wh.value.w,                               // CSS 변수: 가로
  '--h': wh.value.h,                               // CSS 변수: 세로
  // 구형 브라우저 fallback: aspect-ratio 미지원 시 padding-bottom으로 높이 확보
  paddingBottom: `calc(100% * var(--h) / var(--w))`
}))
</script>

<style scoped>
/* ────────────────────────────────────────────────────────────────
   [STYLE] 반응형 비율 컨테이너 + iframe 채우기
   - 최신 브라우저: aspect-ratio 사용
   - 구형 브라우저: height:0 + padding-bottom 트릭(상단 script에서 계산)
   ──────────────────────────────────────────────────────────────── */
.yt-container {
  position: relative;
  width: 100%;
  /* 최신 브라우저: CSS 변수로 비율 지정 */
  aspect-ratio: var(--w) / var(--h);
}

.yt-container iframe {
  position: absolute;
  inset: 0;                /* top/right/bottom/left: 0 */
  width: 100%;
  height: 100%;
  border: 0;
  border-radius: 12px;     /* 모서리 둥글게 */
  box-shadow: 0 4px 10px rgba(0,0,0,.2); /* 은은한 그림자 */
}

/* 구형 브라우저 fallback: aspect-ratio 미지원 시 */
@supports not (aspect-ratio: 1 / 1) {
  .yt-container {
    height: 0; /* 실제 높이는 padding-bottom으로 확보(인라인 style로 전달됨) */
  }
}
</style>
