<template>
  <div class="p-3">
    <div class="d-flex justify-content-around mb-3">
      <div
          v-for="tab in dangerTabs"
          :key="tab.key"
          class="text-center py-2 px-3 rounded fw-bold flex-fill mx-1"
          :style="tabStyle(tab)"
          @click="active = tab.key"
      >
        {{ tab.label }}
      </div>
    </div>

    <!-- 각 탭별 표시 -->
    <div v-if="active === 'rip'">
      <h6 class="fw-bold mb-3">이안류 안전등급</h6>
      <div class="border rounded p-3 text-center small">
        <p>관심 ~ 위험 등급을 그래프로 시각화</p>
      </div>
    </div>
    <div v-else-if="active === 'jelly'">
      <h6 class="fw-bold mb-3">해파리 출현 정보</h6>
      <div class="border rounded p-3 text-center small">
        <p>최근 신고 건수 및 위험 구역 표시</p>
      </div>
    </div>
    <div v-else>
      <h6 class="fw-bold mb-3">파고 높이</h6>
      <div class="border rounded p-3 text-center small">
        <p>파고 그래프 표시 (평균 1.8m)</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const active = ref('rip')

const dangerTabs = [
  { key: 'rip', label: '이안류', color: '#8482FF' },
  { key: 'jelly', label: '해파리', color: '#EB725B' },
  { key: 'wave', label: '파고', color: '#FFB354' },
]

const tabStyle = (tab) => ({
  backgroundColor: active.value === tab.key ? tab.color : tab.color + '80',
  color: '#fff',
  cursor: 'pointer',
  transition: '0.2s'
})
</script>
