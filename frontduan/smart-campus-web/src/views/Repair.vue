<template>
  <div class="repair-layout">
    
    <div class="sliding-panel" :class="{ 'is-open': formVisible }">
      <div class="panel-content">
        <div class="panel-header">
          <span class="emoji">🛠️</span>
          <span class="title">发起新报修</span>
        </div>
        
        <div class="panel-form-body">
          <el-form ref="repairFormRef" :model="repairForm" :rules="rules" label-position="top">
            <el-form-item label="报修标题" prop="title">
              <el-input v-model="repairForm.title" placeholder="一句话描述问题，如：空调不制冷" />
            </el-form-item>

            <el-form-item label="宿舍楼栋" prop="building">
              <el-cascader
                v-model="repairForm.building"
                :options="dormOptions"
                placeholder="请选择校区与楼栋"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="详细房间号" prop="room">
              <el-input v-model="repairForm.room" placeholder="如：401" >
                <template #append>室</template>
              </el-input>
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
          </el-form>
        </div>

        <div class="panel-footer">
          <el-button @click="formVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitRepair">提交工单</el-button>
        </div>
      </div>
    </div>

    <div class="toggle-handle" @click="formVisible = !formVisible">
      <el-icon v-if="!formVisible"><CaretRight /></el-icon>
      <el-icon v-else><CaretLeft /></el-icon>
      <span class="vertical-text">{{ formVisible ? '收起表单' : '发起报修' }}</span>
    </div>

    <div class="main-list">
      <el-card shadow="never" class="list-card">
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <span class="emoji">📋</span>
              <span class="title">我的报修记录</span>
            </div>
            <div class="header-right">
              <el-button type="primary" icon="Plus" @click="formVisible = true" v-if="!formVisible">发起报修</el-button>
              <el-button type="success" plain icon="Refresh" @click="handleRefresh">刷新进度</el-button>
            </div>
          </div>
        </template>

        <el-table :data="myOrders" style="width: 100%" v-loading="loading" stripe border>
          <el-table-column prop="title" label="报修标题" min-width="150" show-overflow-tooltip>
            <template #default="scope"><span style="font-weight: bold;">{{ scope.row.title }}</span></template>
          </el-table-column>
          <el-table-column prop="dormLocation" label="宿舍位置" width="180" align="center">
            <template #default="scope"><el-tag type="info" effect="plain">{{ scope.row.dormLocation }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="createTime" label="提交时间" width="170" align="center" />
          <el-table-column label="当前状态" width="110" align="center">
            <template #default="scope">
              <el-tag :type="getStatusConfig(scope.row.status).type" effect="light" round>
                {{ getStatusConfig(scope.row.status).text }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作台" width="160" align="center">
            <template #default="scope">
              <el-button v-if="scope.row.status === 0" type="danger" size="small" plain @click="handleCancel(scope.row.id)">撤销</el-button>
              <el-button v-if="scope.row.status === 2" type="success" size="small" @click="openEvaluateDialog(scope.row)">去评价</el-button>
              <span v-if="scope.row.status === 1" style="color: #409EFF; font-size: 13px;">👨‍🔧维修中</span>
              <span v-if="scope.row.status === 3" style="color: #67C23A; font-size: 13px;">🎉已完成</span>
              <span v-if="scope.row.status === -1" style="color: #999; font-size: 13px;">已撤销</span>
            </template>
          </el-table-column>

          <template #empty><el-empty description="暂无报修记录，宿舍设施一切正常！" /></template>
        </el-table>
      </el-card>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const repairFormRef = ref(null)
const myOrders = ref([])
const formVisible = ref(false)

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const currentUserId = userInfo.id || 1001
const uploadHeaders = {
  token: localStorage.getItem('Authorization') 
}

// 【新增】定义全校标准的校区与宿舍楼字典
const dormOptions = [
  {
    value: '东校区',
    label: '东校区',
    children: [
      { value: '1舍', label: '1舍' },
      { value: '2舍', label: '2舍' },
      { value: '3舍', label: '3舍' }
    ]
  },
  {
    value: '南校区',
    label: '南校区',
    children: [
      { value: '1舍', label: '1舍' },
      { value: '2舍', label: '2舍' },
      { value: '3舍', label: '3舍' },
      { value: '4舍', label: '4舍' }
    ]
  },
  {
    value: '西校区',
    label: '西校区',
    children: [
      { value: '1舍', label: '1舍' },
      { value: '2舍', label: '2舍' }
    ]
  }
]

// 【修改】拆分表单字段，将 dormLocation 拆为 building 和 room
const repairForm = reactive({
  studentId: currentUserId,
  title: '',
  building: [], // 接收数组格式，如：['南校区', '3舍']
  room: '',     // 房间号
  description: ''
})

// 【修改】对应的必填校验规则更新
const rules = {
  title: [{ required: true, message: '请输入报修标题', trigger: 'blur' }],
  building: [{ required: true, message: '请选择校区和楼栋', trigger: 'change' }],
  room: [{ required: true, message: '请输入详细房间号', trigger: 'blur' }],
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
        
        // 【核心】在这里进行数据的重组与拼接
        // 将前端两个组件的值拼成后端需要的标准字符串，例如："南校区-3舍-401室"
        const standardLocation = `${repairForm.building[0]}-${repairForm.building[1]}-${repairForm.room}室`

        const submitData = {
          studentId: repairForm.studentId,
          title: repairForm.title,
          dormLocation: standardLocation, // 把拼接好的字符串传给后端
          description: repairForm.description,
          images: JSON.stringify(imagesToSubmit) 
        }
        
        await request.post('/repair/submit', submitData)
        ElMessage.success('报单提交成功，后勤大叔正火速赶来！')
        
        repairFormRef.value.resetFields()
        fileList.value = [] 
        formVisible.value = false 
        
        fetchMyOrders()
      } catch (error) {
        console.error('提交失败', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确定要撤销这个报修单吗？', '提示', { type: 'warning' })
    await request.post(`/repair/cancel/${id}`)
    ElMessage.success('撤销成功！')
    fetchMyOrders() 
  } catch (error) {
    console.log('取消撤销操作')
  }
}

const evaluateVisible = ref(false)
const evaluateForm = reactive({
  orderId: null,
  score: 5,
  comment: ''
})

const openEvaluateDialog = (row) => {
  evaluateForm.orderId = row.id
  evaluateForm.score = 5
  evaluateForm.comment = ''
  evaluateVisible.value = true
}

const submitEvaluate = async () => {
  if (!evaluateForm.score) {
    ElMessage.warning('请至少打一颗星吧！')
    return
  }
  try {
    await request.post('/repair/evaluate', evaluateForm)
    ElMessage.success('评价成功，感谢反馈！')
    evaluateVisible.value = false
    fetchMyOrders()
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

const handleRefresh = async () => {
  await fetchMyOrders()
  ElMessage.success('状态刷新成功！')
}

onMounted(() => {
  fetchMyOrders()
})
</script>

<style scoped>
.repair-layout {
  display: flex;
  align-items: stretch;
  gap: 12px;
  min-height: calc(100vh - 120px); 
}

.sliding-panel {
  width: 0;
  opacity: 0;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  flex-shrink: 0;
}

.sliding-panel.is-open {
  width: 360px;
  opacity: 1;
}

.panel-content {
  width: 360px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  height: 100%;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.panel-header {
  padding: 20px;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #f0f0f0;
}
.panel-header .emoji { font-size: 20px; margin-right: 8px; }

.panel-form-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.panel-footer {
  padding: 15px 20px;
  border-top: 1px solid #f0f0f0;
  text-align: right;
  background: #fafbfc;
  border-radius: 0 0 12px 12px;
}

.toggle-handle {
  width: 28px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #909399;
  transition: all 0.3s;
  flex-shrink: 0;
}

.toggle-handle:hover {
  background: #f4f4f5;
  color: #409EFF;
}

.vertical-text {
  writing-mode: vertical-rl;
  letter-spacing: 5px;
  font-size: 13px;
  margin-top: 15px;
  font-weight: bold;
}

.main-list {
  flex: 1;
  min-width: 0; 
}

.list-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  font-weight: bold;
}

.header-left { display: flex; align-items: center; }
.header-left .emoji { font-size: 20px; margin-right: 8px; }
.header-right { display: flex; gap: 10px; }
.upload-tip { font-size: 12px; color: #999; margin-top: 5px; line-height: 1.2; }
</style>