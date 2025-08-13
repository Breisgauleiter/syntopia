import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import QuestsView from '@/views/QuestsView.vue'
import { useUserStore } from '@/stores/user'
import { useQuestStore } from '@/stores/quests'
import questService, { QuestStatus, UserQuestStatus } from '@/services/quest.service'

// Integration-ish test: verify button click updates badge class from pending-verify to verified

describe('QuestsView verification badge UI', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('updates completion badge class after verify', async () => {
    // Mock service verifyQuest
    const verifySpy = vi.spyOn(questService, 'verifyQuest').mockResolvedValue({ success: true, data: { questId: 'q1', status: UserQuestStatus.USER_VERIFIED } as any })
    vi.spyOn(questService, 'getUserQuests').mockResolvedValue({ success: true, data: { data: [ { userQuestId: 'uq1', userId: 'u1', questId: 'q1', status: UserQuestStatus.USER_COMPLETED, progress: 100, verified: false, quest: { id: 'q1', key: 'k1', title: 'Quest 1', description: 'Desc', role: null, requiredLevel: 1, experienceReward: 50, type: 'LEARNING' as any, status: QuestStatus.COMPLETED, difficulty: 'BEGINNER' as any, createdAt: '', updatedAt: '', autoValidated: false } } ], pagination: { page:0,size:50,total:1 } } as any })
    vi.spyOn(questService, 'getUserCompletedQuests').mockResolvedValue({ success: true, data: { data: [ { userQuestId: 'uq1', userId: 'u1', questId: 'q1', status: UserQuestStatus.USER_COMPLETED, progress: 100, verified: false, quest: { id: 'q1', key: 'k1', title: 'Quest 1', description: 'Desc', role: null, requiredLevel: 1, experienceReward: 50, type: 'LEARNING' as any, status: QuestStatus.COMPLETED, difficulty: 'BEGINNER' as any, createdAt: '', updatedAt: '', autoValidated: false } } ], pagination: { page:0,size:50,total:1 } } as any })
    vi.spyOn(questService, 'getQuests').mockResolvedValue({ success: true, data: { quests: [] } as any })
    vi.spyOn(questService, 'getUserQuestStatistics').mockResolvedValue({ success: true, data: { totalCompleted:1, activeQuests:0, completedThisWeek:1, recentCompletions: [] } as any })

  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
  const wrapper = mount(QuestsView, {
      global: {
    plugins: [pinia],
        mocks: {
          // minimal user store expectations via pinia
        }
      }
    })

    // Wait a tick for mounted hooks
  await new Promise(r => setTimeout(r, 0))
  // Populate user so quest store loaders run
  const userStore = useUserStore()
  userStore.user = { id: 'u1', username: 'tester', email: 't@e', currentLevel:1, experiencePoints:0, questsCompleted:0, isGitHubIntegrated:false, createdAt:'', selectedRole: 'None' } as any
  const questStore = useQuestStore()
  await questStore.loadAvailable(0)
  await questStore.loadCompleted(0)
  await wrapper.vm.$nextTick()
  await new Promise(r => setTimeout(r, 0))
    // Switch via UI button to ensure template re-renders expected section
    const completedTabBtn = wrapper.findAll('button.tab-button').find(b => b.text().includes('Completed'))
    if (completedTabBtn) {
      await completedTabBtn.trigger('click')
    } else {
      ;(wrapper.vm as any).activeTab = 'completed'
    }
    await wrapper.vm.$nextTick()
    await new Promise(r => setTimeout(r, 10))

  // Move to completed tab early (quest status COMPLETED only visible there in filtered list logic)
  const completedTabBtnEarly = wrapper.findAll('button.tab-button').find(b => b.text().includes('Completed'))
  if (completedTabBtnEarly) await completedTabBtnEarly.trigger('click')
  await wrapper.vm.$nextTick()
  await new Promise(r => setTimeout(r, 10))
  const verifyBtnInitial = wrapper.findAll('button').find(b => b.text().trim() === 'Verify')
  expect(verifyBtnInitial).toBeDefined()

    // Click Verify button
  const verifyBtn = wrapper.findAll('button').find(b => b.text().trim() === 'Verify')
  expect(verifyBtn).toBeDefined()
  await verifyBtn!.trigger('click')

    // Simulate quest becoming verified via store update (service already mocked)
    // Wait microtasks
    await new Promise(r => setTimeout(r, 0))
    // Force re-render
    await wrapper.vm.$forceUpdate()

    // After verification attempt, modify spy return for next retrieval if component reloads (optional)
    // We directly mutate DOM expectations based on class change logic (optimistic inside component)

    // Badge should now have verified class
  // Switch to completed tab to view badge (post verification)
  const completedTabBtn2 = wrapper.findAll('button.tab-button').find(b => b.text().includes('Completed'))
  if (completedTabBtn2) await completedTabBtn2.trigger('click')
  await wrapper.vm.$nextTick()
  await new Promise(r => setTimeout(r, 10))
  const badgeAfter = wrapper.find('.completion-badge.verified')
  expect(badgeAfter.exists()).toBe(true)
    expect(verifySpy).toHaveBeenCalledOnce()
  })
})
