<template>
  <div class="profile-page">
    <h1 class="page-title">
      <el-icon><User /></el-icon>
      个人中心
    </h1>
    
    <div class="profile-content">
      <!-- 用户信息卡片 -->
      <div class="profile-card">
        <div class="avatar-section">
          <el-avatar :size="100" :src="getAvatarUrl(userInfo?.avatar)">
            {{ userInfo?.nickname?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="user-meta">
            <h2>{{ userInfo?.nickname || userInfo?.username }}</h2>
            <el-tag v-if="userInfo?.role === 'ADMIN'" type="danger">管理员</el-tag>
            <el-tag v-else>普通用户</el-tag>
          </div>
        </div>
        
        <el-divider />
        
        <div class="info-section">
          <div class="info-item">
            <span class="label">用户名</span>
            <span class="value">{{ userInfo?.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">邮箱</span>
            <span class="value">{{ userInfo?.email || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label">账户余额</span>
            <span class="value balance">¥{{ (userInfo?.balance || 0).toFixed(2) }}</span>
          </div>
        </div>
        
        <div class="action-section">
          <el-button type="primary" @click="showEditDialog = true">
            <el-icon><Edit /></el-icon>
            编辑资料
          </el-button>
          <el-button type="success" @click="showRechargeDialog = true">
            <el-icon><Wallet /></el-icon>
            充值余额
          </el-button>
        </div>
      </div>
      
      <!-- 统计卡片 -->
      <div class="stats-grid">
        <div class="stat-card">
          <el-icon :size="32" color="var(--steam-light-blue)"><Collection /></el-icon>
          <div class="stat-info">
            <span class="stat-value">{{ libraryCount }}</span>
            <span class="stat-label">已拥有游戏</span>
          </div>
        </div>
        <div class="stat-card">
          <el-icon :size="32" color="var(--steam-green)"><ShoppingCart /></el-icon>
          <div class="stat-info">
            <span class="stat-value">{{ cartCount }}</span>
            <span class="stat-label">购物车</span>
          </div>
        </div>
        <div class="stat-card">
          <el-icon :size="32" color="#ffd700"><Star /></el-icon>
          <div class="stat-info">
            <span class="stat-value">{{ wishlistCount }}</span>
            <span class="stat-label">愿望单</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 编辑资料对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑资料" width="400px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleSaveProfile">
          保存
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 充值对话框 -->
    <el-dialog v-model="showRechargeDialog" title="充值余额" width="400px">
      <div class="recharge-options">
        <div 
          v-for="amount in rechargeAmounts" 
          :key="amount"
          class="recharge-option"
          :class="{ active: selectedAmount === amount }"
          @click="selectedAmount = amount"
        >
          ¥{{ amount }}
        </div>
      </div>
      <el-input 
        v-model.number="customAmount" 
        placeholder="自定义金额"
        type="number"
        :min="1"
        style="margin-top: 16px"
        @focus="selectedAmount = 0"
      >
        <template #prepend>¥</template>
      </el-input>
      <template #footer>
        <el-button @click="showRechargeDialog = false">取消</el-button>
        <el-button type="primary" :loading="rechargeLoading" @click="handleRecharge">
          充值 ¥{{ finalAmount }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'
import { libraryApi, wishlistApi } from '@/api'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const cartStore = useCartStore()

const userInfo = computed(() => userStore.userInfo)

// 获取头像URL，处理默认头像
function getAvatarUrl(avatar: string | undefined): string {
  if (!avatar || avatar === '/avatars/default.png') {
    return '/avatars/default.svg'
  }
  return avatar
}

const libraryCount = ref(0)
const wishlistCount = ref(0)
const cartCount = computed(() => cartStore.count)

const showEditDialog = ref(false)
const showRechargeDialog = ref(false)
const editLoading = ref(false)
const rechargeLoading = ref(false)

const editForm = reactive({
  nickname: '',
  email: ''
})

const rechargeAmounts = [50, 100, 200, 500, 1000]
const selectedAmount = ref(100)
const customAmount = ref<number | null>(null)

const finalAmount = computed(() => {
  return selectedAmount.value > 0 ? selectedAmount.value : (customAmount.value || 0)
})

onMounted(async () => {
  editForm.nickname = userInfo.value?.nickname || ''
  editForm.email = userInfo.value?.email || ''
  
  await Promise.all([
    fetchLibraryCount(),
    fetchWishlistCount()
  ])
})

async function fetchLibraryCount() {
  try {
    const res = await libraryApi.getLibraryCount()
    libraryCount.value = res.data.data || 0
  } catch (error) {
    libraryCount.value = 0
  }
}

async function fetchWishlistCount() {
  try {
    const res = await wishlistApi.getWishlist()
    wishlistCount.value = res.data.data?.length || 0
  } catch (error) {
    wishlistCount.value = 0
  }
}

async function handleSaveProfile() {
  editLoading.value = true
  try {
    await userStore.updateUserInfo({
      nickname: editForm.nickname,
      email: editForm.email
    })
    ElMessage.success('资料更新成功')
    showEditDialog.value = false
  } catch (error) {
    // 错误已处理
  } finally {
    editLoading.value = false
  }
}

async function handleRecharge() {
  if (finalAmount.value <= 0) {
    ElMessage.warning('请选择或输入充值金额')
    return
  }
  
  rechargeLoading.value = true
  try {
    await userStore.recharge(finalAmount.value)
    ElMessage.success(`充值成功，当前余额 ¥${userInfo.value?.balance?.toFixed(2)}`)
    showRechargeDialog.value = false
    selectedAmount.value = 100
    customAmount.value = null
  } catch (error) {
    // 错误已处理
  } finally {
    rechargeLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.profile-page {
  max-width: 800px;
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

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 32px;
  border: 1px solid var(--border-color);
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 24px;
  
  .user-meta {
    h2 {
      font-size: 24px;
      color: var(--text-white);
      margin-bottom: 8px;
    }
  }
}

.info-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  
  .info-item {
    display: flex;
    flex-direction: column;
    gap: 4px;
    
    .label {
      color: var(--text-secondary);
      font-size: 14px;
    }
    
    .value {
      color: var(--text-white);
      font-size: 16px;
      
      &.balance {
        color: var(--steam-green);
        font-size: 20px;
        font-weight: 600;
      }
    }
  }
}

.action-section {
  margin-top: 24px;
  display: flex;
  gap: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.stat-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid var(--border-color);
  
  .stat-info {
    display: flex;
    flex-direction: column;
    
    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: var(--text-white);
    }
    
    .stat-label {
      font-size: 14px;
      color: var(--text-secondary);
    }
  }
}

.recharge-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  
  .recharge-option {
    padding: 16px;
    text-align: center;
    background: rgba(0, 0, 0, 0.2);
    border: 2px solid var(--border-color);
    border-radius: var(--radius-md);
    cursor: pointer;
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
    transition: all 0.3s;
    
    &:hover {
      border-color: var(--steam-light-blue);
    }
    
    &.active {
      border-color: var(--steam-green);
      background: rgba(164, 208, 7, 0.1);
      color: var(--steam-green);
    }
  }
}

@media (max-width: 768px) {
  .info-section {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .avatar-section {
    flex-direction: column;
    text-align: center;
  }
  
  .action-section {
    flex-direction: column;
  }
}
</style>
