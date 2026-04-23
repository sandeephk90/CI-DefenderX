package com.cidefenderx.service;

import com.cidefenderx.model.Threat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${cidefenderx.kafka.topic.threats:cidefenderx.threats}")
    private String threatsTopic;

    public void publishThreat(Threat threat) {
        Map<String, Object> payload = Map.of(
                "threatId", threat.getId().toString(),
                "type", threat.getThreatType(),
                "severity", threat.getSeverity().name(),
                "title", threat.getTitle(),
                "endpointId", threat.getEndpoint().getId().toString(),
                "hostname", threat.getEndpoint().getHostname(),
                "mitre", threat.getMitreTechnique() != null ? threat.getMitreTechnique() : "",
                "riskScore", threat.getRiskScore(),
                "detectedAt", threat.getDetectedAt().toString()
        );

        kafkaTemplate.send(threatsTopic, threat.getId().toString(), payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to publish threat to Kafka: {}", ex.getMessage());
                    } else {
                        log.debug("Threat published to Kafka topic {}: {}", threatsTopic, threat.getId());
                    }
                });
    }
}
