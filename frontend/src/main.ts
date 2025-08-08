import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// Import API test utilities for development
import { testAPI } from './utils/api-test'
import authService from './services/auth.service'
import apiService from './services/api'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')

// Make test utilities available in development
if (typeof window !== 'undefined') {
  // testAPI is already available from api-test.ts
  // Just add the services
  // @ts-ignore
  window.authService = authService
  // @ts-ignore
  window.apiService = apiService
  
  console.log('🧪 API test utilities available via window.testAPI')
  console.log('📝 Try: testAPI.runAllTests()')
  console.log('🔐 Direct services: authService, apiService')
}
