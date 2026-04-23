package com.cidefenderx.service;

import com.cidefenderx.model.Threat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final KafkaEventPublisher kafkaPublisher;

    public void sendAlerts(Threat threat) {
        log.info("Sending alerts for threat: {} [{}]", threat.getTitle(), threat.getSeverity());

        // Publish to Kafka for downstream consumers (SIEM, webhooks, email workers)
        kafkaPublisher.publishThreat(threat);

        // For CRITICAL threats, log at highest priority
        if (threat.getSeverity() == Threat.Severity.CRITICAL) {
            log.error("CRITICAL THREAT: {} | endpoint={} | mitre={} | score={}",
                    threat.getTitle(),
                    threat.getEndpoint().getHostname(),
                    threat.getMitreTechnique(),
                    threat.getRiskScore());
        }
    }
}
