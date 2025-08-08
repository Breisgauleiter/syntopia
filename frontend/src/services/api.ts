import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios'

/**
 * Base API Service for Syntopia Platform
 * 
 * Provides a configured Axios instance with:
 * - Auto token attachment
 * - Request/Response interceptors
 * - Error standardization
 * - Development logging
 */

// API Response wrapper for consistent error handling
export interface ApiResponse<T = any> {
  success: boolean
  data?: T
  error?: ApiError
}

// Standardized error structure
export interface ApiError {
  message: string
  field?: string
  code?: string
  status?: number
}

// Base configuration for API calls
const API_CONFIG = {
  baseURL: '/api', // Vite proxy handles routing to localhost:8080
  timeout: 10000,  // 10 seconds timeout
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
}

class ApiService {
  private axiosInstance: AxiosInstance

  constructor() {
    this.axiosInstance = axios.create(API_CONFIG)
    this.setupInterceptors()
  }

  /**
   * Setup request and response interceptors
   */
  private setupInterceptors(): void {
    // Request interceptor - Auto attach token
    this.axiosInstance.interceptors.request.use(
      (config) => {
        // Auto-attach JWT token if available
        const token = localStorage.getItem('syntopia_token')
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }

        // Development logging (always enabled for now)
        console.log('🚀 API Request:', {
          method: config.method?.toUpperCase(),
          url: config.url,
          headers: config.headers,
          data: config.data
        })

        return config
      },
      (error) => {
        console.error('❌ Request Error:', error)
        return Promise.reject(error)
      }
    )

    // Response interceptor - Handle errors and logging
    this.axiosInstance.interceptors.response.use(
      (response: AxiosResponse) => {
        // Development logging
        console.log('✅ API Response:', {
          status: response.status,
          url: response.config.url,
          data: response.data
        })

        return response
      },
      (error: AxiosError) => {
        const apiError = this.handleError(error)
        
        // Development logging
        console.error('❌ API Error:', {
          status: error.response?.status,
          url: error.config?.url,
          message: apiError.message,
          response: error.response?.data
        })

        return Promise.reject(apiError)
      }
    )
  }

  /**
   * Standardize error responses across the application
   */
  private handleError(error: AxiosError): ApiError {
    // Network or timeout errors
    if (!error.response) {
      return {
        message: 'Network error. Please check your connection.',
        code: 'NETWORK_ERROR'
      }
    }

    const status = error.response.status
    const data = error.response.data as any

    // Handle different HTTP status codes
    switch (status) {
      case 400:
        return {
          message: data?.message || 'Invalid request data',
          field: data?.field,
          code: 'BAD_REQUEST',
          status
        }
      
      case 401:
        // Token expired or invalid - clear auth data
        this.clearAuthData()
        return {
          message: 'Authentication required. Please log in.',
          code: 'UNAUTHORIZED',
          status
        }
      
      case 403:
        return {
          message: 'Access denied. Insufficient permissions.',
          code: 'FORBIDDEN',
          status
        }
      
      case 404:
        return {
          message: 'Resource not found',
          code: 'NOT_FOUND',
          status
        }
      
      case 422:
        return {
          message: data?.message || 'Validation failed',
          field: data?.field,
          code: 'VALIDATION_ERROR',
          status
        }
      
      case 500:
        return {
          message: 'Server error. Please try again later.',
          code: 'SERVER_ERROR',
          status
        }
      
      default:
        return {
          message: data?.message || 'An unexpected error occurred',
          code: 'UNKNOWN_ERROR',
          status
        }
    }
  }

  /**
   * Clear authentication data when token is invalid
   */
  private clearAuthData(): void {
    localStorage.removeItem('syntopia_token')
    localStorage.removeItem('syntopia_refresh_token')
    
    // Redirect to login if not already there
    if (window.location.pathname !== '/login') {
      window.location.href = '/login'
    }
  }

  /**
   * Generic GET request
   */
  async get<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    try {
      const response = await this.axiosInstance.get<T>(url, config)
      return {
        success: true,
        data: response.data
      }
    } catch (error) {
      return {
        success: false,
        error: error as ApiError
      }
    }
  }

  /**
   * Generic POST request
   */
  async post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    try {
      const response = await this.axiosInstance.post<T>(url, data, config)
      return {
        success: true,
        data: response.data
      }
    } catch (error) {
      return {
        success: false,
        error: error as ApiError
      }
    }
  }

  /**
   * Generic PUT request
   */
  async put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    try {
      const response = await this.axiosInstance.put<T>(url, data, config)
      return {
        success: true,
        data: response.data
      }
    } catch (error) {
      return {
        success: false,
        error: error as ApiError
      }
    }
  }

  /**
   * Generic DELETE request
   */
  async delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    try {
      const response = await this.axiosInstance.delete<T>(url, config)
      return {
        success: true,
        data: response.data
      }
    } catch (error) {
      return {
        success: false,
        error: error as ApiError
      }
    }
  }

  /**
   * Get the raw Axios instance for advanced usage
   */
  getInstance(): AxiosInstance {
    return this.axiosInstance
  }

  /**
   * Update base URL (useful for different environments)
   */
  setBaseURL(baseURL: string): void {
    this.axiosInstance.defaults.baseURL = baseURL
  }

  /**
   * Set custom headers
   */
  setHeaders(headers: Record<string, string>): void {
    Object.assign(this.axiosInstance.defaults.headers.common, headers)
  }
}

// Export singleton instance
export const apiService = new ApiService()
export default apiService
