import { useApi } from '@/utils/useApi.js';

// [수정] API 경로를 실제 백엔드 주소로 변경합니다.
const { execute: fetchBeachList } = useApi('post', '/api/beach/beaches');

// 해수욕장 상세 정보 조회 API (GET)
const { execute: fetchBeachDetail } = useApi('get', '/api/beach/detail/{beachNumber}/info');

// 즐겨찾기 추가 API (POST)
const { execute: addFavorite } = useApi('post', '/api/favorites/add');

// 즐겨찾기 제거 API (POST)
const { execute: removeFavorite } = useApi('post', '/api/favorites/remove');

// 활동 해수욕장 선택 API (POST)
const { execute: selectBeach } = useApi('post', '/api/user/select-beach');

// 활동 해수욕장 선택 해제 API (POST)
const { execute: unselectBeach } = useApi('post', '/api/user/unselect-beach');


export const beachApi = {
    fetchBeachList,
    fetchBeachDetail,
    addFavorite,
    removeFavorite,
    selectBeach,
    unselectBeach,
};
