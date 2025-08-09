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

export interface OnboardingQuestFilter {
  role?: string
  level?: number
}

export class OnboardingService {
  
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
    // This would typically connect to user service
    // For now, return mock data structure
    return {
      userId,
      currentLevel: 1,
      currentXP: 0,
      completedQuests: [],
      selectedRole: null,
      isGitHubConnected: false,
      synPrinciplesUnderstood: [],
      onboardingStartedAt: new Date(),
      lastActivityAt: new Date()
    }
  }
  
  /**
   * Update user's onboarding progress
   */
  static async updateOnboardingProgress(userId: string, progress: Partial<OnboardingProgress>): Promise<OnboardingProgress> {
    // This would typically connect to user service
    // For now, simulate progress update
    console.log(`Updating onboarding progress for user ${userId}:`, progress)
    
    // Simulate API call delay
    return new Promise(resolve => {
      setTimeout(() => {
        resolve({
          userId,
          currentLevel: progress.currentLevel || 1,
          currentXP: progress.currentXP || 0,
          completedQuests: progress.completedQuests || [],
          selectedRole: progress.selectedRole || null,
          isGitHubConnected: progress.isGitHubConnected || false,
          synPrinciplesUnderstood: progress.synPrinciplesUnderstood || [],
          onboardingStartedAt: new Date(),
          lastActivityAt: new Date()
        })
      }, 500)
    })
  }
  
  /**
   * Mark a quest as completed and award XP
   */
  static async completeQuest(userId: string, questId: string): Promise<QuestCompletionResult> {
    // This would typically connect to user service and quest service
    // For now, simulate quest completion
    console.log(`Completing quest ${questId} for user ${userId}`)
    
    return new Promise(resolve => {
      setTimeout(() => {
        resolve({
          questId,
          xpAwarded: 100, // This should come from quest data
          newLevel: 1,
          newTotalXP: 100,
          isLevelUp: false,
          unlockedFeatures: [],
          completedAt: new Date()
        })
      }, 1000)
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
