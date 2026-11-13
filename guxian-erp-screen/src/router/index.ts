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
        component: () => import('@/views/system/CsHome.vue'),
        meta: {
          cacheName: 'CsHome', title: '首页看板' }
      },
      {
        path: 'user',
        name: 'UserManage',
        component: () => import('@/views/system/UserManage.vue'),
        meta: {
          cacheName: 'UserManage',
          title: '用户管理',
          icon: 'User'
        }
      },
      {
        path: 'role',
        name: 'RoleManage',
        component: () => import('@/views/system/RoleManage.vue'),
        meta: {
          cacheName: 'RoleManage',
          title: '角色管理',
          icon: 'Role'
        },
      },
            {
        path: 'menu',
        name: 'MenuManage',
        component: () => import('@/views/system/MenuManage.vue'),
        meta: {
          cacheName: 'MenuManage',
          title: '菜单管理',
          icon: 'Menu'
        },
      },
      {
        path: 'dict',
        name: 'DictTypeData',
        component: () => import('@/views/system/DictTypeData.vue'),
        meta: {
          cacheName: 'DictTypeData', 
          title: '字典管理' ,
          icon: 'Dice'
        }
      },
      {
        path: 'printTemplate',
        name: 'PrintTemplate',
        component: () => import('@/views/system/PrintTemplate.vue'),
        meta: {
          cacheName: 'PrintTemplate', 
          title: '打印模板' ,
          icon: 'PrintTemplate'
        }
      },
      {
        path: 'operLog',
        name: 'OperLog',
        component: () => import('@/views/system/OperLog.vue'),
        meta: {
          cacheName: 'OperLog', 
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
          cacheName: 'Customer',
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
          cacheName: 'CustomerFollow',
          title: '客户跟进',
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
      // 下单：明细级列表，可新增/编辑
      {
        path: 'order/entry',
        name: 'OrderEntry',
        component: () => import('@/views/sales/OrderItemList.vue'),
        props: { mode: 'entry' },
        meta: {
          cacheName: 'OrderItemList', title: '新建订单', icon: '' }
      },
      // 订单录入/编辑表单（从"下单"/"订单列表"进入，不进菜单）
      { path: 'order/edit', name: 'OrderEdit', component: () => import('@/views/sales/OrderAdd.vue'), meta: { title: '订单录入', icon: '' } },
      // 兼容旧地址
      { path: 'order/add', redirect: '/sales/order/edit' },
      // 订单：订单维度列表（含收付款、状态流转）
      { path: 'order/list', name: 'OrderList', component: () => import('@/views/sales/OrderList.vue'), meta: {
          cacheName: 'OrderList', title: '订单列表', icon: '' } },
      // 订单明细：明细级列表（只读，含加载统计）
      {
        path: 'order/items',
        name: 'OrderItems',
        component: () => import('@/views/sales/OrderItemList.vue'),
        props: { mode: 'items' },
        meta: {
          cacheName: 'OrderItemList', title: '订单明细', icon: '' }
      },
      { path: 'order/track', name: 'OrderTrack', component: () => import('@/views/sales/OrderTrack.vue'), meta: {
          cacheName: 'OrderTrack', title: '进度跟踪', icon: '' } }
    ]
  },
  // ============ 库存管理 ============
  {
    path: '/stock',
    name: 'StockIndex',
    component: () => import('@/views/system/SystemIndex.vue'),
    redirect: '/stock/shelf',
    meta: { title: '库存管理', icon: 'Stock' },
    children: [
      { path: 'shelf', name: 'ShelfList', component: () => import('@/views/stock/ShelfList.vue'), meta: {
          cacheName: 'ShelfList', title: '库位货架', icon: '' } },
      { path: 'instock', name: 'InstockList', component: () => import('@/views/stock/InstockList.vue'), meta: {
          cacheName: 'InstockList', title: '成品入库', icon: '' } }
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
      { path: 'process', name: 'ProcessList', component: () => import('@/views/produce/ProcessList.vue'), meta: {
          cacheName: 'ProcessList', title: '工序管理', icon: '' } },
      { path: 'price', name: 'ProductPrice', component: () => import('@/views/produce/ProductPrice.vue'), meta: {
          cacheName: 'ProductPrice', title: '产品计件价', icon: '' } },
      { path: 'workorder', name: 'WorkOrderList', component: () => import('@/views/produce/WorkOrderList.vue'), meta: {
          cacheName: 'WorkOrderList', title: '生产工单', icon: '' } },
      { path: 'report', name: 'WorkReportList', component: () => import('@/views/produce/WorkReportList.vue'), meta: {
          cacheName: 'WorkReportList', title: '报工记录', icon: '' } },
    ]
  },
  // ============ 财务管理 ============
  {
    path: '/finance',
    name: 'FinanceIndex',
    component: () => import('@/views/system/SystemIndex.vue'),
    redirect: '/finance/receivable',
    meta: { title: '财务管理', icon: 'Finance' },
    children: [
      { path: 'receivable', name: 'FinanceReceivable', component: () => import('@/views/finance/ReceivableList.vue'), meta: {
          cacheName: 'ReceivableList', title: '成品应收', icon: '' } },
      { path: 'merge', name: 'FinanceMerge', component: () => import('@/views/finance/MergeCollect.vue'), meta: {
          cacheName: 'MergeCollect', title: '财务合并', icon: '' } },
      { path: 'customer', name: 'FinanceCustomer', component: () => import('@/views/finance/CustomerFinanceList.vue'), meta: {
          cacheName: 'CustomerFinanceList', title: '客户管理', icon: '' } },
      { path: 'record', name: 'FinanceRecord', component: () => import('@/views/finance/CollectionRecordList.vue'), meta: {
          cacheName: 'CollectionRecordList', title: '收款明细', icon: '' } },
      { path: 'account', name: 'FinanceAccount', component: () => import('@/views/finance/CollectionAccountList.vue'), meta: {
          cacheName: 'CollectionAccountList', title: '收款账户', icon: '' } },
      { path: 'wage', name: 'FinanceWage', component: () => import('@/views/produce/WageList.vue'), meta: {
          cacheName: 'WageList', title: '计件工资', icon: '' } }
    ]
  },
  // ============ 报表统计中心（8088） ============
  {
    path: '/report',
    name: 'ReportIndex',
    component: () => import('@/views/system/SystemIndex.vue'),
    meta: { title: '报表统计中心' },
    children: [
      { path: 'sales', name: 'SalesReport', component: () => import('@/views/report/SalesReport.vue'), meta: {
          cacheName: 'SalesReport', title: '销售报表', icon: '' } },
      { path: 'production', name: 'ProductionReport', component: () => import('@/views/report/ProductionReport.vue'), meta: {
          cacheName: 'ProductionReport', title: '生产报表', icon: '' } },
      { path: 'stock', name: 'StockReport', component: () => import('@/views/report/StockReport.vue'), meta: {
          cacheName: 'StockReport', title: '库存报表', icon: '' } },
      { path: 'profit', name: 'ProfitReport', component: () => import('@/views/report/ProfitReport.vue'), meta: {
          cacheName: 'ProfitReport', title: '利润报表', icon: '' } }
    ]
  },
  // ============ 小程序配置 ============
  {
    path: '/mini',
    name: 'MiniIndex',
    component: () => import('@/views/system/SystemIndex.vue'),
    meta: { title: '小程序配置' },
    children: [
      { path: 'wechat', name: 'LuOfficial', component: () => import('@/views/games/Official.vue'), meta: {
          title: '小卢官方', icon: '' } },
      { path: 'games/snake', name: 'SnakeGame', component: () => import('@/views/games/Snake.vue'), meta: {
          title: '贪吃蛇', icon: '' } },
      { path: 'games/tetris', name: 'TetrisGame', component: () => import('@/views/games/Tetris.vue'), meta: {
          title: '俄罗斯方块', icon: '' } },
      { path: 'games/gomoku', name: 'GomokuGame', component: () => import('@/views/games/Gomoku.vue'), meta: {
          title: '五子棋', icon: '' } },
      { path: 'games/chess', name: 'ChessGame', component: () => import('@/views/games/ChineseChess.vue'), meta: {
          title: '中国象棋', icon: '' } },
      // 系统设置：字号/密度/侧栏/斑马纹/打印留白（放在通配路由之前，否则会被兜底页吃掉）
      { path: 'settings', name: 'GxSettings', component: () => import('@/views/mini/Settings.vue'), meta: {
          title: '系统设置', icon: '' } },
      { path: ':pathMatch(.*)*', component: () => import('@/views/common/ComingSoon.vue'), meta: { title: '小程序配置' } }
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
    { path: '/purchase', title: '采购管理' }
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
