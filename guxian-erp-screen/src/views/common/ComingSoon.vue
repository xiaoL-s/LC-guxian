<template>
  <div class="coming-soon">
    <el-card shadow="never" class="box">
      <div class="icon">🚧</div>
      <h2 class="title">{{ title }}</h2>
      <p class="desc">该模块尚未开发。</p>
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
  padding: 32px 0;
}
.box {
  width: 520px;
  text-align: center;
}
.icon {
  font-size: 40px;
  line-height: 1.2;
}
.title {
  margin: 6px 0 8px;
  font-size: 18px;
  color: #303133;
}
.desc {
  color: #909399;
  font-size: 13px;
  margin: 0 0 4px;
}
.actions {
  margin-top: 14px;
}
</style>
