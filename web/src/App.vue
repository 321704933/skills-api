<script setup lang="ts">
import { onMounted } from 'vue'
import Navigation from './components/Navigation.vue'
import HeroSection from './components/HeroSection.vue'
import FeaturesSection from './components/FeaturesSection.vue'
import CodePreview from './components/CodePreview.vue'
import TechStack from './components/TechStack.vue'
import Footer from './components/Footer.vue'
import { useTheme } from './composables/useTheme'

const { initTheme } = useTheme()

onMounted(() => {
  initTheme()
})
</script>

<template>
  <div class="min-h-screen bg-[var(--bg-primary)] transition-colors duration-300">
    <Navigation />
    <main>
      <HeroSection />
      <FeaturesSection />
      <CodePreview />
      <TechStack />
    </main>
    <Footer />
  </div>
</template>

<style>
/* Ensure smooth theme transitions */
* {
  transition-property: background-color, border-color, color, fill, stroke;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 150ms;
}

/* Exclude transform and opacity from global transition for performance */
.transform,
.opacity-0,
.opacity-100 {
  transition-property: transform, opacity;
}
</style>
