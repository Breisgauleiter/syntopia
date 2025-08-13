import api from './api'
import type { User } from '../types/api.types'

/**
 * Profile Service for Syntopia Platform
 * 
 * Provides consistent API interface for profile management operations
 * following the same patterns as Quest and Community services.
 */
export class ProfileService {
  
  /**
   * Get current user's complete profile
   */
  async getCurrentProfile() {
    try {
      const response = await api.get('/profile')
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
   * Update user profile
   */
  async updateProfile(profileData: Partial<User>) {
    try {
      const response = await api.put('/profile', profileData)
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
   * Upload profile avatar
   */
  async uploadAvatar(file: File) {
    try {
      const formData = new FormData()
      formData.append('avatar', file)
      
      const response = await api.post('/profile/avatar', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
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
   * Get user achievements
   */
  async getAchievements() {
    try {
      const response = await api.get('/profile/achievements')
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
   * Get social connections
   */
  async getSocialConnections() {
    try {
      const response = await api.get('/profile/social')
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
   * Get public profile by user ID
   */
  async getPublicProfile(userId: string) {
    try {
      const response = await api.get(`/profile/${userId}`)
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
   * Search users with filters
   */
  async searchUsers(query?: string, role?: string, minLevel?: number, page = 0, size = 20) {
    try {
      const params = new URLSearchParams()
      if (query) params.append('query', query)
      if (role) params.append('role', role)
      if (minLevel !== undefined) params.append('minLevel', minLevel.toString())
      params.append('page', page.toString())
      params.append('size', size.toString())
      
      const response = await api.get(`/profile/search?${params}`)
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
}

// Export singleton instance
export const profileService = new ProfileService()
export default profileService
