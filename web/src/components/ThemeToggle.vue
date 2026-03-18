<script setup lang="ts">
import { Sun, Moon } from 'lucide-vue-next'
import { useTheme } from '@/composables/useTheme'

const { isDark, toggleTheme } = useTheme()
</script>

<template>
  <button
    @click="toggleTheme"
    class="relative p-2.5 rounded-xl transition-all duration-300 ease-out hover:scale-110"
    :class="isDark ? 'bg-white/10 hover:bg-white/15' : 'bg-black/5 hover:bg-black/10'"
    aria-label="Toggle theme"
  >
    <div class="relative w-5 h-5">
      <Transition
        enter-active-class="transition-all duration-300 ease-out"
        enter-from-class="opacity-0 rotate-90 scale-0"
        enter-to-class="opacity-100 rotate-0 scale-100"
        leave-active-class="transition-all duration-300 ease-in"
        leave-from-class="opacity-100 rotate-0 scale-100"
        leave-to-class="opacity-0 -rotate-90 scale-0"
        mode="out-in"
      >
        <Sun
          v-if="isDark"
          :size="20"
          class="text-amber-400"
        />
        <Moon
          v-else
          :size="20"
          class="text-slate-600"
        />
      </Transition>
    </div>
  </button>
</template>
