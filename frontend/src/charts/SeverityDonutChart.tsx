import ReactECharts from 'echarts-for-react'

interface Props {
  data: Record<string, number>
}

const COLORS: Record<string, string> = {
  CRITICAL: '#ef4444',
  HIGH:     '#f97316',
  MEDIUM:   '#eab308',
  LOW:      '#22c55e',
}

export default function SeverityDonutChart({ data }: Props) {
  const seriesData = Object.entries(data).map(([name, value]) => ({
    name,
    value,
    itemStyle: { color: COLORS[name] ?? '#6b7280' },
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
      orient: 'vertical',
      right: 0,
      top: 'center',
      textStyle: { color: '#9ca3af', fontSize: 12 },
    },
    series: [
      {
        type: 'pie',
        radius: ['50%', '75%'],
        center: ['38%', '50%'],
        avoidLabelOverlap: false,
        label: { show: false },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold',
            color: '#f9fafb',
          },
        },
        data: seriesData,
      },
    ],
  }

  return <ReactECharts option={option} style={{ height: '200px' }} />
}
