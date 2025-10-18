<template>
  <div v-if="isVisible" class="modal-backdrop d-flex align-items-center justify-content-center" style="z-index: 1060;">
    <div class="modal-dialog modal-dialog-centered modal-sm" role="document">
      <div class="modal-content shadow-xl rounded-xl border-0 bg-white">
        <div class="modal-header p-3 pb-0 border-0 d-flex justify-content-between align-items-center">
          <h5 class="fw-bolder mb-0" :style="{ color: darkColor }">{{ title }} 응급 처치</h5>
          <button type="button" class="btn-close" @click="handleClose"></button>
        </div>

        <div class="modal-body p-4 pt-1" style="max-height: 80vh; overflow-y: auto;">

          <!-- 경고 메시지 -->
          <div class="alert alert-danger p-2 small fw-bold mb-3" role="alert" style="background-color: #f8d7da; border-color: #f5c6cb; color: #721c24;">
            <i class="fas fa-exclamation-triangle me-1"></i> 즉시 응급처치가 필요합니다. 심한 경우 119에 신고하세요.
          </div>

          <!-- 단계별 응급 처치 -->
          <div v-for="(step, index) in steps" :key="index" class="mb-4">
            <h6 class="fw-bold mb-2" :style="{ color: mainColor }">
              <span class="badge rounded-pill me-2" :style="{ backgroundColor: mainColor }">{{ index + 1 }}</span>
              {{ step.title }}
            </h6>

            <!-- 이미지 Placeholder (선택적) -->
            <div v-if="step.imagePlaceholder"
                 class="rounded bg-light border d-flex justify-content-center align-items-center mb-2"
                 style="height: 100px;">
              <p class="text-muted small mb-0">{{ step.imagePlaceholder }}</p>
            </div>

            <!-- 내용 -->
            <ul class="list-unstyled small ps-3">
              <li v-for="(instruction, i) in step.instructions" :key="i" class="mb-1"
                  :style="{ color: darkColor }">
                <i class="fas fa-caret-right me-1" :style="{ color: dangerColor }"></i> {{ instruction }}
              </li>
            </ul>
          </div>

          <!-- 병원 방문 권고 -->
          <div class="alert p-3 mt-4" style="background-color: #ffe0b2; border-color: #ffcc80; color: #e65100;">
            <h6 class="fw-bold mb-1"><i class="fas fa-hospital me-1"></i> 이런 증상이 있으면 즉시 병원으로!</h6>
            <ul class="list-unstyled small mb-0 ps-3">
              <li v-for="(symptom, index) in symptoms" :key="index" class="mb-1" :style="{ color: darkColor }">
                <i class="fas fa-dot-circle me-1" :style="{ color: dangerColor, fontSize: '0.4rem' }"></i> {{ symptom }}
              </li>
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
import { computed } from 'vue';

const mainColor = '#0092BA';
const darkColor = '#0B1956';
const dangerColor = '#EB725B';

const props = defineProps({
  isVisible: { type: Boolean, default: false },
  type: {
    type: String,
    required: true,
    validator: (value) => ['jellyfish', 'sunburn', 'bugbite'].includes(value),
  },
});

const emit = defineEmits(['update:isVisible']);

const config = computed(() => {
  switch (props.type) {
    case 'jellyfish':
      return {
        title: '해파리 쏘임',
        steps: [
          { title: '바다에서 나오기', imagePlaceholder: '해변으로 이동하는 모습', instructions: ['침착하게 해변으로 이동하세요.', '안전한 곳으로 이동하세요.'] },
          { title: '촉수 제거하기', imagePlaceholder: '핀셋이나 카드로 촉수를 제거하는 모습', instructions: ['핀셋이나 카드로 조심스럽게 제거하세요.', '맨손으로 만지거나 문지르지 마세요!'] },
          { title: '바닷물로 씻기', imagePlaceholder: '바닷물로 씻는 모습', instructions: ['바닷물로 부드럽게 씻어내세요.', '민물이나 알코올 사용 금지!'] },
          { title: '냉찜질하기', imagePlaceholder: '얼음팩으로 냉찜질하는 모습', instructions: ['깨끗한 물수건이나 얼음팩을 수건에 감싸 피부 온도를 낮춰주세요.', '얼음을 직접 피부에 대지 마세요!'] },
        ],
        symptoms: ['호흡 곤란이나 가슴 답답함', '의식 장애나 경련', '전신 통증이 계속됨', '어지럽거나 의식이 흐려짐']
      };
    case 'sunburn':
      return {
        title: '햇빛 화상',
        steps: [
          { title: '즉시 실내로 이동하기', imagePlaceholder: '시원한 곳으로 이동하는 모습', instructions: ['햇볕 노출을 즉시 중단하고, 시원한 곳으로 이동해 피부 자극을 멈추세요.'] },
          { title: '냉찜질하기 (10~15분)', imagePlaceholder: '냉찜질하는 모습', instructions: ['깨끗한 물수건이나 얼음팩을 수건에 감싸 피부 온도를 낮춰주세요.', '얼음을 직접 피부에 대지 마세요!'] },
          { title: '피부 수분 공급하기', imagePlaceholder: '알로에젤을 바르는 모습', instructions: ['알로에젤, 수딩젤, 무향 보습젤 등을 발라주세요.', '연고나 알코올 사용 금지!'] },
          { title: '충분한 수분 섭취하기', imagePlaceholder: '물을 마시는 모습', instructions: ['화상으로 인해 체내 수분이 빠져나가므로 물을 자주 마시세요.'] },
          { title: '물집은 터뜨리지 말기', imagePlaceholder: '물집을 냉찜질하는 모습', instructions: ['물집은 자연 보호막 역할을 합니다.', '억지로 터뜨리면 세균 감염 위험이 있으니 터뜨리지 마세요!'] },
        ],
        symptoms: ['심한 통증이나 부위에 큰 물집이 생김', '고열이 나거나 오한 동반', '심한 통증이 지속됨', '어지럽거나 의식이 흐려짐']
      };
    case 'bugbite':
      return {
        title: '바다벌레 물림',
        steps: [
          { title: '바다에서 나오기', imagePlaceholder: '해변으로 이동하는 모습', instructions: ['침착하게 해변으로 이동하세요.'] },
          { title: '침·촉수·가시 등 이물질 제거하기', imagePlaceholder: '핀셋으로 이물질을 제거하는 모습', instructions: ['핀셋으로 조심스럽게 제거해주세요.', '독이 퍼질 수 있으니 손으로 문지르거나 긁지 마세요.'] },
          { title: '바닷물로 씻기', imagePlaceholder: '바닷물로 씻는 모습', instructions: ['바닷물로 부드럽게 씻어내세요.', '민물이나 알코올 사용 금지!'] },
          { title: '냉찜질하기 (10~15분)', imagePlaceholder: '얼음팩으로 냉찜질하는 모습', instructions: ['깨끗한 물수건이나 얼음팩을 수건에 감싸 피부 온도를 낮춰주세요.'] },
          { title: '가려움, 통증 완화 연고 바르기', imagePlaceholder: '연고를 바르는 모습', instructions: ['스테로이드 연고를 발라주세요.', '상처를 깨끗이 유지하세요.'] },
        ],
        symptoms: ['심한 통증이나 넓은 부위의 물집이 생김', '호흡 곤란이나 전신 발진', '심한 통증이 계속됨', '어지럽거나 의식이 흐려짐']
      };
    default:
      return { title: '응급 처치 정보', steps: [], symptoms: [] };
  }
});

const title = computed(() => config.value.title);
const steps = computed(() => config.value.steps);
const symptoms = computed(() => config.value.symptoms);


const handleClose = () => {
  emit('update:isVisible', false);
};

const handleCall = () => {
  alert('119에 응급 상황 신고 요청을 보냅니다.'); // 실제로는 tel:119 사용
};

</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5); /* 배경 반투명 */
  transition: opacity 0.3s ease;
}
.modal-dialog {
  max-width: 90%;
}
@media (min-width: 576px) {
  .modal-dialog {
    max-width: 450px;
  }
}
.modal-content {
  animation: modal-in 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}
@keyframes modal-in {
  0% { transform: scale(0.9); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}
</style>
