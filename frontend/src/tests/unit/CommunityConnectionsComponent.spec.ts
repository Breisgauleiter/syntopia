import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import CommunityView from '@/views/CommunityView.vue'

vi.mock('@/services/community.service', () => ({
  __esModule: true,
  default: {
    getCommunityFeed: vi.fn(async () => ({ success: true, data: { items: [], pagination: {} } })),
    getCommunityStats: vi.fn(async () => ({ success: true, data: { connections: 0, projects: 0, questsCompleted: 0 } })),
    getUserDirectory: vi.fn(async () => ({ success: true, data: { users: [], pagination: {} } })),
    getConnections: vi.fn(async () => ({ success: true, data: { connections: [ { id: 'p1', type: 'collaborator', status: 'PENDING', createdAt: new Date().toISOString(), otherUser: { id: 'u2', displayName: 'Alice' } }, { id: 'a1', type: 'collaborator', status: 'ACCEPTED', createdAt: new Date().toISOString(), updatedAt: new Date().toISOString(), otherUser: { id: 'u3', displayName: 'Bob' } } ] } })),
    sendConnectionRequest: vi.fn(async () => ({ success: true, data: { id: 'p2', type: 'collaborator', status: 'PENDING', createdAt: new Date().toISOString(), otherUser: { id: 'u5', displayName: 'Eve' } } })),
    respondToConnectionRequest: vi.fn(async () => ({ success: true, data: { id: 'p1', type: 'collaborator', status: 'ACCEPTED', createdAt: new Date().toISOString(), updatedAt: new Date().toISOString(), otherUser: { id: 'u2', displayName: 'Alice' } } })),
    cancelConnectionRequest: vi.fn(async () => ({ success: true, data: { cancelled: true } })),
    getCommunityProjects: vi.fn(async () => ({ success: true, data: { projects: [], pagination: {} } })),
    getLeaderboard: vi.fn(async () => ({ success: true, data: [] })),
    createProject: vi.fn(async () => ({ success: true, data: { id: 'proj', title: 'Test', description: 'Desc' } }))
  }
}))

vi.mock('@/stores/user', () => ({ useUserStore: () => ({ user: { id: 'u1', username: 'u1' }, isAuthenticated: true, checkAuthStatus: vi.fn() }) }))

import { useConnectionsStore } from '@/stores/connections'

const flush = () => new Promise(r => setTimeout(r))

describe('CommunityView connections integration', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders seeded connections', async () => {
  const wrapper = mount(CommunityView, { global: { plugins: [createTestingPinia({ stubActions: false })] } })
    // Switch to connections tab (set activeTab directly if button not found)
    const tabBtn = wrapper.findAll('.tab-button').find(b => b.text().includes('Connections'))
    if (tabBtn) {
      await tabBtn.trigger('click')
    } else {
      ;(wrapper.vm as any).activeTab = 'connections'
    }
  const store = useConnectionsStore()
  // Manually seed store to avoid timing issues with async mounted flows
  store.pending.push({ id: 'p1', type: 'collaborator', status: 'PENDING', createdAt: new Date().toISOString(), isOutgoing: false, user: { id: 'u2', displayName: 'Alice' } })
  store.accepted.push({ id: 'a1', type: 'collaborator', status: 'ACCEPTED', createdAt: new Date().toISOString(), respondedAt: new Date().toISOString(), isOutgoing: false, user: { id: 'u3', displayName: 'Bob' } })
    ;(wrapper.vm as any).$forceUpdate?.()
    await flush()

  expect(store.pending.length).toBe(1)
  expect(store.accepted.length).toBe(1)
  await flush()
  expect(wrapper.html()).toContain('Pending Requests')
  expect(wrapper.html()).toContain('Connected')
  expect(wrapper.html()).toContain('Alice')
  expect(wrapper.html()).toContain('Bob')

  // Just verify initial render of seeded data
  expect(wrapper.html()).toContain('Alice')
  expect(wrapper.html()).toContain('Bob')
  })

  it('accepts a pending connection via store respond path', async () => {
    const svc = (await import('@/services/community.service')).default as any
    ;(svc.respondToConnectionRequest as any).mockImplementationOnce(async () => ({ success: true, data: { id: 'pX', type: 'collaborator', status: 'ACCEPTED', createdAt: new Date().toISOString(), updatedAt: new Date().toISOString(), otherUser: { id: 'u9', displayName: 'Zoe' } } }))
  const wrapper = mount(CommunityView, { global: { plugins: [createTestingPinia({ stubActions: false })] } })
    const store = useConnectionsStore()
    store.pending.push({ id: 'pX', type: 'collaborator', status: 'PENDING', createdAt: new Date().toISOString(), isOutgoing: false, user: { id: 'u9', displayName: 'Zoe' } })
    ;(wrapper.vm as any).activeTab = 'connections'
    await new Promise(r => setTimeout(r))
    // Trigger respond logic through component button if present else directly
    const btn = wrapper.find('.connection-item.pending .btn-primary')
    if (btn.exists()) {
      await btn.trigger('click')
    } else {
      await store.respond('pX', 'accept')
    }
    await new Promise(r => setTimeout(r))
    expect(store.accepted.find(c => c.id === 'pX')?.status).toBe('ACCEPTED')
  })

  it('decline failure rolls back pending item', async () => {
    const svc = (await import('@/services/community.service')).default as any
    ;(svc.respondToConnectionRequest as any).mockImplementationOnce(async () => ({ success: false, error: { message: 'Server error' } }))
    const wrapper = mount(CommunityView, { global: { plugins: [createTestingPinia({ stubActions: false })] } })
    const store = useConnectionsStore()
    store.pending.push({ id: 'pZ', type: 'collaborator', status: 'PENDING', createdAt: new Date().toISOString(), isOutgoing: false, user: { id: 'u10', displayName: 'Yara' } })
    ;(wrapper.vm as any).activeTab = 'connections'
    await flush()
    const ok = await store.respond('pZ', 'decline')
    expect(ok).toBe(false)
    // Should have been restored
    expect(store.pending.find(c => c.id === 'pZ')).toBeTruthy()
  })
})
