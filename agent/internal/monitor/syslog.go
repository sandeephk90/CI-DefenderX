package monitor

import (
	"bufio"
	"context"
	"os"
	"strings"

	"go.uber.org/zap"
)

var syslogPaths = []string{"/var/log/auth.log", "/var/log/secure"}

type SyslogMonitor struct {
	logger *zap.Logger
}

func NewSyslogMonitor(logger *zap.Logger) *SyslogMonitor {
	return &SyslogMonitor{logger: logger}
}

func (m *SyslogMonitor) Name() string { return "syslog" }

func (m *SyslogMonitor) Start(ctx context.Context) {
	for _, path := range syslogPaths {
		if _, err := os.Stat(path); err == nil {
			go m.tail(ctx, path)
		}
	}
}

func (m *SyslogMonitor) tail(ctx context.Context, path string) {
	f, err := os.Open(path)
	if err != nil {
		return
	}
	defer f.Close()

	// Seek to end
	f.Seek(0, 2)

	scanner := bufio.NewScanner(f)
	for {
		select {
		case <-ctx.Done():
			return
		default:
			if scanner.Scan() {
				line := scanner.Text()
				m.analyze(line)
			}
		}
	}
}

func (m *SyslogMonitor) analyze(line string) {
	lower := strings.ToLower(line)

	switch {
	case strings.Contains(lower, "failed password") || strings.Contains(lower, "authentication failure"):
		m.logger.Warn("SSH brute force indicator", zap.String("log", line))

	case strings.Contains(lower, "sudo:") && strings.Contains(lower, "command not allowed"):
		m.logger.Warn("Privilege escalation attempt", zap.String("log", line))

	case strings.Contains(lower, "accepted publickey") || strings.Contains(lower, "accepted password"):
		m.logger.Info("Successful SSH login", zap.String("log", line))

	case strings.Contains(lower, "su:") && strings.Contains(lower, "session opened for user root"):
		m.logger.Warn("Root session opened via su", zap.String("log", line))
	}
}
