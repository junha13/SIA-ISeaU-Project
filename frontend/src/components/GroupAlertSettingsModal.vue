<template>
  <div v-if="isVisible" class="ga-backdrop">
    <div class="ga-modal">
      <div class="ga-header">
        <h5>설정</h5>
        <button class="ga-close" @click="close">×</button>
      </div>

      <div class="ga-section-title">그룹 이탈 알림</div>

      <div class="ga-row" v-for="level in localLevels" :key="level.id">
        <div class="ga-label">
          {{ level.label }}  {{ level.radius===0 ? '' : `(현재 ${level.radius}m)`}}
        </div>
        <button
          class="ga-toggle"
          :class="{ on: level.enabled }"
          @click="toggle(level.id)"
        >
          <span class="ga-knob"></span>
        </button>
      </div>

      <div class="ga-footer">
        <button class="ga-btn" @click="save">저장</button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { computed, watch, ref } from 'vue'
import axios from 'axios'

const props = defineProps({
  isVisible: { type: Boolean, default: false },
  // 그룹 ID는 필수 prop입니다.
  groupId: { type: Number, required: true },
  levels: {
    type: Array,
    default: () => [
      { id: 1, label: '1단계', radius: 3, enabled: true },
      { id: 2, label: '2단계', radius: 200, enabled: false },
      { id: 3, label: '수영 알림', radius: 0, enabled: true },
    ],
  },
})

// 'settings-synced' 이벤트를 추가하여 부모에게 로컬 데이터 전달
const emit = defineEmits(['update:isVisible', 'save', 'settings-updated', 'settings-synced'])

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL
const localLevels = ref(JSON.parse(JSON.stringify(props.levels)))

// 🚨 DB 칼럼 매핑 (tb_group_settings 기준)
const FIELD_MAP = {
    1: { alert: 'groupLeaveLevel1Alert', distance: 'groupLeaveLevel1Distance' },
    2: { alert: 'groupLeaveLevel2Alert', distance: 'groupLeaveLevel2Distance' },
    3: { alert: 'tideAlert', distance: null }, 
}

/**
 * DB에서 설정을 불러와 localLevels에 적용합니다.
 */
const loadSettings = async () => {
    if (!props.groupId) return

    try {
        const url = `${API_BASE_URL}/api/groups/settings/${props.groupId}`;
        const res = await axios.get(url, { withCredentials: true })

        const dbSettings = res.data?.data?.settings

        if (dbSettings) {
            // props.levels를 기준으로 시작하여 DB 값으로 덮어씁니다.
            const newLevels = props.levels.map((level) => { 
                const map = FIELD_MAP[level.id]
                if (!map) return level

                const enabled = dbSettings[map.alert] === 'Y'
                // DB 값이 null이면 props의 기본값 사용
                const radius = map.distance ? dbSettings[map.distance] : level.radius

                return {
                    ...level,
                    enabled,
                    radius: radius ?? level.radius, 
                }
            })
            localLevels.value = newLevels
            
            // 💡 로드 성공 시 부모에게 최신 데이터를 전달하여 props.levels를 갱신하도록 유도
            emit('settings-synced', newLevels); 
            
        } else {
            // 설정이 없는 경우, props의 기본값을 사용하도록 초기화
            localLevels.value = JSON.parse(JSON.stringify(props.levels))
        }
    } catch (e) {
        // 400 에러는 백엔드 문제입니다. 에러가 나면 화면은 이전 상태 유지.
        console.error('알림 설정 로드 실패:', e) 
    }
}

// 모달이 열릴 때 DB 값을 로드하고, 모달이 닫힐 때 부모에게 갱신 요청을 알림
watch(
    () => props.isVisible,
    (v) => {
        if (v) {
            loadSettings()
        } 
        // 모달이 닫힐 때만 부모에게 'settings-updated'를 알림
        if (!v) {
            emit('settings-updated'); 
        }
    },
)

// props.levels가 외부에서 갱신될 때 localLevels를 동기화
watch(
    () => props.levels,
    (newLevels) => {
        // 모달이 닫힌 상태(isVisible: false)에서만 props 갱신 시 localLevels를 갱신
        if (!props.isVisible) {
            localLevels.value = JSON.parse(JSON.stringify(newLevels))
        }
    }, { deep: true }
)


const close = () => {
    emit('update:isVisible', false)
}

const toggle = (id) => {
    localLevels.value = localLevels.value.map((lv) =>
        lv.id === id ? { ...lv, enabled: !lv.enabled } : lv,
    )
}

const save = async () => {
    // 1. DTO 구조로 데이터 변환
    const payload = {} 
    
    localLevels.value.forEach((level) => {
        const map = FIELD_MAP[level.id]
        if (!map) return

        // 알림 상태 ('Y'/'N')
        payload[map.alert] = level.enabled ? 'Y' : 'N'

        // 거리 (distance 필드가 있는 레벨에만 적용)
        if (map.distance) {
            payload[map.distance] = level.radius
        }
    })
    console.log('API 요청 페이로드:', payload);

    const url = `${API_BASE_URL}/api/groups/settings/${props.groupId}`; 
    
    try {
        const res = await axios.post(url, payload, { withCredentials: true })

        if (res.data?.data?.success === true) {
            console.log('알림 설정 저장 성공.')
            
            // 💡 [핵심] 저장 성공 시, localLevels의 최신 상태를 부모에게 직접 전달
            emit('settings-synced', localLevels.value); 
            
            close();

        } else {
            console.error('알림 설정 저장 실패:', res.data.message)
            alert(`설정 저장 실패: ${res.data.message}`)
        }
    } catch (e) {
        console.error('알림 설정 저장 중 네트워크 오류:', e)
        alert('설정 저장 중 오류가 발생했습니다.')
    }
}
</script>

<style scoped>
/* --------------------------------- */
/* 🎨 디자인 (CSS) */
/* --------------------------------- */
.ga-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1300;
}
.ga-modal {
  width: 320px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 14px 35px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}
.ga-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px 6px;
}
.ga-header h5 {
  margin: 0;
  font-weight: 700;
  font-size: 16px;
  color: #0b1956;
}
.ga-close {
  background: transparent;
  border: none;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
}
.ga-section-title {
  padding: 6px 16px 12px;
  font-weight: 700;
  font-size: 14px;
  color: #0b1956;
}
.ga-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 16px;
  gap: 12px;
}
.ga-label {
  font-size: 13px;
  color: #0b1956;
}
.ga-toggle {
  width: 52px;
  height: 28px;
  background: #d1d5db;
  border-radius: 999px;
  border: none;
  padding: 3px;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all 0.18s;
}
.ga-toggle.on {
  background: #1982c4; /* 네가 쓰는 파란색으로 바꿔도 됨 */
  justify-content: flex-end;
}
.ga-knob {
  width: 22px;
  height: 22px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.25);
}
.ga-footer {
  padding: 14px 16px 14px;
  display: flex;
  justify-content: flex-end;
}
.ga-btn {
  background: #0092ba;
  border: none;
  color: #fff;
  font-weight: 600;
  font-size: 13px;
  padding: 6px 14px;
  border-radius: 8px;
  cursor: pointer;
}
</style>