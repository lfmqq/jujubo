import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 所有 Element Plus 图标组件（PascalCase）
const ICONS = ElementPlusIconsVue

// 全部图标名称（PascalCase），排序后
export const iconNames = Object.keys(ICONS)
  .filter(k => k !== 'default')
  .sort((a, b) => a.localeCompare(b))

// 名称规范：kebab-case -> PascalCase
export function toPascalCase(name) {
  if (!name) return ''
  return name.replace(/(^|-)([a-z])/g, (_, __, letter) => letter.toUpperCase())
}

// 名称规范：PascalCase -> kebab-case
export function toKebabCase(name) {
  if (!name) return ''
  return name
    .replace(/([a-z0-9])([A-Z])/g, '$1-$2')
    .replace(/([A-Z])([A-Z][a-z])/g, '$1-$2')
    .toLowerCase()
}

// 根据名称（支持 PascalCase / kebab-case）获取图标组件
export function getIcon(name) {
  if (!name) return null
  if (ICONS[name]) return ICONS[name]
  const pascal = toPascalCase(name)
  return ICONS[pascal] || null
}
