<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

interface GeometryPattern {
  id: string
  name: string
  description: string
  difficulty: number
  unlockLevel: number
  category: 'sacred' | 'mathematical' | 'quantum'
}

const selectedPattern = ref<GeometryPattern | null>(null)
const canvasContainer = ref<HTMLDivElement>()
const currentSketch = ref<any>(null)

const patterns: GeometryPattern[] = [
  {
    id: 'flower-of-life',
    name: 'Flower of Life',
    description: 'The fundamental pattern of creation, containing all other sacred geometric forms.',
    difficulty: 1,
    unlockLevel: 1,
    category: 'sacred'
  },
  {
    id: 'vesica-piscis',
    name: 'Vesica Piscis',
    description: 'The sacred intersection of two circles representing duality and unity.',
    difficulty: 1,
    unlockLevel: 1,
    category: 'sacred'
  },
  {
    id: 'metatrons-cube',
    name: "Metatron's Cube",
    description: 'Contains all five Platonic solids and represents the blueprint of creation.',
    difficulty: 3,
    unlockLevel: 5,
    category: 'sacred'
  },
  {
    id: 'golden-spiral',
    name: 'Golden Spiral',
    description: 'The divine proportion manifested in spiral form, found throughout nature.',
    difficulty: 2,
    unlockLevel: 3,
    category: 'mathematical'
  },
  {
    id: 'sri-yantra',
    name: 'Sri Yantra',
    description: 'Nine interlocking triangles representing the cosmos and consciousness.',
    difficulty: 4,
    unlockLevel: 8,
    category: 'sacred'
  },
  {
    id: 'torus-field',
    name: 'Torus Field',
    description: 'The fundamental energy pattern of the universe and consciousness.',
    difficulty: 5,
    unlockLevel: 12,
    category: 'quantum'
  },
  {
    id: 'platonic-solids',
    name: 'Platonic Solids',
    description: 'The five perfect three-dimensional forms representing the elements.',
    difficulty: 4,
    unlockLevel: 10,
    category: 'mathematical'
  },
  {
    id: 'fractal-tree',
    name: 'Fractal Tree',
    description: 'Self-similar branching patterns found in nature and consciousness.',
    difficulty: 3,
    unlockLevel: 6,
    category: 'mathematical'
  }
]

const availablePatterns = computed(() => {
  return patterns.filter(pattern => 
    userStore.userLevel >= pattern.unlockLevel
  )
})

const lockedPatterns = computed(() => {
  return patterns.filter(pattern => 
    userStore.userLevel < pattern.unlockLevel
  )
})

onMounted(() => {
  if (availablePatterns.value.length > 0) {
    selectPattern(availablePatterns.value[0])
  }
})

const selectPattern = async (pattern: GeometryPattern) => {
  selectedPattern.value = pattern
  
  // Clear previous sketch
  if (currentSketch.value) {
    currentSketch.value.remove()
    currentSketch.value = null
  }
  
  await nextTick()
  
  if (window.p5 && canvasContainer.value) {
    currentSketch.value = new window.p5(getSketch(pattern.id), canvasContainer.value)
  }
}

const getSketch = (patternId: string) => {
  return (p: any) => {
    let time = 0
    let animationSpeed = 0.02
    
    p.setup = () => {
      p.createCanvas(800, 600)
      p.colorMode(p.HSB, 360, 100, 100, 100)
      p.angleMode(p.RADIANS)
    }
    
    p.draw = () => {
      p.background(220, 10, 5, 100)
      p.translate(p.width / 2, p.height / 2)
      
      switch (patternId) {
        case 'flower-of-life':
          drawFlowerOfLife(p, time)
          break
        case 'vesica-piscis':
          drawVesicaPiscis(p, time)
          break
        case 'metatrons-cube':
          drawMetatronsCube(p, time)
          break
        case 'golden-spiral':
          drawGoldenSpiral(p, time)
          break
        case 'sri-yantra':
          drawSriYantra(p, time)
          break
        case 'torus-field':
          drawTorusField(p, time)
          break
        case 'platonic-solids':
          drawPlatonicSolids(p, time)
          break
        case 'fractal-tree':
          drawFractalTree(p, time)
          break
      }
      
      time += animationSpeed
    }
  }
}

const drawFlowerOfLife = (p: any, time: number) => {
  const radius = 80
  const centers = []
  
  // Central circle
  centers.push({ x: 0, y: 0 })
  
  // Six surrounding circles
  for (let i = 0; i < 6; i++) {
    const angle = (Math.PI * 2 / 6) * i
    centers.push({
      x: Math.cos(angle) * radius,
      y: Math.sin(angle) * radius
    })
  }
  
  // Second ring
  for (let i = 0; i < 12; i++) {
    const angle = (Math.PI * 2 / 12) * i
    const distance = radius * Math.sqrt(3)
    centers.push({
      x: Math.cos(angle) * distance,
      y: Math.sin(angle) * distance
    })
  }
  
  // Draw circles
  centers.forEach((center, index) => {
    p.push()
    p.translate(center.x, center.y)
    p.noFill()
    p.stroke(240 + index * 5, 70, 90, 60 + Math.sin(time + index * 0.5) * 20)
    p.strokeWeight(2)
    p.circle(0, 0, radius * 2)
    p.pop()
  })
}

const drawVesicaPiscis = (p: any, time: number) => {
  const radius = 120
  const offset = radius * 0.8
  
  // Left circle
  p.push()
  p.translate(-offset, 0)
  p.noFill()
  p.stroke(200, 80, 90, 70)
  p.strokeWeight(3)
  p.circle(0, 0, radius * 2)
  p.pop()
  
  // Right circle
  p.push()
  p.translate(offset, 0)
  p.noFill()
  p.stroke(280, 80, 90, 70)
  p.strokeWeight(3)
  p.circle(0, 0, radius * 2)
  p.pop()
  
  // Intersection highlight
  p.fill(240, 60, 95, 20 + Math.sin(time) * 10)
  p.noStroke()
  p.beginShape()
  for (let angle = -Math.PI/2; angle <= Math.PI/2; angle += 0.1) {
    const x = offset + Math.cos(angle) * radius
    const y = Math.sin(angle) * radius
    p.vertex(x, y)
  }
  for (let angle = Math.PI/2; angle >= -Math.PI/2; angle -= 0.1) {
    const x = -offset + Math.cos(angle) * radius
    const y = Math.sin(angle) * radius
    p.vertex(x, y)
  }
  p.endShape(p.CLOSE)
}

const drawMetatronsCube = (p: any, time: number) => {
  const radius = 40
  const centers = []
  
  // Central circle
  centers.push({ x: 0, y: 0 })
  
  // Inner hexagon
  for (let i = 0; i < 6; i++) {
    const angle = (Math.PI * 2 / 6) * i
    centers.push({
      x: Math.cos(angle) * radius,
      y: Math.sin(angle) * radius
    })
  }
  
  // Outer hexagon
  for (let i = 0; i < 6; i++) {
    const angle = (Math.PI * 2 / 6) * i + Math.PI / 6
    centers.push({
      x: Math.cos(angle) * radius * 2,
      y: Math.sin(angle) * radius * 2
    })
  }
  
  // Draw connecting lines
  p.stroke(270, 60, 80, 50)
  p.strokeWeight(1)
  for (let i = 0; i < centers.length; i++) {
    for (let j = i + 1; j < centers.length; j++) {
      p.line(centers[i].x, centers[i].y, centers[j].x, centers[j].y)
    }
  }
  
  // Draw circles
  centers.forEach((center, index) => {
    p.push()
    p.translate(center.x, center.y)
    p.fill(260 + index * 10, 70, 90, 30 + Math.sin(time + index * 0.3) * 20)
    p.stroke(260 + index * 10, 80, 95, 80)
    p.strokeWeight(2)
    p.circle(0, 0, 20)
    p.pop()
  })
}

const drawGoldenSpiral = (p: any, time: number) => {
  const phi = (1 + Math.sqrt(5)) / 2
  let size = 200
  
  p.stroke(50, 80, 90, 80)
  p.strokeWeight(3)
  p.noFill()
  
  p.beginShape()
  for (let angle = 0; angle < Math.PI * 8; angle += 0.1) {
    const r = size * Math.pow(phi, -angle / (Math.PI / 2))
    const x = r * Math.cos(angle + time)
    const y = r * Math.sin(angle + time)
    p.vertex(x, y)
  }
  p.endShape()
  
  // Draw golden rectangles
  p.stroke(50, 60, 70, 40)
  p.strokeWeight(1)
  let rectSize = 200
  let angle = time
  for (let i = 0; i < 8; i++) {
    p.push()
    p.rotate(angle)
    p.rect(-rectSize/2, -rectSize/2, rectSize, rectSize / phi)
    rectSize /= phi
    angle += Math.PI / 2
    p.pop()
  }
}

const drawSriYantra = (p: any, time: number) => {
  // Outer triangles pointing up
  for (let i = 0; i < 4; i++) {
    p.push()
    p.rotate(time * 0.2 + i * 0.1)
    p.stroke(0, 80, 90, 60)
    p.strokeWeight(2)
    p.noFill()
    drawTriangle(p, 0, -20 - i * 15, 100 + i * 20, true)
    p.pop()
  }
  
  // Inner triangles pointing down
  for (let i = 0; i < 5; i++) {
    p.push()
    p.rotate(-time * 0.15 + i * 0.08)
    p.stroke(300, 80, 90, 60)
    p.strokeWeight(2)
    p.noFill()
    drawTriangle(p, 0, 15 + i * 12, 80 + i * 15, false)
    p.pop()
  }
}

const drawTriangle = (p: any, x: number, y: number, size: number, pointUp: boolean) => {
  p.push()
  p.translate(x, y)
  if (!pointUp) p.rotate(Math.PI)
  p.beginShape()
  p.vertex(0, -size/2)
  p.vertex(-size * Math.sqrt(3)/4, size/4)
  p.vertex(size * Math.sqrt(3)/4, size/4)
  p.endShape(p.CLOSE)
  p.pop()
}

const drawTorusField = (p: any, time: number) => {
  const majorRadius = 100
  const minorRadius = 40
  
  p.stroke(180, 70, 90, 60)
  p.strokeWeight(1)
  p.noFill()
  
  // Draw torus wireframe
  for (let u = 0; u < Math.PI * 2; u += Math.PI / 8) {
    p.beginShape()
    for (let v = 0; v < Math.PI * 2; v += Math.PI / 16) {
      const x = (majorRadius + minorRadius * Math.cos(v + time)) * Math.cos(u)
      const y = (majorRadius + minorRadius * Math.cos(v + time)) * Math.sin(u)
      const z = minorRadius * Math.sin(v + time)
      
      // Project 3D to 2D
      const scale = 200 / (200 + z)
      p.vertex(x * scale, y * scale)
    }
    p.endShape(p.CLOSE)
  }
}

const drawPlatonicSolids = (p: any, time: number) => {
  // This is a simplified 2D representation
  const solids = [
    { name: 'Tetrahedron', sides: 3, color: 0 },
    { name: 'Cube', sides: 4, color: 60 },
    { name: 'Octahedron', sides: 3, color: 120 },
    { name: 'Dodecahedron', sides: 5, color: 180 },
    { name: 'Icosahedron', sides: 3, color: 240 }
  ]
  
  solids.forEach((solid, index) => {
    p.push()
    const angle = (Math.PI * 2 / 5) * index + time
    p.translate(Math.cos(angle) * 80, Math.sin(angle) * 80)
    p.rotate(time + index)
    
    p.stroke(solid.color, 80, 90, 80)
    p.fill(solid.color, 60, 70, 20)
    p.strokeWeight(2)
    
    drawPolygon(p, solid.sides, 25)
    p.pop()
  })
}

const drawPolygon = (p: any, sides: number, radius: number) => {
  p.beginShape()
  for (let i = 0; i < sides; i++) {
    const angle = (Math.PI * 2 / sides) * i
    const x = Math.cos(angle) * radius
    const y = Math.sin(angle) * radius
    p.vertex(x, y)
  }
  p.endShape(p.CLOSE)
}

const drawFractalTree = (p: any, time: number) => {
  p.stroke(120, 60, 80, 80)
  p.strokeWeight(1)
  
  drawBranch(p, 0, 50, 100, Math.PI / 6, 0, time)
}

const drawBranch = (p: any, x: number, y: number, length: number, angle: number, depth: number, time: number) => {
  if (depth > 8 || length < 2) return
  
  const endX = x + Math.cos(angle) * length
  const endY = y + Math.sin(angle) * length
  
  p.line(x, y, endX, endY)
  
  const newLength = length * 0.7
  const angleOffset = Math.sin(time + depth * 0.5) * 0.2
  
  drawBranch(p, endX, endY, newLength, angle - Math.PI / 6 + angleOffset, depth + 1, time)
  drawBranch(p, endX, endY, newLength, angle + Math.PI / 6 + angleOffset, depth + 1, time)
}

const getPatternsByCategory = (category: string) => {
  return availablePatterns.value.filter(pattern => pattern.category === category)
}

const getCategoryColor = (category: string) => {
  switch (category) {
    case 'sacred': return 'text-purple-400'
    case 'mathematical': return 'text-blue-400'
    case 'quantum': return 'text-cyan-400'
    default: return 'text-gray-400'
  }
}

const getDifficultyStars = (difficulty: number) => {
  return '★'.repeat(difficulty) + '☆'.repeat(5 - difficulty)
}
</script>

<template>
  <div class="geometry-view">
    <div class="container">
      <!-- Header -->
      <div class="geometry-header">
        <h1 class="page-title">Sacred Geometry Explorer</h1>
        <p class="page-subtitle">
          Explore the mathematical patterns that underlie all of creation
        </p>
      </div>

      <div class="geometry-content">
        <!-- Pattern Selection Sidebar -->
        <div class="patterns-sidebar">
          <h2 class="sidebar-title">Available Patterns</h2>
          
          <!-- Sacred Patterns -->
          <div class="pattern-category">
            <h3 class="category-title">
              <span class="category-icon text-purple-400">🌸</span>
              Sacred Patterns
            </h3>
            <div class="pattern-list">
              <button
                v-for="pattern in getPatternsByCategory('sacred')"
                :key="pattern.id"
                class="pattern-button"
                :class="{ active: selectedPattern?.id === pattern.id }"
                @click="selectPattern(pattern)"
              >
                <div class="pattern-name">{{ pattern.name }}</div>
                <div class="pattern-difficulty">{{ getDifficultyStars(pattern.difficulty) }}</div>
              </button>
            </div>
          </div>

          <!-- Mathematical Patterns -->
          <div class="pattern-category">
            <h3 class="category-title">
              <span class="category-icon text-blue-400">🔢</span>
              Mathematical Patterns
            </h3>
            <div class="pattern-list">
              <button
                v-for="pattern in getPatternsByCategory('mathematical')"
                :key="pattern.id"
                class="pattern-button"
                :class="{ active: selectedPattern?.id === pattern.id }"
                @click="selectPattern(pattern)"
              >
                <div class="pattern-name">{{ pattern.name }}</div>
                <div class="pattern-difficulty">{{ getDifficultyStars(pattern.difficulty) }}</div>
              </button>
            </div>
          </div>

          <!-- Quantum Patterns -->
          <div class="pattern-category">
            <h3 class="category-title">
              <span class="category-icon text-cyan-400">⚛️</span>
              Quantum Patterns
            </h3>
            <div class="pattern-list">
              <button
                v-for="pattern in getPatternsByCategory('quantum')"
                :key="pattern.id"
                class="pattern-button"
                :class="{ active: selectedPattern?.id === pattern.id }"
                @click="selectPattern(pattern)"
              >
                <div class="pattern-name">{{ pattern.name }}</div>
                <div class="pattern-difficulty">{{ getDifficultyStars(pattern.difficulty) }}</div>
              </button>
            </div>
          </div>

          <!-- Locked Patterns -->
          <div v-if="lockedPatterns.length > 0" class="pattern-category">
            <h3 class="category-title">
              <span class="category-icon text-gray-400">🔒</span>
              Locked Patterns
            </h3>
            <div class="pattern-list">
              <div
                v-for="pattern in lockedPatterns"
                :key="pattern.id"
                class="pattern-button locked"
              >
                <div class="pattern-name">{{ pattern.name }}</div>
                <div class="unlock-level">Level {{ pattern.unlockLevel }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Main Visualization Area -->
        <div class="visualization-area">
          <div v-if="selectedPattern" class="pattern-info card">
            <h2 class="pattern-title">{{ selectedPattern.name }}</h2>
            <p class="pattern-description">{{ selectedPattern.description }}</p>
            <div class="pattern-meta">
              <span class="pattern-category" :class="getCategoryColor(selectedPattern.category)">
                {{ selectedPattern.category.charAt(0).toUpperCase() + selectedPattern.category.slice(1) }}
              </span>
              <span class="pattern-difficulty-display">
                {{ getDifficultyStars(selectedPattern.difficulty) }}
              </span>
            </div>
          </div>

          <div class="canvas-area">
            <div ref="canvasContainer" class="canvas-container"></div>
          </div>

          <div class="pattern-controls card">
            <h3>Meditation Guide</h3>
            <p>Focus on the center of the pattern and breathe deeply. Allow your awareness to expand with each breath, feeling the sacred geometry resonate within your consciousness.</p>
            <div class="control-buttons">
              <button class="btn btn-ghost">
                <span>🧘‍♀️</span> Start Meditation
              </button>
              <button class="btn btn-ghost">
                <span>📱</span> Share Pattern
              </button>
              <button class="btn btn-ghost">
                <span>💾</span> Save Image
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.geometry-view {
  min-height: 100vh;
  padding: 2rem 0;
}

.geometry-header {
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
  color: var(--color-text-muted);
}

.geometry-content {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 2rem;
}

.patterns-sidebar {
  background: rgba(255, 255, 255, 0.02);
  backdrop-filter: var(--blur-md);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  height: fit-content;
  max-height: 80vh;
  overflow-y: auto;
}

.sidebar-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
  color: var(--color-text);
}

.pattern-category {
  margin-bottom: 2rem;
}

.category-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 500;
  margin-bottom: 1rem;
  color: var(--color-text);
}

.category-icon {
  font-size: 1.25rem;
}

.pattern-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.pattern-button {
  padding: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.02);
  color: var(--color-text);
  text-align: left;
  transition: all var(--transition-normal);
  cursor: pointer;
}

.pattern-button:hover:not(.locked) {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.2);
}

.pattern-button.active {
  background: var(--color-primary-alpha);
  border-color: var(--color-primary);
}

.pattern-button.locked {
  opacity: 0.5;
  cursor: not-allowed;
}

.pattern-name {
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.pattern-difficulty,
.unlock-level {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.visualization-area {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.pattern-info {
  padding: 1.5rem;
}

.pattern-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  color: var(--color-text);
}

.pattern-description {
  color: var(--color-text-muted);
  line-height: 1.6;
  margin-bottom: 1rem;
}

.pattern-meta {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.pattern-category {
  font-weight: 500;
  text-transform: uppercase;
  font-size: 0.875rem;
  letter-spacing: 0.5px;
}

.pattern-difficulty-display {
  color: var(--color-accent);
}

.canvas-area {
  display: flex;
  justify-content: center;
}

.canvas-container {
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.pattern-controls {
  padding: 1.5rem;
}

.pattern-controls h3 {
  margin-bottom: 0.75rem;
  color: var(--color-text);
}

.pattern-controls p {
  color: var(--color-text-muted);
  line-height: 1.6;
  margin-bottom: 1.5rem;
}

.control-buttons {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.control-buttons .btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .geometry-content {
    grid-template-columns: 250px 1fr;
  }
}

@media (max-width: 768px) {
  .geometry-content {
    grid-template-columns: 1fr;
  }
  
  .patterns-sidebar {
    max-height: none;
    order: 2;
  }
  
  .visualization-area {
    order: 1;
  }
  
  .canvas-container {
    width: 100%;
    overflow-x: auto;
  }
}
</style>
