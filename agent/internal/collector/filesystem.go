package collector

import (
	"context"
	"fmt"
	"os"
	"path/filepath"
	"time"

	"go.uber.org/zap"
)

// Critical system paths to monitor for integrity
var watchPaths = []string{
	"/etc/passwd", "/etc/shadow", "/etc/sudoers",
	"/etc/crontab", "/etc/hosts",
	"C:\\Windows\\System32\\drivers\\etc\\hosts",
	"C:\\Windows\\System32\\config\\SAM",
}

type FilesystemCollector struct {
	logger    *zap.Logger
	baseline  map[string]int64 // path -> mtime unix
}

func NewFilesystemCollector(logger *zap.Logger) *FilesystemCollector {
	fc := &FilesystemCollector{
		logger:   logger,
		baseline: make(map[string]int64),
	}
	fc.snapshot()
	return fc
}

func (c *FilesystemCollector) Name() string { return "filesystem" }

func (c *FilesystemCollector) snapshot() {
	for _, p := range watchPaths {
		if info, err := os.Stat(p); err == nil {
			c.baseline[p] = info.ModTime().Unix()
		}
	}
}

func (c *FilesystemCollector) Collect(ctx context.Context) ([]Event, error) {
	events := make([]Event, 0)

	for _, p := range watchPaths {
		info, err := os.Stat(p)
		if err != nil {
			continue
		}

		currentMtime := info.ModTime().Unix()
		if prev, exists := c.baseline[p]; exists && currentMtime != prev {
			events = append(events, Event{
				Type:      EventTypeFilesystem,
				Timestamp: time.Now().UTC(),
				Source:    "filesystem-collector",
				Severity:  SeverityHigh,
				Message:   fmt.Sprintf("FILE INTEGRITY VIOLATION: %s modified (mtime changed)", filepath.Base(p)),
				Data: map[string]any{
					"path":          p,
					"prev_mtime":    prev,
					"current_mtime": currentMtime,
				},
				Tags: []string{"filesystem", "integrity", "threat"},
			})
			c.baseline[p] = currentMtime
		}
	}
	return events, nil
}
