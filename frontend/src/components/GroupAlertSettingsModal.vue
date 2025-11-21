<template>
  <div v-if="isVisible" class="ga-backdrop">
    <div class="ga-modal">

      <div class="ga-header">
        <h5><i class="fas fa-bell me-2"></i> 알림 설정</h5>
        <button class="ga-close" @click="close">×</button>
      </div>

      <div class="ga-section-title">그룹 안전 이탈 알림</div>

      <div class="ga-alert-levels-container">
        
        <div class="ga-level-block">
          <div class="ga-row">
            <div class="ga-label">안전 거리 이탈 알림 설정</div>
            <button
              class="ga-toggle"
              :class="{ on: isDistanceAlertEnabled }"
              @click="toggleDistanceAlert"
            >
              <span class="ga-knob"></span>
            </button>
          </div>

          <div class="ga-custom-distance-section" 
               :class="{ 'ga-disabled-state': !isDistanceAlertEnabled }">
            
            <div class="ga-input-display">
              <span class="ga-current-radius-text">안전 거리:</span>
              <input
                type="number"
                class="ga-number-input"
                :min="MIN_DISTANCE"
                :max="MAX_DISTANCE"
                :step="10"
                v-model.number="distanceRadius"
                :disabled="!isDistanceAlertEnabled"
                @blur="handleInputBlur"
              >
              <span class="ga-unit-label">m</span>
            </div>
            
            <input 
              ref="rangeSliderEl"
              type="range" 
              class="ga-range-slider" 
              :min="MIN_DISTANCE" 
              :max="MAX_DISTANCE" 
              :step="10"
              v-model.number="distanceRadius"
              :disabled="!isDistanceAlertEnabled"
            >
            <div class="ga-range-limits">
              <span>{{ MIN_DISTANCE }}m</span>
              <span>{{ MAX_DISTANCE }}m (1km)</span>
            </div>
            
            <div class="ga-info-text small text-muted mt-2">
                설정한 거리 이상 이탈 시 알림이 발송됩니다.
            </div>
          </div>
        </div>
        
        <div class="ga-level-block">
            <div class="ga-row">
                <div class="ga-label">해안선 이탈 알림 (Tide)</div>
                <button
                    class="ga-toggle"
                    :class="{ on: isTideAlertEnabled }"
                    @click="toggleTideAlert"
                >
                    <span class="ga-knob"></span>
                </button>
            </div>
            <div class="ga-tide-description">
                <i class="fas fa-info-circle me-1"></i> 해안선 안전 경계 이탈 시 즉시 알림 발송됩니다.
            </div>
        </div>
        
        <div class="ga-warning-text small">
            <i class="fas fa-exclamation-circle me-1"></i> 그룹장만 설정을 변경할 수 있습니다.
        </div>
      </div>

      <div class="ga-footer">
        <button class="ga-btn ga-cancel-btn" @click="close">취소</button>
        <button class="ga-btn" @click="save">저장</button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import axios from 'axios'

/* =========================================
   1. Constants & Config
   ========================================= */
const MIN_DISTANCE = 10
const MAX_DISTANCE = 1000
const BRAND_COLOR = '#0B1956'
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

/* =========================================
   2. Props & Emits
   ========================================= */
const props = defineProps({
  isVisible: { type: Boolean, default: false },
  groupId: { type: Number, required: true },
  levels: { type: Array, default: () => [] },
})

const emit = defineEmits([
  'update:isVisible',
  'save',
  'settings-updated',
  'settings-synced',
])

/* =========================================
   3. Local State
   ========================================= */
const isDistanceAlertEnabled = ref(false)
const distanceRadius = ref(MIN_DISTANCE)
const isTideAlertEnabled = ref(false)

// 💡 [핵심 수정] 사용자가 버튼을 직접 눌렀는지 추적하는 플래그
const isTideDirty = ref(false)

const isSaving = ref(false)
const rangeSliderEl = ref(null)

/* =========================================
   4. Helper Functions
   ========================================= */
const clampValue = (val) => {
    const num = Number(val);
    if (!Number.isFinite(num)) return MIN_DISTANCE;
    return Math.max(MIN_DISTANCE, Math.min(MAX_DISTANCE, num));
}

const updateSliderFill = (val) => {
    if (!rangeSliderEl.value) return;
    const clamped = clampValue(val);
    const percent = ((clamped - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE)) * 100;
    rangeSliderEl.value.style.background = `linear-gradient(to right, ${BRAND_COLOR} 0%, ${BRAND_COLOR} ${percent}%, #e0e0e0 ${percent}%, #e0e0e0 100%)`;
}

/* =========================================
   5. Watchers & Event Handlers
   ========================================= */

// 슬라이더 UI 업데이트
watch(distanceRadius, (newVal) => updateSliderFill(newVal));

const handleInputBlur = () => {
    distanceRadius.value = clampValue(distanceRadius.value);
}

const toggleDistanceAlert = () => {
    isDistanceAlertEnabled.value = !isDistanceAlertEnabled.value;
}

// 💡 [핵심 수정] 버튼을 누르면 "내가 건드렸다(Dirty)"고 표시
const toggleTideAlert = () => {
    isTideAlertEnabled.value = !isTideAlertEnabled.value;
    isTideDirty.value = true; 
}

const close = () => emit('update:isVisible', false);

/* =========================================
   6. Core Logic: Data Sync (강력한 방어 적용)
   ========================================= */

const syncSettingsFromProps = async () => {
    const level1 = props.levels.find(l => l.levelField === 'groupLeaveLevel1' || l.id === 1);
    const tide = props.levels.find(l => l.levelField === 'tide' || l.id === 3);

    // --- [1. 거리 알림 동기화] ---
    if (level1) {
        const incomingVal = Number(
            level1.radius ?? level1.distance ?? level1.groupLeaveLevel1Distance ?? MIN_DISTANCE
        );
        const incomingEnabled = (level1.enabled === true || level1.enabled === 'Y');
        
        // "내 거리는 10보다 큰데(설정됨), 서버는 10(기본값)을 준다?" -> 내 값 유지
        if (distanceRadius.value > MIN_DISTANCE && incomingVal <= MIN_DISTANCE) {
            // 방어: 로컬 값 유지
        } else {
            // 그 외엔 서버 값 적용
            distanceRadius.value = clampValue(incomingVal);
            isDistanceAlertEnabled.value = incomingEnabled;
        }
    } else {
        // 데이터 없음 -> 로컬이 기본값일 때만 초기화
        if (distanceRadius.value <= MIN_DISTANCE) {
            distanceRadius.value = MIN_DISTANCE;
            isDistanceAlertEnabled.value = false;
        }
    }

    // --- [2. 해안선(Tide) 동기화 - Dirty Flag 방어] ---
    if (tide) {
        // 💡 [핵심] 내가 버튼을 건드린 적이 있다면(isTideDirty), 서버 값은 무시하고 내 설정 유지!
        if (isTideDirty.value) {
            // 로컬 설정 유지 (서버 동기화 건너뜀)
        } else {
            // 건드린 적 없다면 서버 값 따름
            const incomingTideEnabled = (
                tide.enabled === true || tide.enabled === 'Y' || tide.enabled == 1
            );
            isTideAlertEnabled.value = incomingTideEnabled;
        }
    }

    await nextTick();
    updateSliderFill(distanceRadius.value);
}

// 모달 열릴 때 동기화 실행
watch(() => props.isVisible, (visible) => {
    if (visible) syncSettingsFromProps();
    else emit('settings-updated');
});

// Props 데이터가 비동기로 들어올 때 반응
watch(() => props.levels, (newLevels) => {
    if (newLevels?.length > 0) syncSettingsFromProps();
}, { deep: true });

/**
 * API 저장 요청
 */
const save = async () => {
    if (isSaving.value) return;
    isSaving.value = true;

    try {
        const finalRadius = clampValue(distanceRadius.value);
        const distanceYN = isDistanceAlertEnabled.value ? 'Y' : 'N';
        const tideYN = isTideAlertEnabled.value ? 'Y' : 'N';

        const payload = {
            groupLeaveLevel1Alert: distanceYN,
            groupLeaveLevel1Distance: finalRadius,
            groupLeaveLevel2Alert: distanceYN,
            groupLeaveLevel2Distance: finalRadius,
            tideAlert: tideYN,
            tideDistance: 0,
        };

        const res = await axios.post(`${API_BASE_URL}/api/groups/settings/${props.groupId}`, payload, {
            withCredentials: true
        });

        if (res.data?.success || res.data?.data?.success) {
            alert('설정이 저장되었습니다.');
            
            // 저장 성공했으니 "내가 건드렸다"는 플래그는 유지하되(화면 깜빡임 방지),
            // 부모에게 최신 상태를 명확히 전달
            emit('settings-synced', [
                { 
                    id: 1, levelField: 'groupLeaveLevel1', label: '거리 이탈 알림',
                    radius: finalRadius, distance: finalRadius, groupLeaveLevel1Distance: finalRadius,
                    enabled: isDistanceAlertEnabled.value, unit: 'm'
                },
                { 
                    id: 3, levelField: 'tide', label: '해안선 이탈 알림',
                    radius: 0, enabled: isTideAlertEnabled.value, unit: ''
                }
            ]);
            close();
        } else {
            throw new Error(res.data?.message || '저장 실패');
        }
    } catch (e) {
        console.error('Settings Save Error:', e);
        alert('설정 저장 중 오류가 발생했습니다.');
    } finally {
        isSaving.value = false;
    }
}
</script>

<style scoped>
/* 스타일 기존 유지 */
.ga-backdrop { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.7); display: flex; align-items: center; justify-content: center; z-index: 1300; }
.ga-modal { width: 90%; max-width: 400px; background: #fff; border-radius: 14px; border: 1px solid #0b1956; overflow: hidden; box-shadow: 0 14px 35px rgba(0,0,0,0.15); }
.ga-header { display: flex; justify-content: space-between; align-items: center; padding: 14px 18px 8px; background-color: #0b1956; }
.ga-header h5 { margin: 0; font-weight: 800; font-size: 16px; color: white; }
.ga-close { background: transparent; border: none; font-size: 22px; color: white; cursor: pointer; }
.ga-section-title { padding: 12px 18px; font-weight: 700; color: #0b1956; border-bottom: 1px solid #eee; }
.ga-alert-levels-container { padding: 10px 18px; }
.ga-level-block { padding: 15px 0; border-bottom: 1px solid #eee; }
.ga-level-block:last-child { border-bottom: none; }
.ga-row { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; }
.ga-label { font-weight: 600; color: #333; font-size: 15px; }
.ga-toggle { width: 56px; height: 32px; background: #d1d5db; border-radius: 999px; border: none; padding: 4px; cursor: pointer; display: flex; align-items: center; transition: all 0.18s; }
.ga-toggle.on { background: #0b1956; justify-content: flex-end; }
.ga-knob { width: 24px; height: 24px; background: #fff; border-radius: 50%; box-shadow: 0 1px 3px rgba(0,0,0,0.25); }
.ga-custom-distance-section { padding: 10px 0 5px; transition: opacity 0.3s; }
.ga-disabled-state { opacity: 0.5; pointer-events: none; }
.ga-input-display { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; color: #0b1956; font-weight: 600; }
.ga-current-radius-text { font-weight: 400; color: #666; font-size: 15px; }
.ga-number-input { width: 100px; height: 38px; text-align: center; border: 2px solid #0b1956; border-radius: 8px; font-weight: 700; font-size: 16px; }
.ga-unit-label { font-size: 16px; }
.ga-range-slider { width: 100%; height: 6px; -webkit-appearance: none; background: #e0e0e0; border-radius: 3px; }
.ga-range-slider::-webkit-slider-thumb { -webkit-appearance: none; width: 20px; height: 20px; background: #0B1956; border-radius: 50%; cursor: pointer; box-shadow: 0 2px 4px rgba(0,0,0,0.2); }
.ga-range-limits { display: flex; justify-content: space-between; font-size: 12px; color: #999; margin-top: 5px; }
.ga-tide-description { font-size: 13px; color: #666; padding-top: 5px; }
.ga-warning-text { color: #0b1956; font-weight: 600; padding-top: 5px; }
.ga-footer { padding: 18px; display: flex; gap: 10px; border-top: 1px solid #eee; }
.ga-btn { flex: 1; background: #0b1956; color: white; font-weight: 800; padding: 10px; border-radius: 10px; border: none; cursor: pointer; }
.ga-btn:hover { background: #0092ba; }
.ga-cancel-btn { background: #f1f1f1; color: #333; }
.ga-cancel-btn:hover { background: #ddd; }
</style>