import { defineStore } from 'pinia'
import api from '../api/client'
import type { Asset, MaintenanceInsight, Page, WorkOrder, WorkOrderStatus } from '../types'

export const useOperationsStore = defineStore('operations', {
  state: () => ({
    assets: [] as Asset[],
    workOrders: [] as WorkOrder[],
    loading: false,
    error: '',
  }),
  getters: {
    activeAssetCount: (state) => state.assets.filter((asset) => asset.status === 'ACTIVE').length,
    openOrderCount: (state) => state.workOrders.filter((order) => order.status !== 'DONE').length,
    criticalOrderCount: (state) => state.workOrders.filter((order) => order.priority === 'CRITICAL' && order.status !== 'DONE').length,
  },
  actions: {
    async load(query = '') {
      this.loading = true
      this.error = ''
      try {
        const [assets, workOrders] = await Promise.all([
          api.get<Page<Asset>>('/api/assets', { params: { size: 100, sort: 'createdAt,desc' } }),
          api.get<Page<WorkOrder>>('/api/work-orders', {
            params: { size: 100, sort: 'createdAt,desc', ...(query ? { query } : {}) },
          }),
        ])
        this.assets = assets.data.content
        this.workOrders = workOrders.data.content
      } catch (error: unknown) {
        this.error = error instanceof Error ? error.message : 'Could not load operations data. Check that the API is running and try again.'
      } finally {
        this.loading = false
      }
    },
    async setStatus(id: number, status: WorkOrderStatus) {
      await api.patch(`/api/work-orders/${id}/status`, { status })
      const order = this.workOrders.find((item) => item.id === id)
      if (order) order.status = status
    },
    async sendReading(assetId: number, metric: string, value: number) {
      await api.post('/api/sensor-readings', {
        eventId: crypto.randomUUID(),
        assetId,
        metric,
        value,
        recordedAt: new Date().toISOString(),
      })
    },
    async getMaintenanceInsight(assetId: number, metric: string) {
      const { data } = await api.get<MaintenanceInsight>(
        `/api/assets/${assetId}/maintenance-insight`, { params: { metric } },
      )
      return data
    },
  },
})
