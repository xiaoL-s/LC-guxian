<template>
  <div class="produce-page">
    <!-- 顶部筛选栏（按参考图：日期 + 汇总/明细切换 + 员工搜索 + 订单号/工序/客户/地址搜索 + 查询/重置/导出） -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="search" size="small" @submit.prevent>
        <el-form-item label="开始日期">
          <el-date-picker v-model="search.dateFrom" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" style="width:140px" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="search.dateTo" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" style="width:140px" />
        </el-form-item>
        <el-form-item>
          <el-radio-group v-model="mode" size="small">
            <el-radio-button value="summary">汇总</el-radio-button>
            <el-radio-button value="detail">明细</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-input v-model="search.workerName" placeholder="请输入员工名" clearable style="width:150px" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="search.keyword" placeholder="订单号,工序名称,客户,地址" clearable style="width:230px" @keyup.enter="loadAll" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAll">查询</el-button>
          <el-button @click="onReset">重置</el-button>
          <el-button type="success" plain :disabled="!canExport" @click="exportWage">导出工资表</el-button>
        </el-form-item>
      </el-form>
      <!-- 快捷日期范围 -->
      <el-tabs v-model="quick" class="status-tabs" @tab-change="onQuick">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="今日" name="today" />
        <el-tab-pane label="昨日" name="yesterday" />
        <el-tab-pane label="本月" name="month" />
        <el-tab-pane label="上月" name="lastMonth" />
      </el-tabs>
    </el-card>

    <el-row :gutter="10" style="margin-top:10px">
      <el-col :span="6"><el-card shadow="never" class="stat-card"><div class="stat-label">工资总额</div><div class="stat-value" style="color:#f56c6c">￥{{ fmt(summary.totalWage) }}</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never" class="stat-card"><div class="stat-label">合格总数</div><div class="stat-value">{{ summary.totalQualified }}</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never" class="stat-card"><div class="stat-label">报工次数</div><div class="stat-value">{{ summary.count }}</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never" class="stat-card"><div class="stat-label">工人数</div><div class="stat-value">{{ workerRows.length }}</div></el-card></el-col>
    </el-row>

    <!-- 汇总模式：按工人汇总 -->
    <el-card v-if="mode === 'summary'" shadow="never" class="table-card">
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
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="s">
            <el-button size="small" type="primary" link @click="openDetail(s.row)">明细</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 明细模式：按 员工×订单×工序 聚合（参考图 14 列） -->
    <el-card v-else shadow="never" class="table-card">
      <template #header><span class="block-title">计件工资明细（按 员工×订单×工序 汇总）</span></template>
      <el-table :data="detailRows" v-loading="loading" border stripe size="small">
        <el-table-column type="index" label="#" width="45" align="center" />
        <el-table-column label="员工" prop="employeeName" width="90" show-overflow-tooltip />
        <el-table-column label="工序总数" prop="processCount" width="75" align="center" />
        <el-table-column label="完成数量" prop="totalQualified" width="80" align="right" />
        <el-table-column label="计件方式" prop="calcType" width="85" align="center" />
        <el-table-column label="计件金额" width="85" align="right">
          <template #default="s">{{ fmt(s.row.unitPrice) }}</template>
        </el-table-column>
        <el-table-column label="工资总额" width="95" align="right">
          <template #default="s"><b style="color:#f56c6c">￥{{ fmt(s.row.totalWage) }}</b></template>
        </el-table-column>
        <el-table-column label="工序名称" prop="processName" width="95" show-overflow-tooltip />
        <el-table-column label="扫码时间" width="145">
          <template #default="s">{{ fmtTime(s.row.scanTime) }}</template>
        </el-table-column>
        <el-table-column label="订单号" prop="orderNo" width="135" show-overflow-tooltip />
        <el-table-column label="客户" prop="customerName" width="95" show-overflow-tooltip />
        <el-table-column label="地址" prop="address" width="105" show-overflow-tooltip />
        <el-table-column label="预计完成时间" width="110">
          <template #default="s">{{ fmtDate(s.row.expectDate) }}</template>
        </el-table-column>
        <el-table-column label="超期天数" width="80" align="center">
          <template #default="s">
            <span v-if="Number(s.row.overdueDays) > 0" style="color:#f56c6c;font-weight:bold">{{ s.row.overdueDays }}天</span>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="产品" prop="productName" min-width="120" show-overflow-tooltip />
      </el-table>
      <!-- 底部统计 + 分页 -->
      <div class="detail-footer">
        <div class="footer-stat">
          <span>工资总额：<b style="color:#f56c6c">￥{{ fmt(summary.totalWage) }}</b></span>
          <span style="margin-left:18px">总数量：<b>{{ summary.totalQualified }}</b></span>
          <span style="margin-left:18px;color:#909399">共 {{ detailTotal }} 行</span>
        </div>
        <el-pagination background layout="total, sizes, prev, pager, next, jumper"
          :total="detailTotal" :current-page="detailPage.pageNum" :page-size="detailPage.pageSize"
          :page-sizes="[30, 50, 100]" @current-change="onPage" @size-change="onSize" />
      </div>
    </el-card>

    <!-- 月度工资确认 -->
    <el-card shadow="never" class="table-card" style="margin-top:14px">      <template #header>
        <div class="card-head">
          <span class="block-title">月度工资确认（发工资依据）</span>
          <div class="head-actions">
            <el-date-picker v-model="wageMonth" type="month" value-format="YYYY-MM" size="small"
              placeholder="选择月份" style="width:140px" @change="loadMonth" />
            <el-button type="primary" size="small" style="margin-left:10px" :loading="genLoading"
              @click="generateMonth">生成本月工资</el-button>
          </div>
        </div>
      </template>
      <el-table :data="monthRows" v-loading="monthLoading" border stripe size="small">
        <el-table-column label="月份" prop="wageMonth" width="90" align="center" />
        <el-table-column label="工人ID" prop="workerId" width="80" align="center" />
        <el-table-column label="工人" prop="realName" min-width="100" />
        <el-table-column label="计件工资合计" width="120" align="right">
          <template #default="s">￥{{ fmt(s.row.totalWage) }}</template>
        </el-table-column>
        <el-table-column label="扣款" width="110">
          <template #default="s">
            <el-input v-model="s.row.deductWage" size="small" type="number" @change="recalc(s.row)" />
          </template>
        </el-table-column>
        <el-table-column label="加款" width="110">
          <template #default="s">
            <el-input v-model="s.row.addWage" size="small" type="number" @change="recalc(s.row)" />
          </template>
        </el-table-column>
        <el-table-column label="实发工资" width="130" align="right">
          <template #default="s"><b style="color:#f56c6c">￥{{ fmt(s.row.realWage) }}</b></template>
        </el-table-column>
        <el-table-column label="备注" min-width="120">
          <template #default="s"><el-input v-model="s.row.remark" size="small" placeholder="备注" /></template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="s">
            <el-button size="small" type="primary" link @click="saveMonthRow(s.row)">保存</el-button>
            <el-button size="small" type="warning" link @click="printSlip(s.row)">工资条</el-button>
            <el-button size="small" type="danger" link @click="delMonthRow(s.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 报工明细弹窗：按工序聚合（一个工序一条，合并多次报工的数量与工资） -->
    <el-dialog v-model="detailVisible" :title="`报工明细 - ${detailWorker}（${search.dateFrom || '全部'} ~ ${search.dateTo || '至今'}）`"
      width="760px" destroy-on-close>
      <el-table :data="detailAggRows" v-loading="detailLoading" border stripe size="small" max-height="420">
        <el-table-column type="index" label="#" width="42" align="center" />
        <el-table-column label="工序" prop="processName" min-width="110" show-overflow-tooltip />
        <el-table-column label="报工次数" prop="count" width="90" align="center" />
        <el-table-column label="合格数" prop="totalQualified" width="90" align="right" />
        <el-table-column label="不良数" prop="totalBad" width="90" align="right" />
        <el-table-column label="计件工资" width="120" align="right">
          <template #default="s"><b style="color:#f56c6c">￥{{ fmt(s.row.totalWage) }}</b></template>
        </el-table-column>
        <el-table-column label="报工时间" min-width="180">
          <template #default="s">{{ fmtTime(s.row.firstTime) }} ~ {{ fmtTime(s.row.lastTime) }}</template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button size="small" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'WageList' })
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getWageByWorker, getWageSummary, getWageDetail, getMonthWageList, saveMonthWage, deleteMonthWage
} from '@/api/finance/wage'
import { getReportPage } from '@/api/production/workorder'
import { fmtTime, fmtDate } from '@/utils/time'

const loading = ref(false)
const workerRows = ref<any[]>([])
const summary = ref<any>({ totalWage: 0, totalQualified: 0, count: 0 })
// 汇总 / 明细 切换（默认明细，贴合计件工资核算展示）
const mode = ref<'summary' | 'detail'>('detail')
const search = reactive({ dateFrom: '', dateTo: '', workerName: '', keyword: '' })
const quick = ref('all')

const fmt = (v: any) => (Number(v) || 0).toFixed(2)
const pad2 = (n: number) => String(n).padStart(2, '0')
const dstr = (d: Date) => `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`

// ---------- 明细模式分页 ----------
const detailRows = ref<any[]>([])
const detailTotal = ref(0)
const detailPage = reactive({ pageNum: 1, pageSize: 30 })

const canExport = computed(() =>
  mode.value === 'summary' ? workerRows.value.length > 0 : detailTotal.value > 0
)

const onPage = (p: number) => { detailPage.pageNum = p; loadDetail() }
const onSize = (s: number) => { detailPage.pageSize = s; detailPage.pageNum = 1; loadDetail() }

// 快捷日期范围
const onQuick = (name: string) => {
  const now = new Date()
  if (name === 'today') { search.dateFrom = dstr(now); search.dateTo = dstr(now) }
  else if (name === 'yesterday') {
    const y = new Date(now); y.setDate(y.getDate() - 1)
    search.dateFrom = dstr(y); search.dateTo = dstr(y)
  }
  else if (name === 'month') {
    search.dateFrom = `${now.getFullYear()}-${pad2(now.getMonth() + 1)}-01`
    search.dateTo = dstr(now)
  }
  else if (name === 'lastMonth') {
    const mStart = new Date(now.getFullYear(), now.getMonth() - 1, 1)
    const mEnd = new Date(now.getFullYear(), now.getMonth(), 0)
    search.dateFrom = dstr(mStart); search.dateTo = dstr(mEnd)
  }
  else { search.dateFrom = ''; search.dateTo = '' }
  loadAll()
}

const params = () => ({
  dateFrom: search.dateFrom || undefined,
  dateTo: search.dateTo || undefined,
  workerName: search.workerName || undefined
})

const loadDetail = async () => {
  try {
    const res = await getWageDetail({
      ...params(),
      keyword: search.keyword || undefined,
      pageNum: detailPage.pageNum,
      pageSize: detailPage.pageSize
    })
    if (res.code === 200) {
      detailRows.value = res.data.records || []
      detailTotal.value = Number(res.data.total) || 0
    }
  } catch (e) { console.error(e) }
}

const loadAll = async () => {
  loading.value = true
  try {
    const p = params()
    if (mode.value === 'summary') {
      const [w, s] = await Promise.all([
        getWageByWorker(p),
        getWageSummary(p)
      ])
      if (w.code === 200) workerRows.value = w.data
      if (s.code === 200) summary.value = s.data
    } else {
      const [d, s] = await Promise.all([
        getWageDetail({ ...p, keyword: search.keyword || undefined, pageNum: detailPage.pageNum, pageSize: detailPage.pageSize }),
        getWageSummary(p)
      ])
      if (d.code === 200) {
        detailRows.value = d.data.records || []
        detailTotal.value = Number(d.data.total) || 0
      }
      if (s.code === 200) summary.value = s.data
    }
  } catch (e) { console.error(e) } finally { loading.value = false }
}
const onReset = () => {
  search.dateFrom = ''; search.dateTo = ''; search.workerName = ''; search.keyword = ''
  quick.value = 'all'; detailPage.pageNum = 1
  loadAll()
}
loadAll()

// ---------- 导出工资表 ----------
const exportCsv = (head: string[], lines: string[][]) => {
  const csv = '\ufeff' + [head.join(','), ...lines.map(l => l.map(v => `"${String(v).replace(/"/g, '""')}"`).join(','))].join('\r\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `计件工资_${search.dateFrom || '全部'}_${search.dateTo || '至今'}.csv`
  a.click()
  URL.revokeObjectURL(a.href)
}

const exportWage = async () => {
  if (mode.value === 'summary') {
    const rows = workerRows.value
    if (!rows.length) return
    exportCsv(['工人ID', '工人姓名', '报工次数', '合格数量', '不良数量', '计件工资'],
      rows.map(r => [r.workerId ?? '', r.realName || '', r.count ?? '', r.totalQualified ?? '', r.totalBad ?? '', r.totalWage ?? '']))
  } else {
    // 明细：拉取当前筛选条件下全部数据导出
    try {
      const p = params()
      const first = await getWageDetail({ ...p, keyword: search.keyword || undefined, pageNum: 1, pageSize: 100 })
      if (first.code !== 200) return
      const total = Number(first.data.total) || 0
      if (!total) { ElMessage.warning('当前条件下无数据'); return }
      let all: any[] = first.data.records || []
      const pages = Math.ceil(total / 100)
      for (let pg = 2; pg <= pages; pg++) {
        const r = await getWageDetail({ ...p, keyword: search.keyword || undefined, pageNum: pg, pageSize: 100 })
        if (r.code === 200) all = all.concat(r.data.records || [])
      }
      exportCsv(['员工', '工序总数', '完成数量', '计件方式', '计件金额', '工资总额', '工序名称', '扫码时间', '订单号', '客户', '地址', '预计完成时间', '超期天数', '产品'],
        all.map(r => [
          r.employeeName ?? '', r.processCount ?? '', r.totalQualified ?? '', r.calcType ?? '',
          r.unitPrice ?? '', r.totalWage ?? '', r.processName ?? '', fmtTime(r.scanTime),
          r.orderNo ?? '', r.customerName ?? '', r.address ?? '', fmtDate(r.expectDate),
          Number(r.overdueDays) > 0 ? r.overdueDays : '', r.productName ?? ''
        ]))
    } catch (e) { console.error(e) }
  }
}

// ---------- 月度工资确认 ----------
const wageMonth = ref(`${new Date().getFullYear()}-${pad2(new Date().getMonth() + 1)}`)
const monthRows = ref<any[]>([])
const monthLoading = ref(false)
const genLoading = ref(false)

const loadMonth = async () => {
  monthLoading.value = true
  try {
    const res = await getMonthWageList({ wageMonth: wageMonth.value })
    if (res.code === 200) monthRows.value = res.data
  } catch (e) { console.error(e) } finally { monthLoading.value = false }
}

// 生成本月工资：按该月报工汇总自动为每个工人生成工资单
const generateMonth = async () => {
  const m = wageMonth.value
  const mStart = `${m}-01`
  const end = new Date(Number(m.slice(0, 4)), Number(m.slice(5, 7)), 0)
  const mEnd = dstr(end)
  genLoading.value = true
  try {
    const w = await getWageByWorker({ dateFrom: mStart, dateTo: mEnd })
    if (w.code !== 200) return
    const workers = w.data || []
    if (!workers.length) { ElMessage.warning('该月暂无报工记录'); return }
    for (const wk of workers) {
      await saveMonthWage({ workerId: wk.workerId, wageMonth: m, deductWage: 0, addWage: 0, remark: '' })
    }
    ElMessage.success(`已为 ${workers.length} 名工人生成 ${m} 月工资单`)
    await loadMonth()
  } catch (e) { console.error(e) } finally { genLoading.value = false }
}

// 扣款/加款变化时实时重算实发工资
const recalc = (row: any) => {
  const total = Number(row.totalWage) || 0
  const deduct = Number(row.deductWage) || 0
  const add = Number(row.addWage) || 0
  row.realWage = total - deduct + add
}

const saveMonthRow = async (row: any) => {
  try {
    const res = await saveMonthWage({
      workerId: row.workerId, wageMonth: row.wageMonth,
      deductWage: Number(row.deductWage) || 0, addWage: Number(row.addWage) || 0,
      remark: row.remark || ''
    })
    if (res.code === 200) { ElMessage.success('已保存'); await loadMonth() }
  } catch (e) { console.error(e) }
}

const delMonthRow = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确认删除 ${row.realName} ${row.wageMonth} 的工资单？`, '删除确认', { type: 'warning' })
    const res = await deleteMonthWage(row.id)
    if (res.code === 200) { ElMessage.success('已删除'); await loadMonth() }
  } catch (e) { /* 取消 */ }
}
loadMonth()

// ---------- 报工明细（汇总行点开，按工序聚合） ----------
const detailVisible = ref(false)
const detailAggRows = ref<any[]>([])
const detailLoading = ref(false)
const detailWorker = ref('')

const openDetail = async (row: any) => {
  detailWorker.value = row.realName || `工人${row.workerId}`
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getReportPage({
      pageNum: 1, pageSize: 500,
      workerId: row.workerId,
      dateFrom: search.dateFrom || undefined, dateTo: search.dateTo || undefined
    })
    if (res.code === 200) {
      const rows = res.data.records || []
      // 按工序聚合：一个工序一条（合并合格/不良/工资，记录首末报工时间）
      const map = new Map<string, any>()
      for (const r of rows) {
        const key = r.processName || `工序${r.processId ?? ''}`
        if (!map.has(key)) map.set(key, { processName: key, count: 0, totalQualified: 0, totalBad: 0, totalWage: 0, firstTime: r.reportTime, lastTime: r.reportTime })
        const g = map.get(key)
        g.count += 1
        g.totalQualified += Number(r.qualifiedNum) || 0
        g.totalBad += Number(r.badNum) || 0
        g.totalWage = (Number(g.totalWage) + (Number(r.pieceWage) || 0)).toFixed(2)
        if (r.reportTime && r.reportTime < g.firstTime) g.firstTime = r.reportTime
        if (r.reportTime && r.reportTime > g.lastTime) g.lastTime = r.reportTime
      }
      detailAggRows.value = Array.from(map.values())
    }
  } catch (e) { console.error(e) } finally { detailLoading.value = false }
}

// ---------- 工资条打印 ----------
const printSlip = (row: any) => {
  const w = window.open('', '_blank', 'width=420,height=560')
  if (!w) return
  const total = Number(row.totalWage) || 0
  const deduct = Number(row.deductWage) || 0
  const add = Number(row.addWage) || 0
  const real = Number(row.realWage) || (total - deduct + add)
  const html = `<!DOCTYPE html><html lang="zh-CN"><head><meta charset="utf-8">
<title>工资条 - ${row.realName || ''}</title>
<style>
  body { font-family: 'Microsoft YaHei', sans-serif; margin: 20px; color: #222; }
  .slip { border: 2px solid #333; border-radius: 8px; padding: 18px 22px; width: 340px; margin: 0 auto; }
  .slip-title { text-align: center; font-size: 20px; font-weight: 700; letter-spacing: 4px; margin-bottom: 6px; }
  .slip-sub { text-align: center; font-size: 13px; color: #666; margin-bottom: 14px; }
  .row { display: flex; justify-content: space-between; line-height: 2.1; font-size: 14px; border-bottom: 1px dashed #bbb; }
  .row b { font-size: 15px; }
  .total { color: #333; }
  .real { font-size: 18px; color: #d33; font-weight: 700; }
  .foot { margin-top: 14px; font-size: 12px; color: #888; text-align: center; }
</style></head><body>
  <div class="slip">
    <div class="slip-title">工资条</div>
    <div class="slip-sub">固贤纱窗厂 · ${row.wageMonth || ''}</div>
    <div class="row"><span>工号</span><span>${row.workerId ?? ''}</span></div>
    <div class="row"><span>姓名</span><span>${row.realName || ''}</span></div>
    <div class="row"><span>计件工资合计</span><b class="total">￥${fmt(total)}</b></div>
    <div class="row"><span>扣款</span><b>￥${fmt(deduct)}</b></div>
    <div class="row"><span>加款</span><b>￥${fmt(add)}</b></div>
    <div class="row"><span>实发工资</span><b class="real">￥${fmt(real)}</b></div>
    ${row.remark ? `<div class="row"><span>备注</span><span>${row.remark}</span></div>` : ''}
  </div>
  <div class="foot">打印时间：${new Date().toLocaleString()}</div>
</body></html>`
  w.document.write(html)
  w.document.close()
  w.focus()
  setTimeout(() => { w.print() }, 300)
}
</script>

<style scoped>
.produce-page { padding: 12px; }
.search-card :deep(.el-card__body) { padding: 12px 12px 0; }
.status-tabs { margin: 6px 0 0; }
.status-tabs :deep(.el-tabs__header) { margin-bottom: 6px; }
.table-card { margin-top: 10px; }
.table-card :deep(.el-card__body) { padding: 12px; }
.stat-card :deep(.el-card__body) { padding: 12px; }
.stat-label { font-size: 12px; color: #909399; }
.stat-value { font-size: 22px; font-weight: bold; margin-top: 4px; }
.block-title { font-weight: bold; font-size: 14px; }
.card-head { display: flex; justify-content: space-between; align-items: center; }
.head-actions { display: flex; align-items: center; }
.detail-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 10px; flex-wrap: wrap; gap: 8px; }
.footer-stat { font-size: 13px; }
</style>
