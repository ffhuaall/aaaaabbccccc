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
          
          <el-form-item label="校区">
            <el-select v-model="queryForm.campus" placeholder="请选择" style="width: 120px" clearable>
              <el-option label="东校区" value="东校区" />
              <el-option label="南校区" value="南校区" />
              <el-option label="西校区" value="西校区" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="教学楼">
            <el-select v-model="queryForm.building" placeholder="请选择" style="width: 140px" :disabled="!queryForm.campus" clearable>
              <el-option v-for="b in currentBuildings" :key="b" :label="b" :value="b" />
            </el-select>
          </el-form-item>

          <el-form-item label="周次">
            <el-select v-model="queryForm.week" placeholder="选择周次" style="width: 110px" clearable>
              <el-option v-for="i in 20" :key="i" :label="`第 ${i} 周`" :value="i" />
            </el-select>
          </el-form-item>

          <el-form-item label="星期" clearable>
            <el-select v-model="queryForm.dayOfWeek" placeholder="选择星期" style="width: 100px" clearable>
              <el-option label="周一" :value="1" />
              <el-option label="周二" :value="2" />
              <el-option label="周三" :value="3" />
              <el-option label="周四" :value="4" />
              <el-option label="周五" :value="5" />
              <el-option label="周六" :value="6" />
              <el-option label="周日" :value="7" />
            </el-select>
          </el-form-item>
          		  
          <el-form-item label="上课节次">
            <el-select v-model="queryForm.period" placeholder="选择节次" style="width: 150px" clearable>
              <el-option label="第 1 大节 (1-2节)" :value="1" />
              <el-option label="第 2 大节 (3-4节)" :value="2" />
              <el-option label="第 3 大节 (5-6节)" :value="3" />
              <el-option label="第 4 大节 (7-8节)" :value="4" />
              <el-option label="第 5 大节 (晚课)" :value="5" />
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
           <el-table :data="tableData" border stripe style="width: 100%; max-width: 700px;">
              <el-table-column type="index" label="序号" width="80" align="center" />
              <el-table-column prop="roomName" label="教室名称" align="center" />
              
              <el-table-column prop="capacity" label="容纳人数" align="center" width="120">
                 <template #default="scope">
                    <el-tag type="info" size="small">{{ scope.row.capacity }} 人</el-tag>
                 </template>
              </el-table-column>

              <el-table-column label="当前状态" align="center" width="120">
                 <template #default>
                    <el-tag type="success" effect="plain">空闲可用</el-tag>
                 </template>
              </el-table-column>
              <template #empty><el-empty description="该时间段所选教学楼暂无空闲教室" /></template>
           </el-table>
        </div>
        
        <div v-else class="welcome-state">
          请在上方选择校区、教学楼及时间点进行查询
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const hasSearched = ref(false)
const idleRooms = ref([])

// 1. 定义校区与教学楼的映射关系
const buildingMap = {
  '东校区': ['第一教学楼', '第二教学楼'],
  '南校区': ['第三教学楼'],
  '西校区': ['第四教学楼', '实验大楼']
}

// 2. 表单数据，初始设为 null 以支持可选查询
const queryForm = reactive({
  campus: '',   
  building: '', 
  week: null,   
  dayOfWeek: null,
  period: null
})

// 3. 监听校区变化，自动重置教学楼，防止出现“东校区-第三教学楼”的错误组合
watch(() => queryForm.campus, () => {
  queryForm.building = ''
})

// 4. 计算当前选定校区下拥有的教学楼列表
const currentBuildings = computed(() => {
  return queryForm.campus ? buildingMap[queryForm.campus] : []
})

const tableData = computed(() => {
  return idleRooms.value.map(room => ({
    roomName: `${room.campus}-${room.building}-${room.roomNo}室`,
    capacity: room.capacity || 50
  }))
})

const handleSearch = async () => {
  // 基础校验：至少得选个校区，否则数据量太大
  if (!queryForm.campus) {
    ElMessage.warning('请至少选择一个校区进行查询')
    return
  }
  
  loading.value = true
  hasSearched.value = true
  
  try {
    // 构造参数对象，过滤掉为 null 或空的字段
    const params = {}
    if (queryForm.campus) params.campus = queryForm.campus
    if (queryForm.building) params.building = queryForm.building
    if (queryForm.week) params.week = queryForm.week
    if (queryForm.dayOfWeek) params.dayOfWeek = queryForm.dayOfWeek
    if (queryForm.period) params.period = queryForm.period

    const res = await request.get('/classroom/idle', { params })
    idleRooms.value = res || []
    
    if (idleRooms.value.length === 0) {
      ElMessage.info('暂无符合条件的教室')
    }
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败，请检查网络')
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