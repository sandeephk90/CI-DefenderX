package com.cidefenderx.service;

import com.cidefenderx.model.Threat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final KafkaEventPublisher kafkaPublisher;

    public AlertService(KafkaEventPublisher kafkaPublisher) {
        this.kafkaPublisher = kafkaPublisher;
    }

    public void sendAlerts(Threat threat) {
        log.info("Sending alerts for threat: {} [{}]", threat.getTitle(), threat.getSeverity());
        kafkaPublisher.publishThreat(threat);
        if (threat.getSeverity() == Threat.Severity.CRITICAL) {
            log.error("CRITICAL THREAT: {} | endpoint={} | mitre={} | score={}",
                    threat.getTitle(),
                    threat.getEndpoint().getHostname(),
                    threat.getMitreTechnique(),
                    threat.getRiskScore());
        }
    }
}
