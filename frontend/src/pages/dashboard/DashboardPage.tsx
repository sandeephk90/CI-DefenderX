import { useQuery } from '@tanstack/react-query'
import api from '../../services/api'
import type { DashboardSummary } from '../../types'
import StatCard from '../../components/common/StatCard'
import ThreatTrendChart from '../../charts/ThreatTrendChart'
import SeverityDonutChart from '../../charts/SeverityDonutChart'
import ThreatTypeBarChart from '../../charts/ThreatTypeBarChart'
import OsDistributionChart from '../../charts/OsDistributionChart'

export default function DashboardPage() {
  const { data, isLoading } = useQuery<DashboardSummary>({
    queryKey: ['dashboard'],
    queryFn: () => api.get('/dashboard/summary').then(r => r.data),
    refetchInterval: 30_000,
  })

  if (isLoading || !data) {
    return (
      <div className="flex items-center justify-center h-full text-gray-500">
        Loading…
      </div>
    )
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-white">Security Dashboard</h1>
          <p className="text-gray-500 text-sm mt-0.5">Real-time threat monitoring across all endpoints</p>
        </div>
        <div className="text-xs text-gray-600">Auto-refreshes every 30s</div>
      </div>

      {/* KPI row */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Total Endpoints"    value={data.totalEndpoints}        icon="⬡" />
        <StatCard label="Online Endpoints"   value={data.onlineEndpoints}       icon="🟢" variant="success" />
        <StatCard label="Open Threats"       value={data.openThreats}           icon="⚠" variant="warning" />
        <StatCard label="Critical Threats"   value={data.criticalThreats}       icon="🔴" variant="critical" />
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="High Severity"      value={data.highThreats}           icon="🟠" variant="warning" />
        <StatCard label="Isolated Endpoints" value={data.isolatedEndpoints}     icon="🔒" />
        <StatCard label="High-Risk Endpoints" value={data.highRiskEndpointCount} icon="💀" variant="critical" />
        <StatCard label="Endpoints Monitored" value={data.totalEndpoints}       icon="📡" />
      </div>

      {/* Charts row 1 */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <div className="card lg:col-span-2">
          <h3 className="text-sm font-semibold text-gray-300 mb-3">Threat Trend (7 Days)</h3>
          <ThreatTrendChart data={data.threatTrend} />
        </div>
        <div className="card">
          <h3 className="text-sm font-semibold text-gray-300 mb-3">Severity Distribution</h3>
          <SeverityDonutChart data={data.severityDistribution} />
        </div>
      </div>

      {/* Charts row 2 */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <div className="card lg:col-span-2">
          <h3 className="text-sm font-semibold text-gray-300 mb-3">Threat Types</h3>
          <ThreatTypeBarChart data={data.threatTypeDistribution} />
        </div>
        <div className="card">
          <h3 className="text-sm font-semibold text-gray-300 mb-3">OS Distribution</h3>
          <OsDistributionChart data={data.osDistribution} />
        </div>
      </div>
    </div>
  )
}
