<template>
  <div class="text-center p-4">
    <h4 class="fw-bold text-primary">👥 그룹 위치 공유</h4>
    <p>가족이나 친구와 위치를 공유해 보세요.</p>
    <button class="btn btn-outline-primary w-75" @click="joinGroup">그룹 참여</button>
  </div>
</template>

<script setup>
import { inject } from 'vue'
const openConfirm = inject('openConfirm')

const joinGroup = () => {
  openConfirm('위치공유 시작', '위치 정보를 실시간으로 공유하시겠습니까?', () => {
    console.log('✅ 위치 공유 시작됨')
  })
}
</script>
