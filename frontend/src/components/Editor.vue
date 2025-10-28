<template>
  <quill-editor
    v-model:value="state.content" 
    :options="state.editorOption" 
    @change="onEditorChange($event)" 
  ></quill-editor>
  <div>
    <!-- <button
      style="
        border: none;
        border-radius: 7px;
        width: 100px;
        height: 35px;
        font-size: 23px;
        margin-top: 30px;
        background-color: rgb(209, 209, 209);
        color: #2c3e50;
      "
      @click="submit(state, title)"
    >
      올리기
    </button> -->
  </div>
</template>

<script>
import { reactive } from 'vue'

export default {
  name: 'App',
  setup(props, { expose }) {
    const state = reactive({
      content: '',
      _content: '',
      editorOption: {
        placeholder: '내용을 입력해주세요...', // placeholder 설정
        modules: {
          toolbar: [
            ['bold', 'italic', 'underline', 'strike'],
            [{ list: 'ordered' }, { list: 'bullet' }],
            [{ size: ['small', false, 'large', 'huge'] }],
            [{ color: [] }, { background: [] }],
            [{ align: [] }],
            ['link', 'image', 'video']
          ]
        }
        // more options
      },
      disabled: false
    })

    const onEditorBlur = (quill) => {
      console.log('editor blur!', quill)
    }
    const onEditorFocus = (quill) => {
      console.log('editor focus!', quill)
    }
    const onEditorReady = (quill) => {
      console.log('editor ready!', quill)
    }
    const onEditorChange = ({ quill, html, text }) => {
      console.log('editor change!', quill, html, text)
      state._content = html
    }
    const getContent = () => state._content || state.content || ''
    expose({ getContent })

    setTimeout(() => {
      state.disabled = true
    }, 2000)

    return { state, onEditorBlur, onEditorFocus, onEditorReady, onEditorChange }
  },
  props: { title: String },
  data() {
    return {}
  },
  methods: {
    submit(state, title) {
      console.log(state._content)
      console.log(title)
    }
  }
}
</script>
<style>
.ql-toolbar {
  width: 100% !important;
}
.ql-container {
  width: 100% !important;
  height: 500px !important;
}
.ql-editor {
  height: 500px;
  overflow: scroll;
  overflow-x: hidden;
}
.ql-editor img {
  max-width: 300px !important;
  max-height: 300px !important;
  object-fit: cover; /* 비율 깨짐 방지용으로 추천 */
}
</style>