<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import questService, { type UserQuest, UserQuestStatus } from '@/services/quest.service'
import GameQuestCard from '@/components/quests/GameQuestCard.vue'
import QuestAchievements from '@/components/quests/QuestAchievements.vue'

const userStore = useUserStore()

// Component refs
const achievementSystem = ref<InstanceType<typeof QuestAchievements> | null>(null)

// Reactive state - now using UserQuest types
const userQuests = ref<UserQuest[]>([])
const isLoading = ref(false)
const error = ref<string | null>(null)
const activeFilter = ref('ALL')
const showAchievements = ref(false)

// Mock user progress data (in real app, this would come from store/API)
const userLevel = ref(userStore.userLevel || 1)
const currentXP = ref(userStore.user?.experiencePoints || 150)
const totalQuestsCompleted = ref(3)
const questStreak = ref(2)

// Quest filters
const questFilters = ref([
  { type: 'ALL', label: 'All Quests', icon: '🌟' },
  { type: 'LEARNING', label: 'Learning', icon: '📚' },
  { type: 'SKILL_BUILDING', label: 'Skill Building', icon: '🔨' },
  { type: 'NETWORKING', label: 'Networking', icon: '🤝' },
  { type: 'INTEGRATION', label: 'Integration', icon: '🔗' },
  { type: 'CONTRIBUTION', label: 'Contribution', icon: '🎯' },
  { type: 'GITHUB', label: 'GitHub', icon: '🐙' }
])

// Computed properties
const filteredQuests = computed(() => {
  if (activeFilter.value === 'ALL') {
    return userQuests.value
  }
  return userQuests.value.filter(userQuest => userQuest.quest?.type === activeFilter.value)
})

// Methods
const loadQuests = async () => {
  isLoading.value = true
  error.value = null
  
  try {
    const response = await questService.getUserQuests()
    if (response.success && response.data) {
      userQuests.value = response.data
      console.log('✅ Loaded user quests:', userQuests.value.length)
    } else {
      error.value = response.error?.message || 'Failed to load quests'
    }
  } catch (err) {
    error.value = 'Failed to load quests. Please try again.'
    console.error('Error loading quests:', err)
  } finally {
    isLoading.value = false
  }
}

const setActiveFilter = (filterType: string) => {
  activeFilter.value = filterType
}

const startQuest = async (userQuest: UserQuest) => {
  console.log('Starting quest:', userQuest.quest?.title)
  
  try {
    const response = await questService.acceptQuest(userQuest.questId)
    if (response.success && response.data) {
      // Update local state with new UserQuest data
      const questIndex = userQuests.value.findIndex(q => q.questId === userQuest.questId)
      if (questIndex !== -1) {
        userQuests.value[questIndex] = response.data.userQuest
        console.log(`🚀 Quest "${userQuest.quest?.title}" started!`)
      }
    } else {
      console.error('Failed to start quest:', response.error?.message)
    }
  } catch (error) {
    console.error('Failed to start quest:', error)
  }
}

const completeQuest = async (userQuest: UserQuest) => {
  console.log('Completing quest:', userQuest.quest?.title)
  
  try {
    const response = await questService.completeQuest(userQuest.questId)
    if (response.success && response.data) {
      // Update local state with completed UserQuest
      const questIndex = userQuests.value.findIndex(q => q.questId === userQuest.questId)
      if (questIndex !== -1) {
        userQuests.value[questIndex] = response.data.userQuest
        
        // Update user progress
        totalQuestsCompleted.value++
        currentXP.value += response.data.experienceAwarded
        questStreak.value++ // Increment streak
        
        // Show success animation
        console.log(`✅ Quest "${userQuest.quest?.title}" completed! +${response.data.experienceAwarded} XP`)
        
        // Trigger achievement check
        setTimeout(() => {
          checkForLevelUp()
          checkForAchievements()
        }, 1000)
      }
    } else {
      console.error('Failed to complete quest:', response.error?.message)
    }
  } catch (error) {
    console.error('Failed to complete quest:', error)
  }
}

const abandonQuest = async (userQuest: UserQuest) => {
  console.log('Abandoning quest:', userQuest.quest?.title)
  
  try {
    const response = await questService.abandonQuest(userQuest.questId)
    if (response.success && response.data) {
      // Update local state
      const questIndex = userQuests.value.findIndex(q => q.questId === userQuest.questId)
      if (questIndex !== -1) {
        userQuests.value[questIndex] = response.data.userQuest
        console.log(`🛑 Quest "${userQuest.quest?.title}" abandoned`)
      }
    } else {
      console.error('Failed to abandon quest:', response.error?.message)
    }
  } catch (error) {
    console.error('Failed to abandon quest:', error)
  }
}
        startedAt: null
      }
      
      console.log(`❌ Quest "${quest.title}" abandoned`)
    }
  } catch (error) {
    console.error('Failed to abandon quest:', error)
  }
}

const viewQuestDetails = (userQuest: UserQuest) => {
  console.log('Viewing quest details:', userQuest.quest?.title)
  // Implement quest details modal/page
}

const onAchievementUnlocked = (achievement: any) => {
  console.log('Achievement unlocked:', achievement)
  currentXP.value += achievement.xpBonus
}

const onLevelUp = (newLevel: number) => {
  console.log('Level up!', newLevel)
  userLevel.value = newLevel
}

const checkForLevelUp = () => {
  const xpThresholds = [100, 200, 300, 500, 800, 1300, 2100]
  const currentThreshold = xpThresholds[userLevel.value - 1]
  
  if (currentXP.value >= currentThreshold && userLevel.value < 8) {
    const oldLevel = userLevel.value
    userLevel.value++
    
    console.log(`🎉 LEVEL UP! ${oldLevel} → ${userLevel.value}`)
    achievementSystem.value?.triggerLevelUpEffect()
  }
}

const checkForAchievements = () => {
  // Check for new achievements based on current progress
  const achievementChecks = [
    {
      id: 'first-quest',
      condition: totalQuestsCompleted.value >= 1,
      title: 'Sacred Beginning',
      xpBonus: 50
    },
    {
      id: 'quest-master', 
      condition: totalQuestsCompleted.value >= 10,
      title: 'Quest Master',
      xpBonus: 200
    },
    {
      id: 'streak-keeper',
      condition: questStreak.value >= 7,
      title: 'Streak Keeper', 
      xpBonus: 300
    }
  ]
  
  achievementChecks.forEach(achievement => {
    if (achievement.condition) {
      achievementSystem.value?.unlockAchievement({
        ...achievement,
        description: `Achievement unlocked: ${achievement.title}`,
        icon: '🏆',
        unlocked: true,
        progress: 1,
        required: 1,
        type: 'quest'
      })
    }
  })
}

// Lifecycle
onMounted(() => {
  loadQuests()
})
</script>

<template>
  <div class="quests-view">
    <!-- Quest Achievement System -->
    <QuestAchievements 
      ref="achievementSystem"
      :user-level="userLevel"
      :current-x-p="currentXP"
      :quests-completed="totalQuestsCompleted"
      :current-streak="questStreak"
      :show-gallery="showAchievements"
      @achievement-unlocked="onAchievementUnlocked"
      @level-up="onLevelUp"
    />

    <div class="quest-header-section">
      <h1>Sacred Quests</h1>
      
      <!-- User Progress Summary -->
      <div class="progress-summary">
        <div class="stat-card">
          <span class="stat-number">{{ totalQuestsCompleted }}</span>
          <span class="stat-label">Quests Completed</span>
        </div>
        <div class="stat-card">
          <span class="stat-number">{{ questStreak }}</span>
          <span class="stat-label">Current Streak</span>
        </div>
        <div class="stat-card">
          <span class="stat-number">{{ currentXP }}</span>
          <span class="stat-label">Total XP</span>
        </div>
        <button 
          class="achievements-toggle"
          @click="showAchievements = !showAchievements"
        >
          🏆 Achievements
        </button>
      </div>
    </div>
    
    <!-- Quest Filter Tabs -->
    <div class="quest-filters">
      <button 
        v-for="filter in questFilters" 
        :key="filter.type"
        :class="['filter-tab', { active: activeFilter === filter.type }]"
        @click="setActiveFilter(filter.type)"
      >
        <span class="filter-icon">{{ filter.icon }}</span>
        {{ filter.label }}
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="loading-spinner">
      <div class="spinner"></div>
      <p>Loading sacred quests...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-message">
      <p>{{ error }}</p>
      <button @click="loadQuests" class="retry-button">Retry</button>
    </div>

    <!-- Quests Grid with GameQuestCard -->
    <div v-else class="quests-grid">
      <GameQuestCard
        v-for="userQuest in filteredQuests" 
        :key="userQuest.id"
        :user-quest="userQuest"
        :user-level="userLevel"
        @accept-quest="startQuest"
        @complete-quest="completeQuest"
        @abandon-quest="abandonQuest"
        @quest-click="viewQuestDetails"
      />
    </div>

    <!-- Empty State -->
    <div v-if="!isLoading && !error && filteredQuests.length === 0" class="empty-state">
      <div class="empty-icon">🌟</div>
      <h3>No quests found</h3>
      <p>Try adjusting your filters or check back later for new quests.</p>
    </div>
  </div>
</template>

<style scoped>
.quests-view {
  min-height: 100vh;
  background: linear-gradient(135deg, 
    rgba(139, 92, 246, 0.05) 0%, 
    rgba(236, 72, 153, 0.05) 50%, 
    rgba(245, 158, 11, 0.05) 100%);
  padding: 20px;
}

.quest-header-section {
  text-align: center;
  margin-bottom: 32px;
}

.quest-header-section h1 {
  font-size: 48px;
  font-weight: 700;
  background: linear-gradient(135deg, #8b5cf6, #ec4899, #f59e0b);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0 0 16px 0;
  text-shadow: 0 4px 8px rgba(139, 92, 246, 0.2);
}

.progress-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
  max-width: 800px;
  margin: 0 auto 32px auto;
}

.stat-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 2px solid rgba(139, 92, 246, 0.2);
  border-radius: 16px;
  padding: 20px;
  text-align: center;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(139, 92, 246, 0.15);
}

.stat-number {
  display: block;
  font-size: 32px;
  font-weight: 700;
  color: #8b5cf6;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.achievements-toggle {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white;
  border: none;
  border-radius: 12px;
  padding: 16px 24px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 16px;
}

.achievements-toggle:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(245, 158, 11, 0.3);
}

.quest-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  margin-bottom: 32px;
  padding: 0 20px;
}

.filter-tab {
  background: rgba(255, 255, 255, 0.8);
  border: 2px solid rgba(139, 92, 246, 0.2);
  border-radius: 25px;
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #374151;
}

.filter-tab:hover {
  background: rgba(139, 92, 246, 0.1);
  border-color: rgba(139, 92, 246, 0.4);
  transform: translateY(-2px);
}

.filter-tab.active {
  background: linear-gradient(135deg, #8b5cf6, #a78bfa);
  color: white;
  border-color: #8b5cf6;
  box-shadow: 0 4px 15px rgba(139, 92, 246, 0.3);
}

.filter-icon {
  font-size: 18px;
}

.quests-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #6b7280;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(139, 92, 246, 0.2);
  border-top: 4px solid #8b5cf6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  text-align: center;
  padding: 40px 20px;
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
  border: 2px solid rgba(239, 68, 68, 0.2);
  border-radius: 12px;
  margin: 20px auto;
  max-width: 500px;
}

.retry-button {
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 12px 24px;
  margin-top: 16px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.retry-button:hover {
  background: #dc2626;
  transform: translateY(-2px);
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #6b7280;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 24px;
  margin: 0 0 8px 0;
  color: #374151;
}

.empty-state p {
  font-size: 16px;
  margin: 0;
}

/* Responsive design */
@media (max-width: 768px) {
  .quests-view {
    padding: 16px;
  }
  
  .quest-header-section h1 {
    font-size: 32px;
  }
  
  .progress-summary {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .quest-filters {
    justify-content: flex-start;
    overflow-x: auto;
    padding: 0 16px;
  }
  
  .filter-tab {
    flex-shrink: 0;
  }
  
  .quests-grid {
    grid-template-columns: 1fr;
    gap: 16px;
    padding: 0 16px;
  }
}

@media (max-width: 480px) {
  .progress-summary {
    grid-template-columns: 1fr;
  }
  
  .stat-card {
    padding: 16px;
  }
  
  .stat-number {
    font-size: 24px;
  }
}
</style>
