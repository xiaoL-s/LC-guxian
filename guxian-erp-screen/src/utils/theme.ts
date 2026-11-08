/**
 * 主题切换工具：管理 5 套主题的切换与持久化
 * 用法：
 *   const { themes, currentTheme, applyTheme } = useTheme()
 *   applyTheme('mint')  // 切换并写入 localStorage
 * 启动时调用 initTheme() 恢复上次主题
 */

export interface ThemeOption {
  key: string
  name: string
  desc: string
  /** 侧边栏主色（用于下拉菜单里的色点预览） */
  color: string
}

export const THEMES: ThemeOption[] = [
  { key: 'qing', name: '浅氧青', desc: '清爽默认 · 浅蓝', color: '#409eff' },
  { key: 'purple', name: '芋泥奶紫', desc: '温柔低饱和紫', color: '#8b6fd0' },
  { key: 'mint', name: '薄荷奶绿', desc: '清新活力薄荷', color: '#26b894' },
  { key: 'orange', name: '蜜柚暖橙', desc: '温暖明快蜜柚', color: '#f08c33' },
  { key: 'gray', name: '极简灰白', desc: '高级简约黑白灰', color: '#586675' }
]

const STORAGE_KEY = 'gx-theme'

export function getStoredTheme(): string {
  const saved = localStorage.getItem(STORAGE_KEY)
  return THEMES.some(t => t.key === saved) ? (saved as string) : 'qing'
}

export function applyTheme(key: string): void {
  const themeKey = THEMES.some(t => t.key === key) ? key : 'qing'
  const root = document.documentElement
  if (themeKey === 'qing') {
    root.removeAttribute('data-theme')
  } else {
    root.setAttribute('data-theme', themeKey)
  }
}

/** 应用已保存的主题（页面加载时调用一次） */
export function initTheme(): void {
  applyTheme(getStoredTheme())
}

export function saveTheme(key: string): void {
  localStorage.setItem(STORAGE_KEY, key)
  applyTheme(key)
}

export function currentTheme(): ThemeOption {
  return THEMES.find(t => t.key === getStoredTheme()) || THEMES[0]
}
