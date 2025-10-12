<template>
  <div class="container py-4" style="max-width:900px;">
    <h3 class="fw-bold mb-3 text-center text-success">🛟 해수욕장 안전 정보</h3>

    <ul class="nav nav-pills justify-content-center gap-2 mb-3">
      <li class="nav-item">
        <button :class="['nav-link', tab==='rules' ? 'active' : '']" @click="tab='rules'">안전 수칙</button>
      </li>
      <li class="nav-item">
        <button :class="['nav-link', tab==='lost' ? 'active' : '']" @click="tab='lost'">분실물 안내</button>
      </li>
    </ul>

    <div class="card shadow-sm border-0">
      <div class="card-body">
        <!-- 로딩/에러 -->
        <div v-if="loading" class="d-flex flex-column align-items-center py-5">
          <div class="spinner-border text-primary mb-2"></div>
          <div class="text-muted">불러오는 중…</div>
        </div>
        <div v-else-if="error" class="alert alert-danger">{{ error }}</div>

        <!-- 컨텐츠 -->
        <template v-else>
          <!-- 안전 수칙 -->
          <div v-if="tab==='rules'">
            <ul class="list-group shadow-sm">
              <li v-for="r in rules" :key="r.ruleId" class="list-group-item">
                <div class="fw-bold">{{ r.title }}</div>
                <div class="text-muted small">{{ r.content }}</div>
              </li>
            </ul>
          </div>

          <!-- 분실물 -->
          <div v-else>
            <div class="row g-3">
              <div class="col-12 col-md-6" v-for="item in lostItems" :key="item.id">
                <div class="card border-0 shadow-sm h-100">
                  <div class="card-body">
                    <div class="fw-bold">{{ item.item }}</div>
                    <div class="text-muted small mb-2">습득 위치: {{ item.location }}</div>
                    <div class="badge bg-light text-dark">연락처: {{ item.contact }}</div>
                  </div>
                </div>
              </div>
            </div>
            <div v-if="!lostItems.length" class="text-center text-muted">등록된 분실물이 없습니다.</div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useApi } from '@/composables/useApi'

const tab = ref('rules')
const rules = ref([])
const lostItems = ref([])

const loading = ref(false)
const error = ref('')
const api = useApi()

const loadRules = async () => {
  loading.value = true; error.value = ''
  try {
    const res = await api.get('/api/marine/safety-rules')
    rules.value = res || []
  } catch (e) {
    error.value = e?.message || '안전 수칙 불러오기 실패'
  } finally {
    loading.value = false
  }
}
const loadLost = async () => {
  loading.value = true; error.value = ''
  try {
    const res = await api.get('/api/marine/lost-items', { beachId: 'beach-01', status: 'open' })
    lostItems.value = res || []
  } catch (e) {
    error.value = e?.message || '분실물 목록 불러오기 실패'
  } finally {
    loading.value = false
  }
}

onMounted(loadRules)
watch(tab, (v) => v === 'rules' ? loadRules() : loadLost())
</script>
