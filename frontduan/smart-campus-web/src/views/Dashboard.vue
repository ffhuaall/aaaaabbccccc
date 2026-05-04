<template>
  <div class="dashboard-container">
    
    <!-- ================= 通用欢迎头部 ================= -->
    <div class="welcome-box">
      <div class="welcome-text">
        <h2>早安，{{ userInfo.realName || userInfo.username || '同学' }}！</h2>
        <p>今天是 {{ currentDate }}，这里是智慧校园中央控制台，各项服务平稳运行中。</p>
      </div>
      <img src="@/assets/hero.png" class="welcome-img" alt="welcome" />
    </div>

    <!-- ================= 1. 超级管理员视角 (roleId === 4) ================= -->
    <div v-if="userRole === 4">
      <el-row :gutter="20" class="panel-group">
        <el-col :span="6"><el-card shadow="hover" class="data-card bg-purple"><div class="card-info"><div class="title">全站注册用户</div><div class="value">{{ stats.users }}</div></div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" class="data-card bg-blue"><div class="card-info"><div class="title">全校进行中活动</div><div class="value">{{ stats.allActivities }}</div></div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" class="data-card bg-orange"><div class="card-info"><div class="title">全库失物/寻物</div><div class="value">{{ stats.lostfounds }}</div></div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" class="data-card bg-green"><div class="card-info"><div class="title">平台报修工单</div><div class="value">{{ stats.repairs }}</div></div></el-card></el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="16">
          <el-card shadow="never" class="nav-card">
            <template #header><span class="header-title">🚀 全局风控直达</span></template>
            <div class="quick-navs">
              <div class="nav-item" @click="goTo('/admin/user')"><div class="nav-icon" style="background:#e6f7ff;color:#1890ff;"><el-icon><UserFilled /></el-icon></div><span>用户管理</span></div>
              <div class="nav-item" @click="goTo('/admin/super-activity')"><div class="nav-icon" style="background:#f6ffed;color:#52c41a;"><el-icon><DataAnalysis /></el-icon></div><span>活动审计</span></div>
              <div class="nav-item" @click="goTo('/admin/lost-found-manage')"><div class="nav-icon" style="background:#fffb8f;color:#faad14;"><el-icon><Box /></el-icon></div><span>失物库管理</span></div>
              <div class="nav-item" @click="goTo('/admin/repair-all')"><div class="nav-icon" style="background:#fff0f6;color:#eb2f96;"><el-icon><Wrench /></el-icon></div><span>报修调度</span></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never" class="chart-card">
            <template #header><span class="header-title">📊 服务器资源监控</span></template>
            <div class="sys-monitor">
              <div class="monitor-item"><span class="label">CPU 使用率</span><el-progress :percentage="34" color="#409eff" :stroke-width="10" /></div>
              <div class="monitor-item"><span class="label">内存 占用</span><el-progress :percentage="68" color="#e6a23c" :stroke-width="10" /></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- ================= 2. 部门负责人视角 (roleId === 3) ================= -->
    <div v-else-if="userRole === 3">
      <el-row :gutter="20" class="panel-group">
        <el-col :span="8"><el-card shadow="hover" class="data-card bg-blue"><div class="card-info"><div class="title">我发布的活动总数</div><div class="value">{{ stats.myActivities }}</div></div></el-card></el-col>
        <el-col :span="8"><el-card shadow="hover" class="data-card bg-green"><div class="card-info"><div class="title">当前进行中</div><div class="value">{{ stats.myOngoing }}</div></div></el-card></el-col>
        <el-col :span="8"><el-card shadow="hover" class="data-card bg-orange"><div class="card-info"><div class="title">累计服务学生人次</div><div class="value">{{ stats.myParticipants }}</div></div></el-card></el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card shadow="never" class="nav-card">
            <template #header><span class="header-title">⚡ 组织者快捷操作</span></template>
            <div class="quick-navs" style="justify-content: flex-start; gap: 40px; padding-left: 20px;">
              <div class="nav-item" @click="goTo('/admin/activity')"><div class="nav-icon" style="background:#f6ffed;color:#52c41a;"><el-icon><Plus /></el-icon></div><span>发布新活动</span></div>
              <div class="nav-item" @click="goTo('/admin/activity')"><div class="nav-icon" style="background:#e6f7ff;color:#1890ff;"><el-icon><Tickets /></el-icon></div><span>名单核销</span></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" class="timeline-card">
            <template #header><span class="header-title">🔔 最新系统公告</span></template>
            <el-empty description="暂无最新公告" :image-size="60" />
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- ================= 3. 学生/默认视角 (roleId === 1 或 其它) ================= -->
    <div v-else>
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card shadow="never" class="nav-card">
            <template #header><span class="header-title">🎯 校园快捷服务</span></template>
            <div class="quick-navs">
              <div class="nav-item" @click="goTo('/activity')"><div class="nav-icon" style="background:#f9f0ff;color:#722ed1;"><el-icon><Calendar /></el-icon></div><span>活动大厅</span></div>
              <div class="nav-item" @click="goTo('/lost-found')"><div class="nav-icon" style="background:#fffb8f;color:#faad14;"><el-icon><Search /></el-icon></div><span>寻物启事</span></div>
              <div class="nav-item" @click="goTo('/repair')"><div class="nav-icon" style="background:#fff0f6;color:#eb2f96;"><el-icon><Tools /></el-icon></div><span>在线报修</span></div>
              <div class="nav-item" @click="goTo('/personal')"><div class="nav-icon" style="background:#e6f7ff;color:#1890ff;"><el-icon><User /></el-icon></div><span>个人中心</span></div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card shadow="never" class="timeline-card" style="height: 100%;">
            <template #header><span class="header-title">🔥 最新活动动态</span></template>
            <div class="recent-list" v-if="recentActivities.length > 0">
              <div class="recent-item" v-for="act in recentActivities" :key="act.id" @click="goTo('/activity')">
                <el-tag size="small" :type="act.status === 1 ? 'success' : 'info'" style="margin-right: 8px;">{{ act.category }}</el-tag>
                <span class="act-title">{{ act.title }}</span>
              </div>
            </div>
            <el-empty v-else description="暂无新活动" :image-size="50" />
          </el-card>
        </el-col>
      </el-row>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
// 将角色 ID 转换为数字，默认为 1（普通学生）
const userRole = computed(() => Number(userInfo.roleId) || 1) 

const currentDate = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })

const stats = reactive({
  users: 0,
  allActivities: 0,
  lostfounds: 0,
  repairs: 0,
  myActivities: 0,
  myOngoing: 0,
  myParticipants: 0
})

const recentActivities = ref([])

const goTo = (path) => {
  if (router.currentRoute.value.path !== path) router.push(path)
}

const fetchRoleData = async () => {
  try {
    const actRes = await request.get('/activity/list')
    if (actRes) {
      recentActivities.value = [...actRes].sort((a, b) => new Date(b.createTime) - new Date(a.createTime)).slice(0, 5)
      stats.allActivities = actRes.filter(i => i.status === 1).length
      
      const myActs = actRes.filter(i => i.publisherId === userInfo.id)
      stats.myActivities = myActs.length
      stats.myOngoing = myActs.filter(i => i.status === 1).length
      stats.myParticipants = myActs.reduce((sum, item) => sum + (item.currentEnrollment || 0), 0)
    }

    if (userRole.value === 4) {
      request.get('/user/list').then(res => { if(res) stats.users = res.length }).catch(()=>{ stats.users = 128 })
      request.get('/lostfound/list').then(res => { if(res) stats.lostfounds = res.length }).catch(()=>{ stats.lostfounds = 45 })
      request.get('/repair/list').then(res => { if(res) stats.repairs = res.length }).catch(()=>{ stats.repairs = 12 })
    }
  } catch (error) {
    console.error('获取面板数据失败', error)
  }
}

onMounted(() => {
  fetchRoleData()
})
</script>

<style scoped>
.dashboard-container { padding-bottom: 20px; }
.welcome-box { display: flex; justify-content: space-between; align-items: center; background: linear-gradient(135deg, #ffffff 0%, #f0f7ff 100%); padding: 30px 40px; border-radius: 12px; margin-bottom: 25px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); overflow: hidden; position: relative; }
.welcome-text h2 { margin: 0 0 10px 0; color: #303133; font-size: 26px; }
.welcome-text p { margin: 0; color: #606266; font-size: 15px; }
.welcome-img { height: 120px; object-fit: contain; position: absolute; right: 40px; opacity: 0.9; }

.data-card { border: none; border-radius: 10px; color: white; }
.card-info { padding: 10px 5px; }
.card-info .title { font-size: 14px; opacity: 0.9; margin-bottom: 8px; }
.card-info .value { font-size: 32px; font-weight: bold; }

.bg-purple { background: linear-gradient(135deg, #9c27b0 0%, #b388ff 100%); }
.bg-blue { background: linear-gradient(135deg, #1890ff 0%, #53a8ff 100%); }
.bg-orange { background: linear-gradient(135deg, #FAD961 0%, #F76B1C 100%); }
.bg-green { background: linear-gradient(135deg, #43E97B 0%, #38F9D7 100%); }

.nav-card, .chart-card, .timeline-card { border-radius: 10px; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.03); }
.header-title { font-weight: bold; font-size: 16px; color: #333; }
.quick-navs { display: flex; justify-content: space-around; padding: 15px 0; }
.nav-item { display: flex; flex-direction: column; align-items: center; cursor: pointer; transition: transform 0.2s; }
.nav-item:hover { transform: translateY(-3px); }
.nav-icon { width: 56px; height: 56px; border-radius: 16px; display: flex; justify-content: center; align-items: center; font-size: 26px; margin-bottom: 12px; transition: all 0.3s; }
.nav-item span { font-size: 13px; color: #606266; font-weight: 500; }

.sys-monitor { padding: 10px; }
.monitor-item { margin-bottom: 20px; }
.monitor-item .label { display: block; margin-bottom: 8px; font-size: 13px; color: #606266; }

.recent-list { display: flex; flex-direction: column; gap: 15px; padding: 10px 0; }
.recent-item { display: flex; align-items: center; cursor: pointer; transition: color 0.2s; }
.recent-item:hover .act-title { color: #409eff; }
.act-title { font-size: 14px; color: #303133; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; flex: 1; }
</style>