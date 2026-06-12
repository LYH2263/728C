import { createCache } from './cache'
import { gameApi, categoryApi } from '@/api'

const CACHE_TTL = {
  categories: 5 * 60 * 1000,
  homeData: 2 * 60 * 1000,
  gameDetail: 3 * 60 * 1000,
}

const apiCache = createCache<any>()

export const cachedGameApi = {
  getHomeData: () =>
    apiCache.get('home-data', async () => {
      const res = await gameApi.getHomeData()
      return res.data.data
    }, CACHE_TTL.homeData),

  getGame: (id: number) =>
    apiCache.get(`game-${id}`, async () => {
      const res = await gameApi.getGame(id)
      return res.data.data
    }, CACHE_TTL.gameDetail),
}

export const cachedCategoryApi = {
  getAll: () =>
    apiCache.get('categories', async () => {
      const res = await categoryApi.getAll()
      return res.data.data
    }, CACHE_TTL.categories),
}

export function invalidateGameCache(gameId?: number) {
  if (gameId) {
    apiCache.invalidate(`game-${gameId}`)
  }
  apiCache.invalidate('home-data')
}

export function clearAllCache() {
  apiCache.clear()
}
