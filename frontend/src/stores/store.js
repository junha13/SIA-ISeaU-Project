import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useStore = defineStore('store', () => {
  // 헤더를 다르게 해야하니까 
  const header = ref("I Sea U")

  //
  const beach = ref({})

  const tabOptions = ref([
    { label: '전체', value: 'all' },
    { label: '저장됨', value: 'favorite' },
    { label: '서핑', value: 'surfing' },
    { label: '산책', value: 'walking' },
    { label: '가족', value: 'family' },
  ]);

const sortOptions = ref([
  { label: '이름순', value: 'name' },
  { label: '거리순', value: 'distance' },
  { label: '리뷰순', value: 'review' },
  { label: '평점순', value: 'rating' },
]);

  const regionOptions = ref([
    { label: "전체", value: "" },
    { label: "강원", value: "강원" },
    { label: "경기", value: "경기" },
    { label: "인천", value: "인천" },
    { label: "충청", value: "충청" },
    { label: "전라", value: "전라" },
    { label: "경상", value: "경상" },
    { label: "부산", value: "부산" },
    { label: "제주", value: "제주" },
  ]);

  // 웹 관제
  const controlView = ref('해수욕장')

  const cctvName = ref('')

  const camLabelMap = {
    1: '이호테우',
    2: '중문',
    3: '함덕',
    4: '월정리',
    5: '애월 하귀 가문동 포구',
    6: '김녕리 포구',
    7: '수마 포구',
    8: '유튜브 시연용 라이브',
  }

  const dangerTemplate = {
    1: 0,
    2: 0,
    3: 0,
    4: 0,
    5: 0,
    6: 0,
    7: 0,
    8: 0,
  }

  const cctvLocation = [
    { type: '해수욕장', label: '이호테우', latitude: 33.49794, longitude: 126.453614, direction: 300, fov: 45, range: 300 },
    { type: '해수욕장', label: '중문', latitude: 33.243882, longitude: 126.41454, direction: 285, fov: 45, range: 300 },
    { type: '해수욕장', label: '함덕', latitude: 33.54432, longitude: 126.674138, direction: 200, fov: 60, range: 300 },
    { type: '해수욕장', label: '월정리', latitude: 33.556556, longitude: 126.795072, direction: 70, fov: 45, range: 200 },
    { type: '항구', label: '애월 하귀 가문동 포구', latitude: 33.486824, longitude: 126.392415, direction: 120, fov: 60, range: 300 },
    { type: '항구', label: '김녕리 포구', latitude: 33.557702, longitude: 126.755595, direction: 350, fov: 60, range: 300 },
    { type: '항구', label: '수마 포구', latitude: 33.460759, longitude: 126.933840, direction: 200, fov: 60, range: 300 },
    { type: '항구', label: '시연용 유튜브 라이브', latitude: 37.494363, longitude: 126.887564, direction: 90, fov: 60, range: 300 }
  ]

  return { header, beach, tabOptions, sortOptions, regionOptions, controlView, cctvName, camLabelMap, dangerTemplate, cctvLocation }
})