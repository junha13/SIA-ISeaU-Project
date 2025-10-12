<!---->
<template>
  <div class="alert" :class="tideAlert ? 'alert-danger' : 'alert-info'">
    <strong>{{ tideAlert ? '⚠️ 만조 경보' : '🌊 현재 안전 수위' }}</strong>
    <p>{{ tideMessage }}</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useMarineStore } from '@/stores/marineStore'

const marineStore = useMarineStore()
const tideAlert = ref(false)
const tideMessage = ref('')

onMounted(async () => {
  await marineStore.fetchTideData('beach01')
  tideAlert.value = marineStore.tideAlert
  tideMessage.value = tideAlert.value ? '만조 시 접근을 자제하세요.' : '현재 수위는 안전 수준입니다.'
})
</script>
