<template>
  <div class="stock-page">
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-head">
          <span class="block-title">成品入库</span>
          <div>
            <el-input v-model="query.workOrderId" placeholder="工单ID(可空)" clearable size="small" style="width:130px"
              @keyup.enter="loadTable" />
            <el-select v-model="query.status" placeholder="入库状态" clearable size="small" style="width:130px">
              <el-option label="待入库" :value="1" />
              <el-option label="已入库" :value="2" />
            </el-select>
            <el-button size="small" type="primary" @click="loadTable">查询</el-button>
            <el-button size="small" @click="reset">重置</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" border stripe size="small" empty-text="点查询查看入库记录">
        <el-table-column label="入库时间" prop="instock_time" width="175" :formatter="fmtTime" />
        <el-table-column label="订单编号" prop="orderNo" width="150" />
        <el-table-column label="工单号" prop="workNo" width="140" />
        <el-table-column label="客户" prop="customerName" width="110" show-overflow-tooltip />
        <el-table-column label="客户电话" prop="customerPhone" width="120" />
        <el-table-column label="产品名称" prop="productName" min-width="150" show-overflow-tooltip />
        <el-table-column label="产品类型" prop="productType" width="130" show-overflow-tooltip />
        <el-table-column label="产品数量" prop="productNum" width="80" align="center" />
        <el-table-column label="入库数量" prop="in_num" width="80" align="center" />
        <el-table-column label="货架" prop="shelfName" width="110" />
        <el-table-column label="入库人" width="110">
          <template #default="s">{{ s.row.userName || s.row.realName || '—' }}</template>
        </el-table-column>
        <el-table-column label="入库人电话" prop="userPhone" width="120" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="s">
            <el-tag :type="Number(s.row.instock_status) === 2 ? 'success' : 'warning'" size="small">
              {{ Number(s.row.instock_status) === 2 ? '已入库' : '待入库' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="130" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'InstockList' })
import { ref } from 'vue'
import { getInstockList } from '@/api/production/workorder'

const tableData = ref<any[]>([])
const query = ref<{ workOrderId?: string; status?: number }>({})

const fmtTime = (row: any) => String(row?.instock_time || '').replace('T', ' ')

const loadTable = async () => {
  const params: any = {}
  if (query.value.workOrderId) params.workOrderId = Number(query.value.workOrderId)
  if (query.value.status) params.status = query.value.status
  const res = await getInstockList(params)
  if (res.code === 200) tableData.value = res.data || []
}
const reset = () => { query.value = {}; tableData.value = [] }
</script>

<style scoped>
.stock-page { padding: 12px; }
.table-card :deep(.el-card__body) { padding: 12px; }
.card-head { display: flex; justify-content: space-between; align-items: center; }
.block-title { font-weight: bold; font-size: 14px; }
.card-head > div { display: flex; gap: 8px; align-items: center; }
</style>
