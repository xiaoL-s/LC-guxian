<template>
  <div class="produce-page">
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-head">
          <span class="block-title">工序管理</span>
          <el-button size="small" type="primary" @click="openAdd">新增工序</el-button>
        </div>
      </template>
      <el-table :data="tableData" border stripe size="small" style="width:100%">
        <!-- 选择框 -->
        <el-table-column type="selection" width="45" align="center" />
        <!-- 产品价格：点击弹出计件价弹窗 -->
        <el-table-column label="产品价格" width="100" align="center">
          <template #default="s">
            <el-button type="primary" link size="small" @click.stop="openPriceDialog(s.row)">产品价格</el-button>
          </template>
        </el-table-column>
        <!-- 名称 -->
        <el-table-column label="名称" prop="processName" min-width="140" />
        <!-- 计件方式（固定按板件数） -->
        <el-table-column label="计件方式" width="100" align="center">
          <template #default>按板件数</template>
        </el-table-column>
        <!-- 工资（固定0，后续按计件汇总） -->
        <el-table-column label="工资" width="80" align="center">
          <template #default>0</template>
        </el-table-column>
        <!-- 顺序 -->
        <el-table-column label="顺序" prop="processSort" width="70" align="center" />
        <!-- 创建时间 -->
        <el-table-column label="创建时间" prop="createTime" width="180" />
        <!-- 计件单价更新时间 -->
        <el-table-column label="更新时间" width="180" align="center">
          <template #default="s">{{ s.row.latestPriceUpdateTime || '—' }}</template>
        </el-table-column>
        <!-- 订单流程：根据工序名映射 -->
        <el-table-column label="订单流程" min-width="120" align="center">
          <template #default="s">{{ stepText(s.row) }}</template>
        </el-table-column>
        <!-- 删除 -->
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="s">
            <el-button type="primary" link size="small" @click.stop="openEdit(s.row)">编辑</el-button>
            <el-button type="danger" link size="small" @click.stop="onDelete(s.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.processId ? '编辑工序' : '新增工序'" width="480px" destroy-on-close>
      <el-form :model="form" label-width="90px" size="small">
        <el-form-item label="工序名称" required>
          <el-input v-model="form.processName" />
        </el-form-item>
        <el-form-item label="工序编码" required>
          <el-input v-model="form.processCode" placeholder="如 CUT" :disabled="!!form.processId" />
        </el-form-item>
        <el-form-item label="生产排序" required>
          <el-input-number v-model="form.processSort" :min="1" :controls="false" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="dialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 产品计件价弹窗 -->
    <ProductPriceDialog v-model:visible="priceDialogVisible" :process="currentProcess" />
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'ProcessList' })
import { ref } from 'vue'
import { getProcessList, saveProcess, delProcess } from '@/api/production/workorder'
import type { ProcessDict } from '@/types/production'
import { ElMessage, ElMessageBox } from 'element-plus'
import ProductPriceDialog from './ProductPriceDialog.vue'

// 产品价弹窗
const priceDialogVisible = ref(false)
const currentProcess = ref<any>({})
const openPriceDialog = (row: ProcessDict) => {
  currentProcess.value = row
  priceDialogVisible.value = true
}

// 订单流程文字映射：根据工序名显示对应状态
const stepText = (row: ProcessDict): string => {
  const name = row.processName || ''
  if (name.includes('下料')) return '已下料'
  if (name.includes('剪网') || name.includes('压网')) return '已剪网'
  if (name.includes('组装')) return '已组装'
  if (name.includes('入库')) return '已入库'
  if (name.includes('发货')) return '已发货'
  if (name.includes('焊接')) return '已焊接'
  if (name.includes('喷涂')) return '已喷涂'
  if (name.includes('开孔') || name.includes('开锁孔')) return '已开孔'
  if (name.includes('打磨')) return '已打磨'
  if (name.includes('配件')) return '已配件'
  if (name.includes('中柱')) return '已完成'
  if (name.includes('拼接')) return '已拼接'
  if (name.includes('拼框')) return '已拼框'
  if (name.includes('压框')) return '已压框'
  if (name.includes('质检')) return '已质检'
  return name
}

const tableData = ref<ProcessDict[]>([])
const loadTable = async () => {
  const res = await getProcessList()
  if (res.code === 200) tableData.value = res.data
}
// loadTable() // 初始不自动加载，点查询才出数据
const dialogVisible = ref(false)
const form = ref<ProcessDict>({ processCode: '', processName: '', processSort: 1 })
const openAdd = () => {
  form.value = { processCode: '', processName: '', processSort: 1 }
  dialogVisible.value = true
}
const openEdit = (row: ProcessDict) => {
  form.value = { ...row }
  dialogVisible.value = true
}
const submitForm = async () => {
  if (!form.value.processName || !form.value.processCode) { ElMessage.warning('请填写工序名称与编码'); return }
  const res = await saveProcess(form.value)
  if (res.code === 200) { ElMessage.success('保存成功'); dialogVisible.value = false; loadTable() }
}
const onDelete = (row: ProcessDict) => {
  ElMessageBox.confirm(`确定删除工序「${row.processName}」？`, '提示', { type: 'warning' }).then(async () => {
    const res = await delProcess(row.processId!)
    if (res.code === 200) { ElMessage.success('删除成功'); loadTable() }
  }).catch(() => {})
}
</script>

<style scoped>
.produce-page { padding: 12px; }
.table-card :deep(.el-card__body) { padding: 12px; }
.card-head { display: flex; justify-content: space-between; align-items: center; }
.block-title { font-weight: bold; font-size: 14px; }
</style>
