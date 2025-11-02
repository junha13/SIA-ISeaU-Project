<template>
  <div class="sos-main-page container-fluid p-0">
    <div class="p-3 actions-center">
      <!-- ✅ 상단: 현장 액션 2그리드 -->
      <div class="row g-3 justify-content-center w-100">
        <!-- 라이프가드 -->
        <div class="col-6">
          <div
            class="feature-card"
            :style="{ borderColor: safetyColor, color: darkColor, backgroundImage: `url(${cardImages.lifeguard})`, backgroundSize: 'cover', backgroundPosition: 'center' }"
            @click="handleEmergencyCall('lifeguard')"
          >
           <p class="card-top-right fw-bolder fs-1 mt-2 me-2">라이프가드</p>
          </div>
        </div>

        <!-- 해파리 제보 -->
        <div class="col-6">
          <div
            class="feature-card"
            :style="{ borderColor: mainColor, color: darkColor, backgroundImage: `url(${cardImages.jellyfish})`, backgroundSize: 'cover', backgroundPosition: 'center'}"
            @click="$router.push({ name: 'JellyfishReport' })"
          >
            <p class="card-top-right fw-bolder fs-1 mt-2 me-2">해파리 제보</p>
          </div>
        </div>
      </div> <!-- /.row -->
    </div> <!-- /.p-3 -->

    <!-- ✅ 하단 SOS 바(탭바 위로 고정): 119 / 112 -->
    <div class="sos-bar">
      <div class="container px-3 py-2">
        <div class="row g-2">
          <div class="col-6">
            <button
              class="sos-btn w-100"
              :style="{ backgroundColor: dangerColor }"
              @click="handleEmergencyCall('119')"
              aria-label="119 긴급 신고"
            >
              <div class="sos-num">119</div>
              <div class="sos-label">긴급 신고</div>
            </button>
          </div>
          <div class="col-6">
            <button
              class="sos-btn w-100"
              :style="{ backgroundColor: mainColor }"
              @click="handleEmergencyCall('112')"
              aria-label="112 경찰 신고"
            >
              <div class="sos-num">112</div>
              <div class="sos-label">경찰</div>
            </button>
          </div>
        </div> <!-- /.row -->
      </div> <!-- /.container -->
    </div> <!-- /.sos-bar -->

    <!-- 로딩 오버레이 (기존 그대로) -->
    <div
      v-if="sosStore.isLoading"
      class="loading-overlay d-flex align-items-center justify-content-center"
    >
      <div class="loading-box p-5 rounded-3 shadow-lg text-center" :style="{ backgroundColor: 'white' }">
        <h4 class="fw-bolder mb-0" :style="{ color: darkColor }">연결 중입니다....</h4>
      </div>
    </div>
  </div> <!-- /.sos-main-page -->
</template>


<script setup>
import { ref, onMounted } from 'vue';
import { useSOSStore } from '@/stores/sosStore'; // SOS Store 사용
import { useConfirmModal } from '@/utils/modalUtils';

import { useStore } from '@/stores/store.js';
import { storeToRefs } from 'pinia'

const store = useStore();

const { header } = storeToRefs(store)

onMounted(() => {
  header.value = '긴급신고'
})


const sosStore = useSOSStore();
const { showConfirmModal } = useConfirmModal();

// --- Color Definitions ---
const mainColor = '#0092BA';
const darkColor = '#0B1956';
const safetyColor = '#8482FF';
const cautionColor = '#FFB354';
const dangerColor = '#EB725B';

// 카드별 배경 이미지 매핑 (경로는 public 디렉터리 기준)
const cardImages = {
  jellyfish: '/images/sosButton/jellyfish.png',
  lifeguard: '/images/sosButton/lifeguard.png',

};

// ===== 플랫폼 판단 유틸 =====
const isMobileEnv = () => {
  const ua = navigator.userAgent || navigator.vendor || window.opera;
  return /android|iphone|ipad|ipod|iemobile|opera mini|mobile/i.test(ua);
};

// ===== 실제 다이얼 호출 =====
// - 모바일: 전화앱 호출 (tel:)
// - PC/미지원: 번호 복사 + 안내
const dial = async (displayTitle, number) => {
  try {
    sosStore.setLoading(true);

    // // 스토어에 logEmergencyCall이 존재하면 로깅 (없으면 무시)
    // if (typeof sosStore.logEmergencyCall === 'function') {
    //   await sosStore.logEmergencyCall({ title: displayTitle, target: number }).catch(() => {});
    // }

    if (isMobileEnv()) {
      // 모바일: 전화 앱 호출
      window.location.href = `tel:${number}`;
    } else {
      // PC : 신고는 모바일 전용
      try {
        await navigator.clipboard.writeText(number);
        await showConfirmModal({
          title: '전화 연결 안내',
          message: `신고는 모바일 전용입니다.`,
          type: 'info',
        });
      } catch {
        await showConfirmModal({
          title: '전화 연결 안내',
          message: `${displayTitle} 번호: ${number}\n복사가 차단된 환경이에요. 직접 눌러 전화해 주세요.`,
          type: 'info',
        });
      }
    }
  } finally {
    sosStore.setLoading(false);
  }
};

// --- Methods ---
// 신고 타깃: '119' | '122' | 'lifeguard'
const handleEmergencyCall = async (target) => {
  
  // 1) 타깃별 텍스트/번호 맵
  const CONFIG = {
    '119':        { title: '119 긴급 신고', number: '119' },
    '112':        { title: '112 경찰 신고', number: '112'},
    'lifeguard':  { title: '라이프가드 신고', number: null } // 고정번호가 없으니 별도 처리
  };

  const cfg = CONFIG[target];

  const confirmed = await showConfirmModal({
    title: cfg.title,
    message: `${cfg.title}에 바로 연결할까요?`,
    type: 'confirm',
    confirmText: '전화 연결',
    cancelText: '취소',
  });
  if (!confirmed) return;

  // 실제 다이얼 (로딩/로깅 포함)
  await dial(cfg.title, cfg.number);
};
</script>

<style scoped>
.sos-main-page {
  min-height: calc(100vh - 55px - 60px); /* 상단 헤더/하단 탭바 높이에 맞게 필요시 조정 */
}

/* 카드(상단 2개) */
.feature-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 180px; /* 카드 높이 고정 */
  padding: 20px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative; /* 오버레이용 */
  overflow: hidden;
}
.feature-card:hover { transform: translateY(-3px); box-shadow: 0 6px 15px rgba(0,0,0,.1); }

/* 카드 우측 상단에 텍스트를 배치 (m-1 유사 마진) */
.card-top-right {
  position: absolute;
  top: 0.25rem;
  right: 0.25rem;
  margin: 0;
  z-index: 3;
  color: #ffffff;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
}

/* 하단 SOS 바: 앱 하단 탭바(약 60px) 위에 겹치지 않도록 bottom 오프셋 */
.sos-bar {
  position: fixed; left: 0; right: 0; bottom: 60px; /* ← 하단 탭바 높이만큼 띄움 */
  z-index: 1030; /* 탭바 위로 */
  background: linear-gradient(180deg, rgba(255,255,255,0) 0%, #ffffff 35%);
  padding-top: 6px;
}

/* SOS 버튼 스타일 */
.sos-btn {
  border: none; border-radius: 16px; color: #fff;
  padding: 14px 10px; box-shadow: 0 10px 24px rgba(0,0,0,.12);
}
.sos-num { font-size: 24px; font-weight: 900; line-height: 1; }
.sos-label { font-size: 12px; opacity: .95; }

/* 로딩 오버레이 (기존) */
.loading-overlay {
  position: fixed; inset: 0; background-color: rgba(0,0,0,.4); z-index: 2000;
}
.loading-box { background-color: #fff; padding: 40px; border-radius: 12px; }

.actions-center{
  min-height: inherit;
  display: grid;
  place-items: center;
}

</style>