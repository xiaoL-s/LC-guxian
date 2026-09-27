<template>
  <div class="sales-page">
    <!-- 搜索 -->
    <el-card shadow="never" class="search-card">
      <div class="search-bar">
        <el-input v-model="search.keyword" placeholder="搜索：订单号 / 子单号 / 客户 / 产品" clearable size="small"
          style="width: 260px" @keyup.enter="onSearch">
          <template #prepend>
            <span class="prepend">搜索</span>
          </template>
        </el-input>

        <el-select v-model="search.orderStatus" placeholder="订单状态" clearable size="small" style="width: 130px">
          <el-option v-for="s in ORDER_STATUS" :key="s.code" :label="s.label" :value="s.code" />
        </el-select>
        <el-select v-model="search.financeStatus" placeholder="财务状态" clearable size="small" style="width: 130px">
          <el-option v-for="s in FINANCE_STATUS" :key="s.code" :label="s.label" :value="s.code" />
        </el-select>
        <el-select v-model="search.itemCategory" placeholder="品目" clearable filterable size="small" style="width: 150px">
          <el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" />
        </el-select>

        <el-radio-group v-model="quickRange" size="small" @change="onQuickRange">
          <el-radio-button label="recent2m">最近2月</el-radio-button>
          <el-radio-button label="year">本年</el-radio-button>
          <el-radio-button label="all">全部</el-radio-button>
        </el-radio-group>
        <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" size="small"
          start-placeholder="下单开始" end-placeholder="下单结束" style="width: 230px" @change="onSearch" />

        <el-button type="primary" size="small" @click="onSearch">查询</el-button>
        <el-button size="small" @click="onReset">重置</el-button>
      </div>
    </el-card>

    <!-- 明细列表 -->
    <el-card shadow="never" class="table-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button v-if="mode === 'entry'" type="primary" size="small" @click="goCreate">＋ 新增下单</el-button>
          <el-button size="small" @click="loadAll">刷新</el-button>
          <el-button size="small" disabled>打印</el-button>
          <el-button size="small" disabled>产品目录筛选</el-button>
          <el-dropdown trigger="click">
            <el-button size="small">更多操作</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>导出 Excel（预留）</el-dropdown-item>
                <el-dropdown-item disabled>批量受理（预留）</el-dropdown-item>
                <el-dropdown-item disabled>批量打印标签（预留）</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div class="toolbar-right">
          <el-popover placement="bottom-end" :width="520" trigger="click">
            <template #reference>
              <el-button size="small">设置</el-button>
            </template>
            <div class="col-setting">
              <el-checkbox-group v-model="visibleCols">
                <el-checkbox v-for="c in optionalCols" :key="c.key" :label="c.key" :value="c.key">{{ c.label }}</el-checkbox>
              </el-checkbox-group>
              <div class="col-setting-foot">
                <el-button size="small" link type="primary" @click="visibleCols = optionalCols.map(c => c.key)">全选</el-button>
                <el-button size="small" link @click="visibleCols = []">全不选</el-button>
              </div>
            </div>
          </el-popover>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe size="small"
        height="calc(100vh - 330px)" :row-class-name="rowClass"
        :header-cell-style="{ background: '#f5f7fa', color: '#303133', fontSize: '12px', padding: '4px 0' }"
        :cell-style="{ padding: '2px 0', fontSize: '12px' }">
        <el-table-column type="index" label="#" width="42" align="center" fixed />
        <el-table-column label="订单号" prop="orderNo" width="126" fixed show-overflow-tooltip />
        <el-table-column label="子单号" prop="subOrderNo" width="138" fixed show-overflow-tooltip />
        <el-table-column label="产品" prop="productName" min-width="130" fixed show-overflow-tooltip />
        <el-table-column v-if="show('itemCategory')" label="品目" prop="itemCategory" width="108" show-overflow-tooltip />
        <el-table-column v-if="show('customerName')" label="客户" prop="customerName" width="110" show-overflow-tooltip />

        <el-table-column v-if="show('width')" label="总宽" prop="width" width="70" align="right" />
        <el-table-column v-if="show('height')" label="总高" prop="height" width="70" align="right" />
        <el-table-column v-if="show('deductWidth')" label="扣宽" prop="deductWidth" width="64" align="right" />
        <el-table-column v-if="show('netWidth')" label="净宽" prop="netWidth" width="70" align="right" />

        <el-table-column v-if="show('color')" label="颜色" prop="color" width="80" show-overflow-tooltip />
        <el-table-column v-if="show('netMaterial')" label="网子" prop="netMaterial" width="80" show-overflow-tooltip />
        <el-table-column v-if="show('handle')" label="把手" prop="handle" width="76" show-overflow-tooltip />
        <el-table-column v-if="show('lockSet')" label="锁具" prop="lockSet" width="76" show-overflow-tooltip />
        <el-table-column v-if="show('handleDirection')" label="把手方向" prop="handleDirection" width="80" align="center" />
        <el-table-column v-if="show('openDirection')" label="开向" prop="openDirection" width="66" align="center" />
        <el-table-column v-if="show('addRod')" label="加杆" prop="addRod" width="66" align="center" />
        <el-table-column v-if="show('fixedBottom')" label="下固定" prop="fixedBottom" width="72" align="center" />
        <el-table-column v-if="show('squareBoard')" label="方板" prop="squareBoard" width="72" align="center" />

        <el-table-column v-if="show('num')" label="数量" prop="num" width="56" align="center" />
        <el-table-column v-if="show('unit')" label="单位" prop="unit" width="56" align="center" />
        <el-table-column v-if="show('unitPrice')" label="单价" prop="unitPrice" width="72" align="right" />
        <el-table-column v-if="show('itemTotalArea')" label="面积㎡" prop="itemTotalArea" width="82" align="right" />
        <el-table-column v-if="show('lineAmount')" label="销售金额" prop="lineAmount" width="90" align="right">
          <template #default="s"><b>{{ money(s.row.lineAmount) }}</b></template>
        </el-table-column>
        <el-table-column v-if="show('receiveAmount')" label="实收金额" prop="receiveAmount" width="90" align="right">
          <template #default="s">{{ money(s.row.receiveAmount) }}</template>
        </el-table-column>
        <el-table-column v-if="show('unpaidAmount')" label="未付金额" width="90" align="right">
          <template #default="s"><span class="text-danger">{{ money(rowUnpaid(s.row)) }}</span></template>
        </el-table-column>
        <el-table-column v-if="show('profitAmount')" label="利润" prop="profitAmount" width="86" align="right">
          <template #default="s">{{ money(s.row.profitAmount) }}</template>
        </el-table-column>
        <el-table-column v-if="show('grossProfitRate')" label="毛利率" width="76" align="right">
          <template #default="s">{{ percent(s.row.grossProfitRate) }}</template>
        </el-table-column>
        <el-table-column v-if="show('freight')" label="运费" prop="freight" width="72" align="right">
          <template #default="s">{{ money(s.row.freight) }}</template>
        </el-table-column>

        <el-table-column v-if="show('orderType')" label="订单类型" prop="orderType" width="88" align="center" />
        <el-table-column v-if="show('orderStatus')" label="订单状态" width="98" align="center">
          <template #default="s">
            <el-tag :type="statusTag(s.row.itemStatus ?? s.row.orderStatus).type as any" size="small">{{ s.row.orderStatusDesc }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="show('financeStatus')" label="财务状态" width="104" align="center">
          <template #default="s">
            <el-tag :type="financeTag(s.row.financeStatus).type as any" size="small" effect="plain">{{ s.row.financeStatusDesc }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column v-if="show('terminalAddress')" label="终端地址" prop="terminalAddress" min-width="130" show-overflow-tooltip />
        <el-table-column v-if="show('logistics')" label="物流" prop="logistics" width="86" show-overflow-tooltip />
        <el-table-column v-if="show('contact')" label="联系方式" width="112" show-overflow-tooltip>
          <template #default="s">{{ s.row.contact }}<span v-if="s.row.phone"> / {{ s.row.phone }}</span></template>
        </el-table-column>
        <el-table-column v-if="show('salesOwner')" label="销售商/下单员" prop="salesOwner" width="106" show-overflow-tooltip />

        <el-table-column v-if="show('processName')" label="工序" prop="processName" width="86" align="center">
          <template #default="s">{{ s.row.processName || '—' }}</template>
        </el-table-column>
        <el-table-column v-if="show('flowStatus')" label="生产流程" prop="flowStatus" width="96" align="center">
          <template #default="s">{{ s.row.flowStatus || '—' }}</template>
        </el-table-column>
        <el-table-column v-if="show('produceProgress')" label="生产进度" width="96" align="center">
          <template #default="s">
            <el-progress :percentage="Number(s.row.produceProgress) || 0" :stroke-width="8" :text-inside="true" />
          </template>
        </el-table-column>

        <el-table-column v-if="show('orderDate')" label="下单时间" prop="orderDate" width="100" align="center" />
        <el-table-column v-if="show('expectDate')" label="计划交货" prop="expectDate" width="100" align="center" />
        <el-table-column v-if="show('createTime')" label="创建时间" prop="createTime" width="150" align="center" />
        <el-table-column v-if="show('remark')" label="备注" prop="remark" min-width="110" show-overflow-tooltip />

        <el-table-column label="操作" width="128" fixed="right" align="center">
          <template #default="s">
            <el-button v-if="mode === 'entry' && [0, 6].includes(Number(s.row.orderStatus))" size="small" type="warning" link
              @click="goEdit(s.row)">编辑</el-button>
            <el-button size="small" type="primary" link @click="viewDetail(s.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 加载统计 -->
      <div class="stats-bar">
        <div class="stats-left">
          <span class="stats-title">加载统计</span>
          <span class="stat">总数量 <b>{{ numText(stats.totalNum) }}</b></span>
          <span class="stat">总面积 <b>{{ numText(stats.totalArea) }}</b> ㎡</span>
          <span class="stat">总销售额 <b class="money">{{ money(stats.totalAmount) }}</b></span>
          <span class="stat">总实收 <b>{{ money(stats.totalReceive) }}</b></span>
          <span class="stat">总未付金额 <b class="text-danger">{{ money(stats.totalUnpaid) }}</b></span>
          <span class="stat">总款数 <b>{{ stats.totalRows }}</b></span>
        </div>
        <el-pagination v-model:current-page="page.pageNum" v-model:page-size="page.pageSize" :total="total"
          :page-sizes="[20, 50, 100, 200]" small layout="total, sizes, prev, pager, next" @change="loadTable" />
      </div>
    </el-card>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" :title="`订单详情 - ${detail.orderNo || ''}`" width="900px" destroy-on-close top="6vh">
      <el-descriptions :column="3" border size="small">
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="子单号">{{ detail.subOrderNo }}</el-descriptions-item>
        <el-descriptions-item label="品目">{{ detail.itemCategory }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ detail.customerName }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ detail.contact }} {{ detail.phone }}</el-descriptions-item>
        <el-descriptions-item label="物流">{{ detail.logistics || '—' }}</el-descriptions-item>
        <el-descriptions-item label="终端地址" :span="2">{{ detail.terminalAddress || '—' }}</el-descriptions-item>
        <el-descriptions-item label="订单类型">{{ detail.orderType }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="statusTag(detail.orderStatus).type as any" size="small">{{ detail.orderStatusDesc }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="财务状态">
          <el-tag :type="financeTag(detail.financeStatus).type as any" size="small" effect="plain">{{ detail.financeStatusDesc }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ detail.orderDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="计划交货">{{ detail.expectDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="产品">{{ detail.productName }}</el-descriptions-item>
        <el-descriptions-item label="颜色">{{ detail.color || '—' }}</el-descriptions-item>
        <el-descriptions-item label="网子">{{ detail.netMaterial || '—' }}</el-descriptions-item>
        <el-descriptions-item label="把手">{{ detail.handle || '—' }}</el-descriptions-item>
        <el-descriptions-item label="锁具">{{ detail.lockSet || '—' }}</el-descriptions-item>
        <el-descriptions-item label="把手方向">{{ detail.handleDirection || '—' }}</el-descriptions-item>
        <el-descriptions-item label="开向">{{ detail.openDirection || '—' }}</el-descriptions-item>
        <el-descriptions-item label="加杆">{{ detail.addRod || '—' }}</el-descriptions-item>
        <el-descriptions-item label="下固定">{{ detail.fixedBottom || '—' }}</el-descriptions-item>
        <el-descriptions-item label="方板">{{ detail.squareBoard || '—' }}</el-descriptions-item>
        <el-descriptions-item label="总宽×总高">{{ detail.width }} × {{ detail.height }} mm</el-descriptions-item>
        <el-descriptions-item label="扣宽/净宽">{{ detail.deductWidth || 0 }} / {{ detail.netWidth || detail.width }}</el-descriptions-item>
        <el-descriptions-item label="数量×单价">{{ detail.num }} × {{ money(detail.unitPrice) }}</el-descriptions-item>
        <el-descriptions-item label="面积">{{ detail.itemTotalArea }} ㎡</el-descriptions-item>
        <el-descriptions-item label="销售金额">{{ money(detail.lineAmount) }}</el-descriptions-item>
        <el-descriptions-item label="实收金额">{{ money(detail.receiveAmount) }}</el-descriptions-item>
        <el-descriptions-item label="利润/毛利率">{{ money(detail.profitAmount) }} / {{ percent(detail.grossProfitRate) }}</el-descriptions-item>
        <el-descriptions-item label="工序">{{ detail.processName || '—（生产预留）' }}</el-descriptions-item>
        <el-descriptions-item label="生产流程">{{ detail.flowStatus || '—（生产预留）' }}</el-descriptions-item>
        <el-descriptions-item label="生产进度">{{ Number(detail.produceProgress) || 0 }}%</el-descriptions-item>
        <el-descriptions-item label="备注" :span="3">{{ detail.remark || '—' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button size="small" @click="detailVisible = false">关闭</el-button>
        <el-button v-if="mode === 'entry' && [0, 6].includes(Number(detail.orderStatus))" size="small" type="warning"
          @click="detailVisible = false; goEdit(detail)">编辑该订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderItemPage, getOrderItemStats, getOrderDictOptions } from '@/api/sales/order'
import { ORDER_STATUS, FINANCE_STATUS, statusTag, financeTag, type SalesOrderItemRow, type SalesOrderStats } from '@/types/sales'

const props = withDefaults(defineProps<{ mode?: string }>(), { mode: 'entry' })
const router = useRouter()

const loading = ref(false)
const tableData = ref<SalesOrderItemRow[]>([])
const total = ref(0)
const page = reactive({ pageNum: 1, pageSize: 20 })
const dateRange = ref<[string, string] | null>(null)
const quickRange = ref('recent2m')
const search = reactive({
  keyword: '',
  orderStatus: undefined as number | undefined,
  financeStatus: undefined as number | undefined,
  itemCategory: undefined as string | undefined,
  dateFrom: '' as string,
  dateTo: '' as string
})

const stats = reactive<SalesOrderStats>({ totalNum: 0, totalArea: 0, totalAmount: 0, totalUnpaid: 0, totalReceive: 0, totalRows: 0 })
const categoryOptions = ref<string[]>([])

const money = (v?: number) => (Number(v) || 0).toFixed(2)
const percent = (v?: number) => `${((Number(v) || 0) * 100).toFixed(2)}%`
const numText = (v?: number) => {
  const n = Number(v) || 0
  return Number.isInteger(n) ? String(n) : n.toFixed(2)
}
const rowUnpaid = (row: SalesOrderItemRow) => Math.max((Number(row.lineAmount) || 0) - (Number(row.receiveAmount) || 0), 0)
const rowClass = ({ row }: { row: SalesOrderItemRow }) => {
  const st = Number(row.itemStatus ?? row.orderStatus)
  if (st === 0) return 'row-untreated'
  if (st === 6) return 'row-rejected'
  return ''
}

// 列设置
const optionalCols = [
  { key: 'itemCategory', label: '品目' },
  { key: 'customerName', label: '客户' },
  { key: 'width', label: '总宽' }, { key: 'height', label: '总高' },
  { key: 'deductWidth', label: '扣宽' }, { key: 'netWidth', label: '净宽' },
  { key: 'color', label: '颜色' }, { key: 'netMaterial', label: '网子' },
  { key: 'handle', label: '把手' }, { key: 'lockSet', label: '锁具' },
  { key: 'handleDirection', label: '把手方向' }, { key: 'openDirection', label: '开向' },
  { key: 'addRod', label: '加杆' }, { key: 'fixedBottom', label: '下固定' }, { key: 'squareBoard', label: '方板' },
  { key: 'num', label: '数量' }, { key: 'unit', label: '单位' }, { key: 'unitPrice', label: '单价' },
  { key: 'itemTotalArea', label: '面积' }, { key: 'lineAmount', label: '销售金额' },
  { key: 'receiveAmount', label: '实收金额' }, { key: 'unpaidAmount', label: '未付金额' },
  { key: 'profitAmount', label: '利润' }, { key: 'grossProfitRate', label: '毛利率' }, { key: 'freight', label: '运费' },
  { key: 'orderType', label: '订单类型' }, { key: 'orderStatus', label: '订单状态' }, { key: 'financeStatus', label: '财务状态' },
  { key: 'terminalAddress', label: '终端地址' }, { key: 'logistics', label: '物流' }, { key: 'contact', label: '联系方式' },
  { key: 'salesOwner', label: '销售商/下单员' },
  { key: 'processName', label: '工序' }, { key: 'flowStatus', label: '生产流程' }, { key: 'produceProgress', label: '生产进度' },
  { key: 'orderDate', label: '下单时间' }, { key: 'expectDate', label: '计划交货' },
  { key: 'createTime', label: '创建时间' }, { key: 'remark', label: '备注' }
]
const HIDDEN_BY_DEFAULT = ['deductWidth', 'netWidth', 'squareBoard', 'freight', 'processName', 'flowStatus', 'produceProgress', 'createTime', 'salesOwner']
const visibleCols = ref<string[]>(optionalCols.map(c => c.key).filter(k => !HIDDEN_BY_DEFAULT.includes(k)))
const show = (key: string) => visibleCols.value.includes(key)

const buildParams = () => {
  const params: any = { pageNum: page.pageNum, pageSize: page.pageSize, ...search }
  if (dateRange.value && dateRange.value.length === 2) {
    params.dateFrom = dateRange.value[0]
    params.dateTo = dateRange.value[1]
  }
  return params
}

const loadTable = async () => {
  loading.value = true
  try {
    const res: any = await getOrderItemPage(buildParams())
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = Number(res.data.total) || 0
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  const res: any = await getOrderItemStats(buildParams())
  if (res.code === 200 && res.data) {
    Object.assign(stats, res.data)
  }
}

const loadAll = () => Promise.all([loadTable(), loadStats()])
const onSearch = () => { page.pageNum = 1; loadAll() }
const onReset = () => {
  search.keyword = ''
  search.orderStatus = undefined
  search.financeStatus = undefined
  search.itemCategory = undefined
  quickRange.value = 'recent2m'
  onQuickRange('recent2m')
  page.pageNum = 1
  loadAll()
}

/** 快捷时间范围 */
const onQuickRange = (val: any) => {
  const now = new Date()
  if (val === 'all') {
    dateRange.value = null
  } else if (val === 'year') {
    dateRange.value = [`${now.getFullYear()}-01-01`, `${now.getFullYear()}-12-31`]
  } else {
    const from = new Date(now.getFullYear(), now.getMonth() - 2, now.getDate())
    dateRange.value = [fmtDate(from), fmtDate(now)]
  }
  if (dateRange.value) {
    search.dateFrom = dateRange.value[0]
    search.dateTo = dateRange.value[1]
  } else {
    search.dateFrom = ''
    search.dateTo = ''
  }
  onSearch()
}
const fmtDate = (d: Date) => {
  const m = `${d.getMonth() + 1}`.padStart(2, '0')
  const day = `${d.getDate()}`.padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
}

const goCreate = () => router.push('/sales/order/edit')
const goEdit = (row: SalesOrderItemRow) => router.push({ path: '/sales/order/edit', query: { orderId: row.orderId } })

const detailVisible = ref(false)
const detail = reactive<any>({})
const viewDetail = (row: SalesOrderItemRow) => {
  Object.keys(detail).forEach(k => delete (detail as any)[k])
  Object.assign(detail, row)
  detailVisible.value = true
}

onMounted(async () => {
  onQuickRange('recent2m')
  const res: any = await getOrderDictOptions()
  if (res.code === 200 && res.data?.product) {
    const names = new Set<string>()
    res.data.product.forEach((p: any) => { if (p.typeName) names.add(p.typeName) })
    categoryOptions.value = Array.from(names)
  }
})
</script>

<style scoped>
.sales-page { padding: 12px; }
.search-card :deep(.el-card__body) { padding: 10px 12px; }
.search-bar { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; }
.prepend { font-size: 12px; color: #606266; }
.table-card { margin-top: 10px; }
.table-card :deep(.el-card__body) { padding: 10px 12px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.toolbar-left { display: flex; align-items: center; gap: 6px; }
.col-setting { max-height: 320px; overflow-y: auto; }
.col-setting-foot { border-top: 1px solid #ebeef5; margin-top: 6px; padding-top: 4px; text-align: right; }
:deep(.el-checkbox) { width: 108px; margin-right: 6px; }
.stats-bar { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; padding: 6px 10px; background: #f5f7fa; border: 1px solid #ebeef5; border-radius: 4px; }
.stats-left { display: flex; align-items: center; gap: 16px; font-size: 12px; color: #606266; }
.stats-title { font-weight: bold; color: #303133; }
.stat b { color: #303133; }
.money { color: #409eff; }
.text-danger { color: #f56c6c; }
:deep(.row-untreated) { --el-table-tr-bg-color: #fef0f0; }
:deep(.row-rejected) { --el-table-tr-bg-color: #fdf6ec; }
</style>
