<template>
  <div class="menu-page">
    <!-- 搜索区 -->
    <el-card class="search-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input v-model="searchForm.menuName" placeholder="请输入菜单名称" @keyup.enter="loadTable"></el-input>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadTable">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-col>
        <el-col :span="14" style="text-align:right">
          <el-button type="primary" @click="openDialog()">新增菜单</el-button>
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
        <el-table-column label="菜单名称" prop="menuName"></el-table-column>
        <el-table-column label="路由地址" prop="path"></el-table-column>
        <el-table-column label="图标" prop="icon"></el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button size="small" type="primary" @click="openDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页区域 和角色完全一致 -->
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
    <el-dialog v-model="dialogVisible" title="菜单信息">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="父级菜单" prop="id">
              <el-select v-model="form.parentId" placeholder="请选择父菜单" clearable>
                <el-option
                  v-for="item in menuTree"
                  :key="item.id"
                  :label="item.menuName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单名称" prop="menuName">
              <el-input v-model="form.menuName"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="路由地址" prop="path">
              <el-input v-model="form.path"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="图标" prop="icon">
              <el-input v-model="form.icon"></el-input>
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
import { ref,reactive } from 'vue'
import { getMenuPage,saveMenu,deleteMenu,getMenuTree } from '@/api/system/menu'
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
  menuName:''
})
//弹窗
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id:null,
  parentId: null,
  menuName:'',
  path:'',
  icon:'',
  menuType:'M',
  sort:0,
  status:1
})
const menuTree = ref<any[]>([])
//加载表格
const loadTable = async ()=>{
  const res = await getMenuPage({...page,...searchForm})
  if(res.code ===200){
    tableData.value = res.data.records
    total.value = res.data.total
  }
}
//加载菜单树（父级下拉用）
const loadMenuTree = async () => {
  const res = await getMenuTree()
  if(res.code === 200){
    menuTree.value = res.data
  }
}
loadTable()
loadMenuTree()
//重置搜索
const resetSearch = ()=>{
  searchForm.menuName=''
  page.pageNum=1
  loadTable()
}
//打开弹窗
const openDialog = async (row?:any)=>{
  dialogVisible.value = true
  if(row){
    isEdit.value = true
    Object.assign(form,row)
  }else{
    isEdit.value = false
    form.id = null
    form.parentId = null
    form.menuName=''
    form.path=''
    form.icon=''
    form.menuType='M'
    form.sort=0
    form.status=1
  }
}
//提交保存
const submitForm = async ()=>{
  try {
    const submitData = {
      // 有id就是编辑，无id就是新增
      ...(isEdit.value ? {id:form.id} : {}),
      parentId: form.parentId,
      menuName: form.menuName,
      path: form.path,
      icon: form.icon
    }
    await saveMenu(submitData)
    dialogVisible.value=false
    ElMessage.success('操作成功')
    loadTable()
    loadMenuTree()
  } catch (err) {
    ElMessage.error('保存失败')
  }
}


//删除
const handleDelete = (row:any)=>{
  ElMessageBox.confirm('确定删除该菜单？','提示',{type:'warning'})
  .then(async ()=>{
    await deleteMenu(row.id)
    ElMessage.success('删除成功')
    loadTable()
    loadMenuTree()
  })
}
</script>

<style scoped>
.menu-page{
  padding:16px;
}
</style>
