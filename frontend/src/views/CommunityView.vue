<template>
  <div class="community-view">
    <div class="container">
      <h1 class="page-title">Syntopia Community</h1>
      <p class="page-subtitle">Connect with fellow consciousness explorers</p>
      
      <div class="coming-soon card">
        <h2>🌟 Coming Soon</h2>
        <p>The Syntopia Community platform is being carefully crafted to foster meaningful connections.</p>
        <p>Soon you'll be able to:</p>
        <ul>
          <li>Join Sacred Role discussion groups</li>
          <li>Share your geometry insights</li>
          <li>Collaborate on consciousness projects</li>
          <li>Participate in virtual meditation circles</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Community functionality will be implemented here
</script>

<style scoped>
.community-view {
  min-height: 100vh;
  padding: 2rem 0;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 600;
  margin-bottom: 1rem;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-align: center;
}

.page-subtitle {
  font-size: 1.125rem;
  color: var(--color-text-muted);
  text-align: center;
  margin-bottom: 3rem;
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
  margin-bottom: 1rem;
  line-height: 1.6;
}

.coming-soon ul {
  text-align: left;
  max-width: 400px;
  margin: 2rem auto;
}

.coming-soon li {
  color: var(--color-text-muted);
  margin-bottom: 0.5rem;
  line-height: 1.5;
}
</style>
