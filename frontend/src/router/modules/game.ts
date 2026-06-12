import type { RouteRecordRaw } from 'vue-router'

const gameRoutes: RouteRecordRaw[] = [
  {
    path: '/store',
    name: 'Store',
    component: () => import('@/views/Store.vue'),
    meta: { title: '商店' }
  },
  {
    path: '/game/:id',
    name: 'GameDetail',
    component: () => import('@/views/GameDetail.vue'),
    meta: { title: '游戏详情' }
  },
  {
    path: '/category/:category',
    name: 'Category',
    component: () => import('@/views/Store.vue'),
    meta: { title: '分类浏览' }
  }
]

export default gameRoutes
