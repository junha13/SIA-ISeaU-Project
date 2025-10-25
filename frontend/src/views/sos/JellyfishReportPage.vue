<template>
  <div class="jellyfish-report-page container-fluid p-3">
    
    <!-- Header 
    <div class="d-flex align-items-center justify-content-between pb-3 border-bottom mb-4">
      <div class="d-flex align-items-center">
        <i class="fas fa-chevron-left me-2 fs-5" @click="$router.back()" style="cursor: pointer;" :style="{ color: darkColor }"></i>
        <h5 class="fw-bolder mb-0" :style="{ color: darkColor }">해파리 제보</h5>
      </div>
      <div>
        <i class="fas fa-bell me-3 fs-5" :style="{ color: dangerColor }"></i>
        <i class="fas fa-bars fs-5" :style="{ color: darkColor }"></i>
      </div>
    </div>
    -->

    <!-- 사진 등록 -->
    <div class="text-center mb-4 p-5 rounded-3 border" style="background-color: #f8f9fa; cursor: pointer;" @click="triggerFileUpload">
      <i class="fas fa-camera fs-1 mb-2 text-muted"></i>
      <p class="mb-0 fw-bold text-muted">사진 등록</p>
      <!-- 파일 이름 표시 -->
      <small v-if="sosStore.reportData.imageFile" class="text-success fw-bold">{{ sosStore.reportData.imageFile.name }}</small>
      <input type="file" ref="fileInput" @change="handleFileUpload" accept="image/*" style="display: none;">
    </div>

    <!-- 제보 양식 -->
    <div class="mb-3">
      <label class="fw-bold mb-2" :style="{ color: darkColor }">위치 (필수)</label>
      <!-- 위도 / 경도 분리 입력 -->
      <input
        type="text"
        class="form-control rounded-3 mb-2"
        placeholder="위도"
        :value="sosStore.reportData.lat ?? ''"
        @input="updateLat"
      />
      <input
        type="text"
        class="form-control rounded-3"
        placeholder="경도"
        :value="sosStore.reportData.lng ?? ''"
        @input="updateLng"
      />
      <button
        class="btn py-3 fs-6 rounded-3 mt-2 w-100"
        :disabled="sosStore.isLoading"
        @click="refreshLocation"
        :style="{ borderColor: '#000', backgroundColor: '#fff' }"
      >
        {{ sosStore.isLoading ? '위치 가져오는 중...' : '내 위치 새로고침' }}
      </button>
    </div>

    <div class="mb-3">
      <label class="fw-bold mb-2" :style="{ color: darkColor }">전화번호</label>
      <input type="tel" class="form-control rounded-3" placeholder="전화번호를 입력하세요"
             :value="sosStore.reportData.phone" @input="updatePhone">
    </div>

    <div class="mb-5">
      <label class="fw-bold mb-2" :style="{ color: darkColor }">상세 설명</label>
      <textarea class="form-control rounded-3" rows="4" placeholder="상세 설명을 입력하세요"
                :value="sosStore.reportData.description" @input="updateDescription"></textarea>
    </div>

    <!-- 제보 완료 버튼 (고정 하단) -->
    <div class="fixed-action-bottom p-3">
      <button class="btn w-100 fw-bolder py-3 fs-5 text-white rounded-3 shadow"
              :style="{ backgroundColor: dangerColor }"
              @click="submitReport">
        제보 완료
      </button>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useConfirmModal } from '@/utils/modalUtils.js';
import { useSOSStore } from '@/stores/sosStore';

const { showConfirmModal } = useConfirmModal();
const sosStore = useSOSStore(); // Store 인스턴스

const darkColor = '#0B1956';
const dangerColor = '#EB725B';

const fileInput = ref(null);

// --- Form Update Handlers (Store Action 호출)
const updateLat = (e) => {
  sosStore.updateReportData('lat', e.target.value);
  // 필요 시 location 문자열도 갱신
  if (sosStore.reportData.lng) {
    sosStore.updateReportData('location', `${e.target.value},${sosStore.reportData.lng}`);
  }
};
const updateLng = (e) => {
  sosStore.updateReportData('lng', e.target.value);
  if (sosStore.reportData.lat) {
    sosStore.updateReportData('location', `${sosStore.reportData.lat},${e.target.value}`);
  }
};
const updatePhone = (e) => sosStore.updateReportData('phone', e.target.value);
const updateDescription = (e) => sosStore.updateReportData('description', e.target.value);


const triggerFileUpload = () => {
  fileInput.value.click();
};

const handleFileUpload = (event) => {
  const file = event.target.files[0];
  if (file) {
    sosStore.updateReportData('imageFile', file); // 파일 객체를 Store에 저장
    showConfirmModal({
      title: '사진 등록 완료',
      message: `선택된 파일: ${file.name}`,
      type: 'success',
      autoHide: true,
      duration: 1500
    });
  }
};

// 내 위치 새로고침
const refreshLocation = () => {
  if (!('geolocation' in navigator)) {
    showConfirmModal({
      title: '위치 사용 불가',
      message: '이 브라우저에서는 위치 기능을 사용할 수 없습니다.',
      type: 'error'
    });
    return;
  }

  
  sosStore.setLoading(true);

  navigator.geolocation.getCurrentPosition(
    (pos) => {
      const { latitude, longitude } = pos.coords;
      const lat = latitude.toFixed(6);
      const lng = longitude.toFixed(6);

      sosStore.updateReportData('lat', lat);
      sosStore.updateReportData('lng', lng);
      // 백엔드가 문자열(예: "lat,lng")을 받는 경우 대비해서 함께 저장
      sosStore.updateReportData('location', `${lat},${lng}`);

      sosStore.setLoading(false);
      showConfirmModal({
        title: '위치 업데이트',
        message: `위도 ${lat}, 경도 ${lng}`,
        type: 'success',
        autoHide: true,
        duration: 1500
      });
    },
    (err) => {
      sosStore.setLoading(false);
      const messages = {
        1: '위치 권한이 거부되었습니다.',
        2: '위치를 가져오지 못했습니다(위치 정보 없음).',
        3: '요청 시간이 초과되었습니다.'
      };
      showConfirmModal({
        title: '가져오기 실패',
        message: messages[err.code] || '위치를 가져오는 중 오류가 발생했습니다.',
        type: 'error'
      });
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
  );
};

// 신고 제출
const submitReport = async () => {
  // lat/lng 기준으로 필수 체크
  if (!sosStore.reportData.lat || !sosStore.reportData.lng) {
    showConfirmModal({ title: '필수 입력', message: '발견 위치(위도/경도)를 입력해주세요.', type: 'error' });
    return;
  }

  try {
    await sosStore.submitJellyfishReport();
    showConfirmModal({
      title: '제보 완료',
      message: '해파리 제보가 성공적으로 접수되었습니다. 감사합니다.',
      type: 'success',
      autoHide: true,
      duration: 2000
    });
  } catch (e) {
    showConfirmModal({ title: '제보 실패', message: e.message, type: 'error' });
  }
};
</script>

<style scoped>
/* (스타일 유지) */
.jellyfish-report-page {
  /* 하단 버튼 공간 확보 (버튼 높이 + 패딩 고려) */
  min-height: calc(100vh - 55px - 60px);
  padding-bottom: 100px !important;
}
.form-control {
  border-radius: 0.75rem !important;
}
/* SOSSimpleReportPage와 동일한 고정 버튼 스타일 */
.fixed-action-bottom {
  position: fixed; /* Fixed로 변경 */
  bottom: 60px; /* AppLayout의 footer (60px) 위에 배치 */
  left: 0;
  right: 0;
  background-color: white;
  border-top: 1px solid #eee;
  z-index: 100; /* Footer보다 높은 z-index */
}
</style>