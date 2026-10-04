<template>
  <div class="print-template-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">打印模板管理</h2>
        <p class="page-desc">类 NCReport 可视化报表设计器：拖拽组件、自由调整位置与尺寸，业务人员无需写代码。<br>
          <b>「生产工单打印」直接读取本页 <el-tag size="small" type="primary" effect="plain">WORK_ORDER</el-tag> 类型中"默认"且"启用"的那一条模板</b>，
          改动后生产管理 → 生产工单 → 「打印生产单」同步生效。</p>
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
    <!-- 列表表格 -->
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
          <template #default="scope">{{ paperLabel(scope.row.paperSize) }}</template>
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

    <!-- ============ NCReport 风格全屏设计器 ============ -->
    <el-dialog v-model="dialogVisible" :title="(isEdit?'编辑':'新增')+'打印模板'" width="97%" top="1vh"
               :close-on-click-modal="false" destroy-on-close class="designer-dialog">
      <div class="designer">
        <!-- 顶部：模板基础信息 -->
        <div class="designer-base">
          <el-form :inline="true" size="small">
            <el-form-item label="模板名称">
              <el-input v-model="form.templateName" placeholder="如：生产下料单" style="width:190px" />
            </el-form-item>
            <el-form-item label="单据类型">
              <el-select v-model="form.templateType" style="width:130px" filterable allow-create @change="onTypeChange">
                <el-option v-for="t in typeOptions" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
            <el-form-item label="纸张">
              <el-select v-model="form.paperSize" style="width:120px">
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

        <!-- 工具栏 -->
        <div class="designer-toolbar">
          <div class="tb-group">
            <el-tooltip content="撤销 (Ctrl+Z)" placement="bottom">
              <el-button size="small" text :disabled="!undoStack.length" @click="undo">↶ 撤销</el-button>
            </el-tooltip>
            <el-tooltip content="重做 (Ctrl+Y)" placement="bottom">
              <el-button size="small" text :disabled="!redoStack.length" @click="redo">↷ 重做</el-button>
            </el-tooltip>
          </div>
          <el-divider direction="vertical" />
          <div class="tb-group">
            <el-button size="small" text :disabled="!hasSelection" @click="copyBlocks">⧉ 复制</el-button>
            <el-button size="small" text :disabled="!copyBuffer" @click="pasteBlocks">📋 粘贴</el-button>
            <el-button size="small" text :disabled="!hasSelection" @click="deleteSelected">🗑 删除</el-button>
          </div>
          <el-divider direction="vertical" />
          <div class="tb-group">
            <el-button size="small" text :disabled="!hasSelection" @click="zMove('down')">下移</el-button>
            <el-button size="small" text :disabled="!hasSelection" @click="zMove('up')">上移</el-button>
          </div>
          <el-divider direction="vertical" />
          <div class="tb-group">
            <el-button size="small" text :disabled="!hasSelection" @click="align('left')">⇤左</el-button>
            <el-button size="small" text :disabled="!hasSelection" @click="align('hcenter')">⇹水平居中</el-button>
            <el-button size="small" text :disabled="!hasSelection" @click="align('right')">右⇥</el-button>
            <el-button size="small" text :disabled="!hasSelection" @click="align('top')">⇧上</el-button>
            <el-button size="small" text :disabled="!hasSelection" @click="align('vcenter')">⇳垂直居中</el-button>
            <el-button size="small" text :disabled="!hasSelection" @click="align('bottom')">下⇩</el-button>
          </div>
          <el-divider direction="vertical" />
          <div class="tb-group">
            <el-button size="small" text :disabled="selectedIds.length < 2" @click="sameSize('w')">等宽</el-button>
            <el-button size="small" text :disabled="selectedIds.length < 2" @click="sameSize('h')">等高</el-button>
          </div>
          <el-divider direction="vertical" />
          <div class="tb-group">
            <el-button size="small" text @click="zoom(-0.25)">－</el-button>
            <span class="zoom-val">{{ Math.round(canvasScale * 100) }}%</span>
            <el-button size="small" text @click="zoom(0.25)">＋</el-button>
            <el-button size="small" text @click="canvasScale = 1">1:1</el-button>
          </div>
          <el-divider direction="vertical" />
          <div class="tb-group">
            <el-button size="small" :type="showGrid ? 'primary' : 'default'" @click="showGrid = !showGrid">网格</el-button>
          </div>
        </div>

        <div class="designer-main">
          <!-- 左侧组件库 -->
          <div class="lib-panel">
            <div class="lib-title">组件库</div>
            <div class="lib-item" @click="addBlockAt('text')"><span class="lib-icon">T</span> 文本/字段</div>
            <div class="lib-item" @click="addBlockAt('table')"><span class="lib-icon">▦</span> 明细表格</div>
            <div class="lib-item" @click="addBlockAt('qrcode')"><span class="lib-icon">▣</span> 二维码</div>
            <div class="lib-item" @click="addBlockAt('line')"><span class="lib-icon">―</span> 横线分隔</div>
            <el-divider>字段变量</el-divider>
            <div class="lib-var" v-for="v in fieldVars" :key="v.field" @click="insertFieldVar(v)"
                 :title="'点击插入到画布'">{{ v.label }}</div>
          </div>

          <!-- 中间画布 -->
          <div class="canvas-scroll" ref="canvasScrollRef" tabindex="0"
               @click="onCanvasClick" @keydown.stop>
            <div class="canvas-wrap" :style="{ width: canvasW * canvasScale + 'px', height: canvasH * canvasScale + 'px' }">
              <div class="canvas" :class="{ landscape: form.paperSize==='A4_LANDSCAPE', 'grid-on': showGrid }"
                   :style="{ transform: 'scale(' + canvasScale + ')', transformOrigin: 'top left' }" ref="canvasRef"
                   @mousedown.self="clearSelection">
                <template v-for="b in blocks" :key="b.id">
                  <!-- 文本块 -->
                  <div v-if="b.type==='text'"
                       class="block block-text" :class="{ active: isSelected(b.id) }"
                       :style="{ left: b.x+'px', top: b.y+'px', width: b.w+'px', height: b.h ? b.h+'px' : 'auto',
                                 fontSize: b.fontSize+'px', fontWeight: b.bold ? 'bold' : 'normal',
                                 textAlign: b.textAlign || 'left' }"
                       @mousedown.stop="startMove($event, b)" @click.stop="selectBlock($event, b)">
                    {{ previewText(b) }}
                    <template v-if="isSoloSelected(b.id)">
                      <span v-for="h in handles" :key="h" class="rh" :class="'rh-'+h"
                            @mousedown.stop="startResize($event, b, h)"></span>
                    </template>
                  </div>
                  <!-- 横线 -->
                  <div v-else-if="b.type==='line'"
                       class="block block-line" :class="{ active: isSelected(b.id) }"
                       :style="{ left: b.x+'px', top: b.y+'px', width: b.w+'px', height: (b.h || 1)+'px' }"
                       @mousedown.stop="startMove($event, b)" @click.stop="selectBlock($event, b)">
                    <template v-if="isSoloSelected(b.id)">
                      <span v-for="h in handles" :key="h" class="rh" :class="'rh-'+h"
                            @mousedown.stop="startResize($event, b, h)"></span>
                    </template>
                  </div>
                  <!-- 二维码 -->
                  <div v-else-if="b.type==='qrcode'"
                       class="block block-qr" :class="{ active: isSelected(b.id) }"
                       :style="{ left: b.x+'px', top: b.y+'px', width: b.w+'px', height: (b.h || b.w)+'px' }"
                       @mousedown.stop="startMove($event, b)" @click.stop="selectBlock($event, b)">
                    <div class="qr-box">QR</div>
                    <div class="qr-cap">{{ b.label || '二维码' }}</div>
                    <template v-if="isSoloSelected(b.id)">
                      <span v-for="h in handles" :key="h" class="rh" :class="'rh-'+h"
                            @mousedown.stop="startResize($event, b, h)"></span>
                    </template>
                  </div>
                  <!-- 表格 -->
                  <div v-else-if="b.type==='table'"
                       class="block block-table" :class="{ active: isSelected(b.id) }"
                       :style="{ left: b.x+'px', top: b.y+'px', width: b.w+'px', height: b.h ? b.h+'px' : 'auto' }"
                       @mousedown.stop="startMove($event, b)" @click.stop="selectBlock($event, b)">
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
                    <span v-if="b.dynamic" class="dynamic-badge">动态列</span>
                    <template v-if="isSoloSelected(b.id)">
                      <span v-for="h in handles" :key="h" class="rh" :class="'rh-'+h"
                            @mousedown.stop="startResize($event, b, h)"></span>
                    </template>
                  </div>
                </template>
              </div>
            </div>
          </div>

          <!-- 右侧属性面板 -->
          <div class="prop-panel">
            <div class="lib-title">属性</div>
            <template v-if="selectedIds.length > 1">
              <div class="prop-multi">已选 {{ selectedIds.length }} 个区块</div>
              <el-button size="small" type="primary" style="width:100%;margin-top:8px" @click="align('left')">左对齐</el-button>
              <el-button size="small" style="width:100%;margin-top:6px" @click="align('hcenter')">水平居中</el-button>
              <el-button size="small" style="width:100%;margin-top:6px" @click="align('right')">右对齐</el-button>
              <el-button size="small" style="width:100%;margin-top:6px" @click="align('top')">上对齐</el-button>
              <el-button size="small" style="width:100%;margin-top:6px" @click="align('vcenter')">垂直居中</el-button>
              <el-button size="small" style="width:100%;margin-top:6px" @click="align('bottom')">下对齐</el-button>
              <el-button size="small" style="width:100%;margin-top:6px" @click="sameSize('w')">统一宽度</el-button>
              <el-button size="small" style="width:100%;margin-top:6px" @click="sameSize('h')">统一高度</el-button>
              <el-button size="small" type="danger" style="width:100%;margin-top:12px" @click="deleteSelected">删除选中</el-button>
            </template>
            <template v-else-if="activeBlock">
              <el-form label-width="70px" size="small">
                <el-form-item label="类型"><el-tag>{{ typeLabel(activeBlock.type) }}</el-tag></el-form-item>
                <el-form-item v-if="activeBlock.type==='text'" label="显示文字">
                  <el-input v-model="propLabel" placeholder="固定文字，如：客户名称" />
                </el-form-item>
                <el-form-item v-if="activeBlock.type==='text'" label="绑定字段">
                  <el-select v-model="propField" placeholder="选业务字段" clearable style="width:100%">
                    <el-option v-for="v in fieldVars" :key="v.field" :label="v.label" :value="v.field" />
                  </el-select>
                </el-form-item>
                <el-form-item v-if="activeBlock.type==='qrcode'" label="标签">
                  <el-input v-model="propLabel" />
                </el-form-item>
                <el-form-item v-if="activeBlock.type==='text'" label="字号">
                  <el-input-number v-model="propFontSize" :min="8" :max="48" :step="1" size="small" style="width:100%" />
                </el-form-item>
                <el-form-item v-if="activeBlock.type==='text'" label="加粗">
                  <el-switch v-model="propBold" />
                </el-form-item>
                <el-form-item v-if="activeBlock.type==='text'" label="对齐">
                  <el-radio-group v-model="propTextAlign" size="small">
                    <el-radio-button value="left">左</el-radio-button>
                    <el-radio-button value="center">中</el-radio-button>
                    <el-radio-button value="right">右</el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <el-form-item v-if="activeBlock.type==='table'" label="动态列">
                  <el-switch v-model="propDynamic" />
                  <div class="col-hint">开启：打印时按后端数据自动展开列（序号/品名/宽/高/颜色/纱网/下固/面积/数量/单价/金额/把手/备注）</div>
                </el-form-item>
                <el-form-item v-if="activeBlock.type==='table' && !activeBlock.dynamic" label="列(逗号分隔)">
                  <el-input v-model="propColsText" type="textarea" :rows="5" @change="onColsChange"
                            placeholder="总宽×总高,数量,颜色,固定,外框宽×高..." />
                  <div class="col-hint">自动分组：总宽×总高→原尺寸；外框/内框/内扇/孔位/横杆→下料尺寸；上下纱/中纱→剪网尺寸。</div>
                </el-form-item>
                <el-form-item label="X"><el-input-number v-model="propX" :min="0" :step="5" size="small" style="width:100%" /></el-form-item>
                <el-form-item label="Y"><el-input-number v-model="propY" :min="0" :step="5" size="small" style="width:100%" /></el-form-item>
                <el-form-item label="宽度"><el-input-number v-model="propW" :min="20" :step="10" size="small" style="width:100%" /></el-form-item>
                <el-form-item label="高度">
                  <el-input-number v-model="propH" :min="activeBlock.type==='line'?1:20" :step="10" size="small" style="width:100%" />
                </el-form-item>
                <el-form-item>
                  <el-button type="danger" size="small" @click="deleteSelected">删除区块</el-button>
                </el-form-item>
              </el-form>
            </template>
            <div v-else class="prop-empty">
              点击画布上的区块编辑属性<br>
              <span class="prop-tip">Ctrl+点击 多选<br>方向键 微调 · Delete 删除<br>Ctrl+Z 撤销 · Ctrl+C/V 复制粘贴</span>
            </div>
          </div>
        </div>

        <!-- 状态栏 -->
        <div class="designer-status">
          <span v-if="activeBlock">X:{{ activeBlock.x }} Y:{{ activeBlock.y }} W:{{ activeBlock.w }} H:{{ activeBlock.h || '自动' }}</span>
          <span v-else>未选中区块</span>
          <span class="st-right">纸张 {{ paperLabel(form.paperSize) }} · {{ Math.round(canvasScale*100) }}%</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button @click="loadDefaultTemplate">载入生产单模板</el-button>
        <el-button @click="loadSalesTemplate">载入销售单模板</el-button>
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
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getPrintTemplatePage,
  savePrintTemplate,
  delPrintTemplate,
  togglePrintTemplateStatus,
  getPrintTemplateTypes
} from '@/api/system/printTemplate'
import { renderBlocksToHtml, buildDefaultBlocks, buildSalesDefaultBlocks, buildPrintContext, wrapPrintDocument, buildTableHeadRows, totalColIndex, DEFAULT_COLS, type PrintBlock } from '@/utils/cuttingSheetRenderer'

/* ===================== 列表 ===================== */
const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchForm = reactive({ templateName: '', templateType: '', status: '' as number | '' })
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
const resetSearch = () => {
  searchForm.templateName = ''; searchForm.templateType = ''; searchForm.status = ''; pageNum.value = 1; loadTable()
}
const paperLabel = (p?: string) =>
  p === 'A4' ? 'A4纵向' : p === 'A4_LANDSCAPE' ? 'A4横向' : p === 'A5' ? 'A5' : (p || '—')
const usageOf = (type: string, isDefault: number) => {
  if (type === 'WORK_ORDER') {
    return isDefault === 1 ? '生产工单 → 打印生产单（当前生效）' : '生产工单 → 打印生产单（备用，需设为默认）'
  }
  if (type === 'SALES_ORDER' || type === '销售订单') return '销售订单打印（预留）'
  return '未关联业务入口'
}

/* ===================== 业务字段 ===================== */
const fieldVars = [
  { field: 'workNo', label: '工单编号' },
  { field: 'orderNo', label: '订单编号' },
  { field: 'customerName', label: '客户名称' },
  { field: 'customerPhone', label: '联系电话' },
  { field: 'customerAddress', label: '客户地址' },
  { field: 'projectAddress', label: '工程地址' },
  { field: 'terminalAddress', label: '终端地址' },
  { field: 'productSpec', label: '产品规格' },
  { field: 'seriesName', label: '产品系列' },
  { field: 'createTime', label: '开单日期' },
  { field: 'totalNum', label: '合计数量' },
  { field: 'totalArea', label: '总面积' },
  { field: 'totalAmount', label: '合计金额' },
  { field: 'amountInWords', label: '金额大写' },
  { field: 'deposit', label: '定金' },
  { field: 'balance', label: '尾款' },
  { field: 'finishTime', label: '完工日期' },
  { field: 'createUser', label: '制单人' }
]

/* ===================== 画布 ===================== */
const canvasRef = ref<HTMLElement>()
const canvasScrollRef = ref<HTMLElement>()
const showGrid = ref(true)
const canvasScale = ref(1)
const canvasW = computed(() => form.paperSize === 'A4_LANDSCAPE' ? 1080 : 780)
const canvasH = computed(() => form.paperSize === 'A4_LANDSCAPE' ? 620 : 1080)
const zoom = (d: number) => {
  canvasScale.value = Math.min(2, Math.max(0.4, Math.round((canvasScale.value + d) * 100) / 100))
}

/* ===================== 区块与多选 ===================== */
const blocks = ref<PrintBlock[]>([])
const selectedIds = ref<number[]>([])
const activeId = computed<number>(() => selectedIds.value.length ? selectedIds.value[selectedIds.value.length - 1] : 0)
const activeBlock = computed(() => blocks.value.find(b => b.id === activeId.value))
const hasSelection = computed(() => selectedIds.value.length > 0)
const isSelected = (id: number) => selectedIds.value.includes(id)
const isSoloSelected = (id: number) => selectedIds.value.length === 1 && selectedIds.value.includes(id)
const handles = ['nw', 'n', 'ne', 'e', 'se', 's', 'sw', 'w']

let blockSeq = 1
const nextId = () => blockSeq++
const typeLabel = (t: string) => ({ text: '文本', table: '表格', qrcode: '二维码', line: '横线' } as any)[t]

const selectBlock = (e: MouseEvent, b: PrintBlock) => {
  if (e.ctrlKey || e.metaKey) {
    selectedIds.value = selectedIds.value.includes(b.id)
      ? selectedIds.value.filter(id => id !== b.id)
      : [...selectedIds.value, b.id]
  } else {
    selectedIds.value = [b.id]
  }
}
const clearSelection = () => { selectedIds.value = [] }
const onCanvasClick = () => {
  if (selectedIds.value.length) canvasScrollRef.value?.focus()
}
const validSelection = () => {
  selectedIds.value = selectedIds.value.filter(id => blocks.value.some(b => b.id === id))
}
const selectBlockById = (id: number) => { selectedIds.value = [id]; validSelection() }

/* 表格辅助 */
const colsOf = (b: PrintBlock) => (b.cols && b.cols.length ? b.cols : DEFAULT_COLS)
const headRowsOf = (b: PrintBlock) => buildTableHeadRows(colsOf(b))
const totalColIndexOf = (b: PrintBlock) => totalColIndex(colsOf(b))
const previewText = (b: PrintBlock) => {
  if (b.field) {
    const f = fieldVars.find(v => v.field === b.field)
    return f ? f.label : ('${' + b.field + '}')
  }
  return b.label || '文本'
}

/* ===================== 历史（撤销/重做） ===================== */
const undoStack = ref<string[]>([])
const redoStack = ref<string[]>([])
const historyLock = ref(false)
const snapshot = () => JSON.stringify(blocks.value)
/** 变更前调用：把当前状态压入撤销栈 */
const commitBefore = () => {
  if (historyLock.value) return
  undoStack.value.push(snapshot())
  if (undoStack.value.length > 100) undoStack.value.shift()
  redoStack.value = []
}
const applySnapshot = (s: string) => {
  historyLock.value = true
  const parsed = JSON.parse(s)
  blocks.value = parsed
  selectedIds.value = []
  historyLock.value = false
}
const undo = () => {
  if (!undoStack.value.length) return
  redoStack.value.push(snapshot())
  applySnapshot(undoStack.value.pop()!)
}
const redo = () => {
  if (!redoStack.value.length) return
  undoStack.value.push(snapshot())
  applySnapshot(redoStack.value.pop()!)
}

/* ===================== 拖拽移动/缩放（8 方向手柄） ===================== */
let dragCtx: any = null
const startMove = (e: MouseEvent, b: PrintBlock) => {
  selectBlock(e, b)
  const rect = canvasRef.value!.getBoundingClientRect()
  commitBefore()
  dragCtx = { mode: 'move', id: b.id, dx: e.clientX - rect.left - b.x * canvasScale.value, dy: e.clientY - rect.top - b.y * canvasScale.value }
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}
const startResize = (e: MouseEvent, b: PrintBlock, dir: string) => {
  commitBefore()
  dragCtx = {
    mode: 'resize', id: b.id, dir,
    sx: e.clientX, sy: e.clientY,
    bx: b.x, by: b.y, bw: b.w, bh: b.h ?? 0
  }
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}
const onDrag = (e: MouseEvent) => {
  if (!dragCtx) return
  const b = blocks.value.find(x => x.id === dragCtx.id)
  if (!b) return
  const dx = (e.clientX - dragCtx.sx) / canvasScale.value
  const dy = (e.clientY - dragCtx.sy) / canvasScale.value
  const minW = 20, minH = b.type === 'line' ? 1 : 20
  if (dragCtx.mode === 'move') {
    const rect = canvasRef.value!.getBoundingClientRect()
    b.x = Math.max(0, Math.round((e.clientX - rect.left) / canvasScale.value - dragCtx.dx))
    b.y = Math.max(0, Math.round((e.clientY - rect.top) / canvasScale.value - dragCtx.dy))
  } else {
    const dir = dragCtx.dir, w = dragCtx.bw, h = dragCtx.bh, x0 = dragCtx.bx, y0 = dragCtx.by
    let nw = w, nh = h, nx = x0, ny = y0
    if (dir.includes('e')) nw = Math.max(minW, w + dx)
    if (dir.includes('s')) nh = Math.max(minH, h + dy)
    if (dir.includes('w')) { nw = Math.max(minW, w - dx); nx = x0 + (w - nw) }
    if (dir.includes('n')) { nh = Math.max(minH, h - dy); ny = y0 + (h - nh) }
    b.w = Math.round(nw); b.h = Math.round(nh); b.x = Math.round(nx); b.y = Math.round(ny)
  }
}
const stopDrag = () => {
  dragCtx = null
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

/* ===================== 增删/复制粘贴/层级 ===================== */
let addOffset = 0
const addBlockAt = (type: string) => {
  commitBefore()
  const id = nextId()
  const x = 30 + (addOffset % 5) * 24
  const y = 30 + (addOffset % 8) * 28
  addOffset++
  const base: PrintBlock = { id, type, x, y, w: 200 }
  if (type === 'text') Object.assign(base, { label: '新文本', fontSize: 13, bold: false, h: 24, textAlign: 'left' })
  if (type === 'qrcode') Object.assign(base, { label: '二维码', w: 90, h: 90 })
  if (type === 'line') Object.assign(base, { w: 500, h: 1 })
  if (type === 'table') Object.assign(base, {
    w: 900, h: 260, dynamic: true, cols: ['序号'], colsText: '序号'
  })
  blocks.value.push(base)
  selectBlockById(id)
}
const insertFieldVar = (v: { field: string, label: string }) => {
  commitBefore()
  const id = nextId()
  blocks.value.push({ id, type: 'text', x: 40, y: 40 + (nextId() % 20) * 26, w: 140, h: 24, fontSize: 13, label: v.label + '：', field: v.field, bold: false })
  selectBlockById(id)
}
const deleteSelected = () => {
  if (!selectedIds.value.length) return
  commitBefore()
  blocks.value = blocks.value.filter(b => !selectedIds.value.includes(b.id))
  selectedIds.value = []
}
const onColsChange = () => {
  if (activeBlock.value && activeBlock.value.colsText !== undefined) {
    activeBlock.value.cols = activeBlock.value.colsText.split(/[,，]/).map(s => s.trim()).filter(Boolean)
  }
}

let copyBuffer = ''
const copyBlocks = () => {
  const sel = blocks.value.filter(b => selectedIds.value.includes(b.id))
  if (!sel.length) return
  copyBuffer = JSON.stringify(sel.map(b => ({ ...b })))
  ElMessage.success(`已复制 ${sel.length} 个区块`)
}
const pasteBlocks = () => {
  if (!copyBuffer) return
  commitBefore()
  const parsed: PrintBlock[] = JSON.parse(copyBuffer)
  const news = parsed.map(p => ({ ...p, id: nextId(), x: p.x + 24, y: p.y + 24 }))
  blocks.value.push(...news)
  selectedIds.value = news.map(b => b.id)
  ElMessage.success(`已粘贴 ${news.length} 个区块`)
}

/* 层级：DOM 顺序即 z 顺序 */
const zMove = (dir: 'up' | 'down') => {
  if (!selectedIds.value.length) return
  const id = activeId.value
  const idx = blocks.value.findIndex(b => b.id === id)
  if (idx < 0) return
  commitBefore()
  const arr = [...blocks.value]
  if (dir === 'up' && idx < arr.length - 1) {
    ;[arr[idx], arr[idx + 1]] = [arr[idx + 1], arr[idx]]
  } else if (dir === 'down' && idx > 0) {
    ;[arr[idx], arr[idx - 1]] = [arr[idx - 1], arr[idx]]
  }
  blocks.value = arr
}

/* ===================== 对齐 / 等宽等高 ===================== */
const align = (dir: string) => {
  const sel = blocks.value.filter(b => selectedIds.value.includes(b.id))
  if (!sel.length) return
  commitBefore()
  const cw = canvasW.value, ch = canvasH.value
  if (sel.length === 1) {
    const b = sel[0]
    if (dir === 'left') b.x = 0
    if (dir === 'hcenter') b.x = Math.round((cw - b.w) / 2)
    if (dir === 'right') b.x = cw - b.w
    if (dir === 'top') b.y = 0
    if (dir === 'vcenter') b.y = Math.round((ch - (b.h || 0)) / 2)
    if (dir === 'bottom') b.y = ch - (b.h || 0)
  } else {
    const minX = Math.min(...sel.map(b => b.x))
    const maxX = Math.max(...sel.map(b => b.x + b.w))
    const minY = Math.min(...sel.map(b => b.y))
    const maxY = Math.max(...sel.map(b => b.y + (b.h || 0)))
    const gw = maxX - minX, gh = maxY - minY
    sel.forEach(b => {
      if (dir === 'left') b.x = minX
      if (dir === 'hcenter') b.x = minX + Math.round((gw - b.w) / 2)
      if (dir === 'right') b.x = maxX - b.w
      if (dir === 'top') b.y = minY
      if (dir === 'vcenter') b.y = minY + Math.round((gh - (b.h || 0)) / 2)
      if (dir === 'bottom') b.y = maxY - (b.h || 0)
    })
  }
}
const sameSize = (dim: 'w' | 'h') => {
  const sel = blocks.value.filter(b => selectedIds.value.includes(b.id))
  if (sel.length < 2) return
  commitBefore()
  const max = Math.max(...sel.map(b => dim === 'w' ? b.w : (b.h || 0)))
  sel.forEach(b => { if (dim === 'w') b.w = max; else b.h = max })
}

/* ===================== 键盘操作 ===================== */
const onKey = (e: KeyboardEvent) => {
  const t = e.target as HTMLElement
  const inInput = t && (t.tagName === 'INPUT' || t.tagName === 'TEXTAREA' || t.isContentEditable || t.tagName === 'SELECT')
  const sel = blocks.value.filter(b => selectedIds.value.includes(b.id))
  if (!sel.length) return
  if (inInput && !(e.ctrlKey || e.metaKey)) return
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'z') { e.preventDefault(); e.shiftKey ? redo() : undo(); return }
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'y') { e.preventDefault(); redo(); return }
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'c') { e.preventDefault(); copyBlocks(); return }
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'v') { e.preventDefault(); pasteBlocks(); return }
  if (inInput) return
  const step = e.shiftKey ? 10 : 1
  if (e.key === 'Delete' || e.key === 'Backspace') { e.preventDefault(); deleteSelected(); return }
  if (e.key === 'Escape') { e.preventDefault(); clearSelection(); return }
  const dirs: Record<string, [string, number]> = {
    ArrowLeft: ['x', -step], ArrowRight: ['x', step], ArrowUp: ['y', -step], ArrowDown: ['y', step]
  }
  const d = dirs[e.key]
  if (!d) return
  e.preventDefault()
  commitBefore()
  sel.forEach(b => { (b as any)[d[0]] = Math.max(0, (b as any)[d[0]] + d[1]) })
}

/* ===================== 属性面板（setter 内先记录历史） ===================== */
const propX = computed({
  get: () => activeBlock.value?.x ?? 0,
  set: (v: number) => { if (activeBlock.value) { commitBefore(); activeBlock.value.x = v } }
})
const propY = computed({
  get: () => activeBlock.value?.y ?? 0,
  set: (v: number) => { if (activeBlock.value) { commitBefore(); activeBlock.value.y = v } }
})
const propW = computed({
  get: () => activeBlock.value?.w ?? 0,
  set: (v: number) => { if (activeBlock.value) { commitBefore(); activeBlock.value.w = v } }
})
const propH = computed({
  get: () => activeBlock.value?.h ?? 0,
  set: (v: number | undefined) => { if (activeBlock.value) { commitBefore(); activeBlock.value.h = v || undefined } }
})
const propLabel = computed({
  get: () => activeBlock.value?.label || '',
  set: (v: string) => { if (activeBlock.value) { commitBefore(); activeBlock.value.label = v } }
})
const propField = computed({
  get: () => activeBlock.value?.field || '',
  set: (v: string) => { if (activeBlock.value) { commitBefore(); activeBlock.value.field = v || undefined } }
})
const propFontSize = computed({
  get: () => activeBlock.value?.fontSize ?? 13,
  set: (v: number) => { if (activeBlock.value) { commitBefore(); activeBlock.value.fontSize = v } }
})
const propBold = computed({
  get: () => !!activeBlock.value?.bold,
  set: (v: boolean) => { if (activeBlock.value) { commitBefore(); activeBlock.value.bold = v } }
})
const propTextAlign = computed({
  get: () => activeBlock.value?.textAlign || 'left',
  set: (v: string) => { if (activeBlock.value) { commitBefore(); activeBlock.value.textAlign = v } }
})
const propColsText = computed({
  get: () => activeBlock.value?.colsText || '',
  set: (v: string) => { if (activeBlock.value) { commitBefore(); activeBlock.value.colsText = v } }
})
const propDynamic = computed({
  get: () => !!activeBlock.value?.dynamic,
  set: (v: boolean) => { if (activeBlock.value) { commitBefore(); activeBlock.value.dynamic = v } }
})

/* ===================== 默认生产单模板 ===================== */
const loadDefaultTemplate = () => {
  blocks.value = buildDefaultBlocks().map(b => ({ ...b }))
  blockSeq = Math.max(...blocks.value.map(b => b.id)) + 1
  selectedIds.value = []
  ElMessage.success('已载入默认生产单模板，可拖拽调整')
}
const loadSalesTemplate = () => {
  blocks.value = buildSalesDefaultBlocks().map(b => ({ ...b }))
  blockSeq = Math.max(...blocks.value.map(b => b.id)) + 1
  selectedIds.value = []
  ElMessage.success('已载入销售单模板，可拖拽调整')
}
/** 按单据类型载入对应默认模板 */
const loadTemplateByType = (type: string) => {
  if (type === 'SALES_ORDER') loadSalesTemplate()
  else if (type === 'WORK_ORDER') loadDefaultTemplate()
  else {
    blocks.value = []
    blockSeq = 1
    selectedIds.value = []
    ElMessage.info('自定义单据类型：画布已清空，请从左侧添加区块')
  }
}
/** 用户手动切换单据类型：载入该类型默认模板（编辑已有模板时需确认） */
const onTypeChange = (v: string) => {
  if (isEdit.value) {
    ElMessageBox.confirm('切换单据类型将载入该类型的默认模板，当前编辑内容会被替换，继续？', '提示', { type: 'warning' })
      .then(() => loadTemplateByType(v))
      .catch(() => {})
  } else {
    loadTemplateByType(v)
  }
}

/* ===================== 新增/编辑 ===================== */
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({
  templateId: null as number | null,
  templateName: '', templateType: 'WORK_ORDER',
  paperSize: 'A4_LANDSCAPE', isDefault: 0, status: 1, templateContent: ''
})
const hasDefaultForType = async (type: string) => {
  try {
    const res: any = await getPrintTemplatePage(1, 200, '', type, 1)
    return (res.data?.records || []).some((t: any) => t.isDefault === 1)
  } catch { return false }
}
const openDialog = async (row?: any) => {
  dialogVisible.value = true
  undoStack.value = []
  redoStack.value = []
  if (row) {
    isEdit.value = true
    Object.assign(form, {
      templateId: row.templateId,
      templateName: row.templateName,
      templateType: row.templateType,
      paperSize: row.paperSize === 'A4' ? 'A4' : 'A4_LANDSCAPE',
      isDefault: row.isDefault === 1 ? 1 : 0,
      status: row.status === 1 ? 1 : 0
    })
    try {
      const parsed = JSON.parse(row.templateContent || '[]')
      if (Array.isArray(parsed) && parsed.length) {
        blocks.value = parsed
        blockSeq = Math.max(...parsed.map((b: any) => b.id)) + 1
      } else {
        loadDefaultTemplate()
        ElMessage.info('该模板是旧版 HTML 格式，已载入新版生产单版式，保存后即替换')
      }
    } catch { loadDefaultTemplate() }
  } else {
    isEdit.value = false
    const exists = await hasDefaultForType('WORK_ORDER')
    Object.assign(form, {
      templateId: null, templateName: '', templateType: 'WORK_ORDER',
      paperSize: 'A4_LANDSCAPE', isDefault: exists ? 0 : 1, status: 1
    })
    loadDefaultTemplate()
  }
  selectedIds.value = []
  canvasScale.value = 1
  addOffset = 0
}
const setAsPrintTemplate = async (row: any) => {
  const res: any = await savePrintTemplate({ ...row, isDefault: 1, status: 1 })
  if (res.code === 200) {
    ElMessage.success(`已将「${row.templateName}」设为工单打印模板（同类其它模板自动取消默认）`)
    loadTable()
  }
}
const submitForm = async () => {
  if (!form.templateName) { ElMessage.warning('请填模板名称'); return }
  validSelection()
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
const onToggleStatus = async (row: any, status: number) => {
  const action = status === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(`确定${action}模板「${row.templateName}」？`, '提示', { type: 'warning' })
  const res: any = await togglePrintTemplateStatus(row.templateId, status)
  if (res.code === 200) { ElMessage.success(`${action}成功`); loadTable() }
}
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定删除模板「${row.templateName}」？`, '提示', { type: 'warning' })
    .then(async () => { await delPrintTemplate(row.templateId); ElMessage.success('删除成功'); loadTable() })
    .catch(() => {})
}

/* ===================== 预览/打印 ===================== */
const previewVisible = ref(false)
const previewRow = ref<any>({})
const mockDetail = {
  workNo: 'WO20260922003', orderNo: '202609191520', customerName: '刘家成',
  customerPhone: '13800000000', customerAddress: '发货',
  expectDate: '2026-09-26', orderDate: '2026-09-19', createTime: '2026-09-19T10:00:00',
  totalArea: 3.05, shelfName: 'A区-01',
  orderItems: [
    { width: 500, height: 698, num: 2, color: '金属灰', fixedBottom: '450', netMaterial: '柔性金刚网', handle: '全防护', handleDirection: '左2右', productName: '框中框-32小平房', itemCategory: '框中框系列',
      formulaResult: {
        型材: {
          外框宽: { dim: 500, mult: 2 }, 外框高: { dim: 748, mult: 2 },
          内框宽: { dim: 400, mult: 1 }, 内框高: { dim: 598, mult: 1 },
          内扇宽: { dim: 200, mult: 1 }, 内扇高: { dim: 548, mult: 1 },
          横杆: { dim: 400, mult: 2 }
        },
        孔位: 219.33,
        纱网: {
          上网: { w: 470, h: 240, mult: 2 },
          下网: { w: 430, h: 220, mult: 2 }
        }
      } },
    { width: 655, height: 1300, num: 4, color: '金属灰', fixedBottom: '450', netMaterial: '柔性金刚网', handle: '全防护', handleDirection: '左2右', productName: '8K大料三节', itemCategory: '8K三节系列',
      formulaResult: {
        型材: {
          外框宽: { dim: 655, mult: 2 }, 外框高: { dim: 1350, mult: 2 },
          内框宽: { dim: 555, mult: 1 }, 内框高: { dim: 1250, mult: 1 },
          内扇宽: { dim: 260, mult: 1 }, 内扇高: { dim: 1200, mult: 1 },
          横杆: { dim: 555, mult: 2 }
        },
        孔位: 420,
        纱网: {
          上网: { w: 645, h: 1240, mult: 4 },
          下网: { w: 600, h: 1190, mult: 4 }
        }
      } }
  ]
}
const mockCtx = buildPrintContext(mockDetail)
/** 销售单预览数据：与销售单模板字段一一对应 */
const mockSalesDetail = {
  orderNo: '20260912919', customerName: '门窗胡哥', customerPhone: '13800000000',
  createTime: '2026-09-12', terminalAddress: '湖璟名都府二单元901',
  totalAmount: 116, amountInWords: '壹佰壹拾陆元整', deposit: 0, balance: 116, totalNum: 2,
  orderItems: [
    { productName: '折边三节', width: 387, height: 1270, color: '金属灰', netMaterial: '高清网', fixedBottom: '450', singleArea: 1.5, num: 1, unitPrice: 58, lineAmount: 58, handle: '一字密码锁', remark: '' },
    { productName: '折边三节', width: 400, height: 1270, color: '金属灰', netMaterial: '高清网', fixedBottom: '450', singleArea: 1.55, num: 1, unitPrice: 58, lineAmount: 58, handle: '一字密码锁', remark: '二单元' }
  ]
}
const salesCtx = buildPrintContext(mockSalesDetail)
const renderedPreview = computed(() => {
  let list: PrintBlock[] = []
  try { list = JSON.parse(previewRow.value.templateContent || '[]') } catch {}
  if (!Array.isArray(list) || !list.length) list = blocks.value
  const isSales = (previewRow.value.templateType || form.templateType) === 'SALES_ORDER'
  return renderBlocksToHtml(list, isSales ? salesCtx : mockCtx)
})
const openPreview = (row: any) => { previewRow.value = row; previewVisible.value = true }
const doPrint = () => {
  const w = window.open('', '_blank')
  if (!w) { ElMessage.warning('请允许弹窗'); return }
  w.document.write(wrapPrintDocument(renderedPreview.value, previewRow.value.templateName || '生产单'))
  w.document.close()
  setTimeout(() => w.print(), 300)
}

/* ===================== 生命周期 ===================== */
watch(dialogVisible, v => {
  if (v) document.addEventListener('keydown', onKey)
  else document.removeEventListener('keydown', onKey)
})
onBeforeUnmount(() => document.removeEventListener('keydown', onKey))
onMounted(() => { loadTypes(); loadTable() })
</script>
<style scoped>
.print-template-page { padding: 16px; }
.page-header { display: flex; justify-content: space-between; margin-bottom: 12px; }
.page-title { margin: 0 0 6px; font-size: 20px; font-weight: 600; }
.page-desc { margin: 0; color: #909399; font-size: 13px; }
.search-card { margin-bottom: 12px; }
.dash { color: #c0c4cc; }
.usage-active { color: #67c23a; font-weight: 600; }
.col-hint { font-size: 11px; color: #909399; line-height: 1.6; margin-top: 4px; }

/* ============ 设计器整体 ============ */
.designer { display: flex; flex-direction: column; height: 82vh; border: 1px solid #dcdfe6; border-radius: 6px; overflow: hidden; }
.designer-base { padding: 8px 12px 0; border-bottom: 1px solid #ebeef5; background: #fafafa; }
.designer-base :deep(.el-form-item) { margin-bottom: 8px; }
.designer-toolbar {
  display: flex; align-items: center; flex-wrap: wrap; gap: 2px;
  padding: 4px 8px; background: #f0f2f5; border-bottom: 1px solid #dcdfe6;
}
.tb-group { display: flex; align-items: center; gap: 2px; }
.zoom-val { font-size: 12px; color: #606266; min-width: 42px; text-align: center; }
.designer-main { display: flex; flex: 1; min-height: 0; }

/* 左侧组件库 */
.lib-panel { width: 158px; border-right: 1px solid #ebeef5; padding: 8px; overflow: auto; background: #fafafa; flex-shrink: 0; }
.lib-title { font-size: 13px; font-weight: 600; color: #303133; margin-bottom: 8px; }
.lib-item { padding: 7px; border: 1px solid #dcdfe6; border-radius: 4px; margin-bottom: 6px; cursor: pointer; background: #fff; font-size: 13px; text-align: center; user-select: none; }
.lib-item:hover { border-color: #1989fa; color: #1989fa; }
.lib-icon { margin-right: 4px; color: #1989fa; }
.lib-var { font-size: 12px; padding: 4px 8px; background: #ecf5ff; border-radius: 3px; margin-bottom: 4px; cursor: pointer; color: #1989fa; }
.lib-var:hover { background: #d9ecff; }

/* 中间画布 */
.canvas-scroll { flex: 1; overflow: auto; background: #e9ecef; padding: 24px; outline: none; }
.canvas-wrap { margin: 0 auto; position: relative; }
.canvas {
  position: relative; background: #fff; box-shadow: 0 2px 12px rgba(0, 0, 0, .18);
  overflow: hidden; box-sizing: border-box;
}
.canvas.landscape { width: 1080px; height: 620px; }
.canvas:not(.landscape) { width: 780px; height: 1080px; }
.canvas.grid-on {
  background-image:
    linear-gradient(rgba(25, 137, 250, .06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(25, 137, 250, .06) 1px, transparent 1px);
  background-size: 20px 20px;
}

.block { position: absolute; cursor: move; font-size: 13px; box-sizing: border-box; }
.block-text { padding: 2px 4px; overflow: hidden; white-space: nowrap; }
.block.active { outline: 1px solid #1989fa; background: rgba(25, 137, 250, .06); z-index: 999; }
.block-line { border-top: 1px solid #333; }
.block-qr { text-align: center; font-size: 11px; }
.qr-box { width: 100%; height: 100%; border: 1px dashed #999; display: flex; align-items: center; justify-content: center; color: #999; }
.block-table { overflow: hidden; }
.dynamic-badge {
  position: absolute; right: 2px; bottom: 2px;
  font-size: 10px; color: #1989fa; background: rgba(25, 137, 250, .12);
  border: 1px solid rgba(25, 137, 250, .4); border-radius: 2px; padding: 0 4px;
  line-height: 16px; z-index: 990;
}
.block-table .mini-table { border-collapse: collapse; width: 100%; height: 100%; }
.block-table .mini-table th, .block-table .mini-table td { border: 1px solid #666; padding: 3px; font-size: 11px; }
.block-table .mini-table .foot-label { text-align: left; }
.block-table .mini-table .foot-total { text-align: center; color: #909399; }

/* 8 方向缩放手柄 */
.rh {
  position: absolute; width: 8px; height: 8px; background: #1989fa;
  border: 1px solid #fff; border-radius: 1px; z-index: 1000;
}
.rh-nw { left: -5px; top: -5px; cursor: nwse-resize; }
.rh-n { left: 50%; top: -5px; margin-left: -4px; cursor: ns-resize; }
.rh-ne { right: -5px; top: -5px; cursor: nesw-resize; }
.rh-e { right: -5px; top: 50%; margin-top: -4px; cursor: ew-resize; }
.rh-se { right: -5px; bottom: -5px; cursor: nwse-resize; }
.rh-s { left: 50%; bottom: -5px; margin-left: -4px; cursor: ns-resize; }
.rh-sw { left: -5px; bottom: -5px; cursor: nesw-resize; }
.rh-w { left: -5px; top: 50%; margin-top: -4px; cursor: ew-resize; }

/* 右侧属性面板 */
.prop-panel { width: 232px; border-left: 1px solid #ebeef5; padding: 8px; overflow: auto; background: #fafafa; flex-shrink: 0; }
.prop-empty { color: #909399; font-size: 12px; text-align: center; margin-top: 40px; line-height: 2; }
.prop-tip { color: #c0c4cc; font-size: 11px; }
.prop-multi { font-size: 13px; color: #303133; text-align: center; padding: 8px 0; }

/* 状态栏 */
.designer-status {
  display: flex; justify-content: space-between; align-items: center;
  padding: 4px 12px; font-size: 12px; color: #606266;
  background: #f0f2f5; border-top: 1px solid #dcdfe6;
}
.st-right { color: #909399; }

.preview-wrap { background: #f5f7fa; padding: 16px; overflow: auto; max-height: 80vh; }
.a4-sheet { background: #fff; margin: 0 auto; box-shadow: 0 2px 12px rgba(0, 0, 0, .12); overflow: auto; }
</style>
