/**
 * 全局时间统一展示工具
 * 统一格式：yyyy-MM-dd HH:mm:ss（如 2026-09-12 11:00:30）
 * 兼容后端返回的 ISO 格式（带 T / 带毫秒 / UTC 后缀）以及 null / undefined / 空串
 */
export function fmtTime(t?: string | null): string {
  if (!t) return '—'
  return String(t)
    .replace('T', ' ')
    .replace(/\.\d{1,9}$/, '') // 去掉毫秒
    .slice(0, 19)
}

/** 仅日期：yyyy-MM-dd */
export function fmtDate(t?: string | null): string {
  const s = fmtTime(t)
  return s === '—' ? s : s.slice(0, 10)
}
