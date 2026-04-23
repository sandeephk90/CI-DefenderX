package com.cidefenderx.service;

import com.cidefenderx.model.Threat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class KafkaEventPublisher {

    public void publishThreat(Threat threat) {
        // No-op when Kafka is disabled; swap with real publisher in kafka package
        log.debug("Kafka disabled — threat alert skipped for: {}", threat.getTitle());
    }
}
