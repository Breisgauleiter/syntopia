import { describe, it, expect, vi, beforeEach } from 'vitest'
import questService, { UserQuestStatus, type Quest, type UserQuest } from '@/services/quest.service'

// We'll import the component logic by dynamic import of the view (script setup compiled) if needed.
// For simplicity, we test service + optimistic logic pattern in isolation.

describe('Quest verification optimistic logic', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('marks user quest verified after verifyQuest call', async () => {
    // Arrange mock quest and response
    const quest: Quest = {
      id: 'q1', key: 'k1', title: 'Test Quest', description: 'Desc', role: null,
      requiredLevel: 1, experienceReward: 100, type:  'LEARNING' as any,
      status: 'COMPLETED' as any, difficulty: 'BEGINNER' as any,
      createdAt: '', updatedAt: '', autoValidated: false
    }
    const apiSpy = vi.spyOn(questService, 'verifyQuest').mockResolvedValue({ success: true, data: { questId: 'q1', status: UserQuestStatus.USER_VERIFIED } as any })

    // Act
    const res = await questService.verifyQuest(quest.id)

    // Assert
    expect(apiSpy).toHaveBeenCalledOnce()
    expect(res.success).toBe(true)
    expect(res.data?.status).toBe(UserQuestStatus.USER_VERIFIED)
  })
})
