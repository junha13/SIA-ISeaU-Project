<template>
  <div v-if="isVisible" class="modal-backdrop d-flex align-items-center justify-content-center" style="z-index: 1060;">
    <div class="modal-dialog modal-dialog-centered modal-sm" role="document">
      <div class="modal-content shadow-xl rounded-xl border-0 bg-white">
        <div class="modal-header p-3 pb-0 border-0 d-flex justify-content-between align-items-center">
          <h5 class="fw-bolder p-1" :style="{ color: darkColor }">{{ caseTitle }} </h5>
          <button type="button" class="btn-close" style="width:1.6rem;height:1.6rem;background-size:1.6rem" @click="handleClose"></button>
        </div>

        <div class="modal-body p-4 pt-1" style="max-height: 80vh; overflow-y: auto;">
          <!-- 경고 메시지 -->
          <div class="alert alert-danger p-2 small fw-bold mb-3" role="alert" style="background-color: #f8d7da; border-color: #f5c6cb; color: #721c24;">
            <i class="fas fa-exclamation-triangle me-1"></i> 즉시 응급처치가 필요합니다. 심한 경우 119에 신고하세요.
          </div>

          <!-- 로딩/에러 -->
          <div v-if="loading" class="small text-muted">단계 정보를 불러오는 중...</div>
          <div v-else-if="error" class="small text-danger">정보를 불러오지 못했습니다.</div>

          <!-- 단계별 응급 처치 -->
          <div v-else>
            <!-- steps: { firstAidStep, firstAidContent, firstAidImage }[] -->
            <div v-for="(step, index) in steps" :key="step.firstAidStep ?? index" class="mb-4">
              <h6 class="fw-bold mb-2" :style="{ color: mainColor }">
                <span class="badge rounded-pill me-2" :style="{ backgroundColor: mainColor }">
                  {{ step.firstAidStep ?? (index + 1) }}
                </span>
                {{ step.firstAidContent || '단계' }}
              </h6>

              <!-- 미디어(동영상 or 이미지) -->
              <div v-if="step.firstAidImage && step.firstAidImage.trim() !== ''" class="mb-2">
                <!-- 동영상 -->
                <div v-if="isVideo(step.firstAidImage)" class="media-wrapper rounded bg-light border overflow-hidden">
                  <video
                    :key="step.firstAidImage"
                    :src="toAbs(step.firstAidImage)"
                    controls
                    playsinline
                    muted
                    preload="metadata"
                    style="width:100%; height:auto; display:block;"
                    type="video/mp4"
                  ></video>
                </div>

                <!-- 이미지 -->
                <div v-else class="rounded bg-light border d-flex justify-content-center align-items-center overflow-hidden">
                  <img :src="toAbs(step.firstAidImage)" alt=""
                      style="max-height: 100%; max-width: 100%; object-fit: cover;">
                </div>
              </div>


              <!-- 회색 가이드 박스: firstAidDescription을 줄 단위로 -->
              <div v-if="splitDesc(step.firstAidDescription).length" class="mb-2">
                <div v-for="(note, ni) in splitDesc(step.firstAidDescription)" :key="ni"
                     class="rounded-3 px-3 py-2 mb-2" style="background:#f1f1f1; color:#666;">
                  {{ note }}
                </div>
              </div>
            </div>
          </div>

          <!-- 병원 방문 권고 -->
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
/**
 * 부모(페이지)에서 steps를 이미 표준화해서 내려줌:
 * - { firstAidStep, firstAidContent, firstAidImage }
 */
const mainColor = '#0092BA';
const darkColor = '#0B1956';
const dangerColor = '#EB725B';

const props = defineProps({
  isVisible: { type: Boolean, default: false },
  caseTitle: { type: String,  default: '응급 처치' },
  steps:     { type: Array,  default: () => [] }, // [{ firstAidStep, firstAidContent, firstAidImage, firstAidDescription }]
  loading:   { type: Boolean, default: false },
  error:     { type: Boolean, default: false },
});

const emit = defineEmits(['update:isVisible']);

const handleClose = () => emit('update:isVisible', false);
const handleCall  = () => { window.location.href = 'tel:119'; };

/** firstAidDescription 을 줄/불릿 기준으로 안전하게 분해 */
const splitDesc = (val) => {
  if (!val || typeof val !== 'string') return [];
  return val
    .split(/\r?\n|•|- |\u2022|;|·|,|<\/?br\s*\/?>/gi)  // 개행/불릿/세미콜론/쉼표/<br> 지원
    .map(s => s.replace(/^[•\-\u2022·]\s*/, '').trim()) // 앞 불릿기호 제거
    .filter(Boolean);
};

const isVideo = (url) => /\.(mp4|webm|ogg)(\?.*)?$/i.test(url || '');
// 상대경로 → 절대경로(/images/...)로 보정
const toAbs = (url) => {
  if (!url) return url;
  if (/^https?:\/\//i.test(url)) return url;   // 이미 절대 URL이면 그대로
  return url.startsWith('/') ? url : `/${url}`; // 앞에 / 붙여 절대경로로
};

</script>

<style scoped>
.modal-backdrop { position: fixed; inset: 0; background-color: rgba(0, 0, 0, 0.5); transition: opacity .3s ease; }
.modal-dialog { max-width: 90%; }
@media (min-width: 576px) { .modal-dialog { max-width: 450px; } }
.modal-content { animation: modal-in .3s cubic-bezier(.25,.46,.45,.94); }
@keyframes modal-in { 0% { transform: scale(.9); opacity: 0; } 100% { transform: scale(1); opacity: 1; } }
</style>
