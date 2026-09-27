<template>
  <div class="produce-page">
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-head">
          <span class="block-title">生产工序字典</span>
          <el-button size="small" type="primary" @click="openAdd">新增工序</el-button>
        </div>
      </template>
      <el-table :data="tableData" border stripe size="small">
        <el-table-column label="排序" prop="processSort" width="70" align="center" />
        <el-table-column label="工序编码" prop="processCode" width="120" />
        <el-table-column label="工序名称" prop="processName" min-width="140" />
        <el-table-column label="岗位类型" prop="postType" width="120" />
        <el-table-column label="计件单价(元/件)" prop="unitPrice" width="120" align="right" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="s">
            <el-tag :type="s.row.status === 1 ? 'success' : 'info'" size="small">{{ s.row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="s">
            <el-button size="small" type="primary" link @click="openEdit(s.row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="onDelete(s.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

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
        <el-form-item label="岗位类型">
          <el-input v-model="form.postType" placeholder="如 cut/assemble" />
        </el-form-item>
        <el-form-item label="计件单价">
          <el-input-number v-model="form.unitPrice" :min="0" :precision="2" :controls="false" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="dialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getProcessList, saveProcess, delProcess } from '@/api/production/workorder'
import type { ProcessDict } from '@/types/production'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref<ProcessDict[]>([])
const loadTable = async () => {
  const res = await getProcessList()
  if (res.code === 200) tableData.value = res.data
}
loadTable()

const dialogVisible = ref(false)
const form = ref<ProcessDict>({ processCode: '', processName: '', processSort: 1, postType: '', unitPrice: 0, status: 1 })
const openAdd = () => {
  form.value = { processCode: '', processName: '', processSort: 1, postType: '', unitPrice: 0, status: 1 }
  dialogVisible.value = true
}
const openEdit = (row: ProcessDict) => { form.value = { ...row }; dialogVisible.value = true }
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
