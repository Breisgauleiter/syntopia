import apiService from './api'
import type { ApiResponse } from './api'
import type { 
  LoginRequest, 
  RegisterRequest, 
  AuthResponse, 
  TokenRefreshRequest, 
  TokenRefreshResponse,
  User
} from '../types/api.types'
import { API_ENDPOINTS } from '../types/api.types'

/**
 * Authentication Service for Syntopia Platform
 * 
 * Handles all authentication-related API calls:
 * - User registration/login (Syntopia native auth)
 * - JWT token management
 * - User session validation
 * - GitHub integration (Level 4+ feature, separate from login)
 */
class AuthService {
  
  /**
   * Register a new user with Syntopia platform
   */
  async register(userData: RegisterRequest): Promise<ApiResponse<AuthResponse>> {
    // Validate required fields
    if (!userData.username || !userData.email || !userData.password) {
      return {
        success: false,
        error: {
          message: 'Username, email, and password are required',
          code: 'VALIDATION_ERROR'
        }
      }
    }

    // Validate email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(userData.email)) {
      return {
        success: false,
        error: {
          message: 'Please enter a valid email address',
          field: 'email',
          code: 'VALIDATION_ERROR'
        }
      }
    }

    // Validate username (alphanumeric + underscore, 3-20 chars)
    const usernameRegex = /^[a-zA-Z0-9_]{3,20}$/
    if (!usernameRegex.test(userData.username)) {
      return {
        success: false,
        error: {
          message: 'Username must be 3-20 characters and contain only letters, numbers, and underscores',
          field: 'username',
          code: 'VALIDATION_ERROR'
        }
      }
    }

    // Validate password strength (min 6 chars)
    if (userData.password.length < 6) {
      return {
        success: false,
        error: {
          message: 'Password must be at least 6 characters long',
          field: 'password',
          code: 'VALIDATION_ERROR'
        }
      }
    }

    const response = await apiService.post<AuthResponse>(
      API_ENDPOINTS.AUTH.REGISTER,
      userData
    )

    // Store tokens if registration successful
    if (response.success && response.data) {
      this.storeAuthData(response.data)
    }

    return response
  }

  /**
   * Login user with username/email and password (Syntopia native auth)
   */
  async login(credentials: LoginRequest): Promise<ApiResponse<AuthResponse>> {
    // Validate required fields
    if ((!credentials.username && !credentials.email) || !credentials.password) {
      return {
        success: false,
        error: {
          message: 'Username/email and password are required',
          code: 'VALIDATION_ERROR'
        }
      }
    }

    const response = await apiService.post<AuthResponse>(
      API_ENDPOINTS.AUTH.LOGIN,
      credentials
    )

    // Store tokens if login successful
    if (response.success && response.data) {
      this.storeAuthData(response.data)
    }

    return response
  }

  /**
   * Convenience method: Login with username
   */
  async loginWithUsername(username: string, password: string): Promise<ApiResponse<AuthResponse>> {
    return this.login({ username, password })
  }

  /**
   * Convenience method: Login with email
   */
  async loginWithEmail(email: string, password: string): Promise<ApiResponse<AuthResponse>> {
    return this.login({ email, password })
  }

  /**
   * Logout current user
   */
  async logout(): Promise<ApiResponse<{ message: string }>> {
    // Call backend logout endpoint
    const response = await apiService.post<{ message: string }>(
      API_ENDPOINTS.AUTH.LOGOUT
    )

    // Clear local storage regardless of backend response
    this.clearAuthData()

    return response
  }

  /**
   * Refresh JWT token using refresh token
   */
  async refreshToken(): Promise<ApiResponse<TokenRefreshResponse>> {
    const refreshToken = localStorage.getItem('syntopia_refresh_token')
    
    if (!refreshToken) {
      return {
        success: false,
        error: {
          message: 'No refresh token available',
          code: 'NO_REFRESH_TOKEN'
        }
      }
    }

    const request: TokenRefreshRequest = { refreshToken }
    
    const response = await apiService.post<TokenRefreshResponse>(
      API_ENDPOINTS.AUTH.REFRESH,
      request
    )

    // Update stored tokens if refresh successful
    if (response.success && response.data) {
      localStorage.setItem('syntopia_token', response.data.token)
      localStorage.setItem('syntopia_refresh_token', response.data.refreshToken)
    } else {
      // Refresh failed, clear auth data
      this.clearAuthData()
    }

    return response
  }

  /**
   * Get current authenticated user data
   */
  async getCurrentUser(): Promise<ApiResponse<User>> {
    const token = localStorage.getItem('syntopia_token')
    
    if (!token) {
      return {
        success: false,
        error: {
          message: 'No authentication token found',
          code: 'NO_TOKEN'
        }
      }
    }

    const response = await apiService.get<User>(API_ENDPOINTS.AUTH.ME)

    // If unauthorized, try to refresh token
    if (!response.success && response.error?.status === 401) {
      const refreshResponse = await this.refreshToken()
      
      if (refreshResponse.success) {
        // Retry getting user data with new token
        return await apiService.get<User>(API_ENDPOINTS.AUTH.ME)
      }
    }

    return response
  }

  /**
   * Check if user is currently authenticated
   */
  isAuthenticated(): boolean {
    const token = localStorage.getItem('syntopia_token')
    const refreshToken = localStorage.getItem('syntopia_refresh_token')
    
    return !!(token && refreshToken)
  }

  /**
   * Get stored JWT token
   */
  getToken(): string | null {
    return localStorage.getItem('syntopia_token')
  }

  /**
   * Get stored refresh token
   */
  getRefreshToken(): string | null {
    return localStorage.getItem('syntopia_refresh_token')
  }

  /**
   * Store authentication data in localStorage
   */
  private storeAuthData(authData: AuthResponse): void {
    localStorage.setItem('syntopia_token', authData.token)
    localStorage.setItem('syntopia_refresh_token', authData.refreshToken)
    
    // Store user data for quick access (optional)
    localStorage.setItem('syntopia_user', JSON.stringify(authData.user))
  }

  /**
   * Clear all authentication data from localStorage
   */
  private clearAuthData(): void {
    localStorage.removeItem('syntopia_token')
    localStorage.removeItem('syntopia_refresh_token')
    localStorage.removeItem('syntopia_user')
  }

  /**
   * Validate token format (basic JWT structure check)
   */
  private isValidTokenFormat(token: string): boolean {
    const jwtRegex = /^[A-Za-z0-9-_]+\.[A-Za-z0-9-_]+\.[A-Za-z0-9-_]+$/
    return jwtRegex.test(token)
  }

  /**
   * Check if token is expired (client-side check)
   * Note: This is a basic check, server validation is still required
   */
  isTokenExpired(token?: string): boolean {
    const tokenToCheck = token || this.getToken()
    
    if (!tokenToCheck || !this.isValidTokenFormat(tokenToCheck)) {
      return true
    }

    try {
      // Decode JWT payload (base64)
      const payload = JSON.parse(atob(tokenToCheck.split('.')[1]))
      
      // Check expiration time (exp is in seconds, Date.now() is in milliseconds)
      const currentTime = Math.floor(Date.now() / 1000)
      
      return payload.exp < currentTime
    } catch (error) {
      // If we can't decode the token, consider it expired
      return true
    }
  }

  /**
   * Auto-refresh token if it's about to expire (within 5 minutes)
   */
  async autoRefreshToken(): Promise<boolean> {
    const token = this.getToken()
    
    if (!token) return false

    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      const currentTime = Math.floor(Date.now() / 1000)
      const timeUntilExpiry = payload.exp - currentTime
      
      // Refresh if token expires within 5 minutes (300 seconds)
      if (timeUntilExpiry < 300) {
        const refreshResponse = await this.refreshToken()
        return refreshResponse.success
      }
      
      return true // Token is still valid
    } catch (error) {
      return false
    }
  }
}

// Export singleton instance
export const authService = new AuthService()
export default authService
