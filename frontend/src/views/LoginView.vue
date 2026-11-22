<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  loading.value = true
  try {
    await auth.login(username.value, password.value)
    await router.replace('/')
  } catch {
    error.value = 'Sign-in failed. Check your username and password.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-shell">
    <section class="login-card">
      <a class="brand" href="/">
        <span class="brand-mark">A</span>
        <span>asset<span class="brand-light">pulse</span></span>
      </a>
      <p class="eyebrow">MAINTENANCE OPERATIONS</p>
      <h1>Good to have<br />you back.</h1>
      <p class="muted">Sign in to your operations workspace.</p>
      <form class="login-form" @submit.prevent="submit">
        <label for="username">Username</label>
        <input id="username" v-model="username" autocomplete="username" required placeholder="Your username" />
        <label for="password">Password</label>
        <input id="password" v-model="password" type="password" autocomplete="current-password" required placeholder="Your password" />
        <p v-if="error" class="form-error" role="alert">{{ error }}</p>
        <button class="button button-primary full-width" :disabled="loading">
          {{ loading ? 'Signing in…' : 'Sign in' }} <span aria-hidden="true">→</span>
        </button>
      </form>
      <p class="login-foot">Secure access for your maintenance team</p>
    </section>
    <aside class="login-art">
      <div class="art-orbit orbit-one"></div>
      <div class="art-orbit orbit-two"></div>
      <div class="art-content">
        <span class="art-label">ASSET INTELLIGENCE</span>
        <h2>Less downtime.<br />More uptime.</h2>
        <p>Every asset, reading and repair—connected in one clear view.</p>
        <div class="art-stat"><span class="pulse-dot"></span> Systems operational</div>
      </div>
      <span class="art-index">01 — 03</span>
    </aside>
  </main>
</template>
