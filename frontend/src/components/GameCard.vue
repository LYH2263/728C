<template>
  <div class="game-card" @click="goToDetail">
    <!-- 游戏封面 -->
    <div class="cover-wrapper">
      <img 
        v-lazy="game.coverImage || '/placeholder.jpg'"
        :alt="game.title"
        class="cover-image"
      />
      <!-- 折扣标签 -->
      <div v-if="hasDiscount" class="discount-badge">
        -{{ game.discountPercent }}%
      </div>
    </div>
    
    <!-- 游戏信息 -->
    <div class="game-info">
      <h3 class="game-title">{{ game.title }}</h3>
      
      <!-- 标签 -->
      <div v-if="parsedTags.length" class="game-tags">
        <el-tag 
          v-for="tag in parsedTags.slice(0, 3)" 
          :key="tag" 
          size="small"
          effect="plain"
        >
          {{ tag }}
        </el-tag>
      </div>
      
      <!-- 评分 -->
      <div class="game-rating" v-if="game.rating">
        <el-rate 
          :model-value="game.rating" 
          disabled 
          :max="5"
          size="small"
        />
        <span class="rating-text">{{ game.rating }}</span>
      </div>
      
      <!-- 价格 -->
      <div class="game-price">
        <template v-if="isFree">
          <span class="price free">免费游玩</span>
        </template>
        <template v-else-if="hasDiscount">
          <span class="original-price">¥{{ game.originalPrice }}</span>
          <span class="discount-price">¥{{ game.discountPrice }}</span>
        </template>
        <template v-else>
          <span class="current-price">¥{{ game.originalPrice }}</span>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { Game } from '@/types'

const props = defineProps<{
  game: Game
}>()

const router = useRouter()

const isFree = computed(() => props.game.originalPrice === 0)
const hasDiscount = computed(() => props.game.discountPercent && props.game.discountPercent > 0)

const parsedTags = computed(() => {
  if (!props.game.tags) return []
  try {
    return JSON.parse(props.game.tags)
  } catch {
    return []
  }
})

function goToDetail() {
  router.push(`/game/${props.game.id}`)
}
</script>

<style lang="scss" scoped>
.game-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid var(--border-color);
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-lg);
    border-color: var(--steam-light-blue);
    
    .cover-image {
      transform: scale(1.05);
    }
  }
}

.cover-wrapper {
  position: relative;
  aspect-ratio: 460 / 215;
  overflow: hidden;
  
  .cover-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
  }
  
  .discount-badge {
    position: absolute;
    top: 8px;
    right: 8px;
    background: var(--steam-green);
    color: var(--steam-darker);
    padding: 4px 8px;
    border-radius: var(--radius-sm);
    font-weight: 700;
    font-size: 14px;
  }
}

.game-info {
  padding: 12px;
}

.game-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-white);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.game-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 8px;
  
  .el-tag {
    font-size: 11px;
  }
}

.game-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  
  .rating-text {
    font-size: 12px;
    color: var(--text-secondary);
  }
}

.game-price {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .free {
    color: var(--steam-green);
    font-weight: 600;
  }
  
  .original-price {
    color: var(--text-secondary);
    text-decoration: line-through;
    font-size: 12px;
  }
  
  .discount-price {
    color: var(--steam-green);
    font-weight: 600;
    font-size: 16px;
  }
  
  .current-price {
    color: var(--text-primary);
    font-weight: 600;
    font-size: 16px;
  }
}
</style>
