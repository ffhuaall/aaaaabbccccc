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
                :headers="uploadHeaders"  
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
            
            <el-table-column label="当前状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="getStatusConfig(scope.row.status).type" effect="light" round>
                  {{ getStatusConfig(scope.row.status).text }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="操作台" width="160" align="center">
              <template #default="scope">
                <el-button v-if="scope.row.status === 0" type="danger" size="small" plain @click="handleCancel(scope.row.id)">撤销报修</el-button>
                
                <el-button v-if="scope.row.status === 2" type="success" size="small" @click="openEvaluateDialog(scope.row)">去评价</el-button>
                
                <span v-if="scope.row.status === 1" style="color: #409EFF; font-size: 13px;">👨‍🔧师傅狂奔中...</span>
                <span v-if="scope.row.status === 3" style="color: #67C23A; font-size: 13px;">🎉服务已完成</span>
                <span v-if="scope.row.status === -1" style="color: #999; font-size: 13px;">已撤销</span>
              </template>
            </el-table-column>

            <template #empty><el-empty description="暂无报修记录，宿舍设施一切正常！" /></template>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>

  <el-dialog v-model="evaluateVisible" title="服务质量评价" width="400px" center append-to-body>
    <div style="text-align: center;">
      <h3 style="margin-top: 0; color: #333;">师傅的服务态度和技术如何？</h3>
      <el-rate v-model="evaluateForm.score" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" show-text />
      <el-input 
        v-model="evaluateForm.comment" 
        type="textarea" 
        :rows="3" 
        placeholder="写点评价吧，比如：师傅手艺真不错，空调冻得我直打哆嗦！" 
        style="margin-top: 20px;" 
      />
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="evaluateVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEvaluate">提交评价</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
// 【重要修复】引入了 ElMessageBox，否则点击撤销会报错
import { ElMessage, ElMessageBox } from 'element-plus' 

const loading = ref(false)
const submitting = ref(false)
const repairFormRef = ref(null)
const myOrders = ref([])

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const currentUserId = userInfo.id || 1001
const uploadHeaders = {
  token: localStorage.getItem('Authorization') 
}

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

const fileList = ref([])

const handleUploadSuccess = (response, uploadFile) => {
  if (response.code === 200) {
    uploadFile.url = response.data 
    ElMessage.success('图片上传成功！')
  } else {
    ElMessage.error('上传失败: ' + response.msg)
  }
}

// 【新增 -1 已撤销状态】
const getStatusConfig = (status) => {
  const configMap = {
    '-1': { text: '已撤销', type: 'info' },
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
        const imagesToSubmit = fileList.value.map(file => file.url || file.response.data)
        const submitData = {
          ...repairForm,
          images: JSON.stringify(imagesToSubmit) 
        }
        await request.post('/repair/submit', submitData)
        ElMessage.success('报单提交成功，后勤大叔正火速赶来！')
        repairFormRef.value.resetFields()
        fileList.value = [] 
        fetchMyOrders()
      } catch (error) {
        console.error('提交失败', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 【新增】撤销报修单逻辑
const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确定要撤销这个报修单吗？撤销后后勤将不再处理。', '提示', { type: 'warning' })
    await request.post(`/repair/cancel/${id}`)
    ElMessage.success('撤销成功！')
    fetchMyOrders() // 刷新列表
  } catch (error) {
    console.log('取消撤销操作')
  }
}

// ================== 【新增】评价功能相关逻辑 ==================
const evaluateVisible = ref(false)
const evaluateForm = reactive({
  orderId: null,
  score: 5, // 默认 5 星
  comment: ''
})

// 打开评价弹窗
const openEvaluateDialog = (row) => {
  evaluateForm.orderId = row.id
  evaluateForm.score = 5
  evaluateForm.comment = ''
  evaluateVisible.value = true
}

// 提交评价
const submitEvaluate = async () => {
  if (!evaluateForm.score) {
    ElMessage.warning('请至少给师傅打一颗星吧！')
    return
  }
  try {
    await request.post('/repair/evaluate', evaluateForm)
    ElMessage.success('评价成功，感谢您的反馈！')
    evaluateVisible.value = false
    fetchMyOrders() // 刷新列表更新状态为已完成
  } catch (error) {
    console.error('评价提交失败', error)
  }
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