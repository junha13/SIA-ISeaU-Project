<template>
  <div class="p-3">
    <h6 class="fw-bold mb-2">CCTV</h6>
    <small class="text-muted">25.10.16 3시 기준</small>
    <div class="rounded bg-light border d-flex justify-content-center align-items-center" style="height:200px;">
      CCTV View (해수욕장: {{ detailData?.name }})
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';

defineProps({
  detailData: Object,
});
</script>