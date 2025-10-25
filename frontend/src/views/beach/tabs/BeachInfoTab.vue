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
       <div ref="mapEl" style="width:100%;height:200px;"></div>
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
import { defineProps, onMounted, watch, ref, watchEffect} from 'vue';
import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'
const store = useStore();
const { header, beach } = storeToRefs(store)

const mapEl = ref(null)
let map, marker

// 관련된(함수 내부) 반응형 값들이 바뀌면 이 콜백을 다시 실행해주는 함수
watchEffect(() => {
  // Pinia에서 가져온 beach 정보에서 위경도 꺼냄
  const lat = beach.value.latitude
  const lng = beach.value.longitude

  // 아직 준비 안 된 경우 바로 종료
  if (!lat || !lng || !mapEl.value || !window.naver?.maps) return

  // 네이버 지도에서 쓰는 좌표 객체 생성
  const pos = new window.naver.maps.LatLng(lat, lng)

  // map이 한 번도 만들어진 적 없으면 (초기 렌더 시점)
  if (!map) {
    map = new window.naver.maps.Map(mapEl.value, {
      center: pos,
      zoom: 15
    })
    marker = new window.naver.maps.Marker({
      position: pos,
      map
    })

    // 이미 map이 만들어져 있으면 새로 안 만들고 중심 좌표와 마커 위치만 업데이트
  } else {
    map.setCenter(pos)
    marker.setPosition(pos)
  }
})
</script>