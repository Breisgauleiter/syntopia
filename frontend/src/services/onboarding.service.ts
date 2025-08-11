// Frontend service for onboarding quest management
import axios from 'axios'

// Type definitions - inline for now to avoid import issues
export interface OnboardingQuest {
  id: string
  title: string
  description: string
  level: number
  xpReward: number
  synPrinciple: string
  roleDialect: string
  steps: string[]
  iconType: string
  profileRequired: boolean
  metadata: any
}

export interface SynPrinciple {
  level: number
  name: string
  subtitle: string
  description: string
}

export interface ExperienceLevel {
  level: number
  xpRequired: number
  title: string
  description: string
}

export interface OnboardingProgress {
  userId: string
  currentLevel: number
  currentXP: number
  completedQuests: string[]
  selectedRole: string | null
  isGitHubConnected: boolean
  synPrinciplesUnderstood: string[]
  onboardingStartedAt: Date
  lastActivityAt: Date
  onboardingCompleted?: boolean
  onboardingCompletedAt?: Date
}

export interface QuestCompletionResult {
  questId: string
  xpAwarded: number
  newLevel: number
  newTotalXP: number
  isLevelUp: boolean
  unlockedFeatures: string[]
  completedAt: Date
}

const api = axios.create({
  baseURL: '/api/onboarding', // Uses Vite proxy to route to backend
  headers: {
    'Content-Type': 'application/json'
  }
})

// Attach Authorization header if token exists
api.interceptors.request.use((config) => {
  try {
    const token = localStorage.getItem('syntopia_token')
    if (token) {
      config.headers = config.headers || {}
      ;(config.headers as any)['Authorization'] = `Bearer ${token}`
    }
  } catch {}
  return config
})

export interface OnboardingQuestFilter {
  role?: string
  level?: number
}

export class OnboardingService {
  private static STORAGE_KEY_PREFIX = 'onboarding_progress:'
  private static XP_REQUIREMENTS = [100, 200, 300, 500]
  
  private static getStorageKey(userId: string) {
    return `${this.STORAGE_KEY_PREFIX}${userId}`
  }

  /**
   * Accept/start the onboarding quest for a given role and level on the backend.
   * Backend uses the authenticated user; we only need to send role and level.
   */
  static async accept(role: string, level: number): Promise<{ success: boolean; message?: string }> {
    try {
      const response = await api.post('/accept', { role, level })
      return response.data || { success: true }
    } catch (error) {
      console.error('Error accepting onboarding quest:', error)
      // Return a non-throwing result so UX can still navigate; caller can decide to ignore
      return { success: false, message: 'Failed to accept onboarding quest' }
    }
  }
  
  private static defaultProgress(userId: string): OnboardingProgress {
    const now = new Date()
    return {
      userId,
      currentLevel: 1,
      currentXP: 0,
      completedQuests: [],
      selectedRole: null,
      isGitHubConnected: false,
      synPrinciplesUnderstood: [],
      onboardingStartedAt: now,
      lastActivityAt: now,
      onboardingCompleted: false
    }
  }
  
  private static loadProgress(userId: string): OnboardingProgress {
    try {
      const raw = localStorage.getItem(this.getStorageKey(userId))
      if (!raw) return this.defaultProgress(userId)
      const parsed = JSON.parse(raw)
      return {
        ...this.defaultProgress(userId),
        ...parsed,
      }
    } catch {
      return this.defaultProgress(userId)
    }
  }
  
  private static saveProgress(userId: string, patch: Partial<OnboardingProgress>): OnboardingProgress {
    const current = this.loadProgress(userId)
    const updated: OnboardingProgress = {
      ...current,
      ...patch,
      userId,
      lastActivityAt: new Date()
    }
    localStorage.setItem(this.getStorageKey(userId), JSON.stringify(updated))
    return updated
  }
  
  /**
   * Get all onboarding quests for all roles and levels 1-4
   */
  static async getAllOnboardingQuests(): Promise<OnboardingQuest[]> {
    try {
      const response = await api.get('/quests')
      return response.data
    } catch (error) {
      console.error('Error fetching all onboarding quests:', error)
      throw new Error('Failed to load onboarding quests')
    }
  }
  
  /**
   * Get onboarding quests filtered by role
   */
  static async getQuestsByRole(role: string): Promise<OnboardingQuest[]> {
    try {
      const response = await api.get(`/quests/role/${encodeURIComponent(role)}`)
      return response.data
    } catch (error) {
      console.error(`Error fetching quests for role ${role}:`, error)
      throw new Error(`Failed to load quests for ${role}`)
    }
  }
  
  /**
   * Get onboarding quests filtered by level (1-4)
   */
  static async getQuestsByLevel(level: number): Promise<OnboardingQuest[]> {
    try {
      if (level < 1 || level > 4) {
        throw new Error('Level must be between 1 and 4')
      }
      
      const response = await api.get(`/quests/level/${level}`)
      return response.data
    } catch (error) {
      console.error(`Error fetching quests for level ${level}:`, error)
      throw new Error(`Failed to load level ${level} quests`)
    }
  }
  
  /**
   * Get a specific onboarding quest by role and level
   */
  static async getQuestByRoleAndLevel(role: string, level: number): Promise<OnboardingQuest> {
    try {
      if (level < 1 || level > 4) {
        throw new Error('Level must be between 1 and 4')
      }
      
      const response = await api.get(`/quests/role/${encodeURIComponent(role)}/level/${level}`)
      return response.data
    } catch (error) {
      console.error(`Error fetching quest for ${role} level ${level}:`, error)
      throw new Error(`Failed to load ${role} level ${level} quest`)
    }
  }
  
  /**
   * Get all available sacred roles
   */
  static async getAvailableRoles(): Promise<string[]> {
    try {
      const response = await api.get('/roles')
      return response.data
    } catch (error) {
      console.error('Error fetching available roles:', error)
      throw new Error('Failed to load available roles')
    }
  }
  
  /**
   * Get the 4 SYN principles information
   */
  static async getSynPrinciples(): Promise<SynPrinciple[]> {
    try {
      const response = await api.get('/syn-principles')
      return response.data
    } catch (error) {
      console.error('Error fetching SYN principles:', error)
      throw new Error('Failed to load SYN principles')
    }
  }
  
  /**
   * Get Fibonacci-based experience levels information
   */
  static async getExperienceLevels(): Promise<ExperienceLevel[]> {
    try {
      const response = await api.get('/experience-levels')
      return response.data
    } catch (error) {
      console.error('Error fetching experience levels:', error)
      throw new Error('Failed to load experience levels')
    }
  }
  
  /**
   * Get user's current onboarding progress
   */
  static async getUserOnboardingProgress(userId: string): Promise<OnboardingProgress> {
    // TODO: Replace with backend persistence when available
    return this.loadProgress(userId)
  }
  
  /**
   * Update user's onboarding progress
   */
  static async updateOnboardingProgress(userId: string, progress: Partial<OnboardingProgress>): Promise<OnboardingProgress> {
    console.log(`Updating onboarding progress for user ${userId}:`, progress)
    return new Promise(resolve => {
      setTimeout(() => {
        const updated = this.saveProgress(userId, progress)
        resolve(updated)
      }, 200)
    })
  }
  
  /**
   * Mark a quest as completed and award XP
   */
  static async completeQuest(userId: string, questId: string): Promise<QuestCompletionResult> {
    console.log(`Completing quest ${questId} for user ${userId}`)
    const progress = this.loadProgress(userId)
    const xpAwarded = 100
    const currentLevelIndex = Math.max(0, Math.min(progress.currentLevel - 1, this.XP_REQUIREMENTS.length - 1))
    const requirement = this.XP_REQUIREMENTS[currentLevelIndex]
    const newTotalXP = (progress.currentXP || 0) + xpAwarded
    let isLevelUp = false
    let newLevel = progress.currentLevel
    if (newTotalXP >= requirement) {
      isLevelUp = true
      newLevel = progress.currentLevel + 1
    }
    const onboardingCompleted = newLevel > 4
    const updated = this.saveProgress(userId, {
      currentXP: newTotalXP,
      currentLevel: newLevel,
      completedQuests: Array.from(new Set([...(progress.completedQuests || []), questId])),
      onboardingCompleted,
      onboardingCompletedAt: onboardingCompleted ? new Date() : progress.onboardingCompletedAt
    })
    return new Promise(resolve => {
      setTimeout(() => {
        resolve({
          questId,
          xpAwarded,
          newLevel: updated.currentLevel,
          newTotalXP: updated.currentXP,
          isLevelUp,
          unlockedFeatures: [],
          completedAt: new Date()
        })
      }, 400)
    })
  }
  
  /**
   * Check if user has unlocked GitHub integration (level 4)
   */
  static async isGitHubIntegrationUnlocked(userId: string): Promise<boolean> {
    try {
      const progress = await this.getUserOnboardingProgress(userId)
      return progress.currentLevel >= 4
    } catch (error) {
      console.error('Error checking GitHub integration unlock:', error)
      return false
    }
  }
  
  /**
   * Get role-specific quest recommendations based on user progress
   */
  static async getQuestRecommendations(userId: string, role: string): Promise<OnboardingQuest[]> {
    try {
      const progress = await this.getUserOnboardingProgress(userId)
      const nextLevel = progress.currentLevel + 1
      
      if (nextLevel > 4) {
        return [] // User has completed all onboarding quests
      }
      
      return [await this.getQuestByRoleAndLevel(role, nextLevel)]
    } catch (error) {
      console.error('Error getting quest recommendations:', error)
      return []
    }
  }
}

// Type definitions for onboarding progress tracking
export interface OnboardingProgress {
  userId: string
  currentLevel: number
  currentXP: number
  completedQuests: string[]
  selectedRole: string | null
  isGitHubConnected: boolean
  synPrinciplesUnderstood: string[]
  onboardingStartedAt: Date
  lastActivityAt: Date
  onboardingCompleted?: boolean
  onboardingCompletedAt?: Date
}

export interface QuestCompletionResult {
  questId: string
  xpAwarded: number
  newLevel: number
  newTotalXP: number
  isLevelUp: boolean
  unlockedFeatures: string[]
  completedAt: Date
}

export default OnboardingService
