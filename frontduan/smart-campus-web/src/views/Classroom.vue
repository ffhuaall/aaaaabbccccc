<template>
  <div class="classroom-page">
    <el-card shadow="never" class="search-card">
      <template #header>
        <div class="card-header">
          <span class="title">空闲教室查询</span>
        </div>
      </template>

      <div class="filter-section">
        <el-form :inline="true" :model="queryForm" class="demo-form-inline">
          <el-form-item label="查询日期">
            <el-date-picker 
              v-model="queryForm.date" 
              type="date" 
              placeholder="选择日期" 
              value-format="YYYY-MM-DD" 
              style="width: 150px" 
              :clearable="false"
            />
          </el-form-item>
          
          <el-form-item label="上课节次">
            <el-select v-model="queryForm.period" placeholder="选择节次" style="width: 150px">
              <el-option label="第 1-2 节 (上午)" :value="1" />
              <el-option label="第 3-4 节 (上午)" :value="2" />
              <el-option label="第 5-6 节 (下午)" :value="3" />
              <el-option label="第 7-8 节 (下午)" :value="4" />
              <el-option label="第 9-10 节 (晚自习)" :value="5" />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSearch" :loading="loading">
              查询
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-loading="loading" class="result-section">
        <div v-if="hasSearched">
           <el-table :data="tableData" border stripe style="width: 100%; max-width: 600px;">
              <el-table-column type="index" label="序号" width="80" align="center" />
              <el-table-column prop="roomName" label="教室名称" align="center" />
              <el-table-column label="当前状态" align="center" width="120">
                 <template #default>
                    <el-tag type="success" effect="plain">空闲可用</el-tag>
                 </template>
              </el-table-column>
              <template #empty><el-empty description="该时间段暂无空闲教室" /></template>
           </el-table>
        </div>
        
        <div v-else class="welcome-state">
          请在上方选择日期和节次进行查询
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const hasSearched = ref(false)
const idleRooms = ref([])

// 默认获取今天的日期，格式化为 YYYY-MM-DD
const getToday = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const queryForm = reactive({
  date: getToday(),
  period: 1
})

// 将简单的字符串数组转换为 El-Table 需要的对象数组格式
const tableData = computed(() => {
  return idleRooms.value.map(room => ({ roomName: room }))
})

const handleSearch = async () => {
  if (!queryForm.date) {
    ElMessage.warning('请选择查询日期')
    return
  }
  loading.value = true
  hasSearched.value = true
  try {
    const res = await request.get(`/classroom/idle?date=${queryForm.date}&period=${queryForm.period}`)
    idleRooms.value = res || []
  } catch (error) {
    console.error('查询失败', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.classroom-page { padding-bottom: 20px; }
.search-card { border-radius: 12px; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.05); min-height: 600px; }
.card-header { display: flex; align-items: center; font-size: 16px; font-weight: bold; color: #333; }
.filter-section { background: #f8f9fa; padding: 20px 20px 0; border-radius: 8px; margin-bottom: 20px; }
.welcome-state { text-align: center; color: #999; margin-top: 50px; font-size: 15px; }
</style>