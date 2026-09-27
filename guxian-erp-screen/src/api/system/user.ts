import request from '../request.ts'

// 用户分页查询
export function getUserPage(params: any) {
  return request({
    url: '/system/user/page',
    method: 'get',
    params
  })
}

// 新增/保存用户
export function saveUser(data: any) {
  return request({
    url: '/system/user/save',
    method: 'post',
    data
  })
}

// 删除用户
export function delUser(id: number) {
  return request({
    url: `/system/user/${id}`,
    method: 'delete'
  })
}

// 根据用户id获取角色id列表（编辑回显）
export function getRoleIds(userId: number) {
  return request({
    url: `/system/user/getRoleIds/${userId}`,
    method: 'get'
  })
}

// 重置密码
export function resetPwd(userId: number) {
  return request({
    url: `/system/user/resetPwd/${userId}`,
    method: 'put'
  })
}
