package com.assetpulse.api.sensor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.sensor-ingestion.mode", havingValue = "direct")
public class DirectSensorReadingIngestionService implements SensorReadingIngestionService {
    private final SensorReadingProcessor processor;

    public DirectSensorReadingIngestionService(SensorReadingProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void ingest(SensorReadingEvent event) {
        processor.process(event);
    }

    @Override
    public boolean asynchronous() {
        return false;
    }
}
