<template>
  <div class="notification-container">
    <TransitionGroup name="notification" tag="div">
      <div 
        v-for="notification in notifications" 
        :key="notification.id"
        class="quest-notification"
      >
        <div class="notification-content">
          <div class="notification-icon">✅</div>
          <div class="notification-message">
            <div class="notification-title">{{ notification.title }}</div>
            <div v-if="notification.description">{{ notification.description }}</div>
          </div>
          <button @click="dismissNotification(notification.id)">×</button>
        </div>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface QuestNotification {
  id: string
  title: string
  description?: string
}

const notifications = ref<QuestNotification[]>([])

const dismissNotification = (id: string): void => {
  const index = notifications.value.findIndex(n => n.id === id)
  if (index > -1) {
    notifications.value.splice(index, 1)
  }
}

const addNotification = (notification: Omit<QuestNotification, 'id'>): string => {
  const id = Date.now().toString()
  notifications.value.unshift({ id, ...notification })
  return id
}

defineExpose({ addNotification, dismissNotification })
</script>

<style scoped>
.notification-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
}

.quest-notification {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.notification-content {
  display: flex;
  gap: 12px;
}

.notification-message {
  flex: 1;
}

.notification-title {
  font-weight: 600;
}
</style>
