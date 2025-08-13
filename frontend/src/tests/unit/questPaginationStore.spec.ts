import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useQuestStore } from '@/stores/quests'
import questService, { UserQuestStatus } from '@/services/quest.service'

// Lightweight mock for user store presence (Pinia dependency)
vi.mock('@/stores/user', () => ({ useUserStore: () => ({ user: { id: 'u1' } }) }))

describe('Quest pagination store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.restoreAllMocks()
  })

  it('navigates available quest pages updating items & page index', async () => {
    const store = useQuestStore()
  vi.spyOn(questService, 'getUserQuests').mockImplementation(async (page?: number, size?: number) => ({
      success: true,
      data: {
        data: Array.from({ length: 2 }, (_, i) => ({
          userQuestId: `uq-${page}-${i}`,
          userId: 'u1',
          questId: `q-${page}-${i}`,
          status: UserQuestStatus.USER_AVAILABLE,
          progress: 0,
          quest: {
            id: `q-${page}-${i}`,
            key: `k-${page}-${i}`,
            title: `Quest ${page}-${i}`,
            description: 'Test',
            role: null,
            requiredLevel: 1,
            experienceReward: 10,
            type: 'LEARNING' as any,
            status: 'AVAILABLE' as any,
            difficulty: 'BEGINNER' as any,
            createdAt: '',
            updatedAt: '',
            autoValidated: false
          }
        })),
        pagination: { page, size: 50, total: 4 }
      }
    }) as any)
    vi.spyOn(questService, 'getUserCompletedQuests').mockResolvedValue({ success: true, data: { data: [], pagination: { page: 0, size: 50, total: 0 } } } as any)
    vi.spyOn(questService, 'getQuests').mockResolvedValue({ success: true, data: { quests: [], count: 0 } } as any)
    vi.spyOn(questService, 'getUserQuestStatistics').mockResolvedValue({ success: true, data: { totalCompleted: 0, activeQuests: 0, completedThisWeek: 0, recentCompletions: [] } } as any)

    await store.refreshAll()
    expect(store.availablePagination.page).toBe(0)
    expect(store.availableQuests.length).toBe(2)

    await store.loadAvailable(1)
    expect(store.availablePagination.page).toBe(1)
    expect(store.availableQuests[0].quest?.title).toContain('1-0')
  })

  it('handles completed quests pagination and verification flag', async () => {
    const store = useQuestStore()
    vi.spyOn(questService, 'getUserQuests').mockResolvedValue({ success: true, data: { data: [], pagination: { page: 0, size: 50, total: 0 } } } as any)
  vi.spyOn(questService, 'getUserCompletedQuests').mockImplementation(async (page?: number, size?: number) => ({
      success: true,
      data: {
        data: Array.from({ length: 1 }, (_, i) => ({
          userQuestId: `c-${page}-${i}`,
            userId: 'u1',
            questId: `cq-${page}-${i}`,
            status: UserQuestStatus.USER_COMPLETED,
            progress: 100,
            verified: page === 1,
            quest: {
              id: `cq-${page}-${i}`,
              key: `ck-${page}-${i}`,
              title: `Completed ${page}-${i}`,
              description: 'Done',
              role: null,
              requiredLevel: 1,
              experienceReward: 20,
              type: 'LEARNING' as any,
              status: 'COMPLETED' as any,
              difficulty: 'BEGINNER' as any,
              createdAt: '',
              updatedAt: '',
              autoValidated: false
            }
        })),
        pagination: { page, size: 50, total: 2 }
      }
    }) as any)
    vi.spyOn(questService, 'getQuests').mockResolvedValue({ success: true, data: { quests: [], count: 0 } } as any)
    vi.spyOn(questService, 'getUserQuestStatistics').mockResolvedValue({ success: true, data: { totalCompleted: 0, activeQuests: 0, completedThisWeek: 0, recentCompletions: [] } } as any)

    await store.refreshAll()
    expect(store.completedPagination.page).toBe(0)
    expect(store.completedUserQuests.length).toBe(1)

    await store.loadCompleted(1)
    expect(store.completedPagination.page).toBe(1)
    expect(store.completedUserQuests[0].verified).toBe(true)
  })
})
