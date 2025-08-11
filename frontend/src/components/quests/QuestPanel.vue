<template>
  <div v-if="isVisible" class="quest-panel" :style="{ left: position.x + 'px', top: position.y + 'px' }">
    <!-- Sacred Geometry Background -->
    <div class="sacred-geometry-bg">
      <svg class="geometry-pattern" viewBox="0 0 100 100">
        <circle 
          v-for="n in 6" 
          :key="n"
          :cx="50 + 20 * Math.cos((n * Math.PI * 2) / 6)"
          :cy="50 + 20 * Math.sin((n * Math.PI * 2) / 6)"
          r="8"
          class="geometry-circle"
          :style="{ animationDelay: `${n * 0.1}s` }"
        />
      </svg>
    </div>

    <!-- Draggable Header -->
    <div 
      class="panel-header"
      @mousedown="startDrag"
      @touchstart="startDrag"
    >
      <div class="header-title">⚔️ Quest Journal</div>
      <div class="header-controls">
        <button @click="toggleMinimized" class="minimize-btn" :title="isMinimized ? 'Maximize' : 'Minimize'">
          {{ isMinimized ? '□' : '−' }}
        </button>
        <button @click="closePanel" class="close-btn" title="Close">×</button>
      </div>
    </div>

    <!-- Panel Content -->
    <div v-if="!isMinimized" class="panel-content">
      <!-- Quest Tabs -->
      <div class="quest-tabs">
        <button 
          v-for="tab in questTabs" 
          :key="tab.id"
          @click="activeTab = tab.id"
          class="quest-tab"
          :class="{ active: activeTab === tab.id }"
        >
          {{ tab.icon }} {{ tab.label }}
        </button>
      </div>

      <!-- Available Quests Tab -->
      <div v-if="activeTab === 'available'" class="tab-content">
        <div class="quest-filters">
          <select v-model="selectedFilter" class="filter-select">
            <option value="ALL">All Quests</option>
            <option value="ONBOARDING">Onboarding</option>
            <option value="LEARNING">Learning</option>
            <option value="CONTRIBUTION">Contribution</option>
            <option value="SOCIAL">Social</option>
            <option value="GITHUB">GitHub</option>
          </select>
        </div>

        <!-- Next Onboarding Quest Highlight -->
        <div v-if="nextOnboardingQuest" class="onboarding-highlight" @click="openOnboarding">
          <div class="highlight-icon">🌟</div>
          <div class="highlight-content">
            <div class="highlight-title">Continue Onboarding</div>
            <div class="highlight-quest-title">{{ nextOnboardingQuest.title }}</div>
            <div class="highlight-description">{{ nextOnboardingQuest.description }}</div>
            <div class="highlight-meta">
              <span class="level-badge">Level {{ onboardingProgressLevel || 1 }} ➜ Next</span>
              <span class="xp-badge">+{{ nextOnboardingQuest.xpReward }} XP</span>
            </div>
          </div>
          <button class="highlight-cta" @click.stop="openOnboarding">Open Onboarding</button>
        </div>
        
        <div class="quest-list">
          <div 
            v-for="quest in filteredAvailableQuests" 
            :key="quest.id"
            class="quest-item"
            :class="[`quest-${quest.type?.toLowerCase()}`, `difficulty-${quest.difficulty?.toLowerCase()}`]"
            @click="selectQuest(quest)"
          >
            <div class="quest-icon">{{ getQuestIcon(quest.type) }}</div>
            <div class="quest-info">
              <div class="quest-title">{{ quest.title }}</div>
              <div class="quest-description">{{ quest.description }}</div>
              <div class="quest-rewards">
                <span class="xp-reward">+{{ quest.experienceReward }} XP</span>
                <span class="difficulty">{{ quest.difficulty }}</span>
              </div>
            </div>
            <button 
              @click.stop="acceptQuest(quest)"
              class="accept-btn"
              :disabled="isQuestAccepted(quest.id)"
            >
              {{ isQuestAccepted(quest.id) ? 'Accepted' : 'Accept' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Active Quests Tab -->
      <div v-if="activeTab === 'active'" class="tab-content">
        <div class="quest-list">
          <template v-for="userQuest in activeQuests" :key="userQuest.id">
            <div 
              v-if="userQuest.quest"
              class="quest-item active-quest"
              @click="selectQuest(userQuest.quest)"
            >
              <div class="quest-icon">{{ getQuestIcon(userQuest.quest.type) }}</div>
              <div class="quest-info">
                <div class="quest-title">{{ userQuest.quest.title }}</div>
                <div class="quest-progress-bar">
                  <div class="progress-fill" :style="{ width: (userQuest.progress * 100) + '%' }"></div>
                  <span class="progress-text">{{ Math.floor(userQuest.progress * 100) }}%</span>
                </div>
                <div class="quest-objectives">
                  <div v-for="objective in getQuestObjectives(userQuest)" :key="objective.id" class="objective">
                    <span class="objective-status">{{ objective.completed ? '✓' : '○' }}</span>
                    <span class="objective-text">{{ objective.text }}</span>
                  </div>
                </div>
              </div>
              <div class="quest-actions">
                <button 
                  v-if="userQuest.progress >= 1"
                  @click.stop="completeQuest(userQuest.quest.id)"
                  class="complete-btn"
                >
                  Complete
                </button>
                <button 
                  @click.stop="abandonQuest(userQuest.quest.id)"
                  class="abandon-btn"
                >
                  Abandon
                </button>
              </div>
            </div>
          </template>
        </div>
      </div>

      <!-- Quest Details Panel -->
      <div v-if="selectedQuest" class="quest-details">
        <div class="details-header">
          <h3>{{ selectedQuest.title }}</h3>
          <span class="quest-type-badge">{{ selectedQuest.type }}</span>
        </div>
        <div class="details-content">
          <p class="quest-description">{{ selectedQuest.description }}</p>
          <div class="quest-rewards-detail">
            <div class="reward-item">
              <span class="reward-icon">⭐</span>
              <span>{{ selectedQuest.experienceReward }} Experience Points</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import questService, { UserQuestStatus, QuestType, type Quest, type UserQuest } from '@/services/quest.service'
import OnboardingService, { type OnboardingQuest } from '@/services/onboarding.service'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

// Component state
const isVisible = ref(true)
const isMinimized = ref(false)
const activeTab = ref('available')
const selectedFilter = ref('ALL')
const selectedQuest = ref<Quest | null>(null)
const isDragging = ref(false)
const position = ref({ x: 50, y: 50 })
const dragOffset = ref({ x: 0, y: 0 })

// Quest data
const availableQuests = ref<Quest[]>([])
const userQuests = ref<UserQuest[]>([])
const isLoading = ref(false)
// Onboarding integration
const nextOnboardingQuest = ref<OnboardingQuest | null>(null)
const onboardingProgressLevel = ref<number | null>(null)

// Quest tabs configuration
const questTabs = [
  { id: 'available', label: 'Available', icon: '📋' },
  { id: 'active', label: 'Active', icon: '⚡' }
]

// Computed properties
const filteredAvailableQuests = computed(() => {
  if (selectedFilter.value === 'ALL') {
    return availableQuests.value
  }
  return availableQuests.value.filter(quest => quest.type === selectedFilter.value)
})

const activeQuests = computed(() => {
  return userQuests.value.filter(uq => uq.status === UserQuestStatus.USER_ACTIVE)
})

// Quest utility functions
const getQuestIcon = (type: QuestType): string => {
  const icons: Record<QuestType, string> = {
    [QuestType.ONBOARDING]: '🌟',
    [QuestType.LEARNING]: '📚',
    [QuestType.CONTRIBUTION]: '🎯',
    [QuestType.COMMUNITY]: '🤝',
    [QuestType.GITHUB_ISSUE]: '🐙',
    [QuestType.SKILL_BUILDING]: '🔨',
    [QuestType.NETWORKING]: '🌐',
    [QuestType.INTEGRATION]: '🔗',
    [QuestType.GEOMETRY]: '📐'
  }
  return icons[type] || '📝'
}

const getQuestObjectives = (userQuest: UserQuest) => {
  const progress = userQuest.progress || 0
  const quest = userQuest.quest
  
  if (!quest) return []

  switch (quest.type) {
    case QuestType.ONBOARDING:
      return [{
        id: 'complete',
        text: quest.description || 'Complete onboarding task',
        completed: progress >= 1
      }]
    
    case QuestType.CONTRIBUTION:
      return [
        { id: 'research', text: 'Research the topic', completed: progress >= 0.25 },
        { id: 'create', text: 'Create contribution', completed: progress >= 0.75 },
        { id: 'submit', text: 'Submit for review', completed: progress >= 1 }
      ]
    
    case QuestType.LEARNING:
      return [
        { id: 'study', text: 'Study material', completed: progress >= 0.33 },
        { id: 'practice', text: 'Practice exercises', completed: progress >= 0.66 },
        { id: 'assess', text: 'Complete assessment', completed: progress >= 1 }
      ]
    
    default:
      return [{
        id: 'general',
        text: quest.description || 'Complete quest',
        completed: progress >= 1
      }]
  }
}

const isQuestAccepted = (questId: string) => {
  return userQuests.value.some(uq => uq.quest?.id === questId)
}

// Quest actions
const selectQuest = (quest: Quest) => {
  selectedQuest.value = quest
}

const acceptQuest = async (quest: Quest) => {
  if (!userStore.user?.id || isQuestAccepted(quest.id)) return
  
  try {
    const response = await questService.acceptQuest(quest.id)
    if (response.success) {
      await loadUserQuests()
      activeTab.value = 'active'
    }
  } catch (error) {
    console.error('Failed to accept quest:', error)
  }
}

const completeQuest = async (questId: string) => {
  try {
    const response = await questService.completeQuest(questId)
    if (response.success) {
      await loadUserQuests()
      // Show completion celebration
      showQuestCompletion()
    }
  } catch (error) {
    console.error('Failed to complete quest:', error)
  }
}

const abandonQuest = async (questId: string) => {
  if (!confirm('Are you sure you want to abandon this quest?')) return
  
  try {
    const response = await questService.abandonQuest(questId)
    if (response.success) {
      await loadUserQuests()
    }
  } catch (error) {
    console.error('Failed to abandon quest:', error)
  }
}

const showQuestCompletion = () => {
  // Emit event for quest notification system
  window.dispatchEvent(new CustomEvent('questCompleted'))
}

// Open onboarding flow and ensure backend marks onboarding ACTIVE
const openOnboarding = async () => {
  try {
    const role = userStore.user?.selectedRole
    if (role && nextOnboardingQuest.value) {
      await OnboardingService.accept(role, nextOnboardingQuest.value.level)
      // Refresh active quests so tracker/panel reflect ACTIVE from backend
      await loadUserQuests()
    }
  } catch (e) {
    // Non-blocking; continue navigation regardless
    console.warn('Onboarding accept failed or skipped:', e)
  } finally {
    router.push('/onboarding')
  }
}

// Panel controls
const toggleMinimized = () => {
  isMinimized.value = !isMinimized.value
}

const closePanel = () => {
  isVisible.value = false
}

// Dragging functionality
const startDrag = (event: MouseEvent | TouchEvent) => {
  isDragging.value = true
  const clientX = 'touches' in event ? event.touches[0].clientX : event.clientX
  const clientY = 'touches' in event ? event.touches[0].clientY : event.clientY
  
  dragOffset.value = {
    x: clientX - position.value.x,
    y: clientY - position.value.y
  }
  
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  document.addEventListener('touchmove', onDrag)
  document.addEventListener('touchend', stopDrag)
}

const onDrag = (event: MouseEvent | TouchEvent) => {
  if (!isDragging.value) return
  
  const clientX = 'touches' in event ? event.touches[0].clientX : event.clientX
  const clientY = 'touches' in event ? event.touches[0].clientY : event.clientY
  
  position.value = {
    x: Math.max(0, Math.min(window.innerWidth - 320, clientX - dragOffset.value.x)),
    y: Math.max(0, Math.min(window.innerHeight - 500, clientY - dragOffset.value.y))
  }
}

const stopDrag = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', stopDrag)
}

// Data loading
const loadAvailableQuests = async () => {
  try {
    const response = await questService.getQuests()
    if (response.success && response.data) {
      // Backend returns {success: true, quests: [...], count: ...}
      availableQuests.value = response.data.quests || []
    }
  } catch (error) {
    console.error('Failed to load available quests:', error)
  }
}

const loadUserQuests = async () => {
  if (!userStore.user?.id) return
  
  try {
    const response = await questService.getUserQuests()
    if (response.success && response.data) {
      userQuests.value = response.data
    }
  } catch (error) {
    console.error('Failed to load user quests:', error)
  }
}

const loadNextOnboardingQuest = async () => {
  if (!userStore.user?.id) return
  try {
    const progress = await OnboardingService.getUserOnboardingProgress(userStore.user.id)
    onboardingProgressLevel.value = progress.currentLevel
    // Stop showing if completed or reached final stage
    if (progress.onboardingCompleted || progress.currentLevel >= 4) {
      nextOnboardingQuest.value = null
      return
    }
    const role = userStore.user.selectedRole || progress.selectedRole || ''
    if (!role) {
      nextOnboardingQuest.value = null
      return
    }
    const recs = await OnboardingService.getQuestRecommendations(userStore.user.id, role)
    nextOnboardingQuest.value = recs[0] || null
  } catch (error) {
    console.warn('Failed to load next onboarding quest:', error)
    nextOnboardingQuest.value = null
  }
}

const loadData = async () => {
  isLoading.value = true
  await Promise.all([loadAvailableQuests(), loadUserQuests(), loadNextOnboardingQuest()])
  isLoading.value = false
}

// Lifecycle hooks
onMounted(() => {
  if (userStore.authInitialized && userStore.user?.id) {
    loadData()
  }
})

onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', stopDrag)
})

// Watch for user changes and auth initialization
watch(() => userStore.authInitialized, () => {
  if (userStore.authInitialized && userStore.user?.id) {
    loadData()
  }
})
watch(() => userStore.user?.id, (newUserId) => {
  if (newUserId) {
    loadData()
  }
})
watch(() => userStore.user?.selectedRole, () => {
  if (userStore.authInitialized && userStore.user?.id) {
    loadNextOnboardingQuest()
  }
})
</script>

<style scoped>
.quest-panel {
  position: fixed;
  width: 320px;
  max-height: 600px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  box-shadow: var(--shadow-glass);
  z-index: 1001;
  font-family: var(--font-sans);
  overflow: hidden;
  transition: all 0.3s ease;
}

.quest-panel:hover {
  border-color: rgba(212, 175, 55, 0.6);
  box-shadow: 0 20px 50px rgba(212, 175, 55, 0.2);
}

.sacred-geometry-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.geometry-pattern {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  opacity: 0.03;
}

.geometry-circle {
  fill: none;
  stroke: #d4af37;
  stroke-width: 0.5;
  animation: geometryRotate 8s linear infinite;
}

@keyframes geometryRotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.panel-header {
  background: rgba(255, 255, 255, 0.08);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  padding: 12px 16px;
  cursor: move;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text);
}

.header-controls {
  display: flex;
  gap: 8px;
}

.minimize-btn, .close-btn {
  background: none;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  font-size: 16px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.minimize-btn:hover, .close-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: var(--color-text);
}

.panel-content {
  position: relative;
  z-index: 1;
  max-height: 520px;
  overflow-y: auto;
}

.quest-tabs {
  display: flex;
  background: rgba(255, 255, 255, 0.06);
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}

.quest-tab {
  flex: 1;
  padding: 12px;
  background: none;
  border: none;
  cursor: pointer;
  font-family: var(--font-sans);
  font-weight: 600;
  color: var(--color-text-muted);
  transition: all 0.2s ease;
}

.quest-tab.active {
  background: rgba(255, 255, 255, 0.08);
  color: var(--color-text);
  border-bottom: 2px solid var(--color-primary);
}

.quest-tab:hover:not(.active) {
  background: rgba(212, 175, 55, 0.05);
  color: #34495e;
}

.tab-content {
  padding: 16px;
}

.quest-filters {
  margin-bottom: 16px;
}

.filter-select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  font-family: var(--font-sans);
  background: rgba(255, 255, 255, 0.06);
  color: var(--color-text);
}

.quest-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.onboarding-highlight {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid rgba(212, 175, 55, 0.4);
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.12), rgba(241, 196, 15, 0.08));
  box-shadow: 0 8px 24px rgba(212, 175, 55, 0.15);
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.onboarding-highlight:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 30px rgba(212, 175, 55, 0.25);
}

.highlight-icon {
  font-size: 22px;
}

.highlight-content {
  flex: 1;
}

.highlight-title {
  font-size: 12px;
  font-weight: 700;
  color: #d4af37;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.highlight-quest-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text);
}

.highlight-description {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 2px;
}

.highlight-meta {
  display: flex;
  gap: 8px;
  margin-top: 6px;
}

.level-badge, .xp-badge {
  background: rgba(255, 255, 255, 0.08);
  color: var(--color-text);
  padding: 2px 6px;
  border-radius: 6px;
  font-size: 10px;
  font-weight: 700;
}

.highlight-cta {
  background: linear-gradient(135deg, #d4af37, #f1c40f);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 8px 10px;
  font-family: var(--font-sans);
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.highlight-cta:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(212, 175, 55, 0.4);
}

.quest-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: rgba(255, 255, 255, 0.08);
}

.quest-item:hover {
  border-color: rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.12);
  transform: translateY(-1px);
}

.quest-icon {
  font-size: 20px;
  margin-right: 12px;
  margin-top: 2px;
}

.quest-info {
  flex: 1;
}

.quest-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 4px;
}

.quest-description {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 8px;
  line-height: 1.4;
}

.quest-rewards {
  display: flex;
  gap: 8px;
  font-size: 11px;
}

.xp-reward {
  background: rgba(255, 255, 255, 0.08);
  color: var(--color-text);
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
}

.difficulty {
  background: rgba(255, 255, 255, 0.06);
  color: var(--color-text-muted);
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
}

.accept-btn, .complete-btn, .abandon-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-family: var(--font-sans);
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.accept-btn {
  background: var(--gradient-primary);
  color: white;
}

.accept-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(39, 174, 96, 0.3);
}

.accept-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.complete-btn {
  background: linear-gradient(135deg, #22c55e, #16a34a);
  color: white;
  margin-bottom: 4px;
}

.complete-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(212, 175, 55, 0.3);
}

.abandon-btn {
  background: linear-gradient(135deg, #ef4444, #b91c1c);
  color: white;
  font-size: 10px;
}

.abandon-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.3);
}

.quest-progress-bar {
  position: relative;
  background: rgba(189, 195, 199, 0.3);
  border-radius: 10px;
  height: 16px;
  margin-bottom: 8px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #d4af37, #f1c40f);
  transition: width 0.3s ease;
  border-radius: 10px;
}

.progress-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 10px;
  font-weight: 600;
  color: #2c3e50;
}

.quest-objectives {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.objective {
  display: flex;
  align-items: center;
  font-size: 11px;
  color: #7f8c8d;
}

.objective-status {
  margin-right: 6px;
  font-weight: bold;
}

.quest-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-left: 8px;
}

.quest-details {
  border-top: 1px solid rgba(255, 255, 255, 0.15);
  padding: 16px;
  background: rgba(255, 255, 255, 0.06);
}

.details-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.details-header h3 {
  margin: 0;
  color: var(--color-text);
  font-size: 16px;
}

.quest-type-badge {
  background: rgba(255, 255, 255, 0.08);
  color: var(--color-text);
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 10px;
  font-weight: 600;
}

.details-content {
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 1.5;
}

.quest-requirements {
  margin: 12px 0;
}

.quest-requirements h4 {
  margin: 0 0 6px 0;
  color: #34495e;
  font-size: 12px;
}

.quest-requirements ul {
  margin: 0;
  padding-left: 16px;
}

.quest-rewards-detail {
  margin-top: 12px;
}

.reward-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #d4af37;
  font-weight: 600;
}

/* Mobile responsiveness */
@media (max-width: 768px) {
  .quest-panel {
    width: calc(100vw - 20px);
    max-width: 320px;
    left: 10px !important;
  }
}
</style>
