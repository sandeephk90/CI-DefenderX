package monitor

import "context"

type Monitor interface {
	Name() string
	Start(ctx context.Context)
}
