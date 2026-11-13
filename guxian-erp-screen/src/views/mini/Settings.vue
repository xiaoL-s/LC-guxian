<template>
  <div class="settings-page">
    <div class="page-head">
      <div class="head-main">
        <div class="head-title">系统设置</div>
        <div class="head-sub">这些选项只影响你自己的界面显示，改完立即生效并记住；不影响任何业务数据。</div>
      </div>
    </div>

    <!-- ============ 外观 ============ -->
    <el-card shadow="never" class="block">
      <template #header>
        <div class="block-head">
          <span class="block-title">外观</span>
          <span class="block-tip">换一换，看得更舒服</span>
        </div>
      </template>

      <div class="row">
        <div class="row-label">
          <div class="row-title">主题配色</div>
          <div class="row-desc">只影响侧边栏、顶栏和主按钮</div>
        </div>
        <div class="row-body">
          <div class="theme-grid">
            <button
              v-for="t in themes"
              :key="t.key"
              type="button"
              class="theme-card"
              :class="{ on: t.key === themeKey }"
              @click="pickTheme(t.key)"
            >
              <span class="theme-swatch" :style="{ background: t.color }"></span>
              <span class="theme-text">
                <span class="theme-name">{{ t.name }}</span>
                <span class="theme-desc">{{ t.desc }}</span>
              </span>
              <span v-if="t.key === themeKey" class="theme-check">✓</span>
            </button>
          </div>
        </div>
      </div>

      <div class="row">
        <div class="row-label">
          <div class="row-title">字体大小</div>
          <div class="row-desc">嫌小就调大一档，表格与表单一起变</div>
        </div>
        <div class="row-body">
          <el-radio-group v-model="state.fontSize" @change="(v: any) => update({ fontSize: v })">
            <el-radio-button value="auto">跟随屏幕</el-radio-button>
            <el-radio-button value="s">偏小</el-radio-button>
            <el-radio-button value="m">标准</el-radio-button>
            <el-radio-button value="l">偏大</el-radio-button>
            <el-radio-button value="xl">特大</el-radio-button>
          </el-radio-group>
          <div class="row-hint">「跟随屏幕」会按显示器大小自动选一档（笔记本偏小、2K 屏偏大）。</div>
        </div>
      </div>

      <div class="row">
        <div class="row-label">
          <div class="row-title">界面密度</div>
          <div class="row-desc">一屏能看多少行</div>
        </div>
        <div class="row-body">
          <el-radio-group v-model="state.density" @change="(v: any) => update({ density: v })">
            <el-radio-button value="compact">紧凑</el-radio-button>
            <el-radio-button value="normal">标准</el-radio-button>
            <el-radio-button value="loose">宽松</el-radio-button>
          </el-radio-group>
          <div class="row-hint">紧凑 = 行更矮、一屏更多；宽松 = 行更高、点起来更稳。</div>
        </div>
      </div>

      <div class="row">
        <div class="row-label">
          <div class="row-title">侧边栏宽度</div>
          <div class="row-desc">菜单名太长被截断时选「宽」</div>
        </div>
        <div class="row-body">
          <el-radio-group v-model="state.sidebarWidth" @change="(v: any) => update({ sidebarWidth: v })">
            <el-radio-button value="narrow">窄</el-radio-button>
            <el-radio-button value="normal">跟随屏幕</el-radio-button>
            <el-radio-button value="wide">宽</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <div class="row last">
        <div class="row-label">
          <div class="row-title">表格斑马纹</div>
          <div class="row-desc">隔行浅底色，看长表格不串行</div>
        </div>
        <div class="row-body">
          <el-switch v-model="state.zebra" @change="(v: any) => update({ zebra: !!v })" />
        </div>
      </div>
    </el-card>

    <!-- ============ 打印 ============ -->
    <el-card shadow="never" class="block">
      <template #header>
        <div class="block-head">
          <span class="block-title">打印</span>
          <span class="block-tip">只影响生产单 / 销售单的版面</span>
        </div>
      </template>

      <div class="row last">
        <div class="row-label">
          <div class="row-title">顶部留白</div>
          <div class="row-desc">纸张没放正时，留点白能保住顶部二维码</div>
        </div>
        <div class="row-body">
          <el-input-number
            v-model="state.printTopMargin"
            :min="PRINT_TOP_MIN"
            :max="PRINT_TOP_MAX"
            :step="2"
            controls-position="right"
            style="width: 140px"
            @change="(v: any) => update({ printTopMargin: Number(v) })"
          />
          <span class="unit">mm</span>
          <div class="row-hint">默认 12mm。打印机进纸偏下、二维码老是被切，就调到 16-20mm。</div>
        </div>
      </div>
    </el-card>

    <!-- ============ 效果预览 ============ -->
    <el-card shadow="never" class="block">
      <template #header>
        <div class="block-head">
          <span class="block-title">效果预览</span>
          <span class="block-tip">下面这块会跟着上面的设置一起变</span>
        </div>
      </template>

      <div class="preview">
        <div class="preview-form">
          <span class="pv-label">客户名称</span>
          <el-input v-model="previewText" placeholder="输入点什么看看字号" style="width: 220px" clearable />
          <el-button type="primary">查询</el-button>
          <el-button>重置</el-button>
        </div>

        <el-table :data="previewRows" border size="small" style="width: 100%">
          <el-table-column prop="orderNo" label="订单编号" min-width="140" />
          <el-table-column prop="customer" label="客户" min-width="120" />
          <el-table-column prop="product" label="产品" min-width="180" />
          <el-table-column prop="area" label="面积(m²)" width="100" align="center" />
          <el-table-column prop="amount" label="金额(元)" width="110" align="center" />
          <el-table-column prop="status" label="状态" width="110" align="center">
            <template #default="scope">
              <el-tag size="small" :type="scope.row.status === '已打印' ? 'success' : 'info'">{{ scope.row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <div class="foot-bar">
      <el-button @click="restoreDefault">恢复默认设置</el-button>
      <span class="foot-tip">只重置本页的字号 / 密度 / 侧栏 / 斑马纹 / 打印留白，不动主题和登录状态</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { THEMES, getStoredTheme, saveTheme } from '@/utils/theme'
import {
  getSettings, saveSettings, resetSettings,
  PRINT_TOP_MIN, PRINT_TOP_MAX,
  type GxSettings
} from '@/utils/settings'

defineOptions({ name: 'GxSettings' })

const themes = THEMES
const themeKey = ref(getStoredTheme())
const state = ref<GxSettings>(getSettings())
const previewText = ref('')

const previewRows = [
  { orderNo: '20261105X0001', customer: '永城门窗', product: '32平盒-可拆洗', area: '1.00', amount: '148.00', status: '已打印' },
  { orderNo: '20261104X0002', customer: '刘家成', product: '32可拆平盒-8K外宽', area: '6.00', amount: '1128.00', status: '未打印' },
  { orderNo: '20261103X0001', customer: '唐总', product: '8K拆洗护童款', area: '2.08', amount: '349.44', status: '已打印' }
]

/** 主题切换：saveTheme 会派发事件，顶栏的小圆点同步更新 */
const pickTheme = (key: string) => {
  saveTheme(key)
  themeKey.value = key
}

/** 统一入口：写库 + 立即生效，不弹提示（效果就在眼前，弹提示反而烦） */
const update = (patch: Partial<GxSettings>) => {
  state.value = saveSettings(patch)
}

const restoreDefault = async () => {
  try {
    await ElMessageBox.confirm(
      '确认恢复默认界面设置？字号、密度、侧栏宽度、斑马纹、打印留白都会回到默认，主题保持不变。',
      '恢复默认设置',
      { type: 'warning', confirmButtonText: '恢复默认', cancelButtonText: '取消' }
    )
  } catch { return }
  state.value = resetSettings()
  ElMessage.success('已恢复默认设置')
}
</script>

<style scoped lang="scss">
.settings-page { padding: 12px 4px 20px; }

.page-head {
  margin-bottom: 12px;
  .head-title { font-size: 17px; font-weight: 700; color: #1f2733; letter-spacing: .5px; }
  .head-sub { margin-top: 4px; font-size: 12.5px; color: #8a919c; line-height: 1.6; }
}

.block {
  margin-bottom: 12px;
  border-radius: 12px;

  :deep(.el-card__header) { padding: 12px 16px; }
  :deep(.el-card__body) { padding: 4px 16px 6px; }
}

.block-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  .block-title { font-size: 14.5px; font-weight: 600; color: #1f2733; }
  .block-tip { font-size: 12px; color: #9aa3ae; }
}

/* 一行一项：左边标题、右边控件；窄屏自动上下堆叠 */
.row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 14px 0;
  border-bottom: 1px solid #f0f2f5;

  &.last { border-bottom: none; }
}
.row-label {
  flex: 0 0 128px;
  min-width: 0;
  .row-title { font-size: 13.5px; font-weight: 600; color: #303133; line-height: 1.6; }
  .row-desc { margin-top: 2px; font-size: 12px; color: #9aa3ae; line-height: 1.5; }
}
.row-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}
.row-hint { flex-basis: 100%; font-size: 12px; color: #9aa3ae; line-height: 1.6; }
.unit { font-size: 12.5px; color: #8a919c; }

/* 主题色卡：圆角、悬停轻微上浮，选中描边 */
.theme-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(168px, 1fr));
  gap: 10px;
  width: 100%;
}
.theme-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  background: #fff;
  cursor: pointer;
  text-align: left;
  transition: border-color .15s, box-shadow .15s, transform .15s;
  font-family: inherit;

  &:hover { border-color: #a0cfff; box-shadow: 0 2px 10px rgba(64, 158, 255, .12); transform: translateY(-1px); }
  &.on { border-color: var(--el-color-primary); box-shadow: 0 0 0 2px rgba(64, 158, 255, .14); }

  .theme-swatch {
    width: 16px; height: 16px; border-radius: 50%; flex-shrink: 0;
    border: 2px solid #fff; box-shadow: 0 0 0 1px rgba(0, 0, 0, .12);
  }
  .theme-text { display: flex; flex-direction: column; min-width: 0; flex: 1; }
  .theme-name { font-size: 13px; font-weight: 600; color: #303133; }
  .theme-desc { font-size: 11.5px; color: #9aa3ae; margin-top: 1px; }
  .theme-check { color: var(--el-color-primary); font-weight: 700; }
}

.preview {
  padding: 12px 0 4px;
}
.preview-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;

  .pv-label { font-size: 13px; color: #606266; }
}

.foot-bar {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  padding: 4px 2px 0;
  .foot-tip { font-size: 12px; color: #9aa3ae; }
}

@media (max-width: 900px) {
  .row { flex-direction: column; gap: 8px; }
  .row-label { flex: none; }
}
</style>
