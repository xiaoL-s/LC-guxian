<template>
  <div class="coming-soon">
    <el-card shadow="never" class="box">
      <div class="icon">🚧</div>
      <h2 class="title">{{ title }}</h2>
      <p class="desc">
        该模块尚未开发，当前版本已完成：<b>首页看板、系统管理、客户管理、销售订单管理（下单 / 订单 / 订单明细 / 进度跟踪）</b>。
      </p>
      <p class="path">当前路径：{{ route.path }}</p>
      <el-alert type="info" :closable="false" show-icon
        title="后续规划：物料与库存（物料档案、货架、出入库）、生产（工序、工单、报工、计件工资）、采购、财务（应收应付、成本核算）、报表统计、小程序配置。" />
      <div class="actions">
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goOrder">去销售订单管理</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const title = computed(() => (route.meta?.title as string) || '模块建设中')

const goHome = () => router.push('/system/home')
const goOrder = () => router.push('/sales/order/entry')
</script>

<style scoped>
.coming-soon {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
.box {
  width: 720px;
  text-align: center;
}
.icon {
  font-size: 56px;
  line-height: 1.2;
}
.title {
  margin: 8px 0 12px;
  font-size: 22px;
  color: #303133;
}
.desc {
  color: #606266;
  font-size: 14px;
  line-height: 1.8;
  margin-bottom: 8px;
}
.path {
  color: #909399;
  font-size: 12px;
  margin-bottom: 16px;
}
.actions {
  margin-top: 20px;
}
</style>
