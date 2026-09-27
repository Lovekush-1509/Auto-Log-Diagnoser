package dev.logMonitor.service;

import dev.logMonitor.dto.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final VectorStore vectorStore;
    private final SseNotificationService sseNotificationService;
    private final StringRedisTemplate redisTemplate;
    private final ErrorSpikeService errorSpikeService;
    private final LogAnalysisService logAnalysisService;

    public KafkaConsumerService(VectorStore vectorStore, StringRedisTemplate redisTemplate,
                                ErrorSpikeService errorSpikeService, LogAnalysisService logAnalysisService,
                                SseNotificationService sseNotificationService) {
        this.vectorStore = vectorStore;
        this.redisTemplate = redisTemplate;
        this.errorSpikeService = errorSpikeService;
        this.logAnalysisService = logAnalysisService;
        this.sseNotificationService = sseNotificationService;
    }

    @KafkaListener(topics = "${app.kafka.topic:log-ingestion-topic}", groupId = "${spring.kafka.consumer.group-id:log-monitor-group}")
    public void consumeLogEvent(LogEvent logEvent) {
        logger.info("Received log event from Kafka for service: [{}]", logEvent.getServiceName());

        try {
            // 1. Prepare structured text payload for vector embedding
            String contentToEmbed = String.format(
                    "Service: %s | Level: %s | Message: %s | StackTrace: %s",
                    logEvent.getServiceName(),
                    logEvent.getLogLevel(),
                    logEvent.getMessage(),
                    logEvent.getStackTrace() != null ? logEvent.getStackTrace() : "N/A"
            );

            // 2. Metadata tags for vector search filtering
            Map<String, Object> metadata = Map.of(
                    "serviceName", logEvent.getServiceName(),
                    "logLevel", logEvent.getLogLevel(),
                    "traceId", logEvent.getTraceId() != null ? logEvent.getTraceId() : "N/A"
            );

            // 3. Generate vector via Ollama (nomic-embed-text) and persist to pgvector
            Document document = new Document(contentToEmbed, metadata);
            vectorStore.add(List.of(document));
            logger.info("Successfully stored embedding in pgvector for traceId: {}", logEvent.getTraceId());

            sseNotificationService.broadcast("LOG_EVENT", logEvent);

            if ("ERROR".equalsIgnoreCase(logEvent.getLogLevel())) {
                if (errorSpikeService.isWithinRateLimit(logEvent.serviceName())) {
                    logger.info("Triggering automatic AI RCA for error in service: {}", logEvent.getServiceName());
                    String aiAnalysis = logAnalysisService.analyzeErrorAndSuggestFix(
                            logEvent.message(),
                            logEvent.stackTrace()
                    );
                    logger.info("\n=== AUTOMATED AI RCA RESULT ===\n{}\n================================", aiAnalysis);
                    sseNotificationService.broadcast("AI_ANALYSIS", aiAnalysis);
                } else {
                    logger.info("Skipped AI analysis for [{}] due to active error spike protection.", logEvent.getServiceName());
                }
            }

            // 4. Cache trace key in Redis (expires in 1 hour)
            if (logEvent.getTraceId() != null) {
                String redisKey = "recent_log:" + logEvent.getTraceId();
                redisTemplate.opsForValue().set(redisKey, logEvent.getLogLevel(), Duration.ofHours(1));
            }

        } catch (Exception e) {
            logger.error("Failed to process log event for traceId [{}]: {}", logEvent.getTraceId(), e.getMessage(), e);
        }
    }
}
