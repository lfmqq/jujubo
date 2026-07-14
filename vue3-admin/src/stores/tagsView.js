import { defineStore } from 'pinia'
import { useUserStore } from '@/stores/user'

export const useTagsViewStore = defineStore('tagsView', {
  state: () => ({
    visitedViews: []   // 已打开的标签页路由对象列表
  }),
  getters: {
    // 固钉标签：首页始终保留
    affixViews(state) {
      return state.visitedViews.filter(v => v?.meta?.affix)
    }
  },
  actions: {
    /** 添加标签页 */
    addView(view) {
      // 跳过不需要标签页的路由
      if (view.meta?.noTagsView) return
      // 已存在则跳过
      if (this.visitedViews.some(v => v.fullPath === view.fullPath)) return
      this.visitedViews.push({
        ...view,
        title: view.meta?.title || '未命名'
      })
    },

    /** 删除标签页（固钉标签不允许删除，如首页） */
    delView(view) {
      if (view?.meta?.affix) return
      const i = this.visitedViews.findIndex(v => v.fullPath === view.fullPath)
      if (i > -1) this.visitedViews.splice(i, 1)
    },

    /** 关闭其他标签页 */
    delOthersViews(view) {
      this.visitedViews = this.visitedViews.filter(
        v => v?.meta?.affix || v.fullPath === view.fullPath
      )
    },

    /** 关闭左侧标签页 */
    delLeftViews(view) {
      const i = this.visitedViews.findIndex(v => v.fullPath === view.fullPath)
      if (i > -1) {
        this.visitedViews = this.visitedViews.filter(
          (v, idx) => v?.meta?.affix || v.fullPath === view.fullPath || idx > i
        )
      }
    },

    /** 关闭右侧标签页 */
    delRightViews(view) {
      const i = this.visitedViews.findIndex(v => v.fullPath === view.fullPath)
      if (i > -1) {
        this.visitedViews = this.visitedViews.filter(
          (v, idx) => v?.meta?.affix || v.fullPath === view.fullPath || idx < i
        )
      }
    },

    /** 关闭所有标签页 */
    delAllViews() {
      const userStore = useUserStore()
      this.visitedViews = userStore.token
        ? this.visitedViews.filter(v => v?.meta?.affix)
        : []
    },

    /** 登出时清空 */
    clearViews() {
      this.visitedViews = []
    }
  }
})
