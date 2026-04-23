package com.cidefenderx.threat;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ThreatFact {
    private String agentId;
    private String eventType;
    private String severity;
    private String source;
    private String message;
    private Map<String, Object> data;
    private List<String> tags;
    private OffsetDateTime timestamp;
}
