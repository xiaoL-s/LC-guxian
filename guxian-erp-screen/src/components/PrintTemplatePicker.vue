<!-- 打印前选择模板：默认「自动匹配（按产品）」，也可固定用某套模板；记住上次选择 -->
<template>
  <el-dialog
    v-model="visible"
    :title="dlgTitle"
    width="460px"
    append-to-body
    :close-on-click-modal="false"
    @closed="onClosed"
  >
    <div class="tpl-tip">
      <span class="tip-dot">·</span>
      默认按每张单里的产品自动匹配模板；也可以在这里固定用某一套。
    </div>
    <el-select
      v-model="selected"
      style="width: 100%"
      placeholder="请选择打印模板"
      :loading="loading"
      no-data-text="没有可用模板，将使用内置版式"
    >
      <el-option label="自动匹配（按产品，推荐）" value="auto" />
      <el-option v-for="t in list" :key="t.id" :label="t.name" :value="t.id">
        <div class="opt-row">
          <span class="opt-name">{{ t.name }}</span>
          <span class="opt-tag">{{ tagOf(t) }}</span>
        </div>
      </el-option>
    </el-select>
    <div class="tpl-cur">{{ curDesc }}</div>

    <template #footer>
      <el-button @click="cancel">取消</el-button>
      <el-button type="primary" :loading="loading" @click="confirm">开始打印</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { listPrintTemplates } from '@/utils/cuttingSheetRenderer'

type TplType = 'WORK_ORDER' | 'SALES_ORDER'
interface Choice { templateId: number | null }

const visible = ref(false)
const loading = ref(false)
const list = ref<Array<{ id: number, name: string, isDefault: boolean, bindCount: number }>>([])
const selected = ref<string | number>('auto')
const dlgTitle = ref('打印生产单')
const type = ref<TplType>('WORK_ORDER')
let resolver: ((v: Choice | null) => void) | null = null

const storageKey = (t: TplType) => `guxian_print_template_${t}`

const tagOf = (t: { isDefault: boolean, bindCount: number }) => {
  if (t.bindCount > 0) return `已绑定 ${t.bindCount} 个产品`
  return t.isDefault ? '通用默认' : '通用'
}

const selectedTpl = computed(() => {
  const id = selected.value
  if (id === 'auto') return null
  return list.value.find(t => t.id === Number(id)) || null
})

const curDesc = computed(() => {
  if (loading.value) return '正在读取模板…'
  const t = selectedTpl.value
  if (!t) return '订单里的产品命中哪套模板就用哪套，都没命中用通用默认模板。'
  if (t.bindCount > 0) return `本次所有单子都固定用「${t.name}」打印（已绑定 ${t.bindCount} 个产品，正常会自动匹配）。`
  return `本次所有单子都固定用「${t.name}」打印。`
})

const doOpen = async (t: TplType, title?: string) => {
  type.value = t
  dlgTitle.value = title || (t === 'WORK_ORDER' ? '打印生产单' : '打印销售单')
  selected.value = 'auto'
  list.value = []
  visible.value = true
  loading.value = true
  try {
    const ls = await listPrintTemplates(t)
    list.value = ls
    const saved = localStorage.getItem(storageKey(t))
    if (saved && ls.some(x => String(x.id) === saved)) selected.value = Number(saved)
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

/** 打开选择框：确定返回 { templateId }（null=自动匹配），取消返回 null */
const open = (t: TplType, title?: string): Promise<Choice | null> => {
  return new Promise<Choice | null>(resolve => {
    resolver = resolve
    doOpen(t, title)
  })
}

const finish = (v: Choice | null) => {
  const r = resolver
  resolver = null
  if (r) r(v)
}

const confirm = () => {
  if (loading.value) return
  const id = selected.value === 'auto' ? null : Number(selected.value)
  if (id === null) localStorage.removeItem(storageKey(type.value))
  else localStorage.setItem(storageKey(type.value), String(id))
  visible.value = false
  finish({ templateId: id })
}

const cancel = () => {
  visible.value = false
  finish(null)
}

/** 点右上角 X 或按 Esc 也算取消 */
const onClosed = () => {
  if (resolver) finish(null)
}

defineExpose({ open })
</script>

<style scoped>
.tpl-tip {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.6;
  margin-bottom: 10px;
}
.tip-dot { color: #2f9e7a; margin-right: 4px; }
.opt-row { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.opt-name { color: #1a1b1c; }
.opt-tag { font-size: 12px; color: #9aa0a6; }
.tpl-cur {
  margin-top: 10px;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.6;
  background: rgba(0, 0, 0, 0.03);
  border-radius: 8px;
  padding: 8px 10px;
}
</style>
