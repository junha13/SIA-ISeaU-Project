<template>
  <!-- Modal Backdrop -->
  <div v-if="isVisible" class="modal-backdrop d-flex align-items-center justify-content-center" style="z-index: 1050;">
    <!-- Modal Container -->
    <div class="modal-dialog modal-dialog-centered" role="document">
      <div class="modal-content shadow-xl rounded-lg border-0 bg-white rounded-xl" :class="modalClasses">
        <div class="modal-body p-5 p-md-8 text-center">

          <!-- ICON AREA (Enhanced) -->
          <div class="mb-5">
            <i
                :class="iconClass"
                class="fs-1 fw-bold mb-3 d-inline-block p-4 rounded-circle"
                :style="iconStyle"
            ></i>
          </div>

          <!-- Title & Message -->
          <h5 class="fw-bolder fs-3 mb-2 text-dark">{{ title }}</h5>
          <p class="text-dark fs-6 fw-bold mb-5" style="white-space: pre-line;">{{ message }}</p>

          <!-- Action Buttons -->
          <div v-if="type === 'confirm'" class="d-flex justify-content-center gap-3">
            <!-- 확인 버튼: 주 컬러 #0092BA 적용 -->
            <button type="button" class="btn fw-bold flex-fill" :style="confirmBtnStyle" @click="handleConfirm">{{ confirmText }}</button>
            <!-- 취소 버튼: 경고 색상으로 유지 (type=confirm이므로) -->
            <button type="button" class="btn btn-light-secondary fw-bold flex-fill" @click="handleCancel">{{ cancelText }}</button>
          </div>
          <div v-else class="d-grid">
            <!-- 확인 버튼: 주 컬러 #0092BA 적용 -->
            <button type="button" class="btn fw-bold" :style="confirmBtnStyle" @click="handleConfirm">확인</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';

const mainColor = '#0092BA'; // 주 컬러 정의
const dangerColor = '#EB725B'; // 경고 색상 정의

const props = defineProps({
  isVisible: { type: Boolean, default: false, },
  title: { type: String, required: true, },
  message: { type: String, required: true, },
  type: {
    type: String,
    default: 'info',
    validator: (value) => ['info', 'success', 'error', 'confirm'].includes(value),
  },
  confirmText: { type: String, default: '확인', },
  cancelText: { type: String, default: '취소', },
  autoHide: { type: Boolean, default: false, },
  duration: { type: Number, default: 2000, },
});

const emit = defineEmits(['update:isVisible', 'confirm', 'cancel']);

// 모달 유형에 따른 스타일 클래스 및 아이콘 정의
const modalConfig = computed(() => {
  // 안전 #8482FF, 주의#FFB354, 경고 #EB725B를 활용
  switch (props.type) {
    case 'success': // 안전
      return {
        icon: 'fas fa-check-circle',
        color: '#8482FF', // 안전 색상
        bgColor: '#EEEFFF' // 연한 배경
      };
    case 'error': // 경고
      return {
        icon: 'fas fa-times-circle',
        color: dangerColor, // 경고 색상
        bgColor: '#FFF5F8'
      };
    case 'confirm': // 주의 (confirm)
      return {
        icon: 'fas fa-question-circle',
        color: '#FFB354', // 주의 색상
        bgColor: '#FFF8DD'
      };
    case 'info': // 주 컬러
    default:
      return {
        icon: 'fas fa-info-circle',
        color: mainColor, // 주 컬러
        bgColor: '#F1F9FF'
      };
  }
});

// 확인 버튼 스타일 (주 컬러 적용)
const confirmBtnStyle = computed(() => ({
  backgroundColor: mainColor,
  color: 'white',
  borderColor: mainColor,
}));

const iconClass = computed(() => modalConfig.value.icon);
const iconStyle = computed(() => ({
  color: modalConfig.value.color,
  backgroundColor: modalConfig.value.bgColor,
}));

// 단일 버튼 모드일 때의 스타일 (info, success, error)
const modalClasses = computed(() => ({
  'modal-sm': props.type !== 'confirm',
  'border-primary': props.type === 'info',
  'border-success': props.type === 'success',
  'border-danger': props.type === 'error',
}));

// 확인 버튼 클릭 처리 (type: confirm, info, success, error)
const handleConfirm = () => {
  emit('confirm');
  // emit('update:isVisible', false); // 모달 유틸리티에서 처리하므로 주석 처리
};

// 취소 버튼 클릭 처리 (type: confirm)
const handleCancel = () => {
  emit('cancel');
  // emit('update:isVisible', false); // 모달 유틸리티에서 처리하므로 주석 처리
};

// 자동 숨김 로직
let timeout = null;
watch(() => props.isVisible, (newVal) => {
  if (timeout) {
    clearTimeout(timeout);
  }
  if (newVal && props.autoHide && props.type !== 'confirm') {
    timeout = setTimeout(() => {
      emit('update:isVisible', false);
    }, props.duration);
  }
}, { immediate: true });
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.4); /* 배경을 불투명하게 변경 */
  transition: opacity 0.3s ease;
}
.modal-content {
  animation: modal-in 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  /* 버튼 스타일 오버라이드를 위해 scoped 해제 */
}
.modal-content .btn {
  border-radius: 0.475rem; /* Metronic 기본 스타일 */
}
@keyframes modal-in {
  0% { transform: scale(0.9); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}
/* 모달 크기 조정 - Metronic 기본 클래스 오버라이드 */
.modal-dialog {
  max-width: 90%; /* 모바일에서 더 잘 보이도록 */
}
@media (min-width: 576px) {
  .modal-dialog {
    max-width: 450px;
  }
}
</style>
