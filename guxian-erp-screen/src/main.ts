import { createApp } from 'vue'
import App from './App.vue'
import router from './router/index.ts'
import pinia from './store/index.ts'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 引入中文语言包
import zhCn from 'element-plus/es/locale/lang/zh-cn'

const app = createApp(App)
// 挂载，全局启用中文
app.use(ElementPlus, {
  locale: zhCn
})
app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.mount('#app')