import { defineStore } from 'pinia'

// 芋道风格常用主题色
export const PRESET_PRIMARY_COLORS = [
  { name: '苍穹蓝', value: '#409EFF' },
  { name: '极客黑', value: '#000000' },
  { name: '微风蓝', value: '#5ac8fa' },
  { name: '薄荷绿', value: '#3dd598' },
  { name: '活力红', value: '#ff6b6b' },
  { name: '阳光橙', value: '#ff9500' },
  { name: '优雅紫', value: '#9b59b6' },
  { name: '活力橙', value: '#ff9f43' }
]

export const PRESET_HEADER_THEMES = [
  { name: '默认白', value: '#ffffff' },
  { name: '极客黑', value: '#000000' },
  { name: '科技蓝', value: '#304156' },
  { name: '薄荷绿', value: '#11a983' },
  { name: '活力红', value: '#f56c6c' },
  { name: '苍穹蓝', value: '#409EFF' },
  { name: '活力橙', value: '#e65c00' },
  { name: '湖水蓝', value: '#0096c7' }
]

export const PRESET_MENU_THEMES = [
  { name: '科技蓝', value: '#304156' },
  { name: '深邃蓝', value: '#001529' },
  { name: '薄荷绿', value: '#11a983' },
  { name: '活力红', value: '#f56c6c' },
  { name: '苍穹蓝', value: '#409EFF' },
  { name: '活力橙', value: '#e65c00' },
  { name: '湖水蓝', value: '#0096c7' },
  { name: '薰衣草', value: '#6959cd' }
]

// 从 localStorage 读取或取默认值
function loadTheme(key, fallback) {
  try {
    const v = localStorage.getItem(key)
    return v || fallback
  } catch {
    return fallback
  }
}

export const useThemeStore = defineStore('theme', {
  state: () => ({
    isDark: localStorage.getItem('theme') === 'dark',
    primaryColor: loadTheme('theme_primary', '#409EFF'),
    headerTheme: loadTheme('theme_header', '#ffffff'),
    menuTheme: loadTheme('theme_menu', '#304156'),
    layout: loadTheme('theme_layout', 'vertical')
  }),
  actions: {
    toggle() {
      this.isDark = !this.isDark
      localStorage.setItem('theme', this.isDark ? 'dark' : 'light')
      document.documentElement.classList.toggle('dark', this.isDark)
      this.applyColorVars()
    },

    setPrimary(color) {
      this.primaryColor = color
      localStorage.setItem('theme_primary', color)
      this.applyColorVars()
    },

    setHeader(color) {
      this.headerTheme = color
      localStorage.setItem('theme_header', color)
      this.applyColorVars()
    },

    setMenu(color) {
      this.menuTheme = color
      localStorage.setItem('theme_menu', color)
      this.applyColorVars()
    },

    setLayout(layout) {
      this.layout = layout
      localStorage.setItem('theme_layout', layout)
    },

    /** 应用初始化时调用：恢复所有主题配置 */
    initTheme() {
      document.documentElement.classList.toggle('dark', this.isDark)
      this.applyColorVars()
    },

    applyColorVars() {
      const root = document.documentElement
      root.style.setProperty('--color-primary', this.primaryColor)
      root.style.setProperty('--el-color-primary', this.primaryColor)
      root.style.setProperty('--el-color-primary-light-3', this.tint(this.primaryColor, 0.3))
      root.style.setProperty('--el-color-primary-light-5', this.tint(this.primaryColor, 0.5))
      root.style.setProperty('--el-color-primary-light-7', this.tint(this.primaryColor, 0.7))
      root.style.setProperty('--el-color-primary-light-8', this.tint(this.primaryColor, 0.8))
      root.style.setProperty('--el-color-primary-light-9', this.tint(this.primaryColor, 0.9))
      root.style.setProperty('--el-color-primary-dark-2', this.shade(this.primaryColor, 0.2))

      // 暗色模式强制使用暗色头部/侧边栏，浅色模式使用用户配置
      if (this.isDark) {
        root.style.setProperty('--navbar-bg', '#1d1e1f')
        root.style.setProperty('--navbar-text', '#e5eaf3')
        root.style.setProperty('--navbar-text-secondary', '#a3a6ad')
        root.style.setProperty('--sidebar-bg', '#1d1e1f')
        root.style.setProperty('--sidebar-text', '#a3a6ad')
        root.style.setProperty('--sidebar-hover', 'rgba(255,255,255,0.06)')
        root.style.setProperty('--sidebar-active-bg', this.hexToRgba(this.primaryColor, 0.15))
        root.style.setProperty('--sidebar-active-text', this.primaryColor)
      } else {
        root.style.setProperty('--navbar-bg', this.headerTheme)
        root.style.setProperty('--navbar-text', this.isLightColor(this.headerTheme) ? '#303133' : '#e5eaf3')
        root.style.setProperty('--navbar-text-secondary', this.isLightColor(this.headerTheme) ? '#606266' : '#a3a6ad')
        root.style.setProperty('--sidebar-bg', this.menuTheme)
        // 根据菜单背景色自动计算文字/悬停/激活色，保证可读性
        const isLight = this.isLightColor(this.menuTheme)
        root.style.setProperty('--sidebar-text', isLight ? '#606266' : '#bfcbd9')
        root.style.setProperty('--sidebar-hover', isLight ? this.shade(this.menuTheme, 0.05) : this.shade(this.menuTheme, 0.15))
        root.style.setProperty('--sidebar-active-bg', this.hexToRgba(this.primaryColor, 0.15))
        root.style.setProperty('--sidebar-active-text', this.primaryColor)
      }
    },

    // 判断颜色是否为浅色，用于菜单文字色
    isLightColor(color) {
      const hex = color.replace('#', '')
      const r = parseInt(hex.substring(0, 2), 16)
      const g = parseInt(hex.substring(2, 4), 16)
      const b = parseInt(hex.substring(4, 6), 16)
      // 计算亮度
      const brightness = (r * 299 + g * 587 + b * 114) / 1000
      return brightness > 180
    },

    // 颜色变浅
    tint(color, amount) {
      const r = Math.round(parseInt(color.slice(1, 3), 16) + (255 - parseInt(color.slice(1, 3), 16)) * amount)
      const g = Math.round(parseInt(color.slice(3, 5), 16) + (255 - parseInt(color.slice(3, 5), 16)) * amount)
      const b = Math.round(parseInt(color.slice(5, 7), 16) + (255 - parseInt(color.slice(5, 7), 16)) * amount)
      return `rgb(${r}, ${g}, ${b})`
    },

    // 颜色变深
    shade(color, amount) {
      const r = Math.round(parseInt(color.slice(1, 3), 16) * (1 - amount))
      const g = Math.round(parseInt(color.slice(3, 5), 16) * (1 - amount))
      const b = Math.round(parseInt(color.slice(5, 7), 16) * (1 - amount))
      return `rgb(${r}, ${g}, ${b})`
    },

    // hex转rgba
    hexToRgba(color, alpha) {
      const r = parseInt(color.slice(1, 3), 16)
      const g = parseInt(color.slice(3, 5), 16)
      const b = parseInt(color.slice(5, 7), 16)
      return `rgba(${r}, ${g}, ${b}, ${alpha})`
    }
  }
})
