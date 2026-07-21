import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'
import { reportOperLog } from '@/utils/operlog'

// 将请求/响应数据安全地转为字符串，用于操作日志（失败静默降级）
function safePayload(data) {
  if (data === undefined || data === null) return ''
  if (typeof data === 'string') return data
  try {
    return JSON.stringify(data)
  } catch (e) {
    return String(data)
  }
}

const service = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    timeout: 10000
})

// 请求拦截
service.interceptors.request.use(config => {
    const userStore = useUserStore()
    if (userStore.token) {
        config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
})

// 响应拦截
service.interceptors.response.use(res => {
    // blob/文件下载直接透传原始响应，不校验业务 code
    if (res.config.responseType === 'blob') {
        return res
    }
    if (res.data.code !== 200) {
        ElMessage.error(res.data.msg || '请求失败')
        if (res.data.code === 401) {
            const userStore = useUserStore()
            userStore.logout()
            // 避免重复跳转：如果当前已经在登录页，不再 push
            if (router.currentRoute.value.path !== '/login') {
                router.push('/login')
            }
        }
        // 业务失败也记录到操作日志（排除日志上报接口自身，避免死循环）
        if (!res.config.url?.includes('/monitor/operlog/frontend')) {
            reportOperLog({
                title: '接口请求失败',
                businessType: 0,
                requestMethod: (res.config.method || '').toUpperCase(),
                operUrl: res.config.url,
                operParam: safePayload(res.config.data),
                jsonResult: safePayload(res.data),
                status: 1,
                errorMsg: res.data.msg || '业务处理失败'
            })
        }
        return Promise.reject(res.data)
    }
    return res.data
}, err => {
    // HTTP 403 权限不足
    if (err.response?.status === 403) {
        const msg = err.response?.data?.msg || '你没有该操作权限，请联系管理员！'
        ElMessage.error(msg)
        return Promise.reject(err)
    }
    // HTTP 401 跳转登录
    if (err.response?.status === 401) {
        const userStore = useUserStore()
        userStore.logout()
        // 避免重复跳转：如果当前已经在登录页，不再 push
        if (router.currentRoute.value.path !== '/login') {
            router.push('/login')
        }
        return Promise.reject(err)
    }
    const msg = err.response?.data?.msg || err.message || '网络请求异常'
    // 请求异常记录到操作日志（排除日志上报接口自身，避免死循环）
    if (!err.config?.url?.includes('/monitor/operlog/frontend')) {
        reportOperLog({
            title: '接口请求异常',
            businessType: 0,
            requestMethod: (err.config?.method || '').toUpperCase(),
            operUrl: err.config?.url || '',
            operParam: safePayload(err.config?.data),
            jsonResult: safePayload(err.response?.data),
            status: 1,
            errorMsg: msg
        })
    }
    ElMessage.error(msg)
    return Promise.reject(err)
})

export default service