<template>
  <div class="container-fluid pb-5">
    <div class="mt-2">
      <img :src="beach?.beachImage" class="img-fluid w-100 rounded-1" alt="beach image" style="max-height: 250px; object-fit: cover;"/>

      <div class="d-flex justify-content-around border-bottom bg-white sticky-top mt-2 mb-2" style="top:55px; z-index:100;">
        <button
            v-for="(tab, i) in tabs"
            :key="i"
            class="btn btn-sm flex-fill fw-bold"
            :class="{'text-white': activeTab===tab.key}"
            :style="{
              backgroundColor: activeTab === tab.key ? mainColor : 'transparent',
              color: activeTab === tab.key ? 'white' : darkColor,
              fontSize: '11px'
            }"
            @click="changeTab(tab.key)"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="mt-3 tab-content-wrap">
        <!-- 탭 방향에 따라 애니메이션 이름을 바꿔줌 (앞으로 → tab-left / 뒤로 → tab-right) -->
        <transition :name="isBack ? 'tab-right' : 'tab-left'" mode="out-in">
          <component
            :is="currentTab"
            :key="`${activeTab}-${route.params.beachNumber}`"
            :beach="beach"
            :beach-region="beachRegion"
          />
        </transition>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, proxyRefs } from 'vue';
import { useRoute } from 'vue-router';
import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'
import BeachInfoTab from './tabs/BeachInfoTab.vue';
import BeachDangerTab from './tabs/BeachDangerTab.vue';
import BeachWeatherTab from './tabs/BeachWeatherTab.vue';
import BeachDepthTab from './tabs/BeachDepthTab.vue';
import BeachCctvTab from './tabs/BeachCctvTab.vue';
import BeachJellyFish from '@/views/beach/tabs/BeachJellyFish.vue' 
import axios from 'axios';

const route = useRoute();
const store = useStore();
const { header, beach } = storeToRefs(store)
 
// region 값 계산 (API에 region이 있으면 그대로 사용)
// BeachDetailPage.vue 등에서
const beachRegion = computed(() => detectRegion({
  region: beach.value?.region,
  address: beach.value?.address
}));

function detectRegion({ region, address }) {
  // 1) DB에 region이 있으면 그걸 우선 신뢰
  if (region && typeof region === 'string') return region.trim();

  // 2) 주소 없으면 빈값
  const addr = (address || '').replace(/\s+/g, ''); // 공백 제거
  const t = addr; // 소문자 변환은 한글엔 영향 적어 생략

  // 3) 매핑 테이블: 같은 지역의 다양한 표기를 모두 커버
  const CANDIDATES = [
    { code: '부산', keys: ['부산', '부산광역시'] },
    { code: '강원', keys: ['강원', '강원도', '강원특별자치도'] },
    { code: '제주', keys: ['제주', '제주특별자치도'] },
    { code: '인천', keys: ['인천', '인천광역시'] },
    { code: '경기', keys: ['경기', '경기도'] },
    { code: '경남', keys: ['경남', '경상남도'] },
    { code: '경북', keys: ['경북', '경상북도'] },
    { code: '울산', keys: ['울산', '울산광역시'] },
    { code: '전남', keys: ['전남', '전라남도'] },
    { code: '전북', keys: ['전북', '전라북도'] },
    { code: '충남', keys: ['충남', '충청남도'] },

  ];

  // 4) 주소 문자열에 키워드가 포함되면 해당 code 반환
  for (const { code, keys } of CANDIDATES) {
    if (keys.some(k => t.includes(k))) return code;
  }
  return '';
}

const mainColor = '#0092BA';
const darkColor = '#0B1956';

const tabs = [
  { key: 'info', label: '상세', comp: BeachInfoTab },
  { key: 'danger', label: '위험', comp: BeachDangerTab },
  { key: 'weather', label: '날씨', comp: BeachWeatherTab },
  { key: 'depth', label: '수심', comp: BeachDepthTab },
  { key: 'cctv', label: 'CCTV', comp: BeachCctvTab },
  { key: 'pest', label: '유해생물', comp: BeachJellyFish }
];

const activeTab = ref('info')
const isBack = ref(false)

const changeTab = (nextKey) => {
  // 지금 열려있는 탭이 tabs 배열에서 몇 번째인지 찾기
  const curIdx = tabs.findIndex(t => t.key === activeTab.value)
  const nextIdx = tabs.findIndex(t => t.key === nextKey)
   // '왼쪽으로' 가는지 '오른쪽으로' 가는지 판단해서 애니메이션 방향 정함
  isBack.value = nextIdx < curIdx   // 왼쪽 탭으로 가면 오른쪽에서 들어오게
  activeTab.value = nextKey
}

const currentTab = computed(() => tabs.find(t => t.key === activeTab.value)?.comp);

onMounted(() => {
  requestBeachDetail(route.params.beachNumber)
})
watch(
  () => route.params.beachNumber,
  (n) => n && requestBeachDetail(n)
)
//==== axios 모음 ====

async function requestBeachDetail(beachNumber) {
    try {
const response = await axios.get(
  `${import.meta.env.VITE_API_BASE_URL}/beach/detail/${beachNumber}/info`,
   { headers: { Accept: 'application/json' }, timeout: 5000 }
  )
      beach.value = response.data.data.result
      header.value = beach.value.beachName
    } catch (e) {
      console.error('[Detail] load error:', e)
    }
  }

</script>

<style scoped>
button {
  transition: 0.2s ease-in-out;
}

.tab-content-wrap {
  position: relative;
  overflow: hidden;
}

.tab-content-wrap {
  position: relative;
  overflow: hidden;
  min-height: 240px; /* 탭 바꿀 때 점프 안 하게 */
}

.tab-panel {
  width: 100%;
}

/* → 오른쪽에서 들어오기 */
:deep(.tab-left-enter-from) {
  transform: translateX(100%);
  opacity: 0;
}
:deep(.tab-left-leave-to) {
  transform: translateX(-100%);
  opacity: 0;
}
:deep(.tab-left-enter-active),
:deep(.tab-left-leave-active) {
  transition: all .18s ease;
  position: absolute;
  inset: 0;
}

/* ← 왼쪽에서 들어오기 */
:deep(.tab-right-enter-from) {
  transform: translateX(-100%);
  opacity: 0;
}
:deep(.tab-right-leave-to) {
  transform: translateX(100%);
  opacity: 0;
}
:deep(.tab-right-enter-active),
:deep(.tab-right-leave-active) {
  transition: all .18s ease;
  position: absolute;
  inset: 0;
}
</style>
