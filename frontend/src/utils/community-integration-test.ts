/**
 * Community Integration Test Utility
 * 
 * Quick test script to verify frontend-backend integration
 * after aligning DTOs and service methods.
 */

import { communityService } from '../services/community.service'
import type { ApiResponse } from '../services/api'
import type { ProjectDTO } from '../types/api.types'
import { useUserStore } from '../stores/user'

async function ensureAuthenticated(): Promise<{ authenticated: boolean; reason?: string }> {
  try {
    let userStore: any
    try {
      userStore = useUserStore()
    } catch {
      // Fallback to global pinia from main.ts
      // @ts-ignore
      const pinia = (window as any).pinia
      if (pinia) {
        userStore = useUserStore(pinia)
      } else {
        return { authenticated: false, reason: 'No Pinia context available' }
      }
    }

    await userStore.checkAuthStatus()
    if (userStore.isAuthenticated) return { authenticated: true }

    const testEmail = `tester_${Date.now()}@syntopia.local`
    const testUsername = `tester_${Math.floor(Math.random() * 100000)}`
    const testPassword = 'Test1234'

    const registered = await userStore.register({
      username: testUsername,
      email: testEmail,
      displayName: 'Test User',
      password: testPassword
    })

    if (!registered) {
      const loggedIn = await userStore.loginWithEmail(testEmail, testPassword)
      if (!loggedIn) return { authenticated: false, reason: 'Auto-auth failed' }
    }

    await new Promise(r => setTimeout(r, 400))
    return { authenticated: true }
  } catch (e: any) {
    return { authenticated: false, reason: e?.message || 'Unknown error' }
  }
}

/**
 * Test all community service methods with real backend
 */
export async function testCommunityIntegration() {
  console.log('🧪 Testing Community Service Integration...\n')
  
  try {
    // Ensure we are authenticated to avoid 401s
    const auth = await ensureAuthenticated()
    if (!auth.authenticated) {
      console.log('ℹ️ Proceeding without auth (may see 401s):', auth.reason)
    } else {
      console.log('✅ Authenticated session established for integration test')
    }
    
    // Test 1: Get Community Stats
    console.log('1️⃣ Testing getCommunityStats()...')
    const statsResult = await communityService.getCommunityStats()
    console.log('✅ Stats Result:', statsResult)
    
    // Test 2: Get Community Feed
    console.log('\n2️⃣ Testing getCommunityFeed()...')
    const feedResult = await communityService.getCommunityFeed(0, 10)
    console.log('✅ Feed Result:', feedResult)
    
    // Test 3: Get User Directory
    console.log('\n3️⃣ Testing getUserDirectory()...')
    const directoryResult = await communityService.getUserDirectory(undefined, undefined, undefined, 0, 10)
    console.log('✅ Directory Result:', directoryResult)
    
    // Test 4: Get Community Projects
    console.log('\n4️⃣ Testing getCommunityProjects()...')
    const projectsResult = await communityService.getCommunityProjects(false, 0, 10)
    console.log('✅ Projects Result:', projectsResult)
    
    // Test 5: Get Leaderboard
    console.log('\n5️⃣ Testing getLeaderboard()...')
    const leaderboardResult = await communityService.getLeaderboard('all')
    console.log('✅ Leaderboard Result:', leaderboardResult)
    
    // Test 6: Get Connections
    console.log('\n6️⃣ Testing getConnections()...')
    const connectionsResult = await communityService.getConnections()
    console.log('✅ Connections Result:', connectionsResult)
    
    console.log('\n🎉 All tests completed! Check results above.')
    
    // Summary
    const allSuccessful = [
      statsResult.success,
      feedResult.success, 
      directoryResult.success,
      projectsResult.success,
      leaderboardResult.success,
      connectionsResult.success
    ].every(success => success)
    
    console.log(`\n📊 Integration Test Summary: ${allSuccessful ? '✅ ALL PASSED' : '❌ SOME FAILED'}`)
    
    return {
      allSuccessful,
      results: {
        stats: statsResult,
        feed: feedResult,
        directory: directoryResult,
        projects: projectsResult,
        leaderboard: leaderboardResult,
        connections: connectionsResult
      }
    }
    
  } catch (error) {
    console.error('❌ Integration test failed:', error)
    return {
      allSuccessful: false,
      error
    }
  }
}

/**
 * Test creating a project (requires authentication)
 */
export async function testProjectCreation() {
  console.log('🧪 Testing Project Creation...\n')
  
  try {
    // Ensure auth since project creation requires it
    const auth = await ensureAuthenticated()
    if (!auth.authenticated) {
      console.log('⚠️ Could not establish auth, project creation likely to 401:', auth.reason)
    }
    
    const projectData = {
      title: 'Test Integration Project',
      description: 'Testing the new aligned DTOs and service integration',
      tags: ['test', 'integration'],
      visibility: 'public' as const
    }
    
    const result = await communityService.createProject(projectData)
    console.log('✅ Project Creation Result:', result)
    
    return result
    
  } catch (error) {
    console.error('❌ Project creation test failed:', error)
    return { success: false, error }
  }
}

/**
 * Test connection request (requires authentication and target user)
 */
export async function testConnectionRequest(targetUserId: string) {
  console.log('🧪 Testing Connection Request...\n')
  
  try {
    const result = await communityService.sendConnectionRequest(targetUserId, 'collaborator')
    console.log('✅ Connection Request Result:', result)
    
    return result
    
  } catch (error) {
    console.error('❌ Connection request test failed:', error)
    return { success: false, error }
  }
}

// Augment Window for global helpers and export for browser console
declare global {
  interface Window {
    testCommunityIntegration: typeof testCommunityIntegration
  testProjectCreation: () => Promise<ApiResponse<ProjectDTO> | { success: boolean; error: unknown }>
  testConnectionRequest: (targetUserId: string) => Promise<ApiResponse<any> | { success: boolean; error: unknown }>
  }
}

if (typeof window !== 'undefined') {
  window.testCommunityIntegration = testCommunityIntegration
  window.testProjectCreation = testProjectCreation
  window.testConnectionRequest = testConnectionRequest
}
