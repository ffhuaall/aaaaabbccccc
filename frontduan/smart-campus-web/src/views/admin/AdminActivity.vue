<template>
  <div class="admin-activity-page">
    <el-card shadow="never" class="list-card">
      <template #header>
        <div class="card-header">
          <span class="emoji">📢</span>
          <span class="title">校园活动发布管理</span>
          <el-button type="primary" icon="Plus" style="margin-left: auto;" @click="openAddDialog">
            发布新活动
          </el-button>
        </div>
      </template>

      <el-table :data="allActivities" style="width: 100%" v-loading="loading" stripe>
        <el-table-column prop="title" label="活动标题" min-width="180" />
        <el-table-column prop="startTime" label="开始时间" width="160" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="管理操作" width="220" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" plain @click="viewParticipants(scope.row)">
              报名名单
            </el-button>
            <el-button type="danger" size="small" plain @click="handleStop(scope.row.id)">
              停止报名
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="addVisible" title="发布新校园活动" width="500px" append-to-body>
      <el-form :model="activityForm" label-position="top">
        <el-form-item label="活动名称">
          <el-input v-model="activityForm.title" placeholder="如：2024 校园电竞节" />
        </el-form-item>
        <el-form-item label="活动介绍">
          <el-input v-model="activityForm.content" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="活动时间">
          <el-date-picker
            v-model="activityTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdd">立即发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="userVisible" :title="`报名名单 - ${selectedActivity?.title}`" width="700px" append-to-body>
      <el-table :data="participantList" height="400">
        <el-table-column prop="realName" label="学生姓名" />
        <el-table-column prop="username" label="学号" />
        <el-table-column prop="createTime" label="报名时间" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '已报名' : '已劝退' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 1" 
              type="danger" 
              size="small" 
              link 
              @click="auditUser(scope.row.regId, 0)">
              取消资格
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const allActivities = ref([])

// 发布相关
const addVisible = ref(false)
const activityTimeRange = ref([])
const activityForm = reactive({
  title: '',
  content: '',
  publisherId: JSON.parse(localStorage.getItem('userInfo'))?.id || 1
})

// 人员管理相关
const userVisible = ref(false)
const selectedActivity = ref(null)
const participantList = ref([])

const fetchList = async () => {
  loading.value = true
  const res = await request.get('/activity/list')
  allActivities.value = res || []
  loading.value = false
}

const openAddDialog = () => { addVisible.value = true }

const submitAdd = async () => {
  if (activityTimeRange.value) {
    activityForm.startTime = activityTimeRange.value[0]
    activityForm.endTime = activityTimeRange.value[1]
  }
  await request.post('/activity/add', activityForm)
  ElMessage.success('发布成功！')
  addVisible.value = false
  fetchList()
}

const viewParticipants = async (act) => {
  selectedActivity.value = act
  const res = await request.get(`/activity/participants/${act.id}`)
  participantList.value = res || []
  userVisible.value = true
}

const auditUser = async (regId, status) => {
  await request.post(`/activity/audit-participant?regId=${regId}&status=${status}`)
  ElMessage.success('操作成功')
  // 刷新当前名单
  viewParticipants(selectedActivity.value)
}

onMounted(fetchList)
</script>

<style scoped>
.admin-activity-page { padding-bottom: 20px; }
.list-card { border-radius: 12px; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.card-header { display: flex; align-items: center; font-size: 18px; font-weight: bold; }
.card-header .emoji { font-size: 24px; margin-right: 10px; }
</style>