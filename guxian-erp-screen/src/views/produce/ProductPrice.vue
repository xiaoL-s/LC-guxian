<template>
  <div class="price-page">
    <!-- 筛选区 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="工序">
          <el-select v-model="query.processId" placeholder="全部工序" clearable style="width:200px">
            <el-option v-for="p in processList" :key="p.processId" :label="p.processName" :value="p.processId" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="query.productName" placeholder="产品名模糊搜索" clearable style="width:200px" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="openAdd">＋ 新增</el-button>
          <el-button type="success" @click="triggerImport">📥 导入Excel</el-button>
          <el-button @click="downloadTemplate">下载模板</el-button>
          <el-button type="warning" @click="doExport">📤 导出Excel</el-button>
        </el-form-item>
        <input ref="fileInputRef" type="file" accept=".xlsx,.xls" hidden @change="onImportFile" />
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column label="工序" min-width="160">
          <template #default="s">{{ processName(s.row.processId) }}</template>
        </el-table-column>
        <el-table-column prop="productName" label="产品名称" min-width="220" />
        <el-table-column label="计件单价(元/件)" width="160" align="center">
          <template #default="s">
            <span style="font-weight:600;color:#e6a23c">{{ s.row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="s">
            <el-button type="primary" link @click="openEdit(s.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(s.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top:12px;justify-content:flex-end"
        @change="loadData"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑计件价' : '新增计件价'" width="520px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="工序" required>
          <el-select v-model="form.processId" placeholder="选择工序" style="width:100%">
            <el-option v-for="p in processList" :key="p.processId" :label="p.processName" :value="p.processId" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品名称" required>
          <el-input v-model="form.productName" placeholder="如：护童-压条款（120款）" />
        </el-form-item>
        <el-form-item label="计件单价" required>
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="0.5" style="width:200px" />
          <span style="margin-left:8px;color:#909399;font-size:12px">元/件</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'ProductPrice' })
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPricePage, savePrice, delPrice, importPriceExcel, downloadPriceTemplate, exportPriceExcel } from '@/api/production/price'
import { getProcessEnabled } from '@/api/production/workorder'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const processList = ref<any[]>([])
const query = reactive({ pageNum: 1, pageSize: 50, processId: undefined as number | undefined, productName: '' })

const processName = (id: number) => processList.value.find((p: any) => p.processId === id)?.processName || `#${id}`

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getPricePage(query)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}
const loadProcess = async () => {
  const res: any = await getProcessEnabled()
  processList.value = res.data || []
}
const onSearch = () => { query.pageNum = 1; loadData() }
const onReset = () => { query.processId = undefined; query.productName = ''; query.pageNum = 1; loadData() }

// 新增/编辑
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({ id: null as number | null, processId: undefined as number | undefined, productName: '', price: 0 })
const openAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, processId: undefined, productName: '', price: 0 })
  dialogVisible.value = true
}
const openEdit = (row: any) => {
  isEdit.value = true
  Object.assign(form, { id: row.id, processId: row.processId, productName: row.productName, price: row.price })
  dialogVisible.value = true
}
const submitForm = async () => {
  if (!form.processId) { ElMessage.warning('请选择工序'); return }
  if (!form.productName) { ElMessage.warning('请输入产品名称'); return }
  await savePrice({ ...form })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定删除「${processName(row.processId)} - ${row.productName}」的计件价？`, '提示', { type: 'warning' })
    .then(async () => { await delPrice(row.id); ElMessage.success('已删除'); loadData() })
    .catch(() => {})
}

// Excel 导入导出
const fileInputRef = ref<HTMLInputElement>()
const triggerImport = () => fileInputRef.value?.click()
const onImportFile = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  try {
    ElMessageBox.confirm(`即将导入文件「${file.name}」，同一工序+产品会覆盖更新，继续？`, '导入确认', { type: 'info' })
      .then(async () => {
        const res: any = await importPriceExcel(file)
        ElMessage.success(res.data || '导入完成')
        loadData()
      })
      .catch(() => {})
  } finally {
    input.value = ''
  }
}
const downloadTemplate = async () => {
  const res: any = await downloadPriceTemplate()
  const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = '产品计件价模板.xlsx'
  a.click()
  URL.revokeObjectURL(a.href)
}
const doExport = async () => {
  const res: any = await exportPriceExcel({ processId: query.processId, productName: query.productName })
  const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = '产品计件价.xlsx'
  a.click()
  URL.revokeObjectURL(a.href)
}

onMounted(() => { loadProcess().then(() => loadData()) })
</script>

<style scoped>
.price-page { padding: 16px; }
.search-card { margin-bottom: 12px; }
</style>
