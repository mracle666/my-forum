import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('@/views/Home.vue'),
    },
    {
      path: '/post/:id',
      name: 'PostDetail',
      component: () => import('@/views/PostDetail.vue'),
    },
    {
      path: '/create',
      name: 'PostCreate',
      component: () => import('@/views/PostCreate.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/edit/:id',
      name: 'PostEdit',
      component: () => import('@/views/PostCreate.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/notifications',
      name: 'Notifications',
      component: () => import('@/views/Notifications.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/my-favorites',
      name: 'MyFavorites',
      component: () => import('@/views/MyFavorites.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/my-posts',
      name: 'MyPosts',
      component: () => import('@/views/MyPosts.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue'),
    },
    {
      path: '/user/:id',
      name: 'UserProfile',
      component: () => import('@/views/UserProfile.vue'),
    },
    {
      path: '/messages',
      name: 'Messages',
      component: () => import('@/views/Messages.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/settings',
      name: 'Settings',
      component: () => import('@/views/Settings.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin',
      name: 'Admin',
      component: () => import('@/views/admin/AdminDashboard.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const userInfo = localStorage.getItem('userInfo')
  let role = 0
  if (userInfo) {
    try {
      role = JSON.parse(userInfo).role || 0
    } catch {}
  }

  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.requiresAdmin && role !== 1) {
    next({ name: 'Home' })
    return
  }

  next()
})

export default router
