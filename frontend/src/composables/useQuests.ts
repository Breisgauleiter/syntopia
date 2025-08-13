import { computed } from 'vue'
import { useQuestStore, type PaginationState } from '@/stores/quests'
import questService, { type Quest, QuestStatus, UserQuestStatus } from '@/services/quest.service'

/**
 * Lightweight composable wrapper around the quest Pinia store.
 * Centralizes frequently used derived flags & action helpers for components.
 */
export function useQuests() {
  const questStore = useQuestStore()

  // Direct store state re-export (readonly via computed for safety)
  const available = computed(() => questStore.availableQuests)
  const completed = computed(() => questStore.completedQuestsMapped)
  const stats = computed(() => questStore.questStats)

  // Loading / error flags
  const loadingAvailablePage = computed(() => questStore.availablePageLoading)
  const loadingCompletedPage = computed(() => questStore.completedPageLoading)
  const availableError = computed(() => questStore.availablePageError)
  const completedError = computed(() => questStore.completedPageError)

  // Pagination helpers
  const availablePagination = computed(() => questStore.availablePagination)
  const completedPagination = computed(() => questStore.completedPagination)

  const totalAvailablePages = computed(() => questStore.totalAvailablePages)
  const totalCompletedPages = computed(() => questStore.totalCompletedPages)

  // Derived counts
  const availableCount = computed(() => questStore.availableCount)
  const completedCount = computed(() => questStore.completedCount)

  // Refresh bundle
  const refresh = () => questStore.refreshAll()

  // Explicit page loaders (exposed so views don't touch store directly)
  const loadAvailablePage = (page: number) => questStore.loadAvailable(page)
  const loadCompletedPage = (page: number) => questStore.loadCompleted(page)

  // Wrapper actions returning success boolean for UI convenience
  const accept = async (quest: Quest) => {
    const res = await questService.acceptQuest(quest.id)
    if (res.success && res.data) {
      quest.status = QuestStatus.ACTIVE
      await questStore.loadAvailable(availablePagination.value.page)
      return true
    }
    return false
  }

  const complete = async (quest: Quest) => {
    const res = await questService.completeQuest(quest.id)
    if (res.success && res.data) {
      quest.status = QuestStatus.COMPLETED
      await Promise.all([
        questStore.loadAvailable(availablePagination.value.page),
        questStore.loadCompleted(completedPagination.value.page)
      ])
      return true
    }
    return false
  }

  const verify = async (quest: Quest) => {
    const res = await questService.verifyQuest(quest.id)
    if (res.success && res.data) {
      // Set verified in related user quest if present
      const uq = questStore.availableQuests.find(u => u.questId === quest.id || u.quest?.id === quest.id)
      if (uq) uq.status = UserQuestStatus.USER_VERIFIED
      await questStore.loadCompleted(completedPagination.value.page)
      return true
    }
    return false
  }

  const abandon = async (quest: Quest) => {
    const res = await questService.abandonQuest(quest.id)
    if (res.success && res.data) {
      quest.status = QuestStatus.AVAILABLE
      await questStore.loadAvailable(availablePagination.value.page)
      return true
    }
    return false
  }

  return {
    // raw store
    questStore,
    // data
    available,
    completed,
    stats,
    // flags
    loadingAvailablePage,
    loadingCompletedPage,
    availableError,
    completedError,
    // pagination
    availablePagination,
    completedPagination,
    totalAvailablePages,
    totalCompletedPages,
    availableCount,
    completedCount,
    // actions
    refresh,
    accept,
    complete,
    verify
  , abandon
  , loadAvailablePage
  , loadCompletedPage
  }
}
