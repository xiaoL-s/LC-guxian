import request from '../request.ts'

export function getRolePage(params:any){
  return request({url:'/system/role/page',method:'get',params})
}
export function saveRole(data:any){
  return request({url:'/system/role/save',method:'post',data})
}
export function deleteRole(id:number){
  return request({url:`/system/role/${id}`,method:'delete'})
}
export function getMenuIds(roleId:number){
  return request({url:`/system/role/getMenuIds/${roleId}`,method:'get'})
}
// role.ts 获取全部角色
export function getRoleAll() {
  return request({
    url: '/system/role/listAll',
    method: 'get'
  })
}

export function getMenuTree() {
  return request({
    url: '/system/role/menuTree',
    method: 'get'
  })
}
