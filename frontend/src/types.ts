export type AssetStatus = 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE'
export type WorkOrderStatus = 'OPEN' | 'ASSIGNED' | 'IN_PROGRESS' | 'DONE'
export type WorkOrderPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export interface Asset {
  id: number
  name: string
  assetTag: string
  type: string
  location: string
  status: AssetStatus
  threshold: number
  createdAt: string
}

export interface WorkOrder {
  id: number
  title: string
  description: string
  status: WorkOrderStatus
  priority: WorkOrderPriority
  assetId: number
  assetName: string
  technicianId: number | null
  technicianName: string | null
  createdAt: string
  updatedAt: string
}

export interface Page<T> {
  content: T[]
  totalElements: number
}

export interface MaintenanceInsight {
  assetId: number
  assetName: string
  metric: string
  sampleCount: number
  currentThreshold: number
  suggestedThreshold: number | null
  explanation: string
}
