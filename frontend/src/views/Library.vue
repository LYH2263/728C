<template>
  <div class="library-page">
    <h1 class="page-title">
      <el-icon><Collection /></el-icon>
      我的游戏库
      <span class="count">({{ items.length }}款游戏)</span>
    </h1>
    
    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>
    
    <template v-else>
      <div v-if="items.length" class="library-grid">
        <div v-for="item in items" :key="item.id" class="library-item" @click="goToGame(item.gameId)">
          <img :src="item.game.coverImage" :alt="item.game.title" />
          <div class="item-overlay">
            <h3>{{ item.game.title }}</h3>
            <p class="developer">{{ item.game.developer }}</p>
            <div class="play-info" v-if="item.playTime">
              <el-icon><Timer /></el-icon>
              <span>已游玩 {{ formatPlayTime(item.playTime) }}</span>
            </div>
            <el-button type="success" size="small">
              <el-icon><VideoPlay /></el-icon>
              开始游戏
            </el-button>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="游戏库是空的">
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
import { libraryApi } from '@/api'
import type { UserLibrary } from '@/types'

const router = useRouter()

const loading = ref(true)
const items = ref<UserLibrary[]>([])

onMounted(async () => {
  await fetchLibrary()
  loading.value = false
})

async function fetchLibrary() {
  try {
    const res = await libraryApi.getLibrary()
    items.value = res.data.data || []
  } catch (error) {
    items.value = []
  }
}

function goToGame(gameId: number) {
  router.push(`/game/${gameId}`)
}

function formatPlayTime(minutes: number) {
  if (minutes < 60) return `${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return mins > 0 ? `${hours}小时${mins}分钟` : `${hours}小时`
}
</script>

<style lang="scss" scoped>
.library-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  color: var(--text-white);
  margin-bottom: 24px;
  
  .count {
    font-size: 16px;
    color: var(--text-secondary);
    font-weight: normal;
  }
}

.library-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.library-item {
  position: relative;
  aspect-ratio: 460 / 215;
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s;
  }
  
  .item-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.9) 0%, transparent 50%);
    padding: 16px;
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    opacity: 0;
    transition: opacity 0.3s;
    
    h3 {
      font-size: 16px;
      color: var(--text-white);
      margin-bottom: 4px;
    }
    
    .developer {
      font-size: 12px;
      color: var(--text-secondary);
      margin-bottom: 8px;
    }
    
    .play-info {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: var(--steam-light-blue);
      margin-bottom: 12px;
    }
  }
  
  &:hover {
    img {
      transform: scale(1.05);
    }
    
    .item-overlay {
      opacity: 1;
    }
  }
}

@media (max-width: 600px) {
  .library-grid {
    grid-template-columns: 1fr;
  }
}
</style>
