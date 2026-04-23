-- CI-DefenderX Initial Schema

CREATE TYPE os_type AS ENUM ('WINDOWS', 'LINUX', 'MACOS', 'UNKNOWN');
CREATE TYPE endpoint_status AS ENUM ('ONLINE', 'OFFLINE', 'ISOLATED', 'DECOMMISSIONED');
CREATE TYPE threat_severity AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');
CREATE TYPE threat_status AS ENUM ('OPEN', 'INVESTIGATING', 'RESOLVED', 'FALSE_POSITIVE');
CREATE TYPE alert_channel AS ENUM ('EMAIL', 'SIEM', 'WEBHOOK', 'SMS', 'JIRA', 'SERVICENOW');
CREATE TYPE user_role AS ENUM ('SECURITY_ADMIN', 'SOC_ANALYST', 'AUDITOR', 'COMPLIANCE_OFFICER');

-- Endpoints (servers, DCs, workstations monitored by the agent)
CREATE TABLE endpoints (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id        VARCHAR(128) NOT NULL UNIQUE,
    hostname        VARCHAR(255) NOT NULL,
    ip_address      VARCHAR(64),
    os_type         os_type NOT NULL DEFAULT 'UNKNOWN',
    os_version      VARCHAR(128),
    status          endpoint_status NOT NULL DEFAULT 'OFFLINE',
    risk_score      SMALLINT NOT NULL DEFAULT 0 CHECK (risk_score BETWEEN 0 AND 100),
    agent_version   VARCHAR(32),
    last_seen       TIMESTAMPTZ,
    registered_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    tags            TEXT[] DEFAULT '{}',
    metadata        JSONB DEFAULT '{}'
);

CREATE INDEX idx_endpoints_status      ON endpoints(status);
CREATE INDEX idx_endpoints_risk_score  ON endpoints(risk_score DESC);
CREATE INDEX idx_endpoints_agent_id    ON endpoints(agent_id);

-- Raw telemetry events from agents
CREATE TABLE events (
    id          BIGSERIAL PRIMARY KEY,
    endpoint_id UUID NOT NULL REFERENCES endpoints(id) ON DELETE CASCADE,
    event_type  VARCHAR(64) NOT NULL,
    severity    threat_severity NOT NULL DEFAULT 'LOW',
    source      VARCHAR(128),
    message     TEXT,
    payload     JSONB NOT NULL DEFAULT '{}',
    tags        TEXT[] DEFAULT '{}',
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_events_endpoint_id  ON events(endpoint_id);
CREATE INDEX idx_events_event_type   ON events(event_type);
CREATE INDEX idx_events_severity     ON events(severity);
CREATE INDEX idx_events_occurred_at  ON events(occurred_at DESC);

-- Threats detected by the Drools engine
CREATE TABLE threats (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    endpoint_id     UUID NOT NULL REFERENCES endpoints(id) ON DELETE CASCADE,
    event_id        BIGINT REFERENCES events(id),
    threat_type     VARCHAR(128) NOT NULL,
    severity        threat_severity NOT NULL,
    title           VARCHAR(512) NOT NULL,
    description     TEXT,
    status          threat_status NOT NULL DEFAULT 'OPEN',
    rule_name       VARCHAR(256),
    attack_vector   VARCHAR(256),
    mitre_technique VARCHAR(64),
    risk_score      SMALLINT DEFAULT 0,
    detected_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at     TIMESTAMPTZ,
    analyst_notes   TEXT,
    metadata        JSONB DEFAULT '{}'
);

CREATE INDEX idx_threats_endpoint_id ON threats(endpoint_id);
CREATE INDEX idx_threats_severity    ON threats(severity);
CREATE INDEX idx_threats_status      ON threats(status);
CREATE INDEX idx_threats_detected_at ON threats(detected_at DESC);
CREATE INDEX idx_threats_type        ON threats(threat_type);

-- Alerts sent via various channels
CREATE TABLE alerts (
    id          BIGSERIAL PRIMARY KEY,
    threat_id   UUID NOT NULL REFERENCES threats(id) ON DELETE CASCADE,
    channel     alert_channel NOT NULL,
    recipient   VARCHAR(512),
    payload     JSONB,
    sent_at     TIMESTAMPTZ,
    status      VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    error_msg   TEXT
);

CREATE INDEX idx_alerts_threat_id ON alerts(threat_id);
CREATE INDEX idx_alerts_channel   ON alerts(channel);

-- Admin console users
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username        VARCHAR(128) NOT NULL UNIQUE,
    email           VARCHAR(256) NOT NULL UNIQUE,
    password_hash   VARCHAR(256) NOT NULL,
    full_name       VARCHAR(256),
    role            user_role NOT NULL DEFAULT 'SOC_ANALYST',
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    last_login      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Policies (maps to Drools rule sets)
CREATE TABLE policies (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(256) NOT NULL UNIQUE,
    description TEXT,
    rule_file   VARCHAR(256),
    enabled     BOOLEAN NOT NULL DEFAULT TRUE,
    priority    SMALLINT DEFAULT 100,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Audit log for all admin actions
CREATE TABLE audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    user_id     UUID REFERENCES users(id),
    username    VARCHAR(128),
    action      VARCHAR(128) NOT NULL,
    resource    VARCHAR(256),
    resource_id VARCHAR(128),
    ip_address  VARCHAR(64),
    details     JSONB DEFAULT '{}',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_user_id    ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);

-- Insert default admin user (password: Admin@123 – bcrypt)
INSERT INTO users (username, email, password_hash, full_name, role)
VALUES (
    'admin',
    'admin@cidefenderx.local',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'System Administrator',
    'SECURITY_ADMIN'
);

-- Default policies
INSERT INTO policies (name, description, rule_file, enabled, priority) VALUES
    ('Privilege Escalation Detection', 'Detects privilege escalation attempts on endpoints', 'privilege_escalation.drl', true, 10),
    ('Ransomware Behavior Detection', 'Detects ransomware-like file encryption patterns', 'ransomware_detection.drl', true, 10),
    ('Lateral Movement Detection', 'Detects lateral movement via suspicious network connections', 'lateral_movement.drl', true, 20),
    ('Brute Force Detection', 'Detects SSH and Windows login brute force attempts', 'brute_force.drl', true, 20),
    ('Suspicious Process Detection', 'Detects known malicious process names and behaviors', 'suspicious_process.drl', true, 10),
    ('AD Threat Detection', 'Detects Active Directory attacks including Kerberoasting', 'ad_threats.drl', true, 10);
