// 产品档案
export interface TProduct {
  productId?: number
  productCode: string
  productName: string
  productType?: string
  spec?: string
  unit?: string
  unitPrice?: number
  priceType?: number // 1按面积 2按件
  minArea?: number
  defaultColor?: string
  defaultMaterial?: string
  openDirection?: string
  status?: number
  remark?: string
  createTime?: string
}

// 字典下拉选项（产品来自字典管理 style_* 系列）
export interface DictOption {
  dictType: string
  typeName?: string
  value: string
  label: string
  sort?: number
  /** 产品公式配置（含产品属性 vars 数据列表/默认值），仅产品字典项携带 */
  formulaConfig?: string
}

/** 字典选项集合：product 为产品系列汇总，其余为属性字典（可能为空数组） */
export interface DictOptions {
  product: DictOption[]
  color: DictOption[]
  net: DictOption[]
  material: DictOption[]
  handle: DictOption[]
  lock: DictOption[]
  handle_direction: DictOption[]
  add_rod: DictOption[]
  fixed_bottom: DictOption[]
  square_board: DictOption[]
  open_direction: DictOption[]
  order_type: DictOption[]
  install_type: DictOption[]
  customer_source: DictOption[]
  logistics: DictOption[]
  unit: DictOption[]
  [key: string]: DictOption[]
}

// 订单明细（产品行/子单）
export interface SalesOrderItem {
  itemId?: number
  orderId?: number
  /** 子单号，后端生成 */
  subOrderNo?: string
  productId?: number
  productName?: string
  productType?: string
  /** 品目：字典系列名称 */
  itemCategory?: string
  /** 产品字典类型 style_xxx */
  dictType?: string

  color?: string
  material?: string
  openDirection?: string
  netMaterial?: string
  handle?: string
  lockSet?: string
  handleDirection?: string
  addRod?: string
  fixedBottom?: string
  squareBoard?: string

  unitPrice?: number
  /** 总宽 mm */
  width?: number
  /** 总高 mm */
  height?: number
  deductWidth?: number
  netWidth?: number

  singleArea?: number
  minArea?: number
  chargeArea?: number
  calcType?: number

  num?: number
  unit?: string
  itemTotalArea?: number
  lineAmount?: number

  itemStatus?: number
  itemStatusDesc?: string
  salePriceType?: number
  freight?: number
  receiveAmount?: number
  profitAmount?: number
  salesOwner?: string

  /** 生产预留 */
  processCode?: string
  processName?: string
  flowStatus?: string
  produceProgress?: number
  workOrderId?: number

  // ============ 下料/剪网工艺尺寸(mm)：下单时按产品工艺参数算好存库 ============
  frameOutW?: number   // 外框下料宽
  frameOutH?: number   // 外框下料高
  frameInW?: number    // 内框下料宽
  frameInH?: number    // 内框下料高
  sashW?: number       // 内扇下料宽
  sashH?: number       // 内扇下料高
  netCutW?: number     // 剪网宽
  netCutH?: number     // 剪网高
  // ======================================================================

  remark?: string
}

// 销售订单
export interface SalesOrder {
  orderId?: number
  orderNo?: string
  orderStatus?: number
  orderStatusDesc?: string
  orderType?: string

  customerId?: number
  customerName?: string
  contact?: string
  phone?: string
  address?: string

  terminalAddress?: string
  logistics?: string
  unit?: string
  brand?: string
  installType?: string
  designer?: string
  splitter?: string
  salesman?: string
  customerSource?: string

  orderDate?: string
  expectDate?: string
  totalArea?: number
  projectionArea?: number
  bigBoardNum?: number
  productAmount?: number
  craftFee?: number
  urgentFee?: number
  freight?: number
  discountAmount?: number
  totalAmount?: number

  financeStatus?: number
  financeStatusDesc?: string
  paidAmount?: number
  receiveAmount?: number
  unpaidAmount?: number
  profitAmount?: number
  grossProfitRate?: number
  subOrderCount?: number
  receiveTime?: string

  remark?: string
  auditBy?: number
  auditTime?: string
  rejectReason?: string
  deliveryTime?: string
  finishTime?: string
  cancelReason?: string
  createTime?: string
  createBy?: number
  itemList?: SalesOrderItem[]
}

/** 订单明细行列表（含订单头冗余字段），下单/订单明细页面用 */
export interface SalesOrderItemRow extends SalesOrderItem {
  orderNo?: string
  orderType?: string
  orderStatus?: number
  orderStatusDesc?: string
  orderStatusTag?: string
  financeStatus?: number
  financeStatusDesc?: string
  financeStatusTag?: string

  customerId?: number
  customerName?: string
  contact?: string
  phone?: string
  terminalAddress?: string
  logistics?: string
  brand?: string
  installType?: string
  designer?: string
  splitter?: string
  salesman?: string
  customerSource?: string

  orderDate?: string
  expectDate?: string
  orderCreateTime?: string
  totalAmount?: number
  unpaidAmount?: number
  orderFreight?: number

  grossProfitRate?: number
}

/** 订单明细底部加载统计 */
export interface SalesOrderStats {
  totalNum: number
  totalArea: number
  totalAmount: number
  totalUnpaid: number
  totalReceive: number
  totalRows: number
}

// 订单状态枚举（与后端 OrderStatusEnum 对齐）
export const ORDER_STATUS = [
  { code: 0, label: '订单未受理', type: 'warning' },
  { code: 1, label: '订单已受理', type: 'primary' },
  { code: 2, label: '生产中', type: 'primary' },
  { code: 3, label: '已完工', type: 'success' },
  { code: 4, label: '已发货', type: 'success' },
  { code: 5, label: '已完成', type: 'info' },
  { code: 6, label: '已驳回', type: 'danger' },
  { code: 7, label: '已取消', type: 'info' }
]

export function statusTag(code?: number) {
  return ORDER_STATUS.find(s => s.code === code) || { label: '未知', type: 'info' }
}

// 财务状态枚举（与后端 FinanceStatusEnum 对齐）
export const FINANCE_STATUS = [
  { code: 0, label: '未收账', type: 'danger' },
  { code: 1, label: '部分已收账', type: 'warning' },
  { code: 2, label: '已收账', type: 'success' },
  { code: 3, label: '已结清', type: 'info' }
]

export function financeTag(code?: number) {
  return FINANCE_STATUS.find(s => s.code === code) || { label: '未收账', type: 'danger' }
}

/** 订单类型默认选项（字典中存在 order_type 时以字典为准） */
export const ORDER_TYPES = ['正常单', '加急单', '经销商单', '样品单']

/** 安装方式默认选项 */
export const INSTALL_TYPES = ['客户安装', '上门安装', '自行安装', '不需安装']

// 产品分类
export const PRODUCT_TYPES = ['平开纱窗', '推拉纱窗', '金刚网纱窗', '折叠纱窗']
