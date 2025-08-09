<template>
  <div class="achievement-system">
    <!-- Achievement Notifications -->
    <Transition name="achievement-notification" appear>
      <div v-if="showNotification" class="achievement-notification">
        <div class="achievement-icon">🏆</div>
        <div class="achievement-content">
          <h3>{{ currentAchievement.title }}</h3>
          <p>{{ currentAchievement.description }}</p>
          <div class="achievement-reward">
            +{{ currentAchievement.xpBonus }} XP Bonus!
          </div>
        </div>
        <div class="achievement-effects">
          <div v-for="n in 8" :key="n" class="sparkle" :style="sparkleStyle(n)"></div>
        </div>
      </div>
    </Transition>

    <!-- Quest Streak Counter -->
    <div class="quest-streak" v-if="currentStreak > 0">
      <div class="streak-icon">🔥</div>
      <div class="streak-content">
        <span class="streak-number">{{ currentStreak }}</span>
        <span class="streak-label">Quest Streak</span>
      </div>
      <div class="streak-progress">
        <div 
          class="streak-bar" 
          :style="{ width: `${(currentStreak % 7) * (100/7)}%` }"
        ></div>
      </div>
    </div>

    <!-- Level Progress -->
    <div class="level-progress">
      <div class="level-info">
        <span class="current-level">Level {{ userLevel }}</span>
        <span class="xp-info">{{ currentXP }} / {{ xpToNextLevel }} XP</span>
      </div>
      <div class="progress-bar">
        <div 
          class="progress-fill" 
          :style="{ width: `${levelProgress}%` }"
        >
          <div class="progress-glow"></div>
        </div>
        <div class="progress-markers">
          <div 
            v-for="marker in 5" 
            :key="marker"
            class="progress-marker"
            :style="{ left: `${marker * 20}%` }"
          ></div>
        </div>
      </div>
    </div>

    <!-- Achievement Gallery -->
    <div class="achievement-gallery" v-if="showGallery">
      <h3>Sacred Achievements</h3>
      <div class="achievements-grid">
        <div 
          v-for="achievement in achievements"
          :key="achievement.id"
          class="achievement-badge"
          :class="{ 'unlocked': achievement.unlocked }"
          @click="selectAchievement(achievement)"
        >
          <div class="badge-icon">{{ achievement.icon }}</div>
          <div class="badge-title">{{ achievement.title }}</div>
          <div class="badge-progress" v-if="!achievement.unlocked">
            {{ achievement.progress }} / {{ achievement.required }}
          </div>
        </div>
      </div>
    </div>

    <!-- Sacred Geometry Level-Up Effect -->
    <Transition name="level-up" appear>
      <div v-if="showLevelUpEffect" class="level-up-effect">
        <div class="geometry-burst">
          <svg class="burst-pattern" viewBox="0 0 300 300">
            <!-- Flower of Life Pattern -->
            <g class="flower-of-life">
              <circle
                v-for="(circle, index) in flowerOfLifeCircles"
                :key="index"
                :cx="circle.x"
                :cy="circle.y"
                r="40"
                fill="none"
                stroke="#f59e0b"
                stroke-width="2"
                class="life-circle"
                :style="{ animationDelay: `${index * 0.1}s` }"
              />
            </g>
            <!-- Sacred Polygons -->
            <polygon
              points="150,50 200,100 200,150 150,200 100,150 100,100"
              fill="none"
              stroke="#10b981"
              stroke-width="3"
              class="sacred-hex"
            />
            <polygon
              points="150,75 175,125 150,175 125,125"
              fill="none"
              stroke="#8b5cf6"
              stroke-width="2"
              class="inner-diamond"
            />
          </svg>
        </div>
        <div class="level-up-text">
          <h2>LEVEL UP!</h2>
          <p>You've reached Level {{ userLevel }}!</p>
          <div class="new-abilities">
            <div v-for="ability in newAbilities" :key="ability" class="ability">
              <span class="ability-icon">✨</span>
              {{ ability }}
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

interface Achievement {
  id: string
  title: string
  description: string
  icon: string
  xpBonus: number
  unlocked: boolean
  progress: number
  required: number
  type: 'quest' | 'streak' | 'role' | 'community' | 'special'
}

interface Props {
  userLevel: number
  currentXP: number
  questsCompleted: number
  currentStreak: number
  showGallery?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showGallery: false
})

const emit = defineEmits<{
  'achievement-unlocked': [achievement: Achievement]
  'level-up': [newLevel: number]
}>()

// Reactive state
const showNotification = ref(false)
const showLevelUpEffect = ref(false)
const currentAchievement = ref<Achievement | null>(null)

// Computed properties
const xpToNextLevel = computed(() => {
  // Fibonacci-like progression: 100, 200, 300, 500, 800, 1300, 2100...
  const fibonacci = [100, 200, 300, 500, 800, 1300, 2100, 3400, 5500, 8900]
  return fibonacci[Math.min(props.userLevel - 1, fibonacci.length - 1)] || 10000
})

const levelProgress = computed(() => {
  return (props.currentXP / xpToNextLevel.value) * 100
})

// Flower of Life sacred geometry coordinates
const flowerOfLifeCircles = computed(() => {
  const center = { x: 150, y: 150 }
  const radius = 40
  const circles = [center] // Center circle
  
  // First ring (6 circles)
  for (let i = 0; i < 6; i++) {
    const angle = (i * Math.PI * 2) / 6
    circles.push({
      x: center.x + radius * Math.cos(angle),
      y: center.y + radius * Math.sin(angle)
    })
  }
  
  // Second ring (12 circles) 
  for (let i = 0; i < 12; i++) {
    const angle = (i * Math.PI * 2) / 12
    const distance = radius * Math.sqrt(3)
    circles.push({
      x: center.x + distance * Math.cos(angle),
      y: center.y + distance * Math.sin(angle)
    })
  }
  
  return circles
})

const achievements = ref<Achievement[]>([
  {
    id: 'first-quest',
    title: 'Sacred Beginning',
    description: 'Complete your first quest',
    icon: '🌟',
    xpBonus: 50,
    unlocked: props.questsCompleted >= 1,
    progress: Math.min(props.questsCompleted, 1),
    required: 1,
    type: 'quest'
  },
  {
    id: 'quest-master',
    title: 'Quest Master',
    description: 'Complete 10 quests',
    icon: '🏆',
    xpBonus: 200,
    unlocked: props.questsCompleted >= 10,
    progress: Math.min(props.questsCompleted, 10),
    required: 10,
    type: 'quest'
  },
  {
    id: 'streak-keeper',
    title: 'Streak Keeper',
    description: 'Maintain a 7-day quest streak',
    icon: '🔥',
    xpBonus: 300,
    unlocked: props.currentStreak >= 7,
    progress: Math.min(props.currentStreak, 7),
    required: 7,
    type: 'streak'
  },
  {
    id: 'syn-master',
    title: 'SYN Master',
    description: 'Complete all 4 SYN Principle quests',
    icon: '🔯',
    xpBonus: 500,
    unlocked: false, // Would be calculated based on specific quest completion
    progress: 0,
    required: 4,
    type: 'special'
  },
  {
    id: 'community-builder',
    title: 'Community Builder',
    description: 'Help 5 other users with their quests',
    icon: '🤝',
    xpBonus: 250,
    unlocked: false,
    progress: 0,
    required: 5,
    type: 'community'
  },
  {
    id: 'github-integrator',
    title: 'GitHub Integrator',
    description: 'Complete your first GitHub quest',
    icon: '🐙',
    xpBonus: 400,
    unlocked: false,
    progress: 0,
    required: 1,
    type: 'special'
  }
])

const newAbilities = computed(() => {
  const abilities = {
    2: ['Access to Skill Building Quests', 'SYN Principles Study'],
    3: ['Community Features Unlocked', 'Networking Quests Available'],
    4: ['GitHub Integration', 'Advanced Quest Types'],
    5: ['Mentor Role Available', 'Quest Creation Tools'],
    10: ['Sacred Geometry Master', 'Advanced Customization']
  }
  return abilities[props.userLevel] || []
})

// Methods
const sparkleStyle = (index: number) => {
  const angle = (index * 45) * (Math.PI / 180)
  const distance = 80 + Math.random() * 40
  const x = Math.cos(angle) * distance
  const y = Math.sin(angle) * distance
  
  return {
    '--end-x': `${x}px`,
    '--end-y': `${y}px`,
    animationDelay: `${index * 0.1}s`
  }
}

const selectAchievement = (achievement: Achievement) => {
  console.log('Selected achievement:', achievement)
}

const checkForNewAchievements = () => {
  achievements.value.forEach(achievement => {
    if (!achievement.unlocked && achievement.progress >= achievement.required) {
      unlockAchievement(achievement)
    }
  })
}

const unlockAchievement = (achievement: Achievement) => {
  achievement.unlocked = true
  currentAchievement.value = achievement
  showNotification.value = true
  
  setTimeout(() => {
    showNotification.value = false
  }, 4000)
  
  emit('achievement-unlocked', achievement)
}

const triggerLevelUpEffect = () => {
  showLevelUpEffect.value = true
  setTimeout(() => {
    showLevelUpEffect.value = false
  }, 5000)
  
  emit('level-up', props.userLevel)
}

// Watch for changes
watch(() => props.questsCompleted, checkForNewAchievements)
watch(() => props.currentStreak, checkForNewAchievements)

defineExpose({
  triggerLevelUpEffect,
  unlockAchievement
})
</script>

<style scoped>
.achievement-system {
  position: relative;
}

/* Achievement Notification */
.achievement-notification {
  position: fixed;
  top: 20px;
  right: 20px;
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(245, 158, 11, 0.3);
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 16px;
  max-width: 350px;
  backdrop-filter: blur(10px);
}

.achievement-icon {
  font-size: 32px;
  animation: achievementPulse 2s ease-in-out infinite;
}

.achievement-content h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
}

.achievement-content p {
  margin: 0 0 8px 0;
  opacity: 0.9;
  font-size: 14px;
}

.achievement-reward {
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.achievement-effects {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}

.sparkle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: #fbbf24;
  border-radius: 50%;
  animation: sparkleFloat 2s ease-out forwards;
}

@keyframes achievementPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

@keyframes sparkleFloat {
  0% { transform: translate(0, 0) scale(1); opacity: 1; }
  100% { 
    transform: translate(var(--end-x), var(--end-y)) scale(0); 
    opacity: 0; 
  }
}

/* Quest Streak */
.quest-streak {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
  padding: 12px 16px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.streak-icon {
  font-size: 24px;
  animation: flameFlicker 1s ease-in-out infinite alternate;
}

@keyframes flameFlicker {
  0% { transform: scale(1) rotate(-2deg); }
  100% { transform: scale(1.1) rotate(2deg); }
}

.streak-content {
  flex: 1;
}

.streak-number {
  font-size: 20px;
  font-weight: 700;
  display: block;
}

.streak-label {
  font-size: 12px;
  opacity: 0.9;
}

.streak-progress {
  width: 60px;
  height: 4px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
  overflow: hidden;
}

.streak-bar {
  height: 100%;
  background: #fbbf24;
  transition: width 0.5s ease;
}

/* Level Progress */
.level-progress {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.1), rgba(139, 92, 246, 0.05));
  border: 2px solid rgba(139, 92, 246, 0.3);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
}

.level-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.current-level {
  font-size: 18px;
  font-weight: 600;
  color: #8b5cf6;
}

.xp-info {
  font-size: 14px;
  color: #6b7280;
}

.progress-bar {
  position: relative;
  width: 100%;
  height: 8px;
  background: rgba(139, 92, 246, 0.2);
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #8b5cf6, #a78bfa);
  border-radius: 4px;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.progress-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  animation: progressGlow 2s ease-in-out infinite;
}

@keyframes progressGlow {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.progress-markers {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.progress-marker {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 1px;
  background: rgba(255, 255, 255, 0.3);
}

/* Achievement Gallery */
.achievement-gallery h3 {
  color: #1f2937;
  margin-bottom: 16px;
  font-size: 20px;
  font-weight: 600;
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.achievement-badge {
  background: rgba(156, 163, 175, 0.1);
  border: 2px solid rgba(156, 163, 175, 0.3);
  border-radius: 8px;
  padding: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.achievement-badge.unlocked {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.1), rgba(245, 158, 11, 0.05));
  border-color: rgba(245, 158, 11, 0.5);
  transform: scale(1.02);
}

.achievement-badge:hover {
  transform: translateY(-2px);
}

.badge-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.badge-title {
  font-size: 12px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 4px;
}

.badge-progress {
  font-size: 10px;
  color: #6b7280;
}

/* Level Up Effect */
.level-up-effect {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  backdrop-filter: blur(5px);
}

.geometry-burst {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.burst-pattern {
  width: 300px;
  height: 300px;
  animation: geometryRotate 5s linear infinite;
}

.life-circle {
  animation: lifeCircleExpand 3s ease-out forwards;
}

.sacred-hex {
  animation: hexPulse 2s ease-in-out infinite;
}

.inner-diamond {
  animation: diamondSpin 3s linear infinite;
}

@keyframes geometryRotate {
  from { transform: translate(-50%, -50%) rotate(0deg); }
  to { transform: translate(-50%, -50%) rotate(360deg); }
}

@keyframes lifeCircleExpand {
  0% { r: 0; opacity: 1; }
  50% { r: 50; opacity: 0.8; }
  100% { r: 40; opacity: 0.6; }
}

@keyframes hexPulse {
  0%, 100% { stroke-width: 3; opacity: 0.8; }
  50% { stroke-width: 5; opacity: 1; }
}

@keyframes diamondSpin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.level-up-text {
  text-align: center;
  color: white;
  z-index: 10;
  position: relative;
}

.level-up-text h2 {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 16px 0;
  background: linear-gradient(45deg, #f59e0b, #fbbf24, #f59e0b);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: textGlow 2s ease-in-out infinite alternate;
}

.level-up-text p {
  font-size: 18px;
  margin: 0 0 24px 0;
  opacity: 0.9;
}

@keyframes textGlow {
  from { filter: drop-shadow(0 0 10px rgba(245, 158, 11, 0.5)); }
  to { filter: drop-shadow(0 0 20px rgba(245, 158, 11, 0.8)); }
}

.new-abilities {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ability {
  background: rgba(139, 92, 246, 0.2);
  padding: 8px 16px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.ability-icon {
  font-size: 16px;
}

/* Transitions */
.achievement-notification-enter-active, .achievement-notification-leave-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.achievement-notification-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.achievement-notification-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

.level-up-enter-active, .level-up-leave-active {
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.level-up-enter-from, .level-up-leave-to {
  opacity: 0;
  transform: scale(0.8);
}
</style>
