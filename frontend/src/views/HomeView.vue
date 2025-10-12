<template>
  <div class="container-fluid py-3">
    <!-- 상단 타이틀 -->
    <div class="text-center mb-4">
      <h3 class="fw-bold text-primary mb-1">🌊 실시간 해양 안전 관제</h3>
      <p class="text-muted">AI 기반 익수자 감지 및 수심 위험 경보</p>
    </div>

    <!-- 에러/로딩 -->
    <div v-if="pageLoading" class="d-flex flex-column align-items-center py-5">
      <div class="spinner-border text-primary mb-2"></div>
      <div class="text-muted">데이터 불러오는 중…</div>
    </div>
    <div v-else-if="pageError" class="alert alert-danger">
      {{ pageError }}
    </div>

    <!-- 본문 -->
    <div v-else class="row g-3">
      <div class="col-12 col-lg-8">
        <div class="card shadow-sm border-0">
          <div class="card-body p-0" style="min-height:60vh;">
            <SafetyMap />
          </div>
        </div>
      </div>

      <div class="col-12 col-lg-4 d-grid gap-3">
        <WeatherCard :weather="marineStore.weather" />
        <TideWarning :tide="marineStore.tide" title="조수 정보" />
        <MonitoringPanel />
      </div>
    </div>

    <!-- 하단 네비 버튼 -->
    <div class="d-flex justify-content-center gap-3 flex-wrap mt-4">
      <button class="btn btn-outline-danger fw-bold px-4 py-2" @click="go('/emergency')">🚨 긴급 신고</button>
      <button class="btn btn-outline-primary fw-bold px-4 py-2" @click="go('/group')">👥 위치 공유</button>
      <button class="btn btn-outline-success fw-bold px-4 py-2" @click="go('/safety-info')">🛟 안전 수칙</button>
    </div>

    <ConfirmModal
        :isVisible="confirmVisible"
        title="이동 확인"
        :message="confirmMsg"
        @update:isVisible="confirmVisible = $event"
        @confirm="confirmGo"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { useSafetyStore } from '@/stores/safetyStore'
import { useMapStore } from '@/stores/mapStore'
import { useMarineStore } from '@/stores/marineStore' // 없으면 생성 권장
import { useGeolocation } from '@/composables/useGeolocation'

import SafetyMap from '@/components/map/SafetyMap.vue'
import WeatherCard from '@/components/marine/WeatherCard.vue'
import TideWarning from '@/components/marine/TideWarning.vue'
import MonitoringPanel from '@/components/cctv/MonitoringPanel.vue'
import ConfirmModal from '@/components/common/ConfirmModal.vue'

const router = useRouter()
const userStore = useUserStore()
const safetyStore = useSafetyStore()
const mapStore = useMapStore()
const marineStore = useMarineStore()

const { position, error, getLocation } = useGeolocation()

const pageLoading = ref(true)
const pageError = ref('')
const confirmVisible = ref(false)
const confirmMsg = ref('선택한 화면으로 이동하시겠습니까?')
let nextPath = '/'

const go = (path) => { nextPath = path; confirmVisible.value = true }
const confirmGo = () => { confirmVisible.value = false; router.push(nextPath) }

onMounted(async () => {
  try {
    await getLocation()
    if (error.value) throw new Error(error.value)

    const { lat, lng } = position.value
    await userStore.updateLocation(lat, lng)

    // 지도/위험/해양 데이터 병렬 로드
    await Promise.all([
      mapStore.fetchRiskZones('beach-01'),
      mapStore.fetchDepthMap('beach-01'),
      safetyStore.fetchRealtimeWarning(lat, lng),
      marineStore.fetchWeather('beach-01'),
      marineStore.fetchTide('beach-01'),
    ])
  } catch (e) {
    pageError.value = e?.message || '초기 데이터 로드 실패'
  } finally {
    pageLoading.value = false
  }
})
</script>
