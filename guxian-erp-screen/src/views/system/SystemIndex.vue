<template>
  <div class="system-layout">
    <!-- 把收缩状态传给侧边栏 -->
    <SideBar :menu-list="menuTree" :is-collapse="isCollapse" @expand-sidebar="isCollapse = false"/>

    <div class="main-wrap">
      <header class="top-header">
        <div class="header-left">
          <!-- 收缩切换按钮 -->
          <button class="collapse-btn" @click="isCollapse = !isCollapse">
            ☰
          </button>
          <!-- 面包屑导航：首页 / 一级菜单 / 当前页面 -->
          <el-breadcrumb separator="/" class="header-crumb">
            <el-breadcrumb-item
              v-for="(c, i) in crumbs"
              :key="i"
              :to="i < crumbs.length - 1 ? c.path : undefined"
            >{{ c.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <!-- 主题切换（用户名左侧） -->
          <ThemeSwitcher />
          <span class="username">👤 {{ loginUser.realName }}</span>
          <el-button text type="danger" @click="handleLogout" class="logout-btn">退出登录</el-button>
        </div>
      </header>
      <div class="content">
        <BreadcrumbTabs />
        <!-- 注意：keep-alive 内部只能有一个子节点，注释也不能放进去（dev 模式不剥离注释会报
             "KeepAlive expects exactly one child component"） -->
        <!-- key 带上 orderId：新建/编辑订单共用 /sales/order/edit，只用 path 会复用同一实例，
             导致打开编辑时显示的仍是上一张单（onMounted 不再执行） -->
        <router-view v-slot="{ Component }">
          <keep-alive :include="cachedNames">
            <component :is="Component" :key="route.path + '|' + (route.query.orderId ?? '')"/>
          </keep-alive>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserMenu } from '@/api/system/menu'
import { logoutApi } from '@/api/system/login'
import SideBar from '@/components/SideBar.vue'
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'
import BreadcrumbTabs from '@/components/BreadcrumbTabs.vue'
import { initTheme } from '@/utils/theme'

// ========= 新增侧边栏收缩状态 =========
const isCollapse = ref(false)

const route = useRoute()
const router = useRouter()

/** 面包屑：首页 / 一级菜单 / 当前页面（取自路由 meta.title） */
const crumbs = computed(() => {
  const list = route.matched.filter(r => r.meta && r.meta.title).map(r => ({ title: r.meta.title as string, path: r.path }))
  const filtered = list.filter(l => l.title !== '首页看板' && l.title !== '首页')
  return [{ title: '首页', path: '/system/home' }, ...filtered]
})

/** 多标签 keep-alive 缓存名单（按组件 name 匹配；首页看板常驻缓存） */
const cachedNames = ref<string[]>(['CsHome', 'OrderAdd'])
provide('gxCachedNames', cachedNames)
// 当前登录用户
const loginUser = ref<any>({})

onMounted(async () => {
  // 恢复上次选择的主题
  initTheme()
  // 从localStorage取出登录存储的用户信息（Login登录成功保存进去）
  const userStr = localStorage.getItem('loginUser')
  if(userStr){
    loginUser.value = JSON.parse(userStr)
  }

  const res = await getUserMenu()
  console.log(res.data)
  if(res.code === 200){
    menuTree.value = res.data
  }
})

const menuTree = ref<any[]>([])

// 退出登录逻辑
const handleLogout = async ()=>{
  // 先通知服务端把 token 拉黑（失败也照样退，本地一定能清干净）
  try {
    await logoutApi()
  } catch {
    // 网络异常 / token 已过期：服务端已无法使用该 token，忽略即可
  }
  // 清除token、用户信息、菜单展开状态
  localStorage.removeItem('token')
  localStorage.removeItem('loginUser')
  localStorage.removeItem('menu_expanded')
  // 跳转到登录页面
  router.push('/login')
}
</script>

<style scoped lang="scss">
.system-layout {
  display: flex;
  height: 100vh;
  background: var(--gx-content-bg, #f0f2f5);
}
.main-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.top-header {
  height: 56px;
  background: var(--gx-header-bg, #ffffff);
  border-bottom: 1px solid var(--gx-header-border, #e4e7ed);
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);

  .header-left{
    font-weight: 500;
    display: flex;
    align-items: center;
    gap:12px;
  }
  .collapse-btn{
    width:32px;
    height:32px;
    line-height:30px;
    text-align:center;
    border-radius:8px;
    border:1px solid #dcdfe6;
    background:#f5f7fa;
    cursor: pointer;
    font-size:14px;
    color:#606266;
    &:hover { background:#ecf5ff; color:#409eff; border-color:#b3d8ff; }
  }
  .header-crumb{
    margin-left:16px;
    font-size:14px;
    :deep(.el-breadcrumb__inner){ color:#606266; }
    :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner){ color:#303133; font-weight:600; }
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
  /* 内容区是页面唯一留白来源：去掉外边距，页面容器不再叠加 padding */
  padding: 8px 10px 10px;
  overflow: auto;
  background: var(--gx-content-bg, #f0f2f5);
  margin: 0;
  border-radius: 0;
  box-shadow: none;
}
</style>
