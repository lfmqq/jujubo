<template>
  <div class="tags-view-container">
    <!-- 左滚动箭头 -->
    <span class="scroll-btn left" v-show="showLeftArrow" @click="scrollLeft">
      <el-icon><ArrowLeft /></el-icon>
    </span>

    <!-- 标签页滚动区域 -->
    <div class="tags-scroll-wrap" ref="scrollWrapRef" @wheel.prevent="handleWheel">
      <div class="tags-scroll-body" ref="scrollBodyRef" :style="{ transform: `translateX(${scrollLeftVal}px)` }">
        <div
          v-for="tag in visitedViews"
          :key="tag.fullPath"
          :class="['tag-item', { active: isActive(tag) }]"
          @click="goTo(tag)"
          @contextmenu.prevent="openMenu($event, tag)"
        >
          <span class="tag-dot" v-if="tag.meta?.affix"></span>
          <span class="tag-title">{{ tag.title }}</span>
          <el-icon class="tag-close" v-if="!isAffix(tag)" @click.stop="closeTag(tag)">
            <Close />
          </el-icon>
        </div>
      </div>
    </div>

    <!-- 右滚动箭头 -->
    <span class="scroll-btn right" v-show="showRightArrow" @click="scrollRight">
      <el-icon><ArrowRight /></el-icon>
    </span>

    <!-- 溢出下拉 -->
    <el-dropdown
      v-if="overflowTags.length"
      trigger="click"
      class="overflow-dropdown"
      @command="goToByPath"
    >
      <span class="overflow-btn">
        <el-icon><MoreFilled /></el-icon>
      </span>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            v-for="tag in overflowTags"
            :key="tag.fullPath"
            :command="tag.fullPath"
            :class="{ 'is-active': isActive(tag) }"
          >
            {{ tag.title }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <!-- 右键菜单 -->
    <div
      v-show="contextMenuVisible"
      class="context-menu"
      :style="{ left: contextMenuPos.x + 'px', top: contextMenuPos.y + 'px' }"
    >
      <div class="menu-item" @click="refresh">
        <el-icon><Refresh /></el-icon> 刷新页面
      </div>
      <div class="menu-item" @click="closeCurrent">
        <el-icon><Close /></el-icon> 关闭当前
      </div>
      <div class="menu-item" @click="closeOthers">
        <el-icon><Fold /></el-icon> 关闭其他
      </div>
      <div class="menu-item" :class="{ disabled: !hasLeft }" @click="closeLeft">
        <el-icon><DArrowLeft /></el-icon> 关闭左侧
      </div>
      <div class="menu-item" :class="{ disabled: !hasRight }" @click="closeRight">
        <el-icon><DArrowRight /></el-icon> 关闭右侧
      </div>
      <div class="menu-item" @click="closeAll">
        <el-icon><CircleCloseFilled /></el-icon> 关闭全部
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTagsViewStore } from '@/stores/tagsView'
import {
  ArrowLeft, ArrowRight, Close, Refresh, Fold,
  DArrowLeft, DArrowRight, CircleCloseFilled, MoreFilled
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const tagsViewStore = useTagsViewStore()

const visitedViews = computed(() => tagsViewStore.visitedViews)

// ---- 滚动 ----
const scrollWrapRef = ref(null)
const scrollBodyRef = ref(null)
const scrollLeftVal = ref(0)
const showLeftArrow = ref(false)
const showRightArrow = ref(false)

const checkScroll = () => {
  if (!scrollWrapRef.value || !scrollBodyRef.value) return
  const wrapWidth = scrollWrapRef.value.offsetWidth
  const bodyWidth = scrollBodyRef.value.scrollWidth
  showLeftArrow.value = scrollLeftVal.value < 0
  showRightArrow.value = bodyWidth + scrollLeftVal.value > wrapWidth + 1
}

const scrollLeft = () => {
  const step = 200
  scrollLeftVal.value = Math.min(scrollLeftVal.value + step, 0)
  nextTick(checkScroll)
}
const scrollRight = () => {
  if (!scrollWrapRef.value || !scrollBodyRef.value) return
  const wrapWidth = scrollWrapRef.value.offsetWidth
  const bodyWidth = scrollBodyRef.value.scrollWidth
  const min = wrapWidth - bodyWidth
  scrollLeftVal.value = Math.max(scrollLeftVal.value - 200, min)
  nextTick(checkScroll)
}
const handleWheel = (e) => {
  const delta = e.deltaY || e.detail
  if (delta > 0) scrollRight()
  else scrollLeft()
}

// ---- 溢出下拉 ----
const overflowTags = computed(() => {
  // 返回可能的溢出标签，简化处理：始终展示所有标签在下拉中
  return visitedViews.value.length > 10 ? visitedViews.value.slice(7) : []
})

// ---- 右键菜单 ----
const contextMenuVisible = ref(false)
const contextMenuPos = ref({ x: 0, y: 0 })
const contextMenuTag = ref(null)

const openMenu = (e, tag) => {
  contextMenuPos.value = { x: e.clientX, y: e.clientY }
  contextMenuTag.value = tag
  contextMenuVisible.value = true
}

const closeMenu = () => {
  contextMenuVisible.value = false
}

// ---- 标签操作 ----
const isActive = (tag) => route.fullPath === tag.fullPath
const isAffix = (tag) => tag?.meta?.affix

const goTo = (tag) => {
  if (!isActive(tag)) router.push(tag.fullPath)
}
const goToByPath = (path) => {
  router.push(path)
}

const closeTag = (tag) => {
  // 固钉标签（如首页）不允许关闭
  if (isAffix(tag)) return

  const views = tagsViewStore.visitedViews
  if (isActive(tag)) {
    // 关闭当前标签 → 跳到相邻标签
    const i = views.findIndex(v => v.fullPath === tag.fullPath)
    tagsViewStore.delView(tag)
    if (i < views.length) {
      router.push(views[i]?.fullPath || '/home')
    } else {
      router.push(views[i - 1]?.fullPath || '/home')
    }
  } else {
    tagsViewStore.delView(tag)
  }
}

const closeMenuTagIndex = computed(() => {
  if (!contextMenuTag.value) return -1
  return visitedViews.value.findIndex(v => v.fullPath === contextMenuTag.value.fullPath)
})

const hasLeft = computed(() => {
  const idx = closeMenuTagIndex.value
  if (idx <= 0) return false
  // 左侧至少有一个非固钉标签才算
  return visitedViews.value.slice(0, idx).some(v => !v.meta?.affix)
})

const hasRight = computed(() => {
  const idx = closeMenuTagIndex.value
  if (idx < 0) return false
  // 右侧至少有一个非固钉标签才算
  return visitedViews.value.slice(idx + 1).some(v => !v.meta?.affix)
})

const closeCurrent = () => {
  closeTag(contextMenuTag.value)
  closeMenu()
}

const closeOthers = () => {
  tagsViewStore.delOthersViews(contextMenuTag.value)
  closeMenu()
}

const closeLeft = () => {
  if (!hasLeft.value) return
  tagsViewStore.delLeftViews(contextMenuTag.value)
  closeMenu()
}

const closeRight = () => {
  if (!hasRight.value) return
  tagsViewStore.delRightViews(contextMenuTag.value)
  closeMenu()
}

const closeAll = () => {
  tagsViewStore.delAllViews()
  router.push('/home')
  closeMenu()
}

const refresh = () => {
  // 通过重新跳转当前路由触发刷新
  const { fullPath } = route
  router.replace({ path: '/redirect' + fullPath, query: {} })
  closeMenu()
}

// ---- 监听路由变化自动添加标签 ----
watch(
  () => route.fullPath,
  (newPath) => {
    if (newPath && !newPath.startsWith('/redirect')) {
      tagsViewStore.addView(route)
    }
    nextTick(checkScroll)
  },
  { immediate: true }
)

// 窗口大小改变时重新检查滚动
let resizeHandler
onMounted(() => {
  resizeHandler = () => checkScroll()
  window.addEventListener('resize', resizeHandler)
  document.addEventListener('click', closeMenu)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeHandler)
  document.removeEventListener('click', closeMenu)
})
</script>

<style scoped>
.tags-view-container {
  height: 34px;
  background: var(--tag-bg);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  padding: 0 4px;
  flex-shrink: 0;
  position: relative;
  user-select: none;
  transition: background 0.3s, border-color 0.3s;
}

/* 滚动按钮 */
.scroll-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-secondary);
  font-size: 12px;
  flex-shrink: 0;
  border-radius: 4px;
  transition: color 0.2s, background 0.2s;
}
.scroll-btn:hover {
  color: var(--color-primary);
  background: var(--tag-hover-bg);
}

/* 滚动区域 */
.tags-scroll-wrap {
  flex: 1;
  overflow: hidden;
  white-space: nowrap;
  height: 100%;
}
.tags-scroll-body {
  display: inline-flex;
  align-items: center;
  height: 100%;
  transition: transform 0.2s ease;
}

/* 标签项 */
.tag-item {
  display: inline-flex;
  align-items: center;
  height: 26px;
  line-height: 26px;
  padding: 0 10px;
  margin: 0 2px;
  font-size: 12px;
  color: var(--tag-text);
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;
  position: relative;
}
.tag-item:hover {
  color: var(--color-primary);
  background: var(--tag-hover-bg);
}
.tag-item.active {
  color: var(--color-primary);
  background: var(--tag-active-bg);
  border-color: var(--tag-border);
}
.tag-item.active::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary);
}
.tag-item.active .tag-title {
  margin-left: 8px;
}

/* 固钉圆点 */
.tag-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary);
  margin-right: 6px;
  flex-shrink: 0;
}

.tag-title {
  white-space: nowrap;
}

.tag-close {
  font-size: 12px;
  margin-left: 6px;
  border-radius: 50%;
  width: 14px;
  height: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.tag-close:hover {
  color: #fff;
  background: #ccc;
}

/* 溢出下拉 */
.overflow-dropdown {
  margin-left: 4px;
}
.overflow-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 4px;
  color: var(--text-secondary);
  font-size: 14px;
  transition: color 0.2s, background 0.2s;
}
.overflow-btn:hover {
  color: var(--color-primary);
  background: var(--tag-hover-bg);
}

/* 右键菜单 */
.context-menu {
  position: fixed;
  z-index: 3000;
  background: var(--card-bg);
  border-radius: 6px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
  padding: 4px 0;
  min-width: 140px;
  border: 1px solid var(--border-light);
  transition: background 0.3s;
}
.menu-item {
  padding: 8px 16px;
  font-size: 13px;
  color: var(--text-primary);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}
.menu-item:hover {
  background: var(--tag-hover-bg);
  color: var(--color-primary);
}
.menu-item.disabled {
  color: #c0c4cc;
  cursor: not-allowed;
  pointer-events: none;
}
</style>
