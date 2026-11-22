package com.assetpulse.api.sensor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.sensor-ingestion.mode", havingValue = "kafka", matchIfMissing = true)
public class KafkaSensorReadingIngestionService implements SensorReadingIngestionService {
    private final KafkaTemplate<Object, Object> kafka;
    private final String topic;

    public KafkaSensorReadingIngestionService(
            KafkaTemplate<Object, Object> kafka,
            org.springframework.beans.factory.annotation.Value("${app.kafka.sensor-topic}") String topic) {
        this.kafka = kafka;
        this.topic = topic;
    }

    @Override
    public void ingest(SensorReadingEvent event) {
        kafka.send(topic, event.eventId().toString(), event).join();
    }

    @Override
    public boolean asynchronous() {
        return true;
    }
}
