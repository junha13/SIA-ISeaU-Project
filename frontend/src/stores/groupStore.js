import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { groupApi } from '@/api/group'; // 🚨 'groupApi'가 정의되어 있다고 가정합니다.
import { useConfirmModal } from '@/utils/modalUtils';
import axios from 'axios'; // 🚨 [필수 추가] 'axios' import 구문이 누락되었습니다.

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
			
			// 컨트롤러의 응답 구조(data.data.result)에 맞춤
			myGroupList.value = response.data.data.result;

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
			
			activeGroupLocations.value = response.data.data.result;

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
			// 🚨 'groupApi'에 이 기능이 없다면, axios를 직접 사용합니다.
			const response = await axios.get(PENDING_URL, { withCredentials: true });
			const data = response.data.data;

			// 1. 대기 중인 초대가 있는지 확인 (count > 0)
			if (data && data.count > 0) {
				console.log("대기 중인 초대 발견:", data.invitations[0]);
				// 2. 첫 번째 초대장을 스토어 상태에 저장 (이 순간 App.vue의 모달이 뜸)
				receivedInvitation.value = data.invitations[0];
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
		if (!invitation) return; // 🚨 안전 가드 추가

		try {
			// 🚨 API 호출 파라미터 수정: invitation 객체 전체 대신 invitationId만 전송
			await groupApi.acceptLocationSharing({ invitationId: invitation.invitationId });
			
			showConfirmModal({ title: '초대 수락', message: `${invitation.inviterName} 님의 그룹 초대를 수락했습니다.`, type: 'success', autoHide: true });
			
			closeModal(); // 🚨 공통 닫기 함수 호출
			fetchGroups(); // 그룹 목록 새로고침
		} catch (error) {
			console.error('초대 수락 실패:', error);
			showConfirmModal({ title: '오류', message: '초대 수락에 실패했습니다.', type: 'error' });
			closeModal(); // 🚨 실패 시에도 모달 닫기
		}
	};

	/**
	 * 초대 거절 (초대 모달에서 호출)
	 */
	const rejectInvitation = async (invitation) => {
		if (!invitation) return; // 🚨 안전 가드 추가

		try {
			// 🚨 API 호출 파라미터 수정: invitation 객체 전체 대신 invitationId만 전송
			await groupApi.rejectLocationSharing({ invitationId: invitation.invitationId });
			
			showConfirmModal({ title: '초대 거절', message: `${invitation.inviterName} 님의 그룹 초대를 거절했습니다.`, type: 'info', autoHide: true });

			closeModal(); // 🚨 공통 닫기 함수 호출
		} catch (error) {
			console.error('초대 거절 실패:', error);
			showConfirmModal({ title: '오류', message: '초대 거절에 실패했습니다.', type: 'error' });
			closeModal(); // 🚨 실패 시에도 모달 닫기
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
		checkPendingInvitations, // 🚨 App.vue가 사용할 수 있도록 반환
		closeModal, // 🚨 반환 (선택 사항)
		getActiveGroupLocations,
		getActiveGroupId,
		getMyGroupList,
	};
});