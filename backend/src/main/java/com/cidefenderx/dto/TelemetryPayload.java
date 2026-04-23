package com.cidefenderx.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class TelemetryPayload {
    private String agentId;
    private OffsetDateTime timestamp;
    private List<AgentEvent> events;

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }
    public List<AgentEvent> getEvents() { return events; }
    public void setEvents(List<AgentEvent> events) { this.events = events; }

    public static class AgentEvent {
        private String type;
        private OffsetDateTime timestamp;
        private String severity;
        private String source;
        private String message;
        private Map<String, Object> data;
        private List<String> tags;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public OffsetDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
    }
}
