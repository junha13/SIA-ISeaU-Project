import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useStore = defineStore('store', () => {
  // 헤더를 다르게 해야하니까 
  const header = ref("I Sea U")

  //
  const beach = ref({})

  return { header, beach }
})