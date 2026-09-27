<template>
  <div class="produce-page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="search" size="small">
        <el-form-item label="开始日期">
          <el-date-picker v-model="search.dateFrom" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" style="width:150px" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="search.dateTo" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" style="width:150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAll">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="10" style="margin-top:10px">
      <el-col :span="6"><el-card shadow="never" class="stat-card"><div class="stat-label">工资总额</div><div class="stat-value" style="color:#f56c6c">￥{{ fmt(summary.totalWage) }}</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never" class="stat-card"><div class="stat-label">合格总数</div><div class="stat-value">{{ summary.totalQualified }}</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never" class="stat-card"><div class="stat-label">报工次数</div><div class="stat-value">{{ summary.count }}</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never" class="stat-card"><div class="stat-label">工人数</div><div class="stat-value">{{ workerRows.length }}</div></el-card></el-col>
    </el-row>

    <el-card shadow="never" class="table-card">
      <template #header><span class="block-title">按工人汇总（计件工资核算）</span></template>
      <el-table :data="workerRows" v-loading="loading" border stripe size="small">
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column label="工人ID" prop="workerId" width="80" align="center" />
        <el-table-column label="工人姓名" prop="realName" min-width="110" />
        <el-table-column label="报工次数" prop="count" width="90" align="center" />
        <el-table-column label="合格数量" prop="totalQualified" width="100" align="right" />
        <el-table-column label="不良数量" prop="totalBad" width="100" align="right" />
        <el-table-column label="计件工资" width="130" align="right">
          <template #default="s"><b style="color:#f56c6c">￥{{ fmt(s.row.totalWage) }}</b></template>
        </el-table-column>
      </el-table>
      <div style="margin-top:10px;font-size:12px;color:#909399">
        计件工资 = 合格数量 × 工序单价（按报工当日工序单价快照计算）；如需按工人查看明细，可到「工人报工记录」按工人筛选。
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { getWageByWorker, getWageSummary } from '@/api/production/workorder'

const loading = ref(false)
const workerRows = ref<any[]>([])
const summary = ref<any>({ totalWage: 0, totalQualified: 0, count: 0 })
const search = reactive({ dateFrom: '', dateTo: '' })

const fmt = (v: any) => (Number(v) || 0).toFixed(2)

const loadAll = async () => {
  loading.value = true
  try {
    const [w, s] = await Promise.all([
      getWageByWorker({ dateFrom: search.dateFrom || undefined, dateTo: search.dateTo || undefined }),
      getWageSummary({ dateFrom: search.dateFrom || undefined, dateTo: search.dateTo || undefined })
    ])
    if (w.code === 200) workerRows.value = w.data
    if (s.code === 200) summary.value = s.data
  } catch (e) { console.error(e) } finally { loading.value = false }
}
const onReset = () => { search.dateFrom = ''; search.dateTo = ''; loadAll() }
loadAll()
</script>

<style scoped>
.produce-page { padding: 12px; }
.search-card :deep(.el-card__body) { padding: 12px 12px 0; }
.table-card { margin-top: 10px; }
.table-card :deep(.el-card__body) { padding: 12px; }
.stat-card :deep(.el-card__body) { padding: 12px; }
.stat-label { font-size: 12px; color: #909399; }
.stat-value { font-size: 22px; font-weight: bold; margin-top: 4px; }
.block-title { font-weight: bold; font-size: 14px; }
</style>
