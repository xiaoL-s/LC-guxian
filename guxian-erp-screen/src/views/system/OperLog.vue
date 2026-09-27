<template>
  <el-card>
    <!--搜索区域 -->
    <el-row :gutter="20">
      <el-col :span="5">
        <el-input v-model="searchForm.operModule" placeholder="操作模块" @keyup.enter="loadTable"></el-input>
      </el-col>
      <el-col :span="5">
        <el-input v-model="searchForm.realName" placeholder="操作人姓名" @keyup.enter="loadTable"></el-input>
      </el-col>
      <el-col :span="8">
        <el-date-picker v-model="searchForm.timeRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" style="width:100%"/>
      </el-col>
      <el-col :span="6">
        <el-button type="primary" @click="loadTable">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-col>
    </el-row>
  </el-card>

  <el-card style="margin-top:12px">
    <el-table :data="tableData" border stripe>
      <el-table-column label="序号" type="index" width="60"/>
      <el-table-column prop="operModule" label="操作模块" />
      <el-table-column prop="operType" label="操作类型" />
      <el-table-column prop="realName" label="操作人" />
      <el-table-column prop="username" label="账号" />
      <el-table-column prop="ip" label="IP地址" />
      <el-table-column prop="operTime" label="操作时间" width="180"/>
      <el-table-column label="操作" width="100">
        <template #default="{row}">
          <el-button type="primary" link @click="openDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @current-change="loadTable"
      @size-change="loadTable"
      style="margin-top:12px;justify-content:flex-end"
    />
  </el-card>

  <!--详情弹窗 -->
  <el-dialog v-model="detailVisible" title="日志详情" width="60%">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="操作模块">{{detailRow.operModule}}</el-descriptions-item>
      <el-descriptions-item label="操作类型">{{detailRow.operType}}</el-descriptions-item>
      <el-descriptions-item label="操作账号">{{detailRow.username}}</el-descriptions-item>
      <el-descriptions-item label="操作人姓名">{{detailRow.realName}}</el-descriptions-item>
      <el-descriptions-item label="IP地址">{{detailRow.ip}}</el-descriptions-item>
      <el-descriptions-item label="操作时间">{{detailRow.operTime}}</el-descriptions-item>
      <el-descriptions-item label="操作详情" :span="2">
        <el-input v-model="detailRow.operContent" type="textarea" rows="6" readonly/>
      </el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <el-button @click="detailVisible=false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getOperLogPage } from '@/api/system/operLog'
const searchForm = ref({
  operModule:'',
  realName:'',
  timeRange:[]
})
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const detailRow = ref({})

const loadTable = async ()=>{
  const params:any = {
    pageNum:pageNum.value,
    pageSize:pageSize.value,
    operModule:searchForm.value.operModule,
    realName:searchForm.value.realName
  }
  if(searchForm.value.timeRange?.length ===2){
    params.startTime = searchForm.value.timeRange[0]
    params.endTime = searchForm.value.timeRange[1]
  }
  const res = await getOperLogPage(params)
  if(res.code ===200){
    tableData.value = res.data.records
    total.value = res.data.total
  }
}
const resetSearch = ()=>{
  searchForm.value = {operModule:'',realName:'',timeRange:[]}
  loadTable()
}
const openDetail = (row)=>{
  detailRow.value = row
  detailVisible.value = true
}
loadTable()
</script>
