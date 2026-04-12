<template>
  <div class="login-container">
    <el-card class="login-box">
      <h2 class="title">智慧校园一站式服务门户</h2>
      <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入学号/工号" 
            prefix-icon="User" />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码" 
            prefix-icon="Lock" 
            show-password 
            @keyup.enter="handleLogin" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request' // 引入我们刚刚封装的请求拦截器
// 引入 Element-Plus 的图标
import { User, Lock } from '@element-plus/icons-vue' 

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

// 表单数据绑定
const loginForm = reactive({
  username: 'student01', // 为了测试方便，直接默认填入测试账号
  password: '123456'
})

// 表单校验规则
const rules = {
  username: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }]
}

// 点击登录按钮触发的事件
const handleLogin = () => {
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 【核心】发起真正的后端请求！(因为配了代理，实际会请求 http://localhost:8080/auth/login)
        const res = await request.post('/auth/login', loginForm)
        
        // 走到这里说明响应拦截器判定 code 是 200，登录成功了
        ElMessage.success('登录成功，欢迎回来：' + res.realName)
        
        // 把后端发来的 Token 和个人信息存进本地浏览器
        localStorage.setItem('token', res.token)
        localStorage.setItem('userInfo', JSON.stringify(res))
        
        // 跳转到系统主页
        router.push('/dashboard')
      } catch (error) {
        // 如果密码错误，请求拦截器里已经弹过错了，这里抓一下异常防止控制台飙红即可
        console.log("登录失败")
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  /* 科技感深蓝色渐变背景 */
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%); 
}

.login-box {
  width: 400px;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.login-btn {
  width: 100%;
  font-size: 16px;
  letter-spacing: 2px;
}
</style>