import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import questService, { type Quest, type UserQuest, UserQuestStatus, QuestStatus } from '@/services/quest.service'
import { useUserStore } from '@/stores/user'

export interface PaginationState { page: number; size: number; total: number }

export const useQuestStore = defineStore('quests', () => {
  const userStore = useUserStore()
  const quests = ref<Quest[]>([])
  const availableQuests = ref<UserQuest[]>([])
  const completedUserQuests = ref<UserQuest[]>([])

  const availablePagination = ref<PaginationState>({ page: 0, size: 50, total: 0 })
  const completedPagination = ref<PaginationState>({ page: 0, size: 50, total: 0 })

  const availablePageLoading = ref(false)
  const availablePageError = ref<string | null>(null)
  const completedPageLoading = ref(false)
  const completedPageError = ref<string | null>(null)

  const questStats = ref<{ totalCompleted: number; activeQuests: number; completedThisWeek: number; recentCompletions: UserQuest[] }>({
    totalCompleted: 0,
    activeQuests: 0,
    completedThisWeek: 0,
    recentCompletions: []
  })

  const completedQuestsMapped = computed<Quest[]>(() => completedUserQuests.value
    .filter(uq => !!uq.quest)
    .map(uq => { const q = uq.quest as Quest; q.status = QuestStatus.COMPLETED; return q }))

  const totalAvailablePages = computed(() => availablePagination.value.size > 0 ? Math.max(1, Math.ceil(availablePagination.value.total / availablePagination.value.size)) : 1)
  const totalCompletedPages = computed(() => completedPagination.value.size > 0 ? Math.max(1, Math.ceil(completedPagination.value.total / completedPagination.value.size)) : 1)
  const availableCount = computed(() => availablePagination.value.total || availableQuests.value.length)
  const completedCount = computed(() => completedPagination.value.total || completedQuestsMapped.value.length)

  const loadAllQuestsCatalog = async () => {
    try { const res = await questService.getQuests(); if (res.success && res.data) quests.value = res.data.quests || [] } catch (e) { /* ignore */ }
  }

  const mapUserQuestStatusesOntoQuest = (userQuest: UserQuest) => {
    if (!userQuest.quest) return
    switch (userQuest.status) {
      case UserQuestStatus.USER_ACTIVE: userQuest.quest.status = QuestStatus.ACTIVE; break
      case UserQuestStatus.USER_COMPLETED:
      case UserQuestStatus.USER_VERIFIED: userQuest.quest.status = QuestStatus.COMPLETED; break
      default: userQuest.quest.status = QuestStatus.AVAILABLE
    }
    if (!quests.value.find(q => q.id === userQuest.quest!.id)) quests.value.push(userQuest.quest!)
  }

  const loadAvailable = async (page = availablePagination.value.page, size = availablePagination.value.size) => {
    if (!userStore.user?.id) return
    availablePageLoading.value = true; availablePageError.value = null
    try {
      const res = await questService.getUserQuests(page, size)
      if (res.success && res.data) {
        availableQuests.value = res.data.data || []
        for (const uq of availableQuests.value) mapUserQuestStatusesOntoQuest(uq)
        Object.assign(availablePagination.value, res.data.pagination)
      } else availablePageError.value = res.error?.message || 'Failed to load available quests'
    } catch (e: any) { availablePageError.value = e?.message || 'Failed to load available quests' }
    finally { availablePageLoading.value = false }
  }

  const loadCompleted = async (page = completedPagination.value.page, size = completedPagination.value.size) => {
    if (!userStore.user?.id) return
    completedPageLoading.value = true; completedPageError.value = null
    try {
      const res = await questService.getUserCompletedQuests(page, size)
      if (res.success && res.data) {
        completedUserQuests.value = res.data.data || []
        for (const uq of completedUserQuests.value) mapUserQuestStatusesOntoQuest(uq)
        Object.assign(completedPagination.value, res.data.pagination)
      } else completedPageError.value = res.error?.message || 'Failed to load completed quests'
    } catch (e: any) { completedPageError.value = e?.message || 'Failed to load completed quests' }
    finally { completedPageLoading.value = false }
  }

  const loadStats = async () => { try { const res = await questService.getUserQuestStatistics(); if (res.success && res.data) questStats.value = res.data } catch {} }
  const refreshAll = async () => { await Promise.all([loadAllQuestsCatalog(), loadAvailable(0), loadCompleted(0), loadStats()]) }

  const updateAvailableUserQuest = (uq: UserQuest) => {
    const idx = availableQuests.value.findIndex(a => a.questId === uq.questId)
    if (idx >= 0) availableQuests.value[idx] = uq; else availableQuests.value.push(uq)
    mapUserQuestStatusesOntoQuest(uq)
  }

  return { quests, availableQuests, completedUserQuests, availablePagination, completedPagination, availablePageLoading, completedPageLoading, availablePageError, completedPageError, questStats, completedQuestsMapped, totalAvailablePages, totalCompletedPages, availableCount, completedCount, loadAllQuestsCatalog, loadAvailable, loadCompleted, loadStats, refreshAll, updateAvailableUserQuest }
})
