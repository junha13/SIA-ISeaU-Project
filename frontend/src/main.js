import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

import 'bootstrap/dist/js/bootstrap.bundle.min.js';
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

use([
  BarChart, LineChart, PieChart, ScatterChart, MapChart,   // 차트 타입
  GridComponent, TooltipComponent, LegendComponent, TitleComponent,
  DatasetComponent, VisualMapComponent, ToolboxComponent,   // UI 컴포넌트
  CanvasRenderer                                             // 렌더러 (Canvas 권장)
])

const app = createApp(App)
app.use(createPinia())
app.use(router)

app.component('quill-editor', quillEditor)
app.component('v-chart', ECharts)


app.mount('#app')
