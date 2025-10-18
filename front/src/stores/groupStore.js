// src/stores/groupStore.js
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { groupApi } from '@/api/group';
import { useConfirmModal } from '@/utils/modalUtils';

export const useGroupStore = defineStore('group', () => {
    const { showConfirmModal } = useConfirmModal();
    // --- State ---
    const myGroupList = ref([]);
    const activeGroupId = ref(null);
    const activeGroupLocations = ref([]);
    const receivedInvitation = ref(null); // 초대 모달 데이터

    // --- Actions ---

    /**
     * 그룹 목록을 API로부터 가져와 업데이트합니다.
     */
    const fetchGroups = async () => {
        try {
            // const response = await groupApi.fetchGroupList();
            // myGroupList.value = response.data;

            // Dummy Data
            myGroupList.value = [
                { id: 1, name: '가족여행 부산', memberCount: 4 },
                { id: 2, name: '서핑친구 송정', memberCount: 2 },
            ];
            // 초기 활성 그룹 설정 (MyPage에서 마지막 활동 그룹을 저장할 수도 있음)
            if (!activeGroupId.value && myGroupList.value.length > 0) {
                activeGroupId.value = myGroupList.value[0].id;
            }
        } catch (error) {
            console.error('그룹 목록 조회 실패:', error);
        }
    };

    /**
     * 특정 그룹을 활성화하고 위치 정보를 가져옵니다.
     */
    const setActiveGroup = (groupId) => {
        activeGroupId.value = groupId;
    }

    /**
     * 활성화된 그룹의 멤버 위치 정보를 가져옵니다. (지도 표시용)
     */
    const fetchLocations = async () => {
        if (!activeGroupId.value) return;

        try {
            // const response = await groupApi.fetchGroupLocations({ groupId: activeGroupId.value });
            // activeGroupLocations.value = response.data;

            // Dummy Data
            activeGroupLocations.value = [
                { id: 101, name: '김해양', username: 'marine_kim', phone: '010-1234-5678', color: '#702568', status: '활동 중', lastUpdate: null, lat: 35.16, lng: 129.15 },
                { id: 102, name: '박선장', username: 'captain_park', phone: '010-2345-6789', color: '#B93F67', status: '3분 전', lastUpdate: '3 minutes ago', lat: 35.17, lng: 129.16 },
            ];
        } catch (error) {
            console.error('그룹 위치 정보 조회 실패:', error);
            activeGroupLocations.value = [];
        }
    };

    /**
     * 초대 수락 (초대 모달에서 호출)
     */
    const acceptInvitation = async (invitation) => {
        // await groupApi.acceptLocationSharing(invitation);
        showConfirmModal({ title: '초대 수락', message: `${invitation.inviterName} 님의 그룹 초대를 수락했습니다.`, type: 'success', autoHide: true });
        receivedInvitation.value = null;
        fetchGroups(); // 그룹 목록 새로고침
    };

    /**
     * 초대 거절 (초대 모달에서 호출)
     */
    const rejectInvitation = async (invitation) => {
        // await groupApi.rejectLocationSharing(invitation);
        showConfirmModal({ title: '초대 거절', message: `${invitation.inviterName} 님의 그룹 초대를 거절했습니다.`, type: 'info', autoHide: true });
        receivedInvitation.value = null;
    };

    // --- Getters ---
    const getActiveGroupLocations = computed(() => activeGroupLocations.value);
    const getActiveGroupId = computed(() => activeGroupId.value);
    const getMyGroupList = computed(() => myGroupList.value);

    return {
        myGroupList,
        activeGroupId,
        activeGroupLocations,
        receivedInvitation,
        fetchGroups,
        setActiveGroup,
        fetchLocations,
        acceptInvitation,
        rejectInvitation,
        getActiveGroupLocations,
        getActiveGroupId,
        getMyGroupList,
    };
});