import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import api from '../../services/api'
import type { Page, Threat } from '../../types'
import SeverityBadge from '../../components/common/SeverityBadge'
import clsx from 'clsx'

const STATUS_STYLE: Record<string, string> = {
  OPEN:           'text-red-400',
  INVESTIGATING:  'text-amber-400',
  RESOLVED:       'text-green-400',
  FALSE_POSITIVE: 'text-gray-500',
}

export default function ThreatsPage() {
  const qc = useQueryClient()
  const [filter, setFilter] = useState<string>('')
  const [selected, setSelected] = useState<Threat | null>(null)

  const { data, isLoading } = useQuery<Page<Threat>>({
    queryKey: ['threats', filter],
    queryFn: () =>
      api.get(`/threats?size=50${filter ? `&status=${filter}` : ''}`).then(r => r.data),
    refetchInterval: 20_000,
  })

  const updateStatus = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) =>
      api.patch(`/threats/${id}/status`, { status }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['threats'] })
      setSelected(null)
    },
  })

  const threats = data?.content ?? []

  return (
    <div className="p-6 space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-white">Threats</h1>
          <p className="text-gray-500 text-sm">{data?.totalElements ?? 0} total detections</p>
        </div>
        <div className="flex gap-2">
          {['', 'OPEN', 'INVESTIGATING', 'RESOLVED'].map(s => (
            <button
              key={s}
              onClick={() => setFilter(s)}
              className={clsx(
                'px-3 py-1.5 rounded-lg text-xs font-medium transition-colors',
                filter === s ? 'bg-brand-600 text-white' : 'bg-gray-800 text-gray-400 hover:bg-gray-700'
              )}
            >
              {s || 'All'}
            </button>
          ))}
        </div>
      </div>

      {isLoading ? (
        <div className="text-gray-500">Loading…</div>
      ) : (
        <div className="card overflow-hidden p-0">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-800 text-left">
                {['Severity', 'Title', 'Type', 'Endpoint', 'MITRE', 'Status', 'Detected', 'Action'].map(h => (
                  <th key={h} className="px-4 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide">
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-800">
              {threats.map(t => (
                <tr
                  key={t.id}
                  className="hover:bg-gray-800/50 cursor-pointer transition-colors"
                  onClick={() => setSelected(t)}
                >
                  <td className="px-4 py-3">
                    <SeverityBadge severity={t.severity} />
                  </td>
                  <td className="px-4 py-3 max-w-xs">
                    <div className="font-medium text-white truncate">{t.title}</div>
                    <div className="text-xs text-gray-600 truncate">{t.ruleName}</div>
                  </td>
                  <td className="px-4 py-3 text-xs text-gray-400">{t.threatType.replace(/_/g, ' ')}</td>
                  <td className="px-4 py-3 text-xs text-gray-400">{t.endpoint?.hostname ?? '—'}</td>
                  <td className="px-4 py-3 text-xs font-mono text-brand-400">{t.mitreTechnique || '—'}</td>
                  <td className="px-4 py-3">
                    <span className={clsx('text-xs font-semibold', STATUS_STYLE[t.status])}>
                      {t.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-xs text-gray-500">
                    {new Date(t.detectedAt).toLocaleString()}
                  </td>
                  <td className="px-4 py-3">
                    {t.status === 'OPEN' && (
                      <button
                        onClick={e => { e.stopPropagation(); updateStatus.mutate({ id: t.id, status: 'INVESTIGATING' }) }}
                        className="text-xs text-amber-400 hover:text-amber-300"
                      >
                        Investigate
                      </button>
                    )}
                    {t.status === 'INVESTIGATING' && (
                      <button
                        onClick={e => { e.stopPropagation(); updateStatus.mutate({ id: t.id, status: 'RESOLVED' }) }}
                        className="text-xs text-green-400 hover:text-green-300"
                      >
                        Resolve
                      </button>
                    )}
                  </td>
                </tr>
              ))}
              {threats.length === 0 && (
                <tr>
                  <td colSpan={8} className="px-4 py-8 text-center text-gray-600">
                    No threats found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Detail panel */}
      {selected && (
        <div className="fixed inset-0 bg-black/60 flex items-end sm:items-center justify-center z-50 p-4" onClick={() => setSelected(null)}>
          <div className="card w-full max-w-lg max-h-[80vh] overflow-y-auto" onClick={e => e.stopPropagation()}>
            <div className="flex items-start justify-between mb-4">
              <SeverityBadge severity={selected.severity} />
              <button onClick={() => setSelected(null)} className="text-gray-500 hover:text-gray-300">✕</button>
            </div>
            <h2 className="text-base font-bold text-white mb-2">{selected.title}</h2>
            <p className="text-sm text-gray-400 mb-4">{selected.description}</p>
            <div className="grid grid-cols-2 gap-3 text-xs">
              <div><span className="text-gray-600">Type</span><div className="text-gray-200 mt-0.5">{selected.threatType}</div></div>
              <div><span className="text-gray-600">Risk Score</span><div className="text-gray-200 mt-0.5">{selected.riskScore}/100</div></div>
              <div><span className="text-gray-600">MITRE ATT&CK</span><div className="text-brand-400 mt-0.5 font-mono">{selected.mitreTechnique || '—'}</div></div>
              <div><span className="text-gray-600">Attack Vector</span><div className="text-gray-200 mt-0.5">{selected.attackVector || '—'}</div></div>
              <div><span className="text-gray-600">Rule</span><div className="text-gray-200 mt-0.5">{selected.ruleName}</div></div>
              <div><span className="text-gray-600">Endpoint</span><div className="text-gray-200 mt-0.5">{selected.endpoint?.hostname}</div></div>
            </div>
            <div className="mt-4 flex gap-2">
              {selected.status === 'OPEN' && (
                <button
                  onClick={() => updateStatus.mutate({ id: selected.id, status: 'INVESTIGATING' })}
                  className="btn-primary text-xs"
                >
                  Start Investigation
                </button>
              )}
              {selected.status !== 'RESOLVED' && (
                <button
                  onClick={() => updateStatus.mutate({ id: selected.id, status: 'RESOLVED' })}
                  className="btn-ghost text-xs"
                >
                  Mark Resolved
                </button>
              )}
              <button
                onClick={() => updateStatus.mutate({ id: selected.id, status: 'FALSE_POSITIVE' })}
                className="btn-ghost text-xs"
              >
                False Positive
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
