<template>
  <div class="p-3">
    <h6 class="fw-bold mb-3">상세 정보</h6>
    <p class="text-secondary small">
      속초해수욕장은 강원도 속초시에 위치한 대표적인 해수욕장으로 맑고 깨끗한 바다와 부드러운 모래사장으로 유명합니다.
    </p>

    <!-- 지도 -->
    <div class="rounded shadow-sm overflow-hidden my-3" style="height:200px;">
      <div class="bg-light h-100 d-flex justify-content-center align-items-center text-muted">
        지도 표시 영역
      </div>
    </div>

    <!-- 리뷰 작성 -->
    <div class="mt-4">
      <h6 class="fw-bold">방문자 리뷰</h6>
      <div class="border rounded p-3 mt-2">
        <textarea class="form-control mb-2" placeholder="리뷰를 작성해주세요..." rows="2"></textarea>
        <div class="d-flex justify-content-between">
          <button class="btn btn-outline-secondary btn-sm">사진 첨부</button>
          <button class="btn btn-sm text-white" style="background-color:#0092BA;">등록</button>
        </div>
      </div>

      <!-- 리뷰 목록 -->
      <div class="mt-3">
        <div class="border rounded p-3 mb-2">
          <div class="fw-bold">익명사용자 <small class="text-muted ms-2">2025.01.15</small></div>
          <p class="mb-1 small text-secondary">정말 깨끗하고 아름다운 해수욕장이에요. 가족과 함께 즐거운 시간을 보냈습니다.</p>
        </div>
      </div>
    </div>
  </div>
</template>
