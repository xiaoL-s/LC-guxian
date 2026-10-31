<template>
  <div class="customer-finance-page">
    <el-card shadow="never" class="search-card">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="搜索: 请输入客户或联系方式"
          clearable
          size="small"
          style="width: 280px"
          @keyup.enter="onSearch"
          @clear="onSearch"
        />
        <el-button type="primary" size="small" @click="onSearch">查询</el-button>
        <el-button size="small" @click="onReset">重置</el-button>
        <div class="toolbar-right">
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
      >
        <el-table-column prop="customerName" label="客户" width="160" fixed="left" show-overflow-tooltip />
        <el-table-column label="销售金额" width="120" align="right">
          <template #default="{ row }">{{ money(row.salesAmount) }}</template>
        </el-table-column>
        <el-table-column label="已付金额" width="120" align="right">
          <template #default="{ row }">{{ money(row.paidAmount) }}</template>
        </el-table-column>
        <el-table-column label="未付金额" width="120" align="right">
          <template #default="{ row }">
            <span style="color: #e6a23c; font-weight: 600">{{ money(row.unpaidAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="优惠金额" width="100" align="right">
          <template #default="{ row }">{{ money(row.discountAmount) }}</template>
        </el-table-column>
        <el-table-column prop="salesman" label="业务员" width="100" />
        <el-table-column prop="remark" label="备注" width="140" show-overflow-tooltip />
        <el-table-column label="最后下单时间" width="130" align="center">
          <template #default="{ row }">
            <span :style="row.lastOrderDate ? 'color:#f56c6c;font-weight:600' : 'color:#909399'">
              {{ row.lastOrderDate || '—' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系方式" width="140" />
        <el-table-column prop="address" label="地址" min-width="160" show-overflow-tooltip />
        <el-table-column label="充值" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openRechargeDialog(row)">充值</el-button>
          </template>
        </el-table-column>
        <el-table-column label="余额" width="100" align="right" fixed="right">
          <template #default="{ row }">
            <span style="color: #1c6cd4; font-weight: 600">{{ money(row.balance) }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <div class="footer-stats">
          <span class="stat">显示数 {{ rows.length }}</span>
          <el-divider direction="vertical" />
          <span class="stat">总销售额: <b>{{ money(summary.totalSales) }}</b></span>
          <span class="stat">总已付金额: <b>{{ money(summary.totalPaid) }}</b></span>
          <span class="stat">总未付金额: <b style="color:#e6a23c">{{ money(summary.totalUnpaid) }}</b></span>
          <span class="stat">总优惠金额: <b>{{ money(summary.totalDiscount) }}</b></span>
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

    <!-- 充值弹窗 -->
    <el-dialog v-model="rechargeDialog" title="客户充值" width="400px" :close-on-click-modal="false">
      <el-form label-width="80px" size="small">
        <el-form-item label="客户">
          <el-input :model-value="current?.customerName" disabled />
        </el-form-item>
        <el-form-item label="当前余额">
          <el-input :model-value="'¥ ' + money(current?.balance)" disabled />
        </el-form-item>
        <el-form-item label="充值金额" required>
          <el-input-number
            v-model="rechargeAmount"
            :min="0.01"
            :precision="2"
            :controls="false"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="rechargeDialog = false">取消</el-button>
        <el-button type="primary" size="small" :loading="saving" @click="submitRecharge">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getCustomerFinancePage,
  getCustomerFinanceSummary,
  rechargeCustomer
} from '@/api/finance/collection'

const rows = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const summary = ref<any>({ totalSales: 0, totalPaid: 0, totalUnpaid: 0, totalDiscount: 0 })
const keyword = ref('')

const rechargeDialog = ref(false)
const saving = ref(false)
const current = ref<any>(null)
const rechargeAmount = ref<number | null>(null)

const money = (v: any) => (v == null ? '0.00' : Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }))

async function loadAll() {
  const params: any = {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    keyword: keyword.value || undefined
  }
  const [pageRes, sumRes] = await Promise.all([getCustomerFinancePage(params), getCustomerFinanceSummary(params)])
  rows.value = pageRes.data?.records || []
  total.value = Number(pageRes.data?.total || 0)
  summary.value = sumRes.data || summary.value
}

const onSearch = () => { pageNum.value = 1; loadAll() }
const onReset = () => { keyword.value = ''; pageNum.value = 1; loadAll() }

function openRechargeDialog(row: any) {
  current.value = row
  rechargeAmount.value = null
  rechargeDialog.value = true
}

async function submitRecharge() {
  if (!rechargeAmount.value || rechargeAmount.value <= 0) return ElMessage.warning('请输入充值金额')
  saving.value = true
  try {
    await rechargeCustomer({ customerId: current.value.customerId, amount: rechargeAmount.value })
    ElMessage.success('充值成功')
    rechargeDialog.value = false
    loadAll()
  } finally {
    saving.value = false
  }
}

onMounted(loadAll)
</script>

<style scoped lang="scss">
.customer-finance-page {
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
