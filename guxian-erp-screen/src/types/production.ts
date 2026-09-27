// ===== 库存模块类型 =====

export interface Material {
  id?: number
  materialCode: string
  materialName: string
  materialType: number
  spec?: string
  unit: string
  stockNum: number
  warnNum: number
  shelfId?: number
  enable: number
  createTime?: string
}

export const MATERIAL_TYPES = [
  { value: 1, label: '型材' },
  { value: 2, label: '纱网' },
  { value: 3, label: '五金配件' },
  { value: 4, label: '辅材' }
]

export interface StockRecord {
  id?: number
  materialId: number
  materialCode: string
  materialName: string
  bizType: number
  inNum: number
  outNum: number
  afterNum: number
  relateType?: string
  relateNo?: string
  remark?: string
  createTime?: string
}

export const STOCK_BIZ_TYPES = [
  { value: 1, label: '期初入库' },
  { value: 2, label: '采购入库' },
  { value: 3, label: '生产领料' },
  { value: 4, label: '盘点调整' },
  { value: 5, label: '退货出库' }
]

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
