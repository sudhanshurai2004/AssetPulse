package com.assetpulse.api.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
@ConditionalOnProperty(name = "app.sensor-ingestion.mode", havingValue = "kafka", matchIfMissing = true)
public class KafkaTopicConfig {
    @Bean
    NewTopic sensorReadingsTopic(@Value("${app.kafka.sensor-topic}") String topic) {
        return TopicBuilder.name(topic).partitions(1).replicas(1).build();
    }
}
