/**
 * Extended Authentication Test Utilities
 * 
 * Tests flexible login with username or email
 */

import { authService } from '../services/auth.service'
import { apiService } from '../services/api'

export const flexibleAuthTest = {
  /**
   * Test login with username
   */
  async testUsernameLogin() {
    console.log('🧪 Testing Login with Username...')
    
    try {
      const response = await authService.loginWithUsername('testuser3', 'password123')
      console.log('✅ Username login success:', response)
      return response
    } catch (error) {
      console.error('❌ Username login failed:', error)
      throw error
    }
  },

  /**
   * Test login with email
   */
  async testEmailLogin() {
    console.log('🧪 Testing Login with Email...')
    
    try {
      const response = await authService.loginWithEmail('test3@example.com', 'password123')
      console.log('✅ Email login success:', response)
      return response
    } catch (error) {
      console.error('❌ Email login failed:', error)
      throw error
    }
  },

  /**
   * Test flexible login (auto-detect)
   */
  async testFlexibleLogin() {
    console.log('🧪 Testing Flexible Login...')
    
    try {
      // Test with email
      const emailResponse = await authService.login({
        email: 'test3@example.com',
        password: 'password123'
      })
      console.log('✅ Flexible login with email:', emailResponse)
      
      // Test with username  
      const usernameResponse = await authService.login({
        username: 'testuser3',
        password: 'password123'
      })
      console.log('✅ Flexible login with username:', usernameResponse)
      
      return { emailResponse, usernameResponse }
    } catch (error) {
      console.error('❌ Flexible login failed:', error)
      throw error
    }
  },

  /**
   * Run all flexible auth tests
   */
  async runAllTests() {
    console.log('🚀 Starting Flexible Authentication Tests...')
    
    try {
      await this.testUsernameLogin()
      await this.testEmailLogin()
      await this.testFlexibleLogin()
      
      console.log('🎉 All flexible authentication tests passed!')
    } catch (error) {
      console.error('💥 Test suite failed:', error)
      throw error
    }
  }
}

// Make available globally
declare global {
  interface Window {
    flexibleAuthTest: typeof flexibleAuthTest
  }
}

if (typeof window !== 'undefined') {
  window.flexibleAuthTest = flexibleAuthTest
}
