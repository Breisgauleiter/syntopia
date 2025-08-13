import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// Import API test utilities for development
import { testAPI } from './utils/api-test'
import { testCommunityIntegration } from './utils/community-integration-test'
import { testCommunityWithAuth, quickCommunityTest } from './utils/community-auth-test'
import { testAuthFlow } from './utils/auth-flow-test'
import authService from './services/auth.service'
import apiService from './services/api'

const app = createApp(App)

// Create Pinia instance explicitly so we can expose it for test utilities
const pinia = createPinia()
app.use(pinia)
app.use(router)

app.mount('#app')

// Make test utilities available in development
if (typeof window !== 'undefined') {
  // testAPI is already available from api-test.ts
  // @ts-ignore
  window.authService = authService
  // @ts-ignore
  window.apiService = apiService
  // @ts-ignore
  window.pinia = pinia
  // @ts-ignore
  window.testCommunityIntegration = testCommunityIntegration
  // @ts-ignore
  window.testCommunityWithAuth = testCommunityWithAuth
  // @ts-ignore
  window.quickCommunityTest = quickCommunityTest
  // @ts-ignore
  window.testAuthFlow = testAuthFlow
  
  console.log('🧪 API test utilities available:')
  console.log('  - window.testAPI.runAllTests()')
  console.log('  - window.testCommunityWithAuth()')  
  console.log('  - window.quickCommunityTest()')
  console.log('  - window.testAuthFlow() // Full login + community test')
  console.log('🔐 Direct services: authService, apiService')
}
