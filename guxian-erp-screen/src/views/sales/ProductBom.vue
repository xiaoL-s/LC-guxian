<template>
  <div class="sales-page">
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="search" size="small">
        <el-form-item label="产品名称">
          <el-input v-model="search.productName" placeholder="产品名称" clearable style="width:160px" @keyup.enter="loadTable" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="search.productType" placeholder="全部分类" clearable style="width:140px">
            <el-option v-for="t in dictTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
          <el-button type="primary" @click="openProduct()">新增产品</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 产品表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe size="small">
        <el-table-column label="编码" prop="productCode" width="90" />
        <el-table-column label="产品名称" prop="productName" min-width="170" show-overflow-tooltip />
        <el-table-column label="分类" prop="productType" width="110" />
        <el-table-column label="规格" prop="spec" width="110" show-overflow-tooltip />
        <el-table-column label="单价" prop="unitPrice" width="90" align="right">
          <template #default="s">{{ s.row.unitPrice }} 元/{{ s.row.unit }}</template>
        </el-table-column>
        <el-table-column label="计价" width="70" align="center">
          <template #default="s">{{ s.row.priceType === 2 ? '按件' : '按面积' }}</template>
        </el-table-column>
        <el-table-column label="起算方(㎡)" prop="minArea" width="90" align="right" />
        <el-table-column label="状态" width="70" align="center">
          <template #default="s">
            <el-tag :type="s.row.status === 1 ? 'success' : 'info'" size="small">{{ s.row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="s">
            <el-button size="small" type="primary" link @click="openProduct(s.row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="onDelete(s.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination v-model:current-page="page.pageNum" v-model:page-size="page.pageSize" :total="total"
          small layout="total, sizes, prev, pager, next" @change="loadTable" />
      </div>
    </el-card>

    <!-- 产品新增/编辑弹窗 -->
    <el-dialog v-model="productVisible" :title="form.productId ? '编辑产品' : '新增产品'" width="720px" destroy-on-close>
      <el-form ref="productFormRef" :model="form" :rules="rules" label-width="92px" size="small">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="产品编码" prop="productCode"><el-input v-model="form.productCode" disabled /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品名称" prop="productName">
              <el-select v-model="form.productName" placeholder="请选择产品" style="width:100%" filterable>
                <el-option v-for="n in dictNameOptions" :key="n.value" :label="n.label" :value="n.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品分类">
              <el-select v-model="form.productType" placeholder="请选择" style="width:100%">
                <el-option v-for="t in dictTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格型号"><el-input v-model="form.spec" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="计价方式">
              <el-select v-model="form.priceType" style="width:100%">
                <el-option label="按面积" :value="1" />
                <el-option label="按件" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单价"><el-input-number v-model="form.unitPrice" :min="0" :precision="2" :controls="false" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单位"><el-input v-model="form.unit" placeholder="㎡/件" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="起算方㎡"><el-input-number v-model="form.minArea" :min="0" :precision="4" :controls="false" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="默认颜色">
              <el-select v-model="form.defaultColor" placeholder="颜色" style="width:100%">
                <el-option v-for="c in dictColorOptions" :key="c.value" :label="c.label" :value="c.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="默认材质">
              <el-select v-model="form.defaultMaterial" placeholder="纱网" style="width:100%">
                <el-option v-for="n in dictNetOptions" :key="n.value" :label="n.label" :value="n.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="开启方向"><el-input v-model="form.openDirection" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width:100%">
                <el-option label="启用" :value="1" /><el-option label="停用" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button size="small" @click="productVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="submitProduct">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { getProductPage, saveProduct, delProduct } from '@/api/sales/product'
import type { TProduct } from '@/types/sales'
import { getDictDataPage, getDictTypePage } from '@/api/system/dict'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

// 字典选项
const dictTypeOptions = ref<{label:string,value:string}[]>([])  // 产品分类（sys_dict_type）
const dictNameOptions = ref<{label:string,value:string}[]>([])  // 产品名称（sys_dict_data，随分类联动）
const dictColorOptions = ref<{label:string,value:string}[]>([])
const dictNetOptions = ref<{label:string,value:string}[]>([])

// 加载所有字典类型作为产品分类
const loadDictTypes = async () => {
  const res: any = await getDictTypePage({ pageNum: 1, pageSize: 200 })
  dictTypeOptions.value = (res.data?.records || [])
    .filter((t: any) => t.status === 1)
    .map((t: any) => ({ label: t.dictName, value: t.dictType, code: t.dictType }))
}
// 根据选中的分类编码，加载该分类下的产品名称
const loadNamesByType = async (dictType: string) => {
  if (!dictType) { dictNameOptions.value = []; return }
  const res: any = await getDictDataPage({ pageNum: 1, pageSize: 200, dictType })
  dictNameOptions.value = (res.data?.records || [])
    .filter((d: any) => d.status === 1)
    .map((d: any) => ({ label: d.dictLabel, value: d.dictLabel }))
}
// 加载颜色/纱网
const loadSimpleDict = async (dictType: string) => {
  const res: any = await getDictDataPage({ pageNum: 1, pageSize: 200, dictType })
  return (res.data?.records || []).map((d: any) => ({ label: d.dictLabel, value: d.dictLabel }))
}
onMounted(async () => {
  await loadDictTypes()
  dictColorOptions.value = await loadSimpleDict('product_color')
  dictNetOptions.value = await loadSimpleDict('net_material')
})

const loading = ref(false)
const tableData = ref<TProduct[]>([])
const total = ref(0)
const page = reactive({ pageNum: 1, pageSize: 10 })
const search = reactive({ productName: '', productType: '' })

const loadTable = async () => {
  loading.value = true
  try {
    const res = await getProductPage({ ...page, ...search })
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } catch (e) { console.error(e) } finally { loading.value = false }
}
loadTable()
const onSearch = () => { page.pageNum = 1; loadTable() }
const onReset = () => { search.productName = ''; search.productType = ''; page.pageNum = 1; loadTable() }

// 产品弹窗
const productVisible = ref(false)
const productFormRef = ref<FormInstance>()
const blankForm = (): TProduct => ({ productCode: '', productName: '', productType: '', spec: '', unit: '㎡', unitPrice: 0, priceType: 1, minArea: 0, defaultColor: '', defaultMaterial: '', openDirection: '', status: 1, remark: '' })
const form = reactive<TProduct>(blankForm())
// 分类联动：切换分类时清空产品名称并重新加载
watch(() => form.productType, (newType) => {
  form.productName = ''
  loadNamesByType(newType || '')
})
const rules = {
  productCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}
const openProduct = async (row?: TProduct) => {
  productFormRef.value?.clearValidate()
  Object.assign(form, blankForm())
  if (row) {
    Object.assign(form, row)
  } else {
    // 自动生成编码 gx001 递增
    const res: any = await getProductPage({ pageNum: 1, pageSize: 200, productName: '' })
    const records: any[] = res.data?.records || []
    const maxNum = records
      .map(r => /^gx(\d+)$/.exec(r.productCode || ''))
      .filter(Boolean)
      .map(m => parseInt(m![1]))
      .reduce((a, b) => Math.max(a, b), 0)
    form.productCode = 'gx' + String(maxNum + 1).padStart(3, '0')
  }
  productVisible.value = true
}
const submitProduct = async () => {
  await productFormRef.value?.validate()
  await saveProduct({ ...form })
  ElMessage.success('保存成功')
  productVisible.value = false
  loadTable()
}
const onDelete = (row: TProduct) => {
  ElMessageBox.confirm(`确定删除产品「${row.productName}」？`, '提示', { type: 'warning' }).then(async () => {
    await delProduct(row.productId!)
    ElMessage.success('删除成功'); loadTable()
  }).catch(() => {})
}
</script>

<style scoped>
.sales-page { padding: 12px; }
.search-card :deep(.el-card__body) { padding: 12px 12px 0; }
.table-card { margin-top: 10px; }
.table-card :deep(.el-card__body) { padding: 12px; }
.pager { margin-top: 10px; display: flex; justify-content: flex-end; }
</style>
