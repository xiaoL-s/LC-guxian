<template>
  <div class="report-page">
    <!-- 指标卡 -->
    <div class="stat-grid">
      <div class="stat-card" v-for="s in statCards" :key="s.key">
        <div class="stat-value">{{ s.value }}</div>
        <div class="stat-label">{{ s.label }}</div>
      </div>
    </div>

    <!-- 生产趋势 -->
    <div class="chart-grid" style="margin-bottom:12px">
      <div class="chart-card">
        <div class="chart-title">生产趋势（近30天）</div>
        <SvgLineChart :data="trend" unit="单" />
      </div>
    </div>

    <!-- 排名图 -->
    <div class="chart-grid">
      <div class="chart-card">
        <div class="chart-title">工序产量排名（TOP10）</div>
        <SvgBarChart :data="processRank" unit="件" />
      </div>
      <div class="chart-card">
        <div class="chart-title">工人产量排名（TOP10）</div>
        <SvgBarChart :data="workerRank" unit="件" color="#c47ce8" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getProductionOverview } from '@/api/report/report'
import SvgLineChart from '@/components/charts/SvgLineChart.vue'
import SvgBarChart from '@/components/charts/SvgBarChart.vue'

const overview = ref<any>({})

function fmtInt(n: any): string {
  return Math.round(Number(n) || 0).toLocaleString('zh-CN')
}

const statCards = computed(() => {
  const s = overview.value.stats || {}
  return [
    { key: 'totalOrders', label: '生产工单总数', raw: s.totalOrders ?? 0, value: fmtInt(s.totalOrders) + ' 单' },
    { key: 'waitCount', label: '待生产', raw: s.waitCount ?? 0, value: fmtInt(s.waitCount) + ' 单' },
    { key: 'processingCount', label: '生产中', raw: s.processingCount ?? 0, value: fmtInt(s.processingCount) + ' 单' },
    { key: 'finishedCount', label: '已完工', raw: s.finishedCount ?? 0, value: fmtInt(s.finishedCount) + ' 单' },
    { key: 'noPrintOrders', label: '未打印生产单', raw: s.noPrintOrders ?? 0, value: fmtInt(s.noPrintOrders) + ' 单' },
    { key: 'delayedWorks', label: '延期工单', raw: s.delayedWorks ?? 0, value: fmtInt(s.delayedWorks) + ' 单' }
  ]
})

const trend = computed(() => (overview.value.trend || []).map((r: any) => ({ name: String(r.date).slice(5), value: Number(r.cnt) || 0 })))
const processRank = computed(() => (overview.value.processRank || []).map((r: any) => ({ name: r.name, value: Number(r.value) || 0 })))
const workerRank = computed(() => (overview.value.workerRank || []).map((r: any) => ({
  name: r.name,
  value: Number(r.value) || 0,
  wage: Number(r.wage) || 0
})))

onMounted(async () => {
  const res = await getProductionOverview()
  if (res && res.code === 200) {
    overview.value = res.data
  }
})
</script>
