<template>
  <div class="container py-5">
    <h3 class="fw-bold text-success mb-4">🛟 해수욕장 안전 수칙</h3>

    <div v-for="(rule, i) in rules" :key="i" class="alert alert-info mb-2">
      <strong>{{ i + 1 }}.</strong> {{ rule }}
    </div>

    <button class="btn btn-outline-primary mt-4 fw-bold" @click="openConfirm">🏠 홈으로</button>

    <ConfirmModal
        v-if="showConfirm"
        title="이동 확인"
        message="홈으로 이동하시겠습니까?"
        @confirm="goHome"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import ConfirmModal from '@/components/common/ConfirmModal.vue'
import * as bootstrap from 'bootstrap'

const router = useRouter()
const showConfirm = ref(false)
const rules = [
  '수영 전 반드시 준비운동을 하세요.',
  '구명조끼를 착용하고 지정된 구역에서만 수영하세요.',
  '음주 후 수영은 절대 금지입니다.',
  '아이를 동반한 경우 반드시 보호자가 함께 하세요.',
]

const openConfirm = () => {
  showConfirm.value = true
  const modal = new bootstrap.Modal(document.getElementById('confirmModal'))
  modal.show()
}

const goHome = () => router.push('/')
</script>
