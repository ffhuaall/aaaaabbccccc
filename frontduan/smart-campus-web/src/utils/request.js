import axios from 'axios'
import { ElMessage } from 'element-plus'

//创建axios实例
const request = axios.create({
    // /api被vite.config.js里的代理拦截，转发到 http://localhost:8080
    baseURL: '/api', 
    timeout: 10000 //请求超时时间10秒
})

//请求拦截器：前端发送请求之前做的事（自动塞入 Token）
request.interceptors.request.use(
    config => {
        //从浏览器的本地存储中尝试获取 Token
        const token = localStorage.getItem('token')
        if (token) {
            //如果有Token统一加上Bearer前缀，并放到请求头 Authorization 里
            config.headers['Authorization'] = 'Bearer ' + token
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

//响应拦截器：收到后端数据后做的事
request.interceptors.response.use(
    response => {
        //response.data（后端的Result<T>对象）
        const res = response.data 
        
        //如果后端返回的code不是 200，业务报错
        if (res.code !== 200) {
            ElMessage.error(res.message || '系统内部错误')
            
            //如果是401，没登录或者Token过期
            if (res.code === 401) {
                localStorage.removeItem('token') //清除假 Token
                //强制跳转回登录页
                setTimeout(() => {
                    window.location.href = '/login'
                }, 1000)
            }
            return Promise.reject(new Error(res.message || 'Error'))
        }
        
        //如果是 200 成功，我们直接把核心的 data 剥离出来返回给具体的页面，
        return res.data
    },
    error => {
        ElMessage.error('网络请求失败')
        return Promise.reject(error)
    }
)

export default request