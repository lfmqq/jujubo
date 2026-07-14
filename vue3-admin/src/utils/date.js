/**
 * 日期工具函数
 *
 * 后端 JacksonConfig 已将 LocalDateTime 统一序列化为 yyyy-MM-dd HH:mm:ss，
 * 前端本工具提供通用格式化/解析/相对时间等功能。
 */

const pad = (n) => String(n).padStart(2, '0')

/**
 * 格式化日期（字符串或 Date）
 * @param {string|Date|number} date 日期值
 * @param {string} [fmt='yyyy-MM-dd HH:mm:ss'] 格式模板
 * @returns {string}
 */
export function formatDate(date, fmt = 'yyyy-MM-dd HH:mm:ss') {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  const o = {
    'y+': d.getFullYear(),
    'M+': d.getMonth() + 1,
    'd+': d.getDate(),
    'H+': d.getHours(),
    'm+': d.getMinutes(),
    's+': d.getSeconds(),
    'S+': d.getMilliseconds()
  }
  for (const [k, v] of Object.entries(o)) {
    const reg = new RegExp('(' + k + ')')
    if (reg.test(fmt)) {
      const len = RegExp.$1.length
      fmt = fmt.replace(reg, len === 2 ? pad(v) : String(v))
    }
  }
  return fmt
}

/**
 * 格式化为 yyyy-MM-dd
 */
export function formatDateOnly(date) {
  return formatDate(date, 'yyyy-MM-dd')
}

/**
 * 格式化为 HH:mm:ss
 */
export function formatTimeOnly(date) {
  return formatDate(date, 'HH:mm:ss')
}

/**
 * 人性化相对时间
 * @param {string|Date} date
 * @returns {string} 如 "刚刚"、"5分钟前"、"2小时前"、"3天前"
 */
export function fromNow(date) {
  if (!date) return ''
  const now = Date.now()
  const diff = now - new Date(date).getTime()
  const seconds = Math.floor(diff / 1000)
  if (seconds < 0) return '刚刚'
  if (seconds < 60) return '刚刚'
  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) return minutes + '分钟前'
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return hours + '小时前'
  const days = Math.floor(hours / 24)
  if (days < 30) return days + '天前'
  const months = Math.floor(days / 30)
  if (months < 12) return months + '个月前'
  return Math.floor(months / 12) + '年前'
}
