/**
 * 界面偏好设置：字号 / 界面密度 / 侧栏宽度 / 表格斑马纹 / 打印顶部留白
 *
 * 设计要点：
 * 1. 只影响「显示」，不触碰任何业务数据，出错最多是界面难看，不会算错东西。
 * 2. 存 localStorage（key: gx-settings），启动时 applySettings() 一次性落到
 *    document.documentElement 的 class 上，全局样式（style.css）靠这些 class 覆盖
 *    屏幕自动分档的变量。
 * 3. 全局唯一入口：页面只改 state → saveSettings() → 立即 applySettings()，无需刷新。
 */

export type FontSize = 'auto' | 's' | 'm' | 'l' | 'xl'
export type Density = 'compact' | 'normal' | 'loose'
export type SidebarWidth = 'narrow' | 'normal' | 'wide'

export interface GxSettings {
  /** 字号档；auto = 跟随屏幕分档（1366 / 1440 / 1920 / 2560 自动切） */
  fontSize: FontSize
  /** 界面密度：影响表格行高与页面间距 */
  density: Density
  /** 侧栏宽度；normal = 跟随屏幕分档 */
  sidebarWidth: SidebarWidth
  /** 表格斑马纹 */
  zebra: boolean
  /** 打印顶部留白（mm）：纸张没放正时防止二维码被切掉 */
  printTopMargin: number
}

const STORAGE_KEY = 'gx-settings'

export const DEFAULT_SETTINGS: GxSettings = {
  fontSize: 'auto',
  density: 'normal',
  sidebarWidth: 'normal',
  zebra: false,
  printTopMargin: 12
}

/** 打印顶部留白允许的取值范围（mm）——超出范围打印机容易把内容顶出边界 */
export const PRINT_TOP_MIN = 6
export const PRINT_TOP_MAX = 30

const FONT_SIZES: FontSize[] = ['auto', 's', 'm', 'l', 'xl']
const DENSITIES: Density[] = ['compact', 'normal', 'loose']
const SIDEBAR_WIDTHS: SidebarWidth[] = ['narrow', 'normal', 'wide']

/** 把所有会写进根元素的 class 集中登记，避免 apply 时漏删导致样式叠加 */
const MANAGED_CLASSES = [
  'gx-fs-s', 'gx-fs-m', 'gx-fs-l', 'gx-fs-xl',
  'gx-density-compact', 'gx-density-normal', 'gx-density-loose',
  'gx-sb-narrow', 'gx-sb-wide',
  'gx-zebra'
]

/** 校验并纠正外部传入（可能是旧版本 / 被手改过）的配置，保证永远落在合法取值内 */
function sanitize(raw: any): GxSettings {
  const s = raw || {}
  const margin = Number(s.printTopMargin)
  return {
    fontSize: FONT_SIZES.includes(s.fontSize) ? s.fontSize : DEFAULT_SETTINGS.fontSize,
    density: DENSITIES.includes(s.density) ? s.density : DEFAULT_SETTINGS.density,
    sidebarWidth: SIDEBAR_WIDTHS.includes(s.sidebarWidth) ? s.sidebarWidth : DEFAULT_SETTINGS.sidebarWidth,
    zebra: typeof s.zebra === 'boolean' ? s.zebra : DEFAULT_SETTINGS.zebra,
    printTopMargin: Number.isFinite(margin)
      ? Math.min(PRINT_TOP_MAX, Math.max(PRINT_TOP_MIN, Math.round(margin)))
      : DEFAULT_SETTINGS.printTopMargin
  }
}

/** 读取当前设置（解析失败或没有记录时返回默认值，不让脏数据把界面搞崩） */
export function getSettings(): GxSettings {
  try {
    const raw = JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}')
    return sanitize(raw)
  } catch {
    return { ...DEFAULT_SETTINGS }
  }
}

/** 把设置落到根元素 class 上（只加必要的 class，删掉其余） */
export function applySettings(settings: GxSettings = getSettings()): void {
  const root = document.documentElement
  MANAGED_CLASSES.forEach(c => root.classList.remove(c))

  // auto 不加 class，交给 style.css 里的屏幕分档 media query
  if (settings.fontSize !== 'auto') root.classList.add(`gx-fs-${settings.fontSize}`)

  // 密度始终显式声明：normal 也加，否则小屏笔记本会被 media query 自动收紧，
  // 用户选了「标准」却仍是紧凑档
  root.classList.add(`gx-density-${settings.density}`)

  if (settings.sidebarWidth !== 'normal') root.classList.add(`gx-sb-${settings.sidebarWidth}`)
  if (settings.zebra) root.classList.add('gx-zebra')
}

/** 合并保存并立即生效，返回保存后的完整配置 */
export function saveSettings(patch: Partial<GxSettings>): GxSettings {
  const next = sanitize({ ...getSettings(), ...patch })
  localStorage.setItem(STORAGE_KEY, JSON.stringify(next))
  applySettings(next)
  return next
}

/** 恢复默认（不删主题，主题由 theme.ts 单独管理） */
export function resetSettings(): GxSettings {
  localStorage.removeItem(STORAGE_KEY)
  applySettings(DEFAULT_SETTINGS)
  return { ...DEFAULT_SETTINGS }
}

/** 启动时调用一次；后续修改走 saveSettings */
export function initSettings(): void {
  applySettings()
}
