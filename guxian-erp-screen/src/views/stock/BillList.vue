<template>
  <div class="stock-page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="search" size="small">
        <el-form-item label="单据类型">
          <el-select v-model="search.bizType" placeholder="全部" clearable style="width:120px">
            <el-option label="采购入库" :value="2" />
            <el-option label="退货出库" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="单据号">
          <el-input v-model="search.relateNo" placeholder="如 IN20260922001" clearable style="width:190px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
          <el-button type="primary" @click="openCreate">新建单据</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column label="单据号" prop="relateNo" width="160" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="s">
            <el-tag :type="s.row.bizType === 2 ? 'success' : 'warning'" size="small">
              {{ s.row.bizType === 2 ? '采购入库' : '退货出库' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="物料编码" prop="materialCode" width="100" />
        <el-table-column label="物料名称" prop="materialName" min-width="130" show-overflow-tooltip />
        <el-table-column label="单位" prop="unit" width="60" align="center" />
        <el-table-column label="入库数量" width="100" align="right">
          <template #default="s"><span v-if="s.row.inNum > 0" style="color:#67c23a">{{ s.row.inNum }}</span></template>
        </el-table-column>
        <el-table-column label="出库数量" width="100" align="right">
          <template #default="s"><span v-if="s.row.outNum > 0" style="color:#f56c6c">{{ s.row.outNum }}</span></template>
        </el-table-column>
        <el-table-column label="结存" prop="afterNum" width="90" align="right" />
        <el-table-column label="备注" prop="remark" min-width="110" show-overflow-tooltip />
        <el-table-column label="制单时间" prop="createTime" width="160" />
      </el-table>
      <div class="pager">
        <el-pagination v-model:current-page="page.pageNum" v-model:page-size="page.pageSize" :total="total"
          small layout="total, sizes, prev, pager, next" @change="loadTable" />
      </div>
    </el-card>

    <!-- 新建单据 -->
    <el-dialog v-model="dialogVisible" title="新建出入库单据" width="680px" destroy-on-close>
      <el-form label-width="80px" size="small">
        <el-form-item label="单据类型" required>
          <el-radio-group v-model="bill.bizType">
            <el-radio :value="2">采购入库</el-radio>
            <el-radio :value="5">退货出库</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="单据备注">
          <el-input v-model="bill.remark" placeholder="整单备注" />
        </el-form-item>
        <el-form-item label="物料明细" required>
          <div style="width:100%">
            <div v-for="(row, idx) in bill.items" :key="idx" style="display:flex;gap:8px;margin-bottom:8px;align-items:center;">
              <el-select v-model="row.materialId" filterable placeholder="选择物料" style="flex:1" @change="(v: any) => onPickMaterial(idx, v)">
                <el-option v-for="m in materials" :key="m.id" :label="`${m.materialCode} ${m.materialName}（库存${m.stockNum}${m.unit}）`" :value="m.id" />
              </el-select>
              <el-input-number v-model="row.num" :min="0.001" :precision="3" :controls="false" style="width:130px" placeholder="数量" />
              <el-button size="small" type="danger" link @click="bill.items.splice(idx, 1)">删除</el-button>
            </div>
            <el-button size="small" type="primary" link @click="addRow">+ 添加物料</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="dialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" :loading="submitting" @click="submitBill">保存并过账</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { getBillPage, createBill, listEnabledMaterial } from '@/api/stock/material'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = reactive({ pageNum: 1, pageSize: 10 })
const search = reactive({ bizType: undefined as number | undefined, relateNo: '' })

const loadTable = async () => {
  loading.value = true
  try {
    const res = await getBillPage({ ...page, ...search })
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } catch (e) { console.error(e) } finally { loading.value = false }
}
loadTable()
const onSearch = () => { page.pageNum = 1; loadTable() }
const onReset = () => { search.bizType = undefined; search.relateNo = ''; page.pageNum = 1; loadTable() }

// 新建单据
const dialogVisible = ref(false)
const submitting = ref(false)
const materials = ref<any[]>([])
const bill = reactive<any>({ bizType: 2, remark: '', items: [] })
const addRow = () => bill.items.push({ materialId: undefined, num: 1, remark: '' })
const onPickMaterial = (_idx: number, _v: any) => {}

const openCreate = async () => {
  bill.bizType = 2
  bill.remark = ''
  bill.items = []
  addRow()
  if (!materials.value.length) {
    const res = await listEnabledMaterial()
    if (res.code === 200) materials.value = res.data
  }
  dialogVisible.value = true
}
const submitBill = async () => {
  if (bill.items.some((r: any) => !r.materialId)) { ElMessage.warning('请选择物料'); return }
  if (bill.items.some((r: any) => !r.num || r.num <= 0)) { ElMessage.warning('数量必须大于 0'); return }
  submitting.value = true
  try {
    const res = await createBill({ bizType: bill.bizType, remark: bill.remark, items: bill.items })
    if (res.code === 200) {
      ElMessage.success(`单据 ${res.data} 已过账`)
      dialogVisible.value = false
      search.relateNo = res.data
      loadTable()
    }
  } catch (e) { console.error(e) } finally { submitting.value = false }
}
</script>

<style scoped>
.stock-page { padding: 12px; }
.search-card :deep(.el-card__body) { padding: 12px 12px 0; }
.table-card { margin-top: 10px; }
.table-card :deep(.el-card__body) { padding: 12px; }
.pager { margin-top: 10px; display: flex; justify-content: flex-end; }
</style>
