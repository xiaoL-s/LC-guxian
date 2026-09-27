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
            <el-option v-for="t in PRODUCT_TYPES" :key="t" :label="t" :value="t" />
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="s">
            <el-button size="small" type="primary" link @click="openProduct(s.row)">编辑</el-button>
            <el-button size="small" type="warning" link @click="openBom(s.row)">配置BOM</el-button>
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
            <el-form-item label="产品编码" prop="productCode"><el-input v-model="form.productCode" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品名称" prop="productName"><el-input v-model="form.productName" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品分类">
              <el-select v-model="form.productType" placeholder="请选择" style="width:100%">
                <el-option v-for="t in PRODUCT_TYPES" :key="t" :label="t" :value="t" />
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
            <el-form-item label="默认颜色"><el-input v-model="form.defaultColor" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="默认材质"><el-input v-model="form.defaultMaterial" /></el-form-item>
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

    <!-- BOM配置弹窗 -->
    <el-dialog v-model="bomVisible" :title="`配置BOM - ${bomProduct?.productName ?? ''}`" width="860px" destroy-on-close>
      <el-alert title="单位用量指每㎡（或每件）产品所需物料数量；需求量 = 用量 × 订单面积 × (1+损耗率)" type="info" :closable="false" style="margin-bottom:10px" />
      <el-table :data="bomList" border size="small">
        <el-table-column label="物料" min-width="240">
          <template #default="s">
            <el-select v-model="s.row.materialId" filterable placeholder="选择物料" size="small" style="width:100%" @change="(v)=>onMaterialChange(s.row,v)">
              <el-option v-for="m in materialOptions" :key="m.id" :label="`${m.materialCode} ${m.materialName}${m.spec?'/'+m.spec:''}`" :value="m.id" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="单位" prop="unit" width="60" align="center" />
        <el-table-column label="单位用量" width="130">
          <template #default="s"><el-input-number v-model="s.row.useNum" :min="0" :precision="3" :controls="false" size="small" style="width:100%" /></template>
        </el-table-column>
        <el-table-column label="损耗率" width="120">
          <template #default="s"><el-input-number v-model="s.row.lossRate" :min="0" :max="1" :step="0.01" :precision="2" :controls="false" size="small" style="width:100%" /></template>
        </el-table-column>
        <el-table-column label="操作" width="70" align="center">
          <template #default="s"><el-button size="small" type="danger" link @click="bomList.splice(s.$index,1)">移除</el-button></template>
        </el-table-column>
      </el-table>
      <el-button size="small" type="primary" plain style="margin-top:10px" @click="addBomRow">+ 添加物料</el-button>
      <template #footer>
        <el-button size="small" @click="bomVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="submitBom">保存BOM</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { getProductPage, saveProduct, delProduct, getProductBom, saveProductBom, listMaterials } from '@/api/sales/product'
import { PRODUCT_TYPES, type TProduct, type ProductBom, type MaterialOption } from '@/types/sales'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

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
const rules = {
  productCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}
const openProduct = (row?: TProduct) => {
  productFormRef.value?.clearValidate()
  Object.assign(form, blankForm())
  if (row) Object.assign(form, row)
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

// BOM弹窗
const bomVisible = ref(false)
const bomProduct = ref<TProduct | null>(null)
const bomList = ref<ProductBom[]>([])
const materialOptions = ref<MaterialOption[]>([])
const openBom = async (row: TProduct) => {
  bomProduct.value = row
  bomList.value = []
  if (materialOptions.value.length === 0) {
    const m = await listMaterials()
    if (m.code === 200) materialOptions.value = m.data
  }
  const res = await getProductBom(row.productId!)
  if (res.code === 200) bomList.value = res.data || []
  if (bomList.value.length === 0) addBomRow()
  bomVisible.value = true
}
const addBomRow = () => bomList.value.push({ materialId: undefined, useNum: 1, lossRate: 0.05, unit: '' })
const onMaterialChange = (r: ProductBom, id: number) => {
  const m = materialOptions.value.find(x => x.id === id)
  if (m) { r.materialName = m.materialName; r.materialCode = m.materialCode; r.spec = m.spec; r.unit = m.unit }
}
const submitBom = async () => {
  const valid = bomList.value.filter(b => b.materialId)
  if (valid.length === 0) { ElMessage.warning('请至少选择一种物料'); return }
  valid.forEach((b, i) => b.sort = i + 1)
  await saveProductBom(bomProduct.value!.productId!, valid)
  ElMessage.success('BOM保存成功')
  bomVisible.value = false
}
</script>

<style scoped>
.sales-page { padding: 12px; }
.search-card :deep(.el-card__body) { padding: 12px 12px 0; }
.table-card { margin-top: 10px; }
.table-card :deep(.el-card__body) { padding: 12px; }
.pager { margin-top: 10px; display: flex; justify-content: flex-end; }
</style>
