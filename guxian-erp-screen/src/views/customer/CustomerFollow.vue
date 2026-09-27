<template>
  <div class="customer-follow-page">
    <!-- 头部 -->
    <el-card class="search-card">
      <el-row :gutter="20">
        <el-col :span="16">
          <div class="page-title">
            客户跟进记录
            <el-select v-model="customerId" filterable clearable placeholder="全部客户" style="width: 220px"
              @change="onCustomerChange">
              <el-option v-for="c in customers" :key="c.customerId" :label="c.customerName" :value="c.customerId" />
            </el-select>
            <span class="sub" v-if="customerName">当前：{{ customerName }}</span>
          </div>
        </el-col>
        <el-col :span="8" style="text-align:right">
          <el-button type="primary" @click="openAdd()">新增跟进记录</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 表格 -->
    <el-card style="margin-top:16px">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column label="客户" prop="customerName" width="160" show-overflow-tooltip>
          <template #default="scope">{{ customerNameOf(scope.row.customerId) }}</template>
        </el-table-column>
        <el-table-column label="跟进时间" prop="followTime" width="180"></el-table-column>
        <el-table-column label="跟进人" prop="followUser" width="120"></el-table-column>
        <el-table-column label="跟进内容" prop="followContent" show-overflow-tooltip></el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180"></el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button size="small" type="primary" @click="openEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top:12px;display:flex;justify-content:flex-end">
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
    <el-dialog v-model="dialogVisible" title="跟进记录" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="跟进时间" prop="followTime">
          <el-date-picker v-model="form.followTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择跟进时间" style="width:100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="跟进人" prop="followUser">
          <el-input v-model="form.followUser" placeholder="请输入跟进人"></el-input>
        </el-form-item>
        <el-form-item label="跟进内容" prop="followContent">
          <el-input v-model="form.followContent" type="textarea" :rows="4" placeholder="请输入跟进内容"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getFollowPage, saveFollow, delFollow } from '@/api/customer/follow'
import { getAllCustomerList } from '@/api/customer/customer'
import type { TCustomerFollowDTO } from '@/types/customer'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

const route = useRoute()
// 从客户档案列表跳转携带 customerId 与 customerName；也可在页面内直接切换客户
const customerId = ref<number | undefined>(Number(route.query.customerId) || undefined)
const customerName = ref<string>((route.query.customerName as string) || '')
const customers = ref<any[]>([])

const customerNameOf = (cid?: number) => {
  if (!cid) return '—'
  const c = customers.value.find(x => x.customerId === cid)
  return c ? c.customerName : `ID ${cid}`
}

const onCustomerChange = (cid?: number) => {
  customerName.value = cid ? (customers.value.find(c => c.customerId === cid)?.customerName || '') : ''
  page.pageNum = 1
  loadTable()
}

const loading = ref(false)
const tableData = ref<TCustomerFollowDTO[]>([])
const total = ref(0)
const page = reactive({
  pageNum: 1,
  pageSize: 10
})

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<TCustomerFollowDTO>({
  id: undefined,
  customerId: customerId.value,
  followContent: '',
  followTime: '',
  followUser: ''
})
const rules = {
  followTime: [{ required: true, message: '请选择跟进时间', trigger: 'change' }],
  followContent: [{ required: true, message: '请输入跟进内容', trigger: 'blur' }]
}

const loadTable = async () => {
  loading.value = true
  try {
    // 不选客户时查询全部跟进记录
    const params: any = { ...page }
    if (customerId.value) params.customerId = customerId.value
    const res = await getFollowPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (err) {
    console.error('加载跟进记录失败：', err)
  } finally {
    loading.value = false
  }
}

const openAdd = () => {
  if (!customerId.value) {
    ElMessage.warning('请先在上方选择客户')
    return
  }
  formRef.value?.clearValidate()
  form.id = undefined
  form.customerId = customerId.value
  form.followContent = ''
  form.followTime = ''
  form.followUser = ''
  dialogVisible.value = true
}

const openEdit = (row: TCustomerFollowDTO) => {
  formRef.value?.clearValidate()
  form.id = row.id
  form.customerId = row.customerId
  form.followContent = row.followContent
  form.followTime = row.followTime
  form.followUser = row.followUser
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    await saveFollow(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadTable()
  } catch (err) {
    console.error('保存跟进记录失败：', err)
  }
}

const handleDelete = (row: TCustomerFollowDTO) => {
  ElMessageBox.confirm('确定删除该跟进记录？', '提示', { type: 'warning' })
    .then(async () => {
      try {
        await delFollow(row.id!)
        ElMessage.success('删除成功')
        loadTable()
      } catch (err) {
        console.error('删除跟进记录失败：', err)
      }
    })
    .catch(() => {})
}

onMounted(async () => {
  try {
    const res: any = await getAllCustomerList()
    if (res.code === 200) customers.value = res.data || []
  } catch (e) {
    console.warn('客户列表加载失败', e)
  }
  loadTable()
})
</script>

<style scoped>
.customer-follow-page {
  padding: 16px;
}
.page-title {
  font-size: 18px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
}
.page-title .sub {
  font-size: 13px;
  font-weight: normal;
  color: #888;
}
</style>
