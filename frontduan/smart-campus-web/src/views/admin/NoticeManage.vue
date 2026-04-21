<template>
  <div class="notice-manage-page">
    <el-card shadow="never" class="list-card">
      <template #header>
        <div class="card-header">
          <span class="emoji">📢</span>
          <span class="title">全校系统公告管理</span>
          <el-button type="primary" icon="Plus" style="margin-left: auto;" @click="openAddDialog">
            发布新公告
          </el-button>
        </div>
      </template>

      <el-table :data="noticeList" style="width: 100%" v-loading="loading" stripe border>
        <el-table-column prop="title" label="公告标题" min-width="150" show-overflow-tooltip />
        <el-table-column label="提醒级别" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.level" effect="dark">
              {{ getLevelText(scope.row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" align="center" />
        
        <el-table-column label="当前状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isActive === 1 ? 'success' : 'info'">
              {{ scope.row.isActive === 1 ? '展示中' : '已下线' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="管理操作" width="220" align="center">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button 
              :type="scope.row.isActive === 1 ? 'warning' : 'success'" 
              link 
              size="small" 
              @click="toggleStatus(scope.row)"
            >
              {{ scope.row.isActive === 1 ? '下线' : '上线' }}
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '发布新公告'" width="500px">
      <el-form :model="form" label-position="top">
        <el-form-item label="公告标题" required>
          <el-input v-model="form.title" placeholder="如：系统维护通知" />
        </el-form-item>
        <el-form-item label="提醒级别" required>
          <el-select v-model="form.level" placeholder="请选择级别" style="width: 100%;">
            <el-option label="普通通知 (蓝色)" value="info" />
            <el-option label="成功/喜报 (绿色)" value="success" />
            <el-option label="重要警告 (黄色)" value="warning" />
            <el-option label="紧急错误 (红色)" value="error" />
          </el-select>
        </el-form-item>
        <el-form-item label="公告详细内容" required>
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入公告正文..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSave">立即发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const noticeList = ref([])
const dialogVisible = ref(false)

const form = reactive({
  id: null,
  title: '',
  content: '',
  level: 'info',
  isActive: 1
})

const getLevelText = (level) => {
  const map = { info: '普通', success: '喜报', warning: '警告', error: '紧急' }
  return map[level] || '未知'
}

const fetchNotices = async () => {
  loading.value = true
  try {
    const res = await request.get('/notice/list')
    noticeList.value = res || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  form.id = null
  form.title = ''
  form.content = ''
  form.level = 'info'
  form.isActive = 1
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const submitSave = async () => {
  if (!form.title || !form.content) {
    ElMessage.warning('请填写完整的标题和内容')
    return
  }
  await request.post('/notice/save', form)
  ElMessage.success('操作成功！')
  dialogVisible.value = false
  fetchNotices()
}

const toggleStatus = async (row) => {
  const newStatus = row.isActive === 1 ? 0 : 1
  await request.post('/notice/save', { ...row, isActive: newStatus })
  ElMessage.success(newStatus === 1 ? '公告已上线展示' : '公告已安全下线')
  fetchNotices()
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这条公告记录吗？', '提示', { type: 'warning' }).then(async () => {
    // 假设后端支持根据 ID 删除
    await request.post(`/notice/delete/${id}`)
    ElMessage.success('删除成功')
    fetchNotices()
  })
}

onMounted(fetchNotices)
</script>

<style scoped>
.notice-manage-page { padding-bottom: 20px; }
.list-card { border-radius: 12px; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.05); min-height: 600px; }
.card-header { display: flex; align-items: center; font-size: 18px; font-weight: bold; }
.card-header .emoji { font-size: 24px; margin-right: 10px; }
</style>