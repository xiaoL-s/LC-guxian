<template>
  <div class="receivable-page">
    <!-- 工具条 -->
    <el-card shadow="never" class="search-card">
      <div class="toolbar">
        <el-input
          v-model="search.keyword"
          placeholder="搜索：请输入订单编号或客户或业务员"
          clearable
          style="width: 280px"
          size="small"
          @keyup.enter="onSearch"
          @clear="onSearch"
        />
        <el-select v-model="search.orderStatus" placeholder="状态" clearable size="small" style="width: 130px">
          <el-option label="待受理" :value="0" />
          <el-option label="已受理" :value="1" />
          <el-option label="已开工" :value="2" />
          <el-option label="打包入库" :value="3" />
          <el-option label="已发货" :value="4" />
          <el-option label="已完成" :value="5" />
        </el-select>
        <el-select v-model="search.financeStatus" placeholder="财务状态" clearable size="small" style="width: 130px">
          <el-option label="未收款" :value="0" />
          <el-option label="部分收款" :value="1" />
          <el-option label="已收账" :value="2" />
          <el-option label="已结清" :value="3" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="~"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          size="small"
          style="width: 240px"
        />
        <el-button type="primary" size="small" @click="onSearch">查询</el-button>
        <el-button size="small" @click="onReset">重置</el-button>
        <div class="toolbar-right">
          <el-button size="small" type="success" plain :disabled="!selection.length" @click="openMergeDialog">
            合并收款
          </el-button>
          <el-button size="small" @click="loadAll">刷新</el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <el-table
        :data="rows"
        border
        stripe
        size="small"
        height="calc(100vh - 280px)"
        @selection-change="onSelectionChange"
      >
        <el-table-column type="selection" width="42" fixed="left" />
        <el-table-column prop="orderNo" label="订单编号" width="150" fixed="left" />
        <el-table-column prop="customerName" label="客户" width="140" show-overflow-tooltip />
        <el-table-column prop="address" label="客户地址" width="160" show-overflow-tooltip />
        <el-table-column prop="terminalAddress" label="终端地址" width="160" show-overflow-tooltip />
        <el-table-column prop="orderType" label="订单类型" width="100" />
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.orderStatus === 0 ? 'danger' : 'success'" size="small">
              {{ statusText(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="财务状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="financeTag(row.financeStatus)" size="small">
              {{ financeText(row.financeStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="销售金额" width="110" align="right">
          <template #default="{ row }">{{ money(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="paidAmount" label="已付金额" width="110" align="right">
          <template #default="{ row }">{{ money(row.paidAmount) }}</template>
        </el-table-column>
        <el-table-column prop="unpaidAmount" label="未付金额" width="110" align="right">
          <template #default="{ row }">
            <span style="color: #e6a23c; font-weight: 600">{{ money(row.unpaidAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="discountAmount" label="优惠金额" width="100" align="right">
          <template #default="{ row }">{{ money(row.discountAmount) }}</template>
        </el-table-column>
        <el-table-column prop="salesman" label="业务员" width="100" />
        <el-table-column label="收款" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="Number(row.financeStatus) !== 3"
              type="primary"
              link
              size="small"
              @click="openReceiveDialog(row)"
            >
              收款
            </el-button>
            <span v-else style="color: #909399">已结清</span>
          </template>
        </el-table-column>
        <el-table-column label="财务合并单号" width="130" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openMergeDialog([row])">合并收款</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 底部统计 + 分页 -->
      <div class="table-footer">
        <div class="footer-stats">
          <span class="stat">已勾选 {{ selection.length }}</span>
          <el-button link type="primary" size="small" @click="clearSelection">清除</el-button>
          <span class="stat">显示数 {{ rows.length }}</span>
          <el-divider direction="vertical" />
          <span class="stat">总销售: <b>{{ money(summary.totalSales) }}</b></span>
          <span class="stat">总已付: <b>{{ money(summary.totalPaid) }}</b></span>
          <span class="stat">总未付: <b style="color:#e6a23c">{{ money(summary.totalUnpaid) }}</b></span>
          <span class="stat">总优惠: <b>{{ money(summary.totalDiscount) }}</b></span>
        </div>
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          size="small"
          background
          @size-change="loadAll"
          @current-change="loadAll"
        />
      </div>
    </el-card>

    <!-- 单笔收款弹窗 -->
    <el-dialog v-model="receiveDialog" title="订单收款" width="420px" :close-on-click-modal="false">
      <el-form label-width="80px" size="small">
        <el-form-item label="订单号">
          <el-input :model-value="current?.orderNo" disabled />
        </el-form-item>
        <el-form-item label="客户">
          <el-input :model-value="current?.customerName" disabled />
        </el-form-item>
        <el-form-item label="未付金额">
          <el-input :model-value="'¥ ' + money(current?.unpaidAmount)" disabled />
        </el-form-item>
        <el-form-item label="收款账户" required>
          <el-select v-model="receiveForm.accountId" placeholder="选择收款账户" style="width: 100%">
            <el-option
              v-for="a in accounts"
              :key="a.accountId"
              :label="`${a.accountName}（余额${money(a.balance)}）`"
              :value="a.accountId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="收款金额" required>
          <el-input-number
            v-model="receiveForm.amount"
            :min="0.01"
            :max="Number(current?.unpaidAmount || 999999)"
            :precision="2"
            :controls="false"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="receiveForm.remark" placeholder="收款备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="receiveDialog = false">取消</el-button>
        <el-button type="primary" size="small" :loading="saving" @click="submitReceive">确认收款</el-button>
      </template>
    </el-dialog>

    <!-- 合并收款弹窗 -->
    <el-dialog v-model="mergeDialog" title="合并收款" width="560px" :close-on-click-modal="false">
      <div style="margin-bottom: 10px; font-size: 12px; color: #606266">
        已选 <b>{{ mergeRows.length }}</b> 个订单，共未付
        <b style="color: #e6a23c">¥ {{ money(mergeUnpaid) }}</b>
      </div>
      <el-table :data="mergeRows" border size="small" max-height="260">
        <el-table-column prop="orderNo" label="订单号" width="140" />
        <el-table-column prop="customerName" label="客户" width="120" />
        <el-table-column label="未付金额" width="110" align="right">
          <template #default="{ row }">{{ money(row.unpaidAmount) }}</template>
        </el-table-column>
        <el-table-column label="本次收款" width="130" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row._amount"
              :min="0.01"
              :max="Number(row.unpaidAmount || 999999)"
              :precision="2"
              :controls="false"
              size="small"
              style="width: 100px"
            />
          </template>
        </el-table-column>
      </el-table>
      <el-form label-width="80px" size="small" style="margin-top: 12px">
        <el-form-item label="收款账户" required>
          <el-select v-model="mergeForm.accountId" placeholder="选择收款账户" style="width: 100%">
            <el-option
              v-for="a in accounts"
              :key="a.accountId"
              :label="`${a.accountName}（余额${money(a.balance)}）`"
              :value="a.accountId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="mergeForm.remark" placeholder="合并收款备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="mergeDialog = false">取消</el-button>
        <el-button type="primary" size="small" :loading="saving" @click="submitMerge">确认合并收款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getReceivablePage,
  getReceivableSummary,
  saveRecord,
  mergeCollect,
  getAccountList
} from '@/api/finance/collection'

const rows = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const summary = ref<any>({ totalSales: 0, totalPaid: 0, totalUnpaid: 0, totalDiscount: 0 })
const selection = ref<any[]>([])
const accounts = ref<any[]>([])

const search = reactive<any>({ keyword: '', orderStatus: null, financeStatus: null })
const dateRange = ref<[string, string] | null>(null)

const receiveDialog = ref(false)
const mergeDialog = ref(false)
const saving = ref(false)
const current = ref<any>(null)
const mergeRows = ref<any[]>([])

const receiveForm = reactive<any>({ accountId: null, amount: null, remark: '' })
const mergeForm = reactive<any>({ accountId: null, remark: '' })

const money = (v: any) => (v == null ? '0.00' : Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }))

const statusText = (s: any) => (['待受理', '已受理', '已开工', '打包入库', '已发货', '已完成', '已驳回', '已取消'][Number(s)] ?? '未知')
const financeText = (s: any) => (['未收款', '部分收款', '已收账', '已结清'][Number(s)] ?? '未知')
const financeTag = (s: any) => ({ 0: 'danger', 1: 'warning', 2: 'success', 3: 'info' }[Number(s)] ?? 'info')

const mergeUnpaid = () => mergeRows.value.reduce((sum, r) => sum + Number(r.unpaidAmount || 0), 0)

async function loadAll() {
  const params: any = {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    keyword: search.keyword || undefined,
    orderStatus: search.orderStatus ?? undefined,
    financeStatus: search.financeStatus ?? undefined,
    dateFrom: dateRange.value?.[0] || undefined,
    dateTo: dateRange.value?.[1] || undefined
  }
  const [pageRes, sumRes] = await Promise.all([getReceivablePage(params), getReceivableSummary(params)])
  rows.value = pageRes.data?.records || []
  total.value = Number(pageRes.data?.total || 0)
  summary.value = sumRes.data || summary.value
  clearSelection()
}

async function loadAccounts() {
  const res = await getAccountList()
  accounts.value = res.data || []
}

const onSearch = () => { pageNum.value = 1; loadAll() }
const onReset = () => {
  search.keyword = ''
  search.orderStatus = null
  search.financeStatus = null
  dateRange.value = null
  pageNum.value = 1
  loadAll()
}

const onSelectionChange = (sel: any[]) => { selection.value = sel }

const clearSelection = () => { selection.value = [] }

function openReceiveDialog(row: any) {
  current.value = row
  receiveForm.accountId = accounts.value[0]?.accountId || null
  receiveForm.amount = Number(row.unpaidAmount || 0)
  receiveForm.remark = ''
  receiveDialog.value = true
}

async function submitReceive() {
  if (!receiveForm.accountId) return ElMessage.warning('请选择收款账户')
  if (!receiveForm.amount || receiveForm.amount <= 0) return ElMessage.warning('请输入收款金额')
  saving.value = true
  try {
    await saveRecord({
      accountId: receiveForm.accountId,
      salesOrderId: current.value.orderId,
      amount: receiveForm.amount,
      remark: receiveForm.remark
    })
    ElMessage.success('收款成功')
    receiveDialog.value = false
    loadAll()
  } finally {
    saving.value = false
  }
}

function openMergeDialog(rowsArg?: any[]) {
  const list = rowsArg || selection.value
  if (!list.length) return ElMessage.warning('请先勾选要收款的订单')
  mergeRows.value = list.map(r => ({ ...r, _amount: Number(r.unpaidAmount || 0) }))
  mergeForm.accountId = accounts.value[0]?.accountId || null
  mergeForm.remark = ''
  mergeDialog.value = true
}

async function submitMerge() {
  if (!mergeForm.accountId) return ElMessage.warning('请选择收款账户')
  const orders = mergeRows.value.filter(r => r._amount > 0)
  if (!orders.length) return ElMessage.warning('请填写至少一笔收款金额')
  saving.value = true
  try {
    await mergeCollect({
      accountId: mergeForm.accountId,
      orders: orders.map(r => ({ orderId: r.orderId, amount: r._amount })),
      remark: mergeForm.remark
    })
    ElMessage.success(`合并收款成功，共 ${orders.length} 笔`)
    mergeDialog.value = false
    loadAll()
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadAccounts()
  loadAll()
})
</script>

<style scoped lang="scss">
.receivable-page {
  padding: 10px;
  box-sizing: border-box;
}
.search-card {
  margin-bottom: 10px;
  :deep(.el-card__body) {
    padding: 10px 12px;
  }
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  .toolbar-right {
    margin-left: auto;
    display: flex;
    gap: 8px;
  }
}
.table-card {
  :deep(.el-card__body) {
    padding: 0;
  }
}
.table-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 10px;
  border-top: 1px solid #ebeef5;
  flex-wrap: wrap;
  gap: 6px;
  .footer-stats {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 12px;
    color: #606266;
    flex-wrap: wrap;
  }
  .stat {
    white-space: nowrap;
  }
}
</style>
