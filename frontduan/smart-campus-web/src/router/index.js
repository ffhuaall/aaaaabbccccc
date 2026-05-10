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
      path: '/',
      component: () => import('@/layout/index.vue'),
      redirect: '/dashboard', //默认重定向到数据大屏
      children: [
        {
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
          path: 'lost-found',
          name: 'LostFound',
          component: () => import('@/views/LostFound.vue')
        },
		{
		  path: 'admin/repair',
		  name: 'AdminRepair',
		  component: () => import('@/views/admin/AdminRepair.vue')
	    },
		{
		  path: 'admin/activity',
		  name: 'AdminActivity',
		  component: () => import('@/views/admin/AdminActivity.vue')
	    },
		{
          path: 'admin/user',
          name: 'UserManage',
          component: () => import('@/views/admin/UserManage.vue')
        },
		{
		  path: 'admin/notice',
		  name: 'NoticeManage',
		  component: () => import('@/views/admin/NoticeManage.vue')
		},
		{
		  path: 'personal',
		  name: 'Personal',
		  component: () => import('@/views/PersonalCenter.vue')
		},
		{
		  path: 'admin/repair-all',
		  name: 'SuperRepairManage',
		  component: () => import('@/views/admin/SuperRepairManage.vue')
		},
		{
		  path: 'admin/lost-found-manage', //访问路径/admin/lost-found-manage
		  name: 'SuperLostFoundManage',
		  component: () => import('@/views/admin/SuperLostFoundManage.vue'),
		},
		{
		  path: 'admin/super-activity',
		  name: 'SuperActivityManage',
		  component: () => import('@/views/admin/SuperActivityManage.vue'),
		}
      ]
    }
  ]
})

//路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router