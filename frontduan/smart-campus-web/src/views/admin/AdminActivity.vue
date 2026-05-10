<template>
  <div class="admin-activity-page">
    
    <el-row :gutter="20" class="stat-row">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card bg-blue">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">累计发布活动</div>
              <div class="stat-value">{{ myActivities.length }} <span class="unit">场</span></div>
            </div>
            <el-icon class="stat-icon"><Calendar /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card bg-green">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">当前进行中</div>
              <div class="stat-value">{{ ongoingCount }} <span class="unit">场</span></div>
            </div>
            <el-icon class="stat-icon"><VideoPlay /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card bg-orange">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">累计服务学生</div>
              <div class="stat-value">{{ totalParticipants }} <span class="unit">人次</span></div>
            </div>
            <el-icon class="stat-icon"><UserFilled /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="list-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="emoji">🎪</span>
            <span class="title">我的活动列表</span>
          </div>
          
          <div class="header-right">
            <el-select v-model="filter.status" placeholder="状态筛选" clearable style="width: 120px; margin-right: 12px;">
              <el-option label="进行中" :value="1" />
              <el-option label="已结束" :value="0" />
            </el-select>
            <el-input 
              v-model="filter.keyword" 
              placeholder="搜索活动名称..." 
              clearable 
              prefix-icon="Search"
              style="width: 200px; margin-right: 15px;" 
            />
            <el-button type="primary" icon="Plus" @click="openAddDialog">策划新活动</el-button>
          </div>
        </div>
      </template>

      <el-table :data="displayActivities" style="width: 100%" v-loading="loading" stripe border size="small">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="海报" width="90" align="center">
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
        <el-table-column prop="title" label="活动名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="100" align="center" />
        
        <el-table-column label="报名进度" width="140" align="center">
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

        <el-table-column prop="startTime" label="开始日期" width="120" align="center">
           <template #default="scope">{{ scope.row.startTime?.substring(0, 10) }}</template>
        </el-table-column>

        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" effect="dark" size="small">
              {{ scope.row.status === 1 ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="管理操作" width="230" align="center" fixed="right">
          <template #default="scope">
            <div class="op-btns">
              <el-button type="primary" link size="small" @click="showAdminDetails(scope.row)">预览</el-button>
              <el-button type="success" link size="small" @click="viewParticipants(scope.row)">名单</el-button>
              <el-dropdown trigger="click" style="margin-left: 8px;">
                <el-button type="info" link size="small">更多<el-icon><ArrowDown /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="scope.row.status === 1" @click="stopActivity(scope.row.id)">停止报名</el-dropdown-item>
                    <el-dropdown-item style="color: #F56C6C" @click="deleteActivity(scope.row.id)">删除活动</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="活动发布预览" width="600px" destroy-on-close :lock-scroll="false">
      <div v-if="previewActivity" class="audit-detail">
        <div class="poster-preview">
          <el-image v-if="previewActivity.coverImage" :src="previewActivity.coverImage" fit="contain" class="full-poster" />
          <div v-else class="empty-poster">未上传海报</div>
        </div>
        <el-descriptions :column="2" border size="small" direction="vertical">
          <el-descriptions-item label="活动名称" :span="2"><strong>{{ previewActivity.title }}</strong></el-descriptions-item>
          <el-descriptions-item label="活动分类">{{ previewActivity.category }}</el-descriptions-item>
          <el-descriptions-item label="名额上限">{{ previewActivity.capacity || '不限' }}</el-descriptions-item>
          <el-descriptions-item label="活动地点" :span="2">{{ previewActivity.location }}</el-descriptions-item>
          <el-descriptions-item label="详细内容" :span="2"><div class="content-box">{{ previewActivity.content }}</div></el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <el-dialog v-model="addVisible" title="策划新活动" width="620px" destroy-on-close :lock-scroll="false">
      <el-form :model="form" :rules="rules" ref="addFormRef" label-width="100px">
        <el-form-item label="活动名称" prop="title"><el-input v-model="form.title" /></el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" style="width:100%">
                <el-option label="学术讲座" value="学术讲座" />
                <el-option label="文体演艺" value="文体演艺" />
                <el-option label="志愿服务" value="志愿服务" />
                <el-option label="社团招新" value="社团招新" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="名额" prop="capacity"><el-input-number v-model="form.capacity" :min="0" style="width:100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="活动海报">
          <el-upload action="http://localhost:8080/file/upload" :headers="uploadHeaders" list-type="picture-card" :limit="1" :on-success="handleImageSuccess" v-model:file-list="fileList">
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="起止时间" prop="timeRange">
          <el-date-picker v-model="form.timeRange" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="地点" prop="location"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="详述" prop="content"><el-input v-model="form.content" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAdd">正式发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="participantsVisible" :title="`报名名单`" width="700px" destroy-on-close>
      <div style="margin-bottom: 15px; display: flex; justify-content: space-between;">
        <el-button type="success" size="small" @click="exportCSV">导出签到表 (CSV)</el-button>
      </div>
      <el-table :data="participantList" border size="small" height="350px">
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="username" label="学号" />
        <el-table-column label="状态"><template #default="s"><el-tag size="small">{{ s.row.status === 1 ? '已报名' : '已取消' }}</el-tag></template></el-table-column>
        <el-table-column label="操作"><template #default="s"><el-button type="danger" link size="small" @click="auditUser(s.row.regId, 0)">取消资格</el-button></template></el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

const currentUserId = Number(userInfo.id) || 1001 
const uploadHeaders = { token: localStorage.getItem('Authorization') }

const loading = ref(false)
const allActivities = ref([])

const filter = reactive({ keyword: '', status: null })


const myActivities = computed(() => allActivities.value.filter(i => i.publisherId === currentUserId))

const displayActivities = computed(() => {
  return myActivities.value.filter(item => {
    if (typeof filter.status === 'number' && item.status !== filter.status) return false
    if (filter.keyword && !item.title.includes(filter.keyword)) return false
    return true
  })
})

const ongoingCount = computed(() => myActivities.value.filter(i => i.status === 1).length)
const totalParticipants = computed(() => {
  return myActivities.value.reduce((sum, item) => sum + (item.currentEnrollment || 0), 0)
})

const detailVisible = ref(false)
const previewActivity = ref(null)
const showAdminDetails = (row) => { previewActivity.value = row; detailVisible.value = true; }

const addVisible = ref(false)
const submitting = ref(false)
const fileList = ref([])
const form = reactive({ title: '', category: '', capacity: 0, timeRange: [], location: '', content: '', coverImage: '' })
const rules = { title: [{ required: true }], category: [{ required: true }], timeRange: [{ required: true }] }

const handleImageSuccess = (res) => { if (res.code === 200) form.coverImage = res.data }
const openAddDialog = () => { fileList.value = []; form.coverImage = ''; addVisible.value = true; }

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/activity/list')
    allActivities.value = (res || []).sort((a, b) => b.id - a.id)
  } finally { loading.value = false }
}

const submitAdd = async () => {
  submitting.value = true
  try {
    const data = { ...form, startTime: form.timeRange[0], endTime: form.timeRange[1], publisherId: currentUserId }
    delete data.timeRange
    await request.post('/activity/add', data)
    ElMessage.success('发布成功')
    addVisible.value = false
    fetchList()
  } finally { submitting.value = false }
}

const stopActivity = (id) => {
  ElMessageBox.confirm('停止报名后学生将无法继续加入，确认操作？', '停止确认').then(async () => {
    await request.post(`/activity/stop/${id}`)
    ElMessage.success('已停止')
    fetchList()
  }).catch(() => {})
}

const deleteActivity = (id) => {
  ElMessageBox.confirm('物理删除将不可恢复，确认执行？', '危险操作', { type: 'error' }).then(async () => {
    try {
      await request.delete(`/activity/delete/${id}`)
      ElMessage.success('删除成功')
      fetchList()
    } catch (e) {
      ElMessage.warning('功能正在对接中，稍后可用')
    }
  }).catch(() => {})
}

const participantsVisible = ref(false)
const participantList = ref([])
const selectedActivity = ref(null)
const viewParticipants = async (row) => {
  selectedActivity.value = row
  participantsVisible.value = true
  const res = await request.get(`/activity/participants/${row.id}`)
  participantList.value = res || []
}

const auditUser = async (regId) => {
  await request.post(`/activity/audit-participant?regId=${regId}&status=0`)
  ElMessage.success('已取消资格')
  viewParticipants(selectedActivity.value)
  fetchList() 
}

const exportCSV = () => {
  let csv = "\uFEFF姓名,学号,状态\n"
  participantList.value.forEach(r => csv += `${r.realName},${r.username},${r.status === 1 ? '已报名' : '已取消'}\n`)
  const blob = new Blob([csv], { type: 'text/csv' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement("a"); a.href = url; a.download = "名单.csv"; a.click()
}

const getPercentage = (item) => {
  if (!item.capacity) return 0
  return Math.min(Math.floor(((item.currentEnrollment || 0) / item.capacity) * 100), 100)
}

onMounted(fetchList)
</script>

<style scoped>
.admin-activity-page { padding-bottom: 20px; }

.stat-row { margin-bottom: 20px; }
.stat-card { border-radius: 12px; border: none; color: white; }
.bg-blue { background: linear-gradient(135deg, #409EFF 0%, #53a8ff 100%); }
.bg-green { background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%); }
.bg-orange { background: linear-gradient(135deg, #E6A23C 0%, #ebb563 100%); }
.stat-content { display: flex; justify-content: space-between; align-items: center; padding: 10px; }
.stat-title { font-size: 14px; opacity: 0.9; margin-bottom: 5px; }
.stat-value { font-size: 32px; font-weight: bold; }
.stat-value .unit { font-size: 14px; font-weight: normal; opacity: 0.8; }
.stat-icon { font-size: 48px; opacity: 0.3; }

.list-card { border-radius: 12px; border: none; min-height: 500px; }
.card-header { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.header-left { display: flex; align-items: center; font-weight: bold; font-size: 16px; }
.header-left .emoji { font-size: 20px; margin-right: 8px; }

.table-poster { width: 50px; height: 30px; border-radius: 4px; }
.progress-text { font-size: 12px; color: #409EFF; font-weight: bold; }
.poster-preview { width: 100%; height: 180px; background: #f5f7fa; border-radius: 8px; margin-bottom: 15px; display: flex; justify-content: center; align-items: center; }
.full-poster { width: 100%; height: 100%; }
.empty-poster { color: #909399; font-size: 13px; font-style: italic; }
.content-box { white-space: pre-wrap; color: #666; padding: 10px; background: #fafafa; border-radius: 4px; }
.op-btns { display: flex; justify-content: center; align-items: center; gap: 4px; }
</style>