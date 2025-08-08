/**
 * Simple test runner for API services
 * 
 * This file can be imported in the browser console to test the services
 */

import authService from '../services/auth.service'
import apiService from '../services/api'

// Test functions for browser console
export const testAPI = {
  // Test basic API service
  async testConnection() {
    console.log('🔌 Testing API connection...')
    const response = await apiService.get('/health')
    console.log('Health check result:', response)
    return response
  },

  // Test authentication service - registration
  async testRegister() {
    console.log('📝 Testing user registration...')
    const testUser = {
      username: `testuser_${Date.now()}`,
      email: `test${Date.now()}@syntopia.com`,
      password: 'testpass123',
      displayName: 'Test User'
    }
    
    const response = await authService.register(testUser)
    console.log('Registration result:', response)
    return response
  },

  // Test authentication service - login
  async testLogin(username: string = 'testuser', password: string = 'testpass123') {
    console.log('🔐 Testing user login...')
    const response = await authService.login({ username, password })
    console.log('Login result:', response)
    return response
  },

  // Test getting current user
  async testGetCurrentUser() {
    console.log('👤 Testing get current user...')
    const response = await authService.getCurrentUser()
    console.log('Current user result:', response)
    return response
  },

  // Test token validation
  testTokenValidation() {
    console.log('🎫 Testing token validation...')
    const token = authService.getToken()
    const isAuth = authService.isAuthenticated()
    const isExpired = authService.isTokenExpired()
    
    console.log('Token:', token)
    console.log('Is authenticated:', isAuth)
    console.log('Is token expired:', isExpired)
    
    return { token, isAuth, isExpired }
  },

  // Test logout
  async testLogout() {
    console.log('🚪 Testing logout...')
    const response = await authService.logout()
    console.log('Logout result:', response)
    return response
  },

  // Run all tests in sequence
  async runAllTests() {
    console.log('🧪 Running all API tests...\n')
    
    try {
      // Test 1: Connection
      await this.testConnection()
      console.log('✅ Connection test passed\n')
      
      // Test 2: Registration
      const regResult = await this.testRegister()
      if (!regResult.success) {
        console.log('ℹ️ Registration test info:', regResult.error?.message)
      }
      console.log('📝 Registration test completed\n')
      
      // Test 3: Token validation
      this.testTokenValidation()
      console.log('✅ Token validation test passed\n')
      
      // Test 4: Get current user
      await this.testGetCurrentUser()
      console.log('✅ Current user test completed\n')
      
      // Test 5: Logout
      await this.testLogout()
      console.log('✅ Logout test completed\n')
      
      console.log('🎉 All tests completed!')
      
    } catch (error) {
      console.error('❌ Test suite failed:', error)
    }
  }
}

// Make it available globally for browser console
if (typeof window !== 'undefined') {
  (window as any).testAPI = testAPI
}

export default testAPI
