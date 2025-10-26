<template>
  <BottomSheet
    v-model="isOpen"

    :snap-points="[64, '70%']"
    :initial-snap-point="0"
    
    :blocking="false"              
    :can-swipe-close="false"      
    :can-backdrop-close="false"   
    :expand-on-content-drag="true" 

    :style="{
      '--vsbs-backdrop-bg': 'transparent',             // 배경 어둡게 X
      '--vsbs-background': '#fff',
      '--vsbs-border-radius': '16px 16px 0 0',
      '--vsbs-max-width': '480px',
      '--vsbs-handle-background': 'rgba(0,0,0,0.28)'
    }"
  >


    <!-- 🔼 펼쳐졌을 때 보여줄 내용 (리스트 등) -->
    <div
      style="
        flex:1;
        overflow-y: scroll;
        padding:16px;
        font-size:14px;
        line-height:1.4;
      "
    >
      <!-- 여기가 너 실제 콘텐츠 영역 -->
      <slot />
    </div>
  </BottomSheet>
</template>

<script setup>
import { ref } from 'vue'

const isOpen = ref(true)

/*
TODOLIST
스크롤 지우기  and 리스트 넣기

*/
</script>

<style scoped>
body{
 -ms-overflow-style: none;
 }
::-webkit-scrollbar {
  display: none;
}
</style>