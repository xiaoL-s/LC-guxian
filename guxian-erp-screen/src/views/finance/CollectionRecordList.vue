<template>
  <div class="record-page">
    <el-card shadow="never" class="search-card">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="搜索: 请输入账户或订单号或客户"
          clearable
          size="small"
          style="width: 280px"
          @keyup.enter="onSearch"
          @clear="onSearch"
        />
        <el-select v-model="quickDays" placeholder="快捷筛选" size="small" style="width: 130px" @change="onQuick">
          <el-option label="最近7天" :value="7" />
          <el-option label="最近30天" :value="30" />
          <el-option label="最近90天" :value="90" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="~"
          start-placeholder="收款开始日期"
          end-placeholder="收款结束日期"
          value-format="YYYY-MM-DD"
          size="small"
          style="width: 250px"
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
        <el-table-column prop="accountName" label="账户" width="130" />
        <el-table-column label="收款金额" width="120" align="right">
          <template #default="{ row }">
            <span style="color: #1c6cd4; font-weight: 600">{{ money(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单号" width="150" />
        <el-table-column prop="customerName" label="客户" width="150" show-overflow-tooltip />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column prop="receiveTime" label="收款时间" width="170" />
        <el-table-column label="删除" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确认撤销这笔收款？将回退订单金额与账户余额" @confirm="onDelete(row)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <div class="footer-stats">
          <span class="stat">显示数 {{ rows.length }}</span>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRecordPage, deleteRecord } from '@/api/finance/collection'

const rows = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const keyword = ref('')
const quickDays = ref<number | null>(null)
const dateRange = ref<[string, string] | null>(null)

const money = (v: any) => (v == null ? '0.00' : Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }))

async function loadAll() {
  const params: any = {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    keyword: keyword.value || undefined,
    dateFrom: dateRange.value?.[0] || undefined,
    dateTo: dateRange.value?.[1] || undefined
  }
  const res = await getRecordPage(params)
  rows.value = res.data?.records || []
  total.value = Number(res.data?.total || 0)
}

const onSearch = () => { pageNum.value = 1; loadAll() }
const onReset = () => {
  keyword.value = ''
  quickDays.value = null
  dateRange.value = null
  pageNum.value = 1
  loadAll()
}

function onQuick(days: number) {
  const to = new Date()
  const from = new Date()
  from.setDate(to.getDate() - days + 1)
  const fmt = (d: Date) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  dateRange.value = [fmt(from), fmt(to)]
  onSearch()
}

async function onDelete(row: any) {
  try {
    await deleteRecord(row.recordId)
    ElMessage.success('已撤销该笔收款')
    loadAll()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg || '撤销失败')
  }
}

onMounted(loadAll)
</script>

<style scoped lang="scss">
.record-page {
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
