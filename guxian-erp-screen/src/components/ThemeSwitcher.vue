<template>
  <el-dropdown trigger="click" placement="bottom-end" @command="handleSelect">
    <button class="theme-switcher" title="切换主题">
      <span class="theme-dot" :style="{ background: current.color }"></span>
      <span class="theme-label">主题</span>
    </button>
    <template #dropdown>
      <el-dropdown-menu class="theme-menu">
        <el-dropdown-item
          v-for="t in themes"
          :key="t.key"
          :command="t.key"
          :class="{ 'is-active': t.key === current.key }"
        >
          <span class="theme-item">
            <span class="theme-item-dot" :style="{ background: t.color }"></span>
            <span class="theme-item-name">{{ t.name }}</span>
            <span class="theme-item-desc">{{ t.desc }}</span>
            <span v-if="t.key === current.key" class="theme-item-check">✓</span>
          </span>
        </el-dropdown-item>
        <el-dropdown-item divided disabled class="theme-menu-tip">
          主题仅影响侧边栏/顶栏/主按钮，表格与打印样式不受影响
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { THEMES, initTheme, currentTheme, saveTheme, applyTheme, THEME_CHANGE_EVENT, type ThemeOption } from '@/utils/theme'

const themes = THEMES
const current = ref<ThemeOption>(currentTheme())

onMounted(() => {
  initTheme()
  current.value = currentTheme()
})

// 设置页里切换主题时，顶栏这个小圆点也要跟着变
const onThemeChange = () => { current.value = currentTheme() }
window.addEventListener(THEME_CHANGE_EVENT, onThemeChange)
onUnmounted(() => window.removeEventListener(THEME_CHANGE_EVENT, onThemeChange))

const handleSelect = (key: string) => {
  saveTheme(key)
  current.value = currentTheme()
  applyTheme(key)
}
</script>

<style scoped lang="scss">
.theme-switcher {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 30px;
  padding: 0 10px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #f5f7fa;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;

  &:hover {
    border-color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
  }

  .theme-dot {
    width: 14px;
    height: 14px;
    border-radius: 50%;
    border: 2px solid rgba(255, 255, 255, 0.9);
    box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.12);
    flex-shrink: 0;
  }
  .theme-label {
    font-size: 12.5px;
    color: #444;
  }
}

.theme-item {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 180px;

  .theme-item-dot {
    width: 14px;
    height: 14px;
    border-radius: 50%;
    border: 2px solid rgba(255, 255, 255, 0.9);
    box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.1);
    flex-shrink: 0;
  }
  .theme-item-name {
    font-weight: 600;
    font-size: 13px;
    color: #303133;
  }
  .theme-item-desc {
    font-size: 12px;
    color: #909399;
    flex: 1;
  }
  .theme-item-check {
    color: var(--el-color-primary);
    font-weight: 700;
  }
}

:deep(.theme-menu .el-dropdown-menu__item.is-active) {
  background: var(--el-color-primary-light-9);
}
:deep(.theme-menu-tip) {
  font-size: 11.5px;
  color: #909399;
  cursor: default;
  line-height: 1.5;
  white-space: normal;
  height: auto;
}
</style>
