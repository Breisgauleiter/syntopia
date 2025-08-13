import { describe, it, expect, vi, beforeEach } from 'vitest'
import questService, { UserQuestStatus, QuestType, QuestDifficulty, type Quest, type UserQuest } from '@/services/quest.service'
import * as apiModule from '@/services/api'

// Mock api service
vi.mock('@/services/api', () => {
  return {
    default: {
      post: vi.fn(),
      get: vi.fn()
    }
  }
})

describe('quest.service helper methods', () => {
  const baseQuest: Quest = {
    id: 'q1',
    key: 'q1',
    title: 'Test Quest',
    description: 'Desc',
    role: 'All',
    requiredLevel: 1,
    experienceReward: 100,
    type: QuestType.LEARNING,
    status: 0 as any, // not used
    difficulty: QuestDifficulty.BEGINNER,
    autoValidated: false,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }

  it('questToUserQuest maps quest with default status USER_AVAILABLE', () => {
    const uq = questService.questToUserQuest(baseQuest)
    expect(uq.status).toBe(UserQuestStatus.USER_AVAILABLE)
    expect(uq.quest?.id).toBe('q1')
  })

  it('isUserQuestCompleted returns true for USER_VERIFIED and USER_COMPLETED', () => {
    const completed: UserQuest = { userQuestId: 'u1', userId: 'u', questId: 'q1', status: UserQuestStatus.USER_COMPLETED, progress: 100 }
    const verified: UserQuest = { ...completed, status: UserQuestStatus.USER_VERIFIED }
    expect(questService.isUserQuestCompleted(completed)).toBe(true)
    expect(questService.isUserQuestCompleted(verified)).toBe(true)
  })

  it('getUserQuestDisplayStatus maps statuses', () => {
    const statuses = [
      UserQuestStatus.USER_AVAILABLE,
      UserQuestStatus.USER_ACTIVE,
      UserQuestStatus.USER_COMPLETED,
      UserQuestStatus.USER_ABANDONED,
      UserQuestStatus.USER_VERIFIED
    ]
    const labels = statuses.map(s => questService.getUserQuestDisplayStatus({ userQuestId: 'x', userId: 'u', questId: 'q', status: s, progress: 0 }))
    expect(labels).toEqual(['Available', 'In Progress', 'Completed', 'Abandoned', 'Verified'])
  })
})

describe('quest.service verifyQuest API', () => {
  const api = (apiModule as any).default
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('verifyQuest returns success path data', async () => {
    api.post.mockResolvedValue({ success: true, data: { userQuestId: 'uq1', status: UserQuestStatus.USER_VERIFIED } })
    const res = await questService.verifyQuest('q1')
    expect(res.success).toBe(true)
    expect(res.data?.status).toBe(UserQuestStatus.USER_VERIFIED)
    expect(api.post).toHaveBeenCalledWith('/user-quests/q1/verify')
  })

  it('verifyQuest handles error', async () => {
    api.post.mockRejectedValue(new Error('fail'))
    const res = await questService.verifyQuest('bad')
    expect(res.success).toBe(false)
    expect(res.error?.message).toContain('Failed to verify quest')
  })
})
