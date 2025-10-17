<template>
  <div class="p-3">
    <div class="card shadow-sm mb-3 p-3">
      <h6 class="fw-bold mb-1">실시간 날씨</h6>
      <div class="d-flex align-items-center justify-content-between">
        <div>
          <div class="fs-3 fw-bold">24°C</div>
          <small class="text-muted">맑음</small>
        </div>
        <div class="text-end text-secondary small">
          <div>체감온도 26°C</div>
          <div>풍속 3.2m/s</div>
        </div>
      </div>
    </div>

    <div class="card shadow-sm p-3 mb-3">
      <h6 class="fw-bold mb-2">시간별 날씨</h6>
      <div class="d-flex justify-content-around text-center">
        <div v-for="h in [12,13,14,15,16,17]" :key="h">
          <div><i class="bi bi-brightness-high-fill fs-4"></i></div>
          <div>{{ h }}시</div>
          <div class="fw-bold">25°C</div>
        </div>
      </div>
    </div>

    <div class="card shadow-sm p-3">
      <h6 class="fw-bold mb-2">상세 정보</h6>
      <div class="row text-center">
        <div class="col"><small>풍속</small><div>3.2m/s</div></div>
        <div class="col"><small>가시거리</small><div>10km</div></div>
        <div class="col"><small>습도</small><div>65%</div></div>
        <div class="col"><small>조수간만</small><div>1.2m</div></div>
      </div>
    </div>
  </div>
</template>
