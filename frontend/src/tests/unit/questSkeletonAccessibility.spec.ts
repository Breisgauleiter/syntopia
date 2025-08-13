import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import QuestsView from '@/views/QuestsView.vue'
import questService from '@/services/quest.service'

// Test that skeleton containers expose proper aria attributes

describe('QuestsView skeleton accessibility', () => {
  it('renders initial skeletons with aria attributes while loading', async () => {
    vi.spyOn(questService, 'getUserQuests').mockResolvedValue({ success: true, data: { data: [], pagination: { page:0,size:50,total:0 } } } as any)
    vi.spyOn(questService, 'getUserCompletedQuests').mockResolvedValue({ success: true, data: { data: [], pagination: { page:0,size:50,total:0 } } } as any)
    vi.spyOn(questService, 'getQuests').mockResolvedValue({ success: true, data: { quests: [] } } as any)
    vi.spyOn(questService, 'getUserQuestStatistics').mockResolvedValue({ success: true, data: { totalCompleted:0, activeQuests:0, completedThisWeek:0, recentCompletions: [] } } as any)

    const wrapper = mount(QuestsView, {
      global: { plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false })] }
    })

    // Immediately after mount before awaiting flush promises skeleton should exist
  ;(wrapper.vm as any).loading = true
  await wrapper.vm.$nextTick()
  const skeletonContainer = wrapper.find('.initial-skeletons')
  expect(skeletonContainer.exists()).toBe(true)
    expect(skeletonContainer.attributes('role')).toBe('status')
    expect(skeletonContainer.attributes('aria-busy')).toBe('true')
    expect(skeletonContainer.attributes('aria-label')).toBeDefined()
  })
})
