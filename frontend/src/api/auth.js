// src/api/auth.js
import { useApi } from '@/utils/useApi.js';

// 백엔드 컨트롤러 기본 경로: /api/auth (AuthController)
// 환경변수 VITE_API_BASE_URL은 useApi 내부 axios 인스턴스의 baseURL로 적용됩니다.

// 로그인 API (POST)
const { execute: login } = useApi('post', '/api/auth/login');

// 회원가입 API (POST)
const { execute: register } = useApi('post', '/api/auth/register');

// 아이디 중복확인 API (POST)
const { execute: checkId } = useApi('post', '/api/auth/check-id');

// 회원탈퇴 API (POST)
const { execute: withdraw } = useApi('post', '/api/auth/withdraw');

// 아이디 찾기 API (POST)
const { execute: findId } = useApi('post', '/api/auth/find-id');

// 비밀번호 찾기 - 회원번호 조회 API (POST)
const { execute: findUserNumber } = useApi('post', '/api/auth/find-userNumber');

// 비밀번호 재설정 API (POST)
const { execute: resetPassword } = useApi('post', '/api/auth/reset-password');

// 비밀번호 재설정 전 사용자 검증(뷰에서 참조하는 별칭) - findUserNumber와 동일 엔드포인트 사용
const { execute: verifyUserForPasswordReset } = useApi('post', '/api/auth/find-userNumber');

// (선택) SMS 개발용 엔드포인트 - 프론트에서 참조 중이므로 정의
const { execute: sendSms } = useApi('post', '/api/auth/send-sms');
const { execute: verifySms } = useApi('post', '/api/auth/verify-sms');

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
    verifyUserForPasswordReset,
    resetPassword,
    updateSettings,
    // SMS (개발용)
    sendSms,
    verifySms,
    logout
};