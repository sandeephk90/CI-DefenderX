package com.cidefenderx.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id", nullable = false)
    private Endpoint endpoint;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "threat_severity")
    private Severity severity = Severity.LOW;

    private String source;

    @Column(columnDefinition = "text")
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> tags;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt = OffsetDateTime.now();

    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }

    public Event() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Endpoint getEndpoint() { return endpoint; }
    public void setEndpoint(Endpoint endpoint) { this.endpoint = endpoint; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public OffsetDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(OffsetDateTime occurredAt) { this.occurredAt = occurredAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Event e = new Event();
        public Builder endpoint(Endpoint v) { e.endpoint = v; return this; }
        public Builder eventType(String v) { e.eventType = v; return this; }
        public Builder severity(Severity v) { e.severity = v; return this; }
        public Builder source(String v) { e.source = v; return this; }
        public Builder message(String v) { e.message = v; return this; }
        public Builder payload(Map<String, Object> v) { e.payload = v; return this; }
        public Builder tags(List<String> v) { e.tags = v; return this; }
        public Builder occurredAt(OffsetDateTime v) { e.occurredAt = v; return this; }
        public Event build() { return e; }
    }
}
