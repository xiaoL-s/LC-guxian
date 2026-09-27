<template>
  <div class="print-template-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">打印模板管理</h2>
        <p class="page-desc">配置单据打印模板。编辑时可拖拽文本框/表格/二维码到任意位置，业务人员无需写代码。<br>
          <b>「生产工单打印」直接读取本页 <el-tag size="small" type="primary" effect="plain">WORK_ORDER</el-tag> 类型中"默认"且"启用"的那一条模板</b>，
          所以在这里改动后，生产管理 → 生产工单 → 「打印生产单」打出来的单子会同步变化，无需改代码。</p>
      </div>
      <el-button type="primary" @click="openDialog()">＋ 新增模板</el-button>
    </div>
    <!-- 筛选区 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="模板名称">
          <el-input v-model="searchForm.templateName" placeholder="请输入模板名称" clearable
                    style="width: 220px" @keyup.enter="loadTable" />
        </el-form-item>
        <el-form-item label="单据类型">
          <el-select v-model="searchForm.templateType" placeholder="全部" style="width: 160px">
            <el-option label="全部" value="" />
            <el-option v-for="t in typeOptions" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" style="width: 140px">
            <el-option label="全部" :value="''" />
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="templateName" label="模板名称" min-width="160" />
        <el-table-column prop="templateType" label="单据类型" width="120" />
        <el-table-column label="使用位置" min-width="260">
          <template #default="scope">
            <span :class="{ 'usage-active': scope.row.templateType === 'WORK_ORDER' && scope.row.isDefault === 1 }">
              {{ usageOf(scope.row.templateType, scope.row.isDefault) }}
            </span>
            <el-button v-if="scope.row.templateType === 'WORK_ORDER' && scope.row.isDefault !== 1"
              type="primary" link size="small" style="margin-left:6px" @click="setAsPrintTemplate(scope.row)">
              设为打印模板
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="paperSize" label="纸张大小" width="110">
          <template #default="scope">{{ scope.row.paperSize || '—' }}</template>
        </el-table-column>
        <el-table-column label="默认模板" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isDefault === 1" type="primary" effect="plain">默认</el-tag>
            <span v-else class="dash">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="success">启用</el-tag>
            <el-tag v-else type="info">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="openDialog(scope.row)">编辑</el-button>
            <el-button type="primary" link @click="openPreview(scope.row)">预览</el-button>
            <el-button v-if="scope.row.status === 1" type="warning" link @click="onToggleStatus(scope.row, 0)">禁用</el-button>
            <el-button v-else type="success" link @click="onToggleStatus(scope.row, 1)">启用</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 12px; justify-content: flex-end"
        @change="loadTable"
      />
    </el-card>

    <!-- ============ 全屏可拖拽编辑器 ============ -->
    <el-dialog v-model="dialogVisible" :title="(isEdit?'编辑':'新增')+'打印模板'" width="96%" top="2vh" :close-on-click-modal="false" destroy-on-close>
      <div class="editor-wrap">
        <!-- 顶部基础信息 -->
        <div class="editor-base">
          <el-form :inline="true" size="small">
            <el-form-item label="模板名称">
              <el-input v-model="form.templateName" placeholder="如：生产下料单" style="width:200px" />
            </el-form-item>
            <el-form-item label="单据类型">
              <el-select v-model="form.templateType" style="width:140px" filterable allow-create>
                <el-option v-for="t in typeOptions" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
            <el-form-item label="纸张">
              <el-select v-model="form.paperSize" style="width:130px">
                <el-option label="A4横向" value="A4_LANDSCAPE" />
                <el-option label="A4纵向" value="A4" />
                <el-option label="A5" value="A5" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="默认">
              <el-radio-group v-model="form.isDefault">
                <el-radio :value="1">是</el-radio>
                <el-radio :value="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </div>

        <div class="editor-main">
          <!-- 左侧组件库 -->
          <div class="lib-panel">
            <div class="lib-title">组件库</div>
            <div class="lib-item" @mousedown="startDragNew($event,'text')">
              <span class="lib-icon">T</span> 文本/字段
            </div>
            <div class="lib-item" @mousedown="startDragNew($event,'table')">
              <span class="lib-icon">▦</span> 明细表格
            </div>
            <div class="lib-item" @mousedown="startDragNew($event,'qrcode')">
              <span class="lib-icon">▣</span> 二维码
            </div>
            <div class="lib-item" @mousedown="startDragNew($event,'line')">
              <span class="lib-icon">―</span> 横线分隔
            </div>
            <el-divider>字段变量</el-divider>
            <div class="lib-var" v-for="v in fieldVars" :key="v.field"
                 @click="insertFieldVar(v)" :title="'点击插入到画布'">
              {{ v.label }}
            </div>
          </div>

          <!-- 中间画布 -->
          <div class="canvas-scroll" ref="canvasScrollRef">
            <div class="canvas" :class="{landscape: form.paperSize==='A4_LANDSCAPE'}" ref="canvasRef">
              <template v-for="b in blocks" :key="b.id">
                <!-- 文本块 -->
                <div v-if="b.type==='text'"
                     class="block block-text"
                     :class="{active: activeId===b.id}"
                     :style="{left:b.x+'px', top:b.y+'px', width:b.w+'px', fontSize:b.fontSize+'px', fontWeight:b.bold?'bold':'normal'}"
                     @mousedown="startMove($event,b)"
                     @click.stop="selectBlock(b)">
                  {{ previewText(b) }}
                  <span class="resize-handle" @mousedown.stop="startResize($event,b)"></span>
                </div>
                <!-- 横线 -->
                <div v-else-if="b.type==='line'"
                     class="block block-line"
                     :class="{active: activeId===b.id}"
                     :style="{left:b.x+'px', top:b.y+'px', width:b.w+'px'}"
                     @mousedown="startMove($event,b)"
                     @click.stop="selectBlock(b)">
                  <span class="resize-handle" @mousedown.stop="startResize($event,b)"></span>
                </div>
                <!-- 二维码 -->
                <div v-else-if="b.type==='qrcode'"
                     class="block block-qr"
                     :class="{active: activeId===b.id}"
                     :style="{left:b.x+'px', top:b.y+'px', width:b.w+'px', height:b.w+'px'}"
                     @mousedown="startMove($event,b)"
                     @click.stop="selectBlock(b)">
                  <div class="qr-box">QR</div>
                  <div class="qr-cap">{{ b.label||'二维码' }}</div>
                  <span class="resize-handle" @mousedown.stop="startResize($event,b)"></span>
                </div>
                <!-- 表格：与打印结果同一套表头分组规则（所见即所得） -->
                <div v-else-if="b.type==='table'"
                     class="block block-table"
                     :class="{active: activeId===b.id}"
                     :style="{left:b.x+'px', top:b.y+'px', width:b.w+'px'}"
                     @mousedown="startMove($event,b)"
                     @click.stop="selectBlock(b)">
                  <table class="mini-table">
                    <thead>
                      <tr v-for="(row, ri) in headRowsOf(b)" :key="ri">
                        <th v-for="(cell, ci) in row" :key="ci"
                            :colspan="cell.colspan || 1" :rowspan="cell.rowspan || 1">{{ cell.text }}</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="i in 2" :key="i"><td v-for="(c, ci) in colsOf(b)" :key="ci"> </td></tr>
                    </tbody>
                    <tfoot>
                      <tr>
                        <td v-for="(c, ci) in colsOf(b)" :key="ci"
                            :class="{ 'foot-label': ci === 0, 'foot-total': ci === totalColIndexOf(b) }">
                          {{ ci === 0 ? '合计：' : (ci === totalColIndexOf(b) ? '自动汇总' : '') }}
                        </td>
                      </tr>
                    </tfoot>
                  </table>
                  <span class="resize-handle" @mousedown.stop="startResize($event,b)"></span>
                </div>
              </template>
            </div>
          </div>

          <!-- 右侧属性面板 -->
          <div class="prop-panel">
            <div class="lib-title">属性</div>
            <template v-if="activeBlock">
              <el-form label-width="70px" size="small">
                <el-form-item label="类型">
                  <el-tag>{{ typeLabel(activeBlock.type) }}</el-tag>
                </el-form-item>
                <el-form-item v-if="activeBlock.type==='text'" label="显示文字">
                  <el-input v-model="activeBlock.label" placeholder="固定文字，如：客户名称" />
                </el-form-item>
                <el-form-item v-if="activeBlock.type==='text'" label="绑定字段">
                  <el-select v-model="activeBlock.field" placeholder="选业务字段" clearable style="width:100%">
                    <el-option v-for="v in fieldVars" :key="v.field" :label="v.label" :value="v.field" />
                  </el-select>
                </el-form-item>
                <el-form-item v-if="activeBlock.type==='qrcode'" label="标签">
                  <el-input v-model="activeBlock.label" />
                </el-form-item>
                <el-form-item v-if="activeBlock.type==='text'" label="字号">
                  <el-input-number v-model="activeBlock.fontSize" :min="8" :max="32" :step="1" size="small" />
                </el-form-item>
                <el-form-item v-if="activeBlock.type==='text'" label="加粗">
                  <el-switch v-model="activeBlock.bold" />
                </el-form-item>
                <el-form-item v-if="activeBlock.type==='table'" label="列(逗号分隔)">
                  <el-input v-model="activeBlock.colsText" type="textarea" :rows="4"
                            @change="onColsChange" placeholder="总宽×总高,数量,颜色,固定,外框宽×高..." />
                  <div class="col-hint">
                    自动分组：总宽×总高 →「原尺寸」；外框宽×高 / 内框宽×高 / 内扇宽×高 / 孔位 / 横杆 →「下料尺寸」；
                    上下纱宽×高 / 中纱宽×高 →「剪网尺寸」。合计行自动放在数量列。
                  </div>
                </el-form-item>
                <el-form-item label="X">
                  <el-input-number v-model="activeBlock.x" :min="0" :step="5" size="small" />
                </el-form-item>
                <el-form-item label="Y">
                  <el-input-number v-model="activeBlock.y" :min="0" :step="5" size="small" />
                </el-form-item>
                <el-form-item label="宽度">
                  <el-input-number v-model="activeBlock.w" :min="40" :step="10" size="small" />
                </el-form-item>
                <el-form-item>
                  <el-button type="danger" size="small" @click="deleteBlock(activeBlock)">删除区块</el-button>
                </el-form-item>
              </el-form>
            </template>
            <div v-else class="prop-empty">点击画布上的区块编辑属性</div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button @click="loadDefaultTemplate">载入默认生产单</el-button>
        <el-button type="primary" @click="submitForm">保存模板</el-button>
      </template>
    </el-dialog>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" title="模板预览" width="80%" top="5vh">
      <div class="preview-wrap">
        <div class="a4-sheet" v-html="renderedPreview"></div>
      </div>
      <template #footer>
        <el-button @click="previewVisible=false">关闭</el-button>
        <el-button type="primary" @click="doPrint">打印 / 导出PDF</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getPrintTemplatePage,
  savePrintTemplate,
  delPrintTemplate,
  togglePrintTemplateStatus,
  getPrintTemplateTypes
} from '@/api/system/printTemplate'
import { renderBlocksToHtml, buildDefaultBlocks, buildPrintContext, wrapPrintDocument, buildTableHeadRows, totalColIndex, DEFAULT_COLS, type PrintBlock } from '@/utils/cuttingSheetRenderer'

/* ===================== 列表 ===================== */
const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchForm = reactive({ templateName: '', templateType: '', status: '' as number|'' })
const typeOptions = ref<string[]>([])
const loadTypes = async () => {
  const res: any = await getPrintTemplateTypes()
  typeOptions.value = res.data || []
}
const loadTable = async () => {
  loading.value = true
  try {
    const res: any = await getPrintTemplatePage(pageNum.value, pageSize.value,
      searchForm.templateName, searchForm.templateType, searchForm.status)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}
const onSearch = () => { pageNum.value = 1; loadTable() }
/** 模板与业务入口的关联说明（与工单打印的取值规则保持一致） */
const usageOf = (type: string, isDefault: number) => {
  if (type === 'WORK_ORDER') {
    return isDefault === 1
      ? '生产工单 → 打印生产单（当前生效）'
      : '生产工单 → 打印生产单（备用，需在同类模板中设为默认）'
  }
  if (type === 'SALES_ORDER' || type === '销售订单') return '销售订单打印（预留）'
  return '未关联业务入口'
}
const resetSearch = () => {
  searchForm.templateName=''; searchForm.templateType=''; searchForm.status=''; pageNum.value=1; loadTable()
}

/* ===================== 业务字段 ===================== */
const fieldVars = [
  { field: 'workNo', label: '工单编号' },
  { field: 'orderNo', label: '订单编号' },
  { field: 'customerName', label: '客户名称' },
  { field: 'customerPhone', label: '联系电话' },
  { field: 'customerAddress', label: '客户地址' },
  { field: 'projectAddress', label: '工程地址' },
  { field: 'productSpec', label: '产品规格' },
  { field: 'seriesName', label: '产品系列' },
  { field: 'createTime', label: '开单日期' },
  { field: 'totalNum', label: '合计数量' },
  { field: 'totalArea', label: '总面积' },
  { field: 'finishTime', label: '完工日期' },
  { field: 'createUser', label: '制单人' },
]

/* ===================== 区块 ===================== */
const blocks = ref<PrintBlock[]>([])
const activeId = ref<number>(0)
let blockSeq = 1
const nextId = () => blockSeq++

const activeBlock = computed(() => blocks.value.find(b => b.id === activeId.value))
const selectBlock = (b: PrintBlock) => { activeId.value = b.id }
const typeLabel = (t: string) => ({text:'文本',table:'表格',qrcode:'二维码',line:'横线'} as any)[t]

/* 表格区块：列结构与打印完全一致（分组表头 + 合计行位置） */
const colsOf = (b: PrintBlock) => (b.cols && b.cols.length ? b.cols : DEFAULT_COLS)
const headRowsOf = (b: PrintBlock) => buildTableHeadRows(colsOf(b))
const totalColIndexOf = (b: PrintBlock) => totalColIndex(colsOf(b))

const previewText = (b: PrintBlock) => {
  if (b.field) {
    const f = fieldVars.find(v=>v.field===b.field)
    return f ? f.label : ('${'+b.field+'}')
  }
  return b.label || '文本'
}

/* ---- 拖拽移动 ---- */
let dragCtx: any = null
const canvasRef = ref<HTMLElement>()
const startMove = (e: MouseEvent, b: PrintBlock) => {
  activeId.value = b.id
  const rect = canvasRef.value!.getBoundingClientRect()
  dragCtx = { mode:'move', id:b.id, dx:e.clientX-rect.left-b.x, dy:e.clientY-rect.top-b.y }
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}
const startResize = (e: MouseEvent, b: PrintBlock) => {
  dragCtx = { mode:'resize', id:b.id, startW:b.w, startX:e.clientX }
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}
const onDrag = (e: MouseEvent) => {
  if (!dragCtx) return
  const b = blocks.value.find(x=>x.id===dragCtx.id); if(!b) return
  const rect = canvasRef.value!.getBoundingClientRect()
  if (dragCtx.mode==='move') {
    b.x = Math.max(0, Math.round(e.clientX-rect.left-dragCtx.dx))
    b.y = Math.max(0, Math.round(e.clientY-rect.top-dragCtx.dy))
  } else {
    b.w = Math.max(40, Math.round(dragCtx.startW + (e.clientX-dragCtx.startX)))
  }
}
const stopDrag = () => {
  dragCtx = null
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

/* ---- 从左侧组件库拖新组件 ---- */
const startDragNew = (e: MouseEvent, type: string) => {
  // 鼠标按下即开始在画布相对位置预览（简单实现：松开时放到画布中心）
  const moveHandler = (ev: MouseEvent) => {
    const rect = canvasRef.value!.getBoundingClientRect()
    const x = Math.max(0, Math.round(ev.clientX-rect.left-40))
    const y = Math.max(0, Math.round(ev.clientY-rect.top-15))
    addBlockAt(type, x, y)
    cleanup()
  }
  const cleanup = () => {
    document.removeEventListener('mousemove', moveHandler)
    document.removeEventListener('mouseup', cleanup)
  }
  document.addEventListener('mousemove', moveHandler)
  document.addEventListener('mouseup', cleanup)
}

const addBlockAt = (type: string, x: number, y: number) => {
  const id = nextId()
  const base: PrintBlock = { id, type, x, y, w: 200 }
  if (type==='text') Object.assign(base, {label:'新文本', fontSize:13, bold:false})
  if (type==='qrcode') Object.assign(base, {label:'二维码', w:90})
  if (type==='line') Object.assign(base, {w:500})
  if (type==='table') {
    Object.assign(base, {w:900, cols:['原尺寸','数量','颜色','外框宽×高','纱网','备注'],
      colsText:'原尺寸,数量,颜色,外框宽×高,纱网,备注'})
  }
  blocks.value.push(base)
  activeId.value = id
}

const insertFieldVar = (v: {field:string,label:string}) => {
  const id = nextId()
  blocks.value.push({ id, type:'text', x:40, y: nextId()*26, w:140, fontSize:13, label:v.label+'：', field:v.field, bold:false })
  activeId.value = id
}

const onColsChange = () => {
  if (activeBlock.value && activeBlock.value.colsText !== undefined) {
    activeBlock.value.cols = activeBlock.value.colsText.split(/[,，]/).map(s=>s.trim()).filter(Boolean)
  }
}
const deleteBlock = (b: PrintBlock) => {
  blocks.value = blocks.value.filter(x=>x.id!==b.id)
  activeId.value = 0
}

/* ===================== 默认生产单模板 =====================
 * 直接使用与「生产工单 → 打印生产单」完全相同的内置版式（单一数据源），
 * 保证模板管理里看到的、工单打出来的完全一致。
 * ======================================================== */
const loadDefaultTemplate = () => {
  blocks.value = buildDefaultBlocks().map(b => ({ ...b }))
  blockSeq = Math.max(...blocks.value.map(b => b.id)) + 1
  ElMessage.success('已载入默认生产单模板，可拖拽调整')
}

/* ===================== 新增/编辑 ===================== */
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({
  templateId: null as number|null,
  templateName: '', templateType: 'WORK_ORDER',
  paperSize: 'A4_LANDSCAPE', isDefault: 0, status: 1, templateContent: ''
})
const canvasScrollRef = ref<HTMLElement>()

/** 该单据类型下是否已有"默认且启用"的模板（工单打印只取这一条） */
const hasDefaultForType = async (type: string) => {
  try {
    const res: any = await getPrintTemplatePage(1, 200, '', type, 1)
    return (res.data?.records || []).some((t: any) => t.isDefault === 1)
  } catch { return false }
}

const openDialog = async (row?: any) => {
  dialogVisible.value = true
  if (row) {
    isEdit.value = true
    Object.assign(form, {
      templateId: row.templateId,
      templateName: row.templateName,
      templateType: row.templateType,
      paperSize: row.paperSize === 'A4' ? 'A4' : 'A4_LANDSCAPE',
      isDefault: row.isDefault===1?1:0,
      status: row.status===1?1:0
    })
    // 解析已有 blocks JSON
    try {
      const parsed = JSON.parse(row.templateContent || '[]')
      if (Array.isArray(parsed) && parsed.length) {
        blocks.value = parsed
        blockSeq = Math.max(...parsed.map((b:any)=>b.id)) + 1
      } else {
        // 历史遗留的整段 HTML 模板：载入时可选择转为新版式（保存后即覆盖）
        loadDefaultTemplate()
        ElMessage.info('该模板是旧版 HTML 格式，已载入新版生产单版式，保存后即替换')
      }
    } catch { loadDefaultTemplate() }
  } else {
    isEdit.value = false
    // 该类型下还没有默认模板时，新增的模板自动作为默认（否则工单打印取不到它）
    const exists = await hasDefaultForType('WORK_ORDER')
    Object.assign(form, { templateId:null, templateName:'', templateType:'WORK_ORDER',
      paperSize:'A4_LANDSCAPE', isDefault: exists ? 0 : 1, status:1 })
    loadDefaultTemplate()
  }
  activeId.value = 0
}

/** 一键设为工单打印模板 */
const setAsPrintTemplate = async (row: any) => {
  const res: any = await savePrintTemplate({ ...row, isDefault: 1, status: 1 })
  if (res.code === 200) {
    ElMessage.success(`已将「${row.templateName}」设为工单打印模板（同类其它模板自动取消默认）`)
    loadTable()
  }
}

/* 保存：blocks 序列化成 JSON 存进 templateContent */
const submitForm = async () => {
  if (!form.templateName) { ElMessage.warning('请填模板名称'); return }
  form.templateContent = JSON.stringify(blocks.value)
  const res: any = await savePrintTemplate(form)
  if (res.code === 200) {
    const effective = form.templateType === 'WORK_ORDER' && form.isDefault === 1 && form.status === 1
    ElMessage.success(effective
      ? '保存成功，生产工单「打印生产单」将使用该模板'
      : '保存成功；如需工单打印使用它，请在列表中点「设为打印模板」')
    dialogVisible.value = false
    loadTable()
  }
}

/* ===================== 启停删除 ===================== */
// 注意：本地包装函数不能与导入的接口同名，否则会自己调自己造成无限递归
const onToggleStatus = async (row: any, status: number) => {
  const action = status === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(`确定${action}模板「${row.templateName}」？`, '提示', { type: 'warning' })
  const res: any = await togglePrintTemplateStatus(row.templateId, status)
  if (res.code === 200) { ElMessage.success(`${action}成功`); loadTable() }
}
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定删除模板「${row.templateName}」？`,'提示',{type:'warning'})
    .then(async () => { await delPrintTemplate(row.templateId); ElMessage.success('删除成功'); loadTable() })
    .catch(()=>{})
}

/* ===================== 预览/打印 ===================== */
const previewVisible = ref(false)
const previewRow = ref<any>({})
// 预览用模拟数据：结构与工单详情一致，字段名由 buildPrintContext 统一生成，
// 保证"模板预览"与"工单实际打印"用同一套字段
const mockDetail = {
  workNo: 'WO20260922003', orderNo: '202609191520', customerName: '刘家成',
  customerPhone: '13800000000', customerAddress: '发货',
  expectDate: '2026-09-26', orderDate: '2026-09-19', createTime: '2026-09-19T10:00:00',
  totalArea: 3.05, shelfName: 'A区-01',
  orderItems: [
    { width: 645, height: 1300, num: 2, color: '金属灰', fixedBottom: '450', netMaterial: '柔性金刚网', handle: '全防护', handleDirection: '左2右', productName: '8K大料三节', itemCategory: '8K三节系列' },
    { width: 655, height: 1300, num: 4, color: '金属灰', fixedBottom: '450', netMaterial: '柔性金刚网', handle: '全防护', handleDirection: '左2右', productName: '8K大料三节', itemCategory: '8K三节系列' }
  ]
}
const mockCtx = buildPrintContext(mockDetail)

const renderedPreview = computed(() => {
  let list: PrintBlock[] = []
  try { list = JSON.parse(previewRow.value.templateContent || '[]') } catch {}
  if (!Array.isArray(list) || !list.length) list = blocks.value
  return renderBlocksToHtml(list, mockCtx)
})

const openPreview = (row: any) => { previewRow.value = row; previewVisible.value = true }
const doPrint = () => {
  const w = window.open('', '_blank')
  if (!w) { ElMessage.warning('请允许弹窗'); return }
  w.document.write(wrapPrintDocument(renderedPreview.value, previewRow.value.templateName || '生产单'))
  w.document.close()
  setTimeout(() => w.print(), 300)
}

onMounted(() => { loadTypes(); loadTable() })
</script>

<style scoped>
.print-template-page { padding: 16px; }
.page-header { display:flex; justify-content:space-between; margin-bottom:12px; }
.page-title { margin:0 0 6px; font-size:20px; font-weight:600; }
.page-desc { margin:0; color:#909399; font-size:13px; }
.search-card { margin-bottom:12px; }
.dash { color:#c0c4cc; }
.usage-active { color:#67c23a; font-weight:600; }
.col-hint { font-size:11px; color:#909399; line-height:1.6; margin-top:4px; }

/* 编辑器 */
.editor-wrap { display:flex; flex-direction:column; height:78vh; }
.editor-base { border-bottom:1px solid #ebeef5; padding-bottom:8px; }
.editor-main { display:flex; flex:1; margin-top:8px; overflow:hidden; }
.lib-panel { width:150px; border-right:1px solid #ebeef5; padding:8px; overflow:auto; background:#fafafa; }
.lib-title { font-size:13px; font-weight:600; color:#303133; margin-bottom:8px; }
.lib-item { padding:8px; border:1px solid #dcdfe6; border-radius:4px; margin-bottom:6px; cursor:grab; background:#fff; font-size:13px; text-align:center; }
.lib-item:active { cursor:grabbing; }
.lib-icon { margin-right:4px; color:#1989fa; }
.lib-var { font-size:12px; padding:4px 8px; background:#ecf5ff; border-radius:3px; margin-bottom:4px; cursor:pointer; color:#1989fa; }
.lib-var:hover { background:#d9ecff; }

.canvas-scroll { flex:1; overflow:auto; background:#e9ecef; padding:20px; }
.canvas { position:relative; background:#fff; margin:0 auto; box-shadow:0 2px 12px rgba(0,0,0,.15); }
.canvas.landscape { width:1080px; height:620px; }
.canvas:not(.landscape) { width:780px; height:1080px; }

.block { position:absolute; cursor:move; font-size:13px; }
.block-text { padding:2px 4px; }
.block.active { outline:1px dashed #1989fa; background:rgba(25,137,250,.05); }
.block-line { border-top:1px solid #333; }
.block-qr { text-align:center; font-size:11px; }
.qr-box { width:100%; height:100%; border:1px dashed #999; display:flex; align-items:center; justify-content:center; color:#999; }
.block-table .mini-table { border-collapse:collapse; width:100%; }
.block-table .mini-table th, .block-table .mini-table td { border:1px solid #666; padding:3px; font-size:11px; }
.block-table .mini-table .foot-label { text-align:left; }
.block-table .mini-table .foot-total { text-align:center; color:#909399; }
.resize-handle { position:absolute; right:-4px; bottom:-4px; width:10px; height:10px; background:#1989fa; cursor:nwse-resize; }

.prop-panel { width:220px; border-left:1px solid #ebeef5; padding:8px; overflow:auto; background:#fafafa; }
.prop-empty { color:#909399; font-size:12px; text-align:center; margin-top:30px; }

.preview-wrap { background:#f5f7fa; padding:16px; overflow:auto; max-height:80vh; }
.a4-sheet { background:#fff; margin:0 auto; box-shadow:0 2px 12px rgba(0,0,0,.12); overflow:auto; }
</style>

