<template>
  <div class="p-3">
    <!-- 🔹 기상 정보 내부 탭 -->
    <div class="tab-segment-group mb-3">
      <button
        type="button"
        class="tab-segment flex-fill"
        :class="{ active: innerTab === 'risk' }"
        @click="innerTab = 'risk'"
      >
        위험 정보
      </button>
      <button
        type="button"
        class="tab-segment flex-fill"
        :class="{ active: innerTab === 'weather' }"
        @click="innerTab = 'weather'"
      >
        실시간 날씨
      </button>
    </div>
    
     <!-- ============ ① 위험 정보 탭 ============ -->
    <div v-if="innerTab === 'risk'" class="tab-pane-block">
      <div
        v-if="dangerData && dangerData.length > 0"
        class="card shadow-sm mb-3 p-3 risk-card"
      >
    <h6 class="fw-bold mb-1">위험 정보</h6>

    <div v-if="!latestDanger">
      <small class="text-muted">위험 정보를 불러오는 중...</small>
    </div>

    <!-- 🔹 숫자 + 차트 모두 v-else 안에 -->
    <div v-else>
      <!-- 1) 위쪽 숫자 영역 -->
      <div class="row text-center small mb-3">

    <div class="col">
  <small>유의 파고</small>
  <div class="fw-bold">
    <span v-if="cardWaveHeight !== null">
      {{ cardWaveHeight.toFixed(2) }} m
    </span>
    <span v-else>-- m</span>
  </div>
</div>
<div class="col">
  <small>수온</small>
  <div class="fw-bold">
    <span v-if="cardSeaSurfaceTemp !== null">
      {{ cardSeaSurfaceTemp.toFixed(1) }} ℃
    </span>
    <span v-else>-- ℃</span>
  </div>
</div>
</div>

      <hr class="my-2" />

      <!-- 2) 여기부터가 “위험 정보 밑 차트” 부분 -->
      <h6 class="fw-bold mb-2">시간별 유의 파고</h6>
      <div class="border rounded p-2">
        <VChart
          class="chart chart-sm"
          :option="waveChartOption"

          autoresize
        />
      </div>
      <h6 class="fw-bold mt-3 mb-2">시간별 수온</h6>
      <div class="border rounded p-2">
        <VChart
          class="chart chart-sm"
          :option="seaSurfaceTemperatureChartOption"
          autoresize
        />
      </div>

    </div>
  </div>
</div>



    <!-- ================== ② 실시간 날씨 탭 ================== -->
    <div v-else class="tab-pane-block">
      <!-- 1. 실시간 날씨 -->
      <div class="card shadow-sm mb-3 p-3">
        <h6 class="fw-bold mb-1">실시간 날씨</h6>
        <div class="d-flex align-items-center justify-content-between">
          <div>
            <div class="fs-3 fw-bold">{{ currentWeather?.temperature || '--' }}°C</div>
            <small class="text-muted">{{ getWeatherStatus(currentWeather) || '정보 없음' }}</small>
          </div>
          <div class="text-end text-secondary small">
            <div>체감온도 {{ Math.round((currentWeather?.temperature || 0) + 2) }}°C</div>
            <div>풍속 {{ currentWeather?.windSpeed || '--' }}m/s</div>
          </div>
        </div>
      </div>

      <!-- 2. 시간별 날씨 (오늘) -->
      <div class="card shadow-sm p-3 mb-3">
        <h6 class="fw-bold mb-2">시간별 날씨 (오늘)</h6>
        <div v-if="hourlyForecastSlice.length > 0" class="d-flex justify-content-around text-center">
          <div v-for="item in hourlyForecastSlice" :key="item.forecastTime">
            <div>
              <i :class="getWeatherIcon(item)" class="fs-4" :style="{ color: '#FFB354' }"></i>
            </div>
            <div>{{ formatTimeOnly(item.forecastTime) }}시</div>
            <div class="fw-bold">{{ item.temperature ?? '--' }}°C</div>
          </div>
        </div>
        <div v-else class="text-center text-muted small py-2">
          시간별 예보 정보가 없습니다.
        </div>
      </div>

      <!-- 3. 상세 정보 -->
      <div class="card shadow-sm p-3 mb-3">
        <h6 class="fw-bold mb-2">상세 정보</h6>
        <div class="row text-center">
          <div class="col">
            <small>풍속</small>
            <div>{{ currentWeather?.windSpeed ?? '--' }}m/s</div>
          </div>
          <div class="col">
            <small>습도</small>
            <div>{{ currentWeather?.humidity ?? '--' }}%</div>
          </div>
          <div class="col">
            <small>자외선지수</small>
            <div>{{ currentWeather?.uvIndex ?? '--' }}</div>
          </div>
          <div class="col">
            <small>강수확률</small>
            <div>{{ currentWeather?.rainProbability ?? '--' }}%</div>
          </div>
        </div>
      </div>

      <!-- 4. 일별 예보 -->
      <div class="card shadow-sm p-3">
        <h6 class="fw-bold mb-2">일별 예보</h6>

        <div class="d-flex justify-content-between mb-3 border-bottom">
          <button
            v-for="day in toggleDays"
            :key="day.dateStr"
            @click="selectDay(day.dateStr)"
            class="btn btn-sm flex-fill rounded-0 bg-white border-0 shadow-none text-center py-2"
            :class="{
              'tab-active fw-bold': selectedDay === day.dateStr,
              'text-muted fw-normal': selectedDay !== day.dateStr
            }"
          >
            {{ day.label }} ({{ day.dayOfWeek }})
          </button>
        </div>
        
        <div
          v-if="selectedDayHourlyForecast.length > 0"
          class="d-flex justify-content-around text-center overflow-x-auto"
        >
          <div
            v-for="item in selectedDayHourlyForecast"
            :key="item.forecastTime"
            class="p-2"
            style="min-width: 60px;"
          >
            <div>
              <i :class="getWeatherIcon(item)" class="fs-4" :style="{ color: '#FFB354' }"></i>
            </div>
            <div>{{ formatTimeOnly(item.forecastTime) }}시</div>
            <div class="fw-bold">{{ item.temperature ?? '--' }}°C</div>
          </div>
        </div>

        <div v-else class="text-center text-muted small py-2">
          {{ toggleDays.length > 0 ? '해당 날짜의 예보 정보가 없습니다.' : '다음 날 예보 정보가 없습니다.' }}
        </div>
      </div>
    </div>
  </div>

</template>


<script setup>
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import axios from 'axios'
import VChart from 'vue-echarts'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { TooltipComponent, GridComponent } from 'echarts/components'
import * as echarts from 'echarts/core'

echarts.use([CanvasRenderer, LineChart, TooltipComponent, GridComponent])

function formatForecastDateTime(str) {
  if (!str) return '--'
  try {
    const d = new Date(String(str).replace(' ', 'T'))
    if (isNaN(d)) return String(str)

    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hh = String(d.getHours()).padStart(2, '0')
    const mm = String(d.getMinutes()).padStart(2, '0')

    return `${y}-${m}-${day} ${hh}:${mm}`
  } catch (e) {
    return String(str)
  }
}

const refreshTimer = ref(null)
const REFRESH_MS = 5 * 60 * 1000

const props = defineProps({
  beachNumber: {
    type: [String, Number],
    required: true,
  },
})

const innerTab = ref('risk')

const weatherData = ref(null)
const dangerData  = ref([])

// ======= 위험 정보 정렬 & 로딩 체크 =======
const sortedDangerList = computed(() => {
  const list = dangerData.value
  if (!Array.isArray(list)) return []
  return list
    .filter(item => item && item.forecastTime)
    .slice()
    .sort((a, b) => new Date(a.forecastTime) - new Date(b.forecastTime))
})

const latestDangerIndex = computed(() => {
  const list = sortedDangerList.value
  if (list.length === 0) return -1

  const now = new Date()
  const idx = list.findIndex(item => new Date(item.forecastTime) >= now)
  return idx === -1 ? list.length - 1 : idx
})

const latestDanger = computed(() => {
  const list = sortedDangerList.value
  const idx = latestDangerIndex.value
  if (idx < 0 || idx >= list.length) return null
  return list[idx]
})

// ======= 날씨 관련 =======
const dayNames = ['일', '월', '화', '수', '목', '금', '토']

const toggleDays = computed(() => {
  if (!Array.isArray(weatherData.value) || weatherData.value.length === 0) {
    return []
  }

  const uniqueDates = Array.from(
    new Set(
      weatherData.value
        .map(item => item.forecastTime?.substring(0, 10))
        .filter(Boolean)
    )
  ).sort()

  return uniqueDates.slice(0, 2).map((dateStr, idx) => {
    const d = new Date(dateStr)
    const dow = dayNames[d.getDay()]

    let label = ''
    if (idx === 1) label = '모레'
    else label = '내일'

    return {
      dateStr,
      label,
      dayOfWeek: dow,
    }
  })
})

async function requestWeatherData(beachNumber) {
  try {
    const response = await axios.get(
      `${import.meta.env.VITE_API_BASE_URL}/api/beach/detail/${beachNumber}/weather`
    )

    weatherData.value = response.data.data.result

    if (weatherData.value && weatherData.value.length > 0) {
      const current = weatherData.value[0]
      console.log('[WeatherPanel] currentWeather:', current)
      console.log(' -> uvIndex:', current.uvIndex)
      console.log(' -> rainProbability:', current.rainProbability)
      console.log(' -> rain:', current.rain)

      selectedDay.value = getLocalDateString(
        new Date(Date.now() + 24 * 60 * 60 * 1000)
      )
    } else {
      console.log('[WeatherPanel] 날씨 데이터가 비어있습니다.')
    }
  } catch (e) {
    console.error('[WeatherPanel] 날씨 정보 로딩 실패:', e)
    weatherData.value = []
  }
}

async function requestDangerData(beachNumber) {
  try {
    console.log('[WeatherPanel] 위험 정보 요청:', beachNumber)

    const response = await axios.get(
      `${import.meta.env.VITE_API_BASE_URL}/api/beach/detail/${beachNumber}/danger`
    )

    console.log('[WeatherPanel] /danger 응답 원본:', response.data)

    const outer = response.data?.data
    const list = outer?.result ?? outer

    dangerData.value = Array.isArray(list)
      ? list
      : (list ? [list] : [])

    console.log('[WeatherPanel] 파싱된 dangerData:', dangerData.value)
  } catch (e) {
    console.error('[WeatherPanel] 위험 정보 로딩 실패:', e)
    dangerData.value = []
  }
}

// ======= 마운트/언마운트 =======
onMounted(() => {
  if (props.beachNumber) {
    const loadAll = () => {
      requestWeatherData(props.beachNumber)
      requestDangerData(props.beachNumber)
    }

    loadAll()
    refreshTimer.value = setInterval(loadAll, REFRESH_MS)
  }
})

onBeforeUnmount(() => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
  }
})

watch(
  () => props.beachNumber,
  (val) => {
    if (val) {
      requestWeatherData(val)
      requestDangerData(val)
    }
  }
)

// ======= computed & helpers =======
const selectedDay = ref(null)

function selectDay(dateStr) {
  selectedDay.value = dateStr
}

const currentWeather = computed(() => {
  if (!Array.isArray(weatherData.value) || weatherData.value.length === 0) return null
  return weatherData.value[0]
})

const hourlyForecastSlice = computed(() => {
  if (!Array.isArray(weatherData.value) || weatherData.value.length === 0) return []
  return weatherData.value.slice(0, 6)
})

const selectedDayHourlyForecast = computed(() => {
  if (!Array.isArray(weatherData.value) || weatherData.value.length === 0 || !selectedDay.value) {
    return []
  }

  const selectedDate = selectedDay.value

  const filteredByDay = weatherData.value
    .filter((item) => item.forecastTime && item.forecastTime.substring(0, 10) === selectedDate)
    .sort((a, b) => new Date(a.forecastTime) - new Date(b.forecastTime))

  const result = []

  for (let i = 0; i < filteredByDay.length; i++) {
    const hour = formatTimeOnly(filteredByDay[i].forecastTime)
    if (hour % 3 === 0) {
      result.push(filteredByDay[i])
    }
  }

  const uniqueResult = result.filter(
    (item, index, self) => index === self.findIndex((t) => t.forecastTime === item.forecastTime)
  )

  return uniqueResult
})

// ======= 그래프용 시리즈 공통 =======
const waveSeries = computed(() => {
  const list = Array.isArray(dangerData.value) ? dangerData.value : []
  if (!list.length) return { x: [], y: [] }

  const sliced = list.slice(0, 24)
  const x = sliced.map((_, idx) => `${idx}시`)
  const y = sliced.map(item => {
    const n = Number(item.waveHeight)
    return Number.isFinite(n) ? n : null
  })
  return { x, y }
})

const tempSeries = computed(() => {
  const list = Array.isArray(dangerData.value) ? dangerData.value : []
  if (!list.length) return { x: [], y: [] }

  const sliced = list.slice(0, 24)
  const x = sliced.map((_, idx) => `${idx}시`)
  const y = sliced.map(item => {
    const n = Number(item.seaSurfaceTemperature)
    return Number.isFinite(n) ? n : null
  })
  return { x, y }
})

// ======= 카드에 찍을 값: 그래프에서 그대로 가져오기 =======
// 지금은 "첫 번째 시점(0시)" 값을 사용. 필요하면 max/마지막 값으로 바꿀 수 있음.
const cardWaveHeight = computed(() => {
  if (!latestDanger.value) return null
  const n = Number(latestDanger.value.waveHeight)
  return Number.isFinite(n) ? n : null
})

const cardSeaSurfaceTemp = computed(() => {
  if (!latestDanger.value) return null
  const n = Number(latestDanger.value.seaSurfaceTemperature)
  return Number.isFinite(n) ? n : null
})


// ======= 차트 옵션 =======
const waveChartOption = computed(() => {
  const { x, y } = waveSeries.value

  if (!x.length) {
    return {
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value' },
      series: []
    }
  }

  return {
    tooltip: { trigger: 'axis' },
    grid: { top: 20, bottom: 30, left: 45, right: 10 },
    xAxis: {
      type: 'category',
      data: x,
      boundaryGap: false,
      axisLabel: { fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      axisLabel: { formatter: '{value} m' },
      min: 0
    },
    series: [
      {
        type: 'line',
        data: y,
        smooth: true,
        showSymbol: false
      }
    ]
  }
})

const seaSurfaceTemperatureChartOption = computed(() => {
  const { x, y } = tempSeries.value

  if (!x.length) {
    return {
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value' },
      series: []
    }
  }

  return {
    tooltip: { trigger: 'axis' },
    grid: { top: 20, bottom: 30, left: 45, right: 10 },
    xAxis: {
      type: 'category',
      data: x,
      boundaryGap: false,
      axisLabel: { fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      axisLabel: { formatter: '{value} °C' }
    },
    series: [
      {
        type: 'line',
        data: y,
        smooth: true,
        showSymbol: false
      }
    ]
  }
})

// ======= helpers =======
function getLocalDateString(date) {
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatTimeOnly(dateTimeString) {
  if (!dateTimeString) return '--'
  try {
    const date = new Date(dateTimeString.replace('T', ' '))
    if (isNaN(date)) return '--'
    return date.getHours()
  } catch (e) {
    return '--'
  }
}

function getWeatherStatus(item) {
  if (!item) return null
  if (item.rain > 0) return '비'
  if (item.temperature > 25) return '맑음'
  return '흐림'
}

function getWeatherIcon(item) {
  if (!item) return 'fas fa-question-circle'
  if (item.rain > 0) return 'fas fa-cloud-showers-heavy'
  if (item.temperature > 25) return 'fas fa-sun'
  return 'fas fa-cloud'
}
</script>



<style scoped>
.btn {
  transition: all 0.2s ease-in-out;
}

.tab-active {
  color: var(--bs-primary) !important;
  border-bottom: 2px solid var(--bs-primary) !important;
  position: relative;
}

.btn:not(.tab-active):hover {
  color: var(--bs-gray-700) !important;
}

.btn:focus {
  box-shadow: none !important;
}

.tab-segment-group {
  display: inline-flex;
  border-radius: 999px;
  border: 1px solid #dee2e6;
  overflow: hidden;
  background-color: #ffffff;
  font-size: 0.8rem;
  width: 100%;
}

.tab-segment {
  padding: 0.3rem 1.1rem;
  border: none;
  background: transparent;
  color: #6c757d;
  font-weight: 700;
  min-width: 70px;
  text-align: center;
  cursor: pointer;
}

.tab-segment + .tab-segment {
  border-left: 1px solid #dee2e6;
}

.tab-segment.active {
  background-color: var(--bs-primary);
  color: #40C4FF;
}

.risk-card > h6 {
  margin-bottom: 0.75rem;
}

.risk-card .row.text-center {
  margin-bottom: 0.75rem;
}

/* 차트가 너무 찌그러지지 않게 최소 높이 */
.risk-card .chart {
}
.tab-pane-block {
  min-height: 420px;      /* 둘 다 최소 이만큼은 차지하게 */
  display: flex;
  flex-direction: column;
}

/* 필요하면 전체 패널 높이도 살짝 보정 */
.p-3 {
  /* 너무 크면 숫자 줄여도 됨 */
  /* min-height: 480px; */
}
.risk-card .chart-sm {
  width: 100%;
  height: 150px; 
}


</style>
