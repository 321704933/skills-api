<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  Flame,
  Newspaper,
  TrendingUp,
  Cloud,
  Calendar,
  MapPin,
  Quote,
  Shield,
  Key,
  Image,
} from 'lucide-vue-next'
import { features } from '@/types/features'

const iconMap: Record<string, any> = {
  Flame,
  Newspaper,
  TrendingUp,
  Cloud,
  Calendar,
  MapPin,
  Quote,
  Shield,
  Key,
  Image,
}

const visibleCards = ref<Set<number>>(new Set())

onMounted(() => {
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        const index = parseInt(entry.target.getAttribute('data-index') || '0')
        if (entry.isIntersecting) {
          setTimeout(() => {
            visibleCards.value.add(index)
          }, index * 50)
        }
      })
    },
    { threshold: 0.1, rootMargin: '0px 0px -50px 0px' },
  )

  document.querySelectorAll('.feature-card').forEach((card) => {
    observer.observe(card)
  })
})
</script>

<template>
  <section id="features" class="section relative">
    <div class="container-custom">
      <!-- Section Header -->
      <div class="text-center mb-16">
        <h2 class="font-display font-bold text-3xl sm:text-4xl md:text-5xl text-[var(--text-primary)] mb-4">
          功能特性
        </h2>
        <p class="text-lg text-[var(--text-secondary)] max-w-2xl mx-auto">
          9 大功能模块，覆盖数据服务的核心场景
        </p>
      </div>

      <!-- Features Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="(feature, index) in features"
          :key="feature.id"
          :data-index="index"
          class="feature-card glass-card rounded-2xl p-6 transition-all duration-500"
          :class="visibleCards.has(index) ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-8'"
        >
          <!-- Icon -->
          <div
            class="w-12 h-12 rounded-xl flex items-center justify-center mb-4"
            :style="{ backgroundColor: `${feature.color}20` }"
          >
            <component
              :is="iconMap[feature.icon]"
              class="w-6 h-6"
              :style="{ color: feature.color }"
            />
          </div>

          <!-- Title -->
          <h3 class="font-display font-semibold text-xl text-[var(--text-primary)] mb-2">
            {{ feature.title }}
          </h3>

          <!-- Description -->
          <p class="text-sm text-[var(--text-secondary)] mb-4 leading-relaxed">
            {{ feature.description }}
          </p>

          <!-- Tags -->
          <div class="flex flex-wrap gap-2">
            <span
              v-for="tag in feature.tags"
              :key="tag"
              class="px-2.5 py-1 rounded-lg text-xs font-medium"
              :style="{
                backgroundColor: `${feature.color}15`,
                color: feature.color,
              }"
            >
              {{ tag }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
