<template></template>

<script setup>
import { onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  map: { type: Object, required: true },   // 부모에서 만든 naver.maps.Map
  lat: { type: Number, required: true },
  lng: { type: Number, required: true },
})

let marker

onMounted(() => {
  const pos = new window.naver.maps.LatLng(props.lat, props.lng)
  marker = new window.naver.maps.Marker({ position: pos, map: props.map })
})

watch(() => [props.lat, props.lng], ([lat, lng]) => {
  if (!marker) return
  marker.setPosition(new window.naver.maps.LatLng(lat, lng))
})

onUnmounted(() => {
  if (marker) marker.setMap(null)
})
</script>