<template>
  <div class="report-page">
    <!-- 指标卡（参考速制ERP：两排统计卡） -->
    <div class="stat-grid">
      <div class="stat-card" v-for="s in statCards" :key="s.key">
        <div class="stat-value" :title="s.raw">{{ s.value }}</div>
        <div class="stat-label">{{ s.label }}</div>
      </div>
    </div>

    <!-- 趋势图 -->
    <div class="chart-grid" style="margin-bottom:12px">
      <div class="chart-card">
        <div class="chart-title">下单趋势（近30天）</div>
        <SvgLineChart :data="trend" unit="单" />
      </div>
      <div class="chart-card">
        <div class="chart-title">下单金额趋势（近30天）</div>
        <SvgLineChart :data="amountTrend" unit="元" color="#3aa675" />
      </div>
    </div>

    <!-- 排名图 -->
    <div class="chart-grid">
      <div class="chart-card">
        <div class="chart-title">产品销量排名（TOP10）</div>
        <SvgBarChart :data="productRank" unit="套" />
      </div>
      <div class="chart-card">
        <div class="chart-title">客户销量排名（TOP10）</div>
        <SvgBarChart :data="customerRank" unit="元" color="#c47ce8" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getSalesOverview } from '@/api/report/report'
import SvgLineChart from '@/components/charts/SvgLineChart.vue'
import SvgBarChart from '@/components/charts/SvgBarChart.vue'

const overview = ref<any>({})

/** 金额格式化：千分位，超万显示万 */
function fmtMoney(n: any): string {
  const v = Number(n) || 0
  if (Math.abs(v) >= 10000) return (v / 10000).toFixed(2) + ' 万'
  return v.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}
function fmtInt(n: any): string {
  return Math.round(Number(n) || 0).toLocaleString('zh-CN')
}

const statCards = computed(() => {
  const s = overview.value.stats || {}
  return [
    { key: 'todayOrders', label: '今日新增订单', raw: s.todayOrders ?? 0, value: fmtInt(s.todayOrders) + ' 个' },
    { key: 'monthAmount', label: '月销售金额', raw: s.monthAmount ?? 0, value: fmtMoney(s.monthAmount) + ' 元' },
    { key: 'monthArea', label: '月销售面积', raw: s.monthArea ?? 0, value: fmtMoney(s.monthArea) + ' ㎡' },
    { key: 'receivable', label: '应收款金额', raw: s.receivable ?? 0, value: fmtMoney(s.receivable) + ' 元' },
    { key: 'productionOrders', label: '正在生产订单', raw: s.productionOrders ?? 0, value: fmtInt(s.productionOrders) + ' 个' },
    { key: 'delayedOrders', label: '已延期订单', raw: s.delayedOrders ?? 0, value: fmtInt(s.delayedOrders) + ' 个' },
    { key: 'urgentOrders', label: '加急单', raw: s.urgentOrders ?? 0, value: fmtInt(s.urgentOrders) + ' 个' },
    { key: 'unpaidOrders', label: '未结清订单', raw: s.unpaidOrders ?? 0, value: fmtInt(s.unpaidOrders) + ' 个' }
  ]
})

const trend = computed(() => (overview.value.trend || []).map((r: any) => ({ name: String(r.date).slice(5), value: Number(r.cnt) || 0 })))
const amountTrend = computed(() => (overview.value.amountTrend || []).map((r: any) => ({ name: String(r.date).slice(5), value: Number(r.amount) || 0 })))
const productRank = computed(() => (overview.value.productRank || []).map((r: any) => ({ name: r.name, value: Number(r.value) || 0 })))
const customerRank = computed(() => (overview.value.customerRank || []).map((r: any) => ({ name: r.name, value: Number(r.amount) || 0 })))

onMounted(async () => {
  const res = await getSalesOverview()
  if (res && res.code === 200) {
    overview.value = res.data
  }
})
</script>
