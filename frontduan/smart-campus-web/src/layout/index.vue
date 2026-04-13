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
        <el-menu-item index="/activity">
		  <el-icon><Calendar /></el-icon>
          <template #title>活动中心</template>
        </el-menu-item>
		<el-menu-item index="/classroom">
		  <el-icon><Search /></el-icon>
		  <template #title>空闲教室</template>
		</el-menu-item>
        <el-menu-item index="/repair">
          <el-icon><Wrench /></el-icon>
          <template #title>宿舍报修</template>
        </el-menu-item>
		<el-menu-item index="/course">
		  <el-icon><Calendar /></el-icon>
		  <template #title>我的课表</template>
		</el-menu-item>
      </el-menu>
      
      <div class="bottom-action">
        <el-button circle icon="SwitchButton" @click="handleLogout" type="danger" plain/>
      </div>
    </div>

    <div class="main-container">
      <div class="top-header">
        <h2 class="page-title">智慧校园大厅</h2>
        <div class="user-info">
          <el-avatar size="small" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
          <span class="username">{{ userName }}</span>
        </div>
      </div>
      
      <div class="content-body">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
// 从 localStorage 中取出登录时存的用户信息
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const userName = ref(userInfo.realName || '同学')

// 退出登录逻辑
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  })
}
</script>

<style scoped>
/* 整个屏幕的软渐变背景 */
.app-wrapper {
  display: flex;
  height: 100vh;
  background: #f0f4f8; /* 柔和的浅蓝灰色 */
  padding: 20px; /* 核心：四周留白，打破边界 */
  box-sizing: border-box;
  gap: 20px;
}

/* 侧边栏：圆润悬浮效果 */
.sidebar-capsule {
  width: 80px;
  background: white;
  border-radius: 20px; /* 胶囊圆角 */
  box-shadow: 0 8px 24px rgba(149, 157, 165, 0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.logo {
  font-size: 28px;
  margin-bottom: 30px;
}

/* 去掉 element-plus 默认的边框，让菜单更干净 */
.floating-menu {
  border-right: none;
  flex: 1;
  width: 100%;
}

.bottom-action {
  margin-top: auto;
}

/* 右侧主体容器 */
.main-container {
  flex: 1;
  background: white;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(149, 157, 165, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.top-header {
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  border-bottom: 1px solid #f0f0f0;
}

.page-title {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: bold;
  color: #555;
}

.content-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #fafbfc;
}
</style>