import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import api from '../../services/api'
import type { Page, Endpoint } from '../../types'
import clsx from 'clsx'

const OS_ICON: Record<string, string> = {
  WINDOWS: '🪟', LINUX: '🐧', MACOS: '🍎', UNKNOWN: '❓',
}

const STATUS_BADGE: Record<string, string> = {
  ONLINE:       'text-green-400',
  OFFLINE:      'text-gray-500',
  ISOLATED:     'text-amber-400',
  DECOMMISSIONED: 'text-red-400',
}

function RiskBar({ score }: { score: number }) {
  const color = score >= 80 ? 'bg-red-500' : score >= 50 ? 'bg-orange-500' : 'bg-green-500'
  return (
    <div className="flex items-center gap-2">
      <div className="flex-1 h-1.5 bg-gray-800 rounded-full">
        <div className={clsx('h-1.5 rounded-full', color)} style={{ width: `${score}%` }} />
      </div>
      <span className="text-xs text-gray-400 w-6 text-right">{score}</span>
    </div>
  )
}

export default function EndpointsPage() {
  const qc = useQueryClient()

  const { data, isLoading } = useQuery<Page<Endpoint>>({
    queryKey: ['endpoints'],
    queryFn: () => api.get('/endpoints?size=50').then(r => r.data),
    refetchInterval: 15_000,
  })

  const isolate = useMutation({
    mutationFn: (id: string) => api.post(`/endpoints/${id}/isolate`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['endpoints'] }),
  })

  const restore = useMutation({
    mutationFn: (id: string) => api.post(`/endpoints/${id}/restore`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['endpoints'] }),
  })

  if (isLoading) {
    return <div className="p-6 text-gray-500">Loading…</div>
  }

  const endpoints = data?.content ?? []

  return (
    <div className="p-6 space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-white">Endpoints</h1>
          <p className="text-gray-500 text-sm">
            {endpoints.length} endpoints monitored
          </p>
        </div>
      </div>

      <div className="card overflow-hidden p-0">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-800 text-left">
              {['OS', 'Hostname', 'IP Address', 'Status', 'Risk Score', 'Last Seen', 'Actions'].map(h => (
                <th key={h} className="px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-800">
            {endpoints.map(ep => (
              <tr key={ep.id} className="hover:bg-gray-800/50 transition-colors">
                <td className="px-4 py-3 text-lg">{OS_ICON[ep.osType]}</td>
                <td className="px-4 py-3">
                  <div className="font-medium text-white">{ep.hostname}</div>
                  <div className="text-xs text-gray-600">{ep.agentId}</div>
                </td>
                <td className="px-4 py-3 text-gray-400 font-mono text-xs">{ep.ipAddress || '—'}</td>
                <td className="px-4 py-3">
                  <span className={clsx('text-xs font-semibold', STATUS_BADGE[ep.status])}>
                    ● {ep.status}
                  </span>
                </td>
                <td className="px-4 py-3 w-32">
                  <RiskBar score={ep.riskScore} />
                </td>
                <td className="px-4 py-3 text-xs text-gray-500">
                  {ep.lastSeen ? new Date(ep.lastSeen).toLocaleString() : '—'}
                </td>
                <td className="px-4 py-3">
                  {ep.status !== 'ISOLATED' ? (
                    <button
                      onClick={() => isolate.mutate(ep.id)}
                      className="text-xs text-amber-400 hover:text-amber-300"
                    >
                      Isolate
                    </button>
                  ) : (
                    <button
                      onClick={() => restore.mutate(ep.id)}
                      className="text-xs text-green-400 hover:text-green-300"
                    >
                      Restore
                    </button>
                  )}
                </td>
              </tr>
            ))}
            {endpoints.length === 0 && (
              <tr>
                <td colSpan={7} className="px-4 py-8 text-center text-gray-600">
                  No endpoints registered yet. Deploy the agent to get started.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
