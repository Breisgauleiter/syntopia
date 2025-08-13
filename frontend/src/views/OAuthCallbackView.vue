<template>
  <div class="oauth-callback-view">
    <div class="container">
      <div class="status-card" :class="statusClass">
        <div class="icon" v-if="status==='loading'">⏳</div>
        <div class="icon" v-else-if="status==='success'">🎉</div>
        <div class="icon" v-else>⚠️</div>
        <h1 class="title">{{ title }}</h1>
        <p class="message">{{ message }}</p>
        <div v-if="status==='loading'" class="spinner"></div>
        <button v-if="status!=='loading'" class="btn btn-primary" @click="goNext">{{ nextLabel }}</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useToastStore } from '@/stores/toast'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const toast = useToastStore()

const status = ref<'loading' | 'success' | 'error'>('loading')
const title = ref('Completing GitHub Connection...')
const message = ref('Finalizing secure sign-in and preparing your dashboard.')
const nextLabel = ref('Go to Dashboard')

const statusClass = computed(()=>{
  switch(status.value){
    case 'success': return 'success'
    case 'error': return 'error'
    default: return 'loading'
  }
})

function goNext(){
  router.replace({ name: 'quests' })
}

onMounted(async () => {
  try {
    const token = route.query.token as string | undefined
    const refreshToken = route.query.refreshToken as string | undefined
    const githubIntegrated = route.query.githubIntegrated === 'true'
    const error = route.query.error as string | undefined

    if (error) {
      status.value = 'error'
      title.value = 'GitHub Connection Failed'
      message.value = decodeURIComponent(error)
      toast.push('GitHub connection failed', 'error')
      return
    }

    if (!token || !refreshToken) {
      status.value = 'error'
      title.value = 'Missing Credentials'
      message.value = 'The authentication response was incomplete. Please try again.'
      toast.push('OAuth callback missing tokens', 'error')
      return
    }

    // Persist tokens
    localStorage.setItem('syntopia_token', token)
    localStorage.setItem('syntopia_refresh_token', refreshToken)
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`

    // Fetch current user
    const res = await axios.get('/api/auth/me')
    const payload = (res.data && res.data.data) ? res.data.data : res.data.user ? res.data : res.data
    const user = payload.user || payload
    if (!user || !user.username) throw new Error('Malformed /me response')

    // Integrate into store (lightweight update)
    userStore.updateUserData({ ...user, isGitHubIntegrated: githubIntegrated || user.isGitHubIntegrated })
    // Ensure store recognizes token (if not already set)
    if (!userStore.token) {
      // minimal setter pattern: mimic login outcome
      // (avoid exposing private mutation; safe since store exposes reactive refs)
      ;(userStore as any).token = token
    }
    status.value = 'success'
    title.value = 'GitHub Connected!'
    message.value = 'Your account is now linked. Redirecting to quests...'
    toast.push('GitHub account connected', 'success')

    setTimeout(()=> goNext(), 1500)
  } catch (e:any) {
    console.error('OAuth callback error', e)
    status.value = 'error'
    title.value = 'Connection Error'
    message.value = 'Unable to finalize login. Please retry.'
    toast.push('Failed to finalize GitHub login', 'error')
  }
})
</script>

<style scoped>
.oauth-callback-view { min-height: 70vh; display:flex; align-items:center; }
.container { width:100%; display:flex; justify-content:center; }
.status-card { max-width:480px; width:100%; text-align:center; padding:2rem 2.25rem; border-radius:var(--radius-lg); background:rgba(255,255,255,0.05); backdrop-filter:blur(14px) saturate(140%); border:1px solid rgba(255,255,255,0.12); box-shadow:0 8px 24px rgba(0,0,0,0.4); animation: fadeIn .5s ease; }
.status-card.loading { border-color:rgba(99,102,241,0.4); }
.status-card.success { border-color:rgba(16,185,129,0.45); }
.status-card.error { border-color:rgba(239,68,68,0.45); }
.icon { font-size:2.5rem; margin-bottom:1rem; }
.title { margin:0 0 .75rem; font-size:1.5rem; font-weight:600; background:var(--gradient-primary); -webkit-background-clip:text; background-clip:text; -webkit-text-fill-color:transparent; }
.message { margin:0 0 1.25rem; color:var(--color-text-muted); line-height:1.5; }
.spinner { width:42px; height:42px; margin:0 auto 1rem; border:4px solid rgba(255,255,255,0.15); border-top:4px solid var(--color-primary); border-radius:50%; animation: spin 1s linear infinite; }
.btn { cursor:pointer; }
@keyframes spin { to { transform: rotate(360deg); } }
@keyframes fadeIn { from { opacity:0; transform:translateY(12px);} to { opacity:1; transform:translateY(0);} }
</style>
