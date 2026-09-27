import axios from 'axios'
import { ElMessage } from 'element-plus'

// 不要写完整地址！用代理，直接写 /system
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000
  // ❌ 不要加 withCredentials: true
})

// 请求拦截器：携带token
service.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/** 避免同一时刻弹出大量重复提示 */
let lastErrorMsg = ''
let lastErrorTime = 0
function toastError(msg: string) {
  const now = Date.now()
  if (msg === lastErrorMsg && now - lastErrorTime < 1500) return
  lastErrorMsg = msg
  lastErrorTime = now
  ElMessage.error(msg)
}

// 响应拦截器统一处理返回
service.interceptors.response.use(
  (res: any) => {
    const body = res.data
    // 后端统一返回 {code,msg,data}，非 200 说明是业务/系统异常，必须给出提示，
    // 否则页面只会"点了没反应"
    if (body && typeof body.code !== 'undefined' && body.code !== 200) {
      if (body.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('loginUser')
        toastError(body.msg || '登录已过期，请重新登录')
        setTimeout(() => { window.location.href = '/login' }, 800)
      } else {
        toastError(body.msg || '操作失败')
      }
    }
    return body
  },
  err => {
    console.error('请求异常：', err)
    const status = err?.response?.status
    if (status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('loginUser')
      toastError('登录已过期，请重新登录')
      setTimeout(() => { window.location.href = '/login' }, 800)
    } else if (err?.code === 'ECONNABORTED') {
      toastError('请求超时，请检查后端服务是否已启动')
    } else {
      toastError(err?.response?.data?.msg || err?.message || '网络异常，请稍后重试')
    }
    return Promise.reject(err)
  }
)

export default service
