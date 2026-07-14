import { defineStore } from 'pinia'
import { useMenuStore } from '@/stores/menu'
import { useTagsViewStore } from '@/stores/tagsView'
import { resetRouter } from '@/router'
import request from '@/utils/request'

export const useUserStore = defineStore('user', {
    state: () => ({
        token: localStorage.getItem('token') || '',
        /** 用户信息（昵称、头像等），持久化到 localStorage */
        userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null') || {}
    }),
    getters: {
        /** 用户昵称 */
        nickname: (state) => state.userInfo?.nickname || state.userInfo?.username || '',
        /** 用户头像 */
        avatar: (state) => state.userInfo?.avatar || '',
        /** 用户名 */
        username: (state) => state.userInfo?.username || ''
    },
    actions: {
        setToken(token) {
            this.token = token
            localStorage.setItem('token', token)
        },

        /**
         * 获取当前登录用户信息（参考芋道源码 /system/user/profile）
         * 从后端 Redis 缓存中读取用户数据
         */
        async getUserInfo() {
            try {
                const res = await request.get('/system/user/profile')
                this.userInfo = res.data?.user || res.data || {}
                localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
                return this.userInfo
            } catch {
                this.userInfo = {}
                localStorage.removeItem('userInfo')
                return {}
            }
        },

        /**
         * 更新用户信息（如修改头像、昵称）
         */
        setUserInfo(info) {
            this.userInfo = { ...this.userInfo, ...info }
            localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        },

        logout() {
            this.token = ''
            this.userInfo = {}
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            useMenuStore().clearMenus()
            useTagsViewStore().clearViews()
            resetRouter()
        }
    }
})
