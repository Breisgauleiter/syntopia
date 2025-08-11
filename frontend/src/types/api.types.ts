/**
 * API-related TypeScript interfaces for Syntopia Platform
 * 
 * Contains request/response types for all API endpoints
 */

// Base API response structure
export interface ApiResponse<T = any> {
  success: boolean
  data?: T
  error?: ApiError
}

// Standardized error structure
export interface ApiError {
  message: string
  field?: string
  code?: string
  status?: number
}

// Authentication related types
export interface LoginRequest {
  username?: string;
  email?: string;
  password: string;
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  displayName?: string
}

export interface AuthResponse {
  user: User
  token: string
  refreshToken: string
}

export interface TokenRefreshRequest {
  refreshToken: string
}

export interface TokenRefreshResponse {
  token: string
  refreshToken: string
}

// User related types
export interface User {
  id: string
  username: string
  email: string
  displayName?: string
  selectedRole?: string
  currentLevel: number
  experiencePoints: number
  questsCompleted: number
  isGitHubIntegrated: boolean
  avatarUrl?: string
  createdAt: string
  lastLoginAt?: string
}

export interface UserProfileUpdateRequest {
  displayName?: string
  selectedRole?: string
  avatarUrl?: string
}

export interface RoleSelectionRequest {
  role: string
}

// GitHub Integration types (Level 4+ feature)
export interface GitHubIntegrationRequest {
  code: string
  state: string
}

export interface GitHubProfile {
  login: string
  name: string
  email: string
  avatar_url: string
  public_repos: number
  followers: number
  following: number
}

// Quest System types (future implementation)
export interface Quest {
  id: string
  title: string
  description: string
  type: QuestType
  difficulty: QuestDifficulty
  experienceReward: number
  status: QuestStatus
  requiredLevel: number
  githubIssueUrl?: string
  createdAt: string
  completedAt?: string
}

export enum QuestType {
  CONTRIBUTION = 'CONTRIBUTION',
  LEARNING = 'LEARNING', 
  SOCIAL = 'SOCIAL',
  GITHUB_ISSUE = 'GITHUB_ISSUE'
}

export enum QuestDifficulty {
  BEGINNER = 'BEGINNER',
  INTERMEDIATE = 'INTERMEDIATE',
  ADVANCED = 'ADVANCED',
  EXPERT = 'EXPERT'
}

export enum QuestStatus {
  AVAILABLE = 'AVAILABLE',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  LOCKED = 'LOCKED'
}

// Community features (future implementation)
// Community features - aligned with backend DTOs
export interface ConnectionRequestDTO {
  toUserId: string
  connectionType: 'friend' | 'mentor' | 'collaborator'
}

export interface ConnectionResponseDTO {
  action: 'accept' | 'decline'
}

export interface ConnectionDTO {
  id: string
  type: string
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED'
  createdAt: string
  updatedAt?: string
  otherUser: {
    id: string
    displayName: string
    avatarUrl?: string
    level?: number
    role?: string
  }
}

export interface CreateProjectDTO {
  title: string
  description: string
  tags?: string[]
  visibility?: 'public' | 'private'
}

export interface ProjectDTO {
  id: string
  title: string
  description: string
  tags: string[]
  visibility: string
  createdAt: string
  membersCount: number
  owner: {
    id: string
    displayName: string
    avatarUrl?: string
  }
}

export interface LeaderboardEntryDTO {
  user: {
    id: string
    displayName: string
    avatarUrl?: string
    level?: number
  }
  xp: number
  completed: number
  rank: number
}

export interface FeedItemDTO {
  type: 'quest_completed' | 'project_created' | 'connection_accepted'
  timestamp: string
  user?: {
    id: string
    displayName: string
    avatarUrl?: string
  }
  quest?: {
    id: string
    title: string
    xp?: number
  }
  project?: {
    id: string
    title: string
  }
  connection?: {
    from: string
    to: string
  }
}

export interface CommunityStatsDTO {
  users: number
  activeUsers7d: number
  projects: number
  connections: number
  questsCompleted: number
}

export interface CommunityFeedResponse {
  items: FeedItemDTO[]
  pagination: {
    page: number
    size: number
    total: number
    hasNext: boolean
  }
}

export interface UserDirectoryResponse {
  users: Array<{
    id: string
    displayName: string
    avatarUrl?: string
    role: string
    level: number
    connectionStatus?: 'CONNECTED' | 'PENDING' | 'NONE'
  }>
  pagination: {
    page: number
    size: number
    total: number
    hasNext: boolean
  }
}

export interface Comment {
  id: string
  postId: string
  authorId: string
  author: User
  content: string
  likes: number
  createdAt: string
}

// Pagination wrapper
export interface PaginatedResponse<T> {
  data: T[]
  pagination: {
    page: number
    limit: number
    total: number
    totalPages: number
    hasNext: boolean
    hasPrev: boolean
  }
}

// Search and filtering
export interface SearchRequest {
  query: string
  filters?: Record<string, any>
  sort?: string
  page?: number
  limit?: number
}

// Level and progression types
export interface LevelProgress {
  currentLevel: number
  currentExperience: number
  experienceToNextLevel: number
  totalExperienceForLevel: number
  progressPercentage: number
}

export interface Achievement {
  id: string
  name: string
  description: string
  icon: string
  unlockedAt?: string
  isUnlocked: boolean
}

// Sacred Geometry patterns (for background component)
export type GeometryPattern = 'flower-of-life' | 'metatrons-cube' | 'sri-yantra' | 'tree-of-life'

export interface GeometryConfig {
  pattern: GeometryPattern
  size: number
  speed: number
  colors: string[]
  interactive: boolean
}

// API endpoint paths (for type safety)
export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh',
    ME: '/auth/me'
  },
  USERS: {
    PROFILE: (id: string) => `/users/${id}`,
    ROLE: (id: string) => `/users/${id}/role`,
    GITHUB: (id: string) => `/users/${id}/github`
  },
  QUESTS: {
    LIST: '/quests',
    DETAIL: (id: string) => `/quests/${id}`,
    COMPLETE: (id: string) => `/quests/${id}/complete`
  },
  COMMUNITY: {
    POSTS: '/community/posts',
    POST: (id: string) => `/community/posts/${id}`,
    COMMENTS: (postId: string) => `/community/posts/${postId}/comments`
  }
} as const
