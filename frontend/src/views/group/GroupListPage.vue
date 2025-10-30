<template>
  <div class="group-list-page container-fluid p-3">

    <div class="d-flex align-items-center justify-content-between pb-3 border-bottom mb-4">
      <h5 class="fw-bolder mb-0">그룹 관리</h5>
      <button class="btn fw-bold text-white" :style="{ backgroundColor: mainColor }" @click="showCreateGroupModal = true">
        <i class="fas fa-plus me-1"></i> 그룹 생성
      </button>
    </div>

    <div class="mb-4">
      <h6 class="fw-bold mb-3 text-muted">현재 활동 그룹</h6>
      <div v-if="activeGroup" class="card shadow-sm border-0 rounded-3 p-3"
           :style="{ borderLeft: `5px solid ${mainColor}` }">
        <div class="d-flex justify-content-between align-items-center">
          <div class="flex-grow-1">
            <h5 class="fw-bolder mb-0" :style="{ color: darkColor }">{{ activeGroup.name }}</h5>
            <small class="text-muted">{{ activeGroup.memberCount }}명 활동 중</small>
          </div>
          <button class="btn btn-sm text-white fw-bold" :style="{ backgroundColor: safetyColor }"
                  @click="goToGroupMain(activeGroup.id)">
            <i class="fas fa-map-marker-alt"></i> 위치 보기
          </button>
        </div>
      </div>
      <div v-else class="text-center p-5 border rounded-3 text-muted">
        <p class="mb-0">현재 활성화된 그룹이 없습니다.</p>
      </div>
    </div>

    <div class="mb-4">
      <h6 class="fw-bold mb-3" :style="{ color: darkColor }">내 그룹 목록 ({{ myGroupList.length }}개)</h6>
      <div v-if="otherGroupList.length > 0">
        <div v-for="group in otherGroupList" :key="group.id"
             class="group-list-item card shadow-sm border-0 rounded-3 mb-2 p-3"
             @click="setActiveAndGoToMain(group.id)"
             style="cursor: pointer;">
          <div class="d-flex justify-content-between align-items-center">
            <div class="flex-grow-1">
              <h6 class="fw-bolder mb-0" :style="{ color: darkColor }">{{ group.name }}</h6>
              <small class="text-muted">{{ group.memberCount }}명</small>
            </div>
            <i class="fas fa-chevron-right text-secondary"></i>
          </div>
        </div>
      </div>
      <div v-else-if="!activeGroup" class="text-center p-5 border rounded-3 text-muted">
        <p class="mb-0">참여 중인 그룹이 없습니다. 새로운 그룹을 생성하거나 초대를 기다리세요.</p>
      </div>
    </div>

    <GroupCreateModal v-model:isVisible="showCreateGroupModal" @group-created="handleGroupCreated"/>

  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useGroupStore } from '@/stores/groupStore';
import GroupCreateModal from '@/components/GroupCreateModal.vue';
import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia';

import axios from 'axios'; // 1. Axios를 직접 import 합니다.

const store = useStore();
const { header, beach, tabOptions, sortOptions, regionOptions } = storeToRefs(store);

const router = useRouter();
const groupStore = useGroupStore(); // 2. 스토어 인스턴스는 계속 사용합니다.

// 🎨 Color
const mainColor = '#0092BA';
const darkColor = '#0B1956';
const safetyColor = '#8482FF';

const showCreateGroupModal = ref(false);

/**
 * [신규] Axios를 직접 호출하여 그룹 목록을 가져오고 스토어를 수동 업데이트합니다.
 */
const fetchGroupsDirectly = async () => {
  console.log('[LOG-DIRECT] 1. fetchGroupsDirectly: Axios로 직접 API 호출 시작...');

  try {

    // 3. 캐시(Cache) 문제를 피하기 위해 timestamp 추가
    const url = `${import.meta.env.VITE_API_BASE_URL}/groups?timestamp=${new Date().getTime()}`;

    const response = await axios.get(url, {
      withCredentials: true, // 4. 세션(로그인)을 위해 필수
    });

    console.log('[LOG-DIRECT] 2. API 호출 성공:', response.data);

    // 5. 컨트롤러 응답(data.data.result)에서 실제 목록을 추출
    const groupList = response.data.data.result;
    
    // 6. 스토어의 상태(State)를 직접 업데이트
    groupStore.myGroupList = groupList;

    // 7. 스토어의 activeGroupId도 수동으로 업데이트
    if (!groupStore.activeGroupId && groupList.length > 0) {
      groupStore.activeGroupId = groupList[0].id;
    }
    
    console.log('[LOG-DIRECT] 3. 스토어 상태 수동 업데이트 완료.');

  } catch (error) {
    console.error('[LOG-DIRECT] 4. CATCH! API 호출 실패:', error);
    if (error.response && error.response.status === 401) {
      console.error('  > 401 오류: 로그인이 필요합니다.');
      // router.push('/login'); // 로그인 페이지로 이동
    } else {
      console.error('  > 기타 오류:', error.message);
    }
  }
};


onMounted(() => {
  console.log('[LOG 1] GroupListView가 마운트되었습니다. (onMounted)');
  
  // 8. 스토어 액션 대신, 새로 만든 직접 호출 함수를 사용
  fetchGroupsDirectly();
  
  header.value = "그룹 관리"
});

// --- Computed ---
// (스토어의 상태를 읽어오는 computed는 그대로 둡니다)
const myGroupList = computed(() => groupStore.getMyGroupList);
const activeGroup = computed(() =>
  myGroupList.value.find(g => g.id === groupStore.getActiveGroupId)
);
const otherGroupList = computed(() =>
  myGroupList.value.filter(g => g.id !== groupStore.getActiveGroupId)
);

// (로그용 watch도 그대로 둡니다)
watch(myGroupList, (newList, oldList) => {
  console.log('[LOG 3] myGroupList (computed)가 변경되었습니다.');
  console.log('  > 새 목록:', newList);
  console.log('  > 계산된 activeGroup:', activeGroup.value);
  console.log('  > 계산된 otherGroupList:', otherGroupList.value);
}, {
  immediate: true 
});


// --- Methods ---

const goToGroupMain = (groupId) => {
  router.push({ name: 'GroupMain', params: { id: groupId } });
};

const setActiveAndGoToMain = (groupId) => {
  groupStore.setActiveGroup(groupId); // (이 기능은 스토어의 setActiveGroup 사용)
  goToGroupMain(groupId);
};

const handleGroupCreated = () => {
  console.log('[LOG 2] handleGroupCreated: 모달에서 @group-created 이벤트를 받았습니다.');
  
  // 9. 스토어 액션 대신, 새로 만든 직접 호출 함수를 사용
  fetchGroupsDirectly();
};
</script>
<style scoped>
.group-list-page {
  min-height: calc(100vh - 55px - 60px);
}
.group-list-item {
  transition: transform 0.2s, box-shadow 0.2s;
}
.group-list-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.08) !important;
}
</style>