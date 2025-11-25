<template>
  <div v-if="isVisible" class="report-backdrop">
    <div class="report-modal">

      <div class="report-header">
        <h5>🚨 긴급 신고 접수</h5>
        <button class="report-close" @click="close">×</button>
      </div>

      <div class="report-info-bar">
        <span class="report-warning-label">WARNING</span>
        <span class="report-time">{{ formatTime(reportData.timestamp) }}</span>
      </div>

      <div class="report-body">
        
        <div class="report-section">
          <div class="report-section-title">환자 상태</div>
          <div class="report-status-grid">
            <p><i class="fas fa-user-circle me-2"></i> {{ reportData.memberName || '미확인' }}</p> 
            <p><i class="fas fa-child me-2"></i> (만){{ reportData.age || '미상' }}세 · {{ reportData.gender || 'N/A' }}</p>
          </div>
        </div>

        <hr class="my-3">

        <div class="report-section">
          <div class="report-section-title">추가 정보 입력</div>
          
          <div class="checkbox-grid">
            <div class="form-check-custom">
              <input class="form-check-input" type="checkbox" id="check1" value="MISSING" v-model="selectedTypes">
              <label class="form-check-label" for="check1">1. 일행일탈/실종</label>
            </div>
            <div class="form-check-custom">
              <input class="form-check-input" type="checkbox" id="check2" value="DROWNING" v-model="selectedTypes">
              <label class="form-check-label" for="check2">2. 물에 빠짐</label>
            </div>
            <div class="form-check-custom">
              <input class="form-check-input" type="checkbox" id="check3" value="COLLAPSE" v-model="selectedTypes">
              <label class="form-check-label" for="check3">3. 쓰러짐</label>
            </div>
            <div class="form-check-custom">
              <input class="form-check-input" type="checkbox" id="check4" value="INJURY" v-model="selectedTypes">
              <label class="form-check-label" for="check4">4. 부상</label>
            </div>
          </div>
        </div>
        
        <hr class="my-3">
        <div class="report-section">
          <div class="report-section-title">상황 기록</div>
          
          <div class="col-12 d-flex align-items-center" style="align-items: flex-start;"> 
            <i class="fas fa-map-marker-alt info-icon text-muted me-2" title="위치"></i>
            <div class="info-value fw-bold fs-2" style="line-height: 1.3; font-size: 1rem !important;"> 
                <template v-if="reportData.latitude !== null">
                    <div class="text-truncate">
                        위도: {{ reportData.latitude.toFixed(4) }}
                    </div>
                    <div class="text-truncate" style="margin-top: 4px;"> 
                        경도: {{ reportData.longitude.toFixed(4) }}
                    </div>
                </template>
                <template v-else>
                    위치 정보 없음
                </template>
            </div>
          </div>
          <p class="small text-muted mb-0 mt-3">{{ reportData.log || '관리자 수동 호출' }}</p>
        </div>

      </div>

      <div class="report-footer">
        
        <button class="report-action-btn report-call-btn" @click="sendReportAndCall" :disabled="isSending">
          <i class="fas fa-bullhorn me-2"></i> 
          {{ isSending ? '신고 처리 중...' : '긴급 신고 및 전화' }}
        </button>
        
      </div>

      <div class="report-close-btn-area">
        <button class="report-btn-close-final" @click="close" :disabled="isSending">확인 후 닫기</button>
      </div>
      
    </div>
  </div>
</template>
<script setup>
import { ref, defineProps, defineEmits } from 'vue';
import axios from 'axios';

const mainColor = '#0B1956'; 
// API_BASE_URL은 VITE_API_BASE_URL (예: http://localhost:8080)을 사용합니다.
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''; 

/* =========================================
   1. Props & Emits
   ========================================= */
const props = defineProps({
  isVisible: { type: Boolean, default: false },
  reportData: {
    type: Object,
    default: () => ({
      memberName: '미확인', 
      age: 0,
      gender: 'N/A',
      userNumber: null,
      latitude: 0, 
      longitude: 0, 
      timestamp: Date.now(),
      log: '긴급 구조 요청', 
    }),
  },
});

const emit = defineEmits(['update:isVisible']);

/* =========================================
   2. Local State
   ========================================= */
const selectedTypes = ref([]); 
const isSending = ref(false); 


/* =========================================
   3. Helper Functions
   ========================================= */

const close = () => {
  selectedTypes.value = [];
  emit('update:isVisible', false);
};

const formatTime = (timestamp) => {
  if (!timestamp) return '시간 정보 없음';
  const date = new Date(timestamp);
  return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false });
};


/* =========================================
   4. Action Handlers (핵심 로직: API 호출 및 전화 연결)
   ========================================= */

const sendReportAndCall = async () => {
    if (isSending.value) return;

    // 1. 유효성 및 필수 정보 확인
    if (!props.reportData.userNumber || !Number.isFinite(props.reportData.latitude) || !Number.isFinite(props.reportData.longitude)) {
        alert('필수 위치/사용자 정보가 누락되었습니다. (유효하지 않은 좌표)');
        return;
    }
    
    // 2. Report Type 생성
    const reportType = selectedTypes.value.length > 0 
                       ? selectedTypes.value[0] 
                       : 'OTHERS';

    // 3. API 요청 Payload 구성
    const payload = {
        userNumber: props.reportData.userNumber,
        latitude: props.reportData.latitude, 
        longitude: props.reportData.longitude,
        reportType: reportType, 
    };
    
    // 🚨 [새로운 로그] 전송 전 최종 Payload 확인
    console.log('✅ [Payload Check] Final Report Type:', reportType);
    console.log('✅ [Payload Check] Full Payload:', payload);
    
    isSending.value = true;

    try {
        const finalUrl = `${API_BASE_URL}/api/controltower/manual-report`;
        
        // 4. 신고 API 호출
        await axios.post(finalUrl, payload, {
            withCredentials: true
        });

        alert('✅ 긴급 신고가 관제소에 성공적으로 접수되었습니다.');

        close();

    } catch (e) {
        console.error('❌ 신고 전송 실패:', e);
        alert('❌ 신고 전송 중 오류가 발생했습니다. (네트워크/서버 오류)');
    } finally {
        isSending.value = false;
    }
};
</script>

<style scoped>
/* =========================================
   5. Styles (디자인 변경 없이 그대로 유지)
   ========================================= */

/* --- 기존 스타일 유지 --- */
.report-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1500;
}

.report-modal {
  width: 90%;
  max-width: 360px;
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.4);
}

/* --- Header --- */
.report-header {
  background-color: v-bind(mainColor); 
  color: white; 
  padding: 14px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 3px solid v-bind(mainColor);
}
.report-header h5 {
  margin: 0;
  font-weight: 800;
  font-size: 18px;
  color: white;
}
.report-close {
  background: transparent;
  border: none;
  font-size: 24px;
  color: white;
  cursor: pointer;
  line-height: 1;
}

/* --- Info Bar --- */
.report-info-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background-color: #e6eaf3; 
  border-bottom: 1px solid #eee;
}
.report-warning-label {
  background-color: v-bind(mainColor); 
  color: white;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}
.report-time {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

/* --- Body --- */
.report-body {
  padding: 15px 16px;
  font-size: 14px;
}
.report-section-title {
  font-weight: 700;
  color: v-bind(mainColor); 
  margin-bottom: 10px;
  font-size: 15px;
}
.report-status-grid p {
  margin-bottom: 8px;
  color: #444;
  font-weight: 500;
}
.report-status-grid i {
  color: #6c757d;
  width: 18px;
}
.report-bpm-info {
  font-weight: 700; 
  color: #dc3545 !important; 
}


/* --- 체크박스 스타일 --- */
.checkbox-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 10px;
    margin-top: 5px; 
}

.form-check-custom {
    display: flex;
    align-items: center;
    background-color: #f8f9fa;
    padding: 10px;
    border-radius: 6px;
    border: 1px solid #eee;
}

.form-check-input {
    width: 18px;
    height: 18px;
    margin-right: 8px;
    flex-shrink: 0;
    appearance: none;
    border: 2px solid #6c757d;
    border-radius: 4px;
    cursor: pointer;
    position: relative;
}

.form-check-input:checked {
    background-color: v-bind(mainColor); 
    border-color: v-bind(mainColor); 
}

.form-check-input:checked::after {
    content: '✓';
    color: white;
    font-size: 12px;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    line-height: 1;
}

.form-check-label {
    margin: 0;
    font-size: 13px;
    font-weight: 500;
    color: #333;
    line-height: 1.3;
}

/* --- Footer (Actions) --- */
.report-footer {
  padding: 10px 16px 15px;
  border-top: 1px solid #eee;
}

.report-action-btn {
  width: 100%;
  padding: 12px 10px;
  border-radius: 10px;
  border: none;
  font-weight: 700;
  font-size: 15px;
  transition: background-color 0.2s;
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1);
}

.report-call-btn {
  background-color: v-bind(mainColor); /* 🚨 mainColor로 복원 */
  color: white;
}
.report-call-btn:hover {
  background-color: #07113a; /* 🚨 mainColor보다 살짝 어둡게 복원 */
}

/* --- Final Close Button --- */
.report-close-btn-area {
  padding: 0 16px 15px;
}
.report-btn-close-final {
  background-color: #6c757d; 
  width: 100%;
  color: white;
  padding: 10px;
  border: none;
  border-radius: 10px;
  font-weight: 700;
  font-size: 15px;
}
.report-btn-close-final:hover {
  background-color: #5a6268;
} 
</style>