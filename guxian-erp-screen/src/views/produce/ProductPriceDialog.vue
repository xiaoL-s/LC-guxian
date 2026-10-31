<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    :title="`产品计件价 —— ${process?.processName || ''}（产品来自字典管理，共 ${tableData.length} 个）`"
    width="780px"
    top="5vh"
    :close-on-click-modal="false"
    destroy-on-close
    @open="onOpen"
  >
    <div class="dialog-toolbar">
      <div class="toolbar-left">
        <el-button type="success" size="small" @click="triggerImport">📥 导入Excel</el-button>
        <el-button size="small" @click="onExport">📤 导出当前工序</el-button>
      </div>
      <el-input v-model="kw" size="small" placeholder="筛选产品名" clearable style="width:180px" />
      <input ref="fileRef" type="file" accept=".xlsx,.xls" hidden @change="onFile" />
    </div>

    <el-table :data="filteredData" border stripe size="small" v-loading="loading" max-height="60vh">
      <el-table-column type="index" label="#" width="55" align="center" />
      <el-table-column label="产品名称（字典管理）" min-width="260">
        <template #default="s">
          <span>{{ s.row.productName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="计件单价(元/件)" width="180">
        <template #default="s">
          <el-input-number v-model="s.row.price" :min="0" :precision="2" :step="0.5" size="small" controls-position="right" style="width:150px" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="s">
          <el-button type="primary" link size="small" @click="saveRow(s.row)">保存</el-button>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <el-button size="small" @click="$emit('update:visible', false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPricePage, savePrice, importPriceByProcess, exportPriceExcel } from '@/api/production/price'

const props = defineProps<{ visible: boolean; process: any }>()
const emit = defineEmits(['update:visible'])

const loading = ref(false)
const tableData = ref<any[]>([])
const kw = ref('')
const fileRef = ref<HTMLInputElement>()

const filteredData = computed(() => {
  if (!kw.value) return tableData.value
  return tableData.value.filter((r: any) => (r.productName || '').includes(kw.value))
})

const onOpen = () => loadData()
const loadData = async () => {
  if (!props.process?.processId) return
  loading.value = true
  try {
    // 后端以字典全部产品为基准左拼该工序已配置价
    const res: any = await getPricePage({ pageNum: 1, pageSize: 10000, processId: props.process.processId })
    tableData.value = (res.data?.records || []).map((r: any) => ({ ...r }))
  } finally {
    loading.value = false
  }
}

const saveRow = async (row: any) => {
  // id 为空表示该产品此工序尚未配置过，savePrice 会自动按 工序+产品名 upsert
  await savePrice({ id: row.id, processId: props.process.processId, productName: row.productName, price: row.price })
  ElMessage.success(`已保存：${row.productName} = ${row.price} 元/件`)
  loadData()
}

const triggerImport = () => fileRef.value?.click()
const onFile = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  try {
    await ElMessageBox.confirm(`导入「${file.name}」到工序「${props.process.processName}」，同名产品将覆盖更新`, '导入确认', { type: 'info' })
    const res: any = await importPriceByProcess(props.process.processId, file)
    ElMessage.success(res.data || '导入完成')
    loadData()
  } catch (err: any) {
    if (err !== 'cancel') ElMessage.error('导入失败')
  } finally {
    input.value = ''
  }
}

const onExport = async () => {
  const res: any = await exportPriceExcel({ processId: props.process.processId })
  // 响应拦截器已返回 body（即 Blob 本身），不要再取 .data
  const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `${props.process.processName}-产品计件价.xlsx`
  a.click()
  URL.revokeObjectURL(a.href)
}
</script>

<style scoped>
.dialog-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; gap: 10px; }
.toolbar-left { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
</style>
