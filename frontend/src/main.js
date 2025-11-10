import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import axios from 'axios'

axios.defaults.withCredentials = true

import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import 'bootstrap-icons/font/bootstrap-icons.css'
import { quillEditor } from 'vue3-quill'
import 'quill/dist/quill.core.css'
import 'quill/dist/quill.snow.css'

import ECharts from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart, LineChart, PieChart, ScatterChart, MapChart } from 'echarts/charts'
import {
  GridComponent, TooltipComponent, LegendComponent, TitleComponent,
  DatasetComponent, VisualMapComponent, ToolboxComponent
} from 'echarts/components'

import { CanvasRenderer } from 'echarts/renderers'

import 'animate.css'

use([
  BarChart, LineChart, PieChart, ScatterChart, MapChart,   // 차트 타입
  GridComponent, TooltipComponent, LegendComponent, TitleComponent,
  DatasetComponent, VisualMapComponent, ToolboxComponent,   // UI 컴포넌트
  CanvasRenderer                                             // 렌더러 (Canvas 권장)
])

// 바텀시트
import BottomSheet from '@douxcode/vue-spring-bottom-sheet'
import '@douxcode/vue-spring-bottom-sheet/dist/style.css' 

// 무한스크롤
import { InfiniteLoading } from 'infinite-loading-vue3-ts';

//// 2. 커스텀 SCSS 임포트 (색상 변수 적용)
// // 이 파일은 Bootstrap의 변수를 오버라이드하여야 하므로,
// // Bootstrap 기본 CSS 이후에 임포트하는 것이 안전합니다.
import '@/assets/styles/_custom-vars.scss';

const app = createApp(App)
app.use(createPinia())
app.use(router)

// 애플리케이션 시작 시 Auth 스토어를 바로 초기화하여
// sessionStorage/localStorage에 저장된 로그인 정보를 복원합니다.
// (다른 컴포넌트가 마운트되기 전에 auth 상태가 준비되도록 함)
import { useAuthStore } from '@/stores/authStore'
useAuthStore();

app.component('BottomSheet', BottomSheet)
app.component('quill-editor', quillEditor)
app.component('v-chart', ECharts)
app.component('InfiniteLoading', InfiniteLoading)

app.mount('#app')
