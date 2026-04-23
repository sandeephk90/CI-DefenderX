package collector

import (
	"context"
	"fmt"
	"time"

	"github.com/shirou/gopsutil/v3/net"
	"go.uber.org/zap"
)

// ports commonly targeted by attackers
var suspiciousPorts = map[uint32]string{
	4444: "metasploit-default", 1337: "leet-shell", 6666: "irc-botnet",
	9001: "tor-orport", 9050: "tor-socks", 31337: "elite-backdoor",
}

type NetworkCollector struct {
	logger *zap.Logger
}

func NewNetworkCollector(logger *zap.Logger) *NetworkCollector {
	return &NetworkCollector{logger: logger}
}

func (c *NetworkCollector) Name() string { return "network" }

func (c *NetworkCollector) Collect(ctx context.Context) ([]Event, error) {
	conns, err := net.ConnectionsWithContext(ctx, "all")
	if err != nil {
		return nil, fmt.Errorf("list connections: %w", err)
	}

	events := make([]Event, 0)
	for _, conn := range conns {
		if conn.Status != "ESTABLISHED" {
			continue
		}

		sev := SeverityLow
		tags := []string{"network", "connection"}
		msg := fmt.Sprintf("connection %s:%d -> %s:%d [%s]",
			conn.Laddr.IP, conn.Laddr.Port,
			conn.Raddr.IP, conn.Raddr.Port,
			conn.Status)

		if label, bad := suspiciousPorts[conn.Raddr.Port]; bad {
			sev = SeverityHigh
			tags = append(tags, "suspicious", "threat", label)
			msg = fmt.Sprintf("SUSPICIOUS CONNECTION to port %d (%s): %s", conn.Raddr.Port, label, msg)
		}

		events = append(events, Event{
			Type:      EventTypeNetwork,
			Timestamp: time.Now().UTC(),
			Source:    "network-collector",
			Severity:  sev,
			Message:   msg,
			Data: map[string]any{
				"local_ip":   conn.Laddr.IP,
				"local_port": conn.Laddr.Port,
				"remote_ip":  conn.Raddr.IP,
				"remote_port": conn.Raddr.Port,
				"status":     conn.Status,
				"pid":        conn.Pid,
			},
			Tags: tags,
		})
	}
	return events, nil
}
