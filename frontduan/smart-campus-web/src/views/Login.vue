<template>
  <div class="campus-login-wrapper">
    <div class="background-layer"></div>

    <div class="login-container">
      <div class="welcome-text">
        <h1 class="logo">
          <span class="emoji">🎓</span> Smart Campus
        </h1>
        <h2 class="title">连接校园每一刻</h2>
        <p class="subtitle">智慧校园一站式服务门户 · 全面赋能你的大学生活</p>
      </div>

      <div class="login-glass-card">
        <div class="qr-corner" title="扫码登录">
          <el-icon><Grid /></el-icon>
        </div>

        <div class="card-header">
          <h2>欢迎登录</h2>
          <p>Welcome to the Campus Portal</p>
        </div>

        <el-form ref="loginFormRef" :model="loginForm" :rules="rules" size="large" label-width="0">
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入学号/工号" 
              :prefix-icon="User"
              class="modern-input" 
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码" 
              :prefix-icon="Lock" 
              show-password 
              @keyup.enter="handleLogin"
              class="modern-input" 
            />
          </el-form-item>

          <div class="action-row">
            <el-checkbox v-model="rememberMe">记住账号</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
          </div>

          <el-form-item style="margin-top: 30px; margin-bottom: 0;">
            <el-button type="primary" class="submit-btn" :loading="loading" @click="handleLogin" round>
              登 录 系 统
            </el-button>
          </el-form-item>
          
          <div class="help-tips">
            <el-icon class="tip-icon"><Warning /></el-icon> 
            <span>新生默认密码为身份证后六位，若遇问题请联系辅导员。</span>
          </div>
        </el-form>
      </div>
    </div>
    
    <div class="footer-copyright">
      © 2024 智慧校园一站式服务平台 | 网络信息中心技术支持
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { User, Lock, Grid, Warning } from '@element-plus/icons-vue' 

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '', 
  password: '' // 密码初始始终为空
})

const rules = {
  username: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }]
}

// 初始化钩子：现在只负责读取和反显账号
onMounted(() => {
  const savedUsername = localStorage.getItem('savedUsername')
  
  if (savedUsername) {
    loginForm.username = savedUsername
    rememberMe.value = true // 如果有保存的账号，自动勾选复选框
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
        
        // 核心逻辑：只处理账号的保存与清除
        if (rememberMe.value) {
          localStorage.setItem('savedUsername', loginForm.username)
        } else {
          localStorage.removeItem('savedUsername')
        }
        
        // 安全保底：清除之前版本测试可能遗留的明文密码
        localStorage.removeItem('savedPassword')
        
        router.push('/dashboard')
      } catch (error) {
        console.error("登录失败", error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
/* 全屏背景与布局 */
.campus-login-wrapper {
  position: relative;
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

/* 沉浸式背景图，建议替换为校园实景图 */
.background-layer {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: url('https://images.unsplash.com/photo-1541339907198-e08756dedf3f?q=80&w=2070') no-repeat center center;
  background-size: cover;
  z-index: -1;
  /* 加一层深色滤镜让白色卡片和文字更清晰 */
  filter: brightness(0.75); 
}

/* 主容器布局 */
.login-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  max-width: 1100px;
  padding: 0 40px;
  box-sizing: border-box;
}

/* 左侧文字区 */
.welcome-text {
  color: white;
  text-shadow: 0 2px 10px rgba(0,0,0,0.4);
}
.welcome-text .logo { 
  font-size: 36px; 
  font-weight: 800; 
  margin-bottom: 25px;
  letter-spacing: 1px;
}
.welcome-text .logo .emoji { margin-right: 10px; }
.welcome-text .title { 
  font-size: 46px; 
  letter-spacing: 4px; 
  margin: 0 0 15px 0;
}
.welcome-text .subtitle { 
  font-size: 18px; 
  opacity: 0.9;
  margin: 0;
}

/* 核心：毛玻璃登录卡片 */
.login-glass-card {
  position: relative;
  width: 100%;
  max-width: 400px;
  padding: 45px 40px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px); /* 毛玻璃特效 */
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
}

/* 扫码角标 */
.qr-corner {
  position: absolute;
  top: 15px;
  right: 15px;
  font-size: 26px;
  color: #409eff;
  cursor: pointer;
  transition: all 0.3s;
}
.qr-corner:hover { opacity: 0.8; transform: scale(1.05); }

/* 卡片标题 */
.card-header {
  text-align: center;
  margin-bottom: 35px;
}
.card-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 26px;
  letter-spacing: 2px;
}
.card-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

/* 现代感输入框 */
:deep(.modern-input .el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.02) inset, 0 0 0 1px #e4e7ed inset;
  padding: 4px 15px;
}
:deep(.modern-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #409eff inset;
}

/* 记住账号与忘记密码行 */
.action-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: -10px;
}

/* 登录按钮 */
.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 18px;
  letter-spacing: 4px;
  border-radius: 24px;
  background: linear-gradient(90deg, #409eff, #53a8ff);
  border: none;
  box-shadow: 0 6px 16px rgba(64,158,255,0.3);
  transition: all 0.3s;
}
.submit-btn:hover {
  box-shadow: 0 8px 20px rgba(64,158,255,0.4);
  transform: translateY(-2px);
}

/* 底部温馨提示 */
.help-tips {
  margin-top: 25px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  gap: 5px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  text-align: center;
}
.tip-icon {
  margin-top: 2px;
  font-size: 14px;
}

/* 页脚版权 */
.footer-copyright {
  position: absolute;
  bottom: 25px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
  letter-spacing: 1px;
}
</style>