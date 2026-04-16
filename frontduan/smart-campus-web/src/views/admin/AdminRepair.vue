<template>
  <div class="admin-repair-page">
    <el-card shadow="never" class="list-card">
      <template #header>
        <div class="card-header">
          <span class="emoji">👷‍♂️</span>
          <span class="title">后勤工单调度中心</span>
        </div>
      </template>

      <el-table :data="allOrders" style="width: 100%" v-loading="loading" stripe>
        <el-table-column prop="id" label="工单号" width="80" align="center" />
        <el-table-column prop="dormLocation" label="报修位置" width="150" align="center">
          <template #default="scope"><el-tag type="danger" effect="dark">{{ scope.row.dormLocation }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="title" label="故障简述" min-width="200" show-overflow-tooltip />
        
        <el-table-column label="当前状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getStatusConfig(scope.row.status).type" effect="light" round>
              {{ getStatusConfig(scope.row.status).text }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" plain @click="openDetail(scope.row)">
              工单详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="工单详细档案" width="600px" append-to-body>
      <div v-if="currentOrder">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="工单编号">NO.{{ currentOrder.id }}</el-descriptions-item>
          <el-descriptions-item label="报修位置">
            <el-tag type="danger">{{ currentOrder.dormLocation }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="故障标题"><b>{{ currentOrder.title }}</b></el-descriptions-item>
          <el-descriptions-item label="详细描述">{{ currentOrder.description }}</el-descriptions-item>
          
          <el-descriptions-item label="现场照片">
            <div v-if="safeParseImages(currentOrder.images).length > 0" class="image-gallery">
              <el-image 
                v-for="(url, index) in safeParseImages(currentOrder.images)" 
                :key="index"
                style="width: 100px; height: 100px; border-radius: 8px; margin-right: 10px;"
                :src="url" 
                :preview-src-list="safeParseImages(currentOrder.images)"
                preview-teleported
                fit="cover"
              />
            </div>
            <span v-else style="color: #999;">该学生未上传照片</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-size: 13px; color: #999;">请仔细核对故障信息带对工具</span>
          <div>
            <el-button @click="detailVisible = false">稍后处理</el-button>
            <el-button v-if="currentOrder?.status === 0" type="primary" @click="handleAction('take')">确认接单</el-button>
            <el-button v-if="currentOrder?.status === 1" type="success" @click="handleAction('finish')">登记修完</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const allOrders = ref([])

// 弹窗状态
const detailVisible = ref(false)
const currentOrder = ref(null)

const getStatusConfig = (status) => {
  const configMap = {
    0: { text: '待接单', type: 'warning' },
    1: { text: '维修中', type: 'primary' },
    2: { text: '待评价', type: 'success' },
    3: { text: '已闭环', type: 'info' }
  }
  return configMap[status] || { text: '未知状态', type: 'info' }
}

const fetchAllOrders = async () => {
  loading.value = true
  try {
    const res = await request.get('/repair/list')
    allOrders.value = res || []
    allOrders.value.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  } catch (error) {
    console.error('获取列表失败', error)
  } finally {
    loading.value = false
  }
}

// 【关键修复点】健壮的图片解析器（防止数据库存错格式导致页面崩溃）
const safeParseImages = (imgStr) => {
  if (!imgStr) return []
  try {
    // 正常情况：解析 JSON 数组 ["http://..."]
    return JSON.parse(imgStr)
  } catch (e) {
    // 容错情况：如果你手动存成了一个单条 URL 字符串，包装成数组返回
    return [imgStr]
  }
}

// 打开详情弹窗
const openDetail = (row) => {
  currentOrder.value = row
  detailVisible.value = true
}

// 统一处理流转操作
const handleAction = async (action) => {
  const actionText = action === 'take' ? '确认接手这个工单吗？' : '确认该故障已修复完毕吗？'
  try {
    await ElMessageBox.confirm(actionText, '操作提示', { type: 'warning' })
    await request.post(`/repair/${action}/${currentOrder.value.id}`)
    ElMessage.success('操作成功！流转状态已更新。')
    detailVisible.value = false // 关掉弹窗
    fetchAllOrders() // 刷新列表
  } catch (error) {
    console.log('操作取消或失败')
  }
}

onMounted(() => {
  fetchAllOrders()
})
</script>

<style scoped>
.admin-repair-page { padding-bottom: 20px; }
.list-card { border-radius: 12px; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.05); min-height: 600px; }
.card-header { display: flex; align-items: center; font-size: 18px; font-weight: bold; }
.card-header .emoji { font-size: 24px; margin-right: 10px; }
.image-gallery { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 5px; }
</style>