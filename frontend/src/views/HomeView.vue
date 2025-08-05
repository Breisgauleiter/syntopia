<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { RouterLink } from 'vue-router'

const userStore = useUserStore()
const isLoaded = ref(false)

// Sacred Geometry Animation Component
const geometryCanvas = ref<HTMLCanvasElement>()

onMounted(() => {
  isLoaded.value = true
  
  // Initialize P5.js Sacred Geometry background
  if (window.p5 && geometryCanvas.value) {
    new window.p5((p: any) => {
      let angle = 0
      
      p.setup = () => {
        p.createCanvas(800, 600).parent(geometryCanvas.value)
        p.colorMode(p.HSB, 360, 100, 100, 100)
      }
      
      p.draw = () => {
        p.clear()
        p.translate(p.width / 2, p.height / 2)
        
        // Draw Flower of Life pattern
        for (let i = 0; i < 6; i++) {
          p.push()
          p.rotate(p.radians(60 * i + angle))
          p.noFill()
          p.stroke(240 + i * 20, 60, 80, 30)
          p.strokeWeight(1.5)
          p.circle(80, 0, 160)
          p.pop()
        }
        
        // Center circle
        p.stroke(270, 80, 90, 60)
        p.strokeWeight(2)
        p.circle(0, 0, 160)
        
        angle += 0.5
      }
    })
  }
})

const syntopiaRoles = [
  {
    name: "Sacred Mathematician",
    description: "Explore the numerical patterns underlying creation",
    icon: "∞",
    color: "text-blue-400"
  },
  {
    name: "Digital Architect", 
    description: "Build the technological foundations of consciousness",
    icon: "⚡",
    color: "text-purple-400"
  },
  {
    name: "Community Weaver",
    description: "Connect souls and foster conscious collaboration",
    icon: "🌐",
    color: "text-green-400"
  },
  {
    name: "Consciousness Explorer",
    description: "Navigate the depths of awareness and meaning",
    icon: "🧠",
    color: "text-pink-400"
  },
  {
    name: "Quantum Developer",
    description: "Code at the intersection of mind and matter",
    icon: "⚛️",
    color: "text-cyan-400"
  },
  {
    name: "Wisdom Keeper",
    description: "Preserve and share ancient knowledge for the future",
    icon: "📚",
    color: "text-yellow-400"
  },
  {
    name: "Witness",
    description: "Observe and validate the journey of others",
    icon: "👁️",
    color: "text-indigo-400"
  }
]
</script>

<template>
  <div class="home-view">
    <!-- Hero Section -->
    <section class="hero-section sacred-geometry">
      <div class="container">
        <div class="hero-content" :class="{ 'animate-fade-in': isLoaded }">
          <div class="hero-text">
            <h1 class="hero-title">
              Welcome to <span class="text-primary">Syntopia</span>
            </h1>
            <p class="hero-subtitle">
              Explore the divine mathematics of creation through interactive sacred geometry visualizations
            </p>
            <p class="hero-description">
              A consciousness platform where ancient wisdom meets modern technology, 
              fostering collaboration through the universal language of sacred patterns.
            </p>
            
            <div class="hero-actions">
              <RouterLink v-if="!userStore.isAuthenticated" to="/register" class="btn btn-primary btn-lg">
                Begin Your Journey
              </RouterLink>
              <RouterLink v-else to="/quests" class="btn btn-primary btn-lg">
                Continue Quest (Level {{ userStore.userLevel }})
              </RouterLink>
              <RouterLink to="/geometry" class="btn btn-glass">
                Explore Patterns
              </RouterLink>
            </div>
          </div>
          
          <div class="hero-visual">
            <div class="geometry-container">
              <canvas ref="geometryCanvas" class="geometry-canvas"></canvas>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="features-section">
      <div class="container">
        <h2 class="section-title text-center">The Syntopia Experience</h2>
        
        <div class="features-grid">
          <div class="feature-card card">
            <div class="feature-icon">🎯</div>
            <h3>Gamified Learning</h3>
            <p>Progress through 25 Syntopia Contribution Levels (SCL) with meaningful quests and challenges.</p>
          </div>
          
          <div class="feature-card card">
            <div class="feature-icon">🔗</div>
            <h3>GitHub Integration</h3>
            <p>From Level 4+, contribute to real open-source projects and turn GitHub issues into quests.</p>
          </div>
          
          <div class="feature-card card">
            <div class="feature-icon">🌸</div>
            <h3>Sacred Geometry</h3>
            <p>Interactive visualizations of the Flower of Life, Metatron's Cube, and other divine patterns.</p>
          </div>
          
          <div class="feature-card card">
            <div class="feature-icon">🤝</div>
            <h3>Conscious Collaboration</h3>
            <p>Connect with like-minded individuals through the SYN principles of collective intelligence.</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Roles Section -->
    <section class="roles-section">
      <div class="container">
        <h2 class="section-title text-center">Choose Your Sacred Role</h2>
        <p class="section-subtitle text-center">
          Each role offers a unique path through the Syntopia journey
        </p>
        
        <div class="roles-grid">
          <div 
            v-for="role in syntopiaRoles" 
            :key="role.name"
            class="role-card card"
          >
            <div class="role-icon" :class="role.color">{{ role.icon }}</div>
            <h3 class="role-name">{{ role.name }}</h3>
            <p class="role-description">{{ role.description }}</p>
          </div>
        </div>
        
        <div class="text-center">
          <RouterLink 
            v-if="!userStore.isAuthenticated" 
            to="/register" 
            class="btn btn-primary"
          >
            Start Your Journey
          </RouterLink>
          <RouterLink 
            v-else-if="!userStore.userRole || userStore.userRole === 'None'" 
            to="/onboarding" 
            class="btn btn-primary"
          >
            Select Your Role
          </RouterLink>
        </div>
      </div>
    </section>

    <!-- Community Stats -->
    <section v-if="userStore.isAuthenticated" class="stats-section">
      <div class="container">
        <div class="stats-grid">
          <div class="stat-card glass">
            <div class="stat-number">{{ userStore.user?.currentLevel }}</div>
            <div class="stat-label">Your Level</div>
          </div>
          
          <div class="stat-card glass">
            <div class="stat-number">{{ userStore.user?.experiencePoints.toLocaleString() }}</div>
            <div class="stat-label">Experience Points</div>
          </div>
          
          <div class="stat-card glass">
            <div class="stat-number">{{ userStore.user?.questsCompleted }}</div>
            <div class="stat-label">Quests Completed</div>
          </div>
          
          <div class="stat-card glass">
            <div class="stat-number">{{ userStore.userRole }}</div>
            <div class="stat-label">Sacred Role</div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-view {
  min-height: 100vh;
}

.hero-section {
  padding: 4rem 0 6rem;
  position: relative;
}

.hero-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4rem;
  align-items: center;
  min-height: 60vh;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 700;
  line-height: 1.1;
  margin-bottom: 1.5rem;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 1.25rem;
  color: var(--color-text);
  margin-bottom: 1rem;
  font-weight: 500;
}

.hero-description {
  font-size: 1rem;
  color: var(--color-text-muted);
  margin-bottom: 2rem;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.btn-lg {
  padding: 1rem 2rem;
  font-size: 1.125rem;
}

.geometry-container {
  position: relative;
  border-radius: var(--radius-xl);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.02);
  backdrop-filter: var(--blur-sm);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.geometry-canvas {
  width: 100%;
  height: auto;
  display: block;
}

.section-title {
  font-size: 2.5rem;
  font-weight: 600;
  margin-bottom: 1rem;
  background: var(--gradient-accent);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.section-subtitle {
  font-size: 1.125rem;
  color: var(--color-text-muted);
  margin-bottom: 3rem;
}

.features-section,
.roles-section {
  padding: 4rem 0;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.feature-card {
  text-align: center;
  padding: 2rem;
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.feature-card h3 {
  font-size: 1.25rem;
  margin-bottom: 1rem;
  color: var(--color-text);
}

.roles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-bottom: 3rem;
}

.role-card {
  text-align: center;
  padding: 1.5rem;
  transition: all var(--transition-normal);
}

.role-card:hover {
  transform: translateY(-4px);
}

.role-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.role-name {
  font-size: 1.125rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  color: var(--color-text);
}

.role-description {
  color: var(--color-text-muted);
  line-height: 1.5;
}

.stats-section {
  padding: 3rem 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 2rem;
}

.stat-card {
  text-align: center;
  padding: 2rem 1rem;
  border-radius: var(--radius-lg);
}

.stat-number {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: 0.5rem;
}

.stat-label {
  color: var(--color-text-muted);
  font-weight: 500;
}

/* Responsive Design */
@media (max-width: 768px) {
  .hero-content {
    grid-template-columns: 1fr;
    gap: 2rem;
    text-align: center;
  }
  
  .hero-title {
    font-size: 2.5rem;
  }
  
  .section-title {
    font-size: 2rem;
  }
  
  .roles-grid {
    grid-template-columns: 1fr;
  }
  
  .hero-actions {
    justify-content: center;
  }
}
</style>
