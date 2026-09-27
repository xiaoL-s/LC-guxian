/// <reference types="vite/client" />

// 允许ts识别css、scss等样式文件
declare module '*.css'
declare module '*.scss'
declare module '*.less'

// 识别vue文件
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// 新增这一段
declare module 'element-plus'
declare module 'element-plus/dist/*'