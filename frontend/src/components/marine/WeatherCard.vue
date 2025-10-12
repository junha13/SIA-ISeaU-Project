<!---->
<template>
  <div class="card weather-card text-center p-3 shadow-sm">
    <h5>🌤 현재 해양 기상</h5>
    <p><strong>기온:</strong> {{ weather.temp }}°C</p>
    <p><strong>수온:</strong> {{ weather.waterTemp }}°C</p>
    <p><strong>풍속:</strong> {{ weather.wind }}m/s</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useMarineStore } from '@/stores/marineStore'

const marineStore = useMarineStore()
const weather = ref({ temp: 0, waterTemp: 0, wind: 0 })

onMounted(async () => {
  await marineStore.fetchWeather('beach01')
  weather.value = marineStore.currentWeather
})
</script>
