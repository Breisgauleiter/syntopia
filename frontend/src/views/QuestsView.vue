<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

interface Quest {
  id: string
  title: string
  description: string
  category: 'coding' | 'exploration' | 'community' | 'wisdom' | 'meditation'
  difficulty: number
  experiencePoints: number
  requiredLevel: number
  estimatedTime: string
  objectives: string[]
  isCompleted: boolean
  progress: number
  githubIssue?: {
    repository: string
    issueNumber: number
    url: string
  }
  unlockConditions?: string[]
}

const activeTab = ref<'available' | 'completed' | 'github'>('available')
const selectedQuest = ref<Quest | null>(null)

// Mock quest data - in real app, this would come from the backend
const quests = ref<Quest[]>([
  {
    id: 'first-meditation',
    title: 'Sacred Geometry Meditation',
    description: 'Complete your first 10-minute meditation session while focusing on the Flower of Life pattern.',
    category: 'meditation',
    difficulty: 1,
    experiencePoints: 100,
    requiredLevel: 1,
    estimatedTime: '15 min',
    objectives: [
      'Visit the Geometry Explorer',
      'Select the Flower of Life pattern',
      'Complete 10-minute focused meditation',
      'Record your insights in your journal'
    ],
    isCompleted: false,
    progress: 0
  },
  {
    id: 'profile-setup',
    title: 'Choose Your Sacred Path',
    description: 'Complete your Syntopia profile by selecting your sacred role and setting your intentions.',
    category: 'exploration',
    difficulty: 1,
    experiencePoints: 150,
    requiredLevel: 1,
    estimatedTime: '10 min',
    objectives: [
      'Select your Sacred Role',
      'Write a personal intention statement',
      'Upload a profile avatar',
      'Connect your GitHub account'
    ],
    isCompleted: userStore.user?.role !== 'None',
    progress: userStore.user?.role !== 'None' ? 100 : 25
  },
  {
    id: 'community-intro',
    title: 'Community Introduction',
    description: 'Introduce yourself to the Syntopia community and make your first connection.',
    category: 'community',
    difficulty: 2,
    experiencePoints: 200,
    requiredLevel: 2,
    estimatedTime: '20 min',
    objectives: [
      'Write an introduction post',
      'Comment on 3 other introduction posts',
      'Join a Sacred Role discussion group',
      'Schedule a virtual coffee chat'
    ],
    isCompleted: false,
    progress: 0,
    unlockConditions: ['Complete Profile Setup']
  },
  {
    id: 'first-contribution',
    title: 'First Open Source Contribution',
    description: 'Make your first contribution to an open source project through the Syntopia platform.',
    category: 'coding',
    difficulty: 3,
    experiencePoints: 500,
    requiredLevel: 4,
    estimatedTime: '2-4 hours',
    objectives: [
      'Select a beginner-friendly GitHub issue',
      'Fork the repository',
      'Implement the solution',
      'Submit a pull request',
      'Engage with code review feedback'
    ],
    isCompleted: false,
    progress: 0,
    githubIssue: {
      repository: 'syntopia-project/beginner-challenges',
      issueNumber: 42,
      url: 'https://github.com/syntopia-project/beginner-challenges/issues/42'
    },
    unlockConditions: ['Reach Level 4', 'Connect GitHub Account']
  },
  {
    id: 'geometry-mastery',
    title: 'Sacred Pattern Mastery',
    description: 'Unlock and explore all basic sacred geometry patterns.',
    category: 'exploration',
    difficulty: 3,
    experiencePoints: 300,
    requiredLevel: 3,
    estimatedTime: '1-2 hours',
    objectives: [
      'Explore the Flower of Life',
      'Study the Vesica Piscis',
      'Understand the Golden Spiral',
      'Create a personal geometry journal',
      'Share insights with the community'
    ],
    isCompleted: false,
    progress: 40
  },
  {
    id: 'wisdom-keeper-path',
    title: 'Document Ancient Wisdom',
    description: 'Research and document a piece of ancient mathematical or spiritual wisdom.',
    category: 'wisdom',
    difficulty: 4,
    experiencePoints: 750,
    requiredLevel: 6,
    estimatedTime: '3-5 hours',
    objectives: [
      'Choose a topic from the wisdom library',
      'Research historical sources',
      'Write a comprehensive article',
      'Include visual examples',
      'Present to the community'
    ],
    isCompleted: false,
    progress: 0,
    unlockConditions: ['Choose Wisdom Keeper Role', 'Complete 5 other quests']
  }
])

const availableQuests = computed(() => {
  return quests.value.filter(quest => 
    !quest.isCompleted && 
    userStore.userLevel >= quest.requiredLevel &&
    areUnlockConditionsMet(quest)
  )
})

const completedQuests = computed(() => {
  return quests.value.filter(quest => quest.isCompleted)
})

const githubQuests = computed(() => {
  return quests.value.filter(quest => quest.githubIssue && !quest.isCompleted)
})

const areUnlockConditionsMet = (quest: Quest): boolean => {
  if (!quest.unlockConditions) return true
  
  // For demo purposes, simplified condition checking
  return quest.unlockConditions.every(condition => {
    if (condition.includes('Level')) {
      const requiredLevel = parseInt(condition.match(/\d+/)?.[0] || '0')
      return userStore.userLevel >= requiredLevel
    }
    if (condition.includes('Profile Setup')) {
      return userStore.user?.role !== 'None'
    }
    return true
  })
}

const getCategoryIcon = (category: string): string => {
  switch (category) {
    case 'coding': return '💻'
    case 'exploration': return '🔍'
    case 'community': return '🤝'
    case 'wisdom': return '📚'
    case 'meditation': return '🧘‍♀️'
    default: return '⭐'
  }
}

const getCategoryColor = (category: string): string => {
  switch (category) {
    case 'coding': return 'text-blue-400'
    case 'exploration': return 'text-purple-400'
    case 'community': return 'text-green-400'
    case 'wisdom': return 'text-yellow-400'
    case 'meditation': return 'text-pink-400'
    default: return 'text-gray-400'
  }
}

const getDifficultyStars = (difficulty: number): string => {
  return '★'.repeat(difficulty) + '☆'.repeat(5 - difficulty)
}

const startQuest = (quest: Quest) => {
  selectedQuest.value = quest
  // In a real app, this would make an API call to start the quest
  console.log('Starting quest:', quest.id)
}

const completeObjective = (questId: string, objectiveIndex: number) => {
  const quest = quests.value.find(q => q.id === questId)
  if (quest && !quest.isCompleted) {
    quest.progress = Math.min(100, quest.progress + (100 / quest.objectives.length))
    if (quest.progress >= 100) {
      quest.isCompleted = true
      userStore.addExperiencePoints(quest.experiencePoints)
    }
  }
}

const getProgressColor = (progress: number): string => {
  if (progress < 25) return 'bg-red-500'
  if (progress < 50) return 'bg-yellow-500'
  if (progress < 75) return 'bg-blue-500'
  return 'bg-green-500'
}

onMounted(() => {
  // Load user's quest progress from backend
  console.log('Loading quest progress for user:', userStore.user?.id)
})
</script>

<template>
  <div class="quests-view">
    <div class="container">
      <!-- Header -->
      <div class="quests-header">
        <h1 class="page-title">Sacred Quests</h1>
        <p class="page-subtitle">
          Embark on meaningful challenges that expand consciousness and contribute to the collective
        </p>
        
        <!-- Progress Overview -->
        <div class="progress-overview">
          <div class="progress-card glass">
            <div class="progress-number">{{ userStore.userLevel }}</div>
            <div class="progress-label">Current Level</div>
          </div>
          <div class="progress-card glass">
            <div class="progress-number">{{ userStore.user?.experiencePoints.toLocaleString() }}</div>
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
          <div v-if="availableQuests.length === 0" class="empty-state card">
            <div class="empty-icon">🎯</div>
            <h3>All Quests Completed!</h3>
            <p>You've completed all available quests for your current level. 
               Continue growing to unlock new challenges.</p>
          </div>
          
          <div 
            v-for="quest in availableQuests" 
            :key="quest.id"
            class="quest-card card"
          >
            <div class="quest-header">
              <div class="quest-category">
                <span class="category-icon">{{ getCategoryIcon(quest.category) }}</span>
                <span class="category-name" :class="getCategoryColor(quest.category)">
                  {{ quest.category.charAt(0).toUpperCase() + quest.category.slice(1) }}
                </span>
              </div>
              <div class="quest-difficulty">
                {{ getDifficultyStars(quest.difficulty) }}
              </div>
            </div>
            
            <h3 class="quest-title">{{ quest.title }}</h3>
            <p class="quest-description">{{ quest.description }}</p>
            
            <div class="quest-meta">
              <div class="meta-item">
                <span class="meta-label">XP Reward:</span>
                <span class="meta-value text-primary">{{ quest.experiencePoints }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">Estimated Time:</span>
                <span class="meta-value">{{ quest.estimatedTime }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">Required Level:</span>
                <span class="meta-value">{{ quest.requiredLevel }}</span>
              </div>
            </div>
            
            <!-- Progress Bar -->
            <div v-if="quest.progress > 0" class="quest-progress">
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
            <div v-if="quest.githubIssue" class="github-link">
              <a :href="quest.githubIssue.url" target="_blank" class="btn btn-ghost btn-sm">
                <span>🔗</span>
                {{ quest.githubIssue.repository }}#{{ quest.githubIssue.issueNumber }}
              </a>
            </div>
            
            <div class="quest-actions">
              <button 
                class="btn btn-primary"
                @click="startQuest(quest)"
              >
                {{ quest.progress > 0 ? 'Continue Quest' : 'Start Quest' }}
              </button>
              <button class="btn btn-ghost">
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
                <span class="category-icon">{{ getCategoryIcon(quest.category) }}</span>
                <span class="category-name" :class="getCategoryColor(quest.category)">
                  {{ quest.category.charAt(0).toUpperCase() + quest.category.slice(1) }}
                </span>
              </div>
              <div class="quest-difficulty">
                {{ getDifficultyStars(quest.difficulty) }}
              </div>
            </div>
            
            <h3 class="quest-title">{{ quest.title }}</h3>
            <p class="quest-description">{{ quest.description }}</p>
            
            <div class="quest-meta">
              <div class="meta-item">
                <span class="meta-label">XP Earned:</span>
                <span class="meta-value text-primary">{{ quest.experiencePoints }}</span>
              </div>
            </div>
            
            <div class="quest-actions">
              <button class="btn btn-ghost">
                View Certificate
              </button>
              <button class="btn btn-ghost">
                Share Achievement
              </button>
            </div>
          </div>
        </div>

        <!-- GitHub Quests -->
        <div v-if="activeTab === 'github'" class="quest-list">
          <div v-if="githubQuests.length === 0" class="empty-state card">
            <div class="empty-icon">💻</div>
            <h3>No GitHub Quests Available</h3>
            <p>GitHub integration quests unlock at Level 4. Keep progressing to access collaborative coding challenges!</p>
          </div>
          
          <div 
            v-for="quest in githubQuests" 
            :key="quest.id"
            class="quest-card card github-quest"
          >
            <div class="github-badge">🔗 GitHub Integration</div>
            
            <div class="quest-header">
              <div class="quest-category">
                <span class="category-icon">{{ getCategoryIcon(quest.category) }}</span>
                <span class="category-name" :class="getCategoryColor(quest.category)">
                  {{ quest.category.charAt(0).toUpperCase() + quest.category.slice(1) }}
                </span>
              </div>
              <div class="quest-difficulty">
                {{ getDifficultyStars(quest.difficulty) }}
              </div>
            </div>
            
            <h3 class="quest-title">{{ quest.title }}</h3>
            <p class="quest-description">{{ quest.description }}</p>
            
            <div class="github-details">
              <div class="repo-info">
                <strong>Repository:</strong> {{ quest.githubIssue?.repository }}
              </div>
              <div class="issue-info">
                <strong>Issue:</strong> #{{ quest.githubIssue?.issueNumber }}
              </div>
            </div>
            
            <div class="quest-actions">
              <a 
                :href="quest.githubIssue?.url" 
                target="_blank" 
                class="btn btn-primary"
              >
                View on GitHub
              </a>
              <button 
                class="btn btn-secondary"
                @click="startQuest(quest)"
              >
                Accept Quest
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Quest Detail Modal -->
      <div v-if="selectedQuest" class="quest-modal-overlay" @click="selectedQuest = null">
        <div class="quest-modal card" @click.stop>
          <div class="modal-header">
            <h2>{{ selectedQuest.title }}</h2>
            <button class="close-button" @click="selectedQuest = null">×</button>
          </div>
          
          <div class="modal-content">
            <p>{{ selectedQuest.description }}</p>
            
            <h3>Objectives:</h3>
            <ul class="objectives-list">
              <li 
                v-for="(objective, index) in selectedQuest.objectives" 
                :key="index"
                class="objective-item"
              >
                <input 
                  type="checkbox" 
                  :id="`obj-${index}`"
                  @change="completeObjective(selectedQuest.id, index)"
                >
                <label :for="`obj-${index}`">{{ objective }}</label>
              </li>
            </ul>
          </div>
          
          <div class="modal-actions">
            <button class="btn btn-secondary" @click="selectedQuest = null">
              Close
            </button>
            <button class="btn btn-primary">
              Mark as Complete
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
  color: var(--color-accent);
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

.modal-content h3 {
  margin: 1.5rem 0 1rem;
  color: var(--color-text);
}

.objectives-list {
  list-style: none;
  padding: 0;
}

.objective-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.objective-item:last-child {
  border-bottom: none;
}

.objective-item input[type="checkbox"] {
  accent-color: var(--color-primary);
}

.objective-item label {
  color: var(--color-text-muted);
  line-height: 1.5;
  cursor: pointer;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  padding: 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

/* Responsive Design */
@media (max-width: 768px) {
  .progress-overview {
    grid-template-columns: repeat(2, 1fr);
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
