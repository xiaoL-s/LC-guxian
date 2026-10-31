<template>
  <div class="breadcrumb-tabs">
    <!-- 多标签页 -->
    <div class="tab-bar">
      <div
        v-for="tab in tabs"
        :key="tab.path"
        class="tab-item"
        :class="{ active: tab.path === route.path }"
        @click="goTab(tab)"
      >
        {{ tab.title }}
        <span v-if="tab.closable" class="tab-close" @click.stop="closeTab(tab)">×</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

/** 标签页列表：{ path, title, closable }，首页固定不可关闭 */
const tabs = ref<Array<{ path: string; title: string; closable: boolean; cacheName?: string }>>([])

/** 注入父级 keep-alive 缓存名单（同 SystemIndex provide） */
const cachedNames = inject<{ value: string[] }>('gxCachedNames', null)


/** 确保首页 tab 常驻 */
const ensureHome = () => {
  if (!tabs.value.some(t => t.path === '/system/home')) {
    tabs.value.unshift({ path: '/system/home', title: '首页看板', closable: false, cacheName: 'CsHome' })
  }
}

/** 进入新页面时自动追加标签页 */
const ensureTab = () => {
  ensureHome()
  const path = route.path
  if (path === '/' || path === '/system/home') return
  const title = (route.meta.title as string) || '页面'
  if (!tabs.value.some(t => t.path === path)) {
    const cacheName = (route.meta.cacheName as string) || undefined
    tabs.value.push({ path, title, closable: true, cacheName })
    if (cacheName && cachedNames && !cachedNames.value.includes(cacheName)) {
      cachedNames.value.push(cacheName)
    }
  }
}
watch(() => route.path, ensureTab, { immediate: true })

/** 点击标签切换 */
const goTab = (tab: { path: string }) => {
  if (tab.path !== route.path) router.push(tab.path)
}

/** 关闭标签：关闭当前页则跳转到相邻标签 */
const closeTab = (tab: { path: string }) => {
  const idx = tabs.value.findIndex(t => t.path === tab.path)
  if (idx < 0) return
  const cacheName = tab.cacheName
  tabs.value.splice(idx, 1)
  if (cacheName && cachedNames) {
    const ci = cachedNames.value.indexOf(cacheName)
    if (ci >= 0) cachedNames.value.splice(ci, 1)
  }
  if (route.path === tab.path) {
    const next = tabs.value[idx] || tabs.value[idx - 1] || tabs.value[0]
    if (next) router.push(next.path)
  }
}
</script>

<style scoped lang="scss">
.breadcrumb-tabs {
  background: #fff;
  border-radius: 10px;
  padding: 6px 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 8px;

  .tab-bar {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }

  .tab-item {
    display: inline-flex;
    align-items: center;
    padding: 5px 12px;
    font-size: 13px;
    border-radius: 7px;
    border: 1px solid transparent;
    background: #f5f7fa;
    color: #606266;
    cursor: pointer;
    user-select: none;
    transition: all 0.2s;

    &:hover {
      background: #ecf5ff;
      color: #409eff;
    }

    &.active {
      background: #fff;
      color: #409eff;
      font-weight: 600;
      border-color: #409eff;
      border-bottom: 2px solid #409eff;
    }

    .tab-close {
      margin-left: 8px;
      color: #c0c4cc;
      font-size: 14px;
      line-height: 1;
      border-radius: 50%;

      &:hover {
        color: #f56c6c;
        background: #fef0f0;
      }
    }
  }
}
</style>
