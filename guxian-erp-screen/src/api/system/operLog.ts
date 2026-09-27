import request from '../request.ts'

export function getOperLogPage(params:any){
  return request({
    url:'/system/operLog/page',
    method:'get',
    params
  })
}
