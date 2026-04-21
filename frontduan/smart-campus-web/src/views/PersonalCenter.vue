<template>
  <div class="personal-center">
    <el-card shadow="never" class="profile-card">
      <template #header>
        <div class="card-header">
          <span class="title">👤 个人资料设置</span>
        </div>
      </template>

      <el-form :model="userForm" label-width="120px" style="max-width: 500px; margin-top: 20px;">
        <el-form-item label="登录学号/工号">
          <el-input v-model="userForm.username" disabled />
          <div class="form-tip">账号作为登录凭证，不可自行修改</div>
        </el-form-item>
        
        <el-form-item label="系统角色">
          <el-tag effect="dark">{{ roleName }}</el-tag>
        </el-form-item>

        <el-form-item label="真实姓名">
          <el-input v-model="userForm.realName" placeholder="请填写您的真实姓名" />
        </el-form-item>

        <el-form-item label="联系电话">
          <el-input v-model="userForm.phone" placeholder="以便师傅或社团联系您" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleUpdate">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const saving = ref(false)
// 从本地获取登录时的用户信息
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

const userForm = reactive({
  id: userInfo.id,
  username: userInfo.username,
  realName: userInfo.realName,
  phone: userInfo.phone,
  roleId: userInfo.roleId
})

const roleName = computed(() => {
  const map = { 1: '学生', 2: '维修工', 3: '社团负责人', 4: '系统管理员' }
  return map[userInfo.roleId] || '未知'
})

const handleUpdate = async () => {
  saving.value = true
  try {
    await request.post('/user/update-profile', userForm)
    // 更新本地缓存，防止页面刷新后还是旧数据
    const newUserInfo = { ...userInfo, ...userForm }
    localStorage.setItem('userInfo', JSON.stringify(newUserInfo))
    ElMessage.success('资料修改成功！')
    // 强制页面重新加载部分数据（或刷新）
    setTimeout(() => location.reload(), 500)
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.profile-card { border-radius: 12px; min-height: 600px; }
.form-tip { font-size: 12px; color: #999; margin-top: 5px; }
.card-header .title { font-weight: bold; font-size: 18px; }
</style>