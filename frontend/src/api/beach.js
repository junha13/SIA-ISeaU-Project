import { useApi } from '@/utils/useApi.js';

// 해수욕장 목록 조회 API (POST)
const { execute: fetchBeachList } = useApi('post', '/api/beach/beaches');

// 해수욕장 상세 정보 조회 API (GET)
const fetchBeachDetail = (beachNumber) => useApi('get', `/api/beach/detail/${beachNumber}/info`).execute;

// 즐겨찾기 추가 API (POST)
const { execute: addFavorite } = useApi('post', '/api/favorites/add');

// 즐겨찾기 제거 API (POST)
const { execute: removeFavorite } = useApi('post', '/api/favorites/remove');

// 활동 해수욕장 선택 API (POST)
const { execute: selectBeach } = useApi('post', '/api/user/select-beach');

// 활동 해수욕장 선택 해제 API (POST)
const { execute: unselectBeach } = useApi('post', '/api/user/unselect-beach');

// === 댓글(Comment) APIs 동적구조이기 때문에 정적 구조와 다름 ===
// 목록
const fetchComments = (beachNumber) => useApi('get',  `/api/beach/detail/${beachNumber}/comments`).execute;
// 등록
const addComment    = (beachNumber) => useApi('post', `/api/beach/detail/${beachNumber}/comments/insert`).execute;
// 수정
const editComment   = (beachNumber, beachCommentNumber) =>
  useApi('put', `/api/beach/detail/${beachNumber}/comments/update/${beachCommentNumber}`).execute;
// 삭제 (백엔드 경로 주의: beachNumber 없이 commentNumber만)
const deleteComment = (beachCommentNumber) =>
  useApi('delete', `/api/beach/detail/comments/delete/${beachCommentNumber}`).execute;


export const beachApi = {
    fetchBeachList,
    addFavorite,
    removeFavorite,
    selectBeach,
    unselectBeach,

    fetchBeachDetail,
    fetchComments,
    addComment,
    editComment,
    deleteComment
};
