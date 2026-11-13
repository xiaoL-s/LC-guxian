<template>
  <div class="sales-page">
    <!-- 查询条件 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="search" size="small" @submit.prevent>
        <el-form-item label="订单号">
          <el-input v-model="search.orderNo" placeholder="订单号/子单号" clearable style="width: 168px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="客户">
          <el-input v-model="search.customerName" placeholder="客户名称" clearable style="width: 150px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="订单类型">
          <el-select v-model="search.orderType" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="t in orderTypeOptions" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="打印状态">
          <el-select v-model="search.printFlag" placeholder="全部" clearable style="width: 120px">
            <el-option label="未打印" :value="0" />
            <el-option label="已打印" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="财务状态">
          <el-select v-model="search.financeStatus" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="s in FINANCE_STATUS" :key="s.code" :label="s.label" :value="s.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束"
            style="width: 240px" @change="onSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
      <!-- 订单状态（tabs 样式，整体统一） -->
      <el-tabs v-model="statusTab" class="status-tabs" @tab-change="onTabStatus">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane v-for="s in ORDER_STATUS" :key="s.code" :label="s.label" :name="String(s.code)" />
      </el-tabs>
    </el-card>

    <!-- 订单卡片列表 -->
    <el-card shadow="never" class="table-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button v-if="hasEditPerm('sales')" type="primary" size="small" @click="goCreate">＋ 新建订单</el-button>
          <el-button size="small" @click="loadTable">刷新</el-button>
          <el-checkbox
            :model-value="isAllChecked"
            :indeterminate="isIndeterminate"
            size="small"
            @change="onCheckAll"
          >全选</el-checkbox>
          <el-button type="warning" size="small" :disabled="checkedIds.length === 0" :loading="printing" @click="printSelected">
            批量打印<span v-if="checkedIds.length">({{ checkedIds.length }})</span>
          </el-button>
          <span class="tip">共 {{ total }} 张订单</span>
        </div>
      </div>

      <div v-loading="loading" class="order-list">
        <!-- 初始空白态：进页面默认不加载，必须给出明确指引，避免被误判为系统坏了 -->
        <el-empty v-if="!loading && tableData.length === 0 && !queried"
          description="尚未查询：设置条件后点右上角「查询」加载订单" :image-size="90" />
        <el-empty v-else-if="!loading && tableData.length === 0"
          description="没有符合条件的订单，换个条件或点「重置」试试" :image-size="90" />
        <div v-for="(row, idx) in tableData" :key="row.orderId" class="order-card"
          :class="{ expanded: isExpanded(row) }">
          <!-- 信息栏（每格独立展示，不挤在一起） -->
          <div class="order-head">
            <div class="head-cell seq-cell">
              <span class="cell-label">序号</span>
              <span class="cell-value seq-line">
                <el-checkbox :model-value="checkedIds.includes(row.orderId!)" size="small" @change="(v: any) => onCheck(row, v)" />
                <span class="seq">{{ (page.pageNum - 1) * page.pageSize + idx + 1 }}</span>
              </span>
            </div>

            <div class="head-cell order-no-cell" title="点击查看订单详情">
              <span class="cell-label">订单编号</span>
              <span class="cell-value">
                <span class="order-no" :style="{ color: Number(row.printFlag) === 1 ? '#52c41a' : '#333', cursor: 'pointer' }" @click="toggleDetail(row)">{{ row.orderNo }}</span>
                <el-tag size="small" type="info" class="sub-tag">{{ row.subOrderCount }}子单</el-tag>
              </span>
            </div>

            <div class="head-cell">
              <span class="cell-label">客户</span>
              <span class="cell-value">{{ row.customerName || '—' }}</span>
            </div>

            <div class="head-cell">
              <span class="cell-label">联系电话</span>
              <span class="cell-value">{{ row.phone || row.contact || '—' }}</span>
            </div>

            <div class="head-cell addr-cell">
              <span class="cell-label">终端地址</span>
              <span class="cell-value addr">{{ row.terminalAddress || '—' }}</span>
            </div>

            <div class="head-cell">
              <span class="cell-label">计划交货</span>
              <span class="cell-value">{{ row.expectDate || '—' }}</span>
            </div>

            <div class="head-cell">
              <span class="cell-label">物流</span>
              <span class="cell-value">{{ row.logistics || '—' }}</span>
            </div>

            <div class="head-cell">
              <span class="cell-label">订单类型</span>
              <span class="cell-value">{{ row.orderType || '—' }}</span>
            </div>

            <div class="head-cell">
              <span class="cell-label">财务状态</span>
              <span class="cell-value">
                <el-tag :type="financeTag(row.financeStatus).type as any" size="small" effect="plain">{{ row.financeStatusDesc || '—' }}</el-tag>
              </span>
            </div>

            <div class="head-cell">
              <span class="cell-label">订单状态</span>
              <span class="cell-value">
                <el-tag :type="statusTag(row.orderStatus).type as any" size="small" effect="light">{{ row.orderStatusDesc || '—' }}</el-tag>
              </span>
            </div>

            <div class="head-cell">
              <span class="cell-label">打印状态</span>
              <span class="cell-value">
                <el-tag v-if="Number(row.printFlag) === 1" type="success" size="small" effect="plain">已打印</el-tag>
                <el-tag v-else type="info" size="small" effect="plain">未打印</el-tag>
                <span v-if="Number(row.printFlag) === 1 && row.printTime" class="print-time">{{ fmtTime(row.printTime) }}</span>
              </span>
            </div>

            <div class="head-cell">
              <span class="cell-label">下单时间</span>
              <span class="cell-value">{{ row.orderDate || '—' }}</span>
            </div>

            <div class="head-cell">
              <span class="cell-label">业务员</span>
              <span class="cell-value">{{ row.salesman || '—' }}</span>
            </div>

            <div class="head-cell money-cell">
              <span class="cell-label">面积 / 金额</span>
              <span class="cell-value money">
                {{ row.totalArea }}㎡ · <b>¥{{ money(row.totalAmount) }}</b>
                <span v-if="Number(row.unpaidAmount) > 0" class="unpaid">未付 ¥{{ money(row.unpaidAmount) }}</span>
              </span>
            </div>

            <div class="head-actions">
              <el-button size="small" type="primary" link @click="toggleDetail(row)">
                {{ isExpanded(row) ? '收起详情' : '详情' }}
              </el-button>
              <el-button v-if="canEdit(row) && hasEditPerm('sales')" size="small" type="warning" link @click="goEdit(row)">编辑</el-button>
              <el-button v-if="row.orderStatus === 0 && hasEditPerm('sales')" size="small" type="success" link @click="onPass(row)">受理</el-button>
              <el-button v-if="row.orderStatus === 0 && hasEditPerm('sales')" size="small" type="danger" link @click="onReject(row)">驳回</el-button>
              <el-dropdown trigger="click" @command="(cmd: string) => onCommand(cmd, row)">
                <el-button size="small" type="primary" link>更多</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="printSales" divided>打印销售单</el-dropdown-item>
                    <template v-if="hasEditPerm('sales')">
                      <el-dropdown-item command="receive" :disabled="row.orderStatus === 7">收款</el-dropdown-item>
                      <el-dropdown-item command="settle" :disabled="row.financeStatus === 3">结清</el-dropdown-item>
                      <el-dropdown-item command="start" :disabled="row.orderStatus !== 1" divided>开工</el-dropdown-item>
                      <el-dropdown-item command="finish" :disabled="row.orderStatus !== 2">完工</el-dropdown-item>
                      <el-dropdown-item command="deliver" :disabled="row.orderStatus !== 3">发货</el-dropdown-item>
                      <el-dropdown-item command="complete" :disabled="row.orderStatus !== 4">完成</el-dropdown-item>
                      <el-dropdown-item command="cancel" :disabled="![0, 6].includes(row.orderStatus)" divided>取消</el-dropdown-item>
                      <el-dropdown-item command="delete" :disabled="[1, 2, 3, 4, 5].includes(row.orderStatus)">删除</el-dropdown-item>
                    </template>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <!-- 展开的订单详情（在订单下方） -->
          <div v-if="isExpanded(row)" class="order-detail">
            <template v-if="row._detail">
              <el-tabs
                :model-value="detailTabMap[row.orderId!] || 'info'"
                @update:model-value="(v: any) => detailTabMap[row.orderId!] = v"
                size="small">
                <el-tab-pane label="订单信息" name="info">
                  <el-descriptions :column="4" border size="small">
                    <el-descriptions-item label="订单号">{{ row._detail.orderNo }}</el-descriptions-item>
                    <el-descriptions-item label="订单类型">{{ row._detail.orderType || '—' }}</el-descriptions-item>
                    <el-descriptions-item label="订单状态">
                      <el-tag :type="statusTag(row._detail.orderStatus).type as any" size="small">{{ row._detail.orderStatusDesc }}</el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="财务状态">
                      <el-tag :type="financeTag(row._detail.financeStatus).type as any" size="small" effect="plain">{{ row._detail.financeStatusDesc }}</el-tag>
                    </el-descriptions-item>

                    <el-descriptions-item label="客户">{{ row._detail.customerName }}</el-descriptions-item>
                    <el-descriptions-item label="联系方式">{{ row._detail.contact }} {{ row._detail.phone }}</el-descriptions-item>
                    <el-descriptions-item label="物流">{{ row._detail.logistics || '—' }}</el-descriptions-item>
                    <el-descriptions-item label="单位">{{ row._detail.unit || '—' }}</el-descriptions-item>

                    <el-descriptions-item label="终端地址" :span="2">{{ row._detail.terminalAddress || row._detail.address || '—' }}</el-descriptions-item>
                    <el-descriptions-item label="品牌">{{ row._detail.brand || '—' }}</el-descriptions-item>
                    <el-descriptions-item label="安装方式">{{ row._detail.installType || '—' }}</el-descriptions-item>

                    <el-descriptions-item label="设计师">{{ row._detail.designer || '—' }}</el-descriptions-item>
                    <el-descriptions-item label="拆单师">{{ row._detail.splitter || '—' }}</el-descriptions-item>
                    <el-descriptions-item label="业务员">{{ row._detail.salesman || '—' }}</el-descriptions-item>
                    <el-descriptions-item label="客户来源">{{ row._detail.customerSource || '—' }}</el-descriptions-item>

                    <el-descriptions-item label="下单时间">{{ row._detail.orderDate || '—' }}</el-descriptions-item>
                    <el-descriptions-item label="计划交货">{{ row._detail.expectDate || '—' }}</el-descriptions-item>
                    <el-descriptions-item label="投影面积">{{ row._detail.projectionArea || 0 }} ㎡</el-descriptions-item>
                    <el-descriptions-item label="大板数">{{ row._detail.bigBoardNum || 0 }}</el-descriptions-item>

                    <el-descriptions-item v-if="row._detail.orderStatus === 6" label="驳回原因" :span="4">
                      <span class="text-danger">{{ row._detail.rejectReason }}</span>
                    </el-descriptions-item>
                    <el-descriptions-item v-if="row._detail.cancelReason" label="取消原因" :span="4">{{ row._detail.cancelReason }}</el-descriptions-item>
                  </el-descriptions>
                </el-tab-pane>

                <el-tab-pane label="产品明细" name="items">
                  <el-table :data="row._detail.itemList || []" border size="small">
                    <el-table-column type="index" label="#" width="42" align="center" />
                    <el-table-column label="子单号" prop="subOrderNo" width="140" />
                    <el-table-column label="产品" prop="productName" min-width="140" show-overflow-tooltip />
                    <el-table-column label="品目" prop="itemCategory" width="110" show-overflow-tooltip />
                    <el-table-column label="颜色" prop="color" width="76" show-overflow-tooltip />
                    <el-table-column label="网子" prop="netMaterial" width="80" show-overflow-tooltip />
                    <el-table-column label="把手" prop="handle" width="76" show-overflow-tooltip />
                    <el-table-column label="锁具" prop="lockSet" width="76" show-overflow-tooltip />
                    <el-table-column label="把手方向" prop="handleDirection" width="80" align="center" />
                    <el-table-column label="开向" prop="openDirection" width="70" align="center" />
                    <el-table-column label="加杆" prop="addRod" width="70" align="center" />
                    <el-table-column label="下固定" prop="fixedBottom" width="76" align="center" />
                    <el-table-column label="总宽" prop="width" width="76" align="right" />
                    <el-table-column label="总高" prop="height" width="76" align="right" />
                    <el-table-column label="扣宽" prop="deductWidth" width="70" align="right" />
                    <el-table-column label="净宽" prop="netWidth" width="76" align="right" />
                    <el-table-column label="数量" prop="num" width="56" align="center" />
                    <el-table-column label="单位" prop="unit" width="56" align="center" />
                    <el-table-column label="单价" prop="unitPrice" width="76" align="right" />
                    <el-table-column label="面积㎡" prop="itemTotalArea" width="86" align="right" />
                    <el-table-column label="金额" prop="lineAmount" width="92" align="right" />
                    <el-table-column label="备注" prop="remark" min-width="90" show-overflow-tooltip />
                  </el-table>
                </el-tab-pane>

                <el-tab-pane label="金额汇总" name="money">
                  <el-row :gutter="12">
                    <el-col :span="12">
                      <el-descriptions :column="2" border size="small">
                        <el-descriptions-item label="产品金额">{{ money(row._detail.productAmount) }}</el-descriptions-item>
                        <el-descriptions-item label="工艺加价">{{ money(row._detail.craftFee) }}</el-descriptions-item>
                        <el-descriptions-item label="加急费">{{ money(row._detail.urgentFee) }}</el-descriptions-item>
                        <el-descriptions-item label="运费">{{ money(row._detail.freight) }}</el-descriptions-item>
                        <el-descriptions-item label="优惠减价">{{ money(row._detail.discountAmount) }}</el-descriptions-item>
                        <el-descriptions-item label="总面积">{{ row._detail.totalArea }} ㎡</el-descriptions-item>
                      </el-descriptions>
                    </el-col>
                    <el-col :span="12">
                      <div class="sum-box">
                        <div class="sum-row"><span>订单总金额</span><b>￥{{ money(row._detail.totalAmount) }}</b></div>
                        <div class="sum-row"><span>已付金额</span><b>￥{{ money(row._detail.receiveAmount) }}</b></div>
                        <div class="sum-row"><span>未付金额</span><b class="text-danger">￥{{ money(row._detail.unpaidAmount) }}</b></div>
                        <div class="sum-row sum-total"><span>利润 / 毛利率</span>
                          <b class="price">￥{{ money(row._detail.profitAmount) }} / {{ percent(row._detail.grossProfitRate) }}</b>
                        </div>
                      </div>
                    </el-col>
                  </el-row>
                </el-tab-pane>

                <el-tab-pane label="备注信息" name="remark">
                  <div class="remark-wrap">
                    <!-- 订单原始备注 -->
                    <div v-if="row._detail.remark" class="remark-order">
                      <div class="remark-order-label">订单备注</div>
                      <div class="remark-order-text">{{ row._detail.remark }}</div>
                    </div>

                    <!-- 工作人员备注记录（类似物流详情：操作人 + 时间 + 内容） -->
                    <div class="remark-record-label">备注记录</div>
                    <div v-loading="row._remarksLoading" class="remark-list">
                      <div v-if="!row._remarks || row._remarks.length === 0" class="remark-empty">暂无备注记录</div>
                      <div v-for="r in row._remarks" :key="r.id" class="remark-item">
                        <div class="remark-item-head">
                          <span class="remark-name">{{ r.createByName || '系统' }}</span>
                          <span class="remark-time">{{ fmtTime(r.createTime) }}</span>
                        </div>
                        <div class="remark-item-text">{{ r.remark }}</div>
                      </div>
                    </div>

                    <!-- 添加备注 -->
                    <div class="remark-add">
                      <el-button type="primary" size="small" :loading="row._remarkSaving"
                        @click="addRemark(row)">添加备注</el-button>
                    </div>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </template>
            <div v-else class="detail-loading">详情加载中...</div>
          </div>
        </div>
        <el-empty v-if="!loading && tableData.length === 0" description="暂无订单" />
      </div>

      <div class="pager">
        <el-pagination v-model:current-page="page.pageNum" v-model:page-size="page.pageSize" :total="total"
          :page-sizes="[10, 20, 50, 100]" small layout="total, sizes, prev, pager, next, jumper" @change="loadTable" />
      </div>
    </el-card>

    <!-- 收款登记 -->
    <el-dialog v-model="payVisible" title="收款登记" width="420px" destroy-on-close>
      <el-form :model="payForm" label-width="92px" size="small">
        <el-form-item label="订单号">{{ payForm.orderNo }}</el-form-item>
        <el-form-item label="订单金额">￥{{ money(payForm.totalAmount) }}</el-form-item>
        <el-form-item label="已收金额">￥{{ money(payForm.received) }}</el-form-item>
        <el-form-item label="未付金额"><span class="text-danger">￥{{ money(payForm.unpaid) }}</span></el-form-item>
        <el-form-item label="本次收款" required>
          <el-input-number v-model="payForm.amount" :min="0" :precision="2" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="payForm.remark" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="payVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="paying" @click="submitPay">确认收款</el-button>
      </template>
    </el-dialog>

    <!-- 打印前选模板（默认自动按产品匹配） -->
    <PrintTemplatePicker ref="tplPicker" />
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'OrderList' })
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOrderPage, getOrderInfo, auditPassOrder, auditRejectOrder, delOrder, cancelOrder,
  deliverOrder, completeOrder, startProduceOrder, finishProduceOrder, receiveOrderPayment, settleOrder,
  getOrderRemarkList, saveOrderRemark, printOrders as printOrdersApi
} from '@/api/sales/order'
import { getWorkOrderDetail } from '@/api/production/workorder'
import { renderWorkOrdersHtml, renderSalesOrderHtml } from '@/utils/cuttingSheetRenderer'
import PrintTemplatePicker from '@/components/PrintTemplatePicker.vue'
import { ORDER_STATUS, FINANCE_STATUS, ORDER_TYPES, statusTag, financeTag, type SalesOrder } from '@/types/sales'

const router = useRouter()
const loading = ref(false)
/** 是否已经主动查询过：用于区分"还没查"和"查了没结果"两种空态 */
const queried = ref(false)
const tableData = ref<SalesOrder[]>([])
const total = ref(0)
const page = reactive({ pageNum: 1, pageSize: 20 })
const dateRange = ref<[string, string] | null>(null)
const search = reactive({
  orderNo: '', customerName: '', orderType: undefined as string | undefined,
  orderStatus: undefined as number | undefined, printFlag: undefined as number | undefined,
  financeStatus: undefined as number | undefined
})
// 订单状态 tabs（整体统一样式）
const statusTab = ref('')
const onTabStatus = () => {
  search.orderStatus = statusTab.value === '' ? undefined : Number(statusTab.value)
  page.pageNum = 1
  loadTable()
}

// ---------------- 批量打印（打印即开工） ----------------
const checkedIds = ref<number[]>([])
const printing = ref(false)
const tplPicker = ref<any>(null)
const onCheck = (row: SalesOrder, checked: boolean) => {
  const id = row.orderId!
  const idx = checkedIds.value.indexOf(id)
  if (checked && idx === -1) checkedIds.value.push(id)
  if (!checked && idx !== -1) checkedIds.value.splice(idx, 1)
}

// 全选/半选状态
const isAllChecked = computed(() => {
  const list = tableData.value || []
  return list.length > 0 && list.every((r: SalesOrder) => checkedIds.value.includes(r.orderId!))
})
const isIndeterminate = computed(() => {
  const list = tableData.value || []
  const checkedCount = list.filter((r: SalesOrder) => checkedIds.value.includes(r.orderId!)).length
  return checkedCount > 0 && checkedCount < list.length
})
const onCheckAll = (checked: boolean) => {
  const list = tableData.value || []
  if (checked) {
    checkedIds.value = list.map((r: SalesOrder) => r.orderId!)
  } else {
    checkedIds.value = []
  }
}

/** 批量打印选中订单：后端拆单生成工单并标记已打印/开工，前端合并渲染生产单 */
const printSelected = async () => {
  const ids = [...checkedIds.value]
  if (!ids.length) { ElMessage.warning('请先勾选要打印的订单'); return }
  // 打印前先选模板：默认自动按产品匹配，也可固定用某一套
  const choice = await (tplPicker.value?.open('WORK_ORDER', '打印生产单') ?? Promise.resolve(null))
  if (!choice) return
  printing.value = true
  try {
    const res: any = await printOrdersApi(ids)
    if (res.code !== 200) {
      ElMessage.error(res.msg || '打印接口调用失败')
      return
    }
    const list: any[] = res.data || []
    const okList = list.filter((x: any) => x.success)
    const failList = list.filter((x: any) => !x.success)
    if (failList.length) {
      ElMessage.warning(`${failList.length} 张订单打印失败：${failList.map((x: any) => x.orderNo || x.orderId).join('、')}（${failList[0]?.msg || ''}）`)
    }
    if (!okList.length) { ElMessage.error('没有可打印的订单'); return }
    // 有工单走工单详情（带扫码报工二维码）；没工单退化成订单详情直接出生产单，保证一定能打印
    const details: any[] = []
    for (const x of okList) {
      if (x.workId) {
        const d: any = await getWorkOrderDetail(x.workId)
        if (d.code === 200) { details.push(d.data); continue }
      }
      const o: any = await getOrderInfo(x.orderId)
      if (o.code === 200 && o.data) {
        details.push({ ...o.data, workNo: x.workNo || o.data.orderNo, orderItems: o.data.itemList || o.data.orderItems || [] })
      }
    }
    if (!details.length) { ElMessage.error('订单详情加载失败，无法打印'); return }
    const html = await renderWorkOrdersHtml(details, `批量生产单-${details.length}张`, choice.templateId)
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
    checkedIds.value = []
    ElMessage.success(`已打印 ${details.length} 张订单，订单已开工`)
    loadTable()
  } catch (e) {
    console.error('[批量打印] 失败', e)
    ElMessage.error('批量打印失败：' + (e as any)?.message)
  } finally {
    printing.value = false
  }
}

const orderTypeOptions = ORDER_TYPES
const money = (v?: number) => (Number(v) || 0).toFixed(2)
const percent = (v?: number) => `${((Number(v) || 0) * 100).toFixed(2)}%`
import { fmtTime } from '@/utils/time'
import { hasEditPerm } from '@/utils/perm'

// ---------------- 详情行内展开（点详情在订单下方展开，支持同时展开多个） ----------------
const expandedIds = ref<Set<number>>(new Set())
/** 每张订单各自记住停留在哪个详情页签，避免同时展开多张卡时切一个、其他全跟着跳 */
const detailTabMap = reactive<Record<number, string>>({})
const isExpanded = (row: SalesOrder) => expandedIds.value.has(row.orderId!)

/** 时间统一展示 yyyy-MM-dd HH:mm:ss（全局工具，见 utils/time.ts） */
// 已由全局 fmtTime 提供，此处不再重复定义

/** 加载订单备注记录 */
const loadRemarks = async (row: any) => {
  row._remarksLoading = true
  try {
    const res: any = await getOrderRemarkList(row.orderId!)
    row._remarks = res.code === 200 ? res.data || [] : []
  } catch (e) {
    console.error(e)
    row._remarks = []
  } finally {
    row._remarksLoading = false
  }
}

/** 添加工作人员备注 */
// 添加备注：弹出输入框（确认/取消），保存后记录操作人姓名与时间
const addRemark = async (row: any) => {
  let value = ''
  try {
    const r = await ElMessageBox.prompt('请输入备注内容（保存后记录操作人姓名与时间）', '添加备注', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '填写工作人员备注',
      inputValidator: (v: string) => (v && v.trim() ? true : '备注内容不能为空')
    })
    value = (r.value || '').trim()
  } catch (e) {
    return // 用户取消
  }
  row._remarkSaving = true
  try {
    const res: any = await saveOrderRemark({ orderId: row.orderId!, remark: value })
    if (res.code === 200) {
      ElMessage.success('备注已添加')
      await loadRemarks(row)
    }
  } finally {
    row._remarkSaving = false
  }
}

const toggleDetail = async (row: SalesOrder) => {
  // 已展开：收起
  if (expandedIds.value.has(row.orderId!)) {
    expandedIds.value.delete(row.orderId!)
    expandedIds.value = new Set(expandedIds.value)
    return
  }
  // 未加载过详情：先拉取订单详情（含产品明细/金额），只加载一次
  if (!row._detail) {
    try {
      const res: any = await getOrderInfo(row.orderId!)
      if (res.code === 200) (row as any)._detail = res.data
    } catch (e) {
      console.error(e)
      ElMessage.error('订单详情加载失败')
      return
    }
    // 同时加载备注记录（展开时只需一次，后续增删本地刷新）
    loadRemarks(row as any)
  }
  expandedIds.value.add(row.orderId!)
  expandedIds.value = new Set(expandedIds.value)
}

const loadTable = async () => {
  loading.value = true
  try {
    const params: any = { ...page, ...search }
    if (dateRange.value && dateRange.value.length === 2) {
      params.dateFrom = dateRange.value[0]
      params.dateTo = dateRange.value[1]
    }
    const res: any = await getOrderPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = Number(res.data.total) || 0
      // 列表刷新后收起所有已展开的详情（旧代码清的是已废弃的 expandedId，Set 实际没清）
      expandedIds.value = new Set()
      queried.value = true
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
const onSearch = () => { page.pageNum = 1; loadTable() }
const onReset = () => {
  search.orderNo = ''
  search.customerName = ''
  search.orderType = undefined
  search.orderStatus = undefined
  search.printFlag = undefined
  search.financeStatus = undefined
  statusTab.value = ''
  dateRange.value = null
  page.pageNum = 1
  loadTable()
}

const goCreate = () => router.push('/sales/order/edit')
const goEdit = (row: SalesOrder) => router.push({ path: '/sales/order/edit', query: { orderId: row.orderId } })
const canEdit = (row: SalesOrder) => [0, 6].includes(Number(row.orderStatus))

// 受理 / 驳回
const onPass = (row: any) => {
  ElMessageBox.confirm(`确认受理订单「${row.orderNo}」？受理后进入待排产。`, '订单受理', { type: 'success' })
    .then(async () => {
      const res: any = await auditPassOrder(row.orderId)
      if (res.code === 200) { ElMessage.success('已受理'); loadTable() }
    }).catch(() => {})
}
const onReject = (row: any) => {
  ElMessageBox.prompt('请填写驳回原因（退回制单人修改）', '驳回订单', {
    confirmButtonText: '确认驳回', cancelButtonText: '取消', inputType: 'textarea',
    inputValidator: (v: string) => !!v?.trim() || '驳回原因必填'
  }).then(async ({ value }) => {
    const res: any = await auditRejectOrder(row.orderId, value)
    if (res.code === 200) { ElMessage.success('已驳回'); loadTable() }
  }).catch(() => {})
}

// 收款
const payVisible = ref(false)
const paying = ref(false)
const payForm = reactive<any>({ orderId: undefined, orderNo: '', totalAmount: 0, received: 0, unpaid: 0, amount: 0, remark: '' })
const openPay = (row: any) => {
  Object.assign(payForm, {
    orderId: row.orderId, orderNo: row.orderNo,
    totalAmount: Number(row.totalAmount) || 0,
    received: Number(row.receiveAmount) || 0,
    unpaid: Number(row.unpaidAmount) || 0,
    amount: Number(row.unpaidAmount) || 0,
    remark: ''
  })
  payVisible.value = true
}
const submitPay = async () => {
  if (!(Number(payForm.amount) > 0)) { ElMessage.warning('请输入大于 0 的收款金额'); return }
  paying.value = true
  try {
    const res: any = await receiveOrderPayment(payForm.orderId, { amount: Number(payForm.amount), remark: payForm.remark })
    if (res.code === 200) {
      ElMessage.success('收款已登记')
      payVisible.value = false
      loadTable()
    }
  } finally {
    paying.value = false
  }
}

/** 打印销售单：读取订单详情 → 用 SALES_ORDER 模板渲染（与生产单同一套渲染器） */
const printSales = async (row: any) => {
  try {
    const choice = await (tplPicker.value?.open('SALES_ORDER', '打印销售单') ?? Promise.resolve(null))
    if (!choice) return
    const res: any = await getOrderInfo(row.orderId)
    if (res.code !== 200 || !res.data) { ElMessage.error('订单详情加载失败'); return }
    const html = await renderSalesOrderHtml(res.data, `销售单-${row.orderNo || ''}`, choice.templateId)
    const win = window.open('', '_blank')
    if (!win) { ElMessage.warning('浏览器拦截了弹窗，请允许弹窗后重试'); return }
    win.document.write(html)
    win.document.close()
    win.focus()
    setTimeout(() => {
      try { win.onafterprint = () => win.close() } catch { /* 忽略 */ }
      win.print()
    }, 300)
  } catch (e) {
    console.error('[打印销售单] 失败', e)
    ElMessage.error('打印失败：' + (e as any)?.message)
  }
}

// 更多操作
const onCommand = (cmd: string, row: any) => {  const id = row.orderId
  const simple = (fn: () => Promise<any>, text: string) => {
    ElMessageBox.confirm(`确认${text}订单「${row.orderNo}」？`, text, { type: 'warning' })
      .then(async () => {
        const res: any = await fn()
        if (res.code === 200) { ElMessage.success(`${text}成功`); loadTable() }
      }).catch(() => {})
  }
  switch (cmd) {
    case 'printSales':
      printSales(row)
      break
    case 'receive':
      openPay(row)
      break
    case 'settle':
      simple(() => settleOrder(id), '结清')
      break
    case 'start':
      simple(() => startProduceOrder(id), '开工')
      break
    case 'finish':
      simple(() => finishProduceOrder(id), '完工')
      break
    case 'deliver':
      simple(() => deliverOrder(id), '发货')
      break
    case 'complete':
      simple(() => completeOrder(id), '完成')
      break
    case 'cancel':
      ElMessageBox.prompt('请填写取消原因', '取消订单', {
        confirmButtonText: '确认取消', cancelButtonText: '返回', inputType: 'textarea'
      }).then(async ({ value }) => {
        const res: any = await cancelOrder(id, value)
        if (res.code === 200) { ElMessage.success('已取消'); loadTable() }
      }).catch(() => {})
      break
    case 'delete':
      simple(() => delOrder(id), '删除')
      break
  }
}

// onMounted(loadTable) // 初始不自动加载，点查询才出数据
</script>

<style scoped>
.sales-page { padding: 12px; }
.search-card :deep(.el-card__body) { padding: 12px 12px 0; }
.status-tabs { margin: 6px 0 0; }
.status-tabs :deep(.el-tabs__header) { margin-bottom: 6px; }
.table-card { margin-top: 10px; }
.table-card :deep(.el-card__body) { padding: 10px 12px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.toolbar-left { display: flex; align-items: center; gap: 8px; }
.tip { font-size: 12px; color: #909399; margin-left: 6px; }
.pager { margin-top: 12px; display: flex; justify-content: flex-end; }

/* ---------- 订单卡片列表（仿物流详情模板） ---------- */
.order-list { display: flex; flex-direction: column; gap: 10px; }
.order-card {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fff;
  overflow: hidden;
  transition: box-shadow 0.2s;
}
.order-card:hover { box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06); }
.order-card.expanded { border-color: #409eff; box-shadow: 0 2px 10px rgba(64, 158, 255, 0.15); }

/* 信息栏：每格独立展示，留白不挤压 */
.order-head {
  display: flex;
  flex-wrap: wrap;
  align-items: stretch;
  background: #fafbfc;
  border-bottom: 1px solid #e4e7ed;
}
.head-cell {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 96px;
  padding: 8px 12px;
  border-right: 1px dashed #ebeef5;
  gap: 2px;
}
.cell-label {
  font-size: 11px;
  color: #909399;
  line-height: 1.2;
}
.cell-value {
  font-size: 13px;
  color: #303133;
  line-height: 1.4;
  word-break: break-all;
}
.cell-value b { color: #303133; }

.seq { font-size: 15px; font-weight: 600; color: #409eff; }
.seq-line { display: inline-flex; align-items: center; gap: 6px; }
.print-time { display: block; font-size: 11px; color: #909399; }
.order-no-cell { min-width: 168px; }
.sub-tag { margin-left: 4px; vertical-align: middle; }
.addr-cell { min-width: 200px; flex: 1 1 auto; }
.addr { max-width: 240px; white-space: normal; word-break: break-all; overflow-wrap: anywhere; }
.money-cell { min-width: 170px; background: #f7faff; }
.money { color: #303133; }
.unpaid { display: block; font-size: 12px; color: #f56c6c; }

.head-actions {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 6px 10px;
  margin-left: auto;
  background: #fff;
}

/* ---------- 展开详情 ---------- */
.order-detail {
  padding: 12px 14px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  /* 详情区限高：订单备注/报工流水再多也不撑爆页面，超出可滚动查看 */
  max-height: 70vh;
  overflow-y: auto;
}
.detail-loading { padding: 24px 0; text-align: center; color: #909399; font-size: 13px; }
.order-detail :deep(.el-tabs__header) { margin-bottom: 10px; }
.order-detail :deep(.el-table) { margin-top: 4px; }

.sum-box { border: 1px solid #ebeef5; border-radius: 4px; padding: 6px 14px; background: #fafafa; }
.sum-row { display: flex; justify-content: space-between; line-height: 26px; font-size: 13px; }
.sum-total { border-top: 1px dashed #dcdfe6; margin-top: 4px; padding-top: 4px; font-size: 14px; }
.price { color: #f56c6c; }
.text-danger { color: #f56c6c; }

/* ---------- 备注信息（工作人员备注） ---------- */
.remark-wrap { max-width: 860px; }
.remark-order {
  border: 1px solid #f0c4a2;
  background: #fdf6ef;
  border-radius: 4px;
  padding: 8px 12px;
  margin-bottom: 14px;
}
.remark-order-label { font-size: 12px; color: #b06f2a; font-weight: 600; margin-bottom: 4px; }
.remark-order-text { font-size: 13px; color: #303133; line-height: 1.6; white-space: pre-line; word-break: break-all; }
.remark-record-label { font-size: 12px; color: #909399; margin-bottom: 8px; }
.remark-list { display: flex; flex-direction: column; gap: 8px; min-height: 40px; }
.remark-empty { padding: 14px 0; text-align: center; color: #c0c4cc; font-size: 13px; }
.remark-item {
  border: 1px solid #ebeef5;
  border-left: 3px solid #409eff;
  border-radius: 4px;
  padding: 8px 12px;
  background: #fafbfc;
}
.remark-item-head { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; }
.remark-name { font-size: 12px; font-weight: 600; color: #409eff; }
.remark-time { font-size: 12px; color: #909399; }
.remark-item-text { font-size: 13px; color: #303133; line-height: 1.6; white-space: pre-wrap; }
.remark-add { margin-top: 14px; }
</style>
