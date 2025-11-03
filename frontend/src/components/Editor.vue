<template>
  <quill-editor
    v-model:value="state.content" 
    :options="state.editorOption" 
    @change="onEditorChange($event)" 
  ></quill-editor>
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
  border: 1px solid #ced4da !important;
}
.ql-container {
  width: 100% !important;
  height: 500px !important;
  border: 1px solid #ced4da !important;
  border-top: none;
}
.ql-editor {
  height: 500px;
  overflow: scroll;
  overflow-x: hidden;
}
.ql-editor img {
  max-width: 200% !important;
  max-height: 200px !important;
  object-fit: cover; /* 비율 깨짐 방지용으로 추천 */
}
</style>