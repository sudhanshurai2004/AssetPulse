package com.assetpulse.api.sensor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DirectSensorReadingIngestionServiceTest {
    @Test
    void processesReadingSynchronouslyWithoutKafka() {
        SensorReadingProcessor processor = mock(SensorReadingProcessor.class);
        DirectSensorReadingIngestionService ingestion = new DirectSensorReadingIngestionService(processor);
        SensorReadingEvent event =
                new SensorReadingEvent(UUID.randomUUID(), 10L, "temperature", 90, Instant.now());

        ingestion.ingest(event);

        verify(processor).process(event);
        assertThat(ingestion.asynchronous()).isFalse();
    }
}
