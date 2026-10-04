<template>
  <div class="produce-page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="search" size="small">
        <el-form-item label="工单号">
          <el-input v-model="search.keyword" placeholder="工单号" clearable style="width:160px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="search.workStatus" placeholder="全部" clearable style="width:120px">
            <el-option v-for="s in WORK_STATUS" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
          <el-button type="primary" @click="splitVisible = true">拆单生成工单</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column label="工单号" prop="workNo" width="150" />
        <el-table-column label="客户" prop="customerName" min-width="120" show-overflow-tooltip />
        <el-table-column label="总面积(㎡)" prop="totalArea" width="90" align="right" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="s">
            <el-tag :type="statusTag(s.row.workStatus)" size="small">{{ statusDesc(s.row.workStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="完工时间" prop="finishTime" width="160" />
        <el-table-column label="制单时间" prop="createTime" width="160" />
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
    <el-dialog v-model="detailVisible" :title="`工单 ${detail.workNo || ''}`" width="960px" destroy-on-close>
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
          <el-descriptions-item label="制单时间">{{ detail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="完工时间">{{ detail.finishTime || '—' }}</el-descriptions-item>
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

          <!-- 工序报工 -->
          <el-tab-pane label="工序报工">
            <el-table :data="detail.processList" border stripe size="small">
              <el-table-column label="排序" prop="processSort" width="60" align="center" />
              <el-table-column label="工序" prop="processName" min-width="110" />
              <el-table-column label="单价(元/件)" prop="unitPrice" width="100" align="right" />
              <el-table-column label="状态" width="90" align="center">
                <template #default="s">
                  <el-tag :type="s.row.done ? 'success' : 'info'" size="small">{{ s.row.done ? '已完成' : '未报工' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="报工人" prop="operUsername" width="100" />
              <el-table-column label="操作" width="100">
                <template #default="s">
                  <el-button v-if="!s.row.done" size="small" type="primary" link
                    @click="openReport(s.row)">报工</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="detail.workStatus === 'WAIT_PROCESS'" style="margin-top:8px" class="tip-box">
              提示：工单待生产，首次报工将自动进入「生产中」。
            </div>
          </el-tab-pane>

          <!-- 报工记录 -->
          <el-tab-pane label="报工记录">
            <el-table :data="detail.reportList" border stripe size="small">
              <el-table-column label="时间" prop="reportTime" width="160" />
              <el-table-column label="工序" prop="processName" width="110" />
              <el-table-column label="工人ID" prop="workerId" width="80" align="center" />
              <el-table-column label="合格数" prop="qualifiedNum" width="80" align="center" />
              <el-table-column label="不良数" prop="badNum" width="80" align="center" />
              <el-table-column label="单价" prop="unitPrice" width="90" align="right" />
              <el-table-column label="计件工资" width="110" align="right">
                <template #default="s"><b>￥{{ fmt(s.row.pieceWage) }}</b></template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>

        <div style="margin-top:12px;text-align:right">
          <el-button size="small" type="primary" @click="onPrintCutting">打印生产单</el-button>
          <template v-if="detail.workStatus !== 'FINISHED'">
            <el-button size="small" type="success" @click="openFinish">完工入库</el-button>
          </template>
        </div>
      </template>
    </el-dialog>

    <!-- 报工弹窗 -->
    <el-dialog v-model="reportVisible" title="工序报工" width="480px" destroy-on-close>
      <el-form label-width="90px" size="small">
        <el-form-item label="工序">
          <el-input :model-value="reportRow.processName" disabled />
        </el-form-item>
        <el-form-item label="工人" required>
          <el-select v-model="reportForm.workerId" filterable placeholder="选择工人" style="width:100%">
            <el-option v-for="u in users" :key="u.id" :label="u.realName" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="合格数量" required>
          <el-input-number v-model="reportForm.qualifiedNum" :min="0" :controls="false" style="width:100%" />
        </el-form-item>
        <el-form-item label="不良数量">
          <el-input-number v-model="reportForm.badNum" :min="0" :controls="false" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="reportForm.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="reportVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="submitReport">提交报工</el-button>
      </template>
    </el-dialog>

    <!-- 完工入库弹窗 -->
    <el-dialog v-model="finishVisible" title="完工入库" width="460px" destroy-on-close>
      <el-alert title="系统将校验全部工序已完成，生成成品入库单，并自动回写销售订单状态为「已完工」" type="info" :closable="false" style="margin-bottom:10px" />
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
import { ref, reactive, computed } from 'vue'
import {
  getWorkOrderPage, getAuditedOrders, splitWorkOrder, getWorkOrderDetail,
  reportProcess, finishInstock, getUsers
} from '@/api/production/workorder'
import { getShelfEnabled } from '@/api/stock/material'
import { WORK_STATUS, type WorkOrder } from '@/types/production'
import { ElMessage, ElMessageBox } from 'element-plus'
import { renderWorkOrderHtml } from '@/utils/cuttingSheetRenderer'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = reactive({ pageNum: 1, pageSize: 10 })
const search = reactive({ keyword: '', workStatus: undefined as string | undefined })

const loadTable = async () => {
  loading.value = true
  try {
    const res = await getWorkOrderPage({ ...page, ...search })
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } catch (e) { console.error(e) } finally { loading.value = false }
}
loadTable()
const onSearch = () => { page.pageNum = 1; loadTable() }
const onReset = () => { search.keyword = ''; search.workStatus = undefined; page.pageNum = 1; loadTable() }
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

// 报工
const reportVisible = ref(false)
const reportRow = reactive<any>({})
const reportForm = reactive({ workerId: undefined as number | undefined, qualifiedNum: 1, badNum: 0, remark: '' })
const users = ref<any[]>([])
const openReport = (row: any) => {
  Object.assign(reportRow, row)
  reportForm.workerId = undefined
  reportForm.qualifiedNum = 1
  reportForm.badNum = 0
  reportForm.remark = ''
  reportVisible.value = true
}
const submitReport = async () => {
  if (!reportForm.workerId) { ElMessage.warning('请选择工人'); return }
  if (reportForm.qualifiedNum <= 0 && reportForm.badNum <= 0) { ElMessage.warning('合格与不良不能同时为0'); return }
  const res = await reportProcess({
    workId: detail.workId, processId: reportRow.processId, ...reportForm
  })
  if (res.code === 200) { ElMessage.success('报工成功'); reportVisible.value = false; viewDetail({ workId: detail.workId }) }
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
    setTimeout(() => win.print(), 300)
  } catch (e) {
    console.error('[打印] 渲染失败', e)
    ElMessage.error('打印模板渲染失败，请检查打印模板管理中的模板内容')
  }
}

const init = async () => {
  loadShelves()
  const u = await getUsers()
  if (u.code === 200) users.value = u.data
}
init()
</script>

<style scoped>
.produce-page { padding: 12px; }
.search-card :deep(.el-card__body) { padding: 12px 12px 0; }
.table-card { margin-top: 10px; }
.table-card :deep(.el-card__body) { padding: 12px; }
.pager { margin-top: 10px; display: flex; justify-content: flex-end; }
.tip-box { color: #e6a23c; font-size: 12px; }
</style>
