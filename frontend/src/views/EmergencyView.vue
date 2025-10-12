<template>
  <div class="text-center p-4">
    <h4 class="fw-bold text-danger">🚨 긴급 신고</h4>
    <p class="text-muted">현재 위치를 기반으로 구조 요청을 전송합니다.</p>

    <button class="btn btn-danger w-75 fw-bold mt-3" @click="sendSOS">
      신고 전송
    </button>
  </div>
</template>

<script setup>
import { inject } from 'vue'
const openConfirm = inject('openConfirm')

const sendSOS = () => {
  openConfirm('신고 완료', '구조 요청이 전송되었습니다.\n잠시만 기다려 주세요.', null)
}
</script>
