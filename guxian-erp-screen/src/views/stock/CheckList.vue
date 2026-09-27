<template>
  <div class="stock-page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="search" size="small">
        <el-form-item label="盘点单号">
          <el-input v-model="search.keyword" placeholder="盘点单号" clearable style="width:190px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="search.status" placeholder="全部" clearable style="width:110px">
            <el-option label="草稿" :value="0" />
            <el-option label="已过账" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
          <el-button type="primary" @click="onCreate">新建盘点单</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column label="盘点单号" prop="checkNo" width="170" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="s">
            <el-tag :type="s.row.checkStatus === 1 ? 'success' : 'warning'" size="small">
              {{ s.row.checkStatus === 1 ? '已过账' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="差异汇总" width="110" align="right">
          <template #default="s">
            <b :style="{ color: Number(s.row.totalDiff) === 0 ? '#909399' : (Number(s.row.totalDiff) > 0 ? '#67c23a' : '#f56c6c') }">
              {{ fmt(s.row.totalDiff) }}
            </b>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="130" show-overflow-tooltip />
        <el-table-column label="创建时间" prop="createTime" width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="s">
            <el-button size="small" type="primary" link @click="openDetail(s.row)">录入/查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination v-model:current-page="page.pageNum" v-model:page-size="page.pageSize" :total="total"
          small layout="total, sizes, prev, pager, next" @change="loadTable" />
      </div>
    </el-card>

    <!-- 盘点单详情/录入 -->
    <el-dialog v-model="detailVisible" :title="`盘点单 ${detail.checkNo || ''}`" width="760px" destroy-on-close>
      <template v-if="detail.checkId">
        <el-descriptions :column="3" border size="small" style="margin-bottom:10px">
          <el-descriptions-item label="状态">
            <el-tag :type="detail.checkStatus === 1 ? 'success' : 'warning'" size="small">
              {{ detail.checkStatus === 1 ? '已过账' : '草稿' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="差异汇总">{{ fmt(detail.totalDiff) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="detail.items" border stripe size="small" max-height="400">
          <el-table-column label="物料编码" prop="materialCode" width="100" />
          <el-table-column label="物料名称" prop="materialName" min-width="130" show-overflow-tooltip />
          <el-table-column label="单位" prop="unit" width="55" align="center" />
          <el-table-column label="账面数" prop="bookNum" width="90" align="right" />
          <el-table-column label="实盘数" width="110" align="center">
            <template #default="s">
              <el-input-number v-if="detail.checkStatus === 0" v-model="s.row.realNum" :min="0" :precision="3"
                :controls="false" size="small" style="width:100%" placeholder="录入" />
              <span v-else>{{ s.row.realNum ?? '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="差异" width="90" align="right">
            <template #default="s">
              <span :style="{ color: Number(s.row.diffNum) === 0 ? '#909399' : (Number(s.row.diffNum) > 0 ? '#67c23a' : '#f56c6c') }">
                {{ s.row.realNum != null ? fmt(s.row.diffNum) : '—' }}
              </span>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="detail.checkStatus === 0" style="margin-top:12px;text-align:right">
          <el-button size="small" type="primary" @click="saveItems">保存实盘</el-button>
          <el-button size="small" type="success" :loading="confirming" @click="doConfirm">过账</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { getCheckPage, createCheck, getCheckDetail, saveCheckItems, confirmCheck } from '@/api/stock/material'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = reactive({ pageNum: 1, pageSize: 10 })
const search = reactive({ keyword: '', status: undefined as number | undefined })

const loadTable = async () => {
  loading.value = true
  try {
    const res = await getCheckPage({ ...page, ...search })
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } catch (e) { console.error(e) } finally { loading.value = false }
}
loadTable()
const onSearch = () => { page.pageNum = 1; loadTable() }
const onReset = () => { search.keyword = ''; search.status = undefined; page.pageNum = 1; loadTable() }
const fmt = (v: any) => (Number(v) || 0).toFixed(3)

const onCreate = async () => {
  const res = await createCheck()
  if (res.code === 200) {
    ElMessage.success('盘点单已创建，请录入实盘数')
    loadTable()
    openDetail({ checkId: res.data })
  }
}

const detailVisible = ref(false)
const detail = reactive<any>({ items: [] })
const openDetail = async (row: any) => {
  const res = await getCheckDetail(row.checkId)
  if (res.code === 200) {
    Object.assign(detail, res.data, { items: res.data.items || [] })
    detailVisible.value = true
  }
}

const saveItems = async () => {
  const items = detail.items.filter((it: any) => it.realNum != null)
  if (!items.length) { ElMessage.warning('请至少录入一项实盘数'); return }
  const res = await saveCheckItems(detail.checkId, items.map((it: any) => ({ materialId: it.materialId, realNum: it.realNum })))
  if (res.code === 200) { ElMessage.success('实盘已保存'); openDetail({ checkId: detail.checkId }) }
}

const confirming = ref(false)
const doConfirm = async () => {
  const missing = detail.items.filter((it: any) => it.realNum == null)
  if (missing.length) { ElMessage.warning(`还有 ${missing.length} 项未录入实盘数`); return }
  ElMessageBox.confirm('过账后将按实盘差异调整库存并生成盘点流水，确定过账？', '过账确认', { type: 'warning' }).then(async () => {
    confirming.value = true
    try {
      const res = await confirmCheck(detail.checkId)
      if (res.code === 200) { ElMessage.success('过账成功'); loadTable(); openDetail({ checkId: detail.checkId }) }
    } finally { confirming.value = false }
  }).catch(() => {})
}
</script>

<style scoped>
.stock-page { padding: 12px; }
.search-card :deep(.el-card__body) { padding: 12px 12px 0; }
.table-card { margin-top: 10px; }
.table-card :deep(.el-card__body) { padding: 12px; }
.pager { margin-top: 10px; display: flex; justify-content: flex-end; }
</style>
