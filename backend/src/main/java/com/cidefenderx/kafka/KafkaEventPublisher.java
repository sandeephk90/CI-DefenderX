package com.cidefenderx.kafka;

import com.cidefenderx.model.Threat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class KafkaEventPublisher extends com.cidefenderx.service.KafkaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${cidefenderx.kafka.topic.threats:cidefenderx.threats}")
    private String threatsTopic;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
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
