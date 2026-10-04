/**
 * 纱窗产品公式引擎原型（护重-压条款120款 / 小料三节）
 * 模拟"产品详情 → 公式管理 → 测试"：公式配置化，输入属性参数，引擎按依赖顺序计算。
 * 支持：条件分支(下固定=0/>0)、多条件花括号{加杆=有,下固定=0}、变量引用(孔位+13/防护栏-200)、
 *       同名公式多行(内扇高/加杆按条件取行)、金额加价累加(>1399 与 >1499 两档叠加)。
 * 跑通后逻辑可直接翻译为 Java 版（自研 tokenizer + 白名单）接入后端。
 * 【已确认口径】32小方盒-对开窗 不做；总高加价两档累加；小料三节面积>1 用 总高*总宽；
 * 小料三节把手加价条件 = 一字密码锁。
 */

// ---------- 1. 表达式求值（变量 + 数字 + 四则运算 + 括号 + 比较 + lwxs函数） ----------
const lwxsImpl = (x) => x // 占位：lwxs() 系数函数，待用户提供真实定义
function evalExpr(expr, ctx) {
  if (typeof expr !== 'string') return expr
  let s = expr
  // 0) 公式里的 "=" 是相等比较 → 转成 JS 的 "=="（不影响 <= / >= / ==）
  s = s.replace(/(?<![=<>!])=(?!=)/g, '==')
  // 1) 先按变量名长度降序替换变量（正则边界：前后不能是中文/字母/数字/下划线，防子串误替换）
  const keys = Object.keys(ctx).sort((a, b) => b.length - a.length)
  for (const k of keys) {
    const v = ctx[k]
    const rep = typeof v === 'number' ? `(${v})` : `'${String(v).replace(/'/g, '')}'`
    const esc = k.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    s = s.replace(
      new RegExp(`(?<![\\u4e00-\\u9fa5A-Za-z0-9_])${esc}(?![\\u4e00-\\u9fa5A-Za-z0-9_])`, 'g'),
      rep
    )
  }
  // 2) 剩余裸中文（如 全防护/中间两根）→ 字符串字面量（先保护已带引号的串）
  const quoted = []
  s = s.replace(/'[^']*'/g, (m) => { quoted.push(m); return `\u0000${quoted.length - 1}\u0000` })
  s = s.replace(/([\u4e00-\u9fa5]+)/g, "'$1'")
  s = s.replace(/\u0000(\d+)\u0000/g, (_m, i) => quoted[+i])
  // 仅原型验证用 Function；生产 Java 版用自研 tokenizer + 白名单
  const fn = new Function('lwxs', `return (${s})`)
  return fn(lwxsImpl)
}
const round2 = v => Math.round(v * 100) / 100

// ---------- 2. 条件匹配（支持花括号多条件 AND） ----------
function matchesCond(cond, ctx) {
  if (cond == null || cond === '') return true
  let c = String(cond).trim()
  if (c.startsWith('{') && c.endsWith('}')) {
    const inner = c.slice(1, -1)
    return inner.split(',').every(sub => evalExpr(sub.trim(), ctx))
  }
  return evalExpr(c, ctx)
}

// ---------- 3. 条件分支匹配（变量组/面积） ----------
function pick(conds, ctx) {
  for (const row of conds) {
    if (matchesCond(row.cond, ctx)) return evalExpr(row.expr, ctx)
  }
  return null
}

// ---------- 4. 产品公式配置库（对应图片"公式管理"文本，结构化 JSON） ----------
const PRODUCTS = {
  '护童-压条款（120款）': {
    vars: ['总高', '总宽', '数量', '颜色', '网子', '加杆', '下固定', '把手', '单价', '把手方向'],
    防护杆数量: [
      { cond: '总高<=1400', expr: '6' },
      { cond: '总高>1400', expr: '8' }
    ],
    孔位: [
      { cond: '下固定=0', expr: '(总高-40)/3' },
      { cond: '下固定>0', expr: '(总高-40-下固定)/2' }
    ],
    型材: {
      外框高: { expr: '总高-40', mult: '2*数量' },
      外框宽: { expr: '总宽-40', mult: '2*数量' },
      内框高: { expr: '总高-42', mult: '2*数量' },
      内框宽: { expr: '总宽-43', mult: '2*数量' },
      内扇高: { expr: '孔位+21', mult: '2*数量' },
      内扇宽: { expr: '总宽-40', mult: '2*数量' },
      横杆: { expr: '内框宽', mult: '2*数量' },
      防护栏: { expr: '总宽-15', mult: '防护杆数量*数量', cond: '加杆=全防护' },
      小短杆: { expr: '孔位-20', mult: '1*数量', cond: '加杆=全防护' },
      短横杆: { expr: '总宽-215', mult: '2*数量', cond: '加杆=全防护' },
      加杆: { expr: '孔位-20', mult: '2*数量', cond: '加杆=中间两根' }
    },
    纱网: {
      上网: [{ w: '内框宽+15', h: '孔位+4', mult: '1*数量' }],
      下网: [
        { w: '内框宽+15', h: '孔位+4', mult: '1*数量', cond: '下固定=0' },
        { w: '内框宽+15', h: '下固定+4', mult: '1*数量', cond: '下固定>0' }
      ],
      中网: [{ w: '外框宽-45', h: '孔位-25', mult: '1*数量' }]
    },
    面积: [
      { cond: '总高*总宽*0.000001<=1', expr: '1*数量' },
      { cond: '总高*总宽*0.000001>1', expr: '总高*总宽*0.000001*数量' }
    ],
    金额明细: {
      基本金额: { expr: '单价*面积' },
      总高加价: [
        { expr: '5*面积', cond: '总高>1399' },
        { expr: '5*面积', cond: '总高>1499' }
      ], // 累加：1500 → 10*面积
      加杆加价: { expr: '0*面积', cond: '加杆=全防护' }
    }
  },

  '小料三节': {
    vars: ['总高', '总宽', '数量', '颜色', '网子', '把手', '加杆', '下固定', '单价', '把手方向'],
    孔位: [
      { cond: '下固定=0', expr: '总高/3-6' },
      { cond: '下固定>0', expr: '(总高-下固定)/2-6' }
    ],
    型材: {
      外框宽: { expr: '总宽-28', mult: '2*数量' },
      外框高: { expr: '总高-28', mult: '2*数量' },
      横杆: { expr: '总宽-28', mult: '2*数量' },
      内扇宽: { expr: '总宽-27.5', mult: '2*数量' },
      内扇高: [
        { expr: '总高/3-6+13', mult: '2*数量', cond: '下固定=0' },
        { expr: '孔位+13', mult: '2*数量', cond: '下固定>0' }
      ],
      加杆: [
        { expr: '总高/3-6-36', mult: '2*数量', cond: '{加杆=有,下固定=0}' },
        { expr: '孔位-36', mult: '2*数量', cond: '{加杆=有,下固定>0}' }
      ],
      小短杆: { expr: '孔位-36', mult: '1*数量', cond: '加杆=全防护' },
      短横杆: { expr: '外框宽-182', mult: '2*数量', cond: '加杆=全防护' }
    },
    纱网: {
      上网: [{ w: '内扇宽+15', h: '内扇高-10', mult: '1*数量' }],
      下网: [
        { w: '内扇宽+15', h: '内扇高-10', mult: '1*数量', cond: '下固定=0' },
        { w: '内扇宽+15', h: '下固定-5+3', mult: '1*数量', cond: '下固定>0' }
      ],
      中网: [{ w: '内扇宽-36', h: '内扇高-35', mult: '1*数量' }]
    },
    面积: [
      { cond: '总高*总宽*0.000001<=1', expr: '1*数量' },
      { cond: '总高*总宽*0.000001>1', expr: '总高*总宽*0.000001*数量' } // 已按判断修正为 总高*总宽
    ],
    金额明细: {
      基本金额: { expr: '单价*面积' },
      总高加价: [
        { expr: '5*面积', cond: '总高>1399' },
        { expr: '5*面积', cond: '总高>1499' }
      ], // 累加
      把手加价: { expr: '5*数量', cond: '把手=一字密码锁' } // 已按判断修正
    }
  }
}

// ---------- 5. 引擎：按依赖顺序计算 ----------
function runTest(productName, input) {
  const F = PRODUCTS[productName]
  if (!F) throw new Error(`未找到产品: ${productName}`)
  const ctx = { ...input }
  const out = {}

  // 变量组
  if (F.防护杆数量) { ctx.防护杆数量 = round2(pick(F.防护杆数量, ctx)); out['防护杆数量'] = ctx.防护杆数量 }
  if (F.孔位) { ctx.孔位 = round2(pick(F.孔位, ctx)); out['孔位'] = ctx.孔位 }

  // 型材组（同名多行按条件取第一行）
  out.型材 = {}
  for (const [name, def] of Object.entries(F.型材)) {
    const rows = Array.isArray(def) ? def : [def]
    for (const row of rows) {
      if (!matchesCond(row.cond, ctx)) continue
      const dim = round2(evalExpr(row.expr, ctx))
      const mult = round2(evalExpr(row.mult, ctx))
      out.型材[name] = { dim, mult, total: round2(dim * mult) }
      ctx[name] = dim
      break
    }
  }
  ctx.外框宽 = out.型材['外框宽']?.dim
  ctx.内框宽 = out.型材['内框宽']?.dim
  ctx.内扇宽 = out.型材['内扇宽']?.dim
  ctx.内扇高 = out.型材['内扇高']?.dim

  // 纱网组
  out.纱网 = {}
  for (const [name, rows] of Object.entries(F.纱网)) {
    for (const row of rows) {
      if (!matchesCond(row.cond, ctx)) continue
      const w = round2(evalExpr(row.w, ctx))
      const h = round2(evalExpr(row.h, ctx))
      const mult = round2(evalExpr(row.mult, ctx))
      out.纱网[name] = { w, h, mult, area: round2(w * h * 0.000001 * mult) }
      break
    }
  }

  // 面积
  ctx.面积 = round2(pick(F.面积, ctx))
  out['面积'] = ctx.面积

  // 金额明细（同名多行满足条件全部累加，如总高加价两档叠加）
  out.金额 = {}
  let totalAmount = 0
  for (const [name, def] of Object.entries(F.金额明细)) {
    const rows = Array.isArray(def) ? def : [def]
    let acc = 0
    let any = false
    for (const row of rows) {
      if (!matchesCond(row.cond, ctx)) continue
      acc += evalExpr(row.expr, ctx)
      any = true
    }
    if (any) { out.金额[name] = round2(acc); totalAmount += round2(acc) }
  }
  out['总金额'] = round2(totalAmount)
  return { ctx, out }
}

// ---------- 6. 测试用例 ----------
const cases = [
  // 护童-压条款（120款）
  { p: '护童-压条款（120款）', label: '护童A：总高1500/总宽600/2樘/下固定450/全防护', input: { 总高: 1500, 总宽: 600, 数量: 2, 加杆: '全防护', 下固定: 450, 单价: 120 } },
  { p: '护童-压条款（120款）', label: '护童B：总高1300/总宽700/1樘/下固定0/中间两根', input: { 总高: 1300, 总宽: 700, 数量: 1, 加杆: '中间两根', 下固定: 0, 单价: 120 } },
  { p: '护童-压条款（120款）', label: '护童C：总高1600/总宽900/3樘/下固定600/全防护', input: { 总高: 1600, 总宽: 900, 数量: 3, 加杆: '全防护', 下固定: 600, 单价: 120 } },
  // 小料三节
  { p: '小料三节', label: '小料A：总高1300/总宽700/2樘/下固定0/加杆无', input: { 总高: 1300, 总宽: 700, 数量: 2, 加杆: '无', 下固定: 0, 单价: 65, 把手: '普通把手' } },
  { p: '小料三节', label: '小料B：总高1300/总宽700/2樘/下固定300/加杆有', input: { 总高: 1300, 总宽: 700, 数量: 2, 加杆: '有', 下固定: 300, 单价: 65, 把手: '一字密码锁' } },
  { p: '小料三节', label: '小料C：总高1500/总宽800/3樘/下固定0/全防护', input: { 总高: 1500, 总宽: 800, 数量: 3, 加杆: '全防护', 下固定: 0, 单价: 65, 把手: '一字密码锁' } }
]

for (const c of cases) {
  console.log('='.repeat(74))
  console.log(`【${c.p}】${c.label}`)
  const { out } = runTest(c.p, c.input)
  const pad = (s, n) => String(s).padEnd(n)
  if (out['防护杆数量'] !== undefined) console.log(pad('防护杆数量', 10), out['防护杆数量'])
  if (out['孔位'] !== undefined) console.log(pad('孔位', 10), out['孔位'])
  console.log('-- 型材下料 --')
  for (const [k, v] of Object.entries(out.型材)) {
    console.log(pad(k, 8), pad(`${v.dim} × ${v.mult} = ${v.total}`, 24), 'mm')
  }
  console.log('-- 纱网 --')
  for (const [k, v] of Object.entries(out.纱网)) {
    console.log(pad(k, 8), pad(`${v.w} × ${v.h} × ${v.mult}`, 22), `≈ ${v.area} ㎡`)
  }
  console.log(pad('面积', 10), `${out['面积']} ㎡`)
  console.log('-- 金额明细 --')
  for (const [k, v] of Object.entries(out.金额)) {
    console.log(pad(k, 10), v, '元')
  }
  console.log(pad('总金额', 10), `${out['总金额']} 元`)
}
