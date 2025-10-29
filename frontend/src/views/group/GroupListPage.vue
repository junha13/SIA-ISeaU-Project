<template>
  <div class="group-list-page container-fluid p-3">

    <!-- Header: 그룹 관리 -->
    <div class="d-flex align-items-center justify-content-between pb-3 border-bottom mb-4">
      <h5 class="fw-bolder mb-0">그룹 관리</h5>
      <button class="btn fw-bold text-white" :style="{ backgroundColor: mainColor }" @click="showCreateGroupModal = true">
        <i class="fas fa-plus me-1"></i> 그룹 생성
      </button>
    </div>

    <!-- 현재 활동 그룹 (Selected Group) -->
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

    <!-- 내 그룹 리스트 -->
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

    <!-- 그룹 생성 모달 (컴포넌트 필요) -->
    <GroupCreateModal v-model:isVisible="showCreateGroupModal" @created="handleGroupCreated" />

  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useGroupStore } from '@/stores/groupStore';
import GroupCreateModal from '@/components/GroupCreateModal.vue'; // 컴포넌트 import 필요

import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'

const store = useStore();

const { header, beach, tabOptions, sortOptions, regionOptions } = storeToRefs(store)


const router = useRouter();
const groupStore = useGroupStore();

// 🎨 Color
const mainColor = '#0092BA';
const darkColor = '#0B1956';
const safetyColor = '#8482FF';

const showCreateGroupModal = ref(false);

onMounted(() => {
  // 그룹 목록 불러오기
  groupStore.fetchGroups();

  header.value = "그룹 관리"
});

// --- Computed ---

// 모든 그룹 목록
const myGroupList = computed(() => groupStore.getMyGroupList);

// 현재 활성화된 그룹 정보
const activeGroup = computed(() =>
    myGroupList.value.find(g => g.id === groupStore.getActiveGroupId)
);

// 현재 활성화된 그룹을 제외한 나머지 그룹
const otherGroupList = computed(() =>
    myGroupList.value.filter(g => g.id !== groupStore.getActiveGroupId)
);

// --- Methods ---

/**
 * 특정 그룹의 위치 공유 메인 페이지로 이동
 * @param {number} groupId - 그룹 ID
 */
const goToGroupMain = (groupId) => {
  router.push({ name: 'GroupMain', params: { id: groupId } });
};

/**
 * 그룹 목록에서 그룹을 선택했을 때 활성 그룹을 변경하고 메인 페이지로 이동
 * @param {number} groupId - 그룹 ID
 */
const setActiveAndGoToMain = (groupId) => {
  // Pinia Store에서 활성 그룹 ID 업데이트
  groupStore.setActiveGroup(groupId);
  goToGroupMain(groupId);
};

// const handleGroupCreated = () => {
//   groupStore.fetchGroups();
// };
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