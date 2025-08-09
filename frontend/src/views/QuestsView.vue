<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import questService, { 
  Quest, 
  QuestType, 
  QuestDifficulty, 
  QuestStatus, 
  QuestFilter 
} from '@/services/quest.service'
import onboardingService, { OnboardingQuest } from '@/services/onboarding.service'

const userStore = useUserStore()

// Reactive state
const quests = ref<Quest[]>([])
const availableQuests = ref<Quest[]>([])
const completedQuests = ref<Quest[]>([])
const onboardingQuests = ref<OnboardingQuest[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const selectedQuest = ref<Quest | null>(null)
const showQuestModal = ref(false)
const showExperienceAnimation = ref(false)
const experienceGained = ref(0)

// Tab management
const activeTab = ref<'available' | 'completed' | 'github'>('available')

// Filtering state
const filters = ref<QuestFilter>({
  type: undefined,
  difficulty: undefined,
  role: undefined,
  status: QuestStatus.AVAILABLE
})

const sortBy = ref<'title' | 'difficulty' | 'experience' | 'level'>('title')
const sortDirection = ref<'asc' | 'desc'>('asc')

// Quest statistics
const questStats = ref({
  totalAvailable: 0,
  totalCompleted: 0,
  totalExperience: 0,
  averageDifficulty: 0
})

/**
 * Load quests from API
 */
const loadQuests = async () => {
  loading.value = true
  error.value = null
  
  try {
    // Load all quests
    const allQuestsResponse = await questService.getAllQuests()
    if (allQuestsResponse.success && allQuestsResponse.data?.quests) {
      quests.value = allQuestsResponse.data.quests
    }

    // Load available quests for current user
    if (userStore.user?.id) {
      const availableResponse = await questService.getAvailableQuests(userStore.user.id)
      if (availableResponse.success && availableResponse.data?.quests) {
        availableQuests.value = availableResponse.data.quests
      }
    }

    // Load quest statistics
    const statsResponse = await questService.getQuestStats()
    if (statsResponse.success) {
      questStats.value = {
        ...questStats.value,
        ...statsResponse.data
      }
    }

    // Filter completed quests
    completedQuests.value = quests.value.filter(quest => quest.status === QuestStatus.COMPLETED)

    // Load onboarding quests if user has selected a role
    await loadOnboardingQuests()

  } catch (err) {
    console.error('Error loading quests:', err)
    error.value = 'Failed to load quests. Please try again.'
  } finally {
    loading.value = false
  }
}

/**
 * Load onboarding quests for the current user's role
 */
const loadOnboardingQuests = async () => {
  if (!userStore.userRole || userStore.userRole === 'None') {
    onboardingQuests.value = []
    return
  }

  try {
    const currentLevel = userStore.userLevel
    const role = userStore.userRole
    
    // Load onboarding quests for levels 1-4
    const questPromises = []
    for (let level = 1; level <= 4; level++) {
      questPromises.push(onboardingService.getQuestByRoleAndLevel(role, level))
    }
    
    const quests = await Promise.all(questPromises)
    onboardingQuests.value = quests
  } catch (err) {
    console.warn('Failed to load onboarding quests:', err)
  }
}

// Computed properties
const hasActiveOnboardingQuests = computed(() => {
  return userStore.userRole && 
         userStore.userRole !== 'None' && 
         userStore.userLevel <= 4 &&
         onboardingQuests.value.length > 0
})

const nextOnboardingQuest = computed(() => {
  if (!hasActiveOnboardingQuests.value) return null
  
  const currentLevel = userStore.userLevel
  return onboardingQuests.value.find(quest => quest.level === currentLevel)
})

/**
 * Accept a quest
 */
const acceptQuest = async (quest: Quest) => {
  if (!userStore.user?.id) {
    error.value = 'You must be logged in to accept quests'
    return
  }

  loading.value = true
  
  try {
    const response = await questService.acceptQuest(quest.id, userStore.user.id)
    
    if (response.success) {
      // Update quest status
      const questIndex = quests.value.findIndex(q => q.id === quest.id)
      if (questIndex !== -1) {
        quests.value[questIndex].status = QuestStatus.ACTIVE
      }

      // Update user data if returned
      if (response.data?.user) {
        userStore.updateUserData(response.data.user)
      }

      // Show success message
      console.log('✅ Quest accepted successfully:', response.data?.message)
      
      // Reload quests to get updated data
      await loadQuests()
      
    } else {
      error.value = response.error?.message || 'Failed to accept quest'
    }
  } catch (err) {
    console.error('Error accepting quest:', err)
    error.value = 'Failed to accept quest. Please try again.'
  } finally {
    loading.value = false
  }
}

/**
 * Complete a quest
 */
const completeQuest = async (quest: Quest) => {
  if (!userStore.user?.id) {
    error.value = 'You must be logged in to complete quests'
    return
  }

  loading.value = true
  
  try {
    const response = await questService.completeQuest(quest.id, userStore.user.id)
    
    if (response.success) {
      // Update quest status
      const questIndex = quests.value.findIndex(q => q.id === quest.id)
      if (questIndex !== -1) {
        quests.value[questIndex].status = QuestStatus.COMPLETED
      }

      // Update user data and show experience animation
      if (response.data?.user) {
        const oldExperience = userStore.user?.experiencePoints || 0
        userStore.updateUserData(response.data.user)
        
        // Show experience gained animation
        experienceGained.value = quest.experienceReward
        showExperienceAnimation.value = true
        
        // Hide animation after 3 seconds
        setTimeout(() => {
          showExperienceAnimation.value = false
        }, 3000)
      }

      // Show success message
      console.log('🎉 Quest completed successfully:', response.data?.message)
      
      // Reload quests to get updated data
      await loadQuests()
      
      // Close quest modal if open
      showQuestModal.value = false
      
    } else {
      error.value = response.error?.message || 'Failed to complete quest'
    }
  } catch (err) {
    console.error('Error completing quest:', err)
    error.value = 'Failed to complete quest. Please try again.'
  } finally {
    loading.value = false
  }
}

/**
 * Abandon a quest
 */
const abandonQuest = async (quest: Quest) => {
  if (!userStore.user?.id) {
    error.value = 'You must be logged in to abandon quests'
    return
  }

  if (!confirm('Are you sure you want to abandon this quest?')) {
    return
  }

  loading.value = true
  
  try {
    const response = await questService.abandonQuest(quest.id, userStore.user.id)
    
    if (response.success) {
      // Update quest status
      const questIndex = quests.value.findIndex(q => q.id === quest.id)
      if (questIndex !== -1) {
        quests.value[questIndex].status = QuestStatus.AVAILABLE
      }

      console.log('Quest abandoned successfully')
      await loadQuests()
      
    } else {
      error.value = response.error?.message || 'Failed to abandon quest'
    }
  } catch (err) {
    console.error('Error abandoning quest:', err)
    error.value = 'Failed to abandon quest. Please try again.'
  } finally {
    loading.value = false
  }
}

/**
 * Filter and sort quests
 */
const filteredQuests = computed(() => {
  let questsToFilter: Quest[] = []
  
  switch (activeTab.value) {
    case 'available':
      questsToFilter = availableQuests.value
      break
    case 'completed':
      questsToFilter = completedQuests.value
      break
    case 'github':
      questsToFilter = quests.value.filter(q => q.type === QuestType.GITHUB)
      break
    default:
      questsToFilter = availableQuests.value
  }

  // Apply filters
  let filtered = questsToFilter.filter(quest => {
    if (filters.value.type && quest.type !== filters.value.type) return false
    if (filters.value.difficulty && quest.difficulty !== filters.value.difficulty) return false
    if (filters.value.role && quest.role && quest.role !== filters.value.role && quest.role !== 'All') return false
    return true
  })

  // Sort quests
  filtered.sort((a, b) => {
    let aValue: any, bValue: any
    
    switch (sortBy.value) {
      case 'title':
        aValue = a.title.toLowerCase()
        bValue = b.title.toLowerCase()
        break
      case 'difficulty':
        aValue = Object.values(QuestDifficulty).indexOf(a.difficulty)
        bValue = Object.values(QuestDifficulty).indexOf(b.difficulty)
        break
      case 'experience':
        aValue = a.experienceReward
        bValue = b.experienceReward
        break
      case 'level':
        aValue = a.requiredLevel
        bValue = b.requiredLevel
        break
      default:
        return 0
    }
    
    if (sortDirection.value === 'asc') {
      return aValue < bValue ? -1 : aValue > bValue ? 1 : 0
    } else {
      return aValue > bValue ? -1 : aValue < bValue ? 1 : 0
    }
  })

  return filtered
})

/**
 * GitHub quests computed property
 */
const githubQuests = computed(() => {
  return quests.value.filter(quest => quest.type === QuestType.GITHUB)
})

/**
 * Open quest detail modal
 */
const openQuestModal = (quest: Quest) => {
  selectedQuest.value = quest
  showQuestModal.value = true
}

/**
 * Close quest detail modal
 */
const closeQuestModal = () => {
  selectedQuest.value = null
  showQuestModal.value = false
}

/**
 * Check if user can access quest
 */
const canUserAccessQuest = (quest: Quest): boolean => {
  if (!userStore.user) return false
  
  return questService.canUserAccessQuest(
    quest, 
    userStore.user.currentLevel, 
    userStore.user.selectedRole
  )
}

/**
 * Get quest type icon and color
 */
const getQuestTypeIcon = (type: QuestType): string => {
  return questService.getTypeIcon(type)
}

const getDifficultyColor = (difficulty: QuestDifficulty): string => {
  return questService.getDifficultyColor(difficulty)
}

/**
 * Clear filters
 */
const clearFilters = () => {
  filters.value = {
    type: undefined,
    difficulty: undefined,
    role: undefined,
    status: QuestStatus.AVAILABLE
  }
}

/**
 * Get category color
 */
const getCategoryColor = (type: QuestType): string => {
  switch (type) {
    case QuestType.GITHUB: return 'text-blue-400'
    case QuestType.MEDITATION: return 'text-purple-400'
    case QuestType.LEARNING: return 'text-green-400'
    case QuestType.COMMUNITY: return 'text-yellow-400'
    case QuestType.CODING: return 'text-red-400'
    default: return 'text-gray-400'
  }
}

/**
 * Get difficulty stars
 */
const getDifficultyStars = (difficulty: QuestDifficulty): string => {
  const levels = {
    [QuestDifficulty.EASY]: 1,
    [QuestDifficulty.MEDIUM]: 2,
    [QuestDifficulty.HARD]: 3,
    [QuestDifficulty.EXPERT]: 4,
    [QuestDifficulty.LEGENDARY]: 5
  }
  const level = levels[difficulty] || 1
  return '★'.repeat(level) + '☆'.repeat(5 - level)
}

/**
 * Get progress color
 */
const getProgressColor = (progress: number): string => {
  if (progress < 25) return 'bg-red-500'
  if (progress < 50) return 'bg-yellow-500'
  if (progress < 75) return 'bg-blue-500'
  return 'bg-green-500'
}

// Lifecycle hooks
onMounted(() => {
  loadQuests()
})

// Watch for user changes to reload quests
watch(() => userStore.user?.id, (newUserId) => {
  if (newUserId) {
    loadQuests()
  }
})

// Clear error when switching tabs
watch(activeTab, () => {
  error.value = null
})
</script>

<template>
  <div class="quests-view">
    <div class="container">
      <!-- Experience Animation -->
      <div v-if="showExperienceAnimation" class="experience-animation">
        <div class="experience-bubble">
          +{{ experienceGained }} XP
        </div>
      </div>

      <!-- Error Message -->
      <div v-if="error" class="error-banner">
        <span>{{ error }}</span>
        <button @click="error = null" class="close-error">×</button>
      </div>

      <!-- Loading Spinner -->
      <div v-if="loading" class="loading-overlay">
        <div class="loading-spinner"></div>
      </div>

      <!-- Header -->
      <div class="quests-header">
        <h1 class="page-title">Sacred Quests</h1>
        <p class="page-subtitle">
          Embark on meaningful challenges that expand consciousness and contribute to the collective
        </p>
        
        <!-- Onboarding Call-to-Action -->
        <div v-if="!userStore.userRole || userStore.userRole === 'None'" class="onboarding-banner glass">
          <div class="onboarding-content">
            <div class="onboarding-icon">🌟</div>
            <div class="onboarding-text">
              <h3>Begin Your Sacred Journey</h3>
              <p>Choose your role and start with foundational onboarding quests to unlock your path forward.</p>
            </div>
            <router-link to="/onboarding" class="btn btn-primary btn-lg">
              Start Onboarding
            </router-link>
          </div>
        </div>

        <!-- Active Onboarding Quest -->
        <div v-else-if="hasActiveOnboardingQuests && nextOnboardingQuest" class="onboarding-progress glass">
          <div class="onboarding-header">
            <h3>🎯 Current Onboarding Quest</h3>
            <div class="level-badge">Level {{ nextOnboardingQuest.level }}</div>
          </div>
          <div class="onboarding-quest-card">
            <div class="quest-content">
              <h4>{{ nextOnboardingQuest.title }}</h4>
              <p>{{ nextOnboardingQuest.description }}</p>
              <div class="quest-meta">
                <span class="xp-reward">+{{ nextOnboardingQuest.xpReward }} XP</span>
                <span class="syn-principle">{{ nextOnboardingQuest.synPrinciple }}</span>
              </div>
            </div>
            <div class="quest-action">
              <router-link to="/onboarding" class="btn btn-secondary">
                Continue Quest
              </router-link>
            </div>
          </div>
          <div class="onboarding-timeline">
            <div class="timeline-item" 
                 v-for="quest in onboardingQuests" 
                 :key="quest.id"
                 :class="{ 
                   'completed': quest.level < userStore.userLevel,
                   'current': quest.level === userStore.userLevel,
                   'future': quest.level > userStore.userLevel
                 }">
              <div class="timeline-dot"></div>
              <div class="timeline-label">Level {{ quest.level }}</div>
            </div>
          </div>
        </div>

        <!-- Onboarding Completed -->
        <div v-else-if="userStore.userRole && userStore.userRole !== 'None' && userStore.userLevel > 4" class="onboarding-completed glass">
          <div class="completion-content">
            <div class="completion-icon">🎉</div>
            <div class="completion-text">
              <h3>Onboarding Complete!</h3>
              <p>You have mastered the foundations as a <strong>{{ userStore.userRole }}</strong>. Ready for advanced quests!</p>
            </div>
          </div>
        </div>
        
        <!-- Progress Overview -->
        <div class="progress-overview">
          <div class="progress-card glass">
            <div class="progress-number">{{ userStore.userLevel }}</div>
            <div class="progress-label">Current Level</div>
          </div>
          <div class="progress-card glass">
            <div class="progress-number">{{ userStore.user?.experiencePoints?.toLocaleString() || 0 }}</div>
            <div class="progress-label">Experience Points</div>
          </div>
          <div class="progress-card glass">
            <div class="progress-number">{{ completedQuests.length }}</div>
            <div class="progress-label">Quests Completed</div>
          </div>
          <div class="progress-card glass">
            <div class="progress-number">{{ availableQuests.length }}</div>
            <div class="progress-label">Available Quests</div>
          </div>
        </div>
      </div>

      <!-- Quest Filters -->
      <div class="quest-filters glass">
        <div class="filter-group">
          <label for="type-filter">Type:</label>
          <select id="type-filter" v-model="filters.type">
            <option :value="undefined">All Types</option>
            <option v-for="type in Object.values(QuestType)" :key="type" :value="type">
              {{ type }}
            </option>
          </select>
        </div>
        
        <div class="filter-group">
          <label for="difficulty-filter">Difficulty:</label>
          <select id="difficulty-filter" v-model="filters.difficulty">
            <option :value="undefined">All Difficulties</option>
            <option v-for="difficulty in Object.values(QuestDifficulty)" :key="difficulty" :value="difficulty">
              {{ difficulty }}
            </option>
          </select>
        </div>
        
        <div class="filter-group">
          <label for="sort-filter">Sort by:</label>
          <select id="sort-filter" v-model="sortBy">
            <option value="title">Title</option>
            <option value="difficulty">Difficulty</option>
            <option value="experience">Experience</option>
            <option value="level">Required Level</option>
          </select>
        </div>
        
        <div class="filter-group">
          <label for="direction-filter">Order:</label>
          <select id="direction-filter" v-model="sortDirection">
            <option value="asc">Ascending</option>
            <option value="desc">Descending</option>
          </select>
        </div>
        
        <button @click="clearFilters" class="btn btn-ghost btn-sm">
          Clear Filters
        </button>
      </div>

      <!-- Quest Tabs -->
      <div class="quest-tabs">
        <button 
          class="tab-button"
          :class="{ active: activeTab === 'available' }"
          @click="activeTab = 'available'"
        >
          Available ({{ availableQuests.length }})
        </button>
        <button 
          class="tab-button"
          :class="{ active: activeTab === 'completed' }"
          @click="activeTab = 'completed'"
        >
          Completed ({{ completedQuests.length }})
        </button>
        <button 
          class="tab-button"
          :class="{ active: activeTab === 'github' }"
          @click="activeTab = 'github'"
        >
          GitHub Quests ({{ githubQuests.length }})
        </button>
      </div>

      <!-- Quest Content -->
      <div class="quest-content">
        <!-- Available Quests -->
        <div v-if="activeTab === 'available'" class="quest-list">
          <div v-if="filteredQuests.length === 0" class="empty-state card">
            <div class="empty-icon">🎯</div>
            <h3>No Available Quests</h3>
            <p v-if="availableQuests.length === 0">
              All quests completed! Continue growing to unlock new challenges.
            </p>
            <p v-else>
              No quests match your current filters. Try adjusting the filters above.
            </p>
          </div>
          
          <div 
            v-for="quest in filteredQuests" 
            :key="quest.id"
            class="quest-card card"
          >
            <div class="quest-header">
              <div class="quest-category">
                <span class="category-icon">{{ getQuestTypeIcon(quest.type) }}</span>
                <span class="category-name" :class="getCategoryColor(quest.type)">
                  {{ quest.type }}
                </span>
              </div>
              <div class="quest-difficulty" :style="{ color: getDifficultyColor(quest.difficulty) }">
                {{ getDifficultyStars(quest.difficulty) }}
              </div>
            </div>
            
            <h3 class="quest-title">{{ quest.title }}</h3>
            <p class="quest-description">{{ quest.description }}</p>
            
            <div class="quest-meta">
              <div class="meta-item">
                <span class="meta-label">XP Reward:</span>
                <span class="meta-value text-primary">{{ quest.experienceReward }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">Required Level:</span>
                <span class="meta-value">{{ quest.requiredLevel }}</span>
              </div>
              <div v-if="quest.role" class="meta-item">
                <span class="meta-label">Role:</span>
                <span class="meta-value">{{ quest.role }}</span>
              </div>
            </div>
            
            <!-- Progress Bar (if quest is active) -->
            <div v-if="quest.status === QuestStatus.ACTIVE && quest.progress !== undefined" class="quest-progress">
              <div class="progress-bar">
                <div 
                  class="progress-fill"
                  :class="getProgressColor(quest.progress)"
                  :style="{ width: quest.progress + '%' }"
                ></div>
              </div>
              <span class="progress-text">{{ Math.round(quest.progress) }}% Complete</span>
            </div>
            
            <!-- GitHub Issue Link -->
            <div v-if="quest.githubUrl" class="github-link">
              <a :href="quest.githubUrl" target="_blank" class="btn btn-ghost btn-sm">
                <span>🔗</span>
                View on GitHub
              </a>
            </div>
            
            <div class="quest-actions">
              <button 
                v-if="quest.status === QuestStatus.AVAILABLE"
                class="btn btn-primary"
                @click="acceptQuest(quest)"
                :disabled="!canUserAccessQuest(quest)"
              >
                Accept Quest
              </button>
              
              <button 
                v-if="quest.status === QuestStatus.ACTIVE"
                class="btn btn-success"
                @click="completeQuest(quest)"
              >
                Complete Quest
              </button>
              
              <button 
                v-if="quest.status === QuestStatus.ACTIVE"
                class="btn btn-warning"
                @click="abandonQuest(quest)"
              >
                Abandon Quest
              </button>
              
              <button 
                class="btn btn-ghost"
                @click="openQuestModal(quest)"
              >
                View Details
              </button>
            </div>
          </div>
        </div>

        <!-- Completed Quests -->
        <div v-if="activeTab === 'completed'" class="quest-list">
          <div v-if="completedQuests.length === 0" class="empty-state card">
            <div class="empty-icon">📝</div>
            <h3>No Completed Quests Yet</h3>
            <p>Start your first quest to begin your Syntopia journey!</p>
          </div>
          
          <div 
            v-for="quest in completedQuests" 
            :key="quest.id"
            class="quest-card card completed"
          >
            <div class="completion-badge">✅ Completed</div>
            
            <div class="quest-header">
              <div class="quest-category">
                <span class="category-icon">{{ getQuestTypeIcon(quest.type) }}</span>
                <span class="category-name" :class="getCategoryColor(quest.type)">
                  {{ quest.type }}
                </span>
              </div>
              <div class="quest-difficulty" :style="{ color: getDifficultyColor(quest.difficulty) }">
                {{ getDifficultyStars(quest.difficulty) }}
              </div>
            </div>
            
            <h3 class="quest-title">{{ quest.title }}</h3>
            <p class="quest-description">{{ quest.description }}</p>
            
            <div class="quest-meta">
              <div class="meta-item">
                <span class="meta-label">XP Earned:</span>
                <span class="meta-value text-primary">{{ quest.experienceReward }}</span>
              </div>
              <div v-if="quest.completedAt" class="meta-item">
                <span class="meta-label">Completed:</span>
                <span class="meta-value">{{ new Date(quest.completedAt).toLocaleDateString() }}</span>
              </div>
            </div>
            
            <div class="quest-actions">
              <button 
                class="btn btn-ghost"
                @click="openQuestModal(quest)"
              >
                View Details
              </button>
            </div>
          </div>
        </div>

        <!-- GitHub Quests -->
        <div v-if="activeTab === 'github'" class="quest-list">
          <div v-if="githubQuests.length === 0" class="empty-state card">
            <div class="empty-icon">💻</div>
            <h3>No GitHub Quests Available</h3>
            <p>GitHub integration quests unlock at higher levels. Keep progressing to access collaborative coding challenges!</p>
          </div>
          
          <div 
            v-for="quest in githubQuests" 
            :key="quest.id"
            class="quest-card card github-quest"
          >
            <div class="github-badge">🔗 GitHub Integration</div>
            
            <div class="quest-header">
              <div class="quest-category">
                <span class="category-icon">{{ getQuestTypeIcon(quest.type) }}</span>
                <span class="category-name" :class="getCategoryColor(quest.type)">
                  {{ quest.type }}
                </span>
              </div>
              <div class="quest-difficulty" :style="{ color: getDifficultyColor(quest.difficulty) }">
                {{ getDifficultyStars(quest.difficulty) }}
              </div>
            </div>
            
            <h3 class="quest-title">{{ quest.title }}</h3>
            <p class="quest-description">{{ quest.description }}</p>
            
            <div class="quest-meta">
              <div class="meta-item">
                <span class="meta-label">XP Reward:</span>
                <span class="meta-value text-primary">{{ quest.experienceReward }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">Required Level:</span>
                <span class="meta-value">{{ quest.requiredLevel }}</span>
              </div>
            </div>
            
            <div v-if="quest.githubUrl" class="github-details">
              <div class="repo-info">
                <strong>GitHub Issue:</strong> 
                <a :href="quest.githubUrl" target="_blank" class="github-link-text">
                  View Issue
                </a>
              </div>
            </div>
            
            <div class="quest-actions">
              <a 
                v-if="quest.githubUrl"
                :href="quest.githubUrl" 
                target="_blank" 
                class="btn btn-primary"
              >
                View on GitHub
              </a>
              <button 
                v-if="quest.status === QuestStatus.AVAILABLE"
                class="btn btn-secondary"
                @click="acceptQuest(quest)"
                :disabled="!canUserAccessQuest(quest)"
              >
                Accept Quest
              </button>
              <button 
                class="btn btn-ghost"
                @click="openQuestModal(quest)"
              >
                View Details
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Quest Detail Modal -->
      <div v-if="showQuestModal && selectedQuest" class="quest-modal-overlay" @click="closeQuestModal">
        <div class="quest-modal card" @click.stop>
          <div class="modal-header">
            <h2>{{ selectedQuest.title }}</h2>
            <button class="close-button" @click="closeQuestModal">×</button>
          </div>
          
          <div class="modal-content">
            <p>{{ selectedQuest.description }}</p>
            
            <div class="quest-details">
              <div class="detail-row">
                <strong>Type:</strong> {{ selectedQuest.type }}
              </div>
              <div class="detail-row">
                <strong>Difficulty:</strong> {{ selectedQuest.difficulty }}
              </div>
              <div class="detail-row">
                <strong>Experience Reward:</strong> {{ selectedQuest.experienceReward }} XP
              </div>
              <div class="detail-row">
                <strong>Required Level:</strong> {{ selectedQuest.requiredLevel }}
              </div>
              <div v-if="selectedQuest.role" class="detail-row">
                <strong>Required Role:</strong> {{ selectedQuest.role }}
              </div>
              <div class="detail-row">
                <strong>Status:</strong> {{ selectedQuest.status }}
              </div>
            </div>
            
            <div v-if="selectedQuest.githubUrl" class="github-section">
              <h3>GitHub Integration</h3>
              <a :href="selectedQuest.githubUrl" target="_blank" class="github-link-full">
                🔗 View GitHub Issue
              </a>
            </div>
          </div>
          
          <div class="modal-actions">
            <button class="btn btn-secondary" @click="closeQuestModal">
              Close
            </button>
            
            <button 
              v-if="selectedQuest.status === QuestStatus.AVAILABLE"
              class="btn btn-primary"
              @click="acceptQuest(selectedQuest); closeQuestModal()"
              :disabled="!canUserAccessQuest(selectedQuest)"
            >
              Accept Quest
            </button>
            
            <button 
              v-if="selectedQuest.status === QuestStatus.ACTIVE"
              class="btn btn-success"
              @click="completeQuest(selectedQuest)"
            >
              Complete Quest
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.quests-view {
  min-height: 100vh;
  padding: 2rem 0;
}

/* Experience Animation */
.experience-animation {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 9999;
  pointer-events: none;
}

.experience-bubble {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  color: #1a1a2e;
  padding: 1rem 2rem;
  border-radius: 50px;
  font-size: 1.5rem;
  font-weight: bold;
  animation: experienceFloat 3s ease-out forwards;
  box-shadow: 0 0 30px rgba(255, 215, 0, 0.6);
}

@keyframes experienceFloat {
  0% {
    transform: scale(0) rotate(0deg);
    opacity: 0;
  }
  20% {
    transform: scale(1.2) rotate(-5deg);
    opacity: 1;
  }
  80% {
    transform: scale(1) rotate(5deg);
    opacity: 1;
  }
  100% {
    transform: scale(0.8) translateY(-100px) rotate(0deg);
    opacity: 0;
  }
}

/* Error Banner */
.error-banner {
  background: linear-gradient(135deg, #ff4757, #ff3838);
  color: white;
  padding: 1rem;
  border-radius: var(--radius-md);
  margin-bottom: 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  animation: slideInDown 0.3s ease-out;
}

/* Onboarding Banner */
.onboarding-banner {
  background: linear-gradient(135deg, rgba(147, 51, 234, 0.1), rgba(79, 70, 229, 0.1));
  border: 2px solid rgba(147, 51, 234, 0.3);
  border-radius: var(--radius-lg);
  padding: 2rem;
  margin-bottom: 2rem;
  animation: slideInDown 0.5s ease-out;
}

.onboarding-content {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.onboarding-icon {
  font-size: 3rem;
  animation: pulse 2s infinite;
}

.onboarding-text h3 {
  margin: 0 0 0.5rem 0;
  color: var(--color-primary);
  font-size: 1.5rem;
  font-weight: 600;
}

.onboarding-text p {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 1.1rem;
  line-height: 1.5;
}

/* Onboarding Progress */
.onboarding-progress {
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.1), rgba(147, 51, 234, 0.1));
  border: 2px solid rgba(79, 70, 229, 0.3);
  border-radius: var(--radius-lg);
  padding: 2rem;
  margin-bottom: 2rem;
}

.onboarding-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.onboarding-header h3 {
  margin: 0;
  color: var(--color-primary);
  font-size: 1.4rem;
  font-weight: 600;
}

.level-badge {
  background: var(--color-primary);
  color: white;
  padding: 0.5rem 1rem;
  border-radius: var(--radius-sm);
  font-weight: 600;
  font-size: 0.9rem;
}

.onboarding-quest-card {
  display: flex;
  gap: 2rem;
  align-items: center;
  background: rgba(255, 255, 255, 0.05);
  border-radius: var(--radius-md);
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.quest-content {
  flex: 1;
}

.quest-content h4 {
  margin: 0 0 0.5rem 0;
  color: var(--color-text);
  font-size: 1.2rem;
  font-weight: 600;
}

.quest-content p {
  margin: 0 0 1rem 0;
  color: var(--color-text-muted);
  line-height: 1.4;
}

.quest-meta {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.xp-reward {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  color: #1a1a2e;
  padding: 0.25rem 0.75rem;
  border-radius: var(--radius-sm);
  font-weight: 600;
  font-size: 0.85rem;
}

.syn-principle {
  background: rgba(147, 51, 234, 0.2);
  color: var(--color-primary);
  padding: 0.25rem 0.75rem;
  border-radius: var(--radius-sm);
  font-weight: 500;
  font-size: 0.85rem;
}

.onboarding-timeline {
  display: flex;
  gap: 1rem;
  justify-content: center;
  align-items: center;
}

.timeline-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  opacity: 0.5;
  transition: all 0.3s ease;
}

.timeline-item.completed {
  opacity: 1;
}

.timeline-item.current {
  opacity: 1;
  transform: scale(1.1);
}

.timeline-item.current .timeline-dot {
  background: var(--color-primary);
  box-shadow: 0 0 10px rgba(79, 70, 229, 0.5);
}

.timeline-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--color-text-muted);
  transition: all 0.3s ease;
}

.timeline-item.completed .timeline-dot {
  background: var(--color-success);
}

.timeline-label {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  font-weight: 500;
}

/* Onboarding Completed */
.onboarding-completed {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.1), rgba(16, 185, 129, 0.1));
  border: 2px solid rgba(34, 197, 94, 0.3);
  border-radius: var(--radius-lg);
  padding: 2rem;
  margin-bottom: 2rem;
}

.completion-content {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.completion-icon {
  font-size: 3rem;
  animation: bounce 2s infinite;
}

.completion-text h3 {
  margin: 0 0 0.5rem 0;
  color: var(--color-success);
  font-size: 1.5rem;
  font-weight: 600;
}

.completion-text p {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 1.1rem;
  line-height: 1.5;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

.close-error {
  background: none;
  border: none;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: var(--radius-sm);
  transition: background-color 0.2s;
}

.close-error:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* Loading Overlay */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top: 3px solid var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.quests-header {
  text-align: center;
  margin-bottom: 3rem;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 600;
  margin-bottom: 1rem;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: 1.125rem;
  color: var(--color-text-muted);
  margin-bottom: 2rem;
}

.progress-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.progress-card {
  text-align: center;
  padding: 1.5rem 1rem;
  border-radius: var(--radius-lg);
}

.progress-number {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: 0.5rem;
}

.progress-label {
  color: var(--color-text-muted);
  font-weight: 500;
}

/* Quest Filters */
.quest-filters {
  display: flex;
  gap: 1rem;
  align-items: center;
  padding: 1rem;
  border-radius: var(--radius-lg);
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  min-width: 150px;
}

.filter-group label {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  font-weight: 500;
}

.filter-group select {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-md);
  padding: 0.5rem;
  color: var(--color-text);
  font-size: 0.875rem;
}

.filter-group select:focus {
  outline: none;
  border-color: var(--color-primary);
}

.quest-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.tab-button {
  padding: 1rem 1.5rem;
  background: none;
  border: none;
  color: var(--color-text-muted);
  font-weight: 500;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all var(--transition-normal);
}

.tab-button:hover {
  color: var(--color-text);
}

.tab-button.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
}

.quest-list {
  display: grid;
  gap: 1.5rem;
}

.quest-card {
  padding: 1.5rem;
  position: relative;
  transition: all var(--transition-normal);
}

.quest-card:hover {
  transform: translateY(-2px);
}

.quest-card.completed {
  opacity: 0.8;
  border-color: var(--color-success);
}

.quest-card.github-quest {
  border-color: var(--color-accent);
}

.completion-badge,
.github-badge {
  position: absolute;
  top: 1rem;
  right: 1rem;
  padding: 0.25rem 0.75rem;
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  font-weight: 600;
}

.completion-badge {
  background: var(--color-success);
  color: white;
}

.github-badge {
  background: var(--color-accent);
  color: white;
}

.quest-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.quest-category {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.category-icon {
  font-size: 1.25rem;
}

.category-name {
  font-weight: 500;
  text-transform: uppercase;
  font-size: 0.875rem;
  letter-spacing: 0.5px;
}

.quest-difficulty {
  font-size: 0.875rem;
}

.quest-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  color: var(--color-text);
}

.quest-description {
  color: var(--color-text-muted);
  line-height: 1.6;
  margin-bottom: 1rem;
}

.quest-meta {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  gap: 0.5rem;
}

.meta-label {
  color: var(--color-text-muted);
  font-size: 0.875rem;
}

.meta-value {
  color: var(--color-text);
  font-weight: 500;
  font-size: 0.875rem;
}

.quest-progress {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-full);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  transition: width var(--transition-normal);
  border-radius: var(--radius-full);
}

.progress-text {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  min-width: 80px;
}

.github-link {
  margin-bottom: 1rem;
}

.github-details {
  background: rgba(255, 255, 255, 0.02);
  padding: 1rem;
  border-radius: var(--radius-md);
  margin-bottom: 1rem;
}

.repo-info,
.issue-info {
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.github-link-text {
  color: var(--color-primary);
  text-decoration: none;
  margin-left: 0.5rem;
}

.github-link-text:hover {
  text-decoration: underline;
}

.quest-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.empty-state {
  text-align: center;
  padding: 3rem 2rem;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.empty-state h3 {
  margin-bottom: 0.75rem;
  color: var(--color-text);
}

.empty-state p {
  color: var(--color-text-muted);
}

/* Quest Modal */
.quest-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: var(--blur-sm);
}

.quest-modal {
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  padding: 0;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.modal-header h2 {
  color: var(--color-text);
  margin: 0;
}

.close-button {
  background: none;
  border: none;
  color: var(--color-text-muted);
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: var(--radius-sm);
  transition: all var(--transition-normal);
}

.close-button:hover {
  color: var(--color-text);
  background: rgba(255, 255, 255, 0.1);
}

.modal-content {
  padding: 1.5rem;
}

.quest-details {
  margin: 1.5rem 0;
}

.detail-row {
  display: flex;
  gap: 1rem;
  margin-bottom: 0.75rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.detail-row strong {
  min-width: 120px;
  color: var(--color-text-muted);
}

.github-section {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.github-section h3 {
  margin-bottom: 1rem;
  color: var(--color-text);
}

.github-link-full {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--color-primary);
  text-decoration: none;
  padding: 0.5rem 1rem;
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
}

.github-link-full:hover {
  background: var(--color-primary);
  color: white;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  padding: 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

/* Animations */
@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive Design */
@media (max-width: 768px) {
  .progress-overview {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .quest-filters {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-group {
    min-width: auto;
  }
  
  .quest-tabs {
    flex-wrap: wrap;
  }
  
  .quest-meta {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .quest-actions {
    flex-direction: column;
  }
  
  .modal-actions {
    flex-direction: column;
  }
}
</style>
