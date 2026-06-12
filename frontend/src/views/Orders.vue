<template>
  <div class="orders-page">
    <h1 class="page-title">
      <el-icon><Document /></el-icon>
      我的订单
    </h1>
    
    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>
    
    <template v-else>
      <div v-if="orders.length" class="orders-list">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-header">
            <div class="order-info">
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <span class="order-time">{{ formatDate(order.createdAt) }}</span>
            </div>
            <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
          </div>
          
          <div class="order-items">
            <div v-for="item in order.orderItems" :key="item.id" class="order-item">
              <img :src="item.gameCover" :alt="item.gameTitle" @click="goToGame(item.gameId)" />
              <div class="item-info">
                <h4 @click="goToGame(item.gameId)">{{ item.gameTitle }}</h4>
                <span class="price">¥{{ item.price.toFixed(2) }}</span>
              </div>
            </div>
          </div>
          
          <div class="order-footer">
            <div class="order-total">
              <span v-if="order.discountAmount && order.discountAmount > 0" class="discount">
                已优惠 ¥{{ order.discountAmount.toFixed(2) }}
              </span>
              <span>实付：<strong>¥{{ order.payAmount.toFixed(2) }}</strong></span>
            </div>
            <div class="order-actions" v-if="order.status === 'PENDING'">
              <el-button type="primary" size="small" @click="handlePay(order.orderNo)">去支付</el-button>
              <el-button size="small" @click="handleCancel(order.orderNo)">取消订单</el-button>
            </div>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="暂无订单">
        <router-link to="/store">
          <el-button type="primary">去选购</el-button>
        </router-link>
      </el-empty>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { orderApi } from '@/api'
import { useUserStore } from '@/store/user'
import type { Order } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const orders = ref<Order[]>([])

onMounted(async () => {
  await fetchOrders()
  loading.value = false
})

async function fetchOrders() {
  try {
    const res = await orderApi.getOrders()
    orders.value = res.data.data || []
  } catch (error) {
    orders.value = []
  }
}

function goToGame(gameId: number) {
  router.push(`/game/${gameId}`)
}

function formatDate(date: string) {
  return new Date(date).toLocaleString('zh-CN')
}

function getStatusType(status: string) {
  const types: Record<string, string> = {
    PENDING: 'warning',
    PAID: 'success',
    CANCELLED: 'info',
    COMPLETED: 'success'
  }
  return types[status] || 'info'
}

function getStatusText(status: string) {
  const texts: Record<string, string> = {
    PENDING: '待支付',
    PAID: '已支付',
    CANCELLED: '已取消',
    COMPLETED: '已完成'
  }
  return texts[status] || status
}

async function handlePay(orderNo: string) {
  const order = orders.value.find(o => o.orderNo === orderNo)
  if (!order) return
  
  const balance = userStore.userInfo?.balance || 0
  if (balance < order.payAmount) {
    ElMessage.warning('余额不足，请先充值')
    router.push('/profile')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确认支付 ¥${order.payAmount.toFixed(2)}？`, '确认支付', {
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    
    await orderApi.payOrder(orderNo)
    ElMessage.success('支付成功')
    await Promise.all([fetchOrders(), userStore.fetchUserInfo()])
  } catch (error) {
    // 取消或错误
  }
}

async function handleCancel(orderNo: string) {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await orderApi.cancelOrder(orderNo)
    ElMessage.success('订单已取消')
    await fetchOrders()
  } catch (error) {
    // 取消
  }
}
</script>

<style lang="scss" scoped>
.orders-page {
  max-width: 900px;
  margin: 0 auto;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  color: var(--text-white);
  margin-bottom: 24px;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: rgba(0, 0, 0, 0.2);
  
  .order-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
    
    .order-no {
      color: var(--text-primary);
      font-size: 14px;
    }
    
    .order-time {
      color: var(--text-secondary);
      font-size: 12px;
    }
  }
}

.order-items {
  padding: 16px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  
  &:not(:last-child) {
    border-bottom: 1px solid var(--border-color);
  }
  
  img {
    width: 80px;
    height: 37px;
    object-fit: cover;
    border-radius: var(--radius-sm);
    cursor: pointer;
  }
  
  .item-info {
    flex: 1;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    h4 {
      font-size: 14px;
      color: var(--text-white);
      cursor: pointer;
      
      &:hover {
        color: var(--steam-light-blue);
      }
    }
    
    .price {
      color: var(--text-primary);
    }
  }
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-top: 1px solid var(--border-color);
  
  .order-total {
    display: flex;
    flex-direction: column;
    gap: 4px;
    
    .discount {
      color: var(--steam-green);
      font-size: 12px;
    }
    
    span {
      color: var(--text-secondary);
      
      strong {
        color: var(--text-white);
        font-size: 18px;
      }
    }
  }
  
  .order-actions {
    display: flex;
    gap: 8px;
  }
}

@media (max-width: 600px) {
  .order-footer {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
    
    .order-actions {
      width: 100%;
      
      .el-button {
        flex: 1;
      }
    }
  }
}
</style>
