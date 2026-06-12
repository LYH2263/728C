<template>
  <div class="home-page">
    <!-- 轮播图区域 -->
    <section class="hero-section">
      <el-carousel height="400px" :interval="5000" arrow="always">
        <el-carousel-item v-for="game in featuredGames" :key="game.id">
          <div 
            class="hero-slide" 
            :style="{ backgroundImage: `url(${game.bannerImage || game.coverImage})` }"
            @click="goToGame(game.id)"
          >
            <div class="hero-content">
              <h2>{{ game.title }}</h2>
              <p>{{ game.description }}</p>
              <div class="hero-price">
                <template v-if="game.originalPrice === 0">
                  <span class="free">免费游玩</span>
                </template>
                <template v-else-if="game.discountPercent">
                  <span class="discount-tag">-{{ game.discountPercent }}%</span>
                  <span class="original">¥{{ game.originalPrice }}</span>
                  <span class="current">¥{{ game.discountPrice }}</span>
                </template>
                <template v-else>
                  <span class="current">¥{{ game.originalPrice }}</span>
                </template>
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>
    
    <!-- 特惠游戏 -->
    <section class="game-section" v-if="onSaleGames.length">
      <div class="section-header">
        <h2 class="section-title">特别优惠</h2>
        <router-link to="/store?onSale=true" class="view-more">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>
      <div class="game-grid">
        <GameCard v-for="game in onSaleGames" :key="game.id" :game="game" />
      </div>
    </section>
    
    <!-- 热销榜单 -->
    <section class="game-section" v-if="bestSellers.length">
      <div class="section-header">
        <h2 class="section-title">热销榜单</h2>
        <router-link to="/store?sortBy=sales&sortOrder=desc" class="view-more">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>
      <div class="bestseller-list">
        <div 
          v-for="(game, index) in bestSellers" 
          :key="game.id" 
          class="bestseller-item"
          @click="goToGame(game.id)"
        >
          <span class="rank" :class="{ top3: index < 3 }">{{ index + 1 }}</span>
          <img v-lazy="game.coverImage" :alt="game.title" class="cover" />
          <div class="info">
            <h4>{{ game.title }}</h4>
            <div class="price">
              <template v-if="game.originalPrice === 0">
                <span class="free">免费</span>
              </template>
              <template v-else-if="game.discountPercent">
                <span class="discount">-{{ game.discountPercent }}%</span>
                <span class="amount">¥{{ game.discountPrice }}</span>
              </template>
              <template v-else>
                <span class="amount">¥{{ game.originalPrice }}</span>
              </template>
            </div>
          </div>
        </div>
      </div>
    </section>
    
    <!-- 新品上架 -->
    <section class="game-section" v-if="newReleases.length">
      <div class="section-header">
        <h2 class="section-title">新品上架</h2>
        <router-link to="/store?sortBy=releaseDate&sortOrder=desc" class="view-more">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>
      <div class="game-grid">
        <GameCard v-for="game in newReleases" :key="game.id" :game="game" />
      </div>
    </section>
    
    <!-- 分类浏览 -->
    <section class="category-section" v-if="categories.length">
      <h2 class="section-title">分类浏览</h2>
      <div class="category-grid">
        <router-link 
          v-for="cat in categories" 
          :key="cat.id" 
          :to="`/store?categoryId=${cat.id}`"
          class="category-card"
        >
          <span class="icon">{{ cat.icon }}</span>
          <span class="name">{{ cat.name }}</span>
        </router-link>
      </div>
    </section>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { cachedCategoryApi, cachedGameApi } from '@/utils/cachedApi'
import type { Game, Category } from '@/types'
import GameCard from '@/components/GameCard.vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

const loading = ref(true)
const featuredGames = ref<Game[]>([])
const onSaleGames = ref<Game[]>([])
const bestSellers = ref<Game[]>([])
const newReleases = ref<Game[]>([])
const categories = ref<Category[]>([])

onMounted(async () => {
  await Promise.all([
    fetchHomeData(),
    fetchCategories()
  ])
  loading.value = false
})

async function fetchHomeData() {
  try {
    const data = await cachedGameApi.getHomeData()
    featuredGames.value = data.featured || []
    onSaleGames.value = data.onSale || []
    bestSellers.value = data.bestSellers || []
    newReleases.value = data.newReleases || []
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

async function fetchCategories() {
  try {
    categories.value = await cachedCategoryApi.getAll() || []
  } catch (error) {
    console.error('Failed to fetch categories')
  }
}

function goToGame(id: number) {
  router.push(`/game/${id}`)
}
</script>

<style lang="scss" scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

// 轮播图
.hero-section {
  border-radius: var(--radius-lg);
  overflow: hidden;
  
  :deep(.el-carousel__item) {
    cursor: pointer;
  }
}

.hero-slide {
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(to right, rgba(0, 0, 0, 0.8) 0%, transparent 60%);
  }
}

.hero-content {
  position: absolute;
  left: 40px;
  bottom: 40px;
  max-width: 500px;
  z-index: 1;
  
  h2 {
    font-size: 32px;
    font-weight: 700;
    color: var(--text-white);
    margin-bottom: 12px;
  }
  
  p {
    color: var(--text-light);
    font-size: 14px;
    margin-bottom: 16px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

.hero-price {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .free {
    color: var(--steam-green);
    font-size: 20px;
    font-weight: 600;
  }
  
  .discount-tag {
    background: var(--steam-green);
    color: var(--steam-darker);
    padding: 4px 8px;
    border-radius: var(--radius-sm);
    font-weight: 700;
  }
  
  .original {
    color: var(--text-secondary);
    text-decoration: line-through;
    font-size: 14px;
  }
  
  .current {
    color: var(--text-white);
    font-size: 24px;
    font-weight: 600;
  }
}

// 区块
.game-section, .category-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .view-more {
      display: flex;
      align-items: center;
      gap: 4px;
      color: var(--steam-light-blue);
      font-size: 14px;
      
      &:hover {
        color: var(--text-white);
      }
    }
  }
}

// 游戏网格
.game-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

// 热销榜单
.bestseller-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.bestseller-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid var(--border-color);
  
  &:hover {
    background: var(--bg-hover);
    border-color: var(--steam-light-blue);
  }
  
  .rank {
    width: 28px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--steam-blue);
    border-radius: var(--radius-sm);
    font-weight: 700;
    color: var(--text-primary);
    
    &.top3 {
      background: linear-gradient(135deg, #ffd700, #ff8c00);
      color: var(--steam-darker);
    }
  }
  
  .cover {
    width: 80px;
    height: 37px;
    object-fit: cover;
    border-radius: var(--radius-sm);
  }
  
  .info {
    flex: 1;
    min-width: 0;
    
    h4 {
      font-size: 14px;
      font-weight: 500;
      color: var(--text-white);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      margin-bottom: 4px;
    }
  }
  
  .price {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 12px;
    
    .free {
      color: var(--steam-green);
    }
    
    .discount {
      background: var(--steam-green);
      color: var(--steam-darker);
      padding: 1px 4px;
      border-radius: 2px;
      font-weight: 600;
      font-size: 11px;
    }
    
    .amount {
      color: var(--text-primary);
      font-weight: 500;
    }
  }
}

// 分类网格
.category-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  text-decoration: none;
  transition: all 0.3s;
  border: 1px solid var(--border-color);
  
  &:hover {
    transform: translateY(-2px);
    background: var(--bg-hover);
    border-color: var(--steam-light-blue);
  }
  
  .icon {
    font-size: 28px;
  }
  
  .name {
    color: var(--text-primary);
    font-size: 14px;
  }
}

.loading-container {
  padding: 40px;
}

// 响应式
@media (max-width: 1200px) {
  .game-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .category-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 900px) {
  .game-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .bestseller-list {
    grid-template-columns: 1fr;
  }
  
  .category-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .hero-content {
    left: 20px;
    bottom: 20px;
    
    h2 {
      font-size: 24px;
    }
  }
}

@media (max-width: 600px) {
  .game-grid {
    grid-template-columns: 1fr;
  }
  
  .category-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .hero-section {
    :deep(.el-carousel) {
      height: 300px !important;
    }
  }
}
</style>
