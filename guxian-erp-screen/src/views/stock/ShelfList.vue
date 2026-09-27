<template>
  <div class="stock-page">
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-head">
          <span class="block-title">库位货架管理</span>
          <el-button size="small" type="primary" @click="openAdd">新增货架</el-button>
        </div>
      </template>
      <el-table :data="tableData" border stripe size="small">
        <el-table-column label="货架编码" prop="shelfCode" width="110" />
        <el-table-column label="货架名称" prop="shelfName" min-width="150" />
        <el-table-column label="排序" prop="sort" width="70" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="s">
            <el-tag :type="s.row.status === 1 ? 'success' : 'info'" size="small">{{ s.row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="s">
            <el-button size="small" type="primary" link @click="openEdit(s.row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="onDelete(s.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.shelfId ? '编辑货架' : '新增货架'" width="420px" destroy-on-close>
      <el-form :model="form" label-width="80px" size="small">
        <el-form-item label="货架名称" required>
          <el-input v-model="form.shelfName" />
        </el-form-item>
        <el-form-item label="货架编码">
          <el-input v-model="form.shelfCode" placeholder="如 05" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="1" :controls="false" style="width:100%" />
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
import { getShelfList, saveShelf, delShelf } from '@/api/stock/material'
import type { Shelf } from '@/types/production'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref<Shelf[]>([])
const loadTable = async () => {
  const res = await getShelfList()
  if (res.code === 200) tableData.value = res.data
}
loadTable()

const dialogVisible = ref(false)
const form = ref<Shelf>({ shelfCode: '', shelfName: '', status: 1, sort: 1 })
const openAdd = () => { form.value = { shelfCode: '', shelfName: '', status: 1, sort: 1 }; dialogVisible.value = true }
const openEdit = (row: Shelf) => { form.value = { ...row }; dialogVisible.value = true }
const submitForm = async () => {
  if (!form.value.shelfName) { ElMessage.warning('请填写货架名称'); return }
  const res = await saveShelf(form.value)
  if (res.code === 200) { ElMessage.success('保存成功'); dialogVisible.value = false; loadTable() }
}
const onDelete = (row: Shelf) => {
  ElMessageBox.confirm(`确定删除货架「${row.shelfName}」？`, '提示', { type: 'warning' }).then(async () => {
    const res = await delShelf(row.shelfId!)
    if (res.code === 200) { ElMessage.success('删除成功'); loadTable() }
  }).catch(() => {})
}
</script>

<style scoped>
.stock-page { padding: 12px; }
.table-card :deep(.el-card__body) { padding: 12px; }
.card-head { display: flex; justify-content: space-between; align-items: center; }
.block-title { font-weight: bold; font-size: 14px; }
</style>
