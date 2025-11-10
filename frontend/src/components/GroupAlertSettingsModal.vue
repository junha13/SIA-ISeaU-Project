<template>
  <div v-if="isVisible" class="ga-backdrop">
    <div class="ga-modal">
      <!-- 헤더 -->
      <div class="ga-header">
        <h5>설정</h5>
        <button class="ga-close" @click="close">×</button>
      </div>

      <!-- 제목 -->
      <div class="ga-section-title">그룹 이탈 알림</div>

      <!-- 내용 -->
      <div class="ga-row" v-for="level in localLevels" :key="level.id">
        <div class="ga-label">
          {{ level.label }}  {{ level.radius===0 ? '' : `(현재 ${level.radius}m)`}}
        </div>
        <button
          class="ga-toggle"
          :class="{ on: level.enabled }"
          @click="toggle(level.id)"
        >
          <span class="ga-knob"></span>
        </button>
      </div>

      <!-- 하단 버튼 -->
      <div class="ga-footer">
        <button class="ga-btn" @click="save">저장</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, watch, ref } from 'vue'

const props = defineProps({
  isVisible: { type: Boolean, default: false },
  // 부모가 나중에 DB 값 넣어줄 자리
  levels: {
    type: Array,
    default: () => [
      { id: 1, label: '1단계', radius: 3, enabled: true },
      { id: 2, label: '2단계', radius: 200, enabled: false },
      { id: 3, label: '수영 알림', radius: 0, enabled: true },
    ],
  },
})

const emit = defineEmits(['update:isVisible', 'save'])



const localLevels = ref(JSON.parse(JSON.stringify(props.levels)))

watch(
  () => props.isVisible,
  (v) => {
    if (v) {
      // 열릴 때 부모값 다시 복사
    localLevels.value = JSON.parse(JSON.stringify(props.levels))
    }
  }
)

const close = () => {
  emit('update:isVisible', false)
}

const toggle = (id) => {
  localLevels.value = localLevels.value.map((lv) =>
    lv.id === id ? { ...lv, enabled: !lv.enabled } : lv
  )
}

const save = () => {
  emit('save', localLevels.value)
  close()
}
</script>

<style scoped>
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