import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue')
    },
    {
      // 这里的 Layout 就是我们所有业务页面的“公共悬浮外壳”
      path: '/',
      component: () => import('@/layout/index.vue'),
      redirect: '/dashboard', // 默认重定向到数据大屏
      children: [
        {
          // 具体的数据大屏页面，会渲染在 Layout 的 <router-view> 里
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue')
        },
		{
		  path: 'activity',
		  name: 'Activity',
		  component: () => import('@/views/Activity.vue')
		},
		{
		  path: 'classroom',
		  name: 'Classroom',
		  component: () => import('@/views/Classroom.vue')
		},
		{
		  path: 'repair',
		  name: 'Repair',
		  component: () => import('@/views/Repair.vue')
		},
		{
		  path: 'course',
		  name: 'Course',
		  component: () => import('@/views/Course.vue')
	    },
		{
		  path: 'admin/repair',
		  name: 'AdminRepair',
		  component: () => import('@/views/admin/AdminRepair.vue')
	    }
        // 后续的 activity, repair, course 等页面都会继续往这里面加
      ]
    }
  ]
})

// 路由守卫：没登录不许进
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router