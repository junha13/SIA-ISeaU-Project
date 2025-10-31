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

    <!-- 이안류 탭 -->
    <div v-if="active === 'rip'">
      <h6 class="fw-bold mb-3">
        이안류 안전등급:
        <span :style="{ color: dangerData?.rip === '주의' ? '#FFB354' : '#8482FF' }">
          {{ dangerData?.rip || '정보 없음' }}
        </span>
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
          :option="ripChartOption"
          style="width: 100%; height: 220px"
          autoresize
        />
      </div>
    </div>

    <!-- 해파리 탭 -->
    <div v-else-if="active === 'jelly'">
      <h6 class="fw-bold mb-3">
        해파리 출현 정보:
        <span :style="{ color: dangerData?.jelly === '주의' ? '#EB725B' : '#8482FF' }">
          {{ dangerData?.jelly || '정보 없음' }}
        </span>
      </h6>
      <div class="border rounded p-3 text-center small">
        <p>최근 신고 건수 및 위험 구역 표시</p>
      </div>
    </div>

    <!-- 파고 탭 -->
    <div v-else>
      <h6 class="fw-bold mb-3">
        파고 높이:
        <span :style="{ color: currentWaveHeight > 2 ? '#EB725B' : '#0092BA' }">
          {{ typeof currentWaveHeight === 'number' ? currentWaveHeight.toFixed(1) + 'm' : '정보 없음' }}
        </span>
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
import axios from 'axios';
import * as echarts from 'echarts/core';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { TooltipComponent, GridComponent, VisualMapComponent } from 'echarts/components';
import { CustomChart, LineChart } from 'echarts/charts';
import VChart from 'vue-echarts';

// ECharts 모듈 등록
use([
  CanvasRenderer,
  TooltipComponent,
  GridComponent,
  VisualMapComponent,
  CustomChart,
  LineChart,
]);

// --- Props & Route ---
const props = defineProps({
  detailData: Object,
});
const route = useRoute();

// --- 탭 관리 ---
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
  transition: 'background-color 0.2s',
});

// --- 데이터 상태 ---
const currentWaveHeight = ref(null);
const dangerData = computed(() => props.detailData?.danger);

// 이안류 차트 데이터 (더미)
const ripChartData = ref(
  Array.from({ length: 12 }, (_, i) => [i, Math.floor(Math.random() * 2)])
);

// 파고 차트 데이터
const waveChartData = ref([]); // Y축 값 배열
// [수정] X축 레이블 (0시 ~ 23시, 2시간 간격 12개)
const waveChartHours = ref(Array.from({ length: 12 }, (_, i) => `${i * 2}시`));

// --- API 호출 ---

// 이안류 데이터 가져오기 (더미)
async function fetchRipCurrentData(beachNumber) {
  console.warn('[DangerTab] (Rip) 이안류 API 미구현. 더미 데이터 사용.');
}

// 파고 데이터 가져오기
async function fetchWaveData(beachNumber) {
  const API_URL = `http://172.168.10.15:8080/api/beach/detail/${beachNumber}/danger`;

  waveChartData.value = [];
  currentWaveHeight.value = null;

  try {
    console.log(`[DangerTab] (Wave) API 요청: ${API_URL}`);
    const response = await axios.get(API_URL);
    console.log("[DangerTab] (Wave) API 전체 응답:", response);

    const history = response.data.data.result;
    console.log("[DangerTab] (Wave) 파싱된 'history' 데이터:", history);

    if (!Array.isArray(history) || history.length === 0) {
      console.warn(`[DangerTab] (Wave) 유효한 파고 데이터 없음.`);
      return;
    }

    // 시간순 정렬
    history.sort((a, b) => new Date(a.forecastTime) - new Date(b.forecastTime));

    // 현재 파고 높이 설정 (현재 시간과 가장 가까운 데이터)
    const now = new Date();
    let closestData = null;
    let minDiff = Infinity;
    history.forEach(item => {
      if (!item.forecastTime) return;
      try {
        const itemTime = new Date(item.forecastTime);
        if (isNaN(itemTime)) return;
        const diff = Math.abs(now - itemTime);
        if (diff < minDiff) {
          minDiff = diff;
          closestData = item;
        }
      } catch (e) { /* ignore */ }
    });
    if (closestData && typeof closestData.waveHeight === 'number') {
      currentWaveHeight.value = closestData.waveHeight;
      console.log(`[DangerTab] (Wave) 현재 파고 높이 설정: ${currentWaveHeight.value}m`);
    } else {
       console.warn(`[DangerTab] (Wave) 현재 파고 높이 찾기 실패.`);
    }

    // --- [수정] 차트 데이터 생성 (단순화) ---
    // API에서 받은 데이터 (history)에서 waveHeight 값만 추출 (최대 12개)
    const chartData = history
        .slice(0, 24) // 오늘 하루치 데이터 (최대 24개)
        .filter((_, index) => index % 2 === 0) // 2시간 간격으로 데이터 선택 (12개)
        .map(item => typeof item.waveHeight === 'number' ? item.waveHeight : undefined); // waveHeight 추출

    // 만약 데이터가 12개 미만이면 나머지는 undefined로 채움
    while (chartData.length < 12) {
        chartData.push(undefined);
    }

    console.log("[DangerTab] (Wave) 최종 차트 데이터 (Y축, 12개):", chartData);
    waveChartData.value = chartData;
    // waveChartHours는 고정값 사용

  } catch (e) {
    console.error("[DangerTab] 파고 데이터 로딩 실패:", e);
  }
}

// --- ECharts 옵션 ---

// 이안류 차트 옵션 (변경 없음)
const ripChartOption = computed(() => {
  const data = ripChartData.value;
  const levels = ['관심', '주의', '경계', '위험'];
  const hours = Array.from({ length: 12 }, (_, i) => `${String(i * 2).padStart(2, '0')}-${String(i * 2 + 1).padStart(2, '0')}`);
  const colors = { '관심': '#9AE6B4', '주의': '#FFD700', '경계': '#FF8C00', '위험': '#FF6347' };

  return {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        const xIndex = params.value[0];
        const yIndex = params.value[1];
        if (yIndex === null || yIndex === undefined) return '정보 없음';
        return `<b>${hours[xIndex]}</b><br/>등급: ${levels[yIndex]}`;
      },
      backgroundColor: 'rgba(0,0,0,0.7)',
      borderColor: '#333',
      textStyle: { color: '#fff', fontSize: 12 },
      extraCssText: 'box-shadow: 0 0 8px rgba(0, 0, 0, 0.3);',
    },
    grid: { top: 30, bottom: 20, left: 50, right: 10 },
    xAxis: [
      {
        type: 'category', data: hours, position: 'top',
        axisLine: { show: false }, axisTick: { show: false },
        axisLabel: { color: '#6B7280', fontSize: 9, interval: 0, align: 'center' },
        splitLine: { show: true, lineStyle: { color: '#E0E0E0', type: 'dashed', opacity: 0.5 } },
      },
      {
        type: 'category', data: hours, position: 'bottom',
        axisLine: { show: true, lineStyle: { color: '#E0E0E0' } },
        axisTick: { show: false },
        axisLabel: { show: false },
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
    series: [{
      type: 'custom',
      renderItem: (params, api) => {
        const xIndex = api.value(0);
        const yIndex = api.value(1);
        if (yIndex === undefined || yIndex === null) return;
        const centerPoint = api.coord([xIndex, yIndex]);
        const cellSize = api.size([1, 1]);
        const cellWidth = cellSize[0];
        const cellHeight = cellSize[1];
        const gapRatio = 0.1;
        const rectWidth = cellWidth * (1 - 2 * gapRatio);
        const rectHeight = cellHeight * (1 - 2 * gapRatio);
        const groupX = centerPoint[0] - rectWidth / 2;
        const groupY = centerPoint[1] - rectHeight / 2;
        return {
          type: 'group', x: groupX, y: groupY,
          children: [
            {
              type: 'rect',
              shape: { x: 0, y: 0, width: rectWidth, height: rectHeight },
              style: { fill: api.visual('color'), shadowBlur: 3, shadowColor: 'rgba(0, 0, 0, 0.1)', shadowOffsetX: 1, shadowOffsetY: 1 },
            },
            {
              type: 'text', x: rectWidth / 2, y: rectHeight / 2,
              style: { text: levels[yIndex], fill: '#333', fontSize: 11, fontWeight: 'bold', textAlign: 'center', textVerticalAlign: 'middle' },
            },
          ],
        };
      },
      data: data,
      xAxisIndex: 0,
      yAxisIndex: 0,
    }],
  };
});

// 파고 차트 옵션 (Line Chart)
const waveChartOption = computed(() => {
  const data = waveChartData.value; // Y축 데이터 (API 결과)
  const hours = waveChartHours.value; // X축 레이블 (0시, 2시, ...)

  // 현재 시간에 해당하는 데이터 인덱스 찾기 (markPoint용)
  const now = new Date();
  const currentHour = now.getHours();
  let nowIndex = -1;
   // '0시', '2시' ... 레이블과 현재 시간을 비교하여 가장 가까운 인덱스 찾기
   let minHourDiff = Infinity;
   hours.forEach((hourLabel, index) => {
       const hourNum = parseInt(hourLabel.replace('시', ''));
       const diff = Math.abs(currentHour - hourNum);
       if (diff < minHourDiff) {
           minHourDiff = diff;
           nowIndex = index;
       }
       // 시간이 같으면 현재 시간이 더 가까운 쪽 선택 (오후 시간대 처리)
        else if (diff === minHourDiff && hourNum > parseInt(hours[nowIndex].replace('시', ''))) {
            nowIndex = index;
        }
   });


  return {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const param = params[0];
        const xIndex = param.dataIndex;
        const value = param.value;
        if (value === null || typeof value === 'undefined') {
          return `<b>${hours[xIndex]}</b><br/>파고: 정보 없음`;
        }
        return `<b>${hours[xIndex]}</b><br/>파고: ${value.toFixed(1)}m`;
      },
      backgroundColor: 'rgba(0,0,0,0.7)',
      borderColor: '#333',
      textStyle: { color: '#fff', fontSize: 12 },
    },
    grid: {
      top: 20, bottom: 30, left: 50, right: 20,
    },
    xAxis: [{ // X축 (시간)
      type: 'category',
      data: hours, // '0시', '2시', ...
      position: 'bottom',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: '#6B7280',
        fontSize: 10,
        interval: 0, // 모든 레이블 표시
         // 'Now' 표시 제거 (X축 레이블만 사용)
      },
      splitLine: { show: true, lineStyle: { color: '#EFEFEF', type: 'solid', opacity: 0.8 } },
    }],
    yAxis: { // Y축 (파고 높이)
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#EFEFEF', type: 'solid', opacity: 0.8 } },
      axisLabel: {
        color: '#6B7280',
        fontWeight: 'bold', fontSize: 12, margin: 15,
        formatter: '{value}m'
      },
    },
    series: [{ // 선 그래프
      type: 'line',
      data: data, // 파고 높이 배열
      smooth: true,
      showSymbol: false,
      itemStyle: { color: '#FFB354' },
      lineStyle: { width: 3, color: '#FFB354' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(255, 179, 84, 0.4)' },
          { offset: 1, color: 'rgba(255, 179, 84, 0.05)' }
        ])
      },
      markPoint: { // 현재 값 표시 점
        symbol: 'circle', symbolSize: 10,
        data: [{
            // 현재 시간 인덱스를 사용하여 좌표 설정
            coord: nowIndex !== -1 && data[nowIndex] !== undefined ? [nowIndex, data[nowIndex]] : undefined,
             itemStyle: { color: '#FFB354', borderColor: '#fff', borderWidth: 2 }
        }],
        label: { show: false }
      },
      emphasis: { focus: 'series' },
    }],
  };
});

// --- 생명주기 훅 ---
onMounted(() => {
  const beachNumber = route.params.beachNumber;
  if (beachNumber) {
    fetchRipCurrentData(beachNumber);
    fetchWaveData(beachNumber);
  }
});
</script>

<style scoped>
.chart {
  width: 100%;
  height: 220px;
}
</style>

