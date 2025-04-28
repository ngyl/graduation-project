import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
// import { zhCn } from "element-plus/es/locale/lang/zh-cn"
import App from './App.vue'
import router from './router'
import './style.css'

// 创建应用实例
const app = createApp(App)

// 注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 全局错误处理
app.config.errorHandler = (err, instance, info) => {
  console.error('全局错误：', err)
  console.error('错误位置：', info)
  
  // 处理常见错误
  if (err instanceof TypeError && err.message.includes('Cannot read properties of undefined')) {
    console.warn('出现undefined属性访问错误，可能是组件在数据加载前尝试渲染')
    // 避免这类错误向上冒泡，防止应用崩溃
    return false
  }
}

// 应用挂载前检查用户登录状态
const initApp = async () => {
  // 初始化Pinia
  const pinia = createPinia()
  app.use(pinia)
  
  // 配置Element Plus，使用中文
  app.use(ElementPlus, {
    // locale: zhCn,
    size: 'default',
  })
  
  // 检查是否有保存的主题模式
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme === 'dark') {
    document.documentElement.setAttribute('data-theme', 'dark')
  }
  
  // 使用路由前导入用户状态管理
    const { useUserStore } = await import('./stores/user')
    const userStore = useUserStore()
    
  // 应用路由
  app.use(router)
  
  // 优化：使用本地存储首先判断是否可能已登录，避免不必要的API调用
  const hasSession = localStorage.getItem('hasSession') === 'true'
  
  if (hasSession) {
    // 仅当本地存在会话标记时才尝试获取用户信息
    try {
      console.log('检测到会话记录，正在验证用户登录状态...')
      const isLoggedIn = await userStore.fetchUserInfo()
      
      // 如果登录失败，清除本地会话标记
      if (!isLoggedIn) {
        localStorage.removeItem('hasSession')
      }
  } catch (error) {
      console.error('验证用户状态失败:', error)
      localStorage.removeItem('hasSession')
    }
  } else {
    console.log('未检测到会话记录，跳过用户信息获取')
  }
  
  // 应用挂载
  app.mount('#app')
  
  console.log('动漫社交平台初始化完成 🚀')
}

// 初始化应用
initApp().catch(e => {
  console.error('应用初始化失败：', e)
})
