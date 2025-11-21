<template>
  <div v-if="weatherData && weatherData.length > 0" class="p-3">

    <!-- 1. 실시간 날씨 -->
    <div class="card shadow-sm mb-3 p-3">
      <h6 class="fw-bold mb-1">실시간 날씨</h6>
      <div class="d-flex align-items-center justify-content-between">
        <div>
          <div class="fs-3 fw-bold">{{ currentWeather?.temperature || '--' }}°C</div>
          <small class="text-muted">{{ getWeatherStatus(currentWeather) || '정보 없음' }}</small>
        </div>
        <div class="text-end text-secondary small">
          <!-- TODO: 체감온도 계산 로직은 백엔드에서 제공하는 것이 정확합니다. 임시 값입니다. -->
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
          <div><i :class="getWeatherIcon(item)" class="fs-4" :style="{ color: '#FFB354' }"></i></div>
          <div>{{ formatTimeOnly(item.forecastTime) }}시</div>
          <div class="fw-bold">{{ item.temperature ?? '--' }}°C</div>
        </div>
      </div>
      <div v-else class="text-center text-muted small py-2">
        시간별 예보 정보가 없습니다.
      </div>
    </div>

    <!-- 3. 상세 정보 (위치 변경됨) -->
    <div class="card shadow-sm p-3 mb-3"> 
      <h6 class="fw-bold mb-2">상세 정보</h6>
      <div class="row text-center">
                <div class="col"><small>풍속</small><div>{{ currentWeather?.windSpeed ?? '--' }}m/s</div></div>
        <div class="col"><small>습도</small><div>{{ currentWeather?.humidity ?? '--' }}%</div></div>
        <div class="col"><small>자외선지수</small><div>{{ currentWeather?.uvIndex ?? '--' }}</div></div>
        <div class="col"><small>강수확률</small><div>{{ currentWeather?.rainProbability ?? '--' }}%</div></div>
      </div>
    </div>
  </div>
  
  <div v-else class="p-3 text-center text-muted">
    <small>날씨 정보를 불러오는 중...</small>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import axios from 'axios'

// 🔹 관제 화면(또는 다른 화면)에서 넘겨줄 해수욕장 번호
const props = defineProps({
  beachNumber: {
    type: [String, Number],
    required: true,
  },
})

const weatherData = ref(null)

// API 호출
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
      // 기본 선택 날짜 = 내일
      selectedDay.value = getLocalDateString(new Date(Date.now() + 24 * 60 * 60 * 1000))
    } else {
      console.log('[WeatherPanel] 날씨 데이터가 비어있습니다.')
    }
  } catch (e) {
    console.error('[WeatherPanel] 날씨 정보 로딩 실패:', e)
    weatherData.value = []
  }
}

// 처음 마운트될 때 호출
onMounted(() => {
  if (props.beachNumber) {
    requestWeatherData(props.beachNumber)
  }
})

// beachNumber가 바뀌면 다시 호출
watch(
  () => props.beachNumber,
  (val) => {
    if (val) requestWeatherData(val)
  }
)

// =============== computed & helpers ================

// 내일 / 모레 날짜 문자열
const tomorrowDateStr = computed(() =>
  getLocalDateString(new Date(Date.now() + 24 * 60 * 60 * 1000))
)
const dayAfterTomorrowDateStr = computed(() =>
  getLocalDateString(new Date(Date.now() + 2 * 24 * 60 * 60 * 1000))
)

const selectedDay = ref(null)

function selectDay(dateStr) {
  selectedDay.value = dateStr
}

// 1. 현재 날씨
const currentWeather = computed(() => {
  if (!Array.isArray(weatherData.value) || weatherData.value.length === 0) return null
  return weatherData.value[0]
})

// 2. 시간별 날씨 (앞 6개)
const hourlyForecastSlice = computed(() => {
  if (!Array.isArray(weatherData.value) || weatherData.value.length === 0) return []
  return weatherData.value.slice(0, 6)
})

// 4. [NEW] 선택된 날짜의 시간별 예보 (3시간 간격)
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

  // forecastTime 기준 중복 제거
  const uniqueResult = result.filter(
    (item, index, self) => index === self.findIndex((t) => t.forecastTime === item.forecastTime)
  )

  return uniqueResult
})

// 5. 토글에 보여줄 날짜들 (내일/모레)
const toggleDays = computed(() => {
  if (!Array.isArray(weatherData.value) || weatherData.value.length === 0) return []

  const tomorrow = tomorrowDateStr.value
  const dayAfterTomorrow = dayAfterTomorrowDateStr.value

  const days = []

  if (weatherData.value.some((item) => item.forecastTime.startsWith(tomorrow))) {
    days.push({
      label: '내일',
      dateStr: tomorrow,
      dayOfWeek: getDayOfWeekKorean(new Date(tomorrow + 'T00:00:00')),
    })
  }

  if (weatherData.value.some((item) => item.forecastTime.startsWith(dayAfterTomorrow))) {
    days.push({
      label: '모레',
      dateStr: dayAfterTomorrow,
      dayOfWeek: getDayOfWeekKorean(new Date(dayAfterTomorrow + 'T00:00:00')),
    })
  }

  return days
})

// ================= Helpers =================

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

function getDayOfWeekKorean(date) {
  const days = ['일', '월', '화', '수', '목', '금', '토']
  return days[date.getDay()]
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
</style>