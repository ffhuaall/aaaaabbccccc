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
          <template #default="scope"><el-tag type="danger" effect="dark">{{ scope.row.dormLocation }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="title" label="故障简述" min-width="200" show-overflow-tooltip />
        
        <el-table-column label="当前状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getStatusConfig(scope.row.status).type" effect="light" round>
              {{ getStatusConfig(scope.row.status).text }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" plain @click="openDetail(scope.row)">
              工单详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="工单详细档案" width="600px" append-to-body>
      <div v-if="currentOrder">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="工单编号">NO.{{ currentOrder.id }}</el-descriptions-item>
          <el-descriptions-item label="报修位置">
            <el-tag type="danger">{{ currentOrder.dormLocation }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="故障标题"><b>{{ currentOrder.title }}</b></el-descriptions-item>
          <el-descriptions-item label="详细描述">{{ currentOrder.description }}</el-descriptions-item>
          
          <el-descriptions-item label="现场照片">
            <div v-if="safeParseImages(currentOrder.images).length > 0" class="image-gallery">
              <el-image 
                v-for="(url, index) in safeParseImages(currentOrder.images)" 
                :key="index"
                style="width: 100px; height: 100px; border-radius: 8px; margin-right: 10px;"
                :src="url" 
                :preview-src-list="safeParseImages(currentOrder.images)"
                preview-teleported
                fit="cover"
              />
            </div>
            <span v-else style="color: #999;">该学生未上传照片</span>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="currentOrder.status === 3 && evaluationData" class="evaluation-box">
          <h4 style="margin-top: 0; color: #67C23A; border-bottom: 1px solid #eee; padding-bottom: 10px;">🌟 学生评价反馈</h4>
          <div style="display: flex; align-items: center; margin-bottom: 10px;">
            <span style="margin-right: 15px; color: #666;">服务打分：</span>
            <el-rate v-model="evaluationData.score" disabled show-score text-color="#ff9900" />
          </div>
          <div style="color: #555; background: #f8f9fa; padding: 10px; border-radius: 6px;">
            "{{ evaluationData.comment || '该同学很懒，没有留下文字描述~' }}"
          </div>
        </div>
      </div>

      <template #footer>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-size: 13px; color: #999;">请仔细核对故障信息带对工具</span>
          <div>
            <el-button @click="detailVisible = false">关闭</el-button>
            <el-button v-if="currentOrder?.status === 0" type="primary" @click="handleTakeOrder">确认接单</el-button>
            <el-button v-if="currentOrder?.status === 1" type="success" @click="handleFinishOrder">登记修完</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const allOrders = ref([])
const detailVisible = ref(false)
const currentOrder = ref(null)
const evaluationData = ref(null) // 存储查到的评价数据

// 获取当前登录的师傅信息
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const currentWorkerId = userInfo.id || 2001

const getStatusConfig = (status) => {
  const configMap = {
    '-1': { text: '已撤销', type: 'info' },
    0: { text: '待接单', type: 'warning' },
    1: { text: '维修中', type: 'primary' },
    2: { text: '待评价', type: 'success' },
    3: { text: '已闭环', type: 'info' }
  }
  return configMap[status] || { text: '未知状态', type: 'info' }
}

const safeParseImages = (imgStr) => {
  if (!imgStr) return []
  try { return JSON.parse(imgStr) } 
  catch (e) { return [imgStr] }
}

const fetchAllOrders = async () => {
  loading.value = true
  try {
    const res = await request.get('/repair/list')
    allOrders.value = res || []
    allOrders.value.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  } catch (error) {
    console.error('获取列表失败', error)
  } finally {
    loading.value = false
  }
}

// 【升级】打开详情时，如果是已完成的单子，自动去查评价
const openDetail = async (row) => {
  currentOrder.value = row
  evaluationData.value = null // 先清空上次的评价数据
  detailVisible.value = true
  
  if (row.status === 3) {
    try {
      const res = await request.get(`/repair/evaluation/${row.id}`)
      evaluationData.value = res
    } catch (e) {
      console.log('该工单暂无评价数据')
    }
  }
}

// 【升级】接单时传自己的 workerId
const handleTakeOrder = async () => {
  try {
    await ElMessageBox.confirm('确认接手这个工单吗？', '操作提示', { type: 'warning' })
    // 注意这里多传了一个 workerId
    await request.post(`/repair/take/${currentOrder.value.id}?workerId=${currentWorkerId}`)
    ElMessage.success('接单成功，系统已通知该学生！')
    detailVisible.value = false
    fetchAllOrders()
  } catch (error) {
    console.log('取消操作')
  }
}

// 登记修完
const handleFinishOrder = async () => {
  try {
    await ElMessageBox.confirm('确认该故障已修复完毕吗？', '操作提示', { type: 'warning' })
    await request.post(`/repair/finish/${currentOrder.value.id}`)
    ElMessage.success('已登记修完，等待学生评价！')
    detailVisible.value = false
    fetchAllOrders()
  } catch (error) {
    console.log('取消操作')
  }
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
.image-gallery { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 5px; }
.evaluation-box { margin-top: 20px; padding: 15px; border: 1px dashed #67C23A; border-radius: 8px; background-color: #f0f9eb; }
</style>