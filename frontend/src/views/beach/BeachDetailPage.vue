<template>
  <div class="container-fluid pb-5">
    <div class="d-flex align-items-center justify-content-between p-3 border-bottom">
      <div class="d-flex align-items-center">
        <i class="fas fa-chevron-left me-2" @click="$router.back()" style="cursor:pointer"></i>
        <h5 class="fw-bold mb-0">{{ beach.beachName }}</h5>
      </div>
      <div>
        <i class="fas fa-bell me-3 text-danger"></i>
        <i class="fas fa-bars"></i>
      </div>
    </div>
    <div>
      <img :src=beach.beachImage class="img-fluid w-100" alt="beach image" style="max-height: 250px; object-fit: cover;"/>

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
        <component :is="currentTab"/>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'
import BeachInfoTab from './tabs/BeachInfoTab.vue';
import BeachDangerTab from './tabs/BeachDangerTab.vue';
import BeachWeatherTab from './tabs/BeachWeatherTab.vue';
import BeachDepthTab from './tabs/BeachDepthTab.vue';
import BeachCctvTab from './tabs/BeachCctvTab.vue';
import axios from 'axios';

const route = useRoute();
const store = useStore();
const { header, beach } = storeToRefs(store)

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

onMounted(() => {
  requestBeachDetail(route.params.beachNumber)
})
//==== axios 모음 ====

async function requestBeachDetail(beachNumber) {
    try {
      const response = await axios.post(`http://localhost:8080/api/beach/detail/${beachNumber}/info`,
        {
          headers: { 'Content-Type': 'application/json' },
          timeout: 5000,
        })
        console.log('OK', response.data.data.result)
        beach.value = response.data.data.result
    } catch (e) {
      console.error('[Detail] load error:', e)
    }
  }

</script>

<!-- [수정] 중복된 <style> 태그를 하나로 합칩니다. -->
<style scoped>
button {
  transition: 0.2s ease-in-out;
}
</style>
