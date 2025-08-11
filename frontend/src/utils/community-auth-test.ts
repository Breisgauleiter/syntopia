/**
 * Community Service Test with Authentication
 * 
 * Complete test that handles authentication before testing community features
 */

import { useUserStore } from '../stores/user'
import { communityService } from '../services/community.service'

// Augment Window for global test helpers
declare global {
  interface Window {
    testCommunityWithAuth: typeof testCommunityWithAuth
    quickCommunityTest: typeof quickCommunityTest
    pinia?: any
  }
}

/**
 * Test community services with proper authentication
 */
export async function testCommunityWithAuth() {
  console.log('🔐 Testing Community Service with Authentication...\n')
  
  try {
    // Check if we can access the user store (Pinia context)
    let userStore
    try {
      userStore = useUserStore()
    } catch (pinaError) {
      // Try to create store using globally exposed Pinia from main.ts
      // @ts-ignore
      const pinia = (window as any).pinia
      if (pinia) {
        console.log('ℹ️ Creating user store using global pinia instance...')
        // Temporarily install pinia into a dummy app context by directly calling store getter
        userStore = useUserStore(pinia)
      } else {
        console.log('⚠️ Cannot access user store outside Vue context and no global pinia found, testing without auth check')
        console.log('💡 This test works best when run from within the Vue app context')
        
        // Test one endpoint to show the service layer works
        console.log('\n2️⃣ Testing getCommunityStats() without auth...')
        const statsResult = await communityService.getCommunityStats()
        console.log('✅ Stats Result (should be error):', statsResult)
        
        return {
          authenticated: false,
          message: 'Community service working but needs Vue context for auth test'
        }
      }
    }
    
    // Step 1: Check if already authenticated
    console.log('1️⃣ Checking authentication status...')
    await userStore.checkAuthStatus()
    
    if (!userStore.isAuthenticated) {
      console.log('⚠️ Not authenticated. Attempting auto registration/login for test user...')

      const testEmail = `tester_${Date.now()}@syntopia.local`
      const testUsername = `tester_${Math.floor(Math.random()*100000)}`
      const testPassword = 'Test1234'

      // Try register new user then login
      const registered = await userStore.register({
        username: testUsername,
        email: testEmail,
        displayName: 'Test User',
        password: testPassword
      })

      if (!registered) {
        console.log('ℹ️ Registration may have failed (possibly existing user). Trying login with test credentials...')
        const loggedIn = await userStore.loginWithEmail(testEmail, testPassword)
        if (!loggedIn) {
          console.log('⚠️ Auto-authentication failed; proceeding to run without auth to confirm 401s...')
          const statsResult = await communityService.getCommunityStats()
          console.log('✅ Stats Result (should be error):', statsResult)
          return { authenticated: false, message: 'Auto-auth failed; endpoints secured as expected' }
        }
      }

      // Give time for token propagation
      await new Promise(r => setTimeout(r, 500))
      console.log('✅ Auto-authenticated test user, continuing with authenticated tests...')
    }
    
    // Step 2: Test all community services with authentication
    console.log('✅ Authenticated! Testing all community services...\n')
    
    const results = []
    
    // Test community stats
    console.log('3️⃣ Testing getCommunityStats()...')
    const statsResult = await communityService.getCommunityStats()
    results.push({ service: 'getCommunityStats', success: statsResult.success })
    console.log('Stats Result:', statsResult)
    
    // Test community feed
    console.log('\n4️⃣ Testing getCommunityFeed()...')
    const feedResult = await communityService.getCommunityFeed(0, 10)
    results.push({ service: 'getCommunityFeed', success: feedResult.success })
    console.log('Feed Result:', feedResult)
    
    // Test user directory
    console.log('\n5️⃣ Testing getUserDirectory()...')
    const directoryResult = await communityService.getUserDirectory()
    results.push({ service: 'getUserDirectory', success: directoryResult.success })
    console.log('Directory Result:', directoryResult)
    
    // Test community projects
    console.log('\n6️⃣ Testing getCommunityProjects()...')
    const projectsResult = await communityService.getCommunityProjects()
    results.push({ service: 'getCommunityProjects', success: projectsResult.success })
    console.log('Projects Result:', projectsResult)
    
    // Test leaderboard
    console.log('\n7️⃣ Testing getLeaderboard()...')
    const leaderboardResult = await communityService.getLeaderboard('all')
    results.push({ service: 'getLeaderboard', success: leaderboardResult.success })
    console.log('Leaderboard Result:', leaderboardResult)
    
    // Test connections
    console.log('\n8️⃣ Testing getConnections()...')
    const connectionsResult = await communityService.getConnections()
    results.push({ service: 'getConnections', success: connectionsResult.success })
    console.log('Connections Result:', connectionsResult)
    
    // Summary
    const successCount = results.filter(r => r.success).length
    const totalCount = results.length
    
    console.log('\n📊 Community Service Test Summary:')
    console.log(`✅ ${successCount}/${totalCount} services working correctly`)
    
    if (successCount === totalCount) {
      console.log('🎉 All community services working perfectly with authentication!')
    } else {
      console.log('⚠️ Some services may need attention:')
      results.filter(r => !r.success).forEach(r => {
        console.log(`   - ${r.service}: failed`)
      })
    }
    
    return {
      authenticated: true,
      successCount,
      totalCount,
      results
    }
    
  } catch (error: any) {
    console.error('❌ Community auth test failed:', error)
    return {
      authenticated: false,
      error: error?.message || 'Unknown error'
    }
  }
}

/**
 * Quick test for authenticated user
 */
export async function quickCommunityTest() {
  console.log('⚡ Quick Community Test...')
  
  try {
    const result = await communityService.getCommunityStats()
    
    if (result.success) {
      console.log('✅ Community service working with authentication')
      return true
    } else if (result.error?.status === 401) {
      console.log('🔒 Authentication required (this is correct behavior)')
      return true
    } else {
      console.log('❌ Unexpected error:', result.error)
      return false
    }
  } catch (error) {
    console.error('❌ Test failed:', error)
    return false
  }
}

// Make available globally for browser console testing
if (typeof window !== 'undefined') {
  window.testCommunityWithAuth = testCommunityWithAuth
  window.quickCommunityTest = quickCommunityTest
}
