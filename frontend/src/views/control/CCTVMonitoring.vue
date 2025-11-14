<template>
  <div
    class="event-detail-view container-fluid p-4"
    style="background-color: #F0F2F5; min-height: 100vh;"
  >
    <div
      class="top-info-bar d-flex justify-content-between align-items-center mb-4 pb-3 border-bottom"
      style="border-color: #E0E2E6;"
    >
      <!-- 🛎 관제센터 속보 바 -->
      <div class="alert-bar d-flex align-items-center fs-1">
        <span class="badge rounded-pill bg-danger fs-1 fw-bold me-4">속보</span>

        <transition name="alert-fade" mode="out-in">
          <span :key="currentAlert" class="flex-grow-1 text-truncate fw-semibold">
            {{ currentAlert }}
          </span>
        </transition>
      </div>
    </div>

    <div class="row g-4">
      <!-- =================== 좌측 영역 =================== -->
      <div class="col-lg-8 d-flex flex-column" style="gap: 1.5rem;">
        <!-- CCTV 2x2 스트림 -->
        <UseStreams ws-url="ws://localhost:8000/ws/stream" :cam-ids="[1, 2, 3, 4]" />

        <!-- 이벤트 로그 -->
        <div
          class="card p-3 border-0 shadow-sm flex-shrink-0"
          style="background-color: #FFFFFF;"
        >
          <h6 class="text-muted fw-bold mb-3 small">이벤트 로그 (Timeline)</h6>

          <div class="d-flex flex-column gap-2 log-scroll-area">
            <div
              class="log-entry p-2 small rounded fw-semibold"
              style="
                background-color: #F8F9FA;
                border-left: 4px solid var(--iseu-danger);
                color: #333;
              "
            >
              <span class="text-muted">[06:26:31]</span>
              <span class="text-danger">위반 행위 최종 확정</span>
            </div>
            <div
              class="log-entry p-2 small rounded fw-semibold"
              style="
                background-color: #F8F9FA;
                border-left: 4px solid var(--iseu-warning);
                color: #333;
              "
            >
              <span class="text-muted">[06:26:27]</span>
              <span class="text-warning">접근 금지 감지 시작</span>
            </div>
            <div
              class="log-entry p-2 small rounded fw-semibold"
              style="
                background-color: #F8F9FA;
                border-left: 4px solid var(--color-safe);
                color: #333;
              "
            >
              <span class="text-muted">[06:26:26]</span>
              <span style="color: var(--color-safe);">새로운 객체 영역 진입</span>
            </div>
            <div
              class="log-entry p-2 small rounded text-muted"
              style="background-color: #F8F9FA; border-left: 4px solid #EAECEF;"
            >
              <span class="text-muted">[06:26:20]</span> 시스템 활성화
            </div>
          </div>
        </div>
      </div>

      <!-- =================== 우측 패널 =================== -->
      <div class="col-lg-4 d-flex flex-column" style="gap: 1.5rem;">
        <!-- 진입 / 알림 상세 탭 -->
        <div
          class="d-flex justify-content-end align-items-end flex-shrink-0"
          style="height: 30px;"
        >
          <div class="tab-segment-group">
            <button
              type="button"
              class="tab-segment"
              :class="{ active: rightPanelTab === 'overview' }"
              @click="rightPanelTab = 'overview'"
            >
             위험구역 진입
            </button>

            <button
              type="button"
              class="tab-segment"
              :class="{ active: rightPanelTab === 'detail' }"
              @click="rightPanelTab = 'detail'"
            >
              알림 상세
            </button>
          </div>
        </div>

        <div class="card p-3 border-0 shadow-sm flex-grow-1" style="flex-grow: 2;">
          <!-- 탭에 따라 제목 변경 -->
          <h6 class="fw-bold mb-3 small" style="color: #333;">
            {{ rightPanelTab === 'overview' ? '위험구역 진입' : '알림 상세' }}
          </h6>

          <!-- 위험구역 진입: 지도/레이아웃 -->
          <div
            v-if="rightPanelTab === 'overview'"
            class="map-placeholder-base border rounded d-flex align-items-center justify-content-center text-muted h-100"
            style="background-color: #F0F2F5;"
          >
            [지도/레이아웃 Placeholder]
          </div>

          <!-- 알림 상세 -->
          <div
            v-else
            class="border rounded d-flex align-items-center justify-content-center text-muted h-100"
            style="background-color: #FFF7F5;"
          >
            [알림 상세 Placeholder]
          </div>
        </div>

        <!-- 감지 정보 통계 -->
        <div class="card p-3 border-0 shadow-sm flex-grow-1" style="flex-grow: 1;">
          <h6 class="fw-bold mb-3 small" style="color: #333;">감지 정보 통계</h6>
          <div
            class="chart-placeholder-base border rounded d-flex align-items-center justify-content-center text-muted h-100"
            style="background-color: #F0F2F5;"
          >
            [통계 그래프 Placeholder]
          </div>
        </div>
      </div>
    </div>

    <!-- 하단 액션 바 -->
    <div
      class="action-bar fixed-bottom d-flex justify-content-end p-3 border-top"
      style="background-color: #FFFFFF; border-color: #EAECEF !important;"
    >
      <div class="me-auto text-dark fw-semibold small d-flex align-items-center">
        <span class="me-3">처리 상태:</span>
        <div class="form-check form-check-inline">
          <input
            class="form-check-input"
            type="radio"
            name="actionStatus"
            id="actionPending"
            checked
          />
          <label class="form-check-label text-dark" for="actionPending">미처리</label>
        </div>
        <div class="form-check form-check-inline">
          <input
            class="form-check-input"
            type="radio"
            name="actionStatus"
            id="actionProcessing"
          />
          <label class="form-check-label text-dark" for="actionProcessing">처리 중</label>
        </div>
        <div class="form-check form-check-inline">
          <input
            class="form-check-input"
            type="radio"
            name="actionStatus"
            id="actionComplete"
          />
          <label class="form-check-label text-dark" for="actionComplete">완료</label>
        </div>
      </div>
      <button class="btn btn-outline-secondary me-2">영상 다운로드</button>
      <button class="btn btn-warning me-2 fw-bold text-dark">경고 알림 발송</button>
      <button class="btn btn-danger fw-bold text-white">이벤트 처리</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import UseStreams from '@/components/useStreams.vue'

const rightPanelTab = ref('overview')

// 🛎 속보 텍스트 목록
const alerts = ref([
  '이호테우 해수욕장 신원 미상 남성 해파리 쏘임',
  '함덕 해수욕장 완충구역 내 인원 밀집, 관제 요원 주의 필요',
  '중문 해수욕장 파도 높이 상승, 안전요원 순찰 강화 요망',
  '협재 해수욕장 해파리 다수 관측, 입수객 주의 안내 방송 필요',
  '이호테우 해수욕장 20대 여성 구조 요청'
])

const alertIndex = ref(0)
const currentAlert = computed(() => alerts.value[alertIndex.value])

let alertTimer = null

onMounted(() => {
  alertTimer = setInterval(() => {
    alertIndex.value = (alertIndex.value + 1) % alerts.value.length
  }, 5000)
})

onUnmounted(() => {
  if (alertTimer !== null) {
    clearInterval(alertTimer)
  }
})
</script>

<style scoped>
:root {
  --iseu-primary: #0092ba;
  --iseu-secondary: #0b1956;
  --iseu-warning: #ffb354;
  --iseu-danger: #eb725b;

  --color-safe: #8482ff;
  --color-light-bg: #f0f2f5;
  --color-panel-bg: #ffffff;
  --color-separator: #eaecef;
  --color-text-dark: #333333;
}

.event-detail-view {
  font-family: 'Noto Sans KR', sans-serif;
  color: var(--color-text-dark);
}

.card {
  border-radius: 8px !important;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05) !important;
  background-color: var(--color-panel-bg) !important;
}

/* 지도/통계 placeholder 기본 스타일 */
.map-placeholder-base,
.chart-placeholder-base {
  font-size: 1rem;
}

/* 로그 스크롤 영역 */
.log-scroll-area {
  max-height: 150px;
  overflow-y: auto;
}

/* 라디오 버튼 스타일 */
.form-check-input {
  background-color: #fff;
  border-color: #ccc;
}
.form-check-input:checked {
  background-color: var(--iseu-primary);
  border-color: var(--iseu-primary);
}

/* 버튼 텍스트 색상 */
.btn-warning {
  color: var(--color-text-dark) !important;
}
.btn-danger {
  color: #ffffff !important;
}

/* 속보 바: 한 줄 + ... 처리 */
.alert-bar {
  white-space: nowrap;
  overflow: hidden;
}

/* 속보 전환 애니메이션 */
.alert-fade-enter-active,
.alert-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.alert-fade-enter-from,
.alert-fade-leave-to {
  opacity: 0;
  transform: translateY(6px);
}

/* 우측 탭 (위험구역 진입 / 알림 상세) */
.tab-segment-group {
  display: inline-flex;
  border-radius: 999px;
  border: 1px solid #dee2e6;
  overflow: hidden;
  background-color: #ffffff;
  font-size: 0.8rem;
}

.tab-segment {
  padding: 0.3rem 1.1rem;
  border: none;
  background: transparent;
  color: #6c757d;
  font-weight: 700;
  min-width: 70px;
  text-align: center;
  cursor: pointer;
}

/* 가운데 경계선 */
.tab-segment + .tab-segment {
  border-left: 1px solid #dee2e6;
}

/* 선택된 탭 */
.tab-segment.active {
  background-color: var(--iseu-primary);
  color:#40C4FF
}

/* hover 시 살짝 강조 */
.tab-segment:not(.active):hover {
  background-color: #f1f3f5;
}
</style>
