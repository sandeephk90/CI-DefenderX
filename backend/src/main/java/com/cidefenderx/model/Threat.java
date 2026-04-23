package com.cidefenderx.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "threats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Threat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id", nullable = false)
    private Endpoint endpoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "threat_type", nullable = false)
    private String threatType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "threat_severity")
    private Severity severity;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "threat_status")
    private ThreatStatus status = ThreatStatus.OPEN;

    @Column(name = "rule_name")
    private String ruleName;

    @Column(name = "attack_vector")
    private String attackVector;

    @Column(name = "mitre_technique")
    private String mitreTechnique;

    @Column(name = "risk_score")
    private int riskScore;

    @Column(name = "detected_at", nullable = false)
    private OffsetDateTime detectedAt = OffsetDateTime.now();

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "analyst_notes", columnDefinition = "text")
    private String analystNotes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }
    public enum ThreatStatus { OPEN, INVESTIGATING, RESOLVED, FALSE_POSITIVE }
}
