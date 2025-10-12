<template>
  <div id="map" class="rounded border" style="height:100%;"></div>
</template>

<script setup>
import { onMounted, defineEmits } from 'vue'
const emit = defineEmits(['risk-clicked'])

onMounted(() => {
  if (!window.naver) return
  const mapCenter = new naver.maps.LatLng(35.158, 129.058)
  const map = new naver.maps.Map('map', { center: mapCenter, zoom: 13 })

  const zones = [
    { lat: 35.158, lng: 129.058, depth: 1.5, label: '심층 구역' },
    { lat: 35.160, lng: 129.063, depth: 1.8, label: '위험 구역' },
  ]

  zones.forEach((zone) => {
    const marker = new naver.maps.Marker({
      position: new naver.maps.LatLng(zone.lat, zone.lng),
      map,
      title: zone.label,
    })
    naver.maps.Event.addListener(marker, 'click', () => emit('risk-clicked', zone))
  })
})
</script>
