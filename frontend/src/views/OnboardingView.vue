<template>
  <div class="onboarding-view">
    <div class="container">
      
      <!-- Level Progress Header -->
      <div class="onboarding-header">
        <div class="level-progress">
          <div class="level-indicator">
            <span class="level-number">{{ currentLevel }}</span>
            <span class="level-title">{{ getCurrentLevelTitle() }}</span>
          </div>
          <div class="xp-progress">
            <div class="xp-bar">
              <div 
                class="xp-fill" 
                :style="{ width: `${(currentXP / getXPRequiredForNextLevel()) * 100}%` }"
              ></div>
            </div>
            <span class="xp-text">{{ currentXP }} / {{ getXPRequiredForNextLevel() }} XP</span>
          </div>
        </div>
      </div>

      <!-- Role Selection Phase (Level 1) -->
      <div v-if="currentLevel === 1 && !selectedRole" class="onboarding-card card">
        <h1 class="form-title">🌟 Sacred Role Selection</h1>
        <p class="form-subtitle">Wähle deinen Pfad im bewussten Kollektiv der Weltveränderer</p>
        
        <div class="roles-grid">
          <div 
            v-for="role in availableRoles" 
            :key="role.name"
            class="role-option"
            :class="{ active: pendingRole === role.name }"
            @click="pendingRole = role.name"
          >
            <div class="role-icon" :style="{ color: role.color }">
              {{ getRoleIcon(role.iconType) }}
            </div>
            <h3 class="role-name">{{ role.name }}</h3>
            <p class="role-dialect">{{ role.dialect }}</p>
            <p class="role-description">{{ role.description }}</p>
            <div class="role-characteristics">
              <span 
                v-for="char in role.characteristics" 
                :key="char"
                class="characteristic-tag"
              >
                {{ char }}
              </span>
            </div>
          </div>
        </div>
        
        <button 
          v-if="pendingRole"
          @click="selectRole"
          class="btn btn-primary btn-full"
        >
          Begin as {{ getRoleDialect(pendingRole) }}
        </button>
      </div>

  <!-- Quest Phase (All Levels) -->
  <div v-else-if="currentQuest && !onboardingCompleted" class="quest-container">
        <div class="quest-card card">
          <div class="quest-header">
            <div class="quest-level-badge">Level {{ currentQuest.level }}</div>
            <div class="syn-principle-badge">{{ currentQuest.synPrinciple }}</div>
          </div>
          
          <div class="quest-title-section">
            <div class="quest-icon">{{ getQuestIcon(currentQuest.iconType) }}</div>
            <div>
              <h1 class="quest-title">{{ currentQuest.title }}</h1>
              <p class="quest-description">{{ currentQuest.description }}</p>
            </div>
          </div>

          <div class="quest-metadata" v-if="currentQuest.metadata">
            <div class="principle-intro">
              <h3>🔮 {{ currentQuest.metadata.principleIntro }}</h3>
            </div>
          </div>

          <div class="quest-steps">
            <h3>Quest Steps:</h3>
            <div class="steps-list">
              <div 
                v-for="(step, index) in currentQuest.steps" 
                :key="index"
                class="quest-step"
                :class="{ completed: isStepCompleted(index) }"
              >
                <div class="step-indicator">
                  <span v-if="isStepCompleted(index)">✅</span>
                  <span v-else>{{ index + 1 }}</span>
                </div>
                <span class="step-text">{{ step }}</span>
                <button 
                  v-if="!isStepCompleted(index) && index === currentStepIndex"
                  @click="completeStep(index)"
                  class="btn btn-sm btn-primary"
                >
                  Complete
                </button>
              </div>
            </div>
          </div>

          <div class="quest-rewards">
            <div class="xp-reward">
              <span class="reward-icon">⭐</span>
              <span>{{ currentQuest.xpReward }} XP Reward</span>
            </div>
          </div>

          <div class="quest-actions">
            <button 
              v-if="canCompleteQuest()"
              @click="completeQuest"
              class="btn btn-primary btn-full"
              :disabled="isCompleting"
            >
              {{ isCompleting ? 'Completing...' : 'Complete Quest' }}
            </button>
          </div>
        </div>

        <!-- SYN Principles Learning (Level 2) -->
        <div v-if="currentLevel === 2" class="syn-principles-section card">
          <h2>🔮 Die 4 SYN-Prinzipien</h2>
          <div class="principles-grid">
            <div 
              v-for="principle in synPrinciples" 
              :key="principle.name"
              class="principle-card"
              :class="{ studied: isPrincipleStudied(principle.name) }"
            >
              <div class="principle-level">{{ principle.level }}</div>
              <h3>{{ principle.name }}</h3>
              <p class="principle-subtitle">{{ principle.subtitle }}</p>
              <p class="principle-description">{{ principle.description }}</p>
              <button 
                v-if="!isPrincipleStudied(principle.name)"
                @click="studyPrinciple(principle.name)"
                class="btn btn-secondary btn-sm"
              >
                Study
              </button>
            </div>
          </div>
        </div>

        <!-- GitHub Integration (Level 4) -->
        <div v-if="currentLevel === 4" class="github-integration card">
          <h2>🔗 GitHub Synchronicity Activation</h2>
          <p>Connect your GitHub account to unlock Sacred Development Quests</p>
          
          <div v-if="!isGitHubConnected" class="github-connect">
            <button @click="connectGitHub" class="btn btn-primary">
              Connect GitHub Account
            </button>
          </div>
          
          <div v-else class="github-connected">
            <p>✅ GitHub Connected Successfully!</p>
            <p>Quest synchronization is now active.</p>
          </div>
        </div>
      </div>

      <!-- Completion State -->
      <div v-else-if="onboardingCompleted" class="loading-container card">
        <div class="sacred-spinner"></div>
        <h2>🎉 Onboarding Complete</h2>
        <p>Your sacred journey has begun. Explore quests and the community.</p>
        <button class="btn btn-primary" @click="goToQuests">Go to Quests</button>
      </div>

      <!-- Loading State -->
      <div v-else-if="isLoading" class="loading-container card">
        <div class="sacred-spinner"></div>
        <p>Initializing your sacred journey...</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { OnboardingService } from '@/services/onboarding.service'
import type { 
  OnboardingQuest, 
  SynPrinciple,
  OnboardingProgress 
} from '@/services/onboarding.service'

// Local interface definitions
interface SacredRole {
  name: string
  dialect: string
  description: string
  iconType: string
  color: string
  characteristics: string[]
}

const router = useRouter()
const userStore = useUserStore()

// Reactive state
const isLoading = ref(true)
const isCompleting = ref(false)
const currentLevel = ref(1)
const currentXP = ref(0)
const selectedRole = ref<string>('')
const pendingRole = ref<string>('')
const currentQuest = ref<OnboardingQuest | null>(null)
const onboardingCompleted = ref(false)
const availableRoles = ref<SacredRole[]>([])
const synPrinciples = ref<SynPrinciple[]>([])
const completedSteps = ref<number[]>([])
const completedQuests = ref<string[]>([])
const studiedPrinciples = ref<string[]>([])
const isGitHubConnected = ref(false)

// Computed
const currentStepIndex = computed(() => {
  return completedSteps.value.length
})

// Sacred Roles Data (constants)
const sacredRolesData: SacredRole[] = [
  {
    name: 'Tech Development',
    dialect: 'Codesmith',
    description: 'Sacred architects of conscious technology systems',
    iconType: 'code-sacred',
    color: '#00D4FF',
    characteristics: ['Conscious Coding', 'Sacred Architecture', 'Open Source Wisdom']
  },
  {
    name: 'Business Development', 
    dialect: 'Visionary',
    description: 'Conscious entrepreneurs bridging innovation and sustainability',
    iconType: 'vision-sacred',
    color: '#FF6B35', 
    characteristics: ['Conscious Entrepreneurship', 'Regenerative Business', 'Innovation Leadership']
  },
  {
    name: 'UX Design',
    dialect: 'Creator', 
    description: 'Sacred interface designers expanding user consciousness',
    iconType: 'design-sacred',
    color: '#8B5CF6',
    characteristics: ['Sacred Interface Design', 'Consciousness UI/UX', 'Creative Transformation']
  },
  {
    name: 'Data Science',
    dialect: 'Analyst',
    description: 'Ethical data scientists transforming information into wisdom',
    iconType: 'data-sacred',
    color: '#10B981',
    characteristics: ['Ethical Analytics', 'Privacy-First Data', 'Conscious AI']
  },
  {
    name: 'Legal Advisory',
    dialect: 'Guardian',
    description: 'Legal innovators bridging traditional law and conscious governance',
    iconType: 'justice-sacred', 
    color: '#F59E0B',
    characteristics: ['Ethical Governance', 'Legal Innovation', 'Justice Architecture']
  },
  {
    name: 'Finance Analysis',
    dialect: 'Steward',
    description: 'Conscious finance experts building regenerative economic systems',
    iconType: 'finance-sacred',
    color: '#EF4444',
    characteristics: ['Conscious Capital', 'Regenerative Economics', 'Impact Investment']
  },
  {
    name: 'Sustainability Lead',
    dialect: 'Planetary Steward',
    description: 'Environmental guardians integrating ecological wisdom into all systems',
    iconType: 'sustainability-sacred',
    color: '#059669', 
    characteristics: ['Planetary Stewardship', 'Regenerative Systems', 'Ecological Innovation']
  }
]

// Lifecycle
onMounted(async () => {
  await initializeOnboarding()
})

// Methods
async function initializeOnboarding() {
  try {
    isLoading.value = true
    
    availableRoles.value = sacredRolesData
    synPrinciples.value = await OnboardingService.getSynPrinciples()
    
    // Load user progress (mock for now)
    const userId = userStore.user?.id || 'user-123'
    const progress = await OnboardingService.getUserOnboardingProgress(userId)
    
    currentLevel.value = progress.currentLevel
    currentXP.value = progress.currentXP
    selectedRole.value = progress.selectedRole || ''
    isGitHubConnected.value = progress.isGitHubConnected
    studiedPrinciples.value = progress.synPrinciplesUnderstood
    onboardingCompleted.value = !!progress.onboardingCompleted || currentLevel.value > 4
    
    // If user has selected a role and not completed, ensure backend has ACTIVE onboarding
    if (selectedRole.value && currentLevel.value <= 4 && !onboardingCompleted.value) {
      // Fire-and-forget accept to backend (non-blocking)
      OnboardingService.accept(selectedRole.value, currentLevel.value).finally(() => {})
      await loadCurrentQuest()
    }
    
    // If completed, redirect to quests after a brief delay
    if (onboardingCompleted.value) {
      setTimeout(() => router.replace('/quests'), 600)
      return
    }
    
  } catch (error) {
    console.error('Error initializing onboarding:', error)
  } finally {
    isLoading.value = false
  }
}

async function loadCurrentQuest() {
  try {
  if (selectedRole.value && currentLevel.value <= 4 && !onboardingCompleted.value) {
      currentQuest.value = await OnboardingService.getQuestByRoleAndLevel(selectedRole.value, currentLevel.value)
    }
  } catch (error) {
    console.error('Error loading current quest:', error)
  }
}

async function selectRole() {
  if (!pendingRole.value) return
  
  try {
    selectedRole.value = pendingRole.value
    await loadCurrentQuest()
    
    // Update user progress
    const userId = userStore.user?.id || 'user-123'
    await OnboardingService.updateOnboardingProgress(userId, {
      selectedRole: selectedRole.value
    })
    // Inform backend to mark onboarding ACTIVE at this level
    if (selectedRole.value && currentLevel.value <= 4 && !onboardingCompleted.value) {
      OnboardingService.accept(selectedRole.value, currentLevel.value).finally(() => {})
    }
    
  } catch (error) {
    console.error('Error selecting role:', error)
  }
}

function completeStep(stepIndex: number) {
  if (!completedSteps.value.includes(stepIndex)) {
    completedSteps.value.push(stepIndex)
  }
}

function isStepCompleted(stepIndex: number): boolean {
  return completedSteps.value.includes(stepIndex)
}

function canCompleteQuest(): boolean {
  if (!currentQuest.value) return false
  return completedSteps.value.length >= currentQuest.value.steps.length
}

async function completeQuest() {
  if (!currentQuest.value || !canCompleteQuest()) return
  
  try {
    isCompleting.value = true
    
    const userId = userStore.user?.id || 'user-123'
    const result = await OnboardingService.completeQuest(userId, currentQuest.value.id)
    
    // Update progress
    currentXP.value = result.newTotalXP
    if (result.isLevelUp) {
      currentLevel.value = result.newLevel
    }
    
    completedQuests.value.push(currentQuest.value.id)
    completedSteps.value = []
    
    // Persist progress and determine completion state
    const updated = await OnboardingService.updateOnboardingProgress(userId, {
      currentLevel: currentLevel.value,
      currentXP: currentXP.value,
      completedQuests: completedQuests.value
    })
    onboardingCompleted.value = !!updated.onboardingCompleted || currentLevel.value > 4
    
    // Load next quest or complete onboarding
    if (!onboardingCompleted.value && currentLevel.value <= 4) {
      await loadCurrentQuest()
    } else {
      // Redirect to quests view when complete
      router.replace('/quests')
    }
    
  } catch (error) {
    console.error('Error completing quest:', error)
  } finally {
    isCompleting.value = false
  }
}

function studyPrinciple(principleName: string) {
  if (!studiedPrinciples.value.includes(principleName)) {
    studiedPrinciples.value.push(principleName)
  }
}

function isPrincipleStudied(principleName: string): boolean {
  return studiedPrinciples.value.includes(principleName)
}

async function connectGitHub() {
  try {
    // Mock GitHub connection
    isGitHubConnected.value = true
    
    const userId = userStore.user?.id || 'user-123'
    const updated = await OnboardingService.updateOnboardingProgress(userId, {
      isGitHubConnected: true,
      onboardingCompleted: true,
      onboardingCompletedAt: new Date()
    })
    onboardingCompleted.value = !!updated.onboardingCompleted
    if (onboardingCompleted.value) {
      router.replace('/quests')
    }
    
  } catch (error) {
    console.error('Error connecting GitHub:', error)
  }
}

function goToQuests() {
  router.push('/quests')
}

// Utility functions
function getCurrentLevelTitle(): string {
  const titles = [
    'Profile Genesis',
    'Principle Integration', 
    'Community Formation',
    'Synchronicity Activation'
  ]
  return titles[currentLevel.value - 1] || 'Sacred Journey'
}

function getXPRequiredForNextLevel(): number {
  const requirements = [100, 200, 300, 500, 800]
  return requirements[currentLevel.value - 1] || 1000
}

function getRoleDialect(roleName: string): string {
  const role = sacredRolesData.find(r => r.name === roleName)
  return role?.dialect || roleName
}

function getRoleIcon(iconType: string): string {
  const icons: { [key: string]: string } = {
    'code-sacred': '🔧',
    'vision-sacred': '🌱', 
    'design-sacred': '🎨',
    'data-sacred': '📊',
    'justice-sacred': '⚖️',
    'finance-sacred': '💰',
    'sustainability-sacred': '🌍'
  }
  return icons[iconType] || '🔮'
}

function getQuestIcon(iconType: string): string {
  const icons: { [key: string]: string } = {
    'profile-setup': '👤',
    'synthesis-evolution': '⚡',
    'network-synarchy': '🕸️',
    'synchronicity-github': '🔗'
  }
  return icons[iconType] || '⭐'
}

const getSacredRole = (roleId: string) => {
  return sacredRolesData.find(role => role.name === roleId)
}

const confirmRole = async () => {
  if (selectedRole.value) {
    // Update user role in store
    const role = getSacredRole(selectedRole.value)
    if (role) {
      await userStore.selectRole(role.name)
      router.push('/')
    }
  }
}
</script>

<style scoped>
.onboarding-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 0;
}

.onboarding-card {
  max-width: 800px;
  width: 100%;
  padding: 2rem;
}

.form-title {
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  text-align: center;
  color: var(--color-text);
}

.form-subtitle {
  color: var(--color-text-muted);
  text-align: center;
  margin-bottom: 3rem;
}

.roles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.role-option {
  padding: 1.5rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.02);
  cursor: pointer;
  transition: all var(--transition-normal);
  text-align: center;
}

.role-option:hover {
  background: rgba(255, 255, 255, 0.05);
  transform: translateY(-2px);
}

.role-option.active {
  border-color: var(--color-primary);
  background: var(--color-primary-alpha);
}

.role-icon {
  font-size: 2rem;
  margin-bottom: 1rem;
}

.role-name {
  font-size: 1.125rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: var(--color-text);
}

.role-description {
  color: var(--color-text-muted);
  font-size: 0.875rem;
  line-height: 1.5;
}

.btn-full {
  width: 100%;
}

@media (max-width: 768px) {
  .roles-grid {
    grid-template-columns: 1fr;
  }
}
</style>
