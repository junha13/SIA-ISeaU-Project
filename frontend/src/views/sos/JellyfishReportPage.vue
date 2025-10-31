<template>
  <div class="jellyfish-report-page container-fluid p-3">
    <!-- 사진 -->
    <div class="text-center mb-4 p-5 rounded-3 border" style="background:#f8f9fa; cursor:pointer" @click="fileInput.click()">
      <i class="fas fa-camera fs-1 mb-2 text-muted"></i>
      <p class="mb-0 fw-bold text-muted">사진 등록</p>
      <small v-if="sos.reportData.imageFile" class="text-success fw-bold">{{ sos.reportData.imageFile.name }}</small>
      <input ref="fileInputRef" type="file" accept="image/*" style="display:none" @change="onFile"/>
    </div>

    <!-- 위치 -->
    <div class="mb-3">
      <label class="fw-bold mb-2" :style="{ color: darkColor }">위치 (필수)</label>
      <div class="row g-2 mb-2">
        <div class="col-6"><input class="form-control rounded-3" placeholder="위도" :value="sos.reportData.lat ?? ''" @input="onLat"/></div>
        <div class="col-6"><input class="form-control rounded-3" placeholder="경도" :value="sos.reportData.lng ?? ''" @input="onLng"/></div>
      </div>
      <button class="btn w-100 py-3 rounded-3" :disabled="sos.isLoading" @click="refreshLocation" :style="{ background:'#fff', borderColor:'#000' }">
        {{ sos.isLoading ? '위치 가져오는 중...' : '내 위치 새로고침' }}
      </button>
    </div>

    <!-- 전화/설명 -->
    <div class="mb-3">
      <label class="fw-bold mb-2" :style="{ color: darkColor }">전화번호</label>
      <input class="form-control rounded-3" placeholder="전화번호를 입력하세요" :value="sos.reportData.phone" @input="e=>upd('phone', e.target.value)"/>
    </div>
    <div class="mb-3">
      <label class="fw-bold mb-2" :style="{ color: darkColor }">상세 설명</label>
      <textarea class="form-control rounded-3" rows="4" placeholder="상세 설명을 입력하세요" :value="sos.reportData.description" @input="e=>upd('description', e.target.value)"/>
    </div>

    <!-- 지도 -->
    <div class="mb-5">
      <label class="fw-bold mb-2" :style="{ color: darkColor }">지도로 위치 조정</label>
      <div class="map-wrap rounded-3">
        <div ref="mapEl" class="map-box"></div>
        <div class="center-pin">📍</div>
      </div>
    </div>

    <!-- 제출 -->
    <div class="fixed-action-bottom p-3">
      <button class="btn w-100 fw-bolder py-3 fs-5 text-white rounded-3 shadow" :style="{ backgroundColor: dangerColor }" @click="submitReport">
        제보하기
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watchEffect } from 'vue';
import { useConfirmModal } from '@/utils/modalUtils.js';
import { useSOSStore } from '@/stores/sosStore';
import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'

const store = useStore();

const { header } = storeToRefs(store)

onMounted(() => {
  header.value = '해파리 제보'
})


const { showConfirmModal } = useConfirmModal();
const sos = useSOSStore();

const darkColor = '#0B1956';
const dangerColor = '#EB725B';

const fileInputRef = ref(null);
const fileInput = { click: () => fileInputRef.value?.click() };
const onFile = (e) => {
  const f = e.target.files?.[0];
  if (f) { sos.updateReportData('imageFile', f); showConfirmModal({ title:'사진 등록 완료', message:`선택된 파일: ${f.name}`, type:'success', autoHide:true, duration:1500 }); }
};

// 지도
const mapEl = ref(null);
let map = null;

// 여기 코드 간소화 하기
const loadNaver = () => new Promise((res, rej) => {
  if (window.naver?.maps) return res();
  const id = import.meta.env.VITE_NAVER_MAPS_KEY_ID;
  const s = document.createElement('script');
  s.src = `https://oapi.map.naver.com/openapi/v3/maps.js?ncpClientId=${id}`;
  s.onload = () => window.naver?.maps ? res() : rej(new Error('Naver Maps load failed'));
  s.onerror = rej;
  document.head.appendChild(s);
});

const initMap = () => {
  const lat = Number(sos.reportData.lat) || 37.5665;
  const lng = Number(sos.reportData.lng) || 126.9780;
  const center = new naver.maps.LatLng(lat, lng);

  map = new naver.maps.Map(mapEl.value, { center, zoom: 15, zoomControl: true });

  // 이동이 멈추면 지도 중심을 스토어에 커밋
  naver.maps.Event.addListener(map, 'idle', () => {
    const c = map.getCenter();
    commitCoords(c.y, c.x);
  });

  // 초기 렌더 안정화
  setTimeout(() => naver.maps.Event.trigger(map, 'resize'), 200);
};

// 위/경도 값 바뀌면 지도만 따라가게 (맵 한 번만 생성)
watchEffect(() => {
  if (!map || !mapEl.value) return;
  const lat = Number(sos.reportData.lat);
  const lng = Number(sos.reportData.lng);
  if (Number.isFinite(lat) && Number.isFinite(lng)) {
    map.setCenter(new naver.maps.LatLng(lat, lng));
  }
});

const commitCoords = (lat, lng) => {
  const r = (n)=>Number(n.toFixed(6));
  sos.updateReportData('lat', r(lat));
  sos.updateReportData('lng', r(lng));
  sos.updateReportData('location', `${r(lat)},${r(lng)}`);
};

// 입력 핸들러
const upd = (k, v) => sos.updateReportData(k, v);
const onLat = (e) => { const v = Number(e.target.value); upd('lat', e.target.value); if (Number.isFinite(v)) map?.setCenter(new naver.maps.LatLng(v, Number(sos.reportData.lng))); };
const onLng = (e) => { const v = Number(e.target.value); upd('lng', e.target.value); if (Number.isFinite(v)) map?.setCenter(new naver.maps.LatLng(Number(sos.reportData.lat), v)); };

// GPS
const refreshLocation = () => {
  if (!('geolocation' in navigator)) return showConfirmModal({ title:'위치 불가', message:'이 브라우저에서는 위치 기능을 사용할 수 없습니다.', type:'error' });
  sos.setLoading(true);
  navigator.geolocation.getCurrentPosition(
    ({coords:{latitude, longitude}}) => {
      const lat = +latitude.toFixed(6), lng = +longitude.toFixed(6);
      commitCoords(lat, lng); map?.setCenter(new naver.maps.LatLng(lat, lng));
      sos.setLoading(false);
      showConfirmModal({ title:'위치 업데이트', message:`위도 ${lat}, 경도 ${lng}`, type:'success', autoHide:true, duration:1500 });
    },
    () => { sos.setLoading(false); showConfirmModal({ title:'실패', message:'현재 위치를 가져오지 못했습니다.', type:'error' }); },
    { enableHighAccuracy:true, timeout:8000 }
  );
};

// 제출
const submitReport = async () => {
  const c = map.getCenter(); commitCoords(c.y, c.x); // 중심 좌표 확정
  if (!sos.reportData.lat || !sos.reportData.lng) return showConfirmModal({ title:'필수 입력', message:'발견 위치를 입력하거나 지도를 사용해 선택하세요.', type:'error' });
  try { await sos.submitJellyfishReport(); showConfirmModal({ title:'제보 완료', message:'해파리 제보가 접수되었습니다.', type:'success', autoHide:true, duration:2000 }); }
  catch (e) { showConfirmModal({ title:'제보 실패', message:e.message, type:'error' }); }
};

onMounted(async () => { await loadNaver(); initMap(); });
</script>

<style scoped>
.jellyfish-report-page{ min-height:calc(100vh - 55px - 60px); padding-bottom:100px!important; }
.fixed-action-bottom{ position:fixed; bottom:60px; left:0; right:0; background:#fff; border-top:1px solid #eee; z-index:100; }

.map-wrap{ position:relative; width:100%; height:380px; overflow:hidden; }
.map-box{ width:100%; height:100%; background:#f1f1f1; }
.center-pin{ position:absolute; left:50%; top:50%; transform:translate(-50%,-100%); font-size:28px; pointer-events:none; z-index:10; }
</style>
