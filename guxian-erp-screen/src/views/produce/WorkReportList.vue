<template>
  <div class="produce-page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="search" size="small">
        <el-form-item label="工单号">
          <el-input v-model="search.keyword" placeholder="按工单号筛选" clearable style="width:180px" @keyup.enter="loadTable" />
        </el-form-item>
        <el-form-item label="工人">
          <el-select v-model="search.workerId" placeholder="全部" clearable filterable style="width:140px">
            <el-option v-for="u in users" :key="u.id" :label="u.realName" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="10" style="margin-top:10px">
      <el-col :span="8"><el-card shadow="never" class="stat-card"><div class="stat-label">合格数量</div><div class="stat-value">{{ summary.totalQualified }}</div></el-card></el-col>
      <el-col :span="8"><el-card shadow="never" class="stat-card"><div class="stat-label">不良数量</div><div class="stat-value" style="color:#e6a23c">{{ summary.totalBad }}</div></el-card></el-col>
      <el-col :span="8"><el-card shadow="never" class="stat-card"><div class="stat-label">报工次数</div><div class="stat-value">{{ summary.count }}</div></el-card></el-col>
    </el-row>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe size="small"
        @selection-change="(rows: any[]) => (selectedRows = rows)">
        <el-table-column type="selection" width="42" align="center" />
        <el-table-column type="index" label="#" width="40" align="center" />
        <el-table-column label="报工时间" prop="reportTime" width="150" />
        <el-table-column label="工单号" prop="workNo" width="140" />
        <el-table-column label="客户" prop="customerName" min-width="100" show-overflow-tooltip />
        <el-table-column label="产品" prop="productName" min-width="120" show-overflow-tooltip />
        <el-table-column label="工序" prop="processName" width="100" />
        <el-table-column label="员工" prop="workerName" width="90" show-overflow-tooltip />
        <el-table-column label="合格数" prop="qualifiedNum" width="70" align="center" />
        <el-table-column label="不良数" prop="badNum" width="70" align="center" />
        <el-table-column label="更新时间" prop="updateTime" width="150" />
        <el-table-column label="备注" prop="remark" min-width="100" show-overflow-tooltip />
      </el-table>
      <div class="pager">
        <el-pagination v-model:current-page="page.pageNum" v-model:page-size="page.pageSize" :total="total"
          small layout="total, sizes, prev, pager, next" @change="loadTable" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'WorkReportList' })
import { ref, reactive } from 'vue'
import { getReportPage, getUsers } from '@/api/production/workorder'

const loading = ref(false)
const tableData = ref<any[]>([])
const selectedRows = ref<any[]>([])
const total = ref(0)
const page = reactive({ pageNum: 1, pageSize: 10 })
const search = reactive({ keyword: '', workerId: undefined as number | undefined })
const users = ref<any[]>([])
const summary = ref<any>({ totalQualified: 0, totalBad: 0, count: 0 })

const loadTable = async () => {
  loading.value = true
  try {
    const res = await getReportPage({
      pageNum: page.pageNum, pageSize: page.pageSize,
      workerId: search.workerId, keyword: search.keyword
    })
    if (res.code === 200) {
      // 后端按工单过滤用 workOrderId，这里按关键字先粗筛（简化：关键字为空）
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) { console.error(e) } finally { loading.value = false }
}
const onSearch = () => { page.pageNum = 1; loadTable() }
const onReset = () => { search.keyword = ''; search.workerId = undefined; page.pageNum = 1; loadTable() }

const init = async () => {
  const u = await getUsers()
  if (u.code === 200) users.value = u.data
  loadTable()
}
init()
</script>

<style scoped>
.produce-page { padding: 12px; }
.search-card :deep(.el-card__body) { padding: 12px 12px 0; }
.table-card { margin-top: 10px; }
.table-card :deep(.el-card__body) { padding: 12px; }
.pager { margin-top: 10px; display: flex; justify-content: flex-end; }
.stat-card :deep(.el-card__body) { padding: 12px; }
.stat-label { font-size: 12px; color: #909399; }
.stat-value { font-size: 22px; font-weight: bold; margin-top: 4px; }
</style>
