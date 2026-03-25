import { computed, ref } from 'vue'

const width = ref(typeof window !== 'undefined' ? window.innerWidth : 1440)
const viewportHeight = ref(typeof window !== 'undefined' ? window.innerHeight : 900)
const prefersReducedMotion = ref(false)

let initialized = false
let motionQuery = null
let syncFrame = 0

const getViewportHeight = () => {
  if (typeof window === 'undefined') {
    return 900
  }

  return Math.round(window.visualViewport?.height || window.innerHeight || 900)
}

const syncViewport = () => {
  if (typeof window === 'undefined') {
    return
  }

  width.value = window.innerWidth
  viewportHeight.value = getViewportHeight()
  document.documentElement.style.setProperty('--app-height', `${viewportHeight.value}px`)
}

const handleMotionChange = (event) => {
  prefersReducedMotion.value = event.matches
}

const scheduleSyncViewport = () => {
  if (typeof window === 'undefined' || syncFrame) {
    return
  }

  syncFrame = window.requestAnimationFrame(() => {
    syncFrame = 0
    syncViewport()
  })
}

export function initViewport() {
  if (initialized || typeof window === 'undefined') {
    return
  }

  initialized = true
  syncViewport()

  motionQuery = window.matchMedia('(prefers-reduced-motion: reduce)')
  prefersReducedMotion.value = motionQuery.matches

  window.addEventListener('resize', scheduleSyncViewport, { passive: true })
  window.addEventListener('orientationchange', scheduleSyncViewport, { passive: true })
  window.visualViewport?.addEventListener('resize', scheduleSyncViewport, { passive: true })

  if (motionQuery.addEventListener) {
    motionQuery.addEventListener('change', handleMotionChange)
  } else {
    motionQuery.addListener(handleMotionChange)
  }
}

export function useViewport() {
  initViewport()

  return {
    width,
    viewportHeight,
    prefersReducedMotion,
    isMobile: computed(() => width.value < 768),
    isTablet: computed(() => width.value < 1024)
  }
}
