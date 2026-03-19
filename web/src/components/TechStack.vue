<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface Tech {
  name: string
  icon: string
  color: string
  description: string
}

const techs = ref<Tech[]>([
  {
    name: 'Spring Boot',
    icon: '🍃',
    color: '#6DB33F',
    description: '3.5.11'
  },
  {
    name: 'Java',
    icon: '☕',
    color: '#007396',
    description: '21 LTS'
  },
  {
    name: 'Redis',
    icon: '⚡',
    color: '#DC382D',
    description: 'Optional'
  },
  {
    name: 'Maven',
    icon: '📦',
    color: '#C71A36',
    description: 'Build'
  },
  {
    name: 'OpenAPI',
    icon: '📚',
    color: '#85EA2D',
    description: 'Docs'
  },
  {
    name: 'Docker',
    icon: '🐳',
    color: '#2496ED',
    description: 'Ready'
  }
])

const isVisible = ref(false)

onMounted(() => {
  const observer = new IntersectionObserver(
    (entries) => {
      if (entries[0] && entries[0].isIntersecting) {
        isVisible.value = true
      }
    },
    { threshold: 0.2 }
  )

  const section = document.getElementById('tech-stack')
  if (section) {
    observer.observe(section)
  }
})
</script>

<template>
  <section id="tech-stack" class="section relative">
    <div class="container-custom">
      <!-- Section Header -->
      <div class="text-center mb-16">
        <h2 class="font-display font-bold text-3xl sm:text-4xl md:text-5xl text-[var(--text-primary)] mb-4">
          技术栈
        </h2>
        <p class="text-lg text-[var(--text-secondary)] max-w-2xl mx-auto">
          基于现代化技术构建，高性能、易扩展
        </p>
      </div>

      <!-- Tech Grid -->
      <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
        <div
          v-for="(tech, index) in techs"
          :key="tech.name"
          class="glass-card rounded-2xl p-6 text-center transition-all duration-500"
          :class="isVisible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-8'"
          :style="{ transitionDelay: `${index * 50}ms` }"
        >
          <div
            class="w-16 h-16 mx-auto rounded-2xl flex items-center justify-center text-3xl mb-4"
            :style="{ backgroundColor: `${tech.color}15` }"
          >
            {{ tech.icon }}
          </div>
          <h3 class="font-display font-semibold text-lg text-[var(--text-primary)] mb-1">
            {{ tech.name }}
          </h3>
          <p class="text-sm text-[var(--text-muted)]">
            {{ tech.description }}
          </p>
        </div>
      </div>

      <!-- Additional Info -->
      <div class="mt-16 glass-card rounded-2xl p-8">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div class="text-center">
            <div class="font-display font-bold text-4xl gradient-text mb-2">Zero</div>
            <div class="text-[var(--text-secondary)]">数据库依赖</div>
            <p class="text-sm text-[var(--text-muted)] mt-2">本地缓存 + Redis 可选架构</p>
          </div>
          <div class="text-center">
            <div class="font-display font-bold text-4xl gradient-text mb-2">High</div>
            <div class="text-[var(--text-secondary)]">性能优化</div>
            <p class="text-sm text-[var(--text-muted)] mt-2">智能限流 + 幂等控制</p>
          </div>
          <div class="text-center">
            <div class="font-display font-bold text-4xl gradient-text mb-2">Full</div>
            <div class="text-[var(--text-secondary)]">链路追踪</div>
            <p class="text-sm text-[var(--text-muted)] mt-2">TraceId 全链路监控</p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
