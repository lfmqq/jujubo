import { defineStore } from 'pinia'
import request from '@/utils/request'

/**
 * 字典数据缓存 Store
 *
 * 特性：
 * - 按 dictType 懒加载，首次使用时从后端拉取并缓存
 * - 支持手动刷新单个或多个类型的字典数据
 * - 避免同一字典类型重复请求
 */
export const useDictStore = defineStore('dict', {
  state: () => ({
    /** dictType → DictData[] 缓存映射 */
    dictMap: {},
    /** 正在加载中的 dictType 集合（防并发重复请求） */
    loadingSet: new Set()
  }),

  actions: {
    /**
     * 获取指定 dictType 的字典数据列表（同步返回，模板中可直接调用）
     *
     * 首次调用时触发后台加载，立即返回空数组（Pinia 响应式，加载完成后自动更新视图）
     * 后续调用直接返回缓存数据
     *
     * @param {string} dictType 字典类型编码
     * @returns {Array} 字典数据数组（响应式）
     */
    getDictByType(dictType) {
      if (!dictType) return []
      // 已缓存则直接返回
      if (this.dictMap[dictType]) return this.dictMap[dictType]
      // 未缓存：置空数组 → 触发后台加载 → 加载完成后 vue 响应式自动更新
      this.dictMap[dictType] = []
      this._loadDictData(dictType)
      return this.dictMap[dictType]
    },

    /**
     * 加载字典数据（后台异步，带并发锁）
     */
    async _loadDictData(dictType) {
      if (this.loadingSet.has(dictType)) {
        // 已有同一请求在进行中，等待 100ms 后重试读取缓存
        await new Promise((resolve) => setTimeout(resolve, 100))
        return this.dictMap[dictType] || []
      }
      this.loadingSet.add(dictType)
      try {
        const res = await request.get('/system/dict/data/list', {
          params: { typeCode: dictType }
        })
        const list = (res.data || []).map((item) => ({
          dictType: item.typeCode,
          label: item.label,
          value: item.value,
          colorType: item.listClass || '',
          cssClass: item.cssClass || ''
        }))
        this.dictMap[dictType] = list
        return list
      } catch {
        this.dictMap[dictType] = []
        return []
      } finally {
        this.loadingSet.delete(dictType)
      }
    },

    /**
     * 刷新指定 dictType 的缓存（重新拉取）
     * @param {string|string[]} dictType 单个或多个字典类型编码
     */
    async refreshDict(dictType) {
      const types = Array.isArray(dictType) ? dictType : [dictType]
      types.forEach((t) => delete this.dictMap[t])
      await Promise.all(types.map((t) => this.getDictByType(t)))
    },

    /**
     * 清空所有缓存
     */
    clearCache() {
      this.dictMap = {}
      this.loadingSet.clear()
    }
  }
})

/**
 * 在 setup 外获取 store 的工具函数（用于非组件上下文）
 */
export const useDictStoreWithOut = () => {
  return useDictStore()
}
