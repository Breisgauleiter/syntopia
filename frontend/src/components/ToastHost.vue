<template>
  <div class="toast-host" role="status" aria-live="polite">
    <div v-for="t in toasts" :key="t.id" class="toast" :class="t.type">
      <span>{{ t.message }}</span>
      <button class="close" @click="dismiss(t.id)" aria-label="Dismiss">×</button>
    </div>
  </div>
</template>
<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useToastStore } from '@/stores/toast'

const toastStore = useToastStore()
const { toasts } = storeToRefs(toastStore)
const { dismiss } = toastStore
</script>
<style scoped>
.toast-host { position: fixed; top: 1rem; right: 1rem; display: flex; flex-direction: column; gap: .5rem; z-index: 2000; }
.toast { background: var(--card-bg); color: var(--text-light); padding: .75rem 1rem; border-radius: .5rem; border: 1px solid var(--border-color); box-shadow: 0 4px 12px rgba(0,0,0,.3); font-size: .875rem; display: flex; align-items: center; gap: .75rem; }
.toast.success { border-color: #3fb950; }
.toast.error { border-color: #f85149; }
.toast.info { border-color: var(--accent-purple); }
.close { background: none; border: none; color: inherit; cursor: pointer; font-size: 1rem; line-height: 1; }
.close:hover { opacity: .7; }
</style>
