package collector

import (
	"context"
	"fmt"
	"time"

	"github.com/shirou/gopsutil/v3/cpu"
	"github.com/shirou/gopsutil/v3/disk"
	"github.com/shirou/gopsutil/v3/host"
	"github.com/shirou/gopsutil/v3/mem"
	"go.uber.org/zap"
)

type SystemCollector struct {
	logger *zap.Logger
}

func NewSystemCollector(logger *zap.Logger) *SystemCollector {
	return &SystemCollector{logger: logger}
}

func (c *SystemCollector) Name() string { return "system" }

func (c *SystemCollector) Collect(ctx context.Context) ([]Event, error) {
	events := make([]Event, 0)

	hostInfo, err := host.InfoWithContext(ctx)
	if err != nil {
		return nil, fmt.Errorf("host info: %w", err)
	}

	cpuPct, _ := cpu.PercentWithContext(ctx, 0, false)
	vmStat, _ := mem.VirtualMemoryWithContext(ctx)
	diskStat, _ := disk.UsageWithContext(ctx, "/")

	cpuUsage := 0.0
	if len(cpuPct) > 0 {
		cpuUsage = cpuPct[0]
	}

	sev := SeverityLow
	tags := []string{"system", "health"}

	if cpuUsage > 90 {
		sev = SeverityMedium
		tags = append(tags, "high-cpu")
	}
	if vmStat != nil && vmStat.UsedPercent > 90 {
		sev = SeverityMedium
		tags = append(tags, "high-memory")
	}

	data := map[string]any{
		"hostname":     hostInfo.Hostname,
		"os":           hostInfo.OS,
		"platform":     hostInfo.Platform,
		"uptime_secs":  hostInfo.Uptime,
		"cpu_usage":    cpuUsage,
	}
	if vmStat != nil {
		data["mem_total"] = vmStat.Total
		data["mem_used"] = vmStat.Used
		data["mem_pct"] = vmStat.UsedPercent
	}
	if diskStat != nil {
		data["disk_total"] = diskStat.Total
		data["disk_used"] = diskStat.Used
		data["disk_pct"] = diskStat.UsedPercent
	}

	events = append(events, Event{
		Type:      EventTypeSystem,
		Timestamp: time.Now().UTC(),
		Source:    "system-collector",
		Severity:  sev,
		Message:   fmt.Sprintf("system health: cpu=%.1f%% uptime=%ds", cpuUsage, hostInfo.Uptime),
		Data:      data,
		Tags:      tags,
	})

	return events, nil
}
