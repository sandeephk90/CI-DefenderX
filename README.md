# CI-DefenderX — Endpoint Threat Protection Platform

A full-stack, enterprise-grade Endpoint Threat Protection (ETP) solution that monitors endpoints in real time, detects threats using a Drools rule engine, and presents everything through a React dashboard.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      React Frontend                         │
│   Dashboard · Threats · Endpoints · Reports · Settings      │
│            Apache ECharts visualizations                    │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTPS / REST
┌────────────────────▼────────────────────────────────────────┐
│              Spring Boot Backend  (port 8080)               │
│  JWT Auth · Drools Engine · LDAP Monitor · REST APIs        │
│  Flyway migrations · PostgreSQL JPA · Kafka publisher       │
└──────┬──────────────┬──────────────────┬────────────────────┘
       │              │                  │
  ┌────▼────┐   ┌─────▼─────┐   ┌───────▼──────┐
  │Postgres │   │   Kafka   │   │  LDAP / AD   │
  │  :5432  │   │   :9092   │   │   (optional) │
  └─────────┘   └───────────┘   └──────────────┘
       ▲
┌──────┴──────────────────────────────────────────────────────┐
│              Go Security Agent  (per endpoint)              │
│  Process · Network · Filesystem · Syslog collectors         │
│  Sends JWT-authenticated telemetry every 30 seconds         │
└─────────────────────────────────────────────────────────────┘
```

---

## Technology Stack

| Layer | Technology |
|---|---|
| Agent | Go 1.21 |
| Backend | Spring Boot 3.2 (Java 21) |
| Frontend | React 18 + TypeScript |
| Database | PostgreSQL 16 |
| Threat Engine | Drools 8.44 |
| Directory Monitoring | LDAP + Kerberos (Spring LDAP) |
| Visualization | Apache ECharts |
| Streaming | Apache Kafka (Confluent 7.6) |
| Auth | JWT (jjwt 0.12) + BCrypt |
| Migrations | Flyway |
| Container | Docker + Docker Compose |

---

## Quick Start

### Prerequisites
- Docker Desktop (Hyper-V mode on Windows domain machines)
- Git

### Run the Platform

```bash
git clone https://github.com/sandeephk90/CI-DefenderX.git
cd CI-DefenderX
docker compose up -d
```

Services that start:
- **Frontend** → http://localhost:3000
- **Backend API** → http://localhost:8080
- **PostgreSQL** → localhost:5432
- **Kafka** → localhost:9092

### Default Admin Login

| Field | Value |
|---|---|
| URL | http://localhost:3000 |
| Username | `admin` |
| Password | `Admin@123` |

---

## Project Structure

```
CI-DefenderX/
├── agent/                          # Go security agent
│   ├── cmd/agent/main.go           # Entry point
│   ├── internal/
│   │   ├── beacon/beacon.go        # Telemetry sender (JWT auth)
│   │   ├── collector/
│   │   │   ├── process.go          # Suspicious process detection
│   │   │   ├── network.go          # C2 connection detection
│   │   │   ├── filesystem.go       # File integrity monitoring
│   │   │   └── system.go           # CPU/memory/disk health
│   │   └── monitor/
│   │       ├── ldap.go             # Active Directory monitor
│   │       └── syslog.go           # Auth log monitor
│   └── Dockerfile
│
├── backend/                        # Spring Boot backend
│   ├── src/main/java/com/cidefenderx/
│   │   ├── controller/             # REST controllers
│   │   ├── service/                # Business logic
│   │   ├── model/                  # JPA entities
│   │   ├── repository/             # Spring Data repositories
│   │   ├── security/               # JWT filter, Spring Security config
│   │   ├── threat/                 # Drools engine integration
│   │   ├── kafka/                  # Kafka event publisher
│   │   └── dto/                    # Data transfer objects
│   ├── src/main/resources/
│   │   ├── rules/                  # Drools .drl threat rules
│   │   ├── db/migration/           # Flyway SQL migrations
│   │   └── application.yml
│   └── Dockerfile
│
├── frontend/                       # React frontend
│   ├── src/
│   │   ├── pages/
│   │   │   ├── dashboard/          # ECharts dashboard
│   │   │   ├── threats/            # Threat management table
│   │   │   ├── endpoints/          # Endpoint inventory
│   │   │   ├── reports/            # Reports page
│   │   │   └── settings/           # Settings page
│   │   └── charts/                 # ECharts components
│   └── Dockerfile
│
└── docker-compose.yml
```

---

## Threat Detection Rules (Drools)

| Rule File | Detects |
|---|---|
| `privilege_escalation.drl` | sudo abuse, UAC bypass, token impersonation |
| `ransomware_detection.drl` | Mass file encryption, shadow copy deletion |
| `lateral_movement.drl` | SMB/RDP/WMI lateral movement patterns |
| `brute_force.drl` | SSH and Windows login brute force |
| `suspicious_process.drl` | mimikatz, meterpreter, nmap, PowerSploit |
| `ad_threats.drl` | Kerberoasting, DCSync, Golden Ticket attacks |

MITRE ATT&CK techniques covered: T1055, T1059, T1078, T1110, T1486, T1548, T1558, T1569, T1570, T1574

---

## Go Agent Deployment

The agent runs on each endpoint you want to monitor. Build and configure it:

```bash
cd agent
go build -o ci-defenderx-agent ./cmd/agent

# Set environment variables
export AGENT_BACKEND_URL=http://<backend-host>:8080
export AGENT_JWT_SECRET=<your-jwt-secret>
./ci-defenderx-agent
```

The agent sends telemetry every 30 seconds covering:
- Running processes (flags known malware names)
- Network connections (flags known C2 ports: 4444, 1337, 31337)
- File integrity (monitors `/etc/passwd`, SAM hive, sudoers)
- System health (CPU, memory, disk)
- Active Directory changes (privileged group membership)
- SSH/sudo auth events from syslog

---

## API Endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/auth/login` | Authenticate, get JWT |
| GET | `/api/v1/dashboard/summary` | Dashboard KPIs |
| GET | `/api/v1/threats` | List threats (paginated, filterable) |
| PATCH | `/api/v1/threats/{id}/status` | Update threat status |
| GET | `/api/v1/endpoints` | List endpoints |
| POST | `/api/v1/endpoints/{id}/isolate` | Isolate an endpoint |
| POST | `/api/v1/endpoints/{id}/restore` | Restore an endpoint |
| POST | `/api/v1/agents/register` | Agent self-registration |
| POST | `/api/v1/agents/telemetry` | Agent telemetry ingest |

All endpoints except `/api/v1/auth/**` and `/actuator/health` require a Bearer JWT token.

---

## Configuration

### Backend (`application.yml`)

```yaml
cidefenderx:
  jwt:
    secret: your-secret-key        # Change in production
    expiration-ms: 86400000        # 24 hours
  ldap:
    enabled: false                 # Set true to enable AD monitoring
  kafka:
    enabled: false                 # Set true to enable Kafka streaming
```

### Kafka (optional)

When `spring.kafka.enabled=true`, every detected threat is published to the `cidefenderx.threats` topic in JSON format for SIEM integration.

### LDAP / Active Directory (optional)

Set `cidefenderx.ldap.enabled=true` and configure:
```yaml
spring:
  ldap:
    urls: ldap://your-dc:389
    base: dc=yourdomain,dc=com
    username: cn=svc-account,dc=yourdomain,dc=com
    password: your-password
```

---

## Database Migrations

| Version | Description |
|---|---|
| V1 | Initial schema (endpoints, events, threats, alerts, users, policies, audit_logs) |
| V2 | Fix `risk_score` column type SMALLINT → INTEGER |
| V3 | Fix admin password via pgcrypto |
| V4 | Pre-computed BCrypt hash for `Admin@123` |

---

## Development

### Backend only
```bash
cd backend
mvn spring-boot:run
```

### Frontend only
```bash
cd frontend
npm install
npm start
```

### Rebuild a single service
```bash
docker compose build backend --no-cache
docker compose up -d backend
```

---

## Default Policies

Six detection policies are seeded on first run, each mapped to a Drools rule file. Manage them via the Settings → Policies section in the UI.

---

## Security Notes

- JWT tokens expire after 24 hours
- Passwords are hashed with BCrypt (strength 12)
- All API endpoints are protected by Spring Security filter chain
- CSRF is disabled (stateless JWT architecture)
- CORS is configured to allow all origins — restrict in production

---

## License

Proprietary — CI-DefenderX © 2024 CrossIdentity
