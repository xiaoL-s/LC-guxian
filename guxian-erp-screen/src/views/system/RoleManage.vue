<template>
  <div class="role-page">
    <!-- 搜索区 -->
    <el-card class="search-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input v-model="searchForm.roleName" placeholder="请输入角色名称" @keyup.enter="loadTable"></el-input>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadTable">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-col>
        <el-col :span="14" style="text-align:right">
          <el-button type="primary" @click="openDialog()">新增角色</el-button>
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
        <el-table-column label="角色名称" prop="roleName"></el-table-column>
        <el-table-column label="角色编码" prop="roleCode"></el-table-column>
        <el-table-column label="创建时间" prop="createTime"></el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button size="small" type="primary" @click="openDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页区域 和员工完全一致 -->
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
    <el-dialog v-model="dialogVisible" title="角色信息">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色名称" prop="roleName">
              <el-input v-model="form.roleName"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色编码" prop="roleCode">
              <el-input v-model="form.roleCode"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="分配菜单权限">
          <el-tree
            ref="treeRef"
            :data="menuTreeData"
            show-checkbox
            v-model="checkedKeys"
            node-key="id"
            :default-expand-all="true"
            :props="{ label: 'menuName', children: 'children' }"
          />

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

import { ref,reactive } from 'vue'
import { getRolePage,saveRole,deleteRole,getMenuIds,getMenuTree } from '@/api/system/role'
import { ElMessage, ElMessageBox, type ElTable, type ElTree } from 'element-plus'

const tableRef = ref<ElTable>()
const treeRef = ref<ElTree>()
const selectedRows = ref<any[]>([])
const selectedCount = ref(0)
const crossPageSelect = ref(false)

const menuTreeData = ref([])
const checkedKeys = ref<number[]>([])


// 加载菜单树接口
const loadMenuTree = async () => {
  const res = await getMenuTree() // 你的获取菜单树api
  console.log(res.data)
  if (res.code === 200) {
    menuTreeData.value = res.data
  }
}


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
  roleName:''
})
//弹窗
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id:null,
  roleName:'',
  roleCode:'',
  menuIdList:[] as number[]
})


//加载表格
const loadTable = async ()=>{
  const res = await getRolePage({...page,...searchForm})
  if(res.code ===200){
    tableData.value = res.data.records
    total.value = res.data.total
  }
}
loadTable()

//重置搜索
const resetSearch = ()=>{
  searchForm.roleName=''
  page.pageNum=1
  loadTable()
}

//打开弹窗
const openDialog = async (row?:any)=>{
  
  dialogVisible.value = true
  checkedKeys.value = []
  await loadMenuTree()
  if(row){
    isEdit.value = true
    Object.assign(form,row)
    const menuRes = await getMenuIds(row.id)
    if(menuRes.code ===200){
      checkedKeys.value = menuRes.data
    }
  }else{
    isEdit.value = false
    form.id = null
    form.roleName=''
    form.roleCode=''
  }
}



//提交保存
const submitForm = async ()=>{
  const keys = treeRef.value!.getCheckedKeys()
  form.menuIdList = keys
  await saveRole(form)
  dialogVisible.value=false
  ElMessage.success('操作成功')
  loadTable()
}

//删除
const handleDelete = (row:any)=>{
  ElMessageBox.confirm('确定删除该角色？','提示',{type:'warning'})
  .then(async ()=>{
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    loadTable()
  })
}
</script>
<style scoped>
.role-page{
  padding:16px;
}
</style>
