<template>
  <div class="top-menu-container">
    <div class="top-menu-left">
      <!-- Logo -->
      <div class="logo" @click="router.push('/home')">
        <img src="/logo.png" class="logo-img" />
        <span class="logo-text">桔桔波管理系统</span>
      </div>

      <!-- 横向顶级菜单（仅第一层） -->
      <el-menu
        :default-active="activeTopMenu"
        mode="horizontal"
        :background-color="menuBg"
        :text-color="menuText"
        :active-text-color="activeText"
        class="top-menu"
      >
        <el-menu-item
          v-for="menu in menuStore.menuList"
          :key="menu.id"
          :index="menu.path"
          @click="handleTopClick(menu.path)"
        >
          <el-icon v-if="menu.icon">
            <component :is="getIcon(menu.icon) || Menu" />
          </el-icon>

          <span>{{ menu.menuName }}</span>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 右侧：用户信息 + 主题切换 + 全屏 -->
    <div class="top-menu-right">
      <el-tooltip :content="isFullscreen ? '退出全屏' : '全屏'" placement="bottom" effect="dark">
        <div class="tool-btn" @click="toggleFullscreen">
          <el-icon :size="18">
            <FullScreen v-if="!isFullscreen" />
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M8 3v5H3" />
              <path d="M16 3v5h5" />
              <path d="M8 21v-5H3" />
              <path d="M16 21v-5h5" />
            </svg>
          </el-icon>
        </div>
      </el-tooltip>

      <el-tooltip content="主题切换" placement="bottom" effect="dark">
        <div class="tool-btn" @click="themeStore.toggle()">
          <el-icon :size="18">
            <Sunny v-if="themeStore.isDark" />
            <Moon v-else />
          </el-icon>
        </div>
      </el-tooltip>

      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32" :src="userStore.avatar">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
          <span class="username">{{ userStore.nickname || userStore.username || '管理员' }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              个人中心
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMenuStore } from '@/stores/menu'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { ArrowDown, SwitchButton, Sunny, Moon, FullScreen, Menu, User, UserFilled } from '@element-plus/icons-vue'
import { useThemeStore } from '@/stores/theme'
import { getIcon } from '@/utils/icons'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const menuStore = useMenuStore()
const themeStore = useThemeStore()

const isFullscreen = ref(false)

const menuBg = computed(() => themeStore.isDark ? '#1d1e1f' : themeStore.menuTheme)
const menuText = computed(() => themeStore.isDark ? '#a3a6ad' : (themeStore.isLightColor(themeStore.menuTheme) ? '#606266' : '#8899b4'))
const activeText = computed(() => themeStore.primaryColor)

// 顶部菜单高亮：当前激活的顶级菜单 path
const activeTopMenu = computed(() => menuStore.activeTopMenu || route.path)


// 根据 path 递归查找菜单节点
const findMenuByPath = (list, targetPath) => {
  for (const m of list) {
    if (m.path === targetPath) return m
    if (m.children?.length) {
      const found = findMenuByPath(m.children, targetPath)
      if (found) return found
    }
  }
  return null
}

/** 确保路径以 / 开头 */
const ensureLeadingSlash = (p) => (p ? (p.startsWith('/') ? p : '/' + p) : '')

/**
 * 获取菜单的第一个可导航子菜单的完整路径（拼接父子路径）
 */
const getFirstChildPath = (menu, parentPath = '') => {
  if (!menu?.children?.length) return ensureLeadingSlash(menu?.path)
  const base = parentPath || ensureLeadingSlash(menu.path).replace(/\/$/, '')
  for (const child of menu.children) {
    if (child.children?.length) {
      const deep = getFirstChildPath(child, base)
      if (deep) return deep
    } else if (child.path) {
      const childPath = child.path.startsWith('/') ? child.path : `${base}/${child.path}`
      return childPath
    }
  }
  const first = menu.children[0]
  if (first?.path) {
    return first.path.startsWith('/') ? first.path : `${base}/${first.path}`
  }
  return base
}

// 点击顶级菜单
const handleTopClick = (path) => {
  const fullPath = ensureLeadingSlash(path)
  menuStore.setActiveTopMenu(fullPath)
  // 如果该菜单有子菜单，跳到第一个子菜单；否则直接跳转
  const menu = findMenuByPath(menuStore.menuList, path)
  const target = menu?.children?.length ? getFirstChildPath(menu) : fullPath
  if (target && target !== route.path) {
    router.push(target)
  }
}

// 监听路由变化，自动匹配顶级菜单
watch(
  () => route.path,
  (newPath) => {
    menuStore.matchTopMenu(newPath)
  },
  { immediate: true }
)

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

const onFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement
}

onMounted(() => {
  document.addEventListener('fullscreenchange', onFullscreenChange)
})

onUnmounted(() => {
  document.removeEventListener('fullscreenchange', onFullscreenChange)
})

const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/system/user/profile')
  } else if (command === 'logout') {
    userStore.logout()
    try { await request.post('/auth/logout') } catch { /* ignore */ }
    ElMessage.success('退出成功')
    router.push('/login')
  }
}
</script>

<style scoped>
.top-menu-container {
  height: 50px;
  background: var(--sidebar-bg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
  z-index: 10;
  transition: background 0.3s;
}

.top-menu-left {
  display: flex;
  align-items: center;
  height: 100%;
  min-width: 0;
  flex: 1;
}

.logo {
  height: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px 0 0;
  cursor: pointer;
  flex-shrink: 0;
  border-right: 1px solid color-mix(in srgb, var(--sidebar-text) 12%, transparent);
}

.logo-img {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  flex-shrink: 0;
  object-fit: cover;
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: var(--sidebar-text);
  white-space: nowrap;
}

.top-menu {
  flex: 1;
  border-bottom: none;
  height: 100%;
  background: transparent !important;
  overflow: hidden;
}

:deep(.top-menu.el-menu--horizontal > .el-menu-item) {
  height: 50px;
  line-height: 50px;
}

:deep(.top-menu.el-menu--horizontal > .el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.08) !important;
  border-bottom-color: var(--color-primary) !important;
}

.top-menu-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  margin-left: 12px;
}

.tool-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  color: var(--sidebar-text);
  transition: all 0.2s;
}

.tool-btn:hover {
  color: var(--color-primary);
  background: rgba(255, 255, 255, 0.08);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
  color: var(--sidebar-text);
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.08);
}

.username {
  font-size: 14px;
}

:deep(.el-avatar) {
  background: var(--color-primary);
}
</style>
