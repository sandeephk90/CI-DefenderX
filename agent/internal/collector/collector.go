package collector

import (
	"context"
	"time"
)

type Severity string

const (
	SeverityLow      Severity = "LOW"
	SeverityMedium   Severity = "MEDIUM"
	SeverityHigh     Severity = "HIGH"
	SeverityCritical Severity = "CRITICAL"
)

type EventType string

const (
	EventTypeProcess    EventType = "PROCESS"
	EventTypeNetwork    EventType = "NETWORK"
	EventTypeFilesystem EventType = "FILESYSTEM"
	EventTypeSystem     EventType = "SYSTEM"
	EventTypeLDAP       EventType = "LDAP"
	EventTypeSyslog     EventType = "SYSLOG"
)

type Event struct {
	Type      EventType         `json:"type"`
	Timestamp time.Time         `json:"timestamp"`
	Severity  Severity          `json:"severity"`
	Source    string            `json:"source"`
	Message   string            `json:"message"`
	Data      map[string]any    `json:"data"`
	Tags      []string          `json:"tags"`
}

type Collector interface {
	Name() string
	Collect(ctx context.Context) ([]Event, error)
}
