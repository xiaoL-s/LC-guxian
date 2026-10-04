<template>
  <div class="sidebar" :class="{ collapse: isCollapse }">
    <template v-for="item in menuList" :key="item.id">
      <div class="menu-item">
        <!-- 收缩状态禁止展开子菜单 !isCollapse -->
        <div class="menu-title" @click="!isCollapse && onMenuClick(item)">
          <component
            v-if="item.icon"
            :is="iconMap[item.icon]"
            class="menu-icon"
          />
          <span v-if="!isCollapse" class="menu-text">{{ item.menuName }}</span>

          <span
            v-if="!isCollapse && item.children && item.children.length > 0"
            class="arrow"
            :class="{ expand: item._expand }"
          >></span>
        </div>

        <!-- 收缩状态直接隐藏子菜单 -->
        <div v-show="!isCollapse && item._expand" class="sub-menu">
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
import { defineProps } from 'vue'
import { useRouter } from 'vue-router'
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

// 图标映射：数据库icon字段和key严格一致
const router = useRouter()

// 图标映射：数据库icon字段和key严格一致
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

defineProps<{
  menuList: any[]
  isCollapse: boolean
}>()

const toggleExpand = (item: any) => {
  item._expand = !item._expand
}

/** 顶级菜单点击：有子菜单则展开/收起；无子菜单直接跳转 */
const onMenuClick = (item: any) => {
  if (item.children && item.children.length > 0) {
    toggleExpand(item)
  } else if (item.path) {
    router.push(item.path)
  }
}

console.log("iconMap keys：", Object.keys(iconMap))
</script>

<style scoped lang="scss">
.sidebar {
  width: var(--gx-sidebar-w, 180px);
  height: 100vh;
  padding: 20px 12px;
  box-sizing: border-box;
  background: linear-gradient(180deg, #223040 0%, #2c3e50 100%);
  color: #ecf0f1;
  overflow-y: auto;
  overflow-x: hidden; /* ✅关键，横向溢出隐藏 */
  font-family: "PingFang SC", "Microsoft YaHei", "Helvetica Neue", Arial, sans-serif;
  /* ✅调整动画时间+缓动曲线 */
  transition: width 0.34s cubic-bezier(0.2, 0, 0.2, 1);

  &::-webkit-scrollbar {
    width: 5px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.12);
    border-radius: 4px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }

  /* 收缩模式 */
  &.collapse {
    width: 64px;
    padding: 20px 8px;
  }
}

.menu-item {
  margin-bottom: 6px;

  .menu-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    border-radius: 10px;
    cursor: pointer;
    transition: 0.22s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.07);
    }
  }

  .menu-icon {
    font-size: 17px;
    margin-right: 5px;
    flex-shrink: 0;
  }

  .menu-text {
    flex: 1;
    font-weight: 500;
    font-size: 13px;
    letter-spacing: 0.2px;
    margin-right: 6px;
    white-space: nowrap; /* ✅禁止文字换行 */
    overflow: hidden;
    max-width: 160px;
    transition: max-width 0.34s cubic-bezier(0.2, 0, 0.2, 1);
  }

  .arrow {
    font-size: 11px;
    color: #a0aebf;
    transition: transform 0.28s ease;
    flex-shrink: 0;
    &.expand {
      transform: rotate(90deg);
    }
  }

  /* 收缩状态 */
  :deep(.collapse) & .menu-title {
    justify-content: center;
    padding: 12px 0;
  }
  :deep(.collapse) & .menu-icon {
    margin-right: 0;
  }
  /* 收缩时文字max‑width变成0，文字平滑消失，不会挤压字体 */
  :deep(.collapse) & .menu-text {
    max-width: 0;
    margin-right: 0;
  }
  :deep(.collapse) & .arrow {
    opacity: 0;
  }
}

.sub-menu {
  margin: 6px 0 10px 14px;
  padding-left: 14px;
  border-left: 1px solid rgba(208, 219, 208, 0.473);

  .sub-item {
    display: flex;
    align-items: center;
    padding: 10px 14px;
    margin-bottom: 4px;
    border-radius: 9px;
    color: #b0c2d6;
    text-decoration: none;
    font-size: 12.6px;
    transition: all 0.22s ease;

    .sub-icon {
      font-size: 15px;
      margin-right: 5px;
      flex-shrink: 0;
    }

    &:hover {
      background: rgba(255, 255, 255, 0.06);
      color: #ffffff;
    }

    &.active {
      color: #ffffff;
      background: rgba(64, 158, 255, 0.22);
      box-shadow: 0 1px 6px rgba(64, 158, 255, 0.15);
    }
  }
}
</style>