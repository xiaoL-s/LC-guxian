<template>
  <div class="user-page">
    <!-- 搜索区 -->
    <el-card class="search-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input v-model="searchForm.realName" placeholder="请输入真实姓名" @keyup.enter="loadTable"></el-input>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadTable">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-col>
        <el-col :span="14" style="text-align:right">
          <el-button type="primary" @click="openDialog()">新增员工</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 表格 -->
    <el-card style="margin-top:16px">
      <el-table :data="tableData" border stripe ref="tableRef"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column label="ID" prop="id" width="80"></el-table-column>
        <el-table-column label="用户名" prop="username"></el-table-column>
        <el-table-column label="姓名" prop="realName"></el-table-column>
        <el-table-column label="手机号" prop="phone"></el-table-column>
        <el-table-column label="职位" prop="roleNames"></el-table-column>
<el-table-column label="菜单权限" prop="menuNames" min-width="180">
  <template #default="scope">
    <el-tooltip :content="scope.row.menuNames" effect="dark" placement="top">
      <span class="text-ellipsis" style="display:inline-block;width:160px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">
        {{ scope.row.menuNames || '无' }}
      </span>
    </el-tooltip>
  </template>
</el-table-column>



        <el-table-column label="状态" prop="status">
          <template #default="scope">
            <el-tag :type="scope.row.status ===1 ? 'success':'danger'">
              {{ scope.row.status ===1 ? '启用':'禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime"></el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button size="small" type="primary" @click="openDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleResetPwd(scope.row)">重置密码</el-button>
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
    <el-dialog v-model="dialogVisible" title="员工信息">
      <el-form ref="formRef" :model="form" label-width="80px">
        <!-- 第1行：账号 | 密码 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="账号" prop="username">
              <el-input v-model="form.username" placeholder="请输入账号"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码" prop="password" v-if="!isEdit">
              <el-input v-model="form.password" placeholder="请输入密码"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第2行：姓名 | 联系方式 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入姓名"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系方式" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系方式"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第3行：职位 | 状态 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职位" prop="roleNames">
              <el-input v-model="form.roleNames" placeholder="请输入职位"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" style="width:100%">
                <el-option label="启用" :value="1"></el-option>
                <el-option label="禁用" :value="0"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第4行：分配角色（整行，多选下拉） -->
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="分配角色" prop="roleIdList">
              <el-select
                v-model="form.roleIdList"
                multiple
                placeholder="请选择角色"
                style="width:100%"
              >
                <el-option
                  v-for="role in allRoleList"
                  :key="role.id"
                  :label="role.roleName"
                  :value="role.id"
                />
              </el-select>
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
import { ref,reactive,onMounted } from 'vue'
import { getUserPage,saveUser,delUser,getRoleIds,resetPwd } from '@/api/system/user'
import { ElMessage, ElMessageBox, type ElTable } from 'element-plus'
const tableRef = ref<ElTable>()

const selectedRows = ref<any[]>([])
const selectedCount = ref(0)
const crossPageSelect = ref(false)

const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
  selectedCount.value = rows.length
}
const clearSelect = () => {
  tableRef.value?.clearSelection()
  selectedRows.value = []
  selectedCount.value = 0
}



// 表格
const tableData = ref<any[]>([])
const total = ref(0)
const page = reactive({
  pageNum:1,
  pageSize:10
})
const searchForm = reactive({
  realName:''
})

//弹窗
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id:null,
  username:'',
  password:'',
  realName:'',
  phone:'',
  roleNames: '',   // 加在 form 里
  status:1,
  roleIdList:[]
})

//加载表格
const loadTable = async ()=>{
  const res = await getUserPage({...page,...searchForm})
  if(res.code ===200){
    tableData.value = res.data.records
    total.value = res.data.total
  }
}
loadTable()

//重置搜索
const resetSearch = ()=>{
  searchForm.realName=''
  page.pageNum=1
  loadTable()
}

//打开弹窗
const openDialog = async (row?: any) => {
  dialogVisible.value = true
  // 每次打开弹窗先重置角色数组
  form.roleIdList = []
  if (row) {
    // 编辑
    isEdit.value = true
    // 先拷贝基础信息
    Object.assign(form, row)
    // 单独请求该用户绑定角色
    const roleRes = await getRoleIds(row.id)
    if (roleRes.code === 200) {
      form.roleIdList = roleRes.data
    }
  } else {
    // 新增用户：清空表单
    isEdit.value = false
    form.id = null
    form.username = ''
    form.password = ''
    form.realName = ''
    form.phone = ''
    form.status = 1
    form.roleIdList = []
  }
}


//提交保存
const submitForm = async () => {
  try {
    await saveUser(form);
    dialogVisible.value = false
    ElMessage.success('操作成功')
    loadTable()
  } catch (err) {
    ElMessage.error('提交失败')
  }
};


//删除
const handleDelete = (row:any)=>{
  ElMessageBox.confirm('确定删除该用户？','提示',{type:'warning'})
  .then(async ()=>{
    await delUser(row.id)
    ElMessage.success('删除成功')
    loadTable()
  })
}

//重置密码
const handleResetPwd = (row:any)=>{
  ElMessageBox.confirm('确定重置密码？','提示')
  .then(async ()=>{
    await resetPwd(row.id)
    ElMessage.success('密码重置成功')
  })
}

import { getRoleAll } from '@/api/system/role'

// 角色下拉全部选项
const allRoleList = ref<any[]>([])

// 获取全部角色
const loadAllRole = async()=>{
  const res = await getRoleAll()
  allRoleList.value = res.data
}

// 页面初始化加载角色
onMounted(()=>{
  loadAllRole()
})
</script>

<style scoped>
.user-page{
  padding:16px;
}
</style>
