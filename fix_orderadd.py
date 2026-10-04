# -*- coding: utf-8 -*-
"""OrderAdd.vue：选产品后属性下拉按产品 vars 数据列表 + 默认值预填"""
import io

p = r"D:\Java\guxian\guxian-erp-screen\src\views\sales\OrderAdd.vue"
with io.open(p, "r", encoding="utf-8") as f:
    c = f.read()

# ---------- 1) 模板属性列：选项源改为 attrOptions ----------
old_tpl = '''            <el-select v-model="s.row[col.prop]" filterable allow-create default-first-option clearable size="small"
              placeholder="" style="width: 100%">
              <el-option v-for="o in dict[col.dictKey]" :key="o.value" :label="o.label" :value="o.label" />
            </el-select>'''
new_tpl = '''            <el-select v-model="s.row[col.prop]" filterable allow-create default-first-option clearable size="small"
              placeholder="" style="width: 100%">
              <el-option v-for="o in attrOptions(s.row, col)" :key="o.value + '-' + o.label" :label="o.label" :value="o.label" />
            </el-select>'''
ok_tpl = old_tpl in c
if ok_tpl:
    c = c.replace(old_tpl, new_tpl)

# ---------- 2) blankItem 加 _vars ----------
old_bi = '''  openDirection: '', addRod: '', fixedBottom: '', squareBoard: '',
  width: undefined as number | undefined,'''
new_bi = '''  openDirection: '', addRod: '', fixedBottom: '', squareBoard: '',
  _vars: null as Record<string, any> | null,
  width: undefined as number | undefined,'''
ok_bi = old_bi in c
if ok_bi:
    c = c.replace(old_bi, new_bi)

# ---------- 3) onProductChange 改造（解析 vars + 预填默认） ----------
old_opc = '''const onProductChange = (row: any, val: string) => {
  if (!val) {
    row.dictType = ''
    row.dictValue = ''
    row.productName = ''
    row.itemCategory = ''
    return
  }
  const [dictType, label, value] = String(val).split('|')
  const opt = dict.product.find(p => p.dictType === dictType && p.label === label)
  if (!opt) return
  row.dictType = dictType
  row.dictValue = value || ''
  row.productName = opt.label
  row.itemCategory = opt.typeName || ''
}'''
new_opc = '''/** 明细字段 ↔ 产品属性名 映射 */
const PROP_ATTR_MAP: Record<string, string> = {
  color: '颜色', netMaterial: '网子', handle: '把手',
  handleDirection: '把手方向', addRod: '加杆', fixedBottom: '下固定',
  unitPrice: '单价', num: '数量'
}

/** 解析产品 formulaConfig.vars → { 属性名: { list, def } } */
const parseProductVars = (opt: any): Record<string, any> => {
  const out: Record<string, any> = {}
  try {
    if (!opt?.formulaConfig) return out
    const cfg = JSON.parse(opt.formulaConfig)
    if (Array.isArray(cfg?.vars)) {
      cfg.vars.forEach((v: any) => {
        if (v?.name) out[v.name] = {
          list: v.list || '',
          def: v.default !== undefined ? String(v.default) : ''
        }
      })
    }
  } catch {
    // 公式配置异常时忽略，属性下拉走全局字典兜底
  }
  return out
}

/** 某属性列的下拉选项：优先取该产品自身 vars 的数据列表，否则用全局字典兜底 */
const attrOptions = (row: any, col: any) => {
  const attrName = PROP_ATTR_MAP[col.prop]
  const v = attrName && row._vars ? row._vars[attrName] : null
  if (v && v.list) {
    return v.list.split(/[,，]/).map((s: string) => ({ label: s, value: s }))
  }
  return dict[col.dictKey] || []
}

const onProductChange = (row: any, val: string) => {
  if (!val) {
    row.dictType = ''
    row.dictValue = ''
    row.productName = ''
    row.itemCategory = ''
    row._vars = null
    return
  }
  const [dictType, label, value] = String(val).split('|')
  const opt = dict.product.find(p => p.dictType === dictType && p.label === label)
  if (!opt) return
  row.dictType = dictType
  row.dictValue = value || ''
  row.productName = opt.label
  row.itemCategory = opt.typeName || ''
  // 加载产品属性（vars）并预填默认值（如 加杆默认全防护、单价默认110）
  const vars = parseProductVars(opt)
  row._vars = vars
  Object.entries(PROP_ATTR_MAP).forEach(([prop, attrName]) => {
    const v = vars[attrName]
    if (!v || v.def === '') return
    if (prop === 'unitPrice' || prop === 'num') {
      const n = Number(v.def)
      if (!Number.isNaN(n)) row[prop] = n
    } else {
      row[prop] = v.def
    }
  })
  calcRow(row)
}'''
ok_opc = old_opc in c
if ok_opc:
    c = c.replace(old_opc, new_opc)

with io.open(p, "w", encoding="utf-8", newline="\n") as f:
    f.write(c)
print("模板属性列 attrOptions:", ok_tpl)
print("blankItem _vars:", ok_bi)
print("onProductChange 改造:", ok_opc)
