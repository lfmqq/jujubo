<template>
  <div class="h-sidebar" v-if="hasSubMenus">
    <!-- 当前顶级菜单标题 -->
    <div class="h-sidebar-title">
      <span>{{ menuStore.currentTopMenu?.menuName || '菜单' }}</span>
    </div>

    <!-- 子菜单 -->
    <el-scrollbar>
      <el-menu
        :default-active="activeMenu"
        router
        :background-color="sidebarBg"
        :text-color="sidebarText"
        :active-text-color="sidebarActiveText"
      >
        <MenuItem
          v-for="menu in menuStore.currentSideMenus"
          :key="menu.id"
          :menu="menu"
          :parent-path="menuStore.activeTopMenu"
        />
      </el-menu>
    </el-scrollbar>
  </div>
  <!-- 首页无子菜单时也显示一个空侧栏占位 -->
  <div class="h-sidebar h-sidebar--empty" v-else>
    <div class="h-sidebar-title">
      <span>首页</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useMenuStore } from '@/stores/menu'
import { useThemeStore } from '@/stores/theme'
import MenuItem from './MenuItem.vue'

const route = useRoute()
const menuStore = useMenuStore()
const themeStore = useThemeStore()

const sidebarBg = computed(() => themeStore.isDark ? '#1d1e1f' : themeStore.menuTheme)
const sidebarText = computed(() => themeStore.isDark ? '#a3a6ad' : (themeStore.isLightColor(themeStore.menuTheme) ? '#606266' : '#bfcbd9'))
const sidebarActiveText = computed(() => themeStore.primaryColor)

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta?.activeMenu) return meta.activeMenu
  return path
})

// 是否有子菜单可显示
const hasSubMenus = computed(() => menuStore.currentSideMenus.length > 0)
</script>

<style scoped>
.h-sidebar {
  width: 220px;
  height: 100%;
  background: var(--sidebar-bg);
  transition: background 0.3s;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex-shrink: 0;
  border-right: 1px solid color-mix(in srgb, var(--sidebar-text) 8%, transparent);
}

.h-sidebar--empty {
  width: 0;
  border-right: none;
  overflow: hidden;
}

.h-sidebar-title {
  height: 40px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  font-size: 14px;
  font-weight: 500;
  color: var(--sidebar-text);
  border-bottom: 1px solid color-mix(in srgb, var(--sidebar-text) 10%, transparent);
  flex-shrink: 0;
}

.el-scrollbar {
  flex: 1;
}

.el-menu {
  border-right: none;
}

:deep(.el-menu-item .el-icon),
:deep(.el-sub-menu__title .el-icon) {
  margin-right: 6px;
}
</style>
