<template>
  <div class="classroom-page">
    <el-card shadow="never" class="search-card">
      <div class="header-title">
        <el-icon class="title-icon"><Search /></el-icon>
        <span>空闲教室快速查询</span>
      </div>
      
      <div class="search-bar">
        <span class="label">日期：</span>
        <el-date-picker
          v-model="queryParams.date"
          type="date"
          placeholder="选择查询日期"
          value-format="YYYY-MM-DD"
          :clearable="false"
          style="width: 200px;"
        />
        
        <span class="label" style="margin-left: 20px;">时间段：</span>
        <el-select v-model="queryParams.period" placeholder="选择节次" style="width: 150px;">
          <el-option label="上午 (1-4节)" :value="1" />
          <el-option label="下午 (5-8节)" :value="2" />
          <el-option label="晚上 (9-11节)" :value="3" />
        </el-select>
        
        <el-button type="primary" icon="Search" class="search-btn" @click="handleSearch" :loading="loading">
          立即查询
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="result-card">
      <el-table 
        :data="tableData" 
        style="width: 100%" 
        v-loading="loading" 
        element-loading-text="Redis 极速加载中..."
        stripe
      >
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="buildingName" label="教学楼" align="center">
          <template #default="scope">
            <el-tag type="info" effect="plain">{{ scope.row.buildingName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="roomNumber" label="教室门牌号" align="center">
          <template #default="scope">
            <span style="font-weight: bold; font-size: 16px;">{{ scope.row.roomNumber }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="容纳人数 (座位)" align="center">
          <template #default="scope">
            <el-icon><User /></el-icon> {{ scope.row.capacity }} 人
          </template>
        </el-table-column>
        
        <template #empty>
          <el-empty description="该时间段暂无空闲教室，换个时间试试吧" />
        </template>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])

// 搜索条件：默认填入今天的日期和晚自习(3)
const today = new Date().toISOString().split('T')[0] // 获取今天日期 YYYY-MM-DD
const queryParams = ref({
  date: today,
  period: 3
})

// 触发查询
const handleSearch = async () => {
  if (!queryParams.value.date) {
    ElMessage.warning('请先选择日期！')
    return
  }
  
  loading.value = true
  try {
    // 调用后端接口：GET /classroom/idle?date=xxx&period=xxx
    const res = await request.get('/classroom/idle', {
      params: queryParams.value
    })
    tableData.value = res || []
    
    if (tableData.value.length > 0) {
      ElMessage.success(`查询成功，找到 ${tableData.value.length} 个空闲教室！`)
    }
  } catch (error) {
    console.error('查询失败', error)
  } finally {
    loading.value = false
  }
}

// 进页面时自动查一次
onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.classroom-page {
  padding-bottom: 20px;
}

.search-card {
  border-radius: 12px;
  margin-bottom: 20px;
  border: none;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.header-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
}

.title-icon {
  font-size: 22px;
  color: #409EFF;
  margin-right: 8px;
}

.search-bar {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
}

.label {
  font-size: 14px;
  color: #606266;
  margin-right: 10px;
  font-weight: bold;
}

.search-btn {
  margin-left: auto; /* 把按钮挤到最右边 */
}

.result-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  min-height: 400px;
}
</style>