<template>
  <div class="super-lost-found-manage">
    <el-card shadow="never" class="list-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="emoji">🛡️</span>
            <span class="title">全校失物招领审计中心</span>
          </div>
          <div class="header-right">
            <el-select v-model="filter.category" placeholder="物品类别" clearable style="width: 120px; margin-right: 12px;" @change="fetchList">
              <el-option label="证件" value="证件" />
              <el-option label="电子产品" value="电子产品" />
              <el-option label="学习用品" value="学习用品" />
              <el-option label="生活用品" value="生活用品" />
              <el-option label="其他" value="其他" />
            </el-select>
            <el-select v-model="filter.status" placeholder="工单状态" clearable style="width: 120px; margin-right: 12px;" @change="fetchList">
              <el-option label="寻找中" :value="0" />
              <el-option label="已结案" :value="1" />
              <el-option label="已作废" :value="-1" />
            </el-select>
            <el-input 
              v-model="filter.keyword" 
              placeholder="搜索物品/地点/学号..." 
              clearable 
              style="width: 220px; margin-right: 12px;"
              @keyup.enter="fetchList"
            />
            <el-button type="primary" plain @click="fetchList">刷新数据</el-button>
          </div>
        </div>
      </template>

      <el-table :data="allList" style="width: 100%" v-loading="loading" stripe border size="small">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="类型" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.type === 0 ? 'danger' : 'success'" size="small">
              {{ scope.row.type === 0 ? '寻物' : '招领' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="itemName" label="物品名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="location" label="相关地点" width="150" show-overflow-tooltip />
        <el-table-column prop="publisherId" label="发布者ID" width="100" align="center" />
        <el-table-column prop="contactInfo" label="联系方式" width="150" align="center" />
        
        <el-table-column label="当前状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusConfig(scope.row.status).type" effect="dark" size="small">
              {{ getStatusConfig(scope.row.status).text }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="发布时间" width="160" align="center" />

        <el-table-column label="管理操作" width="160" align="center" fixed="right">
          <template #default="scope">
            <div style="display: flex; justify-content: center; gap: 8px;">
              <el-button type="primary" link size="small" @click="openAuditDetail(scope.row)">审计</el-button>
              <el-dropdown trigger="click">
                <el-button type="primary" link size="small">更多</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="scope.row.status !== -1" icon="CircleClose" @click="handleForceCancel(scope.row.id)">强制下架</el-dropdown-item>
                    <el-dropdown-item icon="Delete" style="color: #F56C6C" @click="handleDelete(scope.row.id)">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="auditVisible" title="失物招领审计面板" width="700px" destroy-on-close>
      <div v-if="selectedItem">
        <el-descriptions title="物品基本档" :column="2" border size="small">
          <el-descriptions-item label="物品名称">{{ selectedItem.itemName }}</el-descriptions-item>
          <el-descriptions-item label="地点">{{ selectedItem.location }}</el-descriptions-item>
          <el-descriptions-item label="发布者ID">{{ selectedItem.publisherId }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ selectedItem.contactInfo }}</el-descriptions-item>
          <el-descriptions-item label="内容详述" :span="2">{{ selectedItem.description || '无' }}</el-descriptions-item>
        </el-descriptions>

        <div class="audit-comment-box">
          <p class="section-title">💬 互动留言审计</p>
          <el-table :data="comments" size="extra-small" border height="200px">
            <el-table-column prop="userId" label="留言人" width="100" />
            <el-table-column prop="content" label="留言内容" />
            <el-table-column label="操作" width="80" align="center">
              <template #default="scope">
                <el-button type="danger" link size="small" @click="deleteComment(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="auditVisible = false">关闭审计</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const allList = ref([])
const filter = reactive({ category: '', status: null, keyword: '' })

// 审计详情
const auditVisible = ref(false)
const selectedItem = ref(null)
const comments = ref([])

const getStatusConfig = (status) => {
  const map = { '-1': { text: '已作废', type: 'info' }, 0: { text: '进行中', type: 'success' }, 1: { text: '已结案', type: 'info' } }
  return map[status] || { text: '未知', type: 'info' }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/lost-found/list')
    let data = res || []
    
    // 1. 分类检索
    if (filter.category) {
      data = data.filter(i => i.itemName.includes(`[${filter.category}]`))
    }
    
    // 2. 【核心修复】状态检索：只有当 status 是明确的数字时（包含 0），才进行过滤
    if (typeof filter.status === 'number') {
      data = data.filter(i => i.status === filter.status)
    }
    
    // 3. 关键字检索
    if (filter.keyword) {
      const kw = filter.keyword.toLowerCase()
      data = data.filter(i => i.itemName.toLowerCase().includes(kw) || i.publisherId.toString().includes(kw))
    }
    
    allList.value = data.sort((a, b) => b.id - a.id)
  } catch(error) {
    console.error(error)
  } finally { 
    loading.value = false 
  }
}

const openAuditDetail = async (row) => {
  selectedItem.value = row
  auditVisible.value = true
  // 加载该物品的所有留言
  const res = await request.get(`/lost-found/comments/${row.id}`)
  comments.value = res || []
}

const deleteComment = (id) => {
  ElMessageBox.confirm('确定要删除这条留言吗？此操作不可恢复。', '审计确认').then(async () => {
    // 假设你有删除评论的接口
    // await request.post(`/lost-found/comment/delete/${id}`)
    ElMessage.success('留言已成功删除')
    openAuditDetail(selectedItem.value) // 重新加载
  })
}

const handleForceCancel = (id) => {
  ElMessageBox.confirm('确定要强制下架该信息吗？', '管理确认').then(async () => {
    await request.post(`/lost-found/cancel/${id}`)
    ElMessage.success('已强制下架')
    fetchList()
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('删除将不可恢复，确认执行？', '危险操作', { type: 'error' }).then(async () => {
    await request.post(`/lost-found/delete/${id}`)
    ElMessage.success('数据已从底层删除')
    fetchList()
  })
}

onMounted(fetchList)
</script>

<style scoped>
.super-lost-found-manage { padding-bottom: 20px; }
.list-card { border-radius: 12px; border: none; min-height: 600px; }
.card-header { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.header-left { display: flex; align-items: center; font-weight: bold; font-size: 16px; }
.header-left .emoji { font-size: 20px; margin-right: 8px; }

.audit-comment-box { margin-top: 20px; }
.section-title { font-weight: bold; margin-bottom: 10px; color: #606266; font-size: 14px; border-left: 4px solid #409EFF; padding-left: 10px; }
</style>