<template>
  <div v-if="isVisible" class="modal fade show d-block" tabindex="-1" style="background: rgba(0,0,0,.5)">
    <div class="modal-dialog modal-dialog-centered modal-sm">
      <div class="modal-content shadow">
        <div class="modal-header bg-primary text-white">
          <h5 class="modal-title">{{ title }}</h5>
          <button class="btn-close" @click="$emit('update:isVisible', false)"></button>
        </div>
        <div class="modal-body text-center">
          <p class="mb-0" style="white-space: pre-line;">{{ message }}</p>
        </div>
        <div class="modal-footer justify-content-center">
          <button class="btn btn-secondary" @click="$emit('update:isVisible', false)">취소</button>
          <button class="btn btn-primary" @click="$emit('confirm')">확인</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  isVisible: Boolean,
  title: { type: String, default: '확인' },
  message: { type: String, default: '진행하시겠습니까?' },
})
defineEmits(['update:isVisible','confirm'])
</script>
