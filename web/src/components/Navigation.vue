<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import ThemeToggle from './ThemeToggle.vue'
import Logo from '../assets/logo.svg'

const isScrolled = ref(false)
const isVisible = ref(true)
let lastScrollY = 0

const handleScroll = () => {
  const currentScrollY = window.scrollY
  isScrolled.value = currentScrollY > 20
  isVisible.value = currentScrollY < lastScrollY || currentScrollY < 100
  lastScrollY = currentScrollY
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <nav
    class="fixed top-0 left-0 right-0 z-50 transition-all duration-500"
    :class="[
      isScrolled
        ? 'bg-[var(--bg-primary)]/80 backdrop-blur-xl border-b border-[var(--border-color)]'
        : 'bg-transparent',
      isVisible ? 'translate-y-0' : '-translate-y-full'
    ]"
  >
    <div class="container-custom">
      <div class="flex items-center justify-between h-16 md:h-20">
        <!-- Logo -->
        <a href="#" class="flex items-center gap-2.5 group">
          <img
            :src="Logo"
            alt="Skills API"
            class="w-9 h-9 rounded-xl transition-transform duration-300 group-hover:scale-110"
          />
          <span class="font-display font-bold text-lg text-[var(--text-primary)]">
            Skills API
          </span>
        </a>

        <!-- Nav Links (Desktop) -->
        <div class="hidden md:flex items-center gap-8">
          <a
            href="#features"
            class="text-sm font-medium text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-colors"
          >
            功能特性
          </a>
          <a
            href="#api"
            class="text-sm font-medium text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-colors"
          >
            API 文档
          </a>
          <a
            href="http://106.54.172.129:8080/doc.html"
            target="_blank"
            rel="noopener"
            class="text-sm font-medium text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-colors"
          >
            在线调试
          </a>
          <a
            href="https://github.com/321704933/skills-api"
            target="_blank"
            rel="noopener"
            class="text-sm font-medium text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-colors"
          >
            GitHub
          </a>
        </div>

        <!-- Right Side -->
        <div class="flex items-center gap-3">
          <ThemeToggle />
          <a
            href="#api"
            class="hidden sm:flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold text-white transition-all duration-300 hover:scale-105 hover:shadow-lg"
            style="background: linear-gradient(135deg, #FF8C42 0%, #F85E00 50%, #FF6B35 100%)"
          >
            开始使用
          </a>
        </div>
      </div>
    </div>
  </nav>
</template>
