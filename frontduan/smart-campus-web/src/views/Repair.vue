<template>
  <div class="repair-page">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="never" class="custom-card form-card">
          <template #header>
            <div class="card-header">
              <span class="emoji">🛠️</span>
              <span class="title">发起报修</span>
            </div>
          </template>
          
          <el-form ref="repairFormRef" :model="repairForm" :rules="rules" label-position="top">
            <el-form-item label="报修标题" prop="title">
              <el-input v-model="repairForm.title" placeholder="一句话描述问题，如：空调不制冷" />
            </el-form-item>
            
            <el-form-item label="宿舍位置" prop="dormLocation">
              <el-input v-model="repairForm.dormLocation" placeholder="如：南区 2 号楼 401" />
            </el-form-item>
            
            <el-form-item label="详细情况" prop="description">
              <el-input v-model="repairForm.description" type="textarea" :rows="4" placeholder="请详细描述故障现象..." />
            </el-form-item>

            <el-form-item label="现场照片 (选填)">
              <el-upload
                action="http://localhost:8080/file/upload"
                list-type="picture-card"
                v-model:file-list="fileList"
                :on-success="handleUploadSuccess"
                :limit="3"
                name="file"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">最多上传 3 张照片，辅助定位问题</div>
            </el-form-item>

            <el-button type="primary" class="submit-btn" :loading="submitting" @click="submitRepair">
              提交工单
            </el-button>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="never" class="custom-card list-card">
          <template #header>
            <div class="card-header">
              <span class="emoji">📋</span>
              <span class="title">我的报修记录</span>
              <el-button type="primary" link icon="Refresh" @click="fetchMyOrders" style="margin-left: auto;">刷新进度</el-button>
            </div>
          </template>

          <el-table :data="myOrders" style="width: 100%" v-loading="loading" stripe>
            <el-table-column prop="title" label="报修标题" min-width="150" show-overflow-tooltip>
              <template #default="scope"><span style="font-weight: bold;">{{ scope.row.title }}</span></template>
            </el-table-column>
            <el-table-column prop="dormLocation" label="宿舍位置" width="120" align="center">
              <template #default="scope"><el-tag type="info" effect="plain">{{ scope.row.dormLocation }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="createTime" label="提交时间" width="160" align="center" />
            <el-table-column label="当前状态" width="120" align="center">
              <template #default="scope">
                <el-tag :type="getStatusConfig(scope.row.status).type" effect="light" round>
                  {{ getStatusConfig(scope.row.status).text }}
                </el-tag>
              </template>
            </el-table-column>
            <template #empty><el-empty description="暂无报修记录，宿舍设施一切正常！" /></template>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const repairFormRef = ref(null)
const myOrders = ref([])

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const currentUserId = userInfo.id || 1001

const repairForm = reactive({
  studentId: currentUserId,
  title: '',
  dormLocation: '',
  description: ''
})

const rules = {
  title: [{ required: true, message: '请输入报修标题', trigger: 'blur' }],
  dormLocation: [{ required: true, message: '请输入宿舍位置', trigger: 'blur' }],
  description: [{ required: true, message: '请简要描述问题情况', trigger: 'blur' }]
}

// 【修复点】维护上传组件的视觉列表
const fileList = ref([])

// 【修复点】图片上传成功后的精准处理
const handleUploadSuccess = (response, uploadFile) => {
  if (response.code === 200) {
    // 关键：把后端返回的真实 URL 赋给组件，让它显示缩略图
    uploadFile.url = response.data 
    ElMessage.success('图片上传成功！')
  } else {
    ElMessage.error('上传失败: ' + response.msg)
  }
}

const getStatusConfig = (status) => {
  const configMap = {
    0: { text: '待接单', type: 'warning' },
    1: { text: '维修中', type: 'primary' },
    2: { text: '待评价', type: 'success' },
    3: { text: '已完成', type: 'info' }
  }
  return configMap[status] || { text: '未知状态', type: 'info' }
}

const submitRepair = () => {
  repairFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // 【修复点】提取出所有成功上传的图片 URL
        const imagesToSubmit = fileList.value.map(file => file.url || file.response.data)
        
        const submitData = {
          ...repairForm,
          images: JSON.stringify(imagesToSubmit) // 严谨地转成 JSON 字符串存入数据库
        }
        
        await request.post('/repair/submit', submitData)
        ElMessage.success('报单提交成功，后勤大叔正火速赶来！')
        
        repairFormRef.value.resetFields()
        fileList.value = [] // 提交后清空图片列表
        fetchMyOrders()
      } catch (error) {
        console.error('提交失败', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

const fetchMyOrders = async () => {
  loading.value = true
  try {
    const res = await request.get('/repair/list')
    myOrders.value = (res || []).filter(item => item.studentId === currentUserId)
    myOrders.value.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  } catch (error) {
    console.error('获取列表失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchMyOrders()
})
</script>

<style scoped>
.repair-page {
  padding-bottom: 20px;
}

.custom-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  min-height: 500px;
}

.card-header {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}

.card-header .emoji {
  font-size: 20px;
  margin-right: 8px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
  line-height: 1.2;
}

.submit-btn {
  width: 100%;
  margin-top: 10px;
  border-radius: 8px;
  padding: 12px;
  font-size: 15px;
  letter-spacing: 1px;
}
</style>