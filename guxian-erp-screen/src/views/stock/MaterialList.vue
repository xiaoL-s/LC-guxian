<template>
  <div class="stock-page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="search" size="small">
        <el-form-item label="关键字">
          <el-input v-model="search.keyword" placeholder="编码/名称/规格" clearable style="width:180px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="search.materialType" placeholder="全部" clearable style="width:120px">
            <el-option v-for="t in MATERIAL_TYPES" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
          <el-button type="primary" @click="openAdd">新增物料</el-button>
          <el-button @click="recordVisible = true">库存流水</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column label="编码" prop="materialCode" width="90" />
        <el-table-column label="物料名称" prop="materialName" min-width="130" show-overflow-tooltip />
        <el-table-column label="类型" width="80" align="center">
          <template #default="s">{{ typeLabel(s.row.materialType) }}</template>
        </el-table-column>
        <el-table-column label="规格" prop="spec" min-width="130" show-overflow-tooltip />
        <el-table-column label="单位" prop="unit" width="60" align="center" />
        <el-table-column label="当前库存" width="100" align="right">
          <template #default="s">
            <el-tag :type="s.row.stockNum < s.row.warnNum ? 'danger' : 'success'" size="small">
              {{ fmt(s.row.stockNum) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预警值" prop="warnNum" width="80" align="right" />
        <el-table-column label="状态" width="70" align="center">
          <template #default="s">
            <el-tag :type="s.row.enable === 1 ? 'success' : 'info'" size="small">{{ s.row.enable === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="s">
            <el-button size="small" type="primary" link @click="openInStock(s.row)">入库</el-button>
            <el-button size="small" type="warning" link @click="openOutStock(s.row)">出库</el-button>
            <el-button size="small" type="primary" link @click="openEdit(s.row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="onDelete(s.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination v-model:current-page="page.pageNum" v-model:page-size="page.pageSize" :total="total"
          small layout="total, sizes, prev, pager, next" @change="loadTable" />
      </div>
    </el-card>

    <!-- 新增/编辑物料 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑物料' : '新增物料'" width="520px" destroy-on-close>
      <el-form :model="form" label-width="90px" size="small">
        <el-form-item label="物料编码" required>
          <el-input v-model="form.materialCode" placeholder="如 M010" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="物料名称" required>
          <el-input v-model="form.materialName" />
        </el-form-item>
        <el-form-item label="物料类型" required>
          <el-select v-model="form.materialType" style="width:100%">
            <el-option v-for="t in MATERIAL_TYPES" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="form.spec" placeholder="如 6米/支 壁厚1.0" />
        </el-form-item>
        <el-form-item label="单位" required>
          <el-select v-model="form.unit" style="width:100%" allow-create filterable>
            <el-option v-for="u in ['米','㎡','个','套','根','公斤']" :key="u" :label="u" :value="u" />
          </el-select>
        </el-form-item>
        <el-form-item label="预警库存">
          <el-input-number v-model="form.warnNum" :min="0" :precision="3" :controls="false" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enable" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="dialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 出入库 -->
    <el-dialog v-model="stockDialog.visible" :title="stockDialog.type === 1 ? '物料入库' : '物料出库'" width="460px" destroy-on-close>
      <el-form :model="stockDialog" label-width="90px" size="small">
        <el-form-item label="物料">
          <el-input :model-value="stockDialog.materialName" disabled />
        </el-form-item>
        <el-form-item label="当前库存">
          <el-input :model-value="fmt(stockDialog.stockNum) + ' ' + stockDialog.unit" disabled />
        </el-form-item>
        <el-form-item label="变动数量" required>
          <el-input-number v-model="stockDialog.changeNum" :min="0.001" :precision="3" :controls="false" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="stockDialog.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="stockDialog.visible = false">取消</el-button>
        <el-button size="small" type="primary" @click="submitStock">确认{{ stockDialog.type === 1 ? '入库' : '出库' }}</el-button>
      </template>
    </el-dialog>

    <!-- 库存流水 -->
    <el-dialog v-model="recordVisible" title="库存流水" width="820px">
      <el-form :inline="true" size="small" style="margin-bottom:8px">
        <el-form-item label="类型">
          <el-select v-model="recordQuery.bizType" placeholder="全部" clearable style="width:120px">
            <el-option v-for="b in STOCK_BIZ_TYPES" :key="b.value" :label="b.label" :value="b.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRecord">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="recordData" v-loading="recordLoading" border stripe size="small" max-height="420">
        <el-table-column label="时间" prop="createTime" width="160" />
        <el-table-column label="物料" prop="materialName" min-width="120" show-overflow-tooltip />
        <el-table-column label="类型" width="90" align="center">
          <template #default="s">{{ bizLabel(s.row.bizType) }}</template>
        </el-table-column>
        <el-table-column label="入库" width="90" align="right">
          <template #default="s"><span v-if="s.row.inNum > 0" style="color:#67c23a">{{ s.row.inNum }}</span></template>
        </el-table-column>
        <el-table-column label="出库" width="90" align="right">
          <template #default="s"><span v-if="s.row.outNum > 0" style="color:#f56c6c">{{ s.row.outNum }}</span></template>
        </el-table-column>
        <el-table-column label="结存" prop="afterNum" width="90" align="right" />
        <el-table-column label="关联单号" prop="relateNo" width="130" />
        <el-table-column label="备注" prop="remark" min-width="100" show-overflow-tooltip />
      </el-table>
      <div class="pager">
        <el-pagination v-model:current-page="recordQuery.pageNum" v-model:page-size="recordQuery.pageSize"
          :total="recordTotal" small layout="total, prev, pager, next" @change="loadRecord" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getMaterialPage, saveMaterial, delMaterial, changeStock, getStockRecordPage } from '@/api/stock/material'
import { MATERIAL_TYPES, STOCK_BIZ_TYPES, type Material } from '@/types/production'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref<Material[]>([])
const total = ref(0)
const page = reactive({ pageNum: 1, pageSize: 10 })
const search = reactive({ keyword: '', materialType: undefined as number | undefined })

const loadTable = async () => {
  loading.value = true
  try {
    const res = await getMaterialPage({ ...page, ...search })
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } catch (e) { console.error(e) } finally { loading.value = false }
}
loadTable()
const onSearch = () => { page.pageNum = 1; loadTable() }
const onReset = () => { search.keyword = ''; search.materialType = undefined; page.pageNum = 1; loadTable() }
const fmt = (v: any) => (Number(v) || 0).toFixed(3).replace(/\.?0+$/, '')
const typeLabel = (t: number) => MATERIAL_TYPES.find(x => x.value === t)?.label || t
const bizLabel = (t: number) => STOCK_BIZ_TYPES.find(x => x.value === t)?.label || t

// 新增/编辑
const dialogVisible = ref(false)
const form = ref<Material>({ materialCode: '', materialName: '', materialType: 1, spec: '', unit: '米', stockNum: 0, warnNum: 0, enable: 1 })
const openAdd = () => {
  form.value = { materialCode: '', materialName: '', materialType: 1, spec: '', unit: '米', stockNum: 0, warnNum: 0, enable: 1 }
  dialogVisible.value = true
}
const openEdit = (row: Material) => { form.value = { ...row }; dialogVisible.value = true }
const submitForm = async () => {
  if (!form.value.materialCode || !form.value.materialName) { ElMessage.warning('请填写编码与名称'); return }
  const res = await saveMaterial(form.value)
  if (res.code === 200) { ElMessage.success('保存成功'); dialogVisible.value = false; loadTable() }
}
const onDelete = (row: Material) => {
  ElMessageBox.confirm(`确定删除物料「${row.materialName}」？`, '提示', { type: 'warning' }).then(async () => {
    const res = await delMaterial(row.id!)
    if (res.code === 200) { ElMessage.success('删除成功'); loadTable() }
  }).catch(() => {})
}

// 出入库
const stockDialog = reactive<any>({ visible: false, type: 1, materialId: null, materialName: '', stockNum: 0, unit: '', changeNum: 1, remark: '' })
const openInStock = (row: Material) => {
  Object.assign(stockDialog, { visible: true, type: 1, materialId: row.id, materialName: row.materialName, stockNum: row.stockNum, unit: row.unit, changeNum: 1, remark: '手动入库' })
}
const openOutStock = (row: Material) => {
  Object.assign(stockDialog, { visible: true, type: 2, materialId: row.id, materialName: row.materialName, stockNum: row.stockNum, unit: row.unit, changeNum: 1, remark: '手动出库' })
}
const submitStock = async () => {
  if (!stockDialog.changeNum || stockDialog.changeNum <= 0) { ElMessage.warning('请输入变动数量'); return }
  const res = await changeStock({
    materialId: stockDialog.materialId,
    changeNum: stockDialog.type === 1 ? stockDialog.changeNum : -stockDialog.changeNum,
    bizType: stockDialog.type === 1 ? 2 : 4,
    remark: stockDialog.remark
  })
  if (res.code === 200) { ElMessage.success('操作成功'); stockDialog.visible = false; loadTable() }
}

// 流水
const recordVisible = ref(false)
const recordLoading = ref(false)
const recordData = ref<any[]>([])
const recordTotal = ref(0)
const recordQuery = reactive({ pageNum: 1, pageSize: 10, bizType: undefined as number | undefined })
const loadRecord = async () => {
  recordLoading.value = true
  try {
    const res = await getStockRecordPage(recordQuery)
    if (res.code === 200) { recordData.value = res.data.records; recordTotal.value = res.data.total }
  } finally { recordLoading.value = false }
}
onMounted(() => {})
</script>

<style scoped>
.stock-page { padding: 12px; }
.search-card :deep(.el-card__body) { padding: 12px 12px 0; }
.table-card { margin-top: 10px; }
.table-card :deep(.el-card__body) { padding: 12px; }
.pager { margin-top: 10px; display: flex; justify-content: flex-end; }
</style>
