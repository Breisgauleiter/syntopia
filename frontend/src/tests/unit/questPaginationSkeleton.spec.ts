import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import QuestsView from '@/views/QuestsView.vue'
import questService from '@/services/quest.service'

// Test pagination skeleton visibility when toggling availablePageLoading

describe('QuestsView pagination skeleton', () => {
  it('shows and hides available page skeletons when loading flag toggles', async () => {
    vi.spyOn(questService, 'getUserQuests').mockResolvedValue({ success: true, data: { data: [], pagination: { page:0,size:50,total:120 } } } as any)
    vi.spyOn(questService, 'getUserCompletedQuests').mockResolvedValue({ success: true, data: { data: [], pagination: { page:0,size:50,total:0 } } } as any)
    vi.spyOn(questService, 'getQuests').mockResolvedValue({ success: true, data: { quests: [] } } as any)
    vi.spyOn(questService, 'getUserQuestStatistics').mockResolvedValue({ success: true, data: { totalCompleted:0, activeQuests:0, completedThisWeek:0, recentCompletions: [] } } as any)

    const wrapper = mount(QuestsView, { 
      global: { 
        plugins: [createTestingPinia({ 
          createSpy: vi.fn, 
          stubActions: false, 
          initialState: {
            quests: {
              availablePageLoading: true,
              availablePagination: { page:0,size:50,total:120 },
              completedPagination: { page:0,size:50,total:0 }
            }
          }
        })]
      }
    })
    ;(wrapper.vm as any).loading = false
    ;(wrapper.vm as any).activeTab = 'available'
    await wrapper.vm.$nextTick()
  const questStore = (wrapper.vm as any).questStore

  let inlineSkeleton = wrapper.find('.skeleton-cards.inline')
  expect(inlineSkeleton.exists()).toBe(true)

    // Turn off loading
  questStore.availablePageLoading = false
    await wrapper.vm.$nextTick()
  inlineSkeleton = wrapper.find('.skeleton-cards.inline')
  expect(inlineSkeleton.exists()).toBe(false)
  })
})
