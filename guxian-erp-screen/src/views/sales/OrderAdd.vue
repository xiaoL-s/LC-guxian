<template>
  <div class="order-edit-page">
    <!-- ============ 订单信息 ============ -->
    <el-card shadow="never" class="block-card">
      <template #header>
        <div class="card-head">
          <span class="block-title">订单信息</span>
          <div>
            <el-button size="small" @click="goBack">返回列表</el-button>
            <el-button size="small" type="primary" :loading="saving" @click="submit">保存订单</el-button>
          </div>
        </div>
      </template>

      <el-form :model="order" label-width="82px" size="small">
        <el-row :gutter="10">
          <el-col :span="6">
            <el-form-item label="订单编号">
              <el-input v-model="order.orderNo" readonly placeholder="保存后自动生成" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="客户姓名" required>
              <el-select v-model="order.customerId" filterable clearable placeholder="选择客户" style="width: 100%"
                @change="onCustomerChange">
                <el-option v-for="c in customers" :key="c.customerId" :label="c.customerName" :value="c.customerId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="联系电话">
              <el-input v-model="order.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="物流">
              <el-select v-model="order.logistics" filterable allow-create default-first-option clearable
                placeholder="选择/输入物流" style="width: 100%">
                <el-option v-for="o in dict.logistics" :key="o.value" :label="o.label" :value="o.label" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="终端地址">
              <el-input v-model="order.terminalAddress" placeholder="安装/收货终端地址" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="下单时间">
              <el-date-picker v-model="order.orderDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="交货日期">
              <el-date-picker v-model="order.expectDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item label="品牌">
              <el-input v-model="order.brand" placeholder="品牌" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="订单类型">
              <el-select v-model="order.orderType" style="width: 100%">
                <el-option v-for="t in orderTypeOptions" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="安装方式">
              <el-select v-model="order.installType" filterable allow-create default-first-option style="width: 100%">
                <el-option v-for="t in installTypeOptions" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="单位">
              <el-input v-model="order.unit" placeholder="套" />
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item label="设计师">
              <el-input v-model="order.designer" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="拆单师">
              <el-input v-model="order.splitter" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="业务员">
              <el-input v-model="order.salesman" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="客户来源">
              <el-select v-model="order.customerSource" filterable allow-create default-first-option clearable
                placeholder="选择/输入" style="width: 100%">
                <el-option v-for="o in dict.customer_source" :key="o.value" :label="o.label" :value="o.label" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="6">
            <el-form-item label="投影面积">
              <el-input-number v-model="order.projectionArea" :min="0" :precision="4" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="大板数">
              <el-input-number v-model="order.bigBoardNum" :min="0" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="order.remark" placeholder="订单备注/特殊要求" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- ============ 产品明细 ============ -->
    <el-card shadow="never" class="block-card">
      <template #header>
        <div class="card-head">
          <span class="block-title">产品明细（子单）</span>
          <div class="detail-tools">
            <el-button size="small" @click="openDeduct">扣宽</el-button>
            <el-button size="small" type="primary" plain @click="addProduct">添加产品</el-button>
            <el-button size="small" type="primary" plain @click="insertProduct">插入产品</el-button>
            <el-button size="small" plain disabled>导入产品</el-button>
            <el-button size="small" type="primary" plain @click="appendProduct">追加产品</el-button>
            <el-dropdown trigger="click" @command="onMoreCommand">
              <el-button size="small">更多操作</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="copyInsert">插入复制</el-dropdown-item>
                  <el-dropdown-item command="copy">复制</el-dropdown-item>
                  <el-dropdown-item command="delete">删除</el-dropdown-item>
                  <el-dropdown-item command="print" disabled>打印标签（预留）</el-dropdown-item>
                  <el-dropdown-item command="attach" disabled>附录图（预留）</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </template>

      <el-table ref="itemTableRef" :data="order.itemList" border size="small" height="460"
        :header-cell-style="headerCellStyle"
        :cell-style="{ padding: '1px 2px' }" @row-click="onRowClick">
        <!-- 冻结左侧：产品 / 品目 -->
        <el-table-column label="产品" width="180" fixed="left" show-overflow-tooltip>
          <template #default="s">
            <el-select v-model="s.row.productKey" filterable clearable placeholder="选择产品" size="small"
              style="width: 100%" @change="(v: string) => onProductChange(s.row, v)">
              <el-option-group v-for="g in productGroups" :key="g.typeName" :label="g.typeName">
                <el-option v-for="p in g.items" :key="p.dictType + '-' + p.value" :label="p.label" :value="p.dictType + '|' + p.label + '|' + p.value" />
              </el-option-group>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="品目" width="100" fixed="left">
          <template #default="s"><span class="cell-text">{{ s.row.itemCategory || '—' }}</span></template>
        </el-table-column>

        <!-- ① 尺寸组：总高 / 总宽 / 下固定 -->
        <el-table-column label="尺寸(mm)">
          <el-table-column label="总高" width="82">
            <template #default="s">
              <el-input-number v-model="s.row.height" :min="0" :controls="false" size="small" style="width: 100%" @change="() => calcRow(s.row)" />
            </template>
          </el-table-column>
          <el-table-column label="总宽" width="82">
            <template #default="s">
              <el-input-number v-model="s.row.width" :min="0" :controls="false" size="small" style="width: 100%" @change="() => calcRow(s.row)" />
            </template>
          </el-table-column>
          <el-table-column label="下固定" width="92">
            <template #default="s">
              <el-select v-model="s.row.fixedBottom" filterable allow-create default-first-option clearable size="small" placeholder="" style="width: 100%">
                <el-option v-for="o in attrOptions(s.row, { prop: 'fixedBottom', dictKey: 'fixed_bottom' })" :key="o.value + '-' + o.label" :label="o.label" :value="o.label" />
              </el-select>
            </template>
          </el-table-column>
        </el-table-column>

        <el-table-column label="数量" width="70">
          <template #default="s">
            <el-input-number v-model="s.row.num" :min="1" :controls="false" size="small" style="width: 100%" @change="() => calcRow(s.row)" />
          </template>
        </el-table-column>

        <!-- ② 产品属性组：颜色 / 网子 / 把手 / 把手方向 / 开向 / 加杆 -->
        <el-table-column label="产品属性">
          <el-table-column v-for="col in attrColsMain" :key="col.prop" :label="col.label" :width="col.width">
            <template #default="s">
              <el-select v-model="s.row[col.prop]" filterable allow-create default-first-option clearable size="small"
                placeholder="" style="width: 100%">
                <el-option v-for="o in attrOptions(s.row, col)" :key="o.value + '-' + o.label" :label="o.label" :value="o.label" />
              </el-select>
            </template>
          </el-table-column>
        </el-table-column>

        <!-- ③ 金额组：单价 / 面积 / 计算方式 / 实收金额 -->
        <el-table-column label="金额">
          <el-table-column label="单价" width="86">
            <template #default="s">
              <el-input-number v-model="s.row.unitPrice" :min="0" :precision="2" :controls="false" size="small"
                style="width: 100%" @change="() => calcRow(s.row)" />
            </template>
          </el-table-column>
          <el-table-column label="面积㎡" width="76" align="right">
            <template #default="s">{{ num(s.row.itemTotalArea) }}</template>
          </el-table-column>
          <el-table-column label="计算方式" width="92">
            <template #default="s">
              <el-select v-model="s.row.calcType" size="small" style="width: 100%" @change="() => calcRow(s.row)">
                <el-option label="按面积" :value="1" />
                <el-option label="按件" :value="2" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="实收金额" width="88" align="right">
            <template #default="s">{{ money(s.row.receiveAmount ?? 0) }}</template>
          </el-table-column>
        </el-table-column>

        <el-table-column label="备注" min-width="130">
          <template #default="s"><el-input v-model="s.row.remark" size="small" /></template>
        </el-table-column>
        <el-table-column label="操作" width="128" fixed="right" align="center">
          <template #default="s">
            <el-button size="small" type="primary" link @click.stop="insertProduct(s.$index)">插入</el-button>
            <el-button size="small" type="primary" link @click.stop="copyRow(s.$index)">复制</el-button>
            <el-button size="small" type="danger" link @click.stop="removeRow(s.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="scroll-tip">↔ 列较多，可横向拖动滚动条查看全部列</div>

      <el-alert type="info" :closable="false" style="margin-top: 8px"
        title="宽高单位毫米(mm)；净宽 = 总宽 − 扣宽；单扇面积 = 净宽 × 总高 ÷ 1000000；行金额 = 面积 × 单价（按件时 = 单价 × 数量）。保存时后端按产品字典与上述口径强制复核。" />
    </el-card>

    <!-- ============ 费用与合计 ============ -->
    <el-card shadow="never" class="block-card">
      <el-row :gutter="12">
        <el-col :span="13">
          <el-form :model="order" label-width="88px" size="small">
            <el-row :gutter="10">
              <el-col :span="8"><el-form-item label="工艺加价">
                <el-input-number v-model="order.craftFee" :min="0" :precision="2" :controls="false" style="width: 100%" />
              </el-form-item></el-col>
              <el-col :span="8"><el-form-item label="加急费">
                <el-input-number v-model="order.urgentFee" :min="0" :precision="2" :controls="false" style="width: 100%" />
              </el-form-item></el-col>
              <el-col :span="8"><el-form-item label="运费">
                <el-input-number v-model="order.freight" :min="0" :precision="2" :controls="false" style="width: 100%" />
              </el-form-item></el-col>
              <el-col :span="8"><el-form-item label="优惠减价">
                <el-input-number v-model="order.discountAmount" :min="0" :precision="2" :controls="false" style="width: 100%" />
              </el-form-item></el-col>
              <el-col :span="8"><el-form-item label="制单人">
                <el-input :model-value="order.createBy ? String(order.createBy) : '当前登录用户'" readonly />
              </el-form-item></el-col>
              <el-col :span="8"><el-form-item label="子单数量">
                <el-input :model-value="String(order.itemList.length)" readonly />
              </el-form-item></el-col>
            </el-row>
          </el-form>
        </el-col>
        <el-col :span="11">
          <div class="sum-box">
            <div class="sum-row"><span>总数量</span><b>{{ totalNum }} 套/扇</b></div>
            <div class="sum-row"><span>总面积</span><b>{{ num(totalArea) }} ㎡</b></div>
            <div class="sum-row"><span>产品金额</span><b>￥{{ money(productAmount) }}</b></div>
            <div class="sum-row"><span>费用合计（工艺+加急+运费−优惠）</span><b>￥{{ money(feeAmount) }}</b></div>
            <div class="sum-row sum-total"><span>订单总金额</span><b class="price">￥{{ money(totalAmount) }}</b></div>
          </div>
        </el-col>
      </el-row>

      <div class="footer-bar">
        <el-button size="small" @click="recalcAll">重新计算</el-button>
        <el-button size="small" @click="resetForm">重置</el-button>
        <el-button size="small" disabled>经销商单（预留）</el-button>
        <el-button size="small" type="primary" :loading="saving" @click="submit">保存订单</el-button>
      </div>
    </el-card>

    <!-- ============ 公式计算预览弹窗 ============ -->
    <el-dialog v-model="formulaDialogVisible" title="公式计算结果预览（下料 / 剪网 / 金额）" width="820px" top="8vh">
      <div v-loading="formulaLoading" class="formula-preview">
        <template v-if="formulaResult">
          <!-- 顶层独立结果（孔位/防护杆数量等变量） -->
          <div v-if="topVars.length" class="fp-top-vars">
            <el-tag v-for="(v, k) in topVars" :key="k" type="info" effect="plain" style="margin-right:8px">
              {{ k }}：{{ v }}
            </el-tag>
          </div>

          <!-- 型材：下料尺寸 -->
          <div class="fp-block">
            <div class="fp-title">型材下料</div>
            <el-table :data="materialRows" size="small" border :show-header="true" max-height="260">
              <el-table-column prop="name" label="名称" min-width="90" />
              <el-table-column prop="dim" label="单支尺寸(mm)" width="110" align="right" />
              <el-table-column prop="mult" label="数量(支)" width="90" align="right" />
              <el-table-column prop="total" label="合计(mm)" width="110" align="right" />
            </el-table>
          </div>

          <!-- 纱网：剪网尺寸 -->
          <div class="fp-block">
            <div class="fp-title">纱网剪网</div>
            <el-table :data="netRows" size="small" border max-height="220">
              <el-table-column prop="name" label="名称" min-width="90" />
              <el-table-column prop="w" label="宽(mm)" width="100" align="right" />
              <el-table-column prop="h" label="高(mm)" width="100" align="right" />
              <el-table-column prop="mult" label="张数" width="80" align="right" />
              <el-table-column prop="area" label="面积(㎡)" width="100" align="right" />
            </el-table>
          </div>

          <!-- 面积与金额 -->
          <div class="fp-summary">
            <div class="fp-sum-item"><span>面积</span><b>{{ formulaResult.面积 ?? '—' }} ㎡</b></div>
            <div class="fp-sum-item" v-for="(v, k) in amountRows" :key="k"><span>{{ k }}</span><b>￥{{ v }}</b></div>
            <div class="fp-sum-item fp-total"><span>总金额</span><b class="fp-price">￥{{ formulaResult.总金额 ?? '—' }}</b></div>
          </div>
        </template>
        <el-empty v-else-if="!formulaLoading" description="请选择有公式的产品并填写总宽、总高、数量后点击计算" />
      </div>
      <template #footer>
        <el-button @click="formulaDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="formulaLoading" @click="openFormula(formulaRow)">重新计算</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDictOptions, getOrderInfo, saveOrder } from '@/api/sales/order'
import { getAllCustomerList } from '@/api/customer/customer'
import { testDictFormula } from '@/api/system/dict'
import { ORDER_TYPES, INSTALL_TYPES, type DictOption } from '@/types/sales'

const route = useRoute()
const router = useRouter()

const customers = ref<any[]>([])
const saving = ref(false)
const itemTableRef = ref()
const selectedRows = ref<any[]>([])
const currentRowIndex = ref(0)

/** 字典选项 */
const dict = reactive<Record<string, DictOption[]>>({
  color: [], net: [], material: [], handle: [], lock: [], handle_direction: [],
  add_rod: [], fixed_bottom: [], square_board: [], open_direction: [],
  order_type: [], install_type: [], customer_source: [], logistics: [], unit: [], product: []
})

/** 明细行的属性列（字典有数据即为下拉，无数据可自由输入） */
const attrCols = [
  { prop: 'color', label: '颜色', width: 104, dictKey: 'color' },
  { prop: 'netMaterial', label: '网子', width: 104, dictKey: 'net' },
  { prop: 'material', label: '材质', width: 100, dictKey: 'material' },
  { prop: 'handle', label: '把手', width: 100, dictKey: 'handle' },
  { prop: 'lockSet', label: '锁具', width: 100, dictKey: 'lock' },
  { prop: 'handleDirection', label: '把手方向', width: 96, dictKey: 'handle_direction' },
  { prop: 'openDirection', label: '开向', width: 88, dictKey: 'open_direction' },
  { prop: 'addRod', label: '加杆', width: 90, dictKey: 'add_rod' },
  { prop: 'fixedBottom', label: '下固定', width: 96, dictKey: 'fixed_bottom' },
  { prop: 'squareBoard', label: '方板', width: 96, dictKey: 'square_board' }
]

/** 属性组渲染列：仅保留下单常用 6 个属性（颜色/网子/把手/把手方向/开向/加杆）；
 *  下固定已单独放在"尺寸"组展示；材质/锁具/方板等未启用列从表格移除（字段仍随单保存） */
const attrColsMain = attrCols.filter(c => ['color', 'netMaterial', 'handle', 'handleDirection', 'openDirection', 'addRod'].includes(c.prop))

/** 分组表头样式：分组父列（有子列）用浅蓝底区分，普通列灰底 */
const headerCellStyle = ({ column }: any) => {
  const base = { background: '#f5f7fa', fontSize: '12px', padding: '3px 2px', fontWeight: 600, color: '#303133' }
  if (column.children && column.children.length) {
    return { ...base, background: '#e9f0fb', color: '#1f4e9c' }
  }
  return base
}

const orderTypeOptions = computed(() => dict.order_type.length ? dict.order_type.map(o => o.label) : ORDER_TYPES)
const installTypeOptions = computed(() => dict.install_type.length ? dict.install_type.map(o => o.label) : INSTALL_TYPES)

/** 产品按字典系列分组 */
const productGroups = computed(() => {
  const map = new Map<string, DictOption[]>()
  dict.product.forEach(p => {
    const key = p.typeName || '未分组'
    if (!map.has(key)) map.set(key, [])
    map.get(key)!.push(p)
  })
  return Array.from(map.entries()).map(([typeName, items]) => ({ typeName, items }))
})

const blankItem = () => ({
  productKey: '', dictType: '', dictValue: '', productName: '', itemCategory: '',
  color: '', netMaterial: '', material: '', handle: '', lockSet: '', handleDirection: '',
  openDirection: '', addRod: '', fixedBottom: '', squareBoard: '',
  _vars: null as Record<string, any> | null,
  width: undefined as number | undefined, height: undefined as number | undefined,
  deductWidth: 0, netWidth: 0, num: 1, unit: '套',
  unitPrice: 0, calcType: 1, singleArea: 0, chargeArea: 0, itemTotalArea: 0, lineAmount: 0,
  minArea: 0, profitAmount: 0, salesOwner: '', remark: ''
})

const order = reactive<any>({
  orderId: undefined, orderNo: '', customerId: undefined, customerName: '', contact: '', phone: '',
  terminalAddress: '', logistics: '', unit: '套', brand: '', installType: '客户安装',
  designer: '', splitter: '', salesman: '', customerSource: '',
  orderDate: new Date().toISOString().slice(0, 10), expectDate: '',
  projectionArea: 0, bigBoardNum: 0,
  craftFee: 0, urgentFee: 0, freight: 0, discountAmount: 0, remark: '',
  itemList: [blankItem()]
})

// ---------------- 行计算（口径与后端一致） ----------------
const calcRow = (r: any) => {
  const w = Math.max((Number(r.width) || 0) - (Number(r.deductWidth) || 0), 0)
  const h = Number(r.height) || 0
  const num = Number(r.num) || 1
  r.netWidth = Number(w.toFixed(2))
  const single = (w * h) / 1000000
  r.singleArea = Number(single.toFixed(4))
  const minArea = Number(r.minArea) || 0
  const charge = Math.max(single, minArea)
  r.chargeArea = Number(charge.toFixed(4))
  r.itemTotalArea = Number((charge * num).toFixed(4))
  const price = Number(r.unitPrice) || 0
  r.lineAmount = Number((Number(r.calcType) === 2 ? price * num : r.itemTotalArea * price).toFixed(2))
}
const recalcAll = () => {
  order.itemList.forEach((r: any) => calcRow(r))
  ElMessage.success('已按当前口径重新计算')
}

/** 明细字段 ↔ 产品属性名 映射 */
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
}

const onCustomerChange = (cid: number) => {
  const c = customers.value.find(x => x.customerId === cid)
  if (c) {
    order.customerName = c.customerName
    order.contact = c.contact || ''
    order.phone = c.phone || ''
    order.terminalAddress = order.terminalAddress || c.address || ''
  }
}

// ---------------- 明细行操作 ----------------
const addProduct = () => { order.itemList.push(blankItem()) }
const appendProduct = () => { order.itemList.push(blankItem()); ElMessage.success('已追加一行产品') }
const insertProduct = (index?: number) => {
  const i = typeof index === 'number' ? index + 1 : currentRowIndex.value
  order.itemList.splice(i, 0, blankItem())
}
const copyRow = (index: number) => {
  const src = order.itemList[index]
  order.itemList.splice(index + 1, 0, { ...src })
}
const removeRow = (index: number) => {
  if (order.itemList.length <= 1) { ElMessage.warning('至少保留一行产品明细'); return }
  order.itemList.splice(index, 1)
}
const onSelectionChange = (rows: any[]) => { selectedRows.value = rows }
const onRowClick = (row: any) => {
  currentRowIndex.value = order.itemList.indexOf(row)
}
const onMoreCommand = (cmd: string) => {
  const idx = currentRowIndex.value
  if (cmd === 'copyInsert') copyRow(idx)
  else if (cmd === 'copy') navigator.clipboard?.writeText(JSON.stringify(order.itemList[idx])).then(() => ElMessage.success('已复制该行数据'))
  else if (cmd === 'delete') removeRow(idx)
}

/** 扣宽：批量给选中行（或当前行）设置扣宽 */
const openDeduct = () => {
  const rows = selectedRows.value.length ? selectedRows.value : [order.itemList[currentRowIndex.value]]
  ElMessageBox.prompt('请输入扣宽（mm），将应用到选中行；未选中则应用当前行', '批量扣宽', {
    inputPattern: /^\d+(\.\d+)?$/, inputErrorMessage: '请输入数字', confirmButtonText: '应用', cancelButtonText: '取消'
  }).then(({ value }) => {
    rows.forEach(r => { r.deductWidth = Number(value); calcRow(r) })
    ElMessage.success('扣宽已应用')
  }).catch(() => {})
}

// ---------------- 公式计算预览 ----------------
const formulaDialogVisible = ref(false)
const formulaLoading = ref(false)
const formulaResult = ref<any>(null)
const formulaRow = ref<any>(null)

/** 打开公式预览：调系统服务实时计算该行下料/剪网/金额 */
const openFormula = async (row: any) => {
  if (!row || (!row.dictType || !row.dictValue)) { ElMessage.warning('请先选择产品'); return }
  if (!row.width || !row.height) { ElMessage.warning('请先填写总宽和总高'); return }
  formulaRow.value = row
  formulaResult.value = null
  formulaDialogVisible.value = true
  formulaLoading.value = true
  try {
    const params: Record<string, any> = {
      总宽: Number(row.width),
      总高: Number(row.height),
      数量: Number(row.num) || 1,
      单价: Number(row.unitPrice) || 0
    }
    if (row.color) params.颜色 = row.color
    if (row.netMaterial) params.网子 = row.netMaterial
    if (row.handle) params.把手 = row.handle
    if (row.handleDirection) params.把手方向 = row.handleDirection
    if (row.addRod) params.加杆 = row.addRod
    const fb = Number(row.fixedBottom)
    params.下固定 = Number.isNaN(fb) ? 0 : fb
    const res: any = await testDictFormula({ dictType: row.dictType, dictValue: row.dictValue, params })
    if (res.code === 200) formulaResult.value = res.data
    else ElMessage.error(res.msg || '公式计算失败')
  } catch (e: any) {
    ElMessage.error(e?.message || '公式计算失败')
  } finally {
    formulaLoading.value = false
  }
}

/** 顶层独立结果（孔位、防护杆数量等非分组键） */
const topVars = computed(() => {
  const r = formulaResult.value
  if (!r || typeof r !== 'object') return {}
  const reserved = ['型材', '纱网', '面积', '金额明细', '总金额']
  const out: Record<string, any> = {}
  Object.entries(r).forEach(([k, v]) => {
    if (!reserved.includes(k) && typeof v !== 'object') out[k] = v
  })
  return out
})

/** 型材行：{名称, dim, mult, total} */
const materialRows = computed(() => {
  const r = formulaResult.value
  const mat = r?.型材
  if (!mat || typeof mat !== 'object') return []
  return Object.entries(mat).map(([name, v]: any) => ({
    name,
    dim: v?.dim ?? '—',
    mult: v?.mult ?? '—',
    total: v?.total ?? '—'
  }))
})

/** 纱网行：{名称, w, h, mult, area} */
const netRows = computed(() => {
  const r = formulaResult.value
  const net = r?.纱网
  if (!net || typeof net !== 'object') return []
  return Object.entries(net).map(([name, v]: any) => ({
    name,
    w: v?.w ?? '—',
    h: v?.h ?? '—',
    mult: v?.mult ?? '—',
    area: v?.area ?? '—'
  }))
})

/** 金额明细行 */
const amountRows = computed(() => {
  const r = formulaResult.value
  const amt = r?.金额明细
  if (!amt || typeof amt !== 'object') return {}
  return amt as Record<string, any>
})

// ---------------- 合计 ----------------
const totalNum = computed(() => order.itemList.reduce((s: number, r: any) => s + (Number(r.num) || 0), 0))
const totalArea = computed(() => order.itemList.reduce((s: number, r: any) => s + (Number(r.itemTotalArea) || 0), 0))
const productAmount = computed(() => order.itemList.reduce((s: number, r: any) => s + (Number(r.lineAmount) || 0), 0))
const feeAmount = computed(() => (Number(order.craftFee) || 0) + (Number(order.urgentFee) || 0)
  + (Number(order.freight) || 0) - (Number(order.discountAmount) || 0))
const totalAmount = computed(() => Math.max(productAmount.value + feeAmount.value, 0))

const money = (v?: number) => (Number(v) || 0).toFixed(2)
const num = (v?: number) => {
  const n = Number(v) || 0
  return Number.isInteger(n) ? String(n) : n.toFixed(2)
}

const resetForm = () => {
  Object.assign(order, {
    orderId: undefined, orderNo: '', customerId: undefined, customerName: '', contact: '', phone: '',
    terminalAddress: '', logistics: '', unit: '套', brand: '', installType: '客户安装',
    designer: '', splitter: '', salesman: '', customerSource: '',
    orderDate: new Date().toISOString().slice(0, 10), expectDate: '',
    projectionArea: 0, bigBoardNum: 0, craftFee: 0, urgentFee: 0, freight: 0, discountAmount: 0,
    remark: '', itemList: [blankItem()]
  })
}

const goBack = () => router.push('/sales/order/entry')

const submit = async () => {
  if (!order.customerId) { ElMessage.warning('请选择客户'); return }
  const bad = order.itemList.findIndex((r: any) => !r.productName || !r.width || !r.height)
  if (bad >= 0) { ElMessage.warning(`第 ${bad + 1} 行请完善产品、总宽、总高`); return }
  saving.value = true
  try {
    // 去掉仅用于下拉回显的 productKey，避免提交多余字段
    const payload = {
      ...order,
      itemList: order.itemList.map((r: any) => {
        const { productKey, ...rest } = r
        void productKey
        return { ...rest, width: Number(r.width), height: Number(r.height) }
      })
    }
    const res: any = await saveOrder(payload)
    if (res.code === 200) {
      // 新增订单：后端返回工单生成情况（拆单失败也会明确提示，不再静默）
      ElMessage.success(order.orderId ? '修改成功' : (res.data || '订单已保存'))
      router.push('/sales/order/entry')
    }
  } catch (e) {
    console.error(e)
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  // 字典
  const d: any = await getOrderDictOptions()
  if (d.code === 200 && d.data) {
    Object.keys(dict).forEach(k => { dict[k] = d.data[k] || [] })
  }
  // 客户
  try {
    const c: any = await getAllCustomerList()
    if (c.code === 200) customers.value = c.data || []
  } catch (e) {
    console.warn('客户列表加载失败（客户服务 8082 未启动？）', e)
  }
  // 编辑模式
  const oid = route.query.orderId
  if (oid) {
    const res: any = await getOrderInfo(Number(oid))
    if (res.code === 200 && res.data) {
      const data = res.data
      Object.assign(order, data)
      order.itemList = (data.itemList && data.itemList.length)
        ? data.itemList.map((it: any) => ({
            ...blankItem(), ...it,
            productKey: it.dictType && it.productName ? `${it.dictType}|${it.productName}|${it.dictValue || ''}` : '',
            deductWidth: Number(it.deductWidth) || 0,
            netWidth: Number(it.netWidth) || Number(it.width) || 0,
            unit: it.unit || '套',
            calcType: Number(it.calcType) || 1,
            profitAmount: Number(it.profitAmount) || 0
          }))
        : [blankItem()]
      order.itemList.forEach((r: any) => calcRow(r))
    }
  }
})
</script>

<style scoped>
.order-edit-page { padding: 12px; }
.block-card { margin-bottom: 10px; }
.block-card :deep(.el-card__body) { padding: 12px; }
.card-head { display: flex; justify-content: space-between; align-items: center; }
.block-title { font-weight: bold; font-size: 14px; }
.detail-tools { display: flex; gap: 6px; }
.cell-text { font-size: 12px; color: #606266; }
.scroll-tip { margin-top: 6px; font-size: 12px; color: #909399; text-align: center; }
.sum-box { border: 1px solid #ebeef5; border-radius: 4px; padding: 8px 14px; background: #fafafa; }
.sum-row { display: flex; justify-content: space-between; line-height: 26px; font-size: 13px; }
.sum-total { border-top: 1px dashed #dcdfe6; margin-top: 4px; padding-top: 4px; font-size: 15px; }
.price { color: #f56c6c; font-size: 18px; }
.footer-bar { text-align: right; margin-top: 10px; }
/* 公式预览 */
.formula-preview { min-height: 220px; }
.fp-top-vars { margin-bottom: 12px; }
.fp-block { margin-bottom: 14px; }
.fp-title { font-size: 13px; font-weight: 600; color: #303133; margin-bottom: 6px; border-left: 3px solid #409eff; padding-left: 8px; }
.fp-summary {
  display: flex; flex-wrap: wrap; gap: 10px 24px; padding: 10px 14px;
  background: #fafafa; border: 1px solid #ebeef5; border-radius: 6px;
}
.fp-sum-item { display: flex; flex-direction: column; font-size: 12px; color: #909399; }
.fp-sum-item b { font-size: 14px; color: #303133; margin-top: 2px; }
.fp-total b { color: #f56c6c; font-size: 18px; }
.fp-price { font-size: 18px; }
</style>
