package dev.logMonitor.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

@Configurable
public class KafkaConfig {

    public static final String LOG_TOPIC = "log-ingestion-topic";

    @Bean
    public NewTopic logIngestionTopic() {
        return TopicBuilder.name(LOG_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
