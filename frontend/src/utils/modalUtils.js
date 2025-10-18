// src/utils/modalUtils.js

import { reactive, shallowRef } from 'vue';

// 모달 상태 관리
const modalState = reactive({
    isVisible: false,
    title: '',
    message: '',
    type: 'info',
    confirmText: '확인',
    cancelText: '취소',
    autoHide: false,
    duration: 2000,
    // Promise resolve/reject를 저장할 ref (Confirm 모달용)
    resolvePromise: shallowRef(null),
    rejectPromise: shallowRef(null),
});

/**
 * 전역 ConfirmModal 상태 관리 및 노출 함수
 * @returns {{ showConfirmModal: (options: Object) => Promise<boolean>, hideModal: () => void, modalState: Object }}
 */
export function useConfirmModal() {

    /**
     * 모달을 표시하고, 'confirm' 타입일 경우 Promise를 반환합니다.
     * @param {Object} options - 모달 설정 옵션
     * @returns {Promise<boolean> | void} - 'confirm' 타입일 경우에만 Promise<true/false> 반환
     */
    const showConfirmModal = (options) => {
        Object.assign(modalState, {
            ...options,
            isVisible: true,
            // confirm 타입이 아니면 promise를 사용하지 않습니다.
            resolvePromise: options.type === 'confirm' ? shallowRef(null) : undefined,
            rejectPromise: options.type === 'confirm' ? shallowRef(null) : undefined,
        });

        if (options.type === 'confirm') {
            return new Promise((resolve, reject) => {
                modalState.resolvePromise = resolve;
                modalState.rejectPromise = reject;
            });
        }
    };

    // 모달 숨기기
    const hideModal = () => {
        modalState.isVisible = false;
        // Promise 정리
        modalState.resolvePromise = null;
        modalState.rejectPromise = null;
    };

    return {
        showConfirmModal,
        hideModal,
        modalState
    };
}
