<template>
  <div class="produce-page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="search" size="small">
        <el-form-item label="工单号">
          <el-input v-model="search.keyword" placeholder="工单号" clearable style="width:160px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
      <!-- 状态切换（tabs 样式） -->
      <el-tabs v-model="search.workStatus" class="status-tabs" @tab-change="onSearch">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane v-for="s in WORK_STATUS" :key="s.value" :label="s.label" :name="s.value" />
      </el-tabs>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column type="selection" width="45" align="center" />
        <el-table-column label="工单号" prop="workNo" width="140" />
        <el-table-column label="客户" prop="customerName" min-width="100" show-overflow-tooltip />
        <el-table-column label="产品" prop="productNames" min-width="150" show-overflow-tooltip />
        <el-table-column label="总面积(㎡)" prop="totalArea" width="85" align="right" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="s">
            <el-tag :type="statusTag(s.row.workStatus)" size="small">{{ statusDesc(s.row.workStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="完工时间" width="145">
          <template #default="s">{{ fmtTime(s.row.finishTime) }}</template>
        </el-table-column>
        <el-table-column label="制单时间" width="145">
          <template #default="s">{{ fmtTime(s.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="更新时间" width="145">
          <template #default="s">{{ fmtTime(s.row.updateTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="s">
            <el-button size="small" type="primary" link @click="viewDetail(s.row)">详情/操作</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination v-model:current-page="page.pageNum" v-model:page-size="page.pageSize" :total="total"
          small layout="total, sizes, prev, pager, next" @change="loadTable" />
      </div>
    </el-card>

    <!-- 拆单弹窗 -->
    <el-dialog v-model="splitVisible" title="拆单生成工单" width="720px">
      <el-form :inline="true" size="small">
        <el-form-item label="关键字">
          <el-input v-model="splitQuery.keyword" placeholder="订单号/客户" clearable style="width:180px" @keyup.enter="loadAudited" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAudited">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="auditedOrders" v-loading="auditedLoading" border stripe size="small" max-height="360"
        highlight-current-row @current-change="(row: any) => (selectedOrder = row)">
        <el-table-column label="订单号" prop="orderNo" width="150" />
        <el-table-column label="客户" prop="customerName" min-width="110" show-overflow-tooltip />
        <el-table-column label="总面积" prop="totalArea" width="80" align="right" />
        <el-table-column label="总金额" prop="totalAmount" width="100" align="right" />
        <el-table-column label="下单日期" prop="orderDate" width="110" />
      </el-table>
      <el-form style="margin-top:10px" label-width="90px" size="small">
        <el-form-item label="入库货架">
          <el-select v-model="splitShelfId" placeholder="预分配货架" style="width:200px">
            <el-option v-for="s in shelves" :key="s.shelfId" :label="s.shelfName" :value="s.shelfId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="splitVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="splitting" @click="doSplit">确认拆单</el-button>
      </template>
    </el-dialog>

    <!-- 工单详情/操作 -->
    <el-dialog v-model="detailVisible" :title="`工单 ${detail.workNo || ''}`" width="1020px" destroy-on-close>
      <template v-if="detail.workId">
        <el-descriptions :column="4" border size="small">
          <el-descriptions-item label="工单号">{{ detail.workNo }}</el-descriptions-item>
          <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="客户">{{ detail.customerName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTag(detail.workStatus)" size="small">{{ detail.workStatusDesc }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="总面积">{{ detail.totalArea }} ㎡</el-descriptions-item>
          <el-descriptions-item label="货架">{{ detail.shelfName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="制单时间">{{ fmtTime(detail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="完工时间">{{ fmtTime(detail.finishTime) }}</el-descriptions-item>
          <el-descriptions-item label="当前阶段" :span="2">
            <el-tag type="warning" size="small">{{ currentStage }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-tabs style="margin-top:10px">
          <!-- 订单明细 -->
          <el-tab-pane label="订单明细">
            <el-table :data="detail.orderItems" border stripe size="small">
              <el-table-column type="index" label="#" width="40" align="center" />
              <el-table-column label="产品" prop="productName" min-width="140" show-overflow-tooltip />
              <el-table-column label="宽" prop="width" width="80" align="right" />
              <el-table-column label="高" prop="height" width="80" align="right" />
              <el-table-column label="数量" prop="num" width="60" align="center" />
              <el-table-column label="面积(㎡)" prop="itemTotalArea" width="90" align="right" />
            </el-table>
          </el-tab-pane>

          <!-- 报工记录（按扫码流水格式：序号/产品/工序/员工/数量/时间/备注；金额统计见财务模块） -->
          <el-tab-pane label="报工记录">
            <el-table :data="detail.reportList" border stripe size="small">
              <el-table-column type="index" label="#" width="40" align="center" />
              <el-table-column label="产品" prop="productName" min-width="120" show-overflow-tooltip />
              <el-table-column label="工序" prop="processName" width="100" />
              <el-table-column label="员工" prop="workerName" width="90" show-overflow-tooltip />
              <el-table-column label="合格数" prop="qualifiedNum" width="70" align="center" />
              <el-table-column label="创建时间" width="150">
                <template #default="s">{{ fmtTime(s.row.reportTime) }}</template>
              </el-table-column>
              <el-table-column label="更新时间" width="150">
                <template #default="s">{{ fmtTime(s.row.updateTime) }}</template>
              </el-table-column>
              <el-table-column label="备注" prop="remark" min-width="100" show-overflow-tooltip />
              <el-table-column v-if="isAdmin" label="操作" width="70" align="center">
                <template #default="s">
                  <el-button size="small" type="danger" link @click="revokeOne(s.row)">撤回</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>

        <div style="margin-top:12px;text-align:right">
          <el-button size="small" type="primary" @click="onPrintCutting">打印生产单</el-button>
          <template v-if="detail.workStatus !== 'FINISHED'">
            <el-button size="small" type="success" @click="openFinish">完工入库</el-button>
          </template>
          <el-button v-if="isAdmin && detail.workStatus === 'FINISHED'" size="small" type="warning" @click="undoFinish">撤回完工入库</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 完工入库弹窗 -->
    <el-dialog v-model="finishVisible" title="完工入库" width="460px" destroy-on-close>
      <el-form label-width="90px" size="small">
        <el-form-item label="入库货架">
          <el-select v-model="finishShelfId" placeholder="选择货架" style="width:100%">
            <el-option v-for="s in shelves" :key="s.shelfId" :label="s.shelfName" :value="s.shelfId" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="finishRemark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="finishVisible = false">取消</el-button>
        <el-button size="small" type="success" :loading="finishing" @click="submitFinish">确认完工入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'WorkOrderList' })
import { ref, reactive, computed } from 'vue'
import {
  getWorkOrderPage, getAuditedOrders, splitWorkOrder, getWorkOrderDetail,
  finishInstock, revokeReport, revokeFinish
} from '@/api/production/workorder'
import { getShelfEnabled } from '@/api/stock/material'
import { WORK_STATUS, type WorkOrder } from '@/types/production'
import { ElMessage, ElMessageBox } from 'element-plus'
import { renderWorkOrderHtml } from '@/utils/cuttingSheetRenderer'
import { fmtTime } from '@/utils/time'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = reactive({ pageNum: 1, pageSize: 10 })
const search = reactive({ keyword: '', workStatus: '' as string })
// 是否超级管理员：撤回等高危操作仅超管可见可点
const isAdmin = ref(false)
try {
  const u = localStorage.getItem('loginUser')
  if (u) isAdmin.value = !!(JSON.parse(u)?.isAdmin)
} catch { /* 忽略解析异常 */ }

const loadTable = async () => {
  loading.value = true
  try {
    const res = await getWorkOrderPage({ ...page, ...search })
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } catch (e) { console.error(e) } finally { loading.value = false }
}
// loadTable() // 初始不自动加载，点查询才出数据
const onSearch = () => { page.pageNum = 1; loadTable() }
const onReset = () => { search.keyword = ''; search.workStatus = ''; page.pageNum = 1; loadTable() }
const fmt = (v: any) => (Number(v) || 0).toFixed(2)
const statusDesc = (s: string) => WORK_STATUS.find(x => x.value === s)?.label || s
const statusTag = (s: string) => {
  if (s === 'WAIT_PROCESS') return 'info'
  if (s === 'PROCESSING') return 'warning'
  if (s === 'FINISHED') return 'success'
  return 'info'
}

// 拆单
const splitVisible = ref(false)
const splitQuery = reactive({ keyword: '' })
const auditedOrders = ref<any[]>([])
const auditedLoading = ref(false)
const selectedOrder = ref<any>(null)
const shelves = ref<any[]>([])
const splitShelfId = ref<number | undefined>()
const splitting = ref(false)
const loadAudited = async () => {
  auditedLoading.value = true
  try {
    const res = await getAuditedOrders(splitQuery.keyword)
    if (res.code === 200) auditedOrders.value = res.data
  } finally { auditedLoading.value = false }
}
const loadShelves = async () => {
  const res = await getShelfEnabled()
  if (res.code === 200) shelves.value = res.data
}
const doSplit = async () => {
  if (!selectedOrder.value) { ElMessage.warning('请选择要拆单的订单'); return }
  splitting.value = true
  try {
    const res = await splitWorkOrder(selectedOrder.value.orderId, splitShelfId.value)
    if (res.code === 200) {
      ElMessage.success(`拆单成功，工单ID ${res.data}`)
      splitVisible.value = false
      loadTable()
      viewDetail({ workId: res.data })
    }
  } catch (e) { console.error(e) } finally { splitting.value = false }
}

// 详情
const detailVisible = ref(false)
const detail = reactive<any>({ orderItems: [], processList: [], reportList: [] })
// 当前生产阶段：开料∥剪网并行 -> 组装 -> 打包 -> 入库
const currentStage = computed(() => {
  if (detail.workStatus === 'FINISHED') return '已完工入库'
  const done = new Set((detail.processList || []).filter((p: any) => p.done).map((p: any) => p.processCode))
  const cut = done.has('CUT'), net = done.has('CUT_NET')
  if (done.has('STOCK_IN')) return '已入库'
  if (done.has('PACKAGE')) return '已打包'
  if (done.has('ASSEMBLE')) return '已组装'
  if (cut && net) return '待组装（下料/剪网已完成）'
  if (cut || net) return `生产中（${cut ? '已下料' : '待下料'} / ${net ? '已剪网' : '待剪网'}）`
  if (detail.workStatus === 'WAIT_PROCESS') return '待生产'
  return '生产中'
})
const viewDetail = async (row: any) => {
  const res = await getWorkOrderDetail(row.workId)
  if (res.code === 200) {
    Object.keys(detail).forEach(k => delete (detail as any)[k])
    Object.assign(detail, res.data, {
      orderItems: res.data.orderItems || [],
      processList: res.data.processList || [], reportList: res.data.reportList || []
    })
    detailVisible.value = true
  }
}

// 完工入库
const finishVisible = ref(false)
const finishShelfId = ref<number | undefined>()
const finishRemark = ref('')
const finishing = ref(false)
const openFinish = () => {
  finishShelfId.value = detail.shelfId
  finishRemark.value = ''
  finishVisible.value = true
}
const submitFinish = async () => {
  finishing.value = true
  try {
    const res = await finishInstock(detail.workId, finishShelfId.value, finishRemark.value)
    if (res.code === 200) {
      ElMessage.success('完工入库成功，订单已回写已完工')
      finishVisible.value = false
      loadTable()
      viewDetail({ workId: detail.workId })
    }
  } catch (e) { console.error(e) } finally { finishing.value = false }
}

// ============ 撤回操作（仅超级管理员） ============
// 撤回报工：撤销该产品行该工序的报工记录与流水，回到未完成状态
const revokeOne = (row: any) => {
  if (!row?.processId) return
  if (!row?.itemId) {
    // 改造前的历史整体报工：无产品归属，无法按产品单独撤回，由系统统一清理
    ElMessage.warning('该记录为改造前的历史报工（无产品归属），不支持界面撤回')
    return
  }
  ElMessageBox.confirm(
    `确定撤回「${row.productName || ''} · ${row.processName || '该工序'}」的报工记录吗？\n撤回后该工序重新变为未完成，工人对应计件工资同步撤销。`,
    '撤回报工', { type: 'warning', confirmButtonText: '确认撤回', cancelButtonText: '取消' }
  ).then(async () => {
    const res = await revokeReport(detail.workId, row.itemId, row.processId)
    if (res.code === 200) {
      ElMessage.success('已撤回报工')
      viewDetail({ workId: detail.workId })
    }
  }).catch(() => {})
}
// 撤回完工入库：删除入库单，工单/订单回生产中
const undoFinish = () => {
  ElMessageBox.confirm(
    '确定撤回该工单的「完工入库」吗？\n将删除成品入库单，工单回到生产中，销售订单同步回到生产中。',
    '撤回完工入库', { type: 'warning', confirmButtonText: '确认撤回', cancelButtonText: '取消' }
  ).then(async () => {
    const res = await revokeFinish(detail.workId)
    if (res.code === 200) {
      ElMessage.success('已撤回完工入库')
      loadTable()
      viewDetail({ workId: detail.workId })
    }
  }).catch(() => {})
}

// ============ 打印生产单 ============
// 与「系统管理 → 打印模板管理」共用同一套模板：按 templateType=WORK_ORDER + isDefault 读取，
// 渲染逻辑在 @/utils/cuttingSheetRenderer（唯一入口 renderWorkOrderHtml），
// 所以这里打出来的单子与模板里配置的、以及模板预览的完全一致。
const onPrintCutting = async () => {
  const d: any = detail
  if (!d.workId) { ElMessage.warning('请先打开工单详情'); return }
  try {
    const html = await renderWorkOrderHtml(d, `生产单-${d.workNo || ''}`)
    const win = window.open('', '_blank')
    if (!win) { ElMessage.warning('浏览器拦截了弹窗，请允许弹窗后重试'); return }
    win.document.write(html)
    win.document.close()
    win.focus()
    setTimeout(() => {
      // 打印/取消后自动关闭打印窗口，避免残留空白窗口干扰操作
      try { win.onafterprint = () => win.close() } catch { /* 忽略 */ }
      win.print()
    }, 300)
  } catch (e) {
    console.error('[打印] 渲染失败', e)
    ElMessage.error('打印模板渲染失败，请检查打印模板管理中的模板内容')
  }
}

const init = async () => {
  loadShelves()
}
init()
</script>

<style scoped>
.produce-page { padding: 12px; }
.search-card :deep(.el-card__body) { padding: 12px 12px 0; }
.status-tabs { margin: 6px 0 0; }
.status-tabs :deep(.el-tabs__header) { margin-bottom: 6px; }
.table-card { margin-top: 10px; }
.table-card :deep(.el-card__body) { padding: 12px; }
.pager { margin-top: 10px; display: flex; justify-content: flex-end; }
.tip-box { color: #e6a23c; font-size: 12px; }
</style>
