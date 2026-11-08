<template>
  <div class="account-page">
    <el-card shadow="never" class="search-card">
      <div class="toolbar">
        <el-button type="primary" size="small" @click="openEditDialog()">新建</el-button>
        <el-input
          v-model="keyword"
          placeholder="搜索：请输入账户"
          clearable
          size="small"
          style="width: 240px"
          @keyup.enter="loadAll"
          @clear="loadAll"
        />
        <el-button type="primary" size="small" @click="loadAll">查询</el-button>
        <div class="toolbar-right">
          <el-button size="small" @click="loadAll">刷新</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table
        :data="filteredRows"
        border
        stripe
        size="small"
        height="calc(100vh - 280px)"
      >
        <el-table-column prop="accountName" label="账户" width="180" />
        <el-table-column label="户名" min-width="240">
          <template #default="{ row }">
            <span v-if="Number(row.isSystem) === 1" style="color: #e6a23c">{{ row.remark || '此账号为系统账号，切勿删除' }}</span>
            <span v-else>{{ row.holderName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="bankCard" label="银行卡号" min-width="200">
          <template #default="{ row }">{{ row.bankCard || '—' }}</template>
        </el-table-column>
        <el-table-column label="账户金额" width="140" align="right">
          <template #default="{ row }">
            <span style="color: #1c6cd4; font-weight: 600">{{ money(row.balance) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="删除" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <el-popconfirm
              title="确认删除该收款账户？"
              :disabled="Number(row.isSystem) === 1"
              @confirm="onDelete(row)"
            >
              <template #reference>
                <el-button type="danger" link size="small" :disabled="Number(row.isSystem) === 1">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <div class="footer-stats">
          <span class="stat">显示数 {{ filteredRows.length }}</span>
          <span class="stat">账户合计: <b>{{ money(totalBalance) }}</b></span>
        </div>
      </div>
    </el-card>

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="editDialog" :title="form.accountId ? '编辑收款账户' : '新建收款账户'" width="420px" :close-on-click-modal="false">
      <el-form :model="form" label-width="90px" size="small">
        <el-form-item label="账户名称" required>
          <el-input v-model="form.accountName" placeholder="如：商户微信" :disabled="Number(form.isSystem) === 1" />
        </el-form-item>
        <el-form-item label="户名">
          <el-input v-model="form.holderName" placeholder="开户人姓名/昵称" :disabled="Number(form.isSystem) === 1" />
        </el-form-item>
        <el-form-item label="银行卡号">
          <el-input v-model="form.bankCard" placeholder="银行卡号（微信/支付宝可留空）" />
        </el-form-item>
        <el-form-item label="账户金额">
          <el-input-number
            v-model="form.balance"
            :min="0"
            :precision="2"
            :controls="false"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" :disabled="Number(form.isSystem) === 1" placeholder="备注说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="editDialog = false">取消</el-button>
        <el-button type="primary" size="small" :loading="saving" @click="submitSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAccountList, saveAccount, deleteAccount } from '@/api/finance/collection'

const rows = ref<any[]>([])
const keyword = ref('')
const editDialog = ref(false)
const saving = ref(false)

const form = reactive<any>({ accountId: null, accountName: '', holderName: '', bankCard: '', balance: 0, isSystem: 0, remark: '' })

const money = (v: any) => (v == null ? '0.00' : Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }))

const filteredRows = computed(() => {
  const kw = keyword.value.trim()
  if (!kw) return rows.value
  return rows.value.filter(r => (r.accountName || '').includes(kw))
})

const totalBalance = computed(() => rows.value.reduce((s, r) => s + Number(r.balance || 0), 0))

async function loadAll() {
  const res = await getAccountList()
  rows.value = res.data || []
}

function openEditDialog(row?: any) {
  if (row) {
    Object.assign(form, {
      accountId: row.accountId,
      accountName: row.accountName,
      holderName: row.holderName,
      bankCard: row.bankCard,
      balance: Number(row.balance || 0),
      isSystem: Number(row.isSystem || 0),
      remark: row.remark
    })
  } else {
    Object.assign(form, { accountId: null, accountName: '', holderName: '', bankCard: '', balance: 0, isSystem: 0, remark: '' })
  }
  editDialog.value = true
}

async function submitSave() {
  if (!form.accountName.trim()) return ElMessage.warning('请输入账户名称')
  saving.value = true
  try {
    await saveAccount({ ...form })
    ElMessage.success('保存成功')
    editDialog.value = false
    loadAll()
  } finally {
    saving.value = false
  }
}

async function onDelete(row: any) {
  if (Number(row.isSystem) === 1) return ElMessage.warning('此账号为系统账号，切勿删除')
  try {
    await deleteAccount(row.accountId)
    ElMessage.success('删除成功')
    loadAll()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg || '删除失败')
  }
}

// onMounted(loadAll) // 初始不自动加载，点查询才出数据
</script>

<style scoped lang="scss">
.account-page {
  padding: 10px;
  box-sizing: border-box;
}
.search-card {
  margin-bottom: 10px;
  :deep(.el-card__body) {
    padding: 10px 12px;
  }
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  .toolbar-right {
    margin-left: auto;
    display: flex;
    gap: 8px;
  }
}
.table-card {
  :deep(.el-card__body) {
    padding: 0;
  }
}
.table-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 10px;
  border-top: 1px solid #ebeef5;
  flex-wrap: wrap;
  gap: 6px;
  .footer-stats {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 12px;
    color: #606266;
  }
}
</style>
