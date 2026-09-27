<template>
  <div class="customer-page">
    <!-- 搜索区 -->
    <el-card class="search-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" @keyup.enter="loadTable"></el-input>
        </el-col>
        <el-col :span="6">
          <el-input v-model="searchForm.phone" placeholder="请输入联系电话" @keyup.enter="loadTable"></el-input>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadTable">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-col>
        <el-col :span="8" style="text-align:right">
          <el-button type="primary" @click="openDialog()">新增客户</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 表格 -->
    <el-card style="margin-top:16px">
      <el-table :data="tableData" border stripe ref="tableRef"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column label="客户ID" prop="customerId" width="80"></el-table-column>
        <el-table-column label="客户名称" prop="customerName"></el-table-column>
        <el-table-column label="联系人" prop="contact"></el-table-column>
        <el-table-column label="联系电话" prop="phone"></el-table-column>
        <el-table-column label="地址" prop="address" show-overflow-tooltip></el-table-column>
        <el-table-column label="备注" prop="remark" show-overflow-tooltip></el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180"></el-table-column>
        <el-table-column label="创建时间" prop="" width="180"></el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="scope">
            <el-button size="small" type="primary" @click="openDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="success" @click="goFollow(scope.row)">跟进</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top:12px;display:flex;align-items:center;gap:8px;flex-wrap:wrap">
        <el-checkbox v-model="crossPageSelect">跨页全选</el-checkbox>
        <span>已勾选 {{ selectedCount }} 条</span>
        <el-button @click="clearSelect">清除</el-button>
        <div style="flex:1"></div>
        <el-pagination
          v-model:current-page="page.pageNum"
          v-model:page-size="page.pageSize"
          :total="total"
          @change="loadTable"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-card>

    <!-- 新增编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="客户信息">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <!-- 第1行：客户名称 | 联系人 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户名称" prop="customerName">
              <el-input v-model="form.customerName" placeholder="请输入客户名称"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人" prop="contact">
              <el-input v-model="form.contact" placeholder="请输入联系人"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第2行：联系电话 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第3行：地址（整行） -->
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入地址"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第4行：备注（整行） -->
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { getCustomerPage, saveCustomer, delCustomer } from '@/api/customer/customer'
import type { TCustomerDTO } from '@/types/customer'
import { ElMessage, ElMessageBox, type ElTable } from 'element-plus'

const tableRef = ref<ElTable>()

const selectedRows = ref<TCustomerDTO[]>([])
const selectedCount = ref(0)
const crossPageSelect = ref(false)

const handleSelectionChange = (rows: TCustomerDTO[]) => {
  selectedRows.value = rows
  selectedCount.value = rows.length
}
const clearSelect = () => {
  tableRef.value?.clearSelection()
  selectedRows.value = []
  selectedCount.value = 0
  crossPageSelect.value = false
}

// 表格
const tableData = ref<TCustomerDTO[]>([])
const total = ref(0)
const page = reactive({
  pageNum: 1,
  pageSize: 10
})
const searchForm = reactive({
  customerName: '',
  phone: ''
})

// 弹窗
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  customerId: null as number | null,
  customerName: '',
  contact: '',
  phone: '',
  address: '',
  remark: ''
})
const rules = {
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }]
}

// 加载表格
const loadTable = async () => {
  try {
    const res = await getCustomerPage({ ...page, ...searchForm })
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (err) {
    console.error('加载客户列表失败：', err)
  }
}
loadTable()

// 重置搜索
const resetSearch = () => {
  searchForm.customerName = ''
  searchForm.phone = ''
  page.pageNum = 1
  loadTable()
}

// 打开弹窗（row 有值为编辑，无值为新增）
const openDialog = (row?: TCustomerDTO) => {
  formRef.value?.clearValidate()
  if (row) {
    // 编辑：回填该行数据
    isEdit.value = true
    form.customerId = row.customerId ?? null
    form.customerName = row.customerName
    form.contact = row.contact
    form.phone = row.phone
    form.address = row.address
    form.remark = row.remark
  } else {
    // 新增：清空表单
    isEdit.value = false
    form.customerId = null
    form.customerName = ''
    form.contact = ''
    form.phone = ''
    form.address = ''
    form.remark = ''
  }
  dialogVisible.value = true
}

// 提交保存
const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    await saveCustomer(form)
    dialogVisible.value = false
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    loadTable()
  } catch (err) {
    console.error('保存客户失败：', err)
  }
}

// 删除
// 跳转客户跟进记录
const router = useRouter()
const goFollow = (row: TCustomerDTO) => {
  router.push({ path: '/customer/follow', query: { customerId: row.customerId, customerName: row.customerName } })
}

const handleDelete = (row: TCustomerDTO) => {
  ElMessageBox.confirm('确定删除该客户？', '提示', { type: 'warning' })
    .then(async () => {
      try {
        await delCustomer(row.customerId!)
        ElMessage.success('删除成功')
        loadTable()
      } catch (err) {
        console.error('删除客户失败：', err)
      }
    })
    .catch(() => {})
}
</script>

<style scoped>
.customer-page {
  padding: 16px;
}
</style>
