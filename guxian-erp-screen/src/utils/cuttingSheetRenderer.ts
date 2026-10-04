/**
 * 打印模板区块渲染器
 * ------------------------------------------------------------------
 * 【唯一渲染入口】打印模板管理（PrintTemplate.vue）与生产工单打印（WorkOrderList.vue）
 * 共用本文件的 blocks 结构、列取值规则与 HTML 渲染逻辑，保证"模板里配的"和"工单打出来的"完全一致。
 *
 * blocks 结构由打印模板管理拖拽编辑器产生，序列化后存到 t_print_template.template_content；
 * 工单打印时按 templateType = 'WORK_ORDER' + isDefault = 1 读取同一条记录。
 */

export interface PrintBlock {
  id: number
  type: 'text' | 'table' | 'qrcode' | 'line'
  x: number
  y: number
  w: number
  /** 区块高度：编辑时可拖拽调整；undefined 时按内容自适应 */
  h?: number
  label?: string
  field?: string
  fontSize?: number
  bold?: boolean
  /** 文本水平对齐：left/center/right */
  textAlign?: string
  /** 动态表格：打印时按后端数据自动展开列（模板里只需定义示意列） */
  dynamic?: boolean
  cols?: string[]
  colsText?: string
}

export interface RenderCtx {
  /** 顶部字段数据：orderNo/customerName/createTime 等 */
  fields: Record<string, any>
  /** 明细行数据（工单 orderItems） */
  items: any[]
  /** 二维码内容占位 */
  qrWork?: string
  qrCustomer?: string
}

/* ==================================================================
 * 一、表头分组（与车间纸质《固贤纱窗纱门生产单》一致的两级表头）
 *    原尺寸(总宽×总高) | 数量 | 颜色 | 固定 | 下料尺寸(外框/内框/内扇/孔位/横杆)
 *    | 纱网 | 剪网尺寸(上下纱/中纱) | 加杆 | 把手 | 备注
 * ================================================================== */
export const COL_GROUPS: Record<string, string[]> = {
  原尺寸: ['总宽×总高'],
  下料尺寸: ['外框宽×高', '内框宽×高', '内扇宽×高', '孔位', '横杆'],
  剪网尺寸: ['上下纱宽×高', '中纱宽×高']
}

/** 列名 → 所属分组（用于自动生成两级表头） */
export const groupOfCol = (col: string): string | undefined =>
  Object.keys(COL_GROUPS).find(g => COL_GROUPS[g].includes(col))

/** 默认列（打印模板管理里"载入默认生产单"与运行时兜底共用） */
export const DEFAULT_COLS = [
  '总宽×总高', '数量', '颜色', '固定',
  '外框宽×高', '内框宽×高', '内扇宽×高', '孔位', '横杆',
  '纱网', '上下纱宽×高', '中纱宽×高',
  '加杆', '把手', '备注'
]

const X = '×'

/** 解析明细行公式结果（JSON 字符串或已解析对象），无公式返回 null */
function formulaOf(it: any): any {
  const fr = it?.formulaResult
  if (!fr) return null
  if (typeof fr === 'object') return fr
  try { return JSON.parse(fr) } catch { return null }
}

/** 从公式结果取某型材项 {dim,mult}，缺失返回 null */
function matOf(f: any, name: string): any {
  const m = f?.型材?.[name]
  return m && m.dim != null ? m : null
}

/* ==================================================================
 * 二、列取值规则（车间下料口径）
 *    优先读订单明细里的公式结果（下单时后端按产品公式算好存 formula_result）：
 *      型材 → 下料尺寸（外框/内框/内扇/横杆），顶层"孔位" → 孔位列
 *      纱网 → 剪网尺寸（上网/下网 → 上下纱列，中网 → 中纱列）
 *    无公式时回退内置口径，再回退旧字段。
 * ================================================================== */
export const colValue = (col: string, it: any): string => {
  const w = Number(it?.width) || 0
  const h = Number(it?.height) || 0
  const n = Number(it?.num) || 1
  const f = formulaOf(it)
  switch (col) {
    case '原尺寸':
    case '总宽×总高': return w && h ? `${w}${X}${h}` : ''
    case '总宽': return w ? String(w) : ''
    case '总高': return h ? String(h) : ''
    case '数量': return String(n)
    case '颜色': return it?.color || ''
    case '固定': return it?.fixedBottom || ''
    case '外框宽×高': {
      const fw = matOf(f, '外框宽'), fh = matOf(f, '外框高')
      if (fw && fh) return `${fw.dim}${X}${fh.dim}${X}${fw.mult ?? n * 2}`
      const ow = it?.frameOutW, oh = it?.frameOutH
      if (ow && oh) return `${ow}${X}${oh}${X}${n * 2}`
      return w ? `${w}${X}${h + 1}${X}${n * 2}` : ''
    }
    case '内框宽×高': {
      const fw = matOf(f, '内框宽'), fh = matOf(f, '内框高')
      if (fw && fh) return `${fw.dim}${X}${fh.dim}${X}${fw.mult ?? n}`
      const ow = it?.frameInW, oh = it?.frameInH
      if (ow && oh) return `${ow}${X}${oh}${X}${n}`
      return w ? `${Math.round(w - 67)}${X}${h - 20}${X}${n}` : ''
    }
    case '内扇宽×高': {
      const fw = matOf(f, '内扇宽'), fh = matOf(f, '内扇高')
      if (fw && fh) return `${fw.dim}${X}${fh.dim}${X}${fw.mult ?? n}`
      const sw = it?.sashW, sh = it?.sashH
      if (sw && sh) return `${sw}${X}${sh}${X}${n}`
      return h ? `227${X}${h - 73}${X}${n}` : ''
    }
    case '孔位': {
      if (f && f.孔位 != null) return String(f.孔位)
      return it?.holePos || '525'
    }
    case '横杆': {
      const bar = matOf(f, '横杆')
      if (bar) return String(bar.dim)
      return it?.crossBar || ''
    }
    case '纱网': return it?.netMaterial || ''
    case '剪网尺寸':
    case '剪网宽×高×数量':
    case '上下纱宽×高': {
      const net = f?.纱网
      if (net) {
        // 上网/下网前面加"上/下"标识，工人一眼区分两条剪网尺寸（同一列两行）
        const fmtNet = (v: any, tag: string) => (v && v.w != null && v.h != null ? `${tag}${v.w}${X}${v.h}${X}${v.mult ?? n}` : '')
        const parts = [fmtNet(net['上网'], '上'), fmtNet(net['下网'], '下')].filter(Boolean)
        if (parts.length) return parts.join('\n')
      }
      const nw = it?.netCutW, nh = it?.netCutH
      if (nw && nh) return `${nw}${X}${nh}${X}${n}`
      return w ? `${w}${X}${h - 52}${X}${n}` : ''
    }
    case '中纱宽×高': {
      const mid = f?.纱网?.['中网']
      if (mid && mid.w != null && mid.h != null) return `${mid.w}${X}${mid.h}${X}${mid.mult ?? n}`
      return it?.midNet || ''
    }
    case '净宽': return it?.netWidth == null ? '' : String(it.netWidth)
    case '加杆': return it?.addRod || ''
    case '把手': return [it?.handle, it?.handleDirection].filter(Boolean).join(' ')
    case '锁具': return it?.lockSet || ''
    case '开向': return it?.openDirection || ''
    case '产品': return it?.productName || ''
    case '品目': return it?.itemCategory || it?.dictType || ''
    case '子单号': return it?.subOrderNo || ''
    case '备注': return it?.remark || ''
    default: return ''
  }
}

const esc = (s: any) =>
  String(s ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')

/* ==================================================================
 * 三、渲染
 * ================================================================== */
const TH = 'border:1px solid #000;padding:3px 4px;font-size:11px;text-align:center;font-weight:normal;'
const TD = 'border:1px solid #000;padding:3px 4px;font-size:11px;height:22px;white-space:pre-line;'

/** 把列按分组归并（相邻同组才合并） */
function groupCols(cols: string[]): Array<{ title?: string; cols: string[] }> {
  const groups: Array<{ title?: string; cols: string[] }> = []
  cols.forEach(c => {
    const g = groupOfCol(c)
    if (!g) {
      groups.push({ cols: [c] })
      return
    }
    const last = groups[groups.length - 1]
    if (last && last.title === g) last.cols.push(c)
    else groups.push({ title: g, cols: [c] })
  })
  return groups
}

export interface HeadCell {
  text: string
  colspan?: number
  rowspan?: number
}

/**
 * 表头单元格结构（编辑器画布与打印共用，保证"所见即所得"）。
 * 有分组时返回两行：第一行是分组名，未分组的列 rowspan=2；第二行是分组内的子表头。
 */
export function buildTableHeadRows(cols: string[]): HeadCell[][] {
  const groups = groupCols(cols)
  if (!groups.some(g => !!g.title)) {
    return [cols.map(c => ({ text: c }))]
  }
  const row1: HeadCell[] = groups.map(g =>
    g.title ? { text: g.title, colspan: g.cols.length } : { text: g.cols[0], rowspan: 2 }
  )
  const row2: HeadCell[] = groups.filter(g => !!g.title).flatMap(g => g.cols.map(c => ({ text: c })))
  return [row1, row2]
}

/** 数量列下标（合计行把总数放在这一列，与纸质单一致） */
export function totalColIndex(cols: string[]): number {
  const i = cols.indexOf('数量')
  return i < 0 ? cols.length - 1 : i
}

/* ==================================================================
 * 四、销售单动态列（SALES_ORDER 模板专用）
 *    模板里表格只定义一列示意，打印时按此顺序自动展开为多列；
 *    列取值直接读后端订单明细字段。
 * ================================================================== */
export interface SalesColDef {
  key: string
  label: string
  get: (it: any, i: number) => string
}
export const AUTO_SALES_COLS: SalesColDef[] = [
  { key: 'seq', label: '序号', get: (_it, i) => String(i + 1) },
  { key: 'product', label: '品名及规格', get: it => it?.productName || '' },
  { key: 'width', label: '宽', get: it => (it?.width == null ? '' : String(it.width)) },
  { key: 'height', label: '高', get: it => (it?.height == null ? '' : String(it.height)) },
  { key: 'color', label: '颜色', get: it => it?.color || '' },
  { key: 'net', label: '纱网', get: it => it?.netMaterial || '' },
  { key: 'fixed', label: '下固', get: it => it?.fixedBottom ?? '' },
  { key: 'area', label: '面积', get: it => it?.singleArea ?? it?.itemTotalArea ?? '' },
  { key: 'num', label: '数量', get: it => (it?.num == null ? '' : String(it.num)) },
  { key: 'price', label: '单价', get: it => it?.unitPrice ?? '' },
  { key: 'amount', label: '金额', get: it => it?.lineAmount ?? it?.amount ?? '' },
  { key: 'handle', label: '把手', get: it => it?.handle || '' },
  { key: 'remark', label: '备注', get: it => it?.remark || '' }
]

/** 动态表格 HTML：表头 + 数据行 + 合计行（数量/金额汇总） */
export function buildDynamicTable(items: any[]): string {
  const cols = AUTO_SALES_COLS
  const head = '<tr>' + cols.map(c => `<th style="${TH}">${esc(c.label)}</th>`).join('') + '</tr>'
  const body = items.map((it, i) =>
    '<tr>' + cols.map(c => `<td style="${TD}">${esc(c.get(it, i))}</td>`).join('') + '</tr>'
  ).join('')
  const totalNum = items.reduce((s, it) => s + (Number(it?.num) || 0), 0)
  const totalAmount = items.reduce((s, it) => s + (Number(it?.lineAmount ?? it?.amount) || 0), 0)
  const foot = '<tr>' + cols.map(c => {
    if (c.key === 'seq') return `<td style="${TD}text-align:left;font-weight:bold;">合计：</td>`
    if (c.key === 'num') return `<td style="${TD}text-align:center;font-weight:bold;">${totalNum}</td>`
    if (c.key === 'amount') return `<td style="${TD}text-align:center;font-weight:bold;">${totalAmount}</td>`
    return `<td style="${TD}"></td>`
  }).join('') + '</tr>'
  return `<thead>${head}</thead><tbody>${body}</tbody><tfoot>${foot}</tfoot>`
}

/** 生成表头 HTML：有分组时输出两级表头，无分组时单级 */
function buildTableHead(cols: string[]): string {
  return buildTableHeadRows(cols).map(row =>
    '<tr>' + row.map(cell =>
      `<th style="${TH}"${cell.colspan ? ` colspan="${cell.colspan}"` : ''}${cell.rowspan ? ` rowspan="${cell.rowspan}"` : ''}>${esc(cell.text)}</th>`
    ).join('') + '</tr>'
  ).join('')
}

/** 合计行：与纸质单一致 —— 第一格"合计："，数量列的格子里放总数，其余留空 */
function buildTableFoot(cols: string[], totalNum: number): string {
  const numIdx = totalColIndex(cols)
  const cells = cols.map((_, i) => {
    if (i === 0) return `<td style="${TD}text-align:left;">合计：</td>`
    if (i === numIdx) return `<td style="${TD}text-align:center;font-weight:bold;">${totalNum}</td>`
    return `<td style="${TD}"></td>`
  }).join('')
  return `<tr>${cells}</tr>`
}

/** 把 blocks 数组渲染成 A4 横向 HTML 片段 */
export function renderBlocksToHtml(blocks: PrintBlock[], ctx: RenderCtx): string {
  const items = ctx.items || []
  const totalNum = items.reduce((s, it) => s + (Number(it?.num) || 0), 0)
  const parts = (blocks || []).map(b => {
    const pos = `position:absolute;left:${b.x}px;top:${b.y}px;width:${b.w}px;`
    const hStyle = b.h ? `height:${b.h}px;` : ''
    if (b.type === 'text') {
      const val = b.field ? (ctx.fields?.[b.field] ?? '') : ''
      const align = b.textAlign || 'left'
      return `<div style="${pos}${hStyle}font-size:${b.fontSize || 13}px;font-weight:${b.bold ? 'bold' : 'normal'};text-align:${align};white-space:nowrap;overflow:hidden;">${esc(b.label || '')}${b.field ? '<b>' + esc(val) + '</b>' : ''}</div>`
    }
    if (b.type === 'line') {
      return `<div style="${pos}${hStyle}border-top:1px solid #000;margin-top:4px;"></div>`
    }
    if (b.type === 'qrcode') {
      const qh = b.h || b.w
      return `<div style="${pos}height:${qh}px;text-align:center;">
        <div style="width:100%;height:${qh}px;border:1px solid #000;display:flex;align-items:center;justify-content:center;font-size:10px;color:#666;">${esc(b.label || '二维码')}</div>
      </div>`
    }
    if (b.type === 'table') {
      // 动态表格：按后端数据自动展开列（销售单模板用）
      if (b.dynamic) {
        return `<table style="${pos}${hStyle}border-collapse:collapse;width:100%;">${buildDynamicTable(items)}</table>`
      }
      const cols = (b.cols && b.cols.length ? b.cols : DEFAULT_COLS)
      const body = items.map(it =>
        '<tr>' + cols.map(c => `<td style="${TD}">${esc(colValue(c, it))}</td>`).join('') + '</tr>'
      ).join('')
      return `<table style="${pos}${hStyle}border-collapse:collapse;width:100%;">
        <thead>${buildTableHead(cols)}</thead>
        <tbody>${body}</tbody>
        <tfoot>${buildTableFoot(cols, totalNum)}</tfoot>
      </table>`
    }
    return ''
  }).join('')
  return `<div style="position:relative;width:1080px;min-height:620px;font-family:'SimSun','Microsoft YaHei',sans-serif;color:#000;">${parts}</div>`
}

/** 打印用纸样式（A4 横向，与纸质生产单一致） */
export const PRINT_PAGE_CSS = '@page{size:A4 landscape;margin:6mm;}body{margin:0;padding:0;}'

/** 把渲染片段包成可直接打印的完整 HTML 文档 */
export function wrapPrintDocument(bodyHtml: string, title = '生产单'): string {
  return `<!DOCTYPE html><html><head><meta charset="UTF-8"><title>${esc(title)}</title>
<style>${PRINT_PAGE_CSS}</style></head><body>${bodyHtml}</body></html>`
}

/* ==================================================================
 * 四、内置默认版式（与照片《固贤纱窗纱门生产单》一致）
 *    打印模板管理"载入默认生产单" 与 运行时读不到模板时的兜底，都用它
 * ================================================================== */
export function buildDefaultBlocks(): PrintBlock[] {
  return [
    { id: 1, type: 'qrcode', x: 15, y: 10, w: 76, label: '设备扫码' },
    { id: 2, type: 'text', x: 15, y: 90, w: 100, fontSize: 10, label: '打印二维码' },
    { id: 3, type: 'text', x: 230, y: 14, w: 620, fontSize: 19, bold: true, label: '固贤纱窗纱门生产单', field: 'specTitle' },
    { id: 4, type: 'qrcode', x: 992, y: 10, w: 76, label: '客户二维码' },
    { id: 5, type: 'text', x: 110, y: 104, w: 260, fontSize: 12, label: '客户名称：', field: 'customerName' },
    { id: 6, type: 'text', x: 110, y: 126, w: 260, fontSize: 12, label: '客户地址：', field: 'customerAddress' },
    { id: 7, type: 'text', x: 420, y: 104, w: 280, fontSize: 12, label: '订单编号：', field: 'orderNo' },
    { id: 8, type: 'text', x: 420, y: 126, w: 280, fontSize: 12, label: '工程地址：', field: 'projectAddress' },
    { id: 9, type: 'text', x: 720, y: 104, w: 300, fontSize: 12, label: '交货日期：', field: 'deliverDate' },
    { id: 10, type: 'text', x: 720, y: 126, w: 300, fontSize: 12, label: '产品名称：', field: 'productName' },
    { id: 11, type: 'line', x: 15, y: 150, w: 1050 },
    { id: 12, type: 'table', x: 15, y: 162, w: 1050, cols: [...DEFAULT_COLS], colsText: DEFAULT_COLS.join(',') }
  ]
}

/** 销售单默认版式（SALES_ORDER 模板）：与《固贤纱窗纱门销售单》纸质单一致 */
export function buildSalesDefaultBlocks(): PrintBlock[] {
  return [
    { id: 1, type: 'text', x: 220, y: 12, w: 640, h: 40, fontSize: 22, bold: true, textAlign: 'center', label: '固贤纱窗纱门销售单' },
    { id: 2, type: 'text', x: 60, y: 66, w: 200, h: 24, fontSize: 13, label: '客户：', field: 'customerName' },
    { id: 3, type: 'text', x: 300, y: 66, w: 240, h: 24, fontSize: 13, label: '电话：', field: 'customerPhone' },
    { id: 4, type: 'qrcode', x: 880, y: 56, w: 90, h: 90, label: '付款码' },
    { id: 5, type: 'text', x: 60, y: 100, w: 220, h: 24, fontSize: 13, label: '下单日期：', field: 'createTime' },
    { id: 6, type: 'text', x: 300, y: 100, w: 300, h: 24, fontSize: 13, label: '单号：', field: 'orderNo' },
    { id: 7, type: 'text', x: 60, y: 134, w: 560, h: 24, fontSize: 13, label: '终端地址：', field: 'terminalAddress' },
    { id: 8, type: 'line', x: 20, y: 160, w: 1040, h: 1 },
    { id: 9, type: 'table', x: 20, y: 168, w: 1040, h: 300, dynamic: true, cols: ['序号'], colsText: '序号' },
    { id: 10, type: 'text', x: 60, y: 486, w: 340, h: 24, fontSize: 13, label: '金额(大写)：', field: 'amountInWords' },
    { id: 11, type: 'text', x: 420, y: 486, w: 140, h: 24, fontSize: 13, label: '定金：', field: 'deposit' },
    { id: 12, type: 'text', x: 560, y: 486, w: 160, h: 24, fontSize: 13, label: '尾款：', field: 'balance' },
    { id: 13, type: 'text', x: 60, y: 520, w: 520, h: 24, fontSize: 13, label: '备注：请仔细核对尺寸，确认无误后下单！' },
    { id: 14, type: 'text', x: 60, y: 548, w: 460, h: 24, fontSize: 13, label: '一律全款下单，概不赊账，敬请理解！' },
    { id: 15, type: 'text', x: 560, y: 548, w: 240, h: 24, fontSize: 13, label: '利润微薄，谢绝抹零！' },
    { id: 16, type: 'text', x: 60, y: 576, w: 700, h: 24, fontSize: 13, label: '防护栏统一间距14公分内，宽度19公分内，如有特殊要求请提前告知' },
    { id: 17, type: 'qrcode', x: 830, y: 540, w: 90, h: 90, label: '付款码' }
  ]
}

/* ==================================================================
 * 五、工单打印上下文 + 模板加载（打印模板管理与工单打印共用同一数据源）
 * ================================================================== */

/** 由工单/销售单详情装配打印上下文（字段名与打印模板管理里的"字段变量"一一对应） */
export function buildPrintContext(detail: any): RenderCtx {
  const items: any[] = detail?.orderItems || []
  const first = items[0] || {}
  const productName = first.productName || ''
  const seriesName = first.itemCategory || first.dictType || ''
  const day = (v: any) => (v ? String(v).slice(0, 10) : '')
  return {
    fields: {
      workNo: detail?.workNo || '',
      orderNo: detail?.orderNo || '',
      customerName: detail?.customerName || '',
      customerPhone: detail?.customerPhone || '',
      customerAddress: detail?.customerAddress || '',
      projectAddress: detail?.customerAddress || '',
      /** 销售单：终端地址 */
      terminalAddress: detail?.terminalAddress || '',
      productName,
      seriesName,
      /** 标题后缀：-产品名，形成"固贤纱窗纱门生产单-8K大料三节" */
      specTitle: productName ? `-${productName}` : '',
      deliverDate: day(detail?.expectDate),
      orderDate: day(detail?.orderDate),
      createTime: day(detail?.createTime),
      finishTime: day(detail?.finishTime),
      totalNum: items.reduce((s, it) => s + (Number(it?.num) || 0), 0),
      totalArea: detail?.totalArea ?? '',
      shelfName: detail?.shelfName || '',
      /** 销售单：金额相关 */
      totalAmount: detail?.totalAmount ?? '',
      amountInWords: detail?.amountInWords ?? '',
      deposit: detail?.deposit ?? '',
      balance: detail?.balance ?? ''
    },
    items,
    qrWork: detail?.workNo || '',
    qrCustomer: detail?.orderNo || ''
  }
}

/** 旧版 HTML 模板兼容：替换 ${field} 占位符 */
function injectFields(html: string, ctx: RenderCtx): string {
  return html.replace(/\$\{(\w+)\}/g, (_m, key) => esc(ctx.fields?.[key] ?? ''))
}

/**
 * 读取工单打印模板：
 *  1) 打印模板管理中 templateType = WORK_ORDER 且 status = 1 的模板；
 *  2) 优先 isDefault = 1，其次取最新启用的一条；
 *  3) 读不到或内容损坏时回退到内置默认版式 buildDefaultBlocks()。
 */
export async function loadWorkOrderTemplate(): Promise<{ blocks?: PrintBlock[]; rawHtml?: string }> {
  try {
    const api: any = await import('@/api/system/printTemplate')
    const res: any = await api.getPrintTemplatePage(1, 100, '', 'WORK_ORDER', 1)
    const list: any[] = res?.data?.records || []
    if (!list.length) return { blocks: buildDefaultBlocks() }
    const tpl = list.find((t: any) => t.isDefault === 1) || list[0]
    const content = String(tpl?.templateContent || '').trim()
    if (!content) return { blocks: buildDefaultBlocks() }
    // 历史遗留的整段 HTML 模板：直接使用，仅做字段替换
    if (content.startsWith('<')) return { rawHtml: content }
    const parsed = JSON.parse(content)
    if (Array.isArray(parsed) && parsed.length) return { blocks: parsed as PrintBlock[] }
  } catch (e) {
    console.warn('[打印] 读取工单打印模板失败，使用内置默认版式', e)
  }
  return { blocks: buildDefaultBlocks() }
}

/**
 * 【工单打印唯一入口】按工单详情渲染出可打印的完整 HTML。
 * 生产工单打印必须走这里，才能保证与"打印模板管理"里配置的模板一致。
 */
export async function renderWorkOrderHtml(detail: any, title = '生产单'): Promise<string> {
  const ctx = buildPrintContext(detail)
  const tpl = await loadWorkOrderTemplate()
  const body = tpl.rawHtml ? injectFields(tpl.rawHtml, ctx) : renderBlocksToHtml(tpl.blocks!, ctx)
  return wrapPrintDocument(body, title)
}
