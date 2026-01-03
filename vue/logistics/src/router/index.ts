import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import OwnerDashboard from '../views/OwnerDashboard.vue'
import DispatcherDashboard from '../views/DispatcherDashboard.vue'
import DriverDashboard from '../views/DriverDashboard.vue'
import AdminDashboard from '../views/AdminDashboard.vue'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/owner',
    name: 'owner',
    component: OwnerDashboard
  },
  {
    path: '/dispatcher',
    name: 'dispatcher',
    component: DispatcherDashboard
  },
  {
    path: '/driver',
    name: 'driver',
    component: DriverDashboard
  },
  {
    path: '/admin',
    name: 'admin',
    component: AdminDashboard
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
