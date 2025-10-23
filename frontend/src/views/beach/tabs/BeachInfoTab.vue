<template>
  <div class="p-3">
    <h6 class="fw-bold mb-3">소개</h6>
    <p class="text-black small">
      {{ beach.beachInformation }}
    </p>

    <h6 class="fw-bold mb-3">상세 주소</h6>
    <p class="text-black small">
      {{ beach.address }}
    </p>
    <div class="rounded shadow-sm overflow-hidden my-3" style="height:200px;">
      <div class="bg-light h-100 w-100 d-flex justify-content-center align-items-center text-muted">
        <div id="naver-map" style="width:100%;height:100%;"></div>
      </div>
    </div>

    <div class="mt-4">
      <h6 class="fw-bold">방문자 리뷰 (개)</h6>
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
import { defineProps, onMounted, watch } from 'vue';
import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'
const store = useStore();
const { header, beach } = storeToRefs(store)


let map, marker

// init: 네이버 지도 SDK가 로드된 뒤 1회 실행.
// Pinia의 beach 좌표로 지도 생성하고 마커 1개 표시합니다.
function init() {
  const center = new window.naver.maps.LatLng(beach.value.latitude, beach.value.longitude)
  map = new window.naver.maps.Map(document.getElementById('naver-map'), { center, zoom: 15 })
  marker = new window.naver.maps.Marker({ position: center, map })  // 맵 중심에 마커 생성
}

// onMounted: 컴포넌트가 DOM에 붙은 직후 1번 실행.
onMounted(() => {
  const s = document.createElement('script')
  s.src = `https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${import.meta.env.VITE_NAVER_CLIENT_ID}`
  s.onload = init
  document.head.appendChild(s)
})

// watch: Pinia의 beach.latitude/longitude 값이 변경될 때마다 실행. (새로고침, 다른라우트도 막았음)
watch(() => [beach.value.latitude, beach.value.longitude], ([lat, lng]) => {
  if (!map) return
  const pos = new window.naver.maps.LatLng(lat, lng)
  map.setCenter(pos)
  marker.setPosition(pos)
})
</script>