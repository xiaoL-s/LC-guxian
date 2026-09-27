<template>
  <div class="stock-page">
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-head">
          <span class="block-title">库存预警</span>
          <el-button size="small" type="primary" @click="loadTable">刷新</el-button>
        </div>
      </template>
      <el-alert title="当前库存低于预警值的物料（预警值可在「物料档案」中调整）" type="warning" :closable="false" style="margin-bottom:10px" />
      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column label="物料编码" prop="materialCode" width="110" />
        <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip />
        <el-table-column label="类型" width="90" align="center">
          <template #default="s">{{ typeLabel(s.row.materialType) }}</template>
        </el-table-column>
        <el-table-column label="当前库存" width="110" align="right">
          <template #default="s">
            <span style="color:#f56c6c;font-weight:bold">{{ fmt(s.row.stockNum) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="预警值" prop="warnNum" width="100" align="right" />
        <el-table-column label="缺口" width="110" align="right">
          <template #default="s">
            <span style="color:#e6a23c">{{ fmt(Number(s.row.warnNum) - Number(s.row.stockNum)) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单位" prop="unit" width="70" align="center" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="s">
            <el-button size="small" type="primary" link @click="openIn(s.row)">补充入库</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 补充入库 -->
    <el-dialog v-model="dialogVisible" title="补充入库" width="440px" destroy-on-close>
      <el-form label-width="90px" size="small">
        <el-form-item label="物料">
          <el-input :model-value="row.materialName" disabled />
        </el-form-item>
        <el-form-item label="当前库存">
          <el-input :model-value="fmt(row.stockNum) + ' ' + row.unit" disabled />
        </el-form-item>
        <el-form-item label="建议补足">
          <el-input :model-value="fmt(Math.max(Number(row.warnNum) - Number(row.stockNum), 0)) + ' ' + row.unit" disabled />
        </el-form-item>
        <el-form-item label="入库数量" required>
          <el-input-number v-model="inNum" :min="0.001" :precision="3" :controls="false" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="dialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="submitting" @click="submitIn">确认入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getWarnList, changeStock } from '@/api/stock/material'
import { MATERIAL_TYPES } from '@/types/production'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const loadTable = async () => {
  loading.value = true
  try {
    const res = await getWarnList()
    if (res.code === 200) tableData.value = res.data
  } finally { loading.value = false }
}
loadTable()
const fmt = (v: any) => (Number(v) || 0).toFixed(3)
const typeLabel = (t: number) => MATERIAL_TYPES.find(x => x.value === t)?.label || t

const dialogVisible = ref(false)
const submitting = ref(false)
const row = ref<any>({})
const inNum = ref(1)
const openIn = (r: any) => {
  row.value = r
  inNum.value = Number(r.warnNum) - Number(r.stockNum)
  dialogVisible.value = true
}
const submitIn = async () => {
  if (!inNum.value || inNum.value <= 0) { ElMessage.warning('请输入入库数量'); return }
  submitting.value = true
  try {
    const res = await changeStock({ materialId: row.value.id, changeNum: inNum.value, bizType: 2, remark: '预警补库' })
    if (res.code === 200) { ElMessage.success('入库成功'); dialogVisible.value = false; loadTable() }
  } finally { submitting.value = false }
}
</script>

<style scoped>
.stock-page { padding: 12px; }
.table-card :deep(.el-card__body) { padding: 12px; }
.card-head { display: flex; justify-content: space-between; align-items: center; }
.block-title { font-weight: bold; font-size: 14px; }
</style>
