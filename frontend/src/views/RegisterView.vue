<template>
  <div class="register-view">
    <div class="container">
      <div class="register-form card">
        <h1 class="form-title">Join Syntopia</h1>
        <p class="form-subtitle">Begin your journey into sacred consciousness</p>
        
        <form @submit.prevent="handleRegister">
          <div class="form-group">
            <label for="username">Username</label>
            <input
              id="username"
              v-model="username"
              type="text"
              class="form-input"
              placeholder="Your sacred name"
              required
            />
          </div>
          
          <div class="form-group">
            <label for="email">Email</label>
            <input
              id="email"
              v-model="email"
              type="email"
              class="form-input"
              placeholder="your@email.com"
              required
            />
          </div>
          
          <div class="form-group">
            <label for="password">Password</label>
            <input
              id="password"
              v-model="password"
              type="password"
              class="form-input"
              placeholder="Create a strong password"
              required
            />
          </div>
          
          <div class="form-group">
            <label for="displayName">Display Name</label>
            <input
              id="displayName"
              v-model="displayName"
              type="text"
              class="form-input"
              placeholder="How others will see you"
              required
            />
          </div>
          
          <button type="submit" class="btn btn-primary btn-full">
            Create Account
          </button>
        </form>
        
        <div class="form-footer">
          <p>Already have an account? <RouterLink to="/login">Sign in</RouterLink></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const username = ref('')
const email = ref('')
const password = ref('')
const displayName = ref('')

const handleRegister = async () => {
  try {
    await userStore.register({
      username: username.value,
      email: email.value,
      displayName: displayName.value
    })
    router.push('/onboarding')
  } catch (error) {
    console.error('Registration failed:', error)
  }
}
</script>

<style scoped>
.register-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 0;
}

.register-form {
  max-width: 400px;
  width: 100%;
  padding: 2rem;
}

.form-title {
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  text-align: center;
  color: var(--color-text);
}

.form-subtitle {
  color: var(--color-text-muted);
  text-align: center;
  margin-bottom: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--color-text);
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.05);
  color: var(--color-text);
  font-size: 1rem;
  transition: all var(--transition-normal);
}

.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
  background: rgba(255, 255, 255, 0.1);
}

.btn-full {
  width: 100%;
  margin-bottom: 1.5rem;
}

.form-footer {
  text-align: center;
}

.form-footer p {
  color: var(--color-text-muted);
}

.form-footer a {
  color: var(--color-primary);
  text-decoration: none;
}

.form-footer a:hover {
  text-decoration: underline;
}
</style>
