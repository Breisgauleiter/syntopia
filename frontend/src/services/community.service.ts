import api from './api'
import type { 
  ApiResponse, 
  ConnectionRequestDTO, 
  ConnectionResponseDTO, 
  ConnectionDTO,
  CreateProjectDTO, 
  ProjectDTO,
  LeaderboardEntryDTO,
  FeedItemDTO,
  CommunityStatsDTO,
  CommunityFeedResponse,
  UserDirectoryResponse
} from '../types/api.types'

/**
 * Community Service for Syntopia Platform
 * 
 * Provides consistent API interface for community management operations
 * aligned with backend DTOs and contracts.
 */
export class CommunityService {
  
  /**
   * Get community activity feed
   */
  async getCommunityFeed(page = 0, size = 20): Promise<ApiResponse<CommunityFeedResponse>> {
    const res = await api.get(`/community/feed?page=${page}&size=${size}`)
    if (!res.success) return res as ApiResponse<any>
    return {
      success: true,
      data: (res.data as any)?.data || (res.data as any) || { items: [], pagination: {} }
    }
  }
  
  /**
   * Get user directory for discovering connections
   */
  async getUserDirectory(
    search?: string, 
    role?: string, 
    minLevel?: number, 
    page = 0, 
    size = 20
  ): Promise<ApiResponse<UserDirectoryResponse>> {
    const params = new URLSearchParams()
    if (search) params.append('search', search)
    if (role) params.append('role', role)
    if (minLevel !== undefined) params.append('minLevel', minLevel.toString())
    params.append('page', page.toString())
    params.append('size', size.toString())

    const res = await api.get(`/community/users?${params}`)
    if (!res.success) return res as ApiResponse<any>
    return {
      success: true,
      data: (res.data as any)?.data || (res.data as any) || { users: [], pagination: {} }
    }
  }
  
  /**
   * Send connection request (aligned with backend DTO)
   */
  async sendConnectionRequest(
    toUserId: string, 
    connectionType: 'friend' | 'mentor' | 'collaborator' = 'collaborator'
  ): Promise<ApiResponse<ConnectionDTO>> {
  const requestData: ConnectionRequestDTO = { toUserId, connectionType }
  const res = await api.post('/community/connect', requestData)
  if (!res.success) return res as ApiResponse<any>
  return { success: true, data: (res.data as any)?.data || (res.data as any) || {} }
  }
  
  /**
   * Respond to connection request (aligned with backend DTO)
   */
  async respondToConnectionRequest(
    connectionId: string, 
    action: 'accept' | 'decline'
  ): Promise<ApiResponse<ConnectionDTO>> {
  const responseData: ConnectionResponseDTO = { action }
  const res = await api.put(`/community/connect/${connectionId}`, responseData)
  if (!res.success) return res as ApiResponse<any>
  return { success: true, data: (res.data as any)?.data || (res.data as any) || {} }
  }
  
  /**
   * Get user's connections with filtering
   */
  async getConnections(
    status?: 'PENDING' | 'ACCEPTED' | 'DECLINED',
    direction: 'in' | 'out' | 'all' = 'all',
    page = 0,
    size = 20
  ): Promise<ApiResponse<{ connections: ConnectionDTO[], pagination: any }>> {
    const params = new URLSearchParams()
    if (status) params.append('status', status)
    params.append('direction', direction)
    params.append('page', page.toString())
    params.append('size', size.toString())

    const res = await api.get(`/community/connections?${params}`)
    if (!res.success) return res as ApiResponse<any>
    return {
      success: true,
      data: (res.data as any)?.data || (res.data as any) || { connections: [], pagination: {} }
    }
  }
  
  /**
   * Get community projects with filtering
   */
  async getCommunityProjects(
    mine = false, 
    page = 0, 
    size = 20
  ): Promise<ApiResponse<{ projects: ProjectDTO[], pagination: any }>> {
    const params = new URLSearchParams()
    params.append('mine', mine.toString())
    params.append('page', page.toString())
    params.append('size', size.toString())

    const res = await api.get(`/community/projects?${params}`)
    if (!res.success) return res as ApiResponse<any>
    const payload: any = (res.data as any)?.data || (res.data as any) || {}
    const projects = payload.projects || payload.items || []
    const pagination = payload.pagination || {
      totalCount: payload.totalCount ?? projects.length,
      hasMore: payload.hasMore ?? false,
      page,
      size
    }
    return { success: true, data: { projects, pagination } }
  }
  
  /**
   * Create new community project (aligned with backend DTO)
   */
  async createProject(projectData: CreateProjectDTO): Promise<ApiResponse<ProjectDTO>> {
  const res = await api.post('/community/projects', projectData)
  if (!res.success) return res as ApiResponse<any>
  return { success: true, data: (res.data as any)?.data || (res.data as any) || {} }
  }
  
  /**
   * Get community statistics
   */
  async getCommunityStats(): Promise<ApiResponse<CommunityStatsDTO>> {
  const res = await api.get('/community/stats')
  if (!res.success) return res as ApiResponse<any>
  return { success: true, data: (res.data as any)?.data || (res.data as any) || {} }
  }
  
  /**
   * Get leaderboard
   * Backend expects query param 'window' with values: 'week' | 'month' | 'all'
   */
  async getLeaderboard(window?: 'week' | 'month' | 'all'): Promise<ApiResponse<LeaderboardEntryDTO[]>> {
  const params = window ? { params: { window } } : {}
  const res = await api.get(`/community/leaderboard`, params)
  if (!res.success) return res as ApiResponse<any>
  const payload: any = (res.data as any)?.data || (res.data as any) || {}
  const entries = payload.entries || payload.items || payload || []
  return { success: true, data: entries }
  }
}

// Export singleton instance
export const communityService = new CommunityService()
export default communityService
