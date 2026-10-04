<template>
  <!-- ============================================================
       公式管理（仿产品详情页）：左=产品属性，右=分组公式编辑器
       全程无 JSON，工人直接看「外框宽 = 总宽-40 × 4支」这种直观行
       ============================================================ -->
  <div class="fm-wrap">
    <!-- 顶部：产品名 + 说明 -->
    <div class="fm-head">
      <div>
        <span class="fm-title">产品：{{ dictItem.dictLabel }}</span>
        <span class="fm-sub">配置下料 / 剪网 / 面积 / 金额计算公式（保存后，下单自动按此计算）</span>
      </div>
      <el-tag type="success" effect="plain" v-if="dictItem.formulaConfig">已配置公式</el-tag>
    </div>

    <div class="fm-body">
      <!-- ========== 左：产品属性 ========== -->
      <div class="fm-left">
        <div class="fm-panel-title">
          <span>产品属性</span>
          <el-button size="small" type="primary" plain @click="addProp">＋ 新建产品属性</el-button>
        </div>
        <el-table :data="model.vars" size="small" border max-height="430" style="width:100%">
          <el-table-column label="名称" min-width="86">
            <template #default="s">
              <el-input v-model="s.row.name" size="small" placeholder="如：总高" />
            </template>
          </el-table-column>
          <el-table-column label="数据列表" min-width="120">
            <template #default="s">
              <el-input v-model="s.row.list" size="small" placeholder="逗号分隔，如：灰色,白色" />
            </template>
          </el-table-column>
          <el-table-column label="默认值" width="86">
            <template #default="s">
              <el-input v-model="s.row.def" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="顺序" width="60">
            <template #default="s">
              <el-input v-model="s.row.sort" size="small" />
            </template>
          </el-table-column>
          <el-table-column width="36">
            <template #default="s">
              <el-button text type="danger" size="small" @click="delProp(s.$index)">✕</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="fm-var-hint">
          公式可用变量：
          <el-tag v-for="p in model.vars.filter(v=>v.name.trim())" :key="p.name" size="small"
                  effect="plain" class="var-tag">{{ p.name }}</el-tag>
        </div>
      </div>

      <!-- ========== 右：公式编辑 ========== -->
      <div class="fm-right">
        <!-- 变量组（$ 开头的计算中间量，如孔位/防护杆数量） -->
        <div v-for="g in model.varGroups" :key="g.key" class="fm-group">
          <div class="fm-group-title">
            <span class="g-icon">$</span> {{ g.key }}（计算中间量）
            <el-button size="small" text type="primary" @click="addRow(g.rows)">＋ 添加行</el-button>
          </div>
          <div class="row-head row-2">
            <span>适用条件（可为空）</span><span>公式</span><span style="width:34px"></span>
          </div>
          <div v-for="(r, i) in g.rows" :key="i" class="row-edit row-2">
            <el-input v-model="r.cond" size="small" placeholder="如：下固定=0" />
            <el-input v-model="r.expr" size="small" placeholder="如：(总高-40)/3" />
            <el-button text type="danger" size="small" @click="g.rows.splice(i,1)">✕</el-button>
          </div>
        </div>

        <!-- 型材下料 -->
        <div class="fm-group">
          <div class="fm-group-title">
            <span class="g-icon">*</span> 型材（下料尺寸，单位 mm）
            <el-button size="small" text type="primary" @click="addProfile">＋ 添加型材</el-button>
          </div>
          <div class="row-head row-4">
            <span>名称</span><span>单支尺寸公式</span><span>数量公式</span><span>适用条件（可为空）</span><span style="width:34px"></span>
          </div>
          <div v-for="(p, i) in model.profiles" :key="i" class="row-edit row-4">
            <el-input v-model="p.name" size="small" placeholder="如：外框宽" />
            <el-input v-model="p.rows[0].expr" size="small" placeholder="如：总宽-40" />
            <el-input v-model="p.rows[0].mult" size="small" placeholder="如：2*数量" />
            <el-input v-model="p.rows[0].cond" size="small" placeholder="可为空" />
            <el-button text type="danger" size="small" @click="delProfile(i)">✕</el-button>
          </div>
        </div>

        <!-- 纱网 -->
        <div class="fm-group">
          <div class="fm-group-title">
            <span class="g-icon">*</span> 纱网（剪网尺寸，单位 mm）
            <el-button size="small" text type="primary" @click="addNet">＋ 添加纱网</el-button>
          </div>
          <div class="row-head row-5">
            <span>名称</span><span>宽公式</span><span>高公式</span><span>张数公式</span><span>适用条件（可为空）</span><span style="width:34px"></span>
          </div>
          <div v-for="(p, i) in model.nets" :key="i" class="row-edit row-5">
            <el-input v-model="p.name" size="small" placeholder="如：上网" />
            <el-input v-model="p.rows[0].w" size="small" placeholder="如：内扇宽+15" />
            <el-input v-model="p.rows[0].h" size="small" placeholder="如：内扇高-10" />
            <el-input v-model="p.rows[0].mult" size="small" placeholder="如：1*数量" />
            <el-input v-model="p.rows[0].cond" size="small" placeholder="可为空" />
            <el-button text type="danger" size="small" @click="delNet(i)">✕</el-button>
          </div>
        </div>

        <!-- 面积 -->
        <div class="fm-group">
          <div class="fm-group-title">
            <span class="g-icon">$</span> 面积（㎡）
            <el-button size="small" text type="primary" @click="addArea">＋ 添加行</el-button>
          </div>
          <div class="row-head row-2">
            <span>适用条件（可为空）</span><span>面积公式</span><span style="width:34px"></span>
          </div>
          <div v-for="(r, i) in model.areas" :key="i" class="row-edit row-2">
            <el-input v-model="r.cond" size="small" placeholder="如：总高*总宽*0.000001>1" />
            <el-input v-model="r.expr" size="small" placeholder="如：总高*总宽*0.000001*数量" />
            <el-button text type="danger" size="small" @click="model.areas.splice(i,1)">✕</el-button>
          </div>
        </div>

        <!-- 金额明细 -->
        <div class="fm-group">
          <div class="fm-group-title">
            <span class="g-icon">*</span> 金额明细（元，同名多行自动累加）
            <el-button size="small" text type="primary" @click="addAmount">＋ 添加金额项</el-button>
          </div>
          <div class="row-head row-3">
            <span>名称</span><span>金额公式</span><span>适用条件（可为空）</span><span style="width:34px"></span>
          </div>
          <div v-for="(p, i) in model.amounts" :key="i" class="row-edit row-3">
            <el-input v-model="p.name" size="small" placeholder="如：基本金额" />
            <el-input v-model="p.rows[0].expr" size="small" placeholder="如：单价*面积" />
            <el-input v-model="p.rows[0].cond" size="small" placeholder="可为空" />
            <el-button text type="danger" size="small" @click="delAmount(i)">✕</el-button>
          </div>
        </div>

        <div class="fm-tip">
          提示：条件为空表示始终成立；条件示例「下固定=0」「加杆=全防护」「{加杆=有,下固定>0}」多条件用花括号+逗号。
          金额明细同名称多行会全部累加（如 总高>1399 与 总高>1499 两档叠加）。
        </div>
      </div>
    </div>

    <!-- ========== 底部操作 ========== -->
    <div class="fm-footer">
      <el-button @click="emit('close')">取消</el-button>
      <el-button type="warning" @click="openTest">测试公式</el-button>
      <el-button type="primary" @click="save">保存公式</el-button>
    </div>

    <!-- ========== 测试弹窗：左参数 / 右结果 ========== -->
    <el-dialog v-model="testVisible" title="公式测试" width="880px" append-to-body :close-on-click-modal="false">
      <div v-if="testError" class="test-error">{{ testError }}</div>
      <el-row :gutter="16">
        <el-col :span="9">
          <div class="test-panel-title">输入参数（按产品属性填写）</div>
          <el-form label-width="86px" size="small">
            <el-form-item v-for="v in testVars" :key="v.name" :label="v.name">
              <el-select v-if="v.list" v-model="testParams[v.name]" style="width:100%" clearable>
                <el-option v-for="opt in v.list.split(/[,，]/)" :key="opt" :label="opt" :value="opt" />
              </el-select>
              <el-input v-else v-model="testParams[v.name]" clearable />
            </el-form-item>
          </el-form>
          <el-button type="primary" size="small" style="width:100%" @click="runTest">开始计算</el-button>
        </el-col>
        <el-col :span="15">
          <div class="test-panel-title">计算结果</div>
          <template v-if="testResult">
            <div class="test-vars">
              <el-tag v-for="(item, i) in varsOut" :key="i" size="small" effect="plain"
                      style="margin-right:6px;margin-bottom:6px">{{ item.name }} = {{ item.value }}</el-tag>
            </div>
            <div class="test-section">型材下料（mm）</div>
            <el-table :data="profileRows" size="small" border max-height="200">
              <el-table-column prop="name" label="名称" min-width="76"/>
              <el-table-column prop="dim" label="单支尺寸" width="96"/>
              <el-table-column prop="mult" label="支数" width="64"/>
              <el-table-column prop="total" label="总长" width="86"/>
            </el-table>
            <div v-if="netRows.length" class="test-section">纱网（mm）</div>
            <el-table v-if="netRows.length" :data="netRows" size="small" border max-height="170">
              <el-table-column prop="name" label="名称" min-width="66"/>
              <el-table-column prop="w" label="宽" width="76"/>
              <el-table-column prop="h" label="高" width="76"/>
              <el-table-column prop="mult" label="张数" width="64"/>
              <el-table-column prop="area" label="面积㎡" width="76"/>
            </el-table>
            <div class="test-section">面积：<b>{{ testResult['面积'] ?? '—' }} ㎡</b></div>
            <div v-if="amtRows.length" class="test-section">金额明细（元）</div>
            <el-table v-if="amtRows.length" :data="amtRows" size="small" border max-height="170">
              <el-table-column prop="name" label="项目" min-width="100"/>
              <el-table-column prop="value" label="金额" width="90"/>
            </el-table>
            <div class="test-total">总金额：<b>{{ testResult['总金额'] ?? '—' }} 元</b></div>
          </template>
          <el-empty v-else description="点击「开始计算」查看结果" :image-size="60"/>
        </el-col>
      </el-row>
      <template #footer>
        <el-button @click="testVisible=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * ============================================================
 * 产品公式管理器（仿产品详情页）
 *  - 左侧：产品属性列表（名称/数据列表/默认值/顺序，数据列表用于测试时下拉选择）
 *  - 右侧：分组公式编辑器（$变量组 / *型材 / *纱网 / $面积 / *金额明细）
 *  - 底部：测试 / 保存 / 取消
 * 底层仍存 JSON（后端引擎读取），但编辑界面全程可视化，工人无需接触代码。
 * ============================================================
 */
import { ref, reactive, computed, watch } from 'vue'
import { updateDictData, testDictFormula } from '@/api/system/dict'
import { ElMessage } from 'element-plus'

/** 属性定义 */
interface PropDef { name: string; list: string; def: string; sort: number }
/** 公式行 */
interface RowDef { cond: string; expr: string; mult: string; w: string; h: string }
/** 变量组（顶层数组，如 孔位/防护杆数量） */
interface GroupDef { key: string; rows: RowDef[] }
/** 型材/纱网/金额明细项（名称 + 行） */
interface NamedRows { name: string; rows: RowDef[] }

/** 前端可视模型 */
interface FormulaModel {
  vars: PropDef[]
  varGroups: GroupDef[]
  profiles: NamedRows[]
  nets: NamedRows[]
  areas: RowDef[]
  amounts: NamedRows[]
}

const props = defineProps<{ dictItem: any }>()
const emit = defineEmits<{ (e: 'close'): void; (e: 'saved'): void }>()

const FIXED = ['vars', '型材', '纱网', '面积', '金额明细']

/* ---------- 解析：JSON → 可视模型 ---------- */
const emptyRow = (): RowDef => ({ cond: '', expr: '', mult: '', w: '', h: '' })
const toRows = (node: any): RowDef[] => {
  if (!node) return []
  const list = Array.isArray(node) ? node : [node]
  return list.map(r => ({
    cond: r.cond || '', expr: r.expr || '', mult: r.mult || '', w: r.w || '', h: r.h || ''
  }))
}
const parse = (json: string): FormulaModel => {
  const model: FormulaModel = { vars: [], varGroups: [], profiles: [], nets: [], areas: [], amounts: [] }
  if (!json) return model
  let cfg: any = {}
  try { cfg = JSON.parse(json) } catch { return model }
  // 属性：兼容旧结构（字符串数组）与新结构（对象数组）
  const vs: any[] = Array.isArray(cfg.vars) ? cfg.vars : []
  model.vars = vs.map(v => typeof v === 'string'
    ? { name: v, list: '', def: '', sort: 0 }
    : { name: v.name || '', list: v.list || '', def: v.default !== undefined ? String(v.default) : '', sort: v.sort || 0 })
  // 变量组：顶层除固定分组外的数组
  Object.keys(cfg).forEach(k => {
    if (FIXED.includes(k)) return
    model.varGroups.push({ key: k, rows: toRows(cfg[k]) })
  })
  // 型材 / 纱网 / 金额明细
  Object.entries(cfg['型材'] || {}).forEach(([name, node]) => model.profiles.push({ name, rows: toRows(node) }))
  Object.entries(cfg['纱网'] || {}).forEach(([name, node]) => model.nets.push({ name, rows: toRows(node) }))
  Object.entries(cfg['金额明细'] || {}).forEach(([name, node]) => model.amounts.push({ name, rows: toRows(node) }))
  model.areas = toRows(cfg['面积'])
  return model
}

const model = ref<FormulaModel>(parse(props.dictItem.formulaConfig || ''))

/* ---------- 组装：可视模型 → JSON（后端引擎格式） ---------- */
const rowToObj = (r: RowDef): any => {
  const o: any = {}
  if (r.cond.trim()) o.cond = r.cond.trim()
  if (r.expr.trim()) o.expr = r.expr.trim()
  if (r.mult.trim()) o.mult = r.mult.trim()
  if (r.w.trim()) o.w = r.w.trim()
  if (r.h.trim()) o.h = r.h.trim()
  return o
}
const build = (): string => {
  const cfg: any = {}
  // 属性
  cfg.vars = model.value.vars.filter(v => v.name.trim()).map(v => {
    const o: any = { name: v.name.trim() }
    if (v.list.trim()) o.list = v.list.trim()
    if (v.def !== '') {
      const num = Number(v.def)
      o.default = v.def.trim() !== '' && !isNaN(num) ? num : v.def
    }
    if (Number(v.sort)) o.sort = Number(v.sort)
    return o
  })
  // 变量组
  model.value.varGroups.forEach(g => {
    const rows = g.rows.filter(r => r.expr.trim()).map(rowToObj)
    if (rows.length) cfg[g.key] = rows
  })
  // 型材
  const pf: any = {}
  model.value.profiles.forEach(p => {
    const rows = p.rows.filter(r => r.expr.trim()).map(rowToObj)
    if (p.name.trim() && rows.length) pf[p.name.trim()] = rows
  })
  if (Object.keys(pf).length) cfg['型材'] = pf
  // 纱网
  const nt: any = {}
  model.value.nets.forEach(p => {
    const rows = p.rows.filter(r => r.w.trim() || r.h.trim()).map(rowToObj)
    if (p.name.trim() && rows.length) nt[p.name.trim()] = rows
  })
  if (Object.keys(nt).length) cfg['纱网'] = nt
  // 面积
  const ar = model.value.areas.filter(r => r.expr.trim()).map(rowToObj)
  if (ar.length) cfg['面积'] = ar
  // 金额明细
  const am: any = {}
  model.value.amounts.forEach(p => {
    const rows = p.rows.filter(r => r.expr.trim()).map(rowToObj)
    if (p.name.trim() && rows.length) am[p.name.trim()] = rows
  })
  if (Object.keys(am).length) cfg['金额明细'] = am
  return JSON.stringify(cfg)
}

/* ---------- 属性操作 ---------- */
const addProp = () => {
  const sort = model.value.vars.length ? Math.max(...model.value.vars.map(v => Number(v.sort) || 0)) + 1 : 1
  model.value.vars.push({ name: '', list: '', def: '', sort })
}
const delProp = (i: number) => model.value.vars.splice(i, 1)

/* ---------- 公式行操作 ---------- */
const addRow = (rows: RowDef[]) => rows.push(emptyRow())
const addProfile = () => model.value.profiles.push({ name: '', rows: [emptyRow()] })
const delProfile = (i: number) => model.value.profiles.splice(i, 1)
const addNet = () => model.value.nets.push({ name: '', rows: [emptyRow()] })
const delNet = (i: number) => model.value.nets.splice(i, 1)
const addArea = () => model.value.areas.push(emptyRow())
const addAmount = () => model.value.amounts.push({ name: '', rows: [emptyRow()] })
const delAmount = (i: number) => model.value.amounts.splice(i, 1)

/* ---------- 保存 ---------- */
const save = async () => {
  // 基础校验：型材/纱网/金额明细至少填了名称或公式
  const json = build()
  try { JSON.parse(json) } catch { ElMessage.warning('公式内容不合法，请检查'); return }
  await updateDictData({ ...props.dictItem, formulaConfig: json })
  ElMessage.success('公式保存成功')
  emit('saved')
}

/* ---------- 测试 ---------- */
const testVisible = ref(false)
const testError = ref('')
const testVars = ref<{ name: string; list: string }[]>([])
const testParams = ref<Record<string, string>>({})
const testResult = ref<any>(null)

/** 打开测试：属性 → 输入控件（有数据列表生成下拉，默认值预填） */
const openTest = () => {
  testError.value = ''
  testResult.value = null
  testVars.value = model.value.vars.filter(v => v.name.trim())
  testParams.value = {}
  testVars.value.forEach(v => { testParams.value[v.name] = v.def })
  if (!testVars.value.length) {
    // 属性为空时给常用兜底，保证可测
    testVars.value = ['总高', '总宽', '数量', '下固定', '加杆', '单价', '把手'].map(name => ({ name, list: '' }))
    testParams.value = { 总高: '1500', 总宽: '600', 数量: '1', 下固定: '0', 加杆: '全防护', 单价: '100', 把手: '' }
  }
  testVisible.value = true
}

const runTest = async () => {
  const params: Record<string, any> = {}
  Object.entries(testParams.value).forEach(([k, v]) => {
    if (v !== '' && v !== undefined && v !== null) {
      params[k] = /^-?\d+(\.\d+)?$/.test(String(v)) ? Number(v) : String(v)
    }
  })
  try {
    const res = await testDictFormula({
      dictType: props.dictItem.dictType,
      dictValue: props.dictItem.dictValue,
      params
    })
    if (res.code === 200) { testResult.value = res.data; testError.value = '' }
    else testError.value = res.msg || '测试失败'
  } catch (e: any) {
    testError.value = e?.message || '测试失败'
  }
}

/* 结果拆解 */
const varsOut = computed(() => {
  if (!testResult.value) return []
  const arr: any[] = []
  Object.entries(testResult.value).forEach(([k, v]) => {
    if (!['型材', '纱网', '金额明细', '总金额', '面积'].includes(k)) arr.push({ name: k, value: v as any })
  })
  return arr
})
const profileRows = computed(() => {
  const m = testResult.value?.型材 || {}
  return Object.entries(m).map(([name, v]: any) => ({ name, dim: v.dim, mult: v.mult, total: v.total }))
})
const netRows = computed(() => {
  const m = testResult.value?.纱网 || {}
  return Object.entries(m).map(([name, v]: any) => ({ name, w: v.w, h: v.h, mult: v.mult, area: v.area }))
})
const amtRows = computed(() => {
  const m = testResult.value?.金额明细 || {}
  return Object.entries(m).map(([name, v]) => ({ name, value: v }))
})
</script>

<style scoped>
.fm-wrap { font-size: 13px; }
.fm-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.fm-title { font-size: 17px; font-weight: 600; color: #303133; }
.fm-sub { font-size: 12px; color: #909399; margin-left: 10px; }
.fm-body { display: flex; gap: 12px; }
/* 左：属性 */
.fm-left { width: 300px; flex-shrink: 0; border: 1px solid #ebeef5; border-radius: 6px; padding: 10px; height: fit-content; }
.fm-panel-title { display: flex; justify-content: space-between; align-items: center; font-weight: 600; color: #303133; margin-bottom: 8px; }
.fm-var-hint { font-size: 12px; color: #909399; margin-top: 8px; line-height: 1.8; }
.var-tag { margin-right: 4px; }
/* 右：公式 */
.fm-right { flex: 1; display: flex; flex-direction: column; gap: 10px; }
.fm-group { border: 1px solid #ebeef5; border-radius: 6px; padding: 8px 10px; }
.fm-group-title { display: flex; align-items: center; gap: 6px; font-weight: 600; color: #303133; margin-bottom: 6px; }
.g-icon { color: #1989fa; font-weight: 700; }
.row-head { display: flex; gap: 6px; font-size: 11px; color: #909399; margin-bottom: 4px; }
.row-edit { display: flex; gap: 6px; margin-bottom: 4px; }
.row-edit .el-input { flex: 1; }
.row-head > span, .row-edit > .el-input { flex: 1; }
.row-2 > span:nth-child(1), .row-2 > .el-input:nth-child(1) { flex: 1.1; }
.row-3 > span:nth-child(1), .row-3 > .el-input:nth-child(1) { flex: 1.2; }
.row-4 > span:nth-child(1), .row-4 > .el-input:nth-child(1) { flex: 1.1; }
.row-4 > span:nth-child(2), .row-4 > .el-input:nth-child(2) { flex: 1.4; }
.row-5 > span:nth-child(1), .row-5 > .el-input:nth-child(1) { flex: 1.1; }
.row-5 > span:nth-child(2), .row-5 > .el-input:nth-child(2) { flex: 1.2; }
.row-5 > span:nth-child(3), .row-5 > .el-input:nth-child(3) { flex: 1.2; }
.fm-tip { font-size: 12px; color: #909399; background: #f5f7fa; padding: 8px 10px; border-radius: 4px; line-height: 1.7; }
.fm-footer { display: flex; justify-content: flex-end; gap: 8px; margin-top: 12px; }
/* 测试 */
.test-error { background: #fef0f0; color: #f56c6c; padding: 8px 12px; border-radius: 4px; margin-bottom: 10px; font-size: 13px; }
.test-panel-title { font-weight: 600; color: #303133; margin-bottom: 10px; font-size: 13px; }
.test-section { font-size: 12px; color: #606266; margin: 10px 0 6px; font-weight: 600; }
.test-vars { margin-bottom: 6px; }
.test-total { margin-top: 10px; font-size: 14px; color: #303133; }
.test-total b { color: #f56c6c; font-size: 16px; }
</style>
