package com.cidefenderx.service;

import com.cidefenderx.dto.TelemetryPayload;
import com.cidefenderx.model.Endpoint;
import com.cidefenderx.model.Event;
import com.cidefenderx.model.Threat;
import com.cidefenderx.repository.EndpointRepository;
import com.cidefenderx.repository.EventRepository;
import com.cidefenderx.repository.ThreatRepository;
import com.cidefenderx.threat.DroolsThreatEngine;
import com.cidefenderx.threat.ThreatFact;
import com.cidefenderx.threat.ThreatResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryService {

    private final EndpointRepository endpointRepository;
    private final EventRepository eventRepository;
    private final ThreatRepository threatRepository;
    private final DroolsThreatEngine threatEngine;
    private final AlertService alertService;

    @Transactional
    public void process(TelemetryPayload payload) {
        Endpoint endpoint = endpointRepository.findByAgentId(payload.getAgentId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown agent: " + payload.getAgentId()));

        endpoint.setLastSeen(OffsetDateTime.now());
        endpoint.setStatus(Endpoint.EndpointStatus.ONLINE);
        endpointRepository.save(endpoint);

        if (payload.getEvents() == null || payload.getEvents().isEmpty()) {
            return;
        }

        for (TelemetryPayload.AgentEvent agentEvent : payload.getEvents()) {
            Event event = Event.builder()
                    .endpoint(endpoint)
                    .eventType(agentEvent.getType())
                    .severity(mapSeverity(agentEvent.getSeverity()))
                    .source(agentEvent.getSource())
                    .message(agentEvent.getMessage())
                    .payload(agentEvent.getData())
                    .tags(agentEvent.getTags())
                    .occurredAt(agentEvent.getTimestamp() != null
                            ? agentEvent.getTimestamp()
                            : OffsetDateTime.now())
                    .build();

            event = eventRepository.save(event);

            // Run through Drools
            ThreatFact fact = toFact(agentEvent, endpoint.getAgentId());
            List<ThreatResult> detections = threatEngine.evaluate(fact);

            for (ThreatResult detection : detections) {
                Threat threat = Threat.builder()
                        .endpoint(endpoint)
                        .event(event)
                        .threatType(detection.getThreatType())
                        .severity(Threat.Severity.valueOf(detection.getSeverity()))
                        .title(detection.getTitle())
                        .description(detection.getDescription())
                        .ruleName(detection.getRuleName())
                        .attackVector(detection.getAttackVector())
                        .mitreTechnique(detection.getMitreTechnique())
                        .riskScore(detection.getRiskScore())
                        .detectedAt(OffsetDateTime.now())
                        .build();

                threat = threatRepository.save(threat);
                log.warn("THREAT DETECTED: [{}] {} on endpoint {} (score={})",
                        threat.getSeverity(), threat.getTitle(),
                        endpoint.getHostname(), threat.getRiskScore());

                alertService.sendAlerts(threat);
                updateEndpointRiskScore(endpoint, detection.getRiskScore());
            }
        }
    }

    private void updateEndpointRiskScore(Endpoint endpoint, int newScore) {
        int current = endpoint.getRiskScore();
        int updated = Math.min(100, Math.max(current, newScore));
        if (updated != current) {
            endpoint.setRiskScore(updated);
            endpointRepository.save(endpoint);
        }
    }

    private ThreatFact toFact(TelemetryPayload.AgentEvent e, String agentId) {
        ThreatFact fact = new ThreatFact();
        fact.setAgentId(agentId);
        fact.setEventType(e.getType());
        fact.setSeverity(e.getSeverity());
        fact.setSource(e.getSource());
        fact.setMessage(e.getMessage() != null ? e.getMessage().toLowerCase() : "");
        fact.setData(e.getData());
        fact.setTags(e.getTags());
        fact.setTimestamp(e.getTimestamp());
        return fact;
    }

    private Event.Severity mapSeverity(String s) {
        if (s == null) return Event.Severity.LOW;
        try { return Event.Severity.valueOf(s.toUpperCase()); }
        catch (IllegalArgumentException e) { return Event.Severity.LOW; }
    }
}
