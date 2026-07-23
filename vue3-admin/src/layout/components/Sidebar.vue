<template>
  <div class="sidebar-container" :class="{ collapsed: isCollapse }">
    <!-- Logo 区域 -->
    <div class="sidebar-logo" @click="router.push('/home')">
      <img src="/logo.png" class="sidebar-logo-img" />
      <transition name="fade">
        <span v-show="!isCollapse" class="sidebar-logo-text">桔桔波管理系统</span>
      </transition>
    </div>

    <!-- 菜单 -->
    <el-scrollbar>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        :background-color="sidebarBg"
        :text-color="sidebarText"
        :active-text-color="sidebarActiveText"
      >
        <!-- 动态菜单（含首页，菜单数据由接口返回） -->
        <MenuItem
          v-for="menu in menuStore.menuList"
          :key="menu.id"
          :menu="menu"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMenuStore } from '@/stores/menu'
import { useThemeStore } from '@/stores/theme'
import MenuItem from './MenuItem.vue'

defineProps({
  isCollapse: Boolean
})

const route = useRoute()
const router = useRouter()
const menuStore = useMenuStore()
const themeStore = useThemeStore()

const sidebarBg = computed(() => themeStore.isDark ? '#1d1e1f' : themeStore.menuTheme)
const sidebarText = computed(() => themeStore.isDark ? '#a3a6ad' : (themeStore.isLightColor(themeStore.menuTheme) ? '#606266' : '#8899b4'))
const sidebarActiveText = computed(() => themeStore.primaryColor)

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta?.activeMenu) return meta.activeMenu
  return path
})
</script>

<style scoped>
.sidebar-container {
  width: 220px;
  height: 100%;
  background: var(--sidebar-bg);
  transition: width 0.28s, background 0.3s;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex-shrink: 0;
}

.sidebar-container.collapsed {
  width: 64px;
}

.sidebar-logo {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  border-bottom: 1px solid color-mix(in srgb, var(--sidebar-text) 12%, transparent);
  flex-shrink: 0;
}

.sidebar-logo-img {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  flex-shrink: 0;
  object-fit: cover;
}

.sidebar-logo-text {
  font-size: 16px;
  font-weight: 700;
  color: var(--sidebar-text);
  white-space: nowrap;
}

.el-scrollbar {
  flex: 1;
}

.el-menu {
  border-right: none;
}

:deep(.el-menu--collapse) {
  width: 64px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 修复折叠时 sub-menu 弹出层级问题 */
:deep(.el-menu--collapse .el-sub-menu.is-active .el-sub-menu__title) {
  color: var(--color-primary);
}

/* 子菜单选中背景色 */
:deep(.el-menu-item.is-active) {
  background-color: var(--sidebar-active-bg) !important;
}

/* 父级菜单只高亮字体，不加背景 */
:deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  background-color: transparent !important;
  color: var(--sidebar-active-text) !important;
}

/* 菜单项内图标与文字间距 */
:deep(.el-menu-item .el-icon),
:deep(.el-sub-menu__title .el-icon) {
  margin-right: 6px;
}
</style>
