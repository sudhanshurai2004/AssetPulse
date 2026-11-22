package com.assetpulse.api.sensor;

public interface SensorReadingIngestionService {
    void ingest(SensorReadingEvent event);

    boolean asynchronous();
}
