<template>
  <div v-if="weatherData && weatherData.length > 0" class="p-3">

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

    <div class="card shadow-sm p-3 mb-3">
      <h6 class="fw-bold mb-2">상세 정보</h6>
      <div class="row text-center">
        <div class="col"><small>풍속</small><div>{{ currentWeather?.windSpeed ?? '--' }}m/s</div></div>
        <div class="col"><small>습도</small><div>{{ currentWeather?.humidity ?? '--' }}%</div></div>
        <div class="col"><small>자외선지수</small><div>{{ currentWeather?.uvIndex ?? '--' }}</div></div>
        <div class="col"><small>강수확률</small><div>{{ currentWeather?.rainProbability ?? '--' }}%</div></div>
      </div>
    </div>

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

      <div v-if="selectedDayHourlyForecast.length > 0" class="d-flex justify-content-around text-center overflow-x-auto">
        <div v-for="item in selectedDayHourlyForecast" :key="item.forecastTime" class="p-2" style="min-width: 60px;">
          <div><i :class="getWeatherIcon(item)" class="fs-4" :style="{ color: '#FFB354' }"></i></div>
          <div>{{ formatTimeOnly(item.forecastTime) }}시</div>
          <div class="fw-bold">{{ item.temperature ?? '--' }}°C</div>
        </div>
      </div>

      <div v-else class="text-center text-muted small py-2">
        {{ toggleDays.length > 0 ? '해당 날짜의 예보 정보가 없습니다.' : '다음 날 예보 정보가 없습니다.' }}
      </div>

<!--    <div v-else class="p-3 text-center text-muted">-->
<!--      <small>날씨 정보를 불러오는 중...</small>-->
<!--    </div>-->

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useBeachStore } from '@/stores/beachStore';
import { storeToRefs } from 'pinia';
import { useRoute } from 'vue-router';
import axios from 'axios';

// API_BASE_URL 변수가 사용되지 않아 제거됨

const route = useRoute();
//const weatherData = ref(null); // 백엔드 raw 리스트


const beachStore = useBeachStore();
const { weatherData, isWeatherLoading } = storeToRefs(beachStore);
// --- API 호출 ---
onMounted(() => {
  const beachNumber = route.params.beachNumber;
    if (beachNumber) {
      requestWeatherData(beachNumber);
    }
});

async function requestWeatherData(beachNumber) {
   try {
     // API_ENDPOINT 대신 URL 하드코딩 유지
     const response = await axios.get(
      `http://172.168.10.15:8080/api/beach/detail/${beachNumber}/weather`
    );

    // 실제 컨트롤러 응답 구조에 맞게 경로 수정
    weatherData.value = response.data.data.result;
   
    // --- [START] 요청하신 콘솔 로그 추가 ---
    if (weatherData.value && weatherData.value.length > 0) {
      const current = weatherData.value[0]; // currentWeather에 해당
      console.log('[WeatherTab] 첫 번째 날씨 데이터 (currentWeather):', current);
      console.log(' -> 자외선 (uvIndex):', current.uvIndex);
      console.log(' -> 강수확률 (rainProbability):', current.rainProbability);
      console.log(' -> (참고) 강수량 (rain):', current.rain);
    } else {
      console.log('[WeatherTab] 날씨 데이터가 비어있습니다.');
    }
    // --- [END] 콘솔 로그 추가 ---

    // 데이터 로딩 후 selectedDay를 내일 날짜로 초기화
    if (weatherData.value && weatherData.value.length > 0) {
        // 내일 날짜를 계산하여 기본값으로 설정
        selectedDay.value = getLocalDateString(new Date(Date.now() + 24 * 60 * 60 * 1000));
    }

  } catch (e) {
    console.error('[WeatherTab] 날씨 정보 로딩 실패:', e);
    weatherData.value = [];
  }
}

// --- Reusable Date Strings (Computed로 변경하여 동적으로 처리) ---
const tomorrowDateStr = computed(() => getLocalDateString(new Date(Date.now() + 24 * 60 * 60 * 1000)));
const dayAfterTomorrowDateStr = computed(() => getLocalDateString(new Date(Date.now() + 2 * 24 * 60 * 60 * 1000)));

// --- State for Toggles ---
const selectedDay = ref(null); // 초기값은 null로 설정, 데이터 로드 후 내일 날짜로 설정됨

function selectDay(dateStr) {
    selectedDay.value = dateStr;
}

// --- Computed 속성 (데이터 가공) ---

// 1. 현재 날씨 (리스트 첫 항목)
const currentWeather = computed(() => {
  if (!Array.isArray(weatherData.value) || weatherData.value.length === 0) return null;
  return weatherData.value[0];
});

// 2. 시간별 날씨 슬라이스 (앞 6개)
const hourlyForecastSlice = computed(() => {
  if (!Array.isArray(weatherData.value) || weatherData.value.length === 0) return [];
  return weatherData.value.slice(0, 6);
});

// 3. (구) 일별 요약: min/max 계산 로직은 유지 (숨겨진 데이터로 사용)
const dailySummaries = computed(() => {
  if (!Array.isArray(weatherData.value) || weatherData.value.length === 0) {
    return [];
  }
  const todayStr = getLocalDateString(new Date()); 
  const tomorrowStr = tomorrowDateStr.value; 
  const dayAfterTomorrowStr = dayAfterTomorrowDateStr.value;

  const dailyData = {}; 

  weatherData.value.forEach(item => {
    const itemDateStr = item.forecastTime.substring(0, 10); 
    if (itemDateStr === todayStr) return;
    if (itemDateStr !== tomorrowStr && itemDateStr !== dayAfterTomorrowStr) return;

    if (!dailyData[itemDateStr]) {
      dailyData[itemDateStr] = { dateStr: itemDateStr, temps: [], items: [] };
    }
    dailyData[itemDateStr].temps.push(item.temperature);
    dailyData[itemDateStr].items.push(item); 
  });

  const summaries = [];
  if (dailyData[tomorrowStr]) summaries.push(processDailyData(dailyData[tomorrowStr]));
  if (dailyData[dayAfterTomorrowStr]) summaries.push(processDailyData(dailyData[dayAfterTomorrowStr]));

  return summaries;
});

// 4. [NEW] 선택된 날짜의 시간별 예보
const selectedDayHourlyForecast = computed(() => {
    if (!Array.isArray(weatherData.value) || weatherData.value.length === 0 || !selectedDay.value) {
        return [];
    }
    const selectedDate = selectedDay.value;
    
    const filteredByDay = weatherData.value
        .filter(item => item.forecastTime && item.forecastTime.substring(0, 10) === selectedDate)
        .sort((a, b) => new Date(a.forecastTime) - new Date(b.forecastTime));
        
    const result = [];
    
    // 3시간 단위로 필터링
    for(let i = 0; i < filteredByDay.length; i++) {
        const hour = formatTimeOnly(filteredByDay[i].forecastTime);
        // 시간 값이 0, 3, 6, 9, 12, 15, 18, 21인 경우만 포함
        if (hour % 3 === 0) {
            result.push(filteredByDay[i]);
        }
    }
    
    // --- [수정] ----------------------------------
    // API가 보내준 중복 데이터를 forecastTime 기준으로 제거합니다.
    const uniqueResult = result.filter((item, index, self) =>
        index === self.findIndex(t => t.forecastTime === item.forecastTime)
    );
    // ---------------------------------------------

    return uniqueResult; // [수정] result -> uniqueResult
});

// 5. [NEW] 토글 버튼에 표시할 날짜/요일 정보
const toggleDays = computed(() => {
    if (!Array.isArray(weatherData.value) || weatherData.value.length === 0) return [];

    const tomorrow = tomorrowDateStr.value;
    const dayAfterTomorrow = dayAfterTomorrowDateStr.value;
    
    const days = [];
    
    // 내일 데이터가 있으면 추가
    if (weatherData.value.some(item => item.forecastTime.startsWith(tomorrow))) {
        days.push({
            label: '내일',
            dateStr: tomorrow,
            dayOfWeek: getDayOfWeekKorean(new Date(tomorrow + 'T00:00:00')) 
        });
    }

    // 모레 데이터가 있으면 추가
    if (weatherData.value.some(item => item.forecastTime.startsWith(dayAfterTomorrow))) {
        days.push({
            label: '모레',
            dateStr: dayAfterTomorrow,
            dayOfWeek: getDayOfWeekKorean(new Date(dayAfterTomorrow + 'T00:00:00'))
        });
    }
    
    return days;
});


// --- Helper 함수 ---

// 날짜 문자열 (YYYY-MM-DD) 생성
function getLocalDateString(date) {
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date.getDate().toString().padStart(2, '0');
  return `${year}-${month}-${day}`;
}

// 일별 데이터 처리 함수 (dailySummaries 내부에서 사용)
function processDailyData(dayData) {
    const jsDate = new Date(dayData.dateStr + 'T00:00:00'); // 날짜 객체 생성 (요일 계산용)
    return {
      date: dayData.dateStr,
      dayOfWeek: getDayOfWeekKorean(jsDate),
      minTemp: Math.min(...dayData.temps),
      maxTemp: Math.max(...dayData.temps),
      icon: getRepresentativeIcon(dayData.items) // 대표 아이콘
    };
}


// 시간만 추출 (HH)
function formatTimeOnly(dateTimeString) {
  if (!dateTimeString) return '--';
  try {
    // API 응답 포맷이 ISO 8601 (T로 구분)이므로 안전하게 파싱
    const date = new Date(dateTimeString.replace('T', ' ')); 
    if (isNaN(date)) return '--';
    return date.getHours();
  } catch (e) { return '--'; }
}

// 날씨 상태 문자열 (임시)
function getWeatherStatus(item) {
  if (!item) return null;
  if (item.rain > 0) return "비";
  if (item.temperature > 25) return "맑음";
  return "흐림";
}

// 날씨 아이콘 클래스 (임시)
function getWeatherIcon(item) {
  if (!item) return "fas fa-question-circle";
  if (item.rain > 0) return "fas fa-cloud-showers-heavy";
  if (item.temperature > 25) return "fas fa-sun";
  return "fas fa-cloud";
}

// 일별 대표 아이콘 (임시)
function getRepresentativeIcon(items) {
  if (!items || items.length === 0) return "fas fa-question-circle";
  const iconCounts = {};
  let maxCount = 0;
  let representativeIcon = "fas fa-cloud";
  items.forEach(item => {
    const icon = getWeatherIcon(item);
    iconCounts[icon] = (iconCounts[icon] || 0) + 1;
    if (iconCounts[icon] > maxCount) {
      maxCount = iconCounts[icon];
      representativeIcon = icon;
    }
  });
  return representativeIcon;
}

// 요일 한글 변환
function getDayOfWeekKorean(date) {
  const days = ['일', '월', '화', '수', '목', '금', '토'];
  return days[date.getDay()];
}
</script>
<style scoped>
/* 트렌디한 탭 버튼 스타일 */
.btn {
    transition: all 0.2s ease-in-out;
}

.tab-active {
    color: var(--bs-primary) !important; /* 활성 텍스트 색상을 Bootstrap primary 색상으로 설정 */
    border-bottom: 2px solid var(--bs-primary) !important; /* 활성 탭 밑줄 강조 */
    position: relative;
}

/* 탭 버튼 Hover 효과 */
.btn:not(.tab-active):hover {
    color: var(--bs-gray-700) !important;
}

/* 탭 버튼의 포커스(클릭) 시 파란색 아웃라인 제거 */
.btn:focus {
    box-shadow: none !important;
}
</style>