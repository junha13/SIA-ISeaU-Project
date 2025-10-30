<template>
  <div class="group-main-page">
    <div class="d-flex align-items-center justify-content-between p-3 border-bottom shadow-sm">
      <div class="d-flex align-items-center">
        <i class="fas fa-chevron-left me-2 fs-5" @click="$router.push({ name: 'GroupList' })" style="cursor: pointer;" :style="{ color: darkColor }"></i>
        <h5 class="fw-bolder mb-0" :style="{ color: darkColor }">{{ activeGroupName }} 위치 알림</h5>
      </div>
      <div>
        <i class="fas fa-bell me-3 fs-5" :style="{ color: dangerColor }"></i>
        <i class="fas fa-bars fs-5" :style="{ color: darkColor }"></i>
      </div>
    </div>

    <div class="map-area position-relative" style="height: 400px; background-color: #f0f0f0;">
      <div class="h-100 w-100 d-flex justify-content-center align-items-center text-muted fw-bold">
        지도 영역 (그룹 ID: {{ activeGroupId }})
      </div>

      <div class="map-overlay-buttons position-absolute top-0 end-0 p-3">
        <button class="btn btn-sm btn-white rounded-pill shadow-sm mb-2" style="background-color: white;" @click="fetchLocations">
          내 위치 새로고침 <i class="fas fa-sync-alt ms-1"></i>
        </button>
        <button class="btn btn-sm btn-primary rounded-circle shadow-sm" style="width: 40px; height: 40px; background-color: white; border: 1px solid #ccc;">
          <i class="fas fa-location-arrow" :style="{ color: darkColor }"></i>
        </button>
      </div>

      <div v-for="member in groupLocations" :key="member.id"
           :style="markerStyle(member.color)"
           class="position-absolute rounded-circle shadow-sm">
      </div>
    </div>

    <div class="group-actions p-3">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <button class="btn btn-light-secondary fw-bold rounded-pill" style="background-color: #e9ecef;">
          <i class="fas fa-bell me-2"></i> 알림 설정
        </button>
        <button class="btn fw-bold text-white" :style="{ backgroundColor: mainColor }" @click="showInviteModal = true">
          <i class="fas fa-user-plus me-2"></i> 그룹 초대
        </button>
      </div>

      <h6 class="fw-bold mb-3" :style="{ color: darkColor }">그룹 멤버 ({{ groupLocations.length }}명)</h6>
      <div class="member-list">
        <div v-for="member in groupLocations" :key="member.id" class="d-flex align-items-center py-2 border-bottom">
          <div class="me-3 rounded-pill" :style="{ backgroundColor: member.color, width: '4px', height: '50px' }"></div>

          <div class="flex-grow-1">
            <h6 class="fw-bolder mb-0 fs-6">{{ member.name }} <span class="small text-muted fw-normal ms-1">{{ member.username }}</span></h6>
            <p class="text-secondary small mb-0">{{ member.phone }}</p>
          </div>

          <div class="d-flex align-items-center">
            <span :class="['small fw-bold', member.status === '활동 중' ? 'text-success' : 'text-danger']">{{ member.status }}</span>
            <i class="fas fa-comment-dots text-secondary ms-3 me-3" style="cursor: pointer;"></i>
            <i class="fas fa-ellipsis-v text-secondary" style="cursor: pointer;"></i>
          </div>
        </div>
      </div>
    </div>

    <GroupInviteModal v-model:isVisible="showInviteModal" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useConfirmModal } from '@/utils/modalUtils';
import axios from 'axios'; 
import GroupInviteModal from '@/components/GroupInviteModal.vue';

const route = useRoute();
const router = useRouter(); 
const { showConfirmModal } = useConfirmModal(); 

const mainColor = '#0092BA';
const darkColor = '#0B1956';
const dangerColor = '#EB725B';

// --- State ---
const myGroupList = ref([]);
const activeGroupLocations = ref([]);
const receivedInvitation = ref(null); 
const showInviteModal = ref(false);

// --- Getters & Computed ---
const activeGroupId = computed(() => parseInt(route.params.id) || null); 
const activeGroupName = computed(() =>
    myGroupList.value.find(g => g.id === activeGroupId.value)?.name || '그룹 위치 알림'
);

/**
 * [수정됨] 최종적으로 UI에 표시될 그룹 멤버 목록 (중복 제거 로직)
 */
const groupLocations = computed(() => {
    const locations = activeGroupLocations.value;
    const uniqueMembers = {};
    
    // id (user_number)를 키로 사용하여 중복 제거
    locations.forEach(member => {
        // 중복될 경우, 나중에 들어온 값(일반적으로 더 정확한 리더 정보)으로 덮어씁니다.
        // (PostgreSQL 쿼리에서 ORDER BY id, order_key를 사용했으므로 안정적입니다)
        uniqueMembers[member.id] = member;
    });

    return Object.values(uniqueMembers);
});


// --- Actions ---

/**
 * 그룹 목록을 API로부터 가져와 업데이트합니다.
 */
const fetchGroups = async () => {
    try {
        const baseUrl = import.meta.env.VITE_API_BASE_URL;
        const url = `${baseUrl}/api/groups?timestamp=${new Date().getTime()}`; 

        const response = await axios.get(url, { withCredentials: true });
        myGroupList.value = response.data.data.result; 

    } catch (error) {
        console.error('그룹 목록 조회 실패:', error, error.response);
        if (error.response && error.response.status === 401) {
            console.log('로그인이 필요합니다.');
            // router.push('/login'); 
        }
    }
};

/**
 * 활성화된 그룹의 멤버 위치 정보를 가져옵니다.
 */
const fetchLocations = async () => {
    if (!activeGroupId.value) return;

    try {
        const baseUrl = import.meta.env.VITE_API_BASE_URL;
        const url = `${baseUrl}/api/groups/locations?groupId=${activeGroupId.value}`;
        
        const response = await axios.get(url, { withCredentials: true });
        
        // State 업데이트
        activeGroupLocations.value = response.data.data.result;

    } catch (error) {
        console.error('그룹 위치 정보 조회 실패:', error);
        activeGroupLocations.value = [];
    }
};

/**
 * 특정 그룹의 위치 공유 메인 페이지로 이동 (컴포넌트 자체에서는 사용하지 않음)
 */
const goToGroupMain = (groupId) => {
  router.push({ name: 'GroupMain', params: { id: groupId } });
};

// --- Lifecycle & Watchers ---

const loadGroupData = () => {
  if (activeGroupId.value) {
    // 위치 정보 로드
    fetchLocations(); 
  }
}

onMounted(() => {
  // 그룹 목록을 먼저 로드 (그룹 이름을 표시하기 위해 필요)
  fetchGroups(); 
});

// URL의 그룹 ID가 변경될 때마다 데이터 다시 로드
watch(activeGroupId, loadGroupData, { immediate: true });

// 지도 마커 스타일 (더미)
const markerStyle = (color) => ({
  backgroundColor: color,
  width: '12px',
  height: '12px',
  top: `${Math.random() * 80 + 10}%`,
  left: `${Math.random() * 80 + 10}%`,
  zIndex: 10,
  border: '2px solid white',
});
</script>