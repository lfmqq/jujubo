<template>
  <div class="navbar">
    <!-- 左侧：折叠按钮 + 面包屑 -->
    <div class="navbar-left">
      <el-icon class="collapse-btn" @click="emit('toggle')">
        <Fold v-if="!isCollapse" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">
          <el-icon><Monitor /></el-icon>
        </el-breadcrumb-item>
        <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
          {{ item.meta?.title || item.name }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 右侧：用户信息 + 主题切换 + 全屏 -->
    <div class="navbar-right">
      <!-- 消息通知 -->
      <el-popover
        placement="bottom"
        :width="420"
        :fallback-placements="[]"
        trigger="click"
        :visible="notifyPopVisible"
        @show="openNotifyPop"
        @hide="notifyPopVisible = false"
        popper-class="notify-popover"
      >
        <template #reference>
          <div class="theme-toggle" @click="notifyPopVisible = !notifyPopVisible">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
              <el-icon :size="18"><Bell /></el-icon>
            </el-badge>
          </div>
        </template>
        <div class="notify-dropdown">
          <div class="notify-dropdown-header">
            <span class="notify-dropdown-title">消息通知</span>
            <el-button v-if="recentMessages.length > 0 && hasRecentUnread" type="primary" link size="small" @click="handleMarkAllRead">
              全部已读
            </el-button>
          </div>
          <div class="notify-dropdown-body">
            <template v-if="recentMessages.length === 0">
              <div class="notify-empty">暂无消息</div>
            </template>
            <template v-else>
              <div
                v-for="msg in recentMessages"
                :key="msg.id"
                class="notify-item"
                :class="{ unread: msg.readStatus === 0 }"
                @click="handleNotifyItemClick(msg)"
              >
                <div class="notify-item-top">
                  <span class="notify-item-title">{{ msg.title }}</span>
                  <el-tag :type="typeTagMap[msg.type]" size="small" class="notify-item-tag">
                    {{ typeMap[msg.type] }}
                  </el-tag>
                </div>
                <div class="notify-item-content">{{ msg.content }}</div>
                <div class="notify-item-time">{{ msg.createTime }}</div>
              </div>
            </template>
          </div>
          <div class="notify-dropdown-footer">
            <el-button type="primary" link size="small" @click="goNotifyPage">
              查看全部 →
            </el-button>
          </div>
        </div>
      </el-popover>

      <!-- 全屏 -->
      <el-tooltip :content="isFullscreen ? '退出全屏' : '全屏'" placement="bottom" effect="dark">
        <div class="theme-toggle" @click="toggleFullscreen">
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

      <!-- 主题切换 -->
      <el-tooltip content="主题切换" placement="bottom" effect="dark">
        <div class="theme-toggle" @click="themeStore.toggle()">
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
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Fold, Expand, Monitor, ArrowDown, SwitchButton, Sunny, Moon, FullScreen, User, UserFilled, Bell } from '@element-plus/icons-vue'
import { useThemeStore } from '@/stores/theme'

const props = defineProps({
  isCollapse: Boolean
})

const emit = defineEmits(['toggle'])

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const isFullscreen = ref(false)
const unreadCount = ref(0)
const notifyPopVisible = ref(false)
const recentMessages = ref([])
let pollTimer = null

const typeMap = { 1: '系统通知', 2: '提醒', 3: '私信' }
const typeTagMap = { 1: '', 2: 'warning', 3: 'success' }

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
  fetchUnreadCount()
  pollTimer = setInterval(fetchUnreadCount, 30000) // 每30秒轮询
  // 监听消息页面发出的已读事件，立即刷新角标
  window.addEventListener('notify-unread-changed', fetchUnreadCount)
})

onUnmounted(() => {
  document.removeEventListener('fullscreenchange', onFullscreenChange)
  window.removeEventListener('notify-unread-changed', fetchUnreadCount)
  if (pollTimer) clearInterval(pollTimer)
})

const breadcrumbs = computed(() => {
  return route.matched.filter(item => item.meta?.title)
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

const fetchUnreadCount = async () => {
  try {
    const res = await request.get('/system/notify/unread-count')
    unreadCount.value = res.data || 0
  } catch (e) {
    console.error('获取未读消息数失败', e)
  }
}

const fetchRecentMessages = async () => {
  try {
    const res = await request.get('/system/notify/page', {
      params: { pageNum: 1, pageSize: 5, readStatus: 0 }
    })
    recentMessages.value = res.data?.records || []
  } catch (e) {
    console.error('获取最近消息失败', e)
  }
}

const hasRecentUnread = computed(() =>
  recentMessages.value.some(m => m.readStatus === 0)
)

const openNotifyPop = () => {
  notifyPopVisible.value = true
  fetchRecentMessages()
}

const handleNotifyItemClick = async (msg) => {
  if (msg.readStatus === 0) {
    try {
      await request.put(`/system/notify/read/${msg.id}`)
      msg.readStatus = 1
      msg.readTime = new Date().toLocaleString()
      fetchUnreadCount()
      // 同步通知其他组件
      window.dispatchEvent(new CustomEvent('notify-unread-changed'))
    } catch { /* ignore */ }
  }
  notifyPopVisible.value = false
  // 动态查找消息通知路由
  const routes = router.getRoutes()
  const notifyRoute = routes.find(r => {
    const path = r.path || ''
    const name = (r.meta?.title || r.name || '')
    return path.includes('notify') || name.includes('通知') || name.includes('消息')
  })
  router.push(notifyRoute?.path || '/system/notify')
}

const handleMarkAllRead = async () => {
  try {
    await request.put('/system/notify/read-all')
    ElMessage.success('全部已读')
    recentMessages.value.forEach(m => { m.readStatus = 1 })
    fetchUnreadCount()
    window.dispatchEvent(new CustomEvent('notify-unread-changed'))
  } catch {
    ElMessage.error('操作失败')
  }
}

const goNotifyPage = () => {
  notifyPopVisible.value = false
  // 动态查找消息通知的路由，适配数据库中的实际 path 配置
  const routes = router.getRoutes()
  const notifyRoute = routes.find(r => {
    const path = r.path || ''
    const name = (r.meta?.title || r.name || '')
    return path.includes('notify') || name.includes('通知') || name.includes('消息')
  })
  const targetPath = notifyRoute?.path || '/system/notify'
  router.push(targetPath)
}
</script>

<style scoped>
.navbar {
  height: 50px;
  background: var(--navbar-bg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  border-bottom: 1px solid var(--border-light);
  flex-shrink: 0;
  z-index: 10;
  transition: background 0.3s, border-color 0.3s;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: var(--navbar-text-secondary);
  transition: color 0.2s;
  flex-shrink: 0;
}

.collapse-btn:hover {
  color: var(--color-primary);
}

/* ===== 消息通知下拉面板 ===== */
.notify-dropdown {
  max-height: 400px;
  display: flex;
  flex-direction: column;
}

.notify-dropdown-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 4px;
  flex-shrink: 0;
}

.notify-dropdown-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.notify-dropdown-body {
  overflow-y: auto;
  flex: 1;
  min-height: 60px;
}

.notify-empty {
  text-align: center;
  padding: 24px 0;
  color: var(--text-secondary);
  font-size: 13px;
}

.notify-item {
  padding: 10px 0;
  border-bottom: 1px solid var(--border-lighter);
  cursor: pointer;
  transition: background 0.15s;
}

.notify-item:last-child {
  border-bottom: none;
}

.notify-item:hover {
  background: var(--tag-hover-bg);
}

.notify-item.unread {
  position: relative;
}

.notify-item.unread::before {
  content: '';
  position: absolute;
  left: -10px;
  top: 14px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary);
}

.notify-item-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.notify-item-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 8px;
}

.notify-item-tag {
  flex-shrink: 0;
}

.notify-item-content {
  font-size: 12px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.notify-item-time {
  font-size: 11px;
  color: var(--text-placeholder);
}

.notify-dropdown-footer {
  text-align: center;
  padding-top: 8px;
  border-top: 1px solid var(--border-light);
  flex-shrink: 0;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.theme-toggle {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  color: var(--navbar-text-secondary);
  transition: all 0.2s;
}
.theme-toggle:hover {
  color: var(--color-primary);
  background: var(--tag-hover-bg);
}


.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}

.user-info:hover {
  background: var(--tag-hover-bg);
}

.username {
  font-size: 14px;
  color: var(--navbar-text);
}

::deep(.el-breadcrumb__inner),
::deep(.el-breadcrumb__separator) {
  color: var(--navbar-text-secondary) !important;
}

::deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--navbar-text) !important;
}

::deep(.el-badge__content) {
  transform: translateY(-50%) translateX(50%) !important;
  border-radius: 50% !important;
  min-width: 16px !important;
  height: 16px !important;
  line-height: 16px !important;
  padding: 0 4px !important;
  font-size: 11px !important;
  top: 5px;
  left: 14px;
}
</style>

<style>
/* 消息通知 popover 容器样式（非 scoped，因为 el-popover 挂在 body 下） */
.notify-popover {
  padding: 10px 16px !important;
}
</style>
