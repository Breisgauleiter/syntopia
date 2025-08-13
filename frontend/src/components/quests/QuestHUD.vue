<template>
  <div v-if="isVisible && primaryQuest" class="quest-hud">
    <div class="quest-header">
      <div class="quest-title">{{ primaryQuest.title }}</div>
      <div class="quest-progress">{{ questProgress }}%</div>
      <button @click="toggleVisibility" class="minimize-btn">−</button>
    </div>
    <div class="objectives-list">
      <div 
        v-for="objective in objectives" 
        :key="objective.id"
        class="objective-item"
        :class="{ completed: objective.completed }"
      >
        <div class="objective-checkbox">
          <div v-if="objective.completed" class="checkmark">✓</div>
        </div>
        <span class="objective-text">{{ objective.text }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from "vue"
import { useUserStore } from "@/stores/user"
import questService, { UserQuestStatus, type Quest, type UserQuest } from "@/services/quest.service"

const userStore = useUserStore()
const primaryQuest = ref<Quest | null>(null)
const objectives = ref<Array<{ id: string; text: string; completed: boolean }>>([])
const isVisible = ref(true)
const questProgress = ref(0)

const toggleVisibility = () => {
  isVisible.value = !isVisible.value
}

const loadPrimaryQuest = async () => {
  if (!userStore.user?.id) return
  try {
    const response = await questService.getUserQuests()
    if (response.success && response.data) {
      const list = Array.isArray((response.data as any).data) ? (response.data as any).data : response.data
      const activeQuests = list.filter((uq: any) => uq.status === UserQuestStatus.USER_ACTIVE)
      if (activeQuests.length > 0 && activeQuests[0].quest) {
        primaryQuest.value = activeQuests[0].quest
        const description = primaryQuest.value ? primaryQuest.value.description : ''
        objectives.value = [{ id: "1", text: description || "Complete quest", completed: false }]
        // Backend progress likely 0-100 already or 0-1? Normalize: if <=1 treat as fraction
        const rawProgress = activeQuests[0].progress || 0
        questProgress.value = rawProgress <= 1 ? Math.floor(rawProgress * 100) : Math.floor(rawProgress)
      }
    }
  } catch (error) {
    console.error("Failed to load quest:", error)
  }
}

watch(() => userStore.user?.id, () => {
  if (userStore.authInitialized && userStore.user?.id) loadPrimaryQuest()
})

watch(() => userStore.authInitialized, () => {
  if (userStore.authInitialized && userStore.user?.id) loadPrimaryQuest()
})

onMounted(() => {
  if (userStore.authInitialized && userStore.user?.id) loadPrimaryQuest()
})
</script>

<style scoped>
.quest-hud {
  position: fixed;
  top: 20px;
  left: 20px;
  width: 300px;
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid rgba(212, 175, 55, 0.3);
  border-radius: 12px;
  padding: 16px;
  z-index: 1000;
  font-family: "Cinzel", serif;
}
.quest-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.quest-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  flex: 1;
}
.quest-progress {
  font-size: 14px;
  color: #d4af37;
  font-weight: bold;
  margin-right: 8px;
}
.minimize-btn {
  background: none;
  border: none;
  color: #7f8c8d;
  cursor: pointer;
  font-size: 18px;
  padding: 4px;
}
.objectives-list {
  margin-bottom: 12px;
}
.objective-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  padding: 6px;
}
.objective-checkbox {
  width: 16px;
  height: 16px;
  border: 2px solid #d4af37;
  border-radius: 3px;
  margin-right: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.objective-item.completed .objective-checkbox {
  background: #d4af37;
}
.checkmark {
  color: white;
  font-size: 12px;
  font-weight: bold;
}
.objective-text {
  flex: 1;
  font-size: 14px;
  color: #34495e;
}
</style>
