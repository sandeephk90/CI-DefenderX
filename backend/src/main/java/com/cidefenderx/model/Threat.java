package com.cidefenderx.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "threats")
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
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
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

    public Threat() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Endpoint getEndpoint() { return endpoint; }
    public void setEndpoint(Endpoint endpoint) { this.endpoint = endpoint; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public String getThreatType() { return threatType; }
    public void setThreatType(String threatType) { this.threatType = threatType; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ThreatStatus getStatus() { return status; }
    public void setStatus(ThreatStatus status) { this.status = status; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getAttackVector() { return attackVector; }
    public void setAttackVector(String attackVector) { this.attackVector = attackVector; }
    public String getMitreTechnique() { return mitreTechnique; }
    public void setMitreTechnique(String mitreTechnique) { this.mitreTechnique = mitreTechnique; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public OffsetDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(OffsetDateTime detectedAt) { this.detectedAt = detectedAt; }
    public OffsetDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(OffsetDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public String getAnalystNotes() { return analystNotes; }
    public void setAnalystNotes(String analystNotes) { this.analystNotes = analystNotes; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Threat t = new Threat();
        public Builder endpoint(Endpoint v) { t.endpoint = v; return this; }
        public Builder event(Event v) { t.event = v; return this; }
        public Builder threatType(String v) { t.threatType = v; return this; }
        public Builder severity(Severity v) { t.severity = v; return this; }
        public Builder title(String v) { t.title = v; return this; }
        public Builder description(String v) { t.description = v; return this; }
        public Builder status(ThreatStatus v) { t.status = v; return this; }
        public Builder ruleName(String v) { t.ruleName = v; return this; }
        public Builder attackVector(String v) { t.attackVector = v; return this; }
        public Builder mitreTechnique(String v) { t.mitreTechnique = v; return this; }
        public Builder riskScore(int v) { t.riskScore = v; return this; }
        public Builder detectedAt(OffsetDateTime v) { t.detectedAt = v; return this; }
        public Threat build() { return t; }
    }
}
