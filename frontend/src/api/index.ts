import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types'
import { useUserStore } from '@/store/user'

// 创建axios实例
const api: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        const userStore = useUserStore()
        userStore.logout()
        ElMessage.error('登录已过期，请重新登录')
        window.location.href = '/login'
      } else if (status === 403) {
        ElMessage.error('没有权限访问')
      } else if (status === 500) {
        ElMessage.error('服务器错误')
      } else {
        ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default api

// 认证相关
export const authApi = {
  login: (data: { username: string; password: string }) =>
    api.post('/auth/login', data),
  register: (data: { username: string; password: string; email?: string; nickname?: string }) =>
    api.post('/auth/register', data)
}

// 用户相关
export const userApi = {
  getProfile: () => api.get('/user/profile'),
  updateProfile: (data: Partial<{ nickname: string; email: string; avatar: string }>) =>
    api.put('/user/profile', data),
  recharge: (amount: number) => api.post('/user/recharge', { amount })
}

// 游戏相关
export const gameApi = {
  getGame: (id: number) => api.get(`/games/${id}`),
  searchGames: (params: Record<string, any>, signal?: AbortSignal) =>
    api.get('/games/search', { params, signal }),
  getHomeData: () => api.get('/games/home'),
  getFeatured: (limit?: number) => api.get('/games/featured', { params: { limit } }),
  getOnSale: (limit?: number) => api.get('/games/on-sale', { params: { limit } }),
  getBestSellers: (limit?: number) => api.get('/games/best-sellers', { params: { limit } }),
  getNewReleases: (limit?: number) => api.get('/games/new-releases', { params: { limit } }),
  getCategories: (gameId: number) => api.get(`/games/${gameId}/categories`)
}

// 分类相关
export const categoryApi = {
  getAll: () => api.get('/categories')
}

// 购物车相关
export const cartApi = {
  getCart: () => api.get('/cart'),
  addToCart: (gameId: number) => api.post('/cart', { gameId }),
  removeFromCart: (gameId: number) => api.delete(`/cart/${gameId}`),
  clearCart: () => api.delete('/cart'),
  getCartCount: () => api.get('/cart/count')
}

// 愿望单相关
export const wishlistApi = {
  getWishlist: () => api.get('/wishlist'),
  addToWishlist: (gameId: number) => api.post('/wishlist', { gameId }),
  removeFromWishlist: (gameId: number) => api.delete(`/wishlist/${gameId}`),
  checkWishlist: (gameId: number) => api.get(`/wishlist/check/${gameId}`)
}

// 订单相关
export const orderApi = {
  createOrder: (gameIds: number[]) => api.post('/orders', { gameIds }),
  payOrder: (orderNo: string) => api.post(`/orders/${orderNo}/pay`),
  cancelOrder: (orderNo: string) => api.post(`/orders/${orderNo}/cancel`),
  getOrders: () => api.get('/orders'),
  getOrderDetail: (orderNo: string) => api.get(`/orders/${orderNo}`)
}

// 游戏库相关
export const libraryApi = {
  getLibrary: () => api.get('/library'),
  checkOwnership: (gameId: number) => api.get(`/library/check/${gameId}`),
  getLibraryCount: () => api.get('/library/count')
}

// 评论相关
export const reviewApi = {
  getGameReviews: (gameId: number, page: number = 1, size: number = 10) =>
    api.get(`/reviews/game/${gameId}`, { params: { page, size } }),
  createReview: (data: { gameId: number; rating: number; content: string; isRecommend?: boolean }) =>
    api.post('/reviews', data),
  markHelpful: (reviewId: number) => api.post(`/reviews/${reviewId}/helpful`)
}
