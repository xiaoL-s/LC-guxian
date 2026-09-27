<template>
  <div class="sales-page">
    <!-- 查询条件 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="search" size="small" @submit.prevent>
        <el-form-item label="订单号">
          <el-input v-model="search.orderNo" placeholder="订单号/子单号" clearable style="width: 168px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="客户">
          <el-input v-model="search.customerName" placeholder="客户名称" clearable style="width: 150px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="订单类型">
          <el-select v-model="search.orderType" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="t in orderTypeOptions" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="search.orderStatus" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="s in ORDER_STATUS" :key="s.code" :label="s.label" :value="s.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="财务状态">
          <el-select v-model="search.financeStatus" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="s in FINANCE_STATUS" :key="s.code" :label="s.label" :value="s.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束"
            style="width: 240px" @change="onSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单列表 -->
    <el-card shadow="never" class="table-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button type="primary" size="small" @click="goCreate">＋ 新建订单</el-button>
          <el-button size="small" @click="loadTable">刷新</el-button>
          <span class="tip">共 {{ total }} 张订单</span>
        </div>
        <div class="toolbar-right">
          <el-popover placement="bottom-end" :width="420" trigger="click">
            <template #reference>
              <el-button size="small">列设置</el-button>
            </template>
            <el-checkbox-group v-model="visibleCols">
              <el-checkbox v-for="c in optionalCols" :key="c.key" :label="c.key" :value="c.key">{{ c.label }}</el-checkbox>
            </el-checkbox-group>
          </el-popover>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe size="small" height="calc(100vh - 300px)"
        :header-cell-style="{ background: '#f5f7fa', color: '#303133', fontSize: '12px' }">
        <el-table-column type="index" label="#" width="46" align="center" fixed />
        <el-table-column label="订单号" prop="orderNo" width="132" fixed show-overflow-tooltip>
          <template #default="s">
            <el-link type="primary" :underline="false" @click="viewDetail(s.row)">{{ s.row.orderNo }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="子单数" prop="subOrderCount" width="64" align="center" />

        <el-table-column v-if="show('customerName')" label="客户" prop="customerName" min-width="120" show-overflow-tooltip />
        <el-table-column v-if="show('terminalAddress')" label="终端地址" prop="terminalAddress" min-width="140" show-overflow-tooltip />
        <el-table-column v-if="show('logistics')" label="物流" prop="logistics" width="90" show-overflow-tooltip />
        <el-table-column v-if="show('unit')" label="单位" prop="unit" width="60" align="center" />
        <el-table-column v-if="show('orderType')" label="订单类型" prop="orderType" width="88" align="center" />
        <el-table-column v-if="show('orderStatus')" label="状态" width="98" align="center">
          <template #default="s">
            <el-tag :type="statusTag(s.row.orderStatus).type as any" size="small" effect="light">{{ s.row.orderStatusDesc }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="show('financeStatus')" label="财务状态" width="104" align="center">
          <template #default="s">
            <el-tag :type="financeTag(s.row.financeStatus).type as any" size="small" effect="plain">{{ s.row.financeStatusDesc }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column v-if="show('totalArea')" label="总面积㎡" prop="totalArea" width="88" align="right" />
        <el-table-column v-if="show('totalAmount')" label="销售金额" prop="totalAmount" width="100" align="right">
          <template #default="s"><b>{{ money(s.row.totalAmount) }}</b></template>
        </el-table-column>
        <el-table-column v-if="show('receiveAmount')" label="已付金额" prop="receiveAmount" width="96" align="right">
          <template #default="s">{{ money(s.row.receiveAmount) }}</template>
        </el-table-column>
        <el-table-column v-if="show('unpaidAmount')" label="未付金额" prop="unpaidAmount" width="96" align="right">
          <template #default="s">
            <span :class="{ 'text-danger': Number(s.row.unpaidAmount) > 0 }">{{ money(s.row.unpaidAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="show('profitAmount')" label="利润" prop="profitAmount" width="92" align="right">
          <template #default="s">{{ money(s.row.profitAmount) }}</template>
        </el-table-column>

        <el-table-column v-if="show('contact')" label="联系方式" width="112" show-overflow-tooltip>
          <template #default="s">{{ s.row.contact }}<span v-if="s.row.phone"> / {{ s.row.phone }}</span></template>
        </el-table-column>
        <el-table-column v-if="show('salesman')" label="业务员" prop="salesman" width="86" show-overflow-tooltip />
        <el-table-column v-if="show('remark')" label="备注" prop="remark" min-width="120" show-overflow-tooltip />
        <el-table-column v-if="show('orderDate')" label="下单时间" prop="orderDate" width="104" align="center" />
        <el-table-column v-if="show('expectDate')" label="计划交货" prop="expectDate" width="104" align="center" />

        <el-table-column label="操作" width="210" fixed="right" align="center">
          <template #default="s">
            <el-button size="small" type="primary" link @click="viewDetail(s.row)">详情</el-button>
            <el-button v-if="canEdit(s.row)" size="small" type="warning" link @click="goEdit(s.row)">编辑</el-button>
            <el-button v-if="s.row.orderStatus === 0" size="small" type="success" link @click="onPass(s.row)">受理</el-button>
            <el-button v-if="s.row.orderStatus === 0" size="small" type="danger" link @click="onReject(s.row)">驳回</el-button>
            <el-dropdown trigger="click" @command="(cmd: string) => onCommand(cmd, s.row)">
              <el-button size="small" type="primary" link>更多</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="receive" :disabled="s.row.orderStatus === 7">收款</el-dropdown-item>
                  <el-dropdown-item command="settle" :disabled="s.row.financeStatus === 3">结清</el-dropdown-item>
                  <el-dropdown-item command="start" :disabled="s.row.orderStatus !== 1" divided>开工</el-dropdown-item>
                  <el-dropdown-item command="finish" :disabled="s.row.orderStatus !== 2">完工</el-dropdown-item>
                  <el-dropdown-item command="deliver" :disabled="s.row.orderStatus !== 3">发货</el-dropdown-item>
                  <el-dropdown-item command="complete" :disabled="s.row.orderStatus !== 4">完成</el-dropdown-item>
                  <el-dropdown-item command="cancel" :disabled="![0, 6].includes(s.row.orderStatus)" divided>取消</el-dropdown-item>
                  <el-dropdown-item command="delete" :disabled="[1, 2, 3, 4, 5].includes(s.row.orderStatus)">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination v-model:current-page="page.pageNum" v-model:page-size="page.pageSize" :total="total"
          :page-sizes="[10, 20, 50, 100]" small layout="total, sizes, prev, pager, next, jumper" @change="loadTable" />
      </div>
    </el-card>

    <!-- 订单详情 -->
    <el-dialog v-model="detailVisible" :title="`订单详情 - ${detail.orderNo || ''}`" width="1000px" destroy-on-close top="6vh">
      <el-descriptions :column="4" border size="small">
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单类型">{{ detail.orderType }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="statusTag(detail.orderStatus).type as any" size="small">{{ detail.orderStatusDesc }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="财务状态">
          <el-tag :type="financeTag(detail.financeStatus).type as any" size="small" effect="plain">{{ detail.financeStatusDesc }}</el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="客户">{{ detail.customerName }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ detail.contact }} {{ detail.phone }}</el-descriptions-item>
        <el-descriptions-item label="物流">{{ detail.logistics || '—' }}</el-descriptions-item>
        <el-descriptions-item label="单位">{{ detail.unit || '—' }}</el-descriptions-item>

        <el-descriptions-item label="终端地址" :span="2">{{ detail.terminalAddress || detail.address || '—' }}</el-descriptions-item>
        <el-descriptions-item label="品牌">{{ detail.brand || '—' }}</el-descriptions-item>
        <el-descriptions-item label="安装方式">{{ detail.installType || '—' }}</el-descriptions-item>

        <el-descriptions-item label="设计师">{{ detail.designer || '—' }}</el-descriptions-item>
        <el-descriptions-item label="拆单师">{{ detail.splitter || '—' }}</el-descriptions-item>
        <el-descriptions-item label="业务员">{{ detail.salesman || '—' }}</el-descriptions-item>
        <el-descriptions-item label="客户来源">{{ detail.customerSource || '—' }}</el-descriptions-item>

        <el-descriptions-item label="下单时间">{{ detail.orderDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="计划交货">{{ detail.expectDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="投影面积">{{ detail.projectionArea || 0 }} ㎡</el-descriptions-item>
        <el-descriptions-item label="大板数">{{ detail.bigBoardNum || 0 }}</el-descriptions-item>

        <el-descriptions-item label="备注" :span="4">{{ detail.remark || '—' }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.orderStatus === 6" label="驳回原因" :span="4">
          <span class="text-danger">{{ detail.rejectReason }}</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.cancelReason" label="取消原因" :span="4">{{ detail.cancelReason }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="detail.itemList" border size="small" style="margin-top: 10px">
        <el-table-column type="index" label="#" width="42" align="center" />
        <el-table-column label="子单号" prop="subOrderNo" width="140" />
        <el-table-column label="产品" prop="productName" min-width="140" show-overflow-tooltip />
        <el-table-column label="品目" prop="itemCategory" width="110" show-overflow-tooltip />
        <el-table-column label="颜色" prop="color" width="76" show-overflow-tooltip />
        <el-table-column label="网子" prop="netMaterial" width="80" show-overflow-tooltip />
        <el-table-column label="把手" prop="handle" width="76" show-overflow-tooltip />
        <el-table-column label="锁具" prop="lockSet" width="76" show-overflow-tooltip />
        <el-table-column label="把手方向" prop="handleDirection" width="80" align="center" />
        <el-table-column label="开向" prop="openDirection" width="70" align="center" />
        <el-table-column label="加杆" prop="addRod" width="70" align="center" />
        <el-table-column label="下固定" prop="fixedBottom" width="76" align="center" />
        <el-table-column label="总宽" prop="width" width="76" align="right" />
        <el-table-column label="总高" prop="height" width="76" align="right" />
        <el-table-column label="扣宽" prop="deductWidth" width="70" align="right" />
        <el-table-column label="净宽" prop="netWidth" width="76" align="right" />
        <el-table-column label="数量" prop="num" width="56" align="center" />
        <el-table-column label="单位" prop="unit" width="56" align="center" />
        <el-table-column label="单价" prop="unitPrice" width="76" align="right" />
        <el-table-column label="面积㎡" prop="itemTotalArea" width="86" align="right" />
        <el-table-column label="金额" prop="lineAmount" width="92" align="right" />
        <el-table-column label="备注" prop="remark" min-width="90" show-overflow-tooltip />
      </el-table>

      <el-row :gutter="12" style="margin-top: 10px">
        <el-col :span="12">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="产品金额">{{ money(detail.productAmount) }}</el-descriptions-item>
            <el-descriptions-item label="工艺加价">{{ money(detail.craftFee) }}</el-descriptions-item>
            <el-descriptions-item label="加急费">{{ money(detail.urgentFee) }}</el-descriptions-item>
            <el-descriptions-item label="运费">{{ money(detail.freight) }}</el-descriptions-item>
            <el-descriptions-item label="优惠减价">{{ money(detail.discountAmount) }}</el-descriptions-item>
            <el-descriptions-item label="总面积">{{ detail.totalArea }} ㎡</el-descriptions-item>
          </el-descriptions>
        </el-col>
        <el-col :span="12">
          <div class="sum-box">
            <div class="sum-row"><span>订单总金额</span><b>￥{{ money(detail.totalAmount) }}</b></div>
            <div class="sum-row"><span>已付金额</span><b>￥{{ money(detail.receiveAmount) }}</b></div>
            <div class="sum-row"><span>未付金额</span><b class="text-danger">￥{{ money(detail.unpaidAmount) }}</b></div>
            <div class="sum-row sum-total"><span>利润 / 毛利率</span>
              <b class="price">￥{{ money(detail.profitAmount) }} / {{ percent(detail.grossProfitRate) }}</b>
            </div>
          </div>
        </el-col>
      </el-row>

      <template #footer>
        <el-button size="small" @click="detailVisible = false">关闭</el-button>
        <el-button v-if="detail.orderStatus === 0" size="small" type="success" @click="detailVisible = false; onPass(detail)">受理</el-button>
        <el-button v-if="canEdit(detail)" size="small" type="warning" @click="detailVisible = false; goEdit(detail)">编辑</el-button>
        <el-button v-if="detail.orderStatus !== 7" size="small" type="primary" @click="detailVisible = false; onCommand('receive', detail)">收款</el-button>
      </template>
    </el-dialog>

    <!-- 收款登记 -->
    <el-dialog v-model="payVisible" title="收款登记" width="420px" destroy-on-close>
      <el-form :model="payForm" label-width="92px" size="small">
        <el-form-item label="订单号">{{ payForm.orderNo }}</el-form-item>
        <el-form-item label="订单金额">￥{{ money(payForm.totalAmount) }}</el-form-item>
        <el-form-item label="已收金额">￥{{ money(payForm.received) }}</el-form-item>
        <el-form-item label="未付金额"><span class="text-danger">￥{{ money(payForm.unpaid) }}</span></el-form-item>
        <el-form-item label="本次收款" required>
          <el-input-number v-model="payForm.amount" :min="0" :precision="2" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="payForm.remark" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="payVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="paying" @click="submitPay">确认收款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOrderPage, getOrderInfo, auditPassOrder, auditRejectOrder, delOrder, cancelOrder,
  deliverOrder, completeOrder, startProduceOrder, finishProduceOrder, receiveOrderPayment, settleOrder
} from '@/api/sales/order'
import { ORDER_STATUS, FINANCE_STATUS, ORDER_TYPES, statusTag, financeTag, type SalesOrder } from '@/types/sales'

const router = useRouter()
const loading = ref(false)
const tableData = ref<SalesOrder[]>([])
const total = ref(0)
const page = reactive({ pageNum: 1, pageSize: 20 })
const dateRange = ref<[string, string] | null>(null)
const search = reactive({
  orderNo: '', customerName: '', orderType: undefined as string | undefined,
  orderStatus: undefined as number | undefined, financeStatus: undefined as number | undefined
})

const orderTypeOptions = ORDER_TYPES
const money = (v?: number) => (Number(v) || 0).toFixed(2)
const percent = (v?: number) => `${((Number(v) || 0) * 100).toFixed(2)}%`

// 列设置
const optionalCols = [
  { key: 'customerName', label: '客户' },
  { key: 'terminalAddress', label: '终端地址' },
  { key: 'logistics', label: '物流' },
  { key: 'unit', label: '单位' },
  { key: 'orderType', label: '订单类型' },
  { key: 'orderStatus', label: '状态' },
  { key: 'financeStatus', label: '财务状态' },
  { key: 'totalArea', label: '总面积' },
  { key: 'totalAmount', label: '销售金额' },
  { key: 'receiveAmount', label: '已付金额' },
  { key: 'unpaidAmount', label: '未付金额' },
  { key: 'profitAmount', label: '利润' },
  { key: 'contact', label: '联系方式' },
  { key: 'salesman', label: '业务员' },
  { key: 'remark', label: '备注' },
  { key: 'orderDate', label: '下单时间' },
  { key: 'expectDate', label: '计划交货' }
]
const visibleCols = ref<string[]>(optionalCols.map(c => c.key))
const show = (key: string) => visibleCols.value.includes(key)

const loadTable = async () => {
  loading.value = true
  try {
    const params: any = { ...page, ...search }
    if (dateRange.value && dateRange.value.length === 2) {
      params.dateFrom = dateRange.value[0]
      params.dateTo = dateRange.value[1]
    }
    const res: any = await getOrderPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = Number(res.data.total) || 0
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
const onSearch = () => { page.pageNum = 1; loadTable() }
const onReset = () => {
  search.orderNo = ''
  search.customerName = ''
  search.orderType = undefined
  search.orderStatus = undefined
  search.financeStatus = undefined
  dateRange.value = null
  page.pageNum = 1
  loadTable()
}

const goCreate = () => router.push('/sales/order/edit')
const goEdit = (row: SalesOrder) => router.push({ path: '/sales/order/edit', query: { orderId: row.orderId } })
const canEdit = (row: SalesOrder) => [0, 6].includes(Number(row.orderStatus))

// 详情
const detailVisible = ref(false)
const detail = reactive<any>({ itemList: [] })
const viewDetail = async (row: SalesOrder) => {
  const res: any = await getOrderInfo(row.orderId!)
  if (res.code === 200) {
    Object.keys(detail).forEach(k => delete (detail as any)[k])
    Object.assign(detail, res.data, { itemList: res.data.itemList || [] })
    detailVisible.value = true
  }
}

// 受理 / 驳回
const onPass = (row: any) => {
  ElMessageBox.confirm(`确认受理订单「${row.orderNo}」？受理后进入待排产。`, '订单受理', { type: 'success' })
    .then(async () => {
      const res: any = await auditPassOrder(row.orderId)
      if (res.code === 200) { ElMessage.success('已受理'); loadTable() }
    }).catch(() => {})
}
const onReject = (row: any) => {
  ElMessageBox.prompt('请填写驳回原因（退回制单人修改）', '驳回订单', {
    confirmButtonText: '确认驳回', cancelButtonText: '取消', inputType: 'textarea',
    inputValidator: (v: string) => !!v?.trim() || '驳回原因必填'
  }).then(async ({ value }) => {
    const res: any = await auditRejectOrder(row.orderId, value)
    if (res.code === 200) { ElMessage.success('已驳回'); loadTable() }
  }).catch(() => {})
}

// 收款
const payVisible = ref(false)
const paying = ref(false)
const payForm = reactive<any>({ orderId: undefined, orderNo: '', totalAmount: 0, received: 0, unpaid: 0, amount: 0, remark: '' })
const openPay = (row: any) => {
  Object.assign(payForm, {
    orderId: row.orderId, orderNo: row.orderNo,
    totalAmount: Number(row.totalAmount) || 0,
    received: Number(row.receiveAmount) || 0,
    unpaid: Number(row.unpaidAmount) || 0,
    amount: Number(row.unpaidAmount) || 0,
    remark: ''
  })
  payVisible.value = true
}
const submitPay = async () => {
  if (!(Number(payForm.amount) > 0)) { ElMessage.warning('请输入大于 0 的收款金额'); return }
  paying.value = true
  try {
    const res: any = await receiveOrderPayment(payForm.orderId, { amount: Number(payForm.amount), remark: payForm.remark })
    if (res.code === 200) {
      ElMessage.success('收款已登记')
      payVisible.value = false
      loadTable()
    }
  } finally {
    paying.value = false
  }
}

// 更多操作
const onCommand = (cmd: string, row: any) => {
  const id = row.orderId
  const simple = (fn: () => Promise<any>, text: string) => {
    ElMessageBox.confirm(`确认${text}订单「${row.orderNo}」？`, text, { type: 'warning' })
      .then(async () => {
        const res: any = await fn()
        if (res.code === 200) { ElMessage.success(`${text}成功`); loadTable() }
      }).catch(() => {})
  }
  switch (cmd) {
    case 'receive':
      openPay(row)
      break
    case 'settle':
      simple(() => settleOrder(id), '结清')
      break
    case 'start':
      simple(() => startProduceOrder(id), '开工')
      break
    case 'finish':
      simple(() => finishProduceOrder(id), '完工')
      break
    case 'deliver':
      simple(() => deliverOrder(id), '发货')
      break
    case 'complete':
      simple(() => completeOrder(id), '完成')
      break
    case 'cancel':
      ElMessageBox.prompt('请填写取消原因', '取消订单', {
        confirmButtonText: '确认取消', cancelButtonText: '返回', inputType: 'textarea'
      }).then(async ({ value }) => {
        const res: any = await cancelOrder(id, value)
        if (res.code === 200) { ElMessage.success('已取消'); loadTable() }
      }).catch(() => {})
      break
    case 'delete':
      simple(() => delOrder(id), '删除')
      break
  }
}

onMounted(loadTable)
</script>

<style scoped>
.sales-page { padding: 12px; }
.search-card :deep(.el-card__body) { padding: 12px 12px 0; }
.table-card { margin-top: 10px; }
.table-card :deep(.el-card__body) { padding: 10px 12px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.toolbar-left { display: flex; align-items: center; gap: 8px; }
.toolbar-right { display: flex; gap: 8px; }
.tip { font-size: 12px; color: #909399; margin-left: 6px; }
.pager { margin-top: 10px; display: flex; justify-content: flex-end; }
.sum-box { border: 1px solid #ebeef5; border-radius: 4px; padding: 6px 14px; background: #fafafa; }
.sum-row { display: flex; justify-content: space-between; line-height: 26px; font-size: 13px; }
.sum-total { border-top: 1px dashed #dcdfe6; margin-top: 4px; padding-top: 4px; font-size: 14px; }
.price { color: #f56c6c; }
.text-danger { color: #f56c6c; }
:deep(.el-checkbox) { width: 92px; margin-right: 8px; }
</style>
