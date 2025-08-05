<template>
  <div class="not-found-view">
    <div class="container">
      <div class="not-found-content">
        <div class="sacred-symbol">
          <svg width="120" height="120" viewBox="0 0 120 120">
            <circle cx="60" cy="60" r="50" fill="none" stroke="currentColor" stroke-width="2"/>
            <circle cx="60" cy="30" r="20" fill="none" stroke="currentColor" stroke-width="1.5"/>
            <circle cx="40" cy="75" r="20" fill="none" stroke="currentColor" stroke-width="1.5"/>
            <circle cx="80" cy="75" r="20" fill="none" stroke="currentColor" stroke-width="1.5"/>
          </svg>
        </div>
        
        <h1 class="error-code">404</h1>
        <h2 class="error-title">Path Not Found</h2>
        <p class="error-message">
          This page has wandered beyond the sacred geometry of our reality.
          Perhaps it exists in another dimension of consciousness.
        </p>
        
        <div class="error-actions">
          <RouterLink to="/" class="btn btn-primary">
            Return Home
          </RouterLink>
          <RouterLink to="/geometry" class="btn btn-secondary">
            Explore Sacred Patterns
          </RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 404 Not Found page
</script>

<style scoped>
.not-found-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 0;
}

.not-found-content {
  text-align: center;
  max-width: 500px;
}

.sacred-symbol {
  margin-bottom: 2rem;
  color: var(--color-primary);
  opacity: 0.7;
}

.error-code {
  font-size: 6rem;
  font-weight: 700;
  margin-bottom: 1rem;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.error-title {
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: var(--color-text);
}

.error-message {
  color: var(--color-text-muted);
  line-height: 1.6;
  margin-bottom: 2rem;
  font-size: 1.125rem;
}

.error-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

@media (max-width: 480px) {
  .error-code {
    font-size: 4rem;
  }
  
  .error-actions {
    flex-direction: column;
    align-items: center;
  }
}
</style>
