import clsx from 'clsx'

interface Props {
  label: string
  value: number | string
  icon?: string
  trend?: 'up' | 'down' | 'neutral'
  variant?: 'default' | 'critical' | 'warning' | 'success'
}

const variantStyles = {
  default:  'border-gray-800',
  critical: 'border-red-900 bg-red-950/30',
  warning:  'border-orange-900 bg-orange-950/20',
  success:  'border-green-900 bg-green-950/20',
}

export default function StatCard({ label, value, icon, variant = 'default' }: Props) {
  return (
    <div className={clsx('card flex items-center gap-4', variantStyles[variant])}>
      {icon && <span className="text-3xl shrink-0">{icon}</span>}
      <div>
        <div className="text-2xl font-bold text-white">{value.toLocaleString()}</div>
        <div className="text-xs text-gray-400 mt-0.5">{label}</div>
      </div>
    </div>
  )
}
