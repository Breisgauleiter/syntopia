import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'
import { unwrap } from '@/utils/api-unwrapper'

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

export interface AuthResponse {
  user: User
  token: string
  refreshToken: string
}

export const useUserStore = defineStore('user', () => {
  // State
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('syntopia_token'))
  const isLoading = ref(false)
  const error = ref<string | null>(null)
  const authInitialized = ref(false) // Track if initial auth check is complete
  const authCheckInProgress = ref(false) // Prevent concurrent auth checks

  // Getters
  const isAuthenticated = computed(() => {
    const hasUser = !!user.value
    const hasToken = !!token.value
    const hasStoredToken = !!localStorage.getItem('syntopia_token')
    const authenticated = hasUser && (hasToken || hasStoredToken)
    
    console.log('isAuthenticated check:', {
      authenticated,
      hasUser,
      hasToken,
      hasStoredToken,
      userValue: user.value?.username || 'none'
    })
    
    return authenticated
  })
  const userLevel = computed(() => user.value?.currentLevel || 1)
  const userRole = computed(() => user.value?.selectedRole || 'None')
  const canAccessGitHub = computed(() => (user.value?.currentLevel || 0) >= 4)

  // Actions
  const setAuthData = (authData: AuthResponse) => {
    user.value = authData.user
    token.value = authData.token
    localStorage.setItem('syntopia_token', authData.token)
    localStorage.setItem('syntopia_refresh_token', authData.refreshToken)
    
    // Set axios default header
    axios.defaults.headers.common['Authorization'] = `Bearer ${authData.token}`
  }

  /**
   * Lightweight token setter used by OAuth callback when backend redirects with tokens
   * and we subsequently fetch /api/auth/me for user data. Avoids needing full AuthResponse shape.
   */
  const setTokens = (accessToken: string, refresh: string) => {
    token.value = accessToken
    localStorage.setItem('syntopia_token', accessToken)
    if (refresh) localStorage.setItem('syntopia_refresh_token', refresh)
    axios.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`
  }

  const clearAuthData = () => {
    user.value = null
    token.value = null
    authInitialized.value = false
    localStorage.removeItem('syntopia_token')
    localStorage.removeItem('syntopia_refresh_token')
    delete axios.defaults.headers.common['Authorization']
  }

  const login = async (credentials: { username?: string; email?: string; password: string }) => {
    isLoading.value = true
    error.value = null
    
    try {
      console.log('Login attempt with:', credentials.email || credentials.username)
      const response = await axios.post('/api/auth/login', credentials)
      const payload: any = unwrap(response.data)
      if (!payload?.user || !payload?.token) {
        throw new Error('Malformed login response')
      }
      console.log('Login successful:', payload.user.username)
      setAuthData(payload)
      authInitialized.value = true
      return true
    } catch (err: any) {
      console.error('Login failed:', err.response?.data?.message || err.message)
      error.value = err.response?.data?.message || 'Login failed'
      return false
    } finally {
      isLoading.value = false
    }
  }

  // Convenience method for username login
  const loginWithUsername = async (username: string, password: string) => {
    return login({ username, password })
  }

  // Convenience method for email login  
  const loginWithEmail = async (email: string, password: string) => {
    return login({ email, password })
  }

  const loginWithGitHub = async (code: string) => {
    isLoading.value = true
    error.value = null
    
    try {
  const response = await axios.post('/api/auth/github', { code })
  const payload: any = unwrap(response.data)
  if (!payload?.user || !payload?.token) throw new Error('Malformed GitHub login response')
  setAuthData(payload)
      return true
    } catch (err: any) {
      error.value = err.response?.data?.message || 'GitHub login failed'
      return false
    } finally {
      isLoading.value = false
    }
  }

  const register = async (userData: {
    username: string
    email: string
    displayName: string
    password: string
  }) => {
    isLoading.value = true
    error.value = null
    
    try {
      // Send registration with password
      await axios.post('/api/auth/register', userData)
      // Immediately login to obtain tokens (registration response only returns user)
      const loginPayload: { username?: string; email?: string; password: string } = { password: userData.password }
      if (userData.username) loginPayload.username = userData.username
      if (userData.email) loginPayload.email = userData.email
      const loginRes = await axios.post('/api/auth/login', loginPayload)
      const loginPayloadData: any = unwrap(loginRes.data)
      if (!loginPayloadData?.user || !loginPayloadData?.token) {
        throw new Error('Malformed login response after registration')
      }
      setAuthData(loginPayloadData)
      return true
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Registration failed'
      return false
    } finally {
      isLoading.value = false
    }
  }

  const logout = () => {
    clearAuthData()
    // Redirect to home page
    window.location.href = '/'
  }

  const checkAuthStatus = async () => {
    // Prevent concurrent auth checks
    if (authCheckInProgress.value) {
      console.log('🔄 Auth check already in progress, waiting...')
      // Wait for the ongoing check to complete
      while (authCheckInProgress.value) {
        await new Promise(resolve => setTimeout(resolve, 100))
      }
      return
    }
    
    authCheckInProgress.value = true
    
    const storedToken = localStorage.getItem('syntopia_token')
    console.log('🔍 Checking auth status, stored token exists:', !!storedToken)
    
    if (!storedToken) {
      console.log('❌ No stored token, clearing auth data')
      clearAuthData()
      authInitialized.value = true
      authCheckInProgress.value = false
      return
    }
    
    // Set token if not already set
    if (!token.value) {
      token.value = storedToken
      console.log('📝 Set token from localStorage')
    }
    
    try {
      // Make sure axios has the auth header set
      axios.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`
      console.log('🚀 Making /api/auth/me request with token:', storedToken.substring(0, 20) + '...')
      
      const response = await axios.get('/api/auth/me')
      const payload: any = unwrap(response.data)
      // Backend /me returns {user: {...}}
      const userData = payload?.user || payload
      if (!userData?.username) {
        throw new Error('Malformed /me response')
      }
      user.value = userData
      token.value = storedToken  // Make sure token is set
      
      console.log('✅ Auth check successful, user:', userData.username, 'token set:', !!token.value)
      
    } catch (err: any) {
      console.warn('⚠️ Auth check failed - Status:', err.response?.status, 'Message:', err.response?.data?.message || err.message)
      
      // Only clear auth data if it's a 401 (invalid token) or 403 (forbidden)
      // Don't clear on network errors or other temporary issues
      if (err.response?.status === 401 || err.response?.status === 403) {
        console.log('🗑️ Clearing invalid token due to 401/403')
        clearAuthData()
      } else {
        console.log('🤔 Keeping token, might be temporary network issue')
        // Keep the user logged in for non-auth errors
        authInitialized.value = true
      }
    } finally {
      authInitialized.value = true
      authCheckInProgress.value = false
    }
  }

  const updateProfile = async (profileData: Partial<User>) => {
    if (!user.value) return false
    
    isLoading.value = true
    error.value = null
    
    try {
  const response = await axios.put(`/api/users/${user.value.id}`, profileData)
  const payload: any = unwrap(response.data)
  const updatedUser = payload?.user || payload
  if (!updatedUser) throw new Error('Malformed profile update response')
  user.value = { ...user.value, ...updatedUser }
      return true
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Profile update failed'
      return false
    } finally {
      isLoading.value = false
    }
  }

  const selectRole = async (role: string) => {
    if (!user.value) return false
    
    try {
  const response = await axios.post(`/api/users/${user.value.id}/role`, { role })
  const payload: any = unwrap(response.data)
  const updatedUser = payload?.user || user.value
  user.value = { ...user.value, ...updatedUser, selectedRole: role }
      return true
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Role selection failed'
      return false
    }
  }

  /**
   * Update user data directly (used for quest completion, level ups, etc.)
   */
  const updateUserData = (newUserData: Partial<User>) => {
    if (user.value) {
      user.value = { ...user.value, ...newUserData }
    }
  }

  /**
   * Add experience points to user
   */
  const addExperiencePoints = (points: number) => {
    if (user.value) {
      user.value.experiencePoints += points
      
      // Check for level up (simple calculation: every 1000 XP = 1 level)
      const newLevel = Math.floor(user.value.experiencePoints / 1000) + 1
      if (newLevel > user.value.currentLevel) {
        user.value.currentLevel = newLevel
      }
    }
  }

  return {
    // State
    user,
    token,
    isLoading,
    error,
    authInitialized,
    authCheckInProgress,
    
    // Getters
    isAuthenticated,
    userLevel,
    userRole,
    canAccessGitHub,
    
    // Actions
    login,
    loginWithUsername,
    loginWithEmail,
    loginWithGitHub,
    register,
    logout,
    checkAuthStatus,
    updateProfile,
    selectRole,
    updateUserData,
    addExperiencePoints
  ,setTokens
  }
})
