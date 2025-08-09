<template>
  <div 
    class="game-quest-card"
    :class="[
      `quest-type-${quest?.type?.toLowerCase()}`,
      `quest-difficulty-${quest?.difficulty?.toLowerCase()}`,
      `quest-statuimport type { UserQuest } from '@/services/quest.service'

interface Props {
  userQuest: UserQuest
  userLevel?: number
  isCompleting?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  userLevel: 1,
  isCompleting: false
})

const emit = defineEmits<{
  'accept-quest': [userQuest: UserQuest]
  'complete-quest': [userQuest: UserQuest]
  'abandon-quest': [userQuest: UserQuest]
  'quest-click': [userQuest: UserQuest]
}>()s?.toLowerCase()}`,
      { 'quest-locked': isLocked, 'quest-completing': isCompleting }
    ]"
    @click="handleQuestClick"
  >
    <!-- Sacred Geometry Background -->
    <div class="sacred-geometry-bg">
      <svg class="geometry-pattern" viewBox="0 0 100 100">
        <circle 
          v-for="n in 6" 
          :key="n"
          :cx="50 + 25 * Math.cos((n * Math.PI * 2) / 6)"
          :cy="50 + 25 * Math.sin((n * Math.PI * 2) / 6)"
          r="8"
          class="geometry-circle"
          :style="{ animationDelay: `${n * 0.1}s` }"
        />
      </svg>
    </div>

    <!-- Progress Ring -->
    <div class="progress-ring-container">
      <svg class="progress-ring" width="80" height="80">
        <circle
          class="progress-ring-bg"
          cx="40"
          cy="40"
          r="35"
          fill="transparent"
          stroke="rgba(139, 92, 246, 0.2)"
          stroke-width="4"
        />
        <circle
          class="progress-ring-fill"
          cx="40"
          cy="40"
          r="35"
          fill="transparent"
          stroke="url(#questGradient)"
          stroke-width="4"
          stroke-linecap="round"
          :stroke-dasharray="circumference"
          :stroke-dashoffset="strokeDashoffset"
          transform="rotate(-90 40 40)"
        />
        <defs>
          <linearGradient id="questGradient" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" :style="`stop-color:${questTypeColor};stop-opacity:1`" />
            <stop offset="100%" :style="`stop-color:${questTypeColor};stop-opacity:0.6`" />
          </linearGradient>
        </defs>
      </svg>
      
      <!-- Quest Type Icon -->
      <div class="quest-icon" :class="`icon-${quest.type?.toLowerCase()}`">
        {{ questTypeIcon }}
      </div>
    </div>

    <!-- Quest Content -->
    <div class="quest-content">
      <!-- Header -->
      <div class="quest-header">
        <h3 class="quest-title">{{ quest?.title }}</h3>
        <div class="quest-meta">
          <span class="quest-level">Level {{ quest.requiredLevel }}</span>
          <span class="quest-xp">{{ quest?.experienceReward }} XP</span>
        </div>
      </div>

      <!-- Description -->
      <p class="quest-description">{{ truncatedDescription }}</p>

      <!-- Sacred Role & SYN Principle -->
      <div class="quest-sacred-info" v-if="quest?.role">
        <div class="role-dialect" v-if="quest.role">
          <span class="label">{{ quest.role }}</span>
        </div>
        <div class="syn-principle" v-if="quest.type">
          <span class="principle-badge">{{ quest.type }}</span>
        </div>
      </div>

      <!-- Progress Steps (für Onboarding Quests) -->
      <div class="quest-steps" v-if="quest?.completionSteps && showSteps">
        <div class="steps-header">
          <span>Quest Steps ({{ completedSteps }}/{{ totalSteps }})</span>
          <button @click.stop="toggleSteps" class="toggle-steps">
            {{ showSteps ? '▲' : '▼' }}
          </button>
        </div>
        <div class="steps-list">
          <div 
            v-for="(step, index) in quest.completionSteps" 
            :key="index"
            class="step-item"
            :class="{ 'step-completed': index < completedSteps }"
          >
            <div class="step-icon">
              {{ index < completedSteps ? '✅' : '⭕' }}
            </div>
            <span class="step-text">{{ step }}</span>
          </div>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="quest-actions">
        <button 
          v-if="userQuest.status === 'USER_AVAILABLE' && !isLocked"
          @click.stop="$emit('accept-quest', userQuest)"
          class="btn-accept"
          :disabled="isCompleting"
        >
          <span class="btn-icon">🚀</span>
          Accept Quest
        </button>
        
        <button 
          v-else-if="userQuest.status === 'USER_ACTIVE'"
          @click.stop="$emit('complete-quest', userQuest)"
          class="btn-complete"
          :disabled="isCompleting"
        >
          <span class="btn-icon">✨</span>
          {{ isCompleting ? 'Completing...' : 'Complete Quest' }}
        </button>
        
        <button 
          v-else-if="userQuest.status === 'USER_ACTIVE'"
          @click.stop="$emit('abandon-quest', userQuest)"
          class="btn-abandon"
        >
          <span class="btn-icon">❌</span>
          Abandon
        </button>
        
        <div v-else-if="userQuest.status === 'USER_COMPLETED'" class="quest-completed">
          <span class="completion-icon">🏆</span>
          Completed!
        </div>
        
        <div v-else-if="isLocked" class="quest-locked">
          <span class="lock-icon">🔒</span>
          Requires Level {{ quest.requiredLevel }}
        </div>
      </div>
    </div>

    <!-- XP Gain Animation -->
    <Transition name="xp-gain" appear>
      <div v-if="showXPAnimation" class="xp-animation">
        <span class="xp-text">+{{ quest?.experienceReward }} XP</span>
        <div class="xp-particles">
          <div v-for="n in 5" :key="n" class="particle" :style="particleStyle(n)"></div>
        </div>
      </div>
    </Transition>

    <!-- Sacred Geometry Completion Effect -->
    <Transition name="completion-effect" appear>
      <div v-if="showCompletionEffect" class="completion-effect">
        <svg class="completion-geometry" viewBox="0 0 200 200">
          <circle
            v-for="n in 12"
            :key="n"
            :cx="100 + 60 * Math.cos((n * Math.PI * 2) / 12)"
            :cy="100 + 60 * Math.sin((n * Math.PI * 2) / 12)"
            r="8"
            class="completion-circle"
            :style="{ animationDelay: `${n * 0.05}s` }"
          />
          <polygon
            points="100,40 140,80 140,120 100,160 60,120 60,80"
            class="completion-hex"
          />
        </svg>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { UserQuest } from '@/services/quest.service'
import { UserQuestStatus } from '@/services/quest.service'

interface Props {
  userQuest: UserQuest
  userLevel?: number
  isCompleting?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  userLevel: 1,
  isCompleting: false
})

const emit = defineEmits<{
  'accept-quest': [userQuest: UserQuest]
  'complete-quest': [userQuest: UserQuest]
  'abandon-quest': [userQuest: UserQuest]
  'quest-click': [userQuest: UserQuest]
}>()

// Reactive state
const showSteps = ref(false)
const showXPAnimation = ref(false)
const showCompletionEffect = ref(false)

// Computed properties
const quest = computed(() => props.userQuest.quest)

const isLocked = computed(() => {
  return props.userLevel < (quest.value?.requiredLevel || 1)
})

const questTypeIcon = computed(() => {
  const icons = {
    'LEARNING': '📚',
    'SKILL_BUILDING': '⚡',
    'NETWORKING': '🔗',
    'INTEGRATION': '🔄',
    'ONBOARDING': '🌟',
    'GITHUB_ISSUE': '🐙',
    'COMMUNITY': '👥',
    'PLATFORM': '🏛️',
    'SACRED_GEOMETRY': '🔯'
  }
  return icons[quest.value?.type || ''] || '⭐'
})

const questTypeColor = computed(() => {
  const colors = {
    'LEARNING': '#10B981',      // Emerald
    'SKILL_BUILDING': '#F59E0B', // Amber
    'NETWORKING': '#8B5CF6',     // Violet
    'INTEGRATION': '#EF4444',    // Red
    'ONBOARDING': '#06B6D4',     // Cyan
    'GITHUB_ISSUE': '#6B7280',   // Gray
    'COMMUNITY': '#EC4899',      // Pink
    'PLATFORM': '#3B82F6',      // Blue
    'SACRED_GEOMETRY': '#8B5CF6' // Violet
  }
  return colors[quest.value?.type || ''] || '#8B5CF6'
})

const truncatedDescription = computed(() => {
  const description = quest.value?.description || ''
  if (description.length <= 120) {
    return description
  }
  return description.substring(0, 120) + '...'
})

const completedSteps = computed(() => {
  const steps = quest.value?.completionSteps?.length || 4
  return Math.floor((props.userQuest.progress / 100) * steps)
})

const totalSteps = computed(() => {
  return quest.value?.completionSteps?.length || 4
})

const circumference = computed(() => 2 * Math.PI * 35)

const strokeDashoffset = computed(() => {
  if (props.userQuest.status === UserQuestStatus.USER_COMPLETED) return 0
  if (props.userQuest.status === UserQuestStatus.USER_ACTIVE) {
    const progress = props.userQuest.progress / 100
    return circumference.value - (progress * circumference.value)
  }
  return circumference.value // No progress for available quests
})

// Methods
const handleQuestClick = () => {
  emit('quest-click', props.userQuest)
}

const toggleSteps = () => {
  showSteps.value = !showSteps.value
}

const particleStyle = (index: number) => {
  const angle = (index * 72) * (Math.PI / 180) // 72 degrees apart
  const distance = 50 + Math.random() * 30
  const x = Math.cos(angle) * distance
  const y = Math.sin(angle) * distance
  
  return {
    '--end-x': `${x}px`,
    '--end-y': `${y}px`,
    animationDelay: `${index * 0.1}s`
  }
}

// Trigger animations
const triggerXPAnimation = () => {
  showXPAnimation.value = true
  setTimeout(() => {
    showXPAnimation.value = false
  }, 2000)
}

const triggerCompletionEffect = () => {
  showCompletionEffect.value = true
  setTimeout(() => {
    showCompletionEffect.value = false
  }, 3000)
}

defineExpose({
  triggerXPAnimation,
  triggerCompletionEffect
})
</script>

<style scoped>
.game-quest-card {
  position: relative;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.1), rgba(139, 92, 246, 0.05));
  border: 2px solid rgba(139, 92, 246, 0.3);
  border-radius: 16px;
  padding: 24px;
  margin: 16px 0;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.game-quest-card:hover {
  transform: translateY(-4px);
  border-color: rgba(139, 92, 246, 0.6);
  box-shadow: 0 10px 25px rgba(139, 92, 246, 0.2);
}

.game-quest-card.quest-locked {
  opacity: 0.6;
  cursor: not-allowed;
}

.game-quest-card.quest-completing {
  animation: pulse 2s infinite;
}

/* Sacred Geometry Background */
.sacred-geometry-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0.1;
  pointer-events: none;
}

.geometry-pattern {
  width: 100%;
  height: 100%;
}

.geometry-circle {
  fill: none;
  stroke: currentColor;
  stroke-width: 1;
  animation: geometryPulse 4s ease-in-out infinite;
}

@keyframes geometryPulse {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.1); }
}

/* Progress Ring */
.progress-ring-container {
  position: relative;
  display: inline-block;
  margin-bottom: 16px;
}

.progress-ring-fill {
  transition: stroke-dashoffset 0.5s ease-in-out;
}

.quest-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 24px;
  animation: iconFloat 3s ease-in-out infinite;
}

@keyframes iconFloat {
  0%, 100% { transform: translate(-50%, -50%) scale(1); }
  50% { transform: translate(-50%, -52%) scale(1.1); }
}

/* Quest Content */
.quest-content {
  position: relative;
  z-index: 2;
}

.quest-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.quest-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  flex: 1;
}

.quest-meta {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.quest-level, .quest-xp {
  background: rgba(139, 92, 246, 0.1);
  color: #8b5cf6;
  padding: 4px 8px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
}

.quest-description {
  color: #6b7280;
  margin-bottom: 16px;
  line-height: 1.5;
}

/* Sacred Info */
.quest-sacred-info {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.role-dialect .label {
  background: linear-gradient(45deg, #f59e0b, #d97706);
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.principle-badge {
  background: linear-gradient(45deg, #10b981, #059669);
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

/* Quest Steps */
.quest-steps {
  background: rgba(0, 0, 0, 0.02);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 16px;
}

.steps-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-weight: 500;
  color: #4b5563;
}

.toggle-steps {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  color: #8b5cf6;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  transition: all 0.3s ease;
}

.step-item.step-completed {
  opacity: 0.7;
}

.step-item.step-completed .step-text {
  text-decoration: line-through;
}

.step-icon {
  font-size: 16px;
}

.step-text {
  font-size: 14px;
  color: #6b7280;
}

/* Action Buttons */
.quest-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.btn-accept, .btn-complete, .btn-abandon {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-accept {
  background: linear-gradient(45deg, #10b981, #059669);
  color: white;
}

.btn-accept:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.btn-complete {
  background: linear-gradient(45deg, #f59e0b, #d97706);
  color: white;
}

.btn-complete:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
}

.btn-abandon {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.quest-completed, .quest-locked {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6b7280;
  font-weight: 500;
}

/* Animations */
.xp-animation {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
  z-index: 10;
}

.xp-text {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: #f59e0b;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  animation: xpFloat 2s ease-out forwards;
}

.xp-particles {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
}

.particle {
  position: absolute;
  width: 6px;
  height: 6px;
  background: #f59e0b;
  border-radius: 50%;
  animation: particleFloat 1.5s ease-out forwards;
}

@keyframes xpFloat {
  0% { transform: scale(0.5) translateY(0); opacity: 0; }
  20% { transform: scale(1.2) translateY(-10px); opacity: 1; }
  100% { transform: scale(1) translateY(-50px); opacity: 0; }
}

@keyframes particleFloat {
  0% { transform: translate(0, 0) scale(1); opacity: 1; }
  100% { 
    transform: translate(var(--end-x), var(--end-y)) scale(0); 
    opacity: 0; 
  }
}

/* Completion Effect */
.completion-effect {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
  z-index: 10;
}

.completion-geometry {
  width: 200px;
  height: 200px;
}

.completion-circle {
  fill: #10b981;
  animation: completionPulse 3s ease-out forwards;
}

.completion-hex {
  fill: none;
  stroke: #f59e0b;
  stroke-width: 3;
  animation: hexExpand 3s ease-out forwards;
}

@keyframes completionPulse {
  0% { r: 0; opacity: 1; }
  70% { r: 12; opacity: 0.8; }
  100% { r: 20; opacity: 0; }
}

@keyframes hexExpand {
  0% { transform: scale(0) rotate(0deg); opacity: 1; }
  50% { transform: scale(1.2) rotate(180deg); opacity: 0.8; }
  100% { transform: scale(2) rotate(360deg); opacity: 0; }
}

/* Transitions */
.xp-gain-enter-active, .xp-gain-leave-active {
  transition: all 0.3s ease;
}

.xp-gain-enter-from, .xp-gain-leave-to {
  opacity: 0;
  transform: scale(0.5);
}

.completion-effect-enter-active, .completion-effect-leave-active {
  transition: all 0.5s ease;
}

.completion-effect-enter-from, .completion-effect-leave-to {
  opacity: 0;
  transform: scale(0);
}

/* Quest Type Specific Styles */
.quest-type-learning {
  border-color: rgba(16, 185, 129, 0.3);
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1), rgba(16, 185, 129, 0.05));
}

.quest-type-skill_building {
  border-color: rgba(245, 158, 11, 0.3);
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.1), rgba(245, 158, 11, 0.05));
}

.quest-type-networking {
  border-color: rgba(139, 92, 246, 0.3);
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.1), rgba(139, 92, 246, 0.05));
}

.quest-type-integration {
  border-color: rgba(239, 68, 68, 0.3);
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.1), rgba(239, 68, 68, 0.05));
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}
</style>
