<!--수심 위험 비교 모달 (아이 키 vs 수심)-->
<template>
  <div v-if="visible" class="alert alert-danger alert-dismissible fade show" role="alert">
    ⚠️ <strong>{{ title }}</strong>
    <p>{{ message }}</p>
    <button type="button" class="btn-close" @click="visible = false"></button>
  </div>
</template>

<script setup>
import { ref, watch, defineProps } from 'vue'

const props = defineProps({
  userHeight: Number,
  currentDepth: Number,
})

const visible = ref(false)
const title = ref('수심 경고')
const message = ref('')

watch(() => props.currentDepth, (depth) => {
  if (depth > props.userHeight) {
    message.value = `현재 수심은 ${depth.toFixed(2)}m로, 아이 키(${props.userHeight}m)를 초과했습니다. 접근을 자제하세요.`
    visible.value = true
  } else {
    visible.value = false
  }
})
</script>
