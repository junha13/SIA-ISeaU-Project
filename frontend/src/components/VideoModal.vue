<template>
  <!-- Video Modal (Youtube Embed Placeholder) -->
  <div v-if="isVisible" class="modal-backdrop d-flex align-items-center justify-content-center" style="z-index: 1080;">
    <div class="modal-dialog modal-dialog-centered modal-lg" role="document">
      <div class="modal-content shadow-xl rounded-lg border-0 bg-white rounded-xl">
        <div class="modal-header d-flex justify-content-between align-items-center border-0 p-4 pb-0">
          <h5 class="modal-title fw-bolder" :style="{ color: darkColor }">{{ title }}</h5>
          <button type="button" class="btn-close" @click="handleClose"></button>
        </div>
        <div class="modal-body p-4 pt-0 text-center">

          <!-- YouTube Video Embed Area (Placeholder) -->
          <div class="embed-responsive embed-responsive-16by9 rounded-3 overflow-hidden shadow">
            <div class="d-flex justify-content-center align-items-center bg-dark text-white" style="width: 100%; height: 300px;">
              <p>모달에 유튜브 영상 (Placeholder)</p>
            </div>
            <!-- 실제 코드에서는 <iframe> 태그를 사용해야 합니다.
            <iframe :src="videoUrl" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>
            -->
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

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
}
@keyframes modal-in {
  0% { transform: scale(0.9); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}
/* 모달 반응형 */
.modal-dialog {
  max-width: 90%;
}
@media (min-width: 992px) {
  .modal-dialog {
    max-width: 600px;
  }
}
</style>
