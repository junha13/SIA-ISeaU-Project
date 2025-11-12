<!-- StreamGrid.vue -->
<template>
  <div class="row g-3">
    <!-- camIds로 전달된 카메라 ID 리스트(예: [1,2,3,4])를 순회하며 2x2 그리드 생성 -->
    <div v-for="id in camIds" :key="id" class="col-6">
      <!-- 16:9 비율을 유지하는 영상 컨테이너 -->
      <div class="position-relative bg-dark" style="aspect-ratio:16/9;">
        <!-- 각 스트림의 최신 JPEG Blob URL을 표시, 없으면 플레이스홀더 -->
        <img
          :src="streams[`stream${id}`]?.videoSrc || placeholder(id)"
          class="w-100 h-100"
          style="object-fit:contain;"
          alt="video"
        />
        <!-- 좌상단: CAM 라벨 -->
        <span class="badge text-bg-secondary position-absolute top-0 start-0 m-2">
          CAM {{ id }}
        </span>
        <!-- 우하단: WebSocket 연결 상태 표시 -->
        <span
          class="badge bg-opacity-75 position-absolute bottom-0 end-0 m-2"
          :class="status === 'ok' ? 'text-bg-success' : 'text-bg-warning'"
        >
          {{ status }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
/* 이 컴포넌트는 WebSocket으로 전달되는 다중 스트림(JPEG 프레임 + JSON 메타데이터)을 받아
   각 스트림의 최신 프레임을 이미지로 렌더링합니다. */

import { reactive, ref, onMounted, onUnmounted } from 'vue';

const props = defineProps({
  // WebSocket 서버 주소 (FastAPI 등) — 필요 시 상위에서 주입
  wsUrl: { type: String, default: 'ws://localhost:8000/ws/stream' },
  // CAM 번호 리스트 — CAM n ↔ streamn 매핑 (예: CAM1→stream1)
  camIds: { type: Array, default: () => [1, 2, 3, 4] },
});

/* camIds([1,2,3,4])를 streamIds(['stream1',...])로 변환 */
const streamIds = props.camIds.map(n => `stream${n}`);

/* 각 스트림의 상태 보관소
   - videoSrc: 최근 수신한 JPEG Blob URL
   - people, motion: 서버가 보내는 메타데이터(사람 수, 움직임 픽셀 등) */
const streams = reactive(
  Object.fromEntries(
    streamIds.map(id => [id, { id, videoSrc: '', people: 0, motion: 0 }])
  )
);

/* WebSocket 연결 상태 표시
   - 'connecting' 접속 중
   - 'ok' 연결 정상
   - 'reconnect' 재접속 대기 */
const status = ref('connecting');

/* WebSocket 핸들/임시 프레임/활성 Blob URL 맵 */
let ws;
let tmpFrame = null;          // 방금 수신한 JPEG 프레임(아직 어느 스트림 것인지 모름)
const activeBlob = {};        // streamId별 현재 화면에 표시 중인 Blob URL (메모리 해제를 위해 추적)

/* Blob URL 해제 유틸리티 (메모리 누수 방지) */
const revoke = (u) => u && URL.revokeObjectURL(u);

/* 프레임이 아직 없을 때 보여줄 플레이스홀더 이미지 */
const placeholder = (id) => `https://placehold.co/640x360/000/FFF?text=stream${id}`;

/* WebSocket 연결 로직
   서버 메시지 가정:
   - 바이너리: JPEG 프레임 (ArrayBuffer)
   - 텍스트: JSON 메타 { stream_id, people, motion }
   처리 흐름:
   1) JPEG 프레임 도착 → tmpFrame에 Blob URL 저장
   2) JSON 메타 도착 → 해당 stream_id의 상태 갱신 + tmpFrame을 그 스트림의 videoSrc로 바인딩 */
function connect() {
  try { ws && ws.close(); } catch {}
  status.value = 'connecting';

  ws = new WebSocket(props.wsUrl);
  ws.binaryType = 'arraybuffer';

  ws.onopen = () => { status.value = 'ok'; };

  ws.onmessage = (e) => {
    if (e.data instanceof ArrayBuffer) {
      // 새 JPEG 프레임 수신 → 이전 임시 URL 해제 후 교체
      revoke(tmpFrame);
      tmpFrame = URL.createObjectURL(new Blob([e.data], { type: 'image/jpeg' }));
      return;
    }
    // JSON 메타 처리
    try {
      const { stream_id, people = 0, motion = 0 } = JSON.parse(e.data);
      const s = streams[stream_id];
      if (!s) return;
      s.people = people;
      s.motion = motion;

      // 직전에 받은 프레임(tmpFrame)을 해당 스트림에 적용
      if (tmpFrame) {
        revoke(activeBlob[stream_id]); // 이전 표시 프레임 해제
        s.videoSrc = tmpFrame;         // 화면 교체
        activeBlob[stream_id] = tmpFrame;
        tmpFrame = null;               // 소비 완료
      }
    } catch {
      // JSON 파싱 실패는 무시
    }
  };

  ws.onclose = () => {
    // 연결 종료 시 자동 재접속 (3초 후)
    status.value = 'reconnect';
    setTimeout(connect, 3000);
  };
}

/* 컴포넌트가 마운트되면 연결, 언마운트 시 정리 */
onMounted(connect);
onUnmounted(() => {
  try { ws && ws.close(); } catch {}
  Object.values(activeBlob).forEach(revoke);
  revoke(tmpFrame);
});
</script>

<style scoped>
/* 배지 텍스트가 드래그되지 않도록 */
.badge { user-select: none; }
</style>