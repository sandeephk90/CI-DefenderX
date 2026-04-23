package com.cidefenderx.service;

import com.cidefenderx.model.Threat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class KafkaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    public void publishThreat(Threat threat) {
        log.debug("Kafka disabled — threat alert skipped for: {}", threat.getTitle());
    }
}
