package com.cidefenderx.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
public class TelemetryPayload {
    private String agentId;
    private OffsetDateTime timestamp;
    private List<AgentEvent> events;

    @Data
    public static class AgentEvent {
        private String type;
        private OffsetDateTime timestamp;
        private String severity;
        private String source;
        private String message;
        private Map<String, Object> data;
        private List<String> tags;
    }
}
