import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'
import gameRoutes from './modules/game'
import userRoutes from './modules/user'
import orderRoutes from './modules/order'

const baseRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面未找到' }
  }
]

const routes: RouteRecordRaw[] = [
  ...baseRoutes.filter(r => r.path === '/'),
  ...gameRoutes,
  ...userRoutes,
  ...orderRoutes,
  ...baseRoutes.filter(r => r.path !== '/')
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || 'Steam'} - Steam游戏商城`

  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.path } })
  } else {
    next()
  }
})

export default router
