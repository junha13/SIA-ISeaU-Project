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

    <div class="card shadow-sm p-3 mb-3"> <h6 class="fw-bold mb-2">상세 정보</h6>
      <div class="row text-center">
        <div class="col"><small>풍속</small><div>{{ currentWeather?.windSpeed || '--' }}m/s</div></div>
        <div class="col"><small>습도</small><div>{{ currentWeather?.humidity || '--' }}%</div></div>
        <div class="col"><small>가시거리</small><div>{{ currentWeather?.visibility || '--' }}km</div></div>
        <div class="col"><small>조수간만</small><div>{{ currentWeather?.tide || '--' }}m</div></div>
      </div>
    </div>

    <div class="card shadow-sm p-3"> <h6 class="fw-bold mb-2">일별 예보</h6>
      
      <div v-if="dailySummaries.length > 0" class="d-flex justify-content-around text-center">
        
        <div v-for="item in dailySummaries" :key="item.date" class="py-2">
          <div class="fw-bold">{{ item.dayOfWeek }}</div>
          <div class="my-1"><i :class="item.icon" class="fs-3 text-warning"></i></div>
          <div class="small">
            <span class="text-muted">{{ Math.round(item.minTemp) }}°</span>
            <span> / </span>
            <span class="fw-bold">{{ Math.round(item.maxTemp) }}°</span>
          </div>
        </div>
      </div>
      
      <div v-else class="text-center text-muted small py-2">
        다음 날 예보 정보가 없습니다.
      </div>
    </div>

  </div>
  
  <div v-else class="p-3 text-center text-muted">
    <small>날씨 정보를 불러오는 중...</small>
  </div>
</template>
<script setup>
import { ref, onMounted, computed } from 'vue'; // defineProps 제거 가능
import { useRoute } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const weatherData = ref(null); // 백엔드 raw 리스트

// --- API 호출 ---
onMounted(() => {
  const beachNumber = route.params.beachNumber;
  console.log('WeatherTab onMounted: beachNumber =', beachNumber);
  if (beachNumber) {
    requestWeatherData(beachNumber);
  }
});
async function requestWeatherData(beachNumber) {
  try {
    console.log(`API 요청 시작: http://localhost:8080/api/beach/detail/${beachNumber}/weather`);
    const response = await axios.get(
      `http://localhost:8080/api/beach/detail/${beachNumber}/weather`
    );

    console.log('서버 응답 (response.data):', response.data.data.result);
    // [확인!] 실제 컨트롤러 응답 구조에 맞게 경로 수정
    weatherData.value = response.data.data.result;
    console.log('weatherData ref에 저장된 값:', weatherData.value);
  } catch (e) {
    console.error('[WeatherTab] 날씨 정보 로딩 실패:', e);
    weatherData.value = [];
  }
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

// 3. ⬇️ [수정/추가] 일별 요약 (내일, 모레만) ⬇️
const dailySummaries = computed(() => {
  if (!Array.isArray(weatherData.value) || weatherData.value.length === 0) {
    return [];
  }
  // JS Date 객체는 불안정할 수 있으므로, 날짜 문자열(YYYY-MM-DD)로 비교
  const todayStr = getLocalDateString(new Date()); // 오늘 날짜 문자열
  const tomorrowStr = getLocalDateString(new Date(Date.now() + 24 * 60 * 60 * 1000)); // 내일
  const dayAfterTomorrowStr = getLocalDateString(new Date(Date.now() + 2 * 24 * 60 * 60 * 1000)); // 모레

  const dailyData = {}; // 날짜별 그룹

  weatherData.value.forEach(item => {
    // forecastTime (예: "2025-10-24T15:00:00")에서 날짜 부분만 추출
    const itemDateStr = item.forecastTime.substring(0, 10); // "YYYY-MM-DD"

    // 오늘 데이터는 건너뜀
    if (itemDateStr === todayStr) return;
    // 내일과 모레 데이터만 처리
    if (itemDateStr !== tomorrowStr && itemDateStr !== dayAfterTomorrowStr) return;

    if (!dailyData[itemDateStr]) {
      dailyData[itemDateStr] = {
        dateStr: itemDateStr, // YYYY-MM-DD
        temps: [],
        items: []
      };
    }
    dailyData[itemDateStr].temps.push(item.temperature); // 온도 추가
    dailyData[itemDateStr].items.push(item); // 아이콘 결정을 위해 원본 저장
  });

  // 객체를 배열로 변환하고 가공 (내일 -> 모레 순서)
  const summaries = [];
  if (dailyData[tomorrowStr]) summaries.push(processDailyData(dailyData[tomorrowStr]));
  if (dailyData[dayAfterTomorrowStr]) summaries.push(processDailyData(dailyData[dayAfterTomorrowStr]));

  return summaries;
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
    const date = new Date(dateTimeString.replace('T', ' '));
    if (isNaN(date)) return '--';
    return date.getHours();
  } catch (e) { return '--'; }
}

// 날씨 상태 문자열 (임시)
function getWeatherStatus(item) {
  // ... (이전 코드와 동일, weatherCode 필요) ...
  if (!item) return null;
  if (item.rain > 0) return "비";
  if (item.temperature > 25) return "맑음";
  return "흐림";
}

// 날씨 아이콘 클래스 (임시)
function getWeatherIcon(item) {
  // ... (이전 코드와 동일, weatherCode 필요) ...
   if (!item) return "fas fa-question-circle";
   if (item.rain > 0) return "fas fa-cloud-showers-heavy";
   if (item.temperature > 25) return "fas fa-sun";
   return "fas fa-cloud";
}

// 일별 대표 아이콘 (임시)
function getRepresentativeIcon(items) {
  // ... (이전 코드와 동일, weatherCode 필요) ...
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