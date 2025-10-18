<template>
  <div class="p-3">
    <h6 class="fw-bold mb-3">상세 정보</h6>
    <p class="text-secondary small">
      {{ detailData?.description || '상세 정보가 없습니다.' }}
    </p>

    <div class="rounded shadow-sm overflow-hidden my-3" style="height:200px;">
      <div class="bg-light h-100 w-100 d-flex justify-content-center align-items-center text-muted">
        지도 표시 영역 ({{ detailData?.name }})
      </div>
    </div>

    <div class="mt-4">
      <h6 class="fw-bold">방문자 리뷰 ({{ detailData?.reviews?.length || 0 }}개)</h6>
      <div class="border rounded p-3 mt-2">
        <textarea class="form-control mb-2" placeholder="리뷰를 작성해주세요..." rows="2"></textarea>
        <div class="d-flex justify-content-between">
          <button class="btn btn-outline-secondary btn-sm">사진 첨부</button>
          <button class="btn btn-sm text-white" style="background-color:#0092BA;">등록</button>
        </div>
      </div>

      <div class="mt-3">
        <div v-for="(review, index) in detailData?.reviews" :key="index" class="border rounded p-3 mb-2">
          <div class="fw-bold">{{ review.user }} <small class="text-muted ms-2">{{ review.date }}</small></div>
          <p class="mb-1 small text-secondary">{{ review.content }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';

defineProps({
  detailData: Object,
});
</script>