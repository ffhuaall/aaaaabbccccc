<template>
  <div class="dashboard-container">
    
    <div class="welcome-box">
      <div class="welcome-text">
        <h2>早安，{{ userInfo.realName || userInfo.username || '同学' }}！</h2>
        <p>今天是 {{ currentDate }}，这里是智慧校园中央控制台，各项服务平稳运行中。</p>
      </div>
      <img src="@/assets/hero.png" class="welcome-img" alt="welcome" />
    </div>

    <div v-if="userRole === 4">
      <el-row :gutter="20" class="panel-group">
        <el-col :span="6"><el-card shadow="hover" class="data-card bg-purple"><div class="card-info"><div class="title">全站注册用户</div><div class="value">{{ stats.users }}</div></div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" class="data-card bg-blue"><div class="card-info"><div class="title">全校进行中活动</div><div class="value">{{ stats.allActivities }}</div></div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" class="data-card bg-orange"><div class="card-info"><div class="title">失物招领记录</div><div class="value">{{ stats.lostfounds }}</div></div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" class="data-card bg-green"><div class="card-info"><div class="title">平台报修工单</div><div class="value">{{ stats.repairs }}</div></div></el-card></el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="16">
          <el-card shadow="never" class="nav-card" style="margin-bottom: 20px;">
            <template #header><span class="header-title">⚙️ 全局管理入口</span></template>
            <div class="quick-navs">
              <div class="nav-item" @click="goTo('/admin/user')"><div class="nav-icon" style="background:#e6f7ff;color:#1890ff;"><el-icon><UserFilled /></el-icon></div><span>用户管理</span></div>
              <div class="nav-item" @click="goTo('/admin/super-activity')"><div class="nav-icon" style="background:#f6ffed;color:#52c41a;"><el-icon><DataAnalysis /></el-icon></div><span>活动审计</span></div>
              <div class="nav-item" @click="goTo('/admin/lost-found-manage')"><div class="nav-icon" style="background:#fffb8f;color:#faad14;"><el-icon><Box /></el-icon></div><span>失物库管理</span></div>
              <div class="nav-item" @click="goTo('/admin/repair-all')"><div class="nav-icon" style="background:#fff0f6;color:#eb2f96;"><el-icon><Wrench /></el-icon></div><span>报修调度</span></div>
            </div>
          </el-card>

          <el-card shadow="never" class="table-card">
            <template #header><span class="header-title">📋 系统操作日志</span></template>
            <el-table :data="superLogs" size="small" stripe border>
              <el-table-column prop="time" label="操作时间" width="140" />
              <el-table-column prop="admin" label="操作人" width="100" />
              <el-table-column prop="action" label="动作" width="120">
                <template #default="scope"><el-tag size="small" :type="scope.row.type">{{ scope.row.action }}</el-tag></template>
              </el-table-column>
              <el-table-column prop="detail" label="详情" show-overflow-tooltip />
            </el-table>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card shadow="never" class="timeline-card" style="margin-bottom: 20px;">
            <template #header><span class="header-title">📌 待办事项 (本地备忘录)</span></template>
            <el-checkbox-group v-model="superTodo" class="custom-todo">
              <el-checkbox label="1" style="display:block;margin-bottom:12px;">审核新申请的社团管理账号</el-checkbox>
              <el-checkbox label="2" style="display:block;margin-bottom:12px;">导出上月全校活动数据报表</el-checkbox>
              <el-checkbox label="3" style="display:block;margin-bottom:12px;">处理系统慢查询告警</el-checkbox>
            </el-checkbox-group>
          </el-card>

          <el-card shadow="never" class="chart-card" style="height: calc(100% - 212px);">
            <template #header><span class="header-title">📊 服务器状态监控</span></template>
            <div class="sys-monitor">
              <div class="monitor-item">
                <div class="monitor-header"><span>CPU 使用率 (4核)</span><span>34%</span></div>
                <el-progress :percentage="34" color="#409eff" :stroke-width="10" />
              </div>
              <div class="monitor-item">
                <div class="monitor-header"><span>内存 占用 (16GB)</span><span>68%</span></div>
                <el-progress :percentage="68" color="#e6a23c" :stroke-width="10" />
              </div>
              <div class="monitor-item">
                <div class="monitor-header"><span>系统磁盘 (500GB)</span><span>45%</span></div>
                <el-progress :percentage="45" color="#67c23a" :stroke-width="10" />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div v-else-if="userRole === 3">
      <el-row :gutter="20" class="panel-group">
        <el-col :span="6"><el-card shadow="hover" class="data-card bg-blue"><div class="card-info"><div class="title">我发布的活动</div><div class="value">{{ stats.myActivities }}</div></div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" class="data-card bg-green"><div class="card-info"><div class="title">当前进行中</div><div class="value">{{ stats.myOngoing }}</div></div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" class="data-card bg-orange"><div class="card-info"><div class="title">累计报名人次</div><div class="value">{{ stats.myParticipants }}</div></div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" class="data-card bg-purple"><div class="card-info"><div class="title">待核销名单</div><div class="value">{{ stats.myParticipants }}</div></div></el-card></el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="16">
          <el-card shadow="never" class="nav-card" style="margin-bottom: 20px;">
            <template #header><span class="header-title">⚡ 快捷操作</span></template>
            <div class="quick-navs" style="justify-content: flex-start; gap: 40px; padding-left: 20px;">
              <div class="nav-item" @click="goTo('/admin/activity')"><div class="nav-icon" style="background:#f6ffed;color:#52c41a;"><el-icon><Plus /></el-icon></div><span>策划新活动</span></div>
              <div class="nav-item" @click="goTo('/admin/activity')"><div class="nav-icon" style="background:#e6f7ff;color:#1890ff;"><el-icon><Tickets /></el-icon></div><span>名单与核销</span></div>
              <div class="nav-item" @click="goTo('/personal')"><div class="nav-icon" style="background:#f9f0ff;color:#722ed1;"><el-icon><User /></el-icon></div><span>部门资料维护</span></div>
            </div>
          </el-card>

          <el-card shadow="never" class="table-card">
            <template #header><span class="header-title">📈 近期活动报名情况</span></template>
            <el-table :data="adminMyActs" size="small" stripe border>
              <el-table-column prop="title" label="活动名称" min-width="180" show-overflow-tooltip />
              <el-table-column prop="status" label="状态" width="80" align="center">
                <template #default="scope">
                  <el-tag size="small" :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '进行中' : '已结束' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="报名进度" width="180" align="center">
                <template #default="scope">
                  <el-progress :percentage="getPercentage(scope.row)" :stroke-width="8" :text-inside="true" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center">
                <template #default>
                  <el-button type="primary" link size="small" @click="goTo('/admin/activity')">管理</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card shadow="never" class="timeline-card" style="margin-bottom: 20px;">
            <template #header><span class="header-title">📌 待办事项 (本地备忘录)</span></template>
            <el-checkbox-group v-model="adminTodo" class="custom-todo">
              <el-checkbox label="1" style="display:block;margin-bottom:12px;">向审批部提交场地申请表</el-checkbox>
              <el-checkbox label="2" style="display:block;margin-bottom:12px;">导出歌手大赛决赛签到表</el-checkbox>
              <el-checkbox label="3" style="display:block;margin-bottom:12px;">审核招新推文草稿</el-checkbox>
              <el-checkbox label="4" style="display:block;margin-bottom:12px;">确认本周活动赞助物资</el-checkbox>
            </el-checkbox-group>
          </el-card>

          <el-card shadow="never" class="timeline-card" style="height: calc(100% - 245px);">
            <template #header><span class="header-title">🔔 最新系统公告</span></template>
            <div class="recent-list" v-if="systemNotices.length > 0">
              <div class="recent-item" v-for="(notice, idx) in systemNotices" :key="idx">
                <el-tag size="small" :type="notice.type" style="margin-right: 8px;">公告</el-tag>
                <span class="act-title" style="color: #606266;">{{ notice.title }}</span>
              </div>
            </div>
            <el-empty v-else description="暂无最新公告" :image-size="40" />
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div v-else-if="userRole === 2">
      <el-row :gutter="20" class="panel-group">
        <el-col :span="6"><el-card shadow="hover" class="data-card bg-orange"><div class="card-info"><div class="title">待处理工单</div><div class="value">{{ stats.workerPending }}</div></div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" class="data-card bg-blue"><div class="card-info"><div class="title">维修中工单</div><div class="value">{{ stats.workerProcessing }}</div></div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" class="data-card bg-green"><div class="card-info"><div class="title">已完成工单</div><div class="value">{{ stats.workerFinished }}</div></div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover" class="data-card bg-purple"><div class="card-info"><div class="title">平均满意度</div><div class="value">98<span class="unit">%</span></div></div></el-card></el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="16">
          <el-card shadow="never" class="nav-card" style="margin-bottom: 20px;">
            <template #header><span class="header-title">⚡ 快捷操作</span></template>
            <div class="quick-navs" style="justify-content: flex-start; gap: 40px; padding-left: 20px;">
              <div class="nav-item" @click="goTo('/admin/repair')"><div class="nav-icon" style="background:#fff0f6;color:#eb2f96;"><el-icon><Tools /></el-icon></div><span>工单处理大厅</span></div>
              <div class="nav-item" @click="goTo('/personal')"><div class="nav-icon" style="background:#e6f7ff;color:#1890ff;"><el-icon><User /></el-icon></div><span>个人排班与考勤</span></div>
            </div>
          </el-card>

          <el-card shadow="never" class="table-card">
            <template #header><span class="header-title">📋 待处理真实工单</span></template>
            <el-table :data="workerPendingList" size="small" stripe border>
              <el-table-column prop="location" label="宿舍位置" width="140" />
              <el-table-column prop="title" label="报修简述" show-overflow-tooltip />
              <el-table-column prop="time" label="提单时间" width="140" />
              <el-table-column label="操作" width="80" align="center">
                <template #default><el-button type="primary" link size="small" @click="goTo('/admin/repair')">去处理</el-button></template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card shadow="never" class="timeline-card" style="margin-bottom: 20px;">
            <template #header><span class="header-title">📌 待办事项 (本地备忘录)</span></template>
            <el-checkbox-group v-model="workerTodo" class="custom-todo">
              <el-checkbox label="1" style="display:block;margin-bottom:12px;">去后勤总仓领取本周维修耗材</el-checkbox>
              <el-checkbox label="2" style="display:block;margin-bottom:12px;">提交上月个人考勤与值班报表</el-checkbox>
              <el-checkbox label="3" style="display:block;margin-bottom:12px;">核对本周五临时排班计划</el-checkbox>
            </el-checkbox-group>
          </el-card>

          <el-card shadow="never" class="chart-card" style="height: calc(100% - 212px);">
            <template #header><span class="header-title">📊 当月工单完成量统计</span></template>
            <div class="sys-monitor">
              <div class="monitor-item">
                <div class="monitor-header"><span>1. 水电维修组</span><span>68单</span></div>
                <el-progress :percentage="100" color="#f56c6c" :show-text="false" :stroke-width="8" />
              </div>
              <div class="monitor-item">
                <div class="monitor-header"><span>2. 木工维修组</span><span>52单</span></div>
                <el-progress :percentage="80" color="#e6a23c" :show-text="false" :stroke-width="8" />
              </div>
              <div class="monitor-item">
                <div class="monitor-header"><span>3. 网络维修组</span><span>41单</span></div>
                <el-progress :percentage="65" color="#409eff" :show-text="false" :stroke-width="8" />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div v-else>
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card shadow="never" class="nav-card" style="margin-bottom: 20px;">
            <template #header><span class="header-title">🎯 校园快捷服务</span></template>
            <div class="quick-navs">
              <div class="nav-item" @click="goTo('/activity')"><div class="nav-icon" style="background:#f9f0ff;color:#722ed1;"><el-icon><Calendar /></el-icon></div><span>活动大厅</span></div>
              <div class="nav-item" @click="goTo('/lost-found')"><div class="nav-icon" style="background:#fffb8f;color:#faad14;"><el-icon><Search /></el-icon></div><span>寻物启事</span></div>
              <div class="nav-item" @click="goTo('/repair')"><div class="nav-icon" style="background:#fff0f6;color:#eb2f96;"><el-icon><Service /></el-icon></div><span>在线报修</span></div>
              <div class="nav-item" @click="goTo('/course')"><div class="nav-icon" style="background:#f6ffed;color:#52c41a;"><el-icon><Reading /></el-icon></div><span>我的课表</span></div>
              <div class="nav-item" @click="goTo('/personal')"><div class="nav-icon" style="background:#e6f7ff;color:#1890ff;"><el-icon><User /></el-icon></div><span>个人中心</span></div>
            </div>
          </el-card>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-card shadow="never" class="table-card" style="height: 250px;">
                <template #header><span class="header-title">📅 今日课表概览</span></template>
                <div class="recent-list" v-if="studentSchedule.length > 0">
                   <div class="recent-item" v-for="(course, idx) in studentSchedule" :key="idx">
                      <el-tag size="small" type="primary" style="margin-right: 8px;">第{{ course.period }}节</el-tag>
                      <span class="act-title"><strong>{{ course.name }}</strong> @ {{ course.loc }}</span>
                   </div>
                </div>
                <el-empty v-else description="今天没课哦，好好休息吧~" :image-size="60" />
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="never" class="timeline-card" style="height: 250px;">
                <template #header><span class="header-title">📢 校园系统公告</span></template>
                <div class="recent-list" v-if="systemNotices.length > 0">
                  <div class="recent-item" v-for="(notice, idx) in systemNotices" :key="idx">
                    <el-tag size="small" :type="notice.type" style="margin-right: 8px;">公告</el-tag>
                    <span class="act-title" style="color: #606266;">{{ notice.title }}</span>
                  </div>
                </div>
                <el-empty v-else description="暂无公告" :image-size="60" />
              </el-card>
            </el-col>
          </el-row>
        </el-col>
        
        <el-col :span="8">
          <el-card shadow="never" class="timeline-card" style="height: 100%;">
            <template #header><span class="header-title">📢 最新活动动态</span></template>
            <div class="recent-list" v-if="recentActivities.length > 0">
              <div class="recent-item" v-for="act in recentActivities" :key="act.id" @click="goTo('/activity')">
                <el-tag size="small" :type="act.status === 1 ? 'success' : 'info'" style="margin-right: 8px;">{{ act.category || '校园' }}</el-tag>
                <span class="act-title">{{ act.title }}</span>
              </div>
            </div>
            <el-empty v-else description="近期暂无新活动" :image-size="50" />
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
const userRole = computed(() => Number(userInfo.roleId) || 1) 
const currentDate = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })

// 所有核心统计数据
const stats = reactive({
  users: 0, allActivities: 0, lostfounds: 0, repairs: 0,
  myActivities: 0, myOngoing: 0, myParticipants: 0,
  workerPending: 0, workerProcessing: 0, workerFinished: 0
})

// 移除原有的假数据，全部变为响应式空数组准备承接后端真实数据
const recentActivities = ref([])
const adminMyActs = ref([])
const workerPendingList = ref([])
const studentSchedule = ref([])
const systemNotices = ref([])
const superLogs = ref([])

// 本地记事本工具 (不存库，仅作前端交互展示)
const superTodo = ref([])
const adminTodo = ref([])
const workerTodo = ref([])

const goTo = (path) => {
  if (router.currentRoute.value.path !== path) router.push(path)
}

const getPercentage = (item) => {
  if (!item.capacity) return 0
  return Math.min(Math.floor(((item.currentEnrollment || 0) / item.capacity) * 100), 100)
}

const fetchRoleData = async () => {
  try {
    // 1. 全局：拉取系统公告 (学生和负责人界面需要展示)
    request.get('/notice/list').then(res => {
      if (res && res.length > 0) {
        systemNotices.value = res.filter(item => item.isActive === 1).map(item => ({
          title: item.title,
          type: item.level || 'info'
        })).slice(0, 4) // 只取最新的 4 条激活公告
      }
    }).catch(()=>{})

    // 2. 全局：拉取真实的活动数据
    const actRes = await request.get('/activity/list')
    if (actRes) {
      recentActivities.value = [...actRes].sort((a, b) => new Date(b.createTime) - new Date(a.createTime)).slice(0, 5)
      stats.allActivities = actRes.filter(i => i.status === 1).length
      
      const myActs = actRes.filter(i => i.publisherId === userInfo.id)
      adminMyActs.value = myActs.sort((a,b) => b.id - a.id).slice(0, 3) 
      stats.myActivities = myActs.length
      stats.myOngoing = myActs.filter(i => i.status === 1).length
      stats.myParticipants = myActs.reduce((sum, item) => sum + (item.currentEnrollment || 0), 0)
    }

    // ================= 超管专属逻辑 =================
    if (userRole.value === 4) {
      // 拉取真实的系统风控日志
      request.get('/log/recent').then(res => {
        if (res && res.length > 0) {
          superLogs.value = res.map(item => ({
            time: item.createTime ? item.createTime.substring(5, 16).replace('T', ' ') : '',
            admin: item.username,
            action: item.action,
            type: item.type,
            detail: item.detail
          }))
        }
      }).catch(()=>{})

      // 拉取真实的其余全站统计数据
      request.get('/user/list').then(res => { if(res) stats.users = res.length }).catch(()=>{})
      request.get('/lost-found/list').then(res => { if(res) stats.lostfounds = res.length }).catch(()=>{})
      request.get('/repair/list').then(res => { if(res) stats.repairs = res.length }).catch(()=>{})
    }

    // ================= 维修工专属逻辑 =================
    if (userRole.value === 2) {
      // 拉取真实的工单列表
      request.get('/repair/list').then(res => {
        if (res && res.length > 0) {
          // 提取待处理工单 (假设 status: 0 是待处理)
          const pending = res.filter(item => item.status === 0)
          stats.workerPending = pending.length
          stats.workerProcessing = res.filter(item => item.status === 1).length
          stats.workerFinished = res.filter(item => item.status === 2 || item.status === 3).length

          // 填充最新的紧急待办表格
          workerPendingList.value = pending.map(item => ({
            location: item.dormLocation || '未填写地址',
            title: item.title || item.description,
            time: item.createTime ? item.createTime.substring(0, 16).replace('T', ' ') : ''
          })).slice(0, 5)
        }
      }).catch(()=>{})
    }

    // ================= 学生专属逻辑 =================
    if (userRole.value === 1) {
          // 获取当前学生的 ID，如果没有则默认用测试账号的 1001
          const studentId = userInfo.id || 1001
          // 假设当前是第 5 周 (你可以根据实际情况改成动态计算)
          const currentWeek = 5 
    
          // ⚠️ 修复点：调用真实的 /course/weekly 接口，并带上必须的参数
          request.get(`/course/weekly?studentId=${studentId}&week=${currentWeek}`).then(res => {
            if (res && res.length > 0) {
              studentSchedule.value = res.map(item => ({
                 period: item.period || '待定',
                 name: item.courseName || item.name || '未知课程',
                 loc: item.location || '待定'
              })).slice(0, 4)
            }
          }).catch(()=>{})
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
/* 样式保持不变 */
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

.nav-card, .chart-card, .timeline-card, .table-card { border-radius: 10px; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.03); }
.header-title { font-weight: bold; font-size: 16px; color: #333; }
.quick-navs { display: flex; justify-content: space-around; padding: 15px 0; }
.nav-item { display: flex; flex-direction: column; align-items: center; cursor: pointer; transition: transform 0.2s; }
.nav-item:hover { transform: translateY(-3px); }
.nav-icon { width: 56px; height: 56px; border-radius: 16px; display: flex; justify-content: center; align-items: center; font-size: 26px; margin-bottom: 12px; transition: all 0.3s; }
.nav-item span { font-size: 13px; color: #606266; font-weight: 500; }

.sys-monitor { padding: 10px; }
.monitor-item { margin-bottom: 25px; }
.monitor-header { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 13px; color: #606266; font-weight: 500;}

.recent-list { display: flex; flex-direction: column; gap: 18px; padding: 10px 0; }
.recent-item { display: flex; align-items: center; cursor: pointer; transition: color 0.2s; }
.recent-item:hover .act-title { color: #409eff; }
.act-title { font-size: 14px; color: #303133; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; flex: 1; line-height: 1.4;}

:deep(.custom-todo .el-checkbox__label) { color: #606266 !important; }
:deep(.custom-todo .is-checked .el-checkbox__label) { color: #c0c4cc !important; text-decoration: line-through; }
</style>