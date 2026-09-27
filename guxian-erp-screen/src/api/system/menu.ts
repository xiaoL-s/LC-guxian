import request from '../request.ts'

// 获取用户菜单树，调试阶段临时传userId，后续删掉参数
export function getUserMenu() {
  return request({
    url: '/system/menu/getUserMenus',
    method: 'GET'
  })
}

//菜单分页列表
export function getMenuPage(params:any){
  return request({
    url:'/system/menu/page',
    method:'get',
    params
  })
}

//保存新增/编辑菜单
export function saveMenu(data:any){
  return request({
    url:'/system/menu/save',
    method:'post',
    data
  })
}

//删除菜单
export function deleteMenu(id:number){
  return request({
    url:`/system/menu/${id}`,
    method:'delete'
  })
}

//获取菜单树（父级下拉）
export function getMenuTree(){
  return request({
    url:'/system/menu/tree',
    method:'get'
  })
}

//获取单条
export function getMenuInfo(id:number){
  return request({url:`/system/menu/${id}`,method:'get'})
}