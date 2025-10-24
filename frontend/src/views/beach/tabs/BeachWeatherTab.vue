<template>

  <div v-if="weatherData && weatherData.length > 0" class="p-3">

    <div class="card shadow-sm mb-3 p-3">

      <h6 class="fw-bold mb-1">실시간 날씨</h6>

      <div class="d-flex align-items-center justify-content-between">

        <div>

          <div class="fs-3 fw-bold">{{ weatherData[0]?.temperature || '--' }}°C</div>

          <small class="text-muted">{{ weatherData[0]?.status || '정보 없음' }}</small>

        </div>

        <div class="text-end text-secondary small">

          <div>체감온도 {{ (weatherData[0]?.temperature + 2) || '--' }}°C</div>

          <div>풍속 {{ weatherData[0]?.windSpeed || '--' }}m/s</div>

        </div>

      </div>

    </div>



    <div class="card shadow-sm p-3 mb-3">

      <h6 class="fw-bold mb-2">시간별 날씨</h6>

      <div class="d-flex justify-content-around text-center">

        <div v-for="item in weatherData.slice(0, 6)" :key="item.forecastTime">

          <div><i class="fas fa-sun fs-4" :style="{ color: '#FFB354' }"></i></div>

          <div>{{ formatTime(item.forecastTime) }}시</div>

          <div class="fw-bold">{{ item.temperature || '--' }}°C</div>

        </div>

      </div>

    </div>



    <div class="card shadow-sm p-3">

      <h6 class="fw-bold mb-2">상세 정보</h6>

      <div class="row text-center">

        <div class="col"><small>풍속</small><div>{{ weatherData[0]?.windSpeed || '--' }}m/s</div></div>

        <div class="col"><small>가시거리</small><div>{{ weatherData[0]?.visibility || '--' }}km</div></div>

        <div class="col"><small>습도</small><div>{{ weatherData[0]?.humidity || '--' }}%</div></div>

        <div class="col"><small>조수간만</small><div>{{ weatherData[0]?.tide || '--' }}m</div></div>

      </div>

    </div>

  </div>

 

  <div v-else class="p-3 text-center text-muted">

    <small>날씨 정보를 불러오는 중...</small>

  </div>

</template>



<script setup>
import { ref, onMounted, defineProps } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';

const props = defineProps({
  beach: Object, 
});

const route = useRoute();
const weatherData = ref(null);

onMounted(() => {
  const beachNumber = route.params.beachNumber;
  
  // [로그 1] 탭이 마운트될 때 beachNumber를 제대로 가져오는지 확인
  console.log('WeatherTab onMounted: beachNumber =', beachNumber);

  if (beachNumber) {
    requestWeatherData(beachNumber);
  }
});

async function requestWeatherData(beachNumber) {
  try {
    // [로그 2] API가 올바른 URL로 요청되는지 확인
    console.log(`API 요청 시작: http://localhost:8080/api/beach/detail/${beachNumber}/weather`);
    
    const response = await axios.get(
      `http://localhost:8080/api/beach/detail/${beachNumber}/weather` 
    );

    // [로그 3] (가장 중요!) 서버가 실제로 어떤 데이터를 보냈는지 확인
    console.log('서버 응답 (response.data):', response.data);
    console.log('할당될 데이터 (response.data.result):', response.data.data.result);

   weatherData.value = response.data.data.result;

    // [로그 4] ref에 값이 제대로 저장되었는지 확인
    console.log('weatherData ref에 저장된 값:', weatherData.value);

  } catch (e) {
    console.error('[WeatherTab] 날씨 정보 로딩 실패:', e);
  }
}

function formatTime(dateTimeString) {
  if (!dateTimeString) return '--';
  try {
    return new Date(dateTimeString).getHours();
  } catch (e) {
    return '--';
  }
}
</script>