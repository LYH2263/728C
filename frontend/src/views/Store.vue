<template>
  <div class="store-page">
    <!-- 筛选栏 -->
    <aside class="filter-sidebar">
      <div class="filter-section">
        <h3>搜索</h3>
        <el-input
          v-model="filters.keyword"
          placeholder="搜索游戏"
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
        />
      </div>
      
      <div class="filter-section">
        <h3>分类</h3>
        <el-radio-group v-model="filters.categoryId" @change="handleSearch">
          <el-radio :label="null">全部</el-radio>
          <el-radio v-for="cat in categories" :key="cat.id" :label="cat.id">
            {{ cat.icon }} {{ cat.name }}
          </el-radio>
        </el-radio-group>
      </div>
      
      <div class="filter-section">
        <h3>价格</h3>
        <el-radio-group v-model="filters.priceRange" @change="handleSearch">
          <el-radio :label="null">全部</el-radio>
          <el-radio label="free">免费游戏</el-radio>
          <el-radio label="under50">¥50以下</el-radio>
          <el-radio label="50to100">¥50 - ¥100</el-radio>
          <el-radio label="100to200">¥100 - ¥200</el-radio>
          <el-radio label="over200">¥200以上</el-radio>
        </el-radio-group>
      </div>
      
      <div class="filter-section">
        <h3>特殊</h3>
        <el-checkbox v-model="filters.onSale" @change="handleSearch">特惠促销</el-checkbox>
      </div>
      
      <el-button type="primary" @click="resetFilters" style="width: 100%">
        重置筛选
      </el-button>
    </aside>
    
    <!-- 游戏列表 -->
    <main class="game-list-container">
      <!-- 排序栏 -->
      <div class="sort-bar">
        <span class="result-count">共找到 {{ total }} 款游戏</span>
        <div class="sort-options">
          <span>排序：</span>
          <el-select v-model="sortOption" @change="handleSort" size="small">
            <el-option label="最新发布" value="releaseDate-desc" />
            <el-option label="价格从低到高" value="price-asc" />
            <el-option label="价格从高到低" value="price-desc" />
            <el-option label="评分最高" value="rating-desc" />
            <el-option label="销量最高" value="sales-desc" />
          </el-select>
        </div>
      </div>
      
      <!-- 游戏网格 -->
      <div v-if="loading" class="loading-grid">
        <el-skeleton v-for="i in 8" :key="i" :rows="3" animated class="skeleton-card" />
      </div>
      
      <div v-else-if="games.length" class="game-grid">
        <GameCard v-for="game in games" :key="game.id" :game="game" />
      </div>
      
      <el-empty v-else description="未找到相关游戏" />
      
      <!-- 分页 -->
      <div class="pagination" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { gameApi } from '@/api'
import { cachedCategoryApi } from '@/utils/cachedApi'
import { createCancelableRequest } from '@/utils/cancelable'
import type { Game, Category } from '@/types'
import GameCard from '@/components/GameCard.vue'
import { Search } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const games = ref<Game[]>([])
const categories = ref<Category[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const sortOption = ref('releaseDate-desc')

const searchRequest = createCancelableRequest()

const filters = reactive({
  keyword: '',
  categoryId: null as number | null,
  priceRange: null as string | null,
  onSale: false
})

onMounted(async () => {
  // 从URL读取初始参数
  if (route.query.keyword) filters.keyword = route.query.keyword as string
  if (route.query.categoryId) filters.categoryId = Number(route.query.categoryId)
  if (route.query.priceRange) filters.priceRange = route.query.priceRange as string
  if (route.query.onSale === 'true') filters.onSale = true
  if (route.query.sortBy && route.query.sortOrder) {
    sortOption.value = `${route.query.sortBy}-${route.query.sortOrder}`
  }
  if (route.query.page) currentPage.value = Number(route.query.page)
  
  await fetchCategories()

  // 支持 /category/:category 动态路由
  if (route.params.category) {
    const catName = decodeURIComponent(route.params.category as string)
    const matched = categories.value.find(
      c => c.name === catName || c.id === Number(catName)
    )
    if (matched) {
      filters.categoryId = matched.id
    }
  }

  await fetchGames()
})

watch(() => route.query, () => {
  if (route.query.keyword) filters.keyword = route.query.keyword as string
  if (route.query.categoryId) filters.categoryId = Number(route.query.categoryId)
  fetchGames()
}, { deep: true })

async function fetchCategories() {
  try {
    categories.value = await cachedCategoryApi.getAll() || []
  } catch (error) {
    console.error('Failed to fetch categories')
  }
}

async function fetchGames() {
  loading.value = true
  try {
    const [sortBy, sortOrder] = sortOption.value.split('-')
    const res = await searchRequest.execute((signal) =>
      gameApi.searchGames({
        keyword: filters.keyword || undefined,
        categoryId: filters.categoryId || undefined,
        priceRange: filters.priceRange || undefined,
        onSale: filters.onSale || undefined,
        sortBy,
        sortOrder,
        page: currentPage.value,
        size: pageSize.value
      }, signal)
    )
    const data = res.data.data
    games.value = data.list || []
    total.value = data.total || 0
  } catch (error: any) {
    if (error?.name === 'AbortError' || error?.code === 'ERR_CANCELED') return
    games.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  searchRequest.cancel()
})

function handleSearch() {
  currentPage.value = 1
  updateRoute()
  fetchGames()
}

function handleSort() {
  currentPage.value = 1
  updateRoute()
  fetchGames()
}

function handlePageChange(page: number) {
  currentPage.value = page
  updateRoute()
  fetchGames()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function resetFilters() {
  filters.keyword = ''
  filters.categoryId = null
  filters.priceRange = null
  filters.onSale = false
  sortOption.value = 'releaseDate-desc'
  currentPage.value = 1
  router.push('/store')
  fetchGames()
}

function updateRoute() {
  const [sortBy, sortOrder] = sortOption.value.split('-')
  const query: Record<string, any> = {}
  if (filters.keyword) query.keyword = filters.keyword
  if (filters.categoryId) query.categoryId = filters.categoryId
  if (filters.priceRange) query.priceRange = filters.priceRange
  if (filters.onSale) query.onSale = 'true'
  if (sortBy !== 'releaseDate' || sortOrder !== 'desc') {
    query.sortBy = sortBy
    query.sortOrder = sortOrder
  }
  if (currentPage.value > 1) query.page = currentPage.value
  
  router.replace({ query })
}
</script>

<style lang="scss" scoped>
.store-page {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 24px;
}

// 筛选栏
.filter-sidebar {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 20px;
  height: fit-content;
  position: sticky;
  top: 84px;
  border: 1px solid var(--border-color);
}

.filter-section {
  margin-bottom: 24px;
  
  h3 {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-white);
    margin-bottom: 12px;
  }
  
  .el-radio-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .el-radio {
      margin-right: 0;
      
      :deep(.el-radio__label) {
        color: var(--text-primary);
      }
    }
  }
  
  .el-checkbox {
    :deep(.el-checkbox__label) {
      color: var(--text-primary);
    }
  }
}

// 游戏列表
.game-list-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sort-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  
  .result-count {
    color: var(--text-secondary);
    font-size: 14px;
  }
  
  .sort-options {
    display: flex;
    align-items: center;
    gap: 8px;
    
    span {
      color: var(--text-secondary);
      font-size: 14px;
    }
  }
}

.game-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.loading-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  
  .skeleton-card {
    background: var(--bg-card);
    border-radius: var(--radius-md);
    padding: 16px;
  }
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

// 响应式
@media (max-width: 1100px) {
  .game-grid, .loading-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 900px) {
  .store-page {
    grid-template-columns: 1fr;
  }
  
  .filter-sidebar {
    position: static;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  
  .filter-section {
    margin-bottom: 0;
  }
}

@media (max-width: 600px) {
  .filter-sidebar {
    grid-template-columns: 1fr;
  }
  
  .game-grid, .loading-grid {
    grid-template-columns: 1fr;
  }
  
  .sort-bar {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>
