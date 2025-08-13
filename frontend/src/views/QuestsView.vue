<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
// @ts-ignore - path alias resolves at build time
import { useQuests } from '@/composables/useQuests'
import questService, { 
  type Quest, 
  type UserQuest,
  QuestType, 
  QuestDifficulty, 
  QuestStatus, 
  UserQuestStatus,
  type QuestFilter 
} from '@/services/quest.service'
import onboardingService, { type OnboardingQuest } from '@/services/onboarding.service'

const userStore = useUserStore()

// Reactive state
const quests = ref<Quest[]>([])
const { 
  questStore,
  available: availableQuests, 
  completed: completedQuests, 
  refresh: refreshQuests,
  availablePagination,
  completedPagination,
  totalAvailablePages,
  totalCompletedPages,
  availableCount,
  completedCount,
  loadingAvailablePage,
  loadingCompletedPage
} = useQuests()
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

// Pagination & counts provided by composable

// Role options for filter (derived from quests + user's role)
const roleOptions = computed<string[]>(() => {
  const roles = new Set<string>()
  const qArr = Array.isArray(quests.value) ? quests.value : []
  for (const q of qArr) {
    if (q.role && q.role !== 'All') roles.add(q.role)
  }
  const list = Array.from(roles).sort()
  const myRole = userStore.userRole
  if (myRole && myRole !== 'None' && !roles.has(myRole)) {
    list.unshift(myRole)
  }
  return list
})

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
  await refreshQuests()
  quests.value = questStore.quests
    await loadOnboardingQuests()
  } catch (err) {
    console.error('Error loading quests:', err)
    error.value = 'Failed to load quests. Please try again.'
  } finally { loading.value = false }
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
 * Continue onboarding: ensure backend marks onboarding ACTIVE, then navigate
 */
const continueOnboarding = async () => {
  try {
    const role = userStore.userRole
    const next = nextOnboardingQuest.value
    if (role && next) {
      await onboardingService.accept(role, next.level)
      // Notify other components (e.g., QuestTracker) to refresh
      window.dispatchEvent(new CustomEvent('onboardingAccepted'))
    }
  } catch (e) {
    // Non-blocking; proceed to navigate regardless
    console.warn('Onboarding accept failed or skipped:', e)
  } finally {
    // Navigate to onboarding flow
    window.location.href = '/onboarding'
  }
}

// Original acceptQuest removed; unified implementation defined later after pagination logic

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
    const res = await questService.completeQuest(quest.id)
    if (res.success && res.data) {
      quest.status = QuestStatus.COMPLETED
      if (res.data.experienceAwarded) {
        experienceGained.value = res.data.experienceAwarded
        showExperienceAnimation.value = true
        setTimeout(() => showExperienceAnimation.value = false, 3000)
      }
  await refreshQuests()
    } else error.value = res.error?.message || 'Failed to complete quest'
  } catch {
    error.value = 'Failed to complete quest'
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
    const res = await questService.abandonQuest(quest.id)
    if (res.success && res.data) {
      quest.status = QuestStatus.AVAILABLE
  await refreshQuests()
    } else error.value = res.error?.message || 'Failed to abandon quest'
  } catch {
    error.value = 'Failed to abandon quest'
  } finally {
    loading.value = false
  }
}

/**
 * Verify a quest (for completed user quests awaiting verification)
 */
const verifyQuest = async (quest: Quest) => {
  try {
    const res = await questService.verifyQuest(quest.id)
    if (res.success && res.data) {
      quest.status = QuestStatus.COMPLETED
      // Optimistically update associated userQuest status to USER_VERIFIED
  const uq = availableQuests.value.find((u: any) => u.questId === quest.id || u.quest?.id === quest.id)
      if (uq) uq.status = UserQuestStatus.USER_VERIFIED
      // Also refresh completed quests page to reflect potential status shifts
  await refreshQuests()
    } else {
      error.value = res.error?.message || 'Failed to verify quest'
    }
  } catch {
    error.value = 'Failed to verify quest'
  }
}

// Pagination controls
function changeAvailablePage(page: number) { if (page < 0 || page >= totalAvailablePages.value) return; questStore.loadAvailable(page) }
function changeCompletedPage(page: number) { if (page < 0 || page >= totalCompletedPages.value) return; questStore.loadCompleted(page) }

// Verification helpers (search both available & completed lists for robustness)
const findUserQuest = (questId: string) => {
  return (
    availableQuests.value.find((uq: any) => uq.quest?.id === questId) ||
    completedQuests.value.find((uq: any) => uq.quest?.id === questId)
  )
}

// Local convenience flags for template readability
const availableLoading = computed(() => loadingAvailablePage.value)
const completedLoading = computed(() => loadingCompletedPage.value)
const needsVerification = (quest: Quest) => {
  const uq = findUserQuest(quest.id)
  return !!(uq && uq.status === UserQuestStatus.USER_COMPLETED && !uq.verified)
}
const isVerified = (quest: Quest) => {
  const uq = findUserQuest(quest.id)
  return !!(uq && uq.status === UserQuestStatus.USER_VERIFIED)
}

// Filtering and sorting logic remains the same...

/**
 * GitHub quests computed property
 */
const githubQuests = computed(() => {
  return Array.isArray(quests.value) ? quests.value.filter(quest => quest.type === QuestType.GITHUB_ISSUE) : []
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
  
  // Check level requirement
  if (userStore.user.currentLevel < quest.requiredLevel) return false
  
  // Check role requirement
  if (quest.role && quest.role !== 'All' && userStore.user.selectedRole !== quest.role) return false
  
  return true
}

/**
 * Get quest type icon and color
 */
const getQuestTypeIcon = (type: QuestType): string => {
  switch (type) {
    case QuestType.GITHUB_ISSUE: return '🐙'
    case QuestType.LEARNING: return '📚'
    case QuestType.COMMUNITY: return '👥'
    case QuestType.CONTRIBUTION: return '🤝'
    case QuestType.NETWORKING: return '🌐'
    case QuestType.SKILL_BUILDING: return '🔧'
    case QuestType.INTEGRATION: return '🔗'
    case QuestType.ONBOARDING: return '🚀'
    case QuestType.GEOMETRY: return '🔮'
    default: return '⭐'
  }
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
    case QuestType.GITHUB_ISSUE: return 'text-blue-400'
    case QuestType.LEARNING: return 'text-green-400'
    case QuestType.COMMUNITY: return 'text-yellow-400'
    case QuestType.CONTRIBUTION: return 'text-red-400'
    case QuestType.NETWORKING: return 'text-purple-400'
    case QuestType.SKILL_BUILDING: return 'text-orange-400'
    case QuestType.INTEGRATION: return 'text-indigo-400'
    case QuestType.ONBOARDING: return 'text-cyan-400'
    case QuestType.GEOMETRY: return 'text-pink-400'
    default: return 'text-gray-400'
  }
}

/**
 * Get difficulty stars
 */
const getDifficultyStars = (difficulty: QuestDifficulty): string => {
  const levels: Record<QuestDifficulty, number> = {
    [QuestDifficulty.BEGINNER]: 1,
    [QuestDifficulty.INTERMEDIATE]: 2,
    [QuestDifficulty.ADVANCED]: 3,
    [QuestDifficulty.EXPERT]: 4
  }
  const level = levels[difficulty] || 1
  return '★'.repeat(level) + '☆'.repeat(4 - level)
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

// Define filteredQuests once
const filteredQuests = computed(() => {
  let questsToFilter: Quest[] = []
  const questsArray = Array.isArray(quests.value) ? quests.value : []
  switch (activeTab.value) {
    case 'available': questsToFilter = questsArray.filter(q => q.status === QuestStatus.AVAILABLE); break
    case 'completed': questsToFilter = Array.isArray(completedQuests.value) ? completedQuests.value : []; break
    case 'github': questsToFilter = questsArray.filter(q => q.type === QuestType.GITHUB_ISSUE); break
    default: questsToFilter = questsArray.filter(q => q.status === QuestStatus.AVAILABLE)
  }
  let filtered = questsToFilter.filter(q => {
    if (filters.value.type && q.type !== filters.value.type) return false
    if (filters.value.difficulty && q.difficulty !== filters.value.difficulty) return false
    if (filters.value.role && q.role && q.role !== filters.value.role && q.role !== 'All') return false
    return true
  })
  filtered.sort((a,b)=>{
    const dir = sortDirection.value === 'asc' ? 1 : -1
    switch (sortBy.value) {
      case 'title': return a.title.localeCompare(b.title) * dir
      case 'difficulty': return (Object.values(QuestDifficulty).indexOf(a.difficulty) - Object.values(QuestDifficulty).indexOf(b.difficulty)) * dir
      case 'experience': return (a.experienceReward - b.experienceReward) * dir
      case 'level': return (a.requiredLevel - b.requiredLevel) * dir
      default: return 0
    }
  })
  return filtered
})

// Single acceptQuest definition
const acceptQuest = async (quest: Quest) => {
  if (!userStore.user?.id) { error.value = 'Login required'; return }
  try {
    const res = await questService.acceptQuest(quest.id)
    if (res.success && res.data) {
  const uq = res.data
  questStore.updateAvailableUserQuest(uq)
      quest.status = QuestStatus.ACTIVE
    } else {
      error.value = res.error?.message || 'Failed to accept'
    }
  } catch {
    error.value = 'Failed to accept quest'
  }
}
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

      <!-- Section Skeletons (replace full-page overlay) -->
  <div v-if="loading" class="initial-skeletons" role="status" aria-live="polite" aria-busy="true" aria-label="Loading quests">
        <div class="skeleton header-skeleton glass-flat">
          <div class="skeleton-line w-40"></div>
          <div class="skeleton-line w-60"></div>
        </div>
        <div class="skeleton-cards">
          <div class="quest-card card skeleton glass-flat" v-for="n in 3" :key="n">
            <div class="skeleton-line w-30 mb"></div>
            <div class="skeleton-line w-80"></div>
            <div class="skeleton-line w-60"></div>
            <div class="skeleton-line w-50"></div>
          </div>
        </div>
      </div>

      <!-- Header -->
      <div class="quests-header">
  <h1 class="page-title">Sacred Quests</h1>
        <p class="page-subtitle">
          Embark on meaningful challenges that expand consciousness and contribute to the collective
        </p>
  <div class="sr-only" aria-live="polite">{{ availableCount }} available quests, {{ completedCount }} completed quests.</div>
        
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
              <button class="btn btn-secondary" @click="continueOnboarding">
                Continue Quest
              </button>
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
          <label for="role-filter">Role:</label>
          <select id="role-filter" v-model="filters.role">
            <option :value="undefined">All Roles</option>
            <option v-for="role in roleOptions" :key="role" :value="role">
              {{ role }}
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
          Available ({{ availableCount }})
        </button>
        <button 
          class="tab-button"
          :class="{ active: activeTab === 'completed' }"
          @click="activeTab = 'completed'"
        >
          Completed ({{ completedCount }})
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
          <!-- Page transition skeletons -->
          <div v-if="availableLoading && !loading" class="skeleton-cards inline" role="status" aria-live="polite" aria-busy="true" aria-label="Loading available quests page">
            <div class="quest-card card skeleton glass-flat" v-for="n in 2" :key="'a-skel-'+n">
              <div class="skeleton-line w-30 mb"></div>
              <div class="skeleton-line w-75"></div>
              <div class="skeleton-line w-55"></div>
            </div>
          </div>
          <!-- Pinned Onboarding Highlight -->
          <div 
            v-if="hasActiveOnboardingQuests && nextOnboardingQuest"
            class="card pinned-onboarding"
          >
            <div class="pinned-inner">
              <div class="pin-icon">🌟</div>
              <div class="pin-content">
                <div class="pin-title">Continue Onboarding</div>
                <div class="pin-quest">{{ nextOnboardingQuest.title }}</div>
                <div class="pin-desc">{{ nextOnboardingQuest.description }}</div>
                <div class="pin-meta">
                  <span class="pin-level">Level {{ nextOnboardingQuest.level }}</span>
                  <span class="pin-xp">+{{ nextOnboardingQuest.xpReward }} XP</span>
                </div>
              </div>
              <button class="btn btn-primary btn-sm" @click="continueOnboarding">Open</button>
            </div>
          </div>
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
            
            <!-- GitHub Issue Link -->
            <div v-if="quest.githubIssueUrl" class="github-link">
              <a :href="quest.githubIssueUrl" target="_blank" class="btn btn-ghost btn-sm">
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

              <!-- New: Verify button for completed but not verified quests -->
              <button 
                v-if="quest.status === QuestStatus.COMPLETED && needsVerification(quest)"
                class="btn btn-warning"
                @click="verifyQuest(quest)"
              >
                Verify
              </button>
              <button 
                v-if="quest.status === QuestStatus.COMPLETED && isVerified(quest)"
                disabled
                class="btn btn-success btn-ghost"
              >
                Verified
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
          <div v-if="completedLoading && !loading" class="skeleton-cards inline" role="status" aria-live="polite" aria-busy="true" aria-label="Loading completed quests page">
            <div class="quest-card card skeleton glass-flat" v-for="n in 2" :key="'c-skel-'+n">
              <div class="skeleton-line w-35 mb"></div>
              <div class="skeleton-line w-70"></div>
              <div class="skeleton-line w-50"></div>
            </div>
          </div>
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
            <div :class="['completion-badge', needsVerification(quest) ? 'pending-verify' : isVerified(quest) ? 'verified' : '']">
              {{ isVerified(quest) ? '✅ Verified' : needsVerification(quest) ? '⏳ Pending Verification' : '✅ Completed' }}
            </div>
            
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
            </div>
            
            <div class="quest-actions">
              <button 
                v-if="needsVerification(quest)"
                class="btn btn-warning"
                @click="verifyQuest(quest)"
              >
                Verify
              </button>
              <button 
                v-if="isVerified(quest)"
                disabled
                class="btn btn-success btn-ghost"
              >
                Verified
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
            
            <div v-if="quest.githubIssueUrl" class="github-details">
              <div class="repo-info">
                <strong>GitHub Issue:</strong> 
                <a :href="quest.githubIssueUrl" target="_blank" class="github-link-text">
                  View Issue
                </a>
              </div>
            </div>
            
            <div class="quest-actions">
              <a 
                v-if="quest.githubIssueUrl"
                :href="quest.githubIssueUrl" 
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

        <!-- Available Quests pagination controls -->
        <div v-if="activeTab === 'available' && availablePagination.total > availablePagination.size" class="pagination-controls">
          <button class="btn btn-ghost btn-sm" :disabled="availablePagination.page === 0 || loadingAvailablePage" @click="changeAvailablePage(availablePagination.page - 1)">Prev</button>
          <span>Page {{ availablePagination.page + 1 }} / {{ totalAvailablePages }}</span>
          <div v-if="loadingAvailablePage" class="page-spinner" aria-label="Loading available quests"></div>
          <span v-if="questStore.availablePageError" class="page-error">{{ questStore.availablePageError }}</span>
          <button class="btn btn-ghost btn-sm" :disabled="availablePagination.page + 1 >= totalAvailablePages || loadingAvailablePage" @click="changeAvailablePage(availablePagination.page + 1)">Next</button>
        </div>
        <div v-if="activeTab === 'completed' && completedPagination.total > completedPagination.size" class="pagination-controls">
          <button class="btn btn-ghost btn-sm" :disabled="completedPagination.page === 0 || loadingCompletedPage" @click="changeCompletedPage(completedPagination.page - 1)">Prev</button>
          <span>Page {{ completedPagination.page + 1 }} / {{ totalCompletedPages }}</span>
          <div v-if="loadingCompletedPage" class="page-spinner" aria-label="Loading completed quests"></div>
          <span v-if="questStore.completedPageError" class="page-error">{{ questStore.completedPageError }}</span>
          <button class="btn btn-ghost btn-sm" :disabled="completedPagination.page + 1 >= totalCompletedPages || loadingCompletedPage" @click="changeCompletedPage(completedPagination.page + 1)">Next</button>
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
            
            <div v-if="selectedQuest.githubIssueUrl" class="github-section">
              <h3>GitHub Integration</h3>
              <a :href="selectedQuest.githubIssueUrl" target="_blank" class="github-link-full">
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

/* Skeleton & Flat Glassmorphism Styles */
.glass-flat {
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  backdrop-filter: blur(10px) saturate(140%);
  -webkit-backdrop-filter: blur(10px) saturate(140%);
  box-shadow: 0 4px 12px rgba(0,0,0,0.25), inset 0 1px 0 rgba(255,255,255,0.05);
}

.initial-skeletons {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin: 2rem 0 3rem;
}

.skeleton-cards.inline {
  display: flex;
  gap: 1.25rem;
  flex-wrap: wrap;
  margin-bottom: 1.5rem;
}

.skeleton {
  position: relative;
  overflow: hidden;
}

.skeleton-line {
  height: 12px;
  background: linear-gradient(90deg, rgba(255,255,255,0.06) 25%, rgba(255,255,255,0.15) 37%, rgba(255,255,255,0.06) 63%);
  background-size: 400% 100%;
  animation: shimmer 1.4s ease-in-out infinite;
  border-radius: 4px;
  margin-bottom: 0.75rem;
}
.skeleton-line.mb { margin-bottom: 1rem; }
.skeleton-line.w-30 { width: 30%; }
.skeleton-line.w-35 { width: 35%; }
.skeleton-line.w-40 { width: 40%; }
.skeleton-line.w-50 { width: 50%; }
.skeleton-line.w-55 { width: 55%; }
.skeleton-line.w-60 { width: 60%; }
.skeleton-line.w-70 { width: 70%; }
.skeleton-line.w-75 { width: 75%; }
.skeleton-line.w-80 { width: 80%; }

@keyframes shimmer {
  0% { background-position: 100% 50%; }
  100% { background-position: 0 50%; }
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

.pinned-onboarding {
  border: 1px solid rgba(212, 175, 55, 0.35);
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.12), rgba(241, 196, 15, 0.08));
}

.pinned-inner {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.pin-icon { font-size: 1.5rem; }
.pin-content { flex: 1; }
.pin-title {
  font-size: 0.8rem;
  font-weight: 700;
  color: #d4af37;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.pin-quest { font-weight: 700; color: var(--color-text); }
.pin-desc { color: var(--color-text-muted); font-size: 0.9rem; margin-top: 2px; }
.pin-meta { display: flex; gap: 0.5rem; margin-top: 6px; }
.pin-level, .pin-xp {
  background: rgba(255, 255, 255, 0.06);
  border-radius: var(--radius-sm);
  padding: 2px 6px;
  font-size: 0.8rem;
  font-weight: 700;
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

.completion-badge { background: var(--color-success); color: white; }
.completion-badge.pending-verify { background: #f59e0b; color: #1f2937; }
.completion-badge.verified { background: linear-gradient(135deg,#10b981,#059669); box-shadow: 0 0 8px rgba(16,185,129,0.6); }

.github-badge {
  background: var(--color-accent);
  color: white;
}

.pagination-controls { display: flex; align-items: center; gap: 0.75rem; margin-top: 1.5rem; }
.page-spinner { width: 16px; height: 16px; border: 2px solid rgba(255,255,255,0.3); border-top: 2px solid var(--color-primary); border-radius: 50%; animation: spin 0.8s linear infinite; }
.page-error { color: #ef4444; font-size: 0.75rem; font-weight: 500; }

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
