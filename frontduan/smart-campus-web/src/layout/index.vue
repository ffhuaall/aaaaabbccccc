<template>
  <div class="app-wrapper">
    <div class="sidebar-capsule">
      <div class="logo">✨</div>
      <el-menu
        default-active="/dashboard"
        class="floating-menu"
        :router="true"
        :collapse="true"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <template #title>数据大屏</template>
        </el-menu-item>
        
        <template v-if="!userInfo.roleId || userInfo.roleId === 1">
          <el-menu-item index="/activity">
            <el-icon><Calendar /></el-icon>
            <template #title>活动中心</template>
          </el-menu-item>
          <el-menu-item index="/classroom">
            <el-icon><Search /></el-icon>
            <template #title>空闲教室</template>
          </el-menu-item>
          <el-menu-item index="/course">
            <el-icon><Calendar /></el-icon>
            <template #title>我的课表</template>
          </el-menu-item>
          <el-menu-item index="/repair">
            <el-icon><Service /></el-icon>
            <template #title>宿舍报修</template>
          </el-menu-item>
		  <el-menu-item index="/lost-found">
            <el-icon><Notification /></el-icon>
            <template #title>失物招领</template>
          </el-menu-item>
        </template>

        <template v-if="userInfo.roleId === 2">
          <el-menu-item index="/admin/repair">
            <el-icon><Tools /></el-icon>
            <template #title>工单处理中心</template>
          </el-menu-item>
        </template>	

        <template v-if="userInfo.roleId === 3">
          <el-menu-item index="/admin/activity">
            <el-icon><Collection /></el-icon>
            <template #title>活动发布管理</template>
          </el-menu-item>
          <el-menu-item index="/activity">
            <el-icon><Monitor /></el-icon>
            <template #title>大厅效果预览</template>
          </el-menu-item>
        </template>

        <template v-if="userInfo.roleId === 4">
          <el-menu-item index="/admin/user">
            <el-icon><UserFilled /></el-icon>
            <template #title>用户与权限管理</template>
          </el-menu-item>
		  <el-menu-item index="/admin/notice">
		    <el-icon><Notification /></el-icon>
		    <template #title>系统公告管理</template>
		  </el-menu-item>
          <el-menu-item index="/dashboard">
            <el-icon><DataLine /></el-icon>
            <template #title>全站数据大屏</template>
          </el-menu-item>
		  <el-menu-item index="/admin/repair-all">
		    <el-icon><Tools /></el-icon>
		    <template #title>宿舍报修管理</template>
		  </el-menu-item>
        </template>
      </el-menu>   
      <div class="bottom-action">
        <el-button circle icon="SwitchButton" @click="handleLogout" type="danger" plain/>
      </div>
    </div>

    <div class="main-container">
      <el-alert
        v-if="latestNotice"
        :title="latestNotice.title + '：' + latestNotice.content"
        :type="latestNotice.level"
        center
        show-icon
        closable
        class="system-notice-banner"
      />

      <div class="top-header">
        <h2 class="page-title">智慧校园大厅</h2>
        
        <div class="header-right">
          <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0" class="msg-badge">
            <el-icon class="bell-icon" @click="openMessageDrawer"><Bell /></el-icon>
          </el-badge>
          
          <div class="user-info">
            <el-dropdown trigger="click">
              <div style="display: flex; align-items: center; cursor: pointer; gap: 10px;">
                <el-avatar size="small" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
                <span class="username">{{ userName }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item icon="User" @click="router.push('/personal')">个人中心</el-dropdown-item>
                  <el-dropdown-item icon="SwitchButton" divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
      
      <div class="content-body">
        <router-view />
      </div>
    </div>
  </div>
  
  <el-drawer v-model="drawerVisible" title="我的消息中心" size="400px">
    <div style="margin-bottom: 15px; text-align: right;">
      <el-button type="primary" link @click="handleReadAll" :disabled="unreadCount === 0">全部标为已读</el-button>
    </div>
    
    <div v-if="messageList.length === 0" style="text-align: center; color: #999; margin-top: 50px;">
      <el-empty description="暂无消息" :image-size="100" />
    </div>

    <div class="msg-item" v-for="msg in messageList" :key="msg.id" :class="{'is-unread': msg.isRead === 0}">
      <div class="msg-header">
        <el-tag size="small" :type="msg.type === 'ACTIVITY' ? 'warning' : 'info'">{{ msg.type === 'ACTIVITY' ? '活动通知' : '系统通知' }}</el-tag>
        <span class="msg-time">{{ msg.createTime }}</span>
      </div>
      <div class="msg-title">{{ msg.title }}</div>
      <div class="msg-content">{{ msg.content }}</div>
      <div class="msg-action" v-if="msg.isRead === 0">
        <el-button type="primary" link size="small" @click="handleRead(msg.id)">标为已读</el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const userName = ref(userInfo.realName || '同学')

// ======== 消息与公告状态 ========
const unreadCount = ref(0)
const drawerVisible = ref(false)
const messageList = ref([])
const latestNotice = ref(null) // 【新增】存储最新的公告内容
let timer = null 

// 获取最新公告
const fetchLatestNotice = async () => {
  try {
    const res = await request.get('/notice/latest')
    latestNotice.value = res // 如果后端返回 null，则横幅自动消失
  } catch (e) {
    console.error("公告获取失败")
  }
}

// 获取未读消息数
const fetchUnreadCount = async () => {
  const currentUserId = userInfo.id || 1001
  try {
    const res = await request.get(`/message/unread-count?userId=${currentUserId}`)
    unreadCount.value = res || 0
  } catch (e) {
    console.error("刷新消息失败")
  }
}

// 轮询刷新：每 30 秒检查一次公告和未读消息
const startPolling = () => {
  timer = setInterval(() => {
    fetchUnreadCount()
    fetchLatestNotice() // 【同步轮询】确保公告也能实时更新
  }, 30000)
}

const openMessageDrawer = async () => {
  drawerVisible.value = true
  const currentUserId = userInfo.id || 1001
  const res = await request.get(`/message/list?userId=${currentUserId}`)
  messageList.value = res || []
}

const handleRead = async (id) => {
  await request.post(`/message/read/${id}`)
  fetchUnreadCount()
  openMessageDrawer()
}

const handleReadAll = async () => {
  const currentUserId = userInfo.id || 1001
  await request.post(`/message/read-all?userId=${currentUserId}`)
  fetchUnreadCount()
  openMessageDrawer()
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' }).then(() => {
    if (timer) clearInterval(timer)
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  })
}

onMounted(() => {
  fetchUnreadCount()
  fetchLatestNotice() // 页面初始加载公告
  startPolling()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.app-wrapper {
  display: flex;
  height: 100vh;
  background: #f0f4f8;
  padding: 20px;
  box-sizing: border-box;
  gap: 20px;
}

.sidebar-capsule {
  width: 80px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(149, 157, 165, 0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.logo { font-size: 28px; margin-bottom: 30px; }
.floating-menu { border-right: none; flex: 1; width: 100%; }
.bottom-action { margin-top: auto; }

.main-container {
  flex: 1;
  background: white;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(149, 157, 165, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 公告横幅样式：固定高度且视觉统一 */
.system-notice-banner {
  border-radius: 0;
  border: none;
  font-weight: bold;
}

.top-header {
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  border-bottom: 1px solid #f0f0f0;
}

.page-title { margin: 0; font-size: 18px; color: #333; }
.header-right { display: flex; align-items: center; gap: 25px; }
.user-info { display: flex; align-items: center; gap: 10px; font-weight: bold; color: #555; }
.msg-badge { cursor: pointer; display: flex; align-items: center; }
.bell-icon { font-size: 22px; color: #555; transition: color 0.3s; }
.bell-icon:hover { color: #409EFF; }

.content-body { flex: 1; padding: 20px; overflow-y: auto; background: #fafbfc; }

.msg-item {
  padding: 15px;
  border-radius: 8px;
  background: #f8f9fa;
  margin-bottom: 15px;
  border-left: 4px solid #dcdfe6;
}
.msg-item.is-unread {
  background: #fff;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  border-left-color: #409EFF;
}
.msg-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.msg-time { font-size: 12px; color: #999; }
.msg-title { font-weight: bold; font-size: 15px; color: #333; margin-bottom: 5px; }
.msg-content { font-size: 13px; color: #666; line-height: 1.5; }
.msg-action { text-align: right; margin-top: 10px; }
</style>