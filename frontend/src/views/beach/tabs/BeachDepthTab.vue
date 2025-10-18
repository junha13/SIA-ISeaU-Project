<template>
  <div class="p-3">
    <h6 class="fw-bold mb-2">수심 지도</h6>
    <small class="text-muted">25.10.16 3시 기준</small>
    <div class="my-3">
      <div class="d-flex align-items-center mb-2">
        <div class="me-2 rounded-circle" style="width:15px;height:15px;background-color:#8482FF;"></div><span class="small">130cm 이하</span>
      </div>
      <div class="d-flex align-items-center mb-2">
        <div class="me-2 rounded-circle" style="width:15px;height:15px;background-color:#FFB354;"></div><span class="small">130cm~200cm</span>
      </div>
      <div class="d-flex align-items-center">
        <div class="me-2 rounded-circle" style="width:15px;height:15px;background-color:#EB725B;"></div><span class="small">200cm 이상</span>
      </div>
    </div>
    <div class="rounded bg-light border d-flex justify-content-center align-items-center" style="height:200px;">
      Depth Map View
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';

defineProps({
  detailData: Object,
});
</script>