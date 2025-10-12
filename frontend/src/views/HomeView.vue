<template>
  <div class="container-fluid py-3">
    <SafetyMap @risk-clicked="onRiskClick" />
    <div class="mt-3 text-center">
      <button class="btn btn-outline-primary w-75" @click="goEmergency">
        🚨 긴급신고 바로가기
      </button>
    </div>
  </div>
</template>

<script setup>
import { inject } from 'vue'
import SafetyMap from '@/components/map/SafetyMap.vue'
import { useRouter } from 'vue-router'

const showDepthAlert = inject('showDepthAlert')
const openConfirm = inject('openConfirm')
const router = useRouter()

const onRiskClick = (zone) => {
  showDepthAlert(zone.depth, 1.3) // 예시: 사용자의 키 1.3m
}

const goEmergency = () => {
  openConfirm('긴급신고', '정말 긴급신고 화면으로 이동하시겠습니까?', () => {
    router.push('/emergency')
  })
}
</script>
