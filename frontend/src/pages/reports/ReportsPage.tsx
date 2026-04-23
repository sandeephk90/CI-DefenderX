import { useQuery } from '@tanstack/react-query'
import api from '../../services/api'
import type { DashboardSummary } from '../../types'
import ThreatTrendChart from '../../charts/ThreatTrendChart'
import SeverityDonutChart from '../../charts/SeverityDonutChart'
import ThreatTypeBarChart from '../../charts/ThreatTypeBarChart'

const COMPLIANCE = [
  { name: 'ISO 27001', status: 'Monitored', coverage: 78 },
  { name: 'NIST CSF',  status: 'Monitored', coverage: 82 },
  { name: 'PCI-DSS',   status: 'Partial',   coverage: 61 },
  { name: 'SOC 2',     status: 'Partial',   coverage: 70 },
  { name: 'RBI Guidelines', status: 'Monitored', coverage: 75 },
]

export default function ReportsPage() {
  const { data } = useQuery<DashboardSummary>({
    queryKey: ['dashboard'],
    queryFn: () => api.get('/dashboard/summary').then(r => r.data),
  })

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-xl font-bold text-white">Reports & Compliance</h1>
        <p className="text-gray-500 text-sm">Security posture and compliance coverage</p>
      </div>

      {/* Compliance table */}
      <div className="card">
        <h3 className="text-sm font-semibold text-gray-300 mb-4">Compliance Framework Coverage</h3>
        <div className="space-y-3">
          {COMPLIANCE.map(f => (
            <div key={f.name} className="flex items-center gap-4">
              <div className="w-36 text-sm text-gray-300">{f.name}</div>
              <div className="flex-1 h-2 bg-gray-800 rounded-full">
                <div
                  className="h-2 rounded-full bg-brand-500"
                  style={{ width: `${f.coverage}%` }}
                />
              </div>
              <div className="w-10 text-right text-xs text-gray-400">{f.coverage}%</div>
              <span className={`text-xs ${f.status === 'Monitored' ? 'text-green-400' : 'text-amber-400'}`}>
                {f.status}
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* Charts */}
      {data && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <div className="card">
            <h3 className="text-sm font-semibold text-gray-300 mb-3">7-Day Threat Volume</h3>
            <ThreatTrendChart data={data.threatTrend} />
          </div>
          <div className="card">
            <h3 className="text-sm font-semibold text-gray-300 mb-3">Open Threat Severity</h3>
            <SeverityDonutChart data={data.severityDistribution} />
          </div>
          <div className="card lg:col-span-2">
            <h3 className="text-sm font-semibold text-gray-300 mb-3">Attack Type Distribution</h3>
            <ThreatTypeBarChart data={data.threatTypeDistribution} />
          </div>
        </div>
      )}

      {/* Report download buttons */}
      <div className="card">
        <h3 className="text-sm font-semibold text-gray-300 mb-4">Available Reports</h3>
        <div className="grid grid-cols-2 lg:grid-cols-3 gap-3">
          {[
            'Endpoint Risk Summary',
            'Threat Detection Report',
            'Malware Activity Report',
            'Suspicious Login Activity',
            'Monthly Security Posture',
            'Attack Vector Analysis',
          ].map(name => (
            <div key={name} className="flex items-center justify-between p-3 bg-gray-800 rounded-lg">
              <span className="text-sm text-gray-300">{name}</span>
              <button className="text-xs text-brand-400 hover:text-brand-300 ml-2">Export</button>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
