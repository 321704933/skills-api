import { ref, onMounted } from 'vue'

type Theme = 'light' | 'dark'

const STORAGE_KEY = 'skills-api-theme'

export function useTheme() {
  const theme = ref<Theme>('light')
  const isDark = ref(false)

  const applyTheme = (newTheme: Theme) => {
    theme.value = newTheme
    isDark.value = newTheme === 'dark'

    if (newTheme === 'dark') {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }

    localStorage.setItem(STORAGE_KEY, newTheme)
  }

  const toggleTheme = () => {
    applyTheme(theme.value === 'light' ? 'dark' : 'light')
  }

  const initTheme = () => {
    // Check localStorage first
    const saved = localStorage.getItem(STORAGE_KEY) as Theme | null

    if (saved) {
      applyTheme(saved)
      return
    }

    // Check system preference
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    applyTheme(prefersDark ? 'dark' : 'light')
  }

  onMounted(() => {
    initTheme()
  })

  // Listen for system theme changes
  if (typeof window !== 'undefined') {
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
      if (!localStorage.getItem(STORAGE_KEY)) {
        applyTheme(e.matches ? 'dark' : 'light')
      }
    })
  }

  return {
    theme,
    isDark,
    toggleTheme,
    applyTheme,
    initTheme,
  }
}
