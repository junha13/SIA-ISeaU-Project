<template>
  <div class="row g-3">
    <div v-for="id in camIds" :key="id" class="col-6">
      <div
        class="position-relative bg-dark stream-tile"
        style="aspect-ratio:16/9;"
        @click="openModal(id)"
      >
        <img
          :src="state[`s${id}`]?.src || placeholder(id)"
          class="w-100 h-100"
          style="object-fit:contain"
          alt="video"
        />
        <span class="badge text-bg-secondary position-absolute top-0 start-0 m-2">
          CAM {{ id }}
        </span>
        <span
          class="badge bg-opacity-75 position-absolute bottom-0 end-0 m-2"
          :class="state[`s${id}`]?.ok ? 'text-bg-success' : 'text-bg-warning'"
        >
          {{ state[`s${id}`]?.ok ? 'ok' : 'reconnect' }}
        </span>
      </div>
    </div>
  </div>

  <!-- 모달 -->
  <div v-if="showModal" class="modal-backdrop" @click.self="closeModal">
    <div class="modal-body-box">
      <div class="d-flex justify-content-between align-items-center mb-2">
        <h5 class="mb-0">CAM {{ selectedCamId }}</h5>
        <button type="button" class="btn btn-sm btn-light" @click="closeModal">닫기</button>
      </div>

      <div class="modal-video-wrapper">
        <img
          v-if="selectedState"
          :src="selectedState.src || placeholder(selectedCamId)"
          class="w-100 h-100"
          style="object-fit:contain;"
          alt="modal-video"
        />
      </div>

      <div v-if="selectedState && selectedState.people != null" class="mt-2 small text-muted">
        사람 수: {{ selectedState.people }} /
        움직임 픽셀: {{ selectedState.motion }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted, onUnmounted, ref, computed } from 'vue';

const emit = defineEmits(['danger-update'])
const props = defineProps({
  // 필요하면 외부에서 주입 가능. 주입이 없으면 아래 buildBaseWs()로 자동 생성.
  baseWs: { type: String, default: '' },
  camIds: { type: Array, default: () => [1, 2, 3, 4] },
});

/** ✅ 현재 페이지 기준으로 안전한 WS URL 자동 생성 */
const buildBaseWs = () => {
  const isHttps = window.location.protocol === 'https:';
  const proto = isHttps ? 'wss' : 'ws';
  const host = import.meta.env.VITE_WS_HOST || window.location.hostname;
  const port = import.meta.env.VITE_WS_PORT || '8000';
  return `${proto}://${host}:${port}/ws/stream`;
};

const wsBase = computed(() => props.baseWs || buildBaseWs());

/** 상태 저장소: s1, s2 ... */
const state = reactive({});

const wsUrl = (id) => `${wsBase.value}/${id}`;

const placeholder = (id) =>
  `https://placehold.co/640x360/000/FFF?text=stream${id}`;

const revoke = (u) => u && URL.revokeObjectURL(u);

/** 프레임 승격 타이머 (바이너리 먼저 와도 화면이 바로 뜨게) */
let promoteTimer = null;
const startPromoter = () => {
  if (promoteTimer) return;
  promoteTimer = setInterval(() => {
    for (const k in state) {
      const s = state[k];
      if (!s) continue;
      if (s.tmp && s.src !== s.tmp) {
        revoke(s.live);
        s.src = s.tmp;
        s.live = s.tmp;
        s.tmp = null;
      }
    }
  }, 150);
};
const stopPromoter = () => {
  if (promoteTimer) clearInterval(promoteTimer);
  promoteTimer = null;
};

function openOne(id) {
  const k = `s${id}`;
  closeOne(id);

  state[k] = {
    ws: null,
    ok: false,
    src: '',
    tmp: null,
    live: null,
    t: null,
    people: null,
    motion: null,
    danger: null,
  };
  const s = state[k];

  s.ws = new WebSocket(wsUrl(id));
  s.ws.binaryType = 'arraybuffer';

  s.ws.onopen = () => {
    s.ok = true;
  };

  s.ws.onmessage = (e) => {
    if (e.data instanceof ArrayBuffer) {
      // 최신 프레임 임시 저장 (즉시 화면 반영은 타이머가 승격)
      revoke(s.tmp);
      s.tmp = URL.createObjectURL(new Blob([e.data], { type: 'image/jpeg' }));
      return;
    }
    // 텍스트 메타(있으면 반영)
    try {
      const meta = JSON.parse(e.data);
      if (meta.people != null) s.people = meta.people;
      if (meta.motion != null) s.motion = meta.motion;
      if (meta.danger != null) s.danger = meta.danger;

      // ★ 위험 인원이 있으면 부모로 이벤트 emit (통계용)
      if (meta.danger != null && meta.danger > 0 && meta.stream_id) {
        emit('danger-update', {
          streamId: meta.stream_id, // e.g. "stream1"
          danger: meta.danger,
          timestamp: meta.timestamp ?? Date.now(),
        })
      }
    } catch {
      /* ignore */
    }
  };

  s.ws.onclose = () => {
    s.ok = false;
    s.t = setTimeout(() => openOne(id), 3000);
  };

  s.ws.onerror = () => {
    try { s.ws.close(); } catch {}
  };
}

function closeOne(id) {
  const k = `s${id}`;
  const s = state[k];
  if (!s) return;
  try { s.ws && s.ws.close(); } catch {}
  clearTimeout(s.t);
  revoke(s.tmp);
  revoke(s.live);
  delete state[k];
}

/** 모달 상태 */
const showModal = ref(false);
const selectedCamId = ref(null);
const selectedState = computed(() => (selectedCamId.value ? state[`s${selectedCamId.value}`] : null));

const openModal = (id) => {
  selectedCamId.value = id;
  showModal.value = true;
};
const closeModal = () => { showModal.value = false; };

/** 마운트/언마운트 */
onMounted(() => {
  startPromoter();
  props.camIds.forEach(openOne);
});
onUnmounted(() => {
  stopPromoter();
  props.camIds.forEach(closeOne);
});
</script>

<style scoped>
.badge { user-select: none; }
.stream-tile { cursor: pointer; }

.modal-backdrop {
  position: fixed; inset: 0; background: rgba(0,0,0,0.6);
  z-index: 1050; display: flex; justify-content: center; align-items: center;
}
.modal-body-box {
  background: #fff; border-radius: .75rem; padding: 1rem 1.25rem;
  width: min(90vw, 960px); max-height: 90vh; display: flex; flex-direction: column;
}
.modal-video-wrapper { position: relative; background: #000; width: 100%; aspect-ratio: 16 / 9; }
</style>