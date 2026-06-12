interface CacheEntry<T> {
  data: T
  timestamp: number
}

export function createCache<T>() {
  const store = new Map<string, CacheEntry<T>>()

  return {
    async get(key: string, fetcher: () => Promise<T>, ttlMs: number): Promise<T> {
      const entry = store.get(key)
      if (entry && Date.now() - entry.timestamp < ttlMs) {
        return entry.data
      }
      const data = await fetcher()
      store.set(key, { data, timestamp: Date.now() })
      return data
    },

    invalidate(key: string) {
      store.delete(key)
    },

    clear() {
      store.clear()
    }
  }
}
