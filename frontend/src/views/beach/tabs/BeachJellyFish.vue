<template>
  <div class="p-3">
    <!-- 제목 + 버튼 -->
    <div class="d-flex align-items-center justify-content-between mb-3">
      <h5 class="fw-bold text-primary mb-0">해파리 출현 현황 ({{ regionLabel }})</h5>
      <button class="btn btn-sm btn-primary text-white shadow-sm" @click="openModal">
     해파리 백과사전
      </button>
    </div>

    <!-- 데이터 없을 때 -->
    <div v-if="rows.length === 0" class="alert alert-light border rounded-3 text-center py-3">
      ⚠️ 해당 지역에 대한 해파리 데이터가 없습니다.
    </div>

    <!-- 표 데이터 -->
    <div v-else class="card border-0 shadow-sm">
      <div class="card-body p-0">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light border-bottom text-center">
            <tr>
              <th>주차</th>
              <th>기간</th>
              <th>노무라입깃해파리</th>
              <th>보름달물해파리</th>
              <th>두빛보름달해파리</th>
              <th>합계</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(r, i) in weeklyMatrix" :key="i" class="text-center">
              <td class="fw-semibold">{{ r.주차라벨 }}</td>
              <td><small class="text-muted">{{ r.기간 }}</small></td>
              <td class="text-danger fw-bold">{{ r.노무라입깃해파리 }}</td>
              <td class="text-info fw-bold">{{ r.보름달물해파리 }}</td>
              <td class="text-purple fw-bold">{{ r.두빛보름달해파리 }}</td>
              <td class="fw-bold text-dark">{{ r.합계 }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 합산 요약 -->
    <div v-if="rows.length" class="card mt-3 border-0 shadow-sm">
      <div class="card-body d-flex flex-wrap align-items-center gap-3">
        <div class="d-flex align-items-center gap-2">
          <span class="badge rounded-pill bg-danger">&nbsp;</span>
          <span>노무라입깃해파리</span>
          <strong>{{ totals.노무라입깃해파리 }}</strong>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span class="badge rounded-pill bg-info">&nbsp;</span>
          <span>보름달물해파리</span>
          <strong>{{ totals.보름달물해파리 }}</strong>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span class="badge rounded-pill bg-purple">&nbsp;</span>
          <span>두빛보름달해파리</span>
          <strong>{{ totals.두빛보름달해파리 }}</strong>
        </div>
        <div class="ms-auto fw-bold fs-6">
          전체 합계: <span class="text-dark">{{ grandTotal }}</span>
        </div>
      </div>
    </div>

    <!--해파리 백과사전 모달 -->
    <div v-if="isModalOpen" class="modal-backdrop" @click.self="closeModal">
      <div class="modal-card">
        <div class="modal-header d-flex justify-content-between align-items-center">
          <h5 class="m-0 fw-bold">해파리 백과사전</h5>
          <button class="btn-close" @click="closeModal"></button>
        </div>
        <div class="modal-body">
          <div class="encyclopedia-grid">
            <div v-for="(item, key) in jellyMeta" :key="key" class="encyclo-card">
              <img :src="item.img" class="encyclo-img" :alt="item.name" />
              <h6 class="fw-bold mt-2 mb-1">{{ item.name }}</h6>
              <small class="text-muted">{{ item.sci }}</small>
              <p class="small mt-2">{{ item.desc }}</p>
              <ul class="small text-secondary ps-3 mb-2">
                <li v-for="(fact, i) in item.facts" :key="i">{{ fact }}</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

/* Props */
const props = defineProps({
  beachRegion: { type: String, required: true }
})

/* Reactive state */
const regionData = ref({})
const isModalOpen = ref(false)

/* Fetch JSON 데이터 (public/data 폴더) */
onMounted(async () => {
  try {
    const res = await fetch('/data/weeklyjellyfish.json')
    regionData.value = await res.json()
  } catch (e) {
    console.error('JSON 불러오기 실패:', e)
  }
})

/* UI용 계산 속성 */
const regionLabel = computed(() => props.beachRegion || '지역미지정')
const rows = computed(() => regionData.value[props.beachRegion] || [])

function startDate(r) {
  const s = (r['기간'] || '').split('~')[0]?.trim()
  return s ? new Date(s.replaceAll('.', '-')) : new Date(0)
}

const weeklyMatrix = computed(() =>
  [...rows.value]
    .sort((a, b) => startDate(b) - startDate(a))
    .map(r => ({
      ...r,
      합계: (r['노무라입깃해파리'] ?? 0) +
           (r['보름달물해파리'] ?? 0) +
           (r['두빛보름달해파리'] ?? 0)
    }))
)

const totals = computed(() =>
  weeklyMatrix.value.reduce((acc, r) => {
    acc['노무라입깃해파리'] += r['노무라입깃해파리'] ?? 0
    acc['보름달물해파리'] += r['보름달물해파리'] ?? 0
    acc['두빛보름달해파리'] += r['두빛보름달해파리'] ?? 0
    return acc
  }, { 노무라입깃해파리: 0, 보름달물해파리: 0, 두빛보름달해파리: 0 })
)

const grandTotal = computed(() =>
  Object.values(totals.value).reduce((a, b) => a + b, 0)
)

/* 해파리 백과사전 데이터 */
const jellyMeta = {
  nomura: {
    name: '노무라입깃해파리',
    sci: 'Nemopilema nomurai',
    img: '/public/nomura.jpg',
    desc: '거대하고 독성이 강한 해파리로, 어업과 해수욕장에 피해를 줍니다.',
    facts: ['우산 지름 1m 이상, 대형종', '강한 독성으로 접촉 시 통증 유발', '대량 출현 시 경보 발령 가능']
  },
  moon: {
    name: '보름달물해파리',
    sci: 'Aurelia aurita',
    img: '/public/moon.jpg',
    desc: '투명하며 X자형 생식선이 특징인 온순한 해파리입니다.',
    facts: ['연안에서 흔히 관찰됨', '독성은 약한 편', '대량 발생 시 수영 불편 초래']
  },
  bimoon: {
    name: '두빛보름달해파리',
    sci: 'Aurelia sp. (two-tone)',
    img: '/public/bimoon.jpg',
    desc: '보름달해파리와 유사하나 색상이 두 톤으로 구분되는 변종입니다.',
    facts: ['야간 조명에 모이는 경향', '시각적으로 식별 용이', '독성은 약~중간 수준']
  }
}

/* 모달 제어 */
function openModal() { isModalOpen.value = true }
function closeModal() { isModalOpen.value = false }
</script>

<style scoped>
.text-purple { color: #8e44ad; }
.bg-purple { background-color: #9b59b6 !important; }

.card { border-radius: 12px; }
.table th { font-weight: 600; }

/* 모달 */
.modal-backdrop {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.35);
  display: flex; justify-content: center; align-items: center;
  z-index: 1050;
}
.modal-card {
  width: min(900px, 95vw);
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 0 40px rgba(0,0,0,0.25);
}
.modal-header {
  background: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
  padding: 0.75rem 1rem;
}
.modal-body {
  padding: 1rem 1.25rem;
  max-height: 80vh;
  overflow-y: auto;
}

/* 백과 카드 */
.encyclopedia-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1rem;
}
.encyclo-card {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e8e8e8;
  padding: 1rem;
  transition: 0.2s;
}
.encyclo-card:hover {
  box-shadow: 0 3px 10px rgba(0,0,0,0.1);
  transform: translateY(-3px);
}
.encyclo-img {
  width: 100%; height: 150px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 0.5rem;
}
</style>
