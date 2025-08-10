import api from './api'

/**
 * Community Service for Syntopia Platform
 * 
 * Provides consistent API interface for community management operations
 * following the same patterns as Quest system for better consistency.
 */
export class CommunityService {
  
  /**
   * Get community activity feed
   */
  async getCommunityFeed(page = 0, size = 20) {
    try {
      const response = await api.get(`/api/community/feed?page=${page}&size=${size}`)
      return {
        success: true,
        data: response.data.data
      }
    } catch (error) {
      return {
        success: false,
        error: error as any
      }
    }
  }
  
  /**
   * Get user directory for discovering connections
   */
  async getUserDirectory(search?: string, role?: string, minLevel?: number, page = 0, size = 20) {
    try {
      const params = new URLSearchParams()
      if (search) params.append('search', search)
      if (role) params.append('role', role)
      if (minLevel !== undefined) params.append('minLevel', minLevel.toString())
      params.append('page', page.toString())
      params.append('size', size.toString())
      
      const response = await api.get(`/api/community/users?${params}`)
      return {
        success: true,
        data: response.data.data
      }
    } catch (error) {
      return {
        success: false,
        error: error as any
      }
    }
  }
  
  /**
   * Send connection request
   */
  async sendConnectionRequest(targetUserId: string, message?: string, connectionType = 'collaboration') {
    try {
      const response = await api.post('/api/community/connect', {
        targetUserId,
        message,
        connectionType
      })
      return {
        success: true,
        data: response.data.data,
        message: response.data.message
      }
    } catch (error) {
      return {
        success: false,
        error: error as any
      }
    }
  }
  
  /**
   * Respond to connection request
   */
  async respondToConnectionRequest(requestId: string, action: 'accept' | 'decline') {
    try {
      const response = await api.put(`/api/community/connect/${requestId}`, { action })
      return {
        success: true,
        data: response.data.data,
        message: response.data.message
      }
    } catch (error) {
      return {
        success: false,
        error: error as any
      }
    }
  }
  
  /**
   * Get user's connections
   */
  async getConnections() {
    try {
      const response = await api.get('/api/community/connections')
      return {
        success: true,
        data: response.data.data
      }
    } catch (error) {
      return {
        success: false,
        error: error as any
      }
    }
  }
  
  /**
   * Get community projects
   */
  async getCommunityProjects(page = 0, size = 20) {
    try {
      const response = await api.get(`/api/community/projects?page=${page}&size=${size}`)
      return {
        success: true,
        data: response.data.data
      }
    } catch (error) {
      return {
        success: false,
        error: error as any
      }
    }
  }
  
  /**
   * Create new community project
   */
  async createProject(projectData: {
    name: string
    description: string
    requiredSkills?: string[]
    maxCollaborators?: number
  }) {
    try {
      const response = await api.post('/api/community/projects', projectData)
      return {
        success: true,
        data: response.data.data,
        message: response.data.message
      }
    } catch (error) {
      return {
        success: false,
        error: error as any
      }
    }
  }
  
  /**
   * Get community statistics
   */
  async getCommunityStats() {
    try {
      const response = await api.get('/api/community/stats')
      return {
        success: true,
        data: response.data.data
      }
    } catch (error) {
      return {
        success: false,
        error: error as any
      }
    }
  }
  
  /**
   * Get leaderboard
   */
  async getLeaderboard(type = 'experience', page = 0, size = 50) {
    try {
      const response = await api.get(`/api/community/leaderboard?type=${type}&page=${page}&size=${size}`)
      return {
        success: true,
        data: response.data
      }
    } catch (error) {
      return {
        success: false,
        error: error as any
      }
    }
  }
}

// Export singleton instance
export const communityService = new CommunityService()
export default communityService
