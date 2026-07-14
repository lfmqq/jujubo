import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import './styles/variables.css'
import './styles/global.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { useThemeStore } from '@/stores/theme'
import * as $date from '@/utils/date'

const app = createApp(App)
const pinia = createPinia()

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

app.use(pinia)

// 初始化主题（在 router 挂载前，避免闪烁）
useThemeStore().initTheme()

app.use(router)
app.use(ElementPlus, { locale: zhCn })

// 全局属性：日期工具
app.config.globalProperties.$date = $date

app.mount('#app')