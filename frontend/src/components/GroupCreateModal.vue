<template>
  <div
    v-if="isVisible"
    class="modal-backdrop d-flex align-items-center justify-content-center"
    style="z-index: 1060;"
  >
    <div class="modal-dialog modal-dialog-centered" role="document">
      <div class="modal-content bg-white rounded-xl shadow-sm" style="border:0; border-radius:12px;">

        <div class="modal-header d-flex justify-content-between align-items-center border-0 p-3">
          <h5 class="modal-title fw-bold" style="margin:0; color:#0B1956;">그룹 생성</h5>
          <button type="button" class="btn-close" @click="close"></button>
        </div>

        <div class="modal-body p-3">
          <div class="mb-3">
            <label class="fw-semibold mb-2" style="font-size:0.9rem; color:#0B1956;">그룹 이름</label>
            <input
              type="text"
              class="form-control"
              placeholder="예: 가족여행 부산"
              :value="groupName"
              @input="handleInput"
            />
          </div>

          <div v-if="message" class="text-danger" style="font-size:0.8rem;">
            {{ message }}
          </div>
        </div>

        <div class="modal-footer border-0 p-3 pt-0 d-flex gap-2">
          <button
            type="button"
            class="btn flex-fill text-white fw-bold"
            :style="{
              backgroundColor:'#0092BA',
              opacity: canCreate ? 1 : 0.5
            }"
            :disabled="!canCreate"
            @click="createGroup"
          >
            <span>생성</span>
          </button>

          <button
            type="button"
            class="btn btn-light flex-fill fw-bold"
            :disabled="isLoading"
            @click="close"
          >
            취소
          </button>
        </div>

      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, watch, computed } from 'vue'
import axios from 'axios'

const message = ref('')

const props = defineProps({
  isVisible: { type: Boolean, default: false },
})

const emit = defineEmits(['update:isVisible', 'group-created'])

const groupName = ref('')
const isLoading = ref(false)

const canCreate = computed(() => {
  return groupName.value.trim().length > 0 && !isLoading.value
})

/**
 * 모달의 모든 상태를 초기화합니다.
 */
const resetState = () => {
  groupName.value = ''
  isLoading.value = false
  message.value = ''
}

/**
 * 모달 닫기
 */
const close = () => {
  if (isLoading.value) return
  emit('update:isVisible', false)
}

/**
 * 입력 필드 핸들러
 */
function handleInput(e) {
  groupName.value = e.target.value
  if (!groupName.value.trim()) {
    message.value = '그룹 이름을 입력해주세요'
  } else {
    message.value = ''
  }
}

/**
 * 그룹 생성
 */
const createGroup = async () => {

  const url = `${import.meta.env.VITE_API_BASE_URL}/api/groups/create`;

  if (!canCreate.value || isLoading.value) return
  
  isLoading.value = true
  message.value = '' 

  try {
    const response = await axios.post(
      url, 
      { groupName: groupName.value },
      {
        headers: { 'Content-Type': 'application/json' },
        timeout: 5000,
      }
    )
    
    // 💡 [수정] 컨트롤러가 'data'로 감싼 내부 객체를 사용
    const responseData = response.data.data; 

    // 💡 [최종 수정] 
    // 성공 응답 (log: {success: true, newGroupId: 37})을 확인
    if (responseData && responseData.success === true) {
      const newGroupId = responseData.newGroupId;
      emit('group-created', newGroupId) 
      emit('update:isVisible', false) 
    
    // 💡 [최종 수정] 
    // 200 OK 응답이지만, 비즈니스 로직 실패 시 (log: {success: false, message: "..."})
    } else if (responseData && responseData.message) { 
      message.value = responseData.message;
      console.warn('[CreateGroupModal] 비즈니스 로직 실패:', responseData.message);
    
    } else {
      // 💡 [수정] 
      // 알 수 없는 응답 구조 (if, else if 모두 실패)
      console.error('[CreateGroupModal] 알 수 없는 응답 구조:', response.data);
      message.value = "알 수 없는 응답을 받았습니다.";
    }

  } catch (err) {
    // 💡 [수정] 
    // 4xx/5xx 네트워크/서버 오류
    console.error('[CreateGroupModal] createGroup error:', err);
    if (err.response && err.response.data && err.response.data.message) {
      message.value = err.response.data.message 
    } else {
      message.value = "그룹 생성 중 오류가 발생했습니다."
    }
  } finally {
    isLoading.value = false
  }
}

// 모달 닫힐 때 리셋
watch(
  () => props.isVisible,
  (now) => {
    if (!now) {
      resetState()
    }
  }
)
</script>


<style scoped>
.modal-backdrop {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background-color: rgba(0,0,0,0.6);
}
.modal-content {
  animation: modal-in .2s ease;
}
@keyframes modal-in {
  0% { opacity:0; transform:scale(.95); }
  100% { opacity:1; transform:scale(1); }
}
</style>