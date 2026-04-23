# CI-DefenderX — Architecture

## Technology Stack

| Layer | Technology | Purpose |
|---|---|---|
| Agent | **Go** | Lightweight endpoint agent — process, network, filesystem, LDAP monitors |
| Backend | **Spring Boot 3** | REST API, threat ingestion, business logic |
| Frontend | **React 18 + TypeScript** | Admin console SPA |
| Database | **PostgreSQL 16** | Persistent storage for endpoints, events, threats |
| Threat Engine | **Drools 8** | Rule-based threat detection (`.drl` rule files) |
| Directory Monitoring | **LDAP + Kerberos** | Active Directory threat detection |
| Visualization | **Apache ECharts** | Interactive security dashboards |
| Streaming | **Apache Kafka** | Event streaming for alerts, SIEM integration |
| Search | **Elasticsearch** | Full-text search across security events |

## Component Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        Admin Console                            │
│              React + TypeScript + Apache ECharts                │
│  Dashboard │ Endpoints │ Threats │ Reports │ Settings           │
└─────────────────────┬───────────────────────────────────────────┘
                      │ REST (JWT Auth)
┌─────────────────────▼───────────────────────────────────────────┐
│                    Spring Boot Backend                          │
│                                                                 │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────────────┐   │
│  │  REST APIs  │  │ Drools Engine│  │  LDAP/AD Monitor    │   │
│  │  /agents    │  │  Threat Rules│  │  Scheduled Polling  │   │
│  │  /telemetry │  │  .drl files  │  │  Kerberos Events    │   │
│  │  /threats   │  └──────┬───────┘  └─────────────────────┘   │
│  │  /endpoints │         │                                      │
│  │  /dashboard │         ▼ Threat detected                      │
│  └─────────────┘  ┌──────────────┐  ┌─────────────────────┐   │
│                   │ Alert Service│  │  Kafka Producer     │   │
│                   └──────────────┘  └─────────────────────┘   │
└──────────┬──────────────────────────────────────┬──────────────┘
           │ JPA                                  │ Kafka
┌──────────▼──────────┐                  ┌────────▼──────────────┐
│   PostgreSQL 16     │                  │    Apache Kafka        │
│                     │                  │  cidefenderx.threats   │
│  endpoints          │                  │  (SIEM / webhooks)     │
│  events             │                  └───────────────────────┘
│  threats            │
│  alerts             │
│  users              │
│  audit_logs         │
└─────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                  Go Security Agent                               │
│  (deployed on each monitored endpoint)                          │
│                                                                 │
│  Collectors:          Monitors:                                 │
│  • ProcessCollector   • LDAPMonitor (AD threats)               │
│  • NetworkCollector   • SyslogMonitor (SSH brute force)        │
│  • FilesystemCollector                                          │
│  • SystemCollector                                              │
│                                                                 │
│  Beacon → POST /api/v1/telemetry → Backend                     │
└──────────────────────────────────────────────────────────────────┘
```

## Drools Threat Rules

| Rule File | Detects |
|---|---|
| `privilege_escalation.drl` | Suspicious privileged processes, sudo abuse, root shells |
| `ransomware_detection.drl` | File integrity violations, ransomware extensions |
| `lateral_movement.drl` | C2 connections, Pass-the-Hash |
| `brute_force.drl` | SSH brute force, Kerberoasting |
| `suspicious_process.drl` | Mimikatz, Meterpreter, network scanners |
| `ad_threats.drl` | DCSync, Domain Admin changes, account lockouts |

## MITRE ATT&CK Coverage

- T1003 — OS Credential Dumping
- T1021 — Remote Services (Lateral Movement)
- T1046 — Network Service Discovery
- T1055 — Process Injection
- T1098 — Account Manipulation
- T1110 — Brute Force
- T1486 — Data Encrypted for Impact (Ransomware)
- T1548 — Abuse Elevation Control Mechanism
- T1550.002 — Pass the Hash
- T1558.003 — Kerberoasting

## Quick Start

```bash
# Start all services
docker compose up -d

# Access
#   Admin Console:  http://localhost:3000
#   Backend API:    http://localhost:8080
#   Default login:  admin / Admin@123

# Build agent for Linux
cd agent && GOOS=linux go build -o ci-defenderx-agent ./cmd/agent

# Deploy agent on a server
./ci-defenderx-agent   # reads agent.yaml
```
