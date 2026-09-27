<template>
  <div class="sales-page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="search" size="small">
        <el-form-item label="订单号">
          <el-input v-model="search.orderNo" placeholder="订单号" clearable style="width:160px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="客户">
          <el-input v-model="search.customerName" placeholder="客户名称" clearable style="width:150px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="search.orderStatus" placeholder="全部状态" clearable style="width:130px" @change="onSearch">
            <el-option v-for="s in ORDER_STATUS" :key="s.code" :label="s.label" :value="s.code" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column label="订单号" prop="orderNo" width="150" />
        <el-table-column label="客户" prop="customerName" min-width="120" show-overflow-tooltip />
        <el-table-column label="总金额" prop="totalAmount" width="100" align="right">
          <template #default="s">￥{{ fmt(s.row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="当前状态" width="100" align="center">
          <template #default="s"><el-tag :type="statusTag(s.row.orderStatus).type" size="small">{{ s.row.orderStatusDesc }}</el-tag></template>
        </el-table-column>
        <el-table-column label="审核时间" prop="auditTime" width="160" />
        <el-table-column label="发货时间" prop="deliveryTime" width="160" />
        <el-table-column label="完成时间" prop="finishTime" width="160" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="s">
            <el-button size="small" type="primary" link @click="viewTrack(s.row)">进度</el-button>
            <el-button v-if="s.row.orderStatus === 3" size="small" type="warning" link @click="onDeliver(s.row)">发货</el-button>
            <el-button v-if="s.row.orderStatus === 4" size="small" type="success" link @click="onComplete(s.row)">完成</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination v-model:current-page="page.pageNum" v-model:page-size="page.pageSize" :total="total"
          small layout="total, sizes, prev, pager, next" @change="loadTable" />
      </div>
    </el-card>

    <!-- 进度时间轴弹窗 -->
    <el-dialog v-model="trackVisible" :title="`订单进度 - ${track.orderNo || ''}`" width="560px" destroy-on-close>
      <el-timeline>
        <el-timeline-item v-for="n in timeline" :key="n.label" :type="n.done ? 'success' : 'info'"
          :timestamp="n.time || '待处理'" :hollow="!n.done">
          <el-tag :type="n.done ? 'success' : 'info'" size="small">{{ n.label }}</el-tag>
          <span v-if="n.extra" style="margin-left:8px;color:#909399;font-size:12px">{{ n.extra }}</span>
        </el-timeline-item>
      </el-timeline>
      <template #footer>
        <el-button size="small" @click="trackVisible = false">关闭</el-button>
        <el-button v-if="track.orderStatus === 3" size="small" type="warning" @click="deliverFromDialog">确认发货</el-button>
        <el-button v-if="track.orderStatus === 4" size="small" type="success" @click="completeFromDialog">确认完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { getOrderPage, getOrderInfo, deliverOrder, completeOrder } from '@/api/sales/order'
import { ORDER_STATUS, statusTag, type SalesOrder } from '@/types/sales'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref<SalesOrder[]>([])
const total = ref(0)
const page = reactive({ pageNum: 1, pageSize: 10 })
const search = reactive({ orderNo: '', customerName: '', orderStatus: undefined as number | undefined })

const loadTable = async () => {
  loading.value = true
  try {
    const res = await getOrderPage({ ...page, ...search })
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } catch (e) { console.error(e) } finally { loading.value = false }
}
loadTable()
const onSearch = () => { page.pageNum = 1; loadTable() }
const onReset = () => { search.orderNo = ''; search.customerName = ''; search.orderStatus = undefined; page.pageNum = 1; loadTable() }
const fmt = (v?: number) => (Number(v) || 0).toFixed(2)

// 进度时间轴
const trackVisible = ref(false)
const track = reactive<any>({})
const FLOW = [
  { code: 0, label: '待审核' }, { code: 1, label: '已审核/待排产' }, { code: 2, label: '生产中' },
  { code: 3, label: '已完工' }, { code: 4, label: '已发货' }, { code: 5, label: '已完成' }
]
const timeline = computed(() => {
  const status = track.orderStatus
  const canceled = status === 7
  const rejected = status === 6
  const order: Record<number, number> = { 0: 0, 1: 1, 2: 2, 3: 3, 4: 4, 5: 5 }
  const reached = order[status as number] ?? -1
  const timeMap: Record<number, string | undefined> = {
    0: track.createTime, 1: track.auditTime, 3: undefined, 4: track.deliveryTime, 5: track.finishTime
  }
  const nodes = FLOW.map((f, i) => ({
    label: f.label,
    done: !canceled && !rejected && i <= reached,
    time: timeMap[f.code]
  }))
  if (rejected) nodes.push({ label: '已驳回：' + (track.rejectReason || ''), done: true, time: track.auditTime, extra: '驳回' })
  if (canceled) nodes.push({ label: '已取消：' + (track.cancelReason || ''), done: true, time: track.updateTime, extra: '取消' })
  return nodes
})

const viewTrack = async (row: SalesOrder) => {
  const res = await getOrderInfo(row.orderId!)
  if (res.code === 200) {
    Object.keys(track).forEach(k => delete (track as any)[k])
    Object.assign(track, res.data)
    trackVisible.value = true
  }
}
const onDeliver = (row: SalesOrder) => {
  ElMessageBox.confirm(`确认订单「${row.orderNo}」已发货？`, '发货确认', { type: 'warning' }).then(async () => {
    const res = await deliverOrder(row.orderId!)
    if (res.code === 200) { ElMessage.success('发货成功'); loadTable() }
  }).catch(() => {})
}
const onComplete = (row: SalesOrder) => {
  ElMessageBox.confirm(`确认订单「${row.orderNo}」安装完成、交易完成？`, '完成确认', { type: 'success' }).then(async () => {
    const res = await completeOrder(row.orderId!)
    if (res.code === 200) { ElMessage.success('订单已完成'); loadTable() }
  }).catch(() => {})
}
const deliverFromDialog = () => { trackVisible.value = false; onDeliver({ orderId: track.orderId, orderNo: track.orderNo } as SalesOrder) }
const completeFromDialog = () => { trackVisible.value = false; onComplete({ orderId: track.orderId, orderNo: track.orderNo } as SalesOrder) }
</script>

<style scoped>
.sales-page { padding: 12px; }
.search-card :deep(.el-card__body) { padding: 12px 12px 0; }
.table-card { margin-top: 10px; }
.table-card :deep(.el-card__body) { padding: 12px; }
.pager { margin-top: 10px; display: flex; justify-content: flex-end; }
</style>
