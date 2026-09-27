<template>
  <div class="dict-wrap" style="display:flex;gap:12px;height:calc(100vh - 170px)">
    <!-- 左侧：字典类型列表（对应截图左侧产品分类） -->
    <el-card style="width:280px;overflow:auto;">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
        <span style="font-weight:bold">字典类型</span>
        <el-button type="primary" size="small" @click="openAddType">新增</el-button>
      </div>
      <el-input v-model="typeSearch" 
        placeholder="输入类型名称搜索" 
        size="small" style="margin-bottom:10px"
        @keyup.enter="loadDictType"
      />
      <el-scrollbar height="calc(100vh - 240px)">
        <div v-for="item in typeList" :key="item.id"
             class="type-item"
             :class="{active: selectedTypeId === item.id}"
             @click="selectType(item)">
          <div style="display:flex;justify-content:space-between">
            <span>{{ item.dictName }}</span>
          </div>
          <div style="margin-top:4px">
            <el-button text type="primary" size="small" @click.stop="openEditType(item)">编辑</el-button>
            <el-button text type="danger" size="small" @click.stop="delType(item)">删除</el-button>
          </div>
        </div>
      </el-scrollbar>
    </el-card>

    <!-- 右侧：选中类型下的字典数据（对应截图右侧产品列表） -->
    <el-card style="flex:1;display:flex;flex-direction:column;">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px">
        <span style="font-weight:bold">
          {{ currentDictType ? `字典项：${currentDictType.dictName}` : '请左侧选择字典类型' }}
        </span>
        <!-- ========== 新增搜索区域（红框位置） ========== -->
        <div v-if="currentDictType" style="display:flex;gap:8px;align-items:center">
          <el-input 
            v-model="dataSearch" 
            placeholder="输入标签名称搜索" 
            size="small" 
            style="width:220px"
            @keyup.enter="loadDictData"
          />
          <el-button type="primary" size="small" @click="loadDictData">搜索</el-button>
          <el-button size="small" @click="resetDataSearch">重置</el-button>
          <el-button type="primary" size="small" @click="openAddData">新增字典项</el-button>
        </div>
        <el-button v-else type="primary" size="small" @click="openAddData">新增字典项</el-button>
      </div>
      <el-table v-if="currentDictType" :data="dataTable" border stripe style="flex:1;overflow:auto;">
        <el-table-column prop="id" label="ID"/>
        <el-table-column prop="dictLabel" label="标签名称"/>
        <el-table-column label="创建时间" prop="createTime"></el-table-column>
        <el-table-column label="更新时间" prop="updateTime"></el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button text type="primary" @click="openEditData(scope.row)">编辑</el-button>
            <el-button text type="danger" @click="delData(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="请在左侧选择字典类型"></el-empty>

      <!-- 字典数据分页 -->
      <div v-if="currentDictType" style="margin-top:10px">
        <el-pagination
          v-model:current-page="dataPage.pageNum"
          v-model:page-size="dataPage.pageSize"
          :total="dataTotal"
          @change="loadDictData"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-card>

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
    <el-dialog v-model="dataDialog.visible" title="字典项">
      <el-form :model="dataForm" label-width="100px">
        <el-form-item label="标签名称">
          <el-input v-model="dataForm.dictLabel"/>
        </el-form-item>
        <el-form-item label="字典值">
          <el-input v-model="dataForm.dictValue"/>
        </el-form-item>
        <el-form-item label="排序">
          <el-input v-model.number="dataForm.sort"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialog.visible=false">取消</el-button>
        <el-button type="primary" @click="submitData">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { getDictTypePage, addDictType, updateDictType, delDictType, getDictTypeInfo,
         getDictDataPage, addDictData, updateDictData, delDictData, getDictDataInfo } from '@/api/system/dict'
import { ElMessage, ElMessageBox } from 'element-plus'

// ==========左侧字典类型==========
const typeSearch = ref('')
const typeList = ref<any[]>([])
const selectedTypeId = ref<number|null>(null)
let currentDictType:any = ref(null)
const typeDialog = reactive({ visible:false, isEdit:false })
const typeForm = ref<any>({})

// ==========右侧字典数据==========
const dataSearch = ref('')
const dataTable = ref<any[]>([])
const dataTotal = ref(0)
const dataPage = reactive({ pageNum:1, pageSize:10 })
const dataDialog = reactive({ visible:false, isEdit:false })
const dataForm = ref<any>({})

// 重置右侧搜索
const resetDataSearch = ()=>{
  dataSearch.value = ''
  dataPage.pageNum = 1
  loadDictData()
}

// 加载全部字典类型（左侧列表）
const loadDictType = async()=>{
  const res = await getDictTypePage({pageNum:1,pageSize:999, dictName:typeSearch.value})
  typeList.value = res.data.records
}

// 选中左侧字典类型
const selectType = (row:any)=>{
  selectedTypeId.value = row.id
  currentDictType.value = row
  dataPage.pageNum =1
  dataSearch.value = '' // 切换分类，清空搜索框
  loadDictData()
}

//加载右侧字典数据【修改这个方法，带上搜索参数传给后端】
const loadDictData = async()=>{
  const res = await getDictDataPage({
    ...dataPage, 
    dictType:currentDictType.value.dictType,
    dictLabel: dataSearch.value
  })
  dataTable.value = res.data.records
  dataTotal.value = res.data.total
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
  // 如果删除的是当前选中项，清空右侧
  if(selectedTypeId.value === row.id){
    selectedTypeId.value = null
    currentDictType.value = null
    dataTable.value = []
  }
}

//字典数据弹窗
const openAddData = ()=>{
  dataDialog.isEdit = false
  dataForm.value = {dictType:currentDictType.value.dictType, sort:0}
  dataDialog.visible = true
}
const openEditData = async(row:any)=>{
  dataDialog.isEdit = true
  const res = await getDictDataInfo(row.id)
  dataForm.value = res.data
  dataDialog.visible = true
}
const submitData = async () => {
  // 构造提交数据，剔除自动填充字段
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
  loadDictData();
};

const delData = async(row:any)=>{
  await ElMessageBox.confirm('确定删除字典项？')
  await delDictData(row.id)
  ElMessage.success('删除成功')
  loadDictData()
}

onMounted(()=>loadDictType())
</script>

<style scoped>
.type-item{
  padding:8px;
  border-radius:4px;
  cursor:pointer;
  border-bottom:1px solid #eee;
}
.type-item:hover{
  background:#f5f7fa;
}
.type-item.active{
  background:#e6f7ff;
}
</style>
