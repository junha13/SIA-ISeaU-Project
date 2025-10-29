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
  { label: '거리순', value: 'distance' },
  { label: '리뷰순', value: 'review' },
  { label: '평점순', value: 'rating' },
]);

const regionOptions = ref([
  { label: "전체", value: "" },
  { label: "강원", value: "강원" },
  { label: "인천", value: "인천" },
  { label: "충청", value: "충청" },
  { label: "전라", value: "전라" },
  { label: "경상", value: "경상" },
  { label: "부산", value: "부산" },
  { label: "제주", value: "제주" },
]);

  return { header, beach, tabOptions, sortOptions, regionOptions }
})