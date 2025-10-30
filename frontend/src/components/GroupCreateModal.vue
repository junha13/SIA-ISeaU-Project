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
import { ref, watch } from 'vue' 
import axios from 'axios'

const message = ref('그룹 이름을 입력해주세요')
const canCreate = ref(false)

const props = defineProps({
  isVisible: { type: Boolean, default: false },
})

const emit = defineEmits(['update:isVisible', 'group-created'])

const groupName = ref('')
const isLoading = ref(false)

/**
 * 모달의 모든 상태를 초기화합니다.
 */
const resetState = () => {
  groupName.value = ''
  isLoading.value = false
  message.value = '그룹 이름을 입력해주세요' 
  canCreate.value = false 
}

/**
 * 모달 닫기
 */
const close = () => {
  if (isLoading.value) return
  emit('update:isVisible', false)
  // watch가 리셋을 처리하므로 여기서는 호출 안 함
}

/**
 * 입력 필드 핸들러
 */
function handleInput(e) {
  const currentValue = e.target.value
  groupName.value = currentValue
  
  if (!currentValue.trim()) {
    message.value = '그룹 이름을 입력해주세요'
    canCreate.value = false
  } else {
    groupDoubleCheck(currentValue)
  }
}

/**
 * [최종본] 그룹명 중복 체크 (사용자의 {"result": "true"} 방식 적용)
 */
async function groupDoubleCheck(name) {
  // 1. .env 환경 변수 확인 (Invalid URL 버그 수정)
  const baseUrl = import.meta.env.VITE_API_BASE_URL;
  if (!baseUrl) {
    console.error('[ERROR] VITE_API_BASE_URL이 .env 파일에 정의되지 않았습니다!');
    message.value = '설정 오류: API 주소를 찾을 수 없습니다.';
    return;
  }
  const url = `${baseUrl}/api/groups/doubleCheck`;
  
  canCreate.value = false // 우선 비활성화
  
  try {
    const response = await axios.post(
      url,
      { groupName: name }, // 'pathVariable' 오타가 없는 올바른 코드
      {
        headers: { 'Content-Type': 'application/json' },
        withCredentials: true,
        timeout: 5000,
      }
    )

    // 2. 사용자의 현 자바 코드 응답 방식 ({"data": {"result": "true"}})에 맞춤
    const result = response.data.data.result;

    if (result === 'true') {
      message.value = "그룹 생성이 가능합니다"
      canCreate.value = true // ⬅️ 활성화
    } else if (result === 'false') {
      message.value = "이미 중복된 그룹 이름이 존재합니다."
      canCreate.value = false;
    } else if (result === 'empty') {
      message.value = "그룹 이름을 입력해주세요."
      canCreate.value = false;
    }

  } catch (err) {
    // 500 에러 또는 네트워크 오류
    console.error('[CreateGroupModal] doubleCheck error:', err); 
    canCreate.value = false 
    
    if (err.response) {
      message.value = err.response.data.message || "이름을 확인할 수 없습니다.";
    } else {
      message.value = "요청 중 오류가 발생했습니다.";
    }
  }
}

/**
 * [최종본] 그룹 생성
 */
const createGroup = async () => {
  // 1. .env 환경 변수 확인 (Invalid URL 버그 수정)
  const baseUrl = import.meta.env.VITE_API_BASE_URL;
  if (!baseUrl) {
    console.error('[ERROR] VITE_API_BASE_URL이 .env 파일에 정의되지 않았습니다!');
    message.value = '설정 오류: API 주소를 찾을 수 없습니다.';
    return;
  }
  const url = `${baseUrl}/api/groups/create`;

  if (!canCreate.value || isLoading.value) return
  
  isLoading.value = true
  message.value = '' 

  try {
    const response = await axios.post(
      url, 
      { groupName: groupName.value },
      {
        headers: { 'Content-Type': 'application/json' },
        withCredentials: true,
        timeout: 5000,
      }
    )
    
    // 2. 사용자의 현 자바 코드 응답 방식 ({"data": {"result": "true"}})에 맞춤
    if (response.data.data.result === 'true') {
      emit('group-created') 
      emit('update:isVisible', false) 
    } else {
      message.value = "그룹 생성에 실패했습니다. (서버 응답 오류)";
    }

  } catch (err) {
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