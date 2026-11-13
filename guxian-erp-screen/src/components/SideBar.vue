<template>
  <div class="sidebar" :class="{ collapse: isCollapse }">
    <!-- 菜单搜索：一级+二级菜单越来越多，靠滚找太慢 -->
    <div v-if="!isCollapse" class="menu-search">
      <el-input v-model="keyword" size="small" clearable placeholder="搜索菜单..." />
    </div>
    <template v-for="item in visibleMenus" :key="item.id">
      <div class="menu-item">
        <!-- 收缩状态点击也要有反馈：自动展开侧栏 -->
        <div class="menu-title" @click="onMenuClick(item)">
          <component
            v-if="item.icon"
            :is="iconMap[item.icon]"
            class="menu-icon"
          />
          <span v-if="!isCollapse" class="menu-text">{{ item.menuName }}</span>

          <span
            v-if="!isCollapse && item.children && item.children.length > 0"
            class="arrow"
            :class="{ expand: isExpanded(item.menuName) }"
          >></span>
        </div>

        <!-- 收缩状态直接隐藏子菜单 -->
        <div v-show="!isCollapse && isExpanded(item.menuName)" class="sub-menu">
          <router-link
            v-for="child in item.children"
            :key="child.id"
            :to="child.path"
            class="sub-item"
            active-class="active"
          >
            <component
              v-if="child.icon"
              :is="iconMap[child.icon]"
              class="sub-icon"
            />
            <span v-if="!isCollapse">{{ child.menuName }}</span>
          </router-link>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
// 导入antd图标
import {
  HomeOutlined,
  SettingOutlined,
  UserOutlined,
  ShopOutlined,
  ShoppingCartOutlined,
  ShoppingOutlined,
  InboxOutlined,
  ToolOutlined,
  MoneyCollectOutlined,
  BarChartOutlined,
  WechatOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()

// 图标映射
const iconMap: Record<string, any> = {
  HomeOutlined,
  SettingOutlined,
  UserOutlined,
  ShopOutlined,
  ShoppingCartOutlined,
  ShoppingOutlined,
  InboxOutlined,
  ToolOutlined,
  MoneyCollectOutlined,
  BarChartOutlined,
  WechatOutlined
}

const props = defineProps<{
  menuList: any[]
  isCollapse: boolean
}>()

const emit = defineEmits<{ (e: 'expand-sidebar'): void }>()

// ========== 菜单搜索（一级/二级名称匹配；搜索时全部展开便于定位） ==========
const keyword = ref('')
const visibleMenus = computed(() => {
  const kw = keyword.value.trim()
  if (!kw) return props.menuList || []
  return (props.menuList || [])
    .map((m: any) => {
      const children = (m.children || []).filter((c: any) => String(c.menuName || '').includes(kw))
      if (String(m.menuName || '').includes(kw)) return { ...m }
      return children.length ? { ...m, children } : null
    })
    .filter(Boolean) as any[]
})

// ========== 展开状态管理（独立 Set，不依赖对象属性） ==========
const expandedNames = ref<Set<string>>(new Set())

// 从 localStorage 恢复
const loadFromStorage = () => {
  try {
    const saved = JSON.parse(localStorage.getItem('menu_expanded') || '[]')
    expandedNames.value = new Set(saved)
  } catch {
    expandedNames.value = new Set()
  }
}

// 保存到 localStorage
const saveToStorage = () => {
  localStorage.setItem('menu_expanded', JSON.stringify([...expandedNames.value]))
}

// 判断菜单是否展开（搜索状态下强制展开，方便看到匹配到的子菜单）
const isExpanded = (name: string) => (keyword.value.trim() ? true : expandedNames.value.has(name))

// 切换展开/收起
const toggleExpand = (item: any) => {
  const name = item.menuName
  if (expandedNames.value.has(name)) {
    expandedNames.value.delete(name)
  } else {
    expandedNames.value.add(name)
  }
  // 触发响应式更新
  expandedNames.value = new Set(expandedNames.value)
  saveToStorage()
}

// 顶级菜单点击
const onMenuClick = (item: any) => {
  // 折叠状态下点图标：先把侧栏展开，否则点了完全没反应
  if (props.isCollapse) { emit('expand-sidebar'); return }
  if (item.children && item.children.length > 0) {
    toggleExpand(item)
  } else if (item.path) {
    router.push(item.path)
  }
}

// 初始化：从 localStorage 恢复（必须在watch之前执行）
loadFromStorage()

// 路由切换时：当前页面的父菜单自动展开
const autoExpandActive = () => {
  // 菜单数据还没加载完时不执行，避免清空localStorage
  if (!props.menuList || props.menuList.length === 0) return
  const cur = route.path
  props.menuList.forEach(m => {
    if (m.children && m.children.some((c: any) => c.path === cur)) {
      expandedNames.value.add(m.menuName)
    }
  })
  expandedNames.value = new Set(expandedNames.value)
  saveToStorage()
}

// 监听路由变化
watch(() => route.path, autoExpandActive)

// 菜单数据加载完后，自动展开当前页的父菜单
watch(() => props.menuList, (newList) => {
  if (newList && newList.length > 0) {
    autoExpandActive()
  }
}, { immediate: true })
</script>

<style scoped lang="scss">
.sidebar {
  width: var(--gx-sidebar-w, 180px);  height: 100vh;
  padding: 16px 10px;
  box-sizing: border-box;
  background: var(--gx-sidebar-bg);
  border-right: 1px solid var(--gx-sidebar-border);
  color: var(--gx-sidebar-text);
  overflow-y: auto;
  overflow-x: hidden;
  font-family: "PingFang SC", "Microsoft YaHei", "Helvetica Neue", Arial, sans-serif;
  transition: width 0.34s cubic-bezier(0.2, 0, 0.2, 1);
  box-shadow: 1px 0 4px rgba(0, 0, 0, 0.04);

  &::-webkit-scrollbar {
    width: 5px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.12);
    border-radius: 4px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &.collapse {
    width: 64px;
    padding: 16px 6px;
  }
}

.menu-search {
  margin: 0 4px 10px;

  :deep(.el-input__wrapper) {
    border-radius: 8px;
    background: rgba(255, 255, 255, 0.9);
  }
}

.menu-item {
  margin-bottom: 4px;
  .menu-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 11px 14px;
    border-radius: 8px;
    cursor: pointer;
    transition: 0.22s ease;

    &:hover {
      background: var(--gx-sidebar-hover-bg);
      color: var(--gx-sidebar-hover-text);
    }
  }

  .menu-icon {
    font-size: 17px;
    margin-right: 6px;
    color: var(--gx-sidebar-icon);
    flex-shrink: 0;
  }

  .menu-text {
    flex: 1;
    font-weight: 500;
    font-size: 13.5px;
    letter-spacing: 0.2px;
    margin-right: 6px;
    white-space: nowrap;
    overflow: hidden;
    max-width: 160px;
    transition: max-width 0.34s cubic-bezier(0.2, 0, 0.2, 1);
  }

  .arrow {
    font-size: 11px;
    color: #a8b0bc;
    transition: transform 0.28s ease;
    flex-shrink: 0;
    &.expand {
      transform: rotate(90deg);
    }
  }

  :deep(.collapse) & .menu-title {
    justify-content: center;
    padding: 11px 0;
  }
  :deep(.collapse) & .menu-icon {
    margin-right: 0;
  }
  :deep(.collapse) & .menu-text {
    max-width: 0;
    margin-right: 0;
  }
  :deep(.collapse) & .arrow {
    opacity: 0;
  }
}

.sub-menu {
  margin: 4px 0 8px 14px;
  padding-left: 14px;
  border-left: 1px solid var(--gx-sidebar-sub-border);

  .sub-item {
    display: flex;
    align-items: center;
    padding: 9px 14px;
    margin-bottom: 3px;
    border-radius: 8px;
    color: var(--gx-sidebar-sub-text);
    text-decoration: none;
    font-size: 13px;
    transition: all 0.22s ease;

    .sub-icon {
      font-size: 15px;
      margin-right: 5px;
      flex-shrink: 0;
    }

    &:hover {
      background: var(--gx-sidebar-hover-bg);
      color: var(--gx-sidebar-hover-text);
    }

    &.active {
      color: var(--gx-sidebar-active-text);
      background: var(--gx-sidebar-active-bg);
      font-weight: 600;
      box-shadow: 0 1px 6px var(--gx-sidebar-active-shadow);
    }
  }
}
</style>
