<template>
  <div class="community-view">
    <div class="container">
    <!-- Live region for connection feedback -->
    <div aria-live="polite" class="visually-hidden" v-if="connectionFeedback">{{ connectionFeedback }}</div>
      <!-- Header -->
      <div class="community-header">
        <h1 class="page-title">Syntopia Community</h1>
        <p class="page-subtitle">Connect with fellow consciousness explorers</p>
        
        <!-- Community Stats -->
        <div class="community-stats">
          <div class="stat-card">
            <div class="stat-value">{{ stats?.connections || 0 }}</div>
            <div class="stat-label">Your Connections</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ stats?.projects || 0 }}</div>
            <div class="stat-label">Projects</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ stats?.questsCompleted || 0 }}</div>
            <div class="stat-label">Quests Completed</div>
          </div>
        </div>
      </div>

      <!-- Community Tabs -->
      <div class="community-tabs">
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
        <!-- Feed Tab -->
        <div v-if="activeTab === 'feed'" class="tab-panel">
          <div class="feed-container">
            <h2><i class="fas fa-stream"></i> Community Feed</h2>
            
            <div v-if="loading" class="loading-state">
              <div class="loading-spinner"></div>
              <p>Loading community activity...</p>
            </div>
            
            <div v-else-if="feed?.activities?.length" class="activity-feed">
              <div 
                v-for="activity in feed.activities" 
                :key="activity.timestamp"
                class="activity-item"
              >
                <div class="activity-user">
                  <img 
                    :src="activity.user.profilePictureUrl || '/default-avatar.png'" 
                    :alt="activity.user.displayName"
                    class="user-avatar"
                  />
                  <div class="user-info">
                    <div class="user-name">{{ activity.user.displayName }}</div>
                    <div class="user-role">{{ activity.user.selectedRole }} • Level {{ activity.user.currentLevel }}</div>
                  </div>
                </div>
                
                <div class="activity-content">
                  <div class="activity-text">
                    <span class="action-text">completed quest:</span>
                    <span class="quest-title">{{ activity.quest.title }}</span>
                  </div>
                  <div class="activity-meta">
                    <span class="quest-type">{{ activity.quest.type }}</span>
                    <span class="experience-reward">+{{ activity.quest.experienceReward }} XP</span>
                    <span class="activity-time">{{ formatTimeAgo(activity.timestamp) }}</span>
                  </div>
                </div>
              </div>
            </div>
            
            <div v-else class="empty-state">
              <i class="fas fa-stream"></i>
              <h3>No recent activity</h3>
              <p>Complete some quests to see community activity!</p>
            </div>
          </div>
        </div>

        <!-- Discover Tab -->
        <div v-if="activeTab === 'discover'" class="tab-panel">
          <div class="discover-container">
            <div class="discover-header">
              <h2><i class="fas fa-compass"></i> Discover Users</h2>
              
              <!-- Search and Filters -->
              <div class="search-filters">
                <div class="search-input">
                  <i class="fas fa-search"></i>
                  <input 
                    v-model="searchQuery"
                    type="text" 
                    placeholder="Search users..."
                    @input="searchUsers"
                  />
                </div>
                
                <select v-model="roleFilter" @change="searchUsers" class="filter-select">
                  <option value="">All Roles</option>
                  <option value="Mystic">Mystic</option>
                  <option value="Architect">Architect</option>
                  <option value="Guardian">Guardian</option>
                  <option value="Weaver">Weaver</option>
                  <option value="Sage">Sage</option>
                  <option value="Catalyst">Catalyst</option>
                  <option value="Harmonizer">Harmonizer</option>
                </select>
                
                <select v-model="levelFilter" @change="searchUsers" class="filter-select">
                  <option value="">All Levels</option>
                  <option value="1">Level 1+</option>
                  <option value="3">Level 3+</option>
                  <option value="5">Level 5+</option>
                  <option value="10">Level 10+</option>
                </select>
              </div>
            </div>
            
            <div v-if="loadingUsers" class="loading-state">
              <div class="loading-spinner"></div>
              <p>Discovering users...</p>
            </div>
            
            <div v-else-if="users?.length" class="users-grid">
              <div 
                v-for="user in users" 
                :key="user.id"
                class="user-card"
              >
                <div class="user-header">
                  <img 
                    :src="user.profilePictureUrl || '/default-avatar.png'" 
                    :alt="user.displayName"
                    class="user-avatar"
                  />
                  <div class="user-level">{{ user.currentLevel }}</div>
                </div>
                
                <div class="user-details">
                  <h3 class="user-name">{{ user.displayName }}</h3>
                  <div class="user-role">{{ user.selectedRole }}</div>
                  <div class="user-stats">
                    <span>{{ user.experiencePoints }} XP</span>
                    <span>{{ user.questsCompleted }} Quests</span>
                  </div>
                  <p v-if="user.bio" class="user-bio">{{ user.bio }}</p>
                </div>
                
                <div class="user-actions">
                  <button 
                    v-if="!user.isConnected"
                    class="btn btn-primary btn-sm"
                    @click="sendConnectionRequest(user.id)"
                    :disabled="connectingTo === user.id"
                  >
                    {{ connectingTo === user.id ? 'Connecting...' : 'Connect' }}
                  </button>
                  <button 
                    v-else
                    class="btn btn-secondary btn-sm"
                    disabled
                  >
                    Connected
                  </button>
                </div>
              </div>
            </div>
            
            <div v-else class="empty-state">
              <i class="fas fa-users"></i>
              <h3>No users found</h3>
              <p>Try adjusting your search filters</p>
            </div>
          </div>
        </div>

        <!-- Connections Tab -->
        <div v-if="activeTab === 'connections'" class="tab-panel">
          <div class="connections-container">
            <h2><i class="fas fa-users"></i> Your Connections</h2>
            <!-- Filters & Pagination Controls -->
            <div class="connections-filters" v-if="!connectionsStore.loading">
              <div class="filter-group">
                <label>Status:</label>
                <select v-model="statusFilterLocal" @change="applyConnectionFilters" class="filter-select sm">
                  <option value="">All</option>
                  <option value="PENDING">Pending</option>
                  <option value="ACCEPTED">Accepted</option>
                </select>
              </div>
              <div class="filter-group">
                <label>Direction:</label>
                <select v-model="directionFilterLocal" @change="applyConnectionFilters" class="filter-select sm">
                  <option value="all">All</option>
                  <option value="in">Incoming</option>
                  <option value="out">Outgoing</option>
                </select>
              </div>
              <div class="pagination-meta" v-if="connectionsStore.pagination.total">
                <span>{{ connectionsStore.pagination.total }} total</span>
                <span v-if="connectionsStore.pagination.hasMore">• more available</span>
              </div>
              <div class="actions">
                <button 
                  v-if="connectionsStore.pagination.hasMore"
                  class="btn btn-secondary btn-sm"
                  @click="loadMoreConnections"
                  :disabled="connectionsStore.loading"
                >
                  {{ connectionsStore.loading ? 'Loading...' : 'Load More' }}
                </button>
              </div>
            </div>
            
            <div v-if="connectionsStore.loading" class="loading-state">
              <div class="loading-spinner"></div>
              <p>Loading connections...</p>
            </div>
            
            <div v-else class="connections-sections">
              <!-- Pending Requests -->
              <div v-if="connections?.pending?.length" class="connection-section">
                <h3>Pending Requests ({{ connections.pending.length }})</h3>
                <div class="connections-list">
                  <div 
                    v-for="request in connections.pending" 
                    :key="request.id"
                    class="connection-item pending"
                  >
                    <img 
                      :src="request.user.profilePictureUrl || '/default-avatar.png'" 
                      :alt="request.user.displayName"
                      class="connection-avatar"
                    />
                    <div class="connection-info">
                      <div class="connection-name">{{ request.user.displayName }}</div>
                      <div class="connection-role">{{ request.user.selectedRole }} • Level {{ request.user.currentLevel }}</div>
                      <div class="connection-meta">{{ request.isOutgoing ? 'Sent' : 'Received' }} • {{ formatTimeAgo(request.createdAt) }}</div>
                    </div>
                    <div class="connection-actions">
                      <template v-if="!request.isOutgoing">
                        <button 
                          class="btn btn-primary btn-sm"
                          @click="respondToRequest(request.id, 'ACCEPT')"
                          :disabled="respondingTo === request.id"
                        >
                          Accept
                        </button>
                        <button 
                          class="btn btn-secondary btn-sm"
                          @click="respondToRequest(request.id, 'DECLINE')"
                          :disabled="respondingTo === request.id"
                        >
                          Decline
                        </button>
                      </template>
                      <template v-else>
                        <button
                          class="btn btn-secondary btn-sm"
                          @click="cancelConnection(request.id)"
                          :disabled="respondingTo === request.id"
                        >
                          Cancel
                        </button>
                      </template>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Accepted Connections -->
              <div v-if="connections?.accepted?.length" class="connection-section">
                <h3>Connected ({{ connections.accepted.length }})</h3>
                <div class="connections-list">
                  <div 
                    v-for="connection in connections.accepted" 
                    :key="connection.id"
                    class="connection-item accepted"
                  >
                    <img 
                      :src="connection.user.profilePictureUrl || '/default-avatar.png'" 
                      :alt="connection.user.displayName"
                      class="connection-avatar"
                    />
                    <div class="connection-info">
                      <div class="connection-name">{{ connection.user.displayName }}</div>
                      <div class="connection-role">{{ connection.user.selectedRole }} • Level {{ connection.user.currentLevel }}</div>
                      <div class="connection-meta">Connected {{ formatTimeAgo(connection.respondedAt || connection.createdAt) }}</div>
                    </div>
                    <div class="connection-type">
                      <span class="type-badge">{{ connection.type }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Empty State -->
              <div v-if="!connections?.pending?.length && !connections?.accepted?.length" class="empty-state">
                <i class="fas fa-user-friends"></i>
                <h3>No connections yet</h3>
                <p>Start connecting with others in the Discover tab!</p>
                <button class="btn btn-primary" @click="activeTab = 'discover'">
                  Discover Users
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Projects Tab -->
        <div v-if="activeTab === 'projects'" class="tab-panel">
          <div class="projects-container">
            <div class="projects-header">
              <h2><i class="fas fa-project-diagram"></i> Community Projects</h2>
              <button class="btn btn-primary" @click="showCreateProject = true">
                <i class="fas fa-plus"></i> Create Project
              </button>
            </div>
            
            <div v-if="loadingProjects" class="loading-state">
              <div class="loading-spinner"></div>
              <p>Loading projects...</p>
            </div>
            
            <div v-else-if="projects?.length" class="projects-grid">
              <div 
                v-for="project in projects" 
                :key="project.id"
                class="project-card"
              >
                <div class="project-header">
                  <h3 class="project-title">{{ project.title }}</h3>
                  <span class="project-status">{{ project.status }}</span>
                </div>
                
                <p class="project-description">{{ project.description }}</p>
                
                <div class="project-contributors">
                  <div class="contributors-label">Contributors ({{ project.contributorCount }})</div>
                  <div class="contributors-list">
                    <div 
                      v-for="contributor in project.contributors.slice(0, 3)" 
                      :key="contributor.id"
                      class="contributor-avatar"
                      :title="contributor.displayName + ' (' + contributor.role + ')'"
                    >
                      <img 
                        :src="contributor.profilePictureUrl || '/default-avatar.png'" 
                        :alt="contributor.displayName"
                      />
                    </div>
                    <div v-if="project.contributorCount > 3" class="more-contributors">
                      +{{ project.contributorCount - 3 }}
                    </div>
                  </div>
                </div>
                
                <div class="project-actions">
                  <button class="btn btn-primary btn-sm">
                    View Project
                  </button>
                  <button class="btn btn-secondary btn-sm">
                    Join Project
                  </button>
                </div>
              </div>
            </div>
            
            <div v-else class="empty-state">
              <i class="fas fa-project-diagram"></i>
              <h3>No projects yet</h3>
              <p>Be the first to create a community project!</p>
              <button class="btn btn-primary" @click="showCreateProject = true">
                Create Project
              </button>
            </div>
          </div>
        </div>

        <!-- Leaderboard Tab -->
        <div v-if="activeTab === 'leaderboard'" class="tab-panel">
          <div class="leaderboard-container">
            <div class="leaderboard-header">
              <h2><i class="fas fa-trophy"></i> Leaderboard</h2>
              
              <div class="leaderboard-filters">
                <button 
                  v-for="type in leaderboardTypes" 
                  :key="type.id"
                  class="filter-btn"
                  :class="{ active: leaderboardType === type.id }"
                  @click="changeLeaderboardType(type.id)"
                >
                  {{ type.label }}
                </button>
              </div>
            </div>
            
            <div v-if="loadingLeaderboard" class="loading-state">
              <div class="loading-spinner"></div>
              <p>Loading leaderboard...</p>
            </div>
            
            <div v-else-if="leaderboard?.length" class="leaderboard-list">
              <div 
                v-for="(user, index) in leaderboard" 
                :key="user.id"
                class="leaderboard-item"
                :class="{ 'top-three': index < 3 }"
              >
                <div class="rank">
                  <span v-if="index === 0" class="rank-icon gold">🥇</span>
                  <span v-else-if="index === 1" class="rank-icon silver">🥈</span>
                  <span v-else-if="index === 2" class="rank-icon bronze">🥉</span>
                  <span v-else class="rank-number">{{ index + 1 }}</span>
                </div>
                
                <img 
                  :src="user.profilePictureUrl || '/default-avatar.png'" 
                  :alt="user.displayName"
                  class="user-avatar"
                />
                
                <div class="user-info">
                  <div class="user-name">{{ user.displayName }}</div>
                  <div class="user-role">{{ user.selectedRole }}</div>
                </div>
                
                <div class="user-score">
                  <div class="score-value">
                    {{ getScoreValue(user) }}
                  </div>
                  <div class="score-label">
                    {{ getScoreLabel() }}
                  </div>
                </div>
              </div>
            </div>
            
            <div v-else class="empty-state">
              <i class="fas fa-trophy"></i>
              <h3>No leaderboard data</h3>
              <p>Complete quests to appear on the leaderboard!</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Create Project Modal -->
    <div v-if="showCreateProject" class="modal-overlay" @click="showCreateProject = false">
      <div class="modal-content" @click.stop>
        <h3>Create Community Project</h3>
        <form @submit.prevent="createProject" class="project-form">
          <div class="form-group">
            <label for="projectTitle">Project Title</label>
            <input 
              id="projectTitle"
              v-model="projectForm.title" 
              type="text" 
              class="form-input"
              placeholder="Project name"
              required
            />
          </div>
          
          <div class="form-group">
            <label for="projectDescription">Description</label>
            <textarea 
              id="projectDescription"
              v-model="projectForm.description" 
              class="form-textarea"
              rows="4"
              placeholder="Describe your project..."
              required
            ></textarea>
          </div>

          <div class="modal-actions">
            <button type="submit" class="btn btn-primary" :disabled="creatingProject">
              {{ creatingProject ? 'Creating...' : 'Create Project' }}
            </button>
            <button type="button" class="btn btn-secondary" @click="showCreateProject = false">
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import communityService from '@/services/community.service'
import { useToastStore } from '@/stores/toast'
import { useConnectionsStore } from '@/stores/connections'

const userStore = useUserStore()

// Reactive data
const activeTab = ref('feed')
const loading = ref(true)
const loadingUsers = ref(false)
// Connections handled by dedicated store now
const connectionsStore = useConnectionsStore()
const loadingProjects = ref(false)
const loadingLeaderboard = ref(false)
const connectingTo = ref<string | null>(null)
const connectionFeedback = ref<string | null>(null)
const toast = useToastStore()
const respondingTo = ref<string | null>(null)
const creatingProject = ref(false)
const showCreateProject = ref(false)

const feed = ref<any>(null)
const users = ref<any[]>([])
// Derived connections shape for template compatibility (reactive computed)
const connections = computed(() => ({ pending: connectionsStore.pending, accepted: connectionsStore.accepted }))
// Local filter state
const statusFilterLocal = ref<string>('')
const directionFilterLocal = ref<'all' | 'in' | 'out'>('all')
const projects = ref<any[]>([])
const leaderboard = ref<any[]>([])
const stats = ref<any>(null)

// Search and filters
const searchQuery = ref('')
const roleFilter = ref('')
const levelFilter = ref('')
const leaderboardType = ref('experience')

// Tab configuration
const tabs = [
  { id: 'feed', label: 'Feed', icon: 'fas fa-stream' },
  { id: 'discover', label: 'Discover', icon: 'fas fa-compass' },
  { id: 'connections', label: 'Connections', icon: 'fas fa-users' },
  { id: 'projects', label: 'Projects', icon: 'fas fa-project-diagram' },
  { id: 'leaderboard', label: 'Leaderboard', icon: 'fas fa-trophy' }
]

const leaderboardTypes = [
  { id: 'experience', label: 'Experience' },
  { id: 'quests', label: 'Quests' },
  { id: 'level', label: 'Level' }
]

// Project form
const projectForm = reactive({
  title: '',
  description: ''
})

// Methods
// Normalize backend feed items into a simple display model the UI expects
const adaptFeedItems = (items: any[] = []) => {
  return items.map((item) => {
    const base = { timestamp: item.timestamp }
    if (item.type === 'quest_completed' && item.user && item.quest) {
      return {
        ...base,
        user: {
          displayName: item.user.displayName,
          profilePictureUrl: item.user.avatarUrl,
          selectedRole: item.user.role,
          currentLevel: item.user.level
        },
        quest: {
          title: item.quest.title,
          type: 'Quest',
          experienceReward: item.quest.xp || 0
        }
      }
    }
    if (item.type === 'project_created' && item.user && item.project) {
      return {
        ...base,
        user: {
          displayName: item.user.displayName,
          profilePictureUrl: item.user.avatarUrl,
          selectedRole: item.user.role,
          currentLevel: item.user.level
        },
        quest: {
          title: `created project: ${item.project.title}`,
          type: 'Project',
          experienceReward: 0
        }
      }
    }
    if (item.type === 'connection_accepted' && item.user && item.connection) {
      const other = item.connection?.to === item.user?.id ? item.connection?.from : item.connection?.to
      return {
        ...base,
        user: {
          displayName: item.user.displayName,
          profilePictureUrl: item.user.avatarUrl,
          selectedRole: item.user.role,
          currentLevel: item.user.level
        },
        quest: {
          title: `connected with ${other}`,
          type: 'Connection',
          experienceReward: 0
        }
      }
    }
    return {
      ...base,
      user: item.user || {},
      quest: { title: item.project?.title || 'Activity', type: item.type, experienceReward: 0 }
    }
  })
}

const loadFeed = async () => {
  try {
    loading.value = true
    const result = await communityService.getCommunityFeed(0, 20)
    if (result.success) {
      const rawItems = result.data?.items || []
      feed.value = { activities: adaptFeedItems(rawItems) }
    } else {
      console.error('Failed to load feed:', result.error)
    }
  } catch (error) {
    console.error('Failed to load feed:', error)
  } finally {
    loading.value = false
  }
}

const searchUsers = async () => {
  try {
    loadingUsers.value = true
    const result = await communityService.getUserDirectory(
      searchQuery.value || undefined,
      roleFilter.value || undefined,
      levelFilter.value ? parseInt(levelFilter.value) : undefined,
      0,
      20
    )
    if (result.success) {
      users.value = result.data?.users || []
    } else {
      console.error('Failed to search users:', result.error)
    }
  } catch (error) {
    console.error('Failed to search users:', error)
  } finally {
    loadingUsers.value = false
  }
}

const loadConnections = async () => { await connectionsStore.loadConnections() }
const loadMoreConnections = async () => { await connectionsStore.loadNextPage() }
const applyConnectionFilters = async () => {
  await connectionsStore.setFilters({
    status: statusFilterLocal.value ? statusFilterLocal.value as 'PENDING' | 'ACCEPTED' | 'DECLINED' : undefined,
    direction: directionFilterLocal.value
  })
}

const loadProjects = async () => {
  try {
    loadingProjects.value = true
    const result = await communityService.getCommunityProjects(false, 0, 20)
    if (result.success) {
  projects.value = result.data?.projects || []
    }
  } catch (error) {
    console.error('Failed to load projects:', error)
  } finally {
    loadingProjects.value = false
  }
}

const loadLeaderboard = async () => {
  try {
    loadingLeaderboard.value = true
    // Map filters to backend 'window' param
    let window: 'week' | 'month' | 'all' | undefined = 'all'
    if (leaderboardType.value === 'experience') {
      window = 'all'
    }
    const result = await communityService.getLeaderboard(window)
    if (result.success) {
      const entries = (result.data as any[]) || []
      // Adapt to flat shape expected by template
      leaderboard.value = entries.map((e: any) => ({
        id: e?.user?.id || e?.id,
        displayName: e?.user?.displayName || e?.displayName,
        profilePictureUrl: e?.user?.avatarUrl || e?.avatarUrl,
        selectedRole: e?.user?.role || e?.role,
        currentLevel: e?.user?.level ?? e?.level ?? 0,
        experiencePoints: e?.xp ?? e?.experiencePoints ?? 0,
        questsCompleted: e?.completed ?? e?.questsCompleted ?? 0
      }))
    } else {
      console.error('Failed to load leaderboard:', result.error)
    }
  } catch (error) {
    console.error('Failed to load leaderboard:', error)
  } finally {
    loadingLeaderboard.value = false
  }
}

const loadStats = async () => {
  try {
    const result = await communityService.getCommunityStats()
    if (result.success) {
      stats.value = result.data
    }
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
}

const sendConnectionRequest = async (toUserId: string) => {
  try {
    connectingTo.value = toUserId
    const ok = await connectionsStore.sendRequest(toUserId, 'collaborator')
    if (ok) {
      const user = users.value.find(u => u.id === toUserId)
      if (user) user.isConnected = true
      connectionFeedback.value = 'Connection request sent.'
  // List auto-reactive via computed
    } else {
      connectionFeedback.value = 'Failed to send connection request.'
      console.error('Failed to send connection request (store returned false)')
    }
  } catch (e) {
    connectionFeedback.value = 'Failed to send connection request.'
    console.error('Failed to send connection request:', e)
  } finally {
    connectingTo.value = null
    setTimeout(() => { if (connectionFeedback.value) connectionFeedback.value = null }, 4000)
  }
}

const respondToRequest = async (requestId: string, action: string) => {
  try {
    respondingTo.value = requestId
    const actionTyped = action.toLowerCase() as 'accept' | 'decline'
    const ok = await connectionsStore.respond(requestId, actionTyped)
    if (ok) {
  // reactive
      const actionText = actionTyped === 'accept' ? 'accepted' : 'declined'
  toast.push(`Connection request ${actionText}!`, 'success')
    } else {
      console.error('Failed to respond to request (store returned false)')
  toast.push('Failed to respond to request. Please try again.', 'error')
    }
  } catch (error) {
    console.error('Failed to respond to request:', error)
  toast.push('Failed to respond to request. Please try again.', 'error')
  } finally {
    respondingTo.value = null
  }
}

const cancelConnection = async (connectionId: string) => {
  try {
    respondingTo.value = connectionId
    const ok = await connectionsStore.cancel(connectionId)
    if (ok) {
  // reactive
  toast.push('Connection request cancelled', 'info')
    } else {
      console.error('Failed to cancel connection (store returned false)')
  toast.push('Failed to cancel request', 'error')
    }
  } catch (e) {
    console.error('Failed to cancel connection:', e)
  toast.push('Failed to cancel request', 'error')
  } finally {
    respondingTo.value = null
  }
}

const createProject = async () => {
  try {
    creatingProject.value = true
    const projectData = {
      title: projectForm.title,
      description: projectForm.description,
      tags: [],
      visibility: 'public' as const
    }
    const result = await communityService.createProject(projectData)
    
    if (result.success) {
      // Reset form and close modal
      projectForm.title = ''
      projectForm.description = ''
      showCreateProject.value = false
      
      // Reload projects
      loadProjects()
      
  toast.push('Project created successfully!', 'success')
    } else {
      console.error('Failed to create project:', result.error)
  toast.push('Failed to create project. Please try again.', 'error')
    }
  } catch (error) {
    console.error('Failed to create project:', error)
  toast.push('Failed to create project. Please try again.', 'error')
  } finally {
    creatingProject.value = false
  }
}

const changeLeaderboardType = (type: string) => {
  leaderboardType.value = type
  loadLeaderboard()
}

const getScoreValue = (user: any) => {
  switch (leaderboardType.value) {
    case 'quests':
      return user.questsCompleted
    case 'level':
      return user.currentLevel
    default:
      return user.experiencePoints
  }
}

const getScoreLabel = () => {
  switch (leaderboardType.value) {
    case 'quests':
      return 'Quests'
    case 'level':
      return 'Level'
    default:
      return 'XP'
  }
}

const formatTimeAgo = (dateString: string) => {
  if (!dateString) return 'Unknown'
  
  const now = new Date()
  const date = new Date(dateString)
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 60) {
    return `${minutes}m ago`
  } else if (hours < 24) {
    return `${hours}h ago`
  } else {
    return `${days}d ago`
  }
}

// Load initial data based on active tab
const loadTabData = () => {
  switch (activeTab.value) {
    case 'feed':
      loadFeed()
      break
    case 'discover':
      searchUsers()
      break
    case 'connections':
      loadConnections()
      break
    case 'projects':
      loadProjects()
      break
    case 'leaderboard':
      loadLeaderboard()
      break
  }
}

// Watch for tab changes
const changeTab = (tabId: string) => {
  activeTab.value = tabId
  loadTabData()
}

// Load data on mount
onMounted(() => {
  loadStats()
  loadTabData()
})
</script>

<style scoped>
.community-view {
  min-height: 100vh;
  padding: 2rem 0;
  background: var(--background-dark);
}

/* Header */
.community-header {
  text-align: center;
  margin-bottom: 3rem;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 600;
  margin-bottom: 1rem;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: 1.125rem;
  color: var(--text-muted);
  margin-bottom: 2rem;
}

.community-stats {
  display: flex;
  justify-content: center;
  gap: 2rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: var(--card-bg);
  padding: 1.5rem;
  border-radius: 1rem;
  border: 1px solid var(--border-color);
  text-align: center;
  min-width: 120px;
}

.stat-value {
  display: block;
  font-size: 2rem;
  font-weight: bold;
  color: var(--accent-purple);
  margin-bottom: 0.5rem;
}

.stat-label {
  color: var(--text-muted);
  font-size: 0.875rem;
}

/* Tabs */
.community-tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  border-bottom: 1px solid var(--border-color);
  justify-content: center;
  flex-wrap: wrap;
}

.tab-button {
  background: none;
  border: none;
  color: var(--text-muted);
  padding: 1rem 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 2px solid transparent;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.tab-button:hover {
  color: var(--text-light);
}

.tab-button.active {
  color: var(--accent-purple);
  border-bottom-color: var(--accent-purple);
}

/* Tab Content */
.tab-content {
  min-height: 500px;
}

.tab-panel h2 {
  color: var(--text-light);
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.tab-panel h2 i {
  color: var(--accent-purple);
}

/* Loading States */
.loading-state {
  text-align: center;
  padding: 3rem;
  color: var(--text-muted);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--border-color);
  border-top: 3px solid var(--accent-purple);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Empty States */
.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--text-muted);
}

.empty-state i {
  font-size: 3rem;
  color: var(--accent-purple);
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-state h3 {
  color: var(--text-light);
  margin-bottom: 0.5rem;
}

/* Feed */
.feed-container,
.discover-container,
.connections-container,
.projects-container,
.leaderboard-container {
  max-width: 800px;
  margin: 0 auto;
}



.activity-item {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 1rem;
  padding: 1.5rem;
  margin-bottom: 1rem;
}

.activity-user {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.user-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.user-info {
  flex: 1;
}

.user-name {
  color: var(--text-light);
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.user-role {
  color: var(--text-muted);
  font-size: 0.875rem;
}

.activity-content {
  padding-left: 61px; /* Account for avatar + gap */
}

.activity-text {
  color: var(--text-light);
  margin-bottom: 0.5rem;
}

.action-text {
  color: var(--text-muted);
}

.quest-title {
  color: var(--accent-purple);
  font-weight: 500;
  margin-left: 0.5rem;
}

.activity-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.875rem;
}

.quest-type {
  color: var(--text-muted);
  background: var(--background-darker);
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
}

.experience-reward {
  color: var(--accent-purple);
  font-weight: 500;
}

.activity-time {
  color: var(--text-muted);
}

/* Discover */
.discover-header {
  margin-bottom: 2rem;
}

.search-filters {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
  justify-content: center;
  margin-top: 1rem;
}

.search-input {
  position: relative;
  flex: 1;
  max-width: 300px;
}

.search-input i {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-muted);
}

.search-input input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 2.5rem;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 0.5rem;
  color: var(--text-light);
}

.filter-select {
  padding: 0.75rem;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 0.5rem;
  color: var(--text-light);
  min-width: 120px;
}

.users-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

.user-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 1rem;
  padding: 1.5rem;
  text-align: center;
  transition: transform 0.3s ease, border-color 0.3s ease;
}

.user-card:hover {
  transform: translateY(-2px);
  border-color: var(--accent-purple);
}

.user-header {
  position: relative;
  margin-bottom: 1rem;
}

.user-card .user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--accent-purple);
}

.user-level {
  position: absolute;
  bottom: -5px;
  right: 50%;
  transform: translateX(50%);
  background: var(--gradient-primary);
  color: white;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.875rem;
  border: 2px solid var(--card-bg);
}

.user-details {
  margin-bottom: 1rem;
}

.user-card .user-name {
  color: var(--text-light);
  font-size: 1.125rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.user-card .user-role {
  color: var(--accent-purple);
  margin-bottom: 0.75rem;
}

.user-stats {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 0.75rem;
  font-size: 0.875rem;
  color: var(--text-muted);
}

.user-bio {
  color: var(--text-light);
  font-size: 0.875rem;
  line-height: 1.4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.user-actions {
  margin-top: 1rem;
}

/* Connections */


.connection-section {
  margin-bottom: 2rem;
}

.connection-section h3 {
  color: var(--text-light);
  margin-bottom: 1rem;
  font-size: 1.25rem;
}



.connection-item {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 1rem;
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.connection-item.pending {
  border-left: 4px solid var(--accent-blue);
}

.connection-item.accepted {
  border-left: 4px solid var(--accent-purple);
}

.connection-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
}

.connection-info {
  flex: 1;
}

.connection-name {
  color: var(--text-light);
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.connection-role {
  color: var(--text-muted);
  font-size: 0.875rem;
  margin-bottom: 0.25rem;
}

.connection-meta {
  color: var(--text-muted);
  font-size: 0.75rem;
}

.connection-actions {
  display: flex;
  gap: 0.5rem;
}

.connection-type {
  display: flex;
  align-items: center;
}

.type-badge {
  background: var(--accent-purple);
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  font-weight: 500;
}

/* Connections Filters */
.connections-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem 1rem;
  align-items: flex-end;
  padding: 0.75rem 1rem 1rem;
  margin: 0 0 1.25rem;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 0.75rem;
}

.connections-filters .filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  min-width: 120px;
}

.connections-filters .filter-group label {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-weight: 600;
  color: var(--text-muted);
}

.connections-filters .filter-select.sm {
  padding: 0.55rem 0.65rem;
  font-size: 0.8rem;
  min-width: 110px;
}

.connections-filters .pagination-meta {
  display: flex;
  gap: 0.5rem;
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-left: auto;
  align-items: center;
}

.connections-filters .actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.connections-filters button.btn-sm {
  padding: 0.45rem 0.9rem;
  font-size: 0.75rem;
}

.connections-filters select.filter-select.sm:focus-visible {
  outline: 2px solid var(--accent-purple);
  outline-offset: 2px;
}

@media (max-width: 640px) {
  .connections-filters {
    flex-direction: column;
    align-items: stretch;
  }
  .connections-filters .pagination-meta {
    margin-left: 0;
  }
  .connections-filters .actions {
    justify-content: flex-start;
  }
}

/* Projects */
.projects-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
}

.project-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 1rem;
  padding: 1.5rem;
  transition: transform 0.3s ease, border-color 0.3s ease;
}

.project-card:hover {
  transform: translateY(-2px);
  border-color: var(--accent-purple);
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.project-title {
  color: var(--text-light);
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
  flex: 1;
}

.project-status {
  background: var(--accent-purple);
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  font-weight: 500;
}

.project-description {
  color: var(--text-light);
  margin-bottom: 1.5rem;
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
}

.project-contributors {
  margin-bottom: 1.5rem;
}

.contributors-label {
  color: var(--text-muted);
  font-size: 0.875rem;
  margin-bottom: 0.5rem;
}

.contributors-list {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.contributor-avatar {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid var(--border-color);
}

.contributor-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.more-contributors {
  background: var(--background-darker);
  color: var(--text-muted);
  width: 35px;
  height: 35px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  border: 2px solid var(--border-color);
}

.project-actions {
  display: flex;
  gap: 0.5rem;
}

/* Leaderboard */
.leaderboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.leaderboard-filters {
  display: flex;
  gap: 0.5rem;
}

.filter-btn {
  background: var(--background-darker);
  border: 1px solid var(--border-color);
  color: var(--text-muted);
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.filter-btn:hover {
  color: var(--text-light);
  border-color: var(--accent-purple);
}

.filter-btn.active {
  background: var(--accent-purple);
  color: white;
  border-color: var(--accent-purple);
}



.leaderboard-item {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 1rem;
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
  transition: transform 0.3s ease;
}

.leaderboard-item:hover {
  transform: translateX(5px);
}

.leaderboard-item.top-three {
  border-color: var(--accent-purple);
  background: linear-gradient(135deg, var(--card-bg), rgba(139, 69, 255, 0.1));
}

.rank {
  width: 40px;
  text-align: center;
}

.rank-icon {
  font-size: 1.5rem;
}

.rank-number {
  color: var(--text-light);
  font-weight: bold;
  font-size: 1.25rem;
}

.leaderboard-item .user-avatar {
  width: 60px;
  height: 60px;
}

.leaderboard-item .user-info {
  flex: 1;
}

.leaderboard-item .user-name {
  font-size: 1.125rem;
  font-weight: 600;
}

.leaderboard-item .user-role {
  color: var(--accent-purple);
}

.user-score {
  text-align: center;
}

.score-value {
  color: var(--text-light);
  font-size: 1.5rem;
  font-weight: bold;
  display: block;
}

.score-label {
  color: var(--text-muted);
  font-size: 0.875rem;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: var(--card-bg);
  padding: 2rem;
  border-radius: 1rem;
  border: 1px solid var(--border-color);
  max-width: 500px;
  width: 90%;
}

.modal-content h3 {
  color: var(--text-light);
  margin-bottom: 1.5rem;
}



.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  color: var(--text-light);
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 0.75rem;
  background: var(--background-darker);
  border: 1px solid var(--border-color);
  border-radius: 0.5rem;
  color: var(--text-light);
  font-size: 1rem;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--accent-purple);
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}

/* Responsive */
@media (max-width: 768px) {
  .community-stats {
    flex-direction: column;
    align-items: center;
  }
  
  .community-tabs {
    justify-content: flex-start;
    overflow-x: auto;
    padding-bottom: 0.5rem;
  }
  
  .search-filters {
    flex-direction: column;
  }
  
  .search-input {
    max-width: none;
  }
  
  .users-grid,
  .projects-grid {
    grid-template-columns: 1fr;
  }
  
  .projects-header,
  .leaderboard-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .leaderboard-filters {
    justify-content: center;
  }
}
</style>
