package beacon

import (
	"context"
	"encoding/json"
	"fmt"
	"os"
	"runtime"
	"time"

	"github.com/cidefenderx/agent/internal/collector"
	"github.com/go-resty/resty/v2"
	"go.uber.org/zap"
)

type Beacon struct {
	client     *resty.Client
	backendURL string
	agentID    string
	logger     *zap.Logger
}

type RegisterRequest struct {
	AgentID   string `json:"agentId"`
	Hostname  string `json:"hostname"`
	IPAddress string `json:"ipAddress"`
	OSType    string `json:"osType"`
	Version   string `json:"version"`
}

type TelemetryPayload struct {
	AgentID   string            `json:"agentId"`
	Timestamp time.Time         `json:"timestamp"`
	Events    []collector.Event `json:"events"`
}

func New(backendURL, agentID, token string, logger *zap.Logger) *Beacon {
	client := resty.New().
		SetBaseURL(backendURL).
		SetHeader("Content-Type", "application/json").
		SetHeader("Authorization", "Bearer "+token).
		SetTimeout(15 * time.Second).
		SetRetryCount(3).
		SetRetryWaitTime(2 * time.Second)

	return &Beacon{
		client:     client,
		backendURL: backendURL,
		agentID:    agentID,
		logger:     logger,
	}
}

func (b *Beacon) Register(ctx context.Context) error {
	hostname, _ := os.Hostname()
	req := RegisterRequest{
		AgentID:  b.agentID,
		Hostname: hostname,
		OSType:   runtime.GOOS,
		Version:  "1.0.0",
	}

	resp, err := b.client.R().
		SetContext(ctx).
		SetBody(req).
		Post("/api/v1/agents/register")

	if err != nil {
		return fmt.Errorf("register request: %w", err)
	}
	if resp.StatusCode() >= 400 {
		return fmt.Errorf("register failed: status=%d body=%s", resp.StatusCode(), resp.String())
	}

	b.logger.Info("agent registered successfully", zap.String("agentID", b.agentID))
	return nil
}

func (b *Beacon) Send(ctx context.Context, events []collector.Event) error {
	if len(events) == 0 {
		return nil
	}

	payload := TelemetryPayload{
		AgentID:   b.agentID,
		Timestamp: time.Now().UTC(),
		Events:    events,
	}

	body, err := json.Marshal(payload)
	if err != nil {
		return fmt.Errorf("marshal payload: %w", err)
	}

	resp, err := b.client.R().
		SetContext(ctx).
		SetBody(body).
		Post("/api/v1/telemetry")

	if err != nil {
		return fmt.Errorf("send telemetry: %w", err)
	}
	if resp.StatusCode() >= 400 {
		return fmt.Errorf("telemetry rejected: status=%d", resp.StatusCode())
	}

	b.logger.Debug("telemetry sent", zap.Int("events", len(events)))
	return nil
}
