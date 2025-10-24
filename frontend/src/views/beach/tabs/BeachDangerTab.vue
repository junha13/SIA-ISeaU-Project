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

    <div v-if="active === 'rip'">
      <h6 class="fw-bold mb-3">이안류 안전등급: <span :style="{ color: dangerData?.rip === '주의' ? '#FFB354' : '#8482FF' }">{{ dangerData?.rip || '정보 없음' }}</span></h6>
      <div class="border rounded text-center small h-500px">
        <span>
          <v-chart :option="option" autoresize class="chart-fill h-50" />
        </span>
      </div>
    </div>
    <div v-else-if="active === 'jelly'">
      <h6 class="fw-bold mb-3">해파리 출현 정보: <span :style="{ color: dangerData?.jelly === '주의' ? '#EB725B' : '#8482FF' }">{{ dangerData?.jelly || '정보 없음' }}</span></h6>
      <div class="border rounded p-3 text-center small">
        <p>최근 신고 건수 및 위험 구역 표시</p>
      </div>
    </div>
    <div v-else>
      <h6 class="fw-bold mb-3">파고 높이: <span :style="{ color: dangerData?.wave > 2 ? '#EB725B' : '#0092BA' }">{{ dangerData?.wave || 0 }}m</span></h6>
      <div class="border rounded p-3 text-center small">
        <p>파고 그래프 표시 (평균 {{ dangerData?.wave || 0 }}m)</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, defineProps } from 'vue'

const props = defineProps({
  detailData: Object,
});

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

if (active === "rip") {

}
const dangerData = computed(() => props.detailData?.danger);


// 차트 옵션만들기
let option = {
  // 차트 제목
  title: {
    text: 'ddd',
  },
  // 범례명
  legend: {
    data: ['개수'],
    top: 20,
  },
  // x축 라벨
  xAxis: {
    data: ['00', '01', '02', '03', '04', '05', '06', '07',
           '08', '09', '10', '11', '12', '13', '14', '15',
           '16', '17', '18', '19', '20', '21', '22', '23',
    ],
  },
  yAxis: {},
  series: [
    {
      name: '개수',
      type: 'bar', // 막대 그래프
      data: [5, 3, 4],
    },
  ],
}

</script>