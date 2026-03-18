<script setup lang="ts">
import { Github, FileText, Heart, Zap } from 'lucide-vue-next'
</script>

<template>
  <footer class="border-t border-[var(--border-color)] bg-[var(--bg-secondary)]/50">
    <div class="container-custom py-12">
      <div class="flex flex-col md:flex-row items-center justify-between gap-6">
        <!-- Logo & Copyright -->
        <div class="flex flex-col items-center md:items-start gap-2">
          <a href="#" class="flex items-center gap-2 group">
            <div
              class="w-8 h-8 rounded-lg flex items-center justify-center transition-transform duration-300 group-hover:scale-110"
              style="background: linear-gradient(135deg, #FF8C42 0%, #F85E00 50%, #FF6B35 100%)"
            >
              <Zap class="w-4 h-4 text-white" />
            </div>
            <span class="font-display font-bold text-lg text-[var(--text-primary)]">
              Skills API
            </span>
          </a>
          <p class="text-sm text-[var(--text-muted)]">
            © 2026 Skills API. All rights reserved.
          </p>
        </div>

        <!-- Links -->
        <div class="flex items-center gap-6">
          <a
            href="https://github.com/321704933/skills-api"
            target="_blank"
            rel="noopener"
            class="flex items-center gap-2 text-sm text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-colors"
          >
            <Github class="w-4 h-4" />
            GitHub
          </a>
          <a
            href="http://106.54.172.129:8080/doc.html"
            target="_blank"
            rel="noopener"
            class="flex items-center gap-2 text-sm text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-colors"
          >
            <FileText class="w-4 h-4" />
            API 文档
          </a>
        </div>

        <!-- Made with -->
        <div class="flex items-center gap-2 text-sm text-[var(--text-muted)]">
          Made with
          <Heart class="w-4 h-4 text-brand-orange fill-brand-orange" />
          by Devil
        </div>
      </div>
    </div>
  </footer>
</template>
