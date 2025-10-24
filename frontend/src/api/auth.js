// src/api/auth.js
import { useApi } from '@/utils/useApi.js';

// 로그인 API (POST)
const { execute: login } = useApi('post', '/auth/login');

// 회원가입 API (POST)
const { execute: register } = useApi('post', '/auth/register');

// 아이디 중복확인 API (POST)
const { execute: checkId } = useApi('post', '/auth/check-id');

// 회원탈퇴 API (POST)
const { execute: withdraw } = useApi('post', '/auth/withdraw');

// 아이디 찾기 API (POST)
const { execute: findId } = useApi('post', '/auth/find-id');

// 비밀번호 찾기 - 회원번호 조회 API (POST)
const { execute: findUserNumber } = useApi('post', '/auth/find-userNumber');

// 비밀번호 재설정 API (POST)
const { execute: resetPassword } = useApi('post', '/auth/reset-password');

// 사용자 설정 업데이트 API (PUT)
const { execute: updateSettings } = useApi('put', '/user/settings');

// 프론트엔드 전용 로그아웃 (localStorage 토큰 삭제 등)
const logout = () => {
    // 로컬 스토리지에서 토큰 제거
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    // 필요시 추가 처리
};

export const authApi = {
    login,
    register,
    checkId,
    withdraw,
    findId,
    findUserNumber,
    resetPassword,
    updateSettings,
    logout
};