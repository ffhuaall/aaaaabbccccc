<template>
  <div class="course-page">
    <el-card shadow="never" class="course-card">
      <div class="control-header">
        <h2 class="title">🎓 我的专属课表</h2>
        <div class="week-selector">
          <el-button circle icon="ArrowLeft" @click="changeWeek(-1)" :disabled="currentWeek <= 1" />
          <span class="week-text">第 {{ currentWeek }} 周</span>
          <el-button circle icon="ArrowRight" @click="changeWeek(1)" :disabled="currentWeek >= 20" />
        </div>
      </div>

      <div class="table-container" v-loading="loading">
        <table class="timetable">
          <thead>
            <tr>
              <th class="time-col">节次 \ 星期</th>
              <th v-for="day in weekDays" :key="day">{{ day }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(periodInfo, pIndex) in periods" :key="pIndex">
              <td class="time-cell">
                <div class="period-num">{{ periodInfo.name }}</div>
                <div class="period-time">{{ periodInfo.time }}</div>
              </td>
              <td v-for="dIndex in 7" :key="dIndex" class="course-cell-wrapper">
                <div 
                  v-if="scheduleMatrix[pIndex][dIndex - 1]" 
                  class="course-block"
                  :class="getColorClass(scheduleMatrix[pIndex][dIndex - 1].courseName)"
                >
                  <div class="c-name">{{ scheduleMatrix[pIndex][dIndex - 1].courseName }}</div>
                  <div class="c-location">
                    <el-icon><Location /></el-icon> 
                    {{ scheduleMatrix[pIndex][dIndex - 1].location }}
                  </div>
                  <div class="c-teacher">
                    <el-icon><User /></el-icon> 
                    {{ scheduleMatrix[pIndex][dIndex - 1].teacherName }}
                  </div>
                </div>
                <div v-else class="empty-block"></div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const currentWeek = ref(1) // 默认展示第1周
const courseList = ref([])

// 获取当前登录用户ID
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const studentId = userInfo.id || 1001

const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const periods = [
  { name: '第一大节', time: '08:00 - 09:40' },
  { name: '第二大节', time: '10:00 - 11:40' },
  { name: '第三大节', time: '14:00 - 15:40' },
  { name: '第四大节', time: '16:00 - 17:40' },
  { name: '第五大节', time: '19:00 - 20:40' }
]

// 【核心算法】将一维数组转换为 5行 x 7列 的二维矩阵
const scheduleMatrix = computed(() => {
  // 初始化一个 5 x 7 的 null 矩阵
  const matrix = Array.from({ length: 5 }, () => Array(7).fill(null))
  
  courseList.value.forEach(course => {
    // period 是 1-5，对应数组下标 0-4
    // dayOfWeek 是 1-7，对应数组下标 0-6
    const rowIndex = course.period - 1
    const colIndex = course.dayOfWeek - 1
    matrix[rowIndex][colIndex] = course
  })
  
  return matrix
})

// 为不同的课程动态分配糖果色背景（根据课程名长度算个伪随机哈希）
const getColorClass = (courseName) => {
  const colors = ['color-1', 'color-2', 'color-3', 'color-4', 'color-5']
  const index = courseName.length % colors.length
  return colors[index]
}

// 切换周次
const changeWeek = (step) => {
  currentWeek.value += step
  fetchSchedule()
}

// 请求后端获取指定周次的课表
const fetchSchedule = async () => {
  loading.value = true
  try {
    const res = await request.get(`/course/weekly?studentId=${studentId}&week=${currentWeek.value}`)
    courseList.value = res || []
    
    if (courseList.value.length === 0) {
      ElMessage.info(`第 ${currentWeek.value} 周您没有安排课程哦，好好休息吧！`)
    }
  } catch (error) {
    console.error('获取课表失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchSchedule()
})
</script>

<style scoped>
.course-page {
  padding-bottom: 20px;
}

.course-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 8px 24px rgba(149, 157, 165, 0.1);
}

.control-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 10px;
}

.title {
  margin: 0;
  color: #2c3e50;
  font-size: 22px;
}

.week-selector {
  display: flex;
  align-items: center;
  gap: 15px;
}

.week-text {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
  min-width: 80px;
  text-align: center;
}

/* 课表核心样式 */
.table-container {
  overflow-x: auto;
  border-radius: 12px;
  border: 1px solid #ebeef5;
}

.timetable {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed; /* 强制等宽，保持网格整齐 */
  min-width: 900px;
}

.timetable th {
  background-color: #f8f9fa;
  color: #606266;
  padding: 15px 0;
  font-weight: bold;
  border-bottom: 1px solid #ebeef5;
  border-right: 1px solid #ebeef5;
}

.timetable td {
  border-bottom: 1px dashed #ebeef5;
  border-right: 1px solid #ebeef5;
  vertical-align: top;
  height: 120px;
  padding: 8px;
}

.time-col {
  width: 100px;
}

.time-cell {
  background-color: #fafbfc;
  text-align: center;
  vertical-align: middle !important;
}

.period-num {
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.period-time {
  font-size: 12px;
  color: #999;
}

/* 具体的课程色块 */
.course-block {
  border-radius: 10px;
  padding: 10px;
  height: 100%;
  box-sizing: border-box;
  color: white;
  display: flex;
  flex-direction: column;
  transition: all 0.2s;
  cursor: pointer;
}

.course-block:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.c-name {
  font-weight: bold;
  font-size: 14px;
  margin-bottom: 8px;
  line-height: 1.3;
}

.c-location, .c-teacher {
  font-size: 12px;
  margin-top: auto; /* 把这俩挤到底部 */
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 0.9;
}

/* 糖果色背景 */
.color-1 { background: linear-gradient(135deg, #FF9A9E 0%, #FECFEF 100%); } /* 粉色 */
.color-2 { background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%); } /* 紫色 */
.color-3 { background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%); } /* 青蓝 */
.color-4 { background: linear-gradient(135deg, #fccb90 0%, #d57eeb 100%); } /* 橙紫 */
.color-5 { background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%); } /* 浅蓝 */

.empty-block {
  height: 100%;
  border-radius: 10px;
  background-color: transparent;
}
</style>