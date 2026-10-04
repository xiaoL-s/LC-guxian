// ===== 库存模块类型 =====

export interface Shelf {
  shelfId?: number
  shelfCode?: string
  shelfName: string
  status: number
  sort?: number
}

// ===== 生产模块类型 =====

export interface WorkOrder {
  workId: number
  workNo: string
  salesOrderId: number
  customerId?: number
  shelfId?: number
  totalArea: number
  workStatus: string
  workStatusDesc?: string
  isRework?: number
  remark?: string
  finishTime?: string
  createTime?: string
}

export const WORK_STATUS = [
  { value: 'WAIT_PROCESS', label: '待生产' },
  { value: 'PROCESSING', label: '生产中' },
  { value: 'FINISHED', label: '已完工' }
]

export interface ProcessDict {
  processId?: number
  processCode: string
  processName: string
  processSort: number
  postType?: string
  unitPrice: number
  status: number
}

export interface WorkReportRow {
  id?: number
  workOrderId: number
  workNo?: string
  processId: number
  processName?: string
  workerId: number
  qualifiedNum: number
  badNum: number
  unitPrice: number
  pieceWage: number
  reportTime?: string
}

export interface ProductInstock {
  id?: number
  workOrderId: number
  workNo?: string
  orderId: number
  orderNo?: string
  shelfId: number
  shelfName?: string
  inNum: number
  instockStatus: number
  instockTime?: string
  remark?: string
}

/** 生产工单明细（对应 t_work_order_item，开工时从订单明细复制） */
export interface WorkOrderItem {
  id?: number
  workOrderId: number
  salesOrderItemId?: number
  subOrderNo?: string
  productId?: number
  productName?: string
  itemCategory?: string
  dictType?: string

  color?: string
  netMaterial?: string
  handle?: string
  handleDirection?: string
  fixedBottom?: string
  remark?: string

  /** 客户下单总宽/总高 mm */
  width?: number
  height?: number
  num?: number

  // ============ 下料/剪网工艺尺寸(mm)：开工时从订单明细复制 ============
  frameOutW?: number   // 外框下料宽
  frameOutH?: number   // 外框下料高
  frameInW?: number    // 内框下料宽
  frameInH?: number    // 内框下料高
  sashW?: number       // 内扇下料宽
  sashH?: number       // 内扇下料高
  netCutW?: number     // 剪网宽
  netCutH?: number     // 剪网高
  // ======================================================================
}
