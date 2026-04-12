<template>
  <div class="dashboard-page">
    <el-skeleton :loading="loading" animated>
      <template #default>
        
        <el-row :gutter="20" class="stat-cards">
          <el-col :span="8">
            <el-card shadow="hover" class="data-card blue-card">
              <div class="stat-title">全网注册用户</div>
              <div class="stat-value">{{ statsData.totalUsers }} <span class="unit">人</span></div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" class="data-card green-card">
              <div class="stat-title">累计报修工单</div>
              <div class="stat-value">{{ statsData.repairStats?.total || 0 }} <span class="unit">件</span></div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" class="data-card orange-card">
              <div class="stat-title">工单完结率</div>
              <div class="stat-value">
                {{ repairCompletionRate }} <span class="unit">%</span>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-row style="margin-top: 20px;">
          <el-col :span="12">
            <el-card shadow="never" class="list-card">
              <template #header>
                <span style="font-weight: bold; font-size: 16px;">🔥 校园活动热度榜 (Top 5)</span>
              </template>
              
              <div v-for="(item, index) in statsData.topActivities" :key="index" class="activity-item">
                <div class="rank-box" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
                <div class="activity-title">{{ item.title }}</div>
                <div class="activity-count">
                  <el-tag size="small" type="danger" effect="light">{{ item.reg_count }} 人报名</el-tag>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

      </template>
    </el-skeleton>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'

const loading = ref(true)
const statsData = ref({})

// 计算工单完成率 (已完成 / 总数)
const repairCompletionRate = computed(() => {
  const total = statsData.value.repairStats?.total || 0
  const completed = statsData.value.repairStats?.completed || 0
  if (total === 0) return 0
  return Math.round((completed / total) * 100)
})

// 页面挂载时立刻请求后端接口
onMounted(async () => {
  try {
    loading.value = true
    // 【核心联调代码】调用我们后端的 DashboardController 接口
    const data = await request.get('/dashboard/statistics')
    statsData.value = data
  } catch (error) {
    console.error('获取大屏数据失败', error)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.dashboard-page {
  height: 100%;
}

.data-card {
  border-radius: 12px;
  color: white;
  border: none;
}
.blue-card { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.green-card { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.orange-card { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }

.stat-title {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
}
.unit {
  font-size: 14px;
  font-weight: normal;
}

.list-card {
  border-radius: 12px;
}

.activity-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px dashed #eee;
}
.activity-item:last-child {
  border-bottom: none;
}

.rank-box {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background: #f0f2f5;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 12px;
  margin-right: 15px;
}
.rank-1 { background: #ffe4e6; color: #f43f5e; }
.rank-2 { background: #ffedd5; color: #f97316; }
.rank-3 { background: #fef3c7; color: #eab308; }

.activity-title {
  flex: 1;
  color: #333;
}
</style>