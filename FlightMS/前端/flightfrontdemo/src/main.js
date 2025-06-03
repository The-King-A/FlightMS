import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { createPinia } from 'pinia'
import './assets/main.css'
import Cookies from 'js-cookie'

// 清除token，确保每次启动应用都需要登录
Cookies.remove('token')

const app = createApp(App)
const pinia = createPinia()

// 全局注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router)
app.use(ElementPlus) // 这会自动注册所有Element Plus组件和指令
app.use(pinia)

// 挂载应用
app.mount('#app')

// 预加载flight store
import { useFlightStore } from './stores/flight'
const flightStore = useFlightStore()
// 初始化时不加载数据，等用户登录后在相关页面加载
// 但确保store已被创建
