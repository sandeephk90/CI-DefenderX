export type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export type EndpointStatus = 'ONLINE' | 'OFFLINE' | 'ISOLATED' | 'DECOMMISSIONED'
export type OsType = 'WINDOWS' | 'LINUX' | 'MACOS' | 'UNKNOWN'
export type ThreatStatus = 'OPEN' | 'INVESTIGATING' | 'RESOLVED' | 'FALSE_POSITIVE'

export interface Endpoint {
  id: string
  agentId: string
  hostname: string
  ipAddress: string
  osType: OsType
  osVersion: string
  status: EndpointStatus
  riskScore: number
  agentVersion: string
  lastSeen: string
  registeredAt: string
  tags: string[]
}

export interface Threat {
  id: string
  endpoint: Endpoint
  threatType: string
  severity: Severity
  title: string
  description: string
  status: ThreatStatus
  ruleName: string
  attackVector: string
  mitreTechnique: string
  riskScore: number
  detectedAt: string
  resolvedAt: string | null
  analystNotes: string | null
}

export interface TrendPoint {
  date: string
  count: number
}

export interface DashboardSummary {
  totalEndpoints: number
  onlineEndpoints: number
  isolatedEndpoints: number
  openThreats: number
  criticalThreats: number
  highThreats: number
  highRiskEndpointCount: number
  severityDistribution: Record<string, number>
  threatTypeDistribution: Record<string, number>
  osDistribution: Record<string, number>
  threatTrend: TrendPoint[]
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}
