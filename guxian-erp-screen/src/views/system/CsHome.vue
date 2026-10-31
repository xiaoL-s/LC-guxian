<template>
  <div class="home-page">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="wb-left">
        <div class="wb-hello">{{ greeting }}，欢迎回来 👋</div>
        <div class="wb-date">{{ dateStr }}</div>
        <div class="wb-tip">新的一天，新的订单，撸起袖子加油干！</div>
      </div>
      <div class="wb-deco">
        <div class="wb-ring wb-ring-1"></div>
        <div class="wb-ring wb-ring-2"></div>
        <div class="wb-star">✦</div>
      </div>
    </div>

    <!-- 今日经营指标（实时数据，接口未就绪时显示 --） -->
    <div class="stat-grid home-stat">
      <div class="stat-card sc-blue" v-for="s in statCards" :key="s.key">
        <div class="sc-icon">{{ s.icon }}</div>
        <div class="sc-body">
          <div class="sc-value">{{ s.value }}</div>
          <div class="sc-label">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <!-- 快捷工作台 -->
    <div class="quick-title">快捷工作台</div>
    <div class="quick-grid">
      <div class="quick-card" v-for="q in quickLinks" :key="q.path" @click="go(q.path)">
        <div class="qc-icon" :style="{ background: q.bg }">{{ q.icon }}</div>
        <div class="qc-name">{{ q.name }}</div>
        <div class="qc-desc">{{ q.desc }}</div>
        <div class="qc-arrow">→</div>
      </div>
    </div>

    <!-- 底部标语 -->
    <div class="home-slogan">
      <span class="sl-left">✦</span>
      <span>一分耕耘，一分收获，今天的每一单都算数！</span>
      <span class="sl-right">✦</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getSalesOverview } from '@/api/report/report'

defineOptions({ name: 'CsHome' })

const router = useRouter()
const stats = ref<any>(null)

/** 按时间段问候 */
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

/** 日期：2026年10月31日 星期六 */
const dateStr = computed(() => {
  const d = new Date()
  const week = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()]
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${week}`
})

function fmtMoney(n: any): string {
  const v = Number(n) || 0
  if (Math.abs(v) >= 10000) return (v / 10000).toFixed(2) + ' 万'
  return v.toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

const statCards = computed(() => {
  const s = stats.value || {}
  const show = (v: any) => (stats.value ? String(v ?? 0) : '--')
  return [
    { key: 'today', icon: '📋', label: '今日新增订单', value: show(s.todayOrders) + (stats.value ? ' 单' : '') },
    { key: 'month', icon: '💰', label: '月销售金额', value: stats.value ? fmtMoney(s.monthAmount) + ' 元' : '--' },
    { key: 'receivable', icon: '🧾', label: '应收款金额', value: stats.value ? fmtMoney(s.receivable) + ' 元' : '--' },
    { key: 'producing', icon: '🏭', label: '正在生产订单', value: show(s.productionOrders) + (stats.value ? ' 单' : '') }
  ]
})

const quickLinks = [
  { path: '/sales/order/list', icon: '📦', name: '订单管理', desc: '新增 / 查询销售订单', bg: 'linear-gradient(135deg,#2f6fe4,#5b8def)' },
  { path: '/production/workorder', icon: '🏭', name: '生产工单', desc: '派工 / 跟踪生产进度', bg: 'linear-gradient(135deg,#0f9aa8,#35c3d0)' },
  { path: '/production/report', icon: '⏱️', name: '扫码报工', desc: '工人扫码登记工序产量', bg: 'linear-gradient(135deg,#f0883a,#f7a35c)' },
  { path: '/finance/wage', icon: '💵', name: '计件工资', desc: '按工人汇总计件工资', bg: 'linear-gradient(135deg,#22b573,#4cd08e)' },
  { path: '/report/sales', icon: '📊', name: '销售报表', desc: '销售趋势与销量排名', bg: 'linear-gradient(135deg,#8a5cf6,#a98cf8)' },
  { path: '/stock/shelf', icon: '🗄️', name: '库存库位', desc: '库位与入库管理', bg: 'linear-gradient(135deg,#e05e9e,#f08bbd)' }
]

function go(path: string) {
  router.push(path)
}

onMounted(async () => {
  // 首页指标依赖报表服务（8088），接口不可用时不报错、显示 --，不影响页面
  try {
    const res = await getSalesOverview()
    if (res && res.code === 200) {
      stats.value = res.data?.stats || {}
    }
  } catch (e) {
    stats.value = null
  }
})
</script>

<style scoped lang="scss">
.home-page {
  padding: 0;
}

/* ---------- 欢迎横幅 ---------- */
.welcome-banner {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-radius: 14px;
  padding: 24px 30px;
  color: #fff;
  overflow: hidden;
  background: linear-gradient(120deg, #2f6fe4 0%, #5b8def 55%, #7aa8f5 100%);
  box-shadow: 0 4px 16px rgba(47, 111, 228, 0.28);
  margin-bottom: 14px;
  min-height: 128px;
  box-sizing: border-box;
}
.wb-hello {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 0.5px;
  position: relative;
  z-index: 2;
}
.wb-date {
  margin-top: 8px;
  font-size: 13.5px;
  opacity: 0.92;
  position: relative;
  z-index: 2;
}
.wb-tip {
  margin-top: 10px;
  font-size: 14px;
  font-weight: 500;
  opacity: 0.95;
  position: relative;
  z-index: 2;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 999px;
  display: inline-block;
  padding: 4px 14px;
}
.wb-deco {
  position: absolute;
  right: 30px;
  top: 50%;
  transform: translateY(-50%);
  width: 170px;
  height: 170px;
}
.wb-ring {
  position: absolute;
  border-radius: 50%;
  border: 2px dashed rgba(255, 255, 255, 0.35);
}
.wb-ring-1 { width: 150px; height: 150px; top: 0; right: 0; }
.wb-ring-2 { width: 100px; height: 100px; bottom: -8px; left: -6px; }
.wb-star {
  position: absolute;
  right: 26px;
  top: 18px;
  font-size: 26px;
  opacity: 0.85;
  animation: wbTwinkle 2.4s ease-in-out infinite;
}
@keyframes wbTwinkle {
  0%, 100% { opacity: 0.35; transform: scale(0.9); }
  50% { opacity: 0.95; transform: scale(1.15); }
}

/* ---------- 指标卡 ---------- */
.home-stat {
  margin-bottom: 14px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  border-radius: 12px;
  padding: 16px 18px;
  color: #fff;
  border: none;
  box-shadow: 0 3px 10px rgba(31, 45, 61, 0.12);
}
.sc-icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.22);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}
.sc-body { min-width: 0; }
.sc-value {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.15;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.sc-label {
  margin-top: 4px;
  font-size: 12.5px;
  opacity: 0.9;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.sc-blue   { background: linear-gradient(135deg, #2f6fe4, #5b8def); }
.sc-green  { background: linear-gradient(135deg, #22b573, #4cd08e); }
.sc-orange { background: linear-gradient(135deg, #f0883a, #f7a35c); }
.sc-purple { background: linear-gradient(135deg, #8a5cf6, #a98cf8); }

/* ---------- 快捷工作台 ---------- */
.quick-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 2px 0 10px;
  padding-left: 8px;
  border-left: 4px solid #2f6fe4;
  line-height: 1.2;
}
.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 12px;
}
.quick-card {
  position: relative;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  padding: 16px 16px 14px;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.05);
  overflow: hidden;
}
.quick-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 18px rgba(47, 111, 228, 0.16);
}
.quick-card:hover .qc-arrow {
  opacity: 1;
  transform: translateX(0);
}
.qc-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 21px;
  color: #fff;
  margin-bottom: 10px;
  box-shadow: 0 3px 8px rgba(31, 45, 61, 0.18);
}
.qc-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.qc-desc {
  margin-top: 4px;
  font-size: 12.5px;
  color: #909399;
}
.qc-arrow {
  position: absolute;
  right: 12px;
  top: 14px;
  font-size: 16px;
  color: #2f6fe4;
  opacity: 0;
  transform: translateX(-6px);
  transition: all 0.18s ease;
}

/* ---------- 底部标语 ---------- */
.home-slogan {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 13.5px;
  color: #7d8aa5;
  background: linear-gradient(90deg, rgba(47, 111, 228, 0.06), rgba(255, 255, 255, 0.6), rgba(138, 92, 246, 0.06));
  border-radius: 10px;
  padding: 12px 0;
}
.sl-left, .sl-right { color: #2f6fe4; font-size: 12px; }
</style>
