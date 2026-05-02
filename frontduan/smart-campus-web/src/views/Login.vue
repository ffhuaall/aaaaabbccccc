<template>
  <div class="portal-login-container">
    
    <!-- 左侧品牌海报区 -->
    <div class="login-hero">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <div class="logo-box">
          <span class="emoji">🎓</span>
          <span class="logo-text">Smart Campus</span>
        </div>
        <div class="slogan-box">
          <h1 class="slogan-title">连接校园每一刻</h1>
          <p class="slogan-desc">智慧校园一站式服务门户 · 全面赋能你的大学生活</p>
          <div class="role-tags">
            <el-tag effect="dark" type="primary" round size="large">学生通道</el-tag>
            <el-tag effect="dark" type="success" round size="large">教职工入口</el-tag>
            <el-tag effect="dark" type="warning" round size="large">管理后台</el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧交互表单区 -->
    <div class="login-panel">
      <div class="login-form-box">
        <div class="form-header">
          <h2>欢迎登录</h2>
          <p>Welcome to the Campus Portal</p>
        </div>

        <el-form ref="loginFormRef" :model="loginForm" :rules="rules" size="large" label-width="0">
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入学号/工号" 
              prefix-icon="User"
              class="custom-input" 
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码" 
              prefix-icon="Lock" 
              show-password 
              @keyup.enter="handleLogin"
              class="custom-input" 
            />
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住账号</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
          </div>

          <el-form-item style="margin-top: 30px;">
            <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin" round>
              登 录 系 统
            </el-button>
          </el-form-item>
          
          <div class="form-tips">
            <span>新生默认密码为身份证后六位，若遇问题请联系辅导员。</span>
          </div>
        </el-form>
      </div>
      
      <div class="panel-footer">
        © 2024 智慧校园一站式服务平台 | 网络信息中心技术支持
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { User, Lock } from '@element-plus/icons-vue' 

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '', 
  password: ''
})

const rules = {
  username: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }]
}

// 初始化钩子：读取记住的账号
onMounted(() => {
  const savedUsername = localStorage.getItem('savedUsername')
  if (savedUsername) {
    loginForm.username = savedUsername
    rememberMe.value = true
  } else {
    // 默认测试数据方便开发
    loginForm.username = 'student01'
    loginForm.password = '123456'
  }
})

const handleLogin = () => {
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await request.post('/auth/login', loginForm)
        
        ElMessage.success('登录成功，欢迎回来：' + res.realName)
        
        localStorage.setItem('token', res.token)
        localStorage.setItem('userInfo', JSON.stringify(res))
        
        // 处理记住账号逻辑
        if (rememberMe.value) {
          localStorage.setItem('savedUsername', loginForm.username)
        } else {
          localStorage.removeItem('savedUsername')
        }
        
        router.push('/dashboard')
      } catch (error) {
        console.log("登录失败")
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
/* 容器全屏铺满 */
.portal-login-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background-color: #f5f7fa;
}

/* 左侧海报区 */
.login-hero {
  flex: 5.5;
  position: relative;
  background-image: url('@/assets/hero.png');
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 深色遮罩，保证文字可读性 */
.hero-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: linear-gradient(135deg, rgba(24,144,255,0.85) 0%, rgba(114,46,209,0.7) 100%);
  z-index: 1;
}

.hero-content {
  position: relative;
  z-index: 2;
  color: white;
  padding: 40px;
  width: 80%;
  max-width: 600px;
}

.logo-box {
  display: flex;
  align-items: center;
  margin-bottom: 60px;
}
.logo-box .emoji { font-size: 42px; margin-right: 15px; }
.logo-box .logo-text { font-size: 28px; font-weight: bold; letter-spacing: 2px;}

.slogan-title {
  font-size: 48px;
  font-weight: 800;
  margin: 0 0 15px 0;
  letter-spacing: 3px;
}
.slogan-desc {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 40px;
}
.role-tags {
  display: flex;
  gap: 15px;
}

/* 右侧表单区 */
.login-panel {
  flex: 4.5;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
  padding: 6vh 40px;
  box-shadow: -10px 0 30px rgba(0,0,0,0.05);
  position: relative;
  z-index: 2;
}

.login-form-box {
  width: 100%;
  max-width: 380px;
  margin-top: auto;
  margin-bottom: auto;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
}
.form-header h2 {
  font-size: 28px;
  color: #303133;
  margin: 0 0 10px 0;
}
.form-header p {
  color: #909399;
  margin: 0;
  font-size: 14px;
}

/* 自定义输入框样式 */
:deep(.custom-input .el-input__wrapper) {
  border-radius: 8px;
  padding: 4px 15px;
  box-shadow: 0 0 0 1px #e4e7ed inset;
}
:deep(.custom-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #409eff inset;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: -10px;
}

.login-btn {
  width: 100%;
  font-size: 18px;
  letter-spacing: 4px;
  height: 48px;
  box-shadow: 0 8px 16px rgba(64,158,255,0.3);
  transition: all 0.3s;
}
.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 20px rgba(64,158,255,0.4);
}

.form-tips {
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 20px;
}

.panel-footer {
  font-size: 12px;
  color: #999;
  text-align: center;
}
</style>