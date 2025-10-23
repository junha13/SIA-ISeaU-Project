<template>
  <div v-if="isVisible" class="modal-backdrop d-flex align-items-center justify-content-center" style="z-index: 1060;">
    <div class="modal-dialog modal-dialog-centered modal-sm" role="document">
      <div class="modal-content shadow-xl rounded-xl border-0 bg-white">
        <div class="modal-header p-3 pb-0 border-0 d-flex justify-content-between align-items-center">
          <h5 class="fw-bolder mb-0" :style="{ color: darkColor }">{{ caseTitle }} 응급 처치</h5>
          <button type="button" class="btn-close" @click="handleClose"></button>
        </div>

        <div class="modal-body p-4 pt-1" style="max-height: 80vh; overflow-y: auto;">

          <!-- 경고 메시지 -->
          <div class="alert alert-danger p-2 small fw-bold mb-3" role="alert" style="background-color: #f8d7da; border-color: #f5c6cb; color: #721c24;">
            <i class="fas fa-exclamation-triangle me-1"></i> 즉시 응급처치가 필요합니다. 심한 경우 119에 신고하세요.
          </div>

          <!-- 로딩/에러 -->
          <div v-if="loading" class="small text-muted">단계 정보를 불러오는 중...</div>
          <div v-else-if="error" class="small text-danger">정보를 불러오지 못했습니다.</div>

          <!-- 단계별 응급 처치 (백엔드 steps 그대로) -->
          <div v-else>
            <div v-for="(step, index) in steps" :key="step.firstAidStep ?? index" class="mb-4">
              <h6 class="fw-bold mb-2" :style="{ color: mainColor }">
                <span class="badge rounded-pill me-2" :style="{ backgroundColor: mainColor }">{{ index + 1 }}</span>
                {{ step.firstAidStep || '단계' }}
              </h6>

              <!-- 이미지 -->
              <div v-if="step.firstAidImage"
                   class="rounded bg-light border d-flex justify-content-center align-items-center mb-2"
                   style="height: 120px; overflow: hidden;">
                <img :src="step.firstAidImage" alt="" style="max-height: 100%; max-width: 100%; object-fit: cover;">
              </div>

              <!-- 내용 -->
              <ul class="list-unstyled small ps-3">
                <li class="mb-1" :style="{ color: darkColor }">
                  <i class="fas fa-caret-right me-1" :style="{ color: dangerColor }"></i>
                  {{ step.firstAidContent || step.firstAidDescription || '내용 없음' }}
                </li>
              </ul>
            </div>
          </div>

          <!-- 병원 방문 권고 (정적) -->
          <div class="alert p-3 mt-4" style="background-color: #ffe0b2; border-color: #ffcc80; color: #e65100;">
            <h6 class="fw-bold mb-1"><i class="fas fa-hospital me-1"></i> 이런 증상이 있으면 즉시 병원으로!</h6>
            <ul class="list-unstyled small mb-0 ps-3">
              <li class="mb-1" :style="{ color: darkColor }"><i class="fas fa-dot-circle me-1" :style="{ color: dangerColor, fontSize: '0.4rem' }"></i> 호흡 곤란/전신 두드러기</li>
              <li class="mb-1" :style="{ color: darkColor }"><i class="fas fa-dot-circle me-1" :style="{ color: dangerColor, fontSize: '0.4rem' }"></i> 의식 저하/어지러움 지속</li>
              <li class="mb-1" :style="{ color: darkColor }"><i class="fas fa-dot-circle me-1" :style="{ color: dangerColor, fontSize: '0.4rem' }"></i> 심한 통증/넓은 부위 수포</li>
            </ul>
          </div>

          <!-- 119 신고 버튼 -->
          <div class="d-grid mt-4">
            <button class="btn fw-bolder py-3 fs-5 text-white rounded-3 shadow"
                    :style="{ backgroundColor: dangerColor }"
                    @click="handleCall">
              <i class="fas fa-phone-volume me-2"></i> 응급 상황 시 119
            </button>
          </div>

        </div>

        <div class="modal-footer d-grid p-3 pt-2 border-0">
          <button type="button" class="btn btn-block btn-outline-secondary fw-bold rounded-3" @click="handleClose">
            확인했습니다
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const mainColor = '#0092BA';
const darkColor = '#0B1956';
const dangerColor = '#EB725B';

const props = defineProps({
  isVisible: { type: Boolean, default: false },
  caseTitle: { type: String,  default: '응급 처치' },
  steps:     { type: Array,   default: () => [] }, // ReportDTO[]
  loading:   { type: Boolean, default: false },
  error:     { type: Boolean, default: false },
});

const emit = defineEmits(['update:isVisible']);

const handleClose = () => emit('update:isVisible', false);
const handleCall  = () => { window.location.href = 'tel:119'; };
</script>

<style scoped>
.modal-backdrop {
  position: fixed; inset: 0; background-color: rgba(0, 0, 0, 0.5);
  transition: opacity .3s ease;
}
.modal-dialog { max-width: 90%; }
@media (min-width: 576px) { .modal-dialog { max-width: 450px; } }
.modal-content { animation: modal-in .3s cubic-bezier(.25,.46,.45,.94); }
@keyframes modal-in { 0% { transform: scale(.9); opacity: 0; } 100% { transform: scale(1); opacity: 1; } }
</style>
