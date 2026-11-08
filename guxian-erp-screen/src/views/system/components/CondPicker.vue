<template>
  <el-popover :model-value="open" @update:model-value="v=>open=v" trigger="click" width="auto" :hide-after="0">
    <template #reference>
      <el-button size="small" text type="primary" title="下拉选择条件">选</el-button>
    </template>
    <div style="display:flex;gap:4px;align-items:center;flex-wrap:wrap;padding:4px">
      <el-select v-model="row._condVar" size="small" placeholder="选变量" style="width:110px">
        <el-option v-for="n in vars" :key="n" :label="n" :value="n" />
      </el-select>
      <el-select v-model="row._condOp" size="small" style="width:70px">
        <el-option v-for="o in OPS" :key="o" :label="o" :value="o" />
      </el-select>
      <el-select v-if="row._condVar && varList(row._condVar).length" v-model="row._condVal" size="small" placeholder="值" style="width:110px">
        <el-option v-for="opt in varList(row._condVar)" :key="opt" :label="opt" :value="opt" />
      </el-select>
      <el-input v-else v-model="row._condVal" size="small" placeholder="值" style="width:110px" />
      <el-button size="small" type="primary" @click="apply">确定</el-button>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  row: any
  vars: string[]
  varList: (name?: string) => string[]
}>()
const emit = defineEmits<{ (e: 'applied'): void }>()

const OPS = ['=', '>', '<', '>=', '<=', '!=']
const open = ref(false)

const apply = () => {
  const r = props.row
  if (!r._condVar) return
  const v = r._condVar, op = r._condOp || '=', val = String(r._condVal ?? '').trim()
  if (!val) return
  const seg = `${v}${op}${val}`
  const cur = (r.cond || '').trim()
  if (!cur) r.cond = seg
  else if (cur.startsWith('{') && cur.endsWith('}')) r.cond = cur.slice(0, -1) + ',' + seg + '}'
  else r.cond = '{' + cur + ',' + seg + '}'
  open.value = false
  emit('applied')
}
</script>
