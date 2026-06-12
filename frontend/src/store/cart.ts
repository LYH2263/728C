import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { CartItem } from '@/types'
import { cartApi } from '@/api'
import { ElMessage } from 'element-plus'

export const useCartStore = defineStore('cart', () => {
  // 状态
  const items = ref<CartItem[]>([])
  const loading = ref(false)
  
  // 计算属性
  const count = computed(() => items.value.length)
  
  const totalAmount = computed(() => {
    return items.value.reduce((sum, item) => {
      const price = item.game.discountPrice ?? item.game.originalPrice
      return sum + price * item.quantity
    }, 0)
  })
  
  const originalAmount = computed(() => {
    return items.value.reduce((sum, item) => {
      return sum + item.game.originalPrice * item.quantity
    }, 0)
  })
  
  const discountAmount = computed(() => {
    return originalAmount.value - totalAmount.value
  })
  
  // 获取购物车
  async function fetchCart() {
    loading.value = true
    try {
      const res = await cartApi.getCart()
      items.value = res.data.data
    } catch (error) {
      items.value = []
    } finally {
      loading.value = false
    }
  }
  
  // 添加到购物车
  async function addToCart(gameId: number) {
    try {
      await cartApi.addToCart(gameId)
      ElMessage.success('已添加到购物车')
      await fetchCart()
    } catch (error: any) {
      // 错误已在拦截器处理
    }
  }
  
  // 从购物车移除
  async function removeFromCart(gameId: number) {
    try {
      await cartApi.removeFromCart(gameId)
      ElMessage.success('已从购物车移除')
      await fetchCart()
    } catch (error: any) {
      // 错误已在拦截器处理
    }
  }
  
  // 清空购物车
  async function clearCart() {
    try {
      await cartApi.clearCart()
      items.value = []
      ElMessage.success('购物车已清空')
    } catch (error: any) {
      // 错误已在拦截器处理
    }
  }
  
  // 检查游戏是否在购物车中
  function isInCart(gameId: number) {
    return items.value.some(item => item.gameId === gameId)
  }
  
  return {
    items,
    loading,
    count,
    totalAmount,
    originalAmount,
    discountAmount,
    fetchCart,
    addToCart,
    removeFromCart,
    clearCart,
    isInCart
  }
})
