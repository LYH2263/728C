<template>
  <div class="cart-page">
    <h1 class="page-title">
      <el-icon><ShoppingCart /></el-icon>
      我的购物车
    </h1>
    
    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>
    
    <template v-else>
      <div v-if="cartStore.items.length" class="cart-content">
        <!-- 购物车列表 -->
        <div class="cart-list">
          <div v-for="item in cartStore.items" :key="item.id" class="cart-item">
            <img 
              :src="item.game.coverImage" 
              :alt="item.game.title" 
              class="game-cover"
              @click="goToGame(item.gameId)"
            />
            <div class="item-info">
              <h3 @click="goToGame(item.gameId)">{{ item.game.title }}</h3>
              <div class="item-price">
                <template v-if="item.game.discountPercent">
                  <span class="discount-tag">-{{ item.game.discountPercent }}%</span>
                  <span class="original-price">¥{{ item.game.originalPrice }}</span>
                  <span class="final-price">¥{{ item.game.discountPrice }}</span>
                </template>
                <template v-else>
                  <span class="final-price">¥{{ item.game.originalPrice }}</span>
                </template>
              </div>
            </div>
            <el-button text type="danger" @click="handleRemove(item.gameId)">
              <el-icon><Delete /></el-icon>
              移除
            </el-button>
          </div>
        </div>
        
        <!-- 结算信息 -->
        <div class="checkout-card">
          <h3>订单摘要</h3>
          <div class="summary-row">
            <span>商品总价</span>
            <span>¥{{ cartStore.originalAmount.toFixed(2) }}</span>
          </div>
          <div v-if="cartStore.discountAmount > 0" class="summary-row discount">
            <span>优惠</span>
            <span>-¥{{ cartStore.discountAmount.toFixed(2) }}</span>
          </div>
          <el-divider />
          <div class="summary-row total">
            <span>应付金额</span>
            <span>¥{{ cartStore.totalAmount.toFixed(2) }}</span>
          </div>
          <el-button type="success" size="large" style="width: 100%" @click="handleCheckout">
            结算 ({{ cartStore.count }}件商品)
          </el-button>
          <el-button text style="width: 100%; margin-left:0; margin-top: 8px" @click="handleClear">
            清空购物车
          </el-button>
        </div>
      </div>
      
      <el-empty v-else description="购物车是空的">
        <router-link to="/store">
          <el-button type="primary">去选购</el-button>
        </router-link>
      </el-empty>
    </template>
    
    <!-- 结算对话框 -->
    <el-dialog v-model="showCheckout" title="确认订单" width="500px">
      <div class="checkout-dialog">
        <div class="order-items">
          <div v-for="item in cartStore.items" :key="item.id" class="order-item">
            <img :src="item.game.coverImage" :alt="item.game.title" />
            <span>{{ item.game.title }}</span>
            <span class="price">¥{{ (item.game.discountPrice ?? item.game.originalPrice).toFixed(2) }}</span>
          </div>
        </div>
        <el-divider />
        <div class="order-total">
          <span>应付金额：</span>
          <span class="amount">¥{{ cartStore.totalAmount.toFixed(2) }}</span>
        </div>
        <div class="balance-info">
          <span>账户余额：</span>
          <span :class="{ insufficient: userBalance < cartStore.totalAmount }">
            ¥{{ userBalance.toFixed(2) }}
          </span>
        </div>
        <el-alert 
          v-if="userBalance < cartStore.totalAmount" 
          type="warning" 
          :closable="false"
          style="margin-top: 16px"
        >
          余额不足，请先充值
        </el-alert>
      </div>
      <template #footer>
        <el-button @click="showCheckout = false">取消</el-button>
        <el-button 
          v-if="userBalance >= cartStore.totalAmount"
          type="primary" 
          :loading="checkoutLoading"
          @click="confirmCheckout"
        >
          确认支付
        </el-button>
        <el-button v-else type="warning" @click="goToRecharge">去充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/store/cart'
import { useUserStore } from '@/store/user'
import { orderApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const loading = ref(true)
const showCheckout = ref(false)
const checkoutLoading = ref(false)

const userBalance = computed(() => userStore.userInfo?.balance || 0)

onMounted(async () => {
  await cartStore.fetchCart()
  loading.value = false
})

function goToGame(gameId: number) {
  router.push(`/game/${gameId}`)
}

async function handleRemove(gameId: number) {
  await cartStore.removeFromCart(gameId)
}

async function handleClear() {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cartStore.clearCart()
  } catch {
    // 取消
  }
}

function handleCheckout() {
  showCheckout.value = true
}

async function confirmCheckout() {
  checkoutLoading.value = true
  try {
    const gameIds = cartStore.items.map(item => item.gameId)
    const res = await orderApi.createOrder(gameIds)
    const order = res.data.data
    
    // 支付订单
    await orderApi.payOrder(order.orderNo)
    
    ElMessage.success('支付成功！游戏已添加到您的游戏库')
    showCheckout.value = false
    
    // 刷新数据
    await Promise.all([
      cartStore.fetchCart(),
      userStore.fetchUserInfo()
    ])
    
    // 跳转到游戏库
    router.push('/library')
  } catch (error) {
    // 错误已处理
  } finally {
    checkoutLoading.value = false
  }
}

function goToRecharge() {
  showCheckout.value = false
  router.push('/profile')
}
</script>

<style lang="scss" scoped>
.cart-page {
  max-width: 1000px;
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

.cart-content {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
}

.cart-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  
  .game-cover {
    width: 120px;
    height: 56px;
    object-fit: cover;
    border-radius: var(--radius-sm);
    cursor: pointer;
    transition: opacity 0.3s;
    
    &:hover {
      opacity: 0.8;
    }
  }
  
  .item-info {
    flex: 1;
    
    h3 {
      font-size: 16px;
      color: var(--text-white);
      margin-bottom: 8px;
      cursor: pointer;
      
      &:hover {
        color: var(--steam-light-blue);
      }
    }
  }
  
  .item-price {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .discount-tag {
      background: var(--steam-green);
      color: var(--steam-darker);
      padding: 2px 6px;
      border-radius: var(--radius-sm);
      font-weight: 600;
      font-size: 12px;
    }
    
    .original-price {
      color: var(--text-secondary);
      text-decoration: line-through;
      font-size: 14px;
    }
    
    .final-price {
      color: var(--text-primary);
      font-weight: 600;
    }
  }
}

.checkout-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 24px;
  border: 1px solid var(--border-color);
  height: fit-content;
  position: sticky;
  top: 84px;
  
  h3 {
    font-size: 18px;
    color: var(--text-white);
    margin-bottom: 16px;
  }
  
  .summary-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12px;
    color: var(--text-primary);
    
    &.discount {
      color: var(--steam-green);
    }
    
    &.total {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-white);
    }
  }
}

// 结算对话框
.checkout-dialog {
  .order-items {
    max-height: 300px;
    overflow-y: auto;
  }
  
  .order-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 8px 0;
    
    img {
      width: 60px;
      height: 28px;
      object-fit: cover;
      border-radius: var(--radius-sm);
    }
    
    span:first-of-type {
      flex: 1;
      color: var(--text-primary);
    }
    
    .price {
      color: var(--text-white);
      font-weight: 500;
    }
  }
  
  .order-total, .balance-info {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    
    span:first-child {
      color: var(--text-secondary);
    }
  }
  
  .order-total .amount {
    font-size: 20px;
    font-weight: 600;
    color: var(--steam-green);
  }
  
  .insufficient {
    color: var(--el-color-danger);
  }
}

@media (max-width: 768px) {
  .cart-content {
    grid-template-columns: 1fr;
  }
  
  .checkout-card {
    position: static;
  }
  
  .cart-item {
    flex-wrap: wrap;
    
    .game-cover {
      width: 80px;
      height: 37px;
    }
  }
}
</style>
