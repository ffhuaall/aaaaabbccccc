<template>
  <div class="admin-repair-page">
    <el-card shadow="never" class="list-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">工单处理中心</span>
          </div>
          <div class="header-right">
            <el-cascader
              v-model="searchLocation"
              :options="dormOptions"
              :props="{ checkStrictly: true }"
              placeholder="按条件筛选"
              clearable
              style="width: 250px; margin-right: 15px;"
            />
            <el-button type="primary" plain icon="Refresh" @click="fetchAllOrders">刷新数据</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="custom-tabs">
        <el-tab-pane name="pool">
          <template #label>
            <span class="tab-label">📥 待接单 <el-badge :value="filteredPool.length" type="primary" class="tab-badge" v-if="filteredPool.length > 0"/></span>
          </template>
          <el-table :data="filteredPool" style="width: 100%" v-loading="loading" stripe>
            <el-table-column prop="dormLocation" label="报修位置" width="180">
              <template #default="scope"><el-tag type="info" effect="dark">{{ scope.row.dormLocation }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="title" label="故障简述" min-width="200" show-overflow-tooltip />
            <el-table-column prop="createTime" label="提交时间" width="180" align="center" />
            <el-table-column label="操作" width="120" align="center">
              <template #default="scope">
                <el-button type="primary" size="small" @click="openDetail(scope.row)">查看详情</el-button>
              </template>
            </el-table-column>
            <template #empty><el-empty description="当前暂无该区域的待处理工单" /></template>
          </el-table>
        </el-tab-pane>

        <el-tab-pane name="myTodo">
          <template #label>
            <span class="tab-label">🛠️ 待处理 <el-badge :value="filteredTodo.length" type="warning" class="tab-badge" v-if="filteredTodo.length > 0"/></span>
          </template>
          <el-table :data="filteredTodo" style="width: 100%" v-loading="loading" stripe>
            <el-table-column prop="dormLocation" label="报修位置" width="180">
              <template #default="scope"><el-tag type="warning" effect="dark">{{ scope.row.dormLocation }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="title" label="故障简述" min-width="200" show-overflow-tooltip />
            <el-table-column label="当前状态" width="120" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'primary' : 'warning'" effect="light" round>
                  {{ scope.row.status === 1 ? '维修处理中' : '待学生评价' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default="scope">
                <el-button type="success" size="small" plain @click="openDetail(scope.row)">跟进处理</el-button>
              </template>
            </el-table-column>
            <template #empty><el-empty description="您的任务列表为空" /></template>
          </el-table>
        </el-tab-pane>

        <el-tab-pane name="myDone">
          <template #label>
            <span class="tab-label">✅ 已完成</span>
          </template>
          
          <div style="margin-bottom: 15px; display: flex; justify-content: space-between; align-items: center;">
            <span style="font-size: 13px; color: #999;">在此查看您已彻底完成的工单履历</span>
            <el-button type="success" icon="Download" @click="exportToCSV" :disabled="filteredDone.length === 0">
              导出历史单据报表 (CSV)
            </el-button>
          </div>

          <el-table :data="filteredDone" style="width: 100%" v-loading="loading" stripe>
            <el-table-column prop="dormLocation" label="报修位置" width="180">
              <template #default="scope"><el-tag type="info" effect="plain">{{ scope.row.dormLocation }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="title" label="故障简述" min-width="200" show-overflow-tooltip />
            <el-table-column label="工单状态" width="120" align="center">
              <template #default><el-tag type="info" effect="light" round>已闭环</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default="scope">
                <el-button type="info" size="small" plain @click="openDetail(scope.row)">查看归档</el-button>
              </template>
            </el-table-column>
            <template #empty><el-empty description="暂无该区域的已完成工单记录" /></template>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="detailVisible" title="工单详细档案" width="600px" append-to-body>
      <div v-if="currentOrder">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="工单编号">NO.{{ currentOrder.id }}</el-descriptions-item>
          <el-descriptions-item label="报修位置">
            <el-tag type="info">{{ currentOrder.dormLocation }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="故障标题"><b>{{ currentOrder.title }}</b></el-descriptions-item>
          <el-descriptions-item label="详细描述">{{ currentOrder.description }}</el-descriptions-item>
          
          <el-descriptions-item label="现场照片">
            <div v-if="safeParseImages(currentOrder.images).length > 0" class="image-gallery">
              <el-image 
                v-for="(url, index) in safeParseImages(currentOrder.images)" :key="index"
                style="width: 100px; height: 100px; border-radius: 8px; margin-right: 10px;"
                :src="url" :preview-src-list="safeParseImages(currentOrder.images)" preview-teleported fit="cover"
              />
            </div>
            <span v-else style="color: #999;">该工单未提供照片</span>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="currentOrder.status === 3 && evaluationData" class="evaluation-box">
          <h4 style="margin-top: 0; color: #67C23A; border-bottom: 1px solid #eee; padding-bottom: 10px;">🌟 服务评价反馈</h4>
          <div style="display: flex; align-items: center; margin-bottom: 10px;">
            <span style="margin-right: 15px; color: #666;">服务打分：</span>
            <el-rate v-model="evaluationData.score" disabled show-score text-color="#ff9900" />
          </div>
          <div style="color: #555; background: #f8f9fa; padding: 10px; border-radius: 6px;">
            "{{ evaluationData.comment || '无附加评语' }}"
          </div>
        </div>
      </div>

      <template #footer>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-size: 13px; color: #999;">请仔细核对故障信息，准备相应工具</span>
          <div>
            <el-button @click="detailVisible = false">关闭</el-button>
            <el-button v-if="currentOrder?.status === 0" type="primary" @click="handleTakeOrder">确认接单</el-button>
            <el-button v-if="currentOrder?.status === 1" type="success" @click="handleFinishOrder">登记完工</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const allOrders = ref([])
const activeTab = ref('pool') 

// 【重构】使用数组来接收级联选择器的值
const searchLocation = ref([])   

const detailVisible = ref(false)
const currentOrder = ref(null)
const evaluationData = ref(null)

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const currentWorkerId = userInfo.id || 2001

// 引入与学生端一致的校区/楼栋字典
const dormOptions = [
  {
    value: '东校区', label: '东校区',
    children: [{ value: '1舍', label: '1舍' }, { value: '2舍', label: '2舍' }, { value: '3舍', label: '3舍' }]
  },
  {
    value: '南校区', label: '南校区',
    children: [{ value: '1舍', label: '1舍' }, { value: '2舍', label: '2舍' }, { value: '3舍', label: '3舍' }, { value: '4舍', label: '4舍' }]
  },
  {
    value: '西校区', label: '西校区',
    children: [{ value: '1舍', label: '1舍' }, { value: '2舍', label: '2舍' }]
  }
]

const safeParseImages = (imgStr) => {
  if (!imgStr) return []
  try { return JSON.parse(imgStr) } catch (e) { return [imgStr] }
}

const fetchAllOrders = async () => {
  loading.value = true
  try {
    const res = await request.get('/repair/list')
    const data = res || []
    allOrders.value = data.sort((a, b) => b.id - a.id)
  } catch (error) {
    console.error('获取列表失败', error)
  } finally {
    loading.value = false
  }
}

// ================== 核心：结构化区域过滤逻辑 ==================
const matchLocation = (order) => {
  // 如果没有选择任何过滤条件，默认显示全部
  if (!searchLocation.value || searchLocation.value.length === 0) return true
  if (!order.dormLocation) return false

  const campus = searchLocation.value[0]
  const building = searchLocation.value[1] 

  // 如果师傅只选了校区（如：南校区），就匹配所有以"南校区"开头的单子
  // 如果师傅选了楼栋（如：南校区 -> 3舍），就精确匹配"南校区-3舍"
  if (building) {
    return order.dormLocation.startsWith(`${campus}-${building}`)
  } else {
    return order.dormLocation.startsWith(`${campus}`)
  }
}

const filteredPool = computed(() => {
  return allOrders.value.filter(item => item.status === 0 && matchLocation(item))
})

const filteredTodo = computed(() => {
  return allOrders.value.filter(item => (item.status === 1 || item.status === 2) && item.workerId === currentWorkerId && matchLocation(item))
})

const filteredDone = computed(() => {
  return allOrders.value.filter(item => item.status === 3 && item.workerId === currentWorkerId && matchLocation(item))
})
// ===================================================================

// 【新增】纯前端 CSV 导出功能
const exportToCSV = () => {
  if (filteredDone.value.length === 0) return
  
  // 1. 构建 CSV 表头
  let csvContent = "工单编号,报修位置,故障简述,提交时间,完成状态\n"
  
  // 2. 遍历数据并转为 CSV 行格式
  filteredDone.value.forEach(order => {
    // 遇到描述里有逗号的，需要用双引号包起来，防止 CSV 错位
    const safeTitle = `"${order.title.replace(/"/g, '""')}"`
    const row = [
      order.id,
      order.dormLocation,
      safeTitle,
      order.createTime,
      '已闭环'
    ].join(',')
    csvContent += row + "\n"
  })
  
  // 3. 生成 Blob 对象并利用浏览器下载机制触发下载 (\uFEFF 解决 Excel 中文乱码)
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement("a")
  link.setAttribute("href", url)
  // 文件名带上当前时间戳
  link.setAttribute("download", `已完成维修工单_${new Date().getTime()}.csv`)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  ElMessage.success('报表导出成功！')
}

const openDetail = async (row) => {
  currentOrder.value = row
  evaluationData.value = null 
  detailVisible.value = true
  if (row.status === 3) {
    try {
      const res = await request.get(`/repair/evaluation/${row.id}`)
      evaluationData.value = res
    } catch (e) { console.log('暂无评价') }
  }
}

const handleTakeOrder = async () => {
  try {
    await ElMessageBox.confirm('确认接手此工单吗？', '接单确认', { type: 'info' })
    await request.post(`/repair/take/${currentOrder.value.id}?workerId=${currentWorkerId}`)
    ElMessage.success('接单成功，已移入"我的任务"。')
    detailVisible.value = false
    activeTab.value = 'myTodo' 
    fetchAllOrders()
  } catch (error) {}
}

const handleFinishOrder = async () => {
  try {
    await ElMessageBox.confirm('确认该工单已维修完毕？', '操作提示', { type: 'info' })
    await request.post(`/repair/finish/${currentOrder.value.id}`)
    ElMessage.success('已登记完工，等待报修人评价。')
    detailVisible.value = false
    fetchAllOrders()
  } catch (error) {}
}

onMounted(() => { fetchAllOrders() })
</script>

<style scoped>
.admin-repair-page { padding-bottom: 20px; }
.list-card { border-radius: 12px; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.05); min-height: 600px; }
.card-header { display: flex; align-items: center; justify-content: space-between; font-size: 18px; font-weight: bold; }
.header-left { display: flex; align-items: center; }
.header-left .emoji { font-size: 24px; margin-right: 10px; }
.header-right { display: flex; align-items: center; }

:deep(.el-tabs__nav-wrap::after) { height: 1px; background-color: #f0f0f0; }
:deep(.el-tabs__item) { font-size: 15px; padding: 0 25px; height: 50px; line-height: 50px; color: #666; }
:deep(.el-tabs__item.is-active) { font-weight: bold; color: #409EFF; }
.tab-label { display: flex; align-items: center; gap: 8px; }
.tab-badge { margin-top: -2px; }

.image-gallery { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 5px; }
.evaluation-box { margin-top: 20px; padding: 15px; border: 1px solid #ebeef5; border-radius: 8px; background-color: #fafafa; }
</style>