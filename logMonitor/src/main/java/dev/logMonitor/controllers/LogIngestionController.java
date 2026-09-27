package dev.logMonitor.controllers;

import dev.logMonitor.dto.LogEvent;
import dev.logMonitor.service.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;





//Invoke-RestMethod -Uri http://localhost:8080/logWatchman -Method Post -ContentType "application/json" -Body '{"serviceName":"payment-service","logLevel":"ERROR","message":"Database connection timeout while processing payment","stackTrace":"java.sql.SQLException: Connection timeout after 3000ms","traceId":"trace-abc-123"}'

@RestController
@RequestMapping("/logWatchman")
public class LogIngestionController {
    Logger logger = LoggerFactory.getLogger(LogIngestionController.class);
    private final KafkaProducerService kafkaProducerService;

    public LogIngestionController(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    /**
     * Ingests an incoming log payload and publishes it asynchronously to Kafka.
     * Returns 202 Accepted so client requests remain fast and non-blocking.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> ingestLog(@RequestBody LogEvent logEvent) {
        kafkaProducerService.sendLogEvent(logEvent);
        logger.info("pushed log event to kafka consumer");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "status", "ACCEPTED",
                "message", "Log event received and queued for processing",
                "serviceName", logEvent.serviceName() != null ? logEvent.serviceName() : "unknown"
        ));
    }

}
