<template>
  <div class="container-fluid pb-5">
    <!-- 상단 타이틀 -->
    <div class="d-flex align-items-center justify-content-between p-3 border-bottom">
      <div class="d-flex align-items-center">
        <i class="bi bi-chevron-left me-2" @click="$router.back()" style="cursor:pointer"></i>
        <h5 class="fw-bold mb-0">{{ beachName }}</h5>
      </div>
      <div>
        <i class="bi bi-bell-fill text-danger me-3"></i>
        <i class="bi bi-list"></i>
      </div>
    </div>

    <!-- 대표 이미지 -->
    <img :src="headerImg" class="img-fluid" alt="beach image" />

    <!-- 탭 메뉴 -->
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

    <!-- 탭 컨텐츠 -->
    <div class="mt-3">
      <component :is="currentTab" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import BeachInfoTab from './tabs/BeachInfoTab.vue'
import BeachDangerTab from './tabs/BeachDangerTab.vue'
import BeachWeatherTab from './tabs/BeachWeatherTab.vue'
import BeachDepthTab from './tabs/BeachDepthTab.vue'
import BeachCctvTab from './tabs/BeachCctvTab.vue'

const beachName = '속초 해수욕장'
const headerImg = '@/public/images/beach/sea1.jpg'

const mainColor = '#0092BA'
const darkColor = '#0B1956'

const tabs = [
  { key: 'info', label: '상세정보', comp: BeachInfoTab },
  { key: 'danger', label: '위험정보', comp: BeachDangerTab },
  { key: 'weather', label: '날씨정보', comp: BeachWeatherTab },
  { key: 'depth', label: '수심지도', comp: BeachDepthTab },
  { key: 'cctv', label: 'CCTV', comp: BeachCctvTab }
]

const activeTab = ref('info')
const currentTab = computed(() => tabs.find(t => t.key === activeTab.value).comp)
</script>

<style scoped>
button {
  transition: 0.2s ease-in-out;
}
</style>
