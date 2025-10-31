<template>
  <div v-if="isVisible" class="modal-backdrop d-flex align-items-center justify-content-center" style="z-index: 1080;" @click="handleClose">
    <div ref="modalDialogRef" class="modal-dialog modal-dialog-centered modal-lg" role="document" @click.stop>
      <div class="modal-content shadow-xl rounded-lg border-0 bg-white rounded-xl">
        <div class="modal-header d-flex justify-content-between align-items-center border-0 p-4 pb-0">
          <h5 class="modal-title fw-bolder" :style="{ color: darkColor }">{{ title }}</h5>
          <button type="button" class="btn-close" @click="handleClose"></button>
        </div>

        <div class="modal-body p-4 pt-3 text-center">
          <div class="ratio ratio-16x9 rounded-3 overflow-hidden shadow-sm">
            <iframe
              :src="videoUrl"
              title="YouTube video player"
              frameborder="0"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
              allowfullscreen>
            </iframe>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, ref, watch, onMounted, nextTick } from 'vue';

const darkColor = '#0B1956';

const props = defineProps({
  isVisible: { type: Boolean, default: false },
  title: { type: String, default: '안전 영상 학습' },
  videoUrl: { type: String, default: '' },
});

const emit = defineEmits(['update:isVisible']);

const handleClose = () => {
  emit('update:isVisible', false);
};

// --- 로그 관련 코드 (디버깅용) ---
const modalDialogRef = ref(null);

onMounted(() => {
  console.log('[VideoModal] 컴포넌트 마운트됨');
});

watch(() => props.isVisible, async (newValue) => {
  console.log(`[VideoModal] isVisible 변경됨: ${newValue}`);
  if (newValue) {
    await nextTick();
    if (modalDialogRef.value) {
      const element = modalDialogRef.value;
      const styles = window.getComputedStyle(element);
      console.log('[VideoModal] modal-dialog 요소:', element);
      console.log(`[VideoModal] CSS max-width 규칙: ${styles.getPropertyValue('max-width')}`); // CSS 규칙 확인용
      console.log(`[VideoModal] CSS width 규칙: ${styles.getPropertyValue('width')}`);       // CSS 규칙 확인용
      console.log(`[VideoModal] 계산된 width: ${styles.width}`);
      console.log(`[VideoModal] 현재 화면 너비: ${window.innerWidth}px`);
    } else {
      console.error('[VideoModal] modalDialogRef를 찾을 수 없음');
    }
  }
});
// --- 로그 관련 코드 끝 ---

</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6);
  transition: opacity 0.3s ease;
}
.modal-content {
  border-radius: 12px;
  animation: modal-in 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  /* [추가] 내용물이 부모 너비에 맞게 늘어나도록 보장 */
  width: 100%;
}
@keyframes modal-in {
  0% { transform: scale(0.9); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}

/* 모달 반응형 */
.modal-dialog {
  /* 모바일 (992px 미만) */
  width: 95%; /* max-width 대신 width 사용 */
  max-width: none; /* 혹시 모를 max-width 제한 해제 */
}
@media (min-width: 992px) {
  .modal-dialog {
    /* 데스크톱 (992px 이상) */
    /* [수정] max-width 대신 width를 90vw로 설정 */
    width: 90vw;
    max-width: none; /* 혹시 모를 max-width 제한 해제 */
  }
  /* .modal-content width: 100% 규칙 제거 (위에서 전역으로 이동) */
}

/* ratio 클래스 */
.ratio {
  position: relative;
  width: 100%;
}
.ratio-16x9::before {
  display: block;
  padding-top: 56.25%;
  content: '';
}
.ratio iframe {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}
</style>