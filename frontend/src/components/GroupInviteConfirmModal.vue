<template>
  <div
    v-if="isVisible"
    class="modal-backdrop d-flex align-items-center justify-content-center"
    style="z-index: 1070;"
  >
    <div class="modal-dialog modal-dialog-centered" role="document">
      <div class="modal-content shadow-xl rounded-lg border-0 bg-white rounded-xl">

        <div class="modal-header d-flex justify-content-between align-items-center border-0 p-4">
          <h5 class="modal-title fw-bolder" :style="{ color: darkColor }">그룹 추가 확인</h5>
          <button type="button" class="btn-close" @click="handleCancel"></button>
        </div>

        <div class="modal-body p-4 text-center">
          <p class="fs-6 fw-bold mb-4" style="white-space: pre-line;">
            {{ inviterName }} 님께서<br />그룹으로 추가하셨습니다.
          </p>

          <div class="p-3 bg-light rounded-lg mb-4 text-muted fw-bold">
            {{ inviterPhone }}
          </div>

          <h6 class="fw-bolder mb-4" :style="{ color: darkColor }">위치 공유를 동의하십니까?</h6>

          <div class="border rounded-lg p-5 mb-4 bg-light">
            <i class="fas fa-map-marker-alt fs-4 mb-2" :style="{ color: mainColor }"></i>
            <div class="fw-bold">내 현재 위치</div>
            <small class="text-muted">부산광역시 해운대구</small>
          </div>

          <div
            class="text-start alert p-3 rounded-lg border-0"
            style="background-color: #f1f9ff;"
          >
            <div class="d-flex align-items-center">
              <i class="fas fa-info-circle me-2" :style="{ color: mainColor }"></i>
              <span class="fw-bold" :style="{ color: darkColor }">위치 공유 정보</span>
            </div>
            <p class="small text-muted mb-0 mt-1 ps-4">
              실시간 위치가 그룹 멤버들과 공유됩니다.
            </p>
          </div>
        </div>

        <div class="modal-footer d-flex justify-content-center border-0 p-4 pt-0">
          <button
            type="button"
            class="btn fw-bold flex-fill text-white"
            :style="{ backgroundColor: mainColor }"
            @click="handleConfirm(true)"
          >
            수락
          </button>

          <button
            type="button"
            class="btn btn-light-secondary fw-bold flex-fill"
            @click="handleConfirm(false)"
          >
            거절
          </button>
        </div>

        <div class="px-4 pb-4">
          <div class="d-flex align-items-start small text-muted text-start">
            <i class="fas fa-exclamation-triangle me-2 pt-1" :style="{ color: cautionColor }"></i>
            <span>
              위치 정보는 해당 해양 안전 목적으로만 사용되며, 언제든지 공유를 중단할 수 있습니다.
            </span>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useGroupStore } from '@/stores/groupStore';

const mainColor = '#0092BA';
const darkColor = '#0B1956';
const cautionColor = '#FFB354';

const props = defineProps({
  isVisible: { type: Boolean, default: false },
  invitationData: { type: Object, default: () => ({ inviterName: '', inviterPhone: '' }) },
});

const emit = defineEmits(['update:isVisible']);

const groupStore = useGroupStore();

const inviterName = computed(() => props.invitationData.inviterName || '그룹장');
const inviterPhone = computed(() => props.invitationData.inviterPhone || '010-XXXX-XXXX');

const handleConfirm = async (isAccepted) => {
  console.log(`[모달] 1. handleConfirm 실행됨. (isAccepted: ${isAccepted})`);

  if (isAccepted) {
    console.log('[모달] 2. store.acceptInvitation() 호출');
    await groupStore.acceptInvitation(props.invitationData);
  } else {
    console.log('[모달] 2. store.rejectInvitation() 호출');
    await groupStore.rejectInvitation(props.invitationData);
  }

  emit('update:isVisible', false);
};

const handleCancel = () => {
  console.log('[모달] handleCancel 실행됨 (X 버튼 클릭)');
  groupStore.closeModal();
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
  0% {
    transform: scale(0.9);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.modal-footer button {
  border-radius: 0.475rem;
}
</style>
