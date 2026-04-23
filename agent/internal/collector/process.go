package collector

import (
	"context"
	"fmt"
	"time"

	"github.com/shirou/gopsutil/v3/process"
	"go.uber.org/zap"
)

var suspiciousProcessNames = map[string]bool{
	"mimikatz": true, "meterpreter": true, "netcat": true, "nc": true,
	"nmap": true, "psexec": true, "wce": true, "fgdump": true,
	"pwdump": true, "gsecdump": true, "lsass": true,
}

type ProcessCollector struct {
	logger *zap.Logger
}

func NewProcessCollector(logger *zap.Logger) *ProcessCollector {
	return &ProcessCollector{logger: logger}
}

func (c *ProcessCollector) Name() string { return "process" }

func (c *ProcessCollector) Collect(ctx context.Context) ([]Event, error) {
	procs, err := process.ProcessesWithContext(ctx)
	if err != nil {
		return nil, fmt.Errorf("list processes: %w", err)
	}

	events := make([]Event, 0)
	for _, p := range procs {
		name, _ := p.NameWithContext(ctx)
		username, _ := p.UsernameWithContext(ctx)
		cmdline, _ := p.CmdlineWithContext(ctx)

		evt := Event{
			Type:      EventTypeProcess,
			Timestamp: time.Now().UTC(),
			Source:    "process-collector",
			Severity:  SeverityLow,
			Message:   fmt.Sprintf("process: %s (pid=%d)", name, p.Pid),
			Data: map[string]any{
				"pid":      p.Pid,
				"name":     name,
				"username": username,
				"cmdline":  cmdline,
			},
			Tags: []string{"process"},
		}

		if suspiciousProcessNames[name] {
			evt.Severity = SeverityCritical
			evt.Tags = append(evt.Tags, "suspicious", "threat")
			evt.Message = fmt.Sprintf("SUSPICIOUS PROCESS detected: %s (pid=%d, user=%s)", name, p.Pid, username)
		}

		// Detect processes running as root/SYSTEM that look suspicious
		if username == "root" || username == "SYSTEM" {
			evt.Tags = append(evt.Tags, "privileged")
		}

		events = append(events, evt)
	}
	return events, nil
}
