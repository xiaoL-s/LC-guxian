<template>
  <div class="report-page">
    <!-- 指标卡 -->
    <div class="stat-grid">
      <div class="stat-card" v-for="s in statCards" :key="s.key">
        <div class="stat-value">{{ s.value }}</div>
        <div class="stat-label">{{ s.label }}</div>
      </div>
    </div>

    <!-- 入库趋势 -->
    <div class="chart-grid" style="margin-bottom:12px">
      <div class="chart-card">
        <div class="chart-title">入库趋势（近30天）</div>
        <SvgLineChart :data="trend" unit="件" />
      </div>
    </div>

    <!-- 库位排名 -->
    <div class="chart-grid">
      <div class="chart-card">
        <div class="chart-title">库位入库排名（TOP10）</div>
        <SvgBarChart :data="shelfRank" unit="件" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getStockOverview } from '@/api/report/report'
import SvgLineChart from '@/components/charts/SvgLineChart.vue'
import SvgBarChart from '@/components/charts/SvgBarChart.vue'

const overview = ref<any>({})

function fmtInt(n: any): string {
  return Math.round(Number(n) || 0).toLocaleString('zh-CN')
}

const statCards = computed(() => {
  const s = overview.value.stats || {}
  return [
    { key: 'totalIn', label: '累计入库', raw: s.totalIn ?? 0, value: fmtInt(s.totalIn) + ' 件' },
    { key: 'monthIn', label: '本月入库', raw: s.monthIn ?? 0, value: fmtInt(s.monthIn) + ' 件' },
    { key: 'todayIn', label: '今日入库', raw: s.todayIn ?? 0, value: fmtInt(s.todayIn) + ' 件' },
    { key: 'inCount', label: '入库单数', raw: s.inCount ?? 0, value: fmtInt(s.inCount) + ' 单' }
  ]
})

const trend = computed(() => (overview.value.trend || []).map((r: any) => ({ name: String(r.date).slice(5), value: Number(r.value) || 0 })))
const shelfRank = computed(() => (overview.value.shelfRank || []).map((r: any) => ({ name: r.name, value: Number(r.value) || 0 })))

onMounted(async () => {
  const res = await getStockOverview()
  if (res && res.code === 200) {
    overview.value = res.data
  }
})
</script>
