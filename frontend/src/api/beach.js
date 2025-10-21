// src/api/beach.js
import { useApi } from '@/utils/useApi.js';

// 해수욕장 목록 조회 API (GET)
const { execute: fetchBeachList } = useApi('post', '/beaches');

// 해수욕장 상세 정보 조회 API (GET)
const { execute: fetchBeachDetail } = useApi('get', '/beach/detail');

// 즐겨찾기 추가 API (POST)
const { execute: addFavorite } = useApi('post', '/favorites/add');

// 즐겨찾기 제거 API (DELETE/POST)
const { execute: removeFavorite } = useApi('post', '/favorites/remove');

// 활동 해수욕장 선택 API (POST)
const { execute: selectBeach } = useApi('post', '/user/select-beach');

// 활동 해수욕장 선택 해제 API (POST)
const { execute: unselectBeach } = useApi('post', '/user/unselect-beach');

export const beachApi = {
    fetchBeachList: { 
        method: 'post', 
        url: '/api/beach/beaches' 
    },
    fetchBeachDetail,
    addFavorite,
    removeFavorite,
    selectBeach,
    unselectBeach,
};
