import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import communityService from '@/services/community.service'
import { useUserStore } from '@/stores/user'

export interface AdaptedConnection {
  id: string
  type: string
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED'
  createdAt: string
  respondedAt?: string
  isOutgoing: boolean
  user: {
    id?: string
    displayName?: string
    profilePictureUrl?: string
    selectedRole?: string
    currentLevel?: number
  }
}

export const useConnectionsStore = defineStore('connections', () => {
  const userStore = useUserStore()

  // Raw lists
  const pending = ref<AdaptedConnection[]>([])
  const accepted = ref<AdaptedConnection[]>([])

  // Pagination & filters
  const page = ref(0)
  const size = ref(20)
  const total = ref(0)
  const hasMore = ref(false)
  const statusFilter = ref<'PENDING' | 'ACCEPTED' | 'DECLINED' | undefined>(undefined)
  const directionFilter = ref<'in' | 'out' | 'all'>('all')

  // Loading / error flags
  const loading = ref(false)
  const error = ref<string | null>(null)
  const actionInProgress = ref<string | null>(null)

  const totalPending = computed(() => pending.value.length)
  const totalAccepted = computed(() => accepted.value.length)

  const all = computed(() => [...pending.value, ...accepted.value])
  const pagination = computed(() => ({ page: page.value, size: size.value, total: total.value, hasMore: hasMore.value }))

  const adapt = (c: any): AdaptedConnection => {
    const currentUserId = userStore.user?.id
    const isOut = c?.direction ? c.direction === 'out' : (c?.fromUserId && currentUserId && c.fromUserId === currentUserId)
    return {
      id: c.id,
      type: c.type,
      status: c.status,
      createdAt: c.createdAt,
      respondedAt: c.updatedAt,
      isOutgoing: !!isOut,
      user: {
        id: c?.otherUser?.id,
        displayName: c?.otherUser?.displayName,
        profilePictureUrl: c?.otherUser?.avatarUrl,
        selectedRole: c?.otherUser?.selectedRole || c?.otherUser?.role,
        currentLevel: c?.otherUser?.currentLevel || c?.otherUser?.level
      }
    }
  }

  const loadConnections = async (reset = true) => {
    loading.value = true
    error.value = null
    try {
      if (reset) page.value = 0
      const res = await communityService.getConnections(statusFilter.value, directionFilter.value, page.value, size.value)
      if (!res.success) throw new Error(res.error?.message || 'Failed to load connections')
      const list = (res.data?.connections || []).map(adapt)
      pending.value = list.filter(c => c.status === 'PENDING')
      accepted.value = list.filter(c => c.status === 'ACCEPTED')
      const pag = (res.data as any)?.pagination || {}
      total.value = pag.totalCount ?? pag.total ?? list.length
      hasMore.value = pag.hasMore ?? ((page.value + 1) * size.value < total.value)
    } catch (e: any) {
      error.value = e?.message || 'Failed to load connections'
    } finally {
      loading.value = false
    }
  }

  const loadNextPage = async () => {
    if (loading.value || !hasMore.value) return
    page.value += 1
    loading.value = true
    try {
      const res = await communityService.getConnections(statusFilter.value, directionFilter.value, page.value, size.value)
      if (!res.success) throw new Error(res.error?.message || 'Failed to load more')
      const list = (res.data?.connections || []).map(adapt)
      // Merge preserving existing categories
      pending.value.push(...list.filter(c => c.status === 'PENDING'))
      accepted.value.push(...list.filter(c => c.status === 'ACCEPTED'))
      const pag = (res.data as any)?.pagination || {}
      total.value = pag.totalCount ?? pag.total ?? (pending.value.length + accepted.value.length)
      hasMore.value = pag.hasMore ?? ((page.value + 1) * size.value < total.value)
    } catch (e: any) {
      error.value = e?.message || 'Failed to load more'
    } finally {
      loading.value = false
    }
  }

  const setFilters = (opts: { status?: 'PENDING' | 'ACCEPTED' | 'DECLINED'; direction?: 'in' | 'out' | 'all'; size?: number }) => {
    if (opts.status !== undefined) statusFilter.value = opts.status
    if (opts.direction) directionFilter.value = opts.direction
    if (opts.size) size.value = opts.size
    return loadConnections(true)
  }

  const sendRequest = async (toUserId: string, type: 'friend' | 'mentor' | 'collaborator' = 'collaborator') => {
    // Optimistic insertion
    const tempId = `temp-${Date.now()}`
    const optimistic: AdaptedConnection = {
      id: tempId,
      type,
      status: 'PENDING',
      createdAt: new Date().toISOString(),
      isOutgoing: true,
      user: { id: toUserId }
    }
    pending.value.unshift(optimistic)
    try {
      const res = await communityService.sendConnectionRequest(toUserId, type)
      if (!res.success || !res.data) throw new Error(res.error?.message || 'Failed to send request')
      // Replace optimistic
      const idx = pending.value.findIndex(c => c.id === tempId)
      if (idx >= 0) pending.value[idx] = adapt(res.data)
      return true
    } catch (e) {
      // Revert
      pending.value = pending.value.filter(c => c.id !== tempId)
      return false
    }
  }

  const respond = async (connectionId: string, action: 'accept' | 'decline') => {
    const existing = pending.value.find(c => c.id === connectionId)
    if (!existing) return false
    actionInProgress.value = connectionId
    const original = { ...existing }
    // Optimistic state change
    if (action === 'accept') {
      existing.status = 'ACCEPTED'
      accepted.value.unshift(existing)
      pending.value = pending.value.filter(c => c.id !== connectionId)
    } else if (action === 'decline') {
      pending.value = pending.value.filter(c => c.id !== connectionId)
    }
    try {
      const res = await communityService.respondToConnectionRequest(connectionId, action)
      if (!res.success || !res.data) throw new Error(res.error?.message || 'Failed to respond')
      if (action === 'accept') {
        // Update accepted entry with server data
        const idx = accepted.value.findIndex(c => c.id === connectionId)
        if (idx >= 0) accepted.value[idx] = adapt(res.data)
      }
      return true
    } catch (e) {
      // Revert optimistic change
      if (action === 'accept') {
        // Remove from accepted and restore to pending
        accepted.value = accepted.value.filter(c => c.id !== connectionId)
        pending.value.unshift(original)
      } else if (action === 'decline') {
        pending.value.unshift(original)
      }
      return false
    } finally {
      actionInProgress.value = null
    }
  }

  const cancel = async (connectionId: string) => {
    const existing = pending.value.find(c => c.id === connectionId && c.isOutgoing)
    if (!existing) return false
    actionInProgress.value = connectionId
    // Optimistic removal
    pending.value = pending.value.filter(c => c.id !== connectionId)
    try {
      const res = await communityService.cancelConnectionRequest(connectionId)
      if (!res.success) throw new Error(res.error?.message || 'Failed to cancel')
      return true
    } catch (e) {
      pending.value.unshift(existing)
      return false
    } finally {
      actionInProgress.value = null
    }
  }

  return { pending, accepted, totalPending, totalAccepted, all, pagination, statusFilter, directionFilter, loading, error, actionInProgress, loadConnections, loadNextPage, setFilters, sendRequest, respond, cancel }
})

export type ConnectionsStore = ReturnType<typeof useConnectionsStore>
