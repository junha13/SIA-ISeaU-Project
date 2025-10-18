<template>
  <div class="group-main-page">
    <!-- 1. Header (AppLayout Header와 구분됨) -->
    <div class="d-flex align-items-center justify-content-between p-3 border-bottom shadow-sm">
      <div class="d-flex align-items-center">
        <!-- 현재 그룹명 표시 (드롭다운으로 그룹 선택 가능하게 구현 예정) -->
        <h5 class="fw-bolder mb-0" :style="{ color: darkColor }">그룹 위치 알림</h5>
      </div>
      <div>
        <i class="fas fa-bell me-3 fs-5" :style="{ color: dangerColor }"></i>
        <i class="fas fa-bars fs-5" :style="{ color: darkColor }"></i>
      </div>
    </div>

    <!-- 2. 지도 영역 -->
    <div class="map-area position-relative" style="height: 400px; background-color: #f0f0f0;">
      <!-- 지도 Placeholder -->
      <div class="h-100 w-100 d-flex justify-content-center align-items-center text-muted fw-bold">
        지도 영역 (Map API 연동 필요)
      </div>

      <!-- 지도 오버레이 버튼 -->
      <div class="map-overlay-buttons position-absolute top-0 end-0 p-3">
        <button class="btn btn-sm btn-white rounded-pill shadow-sm mb-2" style="background-color: white;">
          내 위치 새로고침 <i class="fas fa-sync-alt ms-1"></i>
        </button>
        <button class="btn btn-sm btn-primary rounded-circle shadow-sm" style="width: 40px; height: 40px; background-color: white; border: 1px solid #ccc;">
          <i class="fas fa-location-arrow" :style="{ color: darkColor }"></i>
        </button>
      </div>

      <!-- 멤버 마커 (더미) -->
      <div v-for="member in groupLocations" :key="member.id"
           :style="markerStyle(member.color)"
           class="position-absolute rounded-circle shadow-sm">
      </div>
    </div>

    <!-- 3. 그룹 액션 & 멤버 리스트 -->
    <div class="group-actions p-3">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <button class="btn btn-light-secondary fw-bold rounded-pill" style="background-color: #e9ecef;">
          <i class="fas fa-bell me-2"></i> 알림 설정
        </button>
        <button class="btn fw-bold text-white" :style="{ backgroundColor: mainColor }" @click="showInviteModal = true">
          <i class="fas fa-user-plus me-2"></i> 그룹 초대
        </button>
      </div>

      <!-- 그룹 멤버 리스트 -->
      <h6 class="fw-bold mb-3" :style="{ color: darkColor }">그룹 멤버 ({{ groupLocations.length }}명)</h6>
      <div class="member-list">
        <div v-for="member in groupLocations" :key="member.id" class="d-flex align-items-center py-2 border-bottom">
          <!-- 마커 색상 구분 선 -->
          <div class="me-3 rounded-pill" :style="{ backgroundColor: member.color, width: '4px', height: '50px' }"></div>

          <!-- 멤버 정보 -->
          <div class="flex-grow-1">
            <h6 class="fw-bolder mb-0 fs-6">{{ member.name }} <span class="small text-muted fw-normal ms-1">{{ member.username }}</span></h6>
            <p class="text-secondary small mb-0">{{ member.phone }}</p>
          </div>

          <!-- 상태 및 액션 -->
          <div class="d-flex align-items-center">
            <span :class="['small fw-bold', member.status === '활동 중' ? 'text-success' : 'text-danger']">{{ member.status }}</span>
            <i class="fas fa-comment-dots text-secondary ms-3 me-3" style="cursor: pointer;"></i>
            <i class="fas fa-ellipsis-v text-secondary" style="cursor: pointer;"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- 그룹원 추가 모달 -->
    <GroupInviteModal v-model:isVisible="showInviteModal" />

    <!-- 위치 공유 확인 모달 (초대 받았을 때) -->
    <!-- Pinia Store에 receivedInvitation이 있으면 ConfirmModal 대신 이 모달을 띄워야 함 -->
    <!-- ConfimModal.vue를 확장하여 GroupInviteConfirmModal을 구현하지 않고, GroupInviteConfirmModal을 새로 만듭니다. -->
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useGroupStore } from '@/stores/groupStore.js';
import GroupInviteModal from '@/components/GroupInviteModal.vue'; // 5번 파일

const mainColor = '#0092BA';
const darkColor = '#0B1956';
const dangerColor = '#EB725B';

const groupStore = useGroupStore();
const showInviteModal = ref(false);

// Pinia에서 그룹 위치 정보 가져오기
const groupLocations = computed(() => groupStore.getActiveGroupLocations);

onMounted(() => {
  // 초기 그룹 목록 및 위치 정보 로드
  groupStore.fetchGroups();
  groupStore.fetchLocations();

  // (임시) 초대 테스트를 위한 Pinia Action 호출 (실제는 푸시 알림)
  // setTimeout(() => {
  //   groupStore.receiveInvitation({
  //      id: 99, inviterName: '김해양', inviterPhone: '010-1111-1111', groupId: 1
  //   });
  // }, 3000);
});

// 지도 마커 스타일 (더미)
const markerStyle = (color) => ({
  backgroundColor: color,
  width: '12px',
  height: '12px',
  top: `${Math.random() * 80 + 10}%`, // 맵 영역 내 랜덤 위치
  left: `${Math.random() * 80 + 10}%`,
  zIndex: 10,
  border: '2px solid white',
});

</script>

<style scoped>
.group-main-page {
  /* AppLayout의 Header와 Footer 사이의 공간 */
  min-height: calc(100vh - 55px - 60px);
}
.map-overlay-buttons button:first-child {
  background-color: rgba(255, 255, 255, 0.8);
  color: v-bind(darkColor);
  font-size: 0.8rem;
  padding: 5px 10px;
}
.map-overlay-buttons button:last-child {
  background-color: v-bind(mainColor) !important;
  color: white !important;
}

</style>
