<template>
  <div class="container-fluid pb-5">
    <div class="d-flex align-items-center justify-content-between p-3 border-bottom">
      <div class="d-flex align-items-center">
        <i class="fas fa-chevron-left me-2" @click="$router.back()" style="cursor:pointer"></i>
        <h5 class="fw-bold mb-0">{{ beachStore.currentBeachDetail?.beachName || '해수욕장' }}</h5>
      </div>
      <div>
        <i class="fas fa-bell me-3 text-danger"></i>
        <i class="fas fa-bars"></i>
      </div>
    </div>

    <div v-if="beachStore.isDetailLoading" class="p-5 text-center text-muted">
      <i class="fas fa-spinner fa-spin me-2"></i> 상세 정보를 불러오는 중...
    </div>

    <div v-else-if="beachStore.currentBeachDetail">
      <img src="/src/public/images/beach/sea1.jpg" class="img-fluid w-100" alt="beach image" style="max-height: 250px; object-fit: cover;"/>

      <div class="d-flex justify-content-around border-bottom bg-white sticky-top" style="top:55px; z-index:100;">
        <button
            v-for="(tab, i) in tabs"
            :key="i"
            class="btn flex-fill fw-bold py-2"
            :class="{'text-white': activeTab===tab.key}"
            :style="{
              backgroundColor: activeTab === tab.key ? mainColor : 'transparent',
              color: activeTab === tab.key ? 'white' : darkColor
            }"
            @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="mt-3">
        <component :is="currentTab" :detail-data="beachStore.currentBeachDetail" />
      </div>
    </div>

    <div v-else class="p-5 text-center text-muted">
        <i class="fas fa-exclamation-circle me-2"></i>
        해수욕장 정보를 불러오지 못했습니다.
    </div>

  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useBeachStore } from '@/stores/beachStore.js';
import BeachInfoTab from './tabs/BeachInfoTab.vue';
import BeachDangerTab from './tabs/BeachDangerTab.vue';
import BeachWeatherTab from './tabs/BeachWeatherTab.vue';
import BeachDepthTab from './tabs/BeachDepthTab.vue';
import BeachCctvTab from './tabs/BeachCctvTab.vue';

const route = useRoute();
const beachStore = useBeachStore();

const mainColor = '#0092BA';
const darkColor = '#0B1956';

const tabs = [
  { key: 'info', label: '상세정보', comp: BeachInfoTab },
  { key: 'danger', label: '위험정보', comp: BeachDangerTab },
  { key: 'weather', label: '날씨정보', comp: BeachWeatherTab },
  { key: 'depth', label: '수심지도', comp: BeachDepthTab },
  { key: 'cctv', label: 'CCTV', comp: BeachCctvTab }
];

const activeTab = ref('info');
const currentTab = computed(() => tabs.find(t => t.key === activeTab.value)?.comp);

const beachNumber = computed(() => {
    if (route.params.beachNumber) {
        return parseInt(route.params.beachNumber);
    }
    return null;
});

const loadBeachDetail = () => {
  if (beachNumber.value !== null) { 
    beachStore.fetchBeachDetail(beachNumber.value); 
  }
};
watch(() => beachStore.currentBeachDetail, (newData) => {
  if (newData) {
    console.log('--- 🚨 스토어 데이터 상세 🚨 ---');
    
    // 1. ✅ 이 로그가 가장 중요합니다! 객체 전체를 펼쳐보세요.
    console.log('스토어에 저장된 객체 전체:', newData); 
    
    // 2. ❓ 여기서는 'undefined'가 뜰 것입니다.
    console.log('beachName 속성 값:', newData.beachName); 
    
    console.log('------------------------------');
  } else {
    console.log('❌ 스토어 데이터가 null 입니다.');
  }
}, { deep: true });
watch(beachNumber, (newNumber) => {
    if (newNumber !== null) {
        loadBeachDetail();
    }
}, { immediate: true });
</script>

<!-- [수정] 중복된 <style> 태그를 하나로 합칩니다. -->
<style scoped>
button {
  transition: 0.2s ease-in-out;
}
</style>
