import axios from 'axios'
import { ElMessage } from 'element-plus'

// 1. 创建 axios 实例
const request = axios.create({
    // 这里的 /api 会被 vite.config.js 里的代理拦截，并悄悄转发到 http://localhost:8080
    baseURL: '/api', 
    timeout: 10000 // 请求超时时间：10秒
})

// 2. 请求拦截器：前端发送请求之前做的事（自动塞入 Token）
request.interceptors.request.use(
    config => {
        // 从浏览器的本地存储 (localStorage) 中尝试获取 Token
        const token = localStorage.getItem('token')
        if (token) {
            // 如果有 Token，就统一加上 Bearer 前缀，并放到请求头 Authorization 里
            config.headers['Authorization'] = 'Bearer ' + token
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 3. 响应拦截器：收到后端数据后做的事（全局错误剥离与提示）
request.interceptors.response.use(
    response => {
        // response.data 就是你后端写的那个 Result<T> 对象
        const res = response.data 
        
        // 如果后端返回的 code 不是 200，说明业务报错了（比如密码错误）
        if (res.code !== 200) {
            // 利用 Element-Plus 的全局提示框弹红字报错
            ElMessage.error(res.message || '系统内部错误')
            
            // 如果是 401 状态码，说明没登录或者 Token 伪造/过期
            if (res.code === 401) {
                localStorage.removeItem('token') // 清除假 Token
                // 强制跳转回登录页
                setTimeout(() => {
                    window.location.href = '/login'
                }, 1000)
            }
            return Promise.reject(new Error(res.message || 'Error'))
        }
        
        // 如果是 200 成功，我们直接把核心的 data 剥离出来返回给具体的页面，
        // 这样页面里就不需要每次都写 res.data.data 了！
        return res.data
    },
    error => {
        ElMessage.error('网络请求失败，请检查后端系统是否已启动')
        return Promise.reject(error)
    }
)

export default request