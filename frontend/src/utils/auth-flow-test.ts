/**
 * Authentication Flow Test
 * Tests the complete login -> community API flow to debug token issues
 */

import { useUserStore } from '@/stores/user'
import { communityService } from '@/services/community.service'

export const testAuthFlow = async () => {
  console.log('🔐 Testing Authentication Flow...')
  
  try {
    // Step 1: Get user store
    const userStore = useUserStore()
    
    // Step 2: Clear any existing auth
    console.log('1️⃣ Clearing existing auth...')
    userStore.logout()
    
    // Wait a moment for logout to complete
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // Step 3: Login with correct credentials
    console.log('2️⃣ Logging in with nerdy@nerdy.de...')
    const loginSuccess = await userStore.loginWithEmail('nerdy@nerdy.de', 'Nerdy123')
    
    if (!loginSuccess) {
      console.error('❌ Login failed!')
      return false
    }
    
    console.log('✅ Login successful!')
    
    // Step 4: Check auth state
    console.log('3️⃣ Checking auth state...')
    console.log('Auth state:', {
      isAuthenticated: userStore.isAuthenticated,
      hasUser: !!userStore.user,
      hasToken: !!userStore.token,
      storedToken: !!localStorage.getItem('syntopia_token')
    })
    
    // Step 5: Wait a moment for token to be set
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // Step 6: Test community API
    console.log('4️⃣ Testing community API...')
    const statsResult = await communityService.getCommunityStats()
    console.log('Stats result:', statsResult)
    
    // Step 7: Test another community endpoint
    console.log('5️⃣ Testing community feed...')
    const feedResult = await communityService.getCommunityFeed()
    console.log('Feed result:', feedResult)
    
    console.log('🎉 Auth flow test completed!')
    return true
    
  } catch (error) {
    console.error('❌ Auth flow test failed:', error)
    return false
  }
}

// Make it available globally for testing
declare global {
  interface Window {
    testAuthFlow: () => Promise<boolean>
  }
}

if (typeof window !== 'undefined') {
  window.testAuthFlow = testAuthFlow
}
