import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useConnectionsStore } from '@/stores/connections'

vi.mock('@/stores/user', () => ({ useUserStore: () => ({ user: { id: 'u1' } }) }))

// Mock community service
vi.mock('@/services/community.service', () => ({
  __esModule: true,
  default: {
    getConnections: vi.fn(async () => ({ success: true, data: { connections: [] } })),
    sendConnectionRequest: vi.fn(async () => ({ success: true, data: { id: 'c1', type: 'collaborator', status: 'PENDING', createdAt: new Date().toISOString(), otherUser: { id: 'u2', displayName: 'User 2' } } })),
    respondToConnectionRequest: vi.fn(async () => ({ success: true, data: { id: 'c1', type: 'collaborator', status: 'ACCEPTED', createdAt: new Date().toISOString(), updatedAt: new Date().toISOString(), otherUser: { id: 'u2', displayName: 'User 2' } } })),
    cancelConnectionRequest: vi.fn(async () => ({ success: true, data: { cancelled: true } }))
  }
}))

describe('connections store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('sends connection request optimistically then confirms', async () => {
    const store = useConnectionsStore()
    expect(store.pending.length).toBe(0)
    const ok = await store.sendRequest('u2')
    expect(ok).toBe(true)
    expect(store.pending.length).toBe(1)
    expect(store.pending[0].id).toBe('c1')
  })

  it('accepts a pending request optimistically', async () => {
    const store = useConnectionsStore()
    // Seed pending with a fake request to accept
    store.pending.push({ id: 'c1', type: 'collaborator', status: 'PENDING', createdAt: new Date().toISOString(), isOutgoing: false, user: { id: 'u2', displayName: 'User 2' } })
    const ok = await store.respond('c1', 'accept')
    expect(ok).toBe(true)
    expect(store.pending.find(p => p.id === 'c1')).toBeFalsy()
    expect(store.accepted.find(a => a.id === 'c1')?.status).toBe('ACCEPTED')
  })

  it('cancels an outgoing pending request', async () => {
    const store = useConnectionsStore()
    store.pending.push({ id: 'c2', type: 'collaborator', status: 'PENDING', createdAt: new Date().toISOString(), isOutgoing: true, user: { id: 'u3', displayName: 'User 3' } })
    const ok = await store.cancel('c2')
    expect(ok).toBe(true)
    expect(store.pending.find(p => p.id === 'c2')).toBeFalsy()
  })

  it('decline rollback on service failure', async () => {
    const svc = (await import('@/services/community.service')).default as any
    ;(svc.respondToConnectionRequest as any).mockImplementationOnce(async () => ({ success: false, error: { message: 'boom' } }))
    const store = useConnectionsStore()
    store.pending.push({ id: 'c3', type: 'collaborator', status: 'PENDING', createdAt: new Date().toISOString(), isOutgoing: false, user: { id: 'u4', displayName: 'User 4' } })
    const before = store.pending.length
    const ok = await store.respond('c3', 'decline')
    expect(ok).toBe(false)
    // Should have been restored
    expect(store.pending.length).toBe(before)
    expect(store.pending.find(p => p.id === 'c3')).toBeTruthy()
  })
})
