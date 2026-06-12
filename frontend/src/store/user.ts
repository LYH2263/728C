import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types'
import { authApi, userApi } from '@/api'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string | null>(null)
  const userInfo = ref<User | null>(null)
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  
  // 登录
  async function login(username: string, password: string) {
    const res = await authApi.login({ username, password })
    const data = res.data.data
    token.value = data.token
    userInfo.value = data.userInfo
    return data
  }
  
  // 注册
  async function register(data: { username: string; password: string; email?: string; nickname?: string }) {
    const res = await authApi.register(data)
    const result = res.data.data
    token.value = result.token
    userInfo.value = result.userInfo
    return result
  }
  
  // 登出
  function logout() {
    token.value = null
    userInfo.value = null
  }
  
  // 获取用户信息
  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const res = await userApi.getProfile()
      userInfo.value = res.data.data
    } catch (error) {
      logout()
    }
  }
  
  // 更新用户信息
  async function updateUserInfo(data: Partial<User>) {
    const res = await userApi.updateProfile(data)
    userInfo.value = res.data.data
    return res.data.data
  }
  
  // 充值
  async function recharge(amount: number) {
    await userApi.recharge(amount)
    await fetchUserInfo()
  }
  
  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    login,
    register,
    logout,
    fetchUserInfo,
    updateUserInfo,
    recharge
  }
}, {
  persist: {
    key: 'steam-user',
    paths: ['token', 'userInfo']
  }
})
