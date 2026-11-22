<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useOperationsStore } from '../stores/operations'
import type { MaintenanceInsight, WorkOrder, WorkOrderStatus } from '../types'

const auth = useAuthStore()
const operations = useOperationsStore()
const router = useRouter()
const search = ref('')
const sensorAssetId = ref<number | ''>('')
const sensorValue = ref<number | null>(null)
const sensorMessage = ref('')
const sensorError = ref('')
const sensorBusy = ref(false)
const insight = ref<MaintenanceInsight | null>(null)
const insightBusy = ref(false)
const insightError = ref('')

const columns: { status: WorkOrderStatus; label: string; tone: string }[] = [
  { status: 'OPEN', label: 'Open', tone: 'amber' },
  { status: 'ASSIGNED', label: 'Assigned', tone: 'blue' },
  { status: 'IN_PROGRESS', label: 'In progress', tone: 'violet' },
  { status: 'DONE', label: 'Completed', tone: 'green' },
]

const groupedOrders = computed(() => Object.fromEntries(
  columns.map((column) => [column.status, operations.workOrders.filter((order) => order.status === column.status)]),
) as Record<WorkOrderStatus, WorkOrder[]>)

onMounted(() => operations.load())

async function findOrders() {
  await operations.load(search.value)
}

async function advance(order: WorkOrder) {
  const nextStatus: Partial<Record<WorkOrderStatus, WorkOrderStatus>> = {
    OPEN: 'ASSIGNED',
    ASSIGNED: 'IN_PROGRESS',
    IN_PROGRESS: 'DONE',
  }
  const next = nextStatus[order.status]
  if (next) {
    try {
      await operations.setStatus(order.id, next)
    } catch {
      operations.error = 'Could not update this work order.'
    }
  }
}

async function submitReading() {
  sensorMessage.value = ''
  sensorError.value = ''
  if (!sensorAssetId.value || sensorValue.value === null) return
  sensorBusy.value = true
  try {
    await operations.sendReading(sensorAssetId.value, 'temperature', sensorValue.value)
    sensorMessage.value = 'Reading queued. A work order appears if it exceeds the asset threshold.'
    sensorValue.value = null
  } catch {
    sensorError.value = 'Reading could not be sent. Check the API and try again.'
  } finally {
    sensorBusy.value = false
  }
}

async function generateInsight() {
  insight.value = null
  insightError.value = ''
  if (!sensorAssetId.value) return
  insightBusy.value = true
  try {
    insight.value = await operations.getMaintenanceInsight(sensorAssetId.value, 'temperature')
  } catch {
    insightError.value = 'Could not generate an insight for this asset.'
  } finally {
    insightBusy.value = false
  }
}

async function logout() {
  auth.logout()
  await router.replace('/login')
}
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <a class="brand" href="/">
        <span class="brand-mark">A</span>
        <span>asset<span class="brand-light">pulse</span></span>
      </a>
      <div class="workspace-label">WORKSPACE</div>
      <nav class="side-nav" aria-label="Main navigation">
        <a class="nav-link active" href="#overview"><span class="nav-icon">◫</span> Overview</a>
        <a class="nav-link" href="#assets"><span class="nav-icon">▦</span> Assets <span class="nav-count">{{ operations.assets.length }}</span></a>
        <a class="nav-link" href="#work-orders"><span class="nav-icon">☷</span> Work orders <span class="nav-count">{{ operations.openOrderCount }}</span></a>
      </nav>
      <div class="sidebar-bottom">
        <div class="team-card">
          <div class="team-icon">↗</div>
          <div><strong>Maintenance team</strong><span>Keep your plant moving</span></div>
        </div>
        <button class="profile-button" @click="logout">
          <span class="avatar">{{ auth.username.slice(0, 1).toUpperCase() || 'A' }}</span>
          <span class="profile-copy"><strong>{{ auth.username || 'Admin' }}</strong><small>Administrator</small></span>
          <span class="logout-icon">↗</span>
        </button>
      </div>
    </aside>

    <main id="overview" class="main-content">
      <header class="topbar">
        <div class="breadcrumb">Operations <span>/</span> <strong>Overview</strong></div>
        <div class="top-actions">
          <span class="live-indicator">Operations workspace</span>
          <span class="today">{{ new Date().toLocaleDateString('en', { weekday: 'short', month: 'short', day: 'numeric' }) }}</span>
        </div>
      </header>

      <section class="page-heading">
        <div>
          <p class="eyebrow">MAINTENANCE OPERATIONS</p>
          <h1>Operations overview<span class="heading-period">.</span></h1>
          <p class="muted">A clear view of your assets and what needs attention.</p>
        </div>
        <button class="button button-outline" @click="operations.load()"><span class="refresh-icon">↻</span> Refresh data</button>
      </section>

      <div v-if="operations.error" class="alert" role="alert">{{ operations.error }}</div>

      <section class="metrics-grid" aria-label="Operations summary">
        <article class="metric-card">
          <div class="metric-top"><span class="metric-label">Active assets</span><span class="metric-icon mint">▦</span></div>
          <div class="metric-value">{{ operations.activeAssetCount }}<span class="metric-unit"> / {{ operations.assets.length }}</span></div>
          <div class="metric-note"><span class="trend neutral">●</span> Registered in your workspace</div>
        </article>
        <article class="metric-card">
          <div class="metric-top"><span class="metric-label">Open work orders</span><span class="metric-icon orange">☷</span></div>
          <div class="metric-value">{{ operations.openOrderCount }}<span class="metric-unit"> active</span></div>
          <div class="metric-note"><span class="trend up">↗</span> Across all asset locations</div>
        </article>
        <article class="metric-card">
          <div class="metric-top"><span class="metric-label">Critical attention</span><span class="metric-icon red">!</span></div>
          <div class="metric-value">{{ operations.criticalOrderCount }}<span class="metric-unit"> priority</span></div>
          <div class="metric-note"><span class="trend neutral">●</span> Needs a technician review</div>
        </article>
        <article class="metric-card uptime-card">
          <div class="metric-top"><span class="metric-label">Threshold rules</span><span class="metric-icon green">⌁</span></div>
          <div class="metric-value"><span class="status-text">Enabled</span></div>
          <div class="metric-note"><span class="trend up">●</span> Automatically opens work orders</div>
        </article>
      </section>

      <section id="work-orders" class="board-section">
        <div class="section-heading">
          <div><p class="eyebrow">THE WORK, AT A GLANCE</p><h2>Work order board</h2></div>
          <form class="search-form" @submit.prevent="findOrders">
            <span>⌕</span>
            <input v-model="search" aria-label="Search work orders" placeholder="Search work orders" />
            <button type="submit">Search</button>
          </form>
        </div>
        <div v-if="operations.loading" class="loading-state">Loading your operations…</div>
        <div v-else class="kanban">
          <section v-for="column in columns" :key="column.status" class="kanban-column">
            <div class="column-heading">
              <span class="column-title"><i :class="`column-dot ${column.tone}`"></i>{{ column.label }}</span>
              <span class="column-count">{{ groupedOrders[column.status]?.length || 0 }}</span>
            </div>
            <div v-if="!groupedOrders[column.status]?.length" class="empty-column">Nothing here yet</div>
            <article v-for="order in groupedOrders[column.status]" :key="order.id" class="work-card">
              <div class="work-card-top">
                <span class="priority-pill" :class="order.priority.toLowerCase()">{{ order.priority }}</span>
                <span class="order-id">#{{ String(order.id).padStart(4, '0') }}</span>
              </div>
              <h3>{{ order.title }}</h3>
              <p class="work-description">{{ order.description }}</p>
              <div class="asset-line"><span>▦</span>{{ order.assetName }}</div>
              <div class="work-card-footer">
                <span class="technician"><span class="avatar mini">{{ order.technicianName?.slice(0, 1) || '?' }}</span>{{ order.technicianName || 'Unassigned' }}</span>
                <button v-if="order.status !== 'DONE'" class="advance-button" :aria-label="`Advance ${order.title}`" @click="advance(order)">→</button>
                <span v-else class="done-mark">✓</span>
              </div>
            </article>
          </section>
        </div>
      </section>

      <section id="assets" class="bottom-grid">
        <article class="panel asset-panel">
          <div class="panel-heading"><div><p class="eyebrow">YOUR EQUIPMENT</p><h2>Asset register</h2></div><span class="panel-count">{{ operations.assets.length }} assets</span></div>
          <div v-if="!operations.assets.length" class="empty-panel">Assets will appear here once they are registered.</div>
          <div v-for="asset in operations.assets.slice(0, 5)" :key="asset.id" class="asset-row">
            <div class="asset-symbol">{{ asset.type.slice(0, 1) }}</div>
            <div class="asset-copy"><strong>{{ asset.name }}</strong><span>{{ asset.assetTag }} · {{ asset.location }}</span></div>
            <span class="asset-state" :class="asset.status.toLowerCase()"><i></i>{{ asset.status }}</span>
          </div>
        </article>
        <article class="panel sensor-panel">
          <div class="panel-heading"><div><p class="eyebrow">EVENT-DRIVEN MAINTENANCE</p><h2>Send a sensor reading</h2></div><span class="sensor-symbol">⌁</span></div>
          <p class="sensor-copy">Try an event. Readings over an asset's threshold automatically open a work order.</p>
          <form class="sensor-form" @submit.prevent="submitReading">
            <label for="sensor-asset">Asset</label>
            <select id="sensor-asset" v-model="sensorAssetId" required>
              <option value="" disabled>Select an asset</option>
              <option v-for="asset in operations.assets" :key="asset.id" :value="asset.id">{{ asset.name }} (limit {{ asset.threshold }})</option>
            </select>
            <label for="sensor-value">Temperature reading</label>
            <div class="sensor-input-row">
              <input id="sensor-value" v-model.number="sensorValue" type="number" step="0.1" required placeholder="e.g. 85" />
              <button class="button button-primary" :disabled="sensorBusy || !operations.assets.length">{{ sensorBusy ? 'Sending…' : 'Send event' }} <span>→</span></button>
            </div>
            <p v-if="sensorMessage" class="form-success" role="status">{{ sensorMessage }}</p>
            <p v-if="sensorError" class="form-error" role="alert">{{ sensorError }}</p>
          </form>
          <div class="insight-divider"></div>
          <div class="insight-heading">
            <div><p class="eyebrow">PREDICTIVE MAINTENANCE</p><h3>Threshold suggestion</h3></div>
            <button class="insight-button" :disabled="insightBusy || !sensorAssetId" @click="generateInsight">
              {{ insightBusy ? 'Analyzing…' : 'Analyze readings' }} <span>→</span>
            </button>
          </div>
          <p v-if="insightError" class="form-error" role="alert">{{ insightError }}</p>
          <div v-if="insight" class="insight-result" role="status">
            <template v-if="insight.suggestedThreshold !== null">
              <strong>{{ insight.suggestedThreshold.toFixed(1) }}</strong>
              <span>Suggested {{ insight.metric }} threshold <i>·</i> {{ insight.sampleCount }} readings</span>
            </template>
            <span v-else class="insight-explanation">{{ insight.explanation }}</span>
            <small v-if="insight.suggestedThreshold !== null">{{ insight.explanation }}</small>
          </div>
        </article>
      </section>
      <footer class="footer"><span>ASSET<span class="brand-light">PULSE</span> · MAINTENANCE, IN MOTION</span><span>Built for reliable operations</span></footer>
    </main>
  </div>
</template>
