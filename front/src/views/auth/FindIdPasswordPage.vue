<template>
  <div class="auth-page d-flex flex-column align-items-center justify-content-center min-vh-100 p-4"
       :style="{ backgroundColor: 'white' }">
    <h1 class="logo fw-bolder mb-5" :style="{ color: darkColor, fontSize: '2.5rem' }">I Sea U</h1>

    <div class="auth-card p-4 rounded-3 shadow-lg w-100" style="max-width: 400px; background-color: white; border: 1px solid #eee;">

      <div class="nav nav-tabs d-flex mb-4" role="tablist">
        <button class="nav-link flex-fill fw-bold" :class="{ 'active': activeTab === 'id' }"
                type="button" @click="changeTab('id')" :style="activeTab === 'id' ? activeTabStyle : inactiveTabStyle">
          아이디
        </button>
        <button class="nav-link flex-fill fw-bold" :class="{ 'active': activeTab === 'password' }"
                type="button" @click="changeTab('password')" :style="activeTab === 'password' ? activeTabStyle : inactiveTabStyle">
          비밀번호
        </button>
      </div>

      <div class="tab-content">
        <div v-if="activeTab === 'id'">
          <div v-if="idStep === 1">
            <div class="form-group mb-3">
              <input type="text" class="form-control" placeholder="본인 실명" v-model="idForm.name">
            </div>
            <div class="form-group mb-3">
              <input type="tel" class="form-control" placeholder="전화번호" v-model="idForm.phone">
            </div>
            <div class="form-group mb-4 d-flex gap-2">
              <input type="text" class="form-control" placeholder="인증번호" v-model="idForm.code">
              <button class="btn fw-bold text-white" :style="{ backgroundColor: mainColor, minWidth: '100px' }" @click="requestVerification('id')">
                인증번호 받기
              </button>
            </div>

            <button class="btn w-100 fw-bold text-white py-2" :style="{ backgroundColor: mainColor }" @click="verifyAndFindId">
              아이디 찾기
            </button>
          </div>
          <div v-else-if="idStep === 2">
            <div class="alert text-center p-3 rounded-3 border-0 fw-bold" :style="{ backgroundColor: mainColor + '10', color: darkColor }">
              {{ idResult.name }}님의 아이디는 {{ idResult.username }}입니다.
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'password'">
          <div v-if="pwStep === 1">
            <div class="form-group mb-3">
              <input type="text" class="form-control" placeholder="아이디" v-model="pwForm.username">
            </div>
            <div class="form-group mb-3">
              <input type="tel" class="form-control" placeholder="전화번호" v-model="pwForm.phone">
            </div>
            <div class="form-group mb-4 d-flex gap-2">
              <input type="text" class="form-control" placeholder="인증번호" v-model="pwForm.code">
              <button class="btn fw-bold text-white" :style="{ backgroundColor: mainColor, minWidth: '100px' }" @click="requestVerification('password')">
                인증번호 받기
              </button>
            </div>

            <button class="btn w-100 fw-bold text-white py-2" :style="{ backgroundColor: mainColor }" @click="verifyAndResetPassword">
              인증 완료
            </button>
          </div>
          <div v-else-if="pwStep === 2">
            <div class="alert text-center p-3 rounded-3 border-0 fw-bold" :style="{ backgroundColor: mainColor + '10', color: darkColor }">
              <i class="fas fa-check-circle me-2" :style="{ color: mainColor }"></i>
              인증에 성공했습니다. 비밀번호를 재설정해주세요.
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="mt-4 text-center">
      <div v-if="activeTab === 'id'">
        <a href="#" @click.prevent="changeTab('password')" class="small fw-bold text-decoration-none" :style="{ color: mainColor }">
          비밀번호 찾기 >
        </a>
      </div>
      <div v-else-if="activeTab === 'password'">
        <a href="#" @click.prevent="changeTab('id')" class="small fw-bold text-decoration-none" :style="{ color: mainColor }">
          아이디 찾기 >
        </a>
      </div>

      <a href="#" @click.prevent="$router.push({ name: 'Login' })" class="small fw-bold text-decoration-none ms-3" :style="{ color: darkColor }">
        로그인으로 돌아가기 >
      </a>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirmModal } from '@/utils/modalUtils';

const router = useRouter();
const { showConfirmModal } = useConfirmModal();

const mainColor = '#0092BA';
const darkColor = '#0B1956';
const inactiveColor = '#f8f9fa';

// --- State ---
const activeTab = ref('id'); // 'id' or 'password'
const idStep = ref(1); // 1: 입력, 2: 결과
const pwStep = ref(1); // 1: 인증, 2: 완료

const idForm = ref({ name: '', phone: '', code: '' });
const idResult = ref({ name: '김준하', username: 'junha123' }); // Dummy result

const pwForm = ref({ username: '', phone: '', code: '' });


// --- Styles ---
const activeTabStyle = computed(() => ({
  backgroundColor: mainColor,
  color: 'white',
  borderColor: mainColor,
}));

const inactiveTabStyle = computed(() => ({
  backgroundColor: inactiveColor,
  color: darkColor,
  borderColor: '#ced4da',
}));

// --- Methods ---
const changeTab = (tab) => {
  activeTab.value = tab;
  idStep.value = 1; // 탭 변경 시 초기화
  pwStep.value = 1; // 탭 변경 시 초기화
};

const requestVerification = (type) => {
  // 인증번호 발송 API 호출 로직
  showConfirmModal({ title: '인증번호 발송', message: '인증번호가 발송되었습니다. (더미: 12345)', type: 'info', autoHide: true });
};

const verifyAndFindId = async () => {
  // 인증번호 확인 및 아이디 찾기 API 호출
  if (idForm.value.code === '12345' && idForm.value.name && idForm.value.phone) {
    // API 성공 시
    idStep.value = 2;
  } else {
    showConfirmModal({ title: '인증 실패', message: '정보를 확인하거나 인증번호를 다시 요청해주세요.', type: 'error' });
  }
};

const verifyAndResetPassword = async () => {
  // 인증번호 확인 및 비밀번호 재설정 전 인증 완료
  if (pwForm.value.code === '12345' && pwForm.value.username && pwForm.value.phone) {
    // API 성공 시
    pwStep.value = 2;

    // 1초 후 비밀번호 재설정 페이지로 리다이렉트
    setTimeout(() => {
      router.push({ name: 'ResetPassword' });
    }, 1000);

  } else {
    showConfirmModal({ title: '인증 실패', message: '정보를 확인하거나 인증번호를 다시 요청해주세요.', type: 'error' });
  }
};
</script>

<style scoped>
/* 입력 필드 스타일 오버라이드 */
.form-control {
  border-radius: 0.475rem;
  border: 1px solid #ced4da;
  height: 48px;
}

/* 탭 스타일 오버라이드 (body-3.png 디자인 기반) */
.nav-tabs {
  border-bottom: none;
}
.nav-link {
  border: 1px solid #ced4da;
  border-bottom-color: transparent !important; /* 아래쪽 테두리 제거 */
  border-radius: 0.475rem 0.475rem 0 0;
  color: v-bind(darkColor);
  background-color: #f8f9fa;
  margin-right: 5px;
}
.nav-link:last-child {
  margin-right: 0;
}
.nav-link.active {
  color: white !important;
  background-color: v-bind(mainColor) !important;
  border-color: v-bind(mainColor) !important;
}
</style>