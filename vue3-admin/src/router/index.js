import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useMenuStore } from '@/stores/menu'
import { generateRoutes } from '@/utils/dynamicRouter'
import { reportOperLog } from '@/utils/operlog'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// ========== 静态路由（无需权限，始终存在）==========
const constantRoutes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/index.vue'),
    },
    {
        path: '/redirect/:path(.*)',
        name: 'Redirect',
        component: () => import('@/views/redirect.vue'),
        meta: { noTagsView: true },
    },
    // 404 兜底（未匹配的路由）
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/redirect.vue'),
        meta: { noTagsView: true },
    },
]

// ========== Layout 根路由 ==========
// 注意：不要设置 redirect，避免动态路由未注册时找不到目标路径
const layoutRoute = {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/index.vue'),
    children: [],
}

const router = createRouter({
    history: createWebHistory(),
    routes: [...constantRoutes, layoutRoute],
})

// 标记是否已动态添加过路由
let dynamicRoutesAdded = false

/**
 * 重置路由（登出时调用）
 */
export function resetRouter() {
    dynamicRoutesAdded = false
    const routeNamesToRemove = []
    router.getRoutes().forEach((r) => {
        // 只移除通过 addRoute 添加到 Layout 下的动态路由
        if (r.name && !['Login', 'Redirect', 'NotFound', 'Layout'].includes(r.name)) {
            routeNamesToRemove.push(r.name)
        }
    })
    routeNamesToRemove.forEach((name) => router.removeRoute(name))
}

/**
 * 加载动态路由并添加到 Layout 下
 */
async function loadDynamicRoutes() {
    if (dynamicRoutesAdded) return

    const menuStore = useMenuStore()
    let menus = menuStore.menuList

    if (!menuStore.loaded) {
        try {
            menus = await menuStore.fetchMenus()
            await menuStore.fetchPermissions()
        } catch {
            return
        }
    }

    if (menus?.length) {
        const routes = generateRoutes(menus)
        routes.forEach((route) => {
            router.addRoute('Layout', route)
        })
        dynamicRoutesAdded = true
    }
}

/**
 * 首页路径 — 参考芋道源码，固定为 /home，不依赖动态路由排序
 */
function getHomePath() {
    return '/home'
}

// ========== 路由守卫 ==========
router.beforeEach(async (to, _from, next) => {
    NProgress.start()
    const userStore = useUserStore()

    // 登录页直接放行
    if (to.path === '/login') {
        next()
        return
    }

    // 未登录 → 去登录页
    if (!userStore.token) {
        next(`/login?redirect=${to.path}`)
        return
    }

    // 刷新重定向中转
    if (to.path.startsWith('/redirect/')) {
        const targetPath = to.path.replace('/redirect', '')
        next(targetPath || '/')
        return
    }

    // 确保动态路由已加载
    if (!dynamicRoutesAdded) {
        await loadDynamicRoutes()

        // 如果在加载动态路由期间 token 被清除（如 401 拦截器自动登出），跳回登录页
        if (!userStore.token) {
            next(`/login?redirect=${to.fullPath}`)
            return
        }

        // 路由加载后重试当前导航（关键：让 router 用新路由表重新匹配 to.path）
        if (to.path === '/' || to.name === 'Layout') {
            next({ path: getHomePath(), replace: true })
            return
        }

        // 重试当前路由（用完整路径字符串重导航，避免展开 route 对象导致解析失败）
        next(to.fullPath)
        return
    }

    // 根路径重定向到首页
    if (to.path === '/' || to.name === 'Layout') {
        next({ path: getHomePath(), replace: true })
        return
    }

    // 有 token 但用户信息为空（页面刷新），拉取用户信息
    if (!userStore.userInfo || !userStore.userInfo.id) {
        try {
            await userStore.getUserInfo()
        } catch {
            // 获取失败不影响继续访问
        }
    }

    next()
})

router.afterEach((to) => {
    NProgress.done()
    // 记录前端页面访问日志（未登录、无标题、重定向/404 页面不记录）
    const userStore = useUserStore()
    if (userStore.token && to.meta?.title && !to.meta.noTagsView) {
        reportOperLog({
            title: typeof to.meta.title === 'string' ? to.meta.title : '页面访问',
            businessType: 4, // 查询/访问
            requestMethod: 'GET', // 页面访问按 GET 语义记录，避免请求方式字段为空
            method: to.name ? String(to.name) : to.path, // 用路由名填充「请求方法(类.方法)」列
            operUrl: to.fullPath,
            operParam: (to.query && Object.keys(to.query).length) ? JSON.stringify(to.query) : '', // 页面路由参数
            status: 0
        })
    }
})

export default router
