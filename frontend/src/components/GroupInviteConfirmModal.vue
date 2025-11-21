<template>
  <div
    v-if="isVisible"
    class="modal-backdrop d-flex align-items-center justify-content-center"
    style="z-index: 1070;"
  >
    <div class="modal-dialog modal-dialog-centered" role="document">
      <div class="modal-content shadow-xl rounded-lg border-0 bg-white rounded-xl">

        <div class="modal-header d-flex justify-content-between align-items-center border-0 p-4">
          <h5 class="modal-title fw-bolder" :style="{ color: darkColor }">그룹 초대 확인</h5>
          <button type="button" class="btn-close" @click="handleCancel"></button>
        </div>

        <div class="modal-body p-4 text-center">
          <!-- 실제 inviterName 값이 여기에 표시됩니다. -->
          <p class="fs-6 fw-bold mb-4" style="white-space: pre-line;">
            {{ inviterName }} 님으로부터<br />그룹 초대 요청이 도착했습니다.
          </p>

          <h6 class="fw-bolder mb-4" :style="{ color: darkColor }">위치 공유를 동의하고 그룹에 참여하시겠습니까?</h6>

          <!-- 동적 위치 제거, 기능 설명으로 대체 -->
          <div class="border rounded-lg p-5 mb-4 bg-light">
            <i class="fas fa-users-line fs-4 mb-2" :style="{ color: mainColor }"></i>
            <div class="fw-bold">그룹 활동 시작</div>
            <small class="text-muted">수락 즉시 그룹에 참여하며 위치 공유가 시작됩니다.</small>
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
              실시간 위치가 그룹 멤버들과 공유됩니다. (언제든지 중단 가능)
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
            수락 및 참여
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
              이 기능은 해양 안전 목적으로만 사용되며, 그룹 탈퇴 또는 위치 공유 중단 시 즉시 종료됩니다.
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
  // invitationData: { inviterName: '고예원' } 이 들어올 것을 기대합니다.
  invitationData: { type: Object, default: () => ({ inviterName: '' }) },
});

const emit = defineEmits(['update:isVisible']);

const groupStore = useGroupStore();

// ⭐️ 이 부분이 실제 닉네임을 표시하는 핵심 로직입니다.
// props.invitationData.inviterName 값이 존재하면, 그 값이 사용됩니다.
const inviterName = computed(() => props.invitationData.inviterName || '초대자');

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