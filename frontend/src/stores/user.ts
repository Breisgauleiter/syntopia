import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

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

  // Getters
  const isAuthenticated = computed(() => {
    const authenticated = !!user.value && !!token.value
    console.log('isAuthenticated check:', authenticated, 'user:', !!user.value, 'token:', !!token.value)
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

  const clearAuthData = () => {
    user.value = null
    token.value = null
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
      console.log('Login successful:', response.data.user.username)
      setAuthData(response.data)
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
      setAuthData(response.data)
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
  }) => {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.post('/api/auth/register', userData)
      setAuthData(response.data)
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
    const storedToken = localStorage.getItem('syntopia_token')
    console.log('Checking auth status, stored token exists:', !!storedToken)
    
    if (!storedToken) {
      console.log('No stored token, clearing auth data')
      clearAuthData()
      return
    }
    
    // Set token if not already set
    if (!token.value) {
      token.value = storedToken
      console.log('Set token from localStorage')
    }
    
    try {
      axios.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`
      console.log('Making /api/auth/me request...')
      const response = await axios.get('/api/auth/me')
      user.value = response.data
      console.log('Auth check successful, user:', response.data.username)
      
      // Ensure token is set correctly
      if (!token.value) {
        token.value = storedToken
      }
    } catch (err) {
      console.warn('Auth check failed, clearing invalid token:', err)
      clearAuthData()
    }
  }

  const updateProfile = async (profileData: Partial<User>) => {
    if (!user.value) return false
    
    isLoading.value = true
    error.value = null
    
    try {
      const response = await axios.put(`/api/users/${user.value.id}`, profileData)
      user.value = { ...user.value, ...response.data }
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
      user.value = { ...user.value, selectedRole: role }
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
  }
})
