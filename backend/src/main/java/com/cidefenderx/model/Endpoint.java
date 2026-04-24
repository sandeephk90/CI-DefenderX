package com.cidefenderx.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "endpoints")
public class Endpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "agent_id", nullable = false, unique = true)
    private String agentId;

    @Column(nullable = false)
    private String hostname;

    @Column(name = "ip_address")
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "os_type", nullable = false)
    private OsType osType = OsType.UNKNOWN;

    @Column(name = "os_version")
    private String osVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EndpointStatus status = EndpointStatus.OFFLINE;

    @Column(name = "risk_score", nullable = false)
    private int riskScore = 0;

    @Column(name = "agent_version")
    private String agentVersion;

    @Column(name = "last_seen")
    private OffsetDateTime lastSeen;

    @Column(name = "registered_at", nullable = false)
    private OffsetDateTime registeredAt = OffsetDateTime.now();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> tags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    public enum OsType { WINDOWS, LINUX, MACOS, UNKNOWN }
    public enum EndpointStatus { ONLINE, OFFLINE, ISOLATED, DECOMMISSIONED }

    public Endpoint() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public OsType getOsType() { return osType; }
    public void setOsType(OsType osType) { this.osType = osType; }
    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
    public EndpointStatus getStatus() { return status; }
    public void setStatus(EndpointStatus status) { this.status = status; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public String getAgentVersion() { return agentVersion; }
    public void setAgentVersion(String agentVersion) { this.agentVersion = agentVersion; }
    public OffsetDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(OffsetDateTime lastSeen) { this.lastSeen = lastSeen; }
    public OffsetDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(OffsetDateTime registeredAt) { this.registeredAt = registeredAt; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Endpoint e = new Endpoint();
        public Builder agentId(String v) { e.agentId = v; return this; }
        public Builder hostname(String v) { e.hostname = v; return this; }
        public Builder ipAddress(String v) { e.ipAddress = v; return this; }
        public Builder osType(OsType v) { e.osType = v; return this; }
        public Builder status(EndpointStatus v) { e.status = v; return this; }
        public Builder riskScore(int v) { e.riskScore = v; return this; }
        public Builder agentVersion(String v) { e.agentVersion = v; return this; }
        public Builder lastSeen(OffsetDateTime v) { e.lastSeen = v; return this; }
        public Builder registeredAt(OffsetDateTime v) { e.registeredAt = v; return this; }
        public Endpoint build() { return e; }
    }
}
