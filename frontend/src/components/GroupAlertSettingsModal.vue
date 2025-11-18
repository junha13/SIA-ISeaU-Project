<template>
  <div v-if="isVisible" class="ga-backdrop">
    <div class="ga-modal">

      <!-- Header -->
      <div class="ga-header">
        <h5>설정</h5>
        <button class="ga-close" @click="close">×</button>
      </div>

      <!-- Section title -->
      <div class="ga-section-title">그룹 이탈 알림</div>

      <!-- Alert levels -->
      <div class="ga-row" v-for="level in localLevels" :key="level.id">
        <div class="ga-label">
          {{ level.label }}
          {{ level.radius === 0 ? '' : `(현재 ${level.radius}m)` }}
        </div>

        <button
          class="ga-toggle"
          :class="{ on: level.enabled }"
          @click="toggle(level.id)"
        >
          <span class="ga-knob"></span>
        </button>
      </div>

      <!-- Footer -->
      <div class="ga-footer">
        <button class="ga-btn" @click="save">저장</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import axios from 'axios'

/* ----------------------------- */
/* Props & Emits */
/* ----------------------------- */
const props = defineProps({
  isVisible: { type: Boolean, default: false },
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

const emit = defineEmits([
  'update:isVisible',
  'save',
  'settings-updated',
  'settings-synced',
])

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

/* 로컬 상태 */
const localLevels = ref(JSON.parse(JSON.stringify(props.levels)))

/* DB 컬럼 매핑 */
const FIELD_MAP = {
  1: { alert: 'groupLeaveLevel1Alert', distance: 'groupLeaveLevel1Distance' },
  2: { alert: 'groupLeaveLevel2Alert', distance: 'groupLeaveLevel2Distance' },
  3: { alert: 'tideAlert', distance: null },
}

/* ----------------------------- */
/* 설정 로드 */
/* ----------------------------- */
const loadSettings = async () => {
  if (!props.groupId) return

  try {
    const url = `${API_BASE_URL}/api/groups/settings/${props.groupId}`
    const res = await axios.get(url, { withCredentials: true })
    const dbSettings = res.data?.data?.settings

    if (dbSettings) {
      const newLevels = props.levels.map((level) => {
        const map = FIELD_MAP[level.id]
        if (!map) return level

        return {
          ...level,
          enabled: dbSettings[map.alert] === 'Y',
          radius: map.distance ? (dbSettings[map.distance] ?? level.radius) : level.radius,
        }
      })

      localLevels.value = newLevels
      emit('settings-synced', newLevels)
    } else {
      localLevels.value = JSON.parse(JSON.stringify(props.levels))
    }
  } catch (e) {
    console.error('알림 설정 로드 실패:', e)
  }
}

/* ----------------------------- */
/* 모달 열릴 때/닫힐 때 */
/* ----------------------------- */
watch(
  () => props.isVisible,
  (v) => {
    if (v) loadSettings()
    if (!v) emit('settings-updated')
  },
)

/* ----------------------------- */
/* 부모가 props.levels를 갱신했을 때 동기화 */
/* ----------------------------- */
watch(
  () => props.levels,
  (newLevels) => {
    if (!props.isVisible) {
      localLevels.value = JSON.parse(JSON.stringify(newLevels))
    }
  },
  { deep: true },
)

/* ----------------------------- */
/* 이벤트 */
/* ----------------------------- */
const close = () => {
  emit('update:isVisible', false)
}

const toggle = (id) => {
  localLevels.value = localLevels.value.map((lv) =>
    lv.id === id ? { ...lv, enabled: !lv.enabled } : lv,
  )
}

const save = async () => {
  const payload = {}

  localLevels.value.forEach((level) => {
    const map = FIELD_MAP[level.id]
    if (!map) return

    payload[map.alert] = level.enabled ? 'Y' : 'N'
    if (map.distance) payload[map.distance] = level.radius
  })

  const url = `${API_BASE_URL}/api/groups/settings/${props.groupId}`

  try {
    const res = await axios.post(url, payload, { withCredentials: true })

    if (res.data?.data?.success === true) {
      emit('settings-synced', localLevels.value)
      close()
    } else {
      alert(`설정 저장 실패: ${res.data.message}`)
    }
  } catch (e) {
    console.error('알림 설정 저장 중 오류:', e)
    alert('설정 저장 중 오류가 발생했습니다.')
  }
}
</script>

<style scoped>
.ga-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
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
  border: 1px solid #0b1956;
}

.ga-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px 8px;
  background-color: #0b1956;
}

.ga-header h5 {
  margin: 0;
  font-weight: 800;
  font-size: 18px;
  color: white;
}

.ga-close {
  background: transparent;
  border: none;
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
  color: white;
}

.ga-section-title {
  padding: 12px 18px;
  font-weight: 700;
  font-size: 16px;
  color: #0b1956;
  border-bottom: 1px solid #eee;
}

.ga-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 18px;
  gap: 12px;
}

.ga-label {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.ga-toggle {
  width: 56px;
  height: 32px;
  background: #d1d5db;
  border-radius: 999px;
  border: none;
  padding: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all 0.18s;
}

.ga-toggle.on {
  background: #0b1956;
  justify-content: flex-end;
}

.ga-knob {
  width: 24px;
  height: 24px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.25);
}

.ga-footer {
  padding: 18px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #eee;
}

.ga-btn {
  background: #0b1956;
  border: none;
  color: #fff;
  font-weight: 800;
  font-size: 15px;
  padding: 10px 20px;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.ga-btn:hover {
  background: #0092ba;
}
</style>
