<template>
  <div class="user-manage-page">
    <el-card shadow="never" class="list-card">
      <template #header>
        <div class="card-header">
          <span class="emoji">👑</span>
          <span class="title">全站账号权限控制中心</span>
          
          <div class="header-actions">
            <el-input 
              v-model="searchKeyword" 
              placeholder="搜索学号或姓名..." 
              clearable 
              prefix-icon="Search"
              style="width: 250px; margin-right: 15px;"
              @keyup.enter="fetchUsers"
              @clear="fetchUsers"
            />
            <el-button type="primary" @click="fetchUsers">搜索</el-button>
            <el-button type="success" icon="Plus" @click="openAddDialog">开通新账号</el-button>
          </div>
        </div>
      </template>

      <el-table :data="userList" style="width: 100%" v-loading="loading" stripe border>
        <el-table-column prop="id" label="系统ID" width="80" align="center" />
        <el-table-column prop="username" label="登录账号(学号/工号)" width="160" />
        <el-table-column prop="realName" label="真实姓名" width="120" align="center">
          <template #default="scope"><b>{{ scope.row.realName }}</b></template>
        </el-table-column>
        
        <el-table-column label="系统角色" width="150" align="center">
          <template #default="scope">
            <el-tag :type="getRoleConfig(scope.row.roleId).type" effect="dark" round>
              {{ getRoleConfig(scope.row.roleId).text }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="phone" label="联系电话" width="130" align="center" />
        <el-table-column prop="createTime" label="注册时间" width="160" align="center" />

        <el-table-column label="账号状态 (一键封禁)" width="150" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              active-color="#13ce66"
              inactive-color="#ff4949"
              @change="(val) => handleStatusChange(scope.row.id, val)"
              :disabled="scope.row.roleId === 4" 
            />
          </template>
        </el-table-column>

        <el-table-column label="高级操作" fixed="right" min-width="180" align="center">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="openEditDialog(scope.row)" :disabled="scope.row.roleId === 4">编辑资料</el-button>
            <el-button type="warning" link size="small" @click="handleResetPwd(scope.row.id)" :disabled="scope.row.roleId === 4">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑账号资料' : '开通新系统账号'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="登录账号" required>
          <el-input v-model="form.username" placeholder="请输入学号或工号" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="真实姓名" required>
          <el-input v-model="form.realName" placeholder="如：张三" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="系统角色" required>
          <el-select v-model="form.roleId" placeholder="请分配权限角色" style="width: 100%;">
            <el-option label="学生 (普通权限)" :value="1" />
            <el-option label="后勤维修工 (接单权限)" :value="2" />
            <el-option label="社团负责人 (活动权限)" :value="3" />
          </el-select>
        </el-form-item>
        <div v-if="!form.id" style="margin-left: 100px; color: #999; font-size: 12px;">
          注：新开通账号的初始密码统一为 123456
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSave">确 认 保 存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const userList = ref([])
const searchKeyword = ref('')

const dialogVisible = ref(false)
const form = reactive({
  id: null,
  username: '',
  realName: '',
  phone: '',
  roleId: 1
})

// 角色标识映射
const getRoleConfig = (roleId) => {
  const map = {
    1: { text: '👨‍🎓 学生', type: 'info' },
    2: { text: '👷‍♂️ 维修工', type: 'primary' },
    3: { text: '👨‍🏫 社团主管', type: 'success' },
    4: { text: '👑 超级管理员', type: 'danger' }
  }
  return map[roleId] || { text: '未知身份', type: 'info' }
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await request.get(`/user/list?keyword=${searchKeyword.value}`)
    userList.value = res || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 动态修改账号封禁状态
const handleStatusChange = async (id, status) => {
  try {
    await request.post(`/user/status?id=${id}&status=${status}`)
    ElMessage.success(status === 1 ? '账号已解封！' : '账号已拉黑禁止登录！')
  } catch (error) {
    fetchUsers() // 如果接口失败，把 Switch 开关拨回原样
  }
}

// 重置密码
const handleResetPwd = (id) => {
  ElMessageBox.confirm('确定要将该账号的密码重置为默认的 123456 吗？', '高危操作', { type: 'warning' }).then(async () => {
    await request.post(`/user/reset-pwd/${id}`)
    ElMessage.success('密码重置成功！')
  })
}

const openAddDialog = () => {
  form.id = null
  form.username = ''
  form.realName = ''
  form.phone = ''
  form.roleId = 1
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  form.id = row.id
  form.username = row.username
  form.realName = row.realName
  form.phone = row.phone
  form.roleId = row.roleId
  dialogVisible.value = true
}

const submitSave = async () => {
  if (!form.username || !form.realName || !form.roleId) {
    ElMessage.warning('请填写完整的核心信息！')
    return
  }
  await request.post('/user/save', form)
  ElMessage.success('账号信息保存成功！')
  dialogVisible.value = false
  fetchUsers()
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-manage-page { padding-bottom: 20px; }
.list-card { border-radius: 12px; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.05); min-height: 600px; }
.card-header { display: flex; align-items: center; justify-content: space-between; width: 100%; font-size: 18px; font-weight: bold; }
.card-header .emoji { font-size: 24px; margin-right: 10px; }
.header-actions { display: flex; align-items: center; }
</style>