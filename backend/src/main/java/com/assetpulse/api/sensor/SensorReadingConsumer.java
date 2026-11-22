package com.assetpulse.api.sensor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.sensor-ingestion.mode", havingValue = "kafka", matchIfMissing = true)
public class SensorReadingConsumer {
    private final SensorReadingProcessor processor;

    public SensorReadingConsumer(SensorReadingProcessor processor) {
        this.processor = processor;
    }

    @KafkaListener(topics = "${app.kafka.sensor-topic}")
    @Transactional
    public void consume(SensorReadingEvent event) {
        processor.process(event);
    }
}
