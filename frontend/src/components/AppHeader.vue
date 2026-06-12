<template>
  <header class="app-header">
    <div class="header-content">
      <!-- Logo -->
      <router-link to="/" class="logo">
        <el-icon :size="28"><Platform /></el-icon>
        <span>STEAM</span>
      </router-link>
      
      <!-- 导航菜单 -->
      <nav class="nav-menu">
        <router-link to="/" class="nav-item">首页</router-link>
        <router-link to="/store" class="nav-item">商店</router-link>
        <router-link v-if="isLoggedIn" to="/library" class="nav-item">游戏库</router-link>
      </nav>
      
      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索游戏"
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
        />
      </div>
      
      <!-- 用户区域 -->
      <div class="user-area">
        <template v-if="isLoggedIn">
          <!-- 购物车 -->
          <router-link to="/cart" class="icon-btn">
            <el-badge :value="cartCount" :hidden="cartCount === 0">
              <el-icon :size="22"><ShoppingCart /></el-icon>
            </el-badge>
          </router-link>
          
          <!-- 愿望单 -->
          <router-link to="/wishlist" class="icon-btn">
            <el-icon :size="22"><Star /></el-icon>
          </router-link>
          
          <!-- 用户下拉菜单 -->
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="getAvatarUrl(userInfo?.avatar)">
                {{ userInfo?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userInfo?.nickname || userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="orders">
                  <el-icon><Document /></el-icon>
                  我的订单
                </el-dropdown-item>
                <el-dropdown-item command="library">
                  <el-icon><Collection /></el-icon>
                  我的游戏
                </el-dropdown-item>
                <el-dropdown-item command="wishlist">
                  <el-icon><Star /></el-icon>
                  愿望单
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        
        <template v-else>
          <router-link to="/login">
            <el-button type="primary">登录</el-button>
          </router-link>
          <router-link to="/register">
            <el-button>注册</el-button>
          </router-link>
        </template>
      </div>
      
      <!-- 移动端菜单按钮 -->
      <el-button class="mobile-menu-btn" text @click="showMobileMenu = true">
        <el-icon :size="24"><Menu /></el-icon>
      </el-button>
    </div>
    
    <!-- 移动端抽屉菜单 -->
    <el-drawer v-model="showMobileMenu" direction="rtl" size="280px" :show-close="false">
      <template #header>
        <div class="drawer-header">
          <el-icon :size="24"><Platform /></el-icon>
          <span>STEAM</span>
        </div>
      </template>
      <div class="mobile-menu">
        <router-link to="/" class="mobile-nav-item" @click="showMobileMenu = false">
          <el-icon><HomeFilled /></el-icon>
          首页
        </router-link>
        <router-link to="/store" class="mobile-nav-item" @click="showMobileMenu = false">
          <el-icon><Shop /></el-icon>
          商店
        </router-link>
        <template v-if="isLoggedIn">
          <router-link to="/library" class="mobile-nav-item" @click="showMobileMenu = false">
            <el-icon><Collection /></el-icon>
            游戏库
          </router-link>
          <router-link to="/cart" class="mobile-nav-item" @click="showMobileMenu = false">
            <el-icon><ShoppingCart /></el-icon>
            购物车
          </router-link>
          <router-link to="/wishlist" class="mobile-nav-item" @click="showMobileMenu = false">
            <el-icon><Star /></el-icon>
            愿望单
          </router-link>
          <router-link to="/orders" class="mobile-nav-item" @click="showMobileMenu = false">
            <el-icon><Document /></el-icon>
            我的订单
          </router-link>
          <router-link to="/profile" class="mobile-nav-item" @click="showMobileMenu = false">
            <el-icon><User /></el-icon>
            个人中心
          </router-link>
          <div class="mobile-nav-item" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </div>
        </template>
        <template v-else>
          <router-link to="/login" class="mobile-nav-item" @click="showMobileMenu = false">
            <el-icon><User /></el-icon>
            登录
          </router-link>
          <router-link to="/register" class="mobile-nav-item" @click="showMobileMenu = false">
            <el-icon><Edit /></el-icon>
            注册
          </router-link>
        </template>
      </div>
    </el-drawer>
  </header>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchKeyword = ref('')
const showMobileMenu = ref(false)

const isLoggedIn = computed(() => userStore.isLoggedIn)
const userInfo = computed(() => userStore.userInfo)
const cartCount = computed(() => cartStore.count)

// 获取头像URL，处理默认头像
function getAvatarUrl(avatar: string | undefined): string {
  if (!avatar || avatar === '/avatars/default.png') {
    return '/avatars/default.svg'
  }
  return avatar
}

// 搜索
function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/store', query: { keyword: searchKeyword.value } })
    searchKeyword.value = ''
  }
}

// 下拉菜单命令
function handleCommand(command: string) {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'orders':
      router.push('/orders')
      break
    case 'library':
      router.push('/library')
      break
    case 'wishlist':
      router.push('/wishlist')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    showMobileMenu.value = false
    ElMessage.success('已退出登录')
    router.push('/')
  } catch {
    // 取消
  }
}
</script>

<style lang="scss" scoped>
.app-header {
  background: var(--steam-darker);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: var(--shadow-md);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  height: 64px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-white);
  font-size: 24px;
  font-weight: 700;
  text-decoration: none;
  
  &:hover {
    color: var(--steam-light-blue);
  }
}

.nav-menu {
  display: flex;
  gap: 8px;
  
  .nav-item {
    padding: 8px 16px;
    color: var(--text-primary);
    text-decoration: none;
    border-radius: var(--radius-sm);
    transition: all 0.3s;
    
    &:hover {
      background: var(--bg-hover);
      color: var(--text-white);
    }
    
    &.router-link-active {
      background: var(--steam-blue);
      color: var(--text-white);
    }
  }
}

.search-box {
  flex: 1;
  max-width: 300px;
  
  :deep(.el-input__wrapper) {
    background: rgba(0, 0, 0, 0.3);
    border-radius: var(--radius-md);
  }
}

.user-area {
  display: flex;
  align-items: center;
  gap: 16px;
  
  .icon-btn {
    color: var(--text-primary);
    padding: 8px;
    border-radius: var(--radius-sm);
    transition: all 0.3s;
    
    &:hover {
      background: var(--bg-hover);
      color: var(--steam-light-blue);
    }
  }
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: var(--radius-sm);
    transition: background 0.3s;
    
    &:hover {
      background: var(--bg-hover);
    }
    
    .username {
      color: var(--text-primary);
      font-size: 14px;
    }
  }
}

.mobile-menu-btn {
  display: none;
  color: var(--text-white);
}

.drawer-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-white);
}

.mobile-menu {
  display: flex;
  flex-direction: column;
  
  .mobile-nav-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    color: var(--text-primary);
    text-decoration: none;
    border-radius: var(--radius-sm);
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      background: var(--bg-hover);
      color: var(--text-white);
    }
  }
}

@media (max-width: 900px) {
  .nav-menu {
    display: none;
  }
}

@media (max-width: 768px) {
  .header-content {
    gap: 12px;
  }
  
  .search-box {
    display: none;
  }
  
  .user-area {
    display: none;
  }
  
  .mobile-menu-btn {
    display: flex;
  }
}
</style>
