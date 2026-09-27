import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/auth.ts'

const routes = [
  {
    path: '/login',
    component: () => import('@/views/login/Login.vue')
  },
  {
    path: '/',
    redirect: '/login'
  },
  // ✅ 修改这里 path: '/system'，不要写成 /system/system
  {
    path: '/system',
    name: 'SystemIndex',
    component: () => import('@/views/system/SystemIndex.vue'),
    redirect: '/system/home',
    children: [
      {
        path: 'home',
        component: () => import('@/views/system/CsHome.vue')
      },
      {
        path: 'user',
        name: 'UserManage',
        component: () => import('@/views/system/UserManage.vue'),
        meta: {
          title: '用户管理',
          icon: 'User'
        }
      },
      {
        path: 'role',
        name: 'RoleManage',
        component: () => import('@/views/system/RoleManage.vue'),
        meta: {
          title: '角色管理',
          icon: 'Role'
        },
      },
            {
        path: 'menu',
        name: 'MenuManage',
        component: () => import('@/views/system/MenuManage.vue'),
        meta: {
          title: '菜单管理',
          icon: 'Menu'
        },
      },
      {
        path: 'dict',
        name: 'DictTypeData',
        component: () => import('@/views/system/DictTypeData.vue'),
        meta: { 
          title: '字典管理' ,
          icon: 'Dice'
        }
      },
      {
        path: 'printTemplate',
        name: 'PrintTemplate',
        component: () => import('@/views/system/PrintTemplate.vue'),
        meta: { 
          title: '打印模板管理' ,
          icon: 'PrintTemplate'
        }
      },
      {
        path: 'operLog',
        name: 'OperLog',
        component: () => import('@/views/system/OperLog.vue'),
        meta: { 
          title: '操作日志' ,
          icon: 'operLog'
        }
      },
    ]
  },
  {
    path: '/customer',
    // 父菜单：客户管理，复用 SystemIndex 布局
    name: 'CustomerIndex',
    component: () => import('@/views/system/SystemIndex.vue'),
    redirect: '/customer/list',
    meta: {
      title: '客户管理',
      icon: 'Customer'
    },
    children: [
      // 子模块：客户档案（菜单路径 /customer/list）
      {
        path: 'list',
        name: 'Customer',
        component: () => import('@/views/customer/Customer.vue'),
        meta: {
          title: '客户档案',
          icon: ''
        }
      },
      // 子模块：客户跟进记录（菜单路径 /customer/follow）
      {
        path: 'follow',
        name: 'CustomerFollow',
        component: () => import('@/views/customer/CustomerFollow.vue'),
        meta: {
          title: '客户跟进记录',
          icon: ''
        }
      }
    ]
  },
  // ============ 销售订单管理 ============
  {
    path: '/sales',
    name: 'SalesIndex',
    component: () => import('@/views/system/SystemIndex.vue'),
    redirect: '/sales/order/entry',
    meta: { title: '销售订单管理', icon: 'Sales' },
    children: [
      { path: 'productBom', name: 'ProductBom', component: () => import('@/views/sales/ProductBom.vue'), meta: { title: '产品BOM配置', icon: '' } },
      // 下单：明细级列表，可新增/编辑
      {
        path: 'order/entry',
        name: 'OrderEntry',
        component: () => import('@/views/sales/OrderItemList.vue'),
        props: { mode: 'entry' },
        meta: { title: '下单', icon: '' }
      },
      // 订单录入/编辑表单（从"下单"/"订单列表"进入，不进菜单）
      { path: 'order/edit', name: 'OrderEdit', component: () => import('@/views/sales/OrderAdd.vue'), meta: { title: '订单录入', icon: '' } },
      // 兼容旧地址
      { path: 'order/add', redirect: '/sales/order/edit' },
      // 订单：订单维度列表（含收付款、状态流转）
      { path: 'order/list', name: 'OrderList', component: () => import('@/views/sales/OrderList.vue'), meta: { title: '订单列表', icon: '' } },
      // 订单明细：明细级列表（只读，含加载统计）
      {
        path: 'order/items',
        name: 'OrderItems',
        component: () => import('@/views/sales/OrderItemList.vue'),
        props: { mode: 'items' },
        meta: { title: '订单明细', icon: '' }
      },
      { path: 'order/track', name: 'OrderTrack', component: () => import('@/views/sales/OrderTrack.vue'), meta: { title: '订单进度跟踪', icon: '' } }
    ]
  },
  // ============ 库存管理 ============
  {
    path: '/stock',
    name: 'StockIndex',
    component: () => import('@/views/system/SystemIndex.vue'),
    redirect: '/stock/material',
    meta: { title: '库存管理', icon: 'Stock' },
    children: [
      { path: 'material', name: 'MaterialList', component: () => import('@/views/stock/MaterialList.vue'), meta: { title: '物料档案', icon: '' } },
      { path: 'shelf', name: 'ShelfList', component: () => import('@/views/stock/ShelfList.vue'), meta: { title: '库位货架管理', icon: '' } },
      { path: 'bill', name: 'BillList', component: () => import('@/views/stock/BillList.vue'), meta: { title: '出入库单据', icon: '' } },
      { path: 'check', name: 'CheckList', component: () => import('@/views/stock/CheckList.vue'), meta: { title: '库存盘点', icon: '' } },
      { path: 'warn', name: 'WarnList', component: () => import('@/views/stock/WarnList.vue'), meta: { title: '库存预警', icon: '' } }
    ]
  },
  // ============ 生产管理 ============
  {
    path: '/production',
    name: 'ProduceIndex',
    component: () => import('@/views/system/SystemIndex.vue'),
    redirect: '/production/workorder',
    meta: { title: '生产管理', icon: 'Production' },
    children: [
      { path: 'process', name: 'ProcessList', component: () => import('@/views/produce/ProcessList.vue'), meta: { title: '工序管理', icon: '' } },
      { path: 'workorder', name: 'WorkOrderList', component: () => import('@/views/produce/WorkOrderList.vue'), meta: { title: '生产工单', icon: '' } },
      { path: 'report', name: 'WorkReportList', component: () => import('@/views/produce/WorkReportList.vue'), meta: { title: '工人报工记录', icon: '' } },
      { path: 'wage', name: 'WageList', component: () => import('@/views/produce/WageList.vue'), meta: { title: '计件工资', icon: '' } }
    ]
  },
  // ============ 尚未开发的模块：统一占位，避免点菜单出现空白页 ============
  ...buildPendingRoutes()
]

function buildPendingRoutes() {
  // 菜单里已存在、但后端/前端均未实现的模块
  const pending: Array<{ path: string; title: string }> = [
    { path: '/xiaolu', title: '首页看板' },
    { path: '/supplier', title: '供应商管理' },
    { path: '/purchase', title: '采购管理' },
    { path: '/finance', title: '财务管理' },
    { path: '/report', title: '报表统计中心' },
    { path: '/mini', title: '小程序配置' }
  ]
  const Layout = () => import('@/views/system/SystemIndex.vue')
  const ComingSoon = () => import('@/views/common/ComingSoon.vue')
  return pending.map(m => ({
    path: m.path,
    component: Layout,
    meta: { title: m.title },
    children: [
      { path: '', component: ComingSoon, meta: { title: m.title } },
      { path: ':pathMatch(.*)*', component: ComingSoon, meta: { title: m.title } }
    ]
  }))
}

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = getToken()
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
