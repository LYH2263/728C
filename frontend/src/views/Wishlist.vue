<template>
  <div class="wishlist-page">
    <h1 class="page-title">
      <el-icon><Star /></el-icon>
      我的愿望单
    </h1>
    
    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>
    
    <template v-else>
      <div v-if="items.length" class="wishlist-grid">
        <div v-for="item in items" :key="item.id" class="wishlist-item">
          <img :src="item.game.coverImage" :alt="item.game.title" @click="goToGame(item.gameId)" />
          <div class="item-info">
            <h3 @click="goToGame(item.gameId)">{{ item.game.title }}</h3>
            <div class="price">
              <template v-if="item.game.discountPercent">
                <span class="discount-tag">-{{ item.game.discountPercent }}%</span>
                <span class="original">¥{{ item.game.originalPrice }}</span>
                <span class="current">¥{{ item.game.discountPrice }}</span>
              </template>
              <template v-else-if="item.game.originalPrice === 0">
                <span class="free">免费</span>
              </template>
              <template v-else>
                <span class="current">¥{{ item.game.originalPrice }}</span>
              </template>
            </div>
          </div>
          <div class="item-actions">
            <el-button type="primary" size="small" @click="handleAddToCart(item.gameId)">
              <el-icon><ShoppingCart /></el-icon>
              加入购物车
            </el-button>
            <el-button size="small" @click="handleRemove(item.gameId)">
              <el-icon><Delete /></el-icon>
              移除
            </el-button>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="愿望单是空的">
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
import { wishlistApi } from '@/api'
import { useCartStore } from '@/store/cart'
import type { WishlistItem } from '@/types'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()

const loading = ref(true)
const items = ref<WishlistItem[]>([])

onMounted(async () => {
  await fetchWishlist()
  loading.value = false
})

async function fetchWishlist() {
  try {
    const res = await wishlistApi.getWishlist()
    items.value = res.data.data || []
  } catch (error) {
    items.value = []
  }
}

function goToGame(gameId: number) {
  router.push(`/game/${gameId}`)
}

async function handleAddToCart(gameId: number) {
  await cartStore.addToCart(gameId)
}

async function handleRemove(gameId: number) {
  try {
    await wishlistApi.removeFromWishlist(gameId)
    ElMessage.success('已从愿望单移除')
    await fetchWishlist()
  } catch (error) {
    // 错误已处理
  }
}
</script>

<style lang="scss" scoped>
.wishlist-page {
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

.wishlist-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.wishlist-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  transition: border-color 0.3s;
  
  &:hover {
    border-color: var(--steam-light-blue);
  }
  
  img {
    width: 120px;
    height: 56px;
    object-fit: cover;
    border-radius: var(--radius-sm);
    cursor: pointer;
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
    
    .price {
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
      
      .original {
        color: var(--text-secondary);
        text-decoration: line-through;
        font-size: 14px;
      }
      
      .current {
        color: var(--text-primary);
        font-weight: 600;
      }
      
      .free {
        color: var(--steam-green);
        font-weight: 600;
      }
    }
  }
  
  .item-actions {
    display: flex;
    gap: 8px;
  }
}

@media (max-width: 768px) {
  .wishlist-item {
    flex-direction: column;
    align-items: flex-start;
    
    img {
      width: 100%;
      height: auto;
      aspect-ratio: 460 / 215;
    }
    
    .item-actions {
      width: 100%;
      
      .el-button {
        flex: 1;
      }
    }
  }
}
</style>
