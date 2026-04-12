import { createApp } from 'vue'
import App from './App.vue'
import router from './router' // 引入我们刚才写的路由

// 引入 Element-Plus 样式库和图标库
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router) // 挂载路由
app.use(ElementPlus) // 挂载 UI 库
app.mount('#app')