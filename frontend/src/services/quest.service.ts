import apiService from './api'
// NOTE: Components should prefer using unwrap/extractList from '@/utils/api-unwrapper'
// rather than manually reaching into .data shapes. This service returns raw ApiResponse
// envelopes so that higher layers can consolidate handling.
import type { ApiResponse } from '@/types/api.types'

// Quest Types
export interface Quest {
  id: string
  key: string
  title: string
  description: string
  role: string | null
  requiredLevel: number
  experienceReward: number
  type: QuestType
  status: QuestStatus
  difficulty: QuestDifficulty
  githubIssueUrl?: string
  githubRepository?: string
  githubIssueNumber?: number
  geometryPatterns?: string
  geometryData?: string
  completionCriteria?: string
  completionSteps?: string[]
  createdAt: string
  updatedAt: string
  dueDate?: string
  autoValidated: boolean
  // New fields for quest categorization
  category?: QuestCategory
  isReusable?: boolean
  maxCompletions?: number
  currentCompletions?: number
}

// UserQuest Interface for individual user progress
export interface UserQuest {
  userQuestId: string // renamed from id in DTO
  userId: string
  questId: string
  status: UserQuestStatus
  progress: number
  progressData?: Record<string, any>
  acceptedAt?: string
  startedAt?: string
  completedAt?: string
  abandonedAt?: string
  lastProgressUpdate?: string
  experienceAwarded?: number
  completionNotes?: string
  completionData?: Record<string, any>
  githubPullRequestUrl?: string
  githubCommitSha?: string
  verified?: boolean
  // Include quest data for convenience
  quest?: Quest
}

export interface Paginated<T> {
  data: T[]
  pagination: {
    page: number
    size: number
    total: number
    hasNext?: boolean
    hasPrev?: boolean
    totalPages?: number
  }
}

export enum QuestCategory {
  INDIVIDUAL = 'INDIVIDUAL',
  SHARED = 'SHARED', 
  UNIQUE = 'UNIQUE'
}

export enum UserQuestStatus {
  USER_AVAILABLE = 'USER_AVAILABLE',
  USER_ACTIVE = 'USER_ACTIVE',
  USER_COMPLETED = 'USER_COMPLETED',
  USER_ABANDONED = 'USER_ABANDONED',
  USER_VERIFIED = 'USER_VERIFIED'
}

export enum QuestType {
  LEARNING = 'LEARNING',
  SKILL_BUILDING = 'SKILL_BUILDING', 
  NETWORKING = 'NETWORKING',
  INTEGRATION = 'INTEGRATION',
  CONTRIBUTION = 'CONTRIBUTION',
  GITHUB_ISSUE = 'GITHUB_ISSUE',
  COMMUNITY = 'COMMUNITY',
  ONBOARDING = 'ONBOARDING',
  GEOMETRY = 'GEOMETRY'
}

export enum QuestDifficulty {
  BEGINNER = 'BEGINNER',
  INTERMEDIATE = 'INTERMEDIATE',
  ADVANCED = 'ADVANCED',
  EXPERT = 'EXPERT'
}

export enum QuestStatus {
  AVAILABLE = 'AVAILABLE',
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  ARCHIVED = 'ARCHIVED',
  SUSPENDED = 'SUSPENDED'
}

export interface QuestFilter {
  type?: QuestType
  difficulty?: QuestDifficulty
  role?: string
  status?: QuestStatus
  minLevel?: number
  maxLevel?: number
}

export interface CreateQuestData {
  title: string
  description: string
  role: string
  requiredLevel: number
  experienceReward: number
  type: QuestType
  difficulty: QuestDifficulty
  category?: QuestCategory
  isReusable?: boolean
  maxCompletions?: number
}

export interface QuestResponse {
  quest: Quest
  message: string
}

class QuestService {
  // ===============================
  // UserQuest API Methods (NEW - Primary Methods)
  // ===============================

  /**
   * Get available quests for user with their individual progress (paginated)
   */
  async getUserQuests(page = 0, size = 50): Promise<ApiResponse<Paginated<UserQuest>>> {
    try {
      return await apiService.get<Paginated<UserQuest>>(`/user-quests/available?page=${page}&size=${size}`)
    } catch (error) {
      console.error('❌ Error fetching user quests:', error)
      return {
        success: false,
        error: { message: 'Failed to fetch user quests' }
      }
    }
  }

  /**
   * Get active quests for the current user (paginated)
   */
  async getUserActiveQuests(page = 0, size = 50): Promise<ApiResponse<Paginated<UserQuest>>> {
    try {
      return await apiService.get<Paginated<UserQuest>>(`/user-quests/active?page=${page}&size=${size}`)
    } catch (error) {
      console.warn('⚠️ /user-quests/active failed:', error)
      return { success: false, error: { message: 'Failed to fetch active user quests' } }
    }
  }

    /** Get completed quests for the current user (paginated) */
    async getUserCompletedQuests(page = 0, size = 50): Promise<ApiResponse<Paginated<UserQuest>>> {
      try {
        return await apiService.get<Paginated<UserQuest>>(`/user-quests/completed?page=${page}&size=${size}`)
      } catch (error) {
        console.warn('⚠️ /user-quests/completed failed:', error)
        return { success: false, error: { message: 'Failed to fetch completed user quests' } }
      }
    }

  /** Accept/Start a quest */
  async acceptQuest(questId: string): Promise<ApiResponse<UserQuest>> {
    try {
      return await apiService.post<UserQuest>(`/user-quests/${questId}/accept`)
    } catch (error) {
      console.error('❌ Error accepting quest:', error)
      return { success: false, error: { message: 'Failed to accept quest' } }
    }
  }

  /** Complete a quest */
  async completeQuest(questId: string, completionData?: Record<string, any>): Promise<ApiResponse<{ experienceAwarded?: number } & UserQuest>> {
    try {
      return await apiService.post<{ experienceAwarded?: number } & UserQuest>(`/user-quests/${questId}/complete`, completionData || {})
    } catch (error) {
      console.error('❌ Error completing quest:', error)
      return { success: false, error: { message: 'Failed to complete quest' } }
    }
  }

  /** Abandon a quest */
  async abandonQuest(questId: string): Promise<ApiResponse<UserQuest>> {
    try {
      return await apiService.post<UserQuest>(`/user-quests/${questId}/abandon`)
    } catch (error) {
      console.error('❌ Error abandoning quest:', error)
      return { success: false, error: { message: 'Failed to abandon quest' } }
    }
  }

  /** Verify a completed quest */
  async verifyQuest(questId: string): Promise<ApiResponse<UserQuest>> {
    try {
      return await apiService.post<UserQuest>(`/user-quests/${questId}/verify`)
    } catch (error) {
      console.error('❌ Error verifying quest:', error)
      return { success: false, error: { message: 'Failed to verify quest' } }
    }
  }

  /** Stats */
  async getUserQuestStatistics(): Promise<ApiResponse<{ totalCompleted: number; activeQuests: number; completedThisWeek: number; recentCompletions: UserQuest[] }>> {
    try {
      return await apiService.get<{ totalCompleted: number; activeQuests: number; completedThisWeek: number; recentCompletions: UserQuest[] }>(`/user-quests/statistics`)
    } catch (error) {
      return { success: false, error: { message: 'Failed to fetch quest statistics' } }
    }
  }

  // ===============================
  // Legacy Quest API Methods (for backward compatibility)
  // ===============================

  /**
   * Get all quests (legacy method)
   */
  async getQuests(): Promise<ApiResponse<{quests: Quest[], count: number, success: boolean}>> {
    try {
      return await apiService.get<{quests: Quest[], count: number, success: boolean}>('/quests')
    } catch (error) {
      console.error('❌ Error fetching quests:', error)
      return {
        success: false,
        error: { message: 'Failed to fetch quests' }
      }
    }
  }

  /**
   * Get quest by ID
   */
  async getQuestById(questId: string): Promise<ApiResponse<Quest>> {
    try {
      return await apiService.get<Quest>(`/quests/${questId}`)
    } catch (error) {
      console.error('❌ Error fetching quest:', error)
      return {
        success: false,
        error: { message: 'Failed to fetch quest' }
      }
    }
  }

  /**
   * Create a new quest
   */
  async createQuest(questData: CreateQuestData): Promise<ApiResponse<QuestResponse>> {
    try {
      return await apiService.post<QuestResponse>('/quests', questData)
    } catch (error) {
      console.error('❌ Error creating quest:', error)
      return {
        success: false,
        error: { message: 'Failed to create quest' }
      }
    }
  }

  // ===============================
  // Helper Methods for UserQuests
  // ===============================

  /**
   * Check if UserQuest is completed
   */
  isUserQuestCompleted(userQuest: UserQuest): boolean {
    return userQuest.status === UserQuestStatus.USER_COMPLETED || 
           userQuest.status === UserQuestStatus.USER_VERIFIED
  }

  /**
   * Check if UserQuest is active
   */
  isUserQuestActive(userQuest: UserQuest): boolean {
    return userQuest.status === UserQuestStatus.USER_ACTIVE
  }

  /**
   * Check if UserQuest is available to start
   */
  isUserQuestAvailable(userQuest: UserQuest): boolean {
    return userQuest.status === UserQuestStatus.USER_AVAILABLE
  }

  /**
   * Get progress percentage for UserQuest
   */
  getUserQuestProgress(userQuest: UserQuest): number {
    return Math.max(0, Math.min(100, userQuest.progress || 0))
  }

  /**
   * Get display status for UserQuest
   */
  getUserQuestDisplayStatus(userQuest: UserQuest): string {
    switch (userQuest.status) {
      case UserQuestStatus.USER_AVAILABLE:
        return 'Available'
      case UserQuestStatus.USER_ACTIVE:
        return 'In Progress'
      case UserQuestStatus.USER_COMPLETED:
        return 'Completed'
      case UserQuestStatus.USER_ABANDONED:
        return 'Abandoned'
      case UserQuestStatus.USER_VERIFIED:
        return 'Verified'
      default:
        return 'Unknown'
    }
  }

  /**
   * Get icon for quest type
   */
  getQuestTypeIcon(type: QuestType): string {
    const icons = {
      [QuestType.LEARNING]: '📚',
      [QuestType.SKILL_BUILDING]: '🔨',
      [QuestType.NETWORKING]: '🤝',
      [QuestType.INTEGRATION]: '🔗',
      [QuestType.CONTRIBUTION]: '🎯',
      [QuestType.GITHUB_ISSUE]: '🐙',
      [QuestType.COMMUNITY]: '🌍',
      [QuestType.ONBOARDING]: '🌟',
      [QuestType.GEOMETRY]: '🔷'
    }
    return icons[type] || '❓'
  }

  /**
   * Get color for quest difficulty
   */
  getDifficultyColor(difficulty: QuestDifficulty): string {
    const colors = {
      [QuestDifficulty.BEGINNER]: '#10b981', // green
      [QuestDifficulty.INTERMEDIATE]: '#f59e0b', // yellow
      [QuestDifficulty.ADVANCED]: '#ef4444', // red
      [QuestDifficulty.EXPERT]: '#8b5cf6' // purple
    }
    return colors[difficulty] || '#6b7280'
  }

  /**
   * Convert Quest to UserQuest (for local state management)
   */
  questToUserQuest(quest: Quest, status: UserQuestStatus = UserQuestStatus.USER_AVAILABLE): UserQuest {
    return {
      userQuestId: `userquest-${quest.id}`,
      userId: '', // Will be filled by user store
      questId: quest.id,
      status,
      progress: status === UserQuestStatus.USER_COMPLETED ? 100 : 0,
      quest
    }
  }
}

// Export singleton instance
const questService = new QuestService()
export default questService
