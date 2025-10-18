// src/stores/groupStore.js
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { groupApi } from '@/api/group'; // 그룹 API 로직을 가져옵니다.
import { useConfirmModal } from '@/utils/modalUtils'; // 모달 유틸리티를 사용합니다.

export const useGroupStore = defineStore('group', () => {
    // --- State ---
    // 현재 로그인된 사용자가 속한 그룹 목록
    const myGroupList = ref([]);
    // 현재 선택된 그룹의 ID (GroupMainPage에서 사용)
    const activeGroupId = ref(null);
    // 현재 선택된 그룹의 멤버 위치 정보 목록 (Group 3980에 표시될 데이터)
    const activeGroupLocations = ref([]);
    // 수신된 위치 공유 초대 정보 (Group 3982 모달에 표시될 정보)
    const receivedInvitation = ref(null); // { id: 1, inviterName: '김해양', inviterPhone: '010-1111-1111', groupId: 1 }

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
            activeGroupId.value = myGroupList.value.length > 0 ? myGroupList.value[0].id : null;
        } catch (error) {
            console.error('그룹 목록 조회 실패:', error);
        }
    };

    /**
     * 특정 그룹을 활성화하고 위치 정보를 가져옵니다.
     * @param {number} groupId - 활성화할 그룹 ID
     */
    const setActiveGroup = async (groupId) => {
        activeGroupId.value = groupId;
        // await fetchLocations(groupId);
        // fetchLocations(groupId); // API 호출 로직은 컴포넌트에서 실시간으로 처리할 수 있도록 분리
    }

    /**
     * 활성화된 그룹의 멤버 위치 정보를 가져옵니다.
     * 실제 지도에 표시할 정보입니다.
     */
    const fetchLocations = async () => {
        if (!activeGroupId.value) return;

        try {
            // const response = await groupApi.fetchGroupLocations({ groupId: activeGroupId.value });
            // activeGroupLocations.value = response.data;

            // Dummy Data (Group 3980 기반)
            activeGroupLocations.value = [
                { id: 101, name: '김해양', username: 'marine_kim', phone: '010-1234-5678', color: '#702568', status: '활동 중', lastUpdate: null, lat: 35.16, lng: 129.15 },
                { id: 102, name: '박선장', username: 'captain_park', phone: '010-2345-6789', color: '#B93F67', status: '3분 전', lastUpdate: '3 minutes ago', lat: 35.17, lng: 129.16 },
                { id: 103, name: '이어부', username: 'sailor_lee', phone: '010-3456-7890', color: '#EB725B', status: '10분 전', lastUpdate: '10 minutes ago', lat: 35.15, lng: 129.17 },
                { id: 104, name: '최항해', username: 'voyage_choi', phone: '010-4567-8901', color: '#0B1956', status: '활동 중', lastUpdate: null, lat: 35.18, lng: 129.15 },
            ];
        } catch (error) {
            console.error('그룹 위치 정보 조회 실패:', error);
            activeGroupLocations.value = [];
        }
    };

    /**
     * 위치 공유 초대 수신을 처리하고 모달을 띄웁니다.
     * 실제로는 서버 푸시나 폴링으로 처리됩니다.
     */
    const receiveInvitation = (invitation) => {
        const { showConfirmModal } = useConfirmModal();
        receivedInvitation.value = invitation;

        showConfirmModal({
            title: '그룹 추가 확인',
            message: `${invitation.inviterName} 님께서\n그룹으로 초대하셨습니다.`,
            type: 'confirm', // Confirm 타입으로 사용하여 수락/거절을 유도
            confirmText: '수락',
            cancelText: '거절',
            isInvitation: true, // GroupInviteConfirmModal에서 사용할 플래그
            invitationData: invitation // 모달에 데이터 전달
        }).then(async (result) => {
            if (result === true) {
                // 수락 로직
                // await groupApi.acceptLocationSharing({ groupId: invitation.groupId, invitationId: invitation.id });
                showConfirmModal({
                    title: '초대 수락',
                    message: `${invitation.inviterName} 님의 그룹 초대를 수락했습니다.`,
                    type: 'success',
                    autoHide: true,
                });
            } else {
                // 거절 로직
                // await groupApi.rejectLocationSharing({ groupId: invitation.groupId, invitationId: invitation.id });
                showConfirmModal({
                    title: '초대 거절',
                    message: `${invitation.inviterName} 님의 그룹 초대를 거절했습니다.`,
                    type: 'info',
                    autoHide: true,
                });
            }
            receivedInvitation.value = null; // 처리 완료 후 초기화
        }).catch(() => {
            receivedInvitation.value = null; // 에러 발생 시 초기화
        });
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
        receiveInvitation,
        getActiveGroupLocations,
        getActiveGroupId,
        getMyGroupList,
    };
});
