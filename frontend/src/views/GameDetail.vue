<template>
  <div class="game-detail-page" v-if="game">
    <!-- 头部横幅 -->
    <div class="hero-banner" :style="{ backgroundImage: `url(${game.bannerImage || game.coverImage})` }">
      <div class="hero-overlay"></div>
    </div>
    
    <!-- 主要内容 -->
    <div class="content-container">
      <!-- 左侧：游戏信息 -->
      <div class="main-content">
        <!-- 基本信息 -->
        <div class="game-header">
          <h1>{{ game.title }}</h1>
          <p class="description">{{ game.description }}</p>
          
          <!-- 标签 -->
          <div class="tags" v-if="parsedTags.length">
            <el-tag v-for="tag in parsedTags" :key="tag" effect="plain">{{ tag }}</el-tag>
          </div>
        </div>
        
        <!-- 截图轮播 -->
        <div class="screenshots-section" v-if="parsedScreenshots.length">
          <h2 class="section-title">游戏截图</h2>
          <el-carousel height="360px" :interval="4000">
            <el-carousel-item v-for="(img, index) in parsedScreenshots" :key="index">
              <img 
                v-lazy="img"
                :alt="`截图 ${index + 1}`" 
                class="screenshot-img" 
                @error="handleImageError($event, index)"
              />
            </el-carousel-item>
          </el-carousel>
        </div>
        
        <!-- 详细介绍（富文本渲染） -->
        <div class="detail-section" v-if="game.detailDescription">
          <h2 class="section-title">关于游戏</h2>
          <RichText :content="game.detailDescription" />
        </div>
        
        <!-- 系统需求 -->
        <div class="requirements-section" v-if="minReq || recReq">
          <h2 class="section-title">系统需求</h2>
          <div class="requirements-grid">
            <div class="requirement-card" v-if="minReq">
              <h4>最低配置</h4>
              <ul>
                <li v-if="minReq.os"><strong>操作系统：</strong>{{ minReq.os }}</li>
                <li v-if="minReq.cpu"><strong>处理器：</strong>{{ minReq.cpu }}</li>
                <li v-if="minReq.memory"><strong>内存：</strong>{{ minReq.memory }}</li>
                <li v-if="minReq.gpu"><strong>显卡：</strong>{{ minReq.gpu }}</li>
                <li v-if="minReq.storage"><strong>存储空间：</strong>{{ minReq.storage }}</li>
              </ul>
            </div>
            <div class="requirement-card" v-if="recReq">
              <h4>推荐配置</h4>
              <ul>
                <li v-if="recReq.os"><strong>操作系统：</strong>{{ recReq.os }}</li>
                <li v-if="recReq.cpu"><strong>处理器：</strong>{{ recReq.cpu }}</li>
                <li v-if="recReq.memory"><strong>内存：</strong>{{ recReq.memory }}</li>
                <li v-if="recReq.gpu"><strong>显卡：</strong>{{ recReq.gpu }}</li>
                <li v-if="recReq.storage"><strong>存储空间：</strong>{{ recReq.storage }}</li>
              </ul>
            </div>
          </div>
        </div>
        
        <!-- 评论区 -->
        <div class="reviews-section">
          <h2 class="section-title">玩家评测</h2>
          <div v-if="reviews.length" class="reviews-list">
            <div v-for="review in reviews" :key="review.id" class="review-item">
              <div class="review-header">
                <el-avatar :size="40" :src="getAvatarUrl(review.user?.avatar)">
                  {{ review.user?.nickname?.charAt(0) || 'U' }}
                </el-avatar>
                <div class="review-user">
                  <span class="username">{{ review.user?.nickname || review.user?.username }}</span>
                  <span class="recommend" :class="{ positive: review.isRecommend }">
                    {{ review.isRecommend ? '👍 推荐' : '👎 不推荐' }}
                  </span>
                </div>
                <el-rate :model-value="review.rating" disabled size="small" />
              </div>
              <p class="review-content">{{ review.content }}</p>
              <div class="review-footer">
                <span class="time">{{ formatDate(review.createdAt) }}</span>
                <el-button text size="small" @click="handleHelpful(review.id)">
                  <el-icon><Pointer /></el-icon>
                  有帮助 ({{ review.helpfulCount }})
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无评测" />
        </div>
      </div>
      
      <!-- 右侧：购买信息 -->
      <aside class="sidebar">
        <div class="purchase-card">
          <img v-lazy="game.coverImage" :alt="game.title" class="cover-img" />
          
          <!-- 评分 -->
          <div class="rating-info" v-if="game.rating">
            <el-rate :model-value="game.rating" disabled :max="5" />
            <span class="rating-text">{{ game.rating }} ({{ game.ratingCount }}条评测)</span>
          </div>
          
          <!-- 游戏信息 -->
          <div class="game-meta">
            <div class="meta-item" v-if="game.developer">
              <span class="label">开发商</span>
              <span class="value">{{ game.developer }}</span>
            </div>
            <div class="meta-item" v-if="game.publisher">
              <span class="label">发行商</span>
              <span class="value">{{ game.publisher }}</span>
            </div>
            <div class="meta-item" v-if="game.releaseDate">
              <span class="label">发行日期</span>
              <span class="value">{{ game.releaseDate }}</span>
            </div>
          </div>
          
          <!-- 价格 -->
          <div class="price-section">
            <template v-if="isFree">
              <span class="price free">免费游玩</span>
            </template>
            <template v-else-if="hasDiscount">
              <div class="discount-info">
                <span class="discount-tag">-{{ game.discountPercent }}%</span>
                <span class="original-price">¥{{ game.originalPrice }}</span>
              </div>
              <span class="final-price">¥{{ game.discountPrice }}</span>
            </template>
            <template v-else>
              <span class="final-price">¥{{ game.originalPrice }}</span>
            </template>
          </div>
          
          <!-- 操作按钮 -->
          <div class="action-buttons">
            <template v-if="ownsGame">
              <el-button type="success" size="large" disabled style="width: 100%">
                <el-icon><Check /></el-icon>
                已拥有
              </el-button>
            </template>
            <template v-else>
              <el-button 
                type="primary" 
                size="large" 
                style="width: 100%"
                @click="handleAddToCart"
                :disabled="inCart"
              >
                <el-icon><ShoppingCart /></el-icon>
                {{ inCart ? '已在购物车' : '加入购物车' }}
              </el-button>
              <el-button 
                size="large" 
                style="width: 100%;margin-left:0;"
                @click="handleWishlist"
              >
                <el-icon><Star /></el-icon>
                {{ inWishlist ? '移出愿望单' : '加入愿望单' }}
              </el-button>
            </template>
          </div>
        </div>
      </aside>
    </div>
  </div>
  
  <!-- 加载状态 -->
  <div v-else-if="loading" class="loading-container">
    <el-skeleton :rows="10" animated />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { gameApi, reviewApi, wishlistApi, libraryApi } from '@/api'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'
import type { Game, GameReview } from '@/types'
import { ElMessage } from 'element-plus'
import RichText from '@/components/RichText.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const loading = ref(true)
const game = ref<Game | null>(null)
const reviews = ref<GameReview[]>([])
const ownsGame = ref(false)
const inWishlist = ref(false)

const gameId = computed(() => Number(route.params.id))
const isFree = computed(() => game.value?.originalPrice === 0)

// 获取头像URL，处理默认头像
function getAvatarUrl(avatar: string | undefined): string {
  if (!avatar || avatar === '/avatars/default.png') {
    return '/avatars/default.svg'
  }
  return avatar
}

// 默认截图占位图
const defaultScreenshot = 'data:image/svg+xml,' + encodeURIComponent(`
<svg xmlns="http://www.w3.org/2000/svg" width="640" height="360" viewBox="0 0 640 360">
  <rect fill="#1b2838" width="640" height="360"/>
  <text x="320" y="170" text-anchor="middle" fill="#66c0f4" font-size="24" font-family="Arial">
    图片加载失败
  </text>
  <text x="320" y="200" text-anchor="middle" fill="#8f98a0" font-size="14" font-family="Arial">
    Screenshot unavailable
  </text>
</svg>
`)

// 截图加载失败处理
function handleImageError(event: Event, _index: number) {
  const img = event.target as HTMLImageElement
  if (img && img.src !== defaultScreenshot) {
    img.src = defaultScreenshot
  }
}

const hasDiscount = computed(() => game.value?.discountPercent && game.value.discountPercent > 0)
const inCart = computed(() => cartStore.isInCart(gameId.value))

const parsedTags = computed(() => {
  if (!game.value?.tags) return []
  try { return JSON.parse(game.value.tags) } catch { return [] }
})

const parsedScreenshots = computed(() => {
  if (!game.value?.screenshots) return []
  try { return JSON.parse(game.value.screenshots) } catch { return [] }
})

const minReq = computed(() => {
  if (!game.value?.minRequirements) return null
  try { return JSON.parse(game.value.minRequirements) } catch { return null }
})

const recReq = computed(() => {
  if (!game.value?.recRequirements) return null
  try { return JSON.parse(game.value.recRequirements) } catch { return null }
})

onMounted(async () => {
  await fetchGame()
  await Promise.all([
    fetchReviews(),
    checkOwnership(),
    checkWishlist()
  ])
  loading.value = false
})

async function fetchGame() {
  try {
    const res = await gameApi.getGame(gameId.value)
    game.value = res.data.data
  } catch (error) {
    ElMessage.error('游戏不存在')
    router.push('/store')
  }
}

async function fetchReviews() {
  try {
    const res = await reviewApi.getGameReviews(gameId.value, 1, 10)
    reviews.value = res.data.data.list || []
  } catch (error) {
    console.error('Failed to fetch reviews')
  }
}

async function checkOwnership() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await libraryApi.checkOwnership(gameId.value)
    ownsGame.value = res.data.data
  } catch (error) {
    ownsGame.value = false
  }
}

async function checkWishlist() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await wishlistApi.checkWishlist(gameId.value)
    inWishlist.value = res.data.data
  } catch (error) {
    inWishlist.value = false
  }
}

async function handleAddToCart() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  await cartStore.addToCart(gameId.value)
}

async function handleWishlist() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  
  try {
    if (inWishlist.value) {
      await wishlistApi.removeFromWishlist(gameId.value)
      inWishlist.value = false
      ElMessage.success('已从愿望单移除')
    } else {
      await wishlistApi.addToWishlist(gameId.value)
      inWishlist.value = true
      ElMessage.success('已加入愿望单')
    }
  } catch (error) {
    // 错误已处理
  }
}

async function handleHelpful(reviewId: number) {
  try {
    await reviewApi.markHelpful(reviewId)
    ElMessage.success('感谢您的反馈')
  } catch (error) {
    // 错误已处理
  }
}

function formatDate(date: string) {
  return new Date(date).toLocaleDateString('zh-CN')
}
</script>

<style lang="scss" scoped>
.game-detail-page {
  position: relative;
}

// 头部横幅
.hero-banner {
  height: 400px;
  background-size: cover;
  background-position: center;
  position: absolute;
  top: -20px;
  left: -20px;
  right: -20px;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, transparent 0%, var(--steam-dark) 100%);
}

// 内容容器
.content-container {
  position: relative;
  z-index: 1;
  padding-top: 200px;
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 30px;
}

// 主要内容
.main-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.game-header {
  h1 {
    font-size: 36px;
    font-weight: 700;
    color: var(--text-white);
    margin-bottom: 16px;
  }
  
  .description {
    color: var(--text-light);
    font-size: 16px;
    line-height: 1.6;
    margin-bottom: 16px;
  }
  
  .tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}

// 截图
.screenshots-section {
  .screenshot-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: var(--radius-md);
  }
}

// 详细介绍
.detail-section {
  .detail-content {
    color: var(--text-light);
    line-height: 1.8;
    white-space: pre-wrap;
  }
}

// 系统需求
.requirements-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.requirement-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 20px;
  border: 1px solid var(--border-color);
  
  h4 {
    color: var(--text-white);
    margin-bottom: 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid var(--border-color);
  }
  
  ul {
    list-style: none;
    
    li {
      color: var(--text-light);
      font-size: 14px;
      margin-bottom: 8px;
      
      strong {
        color: var(--text-primary);
      }
    }
  }
}

// 评论区
.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 20px;
  border: 1px solid var(--border-color);
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  
  .review-user {
    flex: 1;
    
    .username {
      display: block;
      color: var(--text-white);
      font-weight: 500;
    }
    
    .recommend {
      font-size: 12px;
      color: var(--text-secondary);
      
      &.positive {
        color: var(--steam-green);
      }
    }
  }
}

.review-content {
  color: var(--text-light);
  line-height: 1.6;
  margin-bottom: 12px;
}

.review-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .time {
    color: var(--text-secondary);
    font-size: 12px;
  }
}

// 侧边栏
.sidebar {
  position: sticky;
  top: 84px;
  height: fit-content;
}

.purchase-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 20px;
  border: 1px solid var(--border-color);
  
  .cover-img {
    width: 100%;
    aspect-ratio: 460 / 215;
    object-fit: cover;
    border-radius: var(--radius-md);
    margin-bottom: 16px;
  }
}

.rating-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  
  .rating-text {
    color: var(--text-secondary);
    font-size: 12px;
  }
}

.game-meta {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-color);
  
  .meta-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 14px;
    
    .label {
      color: var(--text-secondary);
    }
    
    .value {
      color: var(--text-primary);
    }
  }
}

.price-section {
  margin-bottom: 20px;
  text-align: center;
  
  .free {
    color: var(--steam-green);
    font-size: 24px;
    font-weight: 600;
  }
  
  .discount-info {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
    
    .discount-tag {
      background: var(--steam-green);
      color: var(--steam-darker);
      padding: 4px 8px;
      border-radius: var(--radius-sm);
      font-weight: 700;
    }
    
    .original-price {
      color: var(--text-secondary);
      text-decoration: line-through;
    }
  }
  
  .final-price {
    font-size: 28px;
    font-weight: 700;
    color: var(--text-white);
  }
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.loading-container {
  padding: 40px;
}

// 响应式
@media (max-width: 900px) {
  .content-container {
    grid-template-columns: 1fr;
    padding-top: 150px;
  }
  
  .sidebar {
    position: static;
  }
  
  .requirements-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .hero-banner {
    height: 250px;
  }
  
  .content-container {
    padding-top: 100px;
  }
  
  .game-header h1 {
    font-size: 24px;
  }
}
</style>
