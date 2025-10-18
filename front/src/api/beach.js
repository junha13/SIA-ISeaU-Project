// src/api/beach.js
import { useApi } from '@/utils/useApi.js';

// 해수욕장 목록 조회 API (GET)
const { execute: fetchBeachList } = useApi('get', '/beaches');

// 즐겨찾기 추가 API (POST)
const { execute: addFavorite } = useApi('post', '/favorites/add');

// 즐겨찾기 제거 API (DELETE/POST) - 여기서는 POST라고 가정
const { execute: removeFavorite } = useApi('post', '/favorites/remove');

// 활동 해수욕장 선택 API (POST)
const { execute: selectBeach } = useApi('post', '/user/select-beach');

// 활동 해수욕장 선택 해제 API (POST)
const { execute: unselectBeach } = useApi('post', '/user/unselect-beach');

export const beachApi = {
    fetchBeachList,
    addFavorite,
    removeFavorite,
    selectBeach,
    unselectBeach
};
