<template>
  <!-- ============================================================
       公式管理 v2（清爽版）：
       - 顶部 Tab 切换：属性 / 中间量 / 型材 / 纱网 / 面积 / 金额（一次只看一组，不再堆一屏）
       - 组内表格化对齐：列宽固定、表头清晰、行高紧凑
       - 条件用弹层选择，不再把行撑高；翻译绿字保留在输入框正下方
       ============================================================ -->
  <div class="fm-wrap">
    <!-- 顶部：产品名 + 操作 -->
    <div class="fm-head">
      <div>
        <span class="fm-title">产品：{{ dictItem.dictLabel }}</span>
        <span class="fm-sub">配置下料 / 剪网 / 面积 / 金额计算公式（保存后下单自动计算）</span>
        <el-tag v-if="dictItem.formulaConfig" type="success" effect="plain" size="small" style="margin-left:8px">已配置</el-tag>
      </div>
      <div class="fm-head-btns">
        <el-button @click="emit('close')">返回</el-button>
        <el-button type="warning" @click="openTest">测试公式</el-button>
        <el-button type="primary" @click="save">保存公式</el-button>
      </div>
    </div>

    <!-- 可用变量条：点击插入当前输入框 -->
    <div class="fm-varbar">
      <span class="fm-varbar-label">可用变量（先点输入框，再点下面变量即插入）：</span>
      <el-tag v-for="p in model.vars.filter(v=>v.name.trim())" :key="p.name" size="small"
              effect="plain" class="var-tag" @click="insertVar(p.name)">{{ p.name }}</el-tag>
      <el-tag v-for="g in model.varGroups" :key="'g'+g.key" size="small" type="info"
              effect="plain" class="var-tag" @click="insertVar(g.key)">{{ g.key }}</el-tag>
    </div>

    <!-- Tab 主体 -->
    <el-tabs v-model="activeTab" class="fm-tabs">

      <!-- ========== Tab1：产品属性 ========== -->
      <el-tab-pane label="① 产品属性" name="vars">
        <div class="pane-head">
          <span class="pane-tip">客户下单时要选的尺寸/颜色/选项；默认值会自动预填</span>
          <el-button size="small" type="primary" plain @click="addProp">＋ 添加属性</el-button>
        </div>
        <el-table :data="model.vars" size="small" border stripe>
          <el-table-column label="属性名称" min-width="140">
            <template #default="s"><el-input v-model="s.row.name" size="small" placeholder="如：总高"/></template>
          </el-table-column>
          <el-table-column label="可选项（逗号分隔）" min-width="220">
            <template #default="s"><el-input v-model="s.row.list" size="small" placeholder="如：灰色,白色"/></template>
          </el-table-column>
          <el-table-column label="默认值" width="120">
            <template #default="s"><el-input v-model="s.row.def" size="small" placeholder="可空"/></template>
          </el-table-column>
          <el-table-column label="顺序" width="80">
            <template #default="s"><el-input v-model="s.row.sort" size="small"/></template>
          </el-table-column>
          <el-table-column width="50" align="center">
            <template #default="s"><el-button text type="danger" size="small" @click="delProp(s.$index)">删</el-button></template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ========== Tab2：计算中间量 ========== -->
      <el-tab-pane label="② 计算中间量" name="mid">
        <div class="pane-head">
          <span class="pane-t">中间量 = 先算出一个中间数（如孔位数量、防护杆数），后面公式可直接引用它；留空即不需要</span>
          <el-button size="small" type="primary" plain @click="addGroup">＋ 添加中间量</el-button>
        </div>
        <div v-for="(g, gi) in model.varGroups" :key="gi" class="sub-card">
          <div class="sub-card-head">
            <el-input v-model="g.key" size="small" style="width:180px" placeholder="中间量名，如：孔位"/>
            <el-button text type="danger" size="small" @click="model.varGroups.splice(gi,1)">删除此中间量</el-button>
          </div>
          <div v-for="(r, i) in g.rows" :key="i" class="mini-row">
            <span class="mini-label">条件</span>
            <div class="mini-cell">
              <el-input v-model="r.cond" size="small" placeholder="如：下固定=0（空=始终成立）" @focus="onFocus($event, v=>r.cond=v)"/>
              <cond-picker :row="r" :vars="condVarNames" :var-list="condVarList" @applied="r._condShow=false"/>
              <div class="trans" :class="{err: tCond(r.cond).err.length}">{{ condHint(r.cond) }}</div>
            </div>
            <span class="mini-label">公式</span>
            <div class="mini-cell grow">
              <el-input v-model="r.expr" size="small" placeholder="如：(总高-100)/500" @focus="onFocus($event, v=>r.expr=v)"/>
              <div class="trans" :class="{err: tExpr(r.expr).err.length}">{{ exprHint(r.expr) }}</div>
            </div>
            <el-button text type="danger" size="small" @click="g.rows.splice(i,1)">删行</el-button>
          </div>
          <el-button size="small" text type="primary" @click="addRow(g.rows)">＋ 加一行（多条件分支）</el-button>
        </div>
      </el-tab-pane>

      <!-- ========== Tab3：型材下料 ========== -->
      <el-tab-pane label="③ 型材下料" name="profile">
        <div class="pane-head">
          <span class="pane-tip">每根型材切多长、切几支；公式里可用上面所有变量</span>
          <el-button size="small" type="primary" plain @click="addProfile">＋ 添加型材</el-button>
        </div>
        <el-table :data="model.profiles" size="small" border stripe>
          <el-table-column label="型材名称" min-width="120">
            <template #default="s"><el-input v-model="s.row.name" size="small" placeholder="如：外框宽"/></template>
          </el-table-column>
          <el-table-column label="单支尺寸公式 (mm)" min-width="200">
            <template #default="s">
              <el-input v-model="s.row.rows[0].expr" size="small" placeholder="如：总宽-40" @focus="onFocus($event, v=>s.row.rows[0].expr=v)"/>
              <div class="trans" :class="{err: tExpr(s.row.rows[0].expr).err.length}">{{ exprHint(s.row.rows[0].expr) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="数量公式" min-width="150">
            <template #default="s">
              <el-input v-model="s.row.rows[0].mult" size="small" placeholder="如：2*数量" @focus="onFocus($event, v=>s.row.rows[0].mult=v)"/>
              <div class="trans" :class="{err: tExpr(s.row.rows[0].mult).err.length}">{{ exprHint(s.row.rows[0].mult) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="适用条件（可空）" min-width="200">
            <template #default="s">
              <div class="cond-cell">
                <el-input v-model="s.row.rows[0].cond" size="small" placeholder="空=始终成立" @focus="onFocus($event, v=>s.row.rows[0].cond=v)"/>
                <cond-picker :row="s.row.rows[0]" :vars="condVarNames" :var-list="condVarList" @applied="s.row.rows[0]._condShow=false"/>
              </div>
              <div class="trans" :class="{err: tCond(s.row.rows[0].cond).err.length}">{{ condHint(s.row.rows[0].cond) }}</div>
            </template>
          </el-table-column>
          <el-table-column width="60" align="center">
            <template #default="s"><el-button text type="danger" size="small" @click="delProfile(s.$index)">删</el-button></template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ========== Tab4：纱网剪网 ========== -->
      <el-tab-pane label="④ 纱网剪网" name="net">
        <div class="pane-head">
          <span class="pane-tip">每片纱网剪多宽多高、剪几张</span>
          <el-button size="small" type="primary" plain @click="addNet">＋ 添加纱网</el-button>
        </div>
        <el-table :data="model.nets" size="small" border stripe>
          <el-table-column label="纱网名称" min-width="110">
            <template #default="s"><el-input v-model="s.row.name" size="small" placeholder="如：上网"/></template>
          </el-table-column>
          <el-table-column label="宽公式 (mm)" min-width="150">
            <template #default="s">
              <el-input v-model="s.row.rows[0].w" size="small" placeholder="如：内扇宽+15" @focus="onFocus($event, v=>s.row.rows[0].w=v)"/>
              <div class="trans" :class="{err: tExpr(s.row.rows[0].w).err.length}">{{ exprHint(s.row.rows[0].w) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="高公式 (mm)" min-width="150">
            <template #default="s">
              <el-input v-model="s.row.rows[0].h" size="small" placeholder="如：内扇高-10" @focus="onFocus($event, v=>s.row.rows[0].h=v)"/>
              <div class="trans" :class="{err: tExpr(s.row.rows[0].h).err.length}">{{ exprHint(s.row.rows[0].h) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="张数公式" min-width="130">
            <template #default="s">
              <el-input v-model="s.row.rows[0].mult" size="small" placeholder="如：1*数量" @focus="onFocus($event, v=>s.row.rows[0].mult=v)"/>
              <div class="trans" :class="{err: tExpr(s.row.rows[0].mult).err.length}">{{ exprHint(s.row.rows[0].mult) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="适用条件（可空）" min-width="180">
            <template #default="s">
              <div class="cond-cell">
                <el-input v-model="s.row.rows[0].cond" size="small" placeholder="空=始终成立" @focus="onFocus($event, v=>s.row.rows[0].cond=v)"/>
                <cond-picker :row="s.row.rows[0]" :vars="condVarNames" :var-list="condVarList" @applied="s.row.rows[0]._condShow=false"/>
              </div>
              <div class="trans" :class="{err: tCond(s.row.rows[0].cond).err.length}">{{ condHint(s.row.rows[0].cond) }}</div>
            </template>
          </el-table-column>
          <el-table-column width="60" align="center">
            <template #default="s"><el-button text type="danger" size="small" @click="delNet(s.$index)">删</el-button></template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ========== Tab5：面积 ========== -->
      <el-tab-pane label="⑤ 面积（㎡）" name="area">
        <div class="pane-head">
          <span class="pane-tip">用于算金额；同名可写多档（如总高>1399 与 >1499 两档叠加）</span>
          <el-button size="small" type="primary" plain @click="addArea">＋ 添加行</el-button>
        </div>
        <el-table :data="model.areas" size="small" border stripe>
          <el-table-column label="面积公式 (㎡)" min-width="280">
            <template #default="s">
              <el-input v-model="s.row.expr" size="small" placeholder="如：总高*总宽*0.000001*数量" @focus="onFocus($event, v=>s.row.expr=v)"/>
              <div class="trans" :class="{err: tExpr(s.row.expr).err.length}">{{ exprHint(s.row.expr) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="适用条件（可空）" min-width="240">
            <template #default="s">
              <div class="cond-cell">
                <el-input v-model="s.row.cond" size="small" placeholder="空=始终成立" @focus="onFocus($event, v=>s.row.cond=v)"/>
                <cond-picker :row="s.row" :vars="condVarNames" :var-list="condVarList" @applied="s.row._condShow=false"/>
              </div>
              <div class="trans" :class="{err: tCond(s.row.cond).err.length}">{{ condHint(s.row.cond) }}</div>
            </template>
          </el-table-column>
          <el-table-column width="60" align="center">
            <template #default="s"><el-button text type="danger" size="small" @click="model.areas.splice(s.$index,1)">删</el-button></template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ========== Tab6：金额明细 ========== -->
      <el-tab-pane label="⑥ 金额明细" name="amount">
        <div class="pane-head">
          <span class="pane-tip">分项金额会自动累加为总金额；同名多行自动累加</span>
          <el-button size="small" type="primary" plain @click="addAmount">＋ 添加金额项</el-button>
        </div>
        <el-table :data="model.amounts" size="small" border stripe>
          <el-table-column label="项目名称" min-width="140">
            <template #default="s"><el-input v-model="s.row.name" size="small" placeholder="如：基本金额"/></template>
          </el-table-column>
          <el-table-column label="金额公式 (元)" min-width="240">
            <template #default="s">
              <el-input v-model="s.row.rows[0].expr" size="small" placeholder="如：单价*面积" @focus="onFocus($event, v=>s.row.rows[0].expr=v)"/>
              <div class="trans" :class="{err: tExpr(s.row.rows[0].expr).err.length}">{{ exprHint(s.row.rows[0].expr) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="适用条件（可空）" min-width="220">
            <template #default="s">
              <div class="cond-cell">
                <el-input v-model="s.row.rows[0].cond" size="small" placeholder="空=始终成立" @focus="onFocus($event, v=>s.row.rows[0].cond=v)"/>
                <cond-picker :row="s.row.rows[0]" :vars="condVarNames" :var-list="condVarList" @applied="s.row.rows[0]._condShow=false"/>
              </div>
              <div class="trans" :class="{err: tCond(s.row.rows[0].cond).err.length}">{{ condHint(s.row.rows[0].cond) }}</div>
            </template>
          </el-table-column>
          <el-table-column width="60" align="center">
            <template #default="s"><el-button text type="danger" size="small" @click="delAmount(s.$index)">删</el-button></template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 小贴士 -->
    <div class="fm-tip">
      <b>用法：</b>① 先点输入框，再点上方变量自动插入；② 条件点「选」下拉选，不用手写；
      ③ 输入框下绿字实时翻译，红字=有字写错；④ 条件空=始终成立；⑤ 改完记得点右上角「保存公式」。
    </div>

    <!-- 测试弹窗 -->
    <el-dialog v-model="testVisible" title="公式测试" width="920px" append-to-body :close-on-click-modal="false">
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
              <el-table-column prop="dim" label="单支尺寸" width="100"/>
              <el-table-column prop="mult" label="支数" width="70"/>
              <el-table-column prop="total" label="总长" width="90"/>
            </el-table>
            <div v-if="netRows.length" class="test-section">纱网（mm）</div>
            <el-table v-if="netRows.length" :data="netRows" size="small" border max-height="170">
              <el-table-column prop="name" label="名称" min-width="66"/>
              <el-table-column prop="w" label="宽" width="80"/>
              <el-table-column prop="h" label="高" width="80"/>
              <el-table-column prop="mult" label="张数" width="70"/>
              <el-table-column prop="area" label="面积㎡" width="80"/>
            </el-table>
            <div class="test-section">面积：<b>{{ testResult['面积'] ?? '—' }} ㎡</b></div>
            <div v-if="amtRows.length" class="test-section">金额明细（元）</div>
            <el-table v-if="amtRows.length" :data="amtRows" size="small" border max-height="170">
              <el-table-column prop="name" label="项目" min-width="100"/>
              <el-table-column prop="value" label="金额" width="100"/>
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
 * 产品公式管理器 v2（清爽 Tab 版）
 *  - 底层仍存 JSON（后端公式引擎读取），编辑界面全程可视化
 *  - v2 改动：5 组公式全部堆一屏 → 顶部 Tab 分组切换；行改表格化对齐；条件改弹层
 *  - 不改变任何数据格式（parse/build 与后端引擎协议保持一致）
 * ============================================================
 */
import { ref, computed } from 'vue'
import { updateDictData, testDictFormula } from '@/api/system/dict'
import { ElMessage } from 'element-plus'
import CondPicker from './CondPicker.vue'

/** 属性定义 */
interface PropDef { name: string; list: string; def: string; sort: number }
/** 公式行（_cond* 为条件下拉构造器的临时编辑状态，不入库） */
interface RowDef {
  cond: string; expr: string; mult: string; w: string; h: string
  _condShow?: boolean; _condVar?: string; _condOp?: string; _condVal?: string
}
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
const activeTab = ref('vars')

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
  const vs: any[] = Array.isArray(cfg.vars) ? cfg.vars : []
  model.vars = vs.map(v => typeof v === 'string'
    ? { name: v, list: '', def: '', sort: 0 }
    : { name: v.name || '', list: v.list || '', def: v.default !== undefined ? String(v.default) : '', sort: v.sort || 0 })
  Object.keys(cfg).forEach(k => {
    if (FIXED.includes(k)) return
    model.varGroups.push({ key: k, rows: toRows(cfg[k]) })
  })
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
  model.value.varGroups.forEach(g => {
    const rows = g.rows.filter(r => r.expr.trim()).map(rowToObj)
    if (rows.length) cfg[g.key] = rows
  })
  const pf: any = {}
  model.value.profiles.forEach(p => {
    const rows = p.rows.filter(r => r.expr.trim()).map(rowToObj)
    if (p.name.trim() && rows.length) pf[p.name.trim()] = rows
  })
  if (Object.keys(pf).length) cfg['型材'] = pf
  const nt: any = {}
  model.value.nets.forEach(p => {
    const rows = p.rows.filter(r => r.w.trim() || r.h.trim()).map(rowToObj)
    if (p.name.trim() && rows.length) nt[p.name.trim()] = rows
  })
  if (Object.keys(nt).length) cfg['纱网'] = nt
  const ar = model.value.areas.filter(r => r.expr.trim()).map(rowToObj)
  if (ar.length) cfg['面积'] = ar
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
const addGroup = () => model.value.varGroups.push({ key: '', rows: [emptyRow()] })
const addProfile = () => model.value.profiles.push({ name: '', rows: [emptyRow()] })
const delProfile = (i: number) => model.value.profiles.splice(i, 1)
const addNet = () => model.value.nets.push({ name: '', rows: [emptyRow()] })
const delNet = (i: number) => model.value.nets.splice(i, 1)
const addArea = () => model.value.areas.push(emptyRow())
const addAmount = () => model.value.amounts.push({ name: '', rows: [emptyRow()] })
const delAmount = (i: number) => model.value.amounts.splice(i, 1)

/* ============================================================
 * 公式实时翻译 + 不认识的字标红
 * ============================================================ */
const OP_TEXT: Record<string, string> = {
  '+': ' 加 ', '-': ' 减 ', '*': ' 乘 ', '/': ' 除以 ',
  '==': ' 等于 ', '=': ' 等于 ', '>': ' 大于 ', '<': ' 小于 ',
  '>=': ' 大于等于 ', '<=': ' 小于等于 ', '!=': ' 不等于 '
}
const TOKEN_RE = /[0-9]+(?:\.[0-9]+)?|[\u4e00-\u9fa5A-Za-z][\u4e00-\u9fa5A-Za-z0-9]*|>=|<=|==|!=|>|<|=|\+|-|\*|\/|\(|\)/g
const tokenize = (s: string): string[] => {
  const r: string[] = []
  TOKEN_RE.lastIndex = 0
  let m: RegExpExecArray | null
  while ((m = TOKEN_RE.exec(s))) r.push(m[0])
  return r
}
const BUILTIN_VARS = ['数量','单价','面积','总高','总宽','下固定','加杆','颜色','网子','把手','把手方向','开向',
  '纱子','纱网型号','纱网颜色','外框颜色','型材颜色','尺寸类型','高度','成功高','孔位','防护杆数量','超高',
  '外框成本单价','纱网成本单价','纱网颜色单价','单价加价','配件成本','人工成本','辅料成本','总成本','毛利润']
const allVarNames = computed(() => {
  const set = new Set<string>()
  model.value.vars.forEach(v => { if (v.name.trim()) set.add(v.name.trim()) })
  model.value.varGroups.forEach(g => set.add(g.key))
  model.value.profiles.forEach(p => { if (p.name.trim()) set.add(p.name.trim()) })
  model.value.nets.forEach(p => { if (p.name.trim()) set.add(p.name.trim()) })
  model.value.amounts.forEach(p => { if (p.name.trim()) set.add(p.name.trim()) })
  BUILTIN_VARS.forEach(n => set.add(n))
  return [...set]
})
const unknownVars = (str: string): string[] => {
  const uniq: string[] = []
  for (const t of tokenize(str)) {
    if (/^[0-9]+(?:\.[0-9]+)?$/.test(t)) continue
    if (OP_TEXT[t] || t === '(' || t === ')') continue
    if (t === 'lwxs' || t === 'jw') continue
    if (!allVarNames.value.includes(t) && !uniq.includes(t)) uniq.push(t)
  }
  return uniq
}
const tExpr = (expr: string): { text: string; err: string[] } => {
  if (!expr.trim()) return { text: '', err: [] }
  let out = ''
  const errs: string[] = []
  for (const t of tokenize(expr)) {
    if (/^[0-9]+(?:\.[0-9]+)?$/.test(t)) { out += t; continue }
    if (OP_TEXT[t]) { out += OP_TEXT[t]; continue }
    if (t === '(') { out += '('; continue }
    if (t === ')') { out += ')'; continue }
    if (t === 'lwxs' || t === 'jw') { out += t; continue }
    if (allVarNames.value.includes(t)) { out += t; continue }
    if (!errs.includes(t)) errs.push(t)
  }
  return { text: out, err: errs }
}
const tCond = (cond: string): { text: string; err: string[] } => {
  if (!cond.trim()) return { text: '空 = 始终成立', err: [] }
  let inner = cond.trim()
  if (inner.startsWith('{') && inner.endsWith('}')) inner = inner.slice(1, -1)
  const segs = inner.split(',')
  const parts: string[] = []
  const errs: string[] = []
  segs.forEach(s => {
    const m = s.match(/^(.+?)(>=|<=|==|!=|=|>|<)(.+)$/)
    if (!m) { parts.push(s); errs.push(...unknownVars(s)); return }
    const vn = m[1].trim(), op = m[2], val = m[3].trim()
    parts.push(`${vn} ${OP_TEXT[op] || op} ${val}`)
    errs.push(...unknownVars(vn))
  })
  return { text: '当 ' + parts.join(' 且 ') + ' 时', err: errs }
}
/** 翻译提示行：表达式 / 条件 */
const exprHint = (expr: string): string => {
  if (!expr.trim()) return ''
  const r = tExpr(expr)
  return r.err.length ? '✗ 不认识：' + r.err.join('、') : '= ' + r.text
}
const condHint = (cond: string): string => {
  if (!cond.trim()) return ''
  const r = tCond(cond)
  return r.err.length ? '✗ ' + r.err.join('、') : r.text
}

/* ---------- 变量点击插入 ---------- */
let focusTarget: { el: HTMLInputElement; set: (v: string) => void } | null = null
const onFocus = (e: FocusEvent, set: (v: string) => void) => {
  focusTarget = { el: e.target as HTMLInputElement, set }
}
const insertVar = (name: string) => {
  if (!focusTarget) { ElMessage.info('请先点击要插入的输入框，再点变量'); return }
  const el = focusTarget.el
  const s = el.selectionStart ?? el.value.length
  const epos = el.selectionEnd ?? el.value.length
  const nv = el.value.slice(0, s) + name + el.value.slice(epos)
  focusTarget.set(nv)
  requestAnimationFrame(() => { el.focus(); el.setSelectionRange(s + name.length, s + name.length) })
}

/* ============================================================
 * 条件构造弹层（CondPicker 独立组件，避免行被撑高）
 * ============================================================ */
const COND_OPS = ['=', '>', '<', '>=', '<=', '!=']
const condVarNames = computed(() => model.value.vars.filter(v => v.name.trim()).map(v => v.name.trim()))
const condVarList = (name?: string): string[] => {
  if (!name) return []
  const v = model.value.vars.find(x => x.name.trim() === name)
  return v && v.list ? v.list.split(/[,，]/).map(s => s.trim()).filter(Boolean) : []
}

/* ---------- 保存 ---------- */
const save = async () => {
  const json = build()
  try { JSON.parse(json) } catch { ElMessage.warning('公式内容不合法，请检查'); return }
  await updateDictData({ ...props.dictItem, formulaConfig: json })
  ElMessage.success('公式保存成功')
  emit('saved')
}

/* ---------- 测试 ---------- */
const testVisible = ref(false)
const testError = ref('')
const testVars = ref<{ name: string; list: string; def?: string }[]>([])
const testParams = ref<Record<string, string>>({})
const testResult = ref<any>(null)

const openTest = () => {
  testError.value = ''
  testResult.value = null
  testVars.value = model.value.vars.filter(v => v.name.trim())
  testParams.value = {}
  testVars.value.forEach(v => { testParams.value[v.name] = v.def ?? '' })
  if (!testVars.value.length) {
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
.fm-head-btns { display: flex; gap: 8px; }

/* 变量条 */
.fm-varbar { background: #f4f7fb; border: 1px solid #e4ecf7; border-radius: 8px;
  padding: 8px 12px; margin-bottom: 12px; display: flex; flex-wrap: wrap; align-items: center; gap: 6px; }
.fm-varbar-label { font-size: 12px; color: #606266; flex-shrink: 0; }
.var-tag { cursor: pointer; user-select: none; transition: all .15s; }
.var-tag:hover { background: #ecf5ff; color: #1989fa; border-color: #a0cfff; }

/* Tab */
.fm-tabs { margin-top: 4px; }
.pane-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.pane-tip { font-size: 12px; color: #909399; }

/* 中间量子卡片 */
.sub-card { border: 1px solid #e4e7ed; border-radius: 8px; padding: 10px 12px; margin-bottom: 10px; background: #fafbfd; }
.sub-card-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.mini-row { display: flex; align-items: flex-start; gap: 8px; margin-bottom: 8px; }
.mini-label { font-size: 12px; color: #909399; width: 40px; padding-top: 6px; flex-shrink: 0; }
.mini-cell { flex: 1; min-width: 0; }
.mini-cell.grow { flex: 1.6; }

/* 翻译行 */
.trans { font-size: 11px; color: #67c23a; line-height: 1.5; margin-top: 2px; min-height: 15px;
  white-space: normal; word-break: break-all; }
.trans.err { color: #f56c6c; }

/* 条件单元格 */
.cond-cell { display: flex; gap: 4px; align-items: center; }
.cond-cell .el-input { flex: 1; }

.fm-tip { font-size: 12px; color: #909399; background: #f8f9fb; border: 1px dashed #dcdfe6;
  padding: 8px 12px; border-radius: 6px; line-height: 1.7; margin-top: 14px; }

/* 测试弹窗 */
.test-error { background: #fef0f0; color: #f56c6c; padding: 8px 12px; border-radius: 4px; margin-bottom: 10px; font-size: 13px; }
.test-panel-title { font-weight: 600; color: #303133; margin-bottom: 10px; font-size: 13px; }
.test-section { font-size: 12px; color: #606266; margin: 10px 0 6px; font-weight: 600; }
.test-vars { margin-bottom: 6px; }
.test-total { margin-top: 10px; font-size: 14px; color: #303133; }
.test-total b { color: #f56c6c; font-size: 16px; }
</style>
