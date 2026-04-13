<template>
  <div class="admin-repair-page">
    <el-card shadow="never" class="list-card">
      <template #header>
        <div class="card-header">
          <span class="emoji">👷‍♂️</span>
          <span class="title">后勤工单调度中心</span>
        </div>
      </template>

      <el-table :data="allOrders" style="width: 100%" v-loading="loading" stripe>
        <el-table-column prop="id" label="工单号" width="80" align="center" />
        <el-table-column prop="dormLocation" label="报修位置" width="150" align="center">
          <template #default="scope">
            <el-tag type="danger" effect="dark">{{ scope.row.dormLocation }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="故障描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="报修时间" width="160" align="center" />
        
        <el-table-column label="当前状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getStatusConfig(scope.row.status).type" effect="light" round>
              {{ getStatusConfig(scope.row.status).text }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作台" width="180" align="center">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 0" 
              type="primary" 
              size="small" 
              @click="handleAction(scope.row.id, 'take')">
              派单/接单
            </el-button>
            
            <el-button 
              v-if="scope.row.status === 1" 
              type="success" 
              size="small" 
              @click="handleAction(scope.row.id, 'finish')">
              登记完工
            </el-button>
            
            <span v-if="scope.row.status >= 2" style="color: #999; font-size: 13px;">已流转至学生端</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const allOrders = ref([])

const getStatusConfig = (status) => {
  const configMap = {
    0: { text: '待接单', type: 'warning' },
    1: { text: '维修中', type: 'primary' },
    2: { text: '待评价', type: 'success' },
    3: { text: '已闭环', type: 'info' }
  }
  return configMap[status] || { text: '未知状态', type: 'info' }
}

const fetchAllOrders = async () => {
  loading.value = true
  try {
    const res = await request.get('/repair/list')
    allOrders.value = res || []
    // 倒序排，新的工单在最上面
    allOrders.value.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  } catch (error) {
    console.error('获取列表失败', error)
  } finally {
    loading.value = false
  }
}

// 统一处理流转操作
const handleAction = (id, action) => {
  const actionText = action === 'take' ? '确认接手这个工单吗？' : '确认该故障已修复完毕吗？'
  
  ElMessageBox.confirm(actionText, '操作提示', { type: 'warning' }).then(async () => {
    try {
      await request.post(`/repair/${action}/${id}`)
      ElMessage.success('操作成功！流转状态已更新。')
      fetchAllOrders() // 刷新列表
    } catch (error) {
      console.log('操作取消或失败')
    }
  })
}

onMounted(() => {
  fetchAllOrders()
})
</script>

<style scoped>
.admin-repair-page { padding-bottom: 20px; }
.list-card { border-radius: 12px; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.05); min-height: 600px; }
.card-header { display: flex; align-items: center; font-size: 18px; font-weight: bold; }
.card-header .emoji { font-size: 24px; margin-right: 10px; }
</style>