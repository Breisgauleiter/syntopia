import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useUserStore } from '@/stores/user'
import { OnboardingService } from '@/services/onboarding.service'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: false }
    },
    {
      path: '/geometry',
      name: 'geometry',
      component: () => import('../views/GeometryView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/quests',
      name: 'quests',
      component: () => import('../views/QuestsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/community',
      name: 'community',
      component: () => import('../views/CommunityView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { requiresAuth: false, hideWhenAuthenticated: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: { requiresAuth: false, hideWhenAuthenticated: true }
    },
    {
      path: '/onboarding',
      name: 'onboarding',
      component: () => import('../views/OnboardingView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('../views/NotFoundView.vue'),
      meta: { requiresAuth: false }
    }
  ]
})

// Navigation guards
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  console.log('🔍 Router guard - navigating to:', to.name, 'from:', from.name)
  console.log('🔍 Current auth state - initialized:', userStore.authInitialized, 'authenticated:', userStore.isAuthenticated)
  
  // Wait for auth initialization to complete, but only if we haven't already initialized
  if (!userStore.authInitialized) {
    console.log('🔄 Waiting for auth initialization...')
    await userStore.checkAuthStatus()
  }
  
  // For pages that require auth
  if (to.meta.requiresAuth && !userStore.isAuthenticated) {
    console.log('🔒 Auth required but not authenticated, redirecting to login')
    next({ name: 'login', query: { redirect: to.fullPath } })
  } 
  // Prevent reopening onboarding when already completed
  else if (to.name === 'onboarding' && userStore.isAuthenticated) {
    const uid = userStore.user?.id || 'user-123'
    try {
      const progress = await OnboardingService.getUserOnboardingProgress(uid)
      if (progress.onboardingCompleted || (progress.currentLevel || 1) > 4) {
        console.log('✅ Onboarding completed, redirecting to quests')
        next({ name: 'quests' })
        return
      }
    } catch (e) {
      // fall through to next
    }
    next()
  }
  // For login/register pages when already authenticated
  else if (to.meta.hideWhenAuthenticated && userStore.isAuthenticated) {
    console.log('👤 Already authenticated, redirecting to home')
    next('/')
  } 
  // All other cases - allow navigation
  else {
    console.log('✅ Navigation allowed to:', to.name)
    next()
  }
})

export default router
