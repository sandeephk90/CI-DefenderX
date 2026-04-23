package monitor

import (
	"context"
	"fmt"
	"time"

	"github.com/cidefenderx/agent/internal/config"
	"github.com/go-ldap/ldap/v3"
	"go.uber.org/zap"
)

type LDAPMonitor struct {
	cfg    config.LDAPConfig
	logger *zap.Logger
	conn   *ldap.Conn
}

func NewLDAPMonitor(cfg config.LDAPConfig, logger *zap.Logger) *LDAPMonitor {
	return &LDAPMonitor{cfg: cfg, logger: logger}
}

func (m *LDAPMonitor) Name() string { return "ldap" }

func (m *LDAPMonitor) Start(ctx context.Context) {
	if m.cfg.Host == "" {
		m.logger.Info("LDAP monitor disabled: no host configured")
		return
	}

	ticker := time.NewTicker(60 * time.Second)
	defer ticker.Stop()

	m.logger.Info("LDAP monitor started", zap.String("host", m.cfg.Host))

	for {
		select {
		case <-ctx.Done():
			return
		case <-ticker.C:
			if err := m.poll(ctx); err != nil {
				m.logger.Warn("LDAP poll error", zap.Error(err))
			}
		}
	}
}

func (m *LDAPMonitor) connect() error {
	addr := fmt.Sprintf("%s:%d", m.cfg.Host, m.cfg.Port)
	conn, err := ldap.DialURL("ldap://" + addr)
	if err != nil {
		return fmt.Errorf("LDAP dial: %w", err)
	}
	if err := conn.Bind(m.cfg.BindDN, m.cfg.Password); err != nil {
		conn.Close()
		return fmt.Errorf("LDAP bind: %w", err)
	}
	m.conn = conn
	return nil
}

func (m *LDAPMonitor) poll(ctx context.Context) error {
	if m.conn == nil {
		if err := m.connect(); err != nil {
			return err
		}
	}

	// Search for privileged group members (Domain Admins)
	searchReq := ldap.NewSearchRequest(
		m.cfg.BaseDN,
		ldap.ScopeWholeSubtree,
		ldap.NeverDerefAliases,
		0, 30, false,
		"(&(objectClass=user)(memberOf=CN=Domain Admins,CN=Users,"+m.cfg.BaseDN+"))",
		[]string{"sAMAccountName", "distinguishedName", "whenChanged"},
		nil,
	)

	result, err := m.conn.SearchWithPaging(searchReq, 200)
	if err != nil {
		m.conn = nil
		return fmt.Errorf("LDAP search: %w", err)
	}

	m.logger.Debug("LDAP domain admin members polled",
		zap.Int("count", len(result.Entries)),
	)

	// Detect recently changed privileged accounts (within last 5 minutes)
	threshold := time.Now().UTC().Add(-5 * time.Minute)
	for _, entry := range result.Entries {
		whenChanged := entry.GetAttributeValue("whenChanged")
		if whenChanged != "" {
			t, err := time.Parse("20060102150405.0Z", whenChanged)
			if err == nil && t.After(threshold) {
				m.logger.Warn("PRIVILEGED ACCOUNT CHANGE DETECTED",
					zap.String("account", entry.GetAttributeValue("sAMAccountName")),
					zap.String("dn", entry.GetAttributeValue("distinguishedName")),
					zap.String("whenChanged", whenChanged),
				)
			}
		}
	}

	return nil
}
