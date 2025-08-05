<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { onMounted } from 'vue'

const userStore = useUserStore()

onMounted(() => {
  // Check for authenticated user on app load
  userStore.checkAuthStatus()
})
</script>

<template>
  <div id="app">
    <!-- Navigation Header -->
    <header class="app-header">
      <nav class="nav-container">
        <div class="nav-brand">
          <RouterLink to="/" class="brand-link">
            <div class="logo">
              <!-- Sacred Geometry Symbol -->
              <svg width="32" height="32" viewBox="0 0 32 32" class="logo-svg">
                <circle cx="16" cy="16" r="12" fill="none" stroke="currentColor" stroke-width="1.5"/>
                <circle cx="16" cy="8" r="4" fill="none" stroke="currentColor" stroke-width="1"/>
                <circle cx="12" cy="20" r="4" fill="none" stroke="currentColor" stroke-width="1"/>
                <circle cx="20" cy="20" r="4" fill="none" stroke="currentColor" stroke-width="1"/>
              </svg>
            </div>
            <span class="brand-text">Syntopia</span>
          </RouterLink>
        </div>
        
        <div class="nav-links">
          <RouterLink to="/" class="nav-link">Home</RouterLink>
          <RouterLink to="/geometry" class="nav-link">Sacred Geometry</RouterLink>
          <RouterLink to="/quests" class="nav-link">Quests</RouterLink>
          <RouterLink to="/community" class="nav-link">Community</RouterLink>
          
          <div v-if="userStore.isAuthenticated" class="user-menu">
            <RouterLink to="/profile" class="nav-link user-link">
              {{ userStore.user?.displayName || userStore.user?.username }}
            </RouterLink>
            <button @click="userStore.logout" class="logout-btn">Logout</button>
          </div>
          
          <div v-else class="auth-links">
            <RouterLink to="/login" class="nav-link">Login</RouterLink>
            <RouterLink to="/register" class="nav-link register-btn">Join</RouterLink>
          </div>
        </div>
      </nav>
    </header>

    <!-- Main Content -->
    <main class="main-content">
      <RouterView />
    </main>

    <!-- Footer -->
    <footer class="app-footer">
      <div class="footer-content">
        <p>&copy; 2025 Syntopia - Sacred Geometry Platform</p>
        <p>Exploring the divine mathematics of creation</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.app-header {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav-brand {
  display: flex;
  align-items: center;
}

.brand-link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  text-decoration: none;
  color: var(--color-text);
  font-weight: 600;
  font-size: 1.25rem;
}

.logo-svg {
  color: var(--color-primary);
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.nav-link {
  text-decoration: none;
  color: var(--color-text);
  font-weight: 500;
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  transition: all 0.2s ease;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-1px);
}

.nav-link.router-link-active {
  background: rgba(var(--color-primary-rgb), 0.2);
  color: var(--color-primary);
}

.register-btn {
  background: var(--gradient-primary);
  color: white;
  border-radius: 2rem;
  padding: 0.75rem 1.5rem;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.logout-btn {
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text);
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.main-content {
  min-height: calc(100vh - 140px);
  padding: 2rem 0;
}

.app-footer {
  background: rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding: 2rem 0;
  text-align: center;
  color: var(--color-text-muted);
}

.footer-content p {
  margin: 0.25rem 0;
}
</style>
