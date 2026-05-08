<template>
  <div class="super-repair-manage">
    <el-card shadow="never" class="list-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="emoji">️</span>
            <span class="title">宿舍报修管理</span>
          </div>
          
          <div class="header-right">
            <el-cascader
              v-model="filter.location"
              :options="dormOptions"
              :props="{ checkStrictly: true }"
              placeholder="筛选条件"
              clearable
              style="width: 200px; margin-right: 12px;"
              @change="fetchAll"
            />
            <el-select v-model="filter.status" placeholder="工单状态" clearable style="width: 130px; margin-right: 12px;" @change="fetchAll">
              <el-option label="待接单" :value="0" />
              <el-option label="维修中" :value="1" />
              <el-option label="待评价" :value="2" />
              <el-option label="已完成" :value="3" />
              <el-option label="已作废" :value="-1" />
            </el-select>
            <el-button type="primary" plain @click="fetchAll">刷新数据</el-button>
          </div>
        </div>
      </template>

      <el-table :data="allOrders" style="width: 100%" v-loading="loading" stripe border>
        <el-table-column prop="id" label="单号" width="80" align="center" />
        <el-table-column prop="dormLocation" label="宿舍位置" width="160" />
        <el-table-column prop="title" label="故障内容" min-width="150" show-overflow-tooltip />
        
        <el-table-column label="当前状态" width="110" align="center">
          <template #default="scope">
            <el-tag :type="getStatusConfig(scope.row.status).type" effect="dark">
              {{ getStatusConfig(scope.row.status).text }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="服务评分" width="140" align="center">
          <template #default="scope">
            <el-rate v-if="scope.row.status === 3" :model-value="getRating(scope.row.id)" disabled text-color="#ff9900" />
            <span v-else style="color: #999; font-size: 12px;">--</span>
          </template>
        </el-table-column>

        <el-table-column label="管理操作" width="160" align="center" fixed="right">
          <template #default="scope">
            <div style="display: flex; justify-content: center; align-items: center; gap: 4px;">
              <el-button type="primary" link size="small" @click="openDetail(scope.row)">详情</el-button>
              
              <el-dropdown trigger="click">
                <el-button type="primary" link size="small">
                  更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item 
                      v-if="scope.row.status === 0 || scope.row.status === 1" 
                      icon="User" 
                      @click="openAssignDialog(scope.row)"
                    >
                      派单
                    </el-dropdown-item>
                    
                    <el-dropdown-item 
                      v-if="scope.row.status !== -1" 
                      icon="CircleClose" 
                      @click="handleForceCancel(scope.row)"
                    >
                      作废
                    </el-dropdown-item>
                    
                    <el-dropdown-item 
                      icon="Delete" 
                      style="color: #F56C6C" 
                      @click="handleDelete(scope.row.id)"
                    >
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="assignVisible" title="工单调度：指派维修人员" width="400px">
      <div style="padding: 10px 0;">
        <p style="margin-bottom: 15px; color: #666;">请选择该工单的负责师傅：</p>
        <el-select v-model="selectedWorkerId" placeholder="选择在册师傅" style="width: 100%">
          <el-option v-for="worker in workerList" :key="worker.id" :label="worker.realName" :value="worker.id" />
        </el-select>
      </div>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssign">确认指派</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="工单详情" width="600px" append-to-body>
      <div v-if="currentOrder">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="单号">{{ currentOrder.id }}</el-descriptions-item>
          <el-descriptions-item label="位置">{{ currentOrder.dormLocation }}</el-descriptions-item>
          <template v-if="currentOrder.status === 3 && evaluationData">
            <el-descriptions-item label="服务评分">
              <el-rate v-model="evaluationData.score" disabled />
            </el-descriptions-item>
            <el-descriptions-item label="学生评语">{{ evaluationData.comment || '无' }}</el-descriptions-item>
          </template>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const allOrders = ref([])
const filter = reactive({ location: [], status: null })
const ratingsMap = ref({}) 

const detailVisible = ref(false)
const currentOrder = ref(null)
const evaluationData = ref(null)

const assignVisible = ref(false)
const workerList = ref([])
const selectedWorkerId = ref(null)
const currentDispatchOrder = ref(null)

const dormOptions = [
  { value: '东校区', label: '东校区', children: [{ value: '1舍', label: '1舍' }, { value: '2舍', label: '2舍' }] },
  { value: '南校区', label: '南校区', children: [{ value: '1舍', label: '1舍' }, { value: '2舍', label: '2舍' }, { value: '3舍', label: '3舍' }] },
  { value: '西校区', label: '西校区', children: [{ value: '1舍', label: '1舍' }] }
]

const getStatusConfig = (status) => {
  const map = { '-1': { text: '已作废', type: 'info' }, 0: { text: '待接单', type: 'danger' }, 1: { text: '维修中', type: 'primary' }, 2: { text: '待评价', type: 'warning' }, 3: { text: '已闭环', type: 'success' } }
  return map[status] || { text: '未知', type: 'info' }
}

const fetchAll = async () => {
  loading.value = true
  try {
    const res = await request.get('/repair/list')
    let data = res || []
    
    // 过滤逻辑
    if (filter.location && filter.location.length > 0) {
      const path = filter.location.join('-')
      data = data.filter(i => i.dormLocation.startsWith(path))
    }
    if (filter.status !== null && filter.status !== '') {
      data = data.filter(i => i.status === filter.status)
    }

    // 【核心修复】倒序排列，保证单号 100 永远在 1 前面
    allOrders.value = data.sort((a, b) => b.id - a.id)
    
    // 审计评分数据
    data.filter(i => i.status === 3).forEach(async (order) => {
       const evalRes = await request.get(`/repair/evaluation/${order.id}`)
       if(evalRes) ratingsMap.value[order.id] = evalRes.score
    })
  } finally {
    loading.value = false
  }
}

const getRating = (id) => ratingsMap.value[id] || 0

const openDetail = async (row) => {
  currentOrder.value = row
  evaluationData.value = null 
  detailVisible.value = true
  if (row.status === 3) {
    const res = await request.get(`/repair/evaluation/${row.id}`)
    evaluationData.value = res
  }
}

const openAssignDialog = async (row) => {
  currentDispatchOrder.value = row
  selectedWorkerId.value = null
  const res = await request.get('/user/list?roleId=2')
  workerList.value = res || []
  assignVisible.value = true
}

const confirmAssign = async () => {
  if (!selectedWorkerId.value) return ElMessage.warning('请选择师傅')
  await request.post(`/repair/assign?orderId=${currentDispatchOrder.value.id}&workerId=${selectedWorkerId.value}`)
  ElMessage.success('工单指派成功！')
  assignVisible.value = false
  fetchAll()
}

const handleForceCancel = (row) => {
  ElMessageBox.confirm(`确定要作废此工单吗？`, '提示', { type: 'warning' }).then(async () => {
    await request.post(`/repair/cancel/${row.id}`)
    ElMessage.success('已设为作废')
    fetchAll()
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('删除？', '危险', { type: 'error' }).then(async () => {
    await request.post(`/repair/delete/${id}`)
    ElMessage.success('已彻底删除')
    fetchAll()
  })
}

onMounted(fetchAll)
</script>

<style scoped>
.super-repair-manage { padding-bottom: 20px; }
.list-card { border-radius: 12px; border: none; min-height: 600px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; font-weight: bold; font-size: 16px; }
.header-left .emoji { font-size: 20px; margin-right: 8px; }
</style>