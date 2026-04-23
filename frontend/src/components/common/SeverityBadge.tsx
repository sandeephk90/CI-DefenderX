import type { Severity } from '../../types'

const MAP: Record<Severity, string> = {
  CRITICAL: 'badge-critical',
  HIGH:     'badge-high',
  MEDIUM:   'badge-medium',
  LOW:      'badge-low',
}

export default function SeverityBadge({ severity }: { severity: Severity }) {
  return <span className={MAP[severity]}>{severity}</span>
}
