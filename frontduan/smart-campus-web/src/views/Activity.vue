<template>
  <div class="activity-page">
    <el-skeleton :loading="loading" animated>
      <template #default>
        
        <div class="section-header">
          <span class="emoji">✨</span>
          <h3 class="title">猜你喜欢</h3>
          <span class="subtitle">基于协同过滤算法为您精准推荐</span>
        </div>
        
        <el-row :gutter="20" class="recommend-row">
          <el-empty v-if="recommendList.length === 0" description="暂无个性化推荐，多去报名/收藏活动吧！" style="width: 100%;" />
          
          <el-col :span="8" v-for="item in recommendList" :key="item.id">
            <el-card shadow="hover" class="activity-card recommend-card">
              <div class="card-content">
                <div class="tag-box"><el-tag effect="dark" type="warning" round>为你推荐</el-tag></div>
                <h4 class="act-title">{{ item.title }}</h4>
                <p class="act-desc">{{ item.content }}</p>
                <div class="act-meta">
                  <span><el-icon><Location /></el-icon> {{ item.location || '线上活动' }}</span>
                </div>
                <el-button 
                  :type="registeredIds.includes(item.id) ? 'info' : 'primary'" 
                  class="join-btn" 
                  round 
                  :disabled="registeredIds.includes(item.id) || item.status !== 1"
                  @click="openRegisterDialog(item)">
                  {{ registeredIds.includes(item.id) ? '已报名' : (item.status !== 1 ? '已结束' : '立即报名') }}
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-divider border-style="dashed" />

        <div class="section-header">
          <span class="emoji">🔥</span>
          <h3 class="title">活动大厅</h3>
          <span class="subtitle">探索校园里的新鲜事</span>
        </div>

        <el-row :gutter="20">
          <el-col :span="8" v-for="item in allList" :key="item.id" style="margin-bottom: 20px;">
            <el-card shadow="hover" class="activity-card normal-card">
              <div class="card-content">
                <h4 class="act-title">{{ item.title }}</h4>
                <p class="act-desc">{{ item.content }}</p>
                <div class="act-meta" style="margin-bottom: 10px;">
                  <el-tag size="small" :type="item.status === 1 ? 'success' : 'info'">
                    {{ item.status === 1 ? '报名中' : '已结束' }}
                  </el-tag>
                </div>
                <el-button 
                  :type="registeredIds.includes(item.id) ? 'info' : 'primary'" 
                  class="join-btn" 
                  round 
                  size="small"
                  :disabled="registeredIds.includes(item.id) || item.status !== 1"
                  @click="openRegisterDialog(item)">
                  {{ registeredIds.includes(item.id) ? '已报名' : (item.status !== 1 ? '已结束' : '立即报名') }}
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>

      </template>
    </el-skeleton>

    <el-dialog 
      v-model="dialogVisible" 
      title="活动报名确认" 
      width="420px" 
      center
      append-to-body
      align-center
    >
      <div v-if="currentActivity" style="text-align: center; margin-bottom: 20px;">
        <h3 style="margin-top: 0;">{{ currentActivity.title }}</h3>
        <p style="color: #666; font-size: 14px;">
          <el-icon><Calendar /></el-icon> {{ currentActivity.startTime || '时间待定' }}
        </p>
        <el-alert title="报名后名额将被锁定，请准时参加哦！" type="info" show-icon :closable="false" />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">手滑了</el-button>
          <el-button type="primary" :loading="submitting" @click="confirmRegister">确认报名</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(true)
const recommendList = ref([])
const allList = ref([])
const registeredIds = ref([]) // 【新增】存储当前用户已报名的活动ID

const dialogVisible = ref(false)
const currentActivity = ref(null)
const submitting = ref(false)

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

const openRegisterDialog = (activity) => {
  currentActivity.value = activity
  dialogVisible.value = true
}

// 核心数据加载逻辑
const fetchData = async () => {
  loading.value = true
  try {
    const currentUserId = userInfo.id || 1001

    // 1. 获取已报名列表 (为了置灰按钮)
    const regData = await request.get(`/activity/my-registered?userId=${currentUserId}`)
    registeredIds.value = regData || []

    // 2. 获取推荐列表 (现在使用的是你真实的登录 ID！)
    const recData = await request.get(`/activity/recommend?userId=${currentUserId}&topN=3`)
    recommendList.value = recData || []

    // 3. 获取所有活动
    const allData = await request.get('/activity/list')
    allList.value = allData || []
  } catch (error) {
    console.error('获取活动数据失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})

const confirmRegister = async () => {
  submitting.value = true
  try {
    const userId = userInfo.id || 1001 
    const activityId = currentActivity.value.id
    
    await request.post(`/activity/register?activityId=${activityId}&userId=${userId}`)
    
    ElMessage.success('🎉 报名成功！期待你的参与！')
    dialogVisible.value = false
    
    // 【关键一步】报名成功后，重新拉取一次数据，这样页面上的按钮就会瞬间变成灰色的“已报名”！
    fetchData() 
  } catch (error) {
    console.log('报名操作中止')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.activity-page { padding-bottom: 40px; }
.section-header { display: flex; align-items: center; margin: 10px 0 20px 0; }
.section-header .emoji { font-size: 24px; margin-right: 10px; }
.section-header .title { margin: 0; font-size: 20px; color: #333; margin-right: 15px; }
.section-header .subtitle { color: #999; font-size: 13px; margin-top: 5px; }
.recommend-row { margin-bottom: 30px; min-height: 200px; }
.activity-card { border-radius: 16px; transition: all 0.3s; border: none; }
.activity-card:hover { transform: translateY(-5px); }
.recommend-card { background: linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%); position: relative; overflow: visible; }
.normal-card { background: white; }
.card-content { display: flex; flex-direction: column; height: 170px; }
.tag-box { position: absolute; top: -12px; right: 20px; }
.act-title { margin: 0 0 10px 0; font-size: 16px; color: #2c3e50; display: -webkit-box; -webkit-box-orient: vertical; overflow: hidden; }
.act-desc { font-size: 13px; color: #666; margin: 0; flex: 1; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.act-meta { margin-top: auto; font-size: 12px; color: #888; display: flex; align-items: center; }
.join-btn { margin-top: 15px; width: 100px; }

:deep(.el-dialog) {
  border-radius: 16px !important;
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
}
:deep(.el-dialog__header) { margin-right: 0; padding-top: 25px; }
:deep(.el-dialog__title) { font-weight: bold; color: #333; }
</style>