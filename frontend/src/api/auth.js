// src/api/auth.js
import { useApi } from '@/utils/useApi.js';

// 로그인 API (POST)
const { execute: login } = useApi('post', '/auth/login');

// 회원가입 API (POST)
const { execute: register } = useApi('post', '/auth/register');

// 로그아웃 API (POST)
const { execute: logout } = useApi('post', '/auth/logout');

// 아이디 찾기 API (POST)
const { execute: findId } = useApi('post', '/auth/find-id');

// 비밀번호 재설정 API (POST)
const { execute: resetPassword } = useApi('post', '/auth/reset-password');

// 사용자 설정 업데이트 API (PUT)
const { execute: updateSettings } = useApi('put', '/user/settings');

export const authApi = {
    login,
    register,
    logout,
    findId,
    resetPassword,
    updateSettings
};
