import ReactECharts from 'echarts-for-react'

interface Props {
  data: Record<string, number>
}

const OS_COLORS: Record<string, string> = {
  WINDOWS: '#0ea5e9',
  LINUX:   '#f97316',
  MACOS:   '#a855f7',
  UNKNOWN: '#6b7280',
}

export default function OsDistributionChart({ data }: Props) {
  const seriesData = Object.entries(data).map(([name, value]) => ({
    name,
    value,
    itemStyle: { color: OS_COLORS[name] ?? '#6b7280' },
  }))

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: '#1f2937',
      borderColor: '#374151',
      textStyle: { color: '#e5e7eb' },
      formatter: '{b}: {c} ({d}%)',
    },
    legend: {
      bottom: 0,
      textStyle: { color: '#9ca3af', fontSize: 11 },
    },
    series: [
      {
        type: 'pie',
        radius: '65%',
        center: ['50%', '42%'],
        label: { show: false },
        emphasis: {
          label: { show: true, color: '#f9fafb', fontWeight: 'bold' },
        },
        data: seriesData,
      },
    ],
  }

  return <ReactECharts option={option} style={{ height: '200px' }} />
}
