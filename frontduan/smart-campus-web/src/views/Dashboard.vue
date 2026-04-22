<template>
  <div class="dashboard-page">
    
    <div v-if="userInfo.roleId === 2" class="worker-dashboard">
      <h2 style="margin-bottom: 20px; color: #333;">师傅您好，今天是辛勤工作的一天！🔧</h2>
      
      <el-row :gutter="20" class="stat-cards">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card blue">
            <div class="stat-title">待接工单</div>
            <div class="stat-value">{{ workerStats.poolCount }}</div>
            <el-icon class="stat-icon"><List /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card warning">
            <div class="stat-title">我的未完成待办</div>
            <div class="stat-value">{{ workerStats.todoCount }}</div>
            <el-icon class="stat-icon"><Timer /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card success">
            <div class="stat-title">我的历史总完成量</div>
            <div class="stat-value">{{ workerStats.doneCount }}</div>
            <el-icon class="stat-icon"><CircleCheck /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card purple">
            <div class="stat-title">本月好评率</div>
            <div class="stat-value">98.5<span style="font-size: 16px;">%</span></div>
            <el-icon class="stat-icon"><Star /></el-icon>
          </el-card>
        </el-col>
      </el-row>
      
      <el-card shadow="never" style="margin-top: 20px; border-radius: 12px;">
        <template #header><div style="font-weight: bold;">📢 维修人员操作规范</div></template>
        <el-timeline>
          <el-timeline-item type="primary" size="large" timestamp="第一步：及时接单">在工单大厅查看并接手任务，系统会自动通知报修学生。</el-timeline-item>
          <el-timeline-item type="warning" size="large" timestamp="第二步：上门处理">携带工具根据“宿舍位置”和“现场照片”进行精准维修。</el-timeline-item>
          <el-timeline-item type="success" size="large" timestamp="第三步：登记闭环">维修完成后，务必在系统内点击“登记修完”，等待学生进行最终的星级打分！</el-timeline-item>
        </el-timeline>
      </el-card>
    </div>


    <div v-else class="general-dashboard">
      <h2 style="margin-bottom: 20px; color: #333;">智慧校园全景数据大盘 🌐</h2>
      
      <el-row :gutter="20" class="stat-cards">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card blue">
            <div class="stat-title">全站注册用户</div>
            <div class="stat-value">1,254</div>
            <el-icon class="stat-icon"><User /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card success">
            <div class="stat-title">累计举办活动</div>
            <div class="stat-value">38</div>
            <el-icon class="stat-icon"><Medal /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card warning">
            <div class="stat-title">今日报修单数</div>
            <div class="stat-value">12</div>
            <el-icon class="stat-icon"><Tools /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card purple">
            <div class="stat-title">系统平稳运行</div>
            <div class="stat-value">256 <span style="font-size: 16px;">天</span></div>
            <el-icon class="stat-icon"><Odometer /></el-icon>
          </el-card>
        </el-col>
      </el-row>
      
      <el-card shadow="never" style="margin-top: 20px; border-radius: 12px; min-height: 300px;">
        <el-empty description="欢迎使用智慧校园综合服务大厅" />
      </el-card>
    </div>
    
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const currentWorkerId = userInfo.id || 2001

// 维修师傅的数据状态
const workerStats = reactive({
  poolCount: 0,
  todoCount: 0,
  doneCount: 0
})

// 如果是师傅登录，顺便请求一下 /repair/list 统计属于他的数据
const fetchWorkerStats = async () => {
  if (userInfo.roleId !== 2) return
  try {
    const res = await request.get('/repair/list')
    const allOrders = res || []
    
    workerStats.poolCount = allOrders.filter(item => item.status === 0).length
    workerStats.todoCount = allOrders.filter(item => (item.status === 1 || item.status === 2) && item.workerId === currentWorkerId).length
    workerStats.doneCount = allOrders.filter(item => item.status === 3 && item.workerId === currentWorkerId).length
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

onMounted(() => {
  fetchWorkerStats()
})
</script>

<style scoped>
.dashboard-page { padding-bottom: 20px; }

/* 炫酷的统一样式卡片 */
.stat-card {
  position: relative;
  border-radius: 12px;
  border: none;
  color: white;
  overflow: hidden;
}

.stat-card.blue { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-card.success { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.stat-card.warning { background: linear-gradient(135deg, #f6d365 0%, #fda085 100%); }
.stat-card.purple { background: linear-gradient(135deg, #c471f5 0%, #fa71cd 100%); }

.stat-title {
  font-size: 15px;
  opacity: 0.9;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
}

.stat-icon {
  position: absolute;
  right: 15px;
  bottom: 15px;
  font-size: 60px;
  opacity: 0.2;
}
</style>