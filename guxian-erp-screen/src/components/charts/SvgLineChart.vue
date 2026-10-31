<template>
  <div class="svg-line-chart" :style="{ height: height + 'px' }">
    <svg :viewBox="`0 0 ${W} ${H}`" preserveAspectRatio="xMidYMid meet" width="100%" height="100%">
      <!-- 横向网格线 + Y 轴刻度 -->
      <g v-for="gy in gridY" :key="gy">
        <line :x1="PL" :y1="gy.y" :x2="W - PR" :y2="gy.y" stroke="#e8ecf1" stroke-width="1" />
        <text :x="PL - 10" :y="gy.y + 4" text-anchor="end" class="axis-text" font-size="11" fill="#8a94a6">
          {{ fmt(gy.val) }}
        </text>
      </g>
      <!-- 面积填充 -->
      <path v-if="fill && points.length" :d="areaPath" fill="url(#lineGrad)" opacity="0.25" />
      <!-- 折线 -->
      <polyline v-if="points.length" :points="linePoints" fill="none" :stroke="color" stroke-width="2.5"
        stroke-linejoin="round" stroke-linecap="round" />
      <!-- 数据点 + hover 提示 -->
      <g v-for="(p, i) in points" :key="i">
        <circle :cx="p.x" :cy="p.y" r="3" :fill="color" />
        <title>{{ p.name }}：{{ fmt(p.value) }}{{ unit }}</title>
      </g>
      <!-- X 轴标签（抽稀显示，避免重叠） -->
      <g v-for="(p, i) in xLabels" :key="'x' + i">
        <text :x="p.x" :y="H - 6" text-anchor="middle" class="axis-text" font-size="11" fill="#8a94a6">
          {{ p.name }}
        </text>
      </g>
      <defs>
        <linearGradient id="lineGrad" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" :stop-color="color" stop-opacity="0.5" />
          <stop offset="100%" :stop-color="color" stop-opacity="0.02" />
        </linearGradient>
      </defs>
    </svg>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  data: Array<{ name: string; value: number }>
  height?: number
  color?: string
  unit?: string
  fill?: boolean
}>(), {
  height: 260,
  color: '#1c6cd4',
  unit: '',
  fill: true
})

const W = 900
const H = 300
const PL = 58 // 左留白（Y轴刻度）
const PR = 16
const PB = 28 // 底部留白

/** 取整刻度最大值：1/2/5 * 10^n */
function niceMax(v: number): number {
  if (v <= 0) return 1
  const pow = Math.pow(10, Math.floor(Math.log10(v)))
  const d = v / pow
  let nice = 1
  if (d <= 1) nice = 1
  else if (d <= 2) nice = 2
  else if (d <= 5) nice = 5
  else nice = 10
  return nice * pow
}

function fmt(v: number): string {
  const n = Number(v) || 0
  if (Math.abs(n) >= 100000000) return (n / 100000000).toFixed(1) + '亿'
  if (Math.abs(n) >= 10000) return (n / 10000).toFixed(1) + '万'
  return Math.round(n).toString()
}

const maxV = computed(() => niceMax(Math.max(...props.data.map(d => Number(d.value) || 0), 0)))
const innerW = computed(() => W - PL - PR)
const innerH = computed(() => H - 50)

const points = computed(() => {
  const n = props.data.length
  if (!n) return []
  return props.data.map((d, i) => ({
    name: d.name,
    value: Number(d.value) || 0,
    x: PL + (n === 1 ? innerW.value / 2 : (i * innerW.value) / (n - 1)),
    y: innerH.value - ((Number(d.value) || 0) / maxV.value) * innerH.value + 20
  }))
})

const linePoints = computed(() => points.value.map(p => `${p.x},${p.y}`).join(' '))
const areaPath = computed(() => {
  if (!points.value.length) return ''
  const first = points.value[0]
  const last = points.value[points.value.length - 1]
  return `M ${first.x},${first.y} L ` + points.value.map(p => `${p.x},${p.y}`).join(' L ') +
    ` L ${last.x},${H - 20} L ${first.x},${H - 20} Z`
})

const gridY = computed(() => {
  const arr: Array<{ y: number; val: number }> = []
  for (let i = 0; i <= 4; i++) {
    const val = (maxV.value / 4) * i
    arr.push({ y: innerH.value - (val / maxV.value) * innerH.value + 20, val })
  }
  return arr
})

/** X 轴标签抽稀：最多 15 个，保证不重叠 */
const xLabels = computed(() => {
  const n = props.data.length
  if (!n) return []
  const step = Math.max(1, Math.ceil(n / 15))
  const list: Array<{ name: string; x: number }> = []
  points.value.forEach((p, i) => {
    if (i % step === 0 || i === n - 1) list.push({ name: p.name, x: p.x })
  })
  return list
})
</script>

<style scoped>
.svg-line-chart {
  width: 100%;
}
.axis-text {
  user-select: none;
}
</style>
