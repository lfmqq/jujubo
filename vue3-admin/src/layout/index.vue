<template>
  <div class="app-wrapper" :class="[themeStore.layout, { collapsed: isCollapse }]">
    <!-- 左侧菜单布局 -->
    <template v-if="themeStore.layout === 'vertical'">
      <Sidebar :is-collapse="isCollapse" />
      <div
        v-if="isMobile && !isCollapse"
        class="mobile-sidebar-mask"
        aria-hidden="true"
        @click="isCollapse = true"
      ></div>

      <div class="main-container">
        <Navbar :is-collapse="isCollapse" @toggle="isCollapse = !isCollapse" />
        <TagsView />
        <div class="app-main">
          <router-view v-slot="{ Component, route: r }">
            <transition name="fade-transform" mode="out-in">
              <keep-alive :include="cachedNames">
                <component :is="Component" :key="r.name" />
              </keep-alive>
            </transition>
          </router-view>
        </div>
      </div>
    </template>

    <!-- 顶部+左侧子菜单布局 -->
    <template v-else>
      <div class="horizontal-wrapper">
        <TopMenu />
        <!-- 下方：左侧子菜单 + 右侧内容区 -->
        <div class="horizontal-body">
          <HorizontalSidebar />
          <div class="horizontal-main">
            <TagsView />
            <div class="app-main">
              <router-view v-slot="{ Component, route: r }">
                <transition name="fade-transform" mode="out-in">
                  <keep-alive :include="cachedNames">
                    <component :is="Component" :key="r.name" />
                  </keep-alive>
                </transition>
              </router-view>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 项目配置抽屉 -->
    <SettingDrawer ref="settingDrawerRef" />

    <!-- 项目配置入口 -->
    <SettingFloatButton @click="settingDrawerRef?.open()" />

    <!-- 全局水印 -->
    <Watermark :text="themeStore.watermark" />
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import Sidebar from './components/Sidebar.vue'
import Navbar from './components/Navbar.vue'
import TopMenu from './components/TopMenu.vue'
import HorizontalSidebar from './components/HorizontalSidebar.vue'
import TagsView from './components/TagsView.vue'
import SettingDrawer from './components/SettingDrawer.vue'
import SettingFloatButton from './components/SettingFloatButton.vue'
import Watermark from '@/components/Watermark.vue'
import { useTagsViewStore } from '@/stores/tagsView'
import { useThemeStore } from '@/stores/theme'

const isCollapse = ref(false)
const tagsViewStore = useTagsViewStore()
const themeStore = useThemeStore()
const settingDrawerRef = ref(null)
const isMobile = ref(false)

// 同步移动端状态；进入手机宽度时默认收起覆盖式侧栏。
const updateMobileState = () => {
  const mobile = window.innerWidth <= 767
  if (mobile && !isMobile.value) {
    isCollapse.value = true
  }
  isMobile.value = mobile
}

// keep-alive 缓存：标签页中所有路由的 name
const cachedNames = computed(() =>
  tagsViewStore.visitedViews.map(v => v.name).filter(Boolean)
)

// 挂载后监听窗口宽度变化，使侧栏在桌面和移动端之间平滑切换。
onMounted(() => {
  updateMobileState()
  window.addEventListener('resize', updateMobileState)
})

// 页面销毁时移除窗口监听，避免重复注册。
onUnmounted(() => {
  window.removeEventListener('resize', updateMobileState)
})
</script>

<style scoped>
.app-wrapper {
  height: 100vh;
  display: flex;
  overflow: hidden;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--app-bg);
  transition: background 0.3s;
}

.horizontal-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--app-bg);
  overflow: hidden;
}

.horizontal-body {
  flex: 1;
  display: flex;
  min-height: 0;
  overflow: hidden;
}

.horizontal-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--app-bg);
}

.app-main {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.mobile-sidebar-mask {
  display: none;
}

/* 页面切换动画 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.2s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

@media (max-width: 767px) {
  .app-wrapper.vertical :deep(.sidebar-container) {
    position: fixed;
    inset: 0 auto 0 0;
    z-index: 100;
    width: 220px !important;
    transform: translateX(0);
    box-shadow: 8px 0 24px rgba(15, 23, 42, 0.24);
  }

  .app-wrapper.vertical.collapsed :deep(.sidebar-container) {
    transform: translateX(-100%);
  }

  .mobile-sidebar-mask {
    position: fixed;
    inset: 0;
    z-index: 99;
    display: block;
    background: rgba(15, 23, 42, 0.38);
  }

  .app-main {
    padding: 12px;
  }
}
</style>
