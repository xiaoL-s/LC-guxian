<template>
  <div class="report-page">
    <!-- 指标卡 -->
    <div class="stat-grid">
      <div class="stat-card" v-for="s in statCards" :key="s.key">
        <div class="stat-value">{{ s.value }}</div>
        <div class="stat-label">{{ s.label }}</div>
      </div>
    </div>

    <!-- 利润趋势 -->
    <div class="chart-grid" style="margin-bottom:12px">
      <div class="chart-card">
        <div class="chart-title">月度销售额（最近12个月）</div>
        <SvgLineChart :data="salesTrend" unit="元" />
      </div>
      <div class="chart-card">
        <div class="chart-title">月度利润（最近12个月）</div>
        <SvgLineChart :data="profitTrend" unit="元" color="#3aa675" />
      </div>
    </div>

    <!-- 产品利润排名 -->
    <div class="chart-grid">
      <div class="chart-card">
        <div class="chart-title">产品利润排名（TOP10）</div>
        <SvgBarChart :data="productProfitRank" unit="元" color="#c47ce8" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getProfitOverview } from '@/api/report/report'
import SvgLineChart from '@/components/charts/SvgLineChart.vue'
import SvgBarChart from '@/components/charts/SvgBarChart.vue'

const overview = ref<any>({})

function fmtMoney(n: any): string {
  const v = Number(n) || 0
  if (Math.abs(v) >= 10000) return (v / 10000).toFixed(2) + ' 万'
  return v.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}

const statCards = computed(() => {
  const s = overview.value.stats || {}
  return [
    { key: 'monthSales', label: '本月销售额', raw: s.monthSales ?? 0, value: fmtMoney(s.monthSales) + ' 元' },
    { key: 'monthProfit', label: '本月利润', raw: s.monthProfit ?? 0, value: fmtMoney(s.monthProfit) + ' 元' },
    { key: 'avgProfitRate', label: '平均毛利率', raw: s.avgProfitRate ?? 0, value: (Number(s.avgProfitRate) || 0).toFixed(2) + ' %' },
    { key: 'receivable', label: '应收款金额', raw: s.receivable ?? 0, value: fmtMoney(s.receivable) + ' 元' }
  ]
})

const salesTrend = computed(() => (overview.value.salesTrend || []).map((r: any) => ({ name: String(r.ym), value: Number(r.sales) || 0 })))
const profitTrend = computed(() => (overview.value.trend || []).map((r: any) => ({ name: String(r.ym), value: Number(r.profit) || 0 })))
const productProfitRank = computed(() => (overview.value.productProfitRank || []).map((r: any) => ({ name: r.name, value: Number(r.value) || 0 })))

onMounted(async () => {
  const res = await getProfitOverview()
  if (res && res.code === 200) {
    overview.value = res.data
  }
})
</script>
