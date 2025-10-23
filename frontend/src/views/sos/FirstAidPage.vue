<template>
  <div class="simple-report-page container-fluid p-3">
    <!-- Header -->
    <div class="d-flex align-items-center justify-content-between pb-3 border-bottom mb-4">
      <div class="d-flex align-items-center">
        <i class="fas fa-chevron-left me-2 fs-5" @click="$router.back()" style="cursor: pointer;" :style="{ color: darkColor }"></i>
        <h5 class="fw-bolder mb-0" :style="{ color: darkColor }">응급 대처법</h5>
      </div>
      <div>
        <i class="fas fa-bell me-3 fs-5" :style="{ color: dangerColor }"></i>
        <i class="fas fa-bars fs-5" :style="{ color: darkColor }"></i>
      </div>
    </div>

    <h6 class="fw-bold mb-3" :style="{ color: darkColor }">상황 설명</h6>

    <!-- 상태 표시 -->
    <div class="mb-3 small">
      <span v-if="listLoading">상황 목록 불러오는 중...</span>
      <span v-else-if="listError" class="text-danger">목록을 불러오지 못했어요. 새로고침 해주세요.</span>
    </div>

    <!-- 상황 선택 버튼 리스트 -->
    <div class="d-grid gap-3 mb-5">
      <div v-if="!listLoading && !listError && situations.length === 0" class="text-muted">
        등록된 상황이 없습니다.
      </div>

      <button
        v-for="item in situations"
        :key="item.firstAidCaseNum"
        class="btn btn-block fw-bold py-3 rounded-3 shadow-sm"
        :class="{ 'btn-secondary-light': selectedCaseNum !== item.firstAidCaseNum, 'btn-info-highlight': selectedCaseNum === item.firstAidCaseNum }"
        @click="openModal(item)"
      >
        {{ item.firstAidCaseName }}
      </button>
    </div>

    <!-- 119 신고하기 버튼 -->
    <div class="fixed-action-bottom p-3">
      <button
        class="btn btn-block w-100 fw-bolder py-3 fs-5 text-white rounded-3 shadow"
        :style="{ backgroundColor: dangerColor }"
        :disabled="listLoading"
        @click="handle119Report"
      >
        119 신고하기
      </button>
    </div>
  </div>

  <FirstAidModal
    :isVisible="showModal"
    :caseTitle="selectedCaseTitle"
    :steps="steps"
    :loading="detailLoading"
    :error="detailError"
    @update:isVisible="(v) => (showModal = v)"
  />
</template>

<script setup>
import { ref, onMounted } from 'vue';
import FirstAidModal from '@/components/FirstAidModal.vue';
import { useSOSStore } from '@/stores/sosStore';
import { useConfirmModal } from '@/utils/modalUtils.js';
import { sosApi } from '@/api/sos'; // sos.js에서 sosApi 객체 export

const { showConfirmModal } = useConfirmModal();
const sosStore = useSOSStore();

const mainColor   = '#0092BA';
const darkColor   = '#0B1956';
const dangerColor = '#EB725B';

// 목록
const situations  = ref([]);
const listLoading = ref(false);
const listError   = ref(false);

// 선택 + 모달
const showModal         = ref(false);
const selectedCaseNum   = ref(null);
const selectedCaseTitle = ref('');

// 상세(steps)
const steps         = ref([]);
const detailLoading = ref(false);
const detailError   = ref(false);

// 목록 로드
const loadSituations = async () => {
  listLoading.value = true; listError.value = false;
  try {
    const res = await sosApi.listFirstAidCases();
    situations.value = Array.isArray(res?.data) ? res.data : [];
  } catch (e) {
    console.error(e);
    listError.value = true;
  } finally {
    listLoading.value = false;
  }
};
onMounted(loadSituations);

// 모달 열기 + steps 로드
const openModal = async (item) => {
  selectedCaseNum.value   = item.firstAidCaseNum;
  selectedCaseTitle.value = item.firstAidCaseName ?? '응급 처치';
  showModal.value         = true;

  detailLoading.value = true; detailError.value = false; steps.value = [];
  try {
    const res = await sosApi.getFirstAidByCaseNum({ params: { firstAidCaseNum: selectedCaseNum.value } });
    steps.value = Array.isArray(res?.data) ? res.data : [];
  } catch (e) {
    console.error(e);
    detailError.value = true;
  } finally {
    detailLoading.value = false;
  }
};

// 119 신고 (확인 모달)
const handle119Report = async () => {
  if (!selectedCaseNum.value) {
    const go = await showConfirmModal({
      title: '상황 미선택',
      message: '상황을 선택하지 않았습니다. 그래도 119에 신고를 진행할까요?',
      type: 'confirm',
      confirmText: '진행',
      cancelText: '취소',
    });
    if (!go) return;
  }

  const label = situations.value.find(x => x.firstAidCaseNum === selectedCaseNum.value)?.firstAidCaseName || '상황 미선택';
  const confirmed = await showConfirmModal({
    title: '긴급 신고 확인',
    message: `선택된 상황: "${label}"\n정말 119에 신고를 요청하시겠습니까?`,
    type: 'confirm',
    confirmText: '신고 연결',
    cancelText: '취소',
  });
  if (!confirmed) return;

  sosStore.setLoading(true);
  try {
    await sosStore.logEmergencyCall('119_simple_report', label);
    await showConfirmModal({
      title: '신고 요청 완료',
      message: '119 긴급 신고를 요청했습니다.\n잠시 후 전화 앱으로 연결됩니다.',
      type: 'info',
      autoHide: true,
      duration: 2000,
    });
    window.location.href = 'tel:119';
  } catch (e) {
    console.error(e);
    await showConfirmModal({
      title: '신고 실패',
      message: '신고 요청 중 문제가 발생했습니다. 네트워크 상태를 확인하고 다시 시도해주세요.',
      type: 'error',
      confirmText: '확인',
    });
  } finally {
    sosStore.setLoading(false);
  }
};
</script>

<style scoped>
.simple-report-page { min-height: calc(100vh - 55px - 60px); padding-bottom: 100px !important; }
.btn-secondary-light { background-color: #f8f9fa; color: v-bind(darkColor); border: 1px solid #ced4da; }
.btn-info-highlight { background-color: #e6f6fa; color: v-bind(darkColor); border: 1px solid v-bind(mainColor); }
.fixed-action-bottom { position: fixed; bottom: 60px; left: 0; right: 0; background-color: white; border-top: 1px solid #eee; z-index: 100; }
</style>
