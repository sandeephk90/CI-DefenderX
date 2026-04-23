package com.cidefenderx.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "endpoints")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
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
    @Column(name = "os_type", nullable = false,
            columnDefinition = "os_type")
    private OsType osType = OsType.UNKNOWN;

    @Column(name = "os_version")
    private String osVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "endpoint_status")
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
}
