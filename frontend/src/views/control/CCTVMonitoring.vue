<template>
  <div class="cctv-monitoring container-fluid p-3">
    <div class="row">
      <div class="col-lg-8">
        <h4 class="mb-3 text-secondary">실시간 관제 뷰</h4>
        <div class="row g-2">
          <div v-for="cam in cams" :key="cam.id" class="col-6">
            <div
                class="cctv-frame border rounded shadow-sm overflow-hidden"
                :class="{'border-primary': selectedCam === cam.id, 'border-2': selectedCam === cam.id}"
                @click="selectCam(cam.id)"
            >
              <div class="cam-header p-1 ps-2 bg-secondary text-white small">{{ cam.name }}</div>
              <div class="video-placeholder d-flex align-items-center justify-content-center bg-dark text-white-50">
                <span v-if="cam.status === 'ok'">스트리밍 중 (객체 탐지: {{ cam.detections.length }})</span>
                <span v-else class="text-warning">연결 오류</span>
              </div>

              <div v-if="cam.isDanger" class="danger-overlay bg-danger bg-opacity-75 text-white p-2">
                <i class="bi bi-exclamation-triangle-fill"></i> **위험 구역 진입 감지!**
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-lg-4">
        <ul class="nav nav-tabs mb-2">
          <li class="nav-item">
            <a
                class="nav-link"
                :class="{active: currentTab === 'alert', 'text-primary': currentTab === 'alert'}"
                href="#"
                @click.prevent="currentTab = 'alert'"
            >
              알림 <span v-if="newAlertCount > 0" class="badge bg-danger ms-1">{{ newAlertCount }}</span>
            </a>
          </li>
          <li class="nav-item">
            <a
                class="nav-link"
                :class="{active: currentTab === 'detail', 'text-primary': currentTab === 'detail'}"
                href="#"
                @click.prevent="currentTab = 'detail'"
            >
              상세
            </a>
          </li>
        </ul>

        <div v-if="currentTab === 'alert'" class="alert-panel p-3 bg-light rounded" style="height: 600px; overflow-y: auto;">
          <div
              v-for="(alert, index) in alerts"
              :key="index"
              class="alert mb-2 p-2 border-start border-4"
              :class="alert.level === 'danger' ? 'border-danger bg-danger-subtle' : 'border-warning bg-warning-subtle'"
          >
            <small class="text-muted d-block">{{ alert.time }}</small>
            <strong>[{{ alert.cam }}]</strong> {{ alert.message }}
          </div>
          <p v-if="alerts.length === 0" class="text-center text-muted mt-5">현재 활성 알림이 없습니다.</p>
        </div>

        <div v-else class="detail-panel p-3 bg-light rounded" style="height: 600px; overflow-y: auto;">
          <div v-if="selectedCam">
            <h5 class="text-primary">{{ selectedCamName }} 상세 정보</h5>
          </div>
          <p v-else class="text-center text-muted mt-5">CCTV 뷰를 선택하여 상세 정보를 확인하세요.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const cams = ref([
  { id: 1, name: 'CAM 1', status: 'ok', detections: [1, 2], isDanger: true },
  { id: 2, name: 'CAM 2', status: 'ok', detections: [1], isDanger: false },
  { id: 3, name: 'CAM 3', status: 'ok', detections: [1, 2, 3], isDanger: false },
  { id: 4, name: 'CAM 4', status: 'error', detections: [], isDanger: false },
]);

const alerts = ref([
  { cam: 'CAM 1', time: '오후 5:25:00', message: '위험 구역 (낚시 금지) 내 사람 2명 감지', level: 'danger' },
  { cam: 'CAM 3', time: '오후 5:23:45', message: '수상한 사람 1명 감지 (주변 배회)', level: 'warning' },
]);

const newAlertCount = computed(() => alerts.value.length);
const currentTab = ref('alert');
const selectedCam = ref(1);

const selectCam = (id) => {
  selectedCam.value = id;
  currentTab.value = 'detail';
};

const selectedCamData = computed(() => cams.value.find(c => c.id === selectedCam.value) || cams.value[0]);
const selectedCamName = computed(() => selectedCamData.value.name);

</script>

<style scoped>
.cctv-frame {
  aspect-ratio: 16 / 9;
  cursor: pointer;
  position: relative;
  transition: transform 0.2s, box-shadow 0.2s;
}

.video-placeholder {
  height: calc(100% - 28px);
  font-size: 0.9rem;
}

.danger-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  font-size: 0.8rem;
  text-align: center;
}
/* Bootstrap 5는 bg-*-subtle 클래스를 제공합니다. */
</style>