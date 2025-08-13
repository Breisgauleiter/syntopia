import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import OAuthCallbackView from '@/views/OAuthCallbackView.vue'
import { createTestingPinia } from '@pinia/testing'

// Mock router composables
vi.mock('vue-router', () => ({
  useRoute: () => ({ query: testQuery }),
  useRouter: () => ({ replace: vi.fn() })
}))

// Mock toast store
vi.mock('@/stores/toast', () => ({ useToastStore: () => ({ push: vi.fn() }) }))

// Track dynamic query across tests
let testQuery: Record<string, any> = {}

// Minimal axios mock
vi.mock('axios', () => ({
  default: {
    get: vi.fn(async () => ({ data: { user: { username: 'tester', id: 'u1', currentLevel: 5, experiencePoints: 0, questsCompleted: 0, isGitHubIntegrated: false } } })),
    defaults: { headers: { common: {} } }
  }
}))

// Mock user store with updateUserData
vi.mock('@/stores/user', () => ({
  useUserStore: () => ({
    token: null,
    updateUserData: vi.fn(),
    setTokens: vi.fn(),
  })
}))

describe('OAuthCallbackView', () => {
  beforeEach(() => {
    testQuery = {}
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('handles missing tokens gracefully', async () => {
    testQuery = { }
    const wrapper = mount(OAuthCallbackView, { global: { plugins: [createTestingPinia()] } })
    await new Promise(r => setTimeout(r, 10))
    expect(wrapper.html()).toContain('Missing Credentials')
  })

  it('parses tokens and marks success', async () => {
    testQuery = { token: 'abc123', refreshToken: 'ref456', githubIntegrated: 'true' }
    const wrapper = mount(OAuthCallbackView, { global: { plugins: [createTestingPinia()] } })
    await new Promise(r => setTimeout(r, 20))
    expect(localStorage.getItem('syntopia_token')).toBe('abc123')
    expect(wrapper.html()).toContain('GitHub Connected')
  })

  it('shows failure when error param present', async () => {
    testQuery = { error: encodeURIComponent('Denied') }
    const wrapper = mount(OAuthCallbackView, { global: { plugins: [createTestingPinia()] } })
    await new Promise(r => setTimeout(r, 10))
    expect(wrapper.html()).toContain('GitHub Connection Failed')
  })
})
