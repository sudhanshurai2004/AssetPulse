package com.assetpulse.api.sensor;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SensorReadingConsumerTest {
    @Test
    void delegatesKafkaEventsToSharedProcessor() {
        SensorReadingProcessor processor = mock(SensorReadingProcessor.class);
        SensorReadingConsumer consumer = new SensorReadingConsumer(processor);
        SensorReadingEvent event =
                new SensorReadingEvent(UUID.randomUUID(), 10L, "temperature", 90, Instant.now());

        consumer.consume(event);

        verify(processor).process(event);
    }
}
