package dev.logMonitor.service;

import dev.logMonitor.config.KafkaConfig;
import dev.logMonitor.dto.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private final KafkaTemplate<String, LogEvent> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, LogEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes a LogEvent asynchronously to the Kafka topic.
     */
    public void sendLogEvent(LogEvent logEvent) {
        logger.info("Publishing log event from service '{}' [Level: {}] to Kafka...",
                logEvent.serviceName(), logEvent.logLevel());

        // Use serviceName as the key so logs from the same service hit the same partition
        kafkaTemplate.send(KafkaConfig.LOG_TOPIC, logEvent.serviceName(), logEvent);
    }
}
