import type { Directive } from 'vue'

const PLACEHOLDER = 'data:image/svg+xml,' + encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" width="460" height="215" viewBox="0 0 460 215">' +
  '<rect fill="#1b2838" width="460" height="215"/>' +
  '<text x="230" y="110" text-anchor="middle" fill="#3d4f5f" font-size="14" font-family="Arial">Loading...</text>' +
  '</svg>'
)

const observer = new IntersectionObserver(
  (entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        const el = entry.target as HTMLImageElement | HTMLVideoElement
        const src = el.dataset.lazySrc
        if (src) {
          if (el instanceof HTMLImageElement) {
            el.src = src
          } else if (el instanceof HTMLVideoElement) {
            el.src = src
            el.load()
          }
          delete el.dataset.lazySrc
        }
        observer.unobserve(el)
      }
    })
  },
  { rootMargin: '200px' }
)

export const vLazy: Directive<HTMLImageElement | HTMLVideoElement, string> = {
  mounted(el, binding) {
    const realSrc = binding.value
    el.dataset.lazySrc = realSrc

    if (el instanceof HTMLImageElement) {
      el.src = PLACEHOLDER
    }

    observer.observe(el)
  },
  updated(el, binding) {
    if (binding.value !== binding.oldValue) {
      el.dataset.lazySrc = binding.value
      observer.observe(el)
    }
  },
  unmounted(el) {
    observer.unobserve(el)
  }
}
