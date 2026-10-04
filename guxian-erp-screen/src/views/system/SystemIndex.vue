<template>
  <div class="system-layout">
    <!-- 把收缩状态传给侧边栏 -->
    <SideBar :menu-list="menuTree" :is-collapse="isCollapse"/>

    <div class="main-wrap">
      <header class="top-header">
        <div class="header-left">
          <!-- 新增：收缩切换按钮 -->
          <button class="collapse-btn" @click="isCollapse = !isCollapse">
            {{ isCollapse ? "展开" : "收缩" }}
          </button>
          首页
        </div>
        <div class="header-right">
          <span class="username">👤 {{ loginUser.realName }}</span>
          <el-button text type="danger" @click="handleLogout" class="logout-btn">退出登录</el-button>
        </div>
      </header>
      <div class="content">
        <router-view/>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUserMenu } from '@/api/system/menu'
import SideBar from '@/components/SideBar.vue'

// ========= 新增侧边栏收缩状态 =========
const isCollapse = ref(false)

const router = useRouter()
// 当前登录用户
const loginUser = ref<any>({})

onMounted(async () => {
  // 从localStorage取出登录存储的用户信息（Login登录成功保存进去）
  const userStr = localStorage.getItem('loginUser')
  if(userStr){
    loginUser.value = JSON.parse(userStr)
  }

  const res = await getUserMenu()
  console.log(res.data)
  if(res.code === 200){
    menuTree.value = res.data.map( m => ({ ...m, _expand: false }))
  }
})

const menuTree = ref<any[]>([])

// 退出登录逻辑
const handleLogout = ()=>{
  // 清除token、用户信息
  localStorage.removeItem('token')
  localStorage.removeItem('loginUser')
  // 跳转到登录页面
  router.push('/login')
}
</script>

<style scoped lang="scss">
.system-layout {
  display: flex;
  height: 100vh;
  background: #f0f2f5;
}
.main-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.top-header {
  height: 56px;
  background: #ecf1eb;
  border-bottom: 1px solid #e8e8e8;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .header-left{
    font-weight: 500;
    display: flex;
    align-items: center;
    gap:12px;
  }
  .collapse-btn{
    padding:4px 10px;
    border-radius:6px;
    border:1px solid #cdd6cf;
    background:#e8eee7;
    cursor: pointer;
  }
  .header-right{
    display:flex;
    gap:16px;
    align-items:center;
    .username{
      font-size:14px;
      color:#444;
    }
    .logout-btn{
      font-size:14px;
    }
  }
}
.content {
  flex: 1;
  padding: var(--gx-pad);
  overflow: auto;
  background: #fff;
  margin: calc(var(--gx-pad) - 4px);
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}
</style>