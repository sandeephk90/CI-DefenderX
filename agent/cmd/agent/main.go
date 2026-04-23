package main

import (
	"context"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/cidefenderx/agent/internal/beacon"
	"github.com/cidefenderx/agent/internal/collector"
	"github.com/cidefenderx/agent/internal/config"
	"github.com/cidefenderx/agent/internal/monitor"
	"go.uber.org/zap"
)

func main() {
	logger, _ := zap.NewProduction()
	defer logger.Sync()

	cfg, err := config.Load()
	if err != nil {
		logger.Fatal("failed to load config", zap.Error(err))
	}

	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	beaconClient := beacon.New(cfg.BackendURL, cfg.AgentID, cfg.AgentToken, logger)

	// Register agent on startup
	if err := beaconClient.Register(ctx); err != nil {
		logger.Fatal("agent registration failed", zap.Error(err))
	}

	collectors := []collector.Collector{
		collector.NewProcessCollector(logger),
		collector.NewNetworkCollector(logger),
		collector.NewFilesystemCollector(logger),
		collector.NewSystemCollector(logger),
	}

	monitors := []monitor.Monitor{
		monitor.NewLDAPMonitor(cfg.LDAP, logger),
		monitor.NewSyslogMonitor(logger),
	}

	ticker := time.NewTicker(time.Duration(cfg.CollectIntervalSec) * time.Second)
	defer ticker.Stop()

	logger.Info("CI-DefenderX agent started",
		zap.String("agentID", cfg.AgentID),
		zap.String("backend", cfg.BackendURL),
	)

	go func() {
		for _, m := range monitors {
			go m.Start(ctx)
		}
	}()

	sigCh := make(chan os.Signal, 1)
	signal.Notify(sigCh, syscall.SIGINT, syscall.SIGTERM)

	for {
		select {
		case <-ticker.C:
			events := make([]collector.Event, 0)
			for _, c := range collectors {
				evts, err := c.Collect(ctx)
				if err != nil {
					logger.Warn("collector error", zap.String("collector", c.Name()), zap.Error(err))
					continue
				}
				events = append(events, evts...)
			}
			if err := beaconClient.Send(ctx, events); err != nil {
				logger.Warn("beacon send failed", zap.Error(err))
			}

		case <-sigCh:
			logger.Info("shutting down agent")
			return

		case <-ctx.Done():
			return
		}
	}
}
