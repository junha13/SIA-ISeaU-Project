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
      <h6 class="fw-bold mb-3">
        이안류 안전등급:
        <span
          :style="{
            color: dangerData?.rip === '주의' ? '#FFB354' : '#8482FF',
          }"
          >{{ dangerData?.rip || '정보 없음' }}</span
        >
        <i
          class="fas fa-info-circle text-muted ms-1"
          data-bs-toggle="tooltip"
          data-bs-placement="top"
          title="이안류 등급에 대한 설명"
        ></i>
      </h6>

      <div class="border rounded p-2">
        <v-chart
          class="chart"
          :option="chartOption"
          style="width: 100%; height: 220px"
          autoresize
        />
      </div>
    </div>

    <div v-else-if="active === 'jelly'">
      <h6 class="fw-bold mb-3">
        해파리 출현 정보:
        <span
          :style="{
            color: dangerData?.jelly === '주의' ? '#EB725B' : '#8482FF',
          }"
          >{{ dangerData?.jelly || '정보 없음' }}</span
        >
      </h6>
      <div class="border rounded p-3 text-center small">
        <p>최근 신고 건수 및 위험 구역 표시</p>
      </div>
    </div>

    <div v-else>
      <h6 class="fw-bold mb-3">
        파고 높이:
        <span
          :style="{ color: dangerData?.wave > 2 ? '#EB725B' : '#0092BA' }"
          >{{ dangerData?.wave || 0 }}m</span
        >
      </h6>
      <div class="border rounded p-2">
        <v-chart
          class="chart"
          :option="waveChartOption"
          style="width: 100%; height: 220px"
          autoresize
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, defineProps, onMounted } from 'vue';
import { useRoute } from 'vue-router';
// 백엔드 연결 준비가 되면 axios를 다시 사용합니다.
import axios from 'axios';
import * as echarts from 'echarts/core';
// --- ECharts (vue-echarts) ---
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import {
  TooltipComponent,
  GridComponent,
  VisualMapComponent,
} from 'echarts/components';
import { CustomChart, LineChart } from 'echarts/charts';
import VChart from 'vue-echarts';

// ECharts 모듈 수동 등록
use([
  CanvasRenderer,
  TooltipComponent,
  GridComponent,
  VisualMapComponent,
  CustomChart,
  LineChart,
]);
// --- ECharts ---

// --- 부모 Props ---
const props = defineProps({
  detailData: Object,
});

// --- 라우트 ---
const route = useRoute();

// --- 탭 관리 로직 ---
const active = ref('rip');
const dangerTabs = [
  { key: 'rip', label: '이안류', color: '#8482FF' },
  { key: 'jelly', label: '해파리', color: '#EB725B' },
  { key: 'wave', label: '파고', color: '#FFB354' },
];

const tabStyle = (tab) => ({
  backgroundColor: active.value === tab.key ? tab.color : tab.color + '80',
  color: '#fff',
  cursor: 'pointer',
  transition: '0.2s',
});

// --- 데이터 가공 ---

const dangerData = computed(() => props.detailData?.danger);

// === 이안류 차트 데이터 ===
const getDummyChartData = () => {
  // '관심': 0, '주의': 1, '경계': 2, '위험': 3
  const aggregatedLevels = [
    0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0,
  ];
  return aggregatedLevels.map((level, index) => [index, level]);
};
const ripChartData = ref(getDummyChartData());

// === 파고 차트 데이터 ===
const getDummyWaveData = () => {
  return [
    1.2, 1.5, 1.1, 1.3, 1.0, 1.4,
    1.6, 1.3, 1.8, 1.5, 1.7, 1.8
  ];
};
const waveChartData = ref(getDummyWaveData());

// --- API 호출 함수 (이안류) ---
async function fetchRipCurrentData(beachNumber) {
  console.warn(
    '[DangerTab] (Rip) 백엔드 미완성. API 호출을 건너뛰고 더미 데이터를 강제 설정합니다.'
  );
  ripChartData.value = getDummyChartData();
  // 실제 API 연동 시 주석 해제 및 로직 구현
  /*
    try {
        const response = await axios.get(`http://localhost:8080/api/beach/detail/${beachNumber}/rip-current`);
        const history = response.data.data.result.ripHistory; 
        if (!Array.isArray(history) || history.length < 24) { 
            console.warn("API에서 24시간 분량의 history 배열을 받지 못했습니다. (더미 데이터 유지)");
            return; 
        }
        // ... (2시간 단위 12개 집계 로직) ...
        ripChartData.value = aggregatedData;
    } catch (e) {
        console.error("[DangerTab] 이안류 데이터 로딩 실패:", e);
    }
    */
}

// --- API 호출 함수 (파고) ---
async function fetchWaveData(beachNumber) {
  console.warn(
    '[DangerTab] (Wave) 백엔드 미완성. API 호출을 건너뛰고 더미 데이터를 강제 설정합니다.'
  );
  waveChartData.value = getDummyWaveData();
  // 실제 API 연동 시 주석 해제 및 로직 구현
  /*
    try {
        const response = await axios.get(`http://localhost:8080/api/beach/detail/${beachNumber}/wave-height`);
        const history = response.data.data.result.waveHistory;
        if (!Array.isArray(history) || history.length < 24) { 
            console.warn("API에서 24시간 분량의 wave history 배열을 받지 못했습니다. (더미 데이터 유지)");
            return; 
        }
        // ... (24개 데이터를 12개로 집계하는 로직) ...
        // waveChartData.value = aggregatedData;
    } catch (e) {
        console.error("[DangerTab] 파고 데이터 로딩 실패:", e);
    }
    */
}

// --- ECharts 옵션 (이안류 - Custom Chart) ---
const chartOption = computed(() => {
  const data = ripChartData.value;
  const levels = ['관심', '주의', '경계', '위험'];
  const hours = [
    '00-01', '02-03', '04-05', '06-07', '08-09', '10-11',
    '12-13', '14-15', '16-17', '18-19', '20-21', '22-23'
  ];
  const colors = {
    '관심': '#9AE6B4', '주의': '#FFD700',
    '경계': '#FF8C00', '위험': '#FF6347',
  };

  return {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        const xIndex = params.value[0];
        const yIndex = params.value[1];
        if (yIndex === null || yIndex === undefined) return '정보 없음';
        return `<b>${hours[xIndex]}시</b><br/>등급: ${levels[yIndex]}`;
      },
      backgroundColor: 'rgba(0,0,0,0.7)',
      borderColor: '#333',
      textStyle: { color: '#fff', fontSize: 12 },
      extraCssText: 'box-shadow: 0 0 8px rgba(0, 0, 0, 0.3);',
    },
    grid: { top: 40, bottom: 40, left: 50, right: 10 },
    xAxis: [
      {
        type: 'category', data: hours, position: 'top',
        axisLine: { show: false }, axisTick: { show: false },
        axisLabel: { color: '#6B7280', fontSize: 9, interval: 0, align: 'center' },
        splitLine: { show: true, lineStyle: { color: '#E0E0E0', type: 'dashed', opacity: 0.5 } },
      },
      {
        type: 'category', data: hours, position: 'bottom',
        axisLine: { show: false },
        axisTick: {
          show: true, length: 5, lineStyle: { color: '#B0B0B0' },
          alignWithLabel: false,
          interval: (index) => [1, 3, 5, 7, 9, 11].includes(index),
        },
        axisLabel: {
          color: '#374151', fontWeight: 'bold', fontSize: 11,
          formatter: (value, index) => {
            if (index === 11) return '현재';
            if (index === 9) return '4시간 전';
            if (index === 7) return '8시간 전';
            if (index === 5) return '12시간 전';
            if (index === 3) return '16시간 전';
            if (index === 1) return '20시간 전';
            return '';
          },
        },
      },
    ],
    yAxis: {
      type: 'category', data: levels, inverse: true,
      axisLine: { show: false }, axisTick: { show: false },
      axisLabel: { color: '#374151', fontWeight: 'bold', fontSize: 12, margin: 15 },
      splitLine: { show: true, lineStyle: { color: '#E0E0E0', type: 'solid', opacity: 0.7 } },
    },
    visualMap: {
      type: 'piecewise', dimension: 1,
      pieces: [
        { value: 0, label: '관심', color: colors['관심'] },
        { value: 1, label: '주의', color: colors['주의'] },
        { value: 2, label: '경계', color: colors['경계'] },
        { value: 3, label: '위험', color: colors['위험'] },
      ],
      show: false,
    },
    series: [
      {
        type: 'custom',
        renderItem: (params, api) => {
          const xIndex = api.value(0);
          const yIndex = api.value(1);
          if (yIndex === undefined || yIndex === null) return;
          const centerPoint = api.coord([xIndex, yIndex]);
          const cellWidth = api.size([1, 0])[0];
          const cellHeight = api.size([0, 1])[1];
          const xGapRatio = 0.1; const yGapRatio = 0.1;
          const rectWidth = cellWidth * (1 - 2 * xGapRatio);
          const rectHeight = cellHeight * (1 - 2 * yGapRatio);
          const groupX = centerPoint[0] - rectWidth / 2;
          const groupY = centerPoint[1] + cellHeight * yGapRatio;
          return {
            type: 'group', x: groupX, y: groupY,
            children: [
              {
                type: 'rect',
                shape: { x: 0, y: 0, width: rectWidth, height: rectHeight },
                style: {
                  fill: api.visual('color'),
                  shadowBlur: 5, shadowColor: 'rgba(0, 0, 0, 0.1)',
                  shadowOffsetX: 1, shadowOffsetY: 1,
                  stroke: '#fff', lineWidth: 0.5,
                },
              },
              {
                type: 'text',
                x: rectWidth / 2, y: rectHeight / 2,
                style: {
                  text: levels[yIndex], fill: '#333',
                  fontSize: 11, fontWeight: 'bold',
                  textAlign: 'center', textVerticalAlign: 'middle',
                },
              },
            ],
          };
        },
        data: data,
        xAxisIndex: 0,
        yAxisIndex: 0,
      },
    ],
  };
});

// --- ECharts 옵션 (파고 - Line Chart) ---
const waveChartOption = computed(() => {
  const data = waveChartData.value;
  const hours = [
    '00-01', '02-03', '04-05', '06-07', '08-09', '10-11',
    '12-13', '14-15', '16-17', '18-19', '20-21', '22-23'
  ];

  return {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const param = params[0];
        const xIndex = param.dataIndex;
        const value = param.value;
        return `<b>${hours[xIndex]}시</b><br/>파고: ${value}m`;
      },
      backgroundColor: 'rgba(0,0,0,0.7)',
      borderColor: '#333',
      textStyle: { color: '#fff', fontSize: 12 },
    },
    grid: {
      top: 40,
      bottom: 40,
      left: 50,
      right: 10,
    },
    xAxis: [
      {
        type: 'category', data: hours, position: 'top',
        axisLine: { show: false }, axisTick: { show: false },
        axisLabel: { color: '#B0B0B0', fontSize: 9, interval: 0, align: 'center' }, // 색상 연하게 변경
        splitLine: { show: true, lineStyle: { color: '#EFEFEF', type: 'solid', opacity: 0.8 } }, // 그리드 라인 실선, 연하게 변경
      },
      {
        type: 'category', data: hours, position: 'bottom',
        axisLine: { show: false },
        axisTick: {
          show: true, length: 5, lineStyle: { color: '#B0B0B0' },
          alignWithLabel: false,
          interval: (index) => [1, 3, 5, 7, 9, 11].includes(index),
        },
        axisLabel: {
          color: '#6B7280', fontWeight: 'bold', fontSize: 11, // 색상 조정
          formatter: (value, index) => {
            if (index === 11) return '현재';
            if (index === 9) return '4시간 전';
            if (index === 7) return '8시간 전';
            if (index === 5) return '12시간 전';
            if (index === 3) return '16시간 전';
            if (index === 1) return '20시간 전';
            return '';
          },
        },
      },
    ],
    yAxis: {
      type: 'value',
      axisLine: { show: false }, axisTick: { show: false },
      splitLine: {
        lineStyle: { color: '#EFEFEF', type: 'solid', opacity: 0.8 }, // 그리드 라인 실선, 연하게 변경
      },
      axisLabel: {
        color: '#6B7280', // 색상 조정
        fontWeight: 'bold', fontSize: 12, margin: 15,
        formatter: '{value}m'
      },
    },
    series: [
      {
        type: 'line',
        data: data,
        smooth: true, // [수정됨] 부드러운 곡선
        showSymbol: false,
        itemStyle: {
          color: '#FFB354'
        },
        lineStyle: {
          width: 3,
          color: '#FFB354'
        },
        // [신규] 그라디언트 영역 채우기
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255, 179, 84, 0.4)' }, // 시작 색 (진하게)
            { offset: 1, color: 'rgba(255, 179, 84, 0.05)' } // 끝 색 (투명하게)
          ])
        },
        markPoint: {
          symbol: 'circle',
          symbolSize: 12, // [수정됨] 마크 포인트 크기 약간 키움
          data: [
            {
              coord: [data.length - 1, data[data.length - 1]],
              itemStyle: {
                color: '#FFB354',
                borderColor: '#fff',
                borderWidth: 3 // [수정됨] 테두리 두께 키움
              }
            }
          ],
          label: { show: false }
        },
        emphasis: {
            focus: 'series'
        },
      }
    ],
  };
});

// --- ECharts 생명주기 훅 ---
onMounted(() => {
  const beachNumber = route.params.beachNumber;
  if (beachNumber) {
    fetchRipCurrentData(beachNumber);
    fetchWaveData(beachNumber);
  }
});
</script>

<style scoped>
.chart-fill {
  margin: auto;
}
</style>