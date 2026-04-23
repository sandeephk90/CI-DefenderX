import ReactECharts from 'echarts-for-react'

interface Props {
  data: Record<string, number>
}

export default function ThreatTypeBarChart({ data }: Props) {
  const entries = Object.entries(data).sort((a, b) => b[1] - a[1]).slice(0, 8)

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#1f2937',
      borderColor: '#374151',
      textStyle: { color: '#e5e7eb' },
    },
    grid: { left: 120, right: 16, top: 8, bottom: 8 },
    xAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#1f2937' } },
      axisLabel: { color: '#6b7280', fontSize: 10 },
    },
    yAxis: {
      type: 'category',
      data: entries.map(([k]) => k.replace(/_/g, ' ')),
      axisLine: { lineStyle: { color: '#374151' } },
      axisLabel: { color: '#9ca3af', fontSize: 11 },
    },
    series: [
      {
        type: 'bar',
        data: entries.map(([, v]) => v),
        barMaxWidth: 20,
        itemStyle: {
          color: {
            type: 'linear', x: 0, y: 0, x2: 1, y2: 0,
            colorStops: [
              { offset: 0, color: '#4f46e5' },
              { offset: 1, color: '#6366f1' },
            ],
          },
          borderRadius: [0, 4, 4, 0],
        },
      },
    ],
  }

  return <ReactECharts option={option} style={{ height: '240px' }} />
}
