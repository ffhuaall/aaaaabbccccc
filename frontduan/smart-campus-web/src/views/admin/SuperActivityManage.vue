<template>
  <div class="super-activity-page">
    
    <!-- 系统管理员数据总览 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card bg-purple">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">全校历史活动总数</div>
              <div class="stat-value">{{ allActivities.length }} <span class="unit">场</span></div>
            </div>
            <el-icon class="stat-icon"><DataLine /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card bg-blue">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">当前全校进行中</div>
              <div class="stat-value">{{ ongoingCount }} <span class="unit">场</span></div>
            </div>
            <el-icon class="stat-icon"><Activity /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card bg-red">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">全校累计参与人次</div>
              <div class="stat-value">{{ totalParticipants }} <span class="unit">人次</span></div>
            </div>
            <el-icon class="stat-icon"><UserFilled /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统管理员工作台 -->
    <el-card shadow="never" class="list-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="emoji">🛡️</span>
            <span class="title">全校活动审计中心</span>
          </div>
          
          <div class="header-right">
            <!-- 复合搜索舱 -->
            <el-select v-model="filter.category" placeholder="活动分类" clearable style="width: 120px; margin-right: 12px;">
              <el-option label="学术讲座" value="学术讲座" />
              <el-option label="文体演艺" value="文体演艺" />
              <el-option label="志愿服务" value="志愿服务" />
              <el-option label="社团招新" value="社团招新" />
            </el-select>
            
            <el-select v-model="filter.status" placeholder="状态筛选" clearable style="width: 110px; margin-right: 12px;">
              <el-option label="进行中" :value="1" />
              <el-option label="已结束" :value="0" />
            </el-select>

            <el-input 
              v-model="filter.keyword" 
              placeholder="搜索活动名称 / 组织者ID..." 
              clearable 
              prefix-icon="Search"
              style="width: 240px; margin-right: 15px;" 
            />
            <el-button type="primary" plain icon="Refresh" @click="fetchList">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 数据表格 -->
      <el-table :data="displayActivities" style="width: 100%" v-loading="loading" stripe border size="small">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="海报" width="80" align="center">
          <template #default="scope">
            <el-image 
              v-if="scope.row.coverImage" 
              :src="scope.row.coverImage" 
              :preview-src-list="[scope.row.coverImage]"
              fit="cover" 
              class="table-poster"
              preview-teleported
            />
            <el-tag v-else type="info" size="small">无图</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="活动名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="90" align="center" />
        
        <!-- 显示活动发布者 -->
        <el-table-column prop="publisherId" label="发布者ID" width="90" align="center">
          <template #default="scope">
            <el-tag type="warning" size="small" effect="plain">{{ scope.row.publisherId }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="报名进度" width="130" align="center">
          <template #default="scope">
            <div class="progress-text">{{ scope.row.currentEnrollment || 0 }} / {{ scope.row.capacity || '不限' }}</div>
            <el-progress 
              :percentage="getPercentage(scope.row)" 
              :show-text="false" 
              stroke-width="4" 
              :status="scope.row.currentEnrollment >= scope.row.capacity ? 'exception' : ''"
            />
          </template>
        </el-table-column>

        <el-table-column prop="startTime" label="开始日期" width="110" align="center">
           <template #default="scope">{{ scope.row.startTime?.substring(0, 10) }}</template>
        </el-table-column>

        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" effect="dark" size="small">
              {{ scope.row.status === 1 ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="强力干预" width="230" align="center" fixed="right">
          <template #default="scope">
            <div class="op-btns">
              <el-button type="primary" link size="small" @click="showAdminDetails(scope.row)">详情</el-button>
              <el-button type="success" link size="small" @click="viewParticipants(scope.row)">人员名单</el-button>
              <el-dropdown trigger="click" style="margin-left: 8px;">
                <el-button type="danger" link size="small">更多<el-icon><ArrowDown /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="scope.row.status === 1" @click="forceStopActivity(scope.row.id)">停止报名</el-dropdown-item>
                    <el-dropdown-item style="color: #F56C6C" @click="forceDeleteActivity(scope.row.id)">删除活动</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 活动详情 -->
    <el-dialog v-model="detailVisible" title="活动全案详情" width="600px" destroy-on-close :lock-scroll="false">
      <div v-if="previewActivity" class="audit-detail">
        <div class="poster-preview">
          <el-image v-if="previewActivity.coverImage" :src="previewActivity.coverImage" fit="contain" class="full-poster" />
          <div v-else class="empty-poster">该活动未上传海报</div>
        </div>
        <el-descriptions :column="2" border size="small" direction="vertical">
          <el-descriptions-item label="活动名称" :span="2"><strong>{{ previewActivity.title }}</strong></el-descriptions-item>
          <el-descriptions-item label="发布者ID">
            <el-tag type="danger" size="small">{{ previewActivity.publisherId }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="活动分类">{{ previewActivity.category }}</el-descriptions-item>
          <el-descriptions-item label="名额上限">{{ previewActivity.capacity || '不限' }}</el-descriptions-item>
          <el-descriptions-item label="活动地点">{{ previewActivity.location }}</el-descriptions-item>
          <el-descriptions-item label="起止时间" :span="2">{{ previewActivity.startTime }} 至 {{ previewActivity.endTime }}</el-descriptions-item>
          <el-descriptions-item label="详细内容" :span="2"><div class="content-box">{{ previewActivity.content }}</div></el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 活动名单 -->
    <el-dialog v-model="participantsVisible" :title="`【${selectedActivity?.title}】全局报名名单`" width="700px" destroy-on-close>
      <div style="margin-bottom: 15px; display: flex; justify-content: space-between; align-items: center;">
        <span style="font-size: 14px; color: #606266;">
          全库已报有效人数：<strong>{{ validParticipantCount }}</strong> 人
        </span>
        <el-button type="success" icon="Download" size="small" @click="exportCSV">导出完整名单 (CSV)</el-button>
      </div>

      <el-table :data="participantList" border size="small" height="350px">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="realName" label="学生姓名" width="120" />
        <el-table-column prop="username" label="学号" width="150" />
        <el-table-column prop="createTime" label="报名时间" width="160" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '已报名' : '已取消/退赛' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="强制操作" align="center" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 1 && selectedActivity?.status === 1" 
              type="danger" 
              link 
              size="small" 
              @click="forceAuditUser(scope.row.regId)"
            >取消报名</el-button>
            <span v-else style="color: #ccc; font-size: 12px;">无操作</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const allActivities = ref([])

const filter = reactive({ keyword: '', category: '', status: null })

const displayActivities = computed(() => {
  return allActivities.value.filter(item => {
    if (typeof filter.status === 'number' && item.status !== filter.status) return false
    if (filter.category && item.category !== filter.category) return false
    if (filter.keyword) {
      const kw = filter.keyword.toLowerCase()
      if (!item.title.toLowerCase().includes(kw) && !String(item.publisherId).includes(kw)) {
        return false
      }
    }
    return true
  })
})

const ongoingCount = computed(() => allActivities.value.filter(i => i.status === 1).length)
const totalParticipants = computed(() => {
  return allActivities.value.reduce((sum, item) => sum + (item.currentEnrollment || 0), 0)
})

//列表数据与操作
const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/activity/list')
    allActivities.value = (res || []).sort((a, b) => b.id - a.id)
  } finally { loading.value = false }
}

const getPercentage = (item) => {
  if (!item.capacity) return 0
  return Math.min(Math.floor(((item.currentEnrollment || 0) / item.capacity) * 100), 100)
}

//详情
const detailVisible = ref(false)
const previewActivity = ref(null)
const showAdminDetails = (row) => { previewActivity.value = row; detailVisible.value = true; }

//强制停止
const forceStopActivity = (id) => {
  ElMessageBox.confirm('【超管权限】确定要强制提前结束该活动的报名吗？', '风控干预', { type: 'warning' }).then(async () => {
    await request.post(`/activity/stop/${id}`)
    ElMessage.success('活动已强制停止')
    fetchList()
  }).catch(() => {})
}

//强制删除
const forceDeleteActivity = (id) => {
  ElMessageBox.confirm('【危险】此操作将从数据库中彻底抹除该活动及其所有报名记录，确认执行？', '最高权限干预', { type: 'error' }).then(async () => {
    try {
      await request.delete(`/activity/delete/${id}`)
      ElMessage.success('活动数据已被彻底抹除')
      fetchList()
    } catch (e) {
      ElMessage.warning('底层接口正在对接中')
    }
  }).catch(() => {})
}

//人数名单
const participantsVisible = ref(false)
const participantList = ref([])
const selectedActivity = ref(null)

const validParticipantCount = computed(() => {
  return participantList.value.filter(p => p.status === 1).length
})

const viewParticipants = async (row) => {
  selectedActivity.value = row
  participantsVisible.value = true
  const res = await request.get(`/activity/participants/${row.id}`)
  participantList.value = (res || []).sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
}

const forceAuditUser = async (regId) => {
  ElMessageBox.confirm('【超管权限】强制取消该名学生的参与资格？', '操作确认', { type: 'warning' }).then(async () => {
    await request.post(`/activity/audit-participant?regId=${regId}&status=0`)
    ElMessage.success('已取消该生资格')
    viewParticipants(selectedActivity.value)
    fetchList() 
  }).catch(() => {})
}

const exportCSV = () => {
  if (participantList.value.length === 0) {
    ElMessage.warning('当前没有任何数据可导出')
    return
  }
  let csv = "\uFEFF学生姓名,学号,报名时间,当前状态\n"
  participantList.value.forEach(r => csv += `${r.realName},${r.username},${r.createTime},${r.status === 1 ? '已报名' : '已取消/退赛'}\n`)
  const blob = new Blob([csv], { type: 'text/csv' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement("a"); a.href = url; a.download = `全案名单审计_${selectedActivity.value.title}.csv`; a.click()
}

onMounted(fetchList)
</script>

<style scoped>
.super-activity-page { padding-bottom: 20px; }

.stat-row { margin-bottom: 20px; }
.stat-card { border-radius: 12px; border: none; color: white; }
.bg-purple { background: linear-gradient(135deg, #9c27b0 0%, #b388ff 100%); }
.bg-blue { background: linear-gradient(135deg, #1890ff 0%, #53a8ff 100%); }
.bg-red { background: linear-gradient(135deg, #f56c6c 0%, #ff9999 100%); }
.stat-content { display: flex; justify-content: space-between; align-items: center; padding: 10px; }
.stat-title { font-size: 14px; opacity: 0.9; margin-bottom: 5px; }
.stat-value { font-size: 32px; font-weight: bold; }
.stat-value .unit { font-size: 14px; font-weight: normal; opacity: 0.8; }
.stat-icon { font-size: 48px; opacity: 0.3; }

.list-card { border-radius: 12px; border: none; min-height: 500px; }
.card-header { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.header-left { display: flex; align-items: center; font-weight: bold; font-size: 16px; }
.header-left .emoji { font-size: 20px; margin-right: 8px; }

.table-poster { width: 45px; height: 28px; border-radius: 4px; border: 1px solid #eee; }
.progress-text { font-size: 12px; color: #909399; font-weight: bold; margin-bottom: 4px; }
.poster-preview { width: 100%; height: 180px; background: #2c3e50; border-radius: 8px; margin-bottom: 15px; display: flex; justify-content: center; align-items: center; }
.full-poster { width: 100%; height: 100%; }
.empty-poster { color: #909399; font-size: 13px; }
.content-box { white-space: pre-wrap; color: #666; padding: 10px; background: #fafafa; border-radius: 4px; border: 1px dashed #dcdfe6; }
.op-btns { display: flex; justify-content: center; align-items: center; gap: 4px; }
</style>