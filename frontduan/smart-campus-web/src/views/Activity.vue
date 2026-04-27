<template>
  <div class="activity-page">
    <el-card shadow="never" class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="emoji">🎉</span>
            <span class="title">校园活动中心</span>
          </div>
          <div class="header-right" v-if="activeTab === 'all'">
            <el-select v-model="filter.category" placeholder="活动类别" clearable style="width: 130px; margin-right: 12px;">
              <el-option label="学术讲座" value="学术讲座" />
              <el-option label="文体演艺" value="文体演艺" />
              <el-option label="志愿服务" value="志愿服务" />
              <el-option label="社团招新" value="社团招新" />
            </el-select>
            <el-input v-model="filter.keyword" placeholder="搜索活动名称..." clearable style="width: 220px;" prefix-icon="Search" />
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="custom-tabs">
        <el-tab-pane name="all" label="🎪 活动大厅" />
        <el-tab-pane name="my" label="📅 我的日程" />
      </el-tabs>

      <div v-show="activeTab === 'all'">
        <div v-if="recommendList.length > 0 && !filter.keyword && !filter.category" class="recommend-section">
          <div class="section-header">
            <span class="emoji">✨</span>
            <h3 class="title">猜你喜欢</h3>
            <span class="subtitle">基于协同过滤算法为您精准推荐</span>
          </div>
          <div class="items-grid recommend-grid">
            <el-card v-for="item in recommendList" :key="item.id" class="activity-card recommend-card" @click="showDetails(item)">
              <div class="tag-box"><el-tag effect="dark" type="warning" round size="small">为你推荐</el-tag></div>
              <div class="card-content">
                <h4 class="act-title">{{ item.title }}</h4>
                <p class="act-desc">{{ item.content }}</p>
                <div class="act-meta">
                  <span class="time"><el-icon><Clock /></el-icon> {{ item.startTime?.substring(5, 16) }}</span>
                  <span class="loc"><el-icon><Location /></el-icon> {{ item.location || '线上活动' }}</span>
                </div>
              </div>
            </el-card>
          </div>
          <el-divider border-style="dashed" />
        </div>

        <div class="section-header">
          <span class="emoji">🔥</span>
          <h3 class="title">最新活动</h3>
        </div>
        <div v-loading="loading" class="items-grid">
          <el-empty v-if="filteredList.length === 0" description="暂无相关活动" style="grid-column: 1 / -1;" />
          
          <el-card v-for="item in filteredList" :key="item.id" class="activity-card" @click="showDetails(item)">
            <div :class="['poster-box', !item.coverImage ? getCategoryClass(item.category) : '']">
              <el-image v-if="item.coverImage" :src="item.coverImage" fit="cover" class="poster-img" />
              <div v-else class="poster-text">{{ item.category || '校园活动' }}</div>
              <div class="status-badge">
                <el-tag :type="getStatusInfo(item).type" effect="dark" size="small">{{ getStatusInfo(item).text }}</el-tag>
              </div>
            </div>

            <div class="act-info">
              <h4 class="act-title" :title="item.title">{{ item.title }}</h4>
              <div class="act-meta-list">
                <p><el-icon><Clock /></el-icon> {{ item.startTime }}</p>
                <p><el-icon><Location /></el-icon> {{ item.location }}</p>
              </div>
              
              <div class="quota-box">
                <div class="quota-text">
                  <span>报名进度</span>
                  <span :class="{'full-text': item.currentEnrollment >= item.capacity}">
                    {{ item.currentEnrollment || 0 }} / {{ item.capacity || '不限' }}
                  </span>
                </div>
                <el-progress 
                  :percentage="getPercentage(item)" 
                  :status="item.currentEnrollment >= item.capacity ? 'exception' : ''" 
                  :show-text="false" 
                  stroke-width="6" 
                />
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <div v-show="activeTab === 'my'" v-loading="loading">
        <el-empty v-if="myActivities.length === 0" description="您还没有报名任何活动哦，快去大厅看看吧！" />
        <div class="items-grid">
          <el-card v-for="item in myActivities" :key="item.id" class="activity-card my-card">
            <div class="my-card-header">
              <span class="date">{{ item.startTime?.substring(5, 10) }}</span>
              <el-tag size="small" :type="getStatusInfo(item).type">{{ getStatusInfo(item).text }}</el-tag>
            </div>
            <h4 class="act-title">{{ item.title }}</h4>
            <p class="loc"><el-icon><Location /></el-icon> {{ item.location }}</p>
            <div class="my-card-footer">
              <el-button type="primary" plain size="small" @click="showDetails(item)">查看详情</el-button>
              <el-button 
                v-if="item.status === 1" 
                type="danger" 
                link 
                size="small" 
                @click="cancelRegistration(item.id)"
              >取消报名</el-button>
            </div>
          </el-card>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="活动详情" width="550px" destroy-on-close :lock-scroll="false">
      <div v-if="selectedItem" class="detail-container">
        <el-image v-if="selectedItem.coverImage" :src="selectedItem.coverImage" fit="contain" class="detail-poster" />
        
        <h2 class="d-title">{{ selectedItem.title }}</h2>
        <div class="d-tags">
          <el-tag size="small">{{ selectedItem.category || '综合活动' }}</el-tag>
          <el-tag size="small" :type="getStatusInfo(selectedItem).type" effect="dark">{{ getStatusInfo(selectedItem).text }}</el-tag>
        </div>
        
        <el-descriptions :column="1" border size="small" class="d-desc">
          <el-descriptions-item label="活动时间">{{ selectedItem.startTime }} 至 {{ selectedItem.endTime }}</el-descriptions-item>
          <el-descriptions-item label="活动地点">{{ selectedItem.location }}</el-descriptions-item>
          <el-descriptions-item label="活动内容">
            <div class="content-text">{{ selectedItem.content }}</div>
          </el-descriptions-item>
        </el-descriptions>

        <div class="enroll-status-box" v-if="selectedItem.status === 1">
          <div style="margin-bottom: 5px;"><strong>报名情况：</strong> 已报 {{ selectedItem.currentEnrollment || 0 }} 人 / 总名额 {{ selectedItem.capacity || '不限' }}</div>
          <el-progress :percentage="getPercentage(selectedItem)" :status="selectedItem.currentEnrollment >= selectedItem.capacity ? 'exception' : ''" />
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button 
          v-if="!isRegistered(selectedItem?.id) && selectedItem?.status === 1" 
          type="primary" 
          :disabled="selectedItem?.currentEnrollment >= selectedItem?.capacity"
          @click="handleEnroll"
        >
          {{ selectedItem?.currentEnrollment >= selectedItem?.capacity ? '名额已满' : '立即报名' }}
        </el-button>
        <el-button v-else-if="isRegistered(selectedItem?.id)" type="info" disabled>您已报名该活动</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const currentUserId = userInfo.id || 1001

const loading = ref(false)
const activeTab = ref('all')
const filter = reactive({ keyword: '', category: '' })

const allList = ref([])
const recommendList = ref([])
const registeredIds = ref([]) // 存放当前用户已报名的活动ID

const detailVisible = ref(false)
const selectedItem = ref(null)

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/activity/list')
    allList.value = res || []
    
    // 假设后端有推荐接口，先用slice模拟
    recommendList.value = allList.value.slice(0, 3) 

    // 【核心修复】：后端返回的已经是纯数字的列表 [1, 2, 3]，不需要 map 映射
    const myRes = await request.get(`/activity/my-registered?userId=${currentUserId}`)
    registeredIds.value = myRes || []
  } catch (e) { console.error(e) } finally {
    loading.value = false
  }
}

const getStatusInfo = (item) => {
  if (item.status === 3 || item.status === '已结束') return { text: '已结束', type: 'info' }
  if (item.status === 2 || item.status === '进行中') return { text: '进行中', type: 'danger' }
  if (item.status === 1 || item.status === '报名中') {
    if (item.capacity && item.currentEnrollment >= item.capacity) {
      return { text: '已满员', type: 'warning' }
    }
    return { text: '报名中', type: 'success' }
  }
  return { text: '未知', type: 'info' }
}

const getPercentage = (item) => {
  if (!item.capacity) return 0;
  const p = Math.floor(((item.currentEnrollment || 0) / item.capacity) * 100)
  return p > 100 ? 100 : p
}

const isRegistered = (id) => registeredIds.value.includes(id)

const getCategoryClass = (category) => {
  const map = {
    '学术讲座': 'bg-academic',
    '文体演艺': 'bg-art',
    '志愿服务': 'bg-volunteer',
    '社团招新': 'bg-club'
  }
  return map[category] || 'bg-default'
}

const filteredList = computed(() => {
  return allList.value.filter(i => {
    if (filter.category && i.category !== filter.category) return false
    if (filter.keyword && !i.title.includes(filter.keyword)) return false
    return true
  })
})

const myActivities = computed(() => {
  return allList.value.filter(i => isRegistered(i.id))
})

const showDetails = (item) => {
  selectedItem.value = item
  detailVisible.value = true
}

const handleEnroll = async () => {
  try {
    await ElMessageBox.confirm('确认消耗名额报名该活动吗？', '报名确认')
    await request.post(`/activity/enroll?activityId=${selectedItem.value.id}&userId=${currentUserId}`)
    ElMessage.success('报名成功！请准时参加哦。')
    detailVisible.value = false
    fetchList()
  } catch (e) {
    // 取消操作
  }
}

const cancelRegistration = async (activityId) => {
  try {
    await ElMessageBox.confirm('确定要取消报名吗？名额将被释放。', '操作确认', { type: 'warning' })
    await request.post(`/activity/cancel-enroll?activityId=${activityId}&userId=${currentUserId}`)
    ElMessage.success('已取消报名')
    fetchList()
  } catch (e) {}
}

onMounted(fetchList)
</script>

<style scoped>
.activity-page { padding-bottom: 20px; }
.main-card { border-radius: 12px; border: none; min-height: 600px; }
.card-header { display: flex; justify-content: space-between; align-items: center; width: 100%; }
.header-left { display: flex; align-items: center; font-weight: bold; font-size: 18px; }

.section-header { display: flex; align-items: center; margin: 25px 0 15px 0; }
.section-header .emoji { font-size: 22px; margin-right: 8px; }
.section-header .title { margin: 0; font-size: 18px; color: #333; font-weight: 600; margin-right: 12px; }
.section-header .subtitle { color: #909399; font-size: 13px; }

.items-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 20px; }
.recommend-grid { grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); }

/* 活动卡片基础 */
.activity-card { cursor: pointer; border-radius: 12px; overflow: hidden; transition: all 0.3s; border: 1px solid #ebeef5; display: flex; flex-direction: column; }
.activity-card:hover { transform: translateY(-5px); box-shadow: 0 12px 24px rgba(0,0,0,0.08); }

/* 猜你喜欢专属 */
.recommend-card { background: linear-gradient(135deg, #fffaf0 0%, #fff0f0 100%); position: relative; border: none; }
.tag-box { position: absolute; top: 12px; right: 12px; }
.card-content { padding: 20px; }

/* 海报占位符区 */
.poster-box { height: 140px; display: flex; align-items: center; justify-content: center; position: relative; color: white; font-size: 20px; font-weight: bold; letter-spacing: 2px; overflow: hidden; }
.poster-img { width: 100%; height: 100%; position: absolute; top: 0; left: 0; z-index: 0; }
.poster-text { z-index: 1; }

.bg-academic { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.bg-art { background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%); }
.bg-volunteer { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.bg-club { background: linear-gradient(135deg, #f6d365 0%, #fda085 100%); }
.bg-default { background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%); }

.status-badge { position: absolute; top: 10px; left: 10px; z-index: 2; }

/* 卡片信息区 */
.act-info { padding: 15px; flex: 1; display: flex; flex-direction: column; }
.act-title { margin: 0 0 12px; font-size: 16px; color: #303133; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.act-desc { font-size: 13px; color: #666; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; margin-bottom: 15px; }
.act-meta-list p { margin: 0 0 6px 0; font-size: 13px; color: #909399; display: flex; align-items: center; gap: 5px; }

/* 进度条区块 */
.quota-box { margin-top: auto; padding-top: 12px; border-top: 1px dashed #ebeef5; }
.quota-text { display: flex; justify-content: space-between; font-size: 12px; color: #909399; margin-bottom: 6px; }
.full-text { color: #F56C6C; font-weight: bold; }

/* 我的日程卡片 */
.my-card { padding: 15px; }
.my-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; border-bottom: 1px solid #eee; padding-bottom: 10px; }
.my-card-header .date { font-weight: bold; color: #409EFF; font-size: 18px; }
.my-card .loc { font-size: 13px; color: #666; margin: 10px 0; }
.my-card-footer { display: flex; justify-content: space-between; margin-top: auto; }

/* 详情弹窗 */
.detail-container { padding: 0 10px; }
.detail-poster { width: 100%; max-height: 250px; border-radius: 8px; margin-bottom: 15px; }
.d-title { margin-top: 0; color: #303133; }
.d-tags { margin-bottom: 20px; display: flex; gap: 10px; }
.content-text { white-space: pre-wrap; line-height: 1.6; color: #606266; }
.enroll-status-box { margin-top: 20px; padding: 15px; background: #f8f9fa; border-radius: 8px; }
</style>