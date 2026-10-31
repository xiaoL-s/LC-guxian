<template>
  <div class="svg-bar-chart" :style="{ height: height + 'px' }">
    <svg :viewBox="`0 0 ${W} ${H}`" preserveAspectRatio="xMidYMid meet" width="100%" height="100%">
      <!-- 横向网格线 + Y 轴刻度 -->
      <g v-for="gy in gridY" :key="gy">
        <line :x1="PL" :y1="gy.y" :x2="W - PR" :y2="gy.y" stroke="#e8ecf1" stroke-width="1" />
        <text :x="PL - 10" :y="gy.y + 4" text-anchor="end" class="axis-text" font-size="11" fill="#8a94a6">
          {{ fmt(gy.val) }}
        </text>
      </g>
      <!-- 柱子 -->
      <g v-for="(b, i) in bars" :key="i">
        <rect :x="b.x" :y="b.y" :width="b.w" :height="b.h" rx="4" :fill="color" opacity="0.88" />
        <title>{{ b.name }}：{{ fmt(b.value) }}{{ unit }}</title>
        <!-- 柱顶数值 -->
        <text v-if="b.value > 0" :x="b.cx" :y="b.y - 7" text-anchor="middle" font-size="11" fill="#566070" font-weight="600">
          {{ fmt(b.value) }}
        </text>
        <!-- 名称（抽稀显示，长名截断） -->
        <text v-if="b.showLabel" :x="b.cx" :y="H - 6" text-anchor="middle" class="axis-text" font-size="11" fill="#8a94a6">
          {{ cut(b.name) }}
        </text>
      </g>
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
}>(), {
  height: 260,
  color: '#3d84e8',
  unit: ''
})

const W = 900
const H = 300
const PL = 58
const PR = 16
const PB = 28

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

function cut(name: string): string {
  const s = String(name || '')
  return s.length > 6 ? s.slice(0, 6) + '…' : s
}

const maxV = computed(() => niceMax(Math.max(...props.data.map(d => Number(d.value) || 0), 0)))
const innerW = computed(() => W - PL - PR)
const innerH = computed(() => H - 50)

const bars = computed(() => {
  const n = props.data.length
  if (!n) return []
  const slot = innerW.value / n
  const bw = Math.min(54, slot * 0.6)
  const showEvery = n > 10 ? Math.ceil(n / 10) : 1
  return props.data.map((d, i) => {
    const v = Number(d.value) || 0
    const h = (v / maxV.value) * innerH.value
    return {
      name: d.name,
      value: v,
      x: PL + i * slot + (slot - bw) / 2,
      y: innerH.value - h + 20,
      w: bw,
      h,
      cx: PL + i * slot + slot / 2,
      showLabel: i % showEvery === 0 || i === n - 1
    }
  })
})

const gridY = computed(() => {
  const arr: Array<{ y: number; val: number }> = []
  for (let i = 0; i <= 4; i++) {
    const val = (maxV.value / 4) * i
    arr.push({ y: innerH.value - (val / maxV.value) * innerH.value + 20, val })
  }
  return arr
})
</script>

<style scoped>
.svg-bar-chart {
  width: 100%;
}
.axis-text {
  user-select: none;
}
</style>
