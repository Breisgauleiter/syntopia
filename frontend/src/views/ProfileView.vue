<template>
  <div class="profile-view">
    <div class="container">
      <!-- Profile Header -->
      <div class="profile-header">
        <div class="profile-avatar">
          <img 
            :src="profile?.profilePictureUrl || '/default-avatar.png'" 
            :alt="profile?.displayName || 'Profile'"
            @error="handleImageError"
          />
          <div class="level-badge">{{ profile?.currentLevel || 1 }}</div>
        </div>
        
        <div class="profile-info">
          <h1 class="profile-name">{{ profile?.displayName || 'Sacred Traveler' }}</h1>
          <div class="profile-role">{{ profile?.selectedRole || 'Exploring' }}</div>
          <div class="profile-stats">
            <div class="stat">
              <span class="stat-value">{{ profile?.experiencePoints || 0 }}</span>
              <span class="stat-label">Experience</span>
            </div>
            <div class="stat">
              <span class="stat-value">{{ profile?.questsCompleted || 0 }}</span>
              <span class="stat-label">Quests</span>
            </div>
            <div class="stat">
              <span class="stat-value">{{ socialData?.connectionCount || 0 }}</span>
              <span class="stat-label">Connections</span>
            </div>
          </div>
        </div>
        
        <div class="profile-actions">
          <button class="btn btn-primary" @click="editProfile">
            <i class="fas fa-edit"></i> Edit Profile
          </button>
          <button class="btn btn-secondary" @click="uploadAvatar">
            <i class="fas fa-camera"></i> Change Avatar
          </button>
        </div>
      </div>

      <!-- Profile Tabs -->
      <div class="profile-tabs">
        <button 
          v-for="tab in tabs" 
          :key="tab.id"
          class="tab-button"
          :class="{ active: activeTab === tab.id }"
          @click="activeTab = tab.id"
        >
          <i :class="tab.icon"></i>
          {{ tab.label }}
        </button>
      </div>

      <!-- Tab Content -->
      <div class="tab-content">
        <!-- Overview Tab -->
        <div v-if="activeTab === 'overview'" class="tab-panel">
          <div class="profile-grid">
            <!-- Bio Section -->
            <div class="profile-card">
              <h3><i class="fas fa-user"></i> Bio</h3>
              <p v-if="profile?.bio" class="bio-text">{{ profile.bio }}</p>
              <p v-else class="bio-placeholder">No bio added yet. Click Edit Profile to add one.</p>
            </div>

            <!-- Progress Section -->
            <div class="profile-card">
              <h3><i class="fas fa-chart-line"></i> Progress</h3>
              <div class="progress-item">
                <div class="progress-label">Level {{ profile?.currentLevel || 1 }} Progress</div>
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: levelProgress + '%' }"></div>
                </div>
                <div class="progress-text">{{ profile?.experiencePoints || 0 }} / {{ nextLevelXP }} XP</div>
              </div>
            </div>

            <!-- Achievements Section -->
            <div class="profile-card">
              <h3><i class="fas fa-trophy"></i> Achievements</h3>
              <div v-if="unlockedAchievements.length > 0" class="badges-grid">
                <div 
                  v-for="achievement in unlockedAchievements" 
                  :key="achievement.id"
                  class="badge-item earned"
                >
                  <div class="badge-icon">{{ achievement.icon }}</div>
                  <div class="badge-name">{{ achievement.name }}</div>
                </div>
              </div>
              <p v-else class="no-achievements">Complete quests to earn achievements!</p>
            </div>

            <!-- Recent Activity -->
            <div class="profile-card">
              <h3><i class="fas fa-history"></i> Recent Activity</h3>
              <div class="activity-placeholder">
                <p>Quest history will be available once the quest completion tracking is implemented.</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Social Tab -->
        <div v-if="activeTab === 'social'" class="tab-panel">
          <div class="social-grid">
            <!-- Connections -->
            <div class="profile-card">
              <h3><i class="fas fa-users"></i> Connections</h3>
              <div v-if="socialData?.collaborations?.length" class="connections-list">
                <div 
                  v-for="connection in socialData.collaborations.filter(c => c.status === 'ACCEPTED')" 
                  :key="connection.user.id"
                  class="connection-item"
                >
                  <img 
                    :src="connection.user.profilePictureUrl || '/default-avatar.png'" 
                    :alt="connection.user.displayName"
                    class="connection-avatar"
                  />
                  <div class="connection-info">
                    <div class="connection-name">{{ connection.user.displayName }}</div>
                    <div class="connection-role">{{ connection.user.selectedRole }}</div>
                  </div>
                  <div class="connection-level">Lv {{ connection.user.currentLevel }}</div>
                </div>
              </div>
              <p v-else class="no-connections">No connections yet. Visit the Community to connect with others!</p>
            </div>

            <!-- Projects -->
            <div class="profile-card">
              <h3><i class="fas fa-project-diagram"></i> Projects</h3>
              <div v-if="socialData?.projects?.length" class="projects-list">
                <div 
                  v-for="project in socialData.projects" 
                  :key="project.project.id"
                  class="project-item"
                >
                  <div class="project-icon">🚀</div>
                  <div class="project-info">
                    <div class="project-name">{{ project.project.title }}</div>
                    <div class="project-role">{{ project.role }}</div>
                  </div>
                  <div class="project-date">{{ formatDate(project.joinedAt) }}</div>
                </div>
              </div>
              <p v-else class="no-projects">No collaborative projects yet. Join the community to start collaborating!</p>
            </div>
          </div>
        </div>

        <!-- Settings Tab -->
        <div v-if="activeTab === 'settings'" class="tab-panel">
          <div class="settings-grid">
            <div class="profile-card">
              <h3><i class="fas fa-cog"></i> Profile Settings</h3>
              <form @submit.prevent="saveSettings" class="settings-form">
                <div class="form-group">
                  <label for="displayName">Display Name</label>
                  <input 
                    id="displayName"
                    v-model="editForm.displayName" 
                    type="text" 
                    class="form-input"
                    placeholder="Your display name"
                  />
                </div>
                
                <div class="form-group">
                  <label for="bio">Bio</label>
                  <textarea 
                    id="bio"
                    v-model="editForm.bio" 
                    class="form-textarea"
                    rows="4"
                    placeholder="Tell us about your journey..."
                  ></textarea>
                </div>

                <div class="form-group">
                  <label class="checkbox-label">
                    <input 
                      v-model="editForm.isProfilePublic" 
                      type="checkbox"
                      class="form-checkbox"
                    />
                    Make profile public
                  </label>
                </div>

                <div class="form-actions">
                  <button type="submit" class="btn btn-primary" :disabled="saving">
                    {{ saving ? 'Saving...' : 'Save Changes' }}
                  </button>
                  <button type="button" class="btn btn-secondary" @click="resetForm">
                    Reset
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Avatar Upload Modal -->
    <div v-if="showAvatarModal" class="modal-overlay" @click="showAvatarModal = false">
      <div class="modal-content" @click.stop>
        <h3>Upload Avatar</h3>
        <input ref="fileInput" type="file" accept="image/*" @change="handleFileSelect" />
        <div class="modal-actions">
          <button class="btn btn-primary" @click="uploadFile" :disabled="!selectedFile || uploading">
            {{ uploading ? 'Uploading...' : 'Upload' }}
          </button>
          <button class="btn btn-secondary" @click="showAvatarModal = false">
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import api from '../services/api'
import profileService from '../services/profile.service'
import type { User } from '../types/api.types'

interface ProfileData extends User {
  profilePictureUrl?: string
  bio?: string
  socialLinks?: Record<string, string>
  isProfilePublic?: boolean
}

interface SocialData {
  connectionCount: number
  connections: any[]
  collaborations?: any[]
  projects?: any[]
  badges?: Record<string, boolean>
  questHistory?: any[]
}

interface Achievement {
  id: string
  name: string
  description: string
  icon: string
  unlockedAt?: string
  isUnlocked: boolean
}

const profile = ref<ProfileData | null>(null)
const socialData = ref<SocialData | null>(null)
const achievements = ref<Achievement[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const activeTab = ref('overview')
const showAvatarModal = ref(false)
const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const saving = ref(false)

const tabs = [
  { id: 'overview', label: 'Overview', icon: 'fas fa-user' },
  { id: 'achievements', label: 'Achievements', icon: 'fas fa-trophy' },
  { id: 'social', label: 'Social', icon: 'fas fa-users' },
  { id: 'settings', label: 'Settings', icon: 'fas fa-cog' }
]

const editForm = ref({
  displayName: '',
  bio: '',
  isProfilePublic: true
})

const unlockedAchievements = computed(() => 
  achievements.value.filter(a => a.isUnlocked)
)

const lockedAchievements = computed(() => 
  achievements.value.filter(a => !a.isUnlocked)
)

const levelProgress = computed(() => {
  if (!profile.value) return 0
  const currentXP = profile.value.experiencePoints || 0
  const currentLevel = profile.value.currentLevel || 1
  const nextLevelXP = currentLevel * 1000 // Simple calculation
  const levelXP = currentXP % 1000
  return (levelXP / 1000) * 100
})

const nextLevelXP = computed(() => {
  if (!profile.value) return 1000
  return (profile.value.currentLevel || 1) * 1000
})

const loadProfile = async () => {
  try {
    loading.value = true
    const result = await profileService.getCurrentProfile()
    
    if (result.success) {
      profile.value = result.data
      editForm.value.displayName = result.data.displayName || ''
      editForm.value.bio = result.data.bio || ''
      editForm.value.isProfilePublic = result.data.isProfilePublic !== false
    } else {
      error.value = result.error?.message || 'Failed to load profile'
    }
  } catch (err) {
    console.error('Failed to load profile:', err)
    error.value = 'Failed to load profile'
  } finally {
    loading.value = false
  }
}

const loadSocialData = async () => {
  try {
    const result = await profileService.getSocialConnections()
    if (result.success) {
      const d: any = result.data || {}
      const connections: any[] = Array.isArray(d.connections) ? d.connections : []

      // Adapt backend shape to the view's expected structure
      const collaborations = connections.map((c: any) => ({
        status: c.status,
        user: {
          id: c.otherUser?.id,
          displayName: c.otherUser?.displayName,
          profilePictureUrl: c.otherUser?.avatarUrl,
          selectedRole: c.otherUser?.selectedRole,
          currentLevel: c.otherUser?.currentLevel
        }
      }))

      socialData.value = {
        connectionCount: d?.stats?.total ?? d?.totalConnections ?? connections.length ?? 0,
        connections,
        collaborations,
        projects: Array.isArray(d.projects) ? d.projects : []
      }
    }
  } catch (error) {
    console.error('Failed to load social data:', error)
  }
}

const loadAchievements = async () => {
  try {
    const result = await profileService.getAchievements()
    if (result.success) {
      // Backend returns an object with { achievements: [...], totalAchievements, completedAchievements }
      const d: any = result.data || {}
      achievements.value = Array.isArray(d.achievements) ? d.achievements : []
    }
  } catch (error) {
    console.error('Failed to load achievements:', error)
  }
}

const editProfile = () => {
  activeTab.value = 'settings'
}

const uploadAvatar = () => {
  showAvatarModal.value = true
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    selectedFile.value = target.files[0]
  }
}

const uploadFile = async () => {
  if (!selectedFile.value) return
  
  try {
    uploading.value = true
    const result = await profileService.uploadAvatar(selectedFile.value)
    
    if (result.success) {
      profile.value = { ...profile.value!, ...result.data }
      showAvatarModal.value = false
      selectedFile.value = null
      alert('Avatar uploaded successfully!')
    } else {
      alert('Failed to upload avatar: ' + result.error?.message)
    }
  } catch (error) {
    console.error('Failed to upload avatar:', error)
    alert('Failed to upload avatar. Please try again.')
  } finally {
    uploading.value = false
  }
}

const saveSettings = async () => {
  try {
    saving.value = true
    
    const updateData = {
      displayName: editForm.value.displayName,
      bio: editForm.value.bio,
      isProfilePublic: editForm.value.isProfilePublic
    }
    
    const result = await profileService.updateProfile(updateData)
    
    if (result.success) {
      profile.value = { ...profile.value!, ...result.data }
      alert('Profile updated successfully!')
    } else {
      alert('Failed to save profile: ' + result.error?.message)
    }
  } catch (error) {
    console.error('Failed to save profile:', error)
    alert('Failed to save profile. Please try again.')
  } finally {
    saving.value = false
  }
}

const resetForm = () => {
  if (profile.value) {
    editForm.value.displayName = profile.value.displayName || ''
    editForm.value.bio = profile.value.bio || ''
    editForm.value.isProfilePublic = profile.value.isProfilePublic !== false
  }
}

const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement
  target.src = '/default-avatar.png'
}

const formatDate = (dateString: string) => {
  if (!dateString) return 'Unknown'
  return new Date(dateString).toLocaleDateString()
}

onMounted(() => {
  loadProfile()
  loadSocialData()
  loadAchievements()
})
</script>

<style scoped>
.profile-view {
  min-height: 100vh;
  padding: 2rem 0;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 600;
  margin-bottom: 3rem;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-align: center;
}

.coming-soon {
  text-align: center;
  padding: 3rem 2rem;
  max-width: 600px;
  margin: 0 auto;
}

.coming-soon h2 {
  margin-bottom: 1rem;
  color: var(--color-text);
}

.coming-soon p {
  color: var(--color-text-muted);
  margin-bottom: 2rem;
  line-height: 1.6;
}

.coming-soon ul {
  text-align: left;
  max-width: 400px;
  margin: 0 auto;
}

.coming-soon li {
  color: var(--color-text-muted);
  margin-bottom: 0.5rem;
  line-height: 1.5;
}
</style>
