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

      <el-table ref="itemTableRef" :data="order.itemList" border size="small" height="420"
        :header-cell-style="{ background: '#f5f7fa', fontSize: '12px', padding: '3px 0' }"
        :cell-style="{ padding: '1px 0' }" @selection-change="onSelectionChange" @row-click="onRowClick">
        <el-table-column type="selection" width="38" align="center" />
        <el-table-column type="index" label="#" width="40" align="center" />
        <el-table-column label="产品" width="190">
          <template #default="s">
            <el-select v-model="s.row.productKey" filterable clearable placeholder="选择产品" size="small"
              style="width: 100%" @change="(v: string) => onProductChange(s.row, v)">
              <el-option-group v-for="g in productGroups" :key="g.typeName" :label="g.typeName">
                <el-option v-for="p in g.items" :key="p.dictType + '-' + p.value" :label="p.label" :value="p.dictType + '|' + p.label" />
              </el-option-group>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="品目" width="112">
          <template #default="s"><span class="cell-text">{{ s.row.itemCategory || '—' }}</span></template>
        </el-table-column>
        <el-table-column v-for="col in attrCols" :key="col.prop" :label="col.label" :width="col.width">
          <template #default="s">
            <el-select v-model="s.row[col.prop]" filterable allow-create default-first-option clearable size="small"
              placeholder="" style="width: 100%">
              <el-option v-for="o in dict[col.dictKey]" :key="o.value" :label="o.label" :value="o.label" />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column label="总宽mm" width="86">
          <template #default="s">
            <el-input-number v-model="s.row.width" :min="0" :controls="false" size="small" style="width: 100%" @change="() => calcRow(s.row)" />
          </template>
        </el-table-column>
        <el-table-column label="总高mm" width="86">
          <template #default="s">
            <el-input-number v-model="s.row.height" :min="0" :controls="false" size="small" style="width: 100%" @change="() => calcRow(s.row)" />
          </template>
        </el-table-column>
        <el-table-column label="扣宽" width="76">
          <template #default="s">
            <el-input-number v-model="s.row.deductWidth" :min="0" :controls="false" size="small" style="width: 100%" @change="() => calcRow(s.row)" />
          </template>
        </el-table-column>
        <el-table-column label="净宽" width="72" align="right">
          <template #default="s">{{ num(s.row.netWidth) }}</template>
        </el-table-column>
        <el-table-column label="数量" width="72">
          <template #default="s">
            <el-input-number v-model="s.row.num" :min="1" :controls="false" size="small" style="width: 100%" @change="() => calcRow(s.row)" />
          </template>
        </el-table-column>
        <el-table-column label="单位" width="70">
          <template #default="s"><el-input v-model="s.row.unit" size="small" /></template>
        </el-table-column>
        <el-table-column label="单价" width="88">
          <template #default="s">
            <el-input-number v-model="s.row.unitPrice" :min="0" :precision="2" :controls="false" size="small"
              style="width: 100%" @change="() => calcRow(s.row)" />
          </template>
        </el-table-column>
        <el-table-column label="计算方式" width="94">
          <template #default="s">
            <el-select v-model="s.row.calcType" size="small" style="width: 100%" @change="() => calcRow(s.row)">
              <el-option label="按面积" :value="1" />
              <el-option label="按件" :value="2" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="面积㎡" width="82" align="right">
          <template #default="s">{{ num(s.row.itemTotalArea) }}</template>
        </el-table-column>
        <el-table-column label="金额" width="92" align="right">
          <template #default="s"><b>{{ money(s.row.lineAmount) }}</b></template>
        </el-table-column>
        <el-table-column label="利润" width="86">
          <template #default="s">
            <el-input-number v-model="s.row.profitAmount" :precision="2" :controls="false" size="small" style="width: 100%" />
          </template>
        </el-table-column>
        <el-table-column label="销售商" width="100">
          <template #default="s"><el-input v-model="s.row.salesOwner" size="small" /></template>
        </el-table-column>
        <el-table-column label="备注" min-width="120">
          <template #default="s"><el-input v-model="s.row.remark" size="small" /></template>
        </el-table-column>
        <el-table-column label="操作" width="132" fixed="right" align="center">
          <template #default="s">
            <el-button size="small" type="primary" link @click.stop="insertProduct(s.$index)">插入</el-button>
            <el-button size="small" type="primary" link @click.stop="copyRow(s.$index)">复制</el-button>
            <el-button size="small" type="danger" link @click.stop="removeRow(s.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDictOptions, getOrderInfo, saveOrder } from '@/api/sales/order'
import { getAllCustomerList } from '@/api/customer/customer'
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
  productKey: '', dictType: '', productName: '', itemCategory: '',
  color: '', netMaterial: '', material: '', handle: '', lockSet: '', handleDirection: '',
  openDirection: '', addRod: '', fixedBottom: '', squareBoard: '',
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

const onProductChange = (row: any, val: string) => {
  if (!val) {
    row.dictType = ''
    row.productName = ''
    row.itemCategory = ''
    return
  }
  const [dictType, label] = String(val).split('|')
  const opt = dict.product.find(p => p.dictType === dictType && p.label === label)
  if (!opt) return
  row.dictType = dictType
  row.productName = opt.label
  row.itemCategory = opt.typeName || ''
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
      ElMessage.success(order.orderId ? '修改成功，已重新提交受理' : '订单已保存，状态：订单未受理')
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
            productKey: it.dictType && it.productName ? `${it.dictType}|${it.productName}` : '',
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
.sum-box { border: 1px solid #ebeef5; border-radius: 4px; padding: 8px 14px; background: #fafafa; }
.sum-row { display: flex; justify-content: space-between; line-height: 26px; font-size: 13px; }
.sum-total { border-top: 1px dashed #dcdfe6; margin-top: 4px; padding-top: 4px; font-size: 15px; }
.price { color: #f56c6c; font-size: 18px; }
.footer-bar { text-align: right; margin-top: 10px; }
</style>
