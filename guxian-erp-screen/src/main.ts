import { createApp } from 'vue'
import App from './App.vue'
import router from './router/index.ts'
import pinia from './store/index.ts'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 全局样式（屏幕分档 + 组件美化 + 表格约束）
import './style.css'
// 主题系统（5 套年轻化配色，CSS 变量方式）
import './theme.css'
// 主题持久化恢复（刷新后记住上次选择）
import { initTheme } from './utils/theme'
initTheme()
// 引入中文语言包
import zhCn from 'element-plus/es/locale/lang/zh-cn'

const app = createApp(App)
// 挂载，全局启用中文；size 统一为 small —— 让全站控件默认紧凑，减少无效留白
app.use(ElementPlus, {
  locale: zhCn,
  size: 'small'
})
app.use(pinia)
app.use(router)
app.mount('#app')