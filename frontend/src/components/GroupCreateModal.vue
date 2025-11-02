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
import { ref, watch, computed } from 'vue' // 💡 computed 추가
import axios from 'axios'

const message = ref('') // 💡 메시지 초기값 제거
// const canCreate = ref(false) // 💡 [제거] computed로 대체

const props = defineProps({
  isVisible: { type: Boolean, default: false },
})

const emit = defineEmits(['update:isVisible', 'group-created'])

const groupName = ref('')
const isLoading = ref(false)

// 💡 [추가] "생성" 버튼 활성화 조건
// groupName이 비어있지 않고, 로딩 중이 아닐 때
const canCreate = computed(() => {
  return groupName.value.trim().length > 0 && !isLoading.value
})

/**
 * 모달의 모든 상태를 초기화합니다.
 */
const resetState = () => {
  groupName.value = ''
  isLoading.value = false
  message.value = '' // 💡 초기화
}

/**
 * 모달 닫기
 */
const close = () => {
  if (isLoading.value) return
  emit('update:isVisible', false)
}

/**
 * 💡 [수정] 입력 필드 핸들러
 */
function handleInput(e) {
  groupName.value = e.target.value
  // 💡 중복 체크 API 호출 제거
  if (!groupName.value.trim()) {
    message.value = '그룹 이름을 입력해주세요'
  } else {
    message.value = '' // 💡 메시지 클리어
  }
}




/**
 * 그룹 생성
 */
const createGroup = async () => {

  const url = `${import.meta.env.VITE_API_BASE_URL}/api/groups/create`;

  // 💡 [수정] computed된 canCreate를 사용
  if (!canCreate.value || isLoading.value) return
  
  isLoading.value = true
  message.value = '' 

  try {
    const response = await axios.post(
      url, 
      { groupName: groupName.value },
      {
        headers: { 'Content-Type': 'application/json' },
        // 💡 [수정] withCredentials 제거 (토큰 사용)
        timeout: 5000,
      }
    )
    
    // 💡 [수정] 백엔드가 "1인 1그룹" 정책을 적용했으므로,
    //          실패 시(result: false) 메시지를 표시해야 함
    if (response.data.data.result === 'true') {
      const newGroupId = response.data.data.newGroupId;
      emit('group-created', newGroupId) 
      emit('update:isVisible', false) 
    } else {
      // 💡 백엔드가 보낸 '실패' 메시지 (예: "이미 그룹이 있습니다.")
      message.value = response.data.message || "그룹 생성에 실패했습니다.";
    }

  } catch (err) {
    console.error('[CreateGroupModal] createGroup error:', err);
    if (err.response && err.response.data && err.response.data.message) {
      // 💡 백엔드가 500 에러와 함께 보낸 메시지
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