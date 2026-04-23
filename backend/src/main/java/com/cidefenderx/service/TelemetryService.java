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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class TelemetryService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryService.class);

    private final EndpointRepository endpointRepository;
    private final EventRepository eventRepository;
    private final ThreatRepository threatRepository;
    private final DroolsThreatEngine threatEngine;
    private final AlertService alertService;

    public TelemetryService(EndpointRepository endpointRepository,
                            EventRepository eventRepository,
                            ThreatRepository threatRepository,
                            DroolsThreatEngine threatEngine,
                            AlertService alertService) {
        this.endpointRepository = endpointRepository;
        this.eventRepository = eventRepository;
        this.threatRepository = threatRepository;
        this.threatEngine = threatEngine;
        this.alertService = alertService;
    }

    @Transactional
    public void process(TelemetryPayload payload) {
        Endpoint endpoint = endpointRepository.findByAgentId(payload.getAgentId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown agent: " + payload.getAgentId()));

        endpoint.setLastSeen(OffsetDateTime.now());
        endpoint.setStatus(Endpoint.EndpointStatus.ONLINE);
        endpointRepository.save(endpoint);

        if (payload.getEvents() == null || payload.getEvents().isEmpty()) return;

        for (TelemetryPayload.AgentEvent agentEvent : payload.getEvents()) {
            Event event = new Event();
            event.setEndpoint(endpoint);
            event.setEventType(agentEvent.getType());
            event.setSeverity(mapSeverity(agentEvent.getSeverity()));
            event.setSource(agentEvent.getSource());
            event.setMessage(agentEvent.getMessage());
            event.setPayload(agentEvent.getData());
            event.setTags(agentEvent.getTags());
            event.setOccurredAt(agentEvent.getTimestamp() != null ? agentEvent.getTimestamp() : OffsetDateTime.now());
            event = eventRepository.save(event);

            ThreatFact fact = toFact(agentEvent, endpoint.getAgentId());
            List<ThreatResult> detections = threatEngine.evaluate(fact);

            for (ThreatResult detection : detections) {
                Threat threat = new Threat();
                threat.setEndpoint(endpoint);
                threat.setEvent(event);
                threat.setThreatType(detection.getThreatType());
                threat.setSeverity(Threat.Severity.valueOf(detection.getSeverity()));
                threat.setTitle(detection.getTitle());
                threat.setDescription(detection.getDescription());
                threat.setRuleName(detection.getRuleName());
                threat.setAttackVector(detection.getAttackVector());
                threat.setMitreTechnique(detection.getMitreTechnique());
                threat.setRiskScore(detection.getRiskScore());
                threat.setDetectedAt(OffsetDateTime.now());
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
        int updated = Math.min(100, Math.max(endpoint.getRiskScore(), newScore));
        if (updated != endpoint.getRiskScore()) {
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
        catch (IllegalArgumentException ex) { return Event.Severity.LOW; }
    }
}
