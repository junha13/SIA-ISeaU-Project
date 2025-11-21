<template>
  <div v-if="weatherData && weatherData.length > 0" class="p-3">
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

    <!-- ================== ① 위험 정보 탭 ================== -->
    <div v-if="innerTab === 'risk'">
  <div class="card shadow-sm mb-3 p-3">
    <h6 class="fw-bold mb-1">위험 정보</h6>

    <div v-if="!latestDanger">
      <small class="text-muted">위험 정보를 불러오는 중...</small>
    </div>

    <div v-else class="row text-center small">
      <div class="col">
        <small>예측 시각</small>
        <div class="fw-bold">{{ latestDanger.forecastTime ?? '--' }}</div>
      </div>
      <div class="col">
        <small>유의 파고</small>
        <div class="fw-bold">{{ latestDanger.waveHeight ?? '--' }} m</div>
      </div>
      <div class="col">
        <small>수온</small>
        <div class="fw-bold">{{ latestDanger.seaSurfaceTemperature ?? '--' }} ℃</div>
      </div>
    </div>
  </div>
</div>


    <!-- ================== ② 실시간 날씨 탭 ================== -->
    <div v-else>
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

  <div v-else class="p-3 text-center text-muted">
    <small>날씨 정보를 불러오는 중...</small>
  </div>
</template>


<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import axios from 'axios'


// 🔹 관제 화면에서 넘겨주는 해수욕장 번호
const props = defineProps({
  beachNumber: {
    type: [String, Number],
    required: true,
  },
})

// 🔹 기상정보 안쪽 서브 탭: 'risk' | 'weather'
const innerTab = ref('risk')     // 기본은 '위험 정보' 탭

const weatherData = ref(null)    // 시간별/일별 날씨 리스트
const dangerData  = ref([])    // 파고·이안류·수온 등 위험 정보

// ================== API 호출 함수 ==================
const latestDanger = computed(() => {
  if (!Array.isArray(dangerData.value) || dangerData.value.length === 0) return null
  return dangerData.value[0]   // 필요하면 정렬해서 제일 최신 걸로 바꿀 수도 있음
})
const dayNames = ['일', '월', '화', '수', '목', '금', '토']

const toggleDays = computed(() => {
  if (!Array.isArray(weatherData.value) || weatherData.value.length === 0) {
    return []
  }

  // weatherData 에서 날짜만 뽑기 (YYYY-MM-DD)
  const uniqueDates = Array.from(
    new Set(
      weatherData.value
        .map(item => item.forecastTime?.substring(0, 10))
        .filter(Boolean)
    )
  ).sort()

  // 앞에서 3개만 사용 (오늘+내일+모레 느낌)
  return uniqueDates.slice(0, 3).map((dateStr, idx) => {
    const d = new Date(dateStr)
    const dow = dayNames[d.getDay()]

    let label = ''
    if (idx === 0) label = '오늘'
    else if (idx === 1) label = '내일'
    else label = '모레'

    return {
      dateStr,      // '2025-11-21'
      label,        // '오늘' / '내일' / '모레'
      dayOfWeek: dow, // '금' 같은 요일 한 글자
    }
  })
})


async function requestWeatherData(beachNumber) {
  try {
    const response = await axios.get(
      `${import.meta.env.VITE_API_BASE_URL}/api/beach/detail/${beachNumber}/weather`
    )

    // 백엔드에서 data.result 로 내려오고 있어서 그대로 사용
    weatherData.value = response.data.data.result

    if (weatherData.value && weatherData.value.length > 0) {
      const current = weatherData.value[0]
      console.log('[WeatherPanel] currentWeather:', current)
      console.log(' -> uvIndex:', current.uvIndex)
      console.log(' -> rainProbability:', current.rainProbability)
      console.log(' -> rain:', current.rain)

      // 기본 선택 날짜 = 내일
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

    // BeachController 에서 body(Map.of("data", result)) 로 내려보내니까
    const outer = response.data?.data

    // 서비스에서 result 키를 썼다면 그 안의 값 꺼내고,
    // 아니라면 data 자체를 그대로 리스트로 간주
    const list = outer?.result ?? outer

    // 항상 배열 형태로 맞춰두기
    dangerData.value = Array.isArray(list)
      ? list
      : (list ? [list] : [])

    console.log('[WeatherPanel] 파싱된 dangerData:', dangerData.value)
  } catch (e) {
    console.error('[WeatherPanel] 위험 정보 로딩 실패:', e)
    dangerData.value = []   // 에러나면 비워두기
  }
}


// 처음 마운트될 때 호출
onMounted(() => {
  if (props.beachNumber) {
    requestWeatherData(props.beachNumber)
    requestDangerData(props.beachNumber)
  }
})

// beachNumber가 바뀌면 다시 호출
watch(
  () => props.beachNumber,
  (val) => {
    if (val) {
      requestWeatherData(val)
      requestDangerData(val)
    }
  }
)

// =============== computed & helpers ================

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

// 4. 선택된 날짜의 시간별 예보 (3시간 간격)
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

</style>