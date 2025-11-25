<template>
  <div class="p-3">
    <h6 class="fw-bold mb-2">수심 지도</h6>

    <!-- 지도 박스 -->
    <div class="rounded bg-light border shadow-sm overflow-hidden" style="height:400px;">
      <div class="h-100 w-100">
        <div ref="mapEl" style="width:100%;height:100%;"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, ref, watchEffect, onMounted } from 'vue'
import { useStore } from '@/stores/store'
import { storeToRefs } from 'pinia'

defineProps({
  detailData: Object,
})

const ui = useStore()
const { beach } = storeToRefs(ui)

const mapEl = ref(null)

// 네이버 지도 객체들
let map = null
let marker = null
let tifOverlay = null
let lineFeatures = []   // ✅ 라인은 Polyline 배열로 관리

// ✅ 제주 중심 좌표 (대충 이호테우 근처)
const JEJU_CENTER = {
  lat: 33.498035,
  lng: 126.4515,
}

// ============================================================
// 🚨 [좌표 보정 설정] - TIF 이미지만 보정에 사용
// ============================================================
const OFFSET_LAT = -0.00405 
const OFFSET_LNG = -0.0009   // 경도(좌우)는 맞다면 0

// ============================================================
// 1) WMS / WFS URL 설정
// ============================================================

// ✅ 수심 TIF는 WMS 이미지로 가져옴
const TIF_WMS_URL =
  'https://iseau.kr/geoserver/iseau/wms?service=WMS&version=1.1.0&request=GetMap' +
  '&layers=iseau%3Atifshp' +
  '&bbox=125.43753723451356%2C32.48653612344029%2C127.59997397055628%2C34.298153956333806' +
  '&width=2048&height=1714' + 
  '&srs=EPSG%3A4326' +
  '&styles=' +
  '&format=image%2Fpng' +
  '&transparent=true' +
  '&tiled=false'

// ✅ 등고선 라인은 WFS GeoJSON으로 전체 받기 (일단 전체 다 받아서 확인)
const LINE_WFS_URL =
  'https://iseau.kr/geoserver/iseau/ows' +
  '?service=WFS' +
  '&version=1.0.0' +
  '&request=GetFeature' +
  '&typeName=iseau%3Alineshp' +
  '&maxFeatures=50000' +
  '&outputFormat=application%2Fjson' +
  '&srsName=EPSG%3A4326'

// ============================================================
// 2) 오버레이 위치 좌표 (보정값 적용) - TIF용
// ============================================================

const TIF_BBOX = {
  minX: 125.43753723451356 + OFFSET_LNG,
  minY: 32.48653612344029 + OFFSET_LAT,
  maxX: 127.59997397055628 + OFFSET_LNG,
  maxY: 34.298153956333806 + OFFSET_LAT,
}

// =========================
// 3) 라인(WFS) 로드 함수
// =========================
async function loadLineLayer(mapInstance) {
  if (!window.naver?.maps) return

  // 기존 라인 지우기
  lineFeatures.forEach((f) => f.setMap(null))
  lineFeatures = []

  try {
    const res = await fetch(LINE_WFS_URL)
    console.log('WFS status:', res.status)

    if (!res.ok) {
      console.error('WFS 응답 에러:', res.status, res.statusText)
      return
    }

    const geojson = await res.json()
    console.log('WFS geojson:', geojson)

    const features = geojson.features ?? []
    console.log('WFS feature count:', features.length)

    features.forEach((feat, idx) => {
      const geom = feat.geometry
      if (!geom) return

      // 디버깅용: 첫 geometry 찍어보기
      if (idx === 0) {
        console.log('첫 geometry type:', geom.type)
        console.log('첫 coordinates[0]:', geom.coordinates?.[0])
      }

      // ⚠️ 좌표는 [lng, lat] 순서!
      if (geom.type === 'LineString') {
        const path = geom.coordinates.map(([lng, lat]) =>
          // ✅ 등고선에서는 일단 OFFSET 없이 그대로 찍기
          new window.naver.maps.LatLng(lat, lng),
        )

        const poly = new window.naver.maps.Polyline({
          path,
          map: mapInstance,
          strokeColor: '#ff0000',   // 눈에 잘 보이게 빨간색
          strokeWeight: 2,
          strokeOpacity: 1,
        })
        lineFeatures.push(poly)
      } else if (geom.type === 'MultiLineString') {
        geom.coordinates.forEach((lineCoords) => {
          const path = lineCoords.map(([lng, lat]) =>
            new window.naver.maps.LatLng(lat, lng),
          )

          const poly = new window.naver.maps.Polyline({
            path,
            map: mapInstance,
            strokeColor: '#ff0000',
            strokeWeight: 2,
            strokeOpacity: 1,
          })
          lineFeatures.push(poly)
        })
      }
    })

    console.log('생성된 Polyline 개수:', lineFeatures.length)

    // ✅ 라인이 실제로 있다면, 그쪽으로 화면 강제 이동
    if (lineFeatures.length > 0) {
      const bounds = new window.naver.maps.LatLngBounds()

      lineFeatures.forEach((poly) => {
        const path = poly.getPath()
        for (let i = 0; i < path.getLength(); i++) {
          bounds.extend(path.getAt(i))
        }
      })

      mapInstance.fitBounds(bounds)
      console.log('등고선 bounds로 이동 완료')
    } else {
      console.log('등고선 없음 (lineFeatures.length = 0)')
    }
  } catch (e) {
    console.error('라인 WFS 로딩 실패 (예외 발생):', e)
  }
}

// =========================
// 4) 오버레이 생성 함수 (수심 TIF만)
// =========================
function createOverlays(mapInstance) {
  if (!window.naver?.maps) return

  // 1. 수심(TIF) 오버레이만 이미지로 올림
  if (!tifOverlay) {
    const tifBounds = new window.naver.maps.LatLngBounds(
      new window.naver.maps.LatLng(TIF_BBOX.minY, TIF_BBOX.minX),
      new window.naver.maps.LatLng(TIF_BBOX.maxY, TIF_BBOX.maxX),
    )

    tifOverlay = new window.naver.maps.GroundOverlay(TIF_WMS_URL, tifBounds, {
      map: mapInstance,
      opacity: 0.7,
      clickable: false,
    })
  } else {
    tifOverlay.setMap(mapInstance)
  }

  // ⚠️ 라인은 여기서 GroundOverlay로 올리지 않음!
}

// =========================
// 5) 지도 초기화
// =========================
onMounted(() => {
  if (!mapEl.value || !window.naver?.maps) return

  const center = new window.naver.maps.LatLng(JEJU_CENTER.lat, JEJU_CENTER.lng)

  map = new window.naver.maps.Map(mapEl.value, {
    center,
    zoom: 17,  // 너무 줌 인하지 말고 적당히
    mapTypeId: window.naver.maps.MapTypeId.NORMAL,
    scaleControl: false,
    logoControl: false,
    mapDataControl: false,
  })

  // 1) 수심 TIF GroundOverlay
  createOverlays(map)

  // 2) 라인은 WFS로 받아서 폴리라인으로 그리기
  loadLineLayer(map)

  const lat = beach.value?.latitude
  const lng = beach.value?.longitude
  if (lat && lng) {
    const pos = new window.naver.maps.LatLng(lat, lng)
    marker = new window.naver.maps.Marker({
      position: pos,
      map,
    })
  }
})

// =========================
// 6) 마커 업데이트
// =========================
watchEffect(() => {
  const lat = beach.value?.latitude
  const lng = beach.value?.longitude

  if (!map || !window.naver?.maps) return
  if (!lat || !lng) return

  const pos = new window.naver.maps.LatLng(lat, lng)

  if (!marker) {
    marker = new window.naver.maps.Marker({
      position: pos,
      map,
    })
  } else {
    marker.setPosition(pos)
    marker.setMap(map)
  }

  // 마커 기준으로 적당히 확대
  map.setCenter(pos)
  map.setZoom(17)
})
</script>