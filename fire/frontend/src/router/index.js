import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import RegisterView from '../views/RegisterView.vue'
import DashboardView from '../views/DashboardView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
      meta: { requiresAuth: true }
    }
  ]
})

router.beforeEach((to, from, next) => {
  const isAuthenticated = !!localStorage.getItem('userId')
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/')
  } else {
    next()
  }
})

export default router 