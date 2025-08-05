<template>
  <div class="onboarding-view">
    <div class="container">
      <div class="onboarding-card card">
        <h1 class="form-title">Sacred Role Selection</h1>
        <p class="form-subtitle">Choose your path in the Syntopia journey</p>
        
        <div class="roles-grid">
          <div 
            v-for="role in sacredRoles" 
            :key="role.id"
            class="role-option"
            :class="{ active: selectedRole === role.id }"
            @click="selectedRole = role.id"
          >
            <div class="role-icon">{{ role.icon }}</div>
            <h3 class="role-name">{{ role.name }}</h3>
            <p class="role-description">{{ role.description }}</p>
          </div>
        </div>
        
        <button 
          v-if="selectedRole"
          @click="confirmRole"
          class="btn btn-primary btn-full"
        >
          Begin as {{ getSacredRole(selectedRole)?.name }}
        </button>
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

const selectedRole = ref<string>('')

const sacredRoles = [
  {
    id: 'sacred-mathematician',
    name: 'Sacred Mathematician',
    description: 'Explore the numerical patterns underlying creation',
    icon: '∞'
  },
  {
    id: 'digital-architect',
    name: 'Digital Architect',
    description: 'Build the technological foundations of consciousness',
    icon: '⚡'
  },
  {
    id: 'community-weaver',
    name: 'Community Weaver',
    description: 'Connect souls and foster conscious collaboration',
    icon: '🌐'
  },
  {
    id: 'consciousness-explorer',
    name: 'Consciousness Explorer',
    description: 'Navigate the depths of awareness and meaning',
    icon: '🧠'
  },
  {
    id: 'quantum-developer',
    name: 'Quantum Developer',
    description: 'Code at the intersection of mind and matter',
    icon: '⚛️'
  },
  {
    id: 'wisdom-keeper',
    name: 'Wisdom Keeper',
    description: 'Preserve and share ancient knowledge for the future',
    icon: '📚'
  },
  {
    id: 'witness',
    name: 'Witness',
    description: 'Observe and validate the journey of others',
    icon: '👁️'
  }
]

const getSacredRole = (roleId: string) => {
  return sacredRoles.find(role => role.id === roleId)
}

const confirmRole = async () => {
  if (selectedRole.value) {
    // Update user role in store
    const role = getSacredRole(selectedRole.value)
    if (role) {
      await userStore.updateUserRole(role.name)
      router.push('/')
    }
  }
}
</script>

<style scoped>
.onboarding-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 0;
}

.onboarding-card {
  max-width: 800px;
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
  margin-bottom: 3rem;
}

.roles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.role-option {
  padding: 1.5rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.02);
  cursor: pointer;
  transition: all var(--transition-normal);
  text-align: center;
}

.role-option:hover {
  background: rgba(255, 255, 255, 0.05);
  transform: translateY(-2px);
}

.role-option.active {
  border-color: var(--color-primary);
  background: var(--color-primary-alpha);
}

.role-icon {
  font-size: 2rem;
  margin-bottom: 1rem;
}

.role-name {
  font-size: 1.125rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: var(--color-text);
}

.role-description {
  color: var(--color-text-muted);
  font-size: 0.875rem;
  line-height: 1.5;
}

.btn-full {
  width: 100%;
}

@media (max-width: 768px) {
  .roles-grid {
    grid-template-columns: 1fr;
  }
}
</style>
