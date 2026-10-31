<template>
  <div class="merge-page">
    <el-card shadow="never" class="search-card">
      <div class="toolbar">
        <el-input
          v-model="search.keyword"
          placeholder="搜索：请输入订单编号或客户"
          clearable
          size="small"
          style="width: 260px"
          @keyup.enter="onSearch"
          @clear="onSearch"
        />
        <el-select v-model="search.financeStatus" placeholder="财务状态" clearable size="small" style="width: 130px">
          <el-option label="未收款" :value="0" />
          <el-option label="部分收款" :value="1" />
        </el-select>
        <el-button type="primary" size="small" @click="onSearch">查询</el-button>
        <el-button size="small" @click="onReset">重置</el-button>
        <div class="toolbar-right">
          <el-button type="success" size="small" :disabled="!selection.length" @click="openMergeDialog">
            合并收款（{{ selection.length }}）
          </el-button>
          <el-button size="small" @click="loadAll">刷新</el-button>
        </div>
      </div>
    </el-card>

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
        <el-table-column prop="customerName" label="客户" width="150" show-overflow-tooltip />
        <el-table-column label="销售金额" width="110" align="right">
          <template #default="{ row }">{{ money(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="已付金额" width="110" align="right">
          <template #default="{ row }">{{ money(row.paidAmount) }}</template>
        </el-table-column>
        <el-table-column label="未付金额" width="110" align="right">
          <template #default="{ row }">
            <span style="color: #e6a23c; font-weight: 600">{{ money(row.unpaidAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="优惠金额" width="100" align="right">
          <template #default="{ row }">{{ money(row.discountAmount) }}</template>
        </el-table-column>
        <el-table-column prop="salesman" label="业务员" width="110" />
        <el-table-column label="财务状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.financeStatus === 0 ? 'danger' : 'warning'" size="small">
              {{ row.financeStatus === 0 ? '未收款' : '部分收款' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <div class="footer-stats">
          <span class="stat">已勾选 {{ selection.length }} 单，合计未付 <b style="color:#e6a23c">¥ {{ money(selectedUnpaid) }}</b></span>
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

    <!-- 合并收款弹窗 -->
    <el-dialog v-model="mergeDialog" title="合并收款" width="580px" :close-on-click-modal="false">
      <div style="margin-bottom: 10px; font-size: 12px; color: #606266">
        已选 <b>{{ mergeRows.length }}</b> 个订单，共未付
        <b style="color: #e6a23c">¥ {{ money(mergeUnpaid) }}</b>
      </div>
      <el-table :data="mergeRows" border size="small" max-height="260">
        <el-table-column prop="orderNo" label="订单号" width="150" />
        <el-table-column prop="customerName" label="客户" width="130" />
        <el-table-column label="未付金额" width="110" align="right">
          <template #default="{ row }">{{ money(row.unpaidAmount) }}</template>
        </el-table-column>
        <el-table-column label="本次收款" width="140" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row._amount"
              :min="0.01"
              :max="Number(row.unpaidAmount || 999999)"
              :precision="2"
              :controls="false"
              size="small"
              style="width: 110px"
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getReceivablePage, mergeCollect, getAccountList } from '@/api/finance/collection'

const rows = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const selection = ref<any[]>([])
const accounts = ref<any[]>([])
const mergeDialog = ref(false)
const saving = ref(false)
const mergeRows = ref<any[]>([])

const search = reactive<any>({ keyword: '', financeStatus: null })
const mergeForm = reactive<any>({ accountId: null, remark: '' })

const money = (v: any) => (v == null ? '0.00' : Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }))
const selectedUnpaid = computed(() => selection.value.reduce((s, r) => s + Number(r.unpaidAmount || 0), 0))
const mergeUnpaid = () => mergeRows.value.reduce((s, r) => s + Number(r.unpaidAmount || 0), 0)

async function loadAll() {
  const params: any = {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    keyword: search.keyword || undefined,
    // 财务合并只看未结清订单（未收款/部分收款），已结清不带
    financeStatus: search.financeStatus ?? undefined
  }
  const res = await getReceivablePage(params)
  rows.value = (res.data?.records || []).filter(r => Number(r.financeStatus) !== 3)
  total.value = Number(res.data?.total || 0)
  selection.value = []
}

async function loadAccounts() {
  const res = await getAccountList()
  accounts.value = res.data || []
}

const onSearch = () => { pageNum.value = 1; loadAll() }
const onReset = () => { search.keyword = ''; search.financeStatus = null; pageNum.value = 1; loadAll() }
const onSelectionChange = (sel: any[]) => { selection.value = sel }

function openMergeDialog() {
  if (!selection.value.length) return ElMessage.warning('请先勾选要收款的订单')
  mergeRows.value = selection.value.map(r => ({ ...r, _amount: Number(r.unpaidAmount || 0) }))
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
.merge-page {
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
  }
}
</style>
