<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

interface SacredGeometryProps {
  pattern?: 'flower-of-life' | 'metatrons-cube' | 'sri-yantra' | 'tree-of-life'
  size?: number
  speed?: number
  colors?: string[]
  interactive?: boolean
}

const props = withDefaults(defineProps<SacredGeometryProps>(), {
  pattern: 'flower-of-life',
  size: 400,
  speed: 1,
  colors: () => ['#8B5CF6', '#06B6D4', '#10B981', '#F59E0B'],
  interactive: true
})

const canvas = ref<HTMLCanvasElement>()
let animationId: number
let p5Instance: any

onMounted(() => {
  if (window.p5 && canvas.value) {
    initializeP5()
  }
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  if (p5Instance) {
    p5Instance.remove()
  }
})

function initializeP5() {
  p5Instance = new window.p5((p: any) => {
    let angle = 0
    let mouseInfluence = 0
    
    p.setup = () => {
      p.createCanvas(props.size, props.size).parent(canvas.value)
      p.colorMode(p.HSB, 360, 100, 100, 100)
      p.noFill()
    }
    
    p.draw = () => {
      p.clear()
      p.translate(p.width / 2, p.height / 2)
      
      // Mouse interaction if enabled
      if (props.interactive) {
        const distance = p.dist(p.mouseX, p.mouseY, p.width / 2, p.height / 2)
        mouseInfluence = p.map(distance, 0, p.width / 2, 1, 0.3)
      } else {
        mouseInfluence = 1
      }
      
      drawPattern(p, angle * mouseInfluence)
      angle += props.speed * 0.5
    }
    
    function drawPattern(p: any, currentAngle: number) {
      switch (props.pattern) {
        case 'flower-of-life':
          drawFlowerOfLife(p, currentAngle)
          break
        case 'metatrons-cube':
          drawMetatronsCube(p, currentAngle)
          break
        case 'sri-yantra':
          drawSriYantra(p, currentAngle)
          break
        case 'tree-of-life':
          drawTreeOfLife(p, currentAngle)
          break
      }
    }
    
    function drawFlowerOfLife(p: any, currentAngle: number) {
      const radius = props.size * 0.15
      
      // Center circle
      p.strokeWeight(2)
      p.stroke(280, 80, 90, 60)
      p.circle(0, 0, radius * 2)
      
      // Six surrounding circles
      for (let i = 0; i < 6; i++) {
        const angle = (60 * i + currentAngle) * (Math.PI / 180)
        const x = Math.cos(angle) * radius
        const y = Math.sin(angle) * radius
        
        const hue = (240 + i * 30 + currentAngle) % 360
        p.stroke(hue, 70, 85, 40)
        p.strokeWeight(1.5)
        p.circle(x, y, radius * 2)
        
        // Outer layer
        for (let j = 0; j < 6; j++) {
          const outerAngle = (60 * j + currentAngle * 0.5) * (Math.PI / 180)
          const outerX = x + Math.cos(outerAngle) * radius
          const outerY = y + Math.sin(outerAngle) * radius
          
          p.stroke(hue + 60, 60, 70, 20)
          p.strokeWeight(1)
          p.circle(outerX, outerY, radius)
        }
      }
    }
    
    function drawMetatronsCube(p: any, currentAngle: number) {
      const points = []
      const centerRadius = props.size * 0.2
      
      // Create 13 circles (Metatron's Cube)
      points.push({ x: 0, y: 0 }) // Center
      
      // Inner hexagon
      for (let i = 0; i < 6; i++) {
        const angle = (60 * i + currentAngle) * (Math.PI / 180)
        points.push({
          x: Math.cos(angle) * centerRadius,
          y: Math.sin(angle) * centerRadius
        })
      }
      
      // Outer hexagon
      for (let i = 0; i < 6; i++) {
        const angle = (60 * i + currentAngle * 0.8) * (Math.PI / 180)
        points.push({
          x: Math.cos(angle) * centerRadius * 2,
          y: Math.sin(angle) * centerRadius * 2
        })
      }
      
      // Draw circles
      points.forEach((point, index) => {
        const hue = (index * 25 + currentAngle) % 360
        p.stroke(hue, 70, 80, 50)
        p.strokeWeight(2)
        p.circle(point.x, point.y, centerRadius * 0.4)
      })
      
      // Draw connecting lines
      p.strokeWeight(1)
      for (let i = 0; i < points.length; i++) {
        for (let j = i + 1; j < points.length; j++) {
          const distance = Math.sqrt(
            Math.pow(points[i].x - points[j].x, 2) + 
            Math.pow(points[i].y - points[j].y, 2)
          )
          
          if (distance < centerRadius * 2.5) {
            const hue = (i * 20 + j * 15 + currentAngle) % 360
            p.stroke(hue, 50, 60, 15)
            p.line(points[i].x, points[i].y, points[j].x, points[j].y)
          }
        }
      }
    }
    
    function drawSriYantra(p: any, currentAngle: number) {
      const size = props.size * 0.3
      
      // Upward triangles (Shiva)
      for (let i = 0; i < 5; i++) {
        p.push()
        p.rotate((currentAngle + i * 72) * (Math.PI / 180))
        p.stroke(350, 80, 80, 30 + i * 10)
        p.strokeWeight(2 - i * 0.2)
        p.triangle(0, -size + i * 30, -size/2 + i * 20, size/2 - i * 20, size/2 - i * 20, size/2 - i * 20)
        p.pop()
      }
      
      // Downward triangles (Shakti)
      for (let i = 0; i < 4; i++) {
        p.push()
        p.rotate((currentAngle * -1 + i * 90) * (Math.PI / 180))
        p.stroke(200, 80, 80, 30 + i * 15)
        p.strokeWeight(2 - i * 0.3)
        p.triangle(0, size - i * 35, -size/2 + i * 25, -size/2 + i * 25, size/2 - i * 25, -size/2 + i * 25)
        p.pop()
      }
      
      // Central bindu
      p.stroke(60, 90, 90, 80)
      p.strokeWeight(4)
      p.point(0, 0)
    }
    
    function drawTreeOfLife(p: any, currentAngle: number) {
      const positions = [
        { x: 0, y: -props.size * 0.25, name: 'Kether' },
        { x: -props.size * 0.15, y: -props.size * 0.15, name: 'Binah' },
        { x: props.size * 0.15, y: -props.size * 0.15, name: 'Chokmah' },
        { x: -props.size * 0.08, y: -props.size * 0.05, name: 'Geburah' },
        { x: props.size * 0.08, y: -props.size * 0.05, name: 'Chesed' },
        { x: 0, y: 0, name: 'Tiphareth' },
        { x: -props.size * 0.15, y: props.size * 0.1, name: 'Hod' },
        { x: props.size * 0.15, y: props.size * 0.1, name: 'Netzach' },
        { x: 0, y: props.size * 0.18, name: 'Yesod' },
        { x: 0, y: props.size * 0.3, name: 'Malkuth' }
      ]
      
      // Draw connections (paths)
      const connections = [
        [0, 1], [0, 2], [1, 2], [1, 3], [2, 4], [3, 4], 
        [3, 5], [4, 5], [1, 5], [2, 5], [5, 6], [5, 7],
        [6, 7], [6, 8], [7, 8], [8, 9], [5, 8]
      ]
      
      p.strokeWeight(1)
      connections.forEach((connection, index) => {
        const [start, end] = connection
        const hue = (index * 15 + currentAngle) % 360
        p.stroke(hue, 60, 70, 25)
        p.line(
          positions[start].x, positions[start].y,
          positions[end].x, positions[end].y
        )
      })
      
      // Draw sephiroth (spheres)
      positions.forEach((pos, index) => {
        const hue = (index * 36 + currentAngle) % 360
        p.stroke(hue, 80, 85, 60)
        p.strokeWeight(2)
        p.circle(pos.x, pos.y, 30)
        
        // Inner glow
        p.stroke(hue, 60, 95, 30)
        p.strokeWeight(1)
        p.circle(pos.x, pos.y, 20)
      })
    }
  })
}
</script>

<template>
  <div class="sacred-geometry-background">
    <canvas ref="canvas" class="geometry-canvas"></canvas>
  </div>
</template>

<style scoped>
.sacred-geometry-background {
  position: relative;
  border-radius: var(--radius-xl);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.02);
  backdrop-filter: var(--blur-sm);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.geometry-canvas {
  display: block;
  width: 100%;
  height: auto;
  opacity: 0.8;
  transition: opacity var(--transition-normal);
}

.sacred-geometry-background:hover .geometry-canvas {
  opacity: 1;
}
</style>
