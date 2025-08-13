import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface Toast {
  id: string
  message: string
  type: 'success' | 'error' | 'info'
  timeout?: number
}

export const useToastStore = defineStore('toast', () => {
  const toasts = ref<Toast[]>([])

  const push = (message: string, type: Toast['type'] = 'info', timeout = 4000) => {
    const id = `${Date.now()}-${Math.random().toString(36).slice(2)}`
    const toast: Toast = { id, message, type, timeout }
    toasts.value.push(toast)
    if (timeout) {
      setTimeout(() => dismiss(id), timeout)
    }
    return id
  }

  const dismiss = (id: string) => {
    toasts.value = toasts.value.filter(t => t.id !== id)
  }

  return { toasts, push, dismiss }
})
