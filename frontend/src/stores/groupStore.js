import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { groupApi } from '@/api/group'; 
import { useConfirmModal } from '@/utils/modalUtils';
import axios from 'axios'; 

// 🚨 API 엔드포인트 URL (groupApi에 정의되지 않았을 경우 대비)
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
const PENDING_URL = `${API_BASE_URL}/api/groups/invitations/pending`;

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
            const response = await groupApi.fetchGroupList();
            
            // 🚨 [핵심 수정]
            // useApi.js가 반환하는 response.data가 { result: [...] } 입니다.
            // response.data.data.result (X) -> response.data.result (O)
            myGroupList.value = response.data.result;

            // 초기 활성 그룹 설정
            if (!activeGroupId.value && myGroupList.value.length > 0) {
                activeGroupId.value = myGroupList.value[0].id;
            }
        } catch (error) {
            console.error('그룹 목록 조회 실패:', error);
            if (error.response && error.response.status === 401) {
                console.log('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
                // (필요시) router.push('/login');
            }
        }
    };

    /**
     * 특정 그룹을 활성화합니다.
     */
    const setActiveGroup = (groupId) => {
        activeGroupId.value = groupId;
    }

    /**
     * 활성화된 그룹의 멤버 위치 정보를 가져옵니다.
     */
    const fetchLocations = async () => {
        if (!activeGroupId.value) return;

        try {
            const response = await groupApi.fetchGroupLocations({ groupId: activeGroupId.value });
            
            // 🚨 [핵심 수정]
            // useApi.js가 반환하는 response.data가 { result: [...] } 입니다.
            // response.data.data.result (X) -> response.data.result (O)
            activeGroupLocations.value = response.data.result;

        } catch (error) {
            console.error('그룹 위치 정보 조회 실패:', error);
            activeGroupLocations.value = [];
        }
    };
    
    // --- 🚨 [추가된 로직] 초대 모달 관련 ---

    /**
     * [추가] 모달 닫기 (공통 로직)
     */
    const closeModal = () => {
        receivedInvitation.value = null;
    };

    /**
     * 1. [핵심] 앱 시작 시, 로그인한 사용자의 대기 중인 초대를 확인합니다.
     */
    const checkPendingInvitations = async () => {
        // 이미 모달이 떠 있거나 초대장 데이터가 있으면 중복 실행 방지
        if (receivedInvitation.value) return;

        try {
            // 🚨 'axios'를 직접 사용했으므로 response.data.data 형태를 가정합니다.
            const response = await axios.get(PENDING_URL, { withCredentials: true });
            
            // 💡 [수정] axios 응답에서 최상위 data 필드의 result 또는 invitations를 확인
            // 서버 응답 형태가 { data: { success: true, result: [...] } } 또는 
            // { data: { success: true, invitations: [...] } } 일 수 있습니다.
            // 서버가 보내는 실제 리스트 필드 이름(invitations)으로 가정하고 수정
            const responseData = response.data.data || response.data; // 컨트롤러 래핑을 대비하여 데이터 경로를 유동적으로 설정
            const invitationList = responseData.invitations || responseData.result || []; // 초대 리스트 필드 확인

            // 1. 대기 중인 초대가 있는지 확인 (리스트의 길이 확인)
            if (invitationList.length > 0) {
                console.log("대기 중인 초대 발견:", invitationList[0]);
                // 2. 첫 번째 초대장을 스토어 상태에 저장 (이 순간 App.vue의 모달이 뜸)
                receivedInvitation.value = invitationList[0];
            } else {
                console.log("대기 중인 초대 없음.");
                receivedInvitation.value = null;
            }
        } catch (error) {
            console.error("대기 중인 초대 확인 실패:", error);
            receivedInvitation.value = null;
        }
    };

    /**
     * 초대 수락 (초대 모달에서 호출)
     */
    const acceptInvitation = async (invitation) => {
        if (!invitation) {
            console.log("[수락] invitation 객체가 null입니다.");
            return; 
        }

        const groupId = invitation.id; // 💡 [수정] invitation.invitationId 대신 invitation.id 사용
        console.log(`[수락 시작] invitationId: ${groupId} 수락 API 호출 시도...`); 

        try {
            // 1. API 호출 (useApi.js가 응답 본문 { data: {...} }를 반환)
            const response = await groupApi.acceptLocationSharing({ invitationId: groupId });
            
            // 🚨 [수정 완료] 
            // response.data.data.success (X) -> response.data.success (O)
            if (response.data && response.data.success === true) {
                
                // --- 2. [진짜 성공] ---
                console.log("[수락 성공] API 응답 받음:", response.data); 
                showConfirmModal({ title: '초대 수락', message: `${invitation.inviterName} 님의 그룹 초대를 수락했습니다.`, type: 'success', autoHide: true });
                closeModal(); 
                
                // 3. 그룹 목록 새로고침 (중앙 관리소의 핵심 기능)
                console.log("[수락 성공] 그룹 목록 새로고침(fetchGroups)을 호출합니다."); 
                fetchGroups(); 

            } else {
                
                // --- 4. [조용한 실패] ---
                console.error("🚨 [수락 실패] 서버가 success: false를 반환했습니다.", response.data);
                const failMessage = response.data?.message || '초대 수락에 실패했습니다.';
                showConfirmModal({ title: '오류', message: failMessage, type: 'error' });
                closeModal();
            }

        } catch (error) {
            // --- 5. [진짜 실패] (서버가 4xx, 5xx 에러 반환) ---
            console.error("🚨 [수락 실패] CATCH 블록 실행됨:", error); 
            showConfirmModal({ title: '오류', message: '초대 수락 중 서버 오류가 발생했습니다.', type: 'error' });
            closeModal(); 
        }
    };

    /**
     * 초대 거절 (초대 모달에서 호출)
     */
    const rejectInvitation = async (invitation) => {
        if (!invitation) {
            console.log("[거절] invitation 객체가 null입니다.");
            return; 
        }

        const groupId = invitation.id; // 💡 [수정] invitation.invitationId 대신 invitation.id 사용
        console.log(`[거절 시작] invitationId: ${groupId} 거절 API 호출 시도...`); 

        try {
            // 1. API 호출
            const response = await groupApi.rejectLocationSharing({ invitationId: groupId });
            
            // 🚨 [수정 완료] 
            // response.data.data.success (X) -> response.data.success (O)
            if (response.data && response.data.success === true) {

                // --- 2. [진짜 성공] ---
                console.log("[거절 성공] API 응답 받음:", response.data);
                showConfirmModal({ title: '초대 거절', message: `${invitation.inviterName} 님의 그룹 초대를 거절했습니다.`, type: 'info', autoHide: true });
                closeModal(); 

            } else {
                
                // --- 3. [조용한 실패] ---
                console.error("🚨 [거절 실패] 서버가 success: false를 반환했습니다.", response.data);
                const failMessage = response.data?.message || '초대 거절에 실패했습니다.';
                showConfirmModal({ title: '오류', message: failMessage, type: 'error' });
                closeModal();
            }

        } catch (error) {
            // --- 4. [진짜 실패] ---
            console.error("🚨 [거절 실패] CATCH 블록 실행됨:", error);
            showConfirmModal({ title: '오류', message: '초대 거절 중 서버 오류가 발생했습니다.', type: 'error' });
            closeModal(); 
        }
    };
    // --- 🚨 [추가된 로직] 끝 ---

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
        checkPendingInvitations, 
        closeModal, 
        getActiveGroupLocations,
        getActiveGroupId,
        getMyGroupList,
    };
});