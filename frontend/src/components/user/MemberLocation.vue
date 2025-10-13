<!--(P2) 구성원 위치 목록-->
<template>
  <div class="member-list card p-3">
    <h5>👥 그룹 구성원 위치</h5>
    <ul class="list-group">
      <li v-for="member in members" :key="member.userId" class="list-group-item">
        {{ member.userId }} - {{ member.lat.toFixed(4) }}, {{ member.lng.toFixed(4) }}
      </li>
    </ul>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'

defineProps({
  members: {
    type: Array,
    default: () => []
  }
})
</script>
