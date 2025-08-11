<template>
  <div class="quest-tracker" :class="{ collapsed: isCollapsed }">
    <!-- Sacred Geometry Background -->
    <div class="sacred-geometry-bg">
      <svg class="geometry-pattern" viewBox="0 0 100 100">
        <polygon 
          points="50,15 85,35 85,65 50,85 15,65 15,35"
          class="geometry-hexagon"
        />
        <circle cx="50" cy="50" r="20" class="geometry-inner-circle" />
      </svg>
    </div>

    <!-- Collapse Toggle -->
    <button 
      @click="toggleCollapsed" 
      class="collapse-toggle"
      :title="isCollapsed ? 'Expand Quest Tracker' : 'Collapse Quest Tracker'"
    >
      <span class="toggle-icon">{{ isCollapsed ? '◀' : '▶' }}</span>
      <span v-if="!isCollapsed" class="toggle-text">Quest Tracker</span>
    </button>

    <!-- Tracker Content -->
    <div v-if="!isCollapsed" class="tracker-content">
      <!-- Active Quests Summary -->
      <div class="tracker-header">
        <div class="header-title">🎯 Active Quests</div>
  <div class="quest-count">{{ headerActiveCount }}/{{ maxActiveQuests }}</div>
      </div>

      <!-- Quest Navigation -->
      <div v-if="activeQuests.length > 0" class="quest-navigation">
        <div class="nav-controls">
          <button 
            @click="previousQuest" 
            class="nav-btn"
            :disabled="currentQuestIndex === 0"
          >
            ↑
          </button>
          <span class="quest-indicator">
            {{ currentQuestIndex + 1 }} / {{ activeQuests.length }}
          </span>
          <button 
            @click="nextQuest" 
            class="nav-btn"
            :disabled="currentQuestIndex === activeQuests.length - 1"
          >
            ↓
          </button>
        </div>
      </div>

  <!-- Current Quest Display -->
  <div v-if="currentQuest && currentQuest.quest" class="current-quest">
        <!-- Quest Header -->
        <div class="quest-header">
          <div class="quest-icon">{{ getQuestIcon(currentQuest.quest.type) }}</div>
          <div class="quest-info">
            <div class="quest-title">{{ currentQuest.quest.title }}</div>
            <div class="quest-type">{{ currentQuest.quest.type }}</div>
          </div>
          <div class="quest-level">L{{ currentQuest.quest.requiredLevel || 1 }}</div>
        </div>

        <!-- Progress Bar -->
        <div class="progress-section">
          <div class="progress-bar">
            <div 
              class="progress-fill" 
              :style="{ width: (currentQuest.progress * 100) + '%' }"
            >
              <div class="progress-glow"></div>
            </div>
          </div>
          <div class="progress-text">{{ Math.floor(currentQuest.progress * 100) }}%</div>
        </div>

        <!-- Objectives List -->
        <div class="objectives-section">
          <div class="objectives-header">Objectives:</div>
          <div class="objectives-list">
            <div 
              v-for="objective in getQuestObjectives(currentQuest)" 
              :key="objective.id"
              class="objective-item"
              :class="{ completed: objective.completed }"
            >
              <div class="objective-status">
                <div v-if="objective.completed" class="checkmark">✓</div>
                <div v-else class="checkbox"></div>
              </div>
              <span class="objective-text">{{ objective.text }}</span>
            </div>
          </div>
        </div>

        <!-- Quest Actions -->
        <div class="quest-actions">
          <button 
            v-if="currentQuest.progress >= 1"
            @click="completeQuest(currentQuest.quest?.id || '')"
            class="complete-btn"
          >
            ⭐ Complete Quest
          </button>
          <button 
            @click="abandonQuest(currentQuest.quest?.id || '')"
            class="abandon-btn"
          >
            🗑️ Abandon
          </button>
        </div>

        <!-- Rewards Preview -->
        <div class="rewards-preview">
          <div class="reward-item">
            <span class="reward-icon">⭐</span>
            <span class="reward-text">{{ currentQuest.quest?.experienceReward || 0 }} XP</span>
          </div>
        </div>
      </div>

      <!-- No Active Quests -->
  <div v-else class="no-quests">
        <!-- Onboarding highlight when not completed -->
        <div 
          v-if="showOnboardingCard && nextOnboardingQuest"
          class="onboarding-highlight"
          @click="openOnboarding"
        >
          <div class="highlight-icon">🌟</div>
          <div class="highlight-content">
            <div class="highlight-title">Continue Onboarding</div>
            <div class="highlight-quest-title">{{ nextOnboardingQuest.title }}</div>
            <div class="highlight-description">{{ nextOnboardingQuest.description }}</div>
            <div class="highlight-meta">
              <span class="level-badge">Next step</span>
              <span class="xp-badge">+{{ nextOnboardingQuest.xpReward }} XP</span>
            </div>
          </div>
          <button class="highlight-cta" @click.stop="openOnboarding">Open</button>
        </div>

        <template v-if="!(showOnboardingCard && nextOnboardingQuest)">
          <div class="no-quests-icon">📋</div>
          <div class="no-quests-text">No active quests</div>
          <div class="no-quests-hint">Visit the Quest Panel to accept new quests!</div>
        </template>
      </div>

      <!-- Quick Stats -->
      <div class="quick-stats">
        <div class="stat-item">
          <span class="stat-icon">🏆</span>
          <span class="stat-text">{{ completedQuestsCount }} Completed</span>
        </div>
        <div class="stat-item">
          <span class="stat-icon">🔥</span>
          <span class="stat-text">{{ questStreak }} Day Streak</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import questService, { UserQuestStatus, QuestType, type UserQuest } from '@/services/quest.service'
import OnboardingService, { type OnboardingQuest } from '@/services/onboarding.service'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

// Component state
const isCollapsed = ref(false)
const currentQuestIndex = ref(0)
const userQuests = ref<UserQuest[]>([])
const maxActiveQuests = ref(5)
const completedQuestsCount = ref(0)
const questStreak = ref(0)
// Onboarding integration
const showOnboardingCard = ref(false)
const nextOnboardingQuest = ref<OnboardingQuest | null>(null)

// Computed properties
const activeQuests = computed(() => {
  return userQuests.value.filter(uq => uq.status === UserQuestStatus.USER_ACTIVE)
})

const currentQuest = computed(() => {
  return activeQuests.value[currentQuestIndex.value] || null
})

// Count for header: if no active quests but onboarding card exists, count it as 1
const headerActiveCount = computed(() => {
  const count = activeQuests.value.length
  if (count > 0) return count
  return nextOnboardingQuest.value ? 1 : 0
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
        { id: 'create', text: 'Create your contribution', completed: progress >= 0.75 },
        { id: 'submit', text: 'Submit for review', completed: progress >= 1 }
      ]
    
    case QuestType.LEARNING:
      return [
        { id: 'study', text: 'Study the material', completed: progress >= 0.33 },
        { id: 'practice', text: 'Complete exercises', completed: progress >= 0.66 },
        { id: 'assess', text: 'Pass assessment', completed: progress >= 1 }
      ]
    
    case QuestType.COMMUNITY:
      return [
        { id: 'connect', text: 'Connect with community', completed: progress >= 0.5 },
        { id: 'participate', text: 'Participate in discussions', completed: progress >= 1 }
      ]
    
    case QuestType.GITHUB_ISSUE:
      return [
        { id: 'setup', text: 'Set up GitHub integration', completed: progress >= 0.2 },
        { id: 'fork', text: 'Fork the repository', completed: progress >= 0.4 },
        { id: 'code', text: 'Make your changes', completed: progress >= 0.8 },
        { id: 'pr', text: 'Submit pull request', completed: progress >= 1 }
      ]
    
    default:
      return [{
        id: 'general',
        text: quest.description || 'Complete the quest',
        completed: progress >= 1
      }]
  }
}

// Navigation functions
const nextQuest = () => {
  if (currentQuestIndex.value < activeQuests.value.length - 1) {
    currentQuestIndex.value++
  }
}

const previousQuest = () => {
  if (currentQuestIndex.value > 0) {
    currentQuestIndex.value--
  }
}

const toggleCollapsed = () => {
  isCollapsed.value = !isCollapsed.value
}

const openOnboarding = async () => {
  try {
    const role = userStore.user?.selectedRole
    if (role && nextOnboardingQuest.value) {
      await OnboardingService.accept(role, nextOnboardingQuest.value.level)
      await loadUserQuests()
    }
  } catch (e) {
    console.warn('Onboarding accept failed or skipped:', e)
  } finally {
    router.push('/onboarding')
  }
}

// Quest actions
const completeQuest = async (questId: string) => {
  try {
    const response = await questService.completeQuest(questId)
    if (response.success) {
      await loadUserQuests()
      // Show completion notification
      showQuestCompletion()
      // Adjust current index if needed
      if (currentQuestIndex.value >= activeQuests.value.length && activeQuests.value.length > 0) {
        currentQuestIndex.value = activeQuests.value.length - 1
      }
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
      // Adjust current index if needed
      if (currentQuestIndex.value >= activeQuests.value.length && activeQuests.value.length > 0) {
        currentQuestIndex.value = activeQuests.value.length - 1
      }
    }
  } catch (error) {
    console.error('Failed to abandon quest:', error)
  }
}

const showQuestCompletion = () => {
  // Emit event for quest notification system
  window.dispatchEvent(new CustomEvent('questCompleted'))
}

// Data loading
const loadUserQuests = async () => {
  if (!userStore.user?.id) return
  
  try {
    const response = await questService.getUserActiveQuests()
    if (response.success && response.data) {
      userQuests.value = response.data
      
      // Calculate stats
      completedQuestsCount.value = response.data.filter(uq => uq.status === UserQuestStatus.USER_COMPLETED).length
      
      // Calculate quest streak (simplified - in real app would be based on completion dates)
      questStreak.value = Math.min(completedQuestsCount.value, 7)
    }
  } catch (error) {
    console.error('Failed to load user quests:', error)
  }
}

const loadNextOnboardingQuest = async () => {
  if (!userStore.user?.id) return
  try {
    const progress = await OnboardingService.getUserOnboardingProgress(userStore.user.id)
    if (progress.onboardingCompleted || progress.currentLevel >= 4) {
      showOnboardingCard.value = false
      nextOnboardingQuest.value = null
      return
    }
    const role = userStore.user.selectedRole || progress.selectedRole || ''
    if (!role) {
      showOnboardingCard.value = false
      nextOnboardingQuest.value = null
      return
    }
    const recs = await OnboardingService.getQuestRecommendations(userStore.user.id, role)
    nextOnboardingQuest.value = recs[0] || null
    showOnboardingCard.value = !!nextOnboardingQuest.value
  } catch (e) {
    showOnboardingCard.value = false
    nextOnboardingQuest.value = null
  }
}

// Lifecycle hooks
onMounted(() => {
  if (userStore.authInitialized && userStore.user?.id) {
    loadUserQuests()
  loadNextOnboardingQuest()
    
    // Set up periodic refresh
    setInterval(loadUserQuests, 30000) // Refresh every 30 seconds
  }
  // Refresh when onboarding is accepted elsewhere
  window.addEventListener('onboardingAccepted', loadUserQuests)
})

onUnmounted(() => {
  window.removeEventListener('onboardingAccepted', loadUserQuests)
})

// Watch for user changes and auth initialization
watch(() => userStore.authInitialized, () => {
  if (userStore.authInitialized && userStore.user?.id) {
    loadUserQuests()
  loadNextOnboardingQuest()
  }
})

watch(() => userStore.user?.id, (newUserId) => {
  if (userStore.authInitialized && newUserId) {
    loadUserQuests()
  loadNextOnboardingQuest()
  }
})

// Watch for quest changes to reset index
watch(() => activeQuests.value.length, (newLength) => {
  if (currentQuestIndex.value >= newLength && newLength > 0) {
    currentQuestIndex.value = newLength - 1
  } else if (newLength === 0) {
    currentQuestIndex.value = 0
  }
})

watch(() => userStore.user?.selectedRole, () => {
  if (userStore.authInitialized && userStore.user?.id) {
    loadNextOnboardingQuest()
  }
})
</script>

<style scoped>
.quest-tracker {
  position: fixed;
  top: 50%;
  right: 20px;
  transform: translateY(-50%);
  width: 280px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  box-shadow: var(--shadow-glass);
  z-index: 1000;
  font-family: var(--font-sans);
  transition: all 0.3s ease;
  overflow: hidden;
}

.quest-tracker.collapsed {
  width: 50px;
}

.quest-tracker:hover {
  border-color: rgba(212, 175, 55, 0.5);
  box-shadow: 0 16px 40px rgba(212, 175, 55, 0.15);
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
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 150%;
  height: 150%;
  opacity: 0.03;
}

.geometry-hexagon {
  fill: none;
  stroke: #d4af37;
  stroke-width: 0.5;
  animation: geometryPulse 4s ease-in-out infinite;
}

.geometry-inner-circle {
  fill: none;
  stroke: #d4af37;
  stroke-width: 0.3;
  animation: geometryRotate 6s linear infinite reverse;
}

@keyframes geometryPulse {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 0.7; transform: scale(1.05); }
}

@keyframes geometryRotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.collapse-toggle {
  background: rgba(255, 255, 255, 0.08);
  border: none;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  width: 100%;
  padding: 12px;
  cursor: pointer;
  font-family: var(--font-sans);
  font-weight: 600;
  color: var(--color-text);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s ease;
  position: relative;
  z-index: 1;
}

.collapse-toggle:hover {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.2), rgba(241, 196, 15, 0.2));
  color: #d4af37;
}

.toggle-icon {
  font-size: 14px;
  transition: transform 0.3s ease;
}

.toggle-text {
  font-size: 14px;
}

.tracker-content {
  position: relative;
  z-index: 1;
  padding: 16px;
  max-height: 500px;
  overflow-y: auto;
}

.tracker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text);
}

.quest-count {
  background: rgba(255, 255, 255, 0.08);
  color: var(--color-text);
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.quest-navigation {
  margin-bottom: 16px;
}

.nav-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 8px;
  padding: 8px;
}

.nav-btn {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  width: 30px;
  height: 30px;
  cursor: pointer;
  font-size: 14px;
  color: var(--color-text);
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-btn:hover:not(:disabled) {
  background: rgba(212, 175, 55, 0.2);
  transform: translateY(-1px);
}

.nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quest-indicator {
  font-size: 12px;
  font-weight: 600;
  color: #7f8c8d;
  min-width: 50px;
  text-align: center;
}

.current-quest {
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.08);
  margin-bottom: 16px;
}

.quest-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.quest-icon {
  font-size: 24px;
  margin-right: 12px;
}

.quest-info {
  flex: 1;
}

.quest-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 2px;
  line-height: 1.3;
}

.quest-type {
  font-size: 10px;
  color: #7f8c8d;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.quest-level {
  background: rgba(52, 73, 94, 0.1);
  color: #34495e;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 10px;
  font-weight: 600;
}

.progress-section {
  margin-bottom: 12px;
}

.progress-bar {
  position: relative;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  height: 20px;
  overflow: hidden;
  margin-bottom: 4px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #d4af37, #f1c40f);
  transition: width 0.3s ease;
  border-radius: 10px;
  position: relative;
}

.progress-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: progressGlow 2s ease-in-out infinite;
}

@keyframes progressGlow {
  0%, 100% { transform: translateX(-100%); }
  50% { transform: translateX(100%); }
}

.progress-text {
  text-align: center;
  font-size: 11px;
  font-weight: 600;
  color: var(--color-text-muted);
}

.objectives-section {
  margin-bottom: 12px;
}

.objectives-header {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}

.objectives-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.objective-item {
  display: flex;
  align-items: center;
  padding: 6px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.objective-item:hover {
  background: rgba(212, 175, 55, 0.05);
}

.objective-item.completed {
  opacity: 0.7;
}

.objective-status {
  margin-right: 8px;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.checkmark {
  background: #d4af37;
  color: white;
  border-radius: 50%;
  width: 14px;
  height: 14px;
  font-size: 10px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
}

.checkbox {
  border: 2px solid #bdc3c7;
  border-radius: 50%;
  width: 14px;
  height: 14px;
}

.objective-text {
  flex: 1;
  font-size: 11px;
  color: var(--color-text-muted);
  line-height: 1.3;
}

.objective-item.completed .objective-text {
  text-decoration: line-through;
  color: #bdc3c7;
}

.quest-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.complete-btn {
  background: linear-gradient(135deg, #d4af37, #f1c40f);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 8px 12px;
  font-family: 'Cinzel', serif;
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(212, 175, 55, 0.3);
}

.complete-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(212, 175, 55, 0.4);
}

.abandon-btn {
  background: rgba(231, 76, 60, 0.1);
  color: #e74c3c;
  border: 1px solid rgba(231, 76, 60, 0.3);
  border-radius: 6px;
  padding: 6px 12px;
  font-family: 'Cinzel', serif;
  font-size: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.abandon-btn:hover {
  background: rgba(231, 76, 60, 0.2);
  border-color: rgba(231, 76, 60, 0.5);
}

.rewards-preview {
  border-top: 1px solid rgba(212, 175, 55, 0.2);
  padding-top: 8px;
}

.reward-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: var(--color-primary);
  font-weight: 600;
}

.reward-icon {
  font-size: 12px;
}

.no-quests {
  text-align: center;
  padding: 24px 16px;
  color: #7f8c8d;
}

.no-quests-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.no-quests-text {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}

.no-quests-hint {
  font-size: 11px;
  line-height: 1.4;
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
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.onboarding-highlight:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 30px rgba(212, 175, 55, 0.25);
}

.highlight-icon { font-size: 20px; }
.highlight-content { flex: 1; }
.highlight-title {
  font-size: 12px;
  font-weight: 700;
  color: #d4af37;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.highlight-quest-title { font-size: 14px; font-weight: 700; color: var(--color-text); }
.highlight-description { font-size: 12px; color: var(--color-text-muted); margin-top: 2px; }
.highlight-meta { display: flex; gap: 8px; margin-top: 6px; }
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
  padding: 6px 8px;
  font-family: var(--font-sans);
  font-size: 10px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}
.highlight-cta:hover { transform: translateY(-1px); box-shadow: 0 6px 16px rgba(212, 175, 55, 0.4); }

.quick-stats {
  border-top: 1px solid rgba(212, 175, 55, 0.2);
  padding-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  color: #7f8c8d;
}

.stat-icon {
  font-size: 12px;
}

/* Mobile responsiveness */
@media (max-width: 768px) {
  .quest-tracker {
    right: 10px;
    width: 260px;
  }
  
  .quest-tracker.collapsed {
    width: 45px;
  }
}
</style>
