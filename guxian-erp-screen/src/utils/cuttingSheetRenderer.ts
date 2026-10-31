/**
 * 打印模板区块渲染器（生产单 / 销售单共用）
 * ------------------------------------------------------------------
 * 【唯一渲染入口】
 *   · 生产工单打印：renderWorkOrderHtml / renderWorkOrdersHtml
 *   · 销售订单打印：renderSalesOrderHtml
 *   · 打印模板管理预览：renderBlocksToHtml
 * blocks 由打印模板管理拖拽编辑器产生，存 t_print_template.template_content；
 * 打印时按 templateType + isDefault=1 读取同一条记录。
 *
 * 车间可读性约定（28-50 岁工人）：
 *   · 正文 15px、表头 15px 加粗、标题 24px；边框 2px；行高 34px
 *   · 缺失值统一显示「—」，整列为空时自动隐藏该列（单子上不出现空列/空格）
 *   · 列宽按权重固定，数字居中、产品/备注左对齐，长文本换行不溢出
 *   · 一个订单多个产品：默认每页 11 行，超出自动分页并重复表头 + 页码
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
  /** 二维码内容类型：work=工单扫码报工 / customer=客户订单码 */
  qrType?: 'work' | 'customer'
  /** 动态表格：打印时按后端数据自动展开列（模板里只需定义示意列） */
  dynamic?: boolean
  /** 是否隐藏"整列为空"的列：默认 false（照常打印空列，单元格显示占位符） */
  hideEmptyCols?: boolean
  cols?: string[]
  colsText?: string
}

export interface RenderCtx {
  fields: Record<string, any>
  items: any[]
  qrWork?: string
  qrCustomer?: string
  qrWorkData?: string
  qrCustomerData?: string
  workId?: number | string
}

/** 分页信息（由 renderPrintBody 注入） */
export interface PageInfo {
  index: number
  total: number
}

/* ==================================================================
 * 一、表头分组（与车间纸质《固贤纱窗纱门生产单》一致的两级表头）
 * ================================================================== */
export const COL_GROUPS: Record<string, string[]> = {
  原尺寸: ['总宽×总高'],
  下料尺寸: ['外框宽×高', '内框宽×高', '内扇宽×高', '孔位', '横杆'],
  剪网尺寸: ['上下纱宽×高', '中纱宽×高']
}

export const groupOfCol = (col: string): string | undefined =>
  Object.keys(COL_GROUPS).find(g => COL_GROUPS[g].includes(col))

/** 默认列：产品放最前，多产品订单一眼能对上号 */
export const DEFAULT_COLS = [
  '产品', '总宽×总高', '数量', '颜色', '固定',
  '外框宽×高', '内框宽×高', '内扇宽×高', '孔位', '横杆',
  '纱网', '上下纱宽×高', '中纱宽×高',
  '加杆', '把手', '备注'
]

const X = '×'
/** 缺失值统一占位：单子上不出现空白 */
export const NA = '—'

/* ==================================================================
 * 二、列取值规则（优先读订单明细的公式结果，无公式回退旧字段）
 * ================================================================== */
function getFormulaResult(item: any) {
  const fr = item?.formulaResult
  if (!fr) return null
  try {
    return typeof fr === 'object' ? fr : JSON.parse(fr)
  } catch {
    return null
  }
}

function getMatValue(formula: any, matName: string) {
  const mat = formula?.型材?.[matName]
  return mat?.dim != null ? mat : null
}

function fmtDim(dimW: number, dimH: number, mult?: number) {
  if (!dimW && !dimH) return ''
  const base = `${dimW}${X}${dimH}`
  return mult ? `${base}${X}${mult}` : base
}

function fmtNet(netItem: any, tag = '', num = 1) {
  if (!netItem?.w || netItem?.h == null) return ''
  return `${tag}${netItem.w}${X}${netItem.h}${X}${netItem.mult ?? num}`
}

const esc = (s: any) => String(s ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')

export const colValue = (col: string, it: any): string => {
  const w = Number(it?.width) || 0
  const h = Number(it?.height) || 0
  const n = Number(it?.num) || 1
  const f = getFormulaResult(it)

  const map: Record<string, () => string> = {
    '产品': () => it?.productName || '',
    '品目': () => it?.itemCategory || it?.dictType || '',
    '子单号': () => it?.subOrderNo || '',
    '总宽×总高': () => (w && h) ? `${w}${X}${h}` : '',
    '总宽': () => (w ? String(w) : ''),
    '总高': () => (h ? String(h) : ''),
    '数量': () => String(n),
    '颜色': () => it?.color || '',
    '固定': () => it?.fixedBottom || '',

    '外框宽×高': () => {
      const mat = getMatValue(f, '外框宽')
      if (mat) return fmtDim(mat.dim, getMatValue(f, '外框高')?.dim, mat.mult ?? n * 2)
      return it?.frameOutW ? fmtDim(it.frameOutW, it.frameOutH, n * 2) : fmtDim(w, h + 1, n * 2)
    },
    '内框宽×高': () => {
      const mat = getMatValue(f, '内框宽')
      if (mat) return fmtDim(mat.dim, getMatValue(f, '内框高')?.dim, mat.mult ?? n)
      return it?.frameInW ? fmtDim(it.frameInW, it.frameInH, n) : fmtDim(Math.round(w - 67), h - 20, n)
    },
    '内扇宽×高': () => {
      const mat = getMatValue(f, '内扇宽')
      if (mat) return fmtDim(mat.dim, getMatValue(f, '内扇高')?.dim, mat.mult ?? n)
      return it?.sashW ? fmtDim(it.sashW, it.sashH, n) : fmtDim(227, h - 73, n)
    },
    '孔位': () => f?.孔位 != null ? String(f.孔位) : (it?.holePos || '525'),
    '横杆': () => {
      const mat = getMatValue(f, '横杆')
      return mat?.dim ? String(mat.dim) : (it?.crossBar || '')
    },
    '纱网': () => it?.netMaterial || '',

    '上下纱宽×高': () => {
      const net = f?.纱网
      if (net) {
        const parts = [fmtNet(net['上网'], '上', n), fmtNet(net['下网'], '下', n)].filter(Boolean)
        return parts.join('\n')
      }
      return it?.netCutW ? fmtDim(it.netCutW, it.netCutH, n) : fmtDim(w, h - 52, n)
    },
    '中纱宽×高': () => {
      const mid = f?.纱网?.['中网']
      return mid ? fmtNet(mid, '', n) : (it?.midNet || '')
    },

    '净宽': () => it?.netWidth == null ? '' : String(it.netWidth),
    '加杆': () => it?.addRod || '',
    '把手': () => [it?.handle, it?.handleDirection].filter(Boolean).join(' '),
    '锁具': () => it?.lockSet || '',
    '开向': () => it?.openDirection || '',
    '面积': () => it?.singleArea != null ? String(it.singleArea) : (it?.itemTotalArea != null ? String(it.itemTotalArea) : ''),
    '单价': () => it?.unitPrice != null ? String(it.unitPrice) : '',
    '金额': () => it?.lineAmount != null ? String(it.lineAmount) : '',
    '备注': () => it?.remark || ''
  }

  return map[col]?.() ?? ''
}

/** 单元格显示值：空值显示占位符，单子上不留白 */
export const cellText = (col: string, it: any): string => {
  const v = String(colValue(col, it) ?? '').trim()
  return v === '' ? NA : v
}

/** 该列是否整列为空（整列为空则在打印时隐藏该列，版面更干净） */
const isColBlank = (col: string, items: any[]) =>
  items.length > 0 && items.every(it => String(colValue(col, it) ?? '').trim() === '')

/** 打印时实际显示的列 */
export function visibleCols(cols: string[], items: any[]): string[] {
  const list = (cols && cols.length ? cols : DEFAULT_COLS)
  const kept = list.filter(c => !isColBlank(c, items))
  return kept.length ? kept : list
}

/* ==================================================================
 * 三、渲染样式（面向车间：大字号、粗边框、固定列宽）
 * ================================================================== */
const TH = 'border:2px solid #000;padding:6px 4px;font-size:15px;line-height:1.25;text-align:center;font-weight:bold;background:#f2f2f2;'
const TD = 'border:2px solid #000;padding:7px 4px;font-size:15px;line-height:1.25;height:34px;white-space:pre-line;word-break:break-all;vertical-align:middle;font-weight:600;'

/** 列宽权重：按可见列归一化为百分比（表格 fixed 布局，列宽稳定不跳动） */
const COL_WEIGHT: Record<string, number> = {
  '产品': 21, '品名及规格': 21, '序号': 3.5,
  '总宽×总高': 8, '总宽': 5, '总高': 5, '数量': 4.5, '颜色': 6, '固定': 5, '下固': 5,
  '外框宽×高': 8, '内框宽×高': 8, '内扇宽×高': 8, '孔位': 4, '横杆': 4.5,
  '纱网': 6.5, '上下纱宽×高': 8, '中纱宽×高': 6,
  '加杆': 4.5, '把手': 6, '锁具': 5, '开向': 4.5, '净宽': 4.5, '备注': 6,
  '宽': 5, '高': 5, '面积': 6, '单价': 5.5, '金额': 7
}

/** 左对齐的文本列，其余居中 */
const LEFT_COLS = ['产品', '品名及规格', '备注', '纱网']
const alignOf = (col: string) => LEFT_COLS.includes(col) ? 'text-align:left;' : 'text-align:center;'

function buildColgroup(cols: string[]): string {
  const weights = cols.map(c => COL_WEIGHT[c] ?? 6)
  const sum = weights.reduce((a, b) => a + b, 0) || 1
  return '<colgroup>' + weights.map(w => `<col style="width:${(w / sum * 100).toFixed(2)}%">`).join('') + '</colgroup>'
}

function groupCols(cols: string[]): Array<{ title?: string; cols: string[] }> {
  const groups: Array<{ title?: string; cols: string[] }> = []
  cols.forEach(c => {
    const g = groupOfCol(c)
    if (!g) { groups.push({ cols: [c] }); return }
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

export function buildTableHeadRows(cols: string[]): HeadCell[][] {
  const groups = groupCols(cols)
  if (!groups.some(g => !!g.title)) return [cols.map(c => ({ text: c }))]

  const row1: HeadCell[] = groups.map(g =>
    g.title ? { text: g.title, colspan: g.cols.length } : { text: g.cols[0], rowspan: 2 }
  )
  const row2: HeadCell[] = groups.filter(g => !!g.title).flatMap(g => g.cols.map(c => ({ text: c })))
  return [row1, row2]
}

export function totalColIndex(cols: string[]): number {
  const i = cols.indexOf('数量')
  return i < 0 ? cols.length - 1 : i
}

function buildTableHead(cols: string[]): string {
  return buildTableHeadRows(cols).map(row =>
    '<tr>' + row.map(cell =>
      `<th style="${TH}"${cell.colspan ? ` colspan="${cell.colspan}"` : ''}${cell.rowspan ? ` rowspan="${cell.rowspan}"` : ''}>${esc(cell.text)}</th>`
    ).join('') + '</tr>'
  ).join('')
}

/** 合计行：整行合并，只写合计，不留空格子 */
function buildTableFoot(cols: string[], items: any[]): string {
  const totalNum = items.reduce((s, it) => s + (Number(it?.num) || 0), 0)
  const totalArea = items.reduce((s, it) => s + (Number(it?.itemTotalArea) || 0), 0)
  const areaTxt = totalArea > 0 ? `　总面积：${totalArea.toFixed(2)} ㎡` : ''
  return `<tr><td colspan="${Math.max(cols.length, 1)}" style="${TD}text-align:left;font-weight:bold;background:#f7f7f7;">合计：${totalNum} 件${areaTxt}</td></tr>`
}

/* ==================================================================
 * 四、销售单动态列
 * ================================================================== */
export interface SalesColDef {
  key: string
  label: string
  get: (it: any, i: number) => string
}

export const AUTO_SALES_COLS: SalesColDef[] = [
  { key: 'seq', label: '序号', get: (_it, i) => String(i + 1) },
  { key: 'product', label: '品名及规格', get: it => it?.productName || '' },
  { key: 'width', label: '宽', get: it => it?.width == null ? '' : String(it.width) },
  { key: 'height', label: '高', get: it => it?.height == null ? '' : String(it.height) },
  { key: 'color', label: '颜色', get: it => it?.color || '' },
  { key: 'net', label: '纱网', get: it => it?.netMaterial || '' },
  { key: 'fixed', label: '下固', get: it => it?.fixedBottom ?? '' },
  { key: 'area', label: '面积', get: it => it?.singleArea ?? it?.itemTotalArea ?? '' },
  { key: 'num', label: '数量', get: it => it?.num == null ? '' : String(it.num) },
  { key: 'price', label: '单价', get: it => it?.unitPrice ?? '' },
  { key: 'amount', label: '金额', get: it => it?.lineAmount ?? it?.amount ?? '' },
  { key: 'handle', label: '把手', get: it => it?.handle || '' },
  { key: 'remark', label: '备注', get: it => it?.remark || '' }
]

/** 销售单动态表格：默认显示全部列（空值占位）；hideEmpty=true 时隐藏整列为空的列 */
export function buildDynamicTable(items: any[], hideEmpty = false): string {
  const filterBlank = (c: SalesColDef) =>
    c.key === 'seq' || !hideEmpty ||
    (items.length > 0 && items.some(it => String(c.get(it, 0) ?? '').trim() !== ''))
  const cols = AUTO_SALES_COLS.filter(filterBlank)
  const list = cols.length ? cols : AUTO_SALES_COLS

  const head = '<tr>' + list.map(c => `<th style="${TH}">${esc(c.label)}</th>`).join('') + '</tr>'
  const body = items.map((it, i) =>
    '<tr>' + list.map(c => {
      const v = String(c.get(it, i) ?? '').trim()
      return `<td style="${TD}${alignOf(c.label)}">${esc(v === '' ? NA : v)}</td>`
    }).join('') + '</tr>'
  ).join('')
  const totalNum = items.reduce((s, it) => s + (Number(it?.num) || 0), 0)
  const totalAmount = items.reduce((s, it) => s + (Number(it?.lineAmount ?? it?.amount) || 0), 0)
  const amountTxt = totalAmount > 0 ? `　金额合计：¥${totalAmount.toFixed(2)}` : ''
  const foot = `<tr><td colspan="${Math.max(list.length, 1)}" style="${TD}text-align:left;font-weight:bold;background:#f7f7f7;">合计：${totalNum} 件${amountTxt}</td></tr>`
  return `<colgroup>${list.map(c => `<col style="width:${((COL_WEIGHT[c.label] ?? 6) / list.reduce((s, x) => s + (COL_WEIGHT[x.label] ?? 6), 0) * 100).toFixed(2)}%">`).join('')}</colgroup>
    <thead>${head}</thead><tbody>${body}</tbody><tfoot>${foot}</tfoot>`
}

/* ==================================================================
 * 五、渲染
 * ================================================================== */
/** A4 横向可打印高度（px，@96dpi 297×210mm，留 6mm 边距后约 745px） */
export const PAGE_BODY_H = 745
/** 每页明细行数：超出自动分页，重复表头并标注页码 */
export const PAGE_ROWS = 11

function chunkItems<T>(arr: T[], size: number): T[][] {
  const out: T[][] = []
  for (let i = 0; i < arr.length; i += size) out.push(arr.slice(i, i + size))
  return out
}

/** 把 blocks 渲染成一页 HTML 片段 */
export function renderBlocksToHtml(blocks: PrintBlock[], ctx: RenderCtx, pageInfo?: PageInfo): string {
  const items = ctx.items || []
  const parts = (blocks || []).map(b => {
    const pos = `position:absolute;left:${b.x}px;top:${b.y}px;width:${b.w}px;`
    const hStyle = b.h ? `height:${b.h}px;` : ''
    if (b.type === 'text') {
      const val = b.field ? (ctx.fields?.[b.field] ?? '') : ''
      const align = b.textAlign || 'left'
      return `<div style="${pos}${hStyle}font-size:${b.fontSize || 15}px;font-weight:${b.bold ? 'bold' : 'normal'};text-align:${align};white-space:normal;word-break:break-all;line-height:1.3;">${esc(b.label || '')}${b.field ? '<b>' + esc(val) + '</b>' : ''}</div>`
    }
    if (b.type === 'line') {
      return `<div style="${pos}${hStyle}border-top:2px solid #000;margin-top:3px;"></div>`
    }
    if (b.type === 'qrcode') {
      const qh = b.h || b.w
      let qrType = b.qrType
      if (!qrType) {
        const lb = b.label || ''
        if (/设备|工单|扫码|报工/.test(lb)) qrType = 'work'
        else if (/客户|付款|订单/.test(lb)) qrType = 'customer'
      }
      const qrImg = qrType === 'work' ? ctx.qrWorkData : qrType === 'customer' ? ctx.qrCustomerData : ''
      if (qrImg) {
        return `<div style="${pos}height:${qh}px;text-align:center;">
          <img src="${qrImg}" style="width:${b.w}px;height:${qh}px;object-fit:contain;border:1px solid #000;" />
        </div>`
      }
      return `<div style="${pos}height:${qh}px;text-align:center;">
        <div style="width:100%;height:${qh}px;border:1px solid #000;display:flex;align-items:center;justify-content:center;font-size:11px;color:#666;">${esc(b.label || '二维码')}</div>
      </div>`
    }
    if (b.type === 'table') {
      if (b.dynamic) {
        return `<table style="${pos}${hStyle}border-collapse:collapse;width:100%;table-layout:fixed;">${buildDynamicTable(items, !!b.hideEmptyCols)}</table>`
      }
      const baseCols = (b.cols && b.cols.length ? b.cols : DEFAULT_COLS)
      // 默认打印全部配置列（空值显示占位符）；勾选"隐藏空列"时才隐藏整列为空的列
      const cols = b.hideEmptyCols ? visibleCols(baseCols, items) : baseCols
      const body = items.map(it =>
        '<tr>' + cols.map(c => `<td style="${TD}${alignOf(c)}">${esc(cellText(c, it))}</td>`).join('') + '</tr>'
      ).join('')
      return `<table style="${pos}${hStyle}border-collapse:collapse;width:100%;table-layout:fixed;">
        ${buildColgroup(cols)}
        <thead>${buildTableHead(cols)}</thead>
        <tbody>${body}</tbody>
        <tfoot>${buildTableFoot(cols, items)}</tfoot>
      </table>`
    }
    return ''
  }).join('')

  const pager = pageInfo && pageInfo.total > 1
    ? `<div style="position:absolute;right:18px;bottom:8px;font-size:13px;color:#333;">第 ${pageInfo.index + 1} / ${pageInfo.total} 页</div>`
    : ''
  return `<div style="position:relative;width:1080px;min-height:${PAGE_BODY_H}px;font-family:'SimSun','Microsoft YaHei',sans-serif;color:#000;">${parts}${pager}</div>`
}

/**
 * 【分页渲染】明细行超过一页时自动拆页：每页重复表头 + 页码。
 * 每页行数按模板实际位置计算：表格下方若还有字段（如销售单的金额/备注），
 * 就以那些字段为下边界，避免表格压字。
 */
export function renderPrintBody(blocks: PrintBlock[], ctx: RenderCtx): string {
  const items = ctx.items || []
  const perPage = rowsPerPage(blocks)
  if (items.length <= perPage) return renderBlocksToHtml(blocks, ctx)

  const pages = chunkItems(items, perPage)
  return pages.map((slice, i) => {
    const body = renderBlocksToHtml(blocks, { ...ctx, items: slice }, { index: i, total: pages.length })
    return `<div style="page-break-after:${i === pages.length - 1 ? 'auto' : 'always'};">${body}</div>`
  }).join('')
}

/** 依据模板布局推算单页可容纳的明细行数 */
export function rowsPerPage(blocks: PrintBlock[]): number {
  const table = (blocks || []).find(b => b.type === 'table')
  if (!table) return PAGE_ROWS
  // 表格下方其它区块的最高位置，作为分页下边界；没有则用页底
  const below = (blocks || []).filter(b => b.id !== table.id && b.y > table.y + 10).map(b => b.y)
  const limit = below.length ? Math.min(...below) - 8 : PAGE_BODY_H - 34
  const headerH = table.dynamic ? 40 : buildTableHeadRows(
    visibleCols(table.cols && table.cols.length ? table.cols : DEFAULT_COLS, [])
  ).length * 34
  const usable = limit - table.y - headerH - 10
  // 生产单列多、产品名易换行，按 48px 估算行高，避免末行压到页脚
  const rowH = table.dynamic ? 34 : 48
  return Math.max(3, Math.floor(usable / rowH))
}

export const PRINT_PAGE_CSS = '@page{size:A4 landscape;margin:6mm;}body{margin:0;padding:0;}'

export function wrapPrintDocument(bodyHtml: string, title = '生产单'): string {
  return `<!DOCTYPE html><html><head><meta charset="UTF-8"><title>${esc(title)}</title>
<style>${PRINT_PAGE_CSS}
table{page-break-inside:auto}
tr{page-break-inside:avoid;page-break-after:auto}
thead{display:table-header-group}
</style></head><body>${bodyHtml}</body></html>`
}

/* ==================================================================
 * 六、默认版式
 * ================================================================== */
export function buildDefaultBlocks(): PrintBlock[] {
  return [
    { id: 1, type: 'qrcode', x: 15, y: 10, w: 78, label: '设备扫码', qrType: 'work' },
    { id: 2, type: 'text', x: 15, y: 92, w: 110, fontSize: 11, label: '打印二维码' },
    { id: 3, type: 'text', x: 240, y: 14, w: 600, fontSize: 24, bold: true, label: '固贤纱窗纱门生产单', field: 'specTitle' },
    { id: 4, type: 'qrcode', x: 990, y: 10, w: 78, label: '客户二维码', qrType: 'customer' },
    { id: 5, type: 'text', x: 115, y: 104, w: 270, fontSize: 14, label: '客户名称：', field: 'customerName' },
    { id: 6, type: 'text', x: 115, y: 140, w: 270, fontSize: 14, label: '客户地址：', field: 'customerAddress' },
    { id: 7, type: 'text', x: 425, y: 104, w: 280, fontSize: 14, label: '订单编号：', field: 'orderNo' },
    { id: 8, type: 'text', x: 425, y: 140, w: 280, fontSize: 14, label: '工程地址：', field: 'projectAddress' },
    { id: 9, type: 'text', x: 730, y: 104, w: 300, fontSize: 14, label: '交货日期：', field: 'deliverDate' },
    { id: 10, type: 'text', x: 730, y: 140, w: 300, fontSize: 14, label: '产品数量：', field: 'totalNum' },
    { id: 11, type: 'line', x: 15, y: 158, w: 1050 },
    { id: 12, type: 'table', x: 15, y: 170, w: 1050, cols: [...DEFAULT_COLS], colsText: DEFAULT_COLS.join(',') }
  ]
}

export function buildSalesDefaultBlocks(): PrintBlock[] {
  return [
    { id: 1, type: 'text', x: 190, y: 12, w: 700, h: 40, fontSize: 26, bold: true, textAlign: 'center', label: '固贤纱窗纱门销售单' },
    { id: 2, type: 'text', x: 40, y: 68, w: 240, h: 26, fontSize: 15, label: '客户：', field: 'customerName' },
    { id: 3, type: 'text', x: 300, y: 68, w: 260, h: 26, fontSize: 15, label: '电话：', field: 'customerPhone' },
    { id: 4, type: 'qrcode', x: 930, y: 56, w: 92, h: 92, label: '付款码', qrType: 'customer' },
    { id: 5, type: 'text', x: 40, y: 102, w: 250, h: 26, fontSize: 15, label: '下单日期：', field: 'orderDate' },
    { id: 6, type: 'text', x: 300, y: 102, w: 300, h: 26, fontSize: 15, label: '单号：', field: 'orderNo' },
    { id: 7, type: 'text', x: 620, y: 68, w: 290, h: 26, fontSize: 15, label: '业务员：', field: 'salesman' },
    { id: 8, type: 'text', x: 40, y: 136, w: 860, h: 26, fontSize: 15, label: '终端地址：', field: 'terminalAddress' },
    { id: 9, type: 'line', x: 20, y: 170, w: 1040, h: 1 },
    { id: 10, type: 'table', x: 20, y: 182, w: 1040, dynamic: true, cols: ['序号'], colsText: '序号' },
    { id: 11, type: 'text', x: 40, y: 470, w: 1000, h: 26, fontSize: 15, label: '金额(大写)：', field: 'amountInWords' },
    { id: 12, type: 'text', x: 40, y: 502, w: 300, h: 26, fontSize: 15, label: '合计金额：', field: 'totalAmount' },
    { id: 13, type: 'text', x: 380, y: 502, w: 260, h: 26, fontSize: 15, label: '已收：', field: 'deposit' },
    { id: 14, type: 'text', x: 660, y: 502, w: 300, h: 26, fontSize: 15, label: '未收：', field: 'balance' },
    { id: 15, type: 'line', x: 20, y: 536, w: 1040, h: 1 },
    { id: 16, type: 'text', x: 40, y: 548, w: 1000, h: 24, fontSize: 14, label: '请仔细核对尺寸与数量，确认无误后下单。' },
    { id: 17, type: 'text', x: 40, y: 576, w: 1000, h: 24, fontSize: 14, label: '防护栏统一间距 14 公分内、宽度 19 公分内，如有特殊要求请提前告知。' }
  ]
}

/* ==================================================================
 * 七、上下文装配
 * ================================================================== */
const dayText = (v: any) => (v ? String(v).slice(0, 10) : '')
const orNA = (v: any) => (v === null || v === undefined || String(v).trim() === '' ? NA : v)

export function buildPrintContext(detail: any): RenderCtx {
  const items: any[] = detail?.orderItems || []
  const first = items[0] || {}
  const productName: string = first.productName || ''
  const names = Array.from(new Set(items.map((it: any) => it?.productName).filter(Boolean))) as string[]
  return {
    fields: {
      workNo: orNA(detail?.workNo),
      orderNo: orNA(detail?.orderNo),
      customerName: orNA(detail?.customerName),
      customerPhone: orNA(detail?.customerPhone),
      customerAddress: orNA(detail?.customerAddress),
      projectAddress: orNA(detail?.customerAddress),
      terminalAddress: orNA(detail?.terminalAddress || detail?.customerAddress),
      productName: orNA(names.length > 1 ? `${names[0]} 等 ${names.length} 种` : productName),
      seriesName: orNA(first.itemCategory || first.dictType),
      specTitle: names.length > 1 ? `（共 ${names.length} 种产品）` : (productName ? `-${productName}` : ''),
      deliverDate: orNA(dayText(detail?.expectDate)),
      orderDate: orNA(dayText(detail?.orderDate)),
      createTime: orNA(dayText(detail?.createTime)),
      finishTime: orNA(dayText(detail?.finishTime)),
      totalNum: items.reduce((s, it) => s + (Number(it?.num) || 0), 0),
      totalArea: orNA(detail?.totalArea),
      shelfName: orNA(detail?.shelfName)
    },
    items,
    workId: detail?.workId || '',
    qrWork: `GX1|WO|${detail?.workId ?? ''}|${detail?.workNo || ''}|${dayText(new Date()).replace(/-/g, '')}`,
    qrCustomer: `GX-ORDER:${detail?.orderNo || ''}`
  }
}

/** 金额转中文大写 */
export function toChineseAmount(num: any): string {
  const n = Number(num)
  if (!isFinite(n) || n <= 0) return '零元整'
  const digits = ['零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖']
  const units = ['', '拾', '佰', '仟']
  const bigUnits = ['', '万', '亿']
  const intPart = Math.floor(n)
  const decPart = Math.round((n - intPart) * 100)
  const intStr = String(intPart)
  const len = intStr.length
  let out = ''
  let zero = false
  for (let i = 0; i < len; i++) {
    const d = Number(intStr[i])
    const pos = len - i - 1
    const big = bigUnits[Math.floor(pos / 4)]
    if (d === 0) {
      zero = true
      if (pos % 4 === 0 && big) { out += big; zero = false }
    } else {
      if (zero) { out += '零'; zero = false }
      out += digits[d] + units[pos % 4] + (pos % 4 === 0 ? big : '')
    }
  }
  out += '元'
  if (decPart === 0) return out + '整'
  const jiao = Math.floor(decPart / 10)
  const fen = decPart % 10
  if (jiao === 0) out += '零'
  else out += digits[jiao] + '角'
  if (fen > 0) out += digits[fen] + '分'
  return out
}

export function buildSalesContext(order: any): RenderCtx {
  const items: any[] = order?.itemList || []
  const totalAmount = Number(order?.totalAmount) || 0
  const received = Number(order?.receiveAmount) || 0
  const unpaid = Number(order?.unpaidAmount)
  return {
    fields: {
      orderNo: orNA(order?.orderNo),
      customerName: orNA(order?.customerName),
      customerPhone: orNA(order?.phone),
      customerAddress: orNA(order?.address),
      terminalAddress: orNA(order?.terminalAddress || order?.address),
      projectAddress: orNA(order?.terminalAddress || order?.address),
      orderDate: orNA(dayText(order?.orderDate)),
      createTime: orNA(dayText(order?.orderDate || order?.createTime)),
      deliverDate: orNA(dayText(order?.expectDate)),
      salesman: orNA(order?.salesman),
      remark: orNA(order?.remark),
      totalNum: items.reduce((s, it) => s + (Number(it?.num) || 0), 0),
      totalArea: orNA(order?.totalArea),
      totalAmount: totalAmount ? totalAmount.toFixed(2) : NA,
      amountInWords: totalAmount ? toChineseAmount(totalAmount) : NA,
      deposit: received ? received.toFixed(2) : '0.00',
      balance: (isFinite(unpaid) ? unpaid : totalAmount - received).toFixed(2)
    },
    items,
    qrCustomer: `GX-ORDER:${order?.orderNo || ''}`
  }
}

/* ==================================================================
 * 八、二维码与模板加载
 * ================================================================== */
export async function prepareQrData(ctx: RenderCtx): Promise<RenderCtx> {
  try {
    const mod: any = await import('qrcode')
    const QRCode = mod?.default || mod
    const opts = { width: 240, margin: 1, errorCorrectionLevel: 'M' as const }
    if (ctx.qrWork && !ctx.qrWorkData) {
      try { ctx.qrWorkData = await QRCode.toDataURL(ctx.qrWork, opts) } catch (e) { console.warn('[打印] 工单二维码生成失败', e) }
    }
    if (ctx.qrCustomer && !ctx.qrCustomerData) {
      try { ctx.qrCustomerData = await QRCode.toDataURL(ctx.qrCustomer, opts) } catch (e) { console.warn('[打印] 客户二维码生成失败', e) }
    }
  } catch (e) {
    console.warn('[打印] 二维码库加载失败，使用占位框', e)
  }
  return ctx
}

function injectFields(html: string, ctx: RenderCtx): string {
  return html.replace(/\$\{(\w+)\}/g, (_m, key) => esc(ctx.fields?.[key] ?? NA))
}

/** 通用模板加载：按类型取默认启用模板，读不到回退内置版式 */
async function loadTemplate(type: string, fallback: () => PrintBlock[]): Promise<{ blocks?: PrintBlock[]; rawHtml?: string }> {
  try {
    const api: any = await import('../api/system/printTemplate.ts')
    const res: any = await api.getPrintTemplatePage(1, 100, '', type, 1)
    const list: any[] = res?.data?.records || []
    if (!list.length) return { blocks: fallback() }
    const tpl = list.find((t: any) => t.isDefault === 1) || list[0]
    const content = String(tpl?.templateContent || '').trim()
    if (!content) return { blocks: fallback() }
    if (content.startsWith('<')) return { rawHtml: content }
    const parsed = JSON.parse(content)
    if (Array.isArray(parsed) && parsed.length) return { blocks: parsed as PrintBlock[] }
  } catch (e) {
    console.warn('[打印] 读取打印模板失败，使用内置默认版式', e)
  }
  return { blocks: fallback() }
}

export const loadWorkOrderTemplate = () => loadTemplate('WORK_ORDER', buildDefaultBlocks)
export const loadSalesTemplate = () => loadTemplate('SALES_ORDER', buildSalesDefaultBlocks)

/* ==================================================================
 * 九、对外打印入口
 * ================================================================== */
/** 【生产单】渲染完整打印 HTML（多产品超过一页自动分页） */
export async function renderWorkOrderHtml(detail: any, title = '生产单'): Promise<string> {
  const ctx = buildPrintContext(detail)
  await prepareQrData(ctx)
  const tpl = await loadWorkOrderTemplate()
  const body = tpl.rawHtml ? injectFields(tpl.rawHtml, ctx) : renderPrintBody(tpl.blocks!, ctx)
  return wrapPrintDocument(body, title)
}

/** 【批量生产单】多张生产单合并打印，每张分页 */
export async function renderWorkOrdersHtml(details: any[], title = '批量生产单'): Promise<string> {
  const tpl = await loadWorkOrderTemplate()
  const pages: string[] = []
  for (const d of details || []) {
    const ctx = buildPrintContext(d)
    await prepareQrData(ctx)
    const body = tpl.rawHtml ? injectFields(tpl.rawHtml, ctx) : renderPrintBody(tpl.blocks!, ctx)
    pages.push(`<div style="page-break-after:always;">${body}</div>`)
  }
  return wrapPrintDocument(pages.join(''), title)
}

/** 【销售单】渲染完整打印 HTML */
export async function renderSalesOrderHtml(order: any, title = '销售单'): Promise<string> {
  const ctx = buildSalesContext(order)
  await prepareQrData(ctx)
  const tpl = await loadSalesTemplate()
  const body = tpl.rawHtml ? injectFields(tpl.rawHtml, ctx) : renderPrintBody(tpl.blocks!, ctx)
  return wrapPrintDocument(body, title)
}
