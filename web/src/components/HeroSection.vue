<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ArrowRight, Sparkles } from 'lucide-vue-next'

const isVisible = ref(false)

onMounted(() => {
  setTimeout(() => {
    isVisible.value = true
  }, 100)
})
</script>

<template>
  <section class="relative min-h-screen flex items-center justify-center overflow-hidden pt-20">
    <!-- Background Effects -->
    <div class="absolute inset-0 overflow-hidden">
      <!-- Gradient Orbs -->
      <div
        class="absolute top-1/4 left-1/4 w-[500px] h-[500px] rounded-full opacity-30 blur-[100px] animate-pulse"
        style="background: linear-gradient(135deg, #FF8C42 0%, #F85E00 100%)"
      />
      <div
        class="absolute bottom-1/4 right-1/4 w-[400px] h-[400px] rounded-full opacity-20 blur-[100px] animate-pulse"
        style="background: linear-gradient(135deg, #FF6B35 0%, #F85E00 100%); animation-delay: 1s"
      />

      <!-- Grid Pattern -->
      <div
        class="absolute inset-0 opacity-[0.02]"
        style="background-image: linear-gradient(var(--text-primary) 1px, transparent 1px), linear-gradient(90deg, var(--text-primary) 1px, transparent 1px); background-size: 60px 60px"
      />
    </div>

    <!-- Content -->
    <div class="container-custom relative z-10">
      <div class="max-w-4xl mx-auto text-center">
        <!-- Badge -->
        <Transition
          enter-active-class="transition-all duration-700 ease-out"
          enter-from-class="opacity-0 translate-y-4"
          enter-to-class="opacity-100 translate-y-0"
        >
          <div
            v-if="isVisible"
            class="inline-flex items-center gap-2 px-4 py-2 rounded-full mb-8 border glass-card"
          >
            <Sparkles class="w-4 h-4 text-brand-orange" />
            <span class="text-sm font-medium text-[var(--text-secondary)]">
              Spring Boot 轻量级 API 服务平台
            </span>
          </div>
        </Transition>

        <!-- Main Title -->
        <Transition
          enter-active-class="transition-all duration-700 ease-out"
          enter-from-class="opacity-0 translate-y-6"
          enter-to-class="opacity-100 translate-y-0"
          :style="{ transitionDelay: '100ms' }"
        >
          <h1
            v-if="isVisible"
            class="font-display font-bold text-5xl sm:text-6xl md:text-7xl lg:text-8xl mb-6 tracking-tight"
          >
            <span class="gradient-text">Skills API</span>
          </h1>
        </Transition>

        <!-- Subtitle -->
        <Transition
          enter-active-class="transition-all duration-700 ease-out"
          enter-from-class="opacity-0 translate-y-6"
          enter-to-class="opacity-100 translate-y-0"
          :style="{ transitionDelay: '200ms' }"
        >
          <p
            v-if="isVisible"
            class="text-xl sm:text-2xl md:text-3xl font-display font-medium text-[var(--text-secondary)] mb-6"
          >
            开箱即用的数据服务
          </p>
        </Transition>

        <!-- Description -->
        <Transition
          enter-active-class="transition-all duration-700 ease-out"
          enter-from-class="opacity-0 translate-y-6"
          enter-to-class="opacity-100 translate-y-0"
          :style="{ transitionDelay: '300ms' }"
        >
          <p
            v-if="isVisible"
            class="text-base sm:text-lg text-[var(--text-muted)] max-w-2xl mx-auto mb-10 leading-relaxed"
          >
            集成热搜、早报、天气、股票、黄历等 10 大功能模块，基于 Spring Boot 3 + Java 21 构建，
            提供高性能、易集成的 API 服务，助力开发者快速构建应用。
          </p>
        </Transition>

        <!-- CTA Buttons -->
        <Transition
          enter-active-class="transition-all duration-700 ease-out"
          enter-from-class="opacity-0 translate-y-6"
          enter-to-class="opacity-100 translate-y-0"
          :style="{ transitionDelay: '400ms' }"
        >
          <div
            v-if="isVisible"
            class="flex flex-col sm:flex-row items-center justify-center gap-4"
          >
            <a
              href="#api"
              class="group flex items-center gap-2 px-8 py-4 rounded-2xl text-base font-semibold text-white transition-all duration-300 hover:scale-105 hover:shadow-xl"
              style="background: linear-gradient(135deg, #FF8C42 0%, #F85E00 50%, #FF6B35 100%)"
            >
              查看文档
              <ArrowRight class="w-5 h-5 transition-transform group-hover:translate-x-1" />
            </a>
            <a
              href="https://github.com/321704933/skills-api"
              target="_blank"
              rel="noopener"
              class="flex items-center gap-2 px-8 py-4 rounded-2xl text-base font-semibold transition-all duration-300 border glass-card hover:bg-[var(--bg-secondary)]"
              :class="isVisible ? 'text-[var(--text-primary)]' : ''"
            >
              GitHub
            </a>
          </div>
        </Transition>

        <!-- Stats -->
        <Transition
          enter-active-class="transition-all duration-700 ease-out"
          enter-from-class="opacity-0 translate-y-6"
          enter-to-class="opacity-100 translate-y-0"
          :style="{ transitionDelay: '500ms' }"
        >
          <div
            v-if="isVisible"
            class="flex flex-wrap items-center justify-center gap-8 sm:gap-16 mt-16 pt-16 border-t border-[var(--border-color)]"
          >
            <div class="text-center">
              <div class="font-display font-bold text-3xl sm:text-4xl gradient-text">10+</div>
              <div class="text-sm text-[var(--text-muted)] mt-1">功能模块</div>
            </div>
            <div class="text-center">
              <div class="font-display font-bold text-3xl sm:text-4xl gradient-text">25+</div>
              <div class="text-sm text-[var(--text-muted)] mt-1">API 端点</div>
            </div>
            <div class="text-center">
              <div class="font-display font-bold text-3xl sm:text-4xl gradient-text">0</div>
              <div class="text-sm text-[var(--text-muted)] mt-1">数据库依赖</div>
            </div>
          </div>
        </Transition>
      </div>
    </div>

    <!-- Scroll Indicator -->
    <div class="absolute bottom-8 left-1/2 -translate-x-1/2 animate-bounce">
      <div class="w-6 h-10 rounded-full border-2 border-[var(--border-color)] flex items-start justify-center p-2">
        <div class="w-1.5 h-1.5 rounded-full bg-brand-orange animate-pulse" />
      </div>
    </div>
  </section>
</template>
