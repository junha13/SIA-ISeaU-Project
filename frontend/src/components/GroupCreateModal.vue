<template>
  <div
    v-if="isVisible"
    class="modal-backdrop d-flex align-items-center justify-content-center"
    style="z-index: 1060;"
  >
    <div class="modal-dialog modal-dialog-centered" role="document">
      <div class="modal-content bg-white rounded-xl shadow-sm" style="border:0; border-radius:12px;">

        <!-- 헤더 -->
        <div class="modal-header d-flex justify-content-between align-items-center border-0 p-3">
          <h5 class="modal-title fw-bold" style="margin:0; color:#0B1956;">그룹 생성</h5>
          <button type="button" class="btn-close" @click="close"></button>
        </div>

        <!-- 바디 -->
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

        <!-- 푸터 -->
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
import { ref, computed, watch, onMounted } from 'vue'
import axios from 'axios'

const message = ref('그룹 이름을 입력해주세요')
let canCreate = ref(false);

const props = defineProps({
  isVisible: { type: Boolean, default: false },
})

const emit = defineEmits(['update:isVisible', 'group-created'])

const groupName = ref('')
const errorMessage = ref('')
const isLoading = ref(false)

const resetState = () => {
  groupName.value = ''
  errorMessage.value = ''
  isLoading.value = false
}

const close = () => {
  message.value = '그룹 이름을 입력해주세요'
  canCreate.value = false
  if (isLoading.value) return
  emit('update:isVisible', false)
  resetState()
}

// 한글은 keyup으로 하면 최종 값을 적용하기가 어려움
// watch(groupName, (newVal) => {
//   // 그냥 바로 검사 함수 호출
//   groupDoubleCheck(newVal)
// })

function handleInput(e) {
  const currentValue = e.target.value
  groupName.value = currentValue
  groupDoubleCheck(currentValue) // 지금 값으로 즉시 체크
}

async function groupDoubleCheck(groupName) {

  try {
    const response = await axios.post(
      `http://localhost:8080/api/groups/doubleCheck`,
      { groupName: groupName },
      {
        headers: { 'Content-Type': 'application/json' },
        withCredentials: true,
        timeout: 5000,
      }
    )
  
    console.log('group-created', response.data.data)
    if (response.data.data.result === 'true') {
      message.value = "그룹 생성이 가능합니다"
      canCreate.value = true
    }
    if (response.data.data.result === "false") {
      message.value = "이미 중복된 그룹 이름이 존재합니다."
      canCreate.value = false
    }
    if (response.data.data.result === "empty") {
      canCreate.value = false
    }


  } catch (err) {
    console.error('[CreateGroupModal] createGroup error:', err)
}}

const createGroup = async () => {

  try {
    const response = await axios.post(
      `http://localhost:8080/api/groups/create`,
      { groupName: groupName.value,

       },
      {
        headers: { 'Content-Type': 'application/json' },
        withCredentials: true,
        timeout: 5000,
      }
    )

    emit('group-created', response.data)

    // 닫고 리셋
    emit('update:isVisible', false)
    resetState()

  } catch (err) {
    console.error('[CreateGroupModal] createGroup error:', err)
}}

// 외부에서 isVisible 내려올 때 false가 되면 상태 초기화
watch(
  () => props.isVisible,
  (now) => {
    if (!now) resetState()
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