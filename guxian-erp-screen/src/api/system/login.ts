import request from '../request.ts'
import type { LoginDTO } from '@/types/system.ts'

// 对接后端登录接口 POST /system/login/doLogin
export function loginApi(data: LoginDTO) {
  return request.post('/system/login/doLogin', data)
}