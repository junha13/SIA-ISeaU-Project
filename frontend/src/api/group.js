// src/api/group.js
import { useApi } from '@/utils/useApi.js';

// 그룹 목록 조회 API (GET)
const { execute: fetchGroupList } = useApi('get', '/groups');

// 그룹원 추가 (초대) API (POST)
const { execute: inviteGroupMember } = useApi('post', '/groups/invite');

// 위치 공유 수락 API (POST)
const { execute: acceptLocationSharing } = useApi('post', '/groups/location/accept');

// 위치 공유 거절 API (POST)
const { execute: rejectLocationSharing } = useApi('post', '/groups/location/reject');

// 그룹 내 현재 위치 조회 (GET)
const { execute: fetchGroupLocations } = useApi('get', '/groups/locations');

export const groupApi = {
    fetchGroupList,
    inviteGroupMember,
    acceptLocationSharing,
    rejectLocationSharing,
    fetchGroupLocations
};