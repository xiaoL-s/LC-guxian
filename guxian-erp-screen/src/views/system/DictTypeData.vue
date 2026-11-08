<template>
  <div class="dict-page">
    <!-- 顶部：全产品搜索栏 -->
    <el-card shadow="never" class="search-bar">
      <div class="search-inner">
        <el-input
          v-model="globalSearch"
          placeholder="🔍 输入产品名称，全库搜索..."
          size="large"
          clearable
        />
        <el-button type="primary" size="large" @click="doGlobalSearch">搜索</el-button>
        <el-button v-if="searchMode" size="large" @click="exitGlobalSearch">返回分类浏览</el-button>
      </div>
    </el-card>

    <!-- 下方：左右结构 -->
    <div class="main-body">
      <!-- 左侧：字典类型列表 -->
      <el-card shadow="never" class="type-panel">
        <div class="panel-header">
          <span class="panel-title">产品分类</span>
          <el-button type="primary" size="small" @click="openAddType">新增</el-button>
        </div>
        <el-input v-model="typeSearch"
          placeholder="搜索分类..."
          size="small" style="margin-bottom:10px"
        />
        <el-scrollbar>
          <div v-for="item in typeList" :key="item.id"
               class="type-item"
               :class="{active: selectedTypeId === item.id && !searchMode}"
               @click="selectType(item)">
            <div class="type-name">{{ item.dictName }}</div>
            <div class="type-actions">
              <el-button text type="primary" size="small" @click.stop="openEditType(item)">编辑</el-button>
              <el-button text type="danger" size="small" @click.stop="delType(item)">删除</el-button>
            </div>
          </div>
        </el-scrollbar>
      </el-card>

      <!-- 右侧：数据表格 -->
      <el-card shadow="never" class="data-panel">
        <div class="panel-header">
          <span class="panel-title">
            {{ searchMode ? `🔍 搜索结果：${globalSearch}` : (currentDictType ? `📦 ${currentDictType.dictName}` : '请选择左侧分类') }}
          </span>
          <div v-if="currentDictType && !searchMode" class="header-actions">
            <el-input
              v-model="dataSearch"
              placeholder="当前分类内搜索..."
              size="small"
              style="width:200px"
            />
            <el-button type="primary" size="small" @click="loadDictData">搜索</el-button>
            <el-button size="small" @click="resetDataSearch">重置</el-button>
            <el-button type="primary" size="small" @click="openAddData">新增产品</el-button>
          </div>
          <el-button v-if="!currentDictType && !searchMode" type="primary" size="small" @click="openAddData">新增产品</el-button>
        </div>

        <!-- 表格 -->
        <el-table v-if="showTable" :data="dataTable" border stripe class="data-table">
          <el-table-column prop="id" label="ID" width="70"/>
          <el-table-column prop="dictLabel" label="产品名称" min-width="220">
            <template #default="scope">
              <span style="font-weight:500">{{ scope.row.dictLabel }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="searchMode" prop="dictType" label="所属分类" min-width="150">
            <template #default="scope">
              <el-tag size="small" type="info">{{ getTypeName(scope.row.dictType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="dictValue" label="字典值" width="100"/>
          <el-table-column label="公式" width="100" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.formulaConfig" type="success" effect="plain" size="small">已配置</el-tag>
              <span v-else class="dash">—</span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="170"/>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button text type="primary" @click="openEditData(scope.row)">编辑</el-button>
              <el-button text type="success" @click="openFormula(scope.row)">公式</el-button>
              <el-button text type="danger" @click="delData(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="在顶部搜索产品名称，或从左侧选择分类"></el-empty>

        <!-- 分页 -->
        <div v-if="showTable" class="pager">
          <el-pagination
            v-model:current-page="dataPage.pageNum"
            v-model:page-size="dataPage.pageSize"
            :total="dataTotal"
            @change="loadData"
            layout="total, sizes, prev, pager, next, jumper"
          />
        </div>
      </el-card>
    </div>

    <!-- 弹窗：新增/编辑字典类型 -->
    <el-dialog v-model="typeDialog.visible" title="字典类型">
      <el-form :model="typeForm" label-width="100px">
        <el-form-item label="字典名称">
          <el-input v-model="typeForm.dictName"/>
        </el-form-item>
        <el-form-item label="类型编码">
          <el-input v-model="typeForm.dictType"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="typeForm.status">
            <el-option label="正常" :value="1"/>
            <el-option label="停用" :value="0"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialog.visible=false">取消</el-button>
        <el-button type="primary" @click="submitType">确定</el-button>
      </template>
    </el-dialog>

    <!-- 弹窗：新增/编辑字典数据 -->
    <el-dialog v-model="dataDialog.visible" title="产品信息" width="520px" :close-on-click-modal="false">
      <el-form :model="dataForm" label-width="100px">
        <el-form-item label="产品名称">
          <el-input v-model="dataForm.dictLabel"/>
        </el-form-item>
        <el-form-item label="字典值">
          <el-input v-model="dataForm.dictValue"/>
        </el-form-item>
        <el-form-item label="所属分类">
          <el-select v-model="dataForm.dictType" placeholder="选择分类">
            <el-option v-for="t in typeList" :key="t.id" :label="t.dictName" :value="t.dictType"/>
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input v-model.number="dataForm.sort"/>
        </el-form-item>
        <el-form-item v-if="dataForm.id">
          <div class="formula-hint">
            公式配置请在列表点「公式」按钮，用可视化界面编辑。
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialog.visible=false">取消</el-button>
        <el-button type="primary" @click="submitData">保存</el-button>
      </template>
    </el-dialog>

    <!-- 弹窗：可视化公式管理 -->
    <el-dialog v-model="formulaDialog.visible" title="" width="94%" top="3vh"
               :close-on-click-modal="false" destroy-on-close>
      <DictFormulaManager
        v-if="formulaDialog.visible"
        :dict-item="formulaItem"
        @close="formulaDialog.visible=false"
        @saved="onFormulaSaved"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'DictTypeData' })
import { ref, reactive, onMounted } from 'vue'
import { getDictTypePage, addDictType, updateDictType, delDictType, getDictTypeInfo,
         getDictDataPage, addDictData, updateDictData, delDictData, getDictDataInfo } from '@/api/system/dict'
import { ElMessage, ElMessageBox } from 'element-plus'
import DictFormulaManager from './components/DictFormulaManager.vue'

// ==========全局搜索==========
const globalSearch = ref('')
const searchMode = ref(false)

// ==========左侧字典类型==========
const typeSearch = ref('')
const typeList = ref<any[]>([])
const selectedTypeId = ref<number|null>(null)
const currentDictType = ref<any>(null)
const typeDialog = reactive({ visible:false, isEdit:false })
const typeForm = ref<any>({})

// ==========右侧字典数据==========
const dataSearch = ref('')
const dataTable = ref<any[]>([])
const dataTotal = ref(0)
const dataPage = reactive({ pageNum:1, pageSize:20 })
const dataDialog = reactive({ visible:false, isEdit:false })
const dataForm = ref<any>({})

// ==========可视化公式管理==========
const formulaDialog = reactive({ visible:false })
const formulaItem = ref<any>({})

// 类型编码 -> 名称映射
const typeNameMap = ref<Record<string,string>>({})
const getTypeName = (dictType: string) => typeNameMap.value[dictType] || dictType

// 是否显示表格
const showTable = ref(false)

// 全局搜索
const doGlobalSearch = async() => {
  if (!globalSearch.value.trim()) {
    exitGlobalSearch()
    return
  }
  searchMode.value = true
  selectedTypeId.value = null
  currentDictType.value = null
  dataPage.pageNum = 1
  await loadData()
}

// 退出全局搜索，返回分类浏览
const exitGlobalSearch = () => {
  searchMode.value = false
  globalSearch.value = ''
  dataSearch.value = ''
  if (currentDictType.value) {
    loadData()
  } else {
    showTable.value = false
    dataTable.value = []
  }
}

// 加载数据（统一入口：根据searchMode决定查全部还是查指定类型）
const loadData = async() => {
  if (searchMode.value) {
    // 全局搜索：不传dictType，只传dictLabel
    const res = await getDictDataPage({
      ...dataPage,
      dictLabel: globalSearch.value
    })
    dataTable.value = res.data.records
    dataTotal.value = res.data.total
    showTable.value = true
  } else if (currentDictType.value) {
    // 分类浏览：传dictType + dictLabel
    const res = await getDictDataPage({
      ...dataPage,
      dictType: currentDictType.value.dictType,
      dictLabel: dataSearch.value
    })
    dataTable.value = res.data.records
    dataTotal.value = res.data.total
    showTable.value = true
  } else {
    showTable.value = false
    dataTable.value = []
  }
}

// 重置右侧搜索
const resetDataSearch = ()=>{
  dataSearch.value = ''
  dataPage.pageNum = 1
  loadData()
}

// 加载全部字典类型
const loadDictType = async()=>{
  const res = await getDictTypePage({pageNum:1,pageSize:999, dictName:typeSearch.value})
  typeList.value = res.data.records
  // 构建类型编码->名称映射
  typeNameMap.value = {}
  for (const t of typeList.value) {
    typeNameMap.value[t.dictType] = t.dictName
  }
}

// 选中左侧字典类型
const selectType = (row:any)=>{
  searchMode.value = false
  selectedTypeId.value = row.id
  currentDictType.value = row
  dataPage.pageNum =1
  dataSearch.value = ''
  loadData()
}

// 字典类型弹窗
const openAddType = ()=>{
  typeDialog.isEdit = false
  typeForm.value = {status:1}
  typeDialog.visible = true
}
const openEditType = async(row:any)=>{
  typeDialog.isEdit = true
  const res = await getDictTypeInfo(row.id)
  typeForm.value = res.data
  typeDialog.visible = true
}
const submitType = async()=>{
  if(typeDialog.isEdit){
    await updateDictType(typeForm.value)
  }else{
    await addDictType(typeForm.value)
  }
  typeDialog.visible = false
  ElMessage.success('保存成功')
  loadDictType()
}
const delType = async(row:any)=>{
  await ElMessageBox.confirm('确定删除该字典类型？')
  await delDictType(row.id)
  ElMessage.success('删除成功')
  loadDictType()
  if(selectedTypeId.value === row.id){
    selectedTypeId.value = null
    currentDictType.value = null
    showTable.value = false
    dataTable.value = []
  }
}

// 字典数据弹窗
const openAddData = ()=>{
  dataDialog.isEdit = false
  dataForm.value = {dictType:currentDictType.value?.dictType, sort:0}
  dataDialog.visible = true
}
const openEditData = async(row:any)=>{
  dataDialog.isEdit = true
  const res = await getDictDataInfo(row.id)
  dataForm.value = res.data
  dataDialog.visible = true
}
const submitData = async () => {
  const submitData: any = { ...dataForm.value };
  delete submitData.createTime;
  delete submitData.updateTime;
  delete submitData.createBy;
  delete submitData.updateBy;

  if (dataDialog.isEdit) {
    await updateDictData(submitData);
  } else {
    await addDictData(submitData);
  }

  dataDialog.visible = false;
  ElMessage.success('保存成功');
  loadData();
};

const delData = async(row:any)=>{
  await ElMessageBox.confirm('确定删除该产品？')
  await delDictData(row.id)
  ElMessage.success('删除成功')
  loadData()
}

// 可视化公式管理
const openFormula = (row:any)=>{
  formulaItem.value = { ...row }
  formulaDialog.visible = true
}
const onFormulaSaved = ()=>{
  formulaDialog.visible = false
  loadData()
}

onMounted(()=>loadDictType())
</script>

<style scoped>
.dict-page {
  height: calc(100vh - 130px);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 顶部搜索栏 */
.search-bar {
  flex-shrink: 0;
  border-radius: 10px;
}
.search-inner {
  display: flex;
  align-items: center;
  gap: 12px;
}
.search-icon {
  font-size: 20px;
  color: #909399;
}
.search-inner .el-input {
  flex: 1;
}

/* 主体左右布局 */
.main-body {
  flex: 1;
  display: flex;
  gap: 10px;
  overflow: hidden;
}

/* 左侧分类 */
.type-panel {
  width: 240px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 10px;
}
.type-panel :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

/* 右侧数据 */
.data-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 10px;
}
.data-panel :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-shrink: 0;
}
.panel-title {
  font-weight: 600;
  font-size: 15px;
}
.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.type-item {
  padding: 10px 8px;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;
}
.type-item:hover {
  background: #f5f7fa;
}
.type-item.active {
  background: #ecf5ff;
  border-color: #b3d8ff;
}
.type-name {
  font-size: 13px;
  margin-bottom: 4px;
}
.type-actions {
  display: flex;
  gap: 4px;
}

.data-table {
  flex: 1;
  overflow: auto;
}
.pager {
  margin-top: 10px;
  flex-shrink: 0;
  display: flex;
  justify-content: flex-end;
}

.dash { color: #c0c4cc; }
.formula-hint {
  font-size: 12px;
  color: #909399;
  line-height: 1.7;
  background: #f5f7fa;
  padding: 8px 12px;
  border-radius: 6px;
}
</style>
