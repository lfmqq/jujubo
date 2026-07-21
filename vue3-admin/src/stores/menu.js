import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useMenuStore = defineStore('menu', {
    state: () => ({
        /** 侧边栏菜单树 */
        menuList: [],
        /** 扁平权限标识列表 */
        permissions: [],
        /** 是否已加载菜单 */
        loaded: false,
        /** 顶部菜单布局下：当前激活的顶级菜单 path */
        activeTopMenu: ''
    }),

    getters: {
        /**
         * 根据当前路由 path 找到所属的顶级菜单
         */
        currentTopMenu: (state) => {
            if (!state.activeTopMenu) return null
            return state.menuList.find(m => m.path === state.activeTopMenu) || null
        },

        /**
         * 当前顶级菜单的子菜单列表
         */
        currentSideMenus: (state) => {
            const top = state.menuList.find(m => m.path === state.activeTopMenu)
            return top?.children || []
        }
    },

    actions: {
        /**
         * 获取当前用户可见的菜单树和权限列表
         */
        async fetchMenus() {
            try {
                const res = await request.get('/system/menu/user-menu')
                this.menuList = res.data || []
                this.loaded = true
                return this.menuList
            } catch {
                this.menuList = []
                this.permissions = []
                this.loaded = false
                return []
            }
        },

        /**
         * 获取当前用户的所有权限标识（含按钮级权限）
         */
        async fetchPermissions() {
            try {
                const res = await request.get('/system/menu/user-permissions')
                this.permissions = res.data || []
                return this.permissions
            } catch {
                this.permissions = []
                return []
            }
        },

        /**
         * 设置当前激活的顶级菜单
         */
        setActiveTopMenu(path) {
            this.activeTopMenu = path
        },

        /**
         * 根据路由 path 自动匹配顶级菜单
         */
        matchTopMenu(routePath) {
            // 在 menuList 中找到 path 是 routePath 前缀的顶级菜单
            for (const menu of this.menuList) {
                if (routePath === menu.path || routePath.startsWith(menu.path + '/')) {
                    this.activeTopMenu = menu.path
                    return
                }
                // 递归检查子菜单
                if (menu.children?.length) {
                    for (const child of menu.children) {
                        if (routePath === child.path || routePath.startsWith(child.path + '/')) {
                            this.activeTopMenu = menu.path
                            return
                        }
                    }
                }
            }
        },

        /**
         * 清除菜单数据（登出时调用）
         */
        clearMenus() {
            this.menuList = []
            this.permissions = []
            this.loaded = false
            this.activeTopMenu = ''
        }
    }
})
