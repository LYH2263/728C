export function createCancelableRequest() {
  let controller: AbortController | null = null

  return {
    execute<T>(fetcher: (signal: AbortSignal) => Promise<T>): Promise<T> {
      if (controller) {
        controller.abort()
      }
      controller = new AbortController()
      return fetcher(controller.signal)
    },

    cancel() {
      if (controller) {
        controller.abort()
        controller = null
      }
    }
  }
}
