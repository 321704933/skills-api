<script setup lang="ts">
import { ref, computed } from 'vue'
import { Check, Copy, Terminal } from 'lucide-vue-next'
import { apiExamples } from '@/types/api-examples'

const selectedApi = ref(apiExamples[0])
const copied = ref(false)

const formattedJson = computed(() => {
  if (!selectedApi.value) return ''
  return JSON.stringify(selectedApi.value.response, null, 2)
})

const copyCode = async () => {
  if (!formattedJson.value) return
  await navigator.clipboard.writeText(formattedJson.value)
  copied.value = true
  setTimeout(() => {
    copied.value = false
  }, 2000)
}

const selectApi = (api: typeof apiExamples[0]) => {
  selectedApi.value = api
}
</script>

<template>
  <section id="api" class="section relative">
    <div class="container-custom">
      <!-- Section Header -->
      <div class="text-center mb-16">
        <h2 class="font-display font-bold text-3xl sm:text-4xl md:text-5xl text-[var(--text-primary)] mb-4">
          API 预览
        </h2>
        <p class="text-lg text-[var(--text-secondary)] max-w-2xl mx-auto">
          简洁统一的响应格式，完整的链路追踪支持
        </p>
      </div>

      <!-- Code Preview Container -->
      <div class="glass-card rounded-2xl overflow-hidden">
        <!-- Header -->
        <div class="flex items-center justify-between px-4 py-3 border-b border-[var(--border-color)] bg-[var(--bg-secondary)]">
          <div class="flex items-center gap-2">
            <Terminal class="w-4 h-4 text-[var(--text-muted)]" />
            <span class="text-sm font-medium text-[var(--text-secondary)]">Response Preview</span>
          </div>
          <button
            @click="copyCode"
            class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-all duration-200 hover:bg-[var(--bg-primary)]"
            :class="copied ? 'text-green-500' : 'text-[var(--text-muted)]'"
          >
            <Check v-if="copied" class="w-3.5 h-3.5" />
            <Copy v-else class="w-3.5 h-3.5" />
            {{ copied ? '已复制' : '复制' }}
          </button>
        </div>

        <div class="flex flex-col lg:flex-row lg:h-[595px]">
          <!-- API List Sidebar -->
          <div class="lg:w-64 border-b lg:border-b-0 lg:border-r border-[var(--border-color)] bg-[var(--bg-secondary)]/50 overflow-y-auto">
            <div class="p-2">
              <div
                v-for="api in apiExamples"
                :key="api.id"
                @click="selectApi(api)"
                class="w-full text-left px-4 py-3 rounded-xl transition-all duration-200 cursor-pointer"
                :class="selectedApi && selectedApi.id === api.id
                  ? 'bg-gradient-to-r from-brand-orange/10 to-brand-amber/10 border border-brand-orange/20'
                  : 'hover:bg-[var(--bg-primary)]'
                "
              >
                <div class="flex items-center gap-2 mb-1">
                  <span
                    class="px-1.5 py-0.5 rounded text-[10px] font-bold uppercase"
                    :class="api.method === 'GET'
                      ? 'bg-green-500/20 text-green-500'
                      : 'bg-blue-500/20 text-blue-500'
                    "
                  >
                    {{ api.method }}
                  </span>
                  <span class="text-sm font-medium text-[var(--text-primary)]">{{ api.name }}</span>
                </div>
                <p class="text-xs text-[var(--text-muted)] truncate">{{ api.endpoint }}</p>
              </div>
            </div>
          </div>

          <!-- Code Display -->
          <div class="flex-1 overflow-auto">
            <div class="p-4">
              <pre class="text-sm leading-relaxed"><code class="language-json">{{ formattedJson }}</code></pre>
            </div>
          </div>
        </div>

        <!-- Footer Info -->
        <div class="px-4 py-3 border-t border-[var(--border-color)] bg-[var(--bg-secondary)]">
          <p class="text-sm text-[var(--text-secondary)]" v-if="selectedApi">
            <span class="font-medium text-[var(--text-primary)]">{{ selectedApi.name }}：</span>
            {{ selectedApi.description }}
          </p>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
pre {
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  color: var(--text-primary);
}

/* Syntax highlighting colors */
:deep(.language-json) {
  .key {
    color: #9CDCFE;
  }
  .string {
    color: #CE9178;
  }
  .number {
    color: #B5CEA8;
  }
  .boolean {
    color: #569CD6;
  }
  .null {
    color: #569CD6;
  }
}
</style>
