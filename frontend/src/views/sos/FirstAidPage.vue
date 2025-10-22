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
      <span v-if="loading">상황 목록 불러오는 중...</span>
      <span v-else-if="error" class="text-danger">목록을 불러오지 못했어요. 새로고침 해주세요.</span>
    </div>

    <!-- 상황 선택 버튼 리스트 -->
    <div class="d-grid gap-3 mb-5">
      <!-- 빈목록 안내 -->
      <div v-if="!loading && !error && situations.length === 0" class="text-muted">
        등록된 상황이 없습니다.
      </div>

      <!-- 목록 -->
      <button
        v-for="item in situations"
        :key="item.code || item.id || item.label"
        class="btn btn-block fw-bold py-3 rounded-3 shadow-sm"
        :class="{ 'btn-secondary-light': !isSelected(item), 'btn-info-highlight': isSelected(item) }"
        @click="select(item)"
      >
        {{ item.label }}
      </button>
    </div>

    <!-- 119 신고하기 버튼 (고정 하단) -->
    <div class="fixed-action-bottom p-3">
      <button
        class="btn btn-block w-100 fw-bolder py-3 fs-5 text-white rounded-3 shadow"
        :style="{ backgroundColor: dangerColor }"
        :disabled="loading"
        @click="handle119Report"
      >
        119 신고하기
      </button>
    </div>
  </div>
</template>

<script setup>
// ===== 의존성 =====
import { ref, onMounted } from 'vue';
import { useConfirmModal } from '@/utils/modalUtils.js';
import { useSOSStore } from '@/stores/sosStore';

// ===== 공통 모달/스토어 =====
const { showConfirmModal } = useConfirmModal();
const sosStore = useSOSStore();

// ===== 테마 컬러 =====
const mainColor   = '#0092BA';
const darkColor   = '#0B1956';
const dangerColor = '#EB725B';

// ===== API 엔드포인트 어댑터 =====
// - 목록 API: 상황들(코드/라벨) 내려주는 GET
// - 신고 로깅 API: 컨트롤러(@RequestMapping("/report/first-aid"))에 맞춰 text/plain POST
const API_BASE = import.meta.env.VITE_API_BASE || ''; // 예: 'https://api.yourdomain.com'
const API = {
  list:   `${API_BASE}/sos/first-aid/cases`, // ← 네가 만든 목록 엔드포인트로 교체 (예시)
  report: `${API_BASE}/report/first-aid`,     // ← 너의 컨트롤러 경로에 맞춤
};

// ===== 상태 =====
/**
 * situations: [{ code:'JELLYFISH', label:'해파리에 쏘였어요' }, ...]
 * 백엔드가 id/name 등으로 주면 아래 map에서 label/code로 매핑해 사용하면 됨.
 */
const situations = ref([]);
const selected   = ref(null);
const loading    = ref(false);
const error      = ref(false);

// ===== 유틸 =====
const isSelected = (item) =>
  !!selected.value && (selected.value.code ?? selected.value.id ?? selected.value.label) === (item.code ?? item.id ?? item.label);

const select = (item) => {
  selected.value = item;
};

// ===== 데이터 로드 =====
const loadSituations = async () => {
  loading.value = true;
  error.value = false;
  try {
    const res = await fetch(API.list, { method: 'GET', credentials: 'include' });
    if (!res.ok) throw new Error('Failed to load list');
    const data = await res.json();

    // ---- 백엔드 응답 형태에 맞춰 어댑트 ----
    // 예시 1) [{code, label}]
    // 예시 2) [{id, name}] -> {code:id, label:name}로 변환
    const rows = Array.isArray(data?.data) ? data.data : Array.isArray(data) ? data : [];

    situations.value = rows.map((row) => ({
      code:  row.code ?? row.id ?? row.value ?? row.key ?? row.name, // 우선순위 매핑
      label: row.label ?? row.name ?? row.title ?? String(row),
    }));
  } catch (e) {
    error.value = true;
    console.error(e);
  } finally {
    loading.value = false;
  }
};

onMounted(loadSituations);

// ===== 신고 처리 =====
const handle119Report = async () => {
  // 선택 유도(선택 없이도 진행하려면 이 블록 제거)
  if (!selected.value) {
    const go = await showConfirmModal({
      title: '상황 미선택',
      message: '상황을 선택하지 않았습니다. 그래도 119에 신고를 진행할까요?',
      type: 'confirm',
      confirmText: '진행',
      cancelText: '취소',
    });
    if (!go) return;
  }

  const title = '긴급 신고 확인';
  const label = selected.value?.label ?? '상황 미선택';
  const msg = `선택된 상황: "${label}"\n정말 119에 신고를 요청하시겠습니까?`;

  const confirmed = await showConfirmModal({
    title,
    message: msg,
    type: 'confirm',
    confirmText: '신고 연결',
    cancelText: '취소',
  });

  if (!confirmed) return;

  sosStore.setLoading(true);

  try {
    // 1) 스토어 로깅 (기존 기능 유지)
    await sosStore.logEmergencyCall('119_simple_report', label);

    // 2) 백엔드 신고 로깅/처리 (너의 컨트롤러 규약에 맞춰 text/plain)
    //    컨트롤러 시그니처: getFirstAid(@RequestBody String firstAidCase)
    //    → 바디에 코드(권장) 또는 라벨을 그대로 보냄
    const bodyText = selected.value?.code ?? label;

    await fetch(API.report, {
      method: 'POST',
      headers: { 'Content-Type': 'text/plain; charset=UTF-8' },
      body: bodyText,
      credentials: 'include',
    });

    // 3) 사용자 안내
    showConfirmModal({
      title: '신고 요청 완료',
      message: '119 긴급 신고를 요청했습니다.\n잠시 후 연결됩니다.',
      type: 'error',
      autoHide: true,
      duration: 3000,
    });

    // 4) 실제 전화 연결 (웹앱 환경에서는 보통 사용자 제스처 필요)
    // window.location.href = 'tel:119';
  } catch (e) {
    console.error(e);
    showConfirmModal({
      title: '신고 실패',
      message: '신고 요청 중 오류가 발생했습니다. 네트워크 상태를 확인하고 다시 시도해주세요.',
      type: 'error',
      autoHide: true,
      duration: 3000,
    });
  } finally {
    sosStore.setLoading(false);
  }
};
</script>

<style scoped>
.simple-report-page {
  /* 하단 버튼 공간 확보 (버튼 높이 + 패딩 고려) */
  min-height: calc(100vh - 55px - 60px);
  padding-bottom: 100px !important;
}
.btn-secondary-light {
  background-color: #f8f9fa;
  color: v-bind(darkColor);
  border: 1px solid #ced4da;
}
.btn-info-highlight {
  background-color: #e6f6fa;
  color: v-bind(darkColor);
  border: 1px solid v-bind(mainColor);
}
.fixed-action-bottom {
  position: fixed;
  bottom: 60px;
  left: 0;
  right: 0;
  background-color: white;
  border-top: 1px solid #eee;
  z-index: 100;
}
</style>
