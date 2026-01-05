import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import OwnerDashboard from '../views/OwnerDashboard.vue'
import DispatcherDashboard from '../views/DispatcherDashboard.vue'
import DriverDashboard from '../views/DriverDashboard.vue'
import AdminDashboard from '../views/AdminDashboard.vue'
import { getToken } from '../utils/auth'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView
  },
  {
    path: '/owner',
    name: 'owner',
    component: OwnerDashboard,
    meta: { requiresAuth: true, role: 'consignor' }
  },
  {
    path: '/dispatcher',
    name: 'dispatcher',
    component: DispatcherDashboard,
    meta: { requiresAuth: true, role: 'dispatcher' }
  },
  {
    path: '/driver',
    name: 'driver',
    component: DriverDashboard,
    meta: { requiresAuth: true, role: 'driver' }
  },
  {
    path: '/admin',
    name: 'admin',
    component: AdminDashboard,
    meta: { requiresAuth: true, role: 'admin' }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = getToken()
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
