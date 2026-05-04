<template>
  <div class="personal-container">
    <el-row :gutter="20">
      
      <!-- ================= 左侧：头像修改区 ================= -->
      <el-col :span="8">
        <el-card shadow="never" class="avatar-card">
          <div class="avatar-wrapper">
            <!-- action 需要换成你后端实际的文件上传接口 -->
            <el-upload
              class="avatar-uploader"
              action="http://localhost:8080/upload"
              :show-file-list="false"
              :headers="uploadHeaders"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
            >
              <el-avatar 
                v-if="userForm.avatar" 
                :src="userForm.avatar" 
                :size="140" 
                class="avatar-img"
              />
              <div v-else class="avatar-empty">
                <el-icon class="avatar-icon"><UserFilled /></el-icon>
              </div>
              
              <div class="avatar-mask">
                <el-icon><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </el-upload>
            
            <h3 class="user-name">{{ userForm.realName || '未命名用户' }}</h3>
            <p class="user-role">
              <el-tag :type="roleConfig.type" effect="dark" round>
                {{ roleConfig.name }}
              </el-tag>
            </p>
          </div>
          
          <el-divider border-style="dashed" />
          
          <div class="user-stats">
            <div class="stat-item">
              <el-icon><Calendar /></el-icon>
              <span>加入时间：{{ (userForm.createTime || '').substring(0, 10) || '未知' }}</span>
            </div>
            <div class="stat-item">
              <el-icon><Stamp /></el-icon>
              <span>系统状态：正常</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- ================= 右侧：个人信息修改区 ================= -->
      <el-col :span="16">
        <el-card shadow="never" class="info-card">
          <template #header>
            <div class="card-header">
              <span class="header-title">✍️ 基本信息设置</span>
              <!-- 新增的修改密码触发按钮 -->
              <el-button type="danger" plain size="small" icon="Lock" @click="openPwdDialog">修改安全密码</el-button>
            </div>
          </template>

          <el-form 
            ref="formRef" 
            :model="userForm" 
            :rules="rules" 
            label-width="90px" 
            size="large"
            class="personal-form"
          >
            <el-form-item label="登录账号">
              <el-input v-model="userForm.username" disabled placeholder="账号不可修改" />
              <div class="input-tip">用于系统登录的唯一凭证，不可修改。</div>
            </el-form-item>

            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="userForm.realName" placeholder="请输入您的真实姓名" clearable prefix-icon="User" />
            </el-form-item>

            <el-form-item label="手机号码" prop="phone">
              <el-input v-model="userForm.phone" placeholder="请输入11位手机号码" clearable prefix-icon="Iphone" />
            </el-form-item>

            <el-form-item style="margin-top: 40px;">
              <el-button type="primary" @click="saveProfile" :loading="loading" icon="Select" class="save-btn">
                保 存 修 改
              </el-button>
              <el-button @click="resetForm" icon="RefreshRight">重 置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- ================= 修改密码独立弹窗 ================= -->
    <el-dialog v-model="pwdDialogVisible" title="修改安全密码" width="450px" destroy-on-close>
      <el-form 
        ref="pwdFormRef" 
        :model="pwdForm" 
        :rules="pwdRules" 
        label-width="90px" 
        @keyup.enter="submitPassword"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入现在的登录密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="密码长度不少于6位" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="pwdDialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="pwdLoading" @click="submitPassword">确 认 修 改</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const localUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

const loading = ref(false)
const formRef = ref(null)

const uploadHeaders = computed(() => {
  return { Authorization: localStorage.getItem('token') || '' }
})

const userForm = reactive({
  id: '', username: '', realName: '', phone: '', avatar: '', roleId: 1, createTime: ''
})

onMounted(() => {
  Object.assign(userForm, localUserInfo)
  if (!userForm.avatar) {
    userForm.avatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  }
})

const rules = {
  realName: [{ required: true, message: '真实姓名不能为空', trigger: 'blur' }],
  phone: [{ required: false, pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号码', trigger: 'blur' }]
}

const roleConfig = computed(() => {
  const roleId = Number(userForm.roleId)
  switch (roleId) {
    case 4: return { name: '超级管理员', type: 'danger' }
    case 3: return { name: '部门负责人', type: 'warning' }
    case 2: return { name: '后勤维修工', type: 'success' }
    default: return { name: '普通学生', type: 'primary' }
  }
})

const beforeAvatarUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) ElMessage.error('头像只能是 JPG/PNG/GIF 格式!')
  if (!isLt2M) ElMessage.error('头像大小不能超过 2MB!')
  return isImage && isLt2M
}

const handleAvatarSuccess = (res) => {
  if (res.code === 200) {
    userForm.avatar = res.data
    ElMessage.success('头像上传成功！记得点击保存生效哦。')
  } else {
    ElMessage.error(res.message || '头像上传失败')
  }
}

const saveProfile = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // const res = await request.post('/user/update', userForm)
        await new Promise(resolve => setTimeout(resolve, 800))
        ElMessage.success('个人信息更新成功！')
        localStorage.setItem('userInfo', JSON.stringify(userForm))
      } catch (error) {
        ElMessage.error('更新失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const resetForm = () => {
  Object.assign(userForm, localUserInfo)
  ElMessage.info('已恢复为原始信息')
}

// ================== 修改密码专属逻辑 ==================
const pwdDialogVisible = ref(false)
const pwdFormRef = ref(null)
const pwdLoading = ref(false)

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 自定义校验规则：检查两次密码是否一致
const validateConfirmPwd = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的新密码不一致!'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ]
}

const openPwdDialog = () => {
  pwdDialogVisible.value = true
  // 每次打开弹窗清空上一次填写的残留
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
}

const submitPassword = () => {
  pwdFormRef.value.validate(async (valid) => {
    if (valid) {
      pwdLoading.value = true
      try {
        // 把用户ID带上发给后端
        const submitData = {
          userId: userForm.id,
          oldPassword: pwdForm.oldPassword,
          newPassword: pwdForm.newPassword
        }
        
        // ⚠️ 等你后端写好 User 接口后，把下面这行注释解开
        // await request.post('/user/update-password', submitData)
        
        // 模拟请求成功
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        ElMessage.success('安全密码修改成功，请使用新密码重新登录！')
        pwdDialogVisible.value = false
        
        // 强制踢出登录，跳转回登录页
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      } catch (error) {
        ElMessage.error('原密码错误或修改失败')
      } finally {
        pwdLoading.value = false
      }
    }
  })
}
</script>

<style scoped>
/* 基础样式跟之前一致 */
.personal-container { padding-bottom: 20px; }
.avatar-card, .info-card { border-radius: 12px; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.03); height: 100%; }

/* 给头部设置 Flex 布局，把“修改密码”按钮顶到右边 */
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-title { font-weight: bold; font-size: 16px; color: #333; }

.avatar-wrapper { display: flex; flex-direction: column; align-items: center; padding: 20px 0; }
.avatar-uploader { position: relative; border-radius: 50%; cursor: pointer; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1); margin-bottom: 20px; }
.avatar-empty { width: 140px; height: 140px; background: #f0f2f5; display: flex; justify-content: center; align-items: center; }
.avatar-icon { font-size: 60px; color: #c0c4cc; }
.avatar-mask { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.5); display: flex; flex-direction: column; justify-content: center; align-items: center; color: white; font-size: 14px; opacity: 0; transition: opacity 0.3s; }
.avatar-uploader:hover .avatar-mask { opacity: 1; }
.avatar-mask .el-icon { font-size: 24px; margin-bottom: 5px; }

.user-name { margin: 0 0 10px 0; color: #303133; font-size: 20px; }
.user-role { margin: 0; }

.user-stats { padding: 0 10px; color: #606266; font-size: 13px; }
.stat-item { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.stat-item .el-icon { color: #909399; font-size: 16px; }

.personal-form { max-width: 500px; margin: 20px 0 0 20px; }
.input-tip { font-size: 12px; color: #909399; line-height: 1.2; margin-top: 4px; }
.save-btn { letter-spacing: 2px; padding: 0 25px; }
</style>