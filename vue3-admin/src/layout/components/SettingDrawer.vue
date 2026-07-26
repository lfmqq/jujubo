<template>
  <el-drawer
    v-model="visible"
    title="项目配置"
    size="300px"
    direction="rtl"
    :destroy-on-close="false"
    class="setting-drawer"
  >
    <div class="setting-body">
      <!-- 主题 -->
      <div class="setting-section">
        <div class="section-title">主题</div>
        <div class="theme-switch-wrap">
          <div class="theme-switch" :class="{ dark: themeStore.isDark }" @click="themeStore.toggle()">
            <div class="theme-thumb">
              <el-icon :size="14"><Sunny v-if="!themeStore.isDark" /><Moon v-else /></el-icon>
            </div>
          </div>
          <span class="theme-label">{{ themeStore.isDark ? '深色' : '浅色' }}</span>
        </div>
      </div>

      <!-- 布局 -->
      <div class="setting-section">
        <div class="section-title">布局</div>
        <div class="layout-list">
          <div
            v-for="layout in layouts"
            :key="layout.value"
            class="layout-thumb"
            :class="{ active: themeStore.layout === layout.value }"
            @click="themeStore.setLayout(layout.value)"
          >
            <div class="thumb-preview" :class="layout.value">
              <div class="thumb-sidebar" />
              <div class="thumb-main">
                <div class="thumb-header" />
                <div class="thumb-content" />
              </div>
            </div>
            <el-icon v-if="themeStore.layout === layout.value" class="thumb-check" :size="12"><Check /></el-icon>
          </div>
        </div>
      </div>

      <!-- 系统主题 -->
      <div class="setting-section">
        <div class="section-title">系统主题</div>
        <div class="color-list">
          <div
            v-for="c in PRESET_PRIMARY_COLORS"
            :key="c.value"
            class="color-block"
            :class="{ active: themeStore.primaryColor === c.value, light: isLightColor(c.value) }"
            :style="{ background: c.value }"
            :title="c.name"
            @click="themeStore.setPrimary(c.value)"
          >
            <el-icon v-if="themeStore.primaryColor === c.value" :size="12"><Check /></el-icon>
          </div>
          <label class="color-block custom" :class="{ active: isCustomColor('primary'), light: isLightColor(themeStore.primaryColor) }" :title="isCustomColor('primary') ? themeStore.primaryColor : '自定义颜色'">
            <input type="color" :value="themeStore.primaryColor" @change="themeStore.setPrimary($event.target.value)">
            <el-icon v-if="!isCustomColor('primary')" :size="12"><Plus /></el-icon>
            <el-icon v-else :size="12"><Check /></el-icon>
          </label>
        </div>
      </div>

      <!-- 头部主题 -->
      <div class="setting-section">
        <div class="section-title">头部主题</div>
        <div class="color-list">
          <div
            v-for="c in PRESET_HEADER_THEMES"
            :key="c.value"
            class="color-block"
            :class="{ active: themeStore.headerTheme === c.value, light: isLightColor(c.value) }"
            :style="{ background: c.value }"
            :title="c.name"
            @click="themeStore.setHeader(c.value)"
          >
            <el-icon v-if="themeStore.headerTheme === c.value" :size="12"><Check /></el-icon>
          </div>
          <label class="color-block custom" :class="{ active: isCustomColor('header'), light: isLightColor(themeStore.headerTheme) }" :title="isCustomColor('header') ? themeStore.headerTheme : '自定义颜色'">
            <input type="color" :value="themeStore.headerTheme" @change="themeStore.setHeader($event.target.value)">
            <el-icon v-if="!isCustomColor('header')" :size="12"><Plus /></el-icon>
            <el-icon v-else :size="12"><Check /></el-icon>
          </label>
        </div>
      </div>

      <!-- 菜单主题 -->
      <div class="setting-section">
        <div class="section-title">菜单主题</div>
        <div class="color-list">
          <div
            v-for="c in PRESET_MENU_THEMES"
            :key="c.value"
            class="color-block"
            :class="{ active: themeStore.menuTheme === c.value, light: isLightColor(c.value) }"
            :style="{ background: c.value }"
            :title="c.name"
            @click="themeStore.setMenu(c.value)"
          >
            <el-icon v-if="themeStore.menuTheme === c.value" :size="12"><Check /></el-icon>
          </div>
          <label class="color-block custom" :class="{ active: isCustomColor('menu'), light: isLightColor(themeStore.menuTheme) }" :title="isCustomColor('menu') ? themeStore.menuTheme : '自定义颜色'">
            <input type="color" :value="themeStore.menuTheme" @change="themeStore.setMenu($event.target.value)">
            <el-icon v-if="!isCustomColor('menu')" :size="12"><Plus /></el-icon>
            <el-icon v-else :size="12"><Check /></el-icon>
          </label>
        </div>
      </div>

      <!-- 水印配置 -->
      <div class="setting-section">
        <div class="section-title">水印</div>
        <el-input
          v-model="watermarkText"
          placeholder="请输入水印文字，留空则不显示"
          clearable
          @change="themeStore.setWatermark(watermarkText)"
        />
      </div>

      <!-- 操作按钮 -->
      <div class="setting-actions">
        <el-button type="primary" class="action-btn" @click="copyConfig">
          <el-icon class="btn-icon" :size="14"><CopyDocument /></el-icon>
          拷贝
        </el-button>
        <el-button type="danger" class="action-btn" plain @click="resetConfig">
          清除缓存并且重置
        </el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Sunny, Moon, CopyDocument, Plus } from '@element-plus/icons-vue'
import {
  useThemeStore,
  PRESET_PRIMARY_COLORS,
  PRESET_HEADER_THEMES,
  PRESET_MENU_THEMES
} from '@/stores/theme'

const visible = ref(false)
const themeStore = useThemeStore()
const watermarkText = ref(themeStore.watermark)

watch(() => themeStore.watermark, (val) => {
  watermarkText.value = val
})

const layouts = [
  { name: '左侧菜单', value: 'vertical' },
  { name: '顶部菜单', value: 'horizontal' }
]

const isLightColor = (color) => {
  const hex = color.replace('#', '')
  const r = parseInt(hex.substring(0, 2), 16)
  const g = parseInt(hex.substring(2, 4), 16)
  const b = parseInt(hex.substring(4, 6), 16)
  const brightness = (r * 299 + g * 587 + b * 114) / 1000
  return brightness > 180
}

const isCustomColor = (type) => {
  const map = {
    primary: PRESET_PRIMARY_COLORS,
    header: PRESET_HEADER_THEMES,
    menu: PRESET_MENU_THEMES
  }
  const list = map[type] || []
  const current = {
    primary: themeStore.primaryColor,
    header: themeStore.headerTheme,
    menu: themeStore.menuTheme
  }[type]
  return !list.some((c) => c.value.toLowerCase() === (current || '').toLowerCase())
}

const open = () => {
  visible.value = true
}

const copyConfig = async () => {
  const config = {
    theme: themeStore.isDark ? 'dark' : 'light',
    primaryColor: themeStore.primaryColor,
    headerTheme: themeStore.headerTheme,
    menuTheme: themeStore.menuTheme,
    layout: themeStore.layout,
    watermark: themeStore.watermark
  }
  const text = JSON.stringify(config, null, 2)
  try {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(text)
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = text
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
    }
    ElMessage.success('拷贝配置成功')
  } catch {
    ElMessage.error('拷贝失败')
  }
}

const resetConfig = () => {
  localStorage.removeItem('theme')
  localStorage.removeItem('theme_primary')
  localStorage.removeItem('theme_header')
  localStorage.removeItem('theme_menu')
  localStorage.removeItem('theme_layout')
  localStorage.removeItem('theme_watermark')
  ElMessage.success('已清除缓存，即将刷新页面')
  setTimeout(() => location.reload(), 600)
}

defineExpose({ open })
</script>

<style scoped>
.setting-body {
  padding: 8px 0;
}

.setting-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 12px;
  text-align: center;
}

/* 主题切换 */
.theme-switch-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.theme-switch {
  width: 56px;
  height: 28px;
  border-radius: 14px;
  background: var(--border-color);
  position: relative;
  cursor: pointer;
  transition: background 0.3s;
}

.theme-switch.dark {
  background: var(--text-secondary);
}

.theme-thumb {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  left: 2px;
  top: 2px;
  transition: transform 0.3s;
  color: #303133;
}

.theme-switch.dark .theme-thumb {
  transform: translateX(28px);
}

.theme-label {
  font-size: 13px;
  color: var(--text-regular);
  min-width: 36px;
}

/* 布局 */
.layout-list {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.layout-thumb {
  width: 70px;
  height: 56px;
  padding: 4px;
  border-radius: 6px;
  cursor: pointer;
  border: 2px solid transparent;
  background: var(--app-bg);
  position: relative;
  transition: border-color 0.2s;
}

.layout-thumb.active {
  border-color: var(--color-primary);
}

.thumb-preview {
  width: 100%;
  height: 100%;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
}

.thumb-preview.vertical .thumb-sidebar {
  width: 28%;
  height: 100%;
  background: #1a2332;
}

.thumb-preview.vertical .thumb-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.thumb-preview.vertical .thumb-header {
  height: 24%;
  background: #fff;
  border-bottom: 1px solid var(--border-color);
}

.thumb-preview.vertical .thumb-content {
  flex: 1;
  background: var(--app-bg);
}

.thumb-preview.horizontal .thumb-sidebar {
  width: 24%;
  height: 100%;
  background: #1a2332;
}

.thumb-preview.horizontal .thumb-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.thumb-preview.horizontal .thumb-header {
  height: 28%;
  background: #1a2332;
  opacity: 0.6;
}

.thumb-preview.horizontal .thumb-content {
  flex: 1;
  background: var(--app-bg);
}

.thumb-check {
  position: absolute;
  right: -4px;
  bottom: -4px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 颜色选择 */
.color-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
}

.color-block {
  width: 30px;
  height: 30px;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  border: 1px solid transparent;
  transition: transform 0.2s, box-shadow 0.2s;
}

.color-block:hover {
  transform: scale(1.1);
}

.color-block.active {
  box-shadow: 0 0 0 2px var(--color-primary);
}

.color-block.light {
  border-color: var(--border-color);
  color: var(--text-primary);
}

.color-block.light.active {
  box-shadow: 0 0 0 2px var(--color-primary);
}

.color-block.custom {
  position: relative;
  background: var(--app-bg);
  border: 1px dashed var(--text-secondary);
  color: var(--text-regular);
}

.color-block.custom:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.color-block.custom.active {
  background: transparent;
  box-shadow: 0 0 0 2px var(--color-primary);
}

.color-block.custom input[type='color'] {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
  border: none;
  padding: 0;
  margin: 0;
}

/* 操作按钮 */
.setting-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 32px;
  padding: 0 4px;
}

.action-btn {
  width: 100%;
  justify-content: center;
  margin: 0;
  display: flex;
}

.action-btn + .action-btn {
  margin-left: 0 !important;
}

.btn-icon {
  margin-right: 4px;
}
</style>

<style>
/* 抽屉标题居中 */
.setting-drawer .el-drawer__header {
  justify-content: center;
  margin-bottom: 0;
  padding: 16px 20px;
  color: var(--text-primary);
}

.setting-drawer .el-drawer__close-btn {
  position: absolute;
  right: 16px;
}
</style>
