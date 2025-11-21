<template>
  <div class="row g-3">
    <!-- camIds 예: [1,2,3,4] → 2x2 카드 렌더 -->
    <div v-for="id in camIds" :key="id" class="col-6">
      <!-- 16:9 고정 비율 컨테이너 -->
      <div
        class="position-relative bg-dark stream-tile"
        style="aspect-ratio:4/3;"
        @click="setCctvName(state[`s${id}`]?.label)"
      >
        <!-- 최신 JPEG Blob URL 표시 (없으면 플레이스홀더) -->
        <img
          :src="state[`s${id}`]?.src || placeholder(id)"
          class="w-100 h-100"
          style="object-fit:contain"
          alt="video"
        />
        <!-- 좌상단: CAM 라벨 -->
        <span class="badge text-bg-secondary position-absolute top-0 start-0 m-2">
          {{ state[`s${id}`]?.label }}
        </span>
        <!-- 우하단: 연결 상태 배지 (ok / reconnect) -->
        <span
          class="badge bg-opacity-75 position-absolute bottom-0 end-0 m-2"
          :class="state[`s${id}`]?.ok ? 'text-bg-success' : 'text-bg-warning'"
        >
          {{ state[`s${id}`]?.ok ? 'ok' : 'reconnect' }}
        </span>
      </div>
    </div>
  </div>

  <!-- ✅ 클릭 시 띄울 전체 화면 모달    // @click="openModal(id)"
  <div v-if="showModal" class="modal-backdrop" @click.self="closeModal">
    <div class="modal-body-box">
      모달 헤더 : CAM 번호 + 닫기 버튼
      <div class="d-flex justify-content-between align-items-center mb-2">
        <h5 class="mb-0">CAM {{ selectedCamId }}</h5>
        <button type="button" class="btn btn-sm btn-light" @click="closeModal">
          닫기
        </button>
      </div>

      모달 안에 크게 보이는 영상
      <div class="modal-video-wrapper">
        <img
          v-if="selectedState"
          :src="selectedState.src || placeholder(selectedCamId)"
          class="w-100 h-100"
          style="object-fit:contain;"
          alt="modal-video"
        />
      </div>

       선택된 스트림의 부가 정보(people/motion 등을 나중에 추가할 수 있음)
      <div v-if="selectedState && selectedState.people != null" class="mt-2 small text-muted">
        사람 수: {{ selectedState.people }} /
        위험구역 인원: {{ selectedState.danger ?? 0 }}
      </div>
    </div>
  </div> -->
</template>

<script setup>
import { reactive, onMounted, onUnmounted, ref, computed } from 'vue';

import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'
const store = useStore();
const { controlView, cctvName } = storeToRefs(store)

const emit = defineEmits(['danger-update'])

// let frameCounter = 0

const props = defineProps({
  // WebSocket 베이스 URL(슬래시 없이 끝남) 예: ws://IP:8000/ws/stream
 wsUrl: { type: String, default: 'ws://localhost:8000/ws/stream' },
 // 렌더할 카메라 번호 목록 (CAM n ↔ 서버 sid n 매핑)
 camIds: { type: Array, default: () => [1, 2, 3, 4] },
});

// 스트림별 런타임 상태 저장소
// 키: s1, s2 ... 값: { ws, ok, src, tmp, live, t, people?, motion? }
const state = reactive({});

// URL 조합: /ws/stream/{sid}
const buildWsUrl = (id) => `${props.wsUrl}/${id}`;

// 아직 프레임이 없을 때 보여줄 더미 이미지
const placeholder = (id) =>
  `https://placehold.co/640x360/000/FFF?text=stream${id}`;

// Blob URL 메모리 해제 유틸 (매 프레임 교체 시 누수 방지)
const revoke = (u) => u && URL.revokeObjectURL(u);

/**
 * 특정 CAM(id) 소켓 오픈
 * - 바이너리는 임시(tmp)로, 텍스트 오면 화면(src)으로 승격
 * - onclose 시 3초 후 자동 재접속
 */
function openOne(id) {
  const k = `s${id}`;
  closeOne(id); // 기존 연결/Blob 정리

  // 초기 상태 생성
  state[k] = {
    ws: null,
    ok: false,
    src: '',
    live: null,
    t: null,
    people: 0,
    danger: 0,
    label: ''
  };
  const s = state[k];

  // WebSocket 연결
  s.ws = new WebSocket(buildWsUrl(id));
  s.ws.binaryType = 'arraybuffer'; // 바이너리를 ArrayBuffer로 받기

  s.ws.onopen = () => {
    s.ok = true; // 상태 배지: ok
  };

    s.ws.onmessage = (e) => {

    console.log(`[CAM${id}] binary frame size =`, e.data.byteLength);

  // 1) 바이너리: JPEG 프레임 → 바로 화면 src 교체
  if (e.data instanceof ArrayBuffer) {
    const old = s.live; // 이전 URL 잠깐 보관

    const url = URL.createObjectURL(
      new Blob([e.data], { type: 'image/jpeg' })
    );

    console.log(`[CAM${id}] src changed ->`, url.slice(-10));

    // 새 프레임 먼저 꽂고
    s.src = url;
    s.live = url;

    // 이전 것은 약간 있다가 정리 (렌더링 끝난 뒤에)
    if (old && old !== url) {
      setTimeout(() => {
        URL.revokeObjectURL(old);
      }, 2000); // 2초 뒤 정리 (필요하면 500~1000ms로 줄여도 됨)
    }

    return;
  }


 // 2) 텍스트(JSON) 처리 그대로
  try {
    const meta = JSON.parse(e.data);
    if (meta.people != null) s.people = meta.people;
    if (meta.danger != null) s.danger = meta.danger;
    if (meta.label) s.label = meta.label;

    if (meta.danger != null && meta.danger > 0 && meta.stream_id) {
      const camId = Number(String(meta.stream_id).replace('CAM', ''));
      if (!Number.isNaN(camId)) {
        emit('danger-update', {
          camId,
          streamId: meta.stream_id,
          label: meta.label,
          danger: meta.danger,
          timestamp: meta.timestamp ?? Date.now(),
        });
      }
    }
  } catch {
    // 무시
  }
};

  s.ws.onclose = () => {
    s.ok = false; // 상태 배지: reconnect
    // 3초 후 재접속 (네트워크/서버 순간 장애 대응)
    s.t = setTimeout(() => openOne(id), 3000);
  };

  // 오류 시 즉시 닫아서 onclose 재접속 루틴 태우기
  s.ws.onerror = () => {
    try {
      s.ws.close();
    } catch {}
  };
}

/**
 * 특정 CAM(id) 소켓/리소스 정리
 * - WebSocket 종료
 * - 재접속 타이머 제거
 * - Blob URL 해제
 */
function closeOne(id) {
  const k = `s${id}`;
  const s = state[k];
  if (!s) return;
  try {
    s.ws && s.ws.close();
  } catch {}
  clearTimeout(s.t);

  revoke(s.live);
  delete state[k];
}

// ✅ 모달 상태
const showModal = ref(false);
const selectedCamId = ref(null);

// 선택된 CAM의 상태를 computed로 가져오기
const selectedState = computed(() => {
  if (!selectedCamId.value) return null;
  return state[`s${selectedCamId.value}`] || null;
});

function setCctvName(label) {
  cctvName.value = label
}


// 모달 열기
// const openModal = (id) => {
//   selectedCamId.value = id;
//   showModal.value = true;
// };

// 모달 닫기
// const closeModal = () => {
//   showModal.value = false;
// };

// 마운트 시 모든 CAM 연결, 언마운트 시 정리
onMounted(() => props.camIds.forEach(openOne));
onUnmounted(() => props.camIds.forEach(closeOne));
</script>

<style scoped>
/* 배지 텍스트 드래그 방지 (UX) */
.badge {
  user-select: none;
}

/* 카드 클릭 가능한 느낌 */
.stream-tile {
  cursor: pointer;
}

/* 모달 전체 배경 (어두운 반투명 레이어) */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 1050;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 모달 컨텐츠 박스 (흰색 카드) */
.modal-body-box {
  background: #fff;
  border-radius: 0.75rem;
  padding: 1rem 1.25rem;
  width: min(90vw, 960px);
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

/* 모달 안 영상 영역 : 16:9 비율 유지 */
.modal-video-wrapper {
  position: relative;
  background: #000;
  width: 100%;
  aspect-ratio: 16 / 9;
}
</style>
