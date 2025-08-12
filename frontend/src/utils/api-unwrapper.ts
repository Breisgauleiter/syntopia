// Centralized helpers for working with backend ApiResponse & Paginated payloads
// Avoid scattering envelope unwrapping logic across stores/components.

import type { ApiResponse } from '@/types/api.types'

// Generic backend envelope (mirrors backend ApiResponse structure)
interface BackendEnvelope<T = any> {
  success: boolean
  data?: T
  message?: string
  error?: any
}

// Paginated generic shape used in quest.service
export interface GenericPaginated<T> {
  data: T[]
  pagination?: {
    page?: number
    size?: number
    total?: number
    totalPages?: number
    hasNext?: boolean
    hasPrev?: boolean
  }
}

export function isBackendEnvelope(obj: any): obj is BackendEnvelope<any> {
  return obj && typeof obj === 'object' && 'success' in obj && ('data' in obj || 'error' in obj)
}

// Unwrap a backend ApiResponse style object. If not wrapped, returns input.
export function unwrap<T = any>(raw: any): T {
  if (isBackendEnvelope(raw)) {
    return raw.data as T
  }
  return raw as T
}

// Attempt to extract list from either direct array, paginated wrapper, or envelope.
export function extractList<T = any>(raw: any): { items: T[]; pagination?: GenericPaginated<T>['pagination'] } {
  const unwrapped = unwrap<any>(raw)

  if (Array.isArray(unwrapped)) {
    return { items: unwrapped }
  }

  if (unwrapped && typeof unwrapped === 'object') {
    // Paginated shape detection
    if (Array.isArray(unwrapped.data) && (unwrapped.pagination || typeof unwrapped.pagination === 'object')) {
      return { items: unwrapped.data as T[], pagination: unwrapped.pagination }
    }
    // Legacy quests { quests: [...], count: n }
    if (Array.isArray(unwrapped.quests)) {
      return { items: unwrapped.quests as T[], pagination: { total: unwrapped.count, page: 0, size: unwrapped.quests.length } }
    }
  }
  return { items: [] }
}

// Wrap try/catch helpers
export async function safeCall<T>(promise: Promise<ApiResponse<T>>): Promise<ApiResponse<T>> {
  try {
    return await promise
  } catch (e: any) {
    return { success: false, error: { message: e.message || 'Request failed' } }
  }
}

export default { unwrap, extractList, safeCall }
